package dev.anarchy.waifuhax.api;

import dev.anarchy.waifuhax.client.managers.FriendManager;

public interface IClientSync {

    // when called, return every players in the friendlist
    String[] sync();

    // clear the friendlist of the loaded client
    void clear();

    // add the player to the shared friendlist
    void addFriend(FriendManager.Friend player);

    void removeFriend(FriendManager.Friend player);
}
