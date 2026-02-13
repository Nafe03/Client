package dev.anarchy.waifuhax.api.util;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class CapeUtils {

    public static final Identifier test_cape = Identifier.of("waifuhax:textures/capes/test.png");
    public static final Identifier dev_cape = Identifier.of("waifuhax:textures/capes/dev.png");
    public static final Identifier friend_cape = Identifier.of("waifuhax:textures/capes/friend.png");
    private static final HashMap<String, CapeType> capeList = new HashMap<>();

    static {
        capeList.put("ef312ec2-bf85-4c42-9526-f5ee0ff4adb4", CapeType.DEV);
    }

    public static Identifier get(AbstractClientPlayerEntity player) {
        if (capeList.containsKey(player.getGameProfile().getId().toString())) {
            return matchTypeToCape(capeList.get(player.getGameProfile().getId().toString()));
        }
        return null;
    }

    private static Identifier matchTypeToCape(CapeType type) {
        switch (type) {
            case DEV -> {
                return dev_cape;
            }
            case FRIEND -> {
                return friend_cape;
            }
            case PRIDE, CAPE_TEST -> {
                return test_cape;
            }
            default -> {
                return null;
            }
        }
    }

    private enum CapeType {
        DEV,
        CAPE_TEST,
        FRIEND,
        PRIDE
    }
}
