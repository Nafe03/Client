package dev.anarchy.waifuhax.api.mixins;

import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.systems.modules.render.Fullbright;
import dev.anarchy.waifuhax.client.managers.ModuleManager;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {
    
    @ModifyArgs(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/texture/NativeImage;setColor(III)V"
        )
    )
    private void onUpdateLightmap(Args args) {
        Fullbright fullbright = ModuleManager.getModule(Fullbright.class);
        if (fullbright != null && fullbright.isEnabled.getValue()) {
            if (fullbright.mode.getValue() == Fullbright.Mode.LUMINANCE || 
                fullbright.mode.getValue() == Fullbright.Mode.BOTH) {
                args.set(2, 0xFFFFFFFF); // Set to full brightness
            }
        }
    }
}