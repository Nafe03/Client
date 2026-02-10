package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.styling.UIStyle;
import net.minecraft.client.gui.DrawContext;

public class UIContainer extends UIElement {
    
    public enum LayoutMode {
        NONE,           // Manual positioning
        VERTICAL,       // Stack vertically
        HORIZONTAL,     // Stack horizontally
        GRID            // Grid layout
    }
    
    private LayoutMode layoutMode = LayoutMode.NONE;
    private float spacing = 4;
    private int gridColumns = 2;
    
    public UIContainer() {
        this.style = UIStyle.builder()
            .backgroundColor(0x00000000)
            .build();
    }
    
    public UIContainer setLayoutMode(LayoutMode mode) {
        this.layoutMode = mode;
        updateLayout();
        return this;
    }
    
    public UIContainer setSpacing(float spacing) {
        this.spacing = spacing;
        updateLayout();
        return this;
    }
    
    public UIContainer setGridColumns(int columns) {
        this.gridColumns = columns;
        if (layoutMode == LayoutMode.GRID) {
            updateLayout();
        }
        return this;
    }
    
    @Override
    public UIElement addChild(UIElement child) {
        super.addChild(child);
        updateLayout();
        return this;
    }
    
    private void updateLayout() {
        if (childs.isEmpty()) return;
        
        switch (layoutMode) {
            case VERTICAL -> layoutVertical();
            case HORIZONTAL -> layoutHorizontal();
            case GRID -> layoutGrid();
            case NONE -> {}
        }
    }
    
    private void layoutVertical() {
        float currentY = paddingTop;
        float maxWidth = 0;
        
        for (UIElement child : childs) {
            if (!child.isVisible()) continue;
            
            child.setPos(paddingLeft + child.getMarginLeft(), 
                        currentY + child.getMarginTop());
            
            currentY += child.getSize().y + child.getMarginTop() + child.getMarginBottom() + spacing;
            maxWidth = Math.max(maxWidth, child.getSize().x + child.getMarginLeft() + child.getMarginRight());
        }
        
        // Auto-resize container
        setSize((int) (maxWidth + paddingLeft + paddingRight), 
                (int) (currentY - spacing + paddingBottom));
    }
    
    private void layoutHorizontal() {
        float currentX = paddingLeft;
        float maxHeight = 0;
        
        for (UIElement child : childs) {
            if (!child.isVisible()) continue;
            
            child.setPos(currentX + child.getMarginLeft(), 
                        paddingTop + child.getMarginTop());
            
            currentX += child.getSize().x + child.getMarginLeft() + child.getMarginRight() + spacing;
            maxHeight = Math.max(maxHeight, child.getSize().y + child.getMarginTop() + child.getMarginBottom());
        }
        
        // Auto-resize container
        setSize((int) (currentX - spacing + paddingRight), 
                (int) (maxHeight + paddingTop + paddingBottom));
    }
    
    private void layoutGrid() {
        float currentX = paddingLeft;
        float currentY = paddingTop;
        float rowHeight = 0;
        int columnIndex = 0;
        
        for (UIElement child : childs) {
            if (!child.isVisible()) continue;
            
            child.setPos(currentX + child.getMarginLeft(), 
                        currentY + child.getMarginTop());
            
            rowHeight = Math.max(rowHeight, child.getSize().y + child.getMarginTop() + child.getMarginBottom());
            
            columnIndex++;
            if (columnIndex >= gridColumns) {
                columnIndex = 0;
                currentX = paddingLeft;
                currentY += rowHeight + spacing;
                rowHeight = 0;
            } else {
                currentX += child.getSize().x + child.getMarginLeft() + child.getMarginRight() + spacing;
            }
        }
        
        // Calculate final size
        float totalWidth = 0;
        float colWidth = 0;
        int col = 0;
        
        for (UIElement child : childs) {
            if (!child.isVisible()) continue;
            
            colWidth = Math.max(colWidth, child.getSize().x + child.getMarginLeft() + child.getMarginRight());
            col++;
            
            if (col >= gridColumns) {
                totalWidth += colWidth + spacing;
                colWidth = 0;
                col = 0;
            }
        }
        
        if (col > 0) {
            totalWidth += colWidth;
        }
        
        setSize((int) (totalWidth + paddingLeft + paddingRight), 
                (int) (currentY + rowHeight + paddingBottom));
    }
    
    @Override
    protected void render(DrawContext ctx) {
        int x = (int) getAbsolutePos().x;
        int y = (int) getAbsolutePos().y;
        int width = (int) size.x;
        int height = (int) size.y;
        
        // Draw background if not transparent
        if ((style.getBackgroundColor() & 0xFF000000) != 0) {
            if (style.getBorderRadius() > 0) {
                drawRoundedRect(ctx, x, y, width, height, 
                              style.getBorderRadius(), style.getBackgroundColor());
            } else {
                ctx.fill(x, y, x + width, y + height, style.getBackgroundColor());
            }
        }
        
        // Draw border
        if (style.getBorderWidth() > 0) {
            drawBorder(ctx, x, y, width, height, 
                      (int) style.getBorderWidth(), style.getBorderColor());
        }
    }
    
    private void drawRoundedRect(DrawContext ctx, int x, int y, int width, int height, 
                                float radius, int color) {
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
    
    private void drawBorder(DrawContext ctx, int x, int y, int width, int height, 
                           int borderWidth, int color) {
        for (int i = 0; i < borderWidth; i++) {
            ctx.drawBorder(x + i, y + i, width - i * 2, height - i * 2, color);
        }
    }
}