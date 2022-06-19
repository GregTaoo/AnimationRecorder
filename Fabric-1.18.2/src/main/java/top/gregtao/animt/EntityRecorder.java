package top.gregtao.animt;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3f;
import top.gregtao.animt.util.FileHelper;
import top.gregtao.animt.util.Gif;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

public class EntityRecorder {
    public static MinecraftClient CLIENT = MinecraftClient.getInstance();
    public static EntityRecorder CURRENT;

    public boolean trans = true, multiThreads = true, started = false, forceEnd = false;
    public int timer = 0, maxFrames = 120, gifSize = 128;
    public float scale = 1;
    public int itemDeltaX, itemDeltaY;
    public LivingEntity target;
    public PlayerEntity player;

    public int realWidth;
    public HitBoxHelper hitBoxHelper;

    public BufferedImage[] images;
    public OutputGifThread thread;

    public static File config = new File(FileHelper.filePath + "/config.properties");

    public void resetParam() {
        this.scale = 1;
        this.trans = this.multiThreads = true;
        this.maxFrames = 120;
        this.gifSize = 128;
        this.itemDeltaX = this.itemDeltaY = 0;
    }

    public void writeConfig() throws Exception {
        FileHelper.existOrCreateWithParent(config);
        Properties properties = new Properties();
        properties.put("gifSize", String.valueOf(this.gifSize));
        properties.put("itemDeltaX", String.valueOf(this.itemDeltaX));
        properties.put("itemDeltaY", String.valueOf(this.itemDeltaY));
        properties.put("maxFrames", String.valueOf(this.maxFrames));
        properties.put("multiThreads", String.valueOf(this.multiThreads ? 1 : 0));
        properties.put("trans", String.valueOf(this.trans ? 1 : 0));
        properties.store(new FileWriter(config), FileHelper.getCurrentTime() + " ANIMATION RECORDER");
        AnimationRecorder.LOGGER.info("Wrote configs into file: " + config);
    }

    public boolean loadConfig() throws Exception {
        if (!config.exists()) {
            AnimationRecorder.LOGGER.warn("Config file: " + config + " NOT FOUND");
            return false;
        }
        Properties properties = new Properties();
        properties.load(new FileReader(config));
        this.gifSize = FileHelper.getIntegerFromProp(properties, "gifSize", "128");
        this.itemDeltaX = FileHelper.getIntegerFromProp(properties, "itemDeltaX", "0");
        this.itemDeltaY = FileHelper.getIntegerFromProp(properties, "itemDeltaY", "0");
        this.maxFrames = FileHelper.getIntegerFromProp(properties, "maxFrames", "120");
        this.multiThreads = FileHelper.getIntegerFromProp(properties, "multiThreads", "1") > 0;
        this.trans = FileHelper.getIntegerFromProp(properties, "trans", "1") > 0;
        AnimationRecorder.LOGGER.info("Loaded configs from file: " + config);
        return true;
    }

    public void startRecording(PlayerEntity player, MinecraftClient client) {
        if (this.started) {
            this.forceEnd = true;
            return;
        }

        if (client.targetedEntity instanceof LivingEntity entity) {
            this.target = entity;
            this.player = player;
            this.images = new BufferedImage[this.maxFrames + 1];
            Box box = entity.getVisibilityBoundingBox();
            double x = box.getXLength(), z = box.getZLength();
            this.realWidth = (int) ((x + z) / Math.sqrt(2));
            this.started = true;
            this.execHitBoxHelper();
            player.sendMessage(new TranslatableText("animt.record_started", entity.getDisplayName().getString()), false);
            AnimationRecorder.LOGGER.info("Try to record mob: " + this.target.getDisplayName().getString());
            return;
        }
        player.sendMessage(new TranslatableText("animt.no_target"), false);
    }

    public void endRecording() {
        this.thread = new OutputGifThread();
        if (this.multiThreads) this.thread.start();
        else this.thread.run();
        this.started = false;
        this.forceEnd = false;
        this.player.sendMessage(new TranslatableText("animt.end_recording", this.timer), false);
        this.timer = 0;
        AnimationRecorder.LOGGER.info("Finish recording mob: " + this.timer);
    }

    public void execHitBoxHelper() {
        if (this.target != null) {
            Box box = this.target.getBoundingBox();
            this.hitBoxHelper = new HitBoxHelper(box.getXLength(), box.getYLength(), box.getZLength(), Math.PI / 4, 0, 0);
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
        if (recorder.timer >= recorder.maxFrames || recorder.forceEnd) {
            recorder.endRecording();
            return;
        }

        //预处理
        RenderSystem.clearColor(1, 1, 1, 0);
        Framebuffer framebuffer = new SimpleFramebuffer(scaledWidth, scaledHeight, true, MinecraftClient.IS_SYSTEM_MAC);
        framebuffer.beginWrite(true);
        RenderSystem.disableBlend();

        //旋转模型
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(45f));
        matrixStack.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(22.5f));
        matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(22.5f));

        //渲染并保存
        int entitySz = (int) (80 * recorder.scale / Math.max(recorder.hitBoxHelper.width, recorder.hitBoxHelper.height));
        InventoryScreen.drawEntity(
                sz + recorder.itemDeltaX - 27,
                sz / 2 + recorder.itemDeltaY,
                entitySz, 0, 0,
                recorder.target);
        framebuffer.endWrite();
        NativeImage image = new NativeImage(scaledWidth, scaledHeight, false);
        RenderSystem.bindTexture(framebuffer.getColorAttachment());
        image.loadFromTextureImage(0, !recorder.trans);
        image.mirrorVertically();

        try {
            recorder.images[recorder.timer] = ImageIO.read(new ByteArrayInputStream(image.getBytes())).getSubimage(0, 0, sz, sz);
            recorder.timer += 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
class OutputGifThread implements Runnable {
    private static Thread thread;

    @Override
    public void run() {
        EntityRecorder recorder = EntityRecorder.CURRENT;
        String fileName = recorder.target.getDisplayName().getString() + "_" + FileHelper.getCurrentTime() + ".gif";
        recorder.player.sendMessage(new TranslatableText("animt.thread_started", fileName).formatted(Formatting.DARK_AQUA), false);
        String fullPath = FileHelper.filePath + "/" + fileName;
        Gif.convert(recorder.images, fullPath, 5, true, recorder.gifSize, recorder.gifSize);
        recorder.target = null;
        recorder.player.sendMessage(new TranslatableText("animt.thread_finished", FileHelper.getOpenFileLnk(new File(fullPath))).formatted(Formatting.GREEN), false);
    }

    public void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this, "OutputGif Thread");
            thread.start();
        }
    }
}