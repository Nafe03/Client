package dev.anarchy.waifuhax.client.systems.modules.combat;

import dev.anarchy.waifuhax.api.AbstractSetting;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * COMPLETE AutoCrystal - Automatic End Crystal PvP
 * Places and explodes crystals automatically
 */
public class AutoCrystal extends AbstractModule {

    public enum TargetMode {
        CLOSEST,        // Closest enemy
        LOWEST_HP,      // Lowest health
        HIGHEST_HP,     // Highest health
        MOST_DAMAGE,    // Most potential damage
        CLOSEST_ANGLE   // Closest to crosshair
    }
    
    public enum PlaceMode {
        NORMAL,     // Place then attack
        INSTANT,    // Place and attack instantly
        SMART       // Calculate best timing
    }

    @CategorySetting(name = "General")
    public final AbstractSetting<Float> placeRange = new FloatSetting("Place Range", "Crystal placement range", 5.0f, 0f, 6f);
    public final AbstractSetting<Float> breakRange = new FloatSetting("Break Range", "Crystal break range", 5.0f, 0f, 6f);
    public final AbstractSetting<Float> targetRange = new FloatSetting("Target Range", "Target selection range", 12f, 0f, 16f);
    public final AbstractSetting<Boolean> autoSwitch = new BooleanSetting("Auto Switch", "Auto switch to crystals", true);
    public final AbstractSetting<Boolean> rotate = new BooleanSetting("Rotate", "Rotate to crystal", true);
    public final AbstractSetting<Boolean> rayTrace = new BooleanSetting("Ray Trace", "Only place/break visible crystals", false);

    @CategorySetting(name = "Target")
    public final AbstractSetting<TargetMode> targetMode = new EnumSetting<>("Target Mode", "How to select targets", TargetMode.CLOSEST);
    public final AbstractSetting<Boolean> targetFriends = new BooleanSetting("Target Friends", "Target friends", false);
    public final AbstractSetting<Boolean> targetNaked = new BooleanSetting("Target Naked", "Target naked players", true);
    public final AbstractSetting<Boolean> antiSuicide = new BooleanSetting("Anti Suicide", "Don't kill yourself", true);

    @CategorySetting(name = "Placement")
    public final AbstractSetting<PlaceMode> placeMode = new EnumSetting<>("Place Mode", "Crystal placement mode", PlaceMode.NORMAL);
    public final AbstractSetting<Integer> placeDelay = new IntegerSetting("Place Delay", "Delay between placements (ms)", 50, 0, 500);
    public final AbstractSetting<Boolean> support = new BooleanSetting("Support", "Place support blocks", true);
    public final AbstractSetting<Boolean> multiPlace = new BooleanSetting("Multi Place", "Place multiple crystals", false);
    public final AbstractSetting<Integer> maxCrystals = new IntegerSetting("Max Crystals", "Max crystals to place", 3, 1, 10)
            .showIf(multiPlace::getValue);

    @CategorySetting(name = "Break")
    public final AbstractSetting<Integer> breakDelay = new IntegerSetting("Break Delay", "Delay between breaks (ms)", 50, 0, 500);
    public final AbstractSetting<Boolean> inhibit = new BooleanSetting("Inhibit", "Wait for own crystals", true);
    public final AbstractSetting<Boolean> antiWeakness = new BooleanSetting("Anti Weakness", "Switch from crystals when weak", true);
    public final AbstractSetting<Boolean> switchBack = new BooleanSetting("Switch Back", "Switch back after breaking", true);

    @CategorySetting(name = "Damage")
    public final AbstractSetting<Float> minDamage = new FloatSetting("Min Damage", "Minimum damage to target", 6f, 0f, 36f);
    public final AbstractSetting<Float> maxSelfDamage = new FloatSetting("Max Self Damage", "Maximum damage to self", 6f, 0f, 36f);
    public final AbstractSetting<Float> lethalDamage = new FloatSetting("Lethal Damage", "Place even if lethal to target", 8f, 0f, 36f);
    public final AbstractSetting<Boolean> ignoreTerrainDamage = new BooleanSetting("Ignore Terrain", "Ignore terrain damage reduction", false);

    private PlayerEntity target = null;
    private long lastPlaceTime = 0;
    private long lastBreakTime = 0;
    private List<BlockPos> placedCrystals = new ArrayList<>();

    @Override
    public String getDescription() {
        return "Automatic End Crystal combat";
    }

    @Override
    public void onActivate(boolean live) {
        super.onActivate(live);
        target = null;
        placedCrystals.clear();
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        // Find target
        target = findTarget();
        if (target == null) return;

        // Break existing crystals first
        breakCrystals();

        // Then place new crystals
        placeCrystals();
    }

    private PlayerEntity findTarget() {
        List<PlayerEntity> targets = new ArrayList<>();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player) continue;
            if (player.isDead() || !player.isAlive()) continue;
            if (mc.player.distanceTo(player) > targetRange.getValue()) continue;
            if (!targetFriends.getValue() && isFriend(player)) continue;
            if (!targetNaked.getValue() && isNaked(player)) continue;

            targets.add(player);
        }

        if (targets.isEmpty()) return null;

        // Sort by target mode
        targets.sort(getTargetComparator());
        return targets.get(0);
    }

    private void breakCrystals() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBreakTime < breakDelay.getValue()) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            if (mc.player.distanceTo(crystal) > breakRange.getValue()) continue;

            // Check if should break this crystal
            BlockPos crystalPos = crystal.getBlockPos();
            if (inhibit.getValue() && placedCrystals.contains(crystalPos)) {
                // Check if enough time has passed
                continue;
            }

            // Ray trace check
            if (rayTrace.getValue() && !mc.player.canSee(crystal)) continue;

            // Rotate if needed
            if (rotate.getValue()) {
                rotateTo(crystal.getPos());
            }

            // Attack crystal
            mc.player.networkHandler.sendPacket(
                PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking())
            );
            mc.player.swingHand(Hand.MAIN_HAND);

            lastBreakTime = currentTime;
            placedCrystals.remove(crystalPos);
            break; // One crystal per tick
        }
    }

    private void placeCrystals() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPlaceTime < placeDelay.getValue()) return;

        // Find crystal slot
        int crystalSlot = InventoryUtils.searchHotbar(stack -> stack.isOf(Items.END_CRYSTAL));
        if (crystalSlot == -1) {
            if (autoSwitch.getValue()) {
                // Try to find in inventory
                crystalSlot = findCrystalInInventory();
            }
            if (crystalSlot == -1) return;
        }

        // Find best position
        BlockPos bestPos = findBestCrystalPos();
        if (bestPos == null) return;

        // Check if support needed
        if (support.getValue() && !isValidCrystalPos(bestPos)) {
            placeSupport(bestPos);
            return;
        }

        // Place crystal
        placeCrystal(bestPos, crystalSlot);
        lastPlaceTime = currentTime;
    }

    private BlockPos findBestCrystalPos() {
        if (target == null) return null;

        BlockPos targetPos = target.getBlockPos();
        List<BlockPos> validPositions = new ArrayList<>();

        // Search around target
        for (int x = -5; x <= 5; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos pos = targetPos.add(x, y, z);

                    // Check range
                    if (mc.player.getEyePos().distanceTo(pos.toCenterPos()) > placeRange.getValue()) continue;

                    // Check if valid crystal position
                    if (!isValidCrystalPos(pos)) continue;

                    // Check damage
                    float damage = CombatUtils.ExplosionDamage(pos.toCenterPos().add(0, 1, 0), target, 6f);
                    float selfDamage = CombatUtils.ExplosionDamage(pos.toCenterPos().add(0, 1, 0), mc.player, 6f);

                    if (damage < minDamage.getValue()) continue;
                    if (antiSuicide.getValue() && selfDamage > maxSelfDamage.getValue()) continue;
                    if (selfDamage > mc.player.getHealth()) continue;

                    validPositions.add(pos);
                }
            }
        }

        if (validPositions.isEmpty()) return null;

        // Find best position
        validPositions.sort((pos1, pos2) -> {
            float damage1 = CombatUtils.ExplosionDamage(pos1.toCenterPos().add(0, 1, 0), target, 6f);
            float damage2 = CombatUtils.ExplosionDamage(pos2.toCenterPos().add(0, 1, 0), target, 6f);
            return Float.compare(damage2, damage1);
        });

        return validPositions.get(0);
    }

    private boolean isValidCrystalPos(BlockPos pos) {
        // Check if block below is valid
        if (!CombatUtils.VALID_CRYSTAL_BLOCK.contains(mc.world.getBlockState(pos.down()).getBlock())) {
            return false;
        }

        // Check if space is clear
        if (!mc.world.isAir(pos) || !mc.world.isAir(pos.up())) {
            return false;
        }

        // Check for entities
        return mc.world.getOtherEntities(null, new net.minecraft.util.math.Box(pos).expand(0, 1, 0)).isEmpty();
    }

    private void placeCrystal(BlockPos pos, int slot) {
        // Select crystal
        InventoryUtils.selectSlot(slot, false);

        // Rotate if needed
        if (rotate.getValue()) {
            rotateTo(pos.toCenterPos());
        }

        // Place crystal
        BlockHitResult hitResult = new BlockHitResult(
            pos.down().toCenterPos(),
            Direction.UP,
            pos.down(),
            false
        );

        BlockUtils.Interact(Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);

        placedCrystals.add(pos);

        // Switch back if needed
        if (switchBack.getValue()) {
            InventoryUtils.selectBack();
        }
    }

    private void placeSupport(BlockPos pos) {
        int obsidianSlot = InventoryUtils.searchHotbar(stack -> 
            stack.isOf(Items.OBSIDIAN) || stack.isOf(Items.CRYING_OBSIDIAN)
        );
        if (obsidianSlot == -1) return;

        InventoryUtils.selectSlot(obsidianSlot, false);

        BlockHitResult hitResult = new BlockHitResult(
            pos.down().toCenterPos(),
            Direction.UP,
            pos.down(2),
            false
        );

        BlockUtils.Interact(Hand.MAIN_HAND, hitResult);
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

    private Comparator<PlayerEntity> getTargetComparator() {
        return (p1, p2) -> {
            switch (targetMode.getValue()) {
                case CLOSEST:
                    return Double.compare(mc.player.distanceTo(p1), mc.player.distanceTo(p2));
                case LOWEST_HP:
                    return Float.compare(p1.getHealth(), p2.getHealth());
                case HIGHEST_HP:
                    return Float.compare(p2.getHealth(), p1.getHealth());
                case MOST_DAMAGE:
                    // Would need to calculate potential damage
                    return 0;
                case CLOSEST_ANGLE:
                    Vec3d look = mc.player.getRotationVec(1.0f);
                    Vec3d toP1 = p1.getPos().subtract(mc.player.getPos()).normalize();
                    Vec3d toP2 = p2.getPos().subtract(mc.player.getPos()).normalize();
                    double angle1 = Math.acos(look.dotProduct(toP1));
                    double angle2 = Math.acos(look.dotProduct(toP2));
                    return Double.compare(angle1, angle2);
                default:
                    return 0;
            }
        };
    }

    private boolean isFriend(PlayerEntity player) {
        // Implement friend system
        return false;
    }

    private boolean isNaked(PlayerEntity player) {
        for (int i = 0; i < 4; i++) {
            if (!player.getInventory().getStack(36 + i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private int findCrystalInInventory() {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.END_CRYSTAL)) {
                return i;
            }
        }
        return -1;
    }
}