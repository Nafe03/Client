package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.gui.animation.AnimationEasing;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.styling.UIStyle;
import dev.anarchy.waifuhax.client.events.hud.HudMouseDragEvent;
import dev.anarchy.waifuhax.client.events.hud.HudMouseReleasedEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Fixed slider with proper coordinate handling
 * All coordinates received are already scaled by WHWindow
 */
public class UISlider extends UIElement {

    private float value = 0.5f;
    private float min = 0.0f;
    private float max = 1.0f;
    private String label = "Slider";

    private boolean isDragging = false;
    private float hoverProgress = 0.0f;
    private float fillProgress = 0.5f;

    private static final int TRACK_HEIGHT = 4;
    private static final int LABEL_HEIGHT = 10;
    private static final int TOTAL_HEIGHT = 24;
    
    public UISlider(String label, float min, float max, float defaultValue) {
        this.label = label;
        this.min = min;
        this.max = max;
        this.value = Math.max(min, Math.min(max, defaultValue));
        this.fillProgress = (this.value - min) / (max - min);

        this.style = UIStyle.getPurpleSmooth();
        this.setSize(110, TOTAL_HEIGHT);

        setupEvents();
    }
    
    private void setupEvents() {
        this.onHoverEnter(() -> {
            animate("hoverProgress", hoverProgress, 1.0f, 150, AnimationEasing.EASE_OUT_QUAD);
        });

        this.onHoverExit(() -> {
            if (!isDragging) {
                animate("hoverProgress", hoverProgress, 0.0f, 150, AnimationEasing.EASE_IN_QUAD);
            }
        });

        this.addMouseEvent(event -> {
            // Only start dragging if we're actually hovered and it's left click
            // Coordinates from event are already scaled by WHWindow
            if (event.button == 0 && isHoveredScaled(event.mouseX, event.mouseY)) {
                isDragging = true;
                updateValueFromScaledMouse(event.mouseX);
            }
        });

        this.onMouseReleased(event -> {
            if (event.button == 0 && isDragging) {
                stopDragging(event);
            }
        });
    }
    
    public void stopDragging(HudMouseReleasedEvent event) {
        isDragging = false;
        // Coordinates are already scaled
        if (!isHoveredScaled(event.mouseX, event.mouseY)) {
            animate("hoverProgress", hoverProgress, 0.0f, 150, AnimationEasing.EASE_IN_QUAD);
        }
    }
    
    /**
     * Called from WHWindow during drag with scaled coordinates
     */
    public void onMouseDrag(HudMouseDragEvent event) {
        if (isDragging && event.button == 0) {
            // Coordinates from event are already scaled
            updateValueFromScaledMouse(event.mouseX);
        }
    }
    
    /**
     * Update value from mouse position
     * Expects SCALED mouse X coordinate
     * Public for use by WHWindow during drag operations
     */
    public void updateValueFromScaledMouse(double scaledMouseX) {
        float trackX = getAbsolutePos().x;
        float trackWidth = size.x;
        
        // Calculate normalized value
        float relativeX = (float) (scaledMouseX - trackX);
        float normalizedValue = Math.max(0, Math.min(1, relativeX / trackWidth));
        
        this.value = min + (max - min) * normalizedValue;
        this.fillProgress = normalizedValue;
    }
    
    public UISlider setValue(float value) {
        this.value = Math.max(min, Math.min(max, value));
        this.fillProgress = (this.value - min) / (max - min);
        return this;
    }
    
    public float getValue() {
        return value;
    }

    public boolean isDragging() {
        return isDragging;
    }
    
    @Override
    public void setAnimatedProperty(String property, float val) {
        switch (property) {
            case "hoverProgress" -> this.hoverProgress = val;
            case "fillProgress" -> this.fillProgress = val;
            default -> super.setAnimatedProperty(property, val);
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
        
        renderLabel(ctx, client, x, y);
        renderTrack(ctx, x, y + LABEL_HEIGHT + 3, width);
    }
    
    private void renderLabel(DrawContext ctx, MinecraftClient client, int x, int y) {
        // Format value nicely
        String valueStr;
        if (max - min > 100) {
            valueStr = String.valueOf((int) value);
        } else if (max - min > 10) {
            valueStr = String.format("%.1f", value);
        } else {
            valueStr = String.format("%.2f", value);
        }
        
        String labelText = label + ": " + valueStr;
        
        // Smooth color transition
        int labelColor = interpolateColor(0xFFC0C0D0, 0xFFFFFFFF, hoverProgress);
        
        ctx.drawText(client.textRenderer, Text.of(labelText), x, y, labelColor, false);
    }
    
    private void renderTrack(DrawContext ctx, int x, int trackY, int width) {
        // Track background
        int trackBgColor = interpolateColor(0x30FFFFFF, 0x40FFFFFF, hoverProgress);
        drawRoundedRect(ctx, x, trackY, width, TRACK_HEIGHT, TRACK_HEIGHT / 2f, trackBgColor);
        
        // Filled track
        int filledWidth = (int) (width * fillProgress);
        if (filledWidth > 0) {
            int startColor = interpolateColor(0xFFA855F7, 0xFFB855F7, hoverProgress * 0.3f);
            int endColor = interpolateColor(0xFF9333EA, 0xFFA333EA, hoverProgress * 0.3f);
            
            drawGradientRoundedRect(ctx, x, trackY, filledWidth, TRACK_HEIGHT, 
                                   TRACK_HEIGHT / 2f, startColor, endColor);
        }
        
        // Hover highlight
        if (hoverProgress > 0) {
            int highlightColor = (int) (0x20FFFFFF * hoverProgress);
            drawRoundedRect(ctx, x, trackY, width, TRACK_HEIGHT, TRACK_HEIGHT / 2f, highlightColor);
        }
    }
    
    private void drawGradientRoundedRect(DrawContext ctx, int x, int y, int width, int height, 
                                        float radius, int startColor, int endColor) {
        if (width <= 0) return;
        
        int r = (int) Math.min(radius, Math.min(width, height) / 2);
        
        for (int i = 0; i < width; i++) {
            float progress = width > 1 ? (float) i / (width - 1) : 0;
            int color = interpolateColor(startColor, endColor, progress);
            
            if (i < r) {
                for (int dy = 0; dy < height; dy++) {
                    float dx = r - i;
                    float dyCenter = Math.abs(dy - height / 2f);
                    float dist = (float) Math.sqrt(dx * dx + dyCenter * dyCenter);
                    
                    if (dist <= r) {
                        ctx.fill(x + i, y + dy, x + i + 1, y + dy + 1, color);
                    }
                }
            } else if (i >= width - r) {
                for (int dy = 0; dy < height; dy++) {
                    float dx = i - (width - r);
                    float dyCenter = Math.abs(dy - height / 2f);
                    float dist = (float) Math.sqrt(dx * dx + dyCenter * dyCenter);
                    
                    if (dist <= r) {
                        ctx.fill(x + i, y + dy, x + i + 1, y + dy + 1, color);
                    }
                }
            } else {
                ctx.fill(x + i, y, x + i + 1, y + height, color);
            }
        }
    }
    
    private void drawRoundedRect(DrawContext ctx, int x, int y, int width, int height, 
                                float radius, int color) {
        if (width < 1 || height < 1) return;
        
        if (radius <= 0) {
            ctx.fill(x, y, x + width, y + height, color);
            return;
        }
        
        int r = (int) Math.min(radius, Math.min(width, height) / 2);
        
        ctx.fill(x + r, y, x + width - r, y + height, color);
        ctx.fill(x, y + r, x + r, y + height - r, color);
        ctx.fill(x + width - r, y + r, x + width, y + height - r, color);
        
        drawCorner(ctx, x + r, y + r, r, color, 2);
        drawCorner(ctx, x + width - r, y + r, r, color, 1);
        drawCorner(ctx, x + r, y + height - r, r, color, 3);
        drawCorner(ctx, x + width - r, y + height - r, r, color, 4);
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