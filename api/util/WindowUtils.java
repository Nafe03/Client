package dev.anarchy.waifuhax.api.util;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

@UtilityClass
public class WindowUtils {

    public static long getWindowHandle() {
        return MinecraftClient.getInstance()
                .getWindow()
                .getHandle();
    }

    public static boolean isMouseOnText(int posx, int posy, Text text) {
        // this is 0 if gui scale = auto
        int scale = MinecraftClient.getInstance().options.getGuiScale().getValue();
        if (scale == 0) scale = 2; // idk
        posy *= scale;
        posx *= scale;
        return MinecraftClient.getInstance().mouse.getX() > posx &&
                MinecraftClient.getInstance().mouse.getX() < posx + MinecraftClient.getInstance().textRenderer.getWidth(text.getString()) * scale &&
                MinecraftClient.getInstance().mouse.getY() > posy &&
                MinecraftClient.getInstance().mouse.getY() < posy + MinecraftClient.getInstance().textRenderer.fontHeight * scale;
    }
}
