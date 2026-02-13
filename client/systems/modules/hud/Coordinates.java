package dev.anarchy.waifuhax.client.systems.modules.hud;

import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.components.impl.UICheckBox;
import dev.anarchy.waifuhax.api.gui.components.impl.UIText;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.systems.modules.HudElement;
import dev.anarchy.waifuhax.api.util.Dimension;
import dev.anarchy.waifuhax.api.util.PlayerUtils;
import dev.anarchy.waifuhax.client.events.hud.DrawnWaifuHudEvent;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

@Slf4j
public class Coordinates extends HudElement {

    public BooleanSetting singleLine = new BooleanSetting("Inlined", "display all axis on one line", false);

    public BooleanSetting mode = new BooleanSetting("Show nether coords", "if enabled, show relative coordinates in the nether / ow", true);

    private final UIText coordinates = (UIText) new UIText().setText("").setIdentifier("COORDINATES").setPos(3, 4.5f);
    private final UIText coordinates2 = (UIText) new UIText().setText("").setIdentifier("COORDINATES2").setPos(3, 14.5f).setEnabled(false);
    private final UIText coordinates3 = (UIText) new UIText().setText("").setIdentifier("COORDINATES3").setPos(3, 24.5f).setEnabled(false);

    public Coordinates() {
        super("Coordinates");
        UIElement fore = window.getRoot().getChildRecursive("WindowFrameForeground").get();
        fore.addChild(coordinates);
        fore.addChild(coordinates2);
        fore.addChild(coordinates3);
    }

    private ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    public void onRender(DrawnWaifuHudEvent event) {
        double x = round(getPlayer().getPos().x);
        double y = round(getPlayer().getPos().y);
        double z = round(getPlayer().getPos().z);
        boolean nether = PlayerUtils.getDimension().equals(Dimension.NETHER);
        double xAlt = round(nether ? x * 8 : x / 8);
        double zAlt = round(nether ? z * 8 : z / 8);

        coordinates.setText(mode.getValue() ? String.format("X: %s (%s)", x, xAlt) : String.format("X: %s", x));
        coordinates3.setText(mode.getValue() ? String.format("Z: %s (%s)", z, zAlt) : String.format("Z: %s", z));
        coordinates2.setText(String.format("Y: %s", y));

        if (singleLine.getValue()) {
            if (mode.getValue()) {
                coordinates.setText(String.format("X: %s (%s) | Y: %s | Z: %s (%s)", x, xAlt, y, z, zAlt));
            }
            else {
                coordinates.setText(String.format("X: %s | Y: %s | Z: %s", x, y, z));
            }
            coordinates2.setEnabled(false);
            coordinates3.setEnabled(false);
            window.setWindowSize((int) (mc.textRenderer.getWidth(coordinates.getText()) + mc.textRenderer.getWidth("00")), 20);
        }
        else {
            coordinates2.setEnabled(true);
            coordinates3.setEnabled(true);
            window.setWindowSize(maxSize() + mc.textRenderer.getWidth("00"), 40);
        }
    }

    private int maxSize() {
        return Math.max(
                Math.max(mc.textRenderer.getWidth(coordinates.getText()),
                        4 + mc.textRenderer.getWidth("Coordinates") + (UICheckBox.SIZE * 2) + ((UICheckBox.SIZE / 4) * 2)),
                Math.max(mc.textRenderer.getWidth(coordinates2.getText()), mc.textRenderer.getWidth(coordinates3.getText())));
    }

    private double round(double value) {
        return (int) value;
    }
}
