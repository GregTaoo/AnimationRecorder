package top.gregtao.animt.util;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

public class FileHelper {
    public static String filePath = "AnimationRecorder";

    public static boolean existOrCreateWithParent(File file) throws IOException {
        if (!file.exists()) {
            if (file.getParentFile().exists() || file.getParentFile().mkdir()) {
                return file.createNewFile();
            }
            return false;
        } else {
            return true;
        }
    }

    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return formatter.format(calendar.getTime());
    }

    public static int getIntegerFromProp(Properties properties, String key, String def) {
        return Integer.parseInt(properties.getProperty(key, def));
    }

    public static MutableText getOpenFileLnk(File file) {
        return new LiteralText(file.toString()).formatted(Formatting.UNDERLINE)
                .styled(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
    }
}
