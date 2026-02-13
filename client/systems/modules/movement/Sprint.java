package dev.anarchy.waifuhax.client.systems.modules.movement;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;

public class Sprint extends AbstractModule {
    
    public enum Mode {
        RAGE,        // Sprint in all directions
        LEGIT,       // Only sprint forward
        MULTI_DIR    // Sprint in all directions except backward
    }
    
    public final AbstractSetting<Mode> mode = new EnumSetting<>("Mode", "Sprint mode", Mode.LEGIT);
    public final AbstractSetting<Boolean> inLiquids = new BooleanSetting("In Liquids", "Sprint while in water/lava", false);
    public final AbstractSetting<Boolean> whileHungry = new BooleanSetting("While Hungry", "Sprint even when hungry", true);
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null) return;
        
        // Check if player is too hungry (food level < 6 prevents sprinting)
        if (!whileHungry.getValue() && mc.player.getHungerManager().getFoodLevel() <= 6) {
            return;
        }
        
        // Check if in liquids
        if (!inLiquids.getValue() && (mc.player.isTouchingWater() || mc.player.isInLava())) {
            return;
        }
        
        Mode currentMode = mode.getValue();
        boolean shouldSprint = false;
        
        switch (currentMode) {
            case RAGE:
                // Sprint in any direction with movement
                shouldSprint = mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
                break;
                
            case LEGIT:
                // Only sprint when moving forward
                shouldSprint = mc.player.input.playerInput.forward();
                break;
                
            case MULTI_DIR:
                // Sprint in all directions except backward
                shouldSprint = mc.player.input.playerInput.forward() || 
                              mc.player.input.playerInput.left() || 
                              mc.player.input.playerInput.right();
                break;
        }
        
        if (shouldSprint && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
    
    @Override
    public String getDescription() {
        return "Automatically sprint";
    }
}