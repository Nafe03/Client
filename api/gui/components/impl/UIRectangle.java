package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.gui.components.UIElement;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.gui.DrawContext;

@Slf4j
public class UIRectangle extends UIElement {

    private int color = 0xFFFFFFFF;

    public UIRectangle setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    protected void render(DrawContext ctx) {
        ctx.fill((int) getAbsolutePos().x,
                (int) getAbsolutePos().y,
                (int) (getAbsolutePos().x + size.x),
                (int) (getAbsolutePos().y + size.y),
                color);
    }
}
