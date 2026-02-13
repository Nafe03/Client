package dev.anarchy.waifuhax.client.systems.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anarchy.waifuhax.api.gui.WHWindow;
import dev.anarchy.waifuhax.api.systems.commands.AbstractCommand;
import net.minecraft.command.CommandSource;
public class Scale extends AbstractCommand {

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("scale", FloatArgumentType.floatArg(0.01f, 4.0f))
                .executes(ctx -> {
                    if (!Debug.WH_DEBUG_MODE)
                        return 1;
                    float newScale = ctx.getArgument("scale", Float.class);
                    WHWindow.overrideScale = newScale;
                    return 1;
        }));
    }
}
