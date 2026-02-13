package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.styling.UIStyle;
import net.minecraft.client.gui.DrawContext;

public class UIRectangle extends UIElement {

    public UIRectangle() {
        this.style = UIStyle.getPurpleSmooth(); // Changed to purple smooth style
    }

    public UIRectangle setColor(int color) {
        this.style = this.style.toBuilder().backgroundColor(color).build();
        return this;
    }

    @Override
    protected void render(DrawContext ctx) {
        int x = (int) getAbsolutePos().x;
        int y = (int) getAbsolutePos().y;
        int width = (int) size.x;
        int height = (int) size.y;

        // Apply opacity from style
        int finalColor = applyOpacity(style.getBackgroundColor(), style.getOpacity());

        if (style.getBorderRadius() > 0) {
            drawRoundedRect(ctx, x, y, width, height, style.getBorderRadius(), finalColor);
        } else {
            ctx.fill(x, y, x + width, y + height, finalColor);
        }

        // Draw shadow if enabled
        if (style.isHasShadow()) {
            int shadowX = x + (int) style.getShadowOffsetX();
            int shadowY = y + (int) style.getShadowOffsetY();
            drawRoundedRect(ctx, shadowX, shadowY, width, height,
                           style.getBorderRadius(), style.getShadowColor());
        }

        // Draw border if specified
        if (style.getBorderWidth() > 0) {
            drawRoundedRectBorder(ctx, x, y, width, height, style.getBorderRadius(),
                                 (int) style.getBorderWidth(), style.getBorderColor());
        }
    }

    private void drawRoundedRect(DrawContext ctx, int x, int y, int width, int height, float radius, int color) {
        if (radius <= 0) {
            ctx.fill(x, y, x + width, y + height, color);
        } else {
            int r = (int) Math.min(radius, Math.min(width, height) / 2);
            ctx.fill(x + r, y, x + width - r, y + height, color);
            ctx.fill(x, y + r, x + r, y + height - r, color);
            ctx.fill(x + width - r, y + r, x + width, y + height - r, color);
            ctx.fill(x, y, x + r, y + r, color);
            ctx.fill(x + width - r, y, x + width, y + r, color);
            ctx.fill(x, y + height - r, x + r, y + height, color);
            ctx.fill(x + width - r, y + height - r, x + width, y + height, color);
        }
    }

    private void drawRoundedRectBorder(DrawContext ctx, int x, int y, int width, int height,
                                       float radius, int borderWidth, int color) {
        for (int i = 0; i < borderWidth; i++) {
            int offset = i;
            ctx.drawBorder(x + offset, y + offset, width - offset * 2, height - offset * 2, color);
        }
    }

    private int applyOpacity(int color, float opacity) {
        int alpha = (int) (((color >> 24) & 0xFF) * opacity);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
