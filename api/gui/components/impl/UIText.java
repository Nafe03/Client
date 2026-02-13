package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.styling.UIStyle;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class UIText extends UIElement {

    @Getter
    private String text = "";

    @Getter
    private float scale = 1.0f;

    @Getter
    private int color = 0xFFFFFFFF;


    public UIText() {
        this.style = UIStyle.getPurpleSmooth(); // Changed to purple smooth style
    }

    public UIText setText(String text) {
        this.text = text;
        updateSize();
        return this;
    }

    public UIText setScale(float scale) {
        this.scale = scale;
        updateSize();
        return this;
    }

    public UIText setColor(int color) {
        this.color = color;
        return this;
    }

    private void updateSize() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.textRenderer != null) {
            int textWidth = (int) (client.textRenderer.getWidth(Text.of(text)) * scale);
            int textHeight = (int) (client.textRenderer.fontHeight * scale);
            setSize(textWidth, textHeight);
        }
    }

    @Override
    protected void render(DrawContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null || text.isEmpty()) {
            return;
        }

        int x = (int) getAbsolutePos().x;
        int y = (int) getAbsolutePos().y;

        // Apply scale transformation
        if (scale != 1.0f) {
            ctx.getMatrices().pushMatrix();
            ctx.getMatrices().translate(x, y);
            ctx.getMatrices().scale(scale, scale);
            ctx.getMatrices().translate(-x, -y);
        }

        // Apply opacity from style
        int finalColor = applyOpacity(color, style.getOpacity());

        ctx.drawText(client.textRenderer, Text.of(text), x, y, finalColor, style.isShadowedText());

        if (scale != 1.0f) {
            ctx.getMatrices().popMatrix();
        }
    }

    private int applyOpacity(int color, float opacity) {
        int alpha = (int) (((color >> 24) & 0xFF) * opacity);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
