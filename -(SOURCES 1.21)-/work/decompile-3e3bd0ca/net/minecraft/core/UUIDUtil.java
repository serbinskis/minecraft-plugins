package net.minecraft.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.util.UndashedUuid;
import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import net.minecraft.SystemUtils;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;

public final class UUIDUtil {

    public static final Codec<UUID> CODEC = Codec.INT_STREAM.comapFlatMap((intstream) -> {
        return SystemUtils.fixedSize(intstream, 4).map(UUIDUtil::uuidFromIntArray);
    }, (uuid) -> {
        return Arrays.stream(uuidToIntArray(uuid));
    });
    public static final Codec<Set<UUID>> CODEC_SET = Codec.list(UUIDUtil.CODEC).xmap(Sets::newHashSet, Lists::newArrayList);
    public static final Codec<Set<UUID>> CODEC_LINKED_SET = Codec.list(UUIDUtil.CODEC).xmap(Sets::newLinkedHashSet, Lists::newArrayList);
    public static final Codec<UUID> STRING_CODEC = Codec.STRING.comapFlatMap((s) -> {
        try {
            return DataResult.success(UUID.fromString(s), Lifecycle.stable());
        } catch (IllegalArgumentException illegalargumentexception) {
            return DataResult.error(() -> {
                return "Invalid UUID " + s + ": " + illegalargumentexception.getMessage();
            });
        }
    }, UUID::toString);
    public static final Codec<UUID> AUTHLIB_CODEC = Codec.withAlternative(Codec.STRING.comapFlatMap((s) -> {
        try {
            return DataResult.success(UndashedUuid.fromStringLenient(s), Lifecycle.stable());
        } catch (IllegalArgumentException illegalargumentexception) {
            return DataResult.error(() -> {
                return "Invalid UUID " + s + ": " + illegalargumentexception.getMessage();
            });
        }
    }, UndashedUuid::toString), UUIDUtil.CODEC);
    public static final Codec<UUID> LENIENT_CODEC = Codec.withAlternative(UUIDUtil.CODEC, UUIDUtil.STRING_CODEC);
    public static final StreamCodec<ByteBuf, UUID> STREAM_CODEC = new StreamCodec<ByteBuf, UUID>() {
        public UUID decode(ByteBuf bytebuf) {
            return PacketDataSerializer.readUUID(bytebuf);
        }

        public void encode(ByteBuf bytebuf, UUID uuid) {
            PacketDataSerializer.writeUUID(bytebuf, uuid);
        }
    };
    public static final int UUID_BYTES = 16;
    private static final String UUID_PREFIX_OFFLINE_PLAYER = "OfflinePlayer:";

    private UUIDUtil() {}

    public static UUID uuidFromIntArray(int[] aint) {
        return new UUID((long) aint[0] << 32 | (long) aint[1] & 4294967295L, (long) aint[2] << 32 | (long) aint[3] & 4294967295L);
    }

    public static int[] uuidToIntArray(UUID uuid) {
        long i = uuid.getMostSignificantBits();
        long j = uuid.getLeastSignificantBits();

        return leastMostToIntArray(i, j);
    }

    private static int[] leastMostToIntArray(long i, long j) {
        return new int[]{(int) (i >> 32), (int) i, (int) (j >> 32), (int) j};
    }

    public static byte[] uuidToByteArray(UUID uuid) {
        byte[] abyte = new byte[16];

        ByteBuffer.wrap(abyte).order(ByteOrder.BIG_ENDIAN).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
        return abyte;
    }

    public static UUID readUUID(Dynamic<?> dynamic) {
        int[] aint = dynamic.asIntStream().toArray();

        if (aint.length != 4) {
            throw new IllegalArgumentException("Could not read UUID. Expected int-array of length 4, got " + aint.length + ".");
        } else {
            return uuidFromIntArray(aint);
        }
    }

    public static UUID createOfflinePlayerUUID(String s) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + s).getBytes(StandardCharsets.UTF_8));
    }

    public static GameProfile createOfflineProfile(String s) {
        UUID uuid = createOfflinePlayerUUID(s);

        return new GameProfile(uuid, s);
    }
}
