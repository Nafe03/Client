package dev.anarchy.waifuhax.api.mixins;

import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("HEAD"))
    private void onInitTitleScreen(CallbackInfo ci) {
//        MinecraftClient.getInstance().setScreen(new HalfLifeStyleMainMenu());
    }
}
