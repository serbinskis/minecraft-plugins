package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifiable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class PacketPlayOutUpdateAttributes implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutUpdateAttributes> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, PacketPlayOutUpdateAttributes::getEntityId, PacketPlayOutUpdateAttributes.AttributeSnapshot.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketPlayOutUpdateAttributes::getValues, PacketPlayOutUpdateAttributes::new);
    private final int entityId;
    private final List<PacketPlayOutUpdateAttributes.AttributeSnapshot> attributes;

    public PacketPlayOutUpdateAttributes(int i, Collection<AttributeModifiable> collection) {
        this.entityId = i;
        this.attributes = Lists.newArrayList();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            AttributeModifiable attributemodifiable = (AttributeModifiable) iterator.next();

            this.attributes.add(new PacketPlayOutUpdateAttributes.AttributeSnapshot(attributemodifiable.getAttribute(), attributemodifiable.getBaseValue(), attributemodifiable.getModifiers()));
        }

    }

    private PacketPlayOutUpdateAttributes(int i, List<PacketPlayOutUpdateAttributes.AttributeSnapshot> list) {
        this.entityId = i;
        this.attributes = list;
    }

    @Override
    public PacketType<PacketPlayOutUpdateAttributes> type() {
        return GamePacketTypes.CLIENTBOUND_UPDATE_ATTRIBUTES;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleUpdateAttributes(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public List<PacketPlayOutUpdateAttributes.AttributeSnapshot> getValues() {
        return this.attributes;
    }

    public static record AttributeSnapshot(Holder<AttributeBase> attribute, double base, Collection<AttributeModifier> modifiers) {

        public static final StreamCodec<ByteBuf, AttributeModifier> MODIFIER_STREAM_CODEC = StreamCodec.composite(MinecraftKey.STREAM_CODEC, AttributeModifier::id, ByteBufCodecs.DOUBLE, AttributeModifier::amount, AttributeModifier.Operation.STREAM_CODEC, AttributeModifier::operation, AttributeModifier::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutUpdateAttributes.AttributeSnapshot> STREAM_CODEC = StreamCodec.composite(AttributeBase.STREAM_CODEC, PacketPlayOutUpdateAttributes.AttributeSnapshot::attribute, ByteBufCodecs.DOUBLE, PacketPlayOutUpdateAttributes.AttributeSnapshot::base, PacketPlayOutUpdateAttributes.AttributeSnapshot.MODIFIER_STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), PacketPlayOutUpdateAttributes.AttributeSnapshot::modifiers, PacketPlayOutUpdateAttributes.AttributeSnapshot::new);
    }
}
