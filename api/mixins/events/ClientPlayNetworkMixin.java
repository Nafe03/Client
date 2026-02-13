package dev.anarchy.waifuhax.api.mixins.events;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.events.GameJoinedEvent;
import dev.anarchy.waifuhax.client.events.GameLeftEvent;
import dev.anarchy.waifuhax.client.events.PacketEvent;
import dev.anarchy.waifuhax.client.events.SendingMessageEvent;
import dev.anarchy.waifuhax.client.managers.CommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkMixin {

    @Unique
    boolean sendAnyway;

    @Shadow
    private ClientWorld world;

    @Unique
    private boolean dontSend;

    @Unique
    private boolean worldNotNull;

    @Shadow
    public abstract void sendChatMessage(String content);

    @Inject(method = "onGameJoin", at = @At("HEAD"))
    private void onGameJoinHead(GameJoinS2CPacket packet, CallbackInfo info) {
        worldNotNull = world != null;
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoinTail(GameJoinS2CPacket packet, CallbackInfo info) {
        if (worldNotNull) {
            WaifuHax.EVENT_BUS.post(GameLeftEvent.get());
        }

        WaifuHax.EVENT_BUS.post(GameJoinedEvent.get());
    }

    @Inject(method = "onWorldTimeUpdate", at = @At("TAIL"))
    private void onTimeUpdate(WorldTimeUpdateS2CPacket packet, CallbackInfo ci) {
        WaifuHax.EVENT_BUS.post(new PacketEvent.Receive(packet, MinecraftClient.getInstance().getNetworkHandler().getConnection()));
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (MinecraftClient.getInstance().getNetworkHandler() == null || dontSend) {
            return;
        }

        SendingMessageEvent e = WaifuHax.EVENT_BUS.post(SendingMessageEvent.get(message));

        if (e.isModified() && (!(message.startsWith("!") && !message.startsWith("!!")) || sendAnyway) && !message.startsWith("#") && !message.startsWith("*")) {
            ci.cancel();
            dontSend = true;
            sendChatMessage(e.getMessage());
            dontSend = false;
            return;
        }

        if (message.startsWith("!!")) {
            ci.cancel();
            sendAnyway = true;
            sendChatMessage(message.substring(1));
            sendAnyway = false;
        }
        else if (message.startsWith("!")) {
            try {
                CommandManager.getInstance().dispatch(message.substring(1));
            } catch (CommandSyntaxException ex) {
                WHLogger.printToChat(ex.getMessage());
                ex.printStackTrace();
            }

            MinecraftClient.getInstance().inGameHud.getChatHud().addToMessageHistory(message);
            ci.cancel();
        }
    }

}
