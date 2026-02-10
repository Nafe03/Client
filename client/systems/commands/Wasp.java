package dev.anarchy.waifuhax.client.systems.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.systems.commands.AbstractCommand;
import dev.anarchy.waifuhax.client.systems.wasp.WaspController;
import net.minecraft.command.CommandSource;

public class Wasp extends AbstractCommand {

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("stop").executes(ctx -> {
            if (!WaspController.getInstance().isRunning()) {
                WHLogger.printToChat("You are currently not wasping.");
                return 0;
            }

            WaspController.getInstance().stop();
            WHLogger.printToChat("Stopped the wasp");
            return 1;
        }));

        builder.then(literal("start").then(argument("pointAmount", IntegerArgumentType.integer(10, 10000))
                .then(argument("minDistanceBetweenPoints", IntegerArgumentType.integer(250, 25000))
                        .then(argument("mapSize", IntegerArgumentType.integer(0, 60000000))
                                .executes(ctx -> {
                                    if (WaspController.getInstance().isRunning()) {
                                        WHLogger.printToChat("You already have a WASP instance running !");
                                        return 0;
                                    }

                                    int amount = ctx.getArgument("pointAmount", int.class);
                                    int minDist = ctx.getArgument("minDistanceBetweenPoints", int.class);
                                    int mapSize = ctx.getArgument("mapSize", int.class);
                                    WaspController.init(amount, minDist, mapSize);
                                    return 1;
                                })
                        )
                )
        ));
    }
}
