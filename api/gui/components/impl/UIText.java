package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.gui.animation.AnimationEasing;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.styling.UIStyle;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class UIText extends UIElement {

    @Getter
    private String text = "";

    private float textScale = 1f;
    private int textColor = 0xFFFFFFFF;
    private boolean centered = false;
    private boolean rainbow = false;
    private float rainbowOffset = 0;
    
    private float glowIntensity = 0.0f;
    private boolean isGlowing = false;

    public UIText() {
        this.style = UIStyle.getMinimal();
    }

    public UIText setText(String text) {
        this.text = text;
        updateSize();
        return this;
    }

    public UIText setScale(float scale) {
        this.textScale = scale;
        updateSize();
        return this;
    }

    public UIText setColor(int color) {
        this.textColor = color;
        this.style.setForegroundColor(color);
        return this;
    }
    
    public UIText setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }
    
    public UIText setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
        return this;
    }
    
    public UIText enableGlow(boolean glow) {
        this.isGlowing = glow;
        if (glow) {
            startGlowAnimation();
        }
        return this;
    }
    
    private void startGlowAnimation() {
        animate("glowIntensity", 0.0f, 1.0f, 1000, AnimationEasing.EASE_IN_OUT)
            .onComplete(() -> {
                if (isGlowing) {
                    animate("glowIntensity", 1.0f, 0.0f, 1000, AnimationEasing.EASE_IN_OUT)
                        .onComplete(this::startGlowAnimation);
                }
            });
    }
    
    @Override
    public void setAnimatedProperty(String property, float value) {
        if (property.equals("glowIntensity")) {
            this.glowIntensity = value;
        } else {
            super.setAnimatedProperty(property, value);
        }
    }
    
    private void updateSize() {
        // Check if Minecraft client and textRenderer are initialized
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            // Use default size during early initialization
            setSize((int) (text.length() * 6 * textScale), (int) (9 * textScale));
            return;
        }
        
        int width = (int) (client.textRenderer.getWidth(Text.of(text)) * textScale);
        int height = (int) (client.textRenderer.fontHeight * textScale);
        setSize(width, height);
    }

    @Override
    protected void render(DrawContext ctx) {
        if (text.isEmpty()) return;
        
        // Safety check for early rendering
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return;
        }
        
        int x = (int) getAbsolutePos().x;
        int y = (int) getAbsolutePos().y;
        
        if (centered && parent != null) {
            int textWidth = (int) (client.textRenderer.getWidth(Text.of(text)) * textScale);
            x = (int) (parent.getAbsolutePos().x + (parent.getSize().x - textWidth) / 2);
        }
        
        // Apply scale
        if (textScale != 1.0f) {
            ctx.getMatrices().translate(x, y);
            ctx.getMatrices().scale(textScale, textScale);
            ctx.getMatrices().translate(-x, -y);
        }
        
        // Rainbow effect
        if (rainbow) {
            rainbowOffset += 0.05f;
            if (rainbowOffset > 360) rainbowOffset = 0;
            
            String[] chars = text.split("");
            int currentX = x;
            
            for (int i = 0; i < chars.length; i++) {
                float hue = (rainbowOffset + (i * 10)) % 360;
                int color = hsvToRgb(hue / 360f, 1.0f, 1.0f);
                color = (0xFF << 24) | (color & 0xFFFFFF);
                
                ctx.drawText(client.textRenderer, 
                           Text.of(chars[i]), currentX, y, color, style.isShadowedText());
                currentX += client.textRenderer.getWidth(Text.of(chars[i]));
            }
        } else {
            int finalColor = textColor;
            
            // Apply glow effect
            if (isGlowing && glowIntensity > 0) {
                int glowColor = lightenColor(textColor, glowIntensity * 0.5f);
                // Draw glow layers
                for (int offset = 1; offset <= 2; offset++) {
                    int glowAlpha = (int) (glowIntensity * 128 / offset);
                    int colorWithGlow = (glowColor & 0x00FFFFFF) | (glowAlpha << 24);
                    
                    ctx.drawText(client.textRenderer, 
                               Text.of(text), x - offset, y, colorWithGlow, false);
                    ctx.drawText(client.textRenderer, 
                               Text.of(text), x + offset, y, colorWithGlow, false);
                    ctx.drawText(client.textRenderer, 
                               Text.of(text), x, y - offset, colorWithGlow, false);
                    ctx.drawText(client.textRenderer, 
                               Text.of(text), x, y + offset, colorWithGlow, false);
                }
            }
            
            // Draw main text
            ctx.drawText(client.textRenderer, 
                       Text.of(text), x, y, finalColor, style.isShadowedText());
        }
    }
    
    private int hsvToRgb(float h, float s, float v) {
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        
        float r, g, b;
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            case 5: r = v; g = p; b = q; break;
            default: r = g = b = 0; break;
        }
        
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }
    
    private int lightenColor(int color, float amount) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * (1 + amount)));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * (1 + amount)));
        int b = Math.min(255, (int) ((color & 0xFF) * (1 + amount)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}