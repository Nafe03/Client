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
import java.util.List;

@ClientSync(clientid = "rusherhack")
public class RusherSync implements IClientSync {

    final String prefix = PathUtils.readFileToString(PathUtils.join(".", "rusherhack", "config", "command_prefix.txt"));

    @Override
    public String[] sync() {
        List<String> players = new ArrayList<>();
        JSONArray social = new JSONArray(PathUtils.readFileToString(PathUtils.join(".", "rusherhack", "config", "relations.json")));
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
        PlayerUtils.sendSilentMessage(Text.of(prefix + "friend clear"));
    }

    @Override
    public void addFriend(FriendManager.Friend player) {
        PlayerUtils.sendSilentMessage(Text.of(prefix + "friend add " + player.username));
    }

    @Override
    public void removeFriend(FriendManager.Friend player) {
        PlayerUtils.sendSilentMessage(Text.of(prefix + "friend remove " + player.username));
    }
}
