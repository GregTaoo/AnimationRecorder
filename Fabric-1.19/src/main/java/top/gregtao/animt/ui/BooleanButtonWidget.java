package top.gregtao.animt.ui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class BooleanButtonWidget extends ButtonWidget {
    private boolean bl;
    private final MutableText trueText = Text.translatable("animt.button.bl.true");
    private final MutableText falseText = Text.translatable("animt.button.bl.false");

    public BooleanButtonWidget(int x, int y, int width, int height, boolean def) {
        super(x, y, width, height, Text.of(""), BooleanButtonWidget::onClickedLambda);
        this.bl = def;
    }

    public void changeValue() {
        this.bl = !this.bl;
    }

    public void setValue(boolean bl) {
        this.bl = bl;
    }

    public boolean getValue() {
        return this.bl;
    }

    public Text getMessage() {
        return this.bl ? this.trueText : this.falseText;
    }

    public static void onClickedLambda(ButtonWidget button) {
        if (button instanceof BooleanButtonWidget widget) {
            widget.changeValue();
        }
    }

}
