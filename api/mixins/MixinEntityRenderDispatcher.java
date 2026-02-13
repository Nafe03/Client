package dev.anarchy.waifuhax.api.mixins;

import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.systems.modules.render.Fullbright;
import dev.anarchy.waifuhax.client.managers.ModuleManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    
    @Inject(method = "renderShadow", at = @At("HEAD"), cancellable = true)
    private static void onRenderShadow(CallbackInfo ci) {
        Fullbright fullbright = ModuleManager.getModule(Fullbright.class);
        if (fullbright != null && fullbright.shouldRemoveShadows()) {
            ci.cancel();
        }
    }
}