package top.gregtao.animt;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import top.gregtao.animt.ui.BooleanButtonWidget;
import top.gregtao.animt.ui.ColorFieldWidget;
import top.gregtao.animt.ui.FloatNumberFieldWidget;
import top.gregtao.animt.ui.IntNumberFieldWidget;

@Environment(EnvType.CLIENT)
public class SettingsScreen extends Screen {
    private FloatNumberFieldWidget scaleField;
    private FloatNumberFieldWidget rotateXField;
    private FloatNumberFieldWidget rotateYField;
    private FloatNumberFieldWidget rotateZField;
    private IntNumberFieldWidget deltaXField;
    private IntNumberFieldWidget deltaYField;
    private IntNumberFieldWidget gifSizeField;
    private BooleanButtonWidget transButton;
    private ColorFieldWidget colorField;
    private CyclingButtonWidget<ImageFileType> typeWidget;

    public LivingEntity target;
    public HitBoxHelper hitBoxHelper;

    public SettingsScreen() {
        super(new TranslatableText("animt.screen.title"));
    }

    @Override
    public void tick() {
        this.scaleField.tick();
        this.rotateXField.tick();
        this.rotateYField.tick();
        this.rotateZField.tick();
        this.deltaXField.tick();
        this.deltaYField.tick();
        this.gifSizeField.tick();
        this.colorField.tick();
    }

    public void loadFromConfig(RecorderConfig config) {
        this.scaleField.setText(String.valueOf(config.scale));
        this.rotateXField.setText(String.valueOf(config.rotateX));
        this.rotateYField.setText(String.valueOf(config.rotateY));
        this.rotateZField.setText(String.valueOf(config.rotateZ));
        this.deltaXField.setText(String.valueOf(config.itemDeltaX));
        this.deltaYField.setText(String.valueOf(config.itemDeltaY));
        this.gifSizeField.setText(String.valueOf(config.gifSize));
        this.transButton.setValue(config.trans);
        this.colorField.setFromColor(config.bgColor);
        this.typeWidget.setValue(config.type);
    }

    public void saveToConfig(RecorderConfig config) {
        config.scale = this.scaleField.getNumber();
        config.rotateX = this.rotateXField.getNumber();
        config.rotateY = this.rotateYField.getNumber();
        config.rotateZ = this.rotateZField.getNumber();
        config.itemDeltaX = this.deltaXField.getNumber();
        config.itemDeltaY = this.deltaYField.getNumber();
        config.gifSize = this.gifSizeField.getNumber();
        config.trans = this.transButton.getValue();
        config.bgColor = this.colorField.toNumber();
        config.type = this.typeWidget.getValue();
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(new TranslatableText("animt.screen.save"), false);
        }
    }

    public void resetToDefault() {
        this.loadFromConfig(new RecorderConfig());
    }

    @Override
    protected void init() {
        if (this.client == null || this.client.player == null) return;
        this.target = EntityRecorder.CURRENT.target;
        this.target = this.target == null ? this.client.player : this.target;
        this.hitBoxHelper = HitBoxHelper.getFromEntity(this.target);

        this.scaleField = new FloatNumberFieldWidget(
                this.textRenderer, this.width / 2, 45, 60, 12,
                 1, 0.1f, 0.1f, 20);
        this.addSelectableChild(this.scaleField);
        
        this.rotateXField = new FloatNumberFieldWidget(
                this.textRenderer, this.width / 2, 65, 60, 12,
                 0, 5, -360, 360);
        this.addSelectableChild(this.rotateXField);
        
        this.rotateYField = new FloatNumberFieldWidget(
                this.textRenderer, this.width / 2, 85, 60, 12,
                 0, 5, -360, 360);
        this.addSelectableChild(this.rotateYField);

        this.rotateZField = new FloatNumberFieldWidget(
                this.textRenderer, this.width / 2, 105, 60, 12,
                 0, 5, -360, 360);
        this.addSelectableChild(this.rotateZField);
        
        this.deltaXField = new IntNumberFieldWidget(
                this.textRenderer, this.width / 2, 125, 60, 12,
                 0, 1, -1000, 1000);
        this.addSelectableChild(this.deltaXField);

        this.deltaYField = new IntNumberFieldWidget(
                this.textRenderer, this.width / 2, 145, 60, 12,
                 0, 1, -1000, 1000);
        this.addSelectableChild(this.deltaYField);

        this.gifSizeField = new IntNumberFieldWidget(
                this.textRenderer, this.width / 2, 165, 60, 12,
                 128, 8, 1, 1000);
        this.addSelectableChild(this.gifSizeField);

        this.transButton = new BooleanButtonWidget(this.width / 2, 185, 60, 20, true);
        this.addDrawableChild(this.transButton);

        this.addDrawableChild(new ButtonWidget(
                this.width / 2 + 100, 45, 60, 20,
                new TranslatableText("animt.button.reset"), button -> this.resetToDefault()));

        this.addDrawableChild(new ButtonWidget(
                this.width / 2 + 100, 65, 60, 20,
                new TranslatableText("animt.button.save"), button -> this.saveToConfig(EntityRecorder.CURRENT.config)));

        this.addDrawableChild(new ButtonWidget(
                this.width / 2 + 100, 85, 60, 20,
                new TranslatableText("animt.button.save_file"),
                button -> {
                    this.saveToConfig(EntityRecorder.CURRENT.config);
                    try {
                        EntityRecorder.CURRENT.config.writeConfig();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));

        this.addDrawableChild(new ButtonWidget(
                this.width / 2 + 100, 105, 60, 20,
                new TranslatableText("animt.button.open_folder"),
                button -> Util.getOperatingSystem().open(EntityRecorder.CURRENT.config.file.getParentFile().getAbsoluteFile())));

        this.colorField = new ColorFieldWidget(this.width / 2 + 100, 130, this.textRenderer);
        this.colorField.forEach(this::addSelectableChild);

        this.typeWidget = CyclingButtonWidget
                .builder(ImageFileType::getTextName)
                .values(ImageFileType.values())
                .initially(EntityRecorder.CURRENT.config.type)
                .build(10, 185, 110, 20, new TranslatableText("animt.button.types"));
        this.addDrawableChild(this.typeWidget);

        this.loadFromConfig(EntityRecorder.CURRENT.config);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        SettingsScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xffffff);
        super.render(matrices, mouseX, mouseY, delta);
        this.textRenderer.draw(matrices, "scale", 140, 47, 0xffffff);
        this.textRenderer.draw(matrices, "rotateX", 140, 67, 0xffffff);
        this.textRenderer.draw(matrices, "rotateY", 140, 87, 0xffffff);
        this.textRenderer.draw(matrices, "rotateZ", 140, 107, 0xffffff);
        this.textRenderer.draw(matrices, "deltaX", 140, 127, 0xffffff);
        this.textRenderer.draw(matrices, "deltaY", 140, 147, 0xffffff);
        this.textRenderer.draw(matrices, "gifSize", 140, 167, 0xffffff);
        this.textRenderer.draw(matrices, new TranslatableText("animt.screen.trans"), 140, 192, 0xffffff);
        this.scaleField.render(matrices, mouseX, mouseY, delta);
        this.rotateXField.render(matrices, mouseX, mouseY, delta);
        this.rotateYField.render(matrices, mouseX, mouseY, delta);
        this.rotateZField.render(matrices, mouseX, mouseY, delta);
        this.deltaXField.render(matrices, mouseX, mouseY, delta);
        this.deltaYField.render(matrices, mouseX, mouseY, delta);
        this.gifSizeField.render(matrices, mouseX, mouseY, delta);
        this.colorField.render(matrices, mouseX, mouseY, delta, this.textRenderer);

        SettingsScreen.drawCenteredText(
                matrices, this.textRenderer,
                this.gifSizeField.getText() + " x " + this.gifSizeField.getText(),
                65, 160, 0xffffff);

        if (this.client != null && this.client.player != null) {
            drawRectangle(matrices, 10, 40, 120, 150, 1, 0xffffffff);
            if (!this.transButton.getValue()) {
                SettingsScreen.fill(matrices, 10, 40, 120, 150, (int) this.colorField.toNumber());
            }
            EntityRecorder.rotateModelStack(this.rotateXField.getNumber(), this.rotateYField.getNumber(), this.rotateZField.getNumber());
            int deltaX = this.deltaXField.getNumber();
            int deltaY = this.deltaYField.getNumber();
            int entitySz = (int) (36 * this.scaleField.getNumber() / Math.max(this.hitBoxHelper.width, this.hitBoxHelper.height));
            InventoryScreen.drawEntity(
                    134 + deltaX, 72 + deltaY,
                    entitySz,
                    0, 0, this.target);
        }
    }

    public static void drawRectangle(MatrixStack matrices, int x, int y, int x1, int y1, int k, int color) {
        SettingsScreen.fill(matrices, x, y, x1, y + k, color);
        SettingsScreen.fill(matrices, x, y1 - k, x1, y1, color);
        SettingsScreen.fill(matrices, x, y, x + k, y1, color);
        SettingsScreen.fill(matrices, x1, y, x1 - k, y1, color);
    }
}
