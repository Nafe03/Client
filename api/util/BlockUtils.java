package dev.anarchy.waifuhax.api.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class BlockUtils {

    public static final List<Block> ORES = new ArrayList<>();
    public static final List<Block> LEAVES = new ArrayList<>();
    public static final List<Block> LOGS = new ArrayList<>();
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    static {
        ORES.add(Blocks.COAL_ORE);
        ORES.add(Blocks.COPPER_ORE);
        ORES.add(Blocks.IRON_ORE);
        ORES.add(Blocks.LAPIS_ORE);
        ORES.add(Blocks.GOLD_ORE);
        ORES.add(Blocks.REDSTONE_ORE);
        ORES.add(Blocks.DIAMOND_ORE);
        ORES.add(Blocks.EMERALD_ORE);
        ORES.add(Blocks.DEEPSLATE_COAL_ORE);
        ORES.add(Blocks.DEEPSLATE_COPPER_ORE);
        ORES.add(Blocks.DEEPSLATE_IRON_ORE);
        ORES.add(Blocks.DEEPSLATE_LAPIS_ORE);
        ORES.add(Blocks.DEEPSLATE_GOLD_ORE);
        ORES.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        ORES.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        ORES.add(Blocks.DEEPSLATE_EMERALD_ORE);
    }

    static {
        LEAVES.add(Blocks.AZALEA_LEAVES);
        LEAVES.add(Blocks.FLOWERING_AZALEA_LEAVES);
        LEAVES.add(Blocks.OAK_LEAVES);
        LEAVES.add(Blocks.BIRCH_LEAVES);
        LEAVES.add(Blocks.SPRUCE_LEAVES);
        LEAVES.add(Blocks.DARK_OAK_LEAVES);
        LEAVES.add(Blocks.JUNGLE_LEAVES);
        LEAVES.add(Blocks.ACACIA_LEAVES);
        LEAVES.add(Blocks.MANGROVE_LEAVES);
        LEAVES.add(Blocks.CHERRY_LEAVES);
    }

    static {
        LOGS.add(Blocks.OAK_LOG);
        LOGS.add(Blocks.BIRCH_LOG);
        LOGS.add(Blocks.SPRUCE_LOG);
        LOGS.add(Blocks.DARK_OAK_LOG);
        LOGS.add(Blocks.JUNGLE_LOG);
        LOGS.add(Blocks.ACACIA_LOG);
        LOGS.add(Blocks.MANGROVE_LOG);
        LOGS.add(Blocks.CHERRY_LOG);
    }

    @Nullable
    public static BlockPos FindBlockPos(Function<BlockPos, Boolean> valid, Function<BlockPos, Float> getScore, BlockPos origin, float radius) {
        List<BlockPos> valids = new ArrayList<>();
        int r = (int) Math.ceil(radius);
        for (int y = -r; y <= r; y++) {
            for (int z = -r; z <= r; z++) {
                for (int x = -r; x <= r; x++) {
                    BlockPos pos = origin.add(x, y, z);
                    if (valid.apply(pos)) {
                        if (getScore == null) return pos;
                        valids.add(pos);
                    }
                }
            }
        }

        BlockPos best = null;
        float bestScore = Float.POSITIVE_INFINITY;
        for (BlockPos pos : valids) {
            float score = getScore.apply(pos);
            if (score < bestScore) {
                best = pos;
                bestScore = score;
            }
        }
        return best;
    }

    public static void Interact(Hand hand, BlockHitResult bhr) {
        mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, bhr, 0));
    }

    @Nullable
    public static BlockPos findFirstSafeSpaceOnTopOfPlayer() {
        BlockPos pos = mc.player.getBlockPos();

        for (int y = mc.player.getBlockY(); y < mc.world.getBottomY() + mc.world.getHeight(); y++) {
            pos = pos.up();

            if (TpUtils.cannotGo(pos) && TpUtils.canGo(pos.up()) && TpUtils.canGo(pos.up(2))) {
                return pos;
            }
        }

        return null;
    }

    @Nullable
    public static BlockPos findFirstSafeSpaceUnderOfPlayer() {
        ClientPlayerEntity self = MinecraftClient.getInstance().player;

        BlockPos pos = mc.player.getBlockPos();
        pos = pos.down(2);
        for (int y = self.getBlockY(); y > mc.world.getBottomY(); y--) {
            pos = pos.down();

            if (TpUtils.cannotGo(pos) && TpUtils.canGo(pos.up()) && TpUtils.canGo(pos.up(2))) {
                return pos;
            }
        }

        return null;
    }

    public static boolean isBlockShape(BlockPos blockPos) {
        VoxelShape shape = mc.world.getBlockState(blockPos).getCollisionShape(mc.world, blockPos);
        return shape.equals(VoxelShapes.fullCube());
    }

    @Nullable
    public static Block fromName(String name) {
        AtomicReference<Block> found = new AtomicReference<>();
        for (Field field : Blocks.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    Block block = (Block) field.get(null);
                    if (name.equalsIgnoreCase(block.getName().getString().replaceAll("\\s", "")) ||
                            name.equalsIgnoreCase(block.getTranslationKey().substring(6))) {
                        found.set(block);
                        break;
                    }
                } catch (IllegalAccessException ignored) {
                } // its static
            }
        }
        return found.get();
    }

    public static Pair<BlockPos, BlockPos> findMinable(BlockPos origin, Block blockType, float radius) {
        Pair<BlockPos, BlockPos> result = new Pair<>(null, null);
        result.setLeft(BlockUtils.FindBlockPos(blockPos -> {
            if (TpUtils.canGo(blockPos) &&
                    TpUtils.canGo(blockPos.up()) &&
                    !TpUtils.canGo(blockPos.down()) && BlockUtils.isBlockShape(blockPos.down())) {

                BlockPos pos = blockPos.up(2);
                if (mc.world.getBlockState(pos).getBlock() == blockType && isSafeToMine(pos)) {
                    result.setRight(pos);
                    return true;
                }
                for (int i = 0; i <= 1; i++) {
                    for (Direction dir : MathUtils.HORIZONTAL_DIR) {
                        pos = blockPos.offset(dir).up(i);
                        if (mc.world.getBlockState(pos).getBlock() == blockType && isSafeToMine(pos)) {
                            result.setRight(pos);
                            return true;
                        }
                    }
                }
            }
            return false;
        }, blockPos -> (float) origin.getSquaredDistance(blockPos), origin, radius));
        return result;
    }

    public static boolean isSafeToMine(BlockPos pos) {
        if (mc.world.getBlockState(pos).getFluidState().isIn(FluidTags.WATER)) {
            return false;
        }
        for (Direction dir : Direction.values()) {
            if (!mc.world.getBlockState(pos.offset(dir)).getFluidState().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public static ArrayList<BlockPos> FindSupport(Vec3d playerPos, BlockPos block) {
        return FindSupport(playerPos, block, 5, 6, List.of(), List.of());
    }

    @Nullable
    public static ArrayList<BlockPos> FindSupport(Vec3d playerPos, BlockPos block, int depth) {
        return FindSupport(playerPos, block, depth, 6, List.of(), List.of());
    }

    @Nullable
    public static ArrayList<BlockPos> FindSupport(Vec3d playerPos, BlockPos block, double range) {
        return FindSupport(playerPos, block, 5, range, List.of(), List.of());
    }

    @Nullable
    public static ArrayList<BlockPos> FindSupport(Vec3d playerPos, BlockPos block, int depth, double range) {
        return FindSupport(playerPos, block, depth, range, List.of(), List.of());
    }

    @Nullable
    public static ArrayList<BlockPos> FindSupport(Vec3d playerPos, BlockPos block, int depth, double range, List<BlockPos> avoid) {
        return FindSupport(playerPos, block, depth, range, avoid, List.of());
    }

    @Nullable
    public static ArrayList<BlockPos> FindSupport(Vec3d playerPos, BlockPos block, int depth, double range, List<BlockPos> avoid, List<BlockPos> placed) {

        ArrayList<Node> openList = new ArrayList<>(List.of(new Node(block, null)));
        ArrayList<Node> closedList = new ArrayList<>(List.of());

        for (BlockPos bp : avoid) {
            closedList.add(new Node(bp, null));
        }

        for (int i = 0; i < depth; i++) {
            if (openList.isEmpty()) return null;
            Node current = openList.getFirst();
            closedList.add(current);
            openList.removeFirst();
            for (Direction dir : Direction.values()) {
                Node pos = new Node(current.offset(dir), current);
                if (closedList.contains(pos)) continue;
                if (mc.world.getBlockState(pos).isOpaque() || placed.contains(pos)) {
                    return RetracePathBP(current);
                }
                if (!openList.contains(pos) && pos.isWithinDistance(playerPos, range) && canPlace(pos)) {
                    openList.add(pos);
                }
            }
        }
        return null;
    }

    public static ArrayList<BlockPos> RetracePathBP(Node end) {
        Node current = end;
        ArrayList<BlockPos> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        return new ArrayList<>(path.subList(0, path.size() - 1));
    }

    public static boolean canPlace(BlockPos pos) {
        return canPlace(pos, false);
    }

    public static boolean canPlace(BlockPos pos, boolean hitbox) {
        BlockState state = mc.world.getBlockState(pos);
        return state.isReplaceable() &&
                !mc.world.isOutOfHeightLimit(pos.getY()) &&
                (!hitbox || mc.world.canPlace(Blocks.STONE.getDefaultState(), pos, ShapeContext.absent()));
    }

    public static void place(BlockPos pos, int slot, boolean selectBack, boolean swing) {
        boolean shouldSwap = mc.player.getInventory().getSelectedSlot() != slot;
        if (shouldSwap) {InventoryUtils.selectSlot(slot);}
        BlockHitResult bhr = new BlockHitResult(pos.toCenterPos(), Direction.DOWN, pos, false);
        PlayerUtils.Swing(swing, Hand.MAIN_HAND);
        Interact(Hand.MAIN_HAND, bhr);
        if (shouldSwap && selectBack) InventoryUtils.selectBack();
    }

    public enum SupportModes {
        AirPlace,
        Support
    }

    public static class Node extends BlockPos {

        public Node parent;
        public double gCost;
        public double hCost;

        public Node(BlockPos pos, Node parent) {
            super(pos);
            this.parent = parent;
        }

        public Node(BlockPos pos, Node parent, int gCost, int hCost) {
            super(pos);
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
        }

        public BlockPos asBlockPos() {
            return super.toImmutable();
        }

        public Vec3i asChunkPos() {
            return new Vec3i(Math.floorDiv(this.getX(), 16), 0, Math.floorDiv(this.getZ(), 16));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Vec3i vec) {
                return this.getX() == vec.getX() && this.getY() == vec.getY() && this.getZ() == vec.getZ();
            }
            return false;
        }
    }
}
