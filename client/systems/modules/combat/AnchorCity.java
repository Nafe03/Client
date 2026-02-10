package dev.anarchy.waifuhax.client.systems.modules.combat;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.settings.annotations.CategorySetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.api.util.*;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AnchorCity extends AbstractModule {

    @CategorySetting(name = "General")
    public final AbstractSetting<Float> range = new FloatSetting("Range", "Anchor placement range", 6f, 0f, 6f);
    public final AbstractSetting<Float> targetRange = new FloatSetting("Target Range", "Target range", 8f, 0f, 12f);
    public final AbstractSetting<Boolean> selectBack = new BooleanSetting("Select back", "Select slot back", true);
    public final AbstractSetting<Boolean> swingClient = new BooleanSetting("Swing Client", "Client side swing", false);
    public final AbstractSetting<Boolean> swingServer = new BooleanSetting("Swing Server", "Server side swing", true);
    public final AbstractSetting<Boolean> toggle = new BooleanSetting("Toggle", "Toggle when finished", true);
    public final AbstractSetting<Float> mineThreshold = new FloatSetting("Mine Threshold", "Toggle when finished", 0.7f, 0f, 1f);

    @CategorySetting(name = "Target")
    public final AbstractSetting<CombatUtils.TargetModes> targetMode = new EnumSetting<>("Target mode", "Target priority", CombatUtils.TargetModes.ClosestAngle);
    public final AbstractSetting<Boolean> targetFriends = new BooleanSetting("Target friends", "Target your friends", false);
    public final AbstractSetting<Boolean> targetNoStuffs = new BooleanSetting("Target nostuffs", "Waste your equipment on players without armor", false);
    public final AbstractSetting<Boolean> onlySurrounded = new BooleanSetting("Surrounded", "Only target surrounded players", true);

    @CategorySetting(name = "Damage")
    public final AbstractSetting<Integer> minDamage = new IntegerSetting("Min Damage", "Minimum damage", 9, 0, 36);
    public final AbstractSetting<Integer> maxSelfDamage = new IntegerSetting("Max Self Damage", "Maximum self damage", 3, 0, 36);
    public final AbstractSetting<Boolean> noSuicide = new BooleanSetting("No suicide", "Do not kill yourself", true);

    @Override
    public String getDescription() {
        return "Anchor city";
    }

    public BlockPos pos;
    public PlayerEntity target;
    public float mineProgress;
    public int time;

    @Override
    public void onActivate(boolean live) {
        pos = null;
        target = null;
        mineProgress = 0;
        time = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (target == null || target.isDead() || target.isRemoved() || !target.getPos().isInRange(mc.player.getEyePos(), targetRange.getValue())) {
            target = CombatUtils.GetPlayerTarget(targetMode.getValue(), onlySurrounded.getValue() ? CombatUtils::isSurrounded : null, targetRange.getValue(), !targetFriends.getValue(), !targetNoStuffs.getValue());
            if (target == null) {
                WHLogger.printToChat("Target not found.");
                toggle();
                return;
            }
        }
        if (pos == null) {
            pos = FindPos();
            if (pos == null) {
                WHLogger.printToChat("Target block not found.");
                toggle();
                return;
            }
        }

        BlockState state = mc.world.getBlockState(pos);
        if (mineProgress == 0) {
            if (swingClient.getValue()) {
                ((LivingEntity) mc.player).swingHand(Hand.MAIN_HAND);
            }
            if (swingServer.getValue()) {
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.DOWN, 0));
        }
        Pair<Integer, Double> pair = InventoryUtils.getBestTool(state);
        updateProgress(pos, pair.getLeft(), () -> {
            int slot = InventoryUtils.searchHotbar(stack -> stack.isOf(Items.RESPAWN_ANCHOR));
            InventoryUtils.selectSlot(slot, false);

            BlockHitResult bhr = new BlockHitResult(pos.toCenterPos(), Direction.DOWN, pos, false);
            BlockUtils.Interact(Hand.MAIN_HAND, bhr);
        });
    }

    private void updateProgress(BlockPos pos, int tool, Runnable callback) {
        if (mineProgress >= mineThreshold.getValue()) {
            mineProgress = 0;

            if (swingClient.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
            if (swingServer.getValue()) {
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN, 0));

            callback.run();
            if (selectBack.getValue()) InventoryUtils.selectBack();
            if (toggle.getValue()) toggle();
        }
        else {
            ItemStack stack = mc.player.getInventory().getStack(tool);
            mineProgress += WorldUtils.calcBlockBreakingDelta(pos, mc.world.getBlockState(pos), stack);
            if (mineProgress >= mineThreshold.getValue()) {
                InventoryUtils.selectSlot(tool, selectBack.getValue());
            }
        }
        mc.world.setBlockBreakingInfo(mc.player.getId(), pos, (int) (mineProgress * 10.0F) - 1);
    }

    private BlockPos FindPos() {
        BlockPos pos;
        BlockPos targetPos = target.getBlockPos();
        double bestScore = Double.NEGATIVE_INFINITY;
        BlockPos best = null;
        for (Direction dir : MathUtils.HORIZONTAL_DIR) {
            pos = targetPos.offset(dir);
            double score = GetPosScore(pos);
            if (score > bestScore) {
                best = pos;
                bestScore = score;
            }
        }
        pos = BlockPos.ofFloored(target.getEyePos()).up();
        double score = GetPosScore(pos); // might be +1 if target is crawling
        if (score > bestScore) {best = pos;}
        return best;
    }

    private double GetPosScore(BlockPos pos) {
        if (!mc.player.getEyePos().isInRange(pos.toCenterPos(), range.getValue())) {
            return Double.NEGATIVE_INFINITY;
        }
        ;
        BlockState state = mc.world.getBlockState(pos);
        if (state.getHardness(mc.world, pos) == -1) {
            return Double.NEGATIVE_INFINITY;
        }
        ; // unbreakable

        mc.world.setBlockState(pos, Blocks.AIR.getDefaultState());
        float damage = CombatUtils.ExplosionDamage(Vec3d.ofBottomCenter(pos), target, 5f);
        float selfDamage = noSuicide.getValue() ? CombatUtils.ExplosionDamage(Vec3d.ofBottomCenter(pos), mc.player, 5f) : 0;
        mc.world.setBlockState(pos, state);
        if (!CombatUtils.validDamages(selfDamage,
                damage,
                maxSelfDamage.getValue(),
                minDamage.getValue(),
                noSuicide.getValue())) {return Double.NEGATIVE_INFINITY;}

        Pair<Integer, Double> pair = InventoryUtils.getBestTool(state);
        ItemStack stack = mc.player.getInventory().getStack(pair.getLeft());
        double distance = mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos());
        double breakingSpeed = WorldUtils.calcBlockBreakingDelta(pos, mc.world.getBlockState(pos), stack);
        breakingSpeed -= pair.getRight();
        return -distance + breakingSpeed * 100 + damage * 100 + selfDamage * 1000;
    }
}
