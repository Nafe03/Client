package dev.anarchy.waifuhax.client.systems.modules.combat;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.settings.annotations.CategorySetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.api.util.BlockUtils;
import dev.anarchy.waifuhax.api.util.CombatUtils;
import dev.anarchy.waifuhax.api.util.InventoryUtils;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * CevBreak - Crystal + Ender Chest + Void Break
 * Breaks into holes by placing pistons and crystals above target
 * Forces them out of their safe hole
 */
public class CevBreak extends AbstractModule {
    
    public enum Mode {
        CRYSTAL,    // Use end crystals
        PISTON,     // Use pistons
        BOTH        // Use both
    }

    @CategorySetting(name = "General")
    public final AbstractSetting<Mode> mode = new EnumSetting<>("Mode", "Break mode", Mode.BOTH);
    public final AbstractSetting<Float> range = new FloatSetting("Range", "Target range", 6f, 0f, 6f);
    public final AbstractSetting<Float> targetRange = new FloatSetting("Target Range", "Target selection range", 10f, 0f, 16f);
    public final AbstractSetting<Boolean> rotate = new BooleanSetting("Rotate", "Rotate to blocks", true);
    public final AbstractSetting<Boolean> autoToggle = new BooleanSetting("Auto Toggle", "Toggle when complete", true);

    @CategorySetting(name = "Target")
    public final AbstractSetting<CombatUtils.TargetModes> targetMode = new EnumSetting<>("Target Mode", "Target priority", CombatUtils.TargetModes.ClosestAngle);
    public final AbstractSetting<Boolean> targetFriends = new BooleanSetting("Target Friends", "Target friends", false);
    public final AbstractSetting<Boolean> onlySurrounded = new BooleanSetting("Only Surrounded", "Only target surrounded players", true);

    @CategorySetting(name = "Placement")
    public final AbstractSetting<Integer> placeDelay = new IntegerSetting("Place Delay", "Delay between placements (ticks)", 1, 0, 10);
    public final AbstractSetting<Integer> breakDelay = new IntegerSetting("Break Delay", "Delay before breaking (ticks)", 2, 0, 10);
    public final AbstractSetting<Boolean> multiPlace = new BooleanSetting("Multi Place", "Place multiple crystals", false);

    @CategorySetting(name = "Damage")
    public final AbstractSetting<Integer> minDamage = new IntegerSetting("Min Damage", "Minimum damage", 6, 0, 36);
    public final AbstractSetting<Integer> maxSelfDamage = new IntegerSetting("Max Self Damage", "Maximum self damage", 6, 0, 36);
    public final AbstractSetting<Boolean> noSuicide = new BooleanSetting("No Suicide", "Don't kill yourself", true);

    private PlayerEntity target = null;
    private BlockPos targetPos = null;
    private int ticksWaited = 0;
    private boolean placed = false;

    @Override
    public String getDescription() {
        return "Break into holes - force players out of their safe spots";
    }

    @Override
    public void onActivate(boolean live) {
        super.onActivate(live);
        target = null;
        targetPos = null;
        ticksWaited = 0;
        placed = false;
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        // Find target if needed
        if (target == null || target.isDead() || target.isRemoved()) {
            target = CombatUtils.GetPlayerTarget(
                targetMode.getValue(),
                onlySurrounded.getValue() ? CombatUtils::isSurrounded : null,
                targetRange.getValue(),
                !targetFriends.getValue(),
                false
            );

            if (target == null) {
                WHLogger.printToChat("No valid target found");
                if (autoToggle.getValue()) toggle();
                return;
            }
        }

        // Get target head position
        targetPos = target.getBlockPos().up(2);

        // Check if target is out of range
        if (mc.player.getEyePos().distanceTo(targetPos.toCenterPos()) > range.getValue()) {
            return;
        }

        // Check if target is in a hole
        if (!isInHole(target)) {
            WHLogger.printToChat("Target is not in a hole");
            if (autoToggle.getValue()) toggle();
            return;
        }

        // Execute the break
        if (!placed) {
            placeCrystal();
        } else {
            if (ticksWaited >= breakDelay.getValue()) {
                breakCrystal();
                if (autoToggle.getValue()) {
                    WHLogger.printToChat("CevBreak complete!");
                    toggle();
                }
            } else {
                ticksWaited++;
            }
        }
    }

    private void placeCrystal() {
        // Check if space is clear
        if (!mc.world.isAir(targetPos) || !mc.world.isAir(targetPos.up())) {
            return;
        }

        // Check if base block is valid
        BlockPos basePos = targetPos.down();
        if (!mc.world.getBlockState(basePos).isSolidBlock(mc.world, basePos)) {
            // Try to place obsidian
            placeObsidian(basePos);
            return;
        }

        // Find crystal
        int crystalSlot = InventoryUtils.searchHotbar(stack -> stack.isOf(Items.END_CRYSTAL));
        if (crystalSlot == -1) {
            WHLogger.printToChat("No crystals found");
            if (autoToggle.getValue()) toggle();
            return;
        }

        // Check damage
        float damage = CombatUtils.ExplosionDamage(targetPos.toCenterPos().add(0, 1, 0), target, 6f);
        float selfDamage = CombatUtils.ExplosionDamage(targetPos.toCenterPos().add(0, 1, 0), mc.player, 6f);

        if (damage < minDamage.getValue()) {
            WHLogger.printToChat("Not enough damage");
            return;
        }

        if (noSuicide.getValue() && selfDamage > maxSelfDamage.getValue()) {
            WHLogger.printToChat("Too much self damage");
            return;
        }

        // Select crystal
        InventoryUtils.selectSlot(crystalSlot, false);

        // Rotate if needed
        if (rotate.getValue()) {
            rotateTo(targetPos.toCenterPos());
        }

        // Place crystal
        BlockHitResult hitResult = new BlockHitResult(
            basePos.toCenterPos(),
            Direction.UP,
            basePos,
            false
        );

        BlockUtils.Interact(Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);

        placed = true;
        ticksWaited = 0;
    }

    private void breakCrystal() {
        // Find crystal
        EndCrystalEntity crystal = null;
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity)) continue;
            BlockPos crystalPos = entity.getBlockPos();
            if (crystalPos.equals(targetPos)) {
                crystal = (EndCrystalEntity) entity;
                break;
            }
        }

        if (crystal == null) {
            placed = false;
            return;
        }

        // Rotate if needed
        if (rotate.getValue()) {
            rotateTo(crystal.getPos());
        }

        // Attack crystal
        mc.player.networkHandler.sendPacket(
            PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking())
        );
        mc.player.swingHand(Hand.MAIN_HAND);

        placed = false;
    }

    private void placeObsidian(BlockPos pos) {
        int obsidianSlot = InventoryUtils.searchHotbar(stack -> 
            stack.isOf(Items.OBSIDIAN) || stack.isOf(Items.CRYING_OBSIDIAN)
        );

        if (obsidianSlot == -1) {
            WHLogger.printToChat("No obsidian found");
            if (autoToggle.getValue()) toggle();
            return;
        }

        InventoryUtils.selectSlot(obsidianSlot, false);

        if (rotate.getValue()) {
            rotateTo(pos.toCenterPos());
        }

        // Find placement side
        Direction side = findPlacementSide(pos);
        if (side == null) {
            return;
        }

        BlockHitResult hitResult = new BlockHitResult(
            pos.offset(side).toCenterPos(),
            side.getOpposite(),
            pos.offset(side),
            false
        );

        BlockUtils.Interact(Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private Direction findPlacementSide(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            if (!mc.world.getBlockState(neighbor).isReplaceable()) {
                return dir;
            }
        }
        return Direction.DOWN;
    }

    private boolean isInHole(PlayerEntity player) {
        BlockPos pos = player.getBlockPos();

        // Check all horizontal directions
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            BlockPos checkPos = pos.offset(dir);
            if (mc.world.getBlockState(checkPos).isReplaceable()) {
                return false;
            }
        }

        return true;
    }

    private void rotateTo(Vec3d pos) {
        Vec3d eyePos = mc.player.getEyePos();
        double dx = pos.x - eyePos.x;
        double dy = pos.y - eyePos.y;
        double dz = pos.z - eyePos.z;

        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) -(Math.atan2(dy, dist) * 180.0 / Math.PI);

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }
}