package dev.anarchy.waifuhax.client.systems.modules.misc;

import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.api.util.PlayerUtils;
import dev.anarchy.waifuhax.client.events.PlayerEnterRenderDistanceEvent;
import dev.anarchy.waifuhax.client.events.PlayerJoinEvent;
import dev.anarchy.waifuhax.client.events.PlayerLeaveEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;

public class Welcomer extends AbstractModule {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerUtils.sendMessage(Text.of(String.format("Welcome, %s !", event.getPlayer().getProfile().getName())));
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        PlayerUtils.sendMessage(Text.of(String.format("Goodbye %s !", event.getPlayer().getProfile().getName())));
    }

    @EventHandler
    public void onPlayerEnterRenderDistance(PlayerEnterRenderDistanceEvent event) {
        WHLogger.printToChat("%s entered render distance", event.getPlayer().getName().getString());
    }


    @Override
    public String getDescription() {
        return "Automatically welcome players";
    }
}
