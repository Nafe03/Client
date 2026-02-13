package dev.anarchy.waifuhax.api.mixins.events;

import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.events.PlayerJoinEvent;
import dev.anarchy.waifuhax.client.events.PlayerLeaveEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class NetworkHandlerMixin {

    @Inject(at = @At("HEAD"), method = "onPlayerRemove")
    private void onPlayerDisconnect(PlayerRemoveS2CPacket packet, CallbackInfo ci) {

        if (MinecraftClient.getInstance().getNetworkHandler() == null || MinecraftClient.getInstance().player == null) {
            return;
        }

        if (MinecraftClient.getInstance().player.age < 20) {return;}

        for (UUID uUID : packet.profileIds()) {
            PlayerListEntry player = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(uUID);

            if (player != null) {
                WaifuHax.EVENT_BUS.post(PlayerLeaveEvent.get(player));
            }

        }
    }

    @Inject(at = @At("TAIL"), method = "onPlayerList")
    private void onPlayerJoin(PlayerListS2CPacket packet, CallbackInfo ci) {

        if (MinecraftClient.getInstance().getNetworkHandler() == null || MinecraftClient.getInstance().player == null) {
            return;
        }

        if (MinecraftClient.getInstance().player.age < 20) {return;}

        for (PlayerListS2CPacket.Entry entry : packet.getPlayerAdditionEntries()) {
            UUID uUID = entry.profileId();

            PlayerListEntry player = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(uUID);
            if (player != null) {
                WaifuHax.EVENT_BUS.post(PlayerJoinEvent.get(player));
            }
        }

    }

}
