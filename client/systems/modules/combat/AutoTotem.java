package dev.anarchy.waifuhax.client.systems.modules.combat;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends AbstractModule {
    
    public enum Mode {
        SMART,      // Only when health is low or in danger
        ALWAYS,     // Always keep totem in offhand
        INVENTORY   // Keep totems in hotbar too
    }
    
    public final AbstractSetting<Mode> mode = new EnumSetting<>("Mode", "When to equip totems", Mode.SMART);
    public final AbstractSetting<Integer> health = new IntegerSetting("Health", "Health threshold for smart mode", 14, 1, 20);
    public final AbstractSetting<Boolean> elytra = new BooleanSetting("Elytra Priority", "Swap back to elytra when safe", false);
    public final AbstractSetting<Boolean> strict = new BooleanSetting("Strict", "Only swap when inventory is open (safer)", false);
    public final AbstractSetting<Integer> delay = new IntegerSetting("Delay", "Delay between swaps (ms)", 0, 0, 500);
    
    // Hotbar refill
    public final AbstractSetting<Boolean> refillHotbar = new BooleanSetting("Refill Hotbar", "Keep totems in hotbar", true);
    public final AbstractSetting<Integer> hotbarSlots = new IntegerSetting("Hotbar Slots", "Number of hotbar slots for totems", 2, 1, 8);
    
    // Safety
    public final AbstractSetting<Boolean> pauseOnEat = new BooleanSetting("Pause on Eat", "Don't swap while eating", true);
    public final AbstractSetting<Boolean> pauseOnMine = new BooleanSetting("Pause on Mine", "Don't swap while mining", false);
    
    private long lastSwapTime = 0;
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null || mc.interactionManager == null) return;
        
        // Check delays
        if (System.currentTimeMillis() - lastSwapTime < delay.getValue()) {
            return;
        }
        
        // Strict mode check
        if (strict.getValue() && !(mc.currentScreen instanceof InventoryScreen)) {
            return;
        }
        
        // Pause checks
        if (pauseOnEat.getValue() && mc.player.isUsingItem()) {
            return;
        }
        
        if (pauseOnMine.getValue() && mc.interactionManager.isBreakingBlock()) {
            return;
        }
        
        // Check if we need totem
        boolean needTotem = shouldHaveTotem();
        
        // Handle offhand
        if (needTotem && !hasTotemInOffhand()) {
            equipTotem();
        } else if (!needTotem && elytra.getValue() && hasTotemInOffhand()) {
            equipElytra();
        }
        
        // Refill hotbar
        if (refillHotbar.getValue()) {
            refillHotbar();
        }
    }
    
    private boolean shouldHaveTotem() {
        if (mode.getValue() == Mode.ALWAYS) {
            return true;
        }
        
        if (mode.getValue() == Mode.SMART) {
            // Check health
            if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= health.getValue()) {
                return true;
            }
            
            // Check for nearby crystals or dangerous situations
            // Could add explosion prediction here
            
            return false;
        }
        
        return false;
    }
    
    private boolean hasTotemInOffhand() {
        return mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
    }

    
    private void equipTotem() {
        // Find totem in inventory
        int totemSlot = findTotemSlot();
        if (totemSlot == -1) return;
        
        // Swap to offhand
        swapToOffhand(totemSlot);
        lastSwapTime = System.currentTimeMillis();
    }
    
    private void equipElytra() {
        // Find elytra in inventory
        int elytraSlot = findElytraSlot();
        if (elytraSlot == -1) return;
        
        // This would need more complex logic to swap elytra
        // Usually handled differently than totems
    }
    
    private void refillHotbar() {
        int totemsInHotbar = countTotemsInHotbar();
        
        if (totemsInHotbar >= hotbarSlots.getValue()) {
            return;
        }
        
        // Find totem in inventory (not hotbar)
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                // Find empty hotbar slot
                for (int j = 0; j < 9; j++) {
                    if (mc.player.getInventory().getStack(j).isEmpty() || 
                        mc.player.getInventory().getStack(j).getItem() != Items.TOTEM_OF_UNDYING) {
                        
                        // Move to hotbar
                        moveToHotbar(i, j);
                        lastSwapTime = System.currentTimeMillis();
                        return;
                    }
                }
            }
        }
    }
    
    private int findTotemSlot() {
        // Check inventory (slots 9-35 are non-hotbar inventory)
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        
        // Check hotbar if desperate
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        
        return -1;
    }
    
    private int findElytraSlot() {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ELYTRA) {
                return i;
            }
        }
        return -1;
    }
    
    private int countTotemsInHotbar() {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                count++;
            }
        }
        return count;
    }
    
    private void swapToOffhand(int slot) {
        if (mc.interactionManager == null) return;
        
        // Click the slot to pick it up
        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            slot < 9 ? slot + 36 : slot,
            0,
            SlotActionType.PICKUP,
            mc.player
        );
        
        // Click offhand slot (45)
        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            45,
            0,
            SlotActionType.PICKUP,
            mc.player
        );
        
        // Click original slot again to put back what was in offhand
        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            slot < 9 ? slot + 36 : slot,
            0,
            SlotActionType.PICKUP,
            mc.player
        );
    }
    
    private void moveToHotbar(int from, int to) {
        if (mc.interactionManager == null) return;
        
        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            from,
            to,
            SlotActionType.SWAP,
            mc.player
        );
    }
    
    @Override
    public String getDescription() {
        return "Automatically equips totems to prevent death";
    }
}