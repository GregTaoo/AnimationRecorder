package top.gregtao.animt;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.gregtao.animt.util.FileHelper;

public class AnimationRecorder implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("AnimationRecorder");
	public static KeyBinding recordMobKey;

	public static PlayerEntity getPlayerFromCmdCtt(CommandContext<FabricClientCommandSource> ctt) {
		return ctt.getSource().getPlayer();
	}

	@Override
	public void onInitialize() {
		EntityRecorder.CURRENT = new EntityRecorder();
		try {
			EntityRecorder.CURRENT.loadConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			EntityRecorder recorder = EntityRecorder.CURRENT;
			dispatcher.register(ClientCommandManager.literal("recorder")
					.then(ClientCommandManager.literal("saveConfig").executes(context -> {
						try {
							recorder.writeConfig();
						} catch (Exception e) {
							e.printStackTrace();
						}
						getPlayerFromCmdCtt(context).sendMessage(Text.translatable("animt.cmd.save_config", FileHelper.getOpenFileLnk(EntityRecorder.config)));
						return 1;
					}))
					.then(ClientCommandManager.literal("loadConfig").executes(context -> {
						try {
							getPlayerFromCmdCtt(context).sendMessage(Text.translatable(recorder.loadConfig() ? "animt.cmd.load_config" : "animt.cmd.load_config.failed", FileHelper.getOpenFileLnk(EntityRecorder.config)));
						} catch (Exception e) {
							e.printStackTrace();
						}
						return 1;
					}))
					.then(ClientCommandManager.literal("maxFrames")
							.then(ClientCommandManager.argument("frames", IntegerArgumentType.integer(1, 10000))
							.executes(context -> {
								recorder.maxFrames = IntegerArgumentType.getInteger(context, "frames");
								getPlayerFromCmdCtt(context).sendMessage(Text.translatable("animt.cmd.frames", recorder.maxFrames));
								return 1;
							})
					))
					.then(ClientCommandManager.literal("scale")
							.then(ClientCommandManager.argument("scale", FloatArgumentType.floatArg(0.01f, 1000f))
							.executes(context -> {
								recorder.scale = FloatArgumentType.getFloat(context, "scale");
								getPlayerFromCmdCtt(context).sendMessage(Text.translatable("animt.cmd.scale", recorder.scale));
								return 1;
							})
					))
					.then(ClientCommandManager.literal("gifSize")
							.then(ClientCommandManager.argument("size", IntegerArgumentType.integer(1, 10000))
							.executes(context -> {
								recorder.gifSize = IntegerArgumentType.getInteger(context, "size");
								getPlayerFromCmdCtt(context).sendMessage(Text.translatable("animt.cmd.gif_size", recorder.gifSize, recorder.gifSize));
								return 1;
							})
					))
					.then(ClientCommandManager.literal("trans")
							.then(ClientCommandManager.argument("boolean", IntegerArgumentType.integer(0, 1))
							.executes(context -> {
								recorder.trans = IntegerArgumentType.getInteger(context, "boolean") == 1;
								getPlayerFromCmdCtt(context).sendMessage(Text.translatable("animt.cmd.trans", recorder.trans));
								return 1;
							})
					))
					.then(ClientCommandManager.literal("multiThreads")
							.then(ClientCommandManager.argument("boolean", IntegerArgumentType.integer(0, 1))
							.executes(context -> {
								recorder.multiThreads = IntegerArgumentType.getInteger(context, "boolean") == 1;
								getPlayerFromCmdCtt(context).sendMessage(Text.translatable(recorder.multiThreads ? "animt.cmd.multi_threads.on" : "animt.cmd.multi_threads.off"));
								return 1;
							})
					))
					.then(ClientCommandManager.literal("itemDelta")
							.then(ClientCommandManager.argument("x", IntegerArgumentType.integer(-64, 64))
							.then(ClientCommandManager.argument("y", IntegerArgumentType.integer(-66, 66))
									.executes(context -> {
										recorder.itemDeltaX = IntegerArgumentType.getInteger(context, "x");
										recorder.itemDeltaY = IntegerArgumentType.getInteger(context, "y");
										getPlayerFromCmdCtt(context).sendMessage(Text.translatable("animt.cmd.item_delta", recorder.itemDeltaX, recorder.itemDeltaY));
										return 1;
									})
							)))
					.then(ClientCommandManager.literal("reset").executes(context -> {
						recorder.resetParam();
						getPlayerFromCmdCtt(context).sendMessage(Text.translatable("animt.cmd.reset"));
						return 1;
					})));
		});

		HudRenderCallback.EVENT.register(EntityRecorder::rendererTick);
		recordMobKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"animt.key.y",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_Y,
				"animt.key.y"
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (recordMobKey.wasPressed()) {
				EntityRecorder.CURRENT.startRecording(client.player, client);
			}
		});
	}
}