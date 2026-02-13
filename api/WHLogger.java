package dev.anarchy.waifuhax.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WHLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("Zesthub");

    /**
     * Print stuff to the stdout console
     *
     * @param input  stuff to print
     * @param format make use of String#format to format text
     */
    public static void print(String input, Object... format) {
        LOGGER.info(String.format(input, format));
    }

    public static void printToChat(String input, Object... format) {
        printToChatWithoutPrefix("§1[§bZesthub§1]§r " + input, format);
    }

    public static void printToChatWithoutPrefix(String input, Object... format) {
        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().inGameHud != null && MinecraftClient.getInstance().inGameHud.getChatHud() != null) {
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(String.format(input, format)));
        }
    }
}
