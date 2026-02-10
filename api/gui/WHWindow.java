package dev.anarchy.waifuhax.api.gui;

import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.components.impl.UICheckBox;
import dev.anarchy.waifuhax.api.gui.components.impl.UIEmpty;
import dev.anarchy.waifuhax.api.gui.components.impl.UIRectangle;
import dev.anarchy.waifuhax.api.gui.components.impl.UIText;
import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.events.hud.DrawnWaifuHudEvent;
import dev.anarchy.waifuhax.client.events.hud.HudMouseClickedEvent;
import dev.anarchy.waifuhax.client.events.hud.HudMouseDragEvent;
import dev.anarchy.waifuhax.client.events.hud.HudMouseReleasedEvent;
import dev.anarchy.waifuhax.client.systems.commands.Debug;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Vector2f;
import org.joml.Vector3f;

@Slf4j
public class WHWindow {

    public static float overrideScale = 1.0f;
    public boolean shouldMove = false;

    @Getter
    private String name;

    private Vector2f initialPos;

    private static WHWindow selected = null;

    public static float getGlobalScale() {
        return 1.0f / MinecraftClient.getInstance().getWindow().getScaleFactor() * 2;
    }

    @Getter
    private final UIEmpty root = (UIEmpty) new UIEmpty()
            .addChild(new UIRectangle()
                    .setColor(0x20FFFFFF)
                    .setSize(new Vector2f(128, 17))
                    .setIdentifier("RootElement")
                    .setPos(new Vector3f(0, 0, 0))
                    .addChild(new UIRectangle()
                            .setColor(0xC0000000)
                            .setSize(new Vector2f(126, 16))
                            .setIdentifier("WindowHeader")
                            .addMouseEvent(event -> {
                                if (event.button == 1 && Debug.WH_DEBUG_MODE) {
                                    WHLogger.printToChat("Content of WHWindow " + name);
                                    getRoot().dumpChilds(0);
                                }
                            })
                            .setPos(new Vector3f(1, 1, 0))
                            .addChild(new UICheckBox()
                                    .setActive(true)
                                    .setIdentifier("IsActive")
                                    .setPos(new Vector3f(128 - (UICheckBox.SIZE / 4) - UICheckBox.SIZE - 1, 2, 0))
                            )
                            .addChild(new UICheckBox()
                                    .setActive(false)
                                    .setIdentifier("IsPinned")
                                    .setPos(new Vector3f(128 - (((UICheckBox.SIZE / 4) + UICheckBox.SIZE) * 2) - 1, 2, 0))
                            )
                            .addChild(new UIText()
                                    .setText("Hello World !")
                                    .setScale(1f)
                                    .setColor(0xFF55FF55)
                                    .setIdentifier("WindowName")
                                    .setPos(new Vector3f(3, 4f, 0))
                            )
                    )
            ).addChild(new UIRectangle()
                    .setColor(0x20FFFFFF)
                    .setSize(128, 196)
                    .setPos(0, 18)
                    .setIdentifier("WindowFrame")
                    .addChild(new UIRectangle()
                            .setColor(0x82000000)
                            .setIdentifier("WindowFrameForeground")
                            .setPos(1, 0)
                    )
            );

    public WHWindow(String name, Vector2f initialPos) {
        WaifuHax.EVENT_BUS.subscribe(this);
        this.name = name;
        this.initialPos = initialPos;

        root.setPos(new Vector3f(initialPos, 0));

        root.setIdentifier(name);

        root.getChildRecursive("WindowName")
                .ifPresent((element) -> element.as(UIText.class).setText(name));

        UICheckBox isActive = root.getChildRecursive("IsActive").get().as(UICheckBox.class);

        root.getChildRecursive("IsActive")
                .ifPresent((uiElement -> uiElement.as(UICheckBox.class)
                        .addMouseEvent((event) -> getRoot()
                                .getChildRecursive("WindowFrame")
                                .get().enabled = uiElement.as(UICheckBox.class).state)));

        root.getChildRecursive("WindowFrame")
                .ifPresent(element ->
                        element.as(UIRectangle.class)
                                .setEnabled(isActive.state)
                );

        root.getChildRecursive("WindowFrameForeground")
                .ifPresent((rect) -> rect.as(UIRectangle.class)
                        .setSize(126, (int) rect
                                .getParent()
                                .as(UIRectangle.class)
                                .getSize().y - 3));
        executeOnAllChilds((element) -> {
            element.setWindow(this);
        });
    }

    public void setTitle(String name) {
        this.name = name;
        root.setIdentifier(name);
        root.getChildRecursive("WindowName").get().as(UIText.class).setText(name);
    }

    public void setWindowSize(int x, int y) {
        root.getChildAtIndex(0).setSize(x, 18);
        root.getChildRecursive("WindowHeader").get().setSize(x - 2, 16);
        root.getChildRecursive("WindowFrame").get().setSize(x, y - 2);
        root.getChildRecursive("WindowFrameForeground").get().setSize(x - 2, y - 3);
        root.getChildRecursive("IsActive").get().setPos(new Vector3f(x - (UICheckBox.SIZE / 4) - UICheckBox.SIZE - 1, 2, 0));
        root.getChildRecursive("IsPinned").get().setPos(new Vector3f(x - (((UICheckBox.SIZE / 4) + UICheckBox.SIZE) * 2) - 1, 2, 0));
    }

    @EventHandler
    public void onHudBeingDrawn(DrawnWaifuHudEvent event) {
        final DrawContext ctx = event.getGraphics();
        final float scale = getGlobalScale();

        ctx.getMatrices().pushMatrix();
        //ctx.getMatrices().translate(0, 0);//getRoot().getPos().x, getRoot().getPos().y);
        if (Debug.WH_DEBUG_MODE && Debug.WH_WINDOW_OVERRIDE_SCALE)
            ctx.getMatrices().scale(overrideScale);
        else
            ctx.getMatrices().scale(scale);

        root.renderSelf(event.getGraphics());

        ctx.getMatrices().popMatrix();
    }

    @EventHandler
    public void onHudBeingClicked(HudMouseClickedEvent event) {
        if (isHeaderHovered(event.mouseX, event.mouseY) && event.button == 0 && selected == null) {
            selected = this;
            shouldMove = true;
        }
    }

    public void executeOnAllChilds(Listener<UIElement> event) {
        getRoot().executeOnChilds(event);
    }

    @EventHandler
    public void onMouseDrag(HudMouseDragEvent event) {
        float scale = getGlobalScale();
        if (Debug.WH_DEBUG_MODE && Debug.WH_WINDOW_OVERRIDE_SCALE)
            scale = WHWindow.overrideScale;
        final double deltaX = event.deltaX / scale;
        final double deltaY = event.deltaY / scale;

        if (getRoot().getChildRecursive("IsPinned").get().as(UICheckBox.class).state) {
            return;
        }
        if (selected == this && this.shouldMove && event.button == 0) {
            getRoot().setPos(getRoot().getPos().x + deltaX, getRoot().getPos().y + deltaY);
        }
    }

    @EventHandler
    public void onHudBeingReleased(HudMouseReleasedEvent event) {
        root.onClick(event);
        if (selected == this) {
            selected = null;
            shouldMove = false;
        }
    }

    public boolean isHeaderHovered(double mouseX, double mouseY) {
        return root.getChildAtIndex(0).isHovered(mouseX, mouseY);
    }
}
