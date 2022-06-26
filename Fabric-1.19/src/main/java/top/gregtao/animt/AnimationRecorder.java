package top.gregtao.animt;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.gregtao.animt.util.FileHelper;

public class AnimationRecorder implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("AnimationRecorder");
	public static KeyBinding recordMobKey;
	public static KeyBinding bindMobKey;

	public static PlayerEntity getPlayerFromCmdCtt(CommandContext<FabricClientCommandSource> ctt) {
		return ctt.getSource().getPlayer();
	}

	@Override
	public void onInitializeClient() {
		EntityRecorder.CURRENT = new EntityRecorder();
		try {
			EntityRecorder.CURRENT.config.loadConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			EntityRecorder recorder = EntityRecorder.CURRENT;
			dispatcher.register(ClientCommandManager.literal("recorder")
					.then(ClientCommandManager.literal("saveConfig").executes(context -> {
						try {
							recorder.config.writeConfig();
						} catch (Exception e) {
							e.printStackTrace();
						}
						getPlayerFromCmdCtt(context).sendMessage(Text.translatable(
								"animt.cmd.save_config", FileHelper.getOpenFileLnk(recorder.config.file)));
						return 1;
					}))
					.then(ClientCommandManager.literal("loadConfig").executes(context -> {
						try {
							getPlayerFromCmdCtt(context).sendMessage(Text.translatable(
									recorder.config.loadConfig() ? "animt.cmd.load_config" : "animt.cmd.load_config.failed",
									FileHelper.getOpenFileLnk(recorder.config.file)));
						} catch (Exception e) {
							e.printStackTrace();
						}
						return 1;
					}))
					.then(ClientCommandManager.literal("maxFrames")
							.then(ClientCommandManager.argument("frames", IntegerArgumentType.integer(1, 10000))
							.executes(context -> {
								recorder.config.maxFrames = IntegerArgumentType.getInteger(context, "frames");
								getPlayerFromCmdCtt(context).sendMessage(Text.translatable("animt.cmd.frames", recorder.config.maxFrames));
								return 1;
							})
					))
					.then(ClientCommandManager.literal("scale")
							.then(ClientCommandManager.argument("scale", FloatArgumentType.floatArg(0.01f, 1000f))
							.executes(context -> {
								recorder.config.scale = FloatArgumentType.getFloat(context, "scale");
								getPlayerFromCmdCtt(context).sendMessage(Text.translatable("animt.cmd.scale", recorder.config.scale));
								return 1;
							})
					))
					.then(ClientCommandManager.literal("gifSize")
							.then(ClientCommandManager.argument("size", IntegerArgumentType.integer(1, 10000))
							.executes(context -> {
								recorder.config.gifSize = IntegerArgumentType.getInteger(context, "size");
								getPlayerFromCmdCtt(context).sendMessage(Text.translatable("animt.cmd.gif_size", recorder.config.gifSize, recorder.config.gifSize));
								return 1;
							})
					))
					.then(ClientCommandManager.literal("trans")
							.then(ClientCommandManager.argument("boolean", IntegerArgumentType.integer(0, 1))
							.executes(context -> {
								recorder.config.trans = IntegerArgumentType.getInteger(context, "boolean") == 1;
								getPlayerFromCmdCtt(context).sendMessage(Text.translatable("animt.cmd.trans", recorder.config.trans));
								return 1;
							})
					))
					.then(ClientCommandManager.literal("multiThreads")
							.then(ClientCommandManager.argument("boolean", IntegerArgumentType.integer(0, 1))
							.executes(context -> {
								recorder.config.multiThreads = IntegerArgumentType.getInteger(context, "boolean") == 1;
								getPlayerFromCmdCtt(context).sendMessage(Text.translatable(
										recorder.config.multiThreads ? "animt.cmd.multi_threads.on" : "animt.cmd.multi_threads.off"));
								return 1;
							})
					))
					.then(ClientCommandManager.literal("itemDelta")
							.then(ClientCommandManager.argument("x", IntegerArgumentType.integer(-1000, 1000))
							.then(ClientCommandManager.argument("y", IntegerArgumentType.integer(-1000, 1000))
									.executes(context -> {
										recorder.config.itemDeltaX = IntegerArgumentType.getInteger(context, "x");
										recorder.config.itemDeltaY = IntegerArgumentType.getInteger(context, "y");
										getPlayerFromCmdCtt(context).sendMessage(Text.translatable(
												"animt.cmd.item_delta", recorder.config.itemDeltaX, recorder.config.itemDeltaY));
										return 1;
									})
							)))

					.then(ClientCommandManager.literal("rotate")
							.then(ClientCommandManager.argument("x", FloatArgumentType.floatArg(-360, 360))
									.then(ClientCommandManager.argument("y", FloatArgumentType.floatArg(-360, 360))
											.then(ClientCommandManager.argument("z", FloatArgumentType.floatArg(-360, 360))
											.executes(context -> {
												recorder.config.rotateX = FloatArgumentType.getFloat(context, "x");
												recorder.config.rotateY = FloatArgumentType.getFloat(context, "y");
												recorder.config.rotateZ = FloatArgumentType.getFloat(context, "z");
												getPlayerFromCmdCtt(context).sendMessage(Text.translatable(
														"animt.cmd.rotate", recorder.config.rotateX, recorder.config.rotateY, recorder.config.rotateZ));
												return 1;
											})
									))))
					.then(ClientCommandManager.literal("reset").executes(context -> {
						recorder.config.resetToDefault();
						getPlayerFromCmdCtt(context).sendMessage(Text.translatable("animt.cmd.reset"));
						return 1;
					})));
		});

		HudRenderCallback.EVENT.register(EntityRecorder::rendererTick);
		recordMobKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"animt.key.record",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_U,
				"animt.key"
		));
		bindMobKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"animt.key.bind",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_Y,
				"animt.key"
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (bindMobKey.wasPressed()) {
				EntityRecorder.CURRENT.bindTarget(client, Screen.hasControlDown() ? client.player : EntityRecorder.getTargetedEntity(client));
			}
			if (recordMobKey.wasPressed()) {
				EntityRecorder.CURRENT.startRecording(client.player);
			}
		});
	}
}
