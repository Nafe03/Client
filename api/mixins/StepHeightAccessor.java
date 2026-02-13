package dev.anarchy.waifuhax.api.mixins;

/**
 * Mixin accessor interface for accessing and modifying entity step height
 * 
 * To implement this, create a mixin class:
 * 
 * @Mixin(Entity.class)
 * public abstract class EntityMixin implements StepHeightAccessor {
 *     @Shadow private float stepHeight;
 *     
 *     @Override
 *     public float getStepHeight() {
 *         return this.stepHeight;
 *     }
 *     
 *     @Override
 *     public void setStepHeight(float height) {
 *         this.stepHeight = height;
 *     }
 * }
 * 
 * Add to your mixins.json:
 * "EntityMixin"
 */
public interface StepHeightAccessor {
    float getStepHeight();
    void setStepHeight(float height);
}