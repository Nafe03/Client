package dev.anarchy.waifuhax.api.mixins;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * EntityStepHeightMixin - Provides access to entity step height
 */
@Mixin(Entity.class)
public abstract class EntityStepHeightMixin implements StepHeightAccessor {
    @Shadow
    private float stepHeight;

    @Override
    public float getStepHeight() {
        return this.stepHeight;
    }

    @Override
    public void setStepHeight(float height) {
        this.stepHeight = height;
    }
}
