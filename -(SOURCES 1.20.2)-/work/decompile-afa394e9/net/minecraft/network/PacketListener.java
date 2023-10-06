package net.minecraft.network;

import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;

public interface PacketListener {

    EnumProtocolDirection flow();

    EnumProtocol protocol();

    void onDisconnect(IChatBaseComponent ichatbasecomponent);

    boolean isAcceptingMessages();

    default boolean shouldHandleMessage(Packet<?> packet) {
        return this.isAcceptingMessages();
    }

    default boolean shouldPropagateHandlingExceptions() {
        return true;
    }
}
