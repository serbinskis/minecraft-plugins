package net.minecraft.world.item.component;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntity;
import org.slf4j.Logger;

public final class CustomData {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CustomData EMPTY = new CustomData(new NBTTagCompound());
    public static final Codec<CustomData> CODEC = Codec.withAlternative(NBTTagCompound.CODEC, MojangsonParser.AS_CODEC).xmap(CustomData::new, (customdata) -> {
        return customdata.tag;
    });
    public static final Codec<CustomData> CODEC_WITH_ID = CustomData.CODEC.validate((customdata) -> {
        return customdata.getUnsafe().contains("id", 8) ? DataResult.success(customdata) : DataResult.error(() -> {
            return "Missing id for entity in: " + String.valueOf(customdata);
        });
    });
    /** @deprecated */
    @Deprecated
    public static final StreamCodec<ByteBuf, CustomData> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(CustomData::new, (customdata) -> {
        return customdata.tag;
    });
    private final NBTTagCompound tag;

    private CustomData(NBTTagCompound nbttagcompound) {
        this.tag = nbttagcompound;
    }

    public static CustomData of(NBTTagCompound nbttagcompound) {
        return new CustomData(nbttagcompound.copy());
    }

    public static Predicate<ItemStack> itemMatcher(DataComponentType<CustomData> datacomponenttype, NBTTagCompound nbttagcompound) {
        return (itemstack) -> {
            CustomData customdata = (CustomData) itemstack.getOrDefault(datacomponenttype, CustomData.EMPTY);

            return customdata.matchedBy(nbttagcompound);
        };
    }

    public boolean matchedBy(NBTTagCompound nbttagcompound) {
        return GameProfileSerializer.compareNbt(nbttagcompound, this.tag, true);
    }

    public static void update(DataComponentType<CustomData> datacomponenttype, ItemStack itemstack, Consumer<NBTTagCompound> consumer) {
        CustomData customdata = ((CustomData) itemstack.getOrDefault(datacomponenttype, CustomData.EMPTY)).update(consumer);

        if (customdata.tag.isEmpty()) {
            itemstack.remove(datacomponenttype);
        } else {
            itemstack.set(datacomponenttype, customdata);
        }

    }

    public static void set(DataComponentType<CustomData> datacomponenttype, ItemStack itemstack, NBTTagCompound nbttagcompound) {
        if (!nbttagcompound.isEmpty()) {
            itemstack.set(datacomponenttype, of(nbttagcompound));
        } else {
            itemstack.remove(datacomponenttype);
        }

    }

    public CustomData update(Consumer<NBTTagCompound> consumer) {
        NBTTagCompound nbttagcompound = this.tag.copy();

        consumer.accept(nbttagcompound);
        return new CustomData(nbttagcompound);
    }

    public void loadInto(Entity entity) {
        NBTTagCompound nbttagcompound = entity.saveWithoutId(new NBTTagCompound());
        UUID uuid = entity.getUUID();

        nbttagcompound.merge(this.tag);
        entity.load(nbttagcompound);
        entity.setUUID(uuid);
    }

    public boolean loadInto(TileEntity tileentity, HolderLookup.a holderlookup_a) {
        NBTTagCompound nbttagcompound = tileentity.saveCustomOnly(holderlookup_a);
        NBTTagCompound nbttagcompound1 = nbttagcompound.copy();

        nbttagcompound.merge(this.tag);
        if (!nbttagcompound.equals(nbttagcompound1)) {
            try {
                tileentity.loadCustomOnly(nbttagcompound, holderlookup_a);
                tileentity.setChanged();
                return true;
            } catch (Exception exception) {
                CustomData.LOGGER.warn("Failed to apply custom data to block entity at {}", tileentity.getBlockPos(), exception);

                try {
                    tileentity.loadCustomOnly(nbttagcompound1, holderlookup_a);
                } catch (Exception exception1) {
                    CustomData.LOGGER.warn("Failed to rollback block entity at {} after failure", tileentity.getBlockPos(), exception1);
                }
            }
        }

        return false;
    }

    public <T> DataResult<CustomData> update(DynamicOps<NBTBase> dynamicops, MapEncoder<T> mapencoder, T t0) {
        return mapencoder.encode(t0, dynamicops, dynamicops.mapBuilder()).build(this.tag).map((nbtbase) -> {
            return new CustomData((NBTTagCompound) nbtbase);
        });
    }

    public <T> DataResult<T> read(MapDecoder<T> mapdecoder) {
        return this.read(DynamicOpsNBT.INSTANCE, mapdecoder);
    }

    public <T> DataResult<T> read(DynamicOps<NBTBase> dynamicops, MapDecoder<T> mapdecoder) {
        MapLike<NBTBase> maplike = (MapLike) dynamicops.getMap(this.tag).getOrThrow();

        return mapdecoder.decode(dynamicops, maplike);
    }

    public int size() {
        return this.tag.size();
    }

    public boolean isEmpty() {
        return this.tag.isEmpty();
    }

    public NBTTagCompound copyTag() {
        return this.tag.copy();
    }

    public boolean contains(String s) {
        return this.tag.contains(s);
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else if (object instanceof CustomData) {
            CustomData customdata = (CustomData) object;

            return this.tag.equals(customdata.tag);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.tag.hashCode();
    }

    public String toString() {
        return this.tag.toString();
    }

    /** @deprecated */
    @Deprecated
    public NBTTagCompound getUnsafe() {
        return this.tag;
    }
}
