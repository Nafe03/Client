package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.gui.animation.AnimationEasing;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Fixed module selector with proper hover detection
 * All coordinates received are already scaled by WHWindow
 */
public class UIModuleSelector extends UIElement {

    @Getter
    @Setter
    private AbstractModule module;
    
    private float hoverProgress = 0.0f;
    private float enabledProgress = 0.0f;
    private float clickProgress = 0.0f;
    
    private static final int HEIGHT = 14;
    
    public UIModuleSelector() {
        this.setSize(110, HEIGHT);
        setupEvents();
    }
    
    public UIModuleSelector setModule(AbstractModule module) {
        this.module = module;
        this.enabledProgress = module.isEnabled.getValue() ? 1.0f : 0.0f;
        return this;
    }
    
    private void setupEvents() {
        this.onHoverEnter(() -> {
            animate("hoverProgress", hoverProgress, 1.0f, 150, AnimationEasing.EASE_OUT_QUAD);
        });
        
        this.onHoverExit(() -> {
            animate("hoverProgress", hoverProgress, 0.0f, 150, AnimationEasing.EASE_IN_QUAD);
        });
        
        this.addMouseEvent(event -> {
            if (module == null) return;
            
            // Only process if we're actually hovered
            // Coordinates are already scaled and onClick checks hover
            if (!isHoveredScaled(event.mouseX, event.mouseY)) return;
            
            if (event.button == 0) {
                // Left click - toggle
                module.toggle();
                
                animate("enabledProgress", enabledProgress, 
                       module.isEnabled.getValue() ? 1.0f : 0.0f, 
                       200, AnimationEasing.EASE_OUT_CUBIC);
                
                animate("clickProgress", 0.0f, 1.0f, 100, AnimationEasing.EASE_OUT_QUAD)
                    .onComplete(() -> {
                        animate("clickProgress", 1.0f, 0.0f, 100, AnimationEasing.EASE_IN_QUAD);
                    });
                
            } else if (event.button == 1) {
                // Right click - open settings
                module.toggleSettingsDraw();
                
                animate("clickProgress", 0.0f, 0.5f, 100, AnimationEasing.EASE_OUT_QUAD)
                    .onComplete(() -> {
                        animate("clickProgress", 0.5f, 0.0f, 100, AnimationEasing.EASE_IN_QUAD);
                    });
            }
        });
    }
    
    @Override
    public void setAnimatedProperty(String property, float value) {
        switch (property) {
            case "hoverProgress" -> this.hoverProgress = value;
            case "enabledProgress" -> this.enabledProgress = value;
            case "clickProgress" -> this.clickProgress = value;
            default -> super.setAnimatedProperty(property, value);
        }
    }
    
    @Override
    protected void render(DrawContext ctx) {
        if (module == null) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) return;
        
        int x = (int) getAbsolutePos().x;
        int y = (int) getAbsolutePos().y;
        int width = (int) size.x;
        int height = (int) size.y;
        
        renderBackground(ctx, x, y, width, height);
        renderModuleName(ctx, client, x, y);
        renderKeybind(ctx, client, x, y, width);
    }
    
    private void renderBackground(DrawContext ctx, int x, int y, int width, int height) {
        int bgColor = interpolateColor(0x00000000, 0x15FFFFFF, hoverProgress);
        ctx.fill(x, y, x + width, y + height, bgColor);
        
        if (enabledProgress > 0) {
            int barWidth = 2;
            int barColor = interpolateColor(
                0x00A855F7, 
                0xFFA855F7, 
                enabledProgress
            );
            
            ctx.fill(x, y, x + barWidth, y + height, barColor);
        }
        
        if (clickProgress > 0) {
            int pulseColor = (int) (0x20FFFFFF * clickProgress);
            ctx.fill(x, y, x + width, y + height, pulseColor);
        }
        
        int borderColor = interpolateColor(0x10FFFFFF, 0x20FFFFFF, hoverProgress);
        ctx.fill(x, y + height - 1, x + width, y + height, borderColor);
    }
    
    private void renderModuleName(DrawContext ctx, MinecraftClient client, int x, int y) {
        String name = module.getName();
        
        int baseColor = 0xFFC0C0D0;
        int enabledColor = 0xFFA855F7;
        int hoverColor = 0xFFFFFFFF;
        
        int color = interpolateColor(baseColor, enabledColor, enabledProgress);
        color = interpolateColor(color, hoverColor, hoverProgress * 0.5f);
        
        int textX = x + 6;
        int textY = y + (HEIGHT - client.textRenderer.fontHeight) / 2;
        
        ctx.drawText(client.textRenderer, Text.of(name), textX, textY, color, false);
    }
    
    private void renderKeybind(DrawContext ctx, MinecraftClient client, int x, int y, int width) {
        if (module.keycode == null || module.keycode.getValue() <= 0) return;
        
        String keyName = GLFW.glfwGetKeyName(module.keycode.getValue(), 0);
        if (keyName == null) {
            keyName = "?";
        } else {
            keyName = keyName.toUpperCase();
        }
        
        int keyWidth = client.textRenderer.getWidth(Text.of(keyName));
        int keyX = x + width - keyWidth - 6;
        int keyY = y + (HEIGHT - client.textRenderer.fontHeight) / 2;
        
        int keyColor = interpolateColor(0x60808090, 0x80A0A0B0, hoverProgress);
        ctx.drawText(client.textRenderer, Text.of(keyName), keyX, keyY, keyColor, false);
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