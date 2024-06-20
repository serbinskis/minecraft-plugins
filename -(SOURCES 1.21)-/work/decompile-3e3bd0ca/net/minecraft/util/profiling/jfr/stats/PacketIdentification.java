package net.minecraft.util.profiling.jfr.stats;

import jdk.jfr.consumer.RecordedEvent;

public record PacketIdentification(String direction, String protocolId, String packetId) {

    public static PacketIdentification from(RecordedEvent recordedevent) {
        return new PacketIdentification(recordedevent.getString("packetDirection"), recordedevent.getString("protocolId"), recordedevent.getString("packetId"));
    }
}
