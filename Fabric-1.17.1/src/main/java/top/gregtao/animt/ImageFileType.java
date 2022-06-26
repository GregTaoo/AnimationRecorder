package top.gregtao.animt;

import net.minecraft.text.Text;

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
}
