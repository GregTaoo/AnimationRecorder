package top.gregtao.animt.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import top.gregtao.animt.util.FileHelper;

import java.util.function.Consumer;

public class ColorFieldWidget {
    private final IntNumberFieldWidget RField;
    private final IntNumberFieldWidget GField;
    private final IntNumberFieldWidget BField;

    private final int x, y;

    public ColorFieldWidget(int x, int y, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;
        this.RField = new IntNumberFieldWidget(
                textRenderer, this.x + 10, this.y + 16, 40, 12,
                255, 1, 0, 255);

        this.GField = new IntNumberFieldWidget(
                textRenderer, this.x + 10, this.y + 32, 40, 12,
                255, 1, 0, 255);

        this.BField = new IntNumberFieldWidget(
                textRenderer, this.x + 10, this.y + 48, 40, 12,
                255, 1, 0, 255);
    }

    public void setFromColor(long color) {
        this.RField.setNumber((int) ((color >> 16) & 0xff));
        this.GField.setNumber((int) ((color >> 8) & 0xff));
        this.BField.setNumber((int) (color & 0xff));
    }

    public String toHexString() {
        return FileHelper.rgb2Hex(0xff, this.RField.getNumber(), this.GField.getNumber(), this.BField.getNumber());
    }

    public long toNumber() {
        return FileHelper.parseLong(this.toHexString(), 16, 0xffffffff);
    }

    public void forEach(Consumer<IntNumberFieldWidget> lambda) {
        lambda.accept(this.RField);
        lambda.accept(this.GField);
        lambda.accept(this.BField);
    }

    public void tick() {
        this.RField.tick();
        this.GField.tick();
        this.BField.tick();
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, TextRenderer textRenderer) {
        this.RField.render(matrices, mouseX, mouseY, delta);
        this.GField.render(matrices, mouseX, mouseY, delta);
        this.BField.render(matrices, mouseX, mouseY, delta);
        textRenderer.draw(matrices, "Color", this.x + 10, this.y, 0xffffff);
        textRenderer.draw(matrices, "R", this.x, this.y + 18, 0xDC143C);
        textRenderer.draw(matrices, "G", this.x, this.y + 34, 0x3CB371);
        textRenderer.draw(matrices, "B", this.x, this.y + 50, 0x0000CD);
    }
}
