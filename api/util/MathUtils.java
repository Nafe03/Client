package dev.anarchy.waifuhax.api.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.*;

public class MathUtils {

    public static final Direction[] HORIZONTAL_DIR = {Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH};
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static double Angle(Vec3d a, Vec3d b) {
        return Math.toDegrees(Math.acos(a.dotProduct(b) / (a.length() * b.length())));
    }

    public static Vec2f ToPolar(Vec3d from, Vec3d to) {
        Vec3d diff = to.subtract(from);
        double diffXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float pitch = MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diff.y, diffXZ)));
        float yaw = MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90f);
        return new Vec2f(pitch, yaw);
    }

    public static Pair<Float, Float> CalcRotation(Direction dir) {
        return CalcRotation(Vec3d.ZERO, Vec3d.of(dir.getVector()));
    }

    public static Pair<Float, Float> CalcRotation(Vec3d from, Entity to) {
        return CalcRotation(from, to.getPos());
    }

    public static Pair<Float, Float> CalcRotation(Vec3d from, BlockPos to) {
        return CalcRotation(from, Vec3d.of(to));
    }

    public static Pair<Float, Float> CalcRotation(Vec3d from, Vec3d to) {
        Vec3d diff = to.subtract(from);
        double diffXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw = mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90f - mc.player.getYaw());
        float pitch = mc.player.getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diff.y, diffXZ)) - mc.player.getPitch());
        return new Pair<>(yaw, pitch);
    }

    public static BlockPos withAxis(BlockPos pos, Direction.Axis axis, int value) {
        return new BlockPos(
                axis == Direction.Axis.X ? value : pos.getX(),
                axis == Direction.Axis.Y ? value : pos.getY(),
                axis == Direction.Axis.Z ? value : pos.getZ());
    }

    public static Pair<BlockPos, BlockPos> BoxBlockBound(Box box) {
        // fuck you floating point error
        Vec3d lower = new Vec3d(Math.round(box.minX * 10) / 10f, Math.round(box.minY * 10) / 10f, Math.round(box.minZ * 10) / 10f);
        Vec3d high = new Vec3d(Math.round(box.maxX * 10) / 10f, Math.round(box.maxY * 10) / 10f, Math.round(box.maxZ * 10) / 10f);
        high = new Vec3d(Math.ceil(high.x - 1), Math.ceil(high.y - 1), Math.ceil(high.z - 1));

        return new Pair<>(BlockPos.ofFloored(lower), BlockPos.ofFloored(high));
    }

    // tangent & bitangent
    public static Pair<Direction.Axis, Direction.Axis> getTangents(Direction.Axis axis) {
        int ord = axis.ordinal();
        return new Pair<>(Direction.Axis.values()[(ord + 1) % 3], Direction.Axis.values()[(ord + 2) % 3]);
    }

    public static BlockPos toBlockPos(Vec3d vec3d) {
        return new BlockPos((int) vec3d.x, (int) vec3d.y, (int) vec3d.z);
    }

    public static Direction fromVec3i(Vec3i vec) {
        return Direction.fromVector(vec.getX(), vec.getY(), vec.getZ(), Direction.UP);
    }

}
