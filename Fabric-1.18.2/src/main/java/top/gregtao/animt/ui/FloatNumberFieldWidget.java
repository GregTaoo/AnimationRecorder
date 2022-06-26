package top.gregtao.animt.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import top.gregtao.animt.util.FileHelper;

public class FloatNumberFieldWidget extends TextFieldWidget {
    private final float def, delta, min, max;
    
    public FloatNumberFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, float def, float delta, float min, float max) {
        super(textRenderer, x, y, width, height, Text.of(""));
        this.setFocusUnlocked(true);
        this.setEditableColor(-1);
        this.setUneditableColor(-1);
        this.setDrawsBackground(true);
        this.setMaxLength(50);
        this.setText(String.valueOf(def));
        this.def = def;
        this.delta = delta;
        this.min = min;
        this.max = max;
    }
    
    public float getNumber() {
        return FileHelper.parseFloat(this.getText(), this.def);
    }

    public void addNumber(float add) {
        this.setText(String.valueOf(FileHelper.round(this.getNumber() + add, 2)));
        if (this.getNumber() > this.max) {
            this.setNumber(this.max);
        } else if (this.getNumber() < this.min) {
            this.setNumber(this.min);
        }
    }

    public void setNumber(float num) {
        this.setText(String.valueOf(num));
    }

    public static boolean isFloatCharacter(char ch) {
        return ch == '.' || ch == '-' || Character.isDigit(ch);
    }

    public static boolean isFloatCharacters(String str) {
        for (char ch : str.toCharArray()) {
            if (!isFloatCharacter(ch)) return false;
        }
        return true;
    }

    public void write(String text) {
        if (isFloatCharacters(text)) {
            super.write(text);
            if (this.getNumber() > this.max) {
                this.setNumber(this.max);
            } else if (this.getNumber() < this.min) {
                this.setNumber(this.min);
            }
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_DOWN: {
                this.addNumber(-this.delta);
                return true;
            }
            case GLFW.GLFW_KEY_UP: {
                this.addNumber(this.delta);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

}
