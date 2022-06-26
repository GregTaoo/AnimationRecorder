package top.gregtao.animt.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import top.gregtao.animt.util.FileHelper;

public class IntNumberFieldWidget extends TextFieldWidget {
    private final int def, delta, min, max;
    
    public IntNumberFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, int def, int delta, int min, int max) {
        super(textRenderer, x, y, width, height, Text.of(""));
        this.setFocusUnlocked(true);
        this.setEditableColor(-1);
        this.setUneditableColor(-1);
        this.setDrawsBackground(true);
        this.setMaxLength(50);
        this.setText(String.valueOf(def));
        this.def = def;
        this.delta = delta;
        this.max = max;
        this.min = min;
    }
    
    public int getNumber() {
        return FileHelper.parseInt(this.getText(), this.def);
    }

    public void addNumber(int add) {
        this.setText(String.valueOf(this.getNumber() + add));
        if (this.getNumber() > this.max) {
            this.setNumber(this.max);
        } else if (this.getNumber() < this.min) {
            this.setNumber(this.min);
        }
    }

    public void setNumber(int num) {
        this.setText(String.valueOf(num));
    }

    public static boolean isIntCharacter(char ch) {
        return ch == '-' || Character.isDigit(ch);
    }

    public static boolean isIntCharacters(String str) {
        for (char ch : str.toCharArray()) {
            if (!isIntCharacter(ch)) return false;
        }
        return true;
    }

    public void write(String text) {
        if (isIntCharacters(text)) {
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
