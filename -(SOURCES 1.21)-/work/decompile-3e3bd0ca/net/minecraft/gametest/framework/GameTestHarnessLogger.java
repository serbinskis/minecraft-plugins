package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import net.minecraft.SystemUtils;
import org.slf4j.Logger;

public class GameTestHarnessLogger implements GameTestHarnessITestReporter {

    private static final Logger LOGGER = LogUtils.getLogger();

    public GameTestHarnessLogger() {}

    @Override
    public void onTestFailed(GameTestHarnessInfo gametestharnessinfo) {
        String s = gametestharnessinfo.getStructureBlockPos().toShortString();

        if (gametestharnessinfo.isRequired()) {
            GameTestHarnessLogger.LOGGER.error("{} failed at {}! {}", new Object[]{gametestharnessinfo.getTestName(), s, SystemUtils.describeError(gametestharnessinfo.getError())});
        } else {
            GameTestHarnessLogger.LOGGER.warn("(optional) {} failed at {}. {}", new Object[]{gametestharnessinfo.getTestName(), s, SystemUtils.describeError(gametestharnessinfo.getError())});
        }

    }

    @Override
    public void onTestSuccess(GameTestHarnessInfo gametestharnessinfo) {}
}
