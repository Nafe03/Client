package dev.anarchy.waifuhax.client.systems.modules.movement;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class Speed extends AbstractModule {
    
    public enum Mode {
        VANILLA,    // Simple speed multiplier
        BHOP,       // Bunny hop speed
        YPORT,      // Y-port bypass
        STRAFE,     // Strafe speed
        NCP,        // NoCheatPlus bypass
        AAC,        // AAC bypass
        WATCHDOG,   // Watchdog bypass
        GROUND      // Ground only speed
    }
    
    public final AbstractSetting<Mode> mode = new EnumSetting<>("Mode", "Speed mode", Mode.VANILLA);
    public final AbstractSetting<Float> speed = new FloatSetting("Speed", "Speed multiplier", 1.0f, 0.1f, 10.0f);
    public final AbstractSetting<Boolean> autoSprint = new BooleanSetting("Auto Sprint", "Automatically sprint", true);
    public final AbstractSetting<Boolean> strafeOnly = new BooleanSetting("Strafe Only", "Only speed when strafing", false);
    public final AbstractSetting<Float> timer = new FloatSetting("Timer", "Game timer multiplier", 1.0f, 0.1f, 5.0f);
    public final AbstractSetting<Boolean> damageBoost = new BooleanSetting("Damage Boost", "Extra speed when damaged", true);
    
    private int ticksOnGround = 0;
    private double lastDistance = 0;
    private boolean wasOnGround = false;
    private Vec3d lastPos = Vec3d.ZERO;
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null) return;
        
        boolean isMoving = mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
        
        if (!isMoving) {
            lastDistance = 0;
            return;
        }
        
        // Auto sprint
        if (autoSprint.getValue() && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
        
        // Check strafe only
        if (strafeOnly.getValue() && mc.player.forwardSpeed == 0) {
            return;
        }
        
        // Track ground ticks
        if (mc.player.isOnGround()) {
            ticksOnGround++;
        } else {
            ticksOnGround = 0;
        }
        
        Mode currentMode = mode.getValue();
        
        switch (currentMode) {
            case VANILLA:
                handleVanilla();
                break;
                
            case BHOP:
                handleBhop();
                break;
                
            case YPORT:
                handleYPort();
                break;
                
            case STRAFE:
                handleStrafe();
                break;
                
            case NCP:
                handleNCP();
                break;
                
            case AAC:
                handleAAC();
                break;
                
            case WATCHDOG:
                handleWatchdog();
                break;
                
            case GROUND:
                handleGround();
                break;
        }
        
        wasOnGround = mc.player.isOnGround();
        
        // Calculate distance
        Vec3d pos = mc.player.getPos();
        if (lastPos != Vec3d.ZERO) {
            double dx = pos.x - lastPos.x;
            double dz = pos.z - lastPos.z;
            lastDistance = Math.sqrt(dx * dx + dz * dz);
        }
        lastPos = pos;
    }
    
    private void handleVanilla() {
        Vec3d velocity = getMovementVelocity();
        mc.player.setVelocity(
            velocity.x * speed.getValue(),
            mc.player.getVelocity().y,
            velocity.z * speed.getValue()
        );
    }
    
    private void handleBhop() {
        if (mc.player.isOnGround() && wasOnGround) {
            // Jump
            mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
            
            // Speed boost
            Vec3d velocity = getMovementVelocity();
            double boost = speed.getValue() * 0.5;
            mc.player.setVelocity(
                velocity.x * boost,
                mc.player.getVelocity().y,
                velocity.z * boost
            );
        }
    }
    
    private void handleYPort() {
        if (mc.player.isOnGround()) {
            // Teleport up slightly
            Vec3d pos = mc.player.getPos();
            mc.player.setPosition(pos.x, pos.y + 0.4, pos.z);
            
            // Speed boost
            Vec3d velocity = getMovementVelocity();
            mc.player.setVelocity(
                velocity.x * speed.getValue(),
                0,
                velocity.z * speed.getValue()
            );
        }
    }
    
    private void handleStrafe() {
        Vec3d velocity = getMovementVelocity();
        double speedValue = speed.getValue();
        
        if (mc.player.isOnGround()) {
            mc.player.setVelocity(
                velocity.x * speedValue,
                mc.player.getVelocity().y,
                velocity.z * speedValue
            );
        } else {
            // Air strafe
            double airSpeed = Math.min(speedValue, lastDistance + 0.01);
            mc.player.setVelocity(
                velocity.x * airSpeed,
                mc.player.getVelocity().y,
                velocity.z * airSpeed
            );
        }
    }
    
    private void handleNCP() {
        // NoCheatPlus bypass: slower acceleration
        if (mc.player.isOnGround() && ticksOnGround > 2) {
            Vec3d velocity = getMovementVelocity();
            double boost = Math.min(speed.getValue(), 1.0 + ticksOnGround * 0.05);
            
            mc.player.setVelocity(
                velocity.x * boost,
                mc.player.getVelocity().y,
                velocity.z * boost
            );
            
            // Small hop
            if (ticksOnGround % 4 == 0) {
                mc.player.setVelocity(mc.player.getVelocity().x, 0.1, mc.player.getVelocity().z);
            }
        }
    }
    
    private void handleAAC() {
        // AAC bypass: specific movement pattern
        if (mc.player.isOnGround()) {
            Vec3d velocity = getMovementVelocity();
            
            if (ticksOnGround % 3 == 0) {
                // Boost every 3 ticks
                mc.player.setVelocity(
                    velocity.x * speed.getValue() * 0.8,
                    0.05,
                    velocity.z * speed.getValue() * 0.8
                );
            } else {
                mc.player.setVelocity(
                    velocity.x * speed.getValue() * 0.6,
                    mc.player.getVelocity().y,
                    velocity.z * speed.getValue() * 0.6
                );
            }
        }
    }
    
    private void handleWatchdog() {
        // Watchdog bypass: conservative speed
        if (mc.player.isOnGround() && wasOnGround) {
            Vec3d velocity = getMovementVelocity();
            double boost = Math.min(speed.getValue(), 1.3); // Conservative multiplier
            
            mc.player.setVelocity(
                velocity.x * boost,
                mc.player.getVelocity().y,
                velocity.z * boost
            );
        }
    }
    
    private void handleGround() {
        if (!mc.player.isOnGround()) return;
        
        Vec3d velocity = getMovementVelocity();
        mc.player.setVelocity(
            velocity.x * speed.getValue(),
            mc.player.getVelocity().y,
            velocity.z * speed.getValue()
        );
    }
    
    private Vec3d getMovementVelocity() {
        float yaw = mc.player.getYaw();
        float forward = mc.player.forwardSpeed;
        float strafe = mc.player.sidewaysSpeed;
        
        double yawRad = Math.toRadians(yaw);
        double sin = Math.sin(yawRad);
        double cos = Math.cos(yawRad);
        
        double x = strafe * cos - forward * sin;
        double z = forward * cos + strafe * sin;
        
        return new Vec3d(x, 0, z).normalize();
    }
    
    @Override
    public String getDescription() {
        return "Move faster with various bypass modes";
    }
}