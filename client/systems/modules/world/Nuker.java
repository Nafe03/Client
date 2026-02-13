package dev.anarchy.waifuhax.client.systems.modules.world;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class Nuker extends AbstractModule {
    
    public enum Mode {
        NORMAL,    // Break blocks in range
        FLATTEN,   // Only break blocks above you
        SMASH      // Only break blocks below you
    }
    
    public enum Shape {
        SPHERE,    // Spherical range
        CUBE       // Cubic range
    }
    
    public final AbstractSetting<Mode> mode = new EnumSetting<>("Mode", "Nuker mode", Mode.NORMAL);
    public final AbstractSetting<Shape> shape = new EnumSetting<>("Shape", "Range shape", Shape.SPHERE);
    public final AbstractSetting<Float> range = new FloatSetting("Range", "Break range", 4.5f, 1.0f, 6.0f);
    public final AbstractSetting<Integer> delay = new IntegerSetting("Delay", "Delay between breaks (ticks)", 0, 0, 20);
    public final AbstractSetting<Integer> blocksPerTick = new IntegerSetting("Blocks/Tick", "Max blocks to break per tick", 1, 1, 10);
    
    public final AbstractSetting<Boolean> rotate = new BooleanSetting("Rotate", "Rotate to blocks", true);
    public final AbstractSetting<Boolean> swing = new BooleanSetting("Swing", "Swing hand", true);
    public final AbstractSetting<Boolean> filterBlocks = new BooleanSetting("Filter", "Only break specific blocks", false);
    
    // Filter options
    public final AbstractSetting<Boolean> ores = new BooleanSetting("Ores Only", "Only break ores", false);
    public final AbstractSetting<Boolean> logs = new BooleanSetting("Logs", "Break logs", false);
    public final AbstractSetting<Boolean> leaves = new BooleanSetting("Leaves", "Break leaves", false);
    
    private int tickCounter = 0;
    private final List<Block> targetBlocks = new ArrayList<>();
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        
        tickCounter++;
        if (tickCounter < delay.getValue()) {
            return;
        }
        tickCounter = 0;
        
        updateTargetBlocks();
        
        Vec3d playerPos = mc.player.getPos();
        Mode currentMode = mode.getValue();
        Shape currentShape = shape.getValue();
        int blocksDestroyed = 0;
        
        // Find blocks to break
        for (int x = (int) -range.getValue(); x <= range.getValue(); x++) {
            for (int y = (int) -range.getValue(); y <= range.getValue(); y++) {
                for (int z = (int) -range.getValue(); z <= range.getValue(); z++) {
                    if (blocksDestroyed >= blocksPerTick.getValue()) {
                        return;
                    }
                    
                    BlockPos pos = new BlockPos(
                        (int) playerPos.x + x,
                        (int) playerPos.y + y,
                        (int) playerPos.z + z
                    );
                    
                    // Check shape
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (currentShape == Shape.SPHERE && distance > range.getValue()) {
                        continue;
                    }
                    
                    // Check mode
                    if (currentMode == Mode.FLATTEN && y < 0) {
                        continue;
                    }
                    if (currentMode == Mode.SMASH && y > 0) {
                        continue;
                    }
                    
                    Block block = mc.world.getBlockState(pos).getBlock();
                    
                    // Skip air and unbreakable blocks
                    if (block == Blocks.AIR || block == Blocks.BEDROCK) {
                        continue;
                    }
                    
                    // Filter check
                    if (filterBlocks.getValue() && !shouldBreakBlock(block)) {
                        continue;
                    }
                    
                    // Rotate to block
                    if (rotate.getValue()) {
                        Vec3d blockCenter = Vec3d.ofCenter(pos);
                        Vec3d diff = blockCenter.subtract(mc.player.getEyePos());
                        
                        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90;
                        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z)));
                        
                        mc.player.setYaw(yaw);
                        mc.player.setPitch(pitch);
                    }
                    
                    // Break block
                    mc.interactionManager.attackBlock(pos, Direction.UP);
                    
                    if (swing.getValue()) {
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    
                    blocksDestroyed++;
                }
            }
        }
    }
    
    private void updateTargetBlocks() {
        targetBlocks.clear();
        
        if (ores.getValue()) {
            targetBlocks.add(Blocks.COAL_ORE);
            targetBlocks.add(Blocks.IRON_ORE);
            targetBlocks.add(Blocks.GOLD_ORE);
            targetBlocks.add(Blocks.DIAMOND_ORE);
            targetBlocks.add(Blocks.EMERALD_ORE);
            targetBlocks.add(Blocks.LAPIS_ORE);
            targetBlocks.add(Blocks.REDSTONE_ORE);
            targetBlocks.add(Blocks.COPPER_ORE);
            targetBlocks.add(Blocks.DEEPSLATE_COAL_ORE);
            targetBlocks.add(Blocks.DEEPSLATE_IRON_ORE);
            targetBlocks.add(Blocks.DEEPSLATE_GOLD_ORE);
            targetBlocks.add(Blocks.DEEPSLATE_DIAMOND_ORE);
            targetBlocks.add(Blocks.DEEPSLATE_EMERALD_ORE);
            targetBlocks.add(Blocks.DEEPSLATE_LAPIS_ORE);
            targetBlocks.add(Blocks.DEEPSLATE_REDSTONE_ORE);
            targetBlocks.add(Blocks.DEEPSLATE_COPPER_ORE);
            targetBlocks.add(Blocks.NETHER_GOLD_ORE);
            targetBlocks.add(Blocks.NETHER_QUARTZ_ORE);
            targetBlocks.add(Blocks.ANCIENT_DEBRIS);
        }
        
        if (logs.getValue()) {
            targetBlocks.add(Blocks.OAK_LOG);
            targetBlocks.add(Blocks.SPRUCE_LOG);
            targetBlocks.add(Blocks.BIRCH_LOG);
            targetBlocks.add(Blocks.JUNGLE_LOG);
            targetBlocks.add(Blocks.ACACIA_LOG);
            targetBlocks.add(Blocks.DARK_OAK_LOG);
            targetBlocks.add(Blocks.MANGROVE_LOG);
            targetBlocks.add(Blocks.CHERRY_LOG);
        }
        
        if (leaves.getValue()) {
            targetBlocks.add(Blocks.OAK_LEAVES);
            targetBlocks.add(Blocks.SPRUCE_LEAVES);
            targetBlocks.add(Blocks.BIRCH_LEAVES);
            targetBlocks.add(Blocks.JUNGLE_LEAVES);
            targetBlocks.add(Blocks.ACACIA_LEAVES);
            targetBlocks.add(Blocks.DARK_OAK_LEAVES);
            targetBlocks.add(Blocks.MANGROVE_LEAVES);
            targetBlocks.add(Blocks.CHERRY_LEAVES);
            targetBlocks.add(Blocks.AZALEA_LEAVES);
            targetBlocks.add(Blocks.FLOWERING_AZALEA_LEAVES);
        }
    }
    
    private boolean shouldBreakBlock(Block block) {
        return targetBlocks.isEmpty() || targetBlocks.contains(block);
    }
    
    @Override
    public String getDescription() {
        return "Automatically break blocks around you";
    }
}