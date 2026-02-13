package dev.anarchy.waifuhax.client.systems.sync;

import dev.anarchy.waifuhax.api.ClientSync;
import dev.anarchy.waifuhax.api.IClientSync;
import dev.anarchy.waifuhax.client.managers.FriendManager;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

// Notes about this :
// this is incredibly fucked, but meteor being open source and
// not obfuscated allows this.
// their save system is really annoying to parse anyway.
@ClientSync(clientid = "meteor-client")
public class MeteorSync implements IClientSync {

    @Override
    @SneakyThrows
    public String[] sync() {
        List<String> players = new ArrayList<>();
        final Class friendManager = Class.forName("meteordevelopment.meteorclient.systems.friends.Friends");
        final Class friend = Class.forName("meteordevelopment.meteorclient.systems.friends.Friend");

        Method get = friendManager.getDeclaredMethod("get");
        Method iterator = friendManager.getDeclaredMethod("iterator");
        Iterator<Object> meteorFriendList = (Iterator<Object>) iterator.invoke(get.invoke(null));
        meteorFriendList.forEachRemaining((object) -> {
            try {
                players.add((String) object.getClass().getDeclaredField("name").get(object));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return players.toArray(new String[0]);
    }

    @Override
    @SneakyThrows
    public void clear() {
        final Class friendManager = Class.forName("meteordevelopment.meteorclient.systems.friends.Friends");

        Method get = friendManager.getDeclaredMethod("get");
        ((List<Object>) get.invoke(null)
                .getClass()
                .getDeclaredField("friends")
                .get(get.invoke(null)))
                .clear();
    }

    @Override
    @SneakyThrows
    public void addFriend(FriendManager.Friend player) {
        final Class friendManager = Class.forName("meteordevelopment.meteorclient.systems.friends.Friends");
        final Class friend = Class.forName("meteordevelopment.meteorclient.systems.friends.Friend");

        Method get = friendManager.getDeclaredMethod("get");
        Method add = friendManager.getDeclaredMethod("add", friend);
        add.invoke(get.invoke(null), friend.getDeclaredConstructor(String.class, UUID.class)
                .newInstance(player.username, player.uuid));
    }

    @Override
    @SneakyThrows
    public void removeFriend(FriendManager.Friend player) {
        final Class friendManager = Class.forName("meteordevelopment.meteorclient.systems.friends.Friends");
        final Class friend = Class.forName("meteordevelopment.meteorclient.systems.friends.Friend");
        Method get = friendManager.getDeclaredMethod("get");
        Method remove = friendManager.getDeclaredMethod("remove", friend);
        remove.invoke(get.invoke(null), friend.getDeclaredConstructor(String.class, UUID.class)
                .newInstance(player.username, player.uuid));
    }
}
