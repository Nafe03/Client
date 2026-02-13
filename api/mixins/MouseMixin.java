package dev.anarchy.waifuhax.api.mixins;

import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.events.hud.HudMouseClickedEvent;
import dev.anarchy.waifuhax.client.events.hud.HudMouseDragEvent;
import dev.anarchy.waifuhax.client.events.hud.HudMouseReleasedEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.client.Mouse.scaleX;
import static net.minecraft.client.Mouse.scaleY;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Shadow
    public abstract double getScaledX(Window window);

    @Shadow
    public abstract double getScaledY(Window window);

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private double cursorDeltaX;

    @Shadow
    private double cursorDeltaY;

    @Shadow
    private int activeButton;

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        final Window window = MinecraftClient.getInstance().getWindow();

        if (MinecraftClient.getInstance().isWindowFocused()) {
            double scaledMouseX = this.getScaledX(window);
            double scaledMouseY = this.getScaledY(window);
            double scaledDeltaX = scaleX(window, cursorDeltaX);
            double scaledDeltaY = scaleY(window, cursorDeltaY);
            WaifuHax.EVENT_BUS.post(HudMouseDragEvent.get(scaledMouseX, scaledMouseY, activeButton, scaledDeltaX, scaledDeltaY));
        }
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void onClicked(long window, int button, int action, int mods, CallbackInfo ci) {
        final Window win = MinecraftClient.getInstance().getWindow();
        if (window == win.getHandle()) {
            if (action == 1) {
                WaifuHax.EVENT_BUS.post(HudMouseClickedEvent.get(getScaledX(win), getScaledY(win), button));
            }
            else {
                WaifuHax.EVENT_BUS.post(HudMouseReleasedEvent.get(getScaledX(win), getScaledY(win), button));
            }

        }
    }
}
