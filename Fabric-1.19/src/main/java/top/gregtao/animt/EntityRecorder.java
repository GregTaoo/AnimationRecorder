package top.gregtao.animt;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3f;
import top.gregtao.animt.util.FileHelper;
import top.gregtao.animt.util.Gif;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

public class EntityRecorder {
    public static MinecraftClient CLIENT = MinecraftClient.getInstance();
    public static EntityRecorder CURRENT;

    public boolean started = false, forceEnd = false;
    public int timer = 0;
    public LivingEntity target;
    public PlayerEntity player;

    public HitBoxHelper hitBoxHelper;

    public BufferedImage[] images;
    public OutputImgThread thread;

    public RecorderConfig config = new RecorderConfig();

    public EntityRecorder() {
        this.config.file = new File(FileHelper.filePath + "/config.properties");
    }

    public static LivingEntity getTargetedEntity(MinecraftClient client) {
        if (client.targetedEntity instanceof LivingEntity entity) {
            return entity;
        }
        return null;
    }

    public void startRecording(PlayerEntity player) {
        if (this.started) {
            this.forceEnd = true;
            return;
        }
        if (this.target != null && !this.target.isDead()) {
            this.player = player;
            this.recorderPreset();
            this.started = true;
            return;
        }
        player.sendMessage(Text.translatable("animt.no_target"));
    }

    public void bindTarget(MinecraftClient client, LivingEntity tg) {
        PlayerEntity player = client.player;
        if (player == null) return;
        if (tg == null) {
            player.sendMessage(Text.translatable("animt.no_target"));
        } else {
            this.target = tg;
            player.sendMessage(Text.translatable("animt.bind_target", tg.getDisplayName().getString()));
        }
    }

    public void recorderPreset() {
        this.images = new BufferedImage[this.config.type == ImageFileType.GIF ? this.config.maxFrames + 1 : 1];
        this.execHitBoxHelper();
        this.player.sendMessage(Text.translatable("animt.record_started", this.target.getDisplayName().getString()));
        AnimationRecorder.LOGGER.info("Try to record mob: " + this.target.getDisplayName().getString());
    }

    public void endRecording() {
        this.thread = new OutputImgThread();
        if (this.config.multiThreads) this.thread.start();
        else this.thread.run();
        this.started = false;
        this.forceEnd = false;
        this.player.sendMessage(Text.translatable("animt.end_recording", this.timer));
        this.timer = 0;
        AnimationRecorder.LOGGER.info("Finish recording mob: " + this.timer);
    }

    public void execHitBoxHelper() {
        if (this.target != null) {
            this.hitBoxHelper = HitBoxHelper.getFromEntity(this.target);
        }
    }

    public static void rendererTick(MatrixStack matrices, float tickDelta) {
        EntityRecorder recorder = EntityRecorder.CURRENT;
        if (!recorder.started || recorder.target == null) return;
        MinecraftClient client = EntityRecorder.CLIENT;
        if (client.player == null) return;
        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        int sz = Math.min(scaledHeight, scaledWidth);
        if ((recorder.config.type != ImageFileType.GIF && recorder.timer == 1) ||
                (recorder.config.type == ImageFileType.GIF && (recorder.timer >= recorder.config.maxFrames || recorder.forceEnd))) {
            recorder.endRecording();
            return;
        }

        NativeImage image = renderToImage(sz, scaledWidth, scaledHeight, recorder.hitBoxHelper, recorder.target, recorder.config, matrices);

        try {
            recorder.images[recorder.timer] = getSubImageBuffered(image, sz);
            recorder.timer += 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage getSubImageBuffered(NativeImage image, int sz) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(image.getBytes())).getSubimage(0, 0, sz, sz);
    }

    public static NativeImage renderToImage(int sz, int width, int height, HitBoxHelper hitBoxHelper, LivingEntity target, RecorderConfig config, MatrixStack matrices) {
        //预处理
        RenderSystem.clearColor(1, 1, 1, 0);
        Framebuffer framebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
        framebuffer.beginWrite(true);
        RenderSystem.disableBlend();
        if (!config.trans) {
            DrawableHelper.fill(matrices, 0, 0, sz, sz, (int) config.bgColor);
        }

        //旋转模型
        rotateModelStack(config.rotateX, config.rotateY, config.rotateZ);

        //渲染并保存
        int entitySz = (int) (80 * config.scale / Math.max(hitBoxHelper.width, hitBoxHelper.height));
        InventoryScreen.drawEntity(
                sz + config.itemDeltaX - 27,
                sz / 2 + config.itemDeltaY - 14,
                entitySz, 0, 0,
                target);
        framebuffer.endWrite();
        NativeImage image = new NativeImage(width, height, false);
        RenderSystem.bindTexture(framebuffer.getColorAttachment());
        image.loadFromTextureImage(0, !config.trans);
        image.mirrorVertically();

        return image;
    }

    public static void rotateModelStack(float x, float y, float z) {
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.translate(0, 0, 500);
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(y));
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(x));
        matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(z));
    }

    public static String getOutputFileName(LivingEntity target, ImageFileType type) {
        return target.getDisplayName().getString() + "_" + FileHelper.getCurrentTime() + "." + type.suffix;
    }
}
class OutputImgThread implements Runnable {
    private static Thread thread;

    @Override
    public void run() {
        EntityRecorder recorder = EntityRecorder.CURRENT;
        String fileName = EntityRecorder.getOutputFileName(recorder.target, recorder.config.type);
        recorder.player.sendMessage(Text.translatable("animt.thread_started", fileName).formatted(Formatting.DARK_AQUA));
        String fullPath = FileHelper.filePath + "/" + fileName;
        Gif.convert(recorder.images, fullPath, 5, true, recorder.config.gifSize, recorder.config.gifSize);
        recorder.player.sendMessage(Text.translatable("animt.thread_finished", FileHelper.getOpenFileLnk(new File(fullPath))).formatted(Formatting.GREEN));
    }

    public void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this, "OutputImg Thread");
            thread.start();
        }
    }
}