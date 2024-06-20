package net.minecraft.world.entity.ai.attributes;

import com.mojang.serialization.Codec;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class AttributeBase {

    public static final Codec<Holder<AttributeBase>> CODEC = BuiltInRegistries.ATTRIBUTE.holderByNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<AttributeBase>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE);
    private final double defaultValue;
    private boolean syncable;
    private final String descriptionId;
    private AttributeBase.a sentiment;

    protected AttributeBase(String s, double d0) {
        this.sentiment = AttributeBase.a.POSITIVE;
        this.defaultValue = d0;
        this.descriptionId = s;
    }

    public double getDefaultValue() {
        return this.defaultValue;
    }

    public boolean isClientSyncable() {
        return this.syncable;
    }

    public AttributeBase setSyncable(boolean flag) {
        this.syncable = flag;
        return this;
    }

    public AttributeBase setSentiment(AttributeBase.a attributebase_a) {
        this.sentiment = attributebase_a;
        return this;
    }

    public double sanitizeValue(double d0) {
        return d0;
    }

    public String getDescriptionId() {
        return this.descriptionId;
    }

    public EnumChatFormat getStyle(boolean flag) {
        return this.sentiment.getStyle(flag);
    }

    public static enum a {

        POSITIVE, NEUTRAL, NEGATIVE;

        private a() {}

        public EnumChatFormat getStyle(boolean flag) {
            EnumChatFormat enumchatformat;

            switch (this.ordinal()) {
                case 0:
                    enumchatformat = flag ? EnumChatFormat.BLUE : EnumChatFormat.RED;
                    break;
                case 1:
                    enumchatformat = EnumChatFormat.GRAY;
                    break;
                case 2:
                    enumchatformat = flag ? EnumChatFormat.RED : EnumChatFormat.BLUE;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return enumchatformat;
        }
    }
}
