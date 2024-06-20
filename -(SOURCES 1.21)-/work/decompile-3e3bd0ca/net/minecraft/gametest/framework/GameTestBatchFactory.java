package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.server.level.WorldServer;

public class GameTestBatchFactory {

    private static final int MAX_TESTS_PER_BATCH = 50;

    public GameTestBatchFactory() {}

    public static Collection<GameTestHarnessBatch> fromTestFunction(Collection<GameTestHarnessTestFunction> collection, WorldServer worldserver) {
        Map<String, List<GameTestHarnessTestFunction>> map = (Map) collection.stream().collect(Collectors.groupingBy(GameTestHarnessTestFunction::batchName));

        return map.entrySet().stream().flatMap((entry) -> {
            String s = (String) entry.getKey();
            List<GameTestHarnessTestFunction> list = (List) entry.getValue();

            return Streams.mapWithIndex(Lists.partition(list, 50).stream(), (list1, i) -> {
                return toGameTestBatch(list1.stream().map((gametestharnesstestfunction) -> {
                    return toGameTestInfo(gametestharnesstestfunction, 0, worldserver);
                }).toList(), s, i);
            });
        }).toList();
    }

    public static GameTestHarnessInfo toGameTestInfo(GameTestHarnessTestFunction gametestharnesstestfunction, int i, WorldServer worldserver) {
        return new GameTestHarnessInfo(gametestharnesstestfunction, GameTestHarnessStructures.getRotationForRotationSteps(i), worldserver, RetryOptions.noRetries());
    }

    public static GameTestHarnessRunner.b fromGameTestInfo() {
        return fromGameTestInfo(50);
    }

    public static GameTestHarnessRunner.b fromGameTestInfo(int i) {
        return (collection) -> {
            Map<String, List<GameTestHarnessInfo>> map = (Map) collection.stream().filter(Objects::nonNull).collect(Collectors.groupingBy((gametestharnessinfo) -> {
                return gametestharnessinfo.getTestFunction().batchName();
            }));

            return map.entrySet().stream().flatMap((entry) -> {
                String s = (String) entry.getKey();
                List<GameTestHarnessInfo> list = (List) entry.getValue();

                return Streams.mapWithIndex(Lists.partition(list, i).stream(), (list1, j) -> {
                    return toGameTestBatch(List.copyOf(list1), s, j);
                });
            }).toList();
        };
    }

    public static GameTestHarnessBatch toGameTestBatch(Collection<GameTestHarnessInfo> collection, String s, long i) {
        Consumer<WorldServer> consumer = GameTestHarnessRegistry.getBeforeBatchFunction(s);
        Consumer<WorldServer> consumer1 = GameTestHarnessRegistry.getAfterBatchFunction(s);

        return new GameTestHarnessBatch(s + ":" + i, collection, consumer, consumer1);
    }
}
