package dev.anarchy.waifuhax.client.systems.sync;

import dev.anarchy.waifuhax.api.ClientSync;
import dev.anarchy.waifuhax.api.IClientSync;
import dev.anarchy.waifuhax.api.util.PathUtils;
import dev.anarchy.waifuhax.api.util.PlayerUtils;
import dev.anarchy.waifuhax.client.managers.FriendManager;
import net.minecraft.text.Text;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ClientSync(clientid = "mioloader")
public class MioSync implements IClientSync {

    private String prefix = new JSONObject(PathUtils.readFileToString(PathUtils.join(".", "mio fabric", "commands"))).getString("prefix");

    @Override
    public String[] sync() {
        List<String> players = new ArrayList<>();
        JSONObject friends = new JSONObject(PathUtils.readFileToString(PathUtils.join(".", "mio fabric", "social.json")));
        JSONArray social = friends.getJSONArray("socials");
        for (int i = 0; i < social.length(); i++) {
            JSONObject relation = social.getJSONObject(i);
            if (relation.getString("role").equals("friend")) {
                players.add(relation.getString("name"));
            }
        }
        return players.toArray(new String[0]);
    }

    @Override
    public void clear() {
        Arrays.stream(sync())
                .toList()
                .forEach(str -> removeFriend(new FriendManager.Friend(str, null)));
    }

    @Override
    public void addFriend(FriendManager.Friend player) {
        PlayerUtils.sendSilentMessage(Text.of(prefix + "friend add " + player.username));
    }

    @Override
    public void removeFriend(FriendManager.Friend player) {
        PlayerUtils.sendSilentMessage(Text.of(prefix + "friend remove" + player.username));
    }
}
