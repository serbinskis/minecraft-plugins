package net.minecraft.gametest.framework;

import java.util.Collection;
import java.util.function.Consumer;
import net.minecraft.server.level.WorldServer;

public record GameTestHarnessBatch(String name, Collection<GameTestHarnessInfo> gameTestInfos, Consumer<WorldServer> beforeBatchFunction, Consumer<WorldServer> afterBatchFunction) {

    public static final String DEFAULT_BATCH_NAME = "defaultBatch";

    public GameTestHarnessBatch(String s, Collection<GameTestHarnessInfo> collection, Consumer<WorldServer> consumer, Consumer<WorldServer> consumer1) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("A GameTestBatch must include at least one GameTestInfo!");
        } else {
            this.name = s;
            this.gameTestInfos = collection;
            this.beforeBatchFunction = consumer;
            this.afterBatchFunction = consumer1;
        }
    }
}
