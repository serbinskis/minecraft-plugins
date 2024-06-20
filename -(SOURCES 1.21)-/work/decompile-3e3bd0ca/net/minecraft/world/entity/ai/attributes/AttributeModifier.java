package net.minecraft.world.entity.ai.attributes;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;
import org.slf4j.Logger;

public record AttributeModifier(MinecraftKey id, double amount, AttributeModifier.Operation operation) {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<AttributeModifier> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("id").forGetter(AttributeModifier::id), Codec.DOUBLE.fieldOf("amount").forGetter(AttributeModifier::amount), AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AttributeModifier::operation)).apply(instance, AttributeModifier::new);
    });
    public static final Codec<AttributeModifier> CODEC = AttributeModifier.MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, AttributeModifier> STREAM_CODEC = StreamCodec.composite(MinecraftKey.STREAM_CODEC, AttributeModifier::id, ByteBufCodecs.DOUBLE, AttributeModifier::amount, AttributeModifier.Operation.STREAM_CODEC, AttributeModifier::operation, AttributeModifier::new);

    public NBTTagCompound save() {
        DataResult<NBTBase> dataresult = AttributeModifier.CODEC.encode(this, DynamicOpsNBT.INSTANCE, new NBTTagCompound());

        return (NBTTagCompound) dataresult.getOrThrow();
    }

    @Nullable
    public static AttributeModifier load(NBTTagCompound nbttagcompound) {
        DataResult<AttributeModifier> dataresult = AttributeModifier.CODEC.parse(DynamicOpsNBT.INSTANCE, nbttagcompound);

        if (dataresult.isSuccess()) {
            return (AttributeModifier) dataresult.getOrThrow();
        } else {
            AttributeModifier.LOGGER.warn("Unable to create attribute: {}", ((Error) dataresult.error().get()).message());
            return null;
        }
    }

    public boolean is(MinecraftKey minecraftkey) {
        return minecraftkey.equals(this.id);
    }

    public static enum Operation implements INamable {

        ADD_VALUE("add_value", 0), ADD_MULTIPLIED_BASE("add_multiplied_base", 1), ADD_MULTIPLIED_TOTAL("add_multiplied_total", 2);

        public static final IntFunction<AttributeModifier.Operation> BY_ID = ByIdMap.continuous(AttributeModifier.Operation::id, values(), ByIdMap.a.ZERO);
        public static final StreamCodec<ByteBuf, AttributeModifier.Operation> STREAM_CODEC = ByteBufCodecs.idMapper(AttributeModifier.Operation.BY_ID, AttributeModifier.Operation::id);
        public static final Codec<AttributeModifier.Operation> CODEC = INamable.fromEnum(AttributeModifier.Operation::values);
        private final String name;
        private final int id;

        private Operation(final String s, final int i) {
            this.name = s;
            this.id = i;
        }

        public int id() {
            return this.id;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
