package dev.anarchy.waifuhax.client.systems.modules.world;

import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.api.util.InventoryUtils;
import dev.anarchy.waifuhax.client.events.TickEvent;
import dev.anarchy.waifuhax.client.managers.ModuleManager;
import dev.anarchy.waifuhax.client.systems.modules.exploits.AutoFrameDupe;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

public class TntHelper extends AbstractModule {

    public BooleanSetting silentPlace = new BooleanSetting("Silent Place", "Place the tnt without switching in your hotbar", false);
    public IntegerSetting placeRange = new IntegerSetting("Place Range", "Range at which tnts can be placed", 3, 0, 6);

    public BooleanSetting autoIgnite = new BooleanSetting("Auto Ignite", "Automatically ignite any tnt in range", true);
    public BooleanSetting silentIgnite = new BooleanSetting("Silent Ignite", "Silently ignite the tnts", true);
    public IntegerSetting igniteRange = new IntegerSetting("Auto Ignite Range", "Range of the auto-ignite in block", 3, 0, 6);

    public BooleanSetting autoRefill = new BooleanSetting("Auto Refill", "Automatically place tnt in your hotbar", true);

    @EventHandler
    private void onTick(TickEvent event) {

        if (ModuleManager.getModule(AutoFrameDupe.class).isEnabled.getValue()) {
            return;
        }

        // refill
        if (InventoryUtils.searchHotbar(itemStack -> itemStack.getItem().equals(Items.TNT)) == -1) {
            if (!attemptRefill()) {
                WHLogger.printToChat("You ran out of tnt !");
                toggle();
            }
        }

        // place tnt


        // ignite tnt
    }

    private boolean attemptRefill() {
        int slot = InventoryUtils.searchNonHotbar(item -> item.getItem().equals(Items.TNT));

        if (slot == -1) {return false;}

        return InventoryUtils.refill(item -> item.getItem().equals(Items.TNT));
    }

    @Override
    public String getDescription() {
        return "Place tnt efficiently for griefing purpose";
    }
}
