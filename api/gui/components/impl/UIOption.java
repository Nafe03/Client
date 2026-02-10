package dev.anarchy.waifuhax.api.gui.components.impl;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import net.minecraft.client.gui.DrawContext;
public class UIOption extends UIElement {

    private AbstractSetting setting;

    public UIOption setSetting(AbstractSetting setting) {
        this.setting = setting;
        return this;
    }

    @Override
    protected void render(DrawContext ctx) {
        /*ctx.fill((int) getAbsolutePos().x,
                (int) getAbsolutePos().y,
                (int) (getAbsolutePos().x + size.x),
                (int) (getAbsolutePos().y + size.y),
                0x99999999);*/
        setting.draw(ctx, this);
    }

    @Override
    public void dumpChilds(int y) {
        String str = "|";
        for (int i = 0; i < y; i++)
            str += "-";
        str += (y == 0 ? " " : "> ") + this.elementName + " (" + this.getClass().getSimpleName() + ")";
        WHLogger.printToChat(str);
        WHLogger.printToChat("pos.x = " + getAbsolutePos().x);
        WHLogger.printToChat("pos.y = " + getAbsolutePos().y);
        WHLogger.printToChat("scale.x = " + size.x);
        WHLogger.printToChat("scale.y = " + size.y);
        childs.forEach(ch -> ch.dumpChilds(y + 1));
    }

    public void init() {
        setting.draw(null, this);
    }
}
