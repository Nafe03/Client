package dev.anarchy.waifuhax.api.mixins.events;

import dev.anarchy.waifuhax.api.settings.KeybindSetting;
import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.events.OnKeyPress;
import dev.anarchy.waifuhax.client.managers.ModuleManager;
import dev.anarchy.waifuhax.client.screens.ClickGuiScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(at = @At("HEAD"), method = "onKey", cancellable = true)
    private void onKeyPressed(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen != null && !(MinecraftClient.getInstance().currentScreen instanceof ClickGuiScreen)) {
            return;
        }

        if (action == 1) {
            if (KeybindSetting.currentBind == null && !(MinecraftClient.getInstance().currentScreen instanceof ClickGuiScreen)) {
                if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                    MinecraftClient.getInstance().setScreen(new ClickGuiScreen());
                }
                else {
                    ModuleManager.onKey(key);
                    OnKeyPress e = WaifuHax.EVENT_BUS.post(OnKeyPress.get(key, action));
                    if (e.isCancelled()) ci.cancel();
                }
                return;
            }
            if (KeybindSetting.currentBind != null) {
                if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE) {
                    KeybindSetting.currentBind.setValue(-1);
                }
                else {
                    KeybindSetting.currentBind.setValue(key);
                }
                KeybindSetting.currentBind.shouldSave = true;
            }
        }
    }

}
