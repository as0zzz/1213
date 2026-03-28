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
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StorageEspModule extends Module {
    private final BooleanSetting chests;
    private final BooleanSetting enderChests;
    private final BooleanSetting shulkers;
    private final NumberSetting range;
    private final NumberSetting limit;
    private final NumberSetting fillAlpha;
    private final NumberSetting outlineAlpha;
    private final ColorSetting chestColor;
    private final ColorSetting enderChestColor;
    private final ColorSetting shulkerColor;
    private final List<StorageHit> hits = new ArrayList<StorageHit>();
    private BlockPos lastOrigin = BlockPos.ZERO;
    private int scanCooldown;

    public StorageEspModule() {
        super("Storage ESP", "Highlights nearby chests, ender chests and shulkers.", Category.VISUAL);
        this.chests = register(new BooleanSetting("Chests", true));
        this.enderChests = register(new BooleanSetting("Ender Chests", true));
        this.shulkers = register(new BooleanSetting("Shulkers", true));
        this.range = register(new NumberSetting("Range", 20.0D, 6.0D, 48.0D, 1.0D));
        this.limit = register(new NumberSetting("Limit", 64.0D, 8.0D, 160.0D, 1.0D));
        this.fillAlpha = register(new NumberSetting("Fill Alpha", 0.10D, 0.02D, 0.45D, 0.01D));
        this.outlineAlpha = register(new NumberSetting("Line Alpha", 0.88D, 0.10D, 1.00D, 0.01D));
        this.chestColor = register(new ColorSetting("Chest Color", ColorUtil.rgba(255, 188, 96, 255)));
        this.enderChestColor = register(new ColorSetting("Ender Chest Color", ColorUtil.rgba(176, 102, 255, 255)));
        this.shulkerColor = register(new ColorSetting("Shulker Color", ColorUtil.rgba(120, 232, 210, 255)));
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
        this.scanCooldown = 12;
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
        for (final StorageHit hit : this.hits) {
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
        final List<StorageHit> found = new ArrayList<StorageHit>();

        for (final TileEntity tileEntity : minecraft.level.blockEntityList) {
            if (tileEntity == null) {
                continue;
            }

            final BlockPos pos = tileEntity.getBlockPos();
            if (origin.distSqr(pos) > radiusSq) {
                continue;
            }

            final int color = classify(tileEntity, minecraft.level.getBlockState(pos));
            if (color == 0) {
                continue;
            }

            found.add(new StorageHit(pos.immutable(), color, origin.distSqr(pos)));
        }

        Collections.sort(found, Comparator.comparingDouble(StorageHit::getDistanceSq));
        this.hits.addAll(found.subList(0, Math.min(maxHits, found.size())));
    }

    private int classify(final TileEntity tileEntity, final BlockState state) {
        final Block block = state.getBlock();
        if (this.chests.getValue().booleanValue()
            && (tileEntity instanceof ChestTileEntity || block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST)) {
            return this.chestColor.getColor();
        }
        if (this.enderChests.getValue().booleanValue()
            && (tileEntity instanceof EnderChestTileEntity || block == Blocks.ENDER_CHEST)) {
            return this.enderChestColor.getColor();
        }
        if (this.shulkers.getValue().booleanValue()
            && (tileEntity instanceof ShulkerBoxTileEntity || block instanceof ShulkerBoxBlock)) {
            return this.shulkerColor.getColor();
        }
        return 0;
    }

    private int toAlpha(final double normalized) {
        return (int) Math.round(normalized * 255.0D);
    }

    private static final class StorageHit {
        private final BlockPos pos;
        private final int color;
        private final double distanceSq;

        private StorageHit(final BlockPos pos, final int color, final double distanceSq) {
            this.pos = pos;
            this.color = color;
            this.distanceSq = distanceSq;
        }

        private double getDistanceSq() {
            return this.distanceSq;
        }
    }
}
