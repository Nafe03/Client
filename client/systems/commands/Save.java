package dev.anarchy.waifuhax.client.systems.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anarchy.waifuhax.api.systems.commands.AbstractCommand;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.managers.ModuleManager;
import net.minecraft.command.CommandSource;
public class Save extends AbstractCommand {

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            ModuleManager.getModules().forEach(AbstractModule::save);
            return 1;
        });
    }
}
