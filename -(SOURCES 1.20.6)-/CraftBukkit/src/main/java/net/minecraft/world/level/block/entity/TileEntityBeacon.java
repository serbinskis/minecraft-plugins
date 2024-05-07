package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.ChestLock;
import net.minecraft.world.INamableTileEntity;
import net.minecraft.world.ITileInventory;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerBeacon;
import net.minecraft.world.inventory.IContainerProperties;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IBeaconBeam;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.phys.AxisAlignedBB;

// CraftBukkit start
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.potion.PotionEffect;
// CraftBukkit end

public class TileEntityBeacon extends TileEntity implements ITileInventory, INamableTileEntity {

    private static final int MAX_LEVELS = 4;
    public static final List<List<Holder<MobEffectList>>> BEACON_EFFECTS = List.of(List.of(MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED), List.of(MobEffects.DAMAGE_RESISTANCE, MobEffects.JUMP), List.of(MobEffects.DAMAGE_BOOST), List.of(MobEffects.REGENERATION));
    private static final Set<Holder<MobEffectList>> VALID_EFFECTS = (Set) TileEntityBeacon.BEACON_EFFECTS.stream().flatMap(Collection::stream).collect(Collectors.toSet());
    public static final int DATA_LEVELS = 0;
    public static final int DATA_PRIMARY = 1;
    public static final int DATA_SECONDARY = 2;
    public static final int NUM_DATA_VALUES = 3;
    private static final int BLOCKS_CHECK_PER_TICK = 10;
    private static final IChatBaseComponent DEFAULT_NAME = IChatBaseComponent.translatable("container.beacon");
    private static final String TAG_PRIMARY = "primary_effect";
    private static final String TAG_SECONDARY = "secondary_effect";
    List<TileEntityBeacon.BeaconColorTracker> beamSections = Lists.newArrayList();
    private List<TileEntityBeacon.BeaconColorTracker> checkingBeamSections = Lists.newArrayList();
    public int levels;
    private int lastCheckY;
    @Nullable
    public Holder<MobEffectList> primaryPower;
    @Nullable
    public Holder<MobEffectList> secondaryPower;
    @Nullable
    public IChatBaseComponent name;
    public ChestLock lockKey;
    private final IContainerProperties dataAccess;
    // CraftBukkit start - add fields and methods
    public PotionEffect getPrimaryEffect() {
        return (this.primaryPower != null) ? CraftPotionUtil.toBukkit(new MobEffect(this.primaryPower, getLevel(this.levels), getAmplification(levels, primaryPower, secondaryPower), true, true)) : null;
    }

    public PotionEffect getSecondaryEffect() {
        return (hasSecondaryEffect(levels, primaryPower, secondaryPower)) ? CraftPotionUtil.toBukkit(new MobEffect(this.secondaryPower, getLevel(this.levels), getAmplification(levels, primaryPower, secondaryPower), true, true)) : null;
    }
    // CraftBukkit end

    @Nullable
    static Holder<MobEffectList> filterEffect(@Nullable Holder<MobEffectList> holder) {
        return TileEntityBeacon.VALID_EFFECTS.contains(holder) ? holder : null;
    }

    public TileEntityBeacon(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.BEACON, blockposition, iblockdata);
        this.lockKey = ChestLock.NO_LOCK;
        this.dataAccess = new IContainerProperties() {
            @Override
            public int get(int i) {
                int j;

                switch (i) {
                    case 0:
                        j = TileEntityBeacon.this.levels;
                        break;
                    case 1:
                        j = ContainerBeacon.encodeEffect(TileEntityBeacon.this.primaryPower);
                        break;
                    case 2:
                        j = ContainerBeacon.encodeEffect(TileEntityBeacon.this.secondaryPower);
                        break;
                    default:
                        j = 0;
                }

                return j;
            }

            @Override
            public void set(int i, int j) {
                switch (i) {
                    case 0:
                        TileEntityBeacon.this.levels = j;
                        break;
                    case 1:
                        if (!TileEntityBeacon.this.level.isClientSide && !TileEntityBeacon.this.beamSections.isEmpty()) {
                            TileEntityBeacon.playSound(TileEntityBeacon.this.level, TileEntityBeacon.this.worldPosition, SoundEffects.BEACON_POWER_SELECT);
                        }

                        TileEntityBeacon.this.primaryPower = TileEntityBeacon.filterEffect(ContainerBeacon.decodeEffect(j));
                        break;
                    case 2:
                        TileEntityBeacon.this.secondaryPower = TileEntityBeacon.filterEffect(ContainerBeacon.decodeEffect(j));
                }

            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    public static void tick(World world, BlockPosition blockposition, IBlockData iblockdata, TileEntityBeacon tileentitybeacon) {
        int i = blockposition.getX();
        int j = blockposition.getY();
        int k = blockposition.getZ();
        BlockPosition blockposition1;

        if (tileentitybeacon.lastCheckY < j) {
            blockposition1 = blockposition;
            tileentitybeacon.checkingBeamSections = Lists.newArrayList();
            tileentitybeacon.lastCheckY = blockposition.getY() - 1;
        } else {
            blockposition1 = new BlockPosition(i, tileentitybeacon.lastCheckY + 1, k);
        }

        TileEntityBeacon.BeaconColorTracker tileentitybeacon_beaconcolortracker = tileentitybeacon.checkingBeamSections.isEmpty() ? null : (TileEntityBeacon.BeaconColorTracker) tileentitybeacon.checkingBeamSections.get(tileentitybeacon.checkingBeamSections.size() - 1);
        int l = world.getHeight(HeightMap.Type.WORLD_SURFACE, i, k);

        int i1;

        for (i1 = 0; i1 < 10 && blockposition1.getY() <= l; ++i1) {
            IBlockData iblockdata1 = world.getBlockState(blockposition1);
            Block block = iblockdata1.getBlock();

            if (block instanceof IBeaconBeam) {
                float[] afloat = ((IBeaconBeam) block).getColor().getTextureDiffuseColors();

                if (tileentitybeacon.checkingBeamSections.size() <= 1) {
                    tileentitybeacon_beaconcolortracker = new TileEntityBeacon.BeaconColorTracker(afloat);
                    tileentitybeacon.checkingBeamSections.add(tileentitybeacon_beaconcolortracker);
                } else if (tileentitybeacon_beaconcolortracker != null) {
                    if (Arrays.equals(afloat, tileentitybeacon_beaconcolortracker.color)) {
                        tileentitybeacon_beaconcolortracker.increaseHeight();
                    } else {
                        tileentitybeacon_beaconcolortracker = new TileEntityBeacon.BeaconColorTracker(new float[]{(tileentitybeacon_beaconcolortracker.color[0] + afloat[0]) / 2.0F, (tileentitybeacon_beaconcolortracker.color[1] + afloat[1]) / 2.0F, (tileentitybeacon_beaconcolortracker.color[2] + afloat[2]) / 2.0F});
                        tileentitybeacon.checkingBeamSections.add(tileentitybeacon_beaconcolortracker);
                    }
                }
            } else {
                if (tileentitybeacon_beaconcolortracker == null || iblockdata1.getLightBlock(world, blockposition1) >= 15 && !iblockdata1.is(Blocks.BEDROCK)) {
                    tileentitybeacon.checkingBeamSections.clear();
                    tileentitybeacon.lastCheckY = l;
                    break;
                }

                tileentitybeacon_beaconcolortracker.increaseHeight();
            }

            blockposition1 = blockposition1.above();
            ++tileentitybeacon.lastCheckY;
        }

        i1 = tileentitybeacon.levels;
        if (world.getGameTime() % 80L == 0L) {
            if (!tileentitybeacon.beamSections.isEmpty()) {
                tileentitybeacon.levels = updateBase(world, i, j, k);
            }

            if (tileentitybeacon.levels > 0 && !tileentitybeacon.beamSections.isEmpty()) {
                applyEffects(world, blockposition, tileentitybeacon.levels, tileentitybeacon.primaryPower, tileentitybeacon.secondaryPower);
                playSound(world, blockposition, SoundEffects.BEACON_AMBIENT);
            }
        }

        if (tileentitybeacon.lastCheckY >= l) {
            tileentitybeacon.lastCheckY = world.getMinBuildHeight() - 1;
            boolean flag = i1 > 0;

            tileentitybeacon.beamSections = tileentitybeacon.checkingBeamSections;
            if (!world.isClientSide) {
                boolean flag1 = tileentitybeacon.levels > 0;

                if (!flag && flag1) {
                    playSound(world, blockposition, SoundEffects.BEACON_ACTIVATE);
                    Iterator iterator = world.getEntitiesOfClass(EntityPlayer.class, (new AxisAlignedBB((double) i, (double) j, (double) k, (double) i, (double) (j - 4), (double) k)).inflate(10.0D, 5.0D, 10.0D)).iterator();

                    while (iterator.hasNext()) {
                        EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                        CriterionTriggers.CONSTRUCT_BEACON.trigger(entityplayer, tileentitybeacon.levels);
                    }
                } else if (flag && !flag1) {
                    playSound(world, blockposition, SoundEffects.BEACON_DEACTIVATE);
                }
            }
        }

    }

    private static int updateBase(World world, int i, int j, int k) {
        int l = 0;

        for (int i1 = 1; i1 <= 4; l = i1++) {
            int j1 = j - i1;

            if (j1 < world.getMinBuildHeight()) {
                break;
            }

            boolean flag = true;

            for (int k1 = i - i1; k1 <= i + i1 && flag; ++k1) {
                for (int l1 = k - i1; l1 <= k + i1; ++l1) {
                    if (!world.getBlockState(new BlockPosition(k1, j1, l1)).is(TagsBlock.BEACON_BASE_BLOCKS)) {
                        flag = false;
                        break;
                    }
                }
            }

            if (!flag) {
                break;
            }
        }

        return l;
    }

    @Override
    public void setRemoved() {
        playSound(this.level, this.worldPosition, SoundEffects.BEACON_DEACTIVATE);
        super.setRemoved();
    }

    // CraftBukkit start - split into components
    private static byte getAmplification(int i, @Nullable Holder<MobEffectList> holder, @Nullable Holder<MobEffectList> holder1) {
        {
            byte b0 = 0;

            if (i >= 4 && Objects.equals(holder, holder1)) {
                b0 = 1;
            }

            return b0;
        }
    }

    private static int getLevel(int i) {
        {
            int j = (9 + i * 2) * 20;
            return j;
        }
    }

    public static List getHumansInRange(World world, BlockPosition blockposition, int i) {
        {
            double d0 = (double) (i * 10 + 10);

            AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockposition)).inflate(d0).expandTowards(0.0D, (double) world.getHeight(), 0.0D);
            List<EntityHuman> list = world.getEntitiesOfClass(EntityHuman.class, axisalignedbb);

            return list;
        }
    }

    private static void applyEffect(List list, @Nullable Holder<MobEffectList> holder, int j, int b0) {
        {
            Iterator iterator = list.iterator();

            EntityHuman entityhuman;

            while (iterator.hasNext()) {
                entityhuman = (EntityHuman) iterator.next();
                entityhuman.addEffect(new MobEffect(holder, j, b0, true, true), org.bukkit.event.entity.EntityPotionEffectEvent.Cause.BEACON);
            }
        }
    }

    private static boolean hasSecondaryEffect(int i, @Nullable Holder<MobEffectList> holder, @Nullable Holder<MobEffectList> holder1) {
        {
            if (i >= 4 && !Objects.equals(holder, holder1) && holder1 != null) {
                return true;
            }

            return false;
        }
    }

    private static void applyEffects(World world, BlockPosition blockposition, int i, @Nullable Holder<MobEffectList> holder, @Nullable Holder<MobEffectList> holder1) {
        if (!world.isClientSide && holder != null) {
            double d0 = (double) (i * 10 + 10);
            byte b0 = getAmplification(i, holder, holder1);

            int j = getLevel(i);
            List list = getHumansInRange(world, blockposition, i);

            applyEffect(list, holder, j, b0);

            if (hasSecondaryEffect(i, holder, holder1)) {
                applyEffect(list, holder1, j, 0);
            }
        }

    }
    // CraftBukkit end

    public static void playSound(World world, BlockPosition blockposition, SoundEffect soundeffect) {
        world.playSound((EntityHuman) null, blockposition, soundeffect, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public List<TileEntityBeacon.BeaconColorTracker> getBeamSections() {
        return (List) (this.levels == 0 ? ImmutableList.of() : this.beamSections);
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return this.saveCustomOnly(holderlookup_a);
    }

    private static void storeEffect(NBTTagCompound nbttagcompound, String s, @Nullable Holder<MobEffectList> holder) {
        if (holder != null) {
            holder.unwrapKey().ifPresent((resourcekey) -> {
                nbttagcompound.putString(s, resourcekey.location().toString());
            });
        }

    }

    @Nullable
    private static Holder<MobEffectList> loadEffect(NBTTagCompound nbttagcompound, String s) {
        if (nbttagcompound.contains(s, 8)) {
            MinecraftKey minecraftkey = MinecraftKey.tryParse(nbttagcompound.getString(s));

            return minecraftkey == null ? null : (Holder) BuiltInRegistries.MOB_EFFECT.getHolder(minecraftkey).orElse(null); // CraftBukkit - persist manually set non-default beacon effects (SPIGOT-3598)
        } else {
            return null;
        }
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        this.primaryPower = loadEffect(nbttagcompound, "primary_effect");
        this.secondaryPower = loadEffect(nbttagcompound, "secondary_effect");
        this.levels = nbttagcompound.getInt("Levels"); // CraftBukkit - SPIGOT-5053, use where available
        if (nbttagcompound.contains("CustomName", 8)) {
            this.name = parseCustomNameSafe(nbttagcompound.getString("CustomName"), holderlookup_a);
        }

        this.lockKey = ChestLock.fromTag(nbttagcompound);
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        storeEffect(nbttagcompound, "primary_effect", this.primaryPower);
        storeEffect(nbttagcompound, "secondary_effect", this.secondaryPower);
        nbttagcompound.putInt("Levels", this.levels);
        if (this.name != null) {
            nbttagcompound.putString("CustomName", IChatBaseComponent.ChatSerializer.toJson(this.name, holderlookup_a));
        }

        this.lockKey.addToTag(nbttagcompound);
    }

    public void setCustomName(@Nullable IChatBaseComponent ichatbasecomponent) {
        this.name = ichatbasecomponent;
    }

    @Nullable
    @Override
    public IChatBaseComponent getCustomName() {
        return this.name;
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerinventory, EntityHuman entityhuman) {
        return TileEntityContainer.canUnlock(entityhuman, this.lockKey, this.getDisplayName()) ? new ContainerBeacon(i, playerinventory, this.dataAccess, ContainerAccess.create(this.level, this.getBlockPos())) : null;
    }

    @Override
    public IChatBaseComponent getDisplayName() {
        return this.getName();
    }

    @Override
    public IChatBaseComponent getName() {
        return this.name != null ? this.name : TileEntityBeacon.DEFAULT_NAME;
    }

    @Override
    protected void applyImplicitComponents(TileEntity.b tileentity_b) {
        super.applyImplicitComponents(tileentity_b);
        this.name = (IChatBaseComponent) tileentity_b.get(DataComponents.CUSTOM_NAME);
        this.lockKey = (ChestLock) tileentity_b.getOrDefault(DataComponents.LOCK, ChestLock.NO_LOCK);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.a datacomponentmap_a) {
        super.collectImplicitComponents(datacomponentmap_a);
        datacomponentmap_a.set(DataComponents.CUSTOM_NAME, this.name);
        if (!this.lockKey.equals(ChestLock.NO_LOCK)) {
            datacomponentmap_a.set(DataComponents.LOCK, this.lockKey);
        }

    }

    @Override
    public void removeComponentsFromTag(NBTTagCompound nbttagcompound) {
        nbttagcompound.remove("CustomName");
        nbttagcompound.remove("Lock");
    }

    @Override
    public void setLevel(World world) {
        super.setLevel(world);
        this.lastCheckY = world.getMinBuildHeight() - 1;
    }

    public static class BeaconColorTracker {

        final float[] color;
        private int height;

        public BeaconColorTracker(float[] afloat) {
            this.color = afloat;
            this.height = 1;
        }

        protected void increaseHeight() {
            ++this.height;
        }

        public float[] getColor() {
            return this.color;
        }

        public int getHeight() {
            return this.height;
        }
    }
}
