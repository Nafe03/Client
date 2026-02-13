package dev.anarchy.waifuhax.client.systems.modules.movement;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class Jesus extends AbstractModule {
    
    public enum Mode {
        SOLID,      // Walk on water like solid block
        DOLPHIN,    // Jump out of water like dolphin
        BOUNCE,     // Bounce on water surface
        SWIM        // Fast swimming
    }
    
    public final AbstractSetting<Mode> mode = new EnumSetting<>("Mode", "Jesus mode", Mode.SOLID);
    public final AbstractSetting<Boolean> lava = new BooleanSetting("Lava", "Also work on lava", false);
    public final AbstractSetting<Float> speed = new FloatSetting("Speed", "Movement speed on water", 1.0f, 0.1f, 5.0f);
    public final AbstractSetting<Float> jumpHeight = new FloatSetting("Jump Height", "Jump height when on water", 0.42f, 0.1f, 1.0f);
    public final AbstractSetting<Boolean> autoJump = new BooleanSetting("Auto Jump", "Automatically jump on water", false);
    public final AbstractSetting<Boolean> bypassNCPMode = new BooleanSetting("NCP Bypass", "NoCheatPlus bypass mode", false);
    
    private int waterTicks = 0;
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        if (!isOnWater() && !isInWater()) {
            waterTicks = 0;
            return;
        }
        
        waterTicks++;
        
        Mode currentMode = mode.getValue();
        
        switch (currentMode) {
            case SOLID:
                handleSolidMode();
                break;
                
            case DOLPHIN:
                handleDolphinMode();
                break;
                
            case BOUNCE:
                handleBounceMode();
                break;
                
            case SWIM:
                handleSwimMode();
                break;
        }
    }
    
    private void handleSolidMode() {
        if (!isInWater()) return;
        
        // Push player up to surface
        Vec3d velocity = mc.player.getVelocity();
        
        if (bypassNCPMode.getValue()) {
            // NCP bypass: oscillate slightly
            if (waterTicks % 2 == 0) {
                mc.player.setVelocity(velocity.x, 0.1, velocity.z);
            } else {
                mc.player.setVelocity(velocity.x, -0.01, velocity.z);
            }
        } else {
            mc.player.setVelocity(velocity.x, 0.1, velocity.z);
        }
        
        // Apply horizontal speed
        if (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0) {
            Vec3d movementVel = getMovementVelocity();
            mc.player.setVelocity(
                movementVel.x * speed.getValue(),
                mc.player.getVelocity().y,
                movementVel.z * speed.getValue()
            );
        }
        
        // Auto jump
        if (autoJump.getValue() && isOnWater() && mc.player.isOnGround()) {
            mc.player.jump();
        }
        
        // Handle jump key
        if (mc.player.input.playerInput.jump() && isOnWater()) {
            mc.player.setVelocity(mc.player.getVelocity().x, jumpHeight.getValue(), mc.player.getVelocity().z);
        }
    }
    
    private void handleDolphinMode() {
        if (isInWater() && mc.player.input.playerInput.jump()) {
            // Jump out of water
            mc.player.setVelocity(mc.player.getVelocity().x, 0.5, mc.player.getVelocity().z);
        } else if (isOnWater()) {
            // Move quickly on surface
            Vec3d movementVel = getMovementVelocity();
            mc.player.setVelocity(
                movementVel.x * speed.getValue(),
                mc.player.getVelocity().y,
                movementVel.z * speed.getValue()
            );
        }
    }
    
    private void handleBounceMode() {
        if (isOnWater() || isInWater()) {
            Vec3d velocity = mc.player.getVelocity();
            
            if (velocity.y < 0) {
                // Bounce up
                mc.player.setVelocity(velocity.x, Math.abs(velocity.y) * 0.8, velocity.z);
            }
            
            // Horizontal movement
            Vec3d movementVel = getMovementVelocity();
            mc.player.setVelocity(
                movementVel.x * speed.getValue(),
                mc.player.getVelocity().y,
                movementVel.z * speed.getValue()
            );
        }
    }
    
    private void handleSwimMode() {
        if (isInWater()) {
            // Fast swimming
            Vec3d movementVel = getMovementVelocity();
            Vec3d velocity = mc.player.getVelocity();
            
            mc.player.setVelocity(
                movementVel.x * speed.getValue(),
                velocity.y,
                movementVel.z * speed.getValue()
            );
            
            // Fast up/down movement
            if (mc.player.input.playerInput.jump()) {
                mc.player.setVelocity(mc.player.getVelocity().x, speed.getValue() * 0.3, mc.player.getVelocity().z);
            } else if (mc.player.input.playerInput.sneak()) {
                mc.player.setVelocity(mc.player.getVelocity().x, -speed.getValue() * 0.3, mc.player.getVelocity().z);
            }
        }
    }
    
    private boolean isOnWater() {
        if (mc.player == null || mc.world == null) return false;
        
        Box box = mc.player.getBoundingBox().offset(0, -0.01, 0);
        
        return mc.world.getBlockCollisions(mc.player, box).iterator().hasNext() && 
               isFluidAtPos(mc.player.getBlockPos().down());
    }
    
    private boolean isInWater() {
        if (mc.player == null) return false;
        return mc.player.isTouchingWater() || (lava.getValue() && mc.player.isInLava());
    }
    
    private boolean isFluidAtPos(BlockPos pos) {
        if (mc.world == null) return false;
        
        boolean isWater = mc.world.getBlockState(pos).getBlock() instanceof FluidBlock;
        
        if (lava.getValue()) {
            return isWater;
        }
        
        return isWater && mc.world.getFluidState(pos).isStill();
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
        return "Walk on water and lava";
    }
}