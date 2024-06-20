package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import org.slf4j.Logger;

// CraftBukkit start
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.inventory.InventoryHolder;
// CraftBukkit end

public abstract class TileEntity {

    // CraftBukkit start - data containers
    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();
    public CraftPersistentDataContainer persistentDataContainer;
    // CraftBukkit end
    private static final Logger LOGGER = LogUtils.getLogger();
    private final TileEntityTypes<?> type;
    @Nullable
    protected World level;
    protected final BlockPosition worldPosition;
    protected boolean remove;
    private IBlockData blockState;
    private DataComponentMap components;

    public TileEntity(TileEntityTypes<?> tileentitytypes, BlockPosition blockposition, IBlockData iblockdata) {
        this.components = DataComponentMap.EMPTY;
        this.type = tileentitytypes;
        this.worldPosition = blockposition.immutable();
        this.blockState = iblockdata;
    }

    public static BlockPosition getPosFromTag(NBTTagCompound nbttagcompound) {
        return new BlockPosition(nbttagcompound.getInt("x"), nbttagcompound.getInt("y"), nbttagcompound.getInt("z"));
    }

    @Nullable
    public World getLevel() {
        return this.level;
    }

    public void setLevel(World world) {
        this.level = world;
    }

    public boolean hasLevel() {
        return this.level != null;
    }

    // CraftBukkit start - read container
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        this.persistentDataContainer = new CraftPersistentDataContainer(DATA_TYPE_REGISTRY);

        net.minecraft.nbt.NBTBase persistentDataTag = nbttagcompound.get("PublicBukkitValues");
        if (persistentDataTag instanceof NBTTagCompound) {
            this.persistentDataContainer.putAll((NBTTagCompound) persistentDataTag);
        }
    }
    // CraftBukkit end

    public final void loadWithComponents(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        this.loadAdditional(nbttagcompound, holderlookup_a);
        TileEntity.a.COMPONENTS_CODEC.parse(holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), nbttagcompound).resultOrPartial((s) -> {
            TileEntity.LOGGER.warn("Failed to load components: {}", s);
        }).ifPresent((datacomponentmap) -> {
            this.components = datacomponentmap;
        });
    }

    public final void loadCustomOnly(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        this.loadAdditional(nbttagcompound, holderlookup_a);
    }

    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {}

    public final NBTTagCompound saveWithFullMetadata(HolderLookup.a holderlookup_a) {
        NBTTagCompound nbttagcompound = this.saveWithoutMetadata(holderlookup_a);

        this.saveMetadata(nbttagcompound);
        return nbttagcompound;
    }

    public final NBTTagCompound saveWithId(HolderLookup.a holderlookup_a) {
        NBTTagCompound nbttagcompound = this.saveWithoutMetadata(holderlookup_a);

        this.saveId(nbttagcompound);
        return nbttagcompound;
    }

    public final NBTTagCompound saveWithoutMetadata(HolderLookup.a holderlookup_a) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        this.saveAdditional(nbttagcompound, holderlookup_a);
        TileEntity.a.COMPONENTS_CODEC.encodeStart(holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), this.components).resultOrPartial((s) -> {
            TileEntity.LOGGER.warn("Failed to save components: {}", s);
        }).ifPresent((nbtbase) -> {
            nbttagcompound.merge((NBTTagCompound) nbtbase);
        });
        // CraftBukkit start - store container
        if (this.persistentDataContainer != null && !this.persistentDataContainer.isEmpty()) {
            nbttagcompound.put("PublicBukkitValues", this.persistentDataContainer.toTagCompound());
        }
        // CraftBukkit end
        return nbttagcompound;
    }

    public final NBTTagCompound saveCustomOnly(HolderLookup.a holderlookup_a) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        this.saveAdditional(nbttagcompound, holderlookup_a);
        return nbttagcompound;
    }

    public final NBTTagCompound saveCustomAndMetadata(HolderLookup.a holderlookup_a) {
        NBTTagCompound nbttagcompound = this.saveCustomOnly(holderlookup_a);

        this.saveMetadata(nbttagcompound);
        return nbttagcompound;
    }

    private void saveId(NBTTagCompound nbttagcompound) {
        MinecraftKey minecraftkey = TileEntityTypes.getKey(this.getType());

        if (minecraftkey == null) {
            throw new RuntimeException(String.valueOf(this.getClass()) + " is missing a mapping! This is a bug!");
        } else {
            nbttagcompound.putString("id", minecraftkey.toString());
        }
    }

    public static void addEntityType(NBTTagCompound nbttagcompound, TileEntityTypes<?> tileentitytypes) {
        nbttagcompound.putString("id", TileEntityTypes.getKey(tileentitytypes).toString());
    }

    public void saveToItem(ItemStack itemstack, HolderLookup.a holderlookup_a) {
        NBTTagCompound nbttagcompound = this.saveCustomOnly(holderlookup_a);

        this.removeComponentsFromTag(nbttagcompound);
        ItemBlock.setBlockEntityData(itemstack, this.getType(), nbttagcompound);
        itemstack.applyComponents(this.collectComponents());
    }

    private void saveMetadata(NBTTagCompound nbttagcompound) {
        this.saveId(nbttagcompound);
        nbttagcompound.putInt("x", this.worldPosition.getX());
        nbttagcompound.putInt("y", this.worldPosition.getY());
        nbttagcompound.putInt("z", this.worldPosition.getZ());
    }

    @Nullable
    public static TileEntity loadStatic(BlockPosition blockposition, IBlockData iblockdata, NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        String s = nbttagcompound.getString("id");
        MinecraftKey minecraftkey = MinecraftKey.tryParse(s);

        if (minecraftkey == null) {
            TileEntity.LOGGER.error("Block entity has invalid type: {}", s);
            return null;
        } else {
            return (TileEntity) BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(minecraftkey).map((tileentitytypes) -> {
                try {
                    return tileentitytypes.create(blockposition, iblockdata);
                } catch (Throwable throwable) {
                    TileEntity.LOGGER.error("Failed to create block entity {}", s, throwable);
                    return null;
                }
            }).map((tileentity) -> {
                try {
                    tileentity.loadWithComponents(nbttagcompound, holderlookup_a);
                    return tileentity;
                } catch (Throwable throwable) {
                    TileEntity.LOGGER.error("Failed to load data for block entity {}", s, throwable);
                    return null;
                }
            }).orElseGet(() -> {
                TileEntity.LOGGER.warn("Skipping BlockEntity with id {}", s);
                return null;
            });
        }
    }

    public void setChanged() {
        if (this.level != null) {
            setChanged(this.level, this.worldPosition, this.blockState);
        }

    }

    protected static void setChanged(World world, BlockPosition blockposition, IBlockData iblockdata) {
        world.blockEntityChanged(blockposition);
        if (!iblockdata.isAir()) {
            world.updateNeighbourForOutputSignal(blockposition, iblockdata.getBlock());
        }

    }

    public BlockPosition getBlockPos() {
        return this.worldPosition;
    }

    public IBlockData getBlockState() {
        return this.blockState;
    }

    @Nullable
    public Packet<PacketListenerPlayOut> getUpdatePacket() {
        return null;
    }

    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return new NBTTagCompound();
    }

    public boolean isRemoved() {
        return this.remove;
    }

    public void setRemoved() {
        this.remove = true;
    }

    public void clearRemoved() {
        this.remove = false;
    }

    public boolean triggerEvent(int i, int j) {
        return false;
    }

    public void fillCrashReportCategory(CrashReportSystemDetails crashreportsystemdetails) {
        crashreportsystemdetails.setDetail("Name", () -> {
            String s = String.valueOf(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()));

            return s + " // " + this.getClass().getCanonicalName();
        });
        if (this.level != null) {
            CrashReportSystemDetails.populateBlockDetails(crashreportsystemdetails, this.level, this.worldPosition, this.getBlockState());
            CrashReportSystemDetails.populateBlockDetails(crashreportsystemdetails, this.level, this.worldPosition, this.level.getBlockState(this.worldPosition));
        }
    }

    public boolean onlyOpCanSetNbt() {
        return false;
    }

    public TileEntityTypes<?> getType() {
        return this.type;
    }

    /** @deprecated */
    @Deprecated
    public void setBlockState(IBlockData iblockdata) {
        this.blockState = iblockdata;
    }

    protected void applyImplicitComponents(TileEntity.b tileentity_b) {}

    public final void applyComponentsFromItemStack(ItemStack itemstack) {
        this.applyComponents(itemstack.getPrototype(), itemstack.getComponentsPatch());
    }

    public final void applyComponents(DataComponentMap datacomponentmap, DataComponentPatch datacomponentpatch) {
        // CraftBukkit start
        this.applyComponentsSet(datacomponentmap, datacomponentpatch);
    }

    public final Set<DataComponentType<?>> applyComponentsSet(DataComponentMap datacomponentmap, DataComponentPatch datacomponentpatch) {
        // CraftBukkit end
        final Set<DataComponentType<?>> set = new HashSet();

        set.add(DataComponents.BLOCK_ENTITY_DATA);
        final PatchedDataComponentMap patcheddatacomponentmap = PatchedDataComponentMap.fromPatch(datacomponentmap, datacomponentpatch);

        this.applyImplicitComponents(new TileEntity.b() { // CraftBukkit - decompile error
            @Nullable
            @Override
            public <T> T get(DataComponentType<T> datacomponenttype) {
                set.add(datacomponenttype);
                return patcheddatacomponentmap.get(datacomponenttype);
            }

            @Override
            public <T> T getOrDefault(DataComponentType<? extends T> datacomponenttype, T t0) {
                set.add(datacomponenttype);
                return patcheddatacomponentmap.getOrDefault(datacomponenttype, t0);
            }
        });
        Objects.requireNonNull(set);
        DataComponentPatch datacomponentpatch1 = datacomponentpatch.forget(set::contains);

        this.components = datacomponentpatch1.split().added();
        // CraftBukkit start
        set.remove(DataComponents.BLOCK_ENTITY_DATA); // Remove as never actually added by applyImplicitComponents
        return set;
        // CraftBukkit end
    }

    protected void collectImplicitComponents(DataComponentMap.a datacomponentmap_a) {}

    /** @deprecated */
    @Deprecated
    public void removeComponentsFromTag(NBTTagCompound nbttagcompound) {}

    public final DataComponentMap collectComponents() {
        DataComponentMap.a datacomponentmap_a = DataComponentMap.builder();

        datacomponentmap_a.addAll(this.components);
        this.collectImplicitComponents(datacomponentmap_a);
        return datacomponentmap_a.build();
    }

    public DataComponentMap components() {
        return this.components;
    }

    public void setComponents(DataComponentMap datacomponentmap) {
        this.components = datacomponentmap;
    }

    @Nullable
    public static IChatBaseComponent parseCustomNameSafe(String s, HolderLookup.a holderlookup_a) {
        try {
            return IChatBaseComponent.ChatSerializer.fromJson(s, holderlookup_a);
        } catch (Exception exception) {
            TileEntity.LOGGER.warn("Failed to parse custom name from string '{}', discarding", s, exception);
            return null;
        }
    }

    // CraftBukkit start - add method
    public InventoryHolder getOwner() {
        if (level == null) return null;
        org.bukkit.block.BlockState state = level.getWorld().getBlockAt(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()).getState();
        if (state instanceof InventoryHolder) return (InventoryHolder) state;
        return null;
    }
    // CraftBukkit end

    private static class a {

        public static final Codec<DataComponentMap> COMPONENTS_CODEC = DataComponentMap.CODEC.optionalFieldOf("components", DataComponentMap.EMPTY).codec();

        private a() {}
    }

    protected interface b {

        @Nullable
        <T> T get(DataComponentType<T> datacomponenttype);

        <T> T getOrDefault(DataComponentType<? extends T> datacomponenttype, T t0);
    }
}
