package dev.anarchy.waifuhax.client.systems.modules.misc;

import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.StringSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.events.SendingMessageEvent;
import meteordevelopment.orbit.EventHandler;

import java.security.SecureRandom;

public class Suffix extends AbstractModule {

    private final String[] emoji = "☯❤❣⚜☣⁈⁉✪✰✯✭∞♾⌀♪♩♫♬⏻⚝⚧♂♀㊗㊙☮❀✿❁✵❃✾✼❉✷❋✺✹✸✴✳✶☆⯪⯫☽☀⭐★☘❄⚔⛏⚗✂⚓✎✏✒☂☔⌛⏳⌚⚐☕☎⌨✉⌂⚒⚙⚖⚰⚱✈✁✃✄♚♔".split("");
    public StringSetting suffix = new StringSetting("suffix", "stuff to put at the end of your messages", " | WaifuHax B-[GIT_HASH]");
    public BooleanSetting antiantispam = new BooleanSetting("Anti-Anti-Spam", "bypass antispam on some servers", true);

    @EventHandler
    public void onMessageSend(SendingMessageEvent event) {
        if (!event.getMessage().startsWith("!")
                && !event.getMessage()
                .endsWith(suffix.getValue()
                        .replace("[GIT_HASH]", WaifuHax.VERSION))) {
            event.setMessage(event.getMessage() + " " +
                    suffix.getValue().replace("[GIT_HASH]", WaifuHax.VERSION) +
                    (antiantispam.getValue() ? " " +
                            emoji[new SecureRandom().nextInt(0, emoji.length - 1)] : ""));
        }
    }

    @Override
    public String getDescription() {
        return "Turn yourself into a advertiser with this module !";
    }

}
