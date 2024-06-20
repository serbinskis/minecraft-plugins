package net.minecraft.network.syncher;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Vector3f;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RegistryID;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import org.joml.Quaternionf;

public class DataWatcherRegistry {

    private static final RegistryID<DataWatcherSerializer<?>> SERIALIZERS = RegistryID.create(16);
    public static final DataWatcherSerializer<Byte> BYTE = DataWatcherSerializer.forValueType(ByteBufCodecs.BYTE);
    public static final DataWatcherSerializer<Integer> INT = DataWatcherSerializer.forValueType(ByteBufCodecs.VAR_INT);
    public static final DataWatcherSerializer<Long> LONG = DataWatcherSerializer.forValueType(ByteBufCodecs.VAR_LONG);
    public static final DataWatcherSerializer<Float> FLOAT = DataWatcherSerializer.forValueType(ByteBufCodecs.FLOAT);
    public static final DataWatcherSerializer<String> STRING = DataWatcherSerializer.forValueType(ByteBufCodecs.STRING_UTF8);
    public static final DataWatcherSerializer<IChatBaseComponent> COMPONENT = DataWatcherSerializer.forValueType(ComponentSerialization.TRUSTED_STREAM_CODEC);
    public static final DataWatcherSerializer<Optional<IChatBaseComponent>> OPTIONAL_COMPONENT = DataWatcherSerializer.forValueType(ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC);
    public static final DataWatcherSerializer<ItemStack> ITEM_STACK = new DataWatcherSerializer<ItemStack>() {
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ItemStack> codec() {
            return ItemStack.OPTIONAL_STREAM_CODEC;
        }

        public ItemStack copy(ItemStack itemstack) {
            return itemstack.copy();
        }
    };
    public static final DataWatcherSerializer<IBlockData> BLOCK_STATE = DataWatcherSerializer.forValueType(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY));
    private static final StreamCodec<ByteBuf, Optional<IBlockData>> OPTIONAL_BLOCK_STATE_CODEC = new StreamCodec<ByteBuf, Optional<IBlockData>>() {
        public void encode(ByteBuf bytebuf, Optional<IBlockData> optional) {
            if (optional.isPresent()) {
                VarInt.write(bytebuf, Block.getId((IBlockData) optional.get()));
            } else {
                VarInt.write(bytebuf, 0);
            }

        }

        public Optional<IBlockData> decode(ByteBuf bytebuf) {
            int i = VarInt.read(bytebuf);

            return i == 0 ? Optional.empty() : Optional.of(Block.stateById(i));
        }
    };
    public static final DataWatcherSerializer<Optional<IBlockData>> OPTIONAL_BLOCK_STATE = DataWatcherSerializer.forValueType(DataWatcherRegistry.OPTIONAL_BLOCK_STATE_CODEC);
    public static final DataWatcherSerializer<Boolean> BOOLEAN = DataWatcherSerializer.forValueType(ByteBufCodecs.BOOL);
    public static final DataWatcherSerializer<ParticleParam> PARTICLE = DataWatcherSerializer.forValueType(Particles.STREAM_CODEC);
    public static final DataWatcherSerializer<List<ParticleParam>> PARTICLES = DataWatcherSerializer.forValueType(Particles.STREAM_CODEC.apply(ByteBufCodecs.list()));
    public static final DataWatcherSerializer<Vector3f> ROTATIONS = DataWatcherSerializer.forValueType(Vector3f.STREAM_CODEC);
    public static final DataWatcherSerializer<BlockPosition> BLOCK_POS = DataWatcherSerializer.forValueType(BlockPosition.STREAM_CODEC);
    public static final DataWatcherSerializer<Optional<BlockPosition>> OPTIONAL_BLOCK_POS = DataWatcherSerializer.forValueType(BlockPosition.STREAM_CODEC.apply(ByteBufCodecs::optional));
    public static final DataWatcherSerializer<EnumDirection> DIRECTION = DataWatcherSerializer.forValueType(EnumDirection.STREAM_CODEC);
    public static final DataWatcherSerializer<Optional<UUID>> OPTIONAL_UUID = DataWatcherSerializer.forValueType(UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional));
    public static final DataWatcherSerializer<Optional<GlobalPos>> OPTIONAL_GLOBAL_POS = DataWatcherSerializer.forValueType(GlobalPos.STREAM_CODEC.apply(ByteBufCodecs::optional));
    public static final DataWatcherSerializer<NBTTagCompound> COMPOUND_TAG = new DataWatcherSerializer<NBTTagCompound>() {
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, NBTTagCompound> codec() {
            return ByteBufCodecs.TRUSTED_COMPOUND_TAG;
        }

        public NBTTagCompound copy(NBTTagCompound nbttagcompound) {
            return nbttagcompound.copy();
        }
    };
    public static final DataWatcherSerializer<VillagerData> VILLAGER_DATA = DataWatcherSerializer.forValueType(VillagerData.STREAM_CODEC);
    private static final StreamCodec<ByteBuf, OptionalInt> OPTIONAL_UNSIGNED_INT_CODEC = new StreamCodec<ByteBuf, OptionalInt>() {
        public OptionalInt decode(ByteBuf bytebuf) {
            int i = VarInt.read(bytebuf);

            return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
        }

        public void encode(ByteBuf bytebuf, OptionalInt optionalint) {
            VarInt.write(bytebuf, optionalint.orElse(-1) + 1);
        }
    };
    public static final DataWatcherSerializer<OptionalInt> OPTIONAL_UNSIGNED_INT = DataWatcherSerializer.forValueType(DataWatcherRegistry.OPTIONAL_UNSIGNED_INT_CODEC);
    public static final DataWatcherSerializer<EntityPose> POSE = DataWatcherSerializer.forValueType(EntityPose.STREAM_CODEC);
    public static final DataWatcherSerializer<Holder<CatVariant>> CAT_VARIANT = DataWatcherSerializer.forValueType(CatVariant.STREAM_CODEC);
    public static final DataWatcherSerializer<Holder<WolfVariant>> WOLF_VARIANT = DataWatcherSerializer.forValueType(WolfVariant.STREAM_CODEC);
    public static final DataWatcherSerializer<Holder<FrogVariant>> FROG_VARIANT = DataWatcherSerializer.forValueType(FrogVariant.STREAM_CODEC);
    public static final DataWatcherSerializer<Holder<PaintingVariant>> PAINTING_VARIANT = DataWatcherSerializer.forValueType(PaintingVariant.STREAM_CODEC);
    public static final DataWatcherSerializer<Armadillo.a> ARMADILLO_STATE = DataWatcherSerializer.forValueType(Armadillo.a.STREAM_CODEC);
    public static final DataWatcherSerializer<Sniffer.State> SNIFFER_STATE = DataWatcherSerializer.forValueType(Sniffer.State.STREAM_CODEC);
    public static final DataWatcherSerializer<org.joml.Vector3f> VECTOR3 = DataWatcherSerializer.forValueType(ByteBufCodecs.VECTOR3F);
    public static final DataWatcherSerializer<Quaternionf> QUATERNION = DataWatcherSerializer.forValueType(ByteBufCodecs.QUATERNIONF);

    public static void registerSerializer(DataWatcherSerializer<?> datawatcherserializer) {
        DataWatcherRegistry.SERIALIZERS.add(datawatcherserializer);
    }

    @Nullable
    public static DataWatcherSerializer<?> getSerializer(int i) {
        return (DataWatcherSerializer) DataWatcherRegistry.SERIALIZERS.byId(i);
    }

    public static int getSerializedId(DataWatcherSerializer<?> datawatcherserializer) {
        return DataWatcherRegistry.SERIALIZERS.getId(datawatcherserializer);
    }

    private DataWatcherRegistry() {}

    static {
        registerSerializer(DataWatcherRegistry.BYTE);
        registerSerializer(DataWatcherRegistry.INT);
        registerSerializer(DataWatcherRegistry.LONG);
        registerSerializer(DataWatcherRegistry.FLOAT);
        registerSerializer(DataWatcherRegistry.STRING);
        registerSerializer(DataWatcherRegistry.COMPONENT);
        registerSerializer(DataWatcherRegistry.OPTIONAL_COMPONENT);
        registerSerializer(DataWatcherRegistry.ITEM_STACK);
        registerSerializer(DataWatcherRegistry.BOOLEAN);
        registerSerializer(DataWatcherRegistry.ROTATIONS);
        registerSerializer(DataWatcherRegistry.BLOCK_POS);
        registerSerializer(DataWatcherRegistry.OPTIONAL_BLOCK_POS);
        registerSerializer(DataWatcherRegistry.DIRECTION);
        registerSerializer(DataWatcherRegistry.OPTIONAL_UUID);
        registerSerializer(DataWatcherRegistry.BLOCK_STATE);
        registerSerializer(DataWatcherRegistry.OPTIONAL_BLOCK_STATE);
        registerSerializer(DataWatcherRegistry.COMPOUND_TAG);
        registerSerializer(DataWatcherRegistry.PARTICLE);
        registerSerializer(DataWatcherRegistry.PARTICLES);
        registerSerializer(DataWatcherRegistry.VILLAGER_DATA);
        registerSerializer(DataWatcherRegistry.OPTIONAL_UNSIGNED_INT);
        registerSerializer(DataWatcherRegistry.POSE);
        registerSerializer(DataWatcherRegistry.CAT_VARIANT);
        registerSerializer(DataWatcherRegistry.WOLF_VARIANT);
        registerSerializer(DataWatcherRegistry.FROG_VARIANT);
        registerSerializer(DataWatcherRegistry.OPTIONAL_GLOBAL_POS);
        registerSerializer(DataWatcherRegistry.PAINTING_VARIANT);
        registerSerializer(DataWatcherRegistry.SNIFFER_STATE);
        registerSerializer(DataWatcherRegistry.ARMADILLO_STATE);
        registerSerializer(DataWatcherRegistry.VECTOR3);
        registerSerializer(DataWatcherRegistry.QUATERNION);
    }
}
