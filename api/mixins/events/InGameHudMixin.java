package dev.anarchy.waifuhax.api.mixins.events;

import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.events.RenderHudEvent;
import dev.anarchy.waifuhax.client.events.hud.DrawnWaifuHudEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(at = @At("TAIL"), method = "renderMainHud")
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        WaifuHax.EVENT_BUS.post(RenderHudEvent.get(context));
        WaifuHax.EVENT_BUS.post(DrawnWaifuHudEvent.get(context));
    }

}
