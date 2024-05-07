package net.minecraft.gametest.framework;

public interface GameTestHarnessListener {

    void testStructureLoaded(GameTestHarnessInfo gametestharnessinfo);

    void testPassed(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner);

    void testFailed(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner);

    void testAddedForRerun(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessInfo gametestharnessinfo1, GameTestHarnessRunner gametestharnessrunner);
}
