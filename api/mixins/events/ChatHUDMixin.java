package dev.anarchy.waifuhax.api.mixins.events;

import dev.anarchy.waifuhax.api.util.PlayerUtils;
import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.events.MessageReceivedEvent;
import dev.anarchy.waifuhax.client.managers.ModuleManager;
import dev.anarchy.waifuhax.client.systems.modules.misc.AntiSpam;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatHud.class)
public abstract class ChatHUDMixin {


    @Unique
    private boolean ignore = false;

    @Shadow
    public abstract void addMessage(Text message);

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), cancellable = true)
    private void onMessageReceived(Text message, MessageSignatureData signature, MessageIndicator indicator, CallbackInfo ci) {
        for (String str : ModuleManager.getModule(AntiSpam.class).getRules()) {
            if (matches(str, message.getString())) {
                ci.cancel();
                return;
            }
        }
        WaifuHax.EVENT_BUS.post(MessageReceivedEvent.get("null", message.getString()));
    }

    @Inject(method = "addToMessageHistory", at = @At("HEAD"), cancellable = true)
    private void onAddMessageToHistory(String message, CallbackInfo ci) {
        if (!PlayerUtils.saveCurrentMessage) {ci.cancel();}
    }

    @Unique
    private static boolean matches(String regex, CharSequence input) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        return m.find();
    }
}
