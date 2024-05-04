package net.minecraft.util.profiling.jfr.stats;

import jdk.jfr.consumer.RecordedEvent;

public record ChunkIdentification(String level, String dimension, int x, int z) {

    public static ChunkIdentification from(RecordedEvent recordedevent) {
        return new ChunkIdentification(recordedevent.getString("level"), recordedevent.getString("dimension"), recordedevent.getInt("chunkPosX"), recordedevent.getInt("chunkPosZ"));
    }
}
