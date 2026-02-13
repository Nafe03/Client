package dev.anarchy.waifuhax.api.mixins.events;

import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
/*
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V", at = @At("HEAD"))
    private void onSendHead(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            WaifuHax.EVENT_BUS.post(new PacketEvent.Send(packet, MinecraftClient.getInstance().getNetworkHandler().getConnection()));
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V", at = @At("TAIL"))
    private void onSendTail(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            WaifuHax.EVENT_BUS.post(new PacketEvent.Sent(packet, MinecraftClient.getInstance().getNetworkHandler().getConnection()));
        }
    }*/
}
