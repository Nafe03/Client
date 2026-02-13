package dev.anarchy.waifuhax.client.systems.modules.misc;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.settings.annotations.CategorySetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.PacketEvent;
import dev.anarchy.waifuhax.client.events.Render3DEvent;
import dev.anarchy.waifuhax.client.events.TickEvent;
import dev.anarchy.waifuhax.client.systems.modules.combat.renderers.AntiAimRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;

/**
 * AntiAim - CS:GO Style Anti-Aim
 * Makes your hitbox harder to hit with crazy rotation manipulation
 * 
 * Features:
 * - Pitch manipulation (up/down look)
 * - Yaw jitter (left/right spin)
 * - Server-side only (looks normal on client)
 * - Multiple jitter modes
 * - Visual indicators for real vs fake rotation
 */
public class AntiAim extends AbstractModule {
    
    public enum PitchMode {
        NONE,       // No pitch change
        UP,         // Look up (sky)
        DOWN,       // Look down (ground)
        JITTER,     // Jitter up/down
        RANDOM,     // Random pitch
        FAKE_UP,    // Fake looking up
        FAKE_DOWN   // Fake looking down
    }
    
    public enum YawMode {
        NONE,       // No yaw change
        SPIN,       // Continuous spin
        JITTER,     // Jitter left/right
        RANDOM,     // Random yaw
        BACKWARDS,  // Look backwards
        SWITCH,     // Switch 180 degrees
        STATIONARY  // Keep current yaw
    }
    
    @CategorySetting(name = "Pitch Settings")
    public final AbstractSetting<PitchMode> pitchMode = new EnumSetting<>("Pitch Mode", "Pitch manipulation mode", PitchMode.DOWN);
    public final AbstractSetting<Float> pitchValue = new FloatSetting("Pitch Value", "Pitch angle", 90.0f, -90.0f, 90.0f)
            .showIf(() -> pitchMode.getValue() == PitchMode.UP || pitchMode.getValue() == PitchMode.DOWN);
    public final AbstractSetting<Float> pitchSpeed = new FloatSetting("Pitch Speed", "Jitter speed", 20.0f, 1.0f, 90.0f)
            .showIf(() -> pitchMode.getValue() == PitchMode.JITTER);
    
    @CategorySetting(name = "Yaw Settings")
    public final AbstractSetting<YawMode> yawMode = new EnumSetting<>("Yaw Mode", "Yaw manipulation mode", YawMode.JITTER);
    public final AbstractSetting<Float> yawSpeed = new FloatSetting("Yaw Speed", "Spin/jitter speed", 30.0f, 1.0f, 180.0f)
            .showIf(() -> yawMode.getValue() == YawMode.SPIN || yawMode.getValue() == YawMode.JITTER);
    public final AbstractSetting<Float> jitterRange = new FloatSetting("Jitter Range", "Jitter angle range", 90.0f, 10.0f, 180.0f)
            .showIf(() -> yawMode.getValue() == YawMode.JITTER);
    
    @CategorySetting(name = "Advanced")
    public final AbstractSetting<Boolean> serverSide = new BooleanSetting("Server Side Only", "Only affect server-side rotation", true);
    public final AbstractSetting<Boolean> onlyMoving = new BooleanSetting("Only Moving", "Only active while moving", false);
    public final AbstractSetting<Boolean> onlyGround = new BooleanSetting("Only Ground", "Only active on ground", false);
    public final AbstractSetting<Integer> switchDelay = new IntegerSetting("Switch Delay", "Delay between switches (ticks)", 5, 1, 40)
            .showIf(() -> yawMode.getValue() == YawMode.SWITCH);
    
    @CategorySetting(name = "Visuals")
    public final AbstractSetting<Boolean> showVisuals = new BooleanSetting("Show Visuals", "Show rotation indicators", true);
    public final AbstractSetting<Boolean> showReal = new BooleanSetting("Show Real", "Show your real rotation (green)", true)
            .showIf(showVisuals::getValue);
    public final AbstractSetting<Boolean> showFake = new BooleanSetting("Show Fake", "Show server rotation (red)", true)
            .showIf(showVisuals::getValue);
    public final AbstractSetting<Boolean> showCircle = new BooleanSetting("Show Circle", "Show rotation circle", false)
            .showIf(showVisuals::getValue);
    public final AbstractSetting<Float> lineLength = new FloatSetting("Line Length", "Length of direction lines", 2.0f, 0.5f, 5.0f)
            .showIf(showVisuals::getValue);
    public final AbstractSetting<Float> lineWidth = new FloatSetting("Line Width", "Width of lines", 2.0f, 1.0f, 5.0f)
            .showIf(showVisuals::getValue);
    public final AbstractSetting<Float> circleRadius = new FloatSetting("Circle Radius", "Radius of rotation circle", 0.5f, 0.1f, 2.0f)
            .showIf(() -> showVisuals.getValue() && showCircle.getValue());
    public final AbstractSetting<Boolean> spinBody = new BooleanSetting("Spin Body", "Also spin body rotation", false);
    
    // Internal state
    private float serverYaw = 0;
    private float serverPitch = 0;
    private float spinAngle = 0;
    private boolean jitterDirection = false;
    private int switchTicks = 0;
    private float originalYaw = 0;
    private float originalPitch = 0;
    private AntiAimRenderer renderer;
    
    @Override
    public String getDescription() {
        return "CS:GO style anti-aim - makes you harder to hit";
    }
    
    @Override
    public void onActivate(boolean live) {
        super.onActivate(live);
        if (mc.player != null) {
            serverYaw = mc.player.getYaw();
            serverPitch = mc.player.getPitch();
            originalYaw = mc.player.getYaw();
            originalPitch = mc.player.getPitch();
            spinAngle = 0;
            jitterDirection = false;
            switchTicks = 0;
            renderer = new AntiAimRenderer(this);
        }
    }
    
    @Override
    public void onDeactivate(boolean live) {
        super.onDeactivate(live);
        if (mc.player != null && serverSide.getValue()) {
            // Restore original rotation
            mc.player.setYaw(originalYaw);
            mc.player.setPitch(originalPitch);
        }
        renderer = null;
    }
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null) return;
        
        // Check conditions
        if (onlyMoving.getValue() && !isMoving()) return;
        if (onlyGround.getValue() && !mc.player.isOnGround()) return;
        
        // Calculate new rotations
        float newYaw = calculateYaw();
        float newPitch = calculatePitch();
        
        // Store server-side rotation
        serverYaw = newYaw;
        serverPitch = newPitch;
        
        // Apply to client if not server-side only
        if (!serverSide.getValue()) {
            mc.player.setYaw(newYaw);
            mc.player.setPitch(newPitch);
            
            if (spinBody.getValue()) {
                mc.player.bodyYaw = newYaw;
            }
        }
        
        switchTicks++;
    }
    
    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null) return;
        if (!serverSide.getValue()) return;
        
        // Modify movement packets to include fake rotation
        if (event.packet instanceof PlayerMoveC2SPacket.Full packet) {
            PlayerMoveC2SPacket.Full modified = new PlayerMoveC2SPacket.Full(
                packet.getX(0),
                packet.getY(0),
                packet.getZ(0),
                serverYaw,
                serverPitch,
                packet.isOnGround(),
                false // horizontalCollision
            );
            event.cancel();
            mc.player.networkHandler.sendPacket(modified);
        }
        else if (event.packet instanceof PlayerMoveC2SPacket.LookAndOnGround packet) {
            PlayerMoveC2SPacket.LookAndOnGround modified = new PlayerMoveC2SPacket.LookAndOnGround(
                serverYaw,
                serverPitch,
                packet.isOnGround(),
                false // horizontalCollision
            );
            event.cancel();
            mc.player.networkHandler.sendPacket(modified);
        }
        else if (event.packet instanceof PlayerMoveC2SPacket.PositionAndOnGround packet) {
            // Add rotation to position packet
            PlayerMoveC2SPacket.Full modified = new PlayerMoveC2SPacket.Full(
                packet.getX(0),
                packet.getY(0),
                packet.getZ(0),
                serverYaw,
                serverPitch,
                packet.isOnGround(),
                false // horizontalCollision
            );
            event.cancel();
            mc.player.networkHandler.sendPacket(modified);
        }
    }
    
    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (renderer != null && showVisuals.getValue()) {
            renderer.render(event.matrices, event.tickDelta);
        }
    }
    
    private float calculateYaw() {
        float currentYaw = mc.player.getYaw();
        
        switch (yawMode.getValue()) {
            case NONE:
                return currentYaw;
                
            case SPIN:
                spinAngle += yawSpeed.getValue();
                if (spinAngle >= 360) spinAngle -= 360;
                return spinAngle;
                
            case JITTER:
                jitterDirection = !jitterDirection;
                float jitterOffset = jitterDirection ? jitterRange.getValue() : -jitterRange.getValue();
                return currentYaw + jitterOffset;
                
            case RANDOM:
                return (float) (Math.random() * 360);
                
            case BACKWARDS:
                return currentYaw + 180;
                
            case SWITCH:
                if (switchTicks >= switchDelay.getValue()) {
                    jitterDirection = !jitterDirection;
                    switchTicks = 0;
                }
                return currentYaw + (jitterDirection ? 180 : 0);
                
            case STATIONARY:
                return currentYaw;
                
            default:
                return currentYaw;
        }
    }
    
    private float calculatePitch() {
        float currentPitch = mc.player.getPitch();
        
        switch (pitchMode.getValue()) {
            case NONE:
                return currentPitch;
                
            case UP:
                return -pitchValue.getValue();
                
            case DOWN:
                return pitchValue.getValue();
                
            case JITTER:
                jitterDirection = !jitterDirection;
                float jitterOffset = jitterDirection ? pitchSpeed.getValue() : -pitchSpeed.getValue();
                return MathHelper.clamp(currentPitch + jitterOffset, -90, 90);
                
            case RANDOM:
                return (float) ((Math.random() * 180) - 90);
                
            case FAKE_UP:
                return -89.9f; // Almost straight up
                
            case FAKE_DOWN:
                return 89.9f; // Almost straight down
                
            default:
                return currentPitch;
        }
    }
    
    private boolean isMoving() {
        return mc.player.input.playerInput.forward() ||
               mc.player.input.playerInput.backward() ||
               mc.player.input.playerInput.left() ||
               mc.player.input.playerInput.right();
    }
    
    // Public methods for renderer and other modules
    public float getServerYaw() {
        return serverYaw;
    }
    
    public float getServerPitch() {
        return serverPitch;
    }
    
    public float getRealYaw() {
        return serverSide.getValue() ? mc.player.getYaw() : serverYaw;
    }
    
    public float getRealPitch() {
        return serverSide.getValue() ? mc.player.getPitch() : serverPitch;
    }
}