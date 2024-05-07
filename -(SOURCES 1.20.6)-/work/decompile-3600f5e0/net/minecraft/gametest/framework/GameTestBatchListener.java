package net.minecraft.gametest.framework;

public interface GameTestBatchListener {

    void testBatchStarting(GameTestHarnessBatch gametestharnessbatch);

    void testBatchFinished(GameTestHarnessBatch gametestharnessbatch);
}
