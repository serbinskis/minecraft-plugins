package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;

public class GameTestHarnessTicker {

    public static final GameTestHarnessTicker SINGLETON = new GameTestHarnessTicker();
    private final Collection<GameTestHarnessInfo> testInfos = Lists.newCopyOnWriteArrayList();
    @Nullable
    private GameTestHarnessRunner runner;

    private GameTestHarnessTicker() {}

    public void add(GameTestHarnessInfo gametestharnessinfo) {
        this.testInfos.add(gametestharnessinfo);
    }

    public void clear() {
        this.testInfos.clear();
        if (this.runner != null) {
            this.runner.stop();
            this.runner = null;
        }

    }

    public void setRunner(GameTestHarnessRunner gametestharnessrunner) {
        if (this.runner != null) {
            SystemUtils.logAndPauseIfInIde("The runner was already set in GameTestTicker");
        }

        this.runner = gametestharnessrunner;
    }

    public void tick() {
        if (this.runner != null) {
            this.testInfos.forEach((gametestharnessinfo) -> {
                gametestharnessinfo.tick(this.runner);
            });
            this.testInfos.removeIf(GameTestHarnessInfo::isDone);
        }
    }
}
