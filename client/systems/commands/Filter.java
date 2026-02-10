package dev.anarchy.waifuhax.client.systems.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.systems.commands.AbstractCommand;
import dev.anarchy.waifuhax.client.managers.ModuleManager;
import dev.anarchy.waifuhax.client.systems.modules.misc.AntiSpam;
import net.minecraft.command.CommandSource;
public class Filter extends AbstractCommand {

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add")
                .then(argument("regex", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ModuleManager.getModule(AntiSpam.class).addRule(ctx.getArgument("regex", String.class));
                            WHLogger.printToChat("Added rule §b" + ctx.getArgument("regex", String.class) + "§r");
                            return 1;
                        })
                ));

        builder.then(literal("remove")
                .then(argument("index", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            AntiSpam spam = ModuleManager.getModule(AntiSpam.class);
                            int index = ctx.getArgument("index", Integer.class);
                            final String removed = spam.getRules().get(index);
                            spam.rmRule(index);
                            WHLogger.printToChat("Removed rule §b" + removed + "§r");
                            return 1;
                        })
                ));

        builder.then(literal("list")
                .executes(ctx -> {
                    int i = 0;
                    for (String str : ModuleManager.getModule(AntiSpam.class).getRules()) {
                        WHLogger.printToChat("id: [%s] | §b%s§r", i, str);
                        ++i;
                    }
                    return 1;
                }));
    }
}
