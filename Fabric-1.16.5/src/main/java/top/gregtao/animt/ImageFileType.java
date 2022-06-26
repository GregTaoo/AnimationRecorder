package top.gregtao.animt;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public enum ImageFileType {
    GIF("gif"),
    JPG("jpg"),
    PNG("png"),
    TIFF("tiff")
    ;

    public String suffix;

    ImageFileType(String suffix) {
        this.suffix = suffix;
    }

    public static ImageFileType getBySuffix(String suffix) {
        for (ImageFileType type : ImageFileType.values()) {
            if (type.suffix.equals(suffix)) return type;
        }
        return ImageFileType.GIF;
    }

    public Text getTextName() {
        return Text.of(this.suffix);
    }

    public static ImageFileType getByIndex(int index) {
        ImageFileType[] values = ImageFileType.values();
        int len = values.length;
        for (ImageFileType type : values) {
            if (type.ordinal() % (len - 1) == index) return type;
        }
        return ImageFileType.GIF;
    }

    public Text getMessage() {
        return Text.of((new TranslatableText("animt.button.types")).getString() + ": " + this.suffix);
    }
}
