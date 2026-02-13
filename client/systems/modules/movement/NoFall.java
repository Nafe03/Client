package dev.anarchy.waifuhax.client.systems.modules.movement;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends AbstractModule {
    
    public enum Mode {
        PACKET,      // Spoofs onGround packet
        VELOCITY,    // Resets fall distance
        MIXED        // Combines both methods
    }
    
    public final AbstractSetting<Mode> mode = new EnumSetting<>("Mode", "NoFall mode", Mode.PACKET);
    public final AbstractSetting<Boolean> voidCheck = new BooleanSetting("VoidCheck", "Don't prevent fall damage in void", true);
    
    private static final double VOID_Y = -51.0;
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        // Check if player is in void (optional safety)
        if (voidCheck.getValue() && mc.player.getY() < VOID_Y) {
            return;
        }
        
        Mode currentMode = mode.getValue();
        
        // VELOCITY or MIXED mode - reset fall distance
        if (currentMode == Mode.VELOCITY || currentMode == Mode.MIXED) {
            if (mc.player.fallDistance > 2.0f) {
                mc.player.fallDistance = 0.0f;
            }
        }
        
        // PACKET or MIXED mode - send onGround packets
        if (currentMode == Mode.PACKET || currentMode == Mode.MIXED) {
            // Check if player is falling and would take damage
            if (mc.player.fallDistance > 3.0f && !mc.player.isOnGround()) {
                // Send a packet claiming we're on ground to prevent damage
                if (mc.player.networkHandler != null) {
                    mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.OnGroundOnly(true, mc.player.horizontalCollision)
                    );
                    mc.player.fallDistance = 0.0f;
                }
            }
        }
    }
    
    @Override
    public String getDescription() {
        return "Prevents fall damage";
    }
}