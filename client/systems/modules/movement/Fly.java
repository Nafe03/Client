package dev.anarchy.waifuhax.client.systems.modules.movement;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class Fly extends AbstractModule {
    
    public enum Mode {
        VANILLA,    // Simple fly
        CREATIVE,   // Creative-like fly
        PACKET,     // Packet-based fly
        GLIDE,      // Glide fly
        JETPACK     // Jetpack-style fly
    }
    
    public final AbstractSetting<Mode> mode = new EnumSetting<>("Mode", "Fly mode", Mode.VANILLA);
    public final AbstractSetting<Float> speed = new FloatSetting("Speed", "Flight speed", 1f, 0.1f, 10f);
    public final AbstractSetting<Float> verticalSpeed = new FloatSetting("Vertical Speed", "Vertical flight speed", 1f, 0.1f, 10f);
    public final AbstractSetting<Boolean> antiKick = new BooleanSetting("AntiKick", "Prevents getting kicked for flying", true);
    public final AbstractSetting<Float> antiKickHeight = new FloatSetting("AntiKick Height", "Height offset for anti-kick", -0.04f, -0.5f, 0.0f);
    public final AbstractSetting<Boolean> bobbing = new BooleanSetting("Bobbing", "View bobbing while flying", false);
    public final AbstractSetting<Boolean> damageCancel = new BooleanSetting("Damage Cancel", "Cancel fall damage on landing", true);
    
    private int flyTicks = 0;
    
    @Override
    public void onActivate(boolean live) {
        super.onActivate(live);
        flyTicks = 0;
        
        if (mc.player != null && damageCancel.getValue()) {
            mc.player.fallDistance = 0;
        }
    }
    
    @Override
    public void onDeactivate(boolean live) {
        super.onDeactivate(live);
        
        if (mc.player != null && damageCancel.getValue()) {
            mc.player.fallDistance = 0;
        }
    }
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null) return;
        
        flyTicks++;
        
        // Cancel fall damage
        if (damageCancel.getValue()) {
            mc.player.fallDistance = 0;
        }
        
        Mode currentMode = mode.getValue();
        
        switch (currentMode) {
            case VANILLA:
                handleVanilla();
                break;
                
            case CREATIVE:
                handleCreative();
                break;
                
            case PACKET:
                handlePacket();
                break;
                
            case GLIDE:
                handleGlide();
                break;
                
            case JETPACK:
                handleJetpack();
                break;
        }
        
        // View bobbing
        if (!bobbing.getValue()) {
            mc.player.setSprinting(false);
        }
    }
    
    private void handleVanilla() {
        // Get input using the new PlayerInput API
        boolean forward = mc.player.input.playerInput.forward();
        boolean backward = mc.player.input.playerInput.backward();
        boolean left = mc.player.input.playerInput.left();
        boolean right = mc.player.input.playerInput.right();
        boolean jump = mc.player.input.playerInput.jump();
        boolean sneak = mc.player.input.playerInput.sneak();
        
        float forwardValue = forward ? 1.0F : (backward ? -1.0F : 0.0F);
        float sidewaysValue = right ? -1.0F : (left ? 1.0F : 0.0F);
        
        Vec3d velocity = Vec3d.ZERO;
        
        // Horizontal movement
        if (forwardValue != 0 || sidewaysValue != 0) {
            float yaw = mc.player.getYaw();
            double yawRad = Math.toRadians(yaw);
            
            velocity = velocity.add(
                sidewaysValue * Math.cos(yawRad) - forwardValue * Math.sin(yawRad),
                0,
                sidewaysValue * Math.sin(yawRad) + forwardValue * Math.cos(yawRad)
            ).multiply(speed.getValue());
        }
        
        // Vertical movement
        if (jump) {
            velocity = velocity.add(0, verticalSpeed.getValue(), 0);
        }
        if (sneak) {
            velocity = velocity.add(0, -verticalSpeed.getValue(), 0);
        }
        
        // Anti-kick
        if (antiKick.getValue() && !jump && !sneak) {
            if (flyTicks % 40 == 0) {
                velocity = velocity.add(0, antiKickHeight.getValue(), 0);
            }
        }
        
        mc.player.setVelocity(velocity);
    }
    
    private void handleCreative() {
        // Creative-style flying with abilities
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed(speed.getValue() * 0.05f);
        
        // Still handle anti-kick
        if (antiKick.getValue() && flyTicks % 40 == 0) {
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, antiKickHeight.getValue(), vel.z);
        }
    }
    
    private void handlePacket() {
        // Packet-based fly would send specific packets
        // For now, use vanilla approach with packet optimization
        handleVanilla();
        
        // Additional packet handling could be added here
        // Such as sending on-ground packets periodically
        if (antiKick.getValue() && flyTicks % 20 == 0) {
            // Send on-ground packet (would require packet implementation)
        }
    }
    
    private void handleGlide() {
        Vec3d velocity = mc.player.getVelocity();
        
        // Get horizontal input
        boolean forward = mc.player.input.playerInput.forward();
        boolean backward = mc.player.input.playerInput.backward();
        boolean left = mc.player.input.playerInput.left();
        boolean right = mc.player.input.playerInput.right();
        
        float forwardValue = forward ? 1.0F : (backward ? -1.0F : 0.0F);
        float sidewaysValue = right ? -1.0F : (left ? 1.0F : 0.0F);
        
        // Horizontal control
        if (forwardValue != 0 || sidewaysValue != 0) {
            float yaw = mc.player.getYaw();
            double yawRad = Math.toRadians(yaw);
            
            double x = sidewaysValue * Math.cos(yawRad) - forwardValue * Math.sin(yawRad);
            double z = sidewaysValue * Math.sin(yawRad) + forwardValue * Math.cos(yawRad);
            
            velocity = velocity.add(x * speed.getValue() * 0.1, 0, z * speed.getValue() * 0.1);
        }
        
        // Slow fall
        if (velocity.y < -0.1) {
            velocity = new Vec3d(velocity.x, -0.1, velocity.z);
        }
        
        // Vertical control
        if (mc.player.input.playerInput.jump()) {
            velocity = velocity.add(0, verticalSpeed.getValue() * 0.5, 0);
        }
        if (mc.player.input.playerInput.sneak()) {
            velocity = velocity.add(0, -verticalSpeed.getValue() * 0.5, 0);
        }
        
        mc.player.setVelocity(velocity);
    }
    
    private void handleJetpack() {
        Vec3d velocity = mc.player.getVelocity();
        
        // Jetpack only activates with jump
        if (mc.player.input.playerInput.jump()) {
            // Get horizontal input
            boolean forward = mc.player.input.playerInput.forward();
            boolean backward = mc.player.input.playerInput.backward();
            boolean left = mc.player.input.playerInput.left();
            boolean right = mc.player.input.playerInput.right();
            
            float forwardValue = forward ? 1.0F : (backward ? -1.0F : 0.0F);
            float sidewaysValue = right ? -1.0F : (left ? 1.0F : 0.0F);
            
            Vec3d newVelocity = Vec3d.ZERO;
            
            // Horizontal thrust
            if (forwardValue != 0 || sidewaysValue != 0) {
                float yaw = mc.player.getYaw();
                double yawRad = Math.toRadians(yaw);
                
                newVelocity = newVelocity.add(
                    sidewaysValue * Math.cos(yawRad) - forwardValue * Math.sin(yawRad),
                    0,
                    sidewaysValue * Math.sin(yawRad) + forwardValue * Math.cos(yawRad)
                ).multiply(speed.getValue() * 0.5);
            }
            
            // Upward thrust
            newVelocity = newVelocity.add(0, verticalSpeed.getValue(), 0);
            
            mc.player.setVelocity(velocity.add(newVelocity.multiply(0.3)));
        } else {
            // Gravity when not thrusting
            if (velocity.y > -0.5) {
                mc.player.setVelocity(velocity.x * 0.95, velocity.y - 0.08, velocity.z * 0.95);
            }
        }
    }
    
    @Override
    public String getDescription() {
        return "Allows you to fly with multiple modes and anti-kick";
    }
}