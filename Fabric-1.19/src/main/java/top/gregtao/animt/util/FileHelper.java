package top.gregtao.animt.util;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
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

    public static float parseFloat(String num, float orElse) {
        try {
            return Float.parseFloat(num);
        } catch (Exception e) {
            return orElse;
        }
    }

    public static int parseInt(String num, int orElse) {
        try {
            return Integer.parseInt(num);
        } catch (Exception e) {
            return orElse;
        }
    }

    public static int parseInt(String num, int radix, int orElse) {
        try {
            return Integer.parseInt(num, radix);
        } catch (Exception e) {
            return orElse;
        }
    }

    public static long parseLong(String num, int radix, long orElse) {
        try {
            return Long.parseLong(num, radix);
        } catch (Exception e) {
            return orElse;
        }
    }

    public static int getIntegerFromProp(Properties properties, String key, int def) {
        return parseInt(properties.getProperty(key, String.valueOf(def)), def);
    }

    public static float getFloatFromProp(Properties properties, String key, float def) {
        return parseFloat(properties.getProperty(key, String.valueOf(def)), def);
    }

    public static MutableText getOpenFileLnk(File file) {
        return Text.literal(file.toString()).formatted(Formatting.UNDERLINE)
                .styled(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
    }

    public static float round(float num, int p) {
        int x = (int) Math.pow(10, p);
        return (float) (Math.round(num * x)) / x;
    }

    public static String rgb2Hex(int a, int r, int g, int b){
        return String.format("%02X%02X%02X%02X", a, r, g, b);
    }
}
