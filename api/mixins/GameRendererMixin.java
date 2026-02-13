package dev.anarchy.waifuhax.api.mixins;

import dev.anarchy.waifuhax.client.managers.ModuleManager;
import dev.anarchy.waifuhax.client.systems.modules.world.CutAwayWorld;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    MinecraftClient client;

    @Shadow
    public abstract float getFarPlaneDistance();

    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), cancellable = true)
    public void getProjectionMatrix(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        CutAwayWorld module = ModuleManager.getModule(CutAwayWorld.class);

        if (module != null && module.isEnabled.getValue()) {
            if (module.nearPlaneDistance.getValue() <= 0.0f)
                module.nearPlaneDistance.setValue(0.1f);
            Matrix4f matrix4f = new Matrix4f();
            cir.setReturnValue(matrix4f.perspective(fov * ((float)Math.PI / 180F),
                    (float)this.client.getWindow().getFramebufferWidth() / (float)this.client.getWindow().getFramebufferHeight(),
                    module.nearPlaneDistance.getValue(),
                    this.getFarPlaneDistance()));
        }
    }
}
