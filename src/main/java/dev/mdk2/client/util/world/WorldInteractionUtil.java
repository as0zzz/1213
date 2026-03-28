package dev.mdk2.client.util.world;

import dev.mdk2.client.util.combat.InventoryUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.EmptyBlockReader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class WorldInteractionUtil {
    private WorldInteractionUtil() {
    }

    public static boolean isBreakable(final BlockState state) {
        return !state.isAir()
            && state.getDestroySpeed(EmptyBlockReader.INSTANCE, BlockPos.ZERO) >= 0.0F
            && state.getBlock() != Blocks.BEDROCK
            && state.getBlock() != Blocks.BARRIER;
    }

    public static boolean breakBlock(final Minecraft minecraft, final BlockPos pos) {
        if (minecraft.player == null || minecraft.gameMode == null || minecraft.level == null) {
            return false;
        }

        final Direction face = findBestFace(minecraft.level, pos);
        minecraft.gameMode.startDestroyBlock(pos, face);
        minecraft.gameMode.continueDestroyBlock(pos, face);
        minecraft.player.swing(Hand.MAIN_HAND);
        return true;
    }

    public static int findPlaceableBlockSlot(final PlayerEntity player, final boolean fromInventory) {
        final Predicate<ItemStack> predicate = new Predicate<ItemStack>() {
            @Override
            public boolean test(final ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof BlockItem;
            }
        };
        if (fromInventory) {
            return InventoryUtil.ensureHotbarItem(player, predicate, player.inventory.selected);
        }
        return InventoryUtil.findHotbarSlot(player, predicate);
    }

    public static boolean placeBlock(final Minecraft minecraft, final BlockPos pos, final int hotbarSlot) {
        if (minecraft.player == null || minecraft.gameMode == null || minecraft.level == null || hotbarSlot < 0) {
            return false;
        }

        if (!minecraft.level.getBlockState(pos).getMaterial().isReplaceable()) {
            return false;
        }

        final PlacementContext context = findPlacementContext(minecraft.level, pos);
        if (context == null || !InventoryUtil.selectHotbarSlot(minecraft.player, hotbarSlot)) {
            return false;
        }

        final Vector3d hitVector = Vector3d.atCenterOf(context.supportPos)
            .add(context.face.getStepX() * 0.5D, context.face.getStepY() * 0.5D, context.face.getStepZ() * 0.5D);
        final BlockRayTraceResult hitResult = new BlockRayTraceResult(hitVector, context.face, context.supportPos, false);
        minecraft.gameMode.useItemOn(minecraft.player, minecraft.level, Hand.MAIN_HAND, hitResult);
        minecraft.player.swing(Hand.MAIN_HAND);
        return true;
    }

    public static boolean useItemOn(final Minecraft minecraft, final BlockPos pos, final Direction face, final Hand hand) {
        if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null) {
            return false;
        }

        final Vector3d hitVector = Vector3d.atCenterOf(pos)
            .add(face.getStepX() * 0.35D, face.getStepY() * 0.35D, face.getStepZ() * 0.35D);
        final BlockRayTraceResult hitResult = new BlockRayTraceResult(hitVector, face, pos, false);
        minecraft.gameMode.useItemOn(minecraft.player, minecraft.level, hand, hitResult);
        minecraft.player.swing(hand);
        return true;
    }

    public static BlockPos findNearestBlock(final World world, final BlockPos center, final int radius,
                                            final Predicate<BlockState> predicate) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    final BlockPos pos = center.offset(x, y, z);
                    final BlockState state = world.getBlockState(pos);
                    if (!predicate.test(state)) {
                        continue;
                    }

                    final double distance = center.distSqr(pos);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos.immutable();
                    }
                }
            }
        }
        return best;
    }

    public static List<BlockPos> findNearestBlocks(final World world, final BlockPos center, final int radius,
                                                   final int limit, final Predicate<BlockState> predicate) {
        final List<BlockPos> positions = new ArrayList<BlockPos>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    final BlockPos pos = center.offset(x, y, z);
                    if (predicate.test(world.getBlockState(pos))) {
                        positions.add(pos.immutable());
                    }
                }
            }
        }
        positions.sort(new Comparator<BlockPos>() {
            @Override
            public int compare(final BlockPos first, final BlockPos second) {
                return Double.compare(center.distSqr(first), center.distSqr(second));
            }
        });
        if (positions.size() > limit) {
            return new ArrayList<BlockPos>(positions.subList(0, limit));
        }
        return positions;
    }

    public static boolean isMatureCrop(final BlockState state) {
        final Block block = state.getBlock();
        if (block instanceof CropsBlock) {
            return ((CropsBlock) block).isMaxAge(state);
        }
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= 3;
        }
        return block == Blocks.SWEET_BERRY_BUSH && state.getValue(net.minecraft.block.SweetBerryBushBlock.AGE) >= 3;
    }

    public static boolean isTreeLog(final BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(BlockTags.CRIMSON_STEMS) || state.is(BlockTags.WARPED_STEMS);
    }

    public static int findBonemealSlot(final PlayerEntity player, final boolean fromInventory) {
        final Predicate<ItemStack> predicate = new Predicate<ItemStack>() {
            @Override
            public boolean test(final ItemStack stack) {
                return stack.getItem() == Items.BONE_MEAL;
            }
        };
        if (fromInventory) {
            return InventoryUtil.ensureHotbarItem(player, predicate, player.inventory.selected);
        }
        return InventoryUtil.findHotbarSlot(player, predicate);
    }

    public static boolean isEdgeUnsafe(final PlayerEntity player, final double forwardX, final double forwardZ) {
        final World world = player.level;
        final double targetX = player.getX() + forwardX;
        final double targetZ = player.getZ() + forwardZ;
        final BlockPos below = new BlockPos(MathHelper.floor(targetX), MathHelper.floor(player.getY() - 0.6D), MathHelper.floor(targetZ));
        return world.getBlockState(below).isAir();
    }

    private static Direction findBestFace(final World world, final BlockPos pos) {
        for (final Direction direction : Direction.values()) {
            final BlockPos neighbor = pos.relative(direction);
            if (!world.getBlockState(neighbor).isAir()) {
                return direction.getOpposite();
            }
        }
        return Direction.UP;
    }

    private static PlacementContext findPlacementContext(final World world, final BlockPos target) {
        for (final Direction direction : Direction.values()) {
            final BlockPos support = target.relative(direction);
            final BlockState supportState = world.getBlockState(support);
            if (!supportState.isAir() && supportState.getMaterial().isSolid()) {
                return new PlacementContext(support, direction.getOpposite());
            }
        }
        return null;
    }

    private static final class PlacementContext {
        private final BlockPos supportPos;
        private final Direction face;

        private PlacementContext(final BlockPos supportPos, final Direction face) {
            this.supportPos = supportPos;
            this.face = face;
        }
    }
}
