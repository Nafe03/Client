package dev.anarchy.waifuhax.client.managers;

import com.google.gson.Gson;
import dev.anarchy.waifuhax.api.BaseManager;
import dev.anarchy.waifuhax.api.ClientSync;
import dev.anarchy.waifuhax.api.IClientSync;
import dev.anarchy.waifuhax.api.util.ModUtils;
import dev.anarchy.waifuhax.api.util.PathUtils;
import dev.anarchy.waifuhax.client.WaifuHax;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FriendManager extends BaseManager {

    private static final String friendsPath = PathUtils.join(".", "WaifuHax", "friends.json");
    private static final Set<IClientSync> loadedClients = new HashSet<>();
    public static HashSet<Friend> friends = new HashSet<>();

    public static void addFriend(Friend friend) {
        loadedClients.forEach(loader -> loader.addFriend(friend));
        friends.add(friend);
    }

    public static void addFriend(@NotNull String username, @NotNull UUID uuid) {
        addFriend(new Friend(username, uuid));
    }

    public static boolean isFriend(@NotNull String username) {
        for (Friend friend : friends) {
            if (friend.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static void removeFriend(Friend friend) {
        loadedClients.forEach(loader -> loader.removeFriend(friend));
        friends.remove(friend);
    }

    public static void removeFriend(String username) {
        for (Friend friend : friends) {
            if (friend.username.equals(username)) {
                removeFriend(friend);
                return;
            }
        }
    }

    public static void loadFriendList() {
        File file = new File(friendsPath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String json = PathUtils.readFileToString(friendsPath);
        Gson gson = new Gson();
        Friend[] friends = gson.fromJson(json, Friend[].class);
        if (friends == null) return;
        for (Friend friend : friends) {
            addFriend(friend);
        }
        loadedClients.forEach(loader -> {
            for (String player : loader.sync())
                addFriend(new Friend(player, null));
        });
    }

    public static void saveFriendList() {
        Gson gson = new Gson();
        String json = gson.toJson(friends);
        PathUtils.writeStringToFile(friendsPath, json);
    }

    @Override

    public void init() {
        // least unsafe reflection usage :
        new Reflections(WaifuHax.class.getPackage().getName() + ".systems.sync")
                .getSubTypesOf(Object.class)
                .forEach(clazz -> {
                    if (Arrays.asList(clazz.getInterfaces()).contains(IClientSync.class)) {
                        try {
                            if (ModUtils.isModPresent(clazz.getAnnotation(ClientSync.class).clientid())) {
                                loadedClients.add((IClientSync) clazz.getDeclaredConstructor().newInstance());
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    public static class Friend {

        public String username;
        public @Nullable UUID uuid;

        public Friend(String username, @Nullable UUID uuid) {
            this.username = username;
            this.uuid = uuid;
        }

        @Override
        public int hashCode() {
            return this.username.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Friend)) return false;
            return this.username.equals(((Friend) obj).username);
        }

        @Override
        public String toString() {
            return this.username;
        }
    }
}