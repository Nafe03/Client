package dev.anarchy.waifuhax.api.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class PlayerUtils {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean saveCurrentMessage = true;

    public static void LookAt(Vec3d vec) {
        Vec2f polar = MathUtils.ToPolar(mc.player.getPos(), vec);
        PlayerMoveC2SPacket.LookAndOnGround packet = new PlayerMoveC2SPacket.LookAndOnGround(polar.y, polar.x, mc.player.isOnGround(), false);
        mc.player.networkHandler.sendPacket(packet);
//        mc.player.setPitch(polar.x);
        mc.player.setYaw(polar.y);
    }

    public static void LookAt(Direction dir) {
        Vec2f polar = null;
        switch (dir) {
            case EAST -> polar = new Vec2f(0, -90);
            case WEST -> polar = new Vec2f(0, 90);
            case UP -> polar = new Vec2f(-90, -90);
            case DOWN -> polar = new Vec2f(90, -90);
            case SOUTH -> polar = new Vec2f(0, 0);
            case NORTH -> polar = new Vec2f(0, 180);
        }
        PlayerMoveC2SPacket.LookAndOnGround packet = new PlayerMoveC2SPacket.LookAndOnGround(polar.y, polar.x, mc.player.isOnGround(), false);
        mc.player.networkHandler.sendPacket(packet);
    }

    public static Dimension getDimension() {
        if (mc.world == null) return Dimension.OVERWORLD;

        return switch (mc.world.getRegistryKey().getValue().getPath()) {
            case "the_nether" -> Dimension.NETHER;
            case "the_end" -> Dimension.END;
            default -> Dimension.OVERWORLD;
        };
    }

    public static Vec2f getLookAt(Direction dir) {
        Vec2f polar = null;
        switch (dir) {
            case EAST -> polar = new Vec2f(0, -90);
            case WEST -> polar = new Vec2f(0, 90);
            case UP -> polar = new Vec2f(-90, -90);
            case DOWN -> polar = new Vec2f(90, -90);
            case SOUTH -> polar = new Vec2f(0, 0);
            case NORTH -> polar = new Vec2f(0, 180);
        }
        return polar;
    }

    public static void Swing(boolean client, Hand hand) {
        if (client) {
            mc.player.swingHand(hand);
        }
        else {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
        }
    }

    public static void sendMessage(Text of) {
        MinecraftClient.getInstance().getNetworkHandler().sendChatMessage(of.getString());
    }

    public static void sendSilentMessage(Text of) {
        saveCurrentMessage = false;
        MinecraftClient.getInstance().getNetworkHandler().sendChatMessage(of.getString());
        saveCurrentMessage = true;
    }
}