package top.gregtao.animt;

import top.gregtao.animt.util.FileHelper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

public class RecorderConfig {
    public boolean trans = true, multiThreads = true;
    public int maxFrames = 120, gifSize = 128;
    public float scale = 1, rotateX = -22.5f, rotateY = -45f, rotateZ = 22.5f;
    public int itemDeltaX, itemDeltaY;
    public long bgColor = 0xffffffff;
    public ImageFileType type = ImageFileType.GIF;

    public File file;

    public void resetToDefault() {
        this.scale = 1;
        this.trans = this.multiThreads = true;
        this.maxFrames = 120;
        this.gifSize = 128;
        this.itemDeltaX = this.itemDeltaY = 0;
        this.bgColor = 0xffffffff;
        this.type = ImageFileType.GIF;
        this.rotateX = -22.5f;
        this.rotateY = -45f;
        this.rotateZ = 22.5f;
    }

    public void writeConfig() throws Exception {
        FileHelper.existOrCreateWithParent(this.file);
        Properties properties = new Properties();
        properties.put("scale", String.valueOf(this.scale));
        properties.put("gifSize", String.valueOf(this.gifSize));
        properties.put("itemDeltaX", String.valueOf(this.itemDeltaX));
        properties.put("itemDeltaY", String.valueOf(this.itemDeltaY));
        properties.put("rotateX", String.valueOf(this.rotateX));
        properties.put("rotateY", String.valueOf(this.rotateY));
        properties.put("rotateZ", String.valueOf(this.rotateZ));
        properties.put("maxFrames", String.valueOf(this.maxFrames));
        properties.put("multiThreads", String.valueOf(this.multiThreads ? 1 : 0));
        properties.put("trans", String.valueOf(this.trans ? 1 : 0));
        properties.put("bgColor", Long.toHexString(this.bgColor));
        properties.put("type", this.type.suffix);
        properties.store(new FileWriter(this.file), FileHelper.getCurrentTime() + " ANIMATION RECORDER");
        AnimationRecorder.LOGGER.info("Wrote configs into file: " + this.file);
    }

    public boolean loadConfig() throws Exception {
        if (!this.file.exists()) {
            AnimationRecorder.LOGGER.warn("Config file: " + this.file + " NOT FOUND");
            return false;
        }
        Properties properties = new Properties();
        properties.load(new FileReader(this.file));
        this.scale = FileHelper.getFloatFromProp(properties, "scale", 1);
        this.gifSize = FileHelper.getIntegerFromProp(properties, "gifSize", 128);
        this.itemDeltaX = FileHelper.getIntegerFromProp(properties, "itemDeltaX", 0);
        this.itemDeltaY = FileHelper.getIntegerFromProp(properties, "itemDeltaY", 0);
        this.rotateX = FileHelper.getFloatFromProp(properties, "rotateX", 0);
        this.rotateY = FileHelper.getFloatFromProp(properties, "rotateY", 0);
        this.rotateZ = FileHelper.getFloatFromProp(properties, "rotateZ", 0);
        this.maxFrames = FileHelper.getIntegerFromProp(properties, "maxFrames", 120);
        this.multiThreads = FileHelper.getIntegerFromProp(properties, "multiThreads", 1) > 0;
        this.trans = FileHelper.getIntegerFromProp(properties, "trans", 1) > 0;
        this.bgColor = FileHelper.parseInt(properties.getProperty("bgColor"), 16, 0xffffffff);
        this.type = ImageFileType.getBySuffix(properties.getProperty("type", "gif"));
        AnimationRecorder.LOGGER.info("Loaded configs from file: " + this.file);
        return true;
    }
}
