package net.minecraft.gametest.framework;

import java.util.function.Consumer;
import net.minecraft.world.level.block.EnumBlockRotation;

public record GameTestHarnessTestFunction(String batchName, String testName, String structureName, EnumBlockRotation rotation, int maxTicks, long setupTicks, boolean required, boolean manualOnly, int maxAttempts, int requiredSuccesses, boolean skyAccess, Consumer<GameTestHarnessHelper> function) {

    public GameTestHarnessTestFunction(String s, String s1, String s2, int i, long j, boolean flag, Consumer<GameTestHarnessHelper> consumer) {
        this(s, s1, s2, EnumBlockRotation.NONE, i, j, flag, false, 1, 1, false, consumer);
    }

    public GameTestHarnessTestFunction(String s, String s1, String s2, EnumBlockRotation enumblockrotation, int i, long j, boolean flag, Consumer<GameTestHarnessHelper> consumer) {
        this(s, s1, s2, enumblockrotation, i, j, flag, false, 1, 1, false, consumer);
    }

    public void run(GameTestHarnessHelper gametestharnesshelper) {
        this.function.accept(gametestharnesshelper);
    }

    public String toString() {
        return this.testName;
    }

    public boolean isFlaky() {
        return this.maxAttempts > 1;
    }
}
