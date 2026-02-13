package dev.anarchy.waifuhax.api.gui.animation;

import dev.anarchy.waifuhax.api.gui.components.UIElement;
import lombok.Getter;

public class Animation {
    
    private final UIElement target;
    private final String property;
    private final float fromValue;
    private final float toValue;
    private final long duration;
    private final AnimationEasing easing;
    
    @Getter
    private long elapsed = 0;
    
    @Getter
    private boolean complete = false;
    
    private Runnable onComplete;
    
    public Animation(UIElement target, String property, float from, float to, long duration, AnimationEasing easing) {
        this.target = target;
        this.property = property;
        this.fromValue = from;
        this.toValue = to;
        this.duration = duration;
        this.easing = easing;
    }
    
    public Animation onComplete(Runnable callback) {
        this.onComplete = callback;
        return this;
    }
    
    public void update(float deltaTime) {
        if (complete) return;
        
        elapsed += (long) (deltaTime * 1000);
        
        if (elapsed >= duration) {
            elapsed = duration;
            complete = true;
            target.setAnimatedProperty(property, toValue);
            if (onComplete != null) {
                onComplete.run();
            }
        } else {
            float progress = (float) elapsed / duration;
            float easedProgress = easing.apply(progress);
            float currentValue = fromValue + (toValue - fromValue) * easedProgress;
            target.setAnimatedProperty(property, currentValue);
        }
    }
    
    public void reset() {
        elapsed = 0;
        complete = false;
    }
}