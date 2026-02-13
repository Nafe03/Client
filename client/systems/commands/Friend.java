package dev.anarchy.waifuhax.client.systems.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.systems.commands.AbstractCommand;
import dev.anarchy.waifuhax.client.managers.FriendManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;

public class Friend extends AbstractCommand {

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add")
                .then(argument("player", StringArgumentType.word())
                        .executes(ctx -> {

                            if (MinecraftClient.getInstance().getNetworkHandler() == null || MinecraftClient.getInstance().getNetworkHandler().getPlayerList() == null) {
                                WHLogger.printToChat("Man, something went REALLY wrong");
                                return 0;
                            }

                            String username = ctx.getArgument("player", String.class);
                            if (FriendManager.isFriend(username)) {
                                WHLogger.printToChat("%s is already your friend", username);
                                return 0;
                            }

                            PlayerListEntry player = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(username);

                            if (player == null) {
                                WHLogger.printToChat("This player is currently not online");
                                return 0;
                            }

                            GameProfile profile = player.getProfile();
                            if (profile == null) {
                                WHLogger.printToChat("Something else may have gone extremely wrong.");
                                return 0;
                            }
                            FriendManager.addFriend(username, profile.getId());
                            WHLogger.printToChat("Added %s to your friend list", username);
                            FriendManager.saveFriendList();
                            return 1;
                        })
                )
        );

        builder.then(literal("remove")
                .then(argument("player", StringArgumentType.word())
                        .executes(ctx -> {

                            String username1 = ctx.getArgument("player", String.class);
                            if (!FriendManager.isFriend(username1)) {
                                WHLogger.printToChat("%s is not your friend", username1);
                                return 0;
                            }
                            FriendManager.removeFriend(username1);
                            WHLogger.printToChat("Removed %s from your friend list", username1);
                            FriendManager.saveFriendList();
                            return 1;
                        })
                )
        );

        builder.then(literal("list")
                .executes(ctx -> {
                    if (FriendManager.friends.isEmpty()) {
                        WHLogger.printToChat("You have no friends");
                        return 1;
                    }

                    WHLogger.printToChat(FriendManager.friends.stream()
                            .map(friend -> friend.username + " ")
                            .sorted(CharSequence::compare)
                            .reduce("", String::concat));

                    return 1;
                })
        );

        builder.then(literal("clear").executes(ctx -> {
            FriendManager.friends.clear();
            WHLogger.printToChat("Cleared your friend list");
            FriendManager.saveFriendList();
            return 1;
        }));

        builder.then(literal("sync")
                        .then(literal("from-loaded-clients"))
                        .executes(ctx -> {
                            return 1;
                        }))
                .then(literal("sync"))
                .then(literal("to-loaded-clients"))
                .executes(ctx -> {
                    return 1;
                });
    }
}
