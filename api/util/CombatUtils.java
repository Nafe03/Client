package dev.anarchy.waifuhax.api.util;

import dev.anarchy.waifuhax.client.managers.FriendManager;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CombatUtils {

    public final static List<Block> VALID_CRYSTAL_BLOCK = List.of(Blocks.OBSIDIAN, Blocks.BEDROCK);
    public final static List<Item> SUPPORT_BLOCKS = List.of(Items.OBSIDIAN, Items.COBBLESTONE, Items.STONE, Items.COBBLED_DEEPSLATE, Items.DEEPSLATE, Items.DIRT);
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    // TODO: put this is a global setting thing
    public static List<Block> surroundValidBlocks = List.of(Blocks.BEDROCK, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.ENDER_CHEST);

    public static Function<Entity, Double> getScoreFunction(TargetModes mode) {
        Function<Entity, Double> ret = null;
        switch (mode) {
            case LowestHealth -> ret = CombatUtils::LowestHealth;
            case HighestHealth -> ret = CombatUtils::HighestHealth;
            case Closest -> ret = CombatUtils::Closest;
            case Furthest -> ret = CombatUtils::Furthest;
            case ClosestAngle -> ret = CombatUtils::ClosestCrosshair;
        }
        return ret;
    }

    public static double LowestHealth(Entity entity) {
        return ((LivingEntity) entity).getHealth();
    }

    public static double HighestHealth(Entity entity) {
        return 1f / LowestHealth(entity);
    }

    public static double Closest(Entity entity) {
        return mc.player.squaredDistanceTo(entity);
    }

    public static double Furthest(Entity entity) {
        return 1f / Closest(entity);
    }

    public static double ClosestCrosshair(Entity entity) {
        return MathUtils.Angle(Vec3d.fromPolar(mc.player.getPitch(), mc.player.getYaw()), entity.getPos().subtract(mc.player.getEyePos()));
    }

    @Nullable
    public static Entity GetTarget(Iterable<? extends Entity> entities, Function<Entity, Boolean> validFunction, Function<Entity, Double> scoreFunction, List<Entity> avoid) {
        Entity best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (Entity entity : entities) {
            if (mc.player == entity) continue;
            if (!validFunction.apply(entity)) continue;
            double score = scoreFunction.apply(entity);
            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }
        return best;
    }

    @Nullable
    public static LivingEntity GetLivingTarget(Function<Entity, Double> scoreFunction, double range) {
        return GetLivingTarget(scoreFunction, List.of(), range);
    }

    @Nullable
    public static LivingEntity GetLivingTarget(Function<Entity, Double> scoreFunction, List<Entity> avoid, double range) {
        return (LivingEntity) GetTarget(mc.world.getEntities(),
                entity -> entity.isLiving() &&
                        entity.isAlive() &&
                        !((LivingEntity) entity).isDead() &&
                        mc.player.squaredDistanceTo(entity) <= range * range &&
                        !avoid.contains(entity),
                scoreFunction, avoid);
    }

    @Nullable
    public static PlayerEntity GetPlayerTarget(TargetModes mode, Function<PlayerEntity, Boolean> isValid, double range, boolean avoidFriends, boolean avoidNoStuffs) {
        return GetPlayerTarget(getScoreFunction(mode), isValid, List.of(), range, avoidFriends, avoidNoStuffs);
    }

    @Nullable
    public static PlayerEntity GetPlayerTarget(Function<Entity, Double> scoreFunction, Function<PlayerEntity, Boolean> isValid, double range, boolean avoidFriends, boolean avoidNoStuffs) {
        return GetPlayerTarget(scoreFunction, isValid, List.of(), range, avoidFriends, avoidNoStuffs);
    }

    @Nullable
    public static PlayerEntity GetPlayerTarget(Function<Entity, Double> scoreFunction, Function<PlayerEntity, Boolean> isValid, List<Entity> avoid, double range, boolean avoidFriends, boolean avoidNoStuffs) {
        PlayerEntity found = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (PlayerEntity target : mc.world.getPlayers()) {
            if (target == mc.player || target.isRemoved() || target.isDead()) {
                continue;
            }
            double squaredDistance = mc.player.squaredDistanceTo(target);
            if (squaredDistance > range * range) continue;

            if (avoidFriends && FriendManager.isFriend(target.getName().getString())) {
                continue;
            }
            if (avoidNoStuffs && isNoStuff(target)) continue;

            if (isValid != null && !isValid.apply(target)) continue;

            double score = scoreFunction.apply(target) + (squaredDistance > 36 ? 15 : 0) + (avoid.contains(target) ? 10000 : 0); // attack close targets first
            if (score < bestScore) {
                found = target;
                bestScore = score;
            }
        }
        return found;
    }

    public static boolean isSurrounded(Entity entity) {
        Box box = entity.getBoundingBox();
        List<BlockPos> surroundBlocks = getBlocksTrap(box, (int) box.minY, (int) box.minY);
        for (BlockPos pos : surroundBlocks) {
            if (!surroundValidBlocks.contains(mc.world.getBlockState(pos).getBlock())) {
                return false;
            }
        }
        return true;
    }

    public static List<BlockPos> getBlocksTrap(Box box) {
        return getBlocksTrap(box, (int) box.minY - 1, (int) box.maxY + 1);
    }

    public static List<BlockPos> getBlocksTrap(Box box, int minY, int maxY) { // shitty way of doing this but it works
        List<BlockPos> trap = new ArrayList<>();
        List<BlockPos> open = new ArrayList<>(List.of(BlockPos.ofFloored(box.getCenter())));
        List<BlockPos> closed = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            if (open.isEmpty()) break;
            BlockPos current = open.get(0);
            open.remove(0);
            closed.add(current);
            for (Direction dir : Direction.values()) {
                BlockPos neighbour = current.offset(dir);
                if (neighbour.getY() > maxY ||
                        neighbour.getY() < minY) {continue;}
                if (closed.contains(neighbour)) continue;
                Box blockBox = new Box(neighbour);
                if (box.intersects(blockBox)) {
                    if (!open.contains(neighbour)) open.add(neighbour);
                }
                else {trap.add(neighbour);}
            }
        }
        return trap;
    }

    public static boolean isNoStuff(PlayerEntity player) {
        return true;
    }

    @Nullable
    public static Entity CrystalAt(BlockPos pos) {
        return CrystalAt(pos, 0.86);
    }

    @Nullable
    public static Entity CrystalAt(Vec3d pos) {
        return CrystalAt(pos, 0.86);
    }

    @Nullable
    public static Entity CrystalAt(BlockPos pos, double minRadius) {
        return CrystalAt(Vec3d.ofBottomCenter(pos), minRadius);
    }

    @Nullable
    public static Entity CrystalAt(Vec3d pos, double minRadius) {
        Entity entity = null;
        double dist = Math.pow(minRadius, 2);
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof EndCrystalEntity) {
                double d = pos.squaredDistanceTo(e.getPos());
                if (d < dist) {
                    entity = e;
                    dist = d;
                }
            }
        }
        return entity;
    }

    public static boolean validDamages(float selfDamage, float damage, float maxSelf, float min, boolean noSuicide) {
        if (noSuicide) {
            return damage >= min && selfDamage <= maxSelf && mc.player.getHealth() > selfDamage;
        }
        else {return damage >= min;}
    }

    public static float ExplosionDamage(Vec3d pos, Entity entity, float power) {
        if (mc.world.getDifficulty() == Difficulty.PEACEFUL) return 0;

        double w = pos.distanceTo(entity.getPos()) / (power * 2f);
        if (w > 1f) return 0;

        float exposure = 26.0f;//Explosion.(pos, entity);
        float damage = (float) ((1f - w) * exposure);
        damage = (float) ((int) ((damage * damage + damage) / 2f * 7f * power * 2f + 1f));

        switch (mc.world.getDifficulty()) {
            case EASY -> damage = Math.min(damage / 2f + 1f, damage);
            case HARD -> damage = damage * 3f / 2f;
        }

        StatusEffectInstance effect = mc.player.getStatusEffect(StatusEffects.RESISTANCE);
        if (effect != null) {
            int resistance = (int) ((effect.getAmplifier() + 1f) * 5f);
            damage = Math.max(0f, (25f - resistance) * damage / 25f);
        }

        return damage;
    }

    public enum TargetModes {
        LowestHealth,
        HighestHealth,
        Closest,
        Furthest,
        ClosestAngle,
    }
}
