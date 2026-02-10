package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
public class UIModuleSelector extends UIElement {

    private AbstractModule mod;

    private float scale = 1f;

    public UIModuleSelector setModule(AbstractModule mod) {
        this.mod = mod;
        addMouseEvent((event) -> {
            if (event.button == 0)
                this.mod.toggle();
            else if (event.button == 1)
                this.mod.toggleSettingsDraw();
        });
        return this;
    }

    @Override
    protected void render(DrawContext ctx) {
        setSize(MinecraftClient.getInstance().textRenderer.getWidth(Text.of(mod.getName())), MinecraftClient.getInstance().textRenderer.fontHeight);

        final Mouse mouse = MinecraftClient.getInstance().mouse;
        final float stupid = (float) (MinecraftClient.getInstance().getWindow().getScaleFactor());
        final boolean hovered = isHovered(mouse.getX() / stupid, mouse.getY() / stupid);
        int color = hovered ? 0xFF00FF00 : 0xFFFFFFFF;

/*        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate((int)getAbsolutePos().x, (int)getAbsolutePos().y);
        ctx.getMatrices().scale(scale, scale);*/
        ctx.drawText(MinecraftClient.getInstance().textRenderer,
                Text.of((mod.isEnabled.getValue() ? ">" : "") + mod.getName()),
                (int)getAbsolutePos().x, (int)getAbsolutePos().y, color, true);
//        ctx.getMatrices().popMatrix();
    }
}