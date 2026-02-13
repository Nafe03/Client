package dev.anarchy.waifuhax.api.mixins;

import dev.anarchy.waifuhax.client.managers.ModuleManager;
import dev.anarchy.waifuhax.client.systems.modules.world.AntiBlockRotate;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.security.SecureRandom;

@Mixin(MathHelper.class)
public class MathHelperMixin {

    @Inject(method = "hashCode(III)J", at = @At("RETURN"), cancellable = true)
    private static void changeReturnValue(int x, int y, int z, CallbackInfoReturnable<Long> cir) {
        if (ModuleManager.getModule(AntiBlockRotate.class) == null) {return;}
        if (!ModuleManager.getModule(AntiBlockRotate.class).isEnabled.getValue()) {
            return;
        }
        switch (ModuleManager.getModule(AntiBlockRotate.class).mode.getValue()) {
            case AntiBlockRotate.Modes.NO_ROTATION -> cir.setReturnValue(0L);
            case AntiBlockRotate.Modes.CUSTOM_SEED ->
                    cir.setReturnValue(new SecureRandom().nextLong());
            default ->
                    throw new IllegalStateException("Unexpected value: " + ModuleManager.getModule(AntiBlockRotate.class).mode.getValue());
        }
    }
}
