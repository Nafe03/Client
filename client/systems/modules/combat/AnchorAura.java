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
import dev.anarchy.waifuhax.client.events.MouseButtonPressedEvent;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * FIXED: AnchorAura module
 * - Fixed missing target assignment in BestPos() method
 * - Added null checks for better stability
 * - Improved code documentation
 */
public class AnchorAura extends AbstractModule {

    @CategorySetting(name = "General")
    public final AbstractSetting<Float> range = new FloatSetting("Range", "Anchor placement range", 6f, 0f, 6f);
    public final AbstractSetting<Float> targetRange = new FloatSetting("Target Range", "Target range", 8f, 0f, 12f);
    public final AbstractSetting<Integer> searchRadius = new IntegerSetting("Radius", "Search radius (anchor placement).", 3, 0, 6);
    public final AbstractSetting<Boolean> onClick = new BooleanSetting("OnClick", "Only attack on click", false);
    public final AbstractSetting<Boolean> selectBack = new BooleanSetting("Select back", "Select slot back", true);
    public final AbstractSetting<Boolean> rotate = new BooleanSetting("Rotate", "Rotate towards target", true);

    @CategorySetting(name = "Target")
    public final AbstractSetting<CombatUtils.TargetModes> targetMode = new EnumSetting<>("Target mode", "Target priority", CombatUtils.TargetModes.ClosestAngle);
    public final AbstractSetting<Boolean> targetFriends = new BooleanSetting("Target friends", "Target your friends", false);
    public final AbstractSetting<Boolean> targetNoStuffs = new BooleanSetting("Target nostuffs", "Waste your equipment on players without armor", false);

    @CategorySetting(name = "Placement")
    public final AbstractSetting<PlacementMode> placementMode = new EnumSetting<>("Placement Mode", "Placement order", PlacementMode.ExplodePlace);
    public final AbstractSetting<Integer> initDelay = new IntegerSetting("Init Delay", "First delay in explode-place order.", 1, 1, 20)
            .showIf(() -> placementMode.getValue() == PlacementMode.ExplodePlace);
    public final AbstractSetting<Integer> attackDelay = new IntegerSetting("Attack Delay", "Delay between attacks", 10, 1, 20);
    public final AbstractSetting<Integer> destroyDelay = new IntegerSetting("Destroy Delay", "Delay between placing and destroying an anchor (0 for instant)", 0, 0, 20);

    @CategorySetting(name = "Damage")
    public final AbstractSetting<Integer> minDamage = new IntegerSetting("Min Damage", "Minimum damage", 9, 0, 36);
    public final AbstractSetting<Integer> maxSelfDamage = new IntegerSetting("Max Self Damage", "Maximum self damage", 6, 0, 36);
    public final AbstractSetting<Boolean> noSuicide = new BooleanSetting("No suicide", "Do not kill yourself", true);
    
    public AttackPos attackPos;
    public int time;

    @Override
    public String getDescription() {
        return "Anchor aura";
    }

    @Override
    public void onActivate(boolean live) {
        attackPos = null;
        time = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (time > 1) {
            time--;
            return;
        }
        if (onClick.getValue()) return;
        Tick();
    }

    @EventHandler
    public void onMouseButton(MouseButtonPressedEvent event) {
        if (mc.player == null) return;
        if (!onClick.getValue()) return;
        if (event.getButton() != 0 || event.getAction() != 1) return;
        if (Tick()) event.cancel();
    }

    private boolean SelectNonGlow() {
        if (!mc.player.getMainHandStack().isOf(Items.GLOWSTONE)) return true;

        int last = InventoryUtils.getLastSlot();
        if (last != -1) {
            if (!mc.player.getInventory().getStack(last).isOf(Items.GLOWSTONE)) {
                InventoryUtils.selectBack();
                return true;
            }
        }
        int free = InventoryUtils.searchHotbar(stack -> !stack.isOf(Items.GLOWSTONE));
        if (free == -1) {
            return false; // hotbar full of glowstone
        }
        InventoryUtils.selectSlot(free, false);
        return true;
    }

    public Status Attack(PlayerEntity target, List<PlayerEntity> avoid) {
        if (target == null) return Status.BadTarget;
        
        int anchorSlot = InventoryUtils.searchHotbar(item -> item.isOf(Items.RESPAWN_ANCHOR));
        int glowSlot = InventoryUtils.searchHotbar(item -> item.isOf(Items.GLOWSTONE));
        if (glowSlot == -1) return Status.Abort;

        if (attackPos == null) {
            attackPos = BestPos(target);
            if (attackPos == null) {
                avoid.add(target);
                return Status.BadTarget;
            }
        }

        BlockState state = mc.world.getBlockState(attackPos.pos);
        if (placementMode.getValue() == PlacementMode.PlaceExplode) {
            if (state.isOf(Blocks.RESPAWN_ANCHOR)) {
                if (state.get(Properties.CHARGES) <= 0) {
                    Place(attackPos.pos, glowSlot, true, true);
                }
                if (!SelectNonGlow()) return Status.Abort;
                Interact(attackPos.pos);
                time = attackDelay.getValue();
            }
            else {
                if (anchorSlot == -1) return Status.Abort;
                if (!BlockUtils.canPlace(attackPos.pos, true)) {
                    attackPos = null;
                    return Status.BadTarget;
                }
                if (destroyDelay.getValue() == 0) { // instant
                    Place(attackPos.pos, anchorSlot, true, true);
                    Place(attackPos.pos, glowSlot, false, true);
                    if (!SelectNonGlow()) return Status.Abort;
                    Interact(attackPos.pos);
                    time = attackDelay.getValue();
                }
                else {
                    Place(attackPos.pos, anchorSlot, true, false);
                    time = destroyDelay.getValue();
                }
            }
        }
        else {
            if (state.isOf(Blocks.RESPAWN_ANCHOR)) {
                if (destroyDelay.getValue() == 0 && anchorSlot == -1) {
                    return Status.Abort;
                }
                if (state.get(Properties.CHARGES) <= 0) {
                    Place(attackPos.pos, glowSlot, true, true);
                }

                if (destroyDelay.getValue() == 0) {
                    InventoryUtils.selectSlot(anchorSlot, false);
                    Interact(attackPos.pos); // explode
                    Interact(attackPos.pos, false); // place
                    if (selectBack.getValue()) InventoryUtils.selectBack();
                    time = attackDelay.getValue();
                }
                else {
                    if (!SelectNonGlow()) return Status.Abort;
                    Interact(attackPos.pos);
                    time = destroyDelay.getValue();
                }
            }
            else {
                if (anchorSlot == -1) return Status.Abort;
                Place(attackPos.pos, anchorSlot, true, false);
                time = initDelay.getValue();
            }
        }
        return Status.Success;
    }

    public boolean Tick() {
        if (mc.player == null || mc.world == null) return false;
        
        Status status;
        ArrayList<PlayerEntity> avoid = new ArrayList<>();
        if (attackPos != null) {
            status = Attack(attackPos.target, avoid);
            if (status == Status.Success) return true;
            if (status == Status.Abort) return false;
            if (attackPos != null && attackPos.target != null) {
                avoid.add(attackPos.target);
            }
        }
        // first try current target then try others if it fails

        while (true) {
            PlayerEntity target = CombatUtils.GetPlayerTarget(targetMode.getValue(),
                    e -> !avoid.contains(e),
                    targetRange.getValue(),
                    !targetFriends.getValue(),
                    !targetNoStuffs.getValue());
            if (target == null) return false;

            status = Attack(target, avoid);
            if (status == Status.Success) return true;
            if (status == Status.Abort) return false;
            avoid.add(target);
        }
    }

    public void Interact(BlockPos pos) {
        Interact(pos, true);
    }

    public void Interact(BlockPos pos, boolean inside) {
        BlockHitResult bhr = new BlockHitResult(pos.toCenterPos(), Direction.UP, pos, inside);
        BlockUtils.Interact(Hand.MAIN_HAND, bhr);
    }

    public void Place(BlockPos pos, int slot, boolean setLast, boolean forceNoSelectBack) {
        InventoryUtils.selectSlot(slot, setLast);
        BlockHitResult bhr = new BlockHitResult(pos.toCenterPos(), Direction.DOWN, pos, false);

        BlockUtils.Interact(Hand.MAIN_HAND, bhr);
        if (!forceNoSelectBack && selectBack.getValue()) {
            InventoryUtils.selectBack();
        }
    }

    @Nullable
    public AttackPos BestPos(PlayerEntity target) {
        if (target == null) return null;
        
        BlockPos targetBlockPos = target.getBlockPos();
        int r = searchRadius.getValue();

        AttackPos best = null;
        float bestScore = Float.NEGATIVE_INFINITY;
        for (int y = -r; y <= r; y++) {
            for (int z = -r; z <= r; z++) {
                for (int x = -r; x <= r; x++) {
                    if (x * x + y * y + z * z > r * r) continue;
                    BlockPos pos = targetBlockPos.add(x, y, z);
                    if (mc.player.getEyePos().squaredDistanceTo(pos.toCenterPos()) >= range.getValue() * range.getValue()) {
                        continue;
                    }

                    float score = 0;
                    BlockState state = mc.world.getBlockState(pos);
                    if (state.isOf(Blocks.RESPAWN_ANCHOR)) {
                        score++;
                        if (state.get(Properties.CHARGES) > 0) score++;
                        mc.world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                    else if (!BlockUtils.canPlace(pos, true)) {
                        continue;
                    }

                    float damage = CombatUtils.ExplosionDamage(pos.toCenterPos(), target, 5f);
                    float selfDamage = noSuicide.getValue() ? CombatUtils.ExplosionDamage(pos.toCenterPos(), mc.player, 5f) : 0;

                    if (state.isOf(Blocks.RESPAWN_ANCHOR)) {
                        mc.world.setBlockState(pos, state);
                    }

                    if (!CombatUtils.validDamages(selfDamage,
                            damage,
                            maxSelfDamage.getValue(),
                            minDamage.getValue(),
                            noSuicide.getValue())) {
                        continue;
                    }

                    score += damage;
                    AttackPos attackPos = new AttackPos(pos, target); // FIX: Pass target to constructor
                    if (score > bestScore) {
                        bestScore = score;
                        best = attackPos;
                    }
                }
            }
        }
        return best;
    }

    private enum PlacementMode {
        PlaceExplode,
        ExplodePlace,
    }

    private enum Status {
        Success,
        BadTarget,
        Abort,
    }

    public static class AttackPos {

        public BlockPos pos;
        public PlayerEntity target; // keep attacking the same

        // FIX: Added constructor with target parameter
        public AttackPos(BlockPos pos, PlayerEntity target) {
            this.pos = pos;
            this.target = target;
        }

        // Keep old constructor for compatibility
        public AttackPos(BlockPos pos) {
            this(pos, null);
        }
    }
}