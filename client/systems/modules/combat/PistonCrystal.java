package dev.anarchy.waifuhax.client.systems.modules.combat;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.mixins.PlayerMoveC2SPacketAccessor;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.settings.annotations.CategorySetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.api.util.*;
import dev.anarchy.waifuhax.client.events.PacketEvent;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.block.NeighborUpdater;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PistonCrystal extends AbstractModule {

    public static final ArrayList<Vec3i> TORCH_UPDATE_ORDER = new ArrayList<>();
    private static final Direction[] XZ = new Direction[]{Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH};
    private static final Direction[] XZ_DOWN = new Direction[]{Direction.DOWN, Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH};
    private static final Direction[] AxisX = new Direction[]{Direction.WEST, Direction.EAST};
    private static final Direction[] AxisZ = new Direction[]{Direction.SOUTH, Direction.NORTH};
    private static final List<Block> REDSTONE_TORCH = List.of(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH);
    private static final List<Block> PISTON_BLOCK = List.of(Blocks.PISTON, Blocks.MOVING_PISTON);
    private static final List<Block> RESISTANT_FULL_BLOCKS = List.of(Blocks.OBSIDIAN, Blocks.BEDROCK, Blocks.NETHERITE_BLOCK);

    static {
        // generate torch update order table
        for (Direction dir : Direction.values()) {
            Direction inv = dir.getOpposite();
            for (Direction updateDir : NeighborUpdater.UPDATE_ORDER) {
                if (updateDir == inv) continue;
                Vec3i vec = dir.getVector().offset(updateDir);
                if (!TORCH_UPDATE_ORDER.contains(vec)) {
                    TORCH_UPDATE_ORDER.add(vec);
                }
            }
        }
        for (Direction updateDir : NeighborUpdater.UPDATE_ORDER) {
            TORCH_UPDATE_ORDER.add(updateDir.getVector());
        }
    }

    @CategorySetting(name = "General")
    public final AbstractSetting<Float> range = new FloatSetting("Range", "Range", 6f, 0, 6);
    public final AbstractSetting<Boolean> swing = new BooleanSetting("Swing", "Client side swing", true);
    public final AbstractSetting<Boolean> debug = new BooleanSetting("Debug", "Debuggign", false);

    @CategorySetting(name = "Target")
    public final AbstractSetting<CombatUtils.TargetModes> targetMode = new EnumSetting<>("Target mode", "Target priority", CombatUtils.TargetModes.ClosestAngle);
    public final AbstractSetting<Boolean> targetUntilDead = new BooleanSetting("Target until dead", "Target the player until he dies", true);
    public final AbstractSetting<Boolean> targetFriends = new BooleanSetting("Target friends", "Target your friends if they exists", false);
    public final AbstractSetting<Boolean> targetNoStuffs = new BooleanSetting("Target nostuffs", "Waste your equipment on players without armor", false);
    public final AbstractSetting<Boolean> onlySurrounded = new BooleanSetting("Surrounded", "Only target surrounded players", true);

    @CategorySetting(name = "Support")
    public final EnumSetting<BlockUtils.SupportModes> placeMode = new EnumSetting<>("Place mode", "Placing mode (airplace / support)", BlockUtils.SupportModes.AirPlace);
    public final AbstractSetting<Integer> supportDepth = new IntegerSetting("Support depth", "Max support blocks", 3, 0, 5)
            .showIf(() -> this.placeMode.getValue() == BlockUtils.SupportModes.Support);
    public final AbstractSetting<Integer> supportDelay = new IntegerSetting("Support delay", "Delay between placing support blocks (ticks)", 0, 0, 10)
            .showIf(() -> this.placeMode.getValue() == BlockUtils.SupportModes.Support);
    ;

    @CategorySetting(name = "Placement")
    public final AbstractSetting<Boolean> fastPlace = new BooleanSetting("Fast place", "Place crystal & redstone in the same tick", false);
    public final AbstractSetting<Boolean> fastBreak = new BooleanSetting("Fast break", "Break crystal & redstone in the same tick", true);
    ;

    @CategorySetting(name = "Delay")
    public final AbstractSetting<Boolean> allowNormal = new BooleanSetting("Normal mode", "Allow normal mode", true);
    public final AbstractSetting<Integer> placeDelayNormal = new IntegerSetting("Place delay normal", "Delay between placing blocks (normal mode)", 0, 0, 10)
            .showIf(this.allowNormal::getValue);
    public final AbstractSetting<Integer> placePistonDelayNormal = new IntegerSetting("Place piston delay normal", "Delay between placing pistons (normal mode)", 1, 0, 10)
            .showIf(this.allowNormal::getValue);
    public final AbstractSetting<Integer> pushDelayNormal = new IntegerSetting("Push delay normal", "Delay when pushing crystal (normal mode)", 0, 0, 10)
            .showIf(this.allowNormal::getValue);
    public final AbstractSetting<Integer> attackDelayNormal = new IntegerSetting("Attack delay normal", "Delay between attacks (normal mode)", 0, 0, 10)
            .showIf(this.allowNormal::getValue);
    public final AbstractSetting<Boolean> allowHead = new BooleanSetting("Head mode", "Allow head mode (pushing crystal above head)", true);
    public final AbstractSetting<Integer> placeDelayHead = new IntegerSetting("Place delay head", "Delay between placing blocks (head mode)", 0, 0, 10)
            .showIf(this.allowHead::getValue);
    public final AbstractSetting<Integer> placePistonDelayHead = new IntegerSetting("Place piston delay head", "Delay between placing pistons (head mode)", 1, 0, 10)
            .showIf(this.allowHead::getValue);
    public final AbstractSetting<Integer> pushDelayHead = new IntegerSetting("Push delay head", "Delay when pushing crystal (head mode)", 0, 0, 10)
            .showIf(this.allowHead::getValue);
    public final AbstractSetting<Integer> attackDelayHead = new IntegerSetting("Attack delay head", "Delay between attacks (head mode)", 0, 0, 10)
            .showIf(this.allowHead::getValue);
    public final AbstractSetting<Boolean> allowDouble = new BooleanSetting("Double mode", "Allow double mode (using 2 pistons to push the crystal)", false);
    public final AbstractSetting<Integer> placeDelayDouble = new IntegerSetting("Place delay double", "Delay between placing blocks (double mode)", 0, 0, 10)
            .showIf(this.allowDouble::getValue);
    public final AbstractSetting<Integer> placePistonDelayDouble = new IntegerSetting("Place piston delay double", "Delay between placing pistons (double mode)", 1, 0, 10)
            .showIf(this.allowDouble::getValue);
    public final AbstractSetting<Integer> pushDelayDouble = new IntegerSetting("Push delay double", "Delay when pushing crystal (double mode)", 0, 0, 10)
            .showIf(this.allowDouble::getValue);
    public final AbstractSetting<Integer> attackDelayDouble = new IntegerSetting("Attack delay double", "Delay between attacks (double mode)", 0, 0, 10)
            .showIf(this.allowDouble::getValue);

    @CategorySetting(name = "Computation")
    public final AbstractSetting<Integer> skipScoreThreshold = new IntegerSetting("Skip threshold", "Skip calculations when score reached (0 to disable skipping, recommended)", 0, -10, 10);
    public final AbstractSetting<Integer> torchOnCrystalCost = new IntegerSetting("Torch on crystal penalty", "Score penalty when torch placed on crystal", 2, 0, 10);
    public final AbstractSetting<Integer> XZPlusCost = new IntegerSetting("XZ+ penalty", "Score penaly when pushing crystal in XZ- direction (double mode)", 3, 0, 10)
            .showIf(this.allowDouble::getValue);
    public final AbstractSetting<Integer> concatBonus = new IntegerSetting("Concat bonus", "Score bonus when torch powering multiple pistons (faster)", 3, 0, 10);
    public final AbstractSetting<Boolean> allowContract = new BooleanSetting("Allow contract", "Allow any piston contraction (allow contracting piston on the side of crystal)", true)
            .showIf(this.allowDouble::getValue);
    private PlayerEntity target = null;
    private AttackPos attack = null;
    private Direction currentDir = null;
    private int loop = 0;

    @Override
    public String getDescription() {
        return "Piston aura";
    }

    @Override
    public void onActivate(boolean live) {
        loop = 0;
        attack = null;
        target = null;
        currentDir = null;
    }

    @Override
    public void onDeactivate(boolean live) {
        if (attack == null) return;
        Entity crystal = CombatUtils.CrystalAt(attack.readyCrystalPos, 3.5);
        if (crystal == null) return;
        PlayerUtils.Swing(swing.getValue(), Hand.MAIN_HAND);
        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
        crystal.remove(Entity.RemovalReason.KILLED);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.world == null) return;
        if (target == null || target.isDead() || target.isRemoved() || !target.getPos().isInRange(mc.player.getPos(), range.getValue())) {
            target = CombatUtils.GetPlayerTarget(targetMode.getValue(), onlySurrounded.getValue() ? CombatUtils::isSurrounded : null, range.getValue(), !targetFriends.getValue(), !targetNoStuffs.getValue());
            if (target == null) return;
        }
        if (loop-- > 0) return;
        if (attack == null) {
            attack = FindAttackPos();
            if (attack == null) return;
            if (debug.getValue()) {
                for (PistonPos piston : attack.pistons) {
                    WHLogger.printToChat("Piston " + piston.pos.toShortString());
                    if (piston.support != null) {
                        for (BlockPos pos : piston.support) {
                            WHLogger.printToChat(" - " + pos.toShortString());
                        }
                    }
                    WHLogger.printToChat(" Torch " + piston.redstonePos.torch.toShortString());
                    WHLogger.printToChat("  Wall " + piston.redstonePos.wall.toShortString());
                    if (piston.redstonePos.supports != null) {
                        for (BlockPos pos : piston.redstonePos.supports) {
                            WHLogger.printToChat("  - " + pos.toShortString());
                        }
                    }
                }
            }
        }
        Attack();
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (currentDir == null) return;
        if (event.packet instanceof PlayerMoveC2SPacket movePacket) {
            if (movePacket.changesLook()) {
                float packetYaw = movePacket.getYaw(0);
                float packetPitch = movePacket.getPitch(0);
                Vec3d vec = Vec3d.of(currentDir.getVector());
                double error = MathUtils.Angle(vec, Vec3d.fromPolar(packetPitch, packetYaw));
                if (error > 40) {
                    ((PlayerMoveC2SPacketAccessor) movePacket).setYaw((currentDir.getHorizontalQuarterTurns() & 3) * 90f);
                    ((PlayerMoveC2SPacketAccessor) movePacket).setPitch(currentDir.getVector().getY() * -90f);
                }
            }
        }
    }

    private void Attack() {
        Status status;
        Entity pushedCrystal = CombatUtils.CrystalAt(attack.readyCrystalPos, 0.5);
        if (pushedCrystal == null) {
            // all piston placed
            // check
            boolean pushed = false;
            for (PistonPos piston : attack.pistons) {
                Entity crystal = CombatUtils.CrystalAt(piston.requiredCrystal, 0.25);
                if (crystal != null) {
                    BlockPos headPos = piston.head();
                    BlockState headState = mc.world.getBlockState(headPos);
                    if (canExtend(headState)) {
                        // extend piston
                        status = PlaceRedstone(piston.redstonePos);
                        if (status == Status.Abort) {
                            if (debug.getValue()) {
                                WHLogger.printToChat("Cannot place torch " + piston.redstonePos.torch.toShortString());
                            }
                            Reset();
                            return;
                        }
                        SetDelay(DelayType.Push);
                        pushed = true;
                        break;
                    }
                    else if (headState.isOf(Blocks.PISTON_HEAD) || headState.isOf(Blocks.MOVING_PISTON)) {
                        // other piston is blocking, deactivate it
                        BlockPos blockingPos = headPos.offset(headState.get(Properties.FACING).getOpposite());
                        PistonPos blocking = null;
                        for (PistonPos p : attack.pistons) {
                            if (blockingPos.equals(p.pos)) {
                                blocking = p;
                                break;
                            }
                        }
                        if (piston.equals(blocking)) return;
                        if (blocking == null || !blocking.canContract) {
                            // abort attack
                            if (debug.getValue()) {
                                WHLogger.printToChat("Piston " + piston.pos.toShortString() + " blocked by non contractible piston " + blockingPos.toShortString());
                            }
                            Reset();
                            return;
                        }
                        BreakRedstone(blocking.redstonePos.torch);
                        SetDelay(DelayType.Place);
                        pushed = true;
                        break;
                    }
                    else {
                        // abort attack
                        if (debug.getValue()) {
                            WHLogger.printToChat("Piston " + piston.pos.toShortString() + " blocked by block");
                        }
                        Reset();
                        return;
                    }
                }
            }

            if (!pushed) {
                // nothing to push -> missing crystal
                // turn off every piston then place it
                // place every supports (crystal hitbox might prevent placing them later)
                if (BreakRedstones() == Status.Continue) {
                    status = PlaceSupports();
                    if (status != Status.Continue) {
                        if (status == Status.Abort) {
                            if (debug.getValue()) {
                                WHLogger.printToChat("Cannot place supports");
                            }
                            Reset();
                            return;
                        }
                        SetDelay(DelayType.Place);
                        return;
                    }

                    ArrayList<PistonPos> toPlace = new ArrayList<>();
                    for (PistonPos piston : attack.pistons) {
                        BlockState pState = mc.world.getBlockState(piston.pos);
                        if (pState.isReplaceable()) {
                            toPlace.add(piston);
                        }
                        else if (!(PISTON_BLOCK.contains(pState.getBlock()) && pState.get(Properties.FACING) == piston.dir)) {
                            if (debug.getValue()) {
                                WHLogger.printToChat("Piston " + piston.pos.toShortString() + " facing wrong direction");
                            }
                            Reset();
                            return;
                        }
                    }

                    boolean placed = false;
                    if (currentDir != null) {
                        for (int i = 0; i < toPlace.size(); i++) {
                            PistonPos piston = toPlace.get(i);
                            if (piston.dir == currentDir.getOpposite()) {
                                status = PlacePiston(piston);
                                if (status == Status.Abort) {
                                    WHLogger.printToChat("Cannot place piston " + piston.pos.toShortString());
                                    Reset();
                                    return;
                                }
                                SetDelay(DelayType.Place);
                                toPlace.remove(i);
                                placed = true;
                                break;
                            }
                        }
                    }
                    if (!toPlace.isEmpty()) {
                        PistonPos piston = toPlace.getFirst();
                        Direction required = piston.dir.getOpposite();
                        Pair<Float, Float> rot = MathUtils.CalcRotation(required);
                        //((ClientConnectionInvoker) mc.getNetworkHandler().getConnection()).invokeSendImmediately(new PlayerMoveC2SPacket.LookAndOnGround(rot.getLeft(), rot.getRight(), mc.player.isOnGround(), false), null, false);
                        SetDelay(DelayType.PlacePiston);
                        currentDir = required;
                        placed = true;
                    }

                    if (!placed) {
                        BlockPos downPos = attack.crystalPos.down();
                        BlockState downState = mc.world.getBlockState(downPos);
                        if (downState.isReplaceable()) {
                            PlaceObsidian(downPos);
                        }
                        else if (CombatUtils.VALID_CRYSTAL_BLOCK.contains(downState.getBlock())) {
                            for (PistonPos piston : attack.pistons) {
                                if (mc.world.getBlockState(piston.pos).isOf(Blocks.MOVING_PISTON)) {
                                    return;
                                }
                            }
                            status = PlaceCrystal();
                            if (status != Status.Continue) {
                                if (status == Status.Abort) {
                                    if (debug.getValue()) {
                                        WHLogger.printToChat("Cannot place crystal " + attack.crystalPos.toShortString());
                                    }
                                    Reset();
                                }
                                return;
                            }
                            if (fastPlace.getValue()) {
                                status = PlaceRedstone(attack.pistons.getFirst().redstonePos);
                                if (status == Status.Abort) {
                                    if (debug.getValue()) {
                                        WHLogger.printToChat("Fast place: cannot place torch " + attack.pistons.getFirst().redstonePos.torch.toShortString());
                                    }
                                    Reset();
                                    return;
                                }
                                SetDelay(DelayType.Push);
                            }
                        }
                    }
                }
            }
        }
        else {
            // crystal pushed -> explode crystal
            PlayerUtils.Swing(swing.getValue(), Hand.MAIN_HAND);
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(pushedCrystal, mc.player.isSneaking()));
            pushedCrystal.remove(Entity.RemovalReason.KILLED);
            if (fastBreak.getValue()) BreakRedstones();
            SetDelay(DelayType.Attack);
            if (!targetUntilDead.getValue()) target = null;
        }
    }

    private void Reset() {
        // break crystal, might help cleaning up
        Entity crystal = CombatUtils.CrystalAt(attack.crystalPos, 3.5);
        if (crystal != null) {
            PlayerUtils.Swing(swing.getValue(), Hand.MAIN_HAND);
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
            crystal.remove(Entity.RemovalReason.KILLED);
        }
        SetDelay(DelayType.Attack);
        attack = null;
        if (!targetUntilDead.getValue()) target = null;
    }

    private void SetDelay(DelayType type) {
        AttackType attackType = attack.getAttackType();
        // horrible
        switch (attackType) {
            case Normal -> {
                switch (type) {
                    case Place -> loop = placeDelayNormal.getValue();
                    case PlacePiston ->
                            loop = placePistonDelayNormal.getValue();
                    case Push -> loop = pushDelayNormal.getValue();
                    case Attack -> loop = attackDelayNormal.getValue();
                }
            }
            case Head -> {
                switch (type) {
                    case Place -> loop = placeDelayHead.getValue();
                    case PlacePiston -> loop = placePistonDelayHead.getValue();
                    case Push -> loop = pushDelayHead.getValue();
                    case Attack -> loop = attackDelayHead.getValue();
                }
            }
            case Double, Triple -> { // won't impl triple
                switch (type) {
                    case Place -> loop = placeDelayDouble.getValue();
                    case PlacePiston ->
                            loop = placePistonDelayDouble.getValue();
                    case Push -> loop = pushDelayDouble.getValue();
                    case Attack -> loop = attackDelayDouble.getValue();
                }
            }
        }
    }

    private Status PlaceSupports() {
        Status status;
        for (PistonPos piston : attack.pistons) {
            status = PlaceSupport(piston.support);
            if (status != Status.Continue) return status;
        }
        for (PistonPos piston : attack.pistons) {
            status = PlaceSupport(piston.redstonePos.supports);
            if (status != Status.Continue) return status;
            BlockPos wall = piston.redstonePos.wall;
            BlockState wallState = mc.world.getBlockState(wall);
            Direction dir = MathUtils.fromVec3i(wall.subtract(piston.redstonePos.torch));
            if (wallState.isReplaceable()) {
                status = PlaceObsidian(wall);
                if (status != Status.Continue) return status;
            }
            else if (!canPlaceTorch(wall, dir)) return Status.Abort;
        }
        return Status.Continue;
    }

    private Status PlaceSupport(ArrayList<BlockPos> blocks) {
        if (blocks == null) return Status.Continue;
        int support = InventoryUtils.searchHotbar((itemStack) -> CombatUtils.SUPPORT_BLOCKS.contains(itemStack.getItem()));
        if (support == -1) return Status.Return;

        int i = blocks.size() - 1;
        while (i >= 0 && !mc.world.getBlockState(blocks.get(i)).isFullCube(mc.world, blocks.get(i)))
            i--;
        if (i == blocks.size() - 1) {
            return Status.Continue; // support already placed
        }
        i++;

        BlockPos pos = blocks.get(i);
        if (!pos.isWithinDistance(mc.player.getEyePos(), range.getValue())) {
            return Status.Abort;
        }
        Status status = CheckPlaceBlock(pos);
        if (status != Status.Continue) return status;
        BlockUtils.place(pos, support, true, swing.getValue());
        loop = supportDelay.getValue();
        return Status.Return;
    }

    private Status CheckPlaceBlock(BlockPos pos) {
        return CheckPlace(new Box(pos), true);
    }

    private Status CheckPlace(Box box, boolean isBlock) {
        AtomicReference<EndCrystalEntity> last = new AtomicReference<>();
        if (WorldUtils.EntityIntersect(box, entity -> {
            if (entity instanceof EndCrystalEntity crystal) last.set(crystal);
            return !isBlock || !(entity instanceof ItemEntity);
        })) {
            Entity crystal = last.get();
            if (crystal == null) return Status.Abort;
            PlayerUtils.Swing(swing.getValue(), Hand.MAIN_HAND);
            mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
            crystal.remove(Entity.RemovalReason.KILLED);
            return Status.Return;
        }
        return Status.Continue;
    }




    /*
     * Computation
     */

    private Status PlaceObsidian(BlockPos pos) {
        int obsidianSlot = InventoryUtils.searchHotbar(stack -> stack.isOf(Items.OBSIDIAN));
        if (obsidianSlot == -1) return Status.Return;
        if (!pos.isWithinDistance(mc.player.getEyePos(), range.getValue())) {
            return Status.Abort;
        }
        Status status = CheckPlaceBlock(pos);
        if (status != Status.Continue) return status;
        BlockUtils.place(pos, obsidianSlot, true, swing.getValue());
        return Status.Return;
    }

    private Status BreakRedstones() {
        for (PistonPos piston : attack.pistons) {
            BlockPos pos = piston.redstonePos.torch;
            BlockState state = mc.world.getBlockState(pos);
            if (REDSTONE_TORCH.contains(state.getBlock())) {
                BreakRedstone(pos);
                return Status.Return;
            }
        }
        return Status.Continue;
    }

    private void BreakRedstone(BlockPos redstonePos) {
        if (!redstonePos.isWithinDistance(mc.player.getEyePos(), range.getValue())) {
            return;
        }
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, redstonePos, Direction.UP));
        PlayerUtils.Swing(swing.getValue(), Hand.MAIN_HAND);
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, redstonePos, Direction.UP));
    }

    private Status PlaceRedstone(RedstonePos redstonePos) {
        Status status;
        BlockState wallState = mc.world.getBlockState(redstonePos.wall);
        if (wallState.isReplaceable()) {
            status = PlaceSupport(redstonePos.supports);
            if (status != Status.Continue) return status;
            return PlaceObsidian(redstonePos.wall);
        }

        BlockPos wall = redstonePos.wall;
        if (!wall.isWithinDistance(mc.player.getEyePos(), range.getValue())) {
            return Status.Abort;
        }
        int torchSlot = InventoryUtils.searchHotbar(stack -> stack.isOf(Items.REDSTONE_TORCH));
        if (torchSlot == -1) return Status.Return;

        BlockHitResult blockHit = new BlockHitResult(Vec3d.ofCenter(wall), redstonePos.dir.getOpposite(), wall, false);
        InventoryUtils.selectSlot(torchSlot, true);
        PlayerUtils.Swing(swing.getValue(), Hand.MAIN_HAND);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHit);
        InventoryUtils.selectBack();
        return Status.Return;
    }

    private Status PlacePiston(PistonPos piston) {
        BlockPos pos = piston.pos;
        if (!pos.isWithinDistance(mc.player.getEyePos(), range.getValue())) {
            return Status.Abort;
        }
        int pistonSlot = InventoryUtils.searchHotbar(stack -> stack.isOf(Items.PISTON));
        if (pistonSlot == -1) return Status.Return;
        Status status = CheckPlaceBlock(pos);
        if (status != Status.Continue) return status;
        BlockUtils.place(pos, pistonSlot, true, swing.getValue());
        return Status.Return;
    }

    private Status PlaceCrystal() {
        BlockPos interactPos = attack.crystalPos.down();
        if (!interactPos.isWithinDistance(mc.player.getEyePos(), range.getValue())) {
            return Status.Abort;
        }
        int endCrystal = InventoryUtils.searchHotbar(stack -> stack.isOf(Items.END_CRYSTAL));
        if (endCrystal == -1) return Status.Return;

        if (!mc.world.getBlockState(attack.crystalPos).isAir()) {
            return Status.Abort;
        }
        if (!CombatUtils.VALID_CRYSTAL_BLOCK.contains(mc.world.getBlockState(interactPos).getBlock())) {
            return Status.Abort;
        }

        Status status = CheckPlace(new Box(attack.crystalPos).stretch(0, 1, 0), false);
        if (status != Status.Continue) return status;

        InventoryUtils.selectSlot(endCrystal, true);
        PlayerUtils.Swing(swing.getValue(), Hand.MAIN_HAND);
        BlockHitResult bhr = new BlockHitResult(Vec3d.ofCenter(interactPos), Direction.UP, interactPos, true);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        InventoryUtils.selectBack();
        return Status.Continue;
    }

    @Nullable
    private AttackPos FindAttackPos() {
        BlockPos pos = target.getBlockPos().up(); // head block pos

        AttackPos best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (int i = 0; i <= 1; i++) {
            if (!allowHead.getValue() && i == 1) continue;
            if (!allowNormal.getValue() && i == 0) continue;
            for (Direction dir : XZ) {
                BlockPos crystalPos = pos.offset(dir).up(i);
                AttackPos attackPos = findNormal(crystalPos, dir.getOpposite());
                if (attackPos == null) continue;
                double score = attackPos.getScore();
                if (score < bestScore) {
                    best = attackPos;
                    bestScore = score;
                    if (skipScoreThreshold.getValue() != 0 && score <= skipScoreThreshold.getValue()) {
                        return attackPos; // can't be better
                    }
                }
            }
        }
        if (!allowDouble.getValue() || best != null) {
            return best; // don't do double if single found
        }

        pos = target.getBlockPos(); // you can double into their feet
        for (int i = 0; i <= 2; i++) {
            if (mc.world.isOutOfHeightLimit(pos.getY() + i)) continue;
            for (Direction dir2 : AxisZ) {
                for (Direction dir1 : AxisX) {
                    BlockPos crystalPos = pos.offset(dir1).offset(dir2).up(i);
                    AttackPos attackPos = findDouble(crystalPos, dir1.getOpposite(), dir2.getOpposite());
                    if (attackPos != null) {
                        double score = attackPos.getScore();
                        if (score < bestScore) {
                            best = attackPos;
                            bestScore = score;
                            if (skipScoreThreshold.getValue() != 0 && score <= skipScoreThreshold.getValue()) {
                                return attackPos; // can't be better
                            }
                        }
                    }

                    attackPos = findDouble(crystalPos, dir2.getOpposite(), dir1.getOpposite());
                    if (attackPos != null) {
                        double score = attackPos.getScore();
                        if (score < bestScore) {
                            best = attackPos;
                            bestScore = score;
                            if (skipScoreThreshold.getValue() != 0 && score <= skipScoreThreshold.getValue()) {
                                return attackPos; // can't be better
                            }
                        }
                    }
                }
            }
        }
        return best;
    }

    @Nullable
    private AttackPos findNormal(BlockPos crystalPos, Direction dir) {
        BlockState crystalPosState = mc.world.getBlockState(crystalPos);
        if (!(crystalPosState.isAir() || REDSTONE_TORCH.contains(crystalPosState.getBlock()))) {
            return null;
        }
        if (!crystalPos.down().isWithinDistance(mc.player.getEyePos(), range.getValue())) {
            return null;
        }
        BlockState downState = mc.world.getBlockState(crystalPos.down());
        if (!downState.isReplaceable() && !CombatUtils.VALID_CRYSTAL_BLOCK.contains(downState.getBlock())) {
            return null;
        }

        Box box = new Box(crystalPos).stretch(0, 1, 0);
        if (WorldUtils.EntityIntersect(box)) return null;

        AttackPos best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        Vec3d firstPos = crystalPos.toBottomCenterPos();
        for (PistonPos piston : getPistonPos(firstPos, dir)) {
            if (mc.world.isOutOfHeightLimit(piston.pos)) continue;
            if (piston.pos.equals(crystalPos)) continue;
            if (!piston.pos.isWithinDistance(mc.player.getEyePos(), range.getValue())) {
                continue;
            }
            if (!canPlacePiston(piston.pos, dir)) continue;

            if (placeMode.getValue() == BlockUtils.SupportModes.Support) {
                ArrayList<BlockPos> support = BlockUtils.FindSupport(mc.player.getEyePos(), piston.pos, supportDepth.getValue(), range.getValue(), List.of(), List.of());
                if (support == null) continue;
                piston.setSupport(support);
            }

            piston.requiredCrystal = firstPos;
            AttackPos attackPos = new AttackPos(crystalPos, firstPos.offset(dir, 0.75), piston);
            if (!findRedstone(attackPos)) continue;

            double score = attackPos.getScore();
            if (score < bestScore) {
                best = attackPos;
                bestScore = score;
                if (skipScoreThreshold.getValue() != 0 && score <= skipScoreThreshold.getValue()) {
                    break;
                }
            }
        }
        return best;
    }

    public AttackPos findDouble(BlockPos crystalPos, Direction dir1, Direction dir2) {
        BlockState crystalPosState = mc.world.getBlockState(crystalPos);
        if (!(crystalPosState.isAir() || REDSTONE_TORCH.contains(crystalPosState.getBlock()))) {
            return null;
        }
        if (!crystalPos.down().isWithinDistance(mc.player.getEyePos(), range.getValue())) {
            return null;
        }
        BlockState downState = mc.world.getBlockState(crystalPos.down());
        if (!downState.isReplaceable() && !CombatUtils.VALID_CRYSTAL_BLOCK.contains(downState.getBlock())) {
            return null;
        }

        Box box = new Box(crystalPos).stretch(0, 1, 0);
        if (WorldUtils.EntityIntersect(box)) return null;

        Vec3d firstPos = crystalPos.toBottomCenterPos();
        Vec3d secondPos = firstPos.offset(dir1, 0.5f); // pushed against block
        ArrayList<PistonPos> firstPistons = getPistonPos(firstPos, dir1);
        ArrayList<PistonPos> secondPistons = getPistonPos(secondPos, dir2);

        AttackPos best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (PistonPos firstPiston : firstPistons) {
            if (mc.world.isOutOfHeightLimit(firstPiston.pos)) continue;
            if (firstPiston.pos.equals(crystalPos)) continue;
            if (!firstPiston.pos.isWithinDistance(mc.player.getEyePos(), range.getValue())) {
                continue;
            }
            if (!canPlacePiston(firstPiston.pos, dir1)) continue;

            if (placeMode.getValue() == BlockUtils.SupportModes.Support) {
                ArrayList<BlockPos> support = BlockUtils.FindSupport(mc.player.getEyePos(), firstPiston.pos, supportDepth.getValue(), range.getValue(), List.of(crystalPos), List.of());
                if (support == null) continue;
                firstPiston.setSupport(support);
            }
            for (PistonPos secondPiston : secondPistons) {
                if (mc.world.isOutOfHeightLimit(secondPiston.pos)) continue;

                if (secondPiston.pos.equals(crystalPos)) continue;

                if (firstPiston.pos.equals(secondPiston.pos)) continue;
                if (secondPiston.head().equals(firstPiston.pos)) continue;
                if (firstPiston.head().equals(secondPiston.pos)) continue;

                if (!canPlacePiston(secondPiston.pos, dir2)) continue;
                if (!secondPiston.pos.isWithinDistance(mc.player.getEyePos(), range.getValue())) {
                    continue;
                }
                if (!firstPiston.canContract && secondPiston.pos.offset(dir2).equals(firstPiston.pos.offset(dir1))) {
                    continue;
                }

                if (placeMode.getValue() == BlockUtils.SupportModes.Support) {
                    ArrayList<BlockPos> support = BlockUtils.FindSupport(mc.player.getEyePos(), firstPiston.pos, supportDepth.getValue(), range.getValue(), List.of(crystalPos, firstPiston.pos), firstPiston.getPlaced());
                    if (support == null) continue;
                    secondPiston.setSupport(support);
                }

                PistonPos confirmedFirstPiston = firstPiston.copy();
                PistonPos confirmedSecondPiston = secondPiston.copy();

                confirmedFirstPiston.requiredCrystal = firstPos;
                confirmedSecondPiston.requiredCrystal = secondPos;
                AttackPos attackPos = new AttackPos(crystalPos, secondPos.offset(dir2, 0.5), new ArrayList<>(List.of(confirmedFirstPiston, confirmedSecondPiston)));
                if (!findRedstone(attackPos)) continue;

                double score = attackPos.getScore();
                if (score < bestScore) {
                    best = attackPos;
                    bestScore = score;
                    if (skipScoreThreshold.getValue() != 0 && score <= skipScoreThreshold.getValue()) {
                        return best;
                    }
                }
            }
        }
        return best;
    }

    public boolean findRedstone(AttackPos attackPos) {
        for (PistonPos piston : attackPos.pistons) {
            RedstonePos best = null;
            double bestScore = Double.POSITIVE_INFINITY;
            List<BlockPos> badWalls = piston.getBadWalls();
            for (int y = 0; y <= 1; y++) { // handle quasi connectivity
                for (Direction dir : Direction.values()) {
                    if (y == 0 && dir == piston.dir) continue;
                    if (y == 1 && dir == Direction.DOWN) {
                        continue; // why look down at piston+1
                    }
                    BlockPos torch = piston.pos.up(y).offset(dir);

                    ArrayList<BlockPos> avoid = attackPos.getAvoid();
                    RedstonePos redstonePos = findRedstoneWall(torch, badWalls, dir == Direction.UP ? XZ : XZ_DOWN, attackPos.getPlaced(), avoid);
                    if (redstonePos == null) continue;
                    if (!OptimizeMultipleExtend(attackPos, redstonePos, piston)) {
                        continue;
                    }

                    double score = redstonePos.score();
                    if (score < bestScore) {
                        best = redstonePos;
                        bestScore = score;
                        if (skipScoreThreshold.getValue() != 0 && score <= skipScoreThreshold.getValue()) {
                            break;
                        }
                    }
                }
            }
            for (Direction dir : XZ_DOWN) { // handle piston powered by torch below block
                if (!RESISTANT_FULL_BLOCKS.contains(mc.world.getBlockState(piston.pos.offset(dir)).getBlock())) {
                    continue;
                }
                BlockPos torch = piston.pos.down().offset(dir);

                ArrayList<BlockPos> avoid = attackPos.getAvoid();
                RedstonePos redstonePos = findRedstoneWall(torch, badWalls, dir == Direction.UP ? XZ : XZ_DOWN, attackPos.getPlaced(), avoid);
                if (redstonePos == null) continue;
                if (!OptimizeMultipleExtend(attackPos, redstonePos, piston)) {
                    continue;
                }

                double score = redstonePos.score();
                if (score < bestScore) {
                    best = redstonePos;
                    bestScore = score;
                    if (skipScoreThreshold.getValue() != 0 && score <= skipScoreThreshold.getValue()) {
                        break;
                    }
                }
            }

            if (best == null) return false;
            piston.setRedstonePos(best);
        }
        return true;
    }

    // return serie of block placement to get a torch there (with airplace, it will contain wall + torch)
    @Nullable
    public RedstonePos findRedstoneWall(BlockPos torchPos, List<BlockPos> badWalls, Direction[] dirs, ArrayList<BlockPos> supportPlaced, ArrayList<BlockPos> supportAvoid) {
        if (mc.world.isOutOfHeightLimit(torchPos)) {
            return null; // outside of world
        }
        BlockState torchState = mc.world.getBlockState(torchPos);
        if (!torchState.isReplaceable()) return null; // cannot place torch
        if (supportAvoid.contains(torchPos)) {
            return null; // cannot place torch on already used block
        }

        RedstonePos best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (Direction dir : dirs) {
            BlockPos pos = torchPos.offset(dir);
            if (mc.world.isOutOfHeightLimit(pos)) continue;
            if (!pos.isWithinDistance(mc.player.getEyePos(), range.getValue())) {
                continue;
            }

            if (badWalls.contains(pos)) continue;

            BlockState state = mc.world.getBlockState(pos);
            if (state.isOf(Blocks.PISTON)) {
                continue; // don't place on piston, they might extend and fuck everything up
            }
            if (supportAvoid.contains(pos)) continue;
            if (canPlaceTorch(pos, dir)) {
                RedstonePos redstonePos = new RedstonePos(torchPos, pos); // best, bail out early
                redstonePos.bonus += 1;
                return redstonePos;
            }

            if (!state.isReplaceable()) continue;

            if (placeMode.getValue() == BlockUtils.SupportModes.AirPlace) {
                if (best == null) {
                    best = new RedstonePos(torchPos, pos); // lowest score is this so just replace
                }
            }
            else {
                ArrayList<BlockPos> support = BlockUtils.FindSupport(mc.player.getEyePos(), pos, supportDepth.getValue(), range.getValue(), supportAvoid, supportPlaced);
                if (support == null) continue;
                RedstonePos redstonePos = new RedstonePos(torchPos, pos, support);
                double score = redstonePos.score();
                if (score < bestScore) {
                    best = redstonePos;
                    bestScore = score;
                    if (skipScoreThreshold.getValue() != 0 && score <= skipScoreThreshold.getValue()) {
                        break;
                    }
                }
            }
        }
        return best;
    }

    // find what the torch power (one torch might power other pistons and do bad stuff)
    // checks for bud too
    // doesn't check powered block
    // affected piston that have overlapping heads -> bad
    // current and future affected needs to be contractible
    // previous piston affected -> update its redstonepos with yours (power both piston at once = much better)
    // future piston affected
    public boolean OptimizeMultipleExtend(AttackPos attackPos, RedstonePos redstonePos, PistonPos current) {
        if (attackPos.pistons.size() == 1) return true;

        ArrayList<Integer> affected = new ArrayList<>();
        BlockPos torch = redstonePos.torch;
        int i = 0;
        int currentIdx = -1;
        for (PistonPos piston : attackPos.pistons) {
            if (piston == current) {
                currentIdx = i++;
                continue;
            }
            ;
            BlockPos pos = piston.pos;

            loop:
            for (int y = 0; y <= 1; y++) { // quasi connectivity
                for (Direction dir : Direction.values()) {
                    if (y == 1 && dir == Direction.DOWN) continue;
                    BlockPos tmp = pos.up(y).offset(dir);
                    if (dir != redstonePos.dir.getOpposite() &&
                            torch.equals(tmp)) {
                        affected.add(i);
                        break loop;
                    }
                    if (y == 1 && dir == Direction.UP) continue;
                    if (RESISTANT_FULL_BLOCKS.contains(mc.world.getBlockState(tmp).getBlock()) &&
                            torch.equals(tmp.down())) {
                        affected.add(i);
                        break loop;
                    }
                }
            }
            i++;
        }
        if (affected.isEmpty()) return true;

        int last = affected.getLast();
        if (last > currentIdx) {
            // current and future affected needs to be contractible
            for (int j : affected) {
                if (j >= currentIdx && !attackPos.pistons.get(j).canContract) {
                    return false;
                }
            }
        }

        int low = affected.getFirst();
        int high = currentIdx;
        if (low > high) {
            int t = low;
            low = high;
            high = t;
        }
        List<PistonPos> pistons = attackPos.pistons.subList(low, high + 1);

        // affected piston that have overlapping heads -> bad
        ArrayList<BlockPos> heads = new ArrayList<>();
        for (PistonPos piston : pistons) {
            if (heads.contains(piston.head())) return false;
            heads.add(piston.head());
        }


        ArrayList<PistonPos> updateOrder = new ArrayList<>();
        // check update order -> bad order might stuck crystal
        for (Vec3i updateDir : TORCH_UPDATE_ORDER) {
            BlockPos pos = redstonePos.torch.add(updateDir);
            for (int j = 0; j < pistons.size(); j++) {
                PistonPos piston = attackPos.pistons.get(j);
                if (pos.equals(piston.pos)) {
                    updateOrder.add(piston);
                    break;
                }
            }
        }

        Vec3d crystalPos = pistons.getFirst().requiredCrystal;
        Box crystalBox = Box.of(crystalPos, 2, 2, 2).offset(0, 1, 0);
        boolean[] pushes = new boolean[updateOrder.size()];
        int j = 0;
        for (PistonPos piston : updateOrder) {
            Vec3d dir = Vec3d.of(piston.dir.getVector());
            Box box = shrink(new Box(piston.head()), dir.multiply(0.75));
            if (crystalBox.intersects(box)) {
                crystalBox = crystalBox.offset(dir.multiply(0.5));
                pushes[j] = true;
            }
            j++;
        }
        j = 0;
        for (PistonPos piston : updateOrder) {
            Box box = new Box(piston.head());
            if (!crystalBox.intersects(box) && !pushes[j]) return false;
            crystalBox = crystalBox.offset(Vec3d.of(piston.dir.getVector()).multiply(0.5));
            j++;
        }

        // update first affected
        PistonPos firstPiston = pistons.getFirst();
        if (firstPiston.equals(current)) {
            redstonePos.bonus += pistons.size() + concatBonus.getValue();
        }
        else {
            RedstonePos newRedstonePos = redstonePos.copy();
            newRedstonePos.bonus += pistons.size() + concatBonus.getValue();
            firstPiston.resetPlaced();
            firstPiston.setRedstonePos(newRedstonePos);
        }
        return true;
    }

    private Box shrink(Box box, Vec3d vec) {
        return box.shrink(vec.x, vec.y, vec.z);
    }

    public boolean canPlaceTorch(BlockPos pos, Direction dir) {
        return mc.world.getBlockState(pos).isSideSolidFullSquare(mc.world, pos, dir.getOpposite());
    }

    public boolean canPlacePiston(BlockPos pos, Direction dir) {
        BlockState state = mc.world.getBlockState(pos);
        boolean alreadyPiston = state.isOf(Blocks.PISTON) && !state.get(Properties.EXTENDED);
        if (!state.isReplaceable() && !alreadyPiston) return false;
        if (!alreadyPiston && !BlockUtils.canPlace(pos, true)) return false;
        if (alreadyPiston && state.get(Properties.FACING) != dir) return false;
        return canExtend(mc.world.getBlockState(pos.offset(dir)));
    }

    public boolean canExtend(BlockState state) {
        return state.isAir() || state.getPistonBehavior() == PistonBehavior.DESTROY;
    }

    // list of pistons that can push crystal (piston whose head collides with crystal hitbox)
    public ArrayList<PistonPos> getPistonPos(Vec3d crystal, Direction pushDir) {
        Box hitbox = Box.of(crystal, 2, 2, 2).offset(0, 1, 0); // crystal hitbox
        int dir = pushDir.getDirection().offset();
        int depth = (int) Math.floor(crystal.getComponentAlongAxis(pushDir.getAxis())) + dir;
        Pair<BlockPos, BlockPos> pair = MathUtils.BoxBlockBound(hitbox);
        BlockPos min = MathUtils.withAxis(pair.getLeft(), pushDir.getAxis(), depth);
        BlockPos max = MathUtils.withAxis(pair.getRight(), pushDir.getAxis(), depth);

        Pair<Direction.Axis, Direction.Axis> tangents = MathUtils.getTangents(pushDir.getAxis());
        Direction.Axis iAxis = tangents.getLeft();
        Direction.Axis jAxis = tangents.getRight();
        int di = max.getComponentAlongAxis(iAxis) - min.getComponentAlongAxis(iAxis);
        int dj = max.getComponentAlongAxis(jAxis) - min.getComponentAlongAxis(jAxis);

        boolean canContract = allowContract.getValue();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        ArrayList<PistonPos> pistons = new ArrayList<>();
        loop:
        while (true) {
            for (int i = 0; i <= di; i++) {
                for (int j = 0; j <= dj; j++) {
                    mutable.set(min).move(Direction.from(iAxis, Direction.AxisDirection.POSITIVE), i).move(Direction.from(jAxis, Direction.AxisDirection.POSITIVE), j);
                    if (hitbox.intersects(new Box(mutable))) {
                        pistons.add(new PistonPos(new BlockPos(mutable).offset(pushDir.getAxis(), -dir), pushDir, canContract));
                    }
                    else {break loop;}
                }
            }
            min = min.offset(pushDir.getAxis(), -dir);
            canContract = true;
        }

        return pistons;
    }

    private enum AttackType {
        Normal,
        Head,
        Double,
        Triple,
    }

    private enum DelayType {
        PlacePiston, // piston requires player rotation
        Place,
        Push,
        Attack
    }

    private enum Status {
        Continue,
        Return,
        Abort,
    }

    private static class PistonPos {

        public BlockPos pos;
        public ArrayList<BlockPos> support;
        public Direction dir;
        public Vec3d requiredCrystal;
        public boolean canContract;
        public ArrayList<BlockPos> placed;
        private RedstonePos redstonePos;

        public PistonPos(BlockPos pos, Direction dir, boolean canContract, ArrayList<BlockPos> support) {
            this.pos = pos;
            this.dir = dir;
            this.canContract = canContract;
            this.support = support;
            resetPlaced();
        }

        public PistonPos(BlockPos pos, Direction dir, boolean canContract) {
            this(pos, dir, canContract, null);
        }

        public BlockPos head() {
            return pos.offset(dir);
        }

        public void resetPlaced() {
            placed = new ArrayList<>(List.of(pos, head()));
            if (support != null) {placed.addAll(support);}
        }

        public void setRedstonePos(RedstonePos redstonePos) {
            this.redstonePos = redstonePos;
        }

        public void setSupport(ArrayList<BlockPos> support) {
            this.support = support;
            placed.addAll(support);
        }

        public ArrayList<BlockPos> getPlaced() {
            ArrayList<BlockPos> tmp = (ArrayList<BlockPos>) placed.clone();
            if (redstonePos != null) {
                if (redstonePos.supports != null) {
                    tmp.addAll(redstonePos.supports);
                }
                tmp.add(redstonePos.wall);
            }
            return tmp;
        }

        public PistonPos copy() {
            PistonPos piston = new PistonPos(pos, dir, canContract, support);
            if (redstonePos != null) piston.redstonePos = redstonePos.copy();
            piston.placed = new ArrayList<>(placed);
            return piston;
        }

        public ArrayList<BlockPos> getBadWalls() {
            return new ArrayList<>(List.of(pos, pos.up()));
        }

        public double score() {
            return redstonePos.score() + (support == null ? 0 : support.size());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof PistonPos p) {
                return this.dir == p.dir && this.pos.equals(p.pos);
            }
            return false;
        }
    }

    public static class RedstonePos {

        public final ArrayList<BlockPos> supports; // may be null if airplace
        public BlockPos torch;
        public BlockPos wall; // or floor but same
        public Direction dir;
        public double bonus = 0;

        public RedstonePos(BlockPos torch, BlockPos wall, ArrayList<BlockPos> supports) {
            this.supports = supports;
            this.wall = wall;
            this.torch = torch;
            this.dir = MathUtils.fromVec3i(wall.subtract(torch));
        }

        public RedstonePos(BlockPos torch, BlockPos wall) {
            this(torch, wall, null);
        }

        public double score() {
            return (supports == null ? 1 : supports.size()) - bonus; // ignore already placed wall because bail out
        }

        public RedstonePos copy() {
            RedstonePos redstonePos = new RedstonePos(torch, wall, supports);
            redstonePos.bonus = bonus;
            return redstonePos;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RedstonePos o) {
                return o.torch.equals(o.torch);
            }
            return false;
        }
    }

    private class AttackPos {

        public BlockPos crystalPos;
        public Vec3d readyCrystalPos;
        public ArrayList<PistonPos> pistons;

        public ArrayList<BlockPos> placed = null;

        public AttackPos(BlockPos crystalPos, Vec3d readyCrystalPos, ArrayList<PistonPos> pistons) {
            this.crystalPos = crystalPos;
            this.readyCrystalPos = readyCrystalPos;
            this.pistons = pistons;
        }

        public AttackPos(BlockPos crystalPos, Vec3d readyCrystalPos, PistonPos piston) {
            this.crystalPos = crystalPos;
            this.readyCrystalPos = readyCrystalPos;
            this.pistons = new ArrayList<>(List.of(piston));
        }

        public double getScore() {
            double score = 0;
            for (PistonPos p : pistons) {
                score += p.score();
            }
            score += CombatUtils.VALID_CRYSTAL_BLOCK.contains(mc.world.getBlockState(crystalPos.down()).getBlock()) ? 0 : 1;
            if (pistons.size() == 2) {
                // floating point fuckery
                Vec3i d = pistons.getFirst().dir.getVector().offset(pistons.getLast().dir);
                if (d.equals(new Vec3i(-1, 0, -1)) ||
                        (mc.world.isAir(crystalPos.add(d)) && d.equals(new Vec3i(1, 0, 1)))) {
                    score += XZPlusCost.getValue();
                }
            }
            if (torchOnCrystalCost.getValue() > 0) {
                for (PistonPos p : pistons) {
                    if (crystalPos.isWithinDistance(p.redstonePos.torch, 1.4)) {
                        score += torchOnCrystalCost.getValue();
                        break;
                    }
                }
            }
            return score;
        }

        public AttackType getAttackType() {
            if (pistons.size() == 2) return AttackType.Double;
            if (pistons.size() == 3) return AttackType.Triple;
            BlockPos head = target.getBlockPos().up(target.isCrawling() ? 1 : 2);
            if (crystalPos.getY() == head.getY()) return AttackType.Head;
            return AttackType.Normal;
        }

        public ArrayList<BlockPos> getAvoid() {
            ArrayList<BlockPos> avoid = new ArrayList<>();
            pistons.forEach(p -> {
                avoid.addAll(p.getPlaced());
                avoid.add(p.pos);
                avoid.add(p.head());
                if (p.redstonePos != null) {avoid.add(p.redstonePos.torch);}
            });
            avoid.add(crystalPos);
            return avoid;
        }

        public ArrayList<BlockPos> getPlaced() {
            if (placed != null) return placed;
            placed = new ArrayList<>();
            pistons.forEach(p -> placed.addAll(p.getPlaced()));
            return placed;
        }
    }
}