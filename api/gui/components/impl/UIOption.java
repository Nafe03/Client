package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.gui.animation.AnimationEasing;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;

/**
 * Optimized container for settings
 * Features:
 * - Smooth expand/collapse
 * - No lag when opening
 * - Clean animations
 */
public class UIOption extends UIElement {

    @Getter
    @Setter
    private AbstractSetting setting;
    
    private boolean initialized = false;
    private float expandProgress = 1.0f;
    
    // Layout mode
    private boolean horizontalLayout = false;
    private boolean verticalLayout = false;
    private float spacing = 4;
    
    public UIOption() {
        this.setSize(110, 24);
    }
    
    public UIOption setSetting(AbstractSetting setting) {
        this.setting = setting;
        return this;
    }
    
    public void init() {
        if (initialized || setting == null) return;
        
        // Let the setting create its own UI
        setting.draw(null, this);
        initialized = true;
        
        // Layout children if needed
        if (horizontalLayout || verticalLayout) {
            layoutChildren();
        }
    }
    
    public UIOption setHorizontalLayout() {
        this.horizontalLayout = true;
        this.verticalLayout = false;
        return this;
    }
    
    public UIOption setVerticalLayout() {
        this.verticalLayout = true;
        this.horizontalLayout = false;
        return this;
    }
    
    public UIOption setSpacing(float spacing) {
        this.spacing = spacing;
        return this;
    }
    
    private void layoutChildren() {
        if (childs.isEmpty()) return;
        
        if (horizontalLayout) {
            float currentX = paddingLeft;
            for (UIElement child : childs) {
                child.setPos(currentX, paddingTop);
                currentX += child.getSize().x + child.getMarginLeft() + child.getMarginRight() + spacing;
            }
        } else if (verticalLayout) {
            float currentY = paddingTop;
            for (UIElement child : childs) {
                child.setPos(paddingLeft, currentY);
                currentY += child.getSize().y + child.getMarginTop() + child.getMarginBottom() + spacing;
            }
        }
    }
    
    @Override
    public UIElement addChild(UIElement child) {
        super.addChild(child);
        if (horizontalLayout || verticalLayout) {
            layoutChildren();
        }
        return this;
    }
    
    @Override
    public void setAnimatedProperty(String property, float value) {
        if (property.equals("expandProgress")) {
            this.expandProgress = value;
        } else {
            super.setAnimatedProperty(property, value);
        }
    }
    
    @Override
    protected void render(DrawContext ctx) {
        if (setting == null) return;
        
        // Initialize on first render
        if (!initialized) {
            init();
        }
        
        // Update setting UI if it exists
        if (initialized) {
            setting.draw(ctx, this);
        }
    }
}