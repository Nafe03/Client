package dev.anarchy.waifuhax.api.gui.components;

import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.components.impl.UICheckBox;
import dev.anarchy.waifuhax.api.gui.components.impl.UIContainer;
import dev.anarchy.waifuhax.api.gui.components.impl.UIRectangle;
import dev.anarchy.waifuhax.api.gui.components.impl.UISlider;
import dev.anarchy.waifuhax.api.gui.components.impl.UIText;
import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.events.hud.DrawnWaifuHudEvent;
import dev.anarchy.waifuhax.client.events.hud.HudMouseClickedEvent;
import dev.anarchy.waifuhax.client.events.hud.HudMouseDragEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;

import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.gui.Listener;
import dev.anarchy.waifuhax.api.gui.PARENT_ALIGN;
import dev.anarchy.waifuhax.api.gui.WHWindow;
import dev.anarchy.waifuhax.api.gui.components.impl.UIEmpty;
import dev.anarchy.waifuhax.api.gui.styling.UIStyle;
import dev.anarchy.waifuhax.api.gui.animation.Animation;
import dev.anarchy.waifuhax.api.gui.animation.AnimationEasing;
import dev.anarchy.waifuhax.client.events.hud.HudMouseReleasedEvent;
import dev.anarchy.waifuhax.client.systems.commands.Debug;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.gui.DrawContext;
import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Fixed UIElement with proper coordinate handling
 * ALL mouse coordinates received are already scaled by WHWindow
 */
@Slf4j
public abstract class UIElement {

    @Getter
    protected Vector2f size = new Vector2f(0, 0);

    public Vector2f getSize() { return size; }

    @Getter
    protected Vector3f pos = new Vector3f(0, 0, 0);

    @Getter
    protected UIElement parent = null;

    @Getter
    protected String elementName = "default";

    @Getter
    protected PARENT_ALIGN alignmentToParent = PARENT_ALIGN.TOP_LEFT;

    protected final List<UIElement> childs = new ArrayList<>();

    public List<UIElement> getChildren() { return childs; }

    public final UUID id = UUID.randomUUID();

    private final List<Listener<HudMouseReleasedEvent>> mouseClicked = new ArrayList<>();
    private final List<Listener<HudMouseReleasedEvent>> mouseReleased = new ArrayList<>();
    private final List<Runnable> hoverEnter = new ArrayList<>();
    private final List<Runnable> hoverExit = new ArrayList<>();

    @Setter
    protected WHWindow window;

    @Setter
    protected UIStyle style = new UIStyle();

    public boolean enabled = true;
    
    @Getter
    protected boolean visible = true;

    public boolean isVisible() { return visible; }
    
    // Hover state tracking
    private boolean wasHovered = false;
    private boolean isCurrentlyHovered = false;
    
    // Padding and margin
    @Getter
    protected float paddingTop = 0, paddingBottom = 0, paddingLeft = 0, paddingRight = 0;
    @Getter
    protected float marginTop = 0, marginBottom = 0, marginLeft = 0, marginRight = 0;

    public float getMarginTop() { return marginTop; }
    public float getMarginBottom() { return marginBottom; }
    public float getMarginLeft() { return marginLeft; }
    public float getMarginRight() { return marginRight; }

    public UIElement setEnabled(boolean state) {
        this.enabled = state;
        return this;
    }
    
    public UIElement setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }
    
    public UIElement fadeIn(long duration) {
        this.opacity = 0.0f;
        animate("opacity", 0.0f, 1.0f, duration, AnimationEasing.EASE_OUT);
        return this;
    }
    
    public UIElement fadeOut(long duration) {
        animate("opacity", this.opacity, 0.0f, duration, AnimationEasing.EASE_IN);
        return this;
    }

    public UIElement setPos(Vector3f newPos) {
        this.pos = newPos;
        return this;
    }

    public UIElement setPos(double x, double y) {
        this.pos = new Vector3f((float) x, (float) y, 0);
        return this;
    }
    
    public UIElement setPadding(float padding) {
        this.paddingTop = this.paddingBottom = this.paddingLeft = this.paddingRight = padding;
        return this;
    }
    
    public UIElement setPadding(float vertical, float horizontal) {
        this.paddingTop = this.paddingBottom = vertical;
        this.paddingLeft = this.paddingRight = horizontal;
        return this;
    }
    
    public UIElement setPadding(float top, float right, float bottom, float left) {
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
        this.paddingLeft = left;
        return this;
    }
    
    public UIElement setMargin(float margin) {
        this.marginTop = this.marginBottom = this.marginLeft = this.marginRight = margin;
        return this;
    }
    
    public UIElement setMargin(float vertical, float horizontal) {
        this.marginTop = this.marginBottom = vertical;
        this.marginLeft = this.marginRight = horizontal;
        return this;
    }

    public UIElement setMargin(float top, float right, float bottom, float left) {
        this.marginTop = top;
        this.marginRight = right;
        this.marginBottom = bottom;
        this.marginLeft = left;
        return this;
    }

    public UIElement setAlignementToParent(PARENT_ALIGN newAlign) {
        this.alignmentToParent = newAlign;
        return this;
    }

    public UIElement addMouseEvent(Listener<HudMouseReleasedEvent> event) {
        mouseClicked.add(event);
        return this;
    }
    
    public UIElement onMouseReleased(Listener<HudMouseReleasedEvent> event) {
        mouseReleased.add(event);
        return this;
    }
    
    public UIElement onHoverEnter(Runnable callback) {
        hoverEnter.add(callback);
        return this;
    }
    
    public UIElement onHoverExit(Runnable callback) {
        hoverExit.add(callback);
        return this;
    }

    public UIElement setIdentifier(String id) {
        this.elementName = id;
        return this;
    }

    public UIElement setParent(UIElement parent) {
        this.parent = parent;
        return this;
    }

    public UIElement addChild(UIElement child) {
        child.setParent(this);
        child.setWindow(this.window);
        childs.add(child);
        return this;
    }

    public UIElement setSize(Vector2f size) {
        this.size = size;
        return this;
    }

    public UIElement setSize(int width, int height) {
        this.size = new Vector2f(width, height);
        return this;
    }
    
    public UIElement setStyle(UIStyle style) {
        this.style = style;
        return this;
    }
    
    // Animation support
    protected final List<Animation> animations = new ArrayList<>();
    protected float opacity = 1.0f;
    protected float scale = 1.0f;

    public Animation animate(String property, float from, float to, long duration, AnimationEasing easing) {
        Animation anim = new Animation(this, property, from, to, duration, easing);
        animations.add(anim);
        return anim;
    }
    
    public void updateAnimations(float deltaTime) {
        List<Animation> toUpdate = new ArrayList<>(animations);
        List<Animation> toRemove = new ArrayList<>();
        
        for (Animation anim : toUpdate) {
            anim.update(deltaTime);
            if (anim.isComplete()) {
                toRemove.add(anim);
            }
        }
        
        animations.removeAll(toRemove);
    }
    
    public void setAnimatedProperty(String property, float value) {
        switch (property) {
            case "opacity" -> this.opacity = value;
            case "scale" -> this.scale = value;
            case "x" -> this.pos.x = value;
            case "y" -> this.pos.y = value;
            case "width" -> this.size.x = value;
            case "height" -> this.size.y = value;
        }
    }

    public Vector3f getAbsolutePos() {
        Vector3f absolutePos = new Vector3f(pos);
        absolutePos.x += marginLeft;
        absolutePos.y += marginTop;
        if (parent != null) {
            absolutePos.add(parent.getAbsolutePos());
            absolutePos.x += parent.paddingLeft;
            absolutePos.y += parent.paddingTop;
        }
        return absolutePos;
    }
    
    public Vector2f getContentSize() {
        return new Vector2f(
            size.x - paddingLeft - paddingRight,
            size.y - paddingTop - paddingBottom
        );
    }

    protected abstract void render(DrawContext ctx);

    public void renderSelf(DrawContext ctx) {
        if (!visible) return;
        
        if (enabled) {
            updateAnimations(0.016f);
            
            if (scale != 1.0f) {
                Vector3f absPos = getAbsolutePos();
                float centerX = absPos.x + size.x / 2;
                float centerY = absPos.y + size.y / 2;
                ctx.getMatrices().translate(centerX, centerY);
                ctx.getMatrices().scale(scale, scale);
                ctx.getMatrices().translate(-centerX, -centerY);
            }
            
            render(ctx);
            childs.forEach(child -> child.renderSelf(ctx));
        }
    }
    
    /**
     * Update hover state with properly scaled coordinates
     * Called from WHWindow with already-scaled mouse coordinates
     */
    public void updateHoverState(double scaledMouseX, double scaledMouseY) {
        if (!enabled || !visible) {
            // Not interactive - ensure we're not hovered
            if (wasHovered) {
                hoverExit.forEach(Runnable::run);
                wasHovered = false;
            }
            isCurrentlyHovered = false;
            
            // Still update children
            for (UIElement child : childs) {
                child.updateHoverState(scaledMouseX, scaledMouseY);
            }
            return;
        }
        
        // Check if we're hovered (coordinates are already scaled)
        boolean isHovered = isHoveredScaled(scaledMouseX, scaledMouseY);
        isCurrentlyHovered = isHovered;
        
        // Trigger hover enter/exit callbacks
        if (isHovered && !wasHovered) {
            hoverEnter.forEach(Runnable::run);
        } else if (!isHovered && wasHovered) {
            hoverExit.forEach(Runnable::run);
        }
        
        wasHovered = isHovered;
        
        // Update children
        for (UIElement child : childs) {
            child.updateHoverState(scaledMouseX, scaledMouseY);
        }
    }
    
    /**
     * Check if element is hovered using already-scaled coordinates
     * This is the internal method used by WHWindow after it has scaled coordinates
     */
    public boolean isHoveredScaled(double scaledMouseX, double scaledMouseY) {
        if (!enabled || !visible) return false;
        
        Vector3f absPos = getAbsolutePos();
        return (scaledMouseX >= absPos.x &&
                scaledMouseX <= absPos.x + size.x &&
                scaledMouseY >= absPos.y &&
                scaledMouseY <= absPos.y + size.y);
    }
    
    /**
     * Get current hover state (useful for rendering)
     */
    public boolean isCurrentlyHovered() {
        return isCurrentlyHovered;
    }

    public UIElement getChildAtIndex(int index) {
        if (index < 0 || index >= childs.size()) {return null;}
        return childs.get(index);
    }

    public Optional<UIElement> getChildByName(String name) {
        return childs.stream().filter(child -> child.getElementName().equals(name)).findFirst();
    }

    public Optional<UIElement> getChildRecursive(String name) {
        if (this.getElementName().equals(name)) {return Optional.of(this);}
        for (UIElement child : childs) {
            Optional<UIElement> result = child.getChildRecursive(name);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    public void clear() {
        childs.clear();
    }

    /**
     * Check if element is hovered
     * WARNING: This method expects UNSCALED coordinates for backwards compatibility
     * Internally, it scales them before checking
     */
    public boolean isHovered(double mouseX, double mouseY) {
        float scale = WHWindow.getGlobalScale();
        if (Debug.WH_DEBUG_MODE && Debug.WH_WINDOW_OVERRIDE_SCALE)
            scale = WHWindow.overrideScale;
        double scaledMouseX = mouseX / scale;
        double scaledMouseY = mouseY / scale;
        return isHoveredScaled(scaledMouseX, scaledMouseY);
    }

    /**
     * Handle click events
     * Event coordinates from WHWindow are ALREADY SCALED
     */
    public void onClick(@Nullable HudMouseReleasedEvent event) {
        if (event == null) {
            mouseClicked.forEach(events -> events.onEvent(event));
            childs.forEach(child -> child.onClick(event));
            return;
        }
        
        if (!enabled || !visible) {
            // Still propagate to children for consistency
            childs.forEach(child -> child.onClick(event));
            return;
        }
        
        // Event coordinates are ALREADY SCALED by WHWindow
        boolean isHovered = isHoveredScaled(event.mouseX, event.mouseY);
        
        // Only trigger OUR click handlers if we're actually hovered
        if (isHovered) {
            mouseClicked.forEach(events -> events.onEvent(event));
        }
        
        // Always propagate to children so they can do their own hover checks
        childs.forEach(child -> child.onClick(event));
    }
    
    /**
     * Handle release events
     * Event coordinates from WHWindow are ALREADY SCALED
     */
    public void onRelease(@Nullable HudMouseReleasedEvent event) {
        if (event == null) {
            mouseReleased.forEach(events -> events.onEvent(event));
            childs.forEach(child -> child.onRelease(event));
            return;
        }
        
        if (!enabled || !visible) {
            // Still propagate to children
            childs.forEach(child -> child.onRelease(event));
            return;
        }
        
        // Event coordinates are ALREADY SCALED by WHWindow
        boolean isHovered = isHoveredScaled(event.mouseX, event.mouseY);
        
        // Only trigger OUR release handlers if we're actually hovered
        if (isHovered) {
            mouseReleased.forEach(events -> events.onEvent(event));
        }
        
        // Always propagate to children
        childs.forEach(child -> child.onRelease(event));
    }

    public UIElement getRoot() {
        if (parent == null)
            return this;
        return parent.getRoot();
    }

    public <T extends UIElement> T as(Class<T> type) {
        return (T) this;
    }

    public int childSize() {
        return childs.size();
    }

    public void executeOnChilds(Listener<UIElement> event) {
        event.onEvent(this);
        childs.forEach(element -> element.executeOnChilds(event));
    }

    public void dumpChilds(int y) {
        String str = "|";
        for (int i = 0; i < y; i++)
            str += "-";
        str += (y == 0 ? " " : "> ") + this.elementName + " (" + this.getClass().getSimpleName() + ")";
        WHLogger.printToChat(str);
        childs.forEach(ch -> ch.dumpChilds(y + 1));
    }
}