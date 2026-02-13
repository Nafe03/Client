package dev.anarchy.waifuhax.client.systems.modules.movement;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class NoSlow extends AbstractModule {
    
    public final AbstractSetting<Boolean> items = new BooleanSetting("Items", "No slowdown when using items", true);
    public final AbstractSetting<Boolean> web = new BooleanSetting("Web", "No slowdown in cobwebs", true);
    public final AbstractSetting<Boolean> soul = new BooleanSetting("Soul Sand", "No slowdown on soul sand", true);
    public final AbstractSetting<Boolean> slime = new BooleanSetting("Slime", "No slowdown on slime blocks", true);
    public final AbstractSetting<Boolean> honey = new BooleanSetting("Honey", "No slowdown in honey blocks", true);
    public final AbstractSetting<Boolean> berries = new BooleanSetting("Berry Bush", "No slowdown in berry bushes", true);
    public final AbstractSetting<Boolean> powder = new BooleanSetting("Powder Snow", "No slowdown in powder snow", true);
    public final AbstractSetting<Boolean> sneaking = new BooleanSetting("Sneaking", "No slowdown while sneaking", false);
    
    // Advanced settings
    public final AbstractSetting<Float> webSpeed = new FloatSetting("Web Speed", "Movement speed multiplier in webs", 1.0f, 0.1f, 2.0f);
    public final AbstractSetting<Float> soulSpeed = new FloatSetting("Soul Speed", "Movement speed multiplier on soul sand", 1.0f, 0.1f, 2.0f);
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        // Remove item use slowdown
        if (items.getValue() && mc.player.isUsingItem()) {
            // This would typically be handled via mixin to modify the slowdown factor
            // For now, we can modify velocity to counteract slowdown
            if (mc.player.isSprinting() && !mc.player.isOnGround()) {
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x / 0.2, vel.y, vel.z / 0.2);
            }
        }
        
        // Handle sneaking speed
        if (sneaking.getValue() && mc.player.isSneaking()) {
            mc.player.setMovementSpeed(mc.player.getMovementSpeed() * 3.333f); // Counteract 0.3x slowdown
        }
    }
    
    // These would typically be implemented via mixins to modify the actual slowdown factors
    // The methods below are conceptual and would need proper mixin implementation
    
    public boolean shouldCancelWebSlow() {
        return web.getValue();
    }
    
    public float getWebSpeedMultiplier() {
        return webSpeed.getValue();
    }
    
    public boolean shouldCancelSoulSlow() {
        return soul.getValue();
    }
    
    public float getSoulSpeedMultiplier() {
        return soulSpeed.getValue();
    }
    
    public boolean shouldCancelSlimeSlow() {
        return slime.getValue();
    }
    
    public boolean shouldCancelHoneySlow() {
        return honey.getValue();
    }
    
    public boolean shouldCancelBerrySlow() {
        return berries.getValue();
    }
    
    public boolean shouldCancelPowderSnowSlow() {
        return powder.getValue();
    }
    
    public boolean shouldCancelItemSlow() {
        return items.getValue();
    }
    
    public boolean shouldCancelSneakingSlow() {
        return sneaking.getValue();
    }
    
    @Override
    public String getDescription() {
        return "Prevents slowdown from various sources";
    }
}