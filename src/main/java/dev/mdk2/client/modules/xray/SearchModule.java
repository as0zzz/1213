package dev.mdk2.client.modules.xray;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.render.WorldRenderUtil;
import dev.mdk2.client.settings.ColorSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.ColorUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchModule extends Module {
    private final ModeSetting target;
    private final NumberSetting range;
    private final NumberSetting limit;
    private final NumberSetting fillAlpha;
    private final NumberSetting outlineAlpha;
    private final ColorSetting diamondColor;
    private final ColorSetting debrisColor;
    private final ColorSetting spawnerColor;
    private final ColorSetting beaconColor;
    private final ColorSetting chestColor;
    private final ColorSetting enderChestColor;
    private final ColorSetting shulkerColor;
    private final ColorSetting portalColor;
    private final List<BlockPos> matches = new ArrayList<BlockPos>();
    private BlockPos lastOrigin = BlockPos.ZERO;
    private String lastTarget = "";
    private int scanCooldown;

    public SearchModule() {
        super("Search", "Searches nearby chunks for selected blocks or portals.", Category.VISUAL);
        this.target = register(new ModeSetting("Target", "Diamond Ore", "Diamond Ore", "Ancient Debris", "Spawner", "Beacon", "Portal", "Chest", "Ender Chest", "Shulker"));
        this.range = register(new NumberSetting("Range", 18.0D, 6.0D, 36.0D, 1.0D));
        this.limit = register(new NumberSetting("Limit", 48.0D, 8.0D, 128.0D, 1.0D));
        this.fillAlpha = register(new NumberSetting("Fill Alpha", 0.11D, 0.02D, 0.45D, 0.01D));
        this.outlineAlpha = register(new NumberSetting("Line Alpha", 0.92D, 0.10D, 1.00D, 0.01D));
        this.diamondColor = register(new ColorSetting("Diamond Color", ColorUtil.rgba(94, 232, 255, 255)));
        this.debrisColor = register(new ColorSetting("Debris Color", ColorUtil.rgba(222, 132, 95, 255)));
        this.spawnerColor = register(new ColorSetting("Spawner Color", ColorUtil.rgba(255, 98, 140, 255)));
        this.beaconColor = register(new ColorSetting("Beacon Color", ColorUtil.rgba(120, 255, 190, 255)));
        this.chestColor = register(new ColorSetting("Chest Color", ColorUtil.rgba(255, 188, 96, 255)));
        this.enderChestColor = register(new ColorSetting("Ender Chest Color", ColorUtil.rgba(176, 102, 255, 255)));
        this.shulkerColor = register(new ColorSetting("Shulker Color", ColorUtil.rgba(120, 232, 210, 255)));
        this.portalColor = register(new ColorSetting("Portal Color", ColorUtil.rgba(180, 112, 255, 255)));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            this.matches.clear();
            return;
        }

        final BlockPos origin = minecraft.player.blockPosition();
        final String currentTarget = this.target.getValue();
        final boolean playerMoved = !origin.closerThan(this.lastOrigin, 2.0D);
        final boolean targetChanged = !currentTarget.equals(this.lastTarget);
        if (this.scanCooldown-- > 0 && !playerMoved && !targetChanged) {
            return;
        }

        this.lastOrigin = origin;
        this.lastTarget = currentTarget;
        this.scanCooldown = 14;
        scanNearby(minecraft, origin);
    }

    @Override
    public void onDisable() {
        this.matches.clear();
    }

    @Override
    public void onRender3D(final MatrixStack matrixStack, final float partialTicks) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        final int baseColor = colorForTarget();
        final net.minecraft.client.renderer.IRenderTypeBuffer.Impl buffer = minecraft.renderBuffers().bufferSource();
        for (final BlockPos pos : this.matches) {
            WorldRenderUtil.drawBufferedBox(
                matrixStack,
                buffer,
                new AxisAlignedBB(pos),
                ColorUtil.withAlpha(baseColor, toAlpha(this.fillAlpha.getValue().doubleValue())),
                ColorUtil.withAlpha(baseColor, toAlpha(this.outlineAlpha.getValue().doubleValue()))
            );
        }
        buffer.endBatch();
    }

    private void scanNearby(final Minecraft minecraft, final BlockPos origin) {
        this.matches.clear();
        final int radius = this.range.getValue().intValue();
        final int limitCount = this.limit.getValue().intValue();
        final double radiusSq = radius * radius;
        final BlockPos.Mutable mutable = new BlockPos.Mutable();
        final List<SearchHit> found = new ArrayList<SearchHit>();

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
                    if (!matchesTarget(state.getBlock())) {
                        continue;
                    }

                    found.add(new SearchHit(mutable.immutable(), origin.distSqr(mutable)));
                }
            }
        }

        Collections.sort(found, Comparator.comparingDouble(SearchHit::getDistanceSq));
        final int count = Math.min(limitCount, found.size());
        for (int i = 0; i < count; i++) {
            this.matches.add(found.get(i).pos);
        }
    }

    private boolean matchesTarget(final Block block) {
        final String mode = this.target.getValue();
        if ("Diamond Ore".equalsIgnoreCase(mode)) {
            return block == Blocks.DIAMOND_ORE;
        }
        if ("Ancient Debris".equalsIgnoreCase(mode)) {
            return block == Blocks.ANCIENT_DEBRIS;
        }
        if ("Spawner".equalsIgnoreCase(mode)) {
            return block == Blocks.SPAWNER;
        }
        if ("Beacon".equalsIgnoreCase(mode)) {
            return block == Blocks.BEACON;
        }
        if ("Portal".equalsIgnoreCase(mode)) {
            return block == Blocks.NETHER_PORTAL || block == Blocks.END_PORTAL || block == Blocks.END_PORTAL_FRAME || block == Blocks.END_GATEWAY;
        }
        if ("Chest".equalsIgnoreCase(mode)) {
            return block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST;
        }
        if ("Ender Chest".equalsIgnoreCase(mode)) {
            return block == Blocks.ENDER_CHEST;
        }
        if ("Shulker".equalsIgnoreCase(mode)) {
            return block instanceof ShulkerBoxBlock;
        }
        return false;
    }

    private int colorForTarget() {
        final String mode = this.target.getValue();
        if ("Diamond Ore".equalsIgnoreCase(mode)) {
            return this.diamondColor.getColor();
        }
        if ("Ancient Debris".equalsIgnoreCase(mode)) {
            return this.debrisColor.getColor();
        }
        if ("Spawner".equalsIgnoreCase(mode)) {
            return this.spawnerColor.getColor();
        }
        if ("Beacon".equalsIgnoreCase(mode)) {
            return this.beaconColor.getColor();
        }
        if ("Chest".equalsIgnoreCase(mode)) {
            return this.chestColor.getColor();
        }
        if ("Ender Chest".equalsIgnoreCase(mode)) {
            return this.enderChestColor.getColor();
        }
        if ("Shulker".equalsIgnoreCase(mode)) {
            return this.shulkerColor.getColor();
        }
        return this.portalColor.getColor();
    }

    private int toAlpha(final double normalized) {
        return (int) Math.round(normalized * 255.0D);
    }

    private static final class SearchHit {
        private final BlockPos pos;
        private final double distanceSq;

        private SearchHit(final BlockPos pos, final double distanceSq) {
            this.pos = pos;
            this.distanceSq = distanceSq;
        }

        private double getDistanceSq() {
            return this.distanceSq;
        }
    }
}
