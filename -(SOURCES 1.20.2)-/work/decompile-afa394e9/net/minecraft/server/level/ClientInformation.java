package net.minecraft.server.level;

import net.minecraft.network.PacketDataSerializer;
import net.minecraft.world.entity.EnumMainHand;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.EnumChatVisibility;

public record ClientInformation(String language, int viewDistance, EnumChatVisibility chatVisibility, boolean chatColors, int modelCustomisation, EnumMainHand mainHand, boolean textFilteringEnabled, boolean allowsListing) {

    public static final int MAX_LANGUAGE_LENGTH = 16;

    public ClientInformation(PacketDataSerializer packetdataserializer) {
        this(packetdataserializer.readUtf(16), packetdataserializer.readByte(), (EnumChatVisibility) packetdataserializer.readEnum(EnumChatVisibility.class), packetdataserializer.readBoolean(), packetdataserializer.readUnsignedByte(), (EnumMainHand) packetdataserializer.readEnum(EnumMainHand.class), packetdataserializer.readBoolean(), packetdataserializer.readBoolean());
    }

    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.language);
        packetdataserializer.writeByte(this.viewDistance);
        packetdataserializer.writeEnum(this.chatVisibility);
        packetdataserializer.writeBoolean(this.chatColors);
        packetdataserializer.writeByte(this.modelCustomisation);
        packetdataserializer.writeEnum(this.mainHand);
        packetdataserializer.writeBoolean(this.textFilteringEnabled);
        packetdataserializer.writeBoolean(this.allowsListing);
    }

    public static ClientInformation createDefault() {
        return new ClientInformation("en_us", 2, EnumChatVisibility.FULL, true, 0, EntityHuman.DEFAULT_MAIN_HAND, false, false);
    }
}
