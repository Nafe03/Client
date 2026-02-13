package dev.anarchy.waifuhax.client.systems.modules.world;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashSet;
import java.util.Set;

public class XRay extends AbstractModule {

    // Ore settings
    public final AbstractSetting<Boolean> coal = new BooleanSetting("Coal", "Show coal ore", true);
    public final AbstractSetting<Boolean> iron = new BooleanSetting("Iron", "Show iron ore", true);
    public final AbstractSetting<Boolean> gold = new BooleanSetting("Gold", "Show gold ore", true);
    public final AbstractSetting<Boolean> diamond = new BooleanSetting("Diamond", "Show diamond ore", true);
    public final AbstractSetting<Boolean> emerald = new BooleanSetting("Emerald", "Show emerald ore", true);
    public final AbstractSetting<Boolean> lapis = new BooleanSetting("Lapis", "Show lapis ore", true);
    public final AbstractSetting<Boolean> redstone = new BooleanSetting("Redstone", "Show redstone ore", true);
    public final AbstractSetting<Boolean> copper = new BooleanSetting("Copper", "Show copper ore", true);
    public final AbstractSetting<Boolean> quartz = new BooleanSetting("Quartz", "Show nether quartz ore", true);
    public final AbstractSetting<Boolean> debris = new BooleanSetting("Ancient Debris", "Show ancient debris", true);

    // Storage settings
    public final AbstractSetting<Boolean> chests = new BooleanSetting("Chests", "Show chests and barrels", true);
    public final AbstractSetting<Boolean> enderChests = new BooleanSetting("Ender Chests", "Show ender chests", true);
    public final AbstractSetting<Boolean> shulkers = new BooleanSetting("Shulker Boxes", "Show shulker boxes", true);
    public final AbstractSetting<Boolean> spawners = new BooleanSetting("Spawners", "Show mob spawners", true);

    // Other valuable blocks
    public final AbstractSetting<Boolean> portals = new BooleanSetting("Portals", "Show nether portals and end portals", false);
    public final AbstractSetting<Boolean> beds = new BooleanSetting("Beds", "Show beds (useful in nether)", false);

    // Visual settings
    public final AbstractSetting<Integer> opacity = new IntegerSetting("Opacity", "Opacity of hidden blocks (0-255)", 0, 0, 255);
    public final AbstractSetting<Boolean> exposedOnly = new BooleanSetting("Exposed Only", "Only show blocks touching air", false);

    private final Set<Block> xrayBlocks = new HashSet<>();
    private static XRay INSTANCE;

    public XRay() {
        INSTANCE = this;
    }

    public static XRay getInstance() {
        return INSTANCE;
    }

    @Override
    public void onActivate(boolean live) {
        super.onActivate(live);
        updateXrayBlocks();

        // Force chunk reload for proper XRay rendering
        if (mc.world != null && mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }

        // Set gamma to maximum for full brightness
        if (mc.options != null) {
            mc.options.getGamma().setValue(16.0);
        }
    }

    @Override
    public void onDeactivate(boolean live) {
        super.onDeactivate(live);

        // Reset world rendering
        if (mc.world != null && mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }

        // Reset gamma to default
        if (mc.options != null) {
            mc.options.getGamma().setValue(1.0);
        }
    }
    
    private void updateXrayBlocks() {
        xrayBlocks.clear();
        
        // Coal
        if (coal.getValue()) {
            xrayBlocks.add(Blocks.COAL_ORE);
            xrayBlocks.add(Blocks.DEEPSLATE_COAL_ORE);
        }
        
        // Iron
        if (iron.getValue()) {
            xrayBlocks.add(Blocks.IRON_ORE);
            xrayBlocks.add(Blocks.DEEPSLATE_IRON_ORE);
            xrayBlocks.add(Blocks.RAW_IRON_BLOCK);
        }
        
        // Gold
        if (gold.getValue()) {
            xrayBlocks.add(Blocks.GOLD_ORE);
            xrayBlocks.add(Blocks.DEEPSLATE_GOLD_ORE);
            xrayBlocks.add(Blocks.NETHER_GOLD_ORE);
            xrayBlocks.add(Blocks.RAW_GOLD_BLOCK);
        }
        
        // Diamond
        if (diamond.getValue()) {
            xrayBlocks.add(Blocks.DIAMOND_ORE);
            xrayBlocks.add(Blocks.DEEPSLATE_DIAMOND_ORE);
            xrayBlocks.add(Blocks.DIAMOND_BLOCK);
        }
        
        // Emerald
        if (emerald.getValue()) {
            xrayBlocks.add(Blocks.EMERALD_ORE);
            xrayBlocks.add(Blocks.DEEPSLATE_EMERALD_ORE);
            xrayBlocks.add(Blocks.EMERALD_BLOCK);
        }
        
        // Lapis
        if (lapis.getValue()) {
            xrayBlocks.add(Blocks.LAPIS_ORE);
            xrayBlocks.add(Blocks.DEEPSLATE_LAPIS_ORE);
            xrayBlocks.add(Blocks.LAPIS_BLOCK);
        }
        
        // Redstone
        if (redstone.getValue()) {
            xrayBlocks.add(Blocks.REDSTONE_ORE);
            xrayBlocks.add(Blocks.DEEPSLATE_REDSTONE_ORE);
            xrayBlocks.add(Blocks.REDSTONE_BLOCK);
        }
        
        // Copper
        if (copper.getValue()) {
            xrayBlocks.add(Blocks.COPPER_ORE);
            xrayBlocks.add(Blocks.DEEPSLATE_COPPER_ORE);
            xrayBlocks.add(Blocks.RAW_COPPER_BLOCK);
        }
        
        // Quartz
        if (quartz.getValue()) {
            xrayBlocks.add(Blocks.NETHER_QUARTZ_ORE);
        }
        
        // Ancient Debris
        if (debris.getValue()) {
            xrayBlocks.add(Blocks.ANCIENT_DEBRIS);
        }
        
        // Chests and storage
        if (chests.getValue()) {
            xrayBlocks.add(Blocks.CHEST);
            xrayBlocks.add(Blocks.TRAPPED_CHEST);
            xrayBlocks.add(Blocks.BARREL);
        }
        
        if (enderChests.getValue()) {
            xrayBlocks.add(Blocks.ENDER_CHEST);
        }
        
        if (shulkers.getValue()) {
            xrayBlocks.add(Blocks.SHULKER_BOX);
            xrayBlocks.add(Blocks.WHITE_SHULKER_BOX);
            xrayBlocks.add(Blocks.ORANGE_SHULKER_BOX);
            xrayBlocks.add(Blocks.MAGENTA_SHULKER_BOX);
            xrayBlocks.add(Blocks.LIGHT_BLUE_SHULKER_BOX);
            xrayBlocks.add(Blocks.YELLOW_SHULKER_BOX);
            xrayBlocks.add(Blocks.LIME_SHULKER_BOX);
            xrayBlocks.add(Blocks.PINK_SHULKER_BOX);
            xrayBlocks.add(Blocks.GRAY_SHULKER_BOX);
            xrayBlocks.add(Blocks.LIGHT_GRAY_SHULKER_BOX);
            xrayBlocks.add(Blocks.CYAN_SHULKER_BOX);
            xrayBlocks.add(Blocks.PURPLE_SHULKER_BOX);
            xrayBlocks.add(Blocks.BLUE_SHULKER_BOX);
            xrayBlocks.add(Blocks.BROWN_SHULKER_BOX);
            xrayBlocks.add(Blocks.GREEN_SHULKER_BOX);
            xrayBlocks.add(Blocks.RED_SHULKER_BOX);
            xrayBlocks.add(Blocks.BLACK_SHULKER_BOX);
        }
        
        if (spawners.getValue()) {
            xrayBlocks.add(Blocks.SPAWNER);
        }
        
        if (portals.getValue()) {
            xrayBlocks.add(Blocks.NETHER_PORTAL);
            xrayBlocks.add(Blocks.END_PORTAL);
            xrayBlocks.add(Blocks.END_PORTAL_FRAME);
        }
        
        if (beds.getValue()) {
            xrayBlocks.add(Blocks.WHITE_BED);
            xrayBlocks.add(Blocks.ORANGE_BED);
            xrayBlocks.add(Blocks.MAGENTA_BED);
            xrayBlocks.add(Blocks.LIGHT_BLUE_BED);
            xrayBlocks.add(Blocks.YELLOW_BED);
            xrayBlocks.add(Blocks.LIME_BED);
            xrayBlocks.add(Blocks.PINK_BED);
            xrayBlocks.add(Blocks.GRAY_BED);
            xrayBlocks.add(Blocks.LIGHT_GRAY_BED);
            xrayBlocks.add(Blocks.CYAN_BED);
            xrayBlocks.add(Blocks.PURPLE_BED);
            xrayBlocks.add(Blocks.BLUE_BED);
            xrayBlocks.add(Blocks.BROWN_BED);
            xrayBlocks.add(Blocks.GREEN_BED);
            xrayBlocks.add(Blocks.RED_BED);
            xrayBlocks.add(Blocks.BLACK_BED);
        }
    }
    
    /**
     * Called by mixin to determine if a block should be rendered
     */
    public boolean shouldDrawBlock(Block block) {
        return xrayBlocks.contains(block);
    }
    
    /**
     * Called by mixin to determine if a block should be rendered based on BlockState
     */
    public boolean shouldDrawBlock(BlockState state) {
        if (state == null) return false;
        return shouldDrawBlock(state.getBlock());
    }
    
    /**
     * Check if block is exposed (has air adjacent)
     */
    public boolean isExposed(BlockPos pos) {
        if (!exposedOnly.getValue() || mc.world == null) {
            return true; // If not checking exposure, allow all
        }
        
        for (Direction dir : Direction.values()) {
            BlockPos adjacentPos = pos.offset(dir);
            BlockState adjacentState = mc.world.getBlockState(adjacentPos);
            
            if (adjacentState.isAir() || shouldDrawBlock(adjacentState)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get opacity for hidden blocks
     */
    public int getOpacity() {
        return opacity.getValue();
    }
    
    /**
     * Get the set of blocks to show
     */
    public Set<Block> getXrayBlocks() {
        return new HashSet<>(xrayBlocks);
    }
    
    @Override
    public String getDescription() {
        return "See ores and valuables through walls";
    }
}