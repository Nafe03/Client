package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.gui.components.UIElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class UIListLayout {
    
    public enum FillDirection {
        VERTICAL,
        HORIZONTAL
    }
    
    public enum HorizontalAlignment {
        LEFT,
        CENTER,
        RIGHT
    }
    
    public enum VerticalAlignment {
        TOP,
        CENTER,
        BOTTOM
    }
    
    @Getter @Setter
    private FillDirection fillDirection = FillDirection.VERTICAL;
    
    @Getter @Setter
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    
    @Getter @Setter
    private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
    
    @Getter @Setter
    private float padding = 4;
    
    @Getter @Setter
    private float spacing = 4;
    
    private UIElement parent;
    
    public UIListLayout(UIElement parent) {
        this.parent = parent;
    }
    
    public void update() {
        if (parent == null) return;
        
        List<UIElement> children = parent.getChildren();
        if (children == null || children.isEmpty()) return;
        
        List<UIElement> visibleChildren = new ArrayList<>();
        for (UIElement child : children) {
            if (child.isVisible()) {
                visibleChildren.add(child);
            }
        }
        
        if (visibleChildren.isEmpty()) return;
        
        if (fillDirection == FillDirection.VERTICAL) {
            layoutVertical(visibleChildren);
        } else {
            layoutHorizontal(visibleChildren);
        }
    }
    
    private void layoutVertical(List<UIElement> children) {
        float currentY = padding;
        float maxWidth = 0;
        
        // Calculate max width for alignment
        for (UIElement child : children) {
            maxWidth = Math.max(maxWidth, child.getSize().x);
        }
        
        for (UIElement child : children) {
            float x = calculateHorizontalPosition(child, maxWidth);
            child.setPos(x, currentY);
            currentY += child.getSize().y + spacing;
        }
        
        // Update parent size
        float totalHeight = currentY - spacing + padding;
        float totalWidth = maxWidth + padding * 2;
        parent.setSize((int) totalWidth, (int) totalHeight);
    }
    
    private void layoutHorizontal(List<UIElement> children) {
        float currentX = padding;
        float maxHeight = 0;
        
        // Calculate max height for alignment
        for (UIElement child : children) {
            maxHeight = Math.max(maxHeight, child.getSize().y);
        }
        
        for (UIElement child : children) {
            float y = calculateVerticalPosition(child, maxHeight);
            child.setPos(currentX, y);
            currentX += child.getSize().x + spacing;
        }
        
        // Update parent size
        float totalWidth = currentX - spacing + padding;
        float totalHeight = maxHeight + padding * 2;
        parent.setSize((int) totalWidth, (int) totalHeight);
    }
    
    private float calculateHorizontalPosition(UIElement child, float containerWidth) {
        return switch (horizontalAlignment) {
            case LEFT -> padding;
            case CENTER -> padding + (containerWidth - child.getSize().x) / 2;
            case RIGHT -> padding + (containerWidth - child.getSize().x);
        };
    }
    
    private float calculateVerticalPosition(UIElement child, float containerHeight) {
        return switch (verticalAlignment) {
            case TOP -> padding;
            case CENTER -> padding + (containerHeight - child.getSize().y) / 2;
            case BOTTOM -> padding + (containerHeight - child.getSize().y);
        };
    }
}