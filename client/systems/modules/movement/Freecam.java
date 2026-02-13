package dev.anarchy.waifuhax.client.systems.modules.movement;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class Freecam extends AbstractModule {
    
    public final AbstractSetting<Float> speed = new FloatSetting("Speed", "Horizontal movement speed", 1.0f, 0.1f, 10.0f);
    public final AbstractSetting<Boolean> verticalSpeed = new BooleanSetting("Vertical Speed", "Use same speed for vertical movement", false);
    public final AbstractSetting<Float> upSpeed = new FloatSetting("Up Speed", "Upward movement speed", 1.0f, 0.1f, 10.0f);
    public final AbstractSetting<Float> downSpeed = new FloatSetting("Down Speed", "Downward movement speed", 1.0f, 0.1f, 10.0f);
    public final AbstractSetting<Boolean> controlPlayer = new BooleanSetting("Control Player", "Control player body while in freecam", false);
    public final AbstractSetting<Boolean> autoCenter = new BooleanSetting("Auto Center", "Automatically center player at freecam position", false);
    
    private Vec3d originalPos;
    private float originalYaw;
    private float originalPitch;
    private Vec3d originalVelocity;
    
    @Override
    public void onActivate(boolean live) {
        super.onActivate(live);
        if (mc.player == null) return;
        
        originalPos = mc.player.getPos();
        originalYaw = mc.player.getYaw();
        originalPitch = mc.player.getPitch();
        originalVelocity = mc.player.getVelocity();
        
        // Disable collision and gravity
        mc.player.noClip = true;
        mc.player.setVelocity(Vec3d.ZERO);
    }
    
    @Override
    public void onDeactivate(boolean live) {
        super.onDeactivate(live);
        if (mc.player == null) return;
        
        // Restore position and state
        if (autoCenter.getValue() && mc.cameraEntity != null) {
            mc.player.setPos(mc.cameraEntity.getX(), mc.cameraEntity.getY(), mc.cameraEntity.getZ());
        } else {
            mc.player.setPos(originalPos.x, originalPos.y, originalPos.z);
        }
        
        mc.player.setYaw(originalYaw);
        mc.player.setPitch(originalPitch);
        mc.player.setVelocity(originalVelocity);
        
        // Re-enable collision
        mc.player.noClip = false;
    }
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null || mc.cameraEntity == null) return;
        
        // Keep noClip enabled
        mc.player.noClip = true;
        
        // Get input using proper input API
        boolean forward = mc.player.input.playerInput.forward();
        boolean backward = mc.player.input.playerInput.backward();
        boolean left = mc.player.input.playerInput.left();
        boolean right = mc.player.input.playerInput.right();
        boolean jump = mc.player.input.playerInput.jump();
        boolean sneak = mc.player.input.playerInput.sneak();
        
        float forwardValue = forward ? 1.0F : (backward ? -1.0F : 0.0F);
        float sidewaysValue = right ? -1.0F : (left ? 1.0F : 0.0F);
        
        Vec3d velocity = Vec3d.ZERO;
        
        // Horizontal movement using camera yaw
        if (forwardValue != 0 || sidewaysValue != 0) {
            float yaw = mc.cameraEntity.getYaw();
            double yawRad = Math.toRadians(yaw);
            double moveX = sidewaysValue * Math.cos(yawRad) - forwardValue * Math.sin(yawRad);
            double moveZ = sidewaysValue * Math.sin(yawRad) + forwardValue * Math.cos(yawRad);
            
            velocity = velocity.add(moveX, 0, moveZ).multiply(speed.getValue());
        }
        
        // Vertical movement
        if (jump) {
            float upSpeedValue = verticalSpeed.getValue() ? speed.getValue() : upSpeed.getValue();
            velocity = velocity.add(0, upSpeedValue, 0);
        }
        if (sneak) {
            float downSpeedValue = verticalSpeed.getValue() ? speed.getValue() : downSpeed.getValue();
            velocity = velocity.add(0, -downSpeedValue, 0);
        }
        
        // Apply velocity to camera
        mc.cameraEntity.setVelocity(velocity);
        
        // Handle player control
        if (controlPlayer.getValue()) {
            mc.player.setVelocity(velocity);
        } else {
            mc.player.setVelocity(Vec3d.ZERO); // Keep player frozen
        }
        
        // Prevent fall damage
        mc.player.fallDistance = 0;
    }
    
    @Override
    public String getDescription() {
        return "Spectate freely without moving your actual position";
    }
}