package dev.anarchy.waifuhax.api.mixins;

import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.systems.modules.render.Fullbright;
import dev.anarchy.waifuhax.client.managers.ModuleManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionType.class)
public class MixinDimensionType {
    
    @Inject(method = "ambientLight", at = @At("HEAD"), cancellable = true)
    private void onAmbientLight(CallbackInfoReturnable<Float> cir) {
        Fullbright fullbright = ModuleManager.getModule(Fullbright.class);
        if (fullbright != null && fullbright.isEnabled.getValue()) {
            if (fullbright.mode.getValue() == Fullbright.Mode.LUMINANCE || 
                fullbright.mode.getValue() == Fullbright.Mode.BOTH) {
                cir.setReturnValue(1.0f);
            }
        }
    }
}
