package top.gregtao.animt.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.gregtao.animt.SettingsScreen;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init()V")
    private void init(CallbackInfo info) {
        if (this.client == null) return;
        this.addDrawableChild(new ButtonWidget(
                this.width / 2 - 155, this.height / 6 + 144 - 6, 150, 20,
                Text.translatable("animt.screen.title"),
                button -> this.client.setScreen(new SettingsScreen())));
    }
}
