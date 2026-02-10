package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.gui.animation.AnimationEasing;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.styling.UIStyle;
import net.minecraft.client.gui.DrawContext;

public class UICheckBox extends UIElement {

    public static final int SIZE = 16;

    public boolean state = false;
    private float checkProgress = 0.0f;
    private float hoverProgress = 0.0f;

    public UICheckBox() {
        this.setSize(SIZE, SIZE);
        this.style = UIStyle.getModernDark();
        
        this.addMouseEvent((event) -> { 
            if (event.button == 0) {
                setActive(!state);
            }
        });
        
        this.onHoverEnter(() -> {
            animate("hoverProgress", hoverProgress, 1.0f, 150, AnimationEasing.EASE_OUT);
        });
        
        this.onHoverExit(() -> {
            animate("hoverProgress", hoverProgress, 0.0f, 150, AnimationEasing.EASE_IN);
        });
    }

    public UICheckBox setActive(boolean state) {
        this.state = state;
        
        // Animate the checkmark
        float targetProgress = state ? 1.0f : 0.0f;
        animate("checkProgress", checkProgress, targetProgress, 200, AnimationEasing.EASE_OUT_CUBIC);
        
        return this;
    }
    
    @Override
    public void setAnimatedProperty(String property, float value) {
        if (property.equals("checkProgress")) {
            this.checkProgress = value;
        } else if (property.equals("hoverProgress")) {
            this.hoverProgress = value;
        } else {
            super.setAnimatedProperty(property, value);
        }
    }

    @Override
    protected void render(DrawContext ctx) {
        int x = (int) getAbsolutePos().x;
        int y = (int) getAbsolutePos().y;
        
        // Calculate colors with hover effect
        int bgColor = interpolateColor(style.getBackgroundColor(), 
                                       lightenColor(style.getBackgroundColor(), 0.2f), 
                                       hoverProgress);
        int borderColor = interpolateColor(style.getBorderColor(), 
                                          style.getHoverColor(), 
                                          hoverProgress);
        int checkColor = style.getActiveColor();
        
        // Draw shadow
        if (style.isHasShadow()) {
            ctx.fill(x + 1, y + 2, x + SIZE + 1, y + SIZE + 2, style.getShadowColor());
        }
        
        // Draw background with rounded corners
        drawRoundedRect(ctx, x, y, SIZE, SIZE, style.getBorderRadius(), bgColor);
        
        // Draw border
        if (style.getBorderWidth() > 0) {
            drawRoundedRectBorder(ctx, x, y, SIZE, SIZE, style.getBorderRadius(), 
                                 (int) style.getBorderWidth(), borderColor);
        }
        
        // Draw animated checkmark
        if (checkProgress > 0.01f) {
            // Scale effect for checkmark
            float scale = 0.5f + (checkProgress * 0.5f);
            
            if (scale != 1.0f) {
                ctx.getMatrices().translate(x + SIZE / 2f, y + SIZE / 2f);
                ctx.getMatrices().scale(scale, scale);
                ctx.getMatrices().translate(-(x + SIZE / 2f), -(y + SIZE / 2f));
            }
            
            // Draw checkmark (simple version - you can make this fancier)
            int checkAlpha = (int) (checkProgress * 255);
            int checkColorWithAlpha = (checkColor & 0x00FFFFFF) | (checkAlpha << 24);
            
            // Checkmark lines
            int padding = 4;
            int x1 = x + padding;
            int y1 = y + SIZE / 2;
            int x2 = x + SIZE / 2 - 1;
            int y2 = y + SIZE - padding;
            int x3 = x + SIZE - padding;
            int y3 = y + padding;
            
            // Draw checkmark stroke
            drawThickLine(ctx, x1, y1, x2, y2, 2, checkColorWithAlpha);
            drawThickLine(ctx, x2, y2, x3, y3, 2, checkColorWithAlpha);
        }
    }
    
    private void drawThickLine(DrawContext ctx, int x1, int y1, int x2, int y2, int thickness, int color) {
        // Simple thick line using rectangles
        for (int i = 0; i < thickness; i++) {
            ctx.drawHorizontalLine(x1, x2, y1 + i, color);
            if (y2 != y1) {
                float progress = (float) i / thickness;
                int currentY = (int) (y1 + (y2 - y1) * progress);
                ctx.drawHorizontalLine(x1, x2, currentY, color);
            }
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
            ctx.drawBorder(x + offset, y + offset, 
                          width - offset * 2, height - offset * 2, color);
        }
    }
    
    private int interpolateColor(int color1, int color2, float progress) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int a = (int) (a1 + (a2 - a1) * progress);
        int r = (int) (r1 + (r2 - r1) * progress);
        int g = (int) (g1 + (g2 - g1) * progress);
        int b = (int) (b1 + (b2 - b1) * progress);
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    private int lightenColor(int color, float amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * (1 + amount)));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * (1 + amount)));
        int b = Math.min(255, (int) ((color & 0xFF) * (1 + amount)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}