package dev.anarchy.waifuhax.api.gui;

import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.components.impl.UICheckBox;
import dev.anarchy.waifuhax.api.gui.components.impl.UIEmpty;
import dev.anarchy.waifuhax.api.gui.components.impl.UIRectangle;
import dev.anarchy.waifuhax.api.gui.components.impl.UIText;
import dev.anarchy.waifuhax.api.gui.components.impl.UISlider;
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

/**
 * Fixed WHWindow with proper coordinate scaling throughout
 * All mouse coordinates are scaled ONCE at entry point, then passed to children
 */
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
                    .setColor(0x30FFFFFF)
                    .setSize(new Vector2f(128, 18))
                    .setIdentifier("RootElement")
                    .setPos(new Vector3f(0, 0, 0))
                    .addChild(new UIRectangle()
                            .setColor(0xD0000000)
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
                                    .setColor(0xFFFFFFFF)
                                    .setIdentifier("WindowName")
                                    .setPos(new Vector3f(3, 4f, 0))
                            )
                    )
            ).addChild(new UIRectangle()
                    .setColor(0x30FFFFFF)
                    .setSize(128, 196)
                    .setPos(0, 18)
                    .setIdentifier("WindowFrame")
                    .addChild(new UIRectangle()
                            .setColor(0xC0000000)
                            .setIdentifier("WindowFrameForeground")
                            .setPos(1, 1)
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
                                .getSize().y - 4));
                                
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
        root.getChildRecursive("WindowFrame").get().setSize(x, y);
        root.getChildRecursive("WindowFrameForeground").get().setSize(x - 2, y - 4);
        root.getChildRecursive("IsActive").get().setPos(new Vector3f(x - (UICheckBox.SIZE / 4) - UICheckBox.SIZE - 1, 2, 0));
        root.getChildRecursive("IsPinned").get().setPos(new Vector3f(x - (((UICheckBox.SIZE / 4) + UICheckBox.SIZE) * 2) - 1, 2, 0));
    }

    @EventHandler
    public void onHudBeingDrawn(DrawnWaifuHudEvent event) {
        final DrawContext ctx = event.getGraphics();
        final float scale = getScale();

        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().scale(scale);

        // CRITICAL: Update hover states EVERY frame with properly scaled coordinates
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.mouse != null) {
            double mouseX = client.mouse.getX();
            double mouseY = client.mouse.getY();
            
            // Scale coordinates ONCE
            double scaledMouseX = mouseX / scale;
            double scaledMouseY = mouseY / scale;
            
            // Update all hover states
            root.updateHoverState(scaledMouseX, scaledMouseY);
        }

        root.renderSelf(event.getGraphics());

        ctx.getMatrices().popMatrix();
    }

    @EventHandler
    public void onHudBeingClicked(HudMouseClickedEvent event) {
        // Scale coordinates ONCE at entry point
        final float scale = getScale();
        final double scaledMouseX = event.mouseX / scale;
        final double scaledMouseY = event.mouseY / scale;
        
        // Update hover states BEFORE processing clicks
        root.updateHoverState(scaledMouseX, scaledMouseY);
        
        // Check if clicking on a slider first
        boolean clickedSlider = checkSliderClick(scaledMouseX, scaledMouseY, event.button);
        
        // Check header hover for window dragging
        if (!clickedSlider && event.button == 0 && selected == null && isHeaderHovered(scaledMouseX, scaledMouseY)) {
            selected = this;
            shouldMove = true;
        }
        
        // Propagate click event to UI elements with scaled coordinates
        HudMouseReleasedEvent releaseEvent = HudMouseReleasedEvent.get(scaledMouseX, scaledMouseY, event.button);
        root.onClick(releaseEvent);
    }
    
    private boolean checkSliderClick(double scaledMouseX, double scaledMouseY, int button) {
        if (button != 0) return false; // Sliders only respond to left click
        
        boolean[] clickedSlider = {false};
        
        executeOnAllChilds(element -> {
            if (element instanceof UISlider slider && slider.isHoveredScaled(scaledMouseX, scaledMouseY)) {
                clickedSlider[0] = true;
            }
        });
        
        return clickedSlider[0];
    }

    public void executeOnAllChilds(Listener<UIElement> event) {
        getRoot().executeOnChilds(event);
    }

    @EventHandler
    public void onMouseDrag(HudMouseDragEvent event) {
        final float scale = getScale();
        final double deltaX = event.deltaX / scale;
        final double deltaY = event.deltaY / scale;
        final double scaledMouseX = event.mouseX / scale;
        final double scaledMouseY = event.mouseY / scale;

        // Don't do anything if pinned
        if (getRoot().getChildRecursive("IsPinned").get().as(UICheckBox.class).state) {
            return;
        }

        // Update hover states during drag
        root.updateHoverState(scaledMouseX, scaledMouseY);

        // Check if any slider is dragging FIRST
        boolean anySliderDragging = isAnySliderDragging();
        
        if (anySliderDragging) {
            // Slider is dragging - ONLY update sliders, don't move window
            propagateDragToSliders(scaledMouseX, scaledMouseY, event.button);
            return;
        }

        // No slider dragging - handle window movement
        if (selected == this && this.shouldMove && event.button == 0) {
            getRoot().setPos(getRoot().getPos().x + deltaX, getRoot().getPos().y + deltaY);
        }
    }

    private boolean isAnySliderDragging() {
        boolean[] dragging = {false};
        executeOnAllChilds(element -> {
            if (element instanceof UISlider slider && slider.isDragging()) {
                dragging[0] = true;
            }
        });
        return dragging[0];
    }

    private void propagateDragToSliders(double scaledMouseX, double scaledMouseY, int button) {
        // Directly update sliders with scaled coordinates
        // They expect coordinates to already be scaled
        executeOnAllChilds(element -> {
            if (element instanceof UISlider slider && slider.isDragging()) {
                // Slider's onMouseDrag just needs the mouse position
                // It already has isDragging state
                slider.updateValueFromScaledMouse(scaledMouseX);
            }
        });
    }

    @EventHandler
    public void onHudBeingReleased(HudMouseReleasedEvent event) {
        final float scale = getScale();
        final double scaledMouseX = event.mouseX / scale;
        final double scaledMouseY = event.mouseY / scale;
        
        // Update hover states before release
        root.updateHoverState(scaledMouseX, scaledMouseY);
        
        // Create scaled event for UI elements
        HudMouseReleasedEvent scaledEvent = HudMouseReleasedEvent.get(scaledMouseX, scaledMouseY, event.button);
        
        // Stop all slider dragging
        executeOnAllChilds(element -> {
            if (element instanceof UISlider slider && slider.isDragging()) {
                slider.stopDragging(scaledEvent);
            }
        });

        // Handle UI click events
        root.onClick(scaledEvent);
        root.onRelease(scaledEvent);

        // Stop window movement
        if (selected == this) {
            selected = null;
            shouldMove = false;
        }
    }

    /**
     * Check if header is hovered
     * Expects SCALED coordinates (already divided by scale factor)
     */
    public boolean isHeaderHovered(double scaledMouseX, double scaledMouseY) {
        UIElement header = root.getChildAtIndex(0);
        if (header == null) return false;
        return header.isHoveredScaled(scaledMouseX, scaledMouseY);
    }
    
    /**
     * Get the current scale factor
     */
    private float getScale() {
        if (Debug.WH_DEBUG_MODE && Debug.WH_WINDOW_OVERRIDE_SCALE) {
            return overrideScale;
        }
        return getGlobalScale();
    }
}