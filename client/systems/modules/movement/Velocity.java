package dev.anarchy.waifuhax.client.systems.modules.movement;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class Velocity extends AbstractModule {
    
    public enum Mode {
        CANCEL,     // Cancel all knockback
        MODIFY,     // Modify knockback by percentage
        REVERSE,    // Reverse knockback direction
        JUMP,       // Jump on hit
        PACKET      // Packet-based velocity cancel
    }
    
    public final AbstractSetting<Mode> mode = new EnumSetting<>("Mode", "Velocity mode", Mode.MODIFY);
    public final AbstractSetting<Float> horizontal = new FloatSetting("Horizontal", "Horizontal knockback modifier", 0.0f, -200f, 200f);
    public final AbstractSetting<Float> vertical = new FloatSetting("Vertical", "Vertical knockback modifier", 0.0f, -200f, 200f);
    public final AbstractSetting<Boolean> onlyPlayers = new BooleanSetting("Only Players", "Only modify knockback from players", false);
    public final AbstractSetting<Boolean> onlyWhileMoving = new BooleanSetting("Only Moving", "Only modify while moving", false);
    public final AbstractSetting<Float> chance = new FloatSetting("Chance", "Chance to modify velocity (%)", 100f, 0f, 100f);
    
    private int hitTicks = 0;
    private Vec3d lastVelocity = Vec3d.ZERO;
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null) return;
        
        if (hitTicks > 0) {
            hitTicks--;
            
            // Check conditions
            if (onlyWhileMoving.getValue()) {
                boolean isMoving = mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
                if (!isMoving) return;
            }
            
            // Check chance
            if (chance.getValue() < 100f && Math.random() * 100 > chance.getValue()) {
                return;
            }
            
            Mode currentMode = mode.getValue();
            
            switch (currentMode) {
                case CANCEL:
                    handleCancel();
                    break;
                    
                case MODIFY:
                    handleModify();
                    break;
                    
                case REVERSE:
                    handleReverse();
                    break;
                    
                case JUMP:
                    handleJump();
                    break;
                    
                case PACKET:
                    handlePacket();
                    break;
            }
        }
        
        lastVelocity = mc.player.getVelocity();
    }
    
    private void handleCancel() {
        mc.player.setVelocity(lastVelocity);
    }
    
    private void handleModify() {
        Vec3d velocity = mc.player.getVelocity();
        Vec3d velocityChange = velocity.subtract(lastVelocity);
        
        double horizontalMod = horizontal.getValue() / 100.0;
        double verticalMod = vertical.getValue() / 100.0;
        
        double newX = lastVelocity.x + velocityChange.x * horizontalMod;
        double newY = lastVelocity.y + velocityChange.y * verticalMod;
        double newZ = lastVelocity.z + velocityChange.z * horizontalMod;
        
        mc.player.setVelocity(newX, newY, newZ);
    }
    
    private void handleReverse() {
        Vec3d velocity = mc.player.getVelocity();
        Vec3d velocityChange = velocity.subtract(lastVelocity);
        
        double newX = lastVelocity.x - velocityChange.x;
        double newY = lastVelocity.y;
        double newZ = lastVelocity.z - velocityChange.z;
        
        mc.player.setVelocity(newX, newY, newZ);
    }
    
    private void handleJump() {
        if (mc.player.isOnGround()) {
            mc.player.jump();
        }
    }
    
    private void handlePacket() {
        // This would typically intercept velocity packets
        // For now, just cancel the velocity
        mc.player.setVelocity(lastVelocity);
    }
    
    // This method should be called when the player is hit
    // Typically this would be done via an event or mixin
    public void onHit() {
        hitTicks = 2; // Track for 2 ticks
    }
    
    // Method to check if attacker is a player
    public void onHitBy(net.minecraft.entity.Entity attacker) {
        if (onlyPlayers.getValue()) {
            if (!(attacker instanceof net.minecraft.entity.player.PlayerEntity)) {
                return;
            }
        }
        onHit();
    }
    
    @Override
    public String getDescription() {
        return "Modify or cancel knockback velocity";
    }
}