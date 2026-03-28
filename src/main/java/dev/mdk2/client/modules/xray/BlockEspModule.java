package dev.mdk2.client.modules.xray;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.render.WorldRenderUtil;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ColorSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.ColorUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BlockEspModule extends Module {
    private final BooleanSetting diamond;
    private final BooleanSetting emerald;
    private final BooleanSetting ancientDebris;
    private final BooleanSetting gold;
    private final BooleanSetting redstone;
    private final BooleanSetting spawners;
    private final NumberSetting range;
    private final NumberSetting limit;
    private final NumberSetting fillAlpha;
    private final NumberSetting outlineAlpha;
    private final ColorSetting diamondColor;
    private final ColorSetting emeraldColor;
    private final ColorSetting ancientDebrisColor;
    private final ColorSetting goldColor;
    private final ColorSetting redstoneColor;
    private final ColorSetting spawnerColor;
    private final List<BlockHit> hits = new ArrayList<BlockHit>();
    private BlockPos lastOrigin = BlockPos.ZERO;
    private int scanCooldown;

    public BlockEspModule() {
        super("Block ESP", "Highlights valuable ores and special blocks.", Category.VISUAL);
        this.diamond = register(new BooleanSetting("Diamond", true));
        this.emerald = register(new BooleanSetting("Emerald", false));
        this.ancientDebris = register(new BooleanSetting("Ancient Debris", true));
        this.gold = register(new BooleanSetting("Gold", false));
        this.redstone = register(new BooleanSetting("Redstone", false));
        this.spawners = register(new BooleanSetting("Spawners", true));
        this.range = register(new NumberSetting("Range", 18.0D, 6.0D, 36.0D, 1.0D));
        this.limit = register(new NumberSetting("Limit", 72.0D, 8.0D, 200.0D, 1.0D));
        this.fillAlpha = register(new NumberSetting("Fill Alpha", 0.09D, 0.02D, 0.35D, 0.01D));
        this.outlineAlpha = register(new NumberSetting("Line Alpha", 0.88D, 0.10D, 1.00D, 0.01D));
        this.diamondColor = register(new ColorSetting("Diamond Color", ColorUtil.rgba(92, 240, 255, 255)));
        this.emeraldColor = register(new ColorSetting("Emerald Color", ColorUtil.rgba(90, 255, 166, 255)));
        this.ancientDebrisColor = register(new ColorSetting("Debris Color", ColorUtil.rgba(232, 137, 96, 255)));
        this.goldColor = register(new ColorSetting("Gold Color", ColorUtil.rgba(255, 212, 92, 255)));
        this.redstoneColor = register(new ColorSetting("Redstone Color", ColorUtil.rgba(255, 96, 116, 255)));
        this.spawnerColor = register(new ColorSetting("Spawner Color", ColorUtil.rgba(178, 112, 255, 255)));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            this.hits.clear();
            return;
        }

        final BlockPos origin = minecraft.player.blockPosition();
        if (this.scanCooldown-- > 0 && origin.closerThan(this.lastOrigin, 2.0D)) {
            return;
        }

        this.lastOrigin = origin;
        this.scanCooldown = 15;
        scanNearby(minecraft, origin);
    }

    @Override
    public void onDisable() {
        this.hits.clear();
    }

    @Override
    public void onRender3D(final MatrixStack matrixStack, final float partialTicks) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        final net.minecraft.client.renderer.IRenderTypeBuffer.Impl buffer = minecraft.renderBuffers().bufferSource();
        for (final BlockHit hit : this.hits) {
            WorldRenderUtil.drawBufferedBox(
                matrixStack,
                buffer,
                new AxisAlignedBB(hit.pos),
                ColorUtil.withAlpha(hit.color, toAlpha(this.fillAlpha.getValue().doubleValue())),
                ColorUtil.withAlpha(hit.color, toAlpha(this.outlineAlpha.getValue().doubleValue()))
            );
        }
        buffer.endBatch();
    }

    private void scanNearby(final Minecraft minecraft, final BlockPos origin) {
        this.hits.clear();
        final int radius = this.range.getValue().intValue();
        final int maxHits = this.limit.getValue().intValue();
        final double radiusSq = radius * radius;
        final BlockPos.Mutable mutable = new BlockPos.Mutable();
        final List<BlockHit> found = new ArrayList<BlockHit>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    mutable.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (origin.distSqr(mutable) > radiusSq) {
                        continue;
                    }
                    if (!minecraft.level.hasChunkAt(mutable)) {
                        continue;
                    }

                    final BlockState state = minecraft.level.getBlockState(mutable);
                    final int color = classify(state.getBlock());
                    if (color == 0) {
                        continue;
                    }

                    found.add(new BlockHit(mutable.immutable(), color, origin.distSqr(mutable)));
                }
            }
        }

        Collections.sort(found, Comparator.comparingDouble(BlockHit::getDistanceSq));
        this.hits.addAll(found.subList(0, Math.min(maxHits, found.size())));
    }

    private int classify(final Block block) {
        if (this.diamond.getValue().booleanValue() && block == Blocks.DIAMOND_ORE) {
            return this.diamondColor.getColor();
        }
        if (this.emerald.getValue().booleanValue() && block == Blocks.EMERALD_ORE) {
            return this.emeraldColor.getColor();
        }
        if (this.ancientDebris.getValue().booleanValue() && block == Blocks.ANCIENT_DEBRIS) {
            return this.ancientDebrisColor.getColor();
        }
        if (this.gold.getValue().booleanValue() && block == Blocks.GOLD_ORE) {
            return this.goldColor.getColor();
        }
        if (this.redstone.getValue().booleanValue() && block == Blocks.REDSTONE_ORE) {
            return this.redstoneColor.getColor();
        }
        if (this.spawners.getValue().booleanValue() && block == Blocks.SPAWNER) {
            return this.spawnerColor.getColor();
        }
        return 0;
    }

    private int toAlpha(final double normalized) {
        return (int) Math.round(normalized * 255.0D);
    }

    private static final class BlockHit {
        private final BlockPos pos;
        private final int color;
        private final double distanceSq;

        private BlockHit(final BlockPos pos, final int color, final double distanceSq) {
            this.pos = pos;
            this.color = color;
            this.distanceSq = distanceSq;
        }

        private double getDistanceSq() {
            return this.distanceSq;
        }
    }
}
