package dev.anarchy.waifuhax.api.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class WorldUtils {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean EntityIntersect(BlockPos pos) {
        return EntityIntersect(new Box(pos), null);
    }

    public static boolean EntityIntersect(Box box) {
        return EntityIntersect(box, null);
    }

    public static boolean EntityIntersect(BlockPos pos, Predicate<Entity> test) {
        return EntityIntersect(new Box(pos), test);
    }

    public static boolean EntityIntersect(Box box, @Nullable Predicate<Entity> test) {
        for (net.minecraft.entity.Entity entity : mc.world.getEntities()) {
            if ((test == null || test.test(entity)) && box.intersects(entity.getBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    public static float calcBlockBreakingDelta(BlockPos pos, BlockState state, ItemStack stack) {
        float hardness = state.getHardness(mc.world, pos);
        if (hardness == -1f) return 0f;
        boolean canHarvest = !state.isToolRequired() || stack.isSuitableFor(state);
        float i = canHarvest ? 30f : 100f;
        return getBlockBreakingSpeed(state, stack) / hardness / i;
    }

    public static float getToolSpeed(BlockState state, ItemStack stack) {
        float f = stack.getMiningSpeedMultiplier(state);
        if (f > 1f) {
            float v = 0;
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : stack.getEnchantments().getEnchantmentEntries()) {
                if (entry.getKey().matchesKey(Enchantments.EFFICIENCY)) {
                    v = (float) entry.getIntValue();
                    break;
                }
            }
            if (v >= 0) f += v * v + 1;
        }
        return f;
    }

    public static float getBlockBreakingSpeed(BlockState state, ItemStack stack) {
        float f = getToolSpeed(state, stack);
        if (StatusEffectUtil.hasHaste(mc.player)) {
            f *= 1f + (float) (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2F;
        }

        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> f *= 0.3f;
                case 1 -> f *= 0.09f;
                case 2 -> f *= 0.0027f;
                default -> f *= 8.1E-4f;
            }
        }

        f *= (float) mc.player.getAttributeValue(EntityAttributes.BLOCK_BREAK_SPEED);
        if (mc.player.isSubmergedIn(FluidTags.WATER)) {
            f *= (float) mc.player.getAttributeInstance(EntityAttributes.SUBMERGED_MINING_SPEED).getValue();
        }
        if (!mc.player.isOnGround()) {f /= 5.0F;}
        return f;
    }

}
