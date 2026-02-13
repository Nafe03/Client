package dev.anarchy.waifuhax.client.systems.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.systems.commands.AbstractCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;

public class Debug extends AbstractCommand {

    public static boolean WH_DEBUG_MODE = false;
    public static boolean WH_WINDOW_OVERRIDE_SCALE = false;
    public static boolean WH_WINDOW_SHOW_DEBUG_CURSOR = false;

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("toggle").executes(ctx -> {
            WH_DEBUG_MODE = !WH_DEBUG_MODE;
            WHLogger.printToChat("debug=%s", WH_DEBUG_MODE);
            return 1;
        }));

        builder.then(literal("whWindowOverrideScale").executes(ctx -> {
            WH_WINDOW_OVERRIDE_SCALE = !WH_WINDOW_OVERRIDE_SCALE;
            WHLogger.printToChat("whwindowscaleoverride=%s", WH_WINDOW_OVERRIDE_SCALE);
            return 1;
        }));

        builder.then(literal("whDisplayScaledCursor").executes(ctx -> {
            WH_WINDOW_SHOW_DEBUG_CURSOR = !WH_WINDOW_SHOW_DEBUG_CURSOR;
            WHLogger.printToChat("whDisplayScaledCursor=%s", WH_WINDOW_SHOW_DEBUG_CURSOR);
            return 1;
        }));

        builder.then(literal("printWhWindowScale").executes(ctx -> {
            final float scale = 1.0f / MinecraftClient.getInstance().getWindow().getScaleFactor();
            WHLogger.printToChat("scale=%s | scale factor: %s", scale, MinecraftClient.getInstance().getWindow().getScaleFactor());
            return 1;
        }));
    }
}
