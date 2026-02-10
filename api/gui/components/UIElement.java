package dev.anarchy.waifuhax.api.gui.components;

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

@Slf4j
public abstract class UIElement {

    @Getter
    protected Vector2f size = new Vector2f(0, 0);

    @Getter
    protected Vector3f pos = new Vector3f(0, 0, 0);

    @Getter
    protected UIElement parent = null;

    @Getter
    protected String elementName = "default";

    @Getter
    protected PARENT_ALIGN alignmentToParent = PARENT_ALIGN.TOP_LEFT;

    protected final List<UIElement> childs = new ArrayList<>();

    public final UUID id = UUID.randomUUID();

    private final List<Listener<HudMouseReleasedEvent>> mouseClicked = new ArrayList<>();
    private final List<Listener<HudMouseReleasedEvent>> mouseReleased = new ArrayList<>();
    private final List<Runnable> hoverEnter = new ArrayList<>();
    private final List<Runnable> hoverExit = new ArrayList<>();

    @Setter
    protected WHWindow window;

    @Getter
    @Setter
    protected UIStyle style = new UIStyle();

    public boolean enabled = true;
    
    @Getter
    protected boolean visible = true;

    // Animation support
    protected final List<Animation> animations = new ArrayList<>();
    protected float opacity = 1.0f;
    protected float scale = 1.0f;
    
    // Hover state tracking
    private boolean wasHovered = false;
    
    // Padding and margin
    @Getter
    protected float paddingTop = 0, paddingBottom = 0, paddingLeft = 0, paddingRight = 0;
    @Getter
    protected float marginTop = 0, marginBottom = 0, marginLeft = 0, marginRight = 0;

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
    
    // Animation system
    public Animation animate(String property, float from, float to, long duration, AnimationEasing easing) {
        Animation anim = new Animation(this, property, from, to, duration, easing);
        animations.add(anim);
        return anim;
    }
    
    public void updateAnimations(float deltaTime) {
        animations.removeIf(anim -> {
            anim.update(deltaTime);
            return anim.isComplete();
        });
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
            // Update animations
            updateAnimations(0.016f); // ~60 FPS
            
            // Note: DrawContext uses Matrix3x2fStack (2D transformations)
            // For opacity and scale effects, you may need to implement custom rendering
            // or use alternative approaches depending on your rendering system
            
            if (scale != 1.0f) {
                // Scale transformation for 2D
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

    public boolean isHovered(double mouseX, double mouseY) {
        float windowScale = WHWindow.getGlobalScale();
        if (Debug.WH_DEBUG_MODE && Debug.WH_WINDOW_OVERRIDE_SCALE)
            windowScale = WHWindow.overrideScale;
        mouseX /= windowScale;
        mouseY /= windowScale;
        
        Vector3f absPos = getAbsolutePos();
        return (enabled && visible && 
                mouseX > absPos.x &&
                absPos.x + getSize().x > mouseX &&
                mouseY > absPos.y &&
                absPos.y + getSize().y > mouseY);
    }
    
    public void updateHoverState(double mouseX, double mouseY) {
        boolean currentlyHovered = isHovered(mouseX, mouseY);
        
        if (currentlyHovered && !wasHovered) {
            hoverEnter.forEach(Runnable::run);
        } else if (!currentlyHovered && wasHovered) {
            hoverExit.forEach(Runnable::run);
        }
        
        wasHovered = currentlyHovered;
        childs.forEach(child -> child.updateHoverState(mouseX, mouseY));
    }

    public void onClick(@Nullable HudMouseReleasedEvent event) {
        if (event == null || isHovered(event.mouseX, event.mouseY) || this.getClass() == UIEmpty.class) {
            mouseClicked.forEach(events -> events.onEvent(event));
            childs.forEach(child -> child.onClick(event));
        }
    }
    
    public void onRelease(@Nullable HudMouseReleasedEvent event) {
        if (event == null || isHovered(event.mouseX, event.mouseY)) {
            mouseReleased.forEach(events -> events.onEvent(event));
            childs.forEach(child -> child.onRelease(event));
        }
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