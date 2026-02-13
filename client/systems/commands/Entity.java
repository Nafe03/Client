package dev.anarchy.waifuhax.client.systems.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.systems.commands.AbstractCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.concurrent.atomic.AtomicInteger;

// This was the worst attempt at a command I ever made.
// fuck this shit !
public class Entity extends AbstractCommand {

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("entity", IdentifierArgumentType.identifier())
                .suggests((ctx, bld) -> {
                    return CommandSource.suggestIdentifiers(
                            Registries.ENTITY_TYPE.getIds().stream(),
                            bld
                    );
                }).executes(ctx -> {
                    Identifier entityId = ctx.getArgument("entity", Identifier.class);
                    EntityType<?> entityType = Registries.ENTITY_TYPE.get(entityId);

                    if (entityType == Registries.ENTITY_TYPE.getDefaultEntry().get().value()) {
                        WHLogger.printToChat("Unknown entity type: " + entityId);
                        return 0;
                    }
                    AtomicInteger count = new AtomicInteger(0);
                    MinecraftClient.getInstance().world.getEntities().forEach(entity -> {
                        if (entity.getType().equals(entityType))
                            count.addAndGet(1);
                    });
                    WHLogger.printToChat("quantity of " + entityId + " : " + count.get());
                    return 1;
                }));
    /*
                .then(CommandManager.argument("entity_type", IdentifierArgumentType.identifier())
                        .suggests((context, builder) -> {
                            // Suggest all entity types

                        })
                        .executes(this::countEntities)
                )
        );*/
    }
}
