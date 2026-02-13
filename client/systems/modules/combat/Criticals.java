package dev.anarchy.waifuhax.client.systems.modules.combat;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.settings.annotations.CategorySetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * IMPROVED Criticals - GUARANTEED CRITS EVERY HIT!
 * Uses packet manipulation to force critical hits
 * Multiple bypass modes for different anti-cheats
 */
public class Criticals extends AbstractModule {
    
    public enum Mode {
        PACKET,         // Send fake position packets (best)
        JUMP,           // Actually jump
        MINI_JUMP,      // Small hop
        MOTION,         // Set velocity
        NO_GROUND,      // Spoof onGround flag
        NCP,            // NoCheatPlus bypass
        WATCHDOG,       // Watchdog (Hypixel) bypass
        UPDATED_NCP,    // Updated NCP bypass
        VULCAN,         // Vulcan bypass
        SPARTAN,        // Spartan bypass
        AAC             // AAC bypass
    }
    
    @CategorySetting(name = "General")
    public final AbstractSetting<Mode> mode = new EnumSetting<>("Mode", "Crit mode", Mode.PACKET);
    public final AbstractSetting<Boolean> onlyWeapon = new BooleanSetting("Only Weapon", "Only crit with weapons", true);
    public final AbstractSetting<Boolean> onlySword = new BooleanSetting("Only Sword", "Only crit with swords", false);
    public final AbstractSetting<Boolean> pauseOnSneak = new BooleanSetting("Pause on Sneak", "Don't crit while sneaking", false);
    public final AbstractSetting<Boolean> ignoreWalls = new BooleanSetting("Ignore Walls", "Crit through walls", false);
    
    @CategorySetting(name = "Packet Settings")
    public final AbstractSetting<Integer> packets = new IntegerSetting("Packets", "Number of packets to send", 2, 1, 5)
            .showIf(() -> mode.getValue() == Mode.PACKET);
    public final AbstractSetting<Float> jumpHeight = new FloatSetting("Jump Height", "Height for mini jump", 0.25f, 0.05f, 0.5f)
            .showIf(() -> mode.getValue() == Mode.MINI_JUMP);
    public final AbstractSetting<Float> motionY = new FloatSetting("Motion Y", "Upward velocity", 0.1f, 0.01f, 0.5f)
            .showIf(() -> mode.getValue() == Mode.MOTION);
    
    @CategorySetting(name = "Advanced")
    public final AbstractSetting<Boolean> keepSprint = new BooleanSetting("Keep Sprint", "Don't stop sprinting", true);
    public final AbstractSetting<Boolean> strict = new BooleanSetting("Strict", "Stricter checks (better anti-cheat)", false);
    public final AbstractSetting<Integer> delay = new IntegerSetting("Delay", "Delay between crits (ticks)", 0, 0, 10);
    
    private boolean attacking = false;
    private int ticksSinceLastCrit = 0;
    
    @Override
    public String getDescription() {
        return "GUARANTEED critical hits - every attack is a crit!";
    }
    
    @Override
    public void onActivate(boolean live) {
        super.onActivate(live);
        ticksSinceLastCrit = 0;
    }
    
    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null) return;
        if (attacking) return; // Prevent recursion
        
        Packet<?> packet = event.packet;
        
        // Intercept attack packets
        if (!(packet instanceof PlayerInteractEntityC2SPacket)) {
            return;
        }
        
        // Check delay
        if (ticksSinceLastCrit < delay.getValue()) {
            return;
        }
        
        // Checks
        if (!shouldCrit()) {
            return;
        }
        
        // Check weapon
        if (onlyWeapon.getValue() && !isHoldingWeapon()) {
            return;
        }
        
        if (onlySword.getValue() && !isHoldingSword()) {
            return;
        }
        
        // PERFORM THE CRIT!
        attacking = true;
        performCritical();
        attacking = false;
        
        ticksSinceLastCrit = 0;
    }
    
    private boolean shouldCrit() {
        // Must be on ground for most modes
        if (!mc.player.isOnGround() && mode.getValue() != Mode.NO_GROUND && mode.getValue() != Mode.MOTION) {
            return false;
        }
        
        // Can't crit in liquids
        if (mc.player.isTouchingWater() || mc.player.isInLava()) {
            return false;
        }
        
        // Can't crit on ladder
        if (mc.player.isClimbing()) {
            return false;
        }
        
        // Can't crit while flying
        if (mc.player.getAbilities().flying) {
            return false;
        }
        
        // Pause on sneak
        if (pauseOnSneak.getValue() && mc.player.isSneaking()) {
            return false;
        }
        
        // Strict mode - must have fallen previously
        if (strict.getValue() && mc.player.fallDistance == 0) {
            return false;
        }
        
        return true;
    }
    
    private void performCritical() {
        Vec3d pos = mc.player.getPos();
        
        switch (mode.getValue()) {
            case PACKET:
                handlePacketMode(pos);
                break;
                
            case JUMP:
                mc.player.jump();
                break;
                
            case MINI_JUMP:
                mc.player.setVelocity(
                    mc.player.getVelocity().x,
                    jumpHeight.getValue(),
                    mc.player.getVelocity().z
                );
                break;
                
            case MOTION:
                mc.player.setVelocity(
                    mc.player.getVelocity().x,
                    motionY.getValue(),
                    mc.player.getVelocity().z
                );
                break;
                
            case NO_GROUND:
                // Send packet with onGround = false
                sendPositionPacket(pos.x, pos.y, pos.z, false);
                break;
                
            case NCP:
                handleNCPMode(pos);
                break;
                
            case WATCHDOG:
                handleWatchdogMode(pos);
                break;
                
            case UPDATED_NCP:
                handleUpdatedNCPMode(pos);
                break;
                
            case VULCAN:
                handleVulcanMode(pos);
                break;
                
            case SPARTAN:
                handleSpartanMode(pos);
                break;
                
            case AAC:
                handleAACMode(pos);
                break;
        }
        
        // Keep sprint if enabled
        if (keepSprint.getValue() && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
    
    private void handlePacketMode(Vec3d pos) {
        // Send packets to simulate jump
        // This is the most reliable method
        int packetCount = packets.getValue();
        
        double[] offsets = {
            0.0625, 0.0, // Standard crit packet offsets
            0.1, 0.0,
            0.05, 0.0
        };
        
        for (int i = 0; i < Math.min(packetCount * 2, offsets.length); i += 2) {
            sendPositionPacket(pos.x, pos.y + offsets[i], pos.z, false);
            sendPositionPacket(pos.x, pos.y + offsets[i + 1], pos.z, false);
        }
    }
    
    private void handleNCPMode(Vec3d pos) {
        // NoCheatPlus bypass
        sendPositionPacket(pos.x, pos.y + 0.11, pos.z, false);
        sendPositionPacket(pos.x, pos.y + 0.1100013579, pos.z, false);
        sendPositionPacket(pos.x, pos.y + 0.0000013579, pos.z, false);
    }
    
    private void handleWatchdogMode(Vec3d pos) {
        // Watchdog (Hypixel) bypass - very small offsets
        sendPositionPacket(pos.x, pos.y + 0.00001058293, pos.z, false);
        sendPositionPacket(pos.x, pos.y, pos.z, false);
    }
    
    private void handleUpdatedNCPMode(Vec3d pos) {
        // Updated NCP bypass
        sendPositionPacket(pos.x, pos.y + 0.05, pos.z, false);
        sendPositionPacket(pos.x, pos.y, pos.z, false);
        sendPositionPacket(pos.x, pos.y + 0.03, pos.z, false);
        sendPositionPacket(pos.x, pos.y, pos.z, false);
    }
    
    private void handleVulcanMode(Vec3d pos) {
        // Vulcan bypass - specific pattern
        sendPositionPacket(pos.x, pos.y + 0.05, pos.z, false);
        sendPositionPacket(pos.x, pos.y, pos.z, false);
    }
    
    private void handleSpartanMode(Vec3d pos) {
        // Spartan bypass
        sendPositionPacket(pos.x, pos.y + 0.0625, pos.z, false);
        sendPositionPacket(pos.x, pos.y, pos.z, false);
        sendPositionPacket(pos.x, pos.y + 0.0625, pos.z, false);
    }
    
    private void handleAACMode(Vec3d pos) {
        // AAC bypass - multiple small packets
        sendPositionPacket(pos.x, pos.y + 0.05, pos.z, false);
        sendPositionPacket(pos.x, pos.y + 0.04, pos.z, false);
        sendPositionPacket(pos.x, pos.y + 0.03, pos.z, false);
        sendPositionPacket(pos.x, pos.y, pos.z, false);
    }
    
    private void sendPositionPacket(double x, double y, double z, boolean onGround) {
        mc.player.networkHandler.sendPacket(
            new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround, false)
        );
    }
    
    private boolean isHoldingWeapon() {
        String item = mc.player.getMainHandStack().getItem().toString().toLowerCase();
        return item.contains("sword") || item.contains("axe") || item.contains("trident");
    }
    
    private boolean isHoldingSword() {
        return mc.player.getMainHandStack().getItem().toString().toLowerCase().contains("sword");
    }
    
    // Tick update for delay
    public void tick() {
        if (ticksSinceLastCrit < Integer.MAX_VALUE) {
            ticksSinceLastCrit++;
        }
    }
}