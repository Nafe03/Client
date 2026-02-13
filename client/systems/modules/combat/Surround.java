package dev.anarchy.waifuhax.client.systems.modules.combat;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.settings.annotations.CategorySetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.api.util.BlockUtils;
import dev.anarchy.waifuhax.api.util.InventoryUtils;
import dev.anarchy.waifuhax.api.util.PlayerUtils;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * FIXED: Surround module - corrected API usage
 */
public class Surround extends AbstractModule {
    
    public enum CenterMode {
        DISABLED,   // Don't center
        SNAP,       // Instant center
        MOTION      // Smooth center with motion
    }
    
    public enum SupportMode {
        NORMAL,     // Place blocks normally
        AIRPLACE,   // Place blocks in air
        SUPPORT     // Place support blocks first
    }
    
    @CategorySetting(name = "General")
    public final AbstractSetting<Integer> blocksPerTick = new IntegerSetting("Blocks Per Tick", "Blocks to place per tick", 2, 1, 8);
    public final AbstractSetting<Integer> delay = new IntegerSetting("Delay", "Delay between placements (ticks)", 0, 0, 10);
    public final AbstractSetting<CenterMode> center = new EnumSetting<>("Center", "Center player in block", CenterMode.SNAP);
    public final AbstractSetting<Boolean> onlyGround = new BooleanSetting("Only on Ground", "Only surround when on ground", true);
    public final AbstractSetting<Boolean> toggle = new BooleanSetting("Toggle", "Disable after placing", true);
    public final AbstractSetting<Boolean> rotate = new BooleanSetting("Rotate", "Rotate when placing", true);
    
    @CategorySetting(name = "Placement")
    public final AbstractSetting<SupportMode> support = new EnumSetting<>("Support Mode", "Block placement mode", SupportMode.NORMAL);
    public final AbstractSetting<Boolean> onlyConfirmed = new BooleanSetting("Only Confirmed", "Wait for block place confirmation", false);
    public final AbstractSetting<Boolean> airPlace = new BooleanSetting("Air Place", "Place blocks in air", false);
    
    @CategorySetting(name = "Blocks")
    public final AbstractSetting<Boolean> obsidian = new BooleanSetting("Obsidian", "Use obsidian", true);
    public final AbstractSetting<Boolean> enderChest = new BooleanSetting("Ender Chest", "Use ender chest", true);
    public final AbstractSetting<Boolean> cryingObsidian = new BooleanSetting("Crying Obsidian", "Use crying obsidian", true);
    public final AbstractSetting<Boolean> ancientDebris = new BooleanSetting("Ancient Debris", "Use ancient debris", false);
    
    @CategorySetting(name = "Pattern")
    public final AbstractSetting<Boolean> floor = new BooleanSetting("Floor", "Place floor block", true);
    public final AbstractSetting<Boolean> extendDown = new BooleanSetting("Extend Down", "Extend surround down if in air", true);
    
    private final List<BlockPos> placePositions = new ArrayList<>();
    private int ticksSincePlace = 0;
    
    @Override
    public String getDescription() {
        return "Automatically surrounds you with obsidian";
    }
    
    @Override
    public void onActivate(boolean live) {
        placePositions.clear();
        ticksSincePlace = 0;
        
        if (mc.player == null || mc.world == null) return;
        
        // Center player if needed
        if (center.getValue() != CenterMode.DISABLED && !isCentered()) {
            centerPlayer();
        }
    }
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        // Check if on ground
        if (onlyGround.getValue() && !mc.player.isOnGround()) {
            return;
        }
        
        // Delay check
        if (ticksSincePlace < delay.getValue()) {
            ticksSincePlace++;
            return;
        }
        
        // Find block to place
        int blockSlot = findBlockSlot();
        if (blockSlot == -1) {
            if (toggle.getValue()) {
                toggle();
            }
            return;
        }
        
        // Calculate positions if empty
        if (placePositions.isEmpty()) {
            calculatePositions();
        }
        
        // Place blocks
        int placed = 0;
        List<BlockPos> toRemove = new ArrayList<>();
        
        for (BlockPos pos : placePositions) {
            if (placed >= blocksPerTick.getValue()) break;
            
            // Check if position can be placed
            if (!canPlace(pos)) {
                toRemove.add(pos);
                continue;
            }
            
            // Place block
            if (placeBlock(pos, blockSlot)) {
                placed++;
                toRemove.add(pos);
                ticksSincePlace = 0;
            }
        }
        
        // Remove placed positions
        placePositions.removeAll(toRemove);
        
        // Toggle if done
        if (placePositions.isEmpty() && toggle.getValue()) {
            toggle();
        }
    }
    
    private void calculatePositions() {
        BlockPos playerPos = mc.player.getBlockPos();
        
        // Floor
        if (floor.getValue()) {
            BlockPos below = playerPos.down();
            if (canPlace(below)) {
                placePositions.add(below);
            }
        }
        
        // Extend down if in air
        if (extendDown.getValue() && !mc.world.getBlockState(playerPos.down()).blocksMovement()) {
            BlockPos current = playerPos.down();
            while (!mc.world.getBlockState(current).blocksMovement() && 
                   Math.abs(current.getY() - playerPos.getY()) < 5) {
                if (canPlace(current)) {
                    placePositions.add(0, current); // Add to front for bottom-up placement
                }
                current = current.down();
            }
        }
        
        // Surrounding blocks
        Direction[] dirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (Direction dir : dirs) {
            BlockPos pos = playerPos.offset(dir);
            if (canPlace(pos)) {
                placePositions.add(pos);
            }
        }
    }
    
    private boolean canPlace(BlockPos pos) {
        if (!mc.world.getBlockState(pos).isReplaceable()) {
            return false;
        }
        
        if (!airPlace.getValue() && !BlockUtils.canPlace(pos, false)) {
            return false;
        }
        
        return true;
    }
    
    private boolean placeBlock(BlockPos pos, int slot) {
        // Select slot
        InventoryUtils.selectSlot(slot, false);
        
        // Find direction to place from
        Direction side = getPlaceSide(pos);
        if (side == null) {
            return false;
        }
        
        // Rotate if needed using yaw/pitch calculation
        if (rotate.getValue()) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;
            
            double diffX = x - mc.player.getX();
            double diffY = y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
            double diffZ = z - mc.player.getZ();
            
            double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
            float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
            float pitch = (float) -(Math.atan2(diffY, dist) * 180.0 / Math.PI);
            
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }
        
        // Place block
        BlockHitResult hitResult = new BlockHitResult(
            pos.toCenterPos(),
            side,
            pos,
            false
        );
        
        BlockUtils.Interact(Hand.MAIN_HAND, hitResult);
        
        // Swing
        PlayerUtils.Swing(true, Hand.MAIN_HAND);
        
        return true;
    }
    
    private Direction getPlaceSide(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            if (!mc.world.getBlockState(neighbor).isReplaceable()) {
                return dir.getOpposite();
            }
        }
        return Direction.DOWN; // Default
    }
    
    private int findBlockSlot() {
        List<Block> validBlocks = new ArrayList<>();
        
        if (obsidian.getValue()) validBlocks.add(Blocks.OBSIDIAN);
        if (enderChest.getValue()) validBlocks.add(Blocks.ENDER_CHEST);
        if (cryingObsidian.getValue()) validBlocks.add(Blocks.CRYING_OBSIDIAN);
        if (ancientDebris.getValue()) validBlocks.add(Blocks.ANCIENT_DEBRIS);
        
        return InventoryUtils.searchHotbar(stack -> 
            validBlocks.stream().anyMatch(block -> 
                Block.getBlockFromItem(stack.getItem()) == block
            )
        );
    }
    
    private boolean isCentered() {
        double x = mc.player.getX();
        double z = mc.player.getZ();
        
        double centerX = Math.floor(x) + 0.5;
        double centerZ = Math.floor(z) + 0.5;
        
        return Math.abs(x - centerX) < 0.1 && Math.abs(z - centerZ) < 0.1;
    }
    
    private void centerPlayer() {
        if (center.getValue() == CenterMode.DISABLED) return;
        
        double x = mc.player.getX();
        double z = mc.player.getZ();
        
        double centerX = Math.floor(x) + 0.5;
        double centerZ = Math.floor(z) + 0.5;
        
        if (center.getValue() == CenterMode.SNAP) {
            // Instant center
            mc.player.setPosition(centerX, mc.player.getY(), centerZ);
        } else if (center.getValue() == CenterMode.MOTION) {
            // Smooth center with motion
            double deltaX = centerX - x;
            double deltaZ = centerZ - z;
            
            mc.player.setVelocity(deltaX, mc.player.getVelocity().y, deltaZ);
        }
    }
}