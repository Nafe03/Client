package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.gui.animation.AnimationEasing;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.styling.UIStyle;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Fixed button with proper hover detection
 * All coordinates received are already scaled by WHWindow
 */
public class UIButton extends UIElement {

    @Getter
    private String text = "";
    
    private float hoverProgress = 0.0f;
    private float pressProgress = 0.0f;

    public UIButton(String text) {
        this.text = text;
        this.style = UIStyle.getPurpleSmooth();
        this.setMargin(2, 4, 2, 4);
        updateSize();
        setupEvents();
    }
    
    public UIButton setText(String text) {
        this.text = text;
        updateSize();
        return this;
    }
    
    private void updateSize() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            setSize(text.length() * 6 + 16, 20);
            return;
        }
        
        int textWidth = client.textRenderer.getWidth(Text.of(text));
        setSize(textWidth + 16, 20);
    }
    
    private void setupEvents() {
        this.onHoverEnter(() -> {
            animate("hoverProgress", hoverProgress, 1.0f, 150, AnimationEasing.EASE_OUT_QUAD);
        });

        this.onHoverExit(() -> {
            animate("hoverProgress", hoverProgress, 0.0f, 150, AnimationEasing.EASE_IN_QUAD);
        });

        this.addMouseEvent(event -> {
            // Event coordinates are already scaled, but we need to verify hover
            // The onClick handler already checks this, so we just need to check button
            if (event.button == 0 && isHoveredScaled(event.mouseX, event.mouseY)) {
                animate("pressProgress", 0.0f, 1.0f, 100, AnimationEasing.EASE_OUT_QUAD)
                    .onComplete(() -> {
                        animate("pressProgress", 1.0f, 0.0f, 100, AnimationEasing.EASE_IN_QUAD);
                    });
            }
        });
    }
    
    @Override
    public void setAnimatedProperty(String property, float value) {
        if (property.equals("hoverProgress")) {
            this.hoverProgress = value;
        } else if (property.equals("pressProgress")) {
            this.pressProgress = value;
        } else {
            super.setAnimatedProperty(property, value);
        }
    }

    @Override
    protected void render(DrawContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return;
        }
        
        int x = (int) getAbsolutePos().x;
        int y = (int) getAbsolutePos().y;
        int width = (int) size.x;
        int height = (int) size.y;
        
        // Background color transitions
        int bgColor = interpolateColor(0x20FFFFFF, 0x30FFFFFF, hoverProgress);
        bgColor = interpolateColor(bgColor, 0x40A855F7, pressProgress);
        
        // Draw rounded background
        drawRoundedRect(ctx, x, y, width, height, 3, bgColor);
        
        // Hover border
        if (hoverProgress > 0) {
            int borderColor = interpolateColor(0x00A855F7, 0x80A855F7, hoverProgress);
            drawRoundedRectBorder(ctx, x, y, width, height, 3, 1, borderColor);
        }
        
        // Text
        int textColor = interpolateColor(0xFFC0C0D0, 0xFFFFFFFF, hoverProgress);
        int textX = x + (width - client.textRenderer.getWidth(Text.of(text))) / 2;
        int textY = y + (height - client.textRenderer.fontHeight) / 2;
        
        ctx.drawText(client.textRenderer, Text.of(text), textX, textY, textColor, false);
    }
    
    private void drawRoundedRect(DrawContext ctx, int x, int y, int width, int height, int radius, int color) {
        if (radius <= 0) {
            ctx.fill(x, y, x + width, y + height, color);
            return;
        }
        
        ctx.fill(x + radius, y, x + width - radius, y + height, color);
        ctx.fill(x, y + radius, x + radius, y + height - radius, color);
        ctx.fill(x + width - radius, y + radius, x + width, y + height - radius, color);
        
        drawCorner(ctx, x + radius, y + radius, radius, color, 2);
        drawCorner(ctx, x + width - radius, y + radius, radius, color, 1);
        drawCorner(ctx, x + radius, y + height - radius, radius, color, 3);
        drawCorner(ctx, x + width - radius, y + height - radius, radius, color, 4);
    }
    
    private void drawRoundedRectBorder(DrawContext ctx, int x, int y, int width, int height, int radius, int borderWidth, int color) {
        for (int i = 0; i < borderWidth; i++) {
            drawRoundedRectOutline(ctx, x + i, y + i, width - i * 2, height - i * 2, radius, color);
        }
    }
    
    private void drawRoundedRectOutline(DrawContext ctx, int x, int y, int width, int height, int radius, int color) {
        ctx.fill(x + radius, y, x + width - radius, y + 1, color);
        ctx.fill(x + radius, y + height - 1, x + width - radius, y + height, color);
        ctx.fill(x, y + radius, x + 1, y + height - radius, color);
        ctx.fill(x + width - 1, y + radius, x + width, y + height - radius, color);
    }
    
    private void drawCorner(DrawContext ctx, int centerX, int centerY, int radius, int color, int quadrant) {
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (!isInQuadrant(dx, dy, quadrant)) continue;
                if (dx * dx + dy * dy <= radius * radius) {
                    ctx.fill(centerX + dx, centerY + dy, centerX + dx + 1, centerY + dy + 1, color);
                }
            }
        }
    }
    
    private boolean isInQuadrant(int dx, int dy, int quadrant) {
        return switch (quadrant) {
            case 1 -> dx >= 0 && dy <= 0;
            case 2 -> dx <= 0 && dy <= 0;
            case 3 -> dx <= 0 && dy >= 0;
            case 4 -> dx >= 0 && dy >= 0;
            default -> false;
        };
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
}