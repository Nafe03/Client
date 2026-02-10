package dev.anarchy.waifuhax.client.systems.modules.combat;

import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

enum targetType {
    CLOSEST,        // focus the closest enemy
    FARTHEST,       // focus the farthest enemy
    HIGHEST_HP,     // focus the enemy with the highest health
    LOWEST_HP,      // focus the enemy with the lowest health
    HIGHEST_DANGER,  // focus the enemy that represent the biggest danger for the player
    LOWEST_DANGER   // focus the enemy that represent the lowest danger for the player
}

public class AutoCrystal extends AbstractModule {

    public final EnumSetting targetMode = new EnumSetting("Target Mode", "The way the target is choosen", targetType.CLOSEST);

    private PlayerEntity target;

    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.world == null) return;

        if (target == null || !target.isAlive()) {

        }
    }

    @Override
    public String getDescription() {
        return "Chinese crystal aura";
    }
}