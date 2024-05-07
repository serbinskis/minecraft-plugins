package net.minecraft.gametest.framework;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.List;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockLectern;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportGameListener implements GameTestHarnessListener {

    private int attempts = 0;
    private int successes = 0;

    public ReportGameListener() {}

    @Override
    public void testStructureLoaded(GameTestHarnessInfo gametestharnessinfo) {
        spawnBeacon(gametestharnessinfo, Blocks.LIGHT_GRAY_STAINED_GLASS);
        ++this.attempts;
    }

    private void handleRetry(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner, boolean flag) {
        RetryOptions retryoptions = gametestharnessinfo.retryOptions();
        String s = String.format("[Run: %4d, Ok: %4d, Fail: %4d", this.attempts, this.successes, this.attempts - this.successes);

        if (!retryoptions.unlimitedTries()) {
            s = s + String.format(", Left: %4d", retryoptions.numberOfTries() - this.attempts);
        }

        s = s + "]";
        String s1 = gametestharnessinfo.getTestName();
        String s2 = s1 + " " + (flag ? "passed" : "failed") + "! " + gametestharnessinfo.getRunTime() + "ms";
        String s3 = String.format("%-53s%s", s, s2);

        if (flag) {
            reportPassed(gametestharnessinfo, s3);
        } else {
            say(gametestharnessinfo.getLevel(), EnumChatFormat.RED, s3);
        }

        if (retryoptions.hasTriesLeft(this.attempts, this.successes)) {
            gametestharnessrunner.rerunTest(gametestharnessinfo);
        }

    }

    @Override
    public void testPassed(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner) {
        ++this.successes;
        if (gametestharnessinfo.retryOptions().hasRetries()) {
            this.handleRetry(gametestharnessinfo, gametestharnessrunner, true);
        } else {
            String s;

            if (!gametestharnessinfo.isFlaky()) {
                s = gametestharnessinfo.getTestName();
                reportPassed(gametestharnessinfo, s + " passed! (" + gametestharnessinfo.getRunTime() + "ms)");
            } else {
                if (this.successes >= gametestharnessinfo.requiredSuccesses()) {
                    s = String.valueOf(gametestharnessinfo);
                    reportPassed(gametestharnessinfo, s + " passed " + this.successes + " times of " + this.attempts + " attempts.");
                } else {
                    WorldServer worldserver = gametestharnessinfo.getLevel();
                    EnumChatFormat enumchatformat = EnumChatFormat.GREEN;
                    String s1 = String.valueOf(gametestharnessinfo);

                    say(worldserver, enumchatformat, "Flaky test " + s1 + " succeeded, attempt: " + this.attempts + " successes: " + this.successes);
                    gametestharnessrunner.rerunTest(gametestharnessinfo);
                }

            }
        }
    }

    @Override
    public void testFailed(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner) {
        if (!gametestharnessinfo.isFlaky()) {
            reportFailure(gametestharnessinfo, gametestharnessinfo.getError());
            if (gametestharnessinfo.retryOptions().hasRetries()) {
                this.handleRetry(gametestharnessinfo, gametestharnessrunner, false);
            }

        } else {
            GameTestHarnessTestFunction gametestharnesstestfunction = gametestharnessinfo.getTestFunction();
            String s = String.valueOf(gametestharnessinfo);
            String s1 = "Flaky test " + s + " failed, attempt: " + this.attempts + "/" + gametestharnesstestfunction.maxAttempts();

            if (gametestharnesstestfunction.requiredSuccesses() > 1) {
                s1 = s1 + ", successes: " + this.successes + " (" + gametestharnesstestfunction.requiredSuccesses() + " required)";
            }

            say(gametestharnessinfo.getLevel(), EnumChatFormat.YELLOW, s1);
            if (gametestharnessinfo.maxAttempts() - this.attempts + this.successes >= gametestharnessinfo.requiredSuccesses()) {
                gametestharnessrunner.rerunTest(gametestharnessinfo);
            } else {
                reportFailure(gametestharnessinfo, new ExhaustedAttemptsException(this.attempts, this.successes, gametestharnessinfo));
            }

        }
    }

    @Override
    public void testAddedForRerun(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessInfo gametestharnessinfo1, GameTestHarnessRunner gametestharnessrunner) {
        gametestharnessinfo1.addListener(this);
    }

    public static void reportPassed(GameTestHarnessInfo gametestharnessinfo, String s) {
        spawnBeacon(gametestharnessinfo, Blocks.LIME_STAINED_GLASS);
        visualizePassedTest(gametestharnessinfo, s);
    }

    private static void visualizePassedTest(GameTestHarnessInfo gametestharnessinfo, String s) {
        say(gametestharnessinfo.getLevel(), EnumChatFormat.GREEN, s);
        GlobalTestReporter.onTestSuccess(gametestharnessinfo);
    }

    protected static void reportFailure(GameTestHarnessInfo gametestharnessinfo, Throwable throwable) {
        spawnBeacon(gametestharnessinfo, gametestharnessinfo.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
        spawnLectern(gametestharnessinfo, SystemUtils.describeError(throwable));
        visualizeFailedTest(gametestharnessinfo, throwable);
    }

    protected static void visualizeFailedTest(GameTestHarnessInfo gametestharnessinfo, Throwable throwable) {
        String s = throwable.getMessage();
        String s1 = s + (throwable.getCause() == null ? "" : " cause: " + SystemUtils.describeError(throwable.getCause()));

        s = gametestharnessinfo.isRequired() ? "" : "(optional) ";
        String s2 = s + gametestharnessinfo.getTestName() + " failed! " + s1;

        say(gametestharnessinfo.getLevel(), gametestharnessinfo.isRequired() ? EnumChatFormat.RED : EnumChatFormat.YELLOW, s2);
        Throwable throwable1 = (Throwable) MoreObjects.firstNonNull(ExceptionUtils.getRootCause(throwable), throwable);

        if (throwable1 instanceof GameTestHarnessAssertionPosition gametestharnessassertionposition) {
            showRedBox(gametestharnessinfo.getLevel(), gametestharnessassertionposition.getAbsolutePos(), gametestharnessassertionposition.getMessageToShowAtBlock());
        }

        GlobalTestReporter.onTestFailed(gametestharnessinfo);
    }

    protected static void spawnBeacon(GameTestHarnessInfo gametestharnessinfo, Block block) {
        WorldServer worldserver = gametestharnessinfo.getLevel();
        BlockPosition blockposition = gametestharnessinfo.getStructureBlockPos();
        BlockPosition blockposition1 = new BlockPosition(-1, -2, -1);
        BlockPosition blockposition2 = DefinedStructure.transform(blockposition.offset(blockposition1), EnumBlockMirror.NONE, gametestharnessinfo.getRotation(), blockposition);

        worldserver.setBlockAndUpdate(blockposition2, Blocks.BEACON.defaultBlockState().rotate(gametestharnessinfo.getRotation()));
        BlockPosition blockposition3 = blockposition2.offset(0, 1, 0);

        worldserver.setBlockAndUpdate(blockposition3, block.defaultBlockState());

        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                BlockPosition blockposition4 = blockposition2.offset(i, -1, j);

                worldserver.setBlockAndUpdate(blockposition4, Blocks.IRON_BLOCK.defaultBlockState());
            }
        }

    }

    private static void spawnLectern(GameTestHarnessInfo gametestharnessinfo, String s) {
        WorldServer worldserver = gametestharnessinfo.getLevel();
        BlockPosition blockposition = gametestharnessinfo.getStructureBlockPos();
        BlockPosition blockposition1 = new BlockPosition(-1, 0, -1);
        BlockPosition blockposition2 = DefinedStructure.transform(blockposition.offset(blockposition1), EnumBlockMirror.NONE, gametestharnessinfo.getRotation(), blockposition);

        worldserver.setBlockAndUpdate(blockposition2, Blocks.LECTERN.defaultBlockState().rotate(gametestharnessinfo.getRotation()));
        IBlockData iblockdata = worldserver.getBlockState(blockposition2);
        ItemStack itemstack = createBook(gametestharnessinfo.getTestName(), gametestharnessinfo.isRequired(), s);

        BlockLectern.tryPlaceBook((Entity) null, worldserver, blockposition2, iblockdata, itemstack);
    }

    private static ItemStack createBook(String s, boolean flag, String s1) {
        StringBuffer stringbuffer = new StringBuffer();

        Arrays.stream(s.split("\\.")).forEach((s2) -> {
            stringbuffer.append(s2).append('\n');
        });
        if (!flag) {
            stringbuffer.append("(optional)\n");
        }

        stringbuffer.append("-------------------\n");
        ItemStack itemstack = new ItemStack(Items.WRITABLE_BOOK);
        DataComponentType datacomponenttype = DataComponents.WRITABLE_BOOK_CONTENT;
        String s2 = String.valueOf(stringbuffer);

        itemstack.set(datacomponenttype, new WritableBookContent(List.of(Filterable.passThrough(s2 + s1))));
        return itemstack;
    }

    protected static void say(WorldServer worldserver, EnumChatFormat enumchatformat, String s) {
        worldserver.getPlayers((entityplayer) -> {
            return true;
        }).forEach((entityplayer) -> {
            entityplayer.sendSystemMessage(IChatBaseComponent.literal(s).withStyle(enumchatformat));
        });
    }

    private static void showRedBox(WorldServer worldserver, BlockPosition blockposition, String s) {
        PacketDebug.sendGameTestAddMarker(worldserver, blockposition, s, -2130771968, Integer.MAX_VALUE);
    }
}
