package dev.anarchy.waifuhax.client.systems.modules.combat;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.annotations.CategorySetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.api.util.*;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.concurrent.atomic.AtomicReference;

public class AutoCity extends AbstractModule {

    @CategorySetting(name = "General")
    public final AbstractSetting<Float> range = new FloatSetting("Range", "Anchor placement range", 6f, 0f, 6f);
    public final AbstractSetting<Float> targetRange = new FloatSetting("Target Range", "Target range", 8f, 0f, 12f);
    public final AbstractSetting<Boolean> selectBack = new BooleanSetting("Select back", "Select slot back", true);
    public final AbstractSetting<Boolean> swingClient = new BooleanSetting("Swing", "Client side swing", false);
    public final AbstractSetting<Boolean> swingServer = new BooleanSetting("Swing", "Server side swing", true);
    public final AbstractSetting<Boolean> toggle = new BooleanSetting("Toggle", "Toggle when finished", true);
    public final AbstractSetting<Float> mineThreshold = new FloatSetting("Mine Threshold", "Toggle when finished", 0.7f, 0f, 1f);
    public final AbstractSetting<Boolean> anchorPriority = new BooleanSetting("Anchor Priority", "Prefer anchors over crystals", true);

    @CategorySetting(name = "Target")
    public final AbstractSetting<CombatUtils.TargetModes> targetMode = new EnumSetting<>("Target mode", "Target priority", CombatUtils.TargetModes.ClosestAngle);
    public final AbstractSetting<Boolean> targetFriends = new BooleanSetting("Target friends", "Target your friends", false);
    public final AbstractSetting<Boolean> targetNoStuffs = new BooleanSetting("Target nostuffs", "Waste your equipment on players without armor", false);
    public final AbstractSetting<Boolean> onlySurrounded = new BooleanSetting("Surrounded", "Only target surrounded players", true);
    public BlockPos pos;
    public PlayerEntity target;
    public float mineProgress;
    public int time;

    @Override
    public String getDescription() {
        return "Auto city";
    }

    @Override
    public void onActivate(boolean live) {
        pos = null;
        target = null;
        mineProgress = 0;
        time = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (target == null || target.isDead() || target.isRemoved() || !target.getPos().isInRange(mc.player.getPos(), range.getValue())) {
            target = CombatUtils.GetPlayerTarget(targetMode.getValue(), onlySurrounded.getValue() ? CombatUtils::isSurrounded : null, range.getValue(), !targetFriends.getValue(), !targetNoStuffs.getValue());
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


            BlockHitResult bhr;
            boolean isAnchor = anchorPriority.getValue();
            AtomicReference<Item> item = new AtomicReference<>(isAnchor ? Items.RESPAWN_ANCHOR : Items.END_CRYSTAL);
            int slot = InventoryUtils.searchHotbar(stack -> stack.isOf(item.get()));
            if (slot == -1) {
                item.set(isAnchor ? Items.END_CRYSTAL : Items.RESPAWN_ANCHOR);
                slot = InventoryUtils.searchHotbar(stack -> stack.isOf(item.get()));
                if (slot == -1) {
                    WHLogger.printToChat("Weapon not found.");
                    toggle();
                    return;
                }
                isAnchor = !isAnchor;
            }
            if (item.get().equals(Items.END_CRYSTAL)) {
                WHLogger.printToChat("Crystal auto city not supported yet.");
                toggle();
                return;
            }
            InventoryUtils.selectSlot(slot, false);

            if (isAnchor) {
                bhr = new BlockHitResult(pos.toCenterPos(), Direction.DOWN, pos, false);
            }
            else {
                bhr = new BlockHitResult(pos.down().toCenterPos(), Direction.DOWN, pos.down(), true);
            }
            BlockUtils.Interact(Hand.MAIN_HAND, bhr);

            if (selectBack.getValue()) InventoryUtils.selectBack();
            if (toggle.getValue()) toggle();
        });
    }

    private void updateProgress(BlockPos pos, int tool, Runnable callback) {
        if (mineProgress >= mineThreshold.getValue()) {
            mineProgress = 0;

            InventoryUtils.selectSlot(tool, selectBack.getValue());
            if (swingClient.getValue()) {
                ((LivingEntity) mc.player).swingHand(Hand.MAIN_HAND);
            }
            if (swingServer.getValue()) {
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN, 0));
            callback.run();
        }
        else {
            ItemStack stack = mc.player.getInventory().getStack(tool);
            mineProgress += WorldUtils.calcBlockBreakingDelta(pos, mc.world.getBlockState(pos), stack);
        }
        mc.world.setBlockBreakingInfo(mc.player.getId(), pos, (int) (mineProgress * 10.0F) - 1);
    }

    private BlockPos FindPos() {
        BlockPos targetPos = target.getBlockPos();
        double bestScore = Double.POSITIVE_INFINITY;
        BlockPos best = null;
        for (Direction dir : MathUtils.HORIZONTAL_DIR) {
            BlockPos pos = targetPos.offset(dir);
            if (!mc.player.getEyePos().isInRange(pos.toCenterPos(), range.getValue())) {
                continue;
            }
            if (!anchorPriority.getValue()) {
                if (!mc.player.getEyePos().isInRange(pos.down().toCenterPos(), range.getValue())) {
                    continue;
                }
                Box box = new Box(pos.down()).stretch(0, 1, 0);
                if (WorldUtils.EntityIntersect(box)) continue;
            }
            BlockState state = mc.world.getBlockState(pos);
            if (state.getHardness(mc.world, pos) == -1) continue; // unbreakable

            Pair<Integer, Double> pair = InventoryUtils.getBestTool(state);
            ItemStack stack = mc.player.getInventory().getStack(pair.getLeft());

            double score = mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos());
            score -= WorldUtils.calcBlockBreakingDelta(pos, mc.world.getBlockState(pos), stack);
            score -= pair.getRight();
            if (score < bestScore) {
                best = pos;
                bestScore = score;
            }
        }
        return best;
    }


}
