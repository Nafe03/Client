package dev.anarchy.waifuhax.client.events;


public class GameLeftEvent {

    private static final GameLeftEvent INSTANCE = new GameLeftEvent();

    public static GameLeftEvent get() {
        return INSTANCE;
    }
}