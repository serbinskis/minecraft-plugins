package net.minecraft.world.level.chunk.status;

import java.util.concurrent.CompletableFuture;
import net.minecraft.world.level.chunk.IChunkAccess;

@FunctionalInterface
public interface ToFullChunk {

    CompletableFuture<IChunkAccess> apply(IChunkAccess ichunkaccess);
}
