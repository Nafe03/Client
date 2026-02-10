package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.gui.animation.AnimationEasing;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.styling.UIStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class UISlider extends UIElement {
    
    private float value = 0.5f;
    private float min = 0.0f;
    private float max = 1.0f;
    private String label = "Slider";
    
    private boolean isDragging = false;
    private float hoverProgress = 0.0f;
    private float thumbScale = 1.0f;
    
    private static final int TRACK_HEIGHT = 4;
    private static final int THUMB_SIZE = 12;
    
    public UISlider(String label, float min, float max, float defaultValue) {
        this.label = label;
        this.min = min;
        this.max = max;
        this.value = Math.max(min, Math.min(max, defaultValue));
        
        this.style = UIStyle.getModernDark();
        this.setSize(150, 30);
        
        setupEvents();
    }
    
    private void setupEvents() {
        this.onHoverEnter(() -> {
            animate("hoverProgress", hoverProgress, 1.0f, 150, AnimationEasing.EASE_OUT);
            animate("thumbScale", thumbScale, 1.2f, 150, AnimationEasing.EASE_OUT_CUBIC);
        });
        
        this.onHoverExit(() -> {
            if (!isDragging) {
                animate("hoverProgress", hoverProgress, 0.0f, 150, AnimationEasing.EASE_IN);
                animate("thumbScale", thumbScale, 1.0f, 150, AnimationEasing.EASE_IN_CUBIC);
            }
        });
        
        this.addMouseEvent(event -> {
            if (event.button == 0) {
                isDragging = true;
                updateValueFromMouse(event.mouseX, event.mouseY);
            }
        });
        
        this.onMouseReleased(event -> {
            if (event.button == 0 && isDragging) {
                isDragging = false;
                if (!isHovered(event.mouseX, event.mouseY)) {
                    animate("thumbScale", thumbScale, 1.0f, 150, AnimationEasing.EASE_IN_CUBIC);
                }
            }
        });
    }
    
    public void updateValueFromMouse(double mouseX, double mouseY) {
        float scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        mouseX /= scale;
        
        int trackX = (int) getAbsolutePos().x;
        int trackWidth = (int) size.x;
        
        float relativeX = (float) (mouseX - trackX);
        float normalizedValue = Math.max(0, Math.min(1, relativeX / trackWidth));
        
        this.value = min + (max - min) * normalizedValue;
    }
    
    public UISlider setValue(float value) {
        this.value = Math.max(min, Math.min(max, value));
        return this;
    }
    
    public float getValue() {
        return value;
    }
    
    @Override
    public void setAnimatedProperty(String property, float val) {
        switch (property) {
            case "hoverProgress" -> this.hoverProgress = val;
            case "thumbScale" -> this.thumbScale = val;
            default -> super.setAnimatedProperty(property, val);
        }
    }
    
    @Override
    protected void render(DrawContext ctx) {
        int x = (int) getAbsolutePos().x;
        int y = (int) getAbsolutePos().y;
        int width = (int) size.x;
        
        // Draw label
        ctx.drawText(MinecraftClient.getInstance().textRenderer,
                    Text.of(label + ": " + String.format("%.2f", value)),
                    x, y, style.getForegroundColor(), true);
        
        int trackY = y + 16;
        
        // Draw track background
        int trackBgColor = interpolateColor(0x40FFFFFF, 0x60FFFFFF, hoverProgress);
        drawRoundedRect(ctx, x, trackY, width, TRACK_HEIGHT, TRACK_HEIGHT / 2f, trackBgColor);
        
        // Draw filled track
        float normalizedValue = (value - min) / (max - min);
        int filledWidth = (int) (width * normalizedValue);
        int trackColor = interpolateColor(style.getActiveColor(), style.getHoverColor(), hoverProgress);
        drawRoundedRect(ctx, x, trackY, filledWidth, TRACK_HEIGHT, TRACK_HEIGHT / 2f, trackColor);
        
        // Draw thumb
        int thumbX = x + filledWidth - THUMB_SIZE / 2;
        int thumbY = trackY - (THUMB_SIZE - TRACK_HEIGHT) / 2;
        
        // Scale thumb
        if (thumbScale != 1.0f) {
            ctx.getMatrices().translate(thumbX + THUMB_SIZE / 2f, thumbY + THUMB_SIZE / 2f);
            ctx.getMatrices().scale(thumbScale, thumbScale);
            ctx.getMatrices().translate(-(thumbX + THUMB_SIZE / 2f), -(thumbY + THUMB_SIZE / 2f));
        }
        
        // Thumb shadow
        if (style.isHasShadow()) {
            drawCircle(ctx, thumbX + 1, thumbY + 2, THUMB_SIZE / 2, style.getShadowColor());
        }
        
        // Thumb
        int thumbColor = interpolateColor(0xFFFFFFFF, style.getHoverColor(), hoverProgress);
        drawCircle(ctx, thumbX, thumbY, THUMB_SIZE / 2, thumbColor);
        
        // Thumb border
        drawCircleBorder(ctx, thumbX, thumbY, THUMB_SIZE / 2, 1, style.getBorderColor());
    }
    
    private void drawRoundedRect(DrawContext ctx, int x, int y, int width, int height, 
                                float radius, int color) {
        if (radius <= 0 || width < 1 || height < 1) {
            if (width >= 1 && height >= 1) {
                ctx.fill(x, y, x + width, y + height, color);
            }
            return;
        }
        int r = (int) Math.min(radius, Math.min(width, height) / 2);
        ctx.fill(x + r, y, x + width - r, y + height, color);
        ctx.fill(x, y + r, x + r, y + height - r, color);
        ctx.fill(x + width - r, y + r, x + width, y + height - r, color);
        ctx.fill(x, y, x + r, y + r, color);
        ctx.fill(x + width - r, y, x + width, y + r, color);
        ctx.fill(x, y + height - r, x + r, y + height, color);
        ctx.fill(x + width - r, y + height - r, x + width, y + height, color);
    }
    
    private void drawCircle(DrawContext ctx, int centerX, int centerY, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    ctx.fill(centerX + radius + dx, centerY + radius + dy,
                            centerX + radius + dx + 1, centerY + radius + dy + 1, color);
                }
            }
        }
    }
    
    private void drawCircleBorder(DrawContext ctx, int centerX, int centerY, int radius, 
                                  int borderWidth, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int distSq = dx * dx + dy * dy;
                int innerRadiusSq = (radius - borderWidth) * (radius - borderWidth);
                int outerRadiusSq = radius * radius;
                
                if (distSq > innerRadiusSq && distSq <= outerRadiusSq) {
                    ctx.fill(centerX + radius + dx, centerY + radius + dy,
                            centerX + radius + dx + 1, centerY + radius + dy + 1, color);
                }
            }
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
}