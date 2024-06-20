package net.minecraft.server.level.progress;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.slf4j.Logger;

public class WorldLoadListenerLogger implements WorldLoadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final int maxCount;
    private int count;
    private long startTime;
    private long nextTickTime = Long.MAX_VALUE;

    private WorldLoadListenerLogger(int i) {
        this.maxCount = i;
    }

    public static WorldLoadListenerLogger createFromGameruleRadius(int i) {
        return i > 0 ? create(i + 1) : createCompleted();
    }

    public static WorldLoadListenerLogger create(int i) {
        int j = WorldLoadListener.calculateDiameter(i);

        return new WorldLoadListenerLogger(j * j);
    }

    public static WorldLoadListenerLogger createCompleted() {
        return new WorldLoadListenerLogger(0);
    }

    @Override
    public void updateSpawnPos(ChunkCoordIntPair chunkcoordintpair) {
        this.nextTickTime = SystemUtils.getMillis();
        this.startTime = this.nextTickTime;
    }

    @Override
    public void onStatusChange(ChunkCoordIntPair chunkcoordintpair, @Nullable ChunkStatus chunkstatus) {
        if (chunkstatus == ChunkStatus.FULL) {
            ++this.count;
        }

        int i = this.getProgress();

        if (SystemUtils.getMillis() > this.nextTickTime) {
            this.nextTickTime += 500L;
            WorldLoadListenerLogger.LOGGER.info(IChatBaseComponent.translatable("menu.preparingSpawn", MathHelper.clamp(i, 0, 100)).getString());
        }

    }

    @Override
    public void start() {}

    @Override
    public void stop() {
        WorldLoadListenerLogger.LOGGER.info("Time elapsed: {} ms", SystemUtils.getMillis() - this.startTime);
        this.nextTickTime = Long.MAX_VALUE;
    }

    public int getProgress() {
        return this.maxCount == 0 ? 100 : MathHelper.floor((float) this.count * 100.0F / (float) this.maxCount);
    }
}
