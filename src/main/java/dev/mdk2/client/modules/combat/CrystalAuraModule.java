package dev.mdk2.client.modules.combat;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.CombatUtil;
import dev.mdk2.client.util.combat.InventoryUtil;
import dev.mdk2.client.util.combat.RotationUtil;
import dev.mdk2.client.util.combat.TimeUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class CrystalAuraModule extends Module {
    private final ModeSetting mode;
    private final BooleanSetting players;
    private final BooleanSetting mobs;
    private final BooleanSetting animals;
    private final BooleanSetting place;
    private final BooleanSetting breakCrystals;
    private final BooleanSetting rotate;
    private final BooleanSetting autoSwitch;
    private final NumberSetting targetRange;
    private final NumberSetting placeRange;
    private final NumberSetting breakRange;
    private final NumberSetting minDamage;
    private final NumberSetting maxSelfDamage;
    private final NumberSetting facePlace;
    private final NumberSetting delay;
    private LivingEntity currentTarget;
    private long lastActionTime;

    public CrystalAuraModule() {
        super("Crystal Aura", "Automates crystals and respawn anchors around targets.", Category.COMBAT);
        this.mode = register(new ModeSetting("Mode", "CrystalAura", "CrystalAura", "AnchorAura"));
        this.players = register(new BooleanSetting("Players", true));
        this.mobs = register(new BooleanSetting("Mobs", true));
        this.animals = register(new BooleanSetting("Animals", false));
        this.place = register(new BooleanSetting("Place", true));
        this.breakCrystals = register(new BooleanSetting("Break", true));
        this.rotate = register(new BooleanSetting("Rotate", true));
        this.autoSwitch = register(new BooleanSetting("Auto Switch", true));
        this.targetRange = register(new NumberSetting("Target Range", 8.0D, 2.0D, 16.0D, 0.1D));
        this.placeRange = register(new NumberSetting("Place Range", 5.0D, 1.0D, 8.0D, 0.1D));
        this.breakRange = register(new NumberSetting("Break Range", 5.0D, 1.0D, 8.0D, 0.1D));
        this.minDamage = register(new NumberSetting("Min Damage", 4.0D, 0.0D, 20.0D, 0.5D));
        this.maxSelfDamage = register(new NumberSetting("Max Self", 8.0D, 0.0D, 20.0D, 0.5D));
        this.facePlace = register(new NumberSetting("Face Place", 8.0D, 0.0D, 20.0D, 0.5D));
        this.delay = register(new NumberSetting("Delay", 110.0D, 0.0D, 1000.0D, 5.0D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            this.currentTarget = null;
            return;
        }

        this.currentTarget = CombatUtil.findBestTarget(minecraft.player, this.targetRange.getValue().doubleValue(), this.targetRange.getValue().doubleValue() * 0.7D,
            360.0D, this.players.getValue().booleanValue(), this.mobs.getValue().booleanValue(), this.animals.getValue().booleanValue(), false, true, "Distance");
        if (this.currentTarget == null) {
            return;
        }

        if (this.breakCrystals.getValue().booleanValue()) {
            final EnderCrystalEntity crystal = findBestCrystal(minecraft);
            if (crystal != null) {
                if (this.rotate.getValue().booleanValue()) {
                    RotationUtil.face(minecraft.player, crystal, 180.0F, 180.0F, true);
                }
                CombatUtil.attack(crystal, true, true);
                this.lastActionTime = TimeUtil.now();
                return;
            }
        }

        if (!this.place.getValue().booleanValue() || !TimeUtil.passed(this.lastActionTime, this.delay.getValue().doubleValue())) {
            return;
        }

        if ("AnchorAura".equalsIgnoreCase(this.mode.getValue())) {
            tickAnchorAura(minecraft);
            return;
        }

        tickCrystalAura(minecraft);
    }

    private void tickCrystalAura(final Minecraft minecraft) {
        final BlockPos basePos = findCrystalBase(minecraft);
        if (basePos == null) {
            return;
        }

        int slot = findItemSlot(minecraft, Items.END_CRYSTAL);
        if (slot < 0 || !InventoryUtil.selectHotbarSlot(minecraft.player, slot)) {
            return;
        }

        if (this.rotate.getValue().booleanValue()) {
            RotationUtil.face(minecraft.player, new Vector3d(basePos.getX() + 0.5D, basePos.getY() + 1.0D, basePos.getZ() + 0.5D), 180.0F, 180.0F, true);
        }

        final ActionResultType result = CombatUtil.useItemOnBlock(basePos, Direction.UP, Hand.MAIN_HAND);
        if (result.consumesAction()) {
            this.lastActionTime = TimeUtil.now();
        }
    }

    private void tickAnchorAura(final Minecraft minecraft) {
        if (minecraft.level.dimensionType().respawnAnchorWorks()) {
            return;
        }

        final BlockPos anchorPos = findAnchorPos(minecraft);
        if (anchorPos == null) {
            return;
        }

        final BlockState state = minecraft.level.getBlockState(anchorPos);
        if (state.is(Blocks.RESPAWN_ANCHOR)) {
            final int charge = state.getValue(RespawnAnchorBlock.CHARGE);
            if (charge == 0) {
                final int glowstoneSlot = findItemSlot(minecraft, Items.GLOWSTONE);
                if (glowstoneSlot >= 0 && InventoryUtil.selectHotbarSlot(minecraft.player, glowstoneSlot)) {
                    final ActionResultType result = CombatUtil.useItemOnBlock(anchorPos, Direction.UP, Hand.MAIN_HAND);
                    if (result.consumesAction()) {
                        this.lastActionTime = TimeUtil.now();
                    }
                }
                return;
            }

            final int useSlot = findNonGlowstoneSlot(minecraft);
            if (useSlot >= 0 && InventoryUtil.selectHotbarSlot(minecraft.player, useSlot)) {
                final ActionResultType result = CombatUtil.useItemOnBlock(anchorPos, Direction.UP, Hand.MAIN_HAND);
                if (result.consumesAction()) {
                    this.lastActionTime = TimeUtil.now();
                }
            }
            return;
        }

        final int anchorSlot = findItemSlot(minecraft, Items.RESPAWN_ANCHOR);
        if (anchorSlot < 0 || !InventoryUtil.selectHotbarSlot(minecraft.player, anchorSlot)) {
            return;
        }

        final ActionResultType result = CombatUtil.useItemOnBlock(anchorPos.below(), Direction.UP, Hand.MAIN_HAND);
        if (result.consumesAction()) {
            this.lastActionTime = TimeUtil.now();
        }
    }

    private EnderCrystalEntity findBestCrystal(final Minecraft minecraft) {
        EnderCrystalEntity bestCrystal = null;
        double bestDamage = 0.0D;
        final double rangeSq = this.breakRange.getValue().doubleValue() * this.breakRange.getValue().doubleValue();
        for (final Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof EnderCrystalEntity) || !entity.isAlive() || minecraft.player.distanceToSqr(entity) > rangeSq) {
                continue;
            }

            final Vector3d source = entity.position().add(0.0D, 1.0D, 0.0D);
            final double targetDamage = CombatUtil.estimateExplosionDamage(this.currentTarget, source, 12.0D);
            final double selfDamage = CombatUtil.estimateExplosionDamage(minecraft.player, source, 12.0D);
            if (!passesDamageChecks(targetDamage, selfDamage)) {
                continue;
            }

            if (targetDamage > bestDamage) {
                bestDamage = targetDamage;
                bestCrystal = (EnderCrystalEntity) entity;
            }
        }
        return bestCrystal;
    }

    private BlockPos findCrystalBase(final Minecraft minecraft) {
        final int radius = (int) Math.ceil(this.placeRange.getValue().doubleValue());
        BlockPos bestPos = null;
        double bestDamage = 0.0D;
        final BlockPos center = minecraft.player.blockPosition();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    final BlockPos pos = center.offset(x, y, z);
                    if (!isValidCrystalBase(minecraft, pos)) {
                        continue;
                    }

                    final Vector3d source = new Vector3d(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
                    final double targetDamage = CombatUtil.estimateExplosionDamage(this.currentTarget, source, 12.0D);
                    final double selfDamage = CombatUtil.estimateExplosionDamage(minecraft.player, source, 12.0D);
                    if (!passesDamageChecks(targetDamage, selfDamage)) {
                        continue;
                    }

                    if (targetDamage > bestDamage || bestPos == null) {
                        bestDamage = targetDamage;
                        bestPos = pos;
                    }
                }
            }
        }
        return bestPos;
    }

    private BlockPos findAnchorPos(final Minecraft minecraft) {
        final int radius = (int) Math.ceil(this.placeRange.getValue().doubleValue());
        BlockPos bestPos = null;
        double bestDamage = 0.0D;
        final BlockPos center = minecraft.player.blockPosition();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    final BlockPos pos = center.offset(x, y, z);
                    if (!isValidAnchorPos(minecraft, pos)) {
                        continue;
                    }

                    final Vector3d source = new Vector3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                    final double targetDamage = CombatUtil.estimateExplosionDamage(this.currentTarget, source, 10.0D);
                    final double selfDamage = CombatUtil.estimateExplosionDamage(minecraft.player, source, 10.0D);
                    if (!passesDamageChecks(targetDamage, selfDamage)) {
                        continue;
                    }

                    if (targetDamage > bestDamage || bestPos == null) {
                        bestDamage = targetDamage;
                        bestPos = pos;
                    }
                }
            }
        }
        return bestPos;
    }

    private int findItemSlot(final Minecraft minecraft, final net.minecraft.item.Item item) {
        int slot = InventoryUtil.findHotbarSlot(minecraft.player, new java.util.function.Predicate<ItemStack>() {
            @Override
            public boolean test(final ItemStack stack) {
                return stack.getItem() == item;
            }
        });
        if (slot < 0 && this.autoSwitch.getValue().booleanValue()) {
            slot = InventoryUtil.ensureHotbarItem(minecraft.player, new java.util.function.Predicate<ItemStack>() {
                @Override
                public boolean test(final ItemStack stack) {
                    return stack.getItem() == item;
                }
            }, minecraft.player.inventory.selected);
        }
        return slot;
    }

    private int findNonGlowstoneSlot(final Minecraft minecraft) {
        if (minecraft.player.getMainHandItem().getItem() != Items.GLOWSTONE) {
            return minecraft.player.inventory.selected;
        }

        int slot = InventoryUtil.findHotbarSlot(minecraft.player, new java.util.function.Predicate<ItemStack>() {
            @Override
            public boolean test(final ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() != Items.GLOWSTONE;
            }
        });
        if (slot >= 0) {
            return slot;
        }

        return findItemSlot(minecraft, Items.RESPAWN_ANCHOR);
    }

    private boolean isValidCrystalBase(final Minecraft minecraft, final BlockPos pos) {
        if (minecraft.player.distanceToSqr(Vector3d.atCenterOf(pos)) > this.placeRange.getValue().doubleValue() * this.placeRange.getValue().doubleValue()) {
            return false;
        }

        final BlockState state = minecraft.level.getBlockState(pos);
        if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK)) {
            return false;
        }

        final BlockPos placePos = pos.above();
        if (!minecraft.level.isEmptyBlock(placePos) || !minecraft.level.isEmptyBlock(placePos.above())) {
            return false;
        }

        return minecraft.level.getEntities((Entity) null, new AxisAlignedBB(placePos.getX(), placePos.getY(), placePos.getZ(),
            placePos.getX() + 1.0D, placePos.getY() + 2.0D, placePos.getZ() + 1.0D)).isEmpty();
    }

    private boolean isValidAnchorPos(final Minecraft minecraft, final BlockPos pos) {
        if (minecraft.player.distanceToSqr(Vector3d.atCenterOf(pos)) > this.placeRange.getValue().doubleValue() * this.placeRange.getValue().doubleValue()) {
            return false;
        }

        final BlockState state = minecraft.level.getBlockState(pos);
        if (state.is(Blocks.RESPAWN_ANCHOR)) {
            return true;
        }

        return minecraft.level.isEmptyBlock(pos) && minecraft.level.getBlockState(pos.below()).getMaterial().isSolid();
    }

    private boolean passesDamageChecks(final double targetDamage, final double selfDamage) {
        if (selfDamage > this.maxSelfDamage.getValue().doubleValue()) {
            return false;
        }

        if (targetDamage >= this.minDamage.getValue().doubleValue()) {
            return true;
        }

        if (this.currentTarget == null) {
            return false;
        }

        final double health = this.currentTarget.getHealth() + this.currentTarget.getAbsorptionAmount();
        if (health <= this.facePlace.getValue().doubleValue()) {
            return true;
        }

        return targetDamage >= 1.0D && Minecraft.getInstance().player != null
            && Minecraft.getInstance().player.distanceToSqr(this.currentTarget) <= 16.0D;
    }
}
