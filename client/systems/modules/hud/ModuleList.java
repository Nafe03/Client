package dev.anarchy.waifuhax.client.systems.modules.hud;

import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.components.impl.UICheckBox;
import dev.anarchy.waifuhax.api.gui.components.impl.UIEmpty;
import dev.anarchy.waifuhax.api.gui.components.impl.UIText;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.api.systems.modules.HudElement;
import dev.anarchy.waifuhax.api.systems.modules.annotations.ListHidden;
import dev.anarchy.waifuhax.client.events.hud.DrawnWaifuHudEvent;
import dev.anarchy.waifuhax.client.managers.ModuleManager;
import net.minecraft.client.MinecraftClient;
import org.joml.Vector3f;

@ListHidden
public class ModuleList extends HudElement {

    public ModuleList() {
        super("Enabled Mods");
        UIElement fore = window.getRoot().getChildRecursive("WindowFrameForeground").get();
        fore.addChild(new UIEmpty().setIdentifier("ModuleContainer").setPos(new Vector3f(0, 0, 0)));
    }

    @Override
    public void onRender(DrawnWaifuHudEvent event) {
        UIElement container = window.getRoot().getChildRecursive("ModuleContainer").orElse(null);

        if (container == null)
            return;
        container.clear();
        int y = 5;
        for (AbstractModule mod : ModuleManager.getEnabledModules()) {
            if (!mod.getClass().isAnnotationPresent(ListHidden.class)) {
                container.addChild(new UIText().setText("> " + mod.getName()).setPos(4, y));
                y += 10;
            }
        }
        window.setTitle("Enabled Mods §f(" + container.childSize() + ") §r");
        window.setWindowSize(minWidth(), y + 5);
    }

    public int minWidth() {
        int header = (MinecraftClient.getInstance().textRenderer.getWidth(window.getName()) + 4 + (UICheckBox.SIZE * 2) + ((UICheckBox.SIZE * 3) / 4));
        for (AbstractModule mod : ModuleManager.getEnabledModules()) {
            header = Math.max(header, 4 + MinecraftClient.getInstance().textRenderer.getWidth(mod.getName()) + MinecraftClient.getInstance().textRenderer.getWidth("00"));
        }
        return header;
    }
}
