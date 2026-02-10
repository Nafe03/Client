package dev.anarchy.waifuhax.client.systems.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.systems.commands.AbstractCommand;
import net.minecraft.command.CommandSource;

public class Ignore extends AbstractCommand {

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", StringArgumentType.word()).executes(context -> {

            WHLogger.printToChat("%s was added to the ignore list, no message containing their username will be shown to you from now on.", context.getArgument("player", String.class));
            WHLogger.printToChat("Note : as it is, does absolutely nothing.");
            return 1;
        }));
    }
}
