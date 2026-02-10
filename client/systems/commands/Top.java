package dev.anarchy.waifuhax.client.systems.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.systems.commands.AbstractCommand;
import dev.anarchy.waifuhax.api.util.BlockUtils;
import dev.anarchy.waifuhax.api.util.TpUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;

public class Top extends AbstractCommand {

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            // tp to center
            TpUtils.Tp(mc.player.getBlockPos().toCenterPos());

            BlockPos nextPos = BlockUtils.findFirstSafeSpaceOnTopOfPlayer();

            if (nextPos != null) {
                nextPos = nextPos.add(0, 1, 0);
                WHLogger.printToChat("currentY=%s, newY=%s. Expected variation: %s", mc.player.getBlockY(), nextPos.getY(), (nextPos.getY() - mc.player.getBlockY()));
                TpUtils.Tp(nextPos.toCenterPos());
                return 1;
            }

            WHLogger.printToChat("Could not find a suitable position to teleport you");
            return 1;

        });
    }
}
