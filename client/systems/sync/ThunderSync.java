package dev.anarchy.waifuhax.client.systems.sync;

import dev.anarchy.waifuhax.api.ClientSync;
import dev.anarchy.waifuhax.api.IClientSync;
import dev.anarchy.waifuhax.client.managers.FriendManager;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

// Same as Meteor, Thunderhack is a free and opensource client
// so we can make change to the friendlist at runtime without
// using the chat like Rusherhack or Mio. This create janky, unsafe looking code
// but it should work in theory
@ClientSync(clientid = "thunderhack")
public class ThunderSync implements IClientSync {

    @Override
    @SneakyThrows
    public String[] sync() {
        final Class friendManager = Class.forName("thunder.hack.core.manager.player.FriendManager");
        Object friendManagerInstance = Class.forName("thunder.hack.core.Managers").getDeclaredField("FRIEND").get(null);
        Method get = friendManager.getDeclaredMethod("getFriends");
        return (String[]) get.invoke(friendManagerInstance);
    }

    @Override
    @SneakyThrows
    public void clear() {
        final Class friendManager = Class.forName("thunder.hack.core.manager.player.FriendManager");
        Object friendManagerInstance = Class.forName("thunder.hack.core.Managers").getDeclaredField("FRIEND").get(null);
        Method clear = friendManager.getDeclaredMethod("clear");
        clear.invoke(friendManagerInstance);
    }

    @Override
    @SneakyThrows
    public void addFriend(FriendManager.Friend player) {
        final Class friendManager = Class.forName("thunder.hack.core.manager.player.FriendManager");
        Object friendManagerInstance = Class.forName("thunder.hack.core.Managers").getDeclaredField("FRIEND").get(null);
        Method add = friendManager.getDeclaredMethod("addFriend", String.class);
        add.invoke(friendManagerInstance, player.username);
    }

    @Override
    @SneakyThrows
    public void removeFriend(FriendManager.Friend player) {
        final Class friendManager = Class.forName("thunder.hack.core.manager.player.FriendManager");
        Object friendManagerInstance = Class.forName("thunder.hack.core.Managers").getDeclaredField("FRIEND").get(null);
        Method remove = friendManager.getDeclaredMethod("removeFriend", String.class);
        remove.invoke(friendManagerInstance, player.username);
    }
}
