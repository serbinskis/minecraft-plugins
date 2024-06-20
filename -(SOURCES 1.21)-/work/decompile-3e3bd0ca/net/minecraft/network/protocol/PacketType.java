package net.minecraft.network.protocol;

import net.minecraft.resources.MinecraftKey;

public record PacketType<T extends Packet<?>>(EnumProtocolDirection flow, MinecraftKey id) {

    public String toString() {
        String s = this.flow.id();

        return s + "/" + String.valueOf(this.id);
    }
}
