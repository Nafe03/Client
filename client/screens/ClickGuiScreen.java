package dev.anarchy.waifuhax.client.screens;

import dev.anarchy.waifuhax.api.settings.KeybindSetting;
import dev.anarchy.waifuhax.api.systems.modules.Category;
import dev.anarchy.waifuhax.client.WaifuHax;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClickGuiScreen extends Screen {

    public ClickGuiScreen() {
        super(Text.of("ClickGUI"));
        Category.enableAll();
    }

    @Override
    public void close() {
        if (KeybindSetting.currentBind != null) {return;}
        super.close();
        WaifuHax.EVENT_BUS.unsubscribe(this);
        Category.disableAll();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {

    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void blur() {

    }
}
