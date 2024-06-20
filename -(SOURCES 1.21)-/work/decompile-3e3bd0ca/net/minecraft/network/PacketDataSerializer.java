package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.IRegistry;
import net.minecraft.core.SectionPosition;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.util.CryptographyException;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MinecraftEncryption;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PacketDataSerializer extends ByteBuf {

    public static final int DEFAULT_NBT_QUOTA = 2097152;
    private final ByteBuf source;
    public static final short MAX_STRING_LENGTH = Short.MAX_VALUE;
    public static final int MAX_COMPONENT_STRING_LENGTH = 262144;
    private static final int PUBLIC_KEY_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_HEADER_SIZE = 256;
    private static final int MAX_PUBLIC_KEY_LENGTH = 512;
    private static final Gson GSON = new Gson();

    public PacketDataSerializer(ByteBuf bytebuf) {
        this.source = bytebuf;
    }

    /** @deprecated */
    @Deprecated
    public <T> T readWithCodecTrusted(DynamicOps<NBTBase> dynamicops, Codec<T> codec) {
        return this.readWithCodec(dynamicops, codec, NBTReadLimiter.unlimitedHeap());
    }

    /** @deprecated */
    @Deprecated
    public <T> T readWithCodec(DynamicOps<NBTBase> dynamicops, Codec<T> codec, NBTReadLimiter nbtreadlimiter) {
        NBTBase nbtbase = this.readNbt(nbtreadlimiter);

        return codec.parse(dynamicops, nbtbase).getOrThrow((s) -> {
            return new DecoderException("Failed to decode: " + s + " " + String.valueOf(nbtbase));
        });
    }

    /** @deprecated */
    @Deprecated
    public <T> PacketDataSerializer writeWithCodec(DynamicOps<NBTBase> dynamicops, Codec<T> codec, T t0) {
        NBTBase nbtbase = (NBTBase) codec.encodeStart(dynamicops, t0).getOrThrow((s) -> {
            return new EncoderException("Failed to encode: " + s + " " + String.valueOf(t0));
        });

        this.writeNbt(nbtbase);
        return this;
    }

    public <T> T readJsonWithCodec(Codec<T> codec) {
        JsonElement jsonelement = (JsonElement) ChatDeserializer.fromJson(PacketDataSerializer.GSON, this.readUtf(), JsonElement.class);
        DataResult<T> dataresult = codec.parse(JsonOps.INSTANCE, jsonelement);

        return dataresult.getOrThrow((s) -> {
            return new DecoderException("Failed to decode json: " + s);
        });
    }

    public <T> void writeJsonWithCodec(Codec<T> codec, T t0) {
        DataResult<JsonElement> dataresult = codec.encodeStart(JsonOps.INSTANCE, t0);

        this.writeUtf(PacketDataSerializer.GSON.toJson((JsonElement) dataresult.getOrThrow((s) -> {
            return new EncoderException("Failed to encode: " + s + " " + String.valueOf(t0));
        })));
    }

    public static <T> IntFunction<T> limitValue(IntFunction<T> intfunction, int i) {
        return (j) -> {
            if (j > i) {
                throw new DecoderException("Value " + j + " is larger than limit " + i);
            } else {
                return intfunction.apply(j);
            }
        };
    }

    public <T, C extends Collection<T>> C readCollection(IntFunction<C> intfunction, StreamDecoder<? super PacketDataSerializer, T> streamdecoder) {
        int i = this.readVarInt();
        C c0 = (Collection) intfunction.apply(i);

        for (int j = 0; j < i; ++j) {
            c0.add(streamdecoder.decode(this));
        }

        return c0;
    }

    public <T> void writeCollection(Collection<T> collection, StreamEncoder<? super PacketDataSerializer, T> streamencoder) {
        this.writeVarInt(collection.size());
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            T t0 = iterator.next();

            streamencoder.encode(this, t0);
        }

    }

    public <T> List<T> readList(StreamDecoder<? super PacketDataSerializer, T> streamdecoder) {
        return (List) this.readCollection(Lists::newArrayListWithCapacity, streamdecoder);
    }

    public IntList readIntIdList() {
        int i = this.readVarInt();
        IntArrayList intarraylist = new IntArrayList();

        for (int j = 0; j < i; ++j) {
            intarraylist.add(this.readVarInt());
        }

        return intarraylist;
    }

    public void writeIntIdList(IntList intlist) {
        this.writeVarInt(intlist.size());
        intlist.forEach(this::writeVarInt);
    }

    public <K, V, M extends Map<K, V>> M readMap(IntFunction<M> intfunction, StreamDecoder<? super PacketDataSerializer, K> streamdecoder, StreamDecoder<? super PacketDataSerializer, V> streamdecoder1) {
        int i = this.readVarInt();
        M m0 = (Map) intfunction.apply(i);

        for (int j = 0; j < i; ++j) {
            K k0 = streamdecoder.decode(this);
            V v0 = streamdecoder1.decode(this);

            m0.put(k0, v0);
        }

        return m0;
    }

    public <K, V> Map<K, V> readMap(StreamDecoder<? super PacketDataSerializer, K> streamdecoder, StreamDecoder<? super PacketDataSerializer, V> streamdecoder1) {
        return this.readMap(Maps::newHashMapWithExpectedSize, streamdecoder, streamdecoder1);
    }

    public <K, V> void writeMap(Map<K, V> map, StreamEncoder<? super PacketDataSerializer, K> streamencoder, StreamEncoder<? super PacketDataSerializer, V> streamencoder1) {
        this.writeVarInt(map.size());
        map.forEach((object, object1) -> {
            streamencoder.encode(this, object);
            streamencoder1.encode(this, object1);
        });
    }

    public void readWithCount(Consumer<PacketDataSerializer> consumer) {
        int i = this.readVarInt();

        for (int j = 0; j < i; ++j) {
            consumer.accept(this);
        }

    }

    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumset, Class<E> oclass) {
        E[] ae = (Enum[]) oclass.getEnumConstants();
        BitSet bitset = new BitSet(ae.length);

        for (int i = 0; i < ae.length; ++i) {
            bitset.set(i, enumset.contains(ae[i]));
        }

        this.writeFixedBitSet(bitset, ae.length);
    }

    public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> oclass) {
        E[] ae = (Enum[]) oclass.getEnumConstants();
        BitSet bitset = this.readFixedBitSet(ae.length);
        EnumSet<E> enumset = EnumSet.noneOf(oclass);

        for (int i = 0; i < ae.length; ++i) {
            if (bitset.get(i)) {
                enumset.add(ae[i]);
            }
        }

        return enumset;
    }

    public <T> void writeOptional(Optional<T> optional, StreamEncoder<? super PacketDataSerializer, T> streamencoder) {
        if (optional.isPresent()) {
            this.writeBoolean(true);
            streamencoder.encode(this, optional.get());
        } else {
            this.writeBoolean(false);
        }

    }

    public <T> Optional<T> readOptional(StreamDecoder<? super PacketDataSerializer, T> streamdecoder) {
        return this.readBoolean() ? Optional.of(streamdecoder.decode(this)) : Optional.empty();
    }

    @Nullable
    public <T> T readNullable(StreamDecoder<? super PacketDataSerializer, T> streamdecoder) {
        return readNullable(this, streamdecoder);
    }

    @Nullable
    public static <T, B extends ByteBuf> T readNullable(B b0, StreamDecoder<? super B, T> streamdecoder) {
        return b0.readBoolean() ? streamdecoder.decode(b0) : null;
    }

    public <T> void writeNullable(@Nullable T t0, StreamEncoder<? super PacketDataSerializer, T> streamencoder) {
        writeNullable(this, t0, streamencoder);
    }

    public static <T, B extends ByteBuf> void writeNullable(B b0, @Nullable T t0, StreamEncoder<? super B, T> streamencoder) {
        if (t0 != null) {
            b0.writeBoolean(true);
            streamencoder.encode(b0, t0);
        } else {
            b0.writeBoolean(false);
        }

    }

    public byte[] readByteArray() {
        return readByteArray(this);
    }

    public static byte[] readByteArray(ByteBuf bytebuf) {
        return readByteArray(bytebuf, bytebuf.readableBytes());
    }

    public PacketDataSerializer writeByteArray(byte[] abyte) {
        writeByteArray(this, abyte);
        return this;
    }

    public static void writeByteArray(ByteBuf bytebuf, byte[] abyte) {
        VarInt.write(bytebuf, abyte.length);
        bytebuf.writeBytes(abyte);
    }

    public byte[] readByteArray(int i) {
        return readByteArray(this, i);
    }

    public static byte[] readByteArray(ByteBuf bytebuf, int i) {
        int j = VarInt.read(bytebuf);

        if (j > i) {
            throw new DecoderException("ByteArray with size " + j + " is bigger than allowed " + i);
        } else {
            byte[] abyte = new byte[j];

            bytebuf.readBytes(abyte);
            return abyte;
        }
    }

    public PacketDataSerializer writeVarIntArray(int[] aint) {
        this.writeVarInt(aint.length);
        int[] aint1 = aint;
        int i = aint.length;

        for (int j = 0; j < i; ++j) {
            int k = aint1[j];

            this.writeVarInt(k);
        }

        return this;
    }

    public int[] readVarIntArray() {
        return this.readVarIntArray(this.readableBytes());
    }

    public int[] readVarIntArray(int i) {
        int j = this.readVarInt();

        if (j > i) {
            throw new DecoderException("VarIntArray with size " + j + " is bigger than allowed " + i);
        } else {
            int[] aint = new int[j];

            for (int k = 0; k < aint.length; ++k) {
                aint[k] = this.readVarInt();
            }

            return aint;
        }
    }

    public PacketDataSerializer writeLongArray(long[] along) {
        this.writeVarInt(along.length);
        long[] along1 = along;
        int i = along.length;

        for (int j = 0; j < i; ++j) {
            long k = along1[j];

            this.writeLong(k);
        }

        return this;
    }

    public long[] readLongArray() {
        return this.readLongArray((long[]) null);
    }

    public long[] readLongArray(@Nullable long[] along) {
        return this.readLongArray(along, this.readableBytes() / 8);
    }

    public long[] readLongArray(@Nullable long[] along, int i) {
        int j = this.readVarInt();

        if (along == null || along.length != j) {
            if (j > i) {
                throw new DecoderException("LongArray with size " + j + " is bigger than allowed " + i);
            }

            along = new long[j];
        }

        for (int k = 0; k < along.length; ++k) {
            along[k] = this.readLong();
        }

        return along;
    }

    public BlockPosition readBlockPos() {
        return readBlockPos(this);
    }

    public static BlockPosition readBlockPos(ByteBuf bytebuf) {
        return BlockPosition.of(bytebuf.readLong());
    }

    public PacketDataSerializer writeBlockPos(BlockPosition blockposition) {
        writeBlockPos(this, blockposition);
        return this;
    }

    public static void writeBlockPos(ByteBuf bytebuf, BlockPosition blockposition) {
        bytebuf.writeLong(blockposition.asLong());
    }

    public ChunkCoordIntPair readChunkPos() {
        return new ChunkCoordIntPair(this.readLong());
    }

    public PacketDataSerializer writeChunkPos(ChunkCoordIntPair chunkcoordintpair) {
        this.writeLong(chunkcoordintpair.toLong());
        return this;
    }

    public SectionPosition readSectionPos() {
        return SectionPosition.of(this.readLong());
    }

    public PacketDataSerializer writeSectionPos(SectionPosition sectionposition) {
        this.writeLong(sectionposition.asLong());
        return this;
    }

    public GlobalPos readGlobalPos() {
        ResourceKey<World> resourcekey = this.readResourceKey(Registries.DIMENSION);
        BlockPosition blockposition = this.readBlockPos();

        return GlobalPos.of(resourcekey, blockposition);
    }

    public void writeGlobalPos(GlobalPos globalpos) {
        this.writeResourceKey(globalpos.dimension());
        this.writeBlockPos(globalpos.pos());
    }

    public Vector3f readVector3f() {
        return readVector3f(this);
    }

    public static Vector3f readVector3f(ByteBuf bytebuf) {
        return new Vector3f(bytebuf.readFloat(), bytebuf.readFloat(), bytebuf.readFloat());
    }

    public void writeVector3f(Vector3f vector3f) {
        writeVector3f(this, vector3f);
    }

    public static void writeVector3f(ByteBuf bytebuf, Vector3f vector3f) {
        bytebuf.writeFloat(vector3f.x());
        bytebuf.writeFloat(vector3f.y());
        bytebuf.writeFloat(vector3f.z());
    }

    public Quaternionf readQuaternion() {
        return readQuaternion(this);
    }

    public static Quaternionf readQuaternion(ByteBuf bytebuf) {
        return new Quaternionf(bytebuf.readFloat(), bytebuf.readFloat(), bytebuf.readFloat(), bytebuf.readFloat());
    }

    public void writeQuaternion(Quaternionf quaternionf) {
        writeQuaternion(this, quaternionf);
    }

    public static void writeQuaternion(ByteBuf bytebuf, Quaternionf quaternionf) {
        bytebuf.writeFloat(quaternionf.x);
        bytebuf.writeFloat(quaternionf.y);
        bytebuf.writeFloat(quaternionf.z);
        bytebuf.writeFloat(quaternionf.w);
    }

    public Vec3D readVec3() {
        return new Vec3D(this.readDouble(), this.readDouble(), this.readDouble());
    }

    public void writeVec3(Vec3D vec3d) {
        this.writeDouble(vec3d.x());
        this.writeDouble(vec3d.y());
        this.writeDouble(vec3d.z());
    }

    public <T extends Enum<T>> T readEnum(Class<T> oclass) {
        return ((Enum[]) oclass.getEnumConstants())[this.readVarInt()];
    }

    public PacketDataSerializer writeEnum(Enum<?> oenum) {
        return this.writeVarInt(oenum.ordinal());
    }

    public <T> T readById(IntFunction<T> intfunction) {
        int i = this.readVarInt();

        return intfunction.apply(i);
    }

    public <T> PacketDataSerializer writeById(ToIntFunction<T> tointfunction, T t0) {
        int i = tointfunction.applyAsInt(t0);

        return this.writeVarInt(i);
    }

    public int readVarInt() {
        return VarInt.read(this.source);
    }

    public long readVarLong() {
        return VarLong.read(this.source);
    }

    public PacketDataSerializer writeUUID(UUID uuid) {
        writeUUID(this, uuid);
        return this;
    }

    public static void writeUUID(ByteBuf bytebuf, UUID uuid) {
        bytebuf.writeLong(uuid.getMostSignificantBits());
        bytebuf.writeLong(uuid.getLeastSignificantBits());
    }

    public UUID readUUID() {
        return readUUID(this);
    }

    public static UUID readUUID(ByteBuf bytebuf) {
        return new UUID(bytebuf.readLong(), bytebuf.readLong());
    }

    public PacketDataSerializer writeVarInt(int i) {
        VarInt.write(this.source, i);
        return this;
    }

    public PacketDataSerializer writeVarLong(long i) {
        VarLong.write(this.source, i);
        return this;
    }

    public PacketDataSerializer writeNbt(@Nullable NBTBase nbtbase) {
        writeNbt(this, nbtbase);
        return this;
    }

    public static void writeNbt(ByteBuf bytebuf, @Nullable NBTBase nbtbase) {
        if (nbtbase == null) {
            nbtbase = NBTTagEnd.INSTANCE;
        }

        try {
            NBTCompressedStreamTools.writeAnyTag((NBTBase) nbtbase, new ByteBufOutputStream(bytebuf));
        } catch (IOException ioexception) {
            throw new EncoderException(ioexception);
        }
    }

    @Nullable
    public NBTTagCompound readNbt() {
        return readNbt((ByteBuf) this);
    }

    @Nullable
    public static NBTTagCompound readNbt(ByteBuf bytebuf) {
        NBTBase nbtbase = readNbt(bytebuf, NBTReadLimiter.create(2097152L));

        if (nbtbase != null && !(nbtbase instanceof NBTTagCompound)) {
            throw new DecoderException("Not a compound tag: " + String.valueOf(nbtbase));
        } else {
            return (NBTTagCompound) nbtbase;
        }
    }

    @Nullable
    public static NBTBase readNbt(ByteBuf bytebuf, NBTReadLimiter nbtreadlimiter) {
        try {
            NBTBase nbtbase = NBTCompressedStreamTools.readAnyTag(new ByteBufInputStream(bytebuf), nbtreadlimiter);

            return nbtbase.getId() == 0 ? null : nbtbase;
        } catch (IOException ioexception) {
            throw new EncoderException(ioexception);
        }
    }

    @Nullable
    public NBTBase readNbt(NBTReadLimiter nbtreadlimiter) {
        return readNbt(this, nbtreadlimiter);
    }

    public String readUtf() {
        return this.readUtf(32767);
    }

    public String readUtf(int i) {
        return Utf8String.read(this.source, i);
    }

    public PacketDataSerializer writeUtf(String s) {
        return this.writeUtf(s, 32767);
    }

    public PacketDataSerializer writeUtf(String s, int i) {
        Utf8String.write(this.source, s, i);
        return this;
    }

    public MinecraftKey readResourceLocation() {
        return MinecraftKey.parse(this.readUtf(32767));
    }

    public PacketDataSerializer writeResourceLocation(MinecraftKey minecraftkey) {
        this.writeUtf(minecraftkey.toString());
        return this;
    }

    public <T> ResourceKey<T> readResourceKey(ResourceKey<? extends IRegistry<T>> resourcekey) {
        MinecraftKey minecraftkey = this.readResourceLocation();

        return ResourceKey.create(resourcekey, minecraftkey);
    }

    public void writeResourceKey(ResourceKey<?> resourcekey) {
        this.writeResourceLocation(resourcekey.location());
    }

    public <T> ResourceKey<? extends IRegistry<T>> readRegistryKey() {
        MinecraftKey minecraftkey = this.readResourceLocation();

        return ResourceKey.createRegistryKey(minecraftkey);
    }

    public Date readDate() {
        return new Date(this.readLong());
    }

    public PacketDataSerializer writeDate(Date date) {
        this.writeLong(date.getTime());
        return this;
    }

    public Instant readInstant() {
        return Instant.ofEpochMilli(this.readLong());
    }

    public void writeInstant(Instant instant) {
        this.writeLong(instant.toEpochMilli());
    }

    public PublicKey readPublicKey() {
        try {
            return MinecraftEncryption.byteToPublicKey(this.readByteArray(512));
        } catch (CryptographyException cryptographyexception) {
            throw new DecoderException("Malformed public key bytes", cryptographyexception);
        }
    }

    public PacketDataSerializer writePublicKey(PublicKey publickey) {
        this.writeByteArray(publickey.getEncoded());
        return this;
    }

    public MovingObjectPositionBlock readBlockHitResult() {
        BlockPosition blockposition = this.readBlockPos();
        EnumDirection enumdirection = (EnumDirection) this.readEnum(EnumDirection.class);
        float f = this.readFloat();
        float f1 = this.readFloat();
        float f2 = this.readFloat();
        boolean flag = this.readBoolean();

        return new MovingObjectPositionBlock(new Vec3D((double) blockposition.getX() + (double) f, (double) blockposition.getY() + (double) f1, (double) blockposition.getZ() + (double) f2), enumdirection, blockposition, flag);
    }

    public void writeBlockHitResult(MovingObjectPositionBlock movingobjectpositionblock) {
        BlockPosition blockposition = movingobjectpositionblock.getBlockPos();

        this.writeBlockPos(blockposition);
        this.writeEnum(movingobjectpositionblock.getDirection());
        Vec3D vec3d = movingobjectpositionblock.getLocation();

        this.writeFloat((float) (vec3d.x - (double) blockposition.getX()));
        this.writeFloat((float) (vec3d.y - (double) blockposition.getY()));
        this.writeFloat((float) (vec3d.z - (double) blockposition.getZ()));
        this.writeBoolean(movingobjectpositionblock.isInside());
    }

    public BitSet readBitSet() {
        return BitSet.valueOf(this.readLongArray());
    }

    public void writeBitSet(BitSet bitset) {
        this.writeLongArray(bitset.toLongArray());
    }

    public BitSet readFixedBitSet(int i) {
        byte[] abyte = new byte[MathHelper.positiveCeilDiv(i, 8)];

        this.readBytes(abyte);
        return BitSet.valueOf(abyte);
    }

    public void writeFixedBitSet(BitSet bitset, int i) {
        if (bitset.length() > i) {
            int j = bitset.length();

            throw new EncoderException("BitSet is larger than expected size (" + j + ">" + i + ")");
        } else {
            byte[] abyte = bitset.toByteArray();

            this.writeBytes(Arrays.copyOf(abyte, MathHelper.positiveCeilDiv(i, 8)));
        }
    }

    public boolean isContiguous() {
        return this.source.isContiguous();
    }

    public int maxFastWritableBytes() {
        return this.source.maxFastWritableBytes();
    }

    public int capacity() {
        return this.source.capacity();
    }

    public PacketDataSerializer capacity(int i) {
        this.source.capacity(i);
        return this;
    }

    public int maxCapacity() {
        return this.source.maxCapacity();
    }

    public ByteBufAllocator alloc() {
        return this.source.alloc();
    }

    public ByteOrder order() {
        return this.source.order();
    }

    public ByteBuf order(ByteOrder byteorder) {
        return this.source.order(byteorder);
    }

    public ByteBuf unwrap() {
        return this.source;
    }

    public boolean isDirect() {
        return this.source.isDirect();
    }

    public boolean isReadOnly() {
        return this.source.isReadOnly();
    }

    public ByteBuf asReadOnly() {
        return this.source.asReadOnly();
    }

    public int readerIndex() {
        return this.source.readerIndex();
    }

    public PacketDataSerializer readerIndex(int i) {
        this.source.readerIndex(i);
        return this;
    }

    public int writerIndex() {
        return this.source.writerIndex();
    }

    public PacketDataSerializer writerIndex(int i) {
        this.source.writerIndex(i);
        return this;
    }

    public PacketDataSerializer setIndex(int i, int j) {
        this.source.setIndex(i, j);
        return this;
    }

    public int readableBytes() {
        return this.source.readableBytes();
    }

    public int writableBytes() {
        return this.source.writableBytes();
    }

    public int maxWritableBytes() {
        return this.source.maxWritableBytes();
    }

    public boolean isReadable() {
        return this.source.isReadable();
    }

    public boolean isReadable(int i) {
        return this.source.isReadable(i);
    }

    public boolean isWritable() {
        return this.source.isWritable();
    }

    public boolean isWritable(int i) {
        return this.source.isWritable(i);
    }

    public PacketDataSerializer clear() {
        this.source.clear();
        return this;
    }

    public PacketDataSerializer markReaderIndex() {
        this.source.markReaderIndex();
        return this;
    }

    public PacketDataSerializer resetReaderIndex() {
        this.source.resetReaderIndex();
        return this;
    }

    public PacketDataSerializer markWriterIndex() {
        this.source.markWriterIndex();
        return this;
    }

    public PacketDataSerializer resetWriterIndex() {
        this.source.resetWriterIndex();
        return this;
    }

    public PacketDataSerializer discardReadBytes() {
        this.source.discardReadBytes();
        return this;
    }

    public PacketDataSerializer discardSomeReadBytes() {
        this.source.discardSomeReadBytes();
        return this;
    }

    public PacketDataSerializer ensureWritable(int i) {
        this.source.ensureWritable(i);
        return this;
    }

    public int ensureWritable(int i, boolean flag) {
        return this.source.ensureWritable(i, flag);
    }

    public boolean getBoolean(int i) {
        return this.source.getBoolean(i);
    }

    public byte getByte(int i) {
        return this.source.getByte(i);
    }

    public short getUnsignedByte(int i) {
        return this.source.getUnsignedByte(i);
    }

    public short getShort(int i) {
        return this.source.getShort(i);
    }

    public short getShortLE(int i) {
        return this.source.getShortLE(i);
    }

    public int getUnsignedShort(int i) {
        return this.source.getUnsignedShort(i);
    }

    public int getUnsignedShortLE(int i) {
        return this.source.getUnsignedShortLE(i);
    }

    public int getMedium(int i) {
        return this.source.getMedium(i);
    }

    public int getMediumLE(int i) {
        return this.source.getMediumLE(i);
    }

    public int getUnsignedMedium(int i) {
        return this.source.getUnsignedMedium(i);
    }

    public int getUnsignedMediumLE(int i) {
        return this.source.getUnsignedMediumLE(i);
    }

    public int getInt(int i) {
        return this.source.getInt(i);
    }

    public int getIntLE(int i) {
        return this.source.getIntLE(i);
    }

    public long getUnsignedInt(int i) {
        return this.source.getUnsignedInt(i);
    }

    public long getUnsignedIntLE(int i) {
        return this.source.getUnsignedIntLE(i);
    }

    public long getLong(int i) {
        return this.source.getLong(i);
    }

    public long getLongLE(int i) {
        return this.source.getLongLE(i);
    }

    public char getChar(int i) {
        return this.source.getChar(i);
    }

    public float getFloat(int i) {
        return this.source.getFloat(i);
    }

    public double getDouble(int i) {
        return this.source.getDouble(i);
    }

    public PacketDataSerializer getBytes(int i, ByteBuf bytebuf) {
        this.source.getBytes(i, bytebuf);
        return this;
    }

    public PacketDataSerializer getBytes(int i, ByteBuf bytebuf, int j) {
        this.source.getBytes(i, bytebuf, j);
        return this;
    }

    public PacketDataSerializer getBytes(int i, ByteBuf bytebuf, int j, int k) {
        this.source.getBytes(i, bytebuf, j, k);
        return this;
    }

    public PacketDataSerializer getBytes(int i, byte[] abyte) {
        this.source.getBytes(i, abyte);
        return this;
    }

    public PacketDataSerializer getBytes(int i, byte[] abyte, int j, int k) {
        this.source.getBytes(i, abyte, j, k);
        return this;
    }

    public PacketDataSerializer getBytes(int i, ByteBuffer bytebuffer) {
        this.source.getBytes(i, bytebuffer);
        return this;
    }

    public PacketDataSerializer getBytes(int i, OutputStream outputstream, int j) throws IOException {
        this.source.getBytes(i, outputstream, j);
        return this;
    }

    public int getBytes(int i, GatheringByteChannel gatheringbytechannel, int j) throws IOException {
        return this.source.getBytes(i, gatheringbytechannel, j);
    }

    public int getBytes(int i, FileChannel filechannel, long j, int k) throws IOException {
        return this.source.getBytes(i, filechannel, j, k);
    }

    public CharSequence getCharSequence(int i, int j, Charset charset) {
        return this.source.getCharSequence(i, j, charset);
    }

    public PacketDataSerializer setBoolean(int i, boolean flag) {
        this.source.setBoolean(i, flag);
        return this;
    }

    public PacketDataSerializer setByte(int i, int j) {
        this.source.setByte(i, j);
        return this;
    }

    public PacketDataSerializer setShort(int i, int j) {
        this.source.setShort(i, j);
        return this;
    }

    public PacketDataSerializer setShortLE(int i, int j) {
        this.source.setShortLE(i, j);
        return this;
    }

    public PacketDataSerializer setMedium(int i, int j) {
        this.source.setMedium(i, j);
        return this;
    }

    public PacketDataSerializer setMediumLE(int i, int j) {
        this.source.setMediumLE(i, j);
        return this;
    }

    public PacketDataSerializer setInt(int i, int j) {
        this.source.setInt(i, j);
        return this;
    }

    public PacketDataSerializer setIntLE(int i, int j) {
        this.source.setIntLE(i, j);
        return this;
    }

    public PacketDataSerializer setLong(int i, long j) {
        this.source.setLong(i, j);
        return this;
    }

    public PacketDataSerializer setLongLE(int i, long j) {
        this.source.setLongLE(i, j);
        return this;
    }

    public PacketDataSerializer setChar(int i, int j) {
        this.source.setChar(i, j);
        return this;
    }

    public PacketDataSerializer setFloat(int i, float f) {
        this.source.setFloat(i, f);
        return this;
    }

    public PacketDataSerializer setDouble(int i, double d0) {
        this.source.setDouble(i, d0);
        return this;
    }

    public PacketDataSerializer setBytes(int i, ByteBuf bytebuf) {
        this.source.setBytes(i, bytebuf);
        return this;
    }

    public PacketDataSerializer setBytes(int i, ByteBuf bytebuf, int j) {
        this.source.setBytes(i, bytebuf, j);
        return this;
    }

    public PacketDataSerializer setBytes(int i, ByteBuf bytebuf, int j, int k) {
        this.source.setBytes(i, bytebuf, j, k);
        return this;
    }

    public PacketDataSerializer setBytes(int i, byte[] abyte) {
        this.source.setBytes(i, abyte);
        return this;
    }

    public PacketDataSerializer setBytes(int i, byte[] abyte, int j, int k) {
        this.source.setBytes(i, abyte, j, k);
        return this;
    }

    public PacketDataSerializer setBytes(int i, ByteBuffer bytebuffer) {
        this.source.setBytes(i, bytebuffer);
        return this;
    }

    public int setBytes(int i, InputStream inputstream, int j) throws IOException {
        return this.source.setBytes(i, inputstream, j);
    }

    public int setBytes(int i, ScatteringByteChannel scatteringbytechannel, int j) throws IOException {
        return this.source.setBytes(i, scatteringbytechannel, j);
    }

    public int setBytes(int i, FileChannel filechannel, long j, int k) throws IOException {
        return this.source.setBytes(i, filechannel, j, k);
    }

    public PacketDataSerializer setZero(int i, int j) {
        this.source.setZero(i, j);
        return this;
    }

    public int setCharSequence(int i, CharSequence charsequence, Charset charset) {
        return this.source.setCharSequence(i, charsequence, charset);
    }

    public boolean readBoolean() {
        return this.source.readBoolean();
    }

    public byte readByte() {
        return this.source.readByte();
    }

    public short readUnsignedByte() {
        return this.source.readUnsignedByte();
    }

    public short readShort() {
        return this.source.readShort();
    }

    public short readShortLE() {
        return this.source.readShortLE();
    }

    public int readUnsignedShort() {
        return this.source.readUnsignedShort();
    }

    public int readUnsignedShortLE() {
        return this.source.readUnsignedShortLE();
    }

    public int readMedium() {
        return this.source.readMedium();
    }

    public int readMediumLE() {
        return this.source.readMediumLE();
    }

    public int readUnsignedMedium() {
        return this.source.readUnsignedMedium();
    }

    public int readUnsignedMediumLE() {
        return this.source.readUnsignedMediumLE();
    }

    public int readInt() {
        return this.source.readInt();
    }

    public int readIntLE() {
        return this.source.readIntLE();
    }

    public long readUnsignedInt() {
        return this.source.readUnsignedInt();
    }

    public long readUnsignedIntLE() {
        return this.source.readUnsignedIntLE();
    }

    public long readLong() {
        return this.source.readLong();
    }

    public long readLongLE() {
        return this.source.readLongLE();
    }

    public char readChar() {
        return this.source.readChar();
    }

    public float readFloat() {
        return this.source.readFloat();
    }

    public double readDouble() {
        return this.source.readDouble();
    }

    public ByteBuf readBytes(int i) {
        return this.source.readBytes(i);
    }

    public ByteBuf readSlice(int i) {
        return this.source.readSlice(i);
    }

    public ByteBuf readRetainedSlice(int i) {
        return this.source.readRetainedSlice(i);
    }

    public PacketDataSerializer readBytes(ByteBuf bytebuf) {
        this.source.readBytes(bytebuf);
        return this;
    }

    public PacketDataSerializer readBytes(ByteBuf bytebuf, int i) {
        this.source.readBytes(bytebuf, i);
        return this;
    }

    public PacketDataSerializer readBytes(ByteBuf bytebuf, int i, int j) {
        this.source.readBytes(bytebuf, i, j);
        return this;
    }

    public PacketDataSerializer readBytes(byte[] abyte) {
        this.source.readBytes(abyte);
        return this;
    }

    public PacketDataSerializer readBytes(byte[] abyte, int i, int j) {
        this.source.readBytes(abyte, i, j);
        return this;
    }

    public PacketDataSerializer readBytes(ByteBuffer bytebuffer) {
        this.source.readBytes(bytebuffer);
        return this;
    }

    public PacketDataSerializer readBytes(OutputStream outputstream, int i) throws IOException {
        this.source.readBytes(outputstream, i);
        return this;
    }

    public int readBytes(GatheringByteChannel gatheringbytechannel, int i) throws IOException {
        return this.source.readBytes(gatheringbytechannel, i);
    }

    public CharSequence readCharSequence(int i, Charset charset) {
        return this.source.readCharSequence(i, charset);
    }

    public int readBytes(FileChannel filechannel, long i, int j) throws IOException {
        return this.source.readBytes(filechannel, i, j);
    }

    public PacketDataSerializer skipBytes(int i) {
        this.source.skipBytes(i);
        return this;
    }

    public PacketDataSerializer writeBoolean(boolean flag) {
        this.source.writeBoolean(flag);
        return this;
    }

    public PacketDataSerializer writeByte(int i) {
        this.source.writeByte(i);
        return this;
    }

    public PacketDataSerializer writeShort(int i) {
        this.source.writeShort(i);
        return this;
    }

    public PacketDataSerializer writeShortLE(int i) {
        this.source.writeShortLE(i);
        return this;
    }

    public PacketDataSerializer writeMedium(int i) {
        this.source.writeMedium(i);
        return this;
    }

    public PacketDataSerializer writeMediumLE(int i) {
        this.source.writeMediumLE(i);
        return this;
    }

    public PacketDataSerializer writeInt(int i) {
        this.source.writeInt(i);
        return this;
    }

    public PacketDataSerializer writeIntLE(int i) {
        this.source.writeIntLE(i);
        return this;
    }

    public PacketDataSerializer writeLong(long i) {
        this.source.writeLong(i);
        return this;
    }

    public PacketDataSerializer writeLongLE(long i) {
        this.source.writeLongLE(i);
        return this;
    }

    public PacketDataSerializer writeChar(int i) {
        this.source.writeChar(i);
        return this;
    }

    public PacketDataSerializer writeFloat(float f) {
        this.source.writeFloat(f);
        return this;
    }

    public PacketDataSerializer writeDouble(double d0) {
        this.source.writeDouble(d0);
        return this;
    }

    public PacketDataSerializer writeBytes(ByteBuf bytebuf) {
        this.source.writeBytes(bytebuf);
        return this;
    }

    public PacketDataSerializer writeBytes(ByteBuf bytebuf, int i) {
        this.source.writeBytes(bytebuf, i);
        return this;
    }

    public PacketDataSerializer writeBytes(ByteBuf bytebuf, int i, int j) {
        this.source.writeBytes(bytebuf, i, j);
        return this;
    }

    public PacketDataSerializer writeBytes(byte[] abyte) {
        this.source.writeBytes(abyte);
        return this;
    }

    public PacketDataSerializer writeBytes(byte[] abyte, int i, int j) {
        this.source.writeBytes(abyte, i, j);
        return this;
    }

    public PacketDataSerializer writeBytes(ByteBuffer bytebuffer) {
        this.source.writeBytes(bytebuffer);
        return this;
    }

    public int writeBytes(InputStream inputstream, int i) throws IOException {
        return this.source.writeBytes(inputstream, i);
    }

    public int writeBytes(ScatteringByteChannel scatteringbytechannel, int i) throws IOException {
        return this.source.writeBytes(scatteringbytechannel, i);
    }

    public int writeBytes(FileChannel filechannel, long i, int j) throws IOException {
        return this.source.writeBytes(filechannel, i, j);
    }

    public PacketDataSerializer writeZero(int i) {
        this.source.writeZero(i);
        return this;
    }

    public int writeCharSequence(CharSequence charsequence, Charset charset) {
        return this.source.writeCharSequence(charsequence, charset);
    }

    public int indexOf(int i, int j, byte b0) {
        return this.source.indexOf(i, j, b0);
    }

    public int bytesBefore(byte b0) {
        return this.source.bytesBefore(b0);
    }

    public int bytesBefore(int i, byte b0) {
        return this.source.bytesBefore(i, b0);
    }

    public int bytesBefore(int i, int j, byte b0) {
        return this.source.bytesBefore(i, j, b0);
    }

    public int forEachByte(ByteProcessor byteprocessor) {
        return this.source.forEachByte(byteprocessor);
    }

    public int forEachByte(int i, int j, ByteProcessor byteprocessor) {
        return this.source.forEachByte(i, j, byteprocessor);
    }

    public int forEachByteDesc(ByteProcessor byteprocessor) {
        return this.source.forEachByteDesc(byteprocessor);
    }

    public int forEachByteDesc(int i, int j, ByteProcessor byteprocessor) {
        return this.source.forEachByteDesc(i, j, byteprocessor);
    }

    public ByteBuf copy() {
        return this.source.copy();
    }

    public ByteBuf copy(int i, int j) {
        return this.source.copy(i, j);
    }

    public ByteBuf slice() {
        return this.source.slice();
    }

    public ByteBuf retainedSlice() {
        return this.source.retainedSlice();
    }

    public ByteBuf slice(int i, int j) {
        return this.source.slice(i, j);
    }

    public ByteBuf retainedSlice(int i, int j) {
        return this.source.retainedSlice(i, j);
    }

    public ByteBuf duplicate() {
        return this.source.duplicate();
    }

    public ByteBuf retainedDuplicate() {
        return this.source.retainedDuplicate();
    }

    public int nioBufferCount() {
        return this.source.nioBufferCount();
    }

    public ByteBuffer nioBuffer() {
        return this.source.nioBuffer();
    }

    public ByteBuffer nioBuffer(int i, int j) {
        return this.source.nioBuffer(i, j);
    }

    public ByteBuffer internalNioBuffer(int i, int j) {
        return this.source.internalNioBuffer(i, j);
    }

    public ByteBuffer[] nioBuffers() {
        return this.source.nioBuffers();
    }

    public ByteBuffer[] nioBuffers(int i, int j) {
        return this.source.nioBuffers(i, j);
    }

    public boolean hasArray() {
        return this.source.hasArray();
    }

    public byte[] array() {
        return this.source.array();
    }

    public int arrayOffset() {
        return this.source.arrayOffset();
    }

    public boolean hasMemoryAddress() {
        return this.source.hasMemoryAddress();
    }

    public long memoryAddress() {
        return this.source.memoryAddress();
    }

    public String toString(Charset charset) {
        return this.source.toString(charset);
    }

    public String toString(int i, int j, Charset charset) {
        return this.source.toString(i, j, charset);
    }

    public int hashCode() {
        return this.source.hashCode();
    }

    public boolean equals(Object object) {
        return this.source.equals(object);
    }

    public int compareTo(ByteBuf bytebuf) {
        return this.source.compareTo(bytebuf);
    }

    public String toString() {
        return this.source.toString();
    }

    public PacketDataSerializer retain(int i) {
        this.source.retain(i);
        return this;
    }

    public PacketDataSerializer retain() {
        this.source.retain();
        return this;
    }

    public PacketDataSerializer touch() {
        this.source.touch();
        return this;
    }

    public PacketDataSerializer touch(Object object) {
        this.source.touch(object);
        return this;
    }

    public int refCnt() {
        return this.source.refCnt();
    }

    public boolean release() {
        return this.source.release();
    }

    public boolean release(int i) {
        return this.source.release(i);
    }
}
