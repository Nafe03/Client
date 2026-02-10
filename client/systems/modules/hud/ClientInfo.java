package dev.anarchy.waifuhax.client.systems.modules.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.Optional;

public class ClientInfo {

    private int getPing() {
        Optional<PlayerListEntry> self = MinecraftClient.getInstance()
                .getNetworkHandler()
                .getPlayerList()
                .stream()
                .filter(pl -> pl.getProfile().getId().equals(MinecraftClient.getInstance().getGameProfile().getId()))
                .findFirst();

        return self.map(PlayerListEntry::getLatency).orElse(-1);
    }
}
