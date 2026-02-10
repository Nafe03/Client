package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.gui.animation.AnimationEasing;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.styling.UIStyle;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class UIButton extends UIElement {

    @Getter
    private String text = "";
    
    private float hoverProgress = 0.0f;
    private float clickProgress = 0.0f;
    private boolean isPressed = false;

    public UIButton(String text) {
        this.text = text;
        this.style = UIStyle.getModernDark();
        
        // Calculate size based on text
        updateSize();
        
        // Setup hover animations
        this.onHoverEnter(() -> {
            animate("hoverProgress", hoverProgress, 1.0f, style.getHoverTransitionDuration(), AnimationEasing.EASE_OUT);
        });
        
        this.onHoverExit(() -> {
            animate("hoverProgress", hoverProgress, 0.0f, style.getHoverTransitionDuration(), AnimationEasing.EASE_IN);
        });
        
        // Setup click animations
        this.addMouseEvent(event -> {
            if (event.button == 0) {
                isPressed = true;
                animate("clickProgress", 0.0f, 1.0f, style.getClickTransitionDuration(), AnimationEasing.EASE_OUT)
                    .onComplete(() -> {
                        animate("clickProgress", 1.0f, 0.0f, style.getClickTransitionDuration(), AnimationEasing.EASE_IN);
                        isPressed = false;
                    });
            }
        });
    }
    
    public UIButton setText(String text) {
        this.text = text;
        updateSize();
        return this;
    }
    
    private void updateSize() {
        // Check if Minecraft client and textRenderer are initialized
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            // Use default size during early initialization (approximate)
            setSize(text.length() * 6 + 20, 9 + 10);
            return;
        }
        
        int textWidth = client.textRenderer.getWidth(Text.of(text));
        int textHeight = client.textRenderer.fontHeight;
        setSize(textWidth + 20, textHeight + 10);
    }
    
    @Override
    public void setAnimatedProperty(String property, float value) {
        if (property.equals("hoverProgress")) {
            this.hoverProgress = value;
        } else if (property.equals("clickProgress")) {
            this.clickProgress = value;
        } else {
            super.setAnimatedProperty(property, value);
        }
    }

    @Override
    protected void render(DrawContext ctx) {
        // Safety check for early rendering
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return;
        }
        
        int x = (int) getAbsolutePos().x;
        int y = (int) getAbsolutePos().y;
        int width = (int) size.x;
        int height = (int) size.y;
        
        // Calculate animated colors
        int bgColor = interpolateColor(style.getBackgroundColor(), style.getHoverColor(), hoverProgress);
        int borderColor = interpolateColor(style.getBorderColor(), style.getHoverColor(), hoverProgress);
        
        // Apply click scale effect
        float clickScale = 1.0f - (clickProgress * 0.05f);
        
        if (clickScale != 1.0f) {
            ctx.getMatrices().translate(x + width / 2f, y + height / 2f);
            ctx.getMatrices().scale(clickScale, clickScale);
            ctx.getMatrices().translate(-(x + width / 2f), -(y + height / 2f));
        }
        
        // Draw shadow if enabled
        if (style.isHasShadow()) {
            int shadowX = x + (int) style.getShadowOffsetX();
            int shadowY = y + (int) style.getShadowOffsetY();
            drawRoundedRect(ctx, shadowX, shadowY, width, height, 
                           style.getBorderRadius(), style.getShadowColor());
        }
        
        // Draw background with border radius
        drawRoundedRect(ctx, x, y, width, height, style.getBorderRadius(), bgColor);
        
        // Draw border
        if (style.getBorderWidth() > 0) {
            drawRoundedRectBorder(ctx, x, y, width, height, 
                                 style.getBorderRadius(), style.getBorderWidth(), borderColor);
        }
        
        // Draw text
        int textColor = interpolateColor(style.getForegroundColor(), 
                                        style.getActiveColor(), hoverProgress);
        int textX = x + (width - client.textRenderer.getWidth(Text.of(text))) / 2;
        int textY = y + (height - client.textRenderer.fontHeight) / 2;
        
        ctx.drawText(client.textRenderer, 
                    Text.of(text), textX, textY, textColor, style.isShadowedText());
    }
    
    private void drawRoundedRect(DrawContext ctx, int x, int y, int width, int height, float radius, int color) {
        if (radius <= 0) {
            ctx.fill(x, y, x + width, y + height, color);
        } else {
            // Simple rounded rect approximation
            int r = (int) radius;
            
            // Main body
            ctx.fill(x + r, y, x + width - r, y + height, color);
            ctx.fill(x, y + r, x + r, y + height - r, color);
            ctx.fill(x + width - r, y + r, x + width, y + height - r, color);
            
            // Corners (simplified - you may want to use proper circle rendering)
            ctx.fill(x, y, x + r, y + r, color);
            ctx.fill(x + width - r, y, x + width, y + r, color);
            ctx.fill(x, y + height - r, x + r, y + height, color);
            ctx.fill(x + width - r, y + height - r, x + width, y + height, color);
        }
    }
    
    private void drawRoundedRectBorder(DrawContext ctx, int x, int y, int width, int height, 
                                       float radius, float borderWidth, int color) {
        int bw = (int) borderWidth;
        
        // Top
        ctx.fill(x, y, x + width, y + bw, color);
        // Bottom
        ctx.fill(x, y + height - bw, x + width, y + height, color);
        // Left
        ctx.fill(x, y, x + bw, y + height, color);
        // Right
        ctx.fill(x + width - bw, y, x + width, y + height, color);
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