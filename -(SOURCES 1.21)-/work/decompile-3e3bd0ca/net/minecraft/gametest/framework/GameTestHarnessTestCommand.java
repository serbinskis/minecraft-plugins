package net.minecraft.gametest.framework;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.EnumChatFormat;
import net.minecraft.FileUtils;
import net.minecraft.commands.CommandDispatcher;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.DebugReportNBT;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.network.chat.ChatClickable;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TileEntityStructure;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class GameTestHarnessTestCommand {

    public static final int STRUCTURE_BLOCK_NEARBY_SEARCH_RADIUS = 15;
    public static final int STRUCTURE_BLOCK_FULL_SEARCH_RADIUS = 200;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_CLEAR_RADIUS = 200;
    private static final int MAX_CLEAR_RADIUS = 1024;
    private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
    private static final int SHOW_POS_DURATION_MS = 10000;
    private static final int DEFAULT_X_SIZE = 5;
    private static final int DEFAULT_Y_SIZE = 5;
    private static final int DEFAULT_Z_SIZE = 5;
    private static final String STRUCTURE_BLOCK_ENTITY_COULD_NOT_BE_FOUND = "Structure block entity could not be found";
    private static final TestFinder.a<GameTestHarnessTestCommand.a> testFinder = new TestFinder.a<>(GameTestHarnessTestCommand.a::new);

    public GameTestHarnessTestCommand() {}

    private static ArgumentBuilder<CommandListenerWrapper, ?> runWithRetryOptions(ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder, Function<CommandContext<CommandListenerWrapper>, GameTestHarnessTestCommand.a> function, Function<ArgumentBuilder<CommandListenerWrapper, ?>, ArgumentBuilder<CommandListenerWrapper, ?>> function1) {
        return argumentbuilder.executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) function.apply(commandcontext)).run();
        }).then(((RequiredArgumentBuilder) CommandDispatcher.argument("numberOfTimes", IntegerArgumentType.integer(0)).executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) function.apply(commandcontext)).run(new RetryOptions(IntegerArgumentType.getInteger(commandcontext, "numberOfTimes"), false));
        })).then((ArgumentBuilder) function1.apply(CommandDispatcher.argument("untilFailed", BoolArgumentType.bool()).executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) function.apply(commandcontext)).run(new RetryOptions(IntegerArgumentType.getInteger(commandcontext, "numberOfTimes"), BoolArgumentType.getBool(commandcontext, "untilFailed")));
        }))));
    }

    private static ArgumentBuilder<CommandListenerWrapper, ?> runWithRetryOptions(ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder, Function<CommandContext<CommandListenerWrapper>, GameTestHarnessTestCommand.a> function) {
        return runWithRetryOptions(argumentbuilder, function, (argumentbuilder1) -> {
            return argumentbuilder1;
        });
    }

    private static ArgumentBuilder<CommandListenerWrapper, ?> runWithRetryOptionsAndBuildInfo(ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder, Function<CommandContext<CommandListenerWrapper>, GameTestHarnessTestCommand.a> function) {
        return runWithRetryOptions(argumentbuilder, function, (argumentbuilder1) -> {
            return argumentbuilder1.then(((RequiredArgumentBuilder) CommandDispatcher.argument("rotationSteps", IntegerArgumentType.integer()).executes((commandcontext) -> {
                return ((GameTestHarnessTestCommand.a) function.apply(commandcontext)).run(new RetryOptions(IntegerArgumentType.getInteger(commandcontext, "numberOfTimes"), BoolArgumentType.getBool(commandcontext, "untilFailed")), IntegerArgumentType.getInteger(commandcontext, "rotationSteps"));
            })).then(CommandDispatcher.argument("testsPerRow", IntegerArgumentType.integer()).executes((commandcontext) -> {
                return ((GameTestHarnessTestCommand.a) function.apply(commandcontext)).run(new RetryOptions(IntegerArgumentType.getInteger(commandcontext, "numberOfTimes"), BoolArgumentType.getBool(commandcontext, "untilFailed")), IntegerArgumentType.getInteger(commandcontext, "rotationSteps"), IntegerArgumentType.getInteger(commandcontext, "testsPerRow"));
            })));
        });
    }

    public static void register(com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> com_mojang_brigadier_commanddispatcher) {
        ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder = runWithRetryOptionsAndBuildInfo(CommandDispatcher.argument("onlyRequiredTests", BoolArgumentType.bool()), (commandcontext) -> {
            return (GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.failedTests(commandcontext, BoolArgumentType.getBool(commandcontext, "onlyRequiredTests"));
        });
        ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder1 = runWithRetryOptionsAndBuildInfo(CommandDispatcher.argument("testClassName", GameTestHarnessTestClassArgument.testClassName()), (commandcontext) -> {
            return (GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.allTestsInClass(commandcontext, GameTestHarnessTestClassArgument.getTestClassName(commandcontext, "testClassName"));
        });
        LiteralArgumentBuilder literalargumentbuilder = (LiteralArgumentBuilder) ((LiteralArgumentBuilder) CommandDispatcher.literal("test").then(CommandDispatcher.literal("run").then(runWithRetryOptionsAndBuildInfo(CommandDispatcher.argument("testName", GameTestHarnessTestFunctionArgument.testFunctionArgument()), (commandcontext) -> {
            return (GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.byArgument(commandcontext, "testName");
        })))).then(CommandDispatcher.literal("runmultiple").then(((RequiredArgumentBuilder) CommandDispatcher.argument("testName", GameTestHarnessTestFunctionArgument.testFunctionArgument()).executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.byArgument(commandcontext, "testName")).run();
        })).then(CommandDispatcher.argument("amount", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.createMultipleCopies(IntegerArgumentType.getInteger(commandcontext, "amount")).byArgument(commandcontext, "testName")).run();
        }))));
        ArgumentBuilder argumentbuilder2 = CommandDispatcher.literal("runall").then(argumentbuilder1);
        TestFinder.a testfinder_a = GameTestHarnessTestCommand.testFinder;

        Objects.requireNonNull(testfinder_a);
        literalargumentbuilder = (LiteralArgumentBuilder) literalargumentbuilder.then(runWithRetryOptionsAndBuildInfo(argumentbuilder2, testfinder_a::allTests));
        LiteralArgumentBuilder literalargumentbuilder1 = CommandDispatcher.literal("runthese");

        testfinder_a = GameTestHarnessTestCommand.testFinder;
        Objects.requireNonNull(testfinder_a);
        literalargumentbuilder = (LiteralArgumentBuilder) literalargumentbuilder.then(runWithRetryOptions(literalargumentbuilder1, testfinder_a::allNearby));
        literalargumentbuilder1 = CommandDispatcher.literal("runclosest");
        testfinder_a = GameTestHarnessTestCommand.testFinder;
        Objects.requireNonNull(testfinder_a);
        literalargumentbuilder = (LiteralArgumentBuilder) literalargumentbuilder.then(runWithRetryOptions(literalargumentbuilder1, testfinder_a::nearest));
        literalargumentbuilder1 = CommandDispatcher.literal("runthat");
        testfinder_a = GameTestHarnessTestCommand.testFinder;
        Objects.requireNonNull(testfinder_a);
        literalargumentbuilder = (LiteralArgumentBuilder) literalargumentbuilder.then(runWithRetryOptions(literalargumentbuilder1, testfinder_a::lookedAt));
        argumentbuilder2 = CommandDispatcher.literal("runfailed").then(argumentbuilder);
        testfinder_a = GameTestHarnessTestCommand.testFinder;
        Objects.requireNonNull(testfinder_a);
        com_mojang_brigadier_commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) literalargumentbuilder.then(runWithRetryOptionsAndBuildInfo(argumentbuilder2, testfinder_a::failedTests))).then(CommandDispatcher.literal("verify").then(CommandDispatcher.argument("testName", GameTestHarnessTestFunctionArgument.testFunctionArgument()).executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.byArgument(commandcontext, "testName")).verify();
        })))).then(CommandDispatcher.literal("verifyclass").then(CommandDispatcher.argument("testClassName", GameTestHarnessTestClassArgument.testClassName()).executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.allTestsInClass(commandcontext, GameTestHarnessTestClassArgument.getTestClassName(commandcontext, "testClassName"))).verify();
        })))).then(CommandDispatcher.literal("locate").then(CommandDispatcher.argument("testName", GameTestHarnessTestFunctionArgument.testFunctionArgument()).executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.locateByName(commandcontext, "minecraft:" + GameTestHarnessTestFunctionArgument.getTestFunction(commandcontext, "testName").structureName())).locate();
        })))).then(CommandDispatcher.literal("resetclosest").executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.nearest(commandcontext)).reset();
        }))).then(CommandDispatcher.literal("resetthese").executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.allNearby(commandcontext)).reset();
        }))).then(CommandDispatcher.literal("resetthat").executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.lookedAt(commandcontext)).reset();
        }))).then(CommandDispatcher.literal("export").then(CommandDispatcher.argument("testName", StringArgumentType.word()).executes((commandcontext) -> {
            return exportTestStructure((CommandListenerWrapper) commandcontext.getSource(), "minecraft:" + StringArgumentType.getString(commandcontext, "testName"));
        })))).then(CommandDispatcher.literal("exportclosest").executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.nearest(commandcontext)).export();
        }))).then(CommandDispatcher.literal("exportthese").executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.allNearby(commandcontext)).export();
        }))).then(CommandDispatcher.literal("exportthat").executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.lookedAt(commandcontext)).export();
        }))).then(CommandDispatcher.literal("clearthat").executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.lookedAt(commandcontext)).clear();
        }))).then(CommandDispatcher.literal("clearthese").executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.allNearby(commandcontext)).clear();
        }))).then(((LiteralArgumentBuilder) CommandDispatcher.literal("clearall").executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.radius(commandcontext, 200)).clear();
        })).then(CommandDispatcher.argument("radius", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return ((GameTestHarnessTestCommand.a) GameTestHarnessTestCommand.testFinder.radius(commandcontext, MathHelper.clamp(IntegerArgumentType.getInteger(commandcontext, "radius"), 0, 1024))).clear();
        })))).then(CommandDispatcher.literal("import").then(CommandDispatcher.argument("testName", StringArgumentType.word()).executes((commandcontext) -> {
            return importTestStructure((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "testName"));
        })))).then(CommandDispatcher.literal("stop").executes((commandcontext) -> {
            return stopTests();
        }))).then(((LiteralArgumentBuilder) CommandDispatcher.literal("pos").executes((commandcontext) -> {
            return showPos((CommandListenerWrapper) commandcontext.getSource(), "pos");
        })).then(CommandDispatcher.argument("var", StringArgumentType.word()).executes((commandcontext) -> {
            return showPos((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "var"));
        })))).then(CommandDispatcher.literal("create").then(((RequiredArgumentBuilder) CommandDispatcher.argument("testName", StringArgumentType.word()).suggests(GameTestHarnessTestFunctionArgument::suggestTestFunction).executes((commandcontext) -> {
            return createNewStructure((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "testName"), 5, 5, 5);
        })).then(((RequiredArgumentBuilder) CommandDispatcher.argument("width", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return createNewStructure((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "testName"), IntegerArgumentType.getInteger(commandcontext, "width"), IntegerArgumentType.getInteger(commandcontext, "width"), IntegerArgumentType.getInteger(commandcontext, "width"));
        })).then(CommandDispatcher.argument("height", IntegerArgumentType.integer()).then(CommandDispatcher.argument("depth", IntegerArgumentType.integer()).executes((commandcontext) -> {
            return createNewStructure((CommandListenerWrapper) commandcontext.getSource(), StringArgumentType.getString(commandcontext, "testName"), IntegerArgumentType.getInteger(commandcontext, "width"), IntegerArgumentType.getInteger(commandcontext, "height"), IntegerArgumentType.getInteger(commandcontext, "depth"));
        })))))));
    }

    private static int resetGameTestInfo(GameTestHarnessInfo gametestharnessinfo) {
        gametestharnessinfo.getLevel().getEntities((Entity) null, gametestharnessinfo.getStructureBounds()).stream().forEach((entity) -> {
            entity.remove(Entity.RemovalReason.DISCARDED);
        });
        gametestharnessinfo.getStructureBlockEntity().placeStructure(gametestharnessinfo.getLevel());
        GameTestHarnessStructures.removeBarriers(gametestharnessinfo.getStructureBounds(), gametestharnessinfo.getLevel());
        say(gametestharnessinfo.getLevel(), "Reset succeded for: " + gametestharnessinfo.getTestName(), EnumChatFormat.GREEN);
        return 1;
    }

    static Stream<GameTestHarnessInfo> toGameTestInfos(CommandListenerWrapper commandlistenerwrapper, RetryOptions retryoptions, StructureBlockPosFinder structureblockposfinder) {
        return structureblockposfinder.findStructureBlockPos().map((blockposition) -> {
            return createGameTestInfo(blockposition, commandlistenerwrapper.getLevel(), retryoptions);
        }).flatMap(Optional::stream);
    }

    static Stream<GameTestHarnessInfo> toGameTestInfo(CommandListenerWrapper commandlistenerwrapper, RetryOptions retryoptions, TestFunctionFinder testfunctionfinder, int i) {
        return testfunctionfinder.findTestFunctions().filter((gametestharnesstestfunction) -> {
            return verifyStructureExists(commandlistenerwrapper.getLevel(), gametestharnesstestfunction.structureName());
        }).map((gametestharnesstestfunction) -> {
            return new GameTestHarnessInfo(gametestharnesstestfunction, GameTestHarnessStructures.getRotationForRotationSteps(i), commandlistenerwrapper.getLevel(), retryoptions);
        });
    }

    private static Optional<GameTestHarnessInfo> createGameTestInfo(BlockPosition blockposition, WorldServer worldserver, RetryOptions retryoptions) {
        TileEntityStructure tileentitystructure = (TileEntityStructure) worldserver.getBlockEntity(blockposition);

        if (tileentitystructure == null) {
            say(worldserver, "Structure block entity could not be found", EnumChatFormat.RED);
            return Optional.empty();
        } else {
            String s = tileentitystructure.getMetaData();
            Optional<GameTestHarnessTestFunction> optional = GameTestHarnessRegistry.findTestFunction(s);

            if (optional.isEmpty()) {
                say(worldserver, "Test function for test " + s + " could not be found", EnumChatFormat.RED);
                return Optional.empty();
            } else {
                GameTestHarnessTestFunction gametestharnesstestfunction = (GameTestHarnessTestFunction) optional.get();
                GameTestHarnessInfo gametestharnessinfo = new GameTestHarnessInfo(gametestharnesstestfunction, tileentitystructure.getRotation(), worldserver, retryoptions);

                gametestharnessinfo.setStructureBlockPos(blockposition);
                return !verifyStructureExists(worldserver, gametestharnessinfo.getStructureName()) ? Optional.empty() : Optional.of(gametestharnessinfo);
            }
        }
    }

    private static int createNewStructure(CommandListenerWrapper commandlistenerwrapper, String s, int i, int j, int k) {
        if (i <= 48 && j <= 48 && k <= 48) {
            WorldServer worldserver = commandlistenerwrapper.getLevel();
            BlockPosition blockposition = createTestPositionAround(commandlistenerwrapper).below();

            GameTestHarnessStructures.createNewEmptyStructureBlock(s.toLowerCase(), blockposition, new BaseBlockPosition(i, j, k), EnumBlockRotation.NONE, worldserver);
            BlockPosition blockposition1 = blockposition.above();
            BlockPosition blockposition2 = blockposition1.offset(i - 1, 0, k - 1);

            BlockPosition.betweenClosedStream(blockposition1, blockposition2).forEach((blockposition3) -> {
                worldserver.setBlockAndUpdate(blockposition3, Blocks.BEDROCK.defaultBlockState());
            });
            GameTestHarnessStructures.addCommandBlockAndButtonToStartTest(blockposition, new BlockPosition(1, 0, -1), EnumBlockRotation.NONE, worldserver);
            return 0;
        } else {
            throw new IllegalArgumentException("The structure must be less than 48 blocks big in each axis");
        }
    }

    private static int showPos(CommandListenerWrapper commandlistenerwrapper, String s) throws CommandSyntaxException {
        MovingObjectPositionBlock movingobjectpositionblock = (MovingObjectPositionBlock) commandlistenerwrapper.getPlayerOrException().pick(10.0D, 1.0F, false);
        BlockPosition blockposition = movingobjectpositionblock.getBlockPos();
        WorldServer worldserver = commandlistenerwrapper.getLevel();
        Optional<BlockPosition> optional = GameTestHarnessStructures.findStructureBlockContainingPos(blockposition, 15, worldserver);

        if (optional.isEmpty()) {
            optional = GameTestHarnessStructures.findStructureBlockContainingPos(blockposition, 200, worldserver);
        }

        if (optional.isEmpty()) {
            commandlistenerwrapper.sendFailure(IChatBaseComponent.literal("Can't find a structure block that contains the targeted pos " + String.valueOf(blockposition)));
            return 0;
        } else {
            TileEntityStructure tileentitystructure = (TileEntityStructure) worldserver.getBlockEntity((BlockPosition) optional.get());

            if (tileentitystructure == null) {
                say(worldserver, "Structure block entity could not be found", EnumChatFormat.RED);
                return 0;
            } else {
                BlockPosition blockposition1 = blockposition.subtract((BaseBlockPosition) optional.get());
                int i = blockposition1.getX();
                String s1 = "" + i + ", " + blockposition1.getY() + ", " + blockposition1.getZ();
                String s2 = tileentitystructure.getMetaData();
                IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.literal(s1).setStyle(ChatModifier.EMPTY.withBold(true).withColor(EnumChatFormat.GREEN).withHoverEvent(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, IChatBaseComponent.literal("Click to copy to clipboard"))).withClickEvent(new ChatClickable(ChatClickable.EnumClickAction.COPY_TO_CLIPBOARD, "final BlockPos " + s + " = new BlockPos(" + s1 + ");")));

                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.literal("Position relative to " + s2 + ": ").append(ichatmutablecomponent);
                }, false);
                PacketDebug.sendGameTestAddMarker(worldserver, new BlockPosition(blockposition), s1, -2147418368, 10000);
                return 1;
            }
        }
    }

    static int stopTests() {
        GameTestHarnessTicker.SINGLETON.clear();
        return 1;
    }

    static int trackAndStartRunner(CommandListenerWrapper commandlistenerwrapper, WorldServer worldserver, GameTestHarnessRunner gametestharnessrunner) {
        gametestharnessrunner.addListener(new GameTestHarnessTestCommand.b(commandlistenerwrapper));
        GameTestHarnessCollector gametestharnesscollector = new GameTestHarnessCollector(gametestharnessrunner.getTestInfos());

        gametestharnesscollector.addListener(new GameTestHarnessTestCommand.c(worldserver, gametestharnesscollector));
        gametestharnesscollector.addFailureListener((gametestharnessinfo) -> {
            GameTestHarnessRegistry.rememberFailedTest(gametestharnessinfo.getTestFunction());
        });
        gametestharnessrunner.start();
        return 1;
    }

    static int saveAndExportTestStructure(CommandListenerWrapper commandlistenerwrapper, TileEntityStructure tileentitystructure) {
        String s = tileentitystructure.getStructureName();

        if (!tileentitystructure.saveStructure(true)) {
            say(commandlistenerwrapper, "Failed to save structure " + s);
        }

        return exportTestStructure(commandlistenerwrapper, s);
    }

    private static int exportTestStructure(CommandListenerWrapper commandlistenerwrapper, String s) {
        Path path = Paths.get(GameTestHarnessStructures.testStructuresDir);
        MinecraftKey minecraftkey = MinecraftKey.parse(s);
        Path path1 = commandlistenerwrapper.getLevel().getStructureManager().createAndValidatePathToGeneratedStructure(minecraftkey, ".nbt");
        Path path2 = DebugReportNBT.convertStructure(CachedOutput.NO_CACHE, path1, minecraftkey.getPath(), path);

        if (path2 == null) {
            say(commandlistenerwrapper, "Failed to export " + String.valueOf(path1));
            return 1;
        } else {
            try {
                FileUtils.createDirectoriesSafe(path2.getParent());
            } catch (IOException ioexception) {
                say(commandlistenerwrapper, "Could not create folder " + String.valueOf(path2.getParent()));
                GameTestHarnessTestCommand.LOGGER.error("Could not create export folder", ioexception);
                return 1;
            }

            say(commandlistenerwrapper, "Exported " + s + " to " + String.valueOf(path2.toAbsolutePath()));
            return 0;
        }
    }

    private static boolean verifyStructureExists(WorldServer worldserver, String s) {
        if (worldserver.getStructureManager().get(MinecraftKey.parse(s)).isEmpty()) {
            say(worldserver, "Test structure " + s + " could not be found", EnumChatFormat.RED);
            return false;
        } else {
            return true;
        }
    }

    static BlockPosition createTestPositionAround(CommandListenerWrapper commandlistenerwrapper) {
        BlockPosition blockposition = BlockPosition.containing(commandlistenerwrapper.getPosition());
        int i = commandlistenerwrapper.getLevel().getHeightmapPos(HeightMap.Type.WORLD_SURFACE, blockposition).getY();

        return new BlockPosition(blockposition.getX(), i + 1, blockposition.getZ() + 3);
    }

    static void say(CommandListenerWrapper commandlistenerwrapper, String s) {
        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.literal(s);
        }, false);
    }

    private static int importTestStructure(CommandListenerWrapper commandlistenerwrapper, String s) {
        Path path = Paths.get(GameTestHarnessStructures.testStructuresDir, s + ".snbt");
        MinecraftKey minecraftkey = MinecraftKey.withDefaultNamespace(s);
        Path path1 = commandlistenerwrapper.getLevel().getStructureManager().createAndValidatePathToGeneratedStructure(minecraftkey, ".nbt");

        try {
            BufferedReader bufferedreader = Files.newBufferedReader(path);
            String s1 = IOUtils.toString(bufferedreader);

            Files.createDirectories(path1.getParent());
            OutputStream outputstream = Files.newOutputStream(path1);

            try {
                NBTCompressedStreamTools.writeCompressed(GameProfileSerializer.snbtToStructure(s1), outputstream);
            } catch (Throwable throwable) {
                if (outputstream != null) {
                    try {
                        outputstream.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                }

                throw throwable;
            }

            if (outputstream != null) {
                outputstream.close();
            }

            commandlistenerwrapper.getLevel().getStructureManager().remove(minecraftkey);
            say(commandlistenerwrapper, "Imported to " + String.valueOf(path1.toAbsolutePath()));
            return 0;
        } catch (CommandSyntaxException | IOException ioexception) {
            GameTestHarnessTestCommand.LOGGER.error("Failed to load structure {}", s, ioexception);
            return 1;
        }
    }

    static void say(WorldServer worldserver, String s, EnumChatFormat enumchatformat) {
        worldserver.getPlayers((entityplayer) -> {
            return true;
        }).forEach((entityplayer) -> {
            entityplayer.sendSystemMessage(IChatBaseComponent.literal(s).withStyle(enumchatformat));
        });
    }

    private static record b(CommandListenerWrapper source) implements GameTestBatchListener {

        @Override
        public void testBatchStarting(GameTestHarnessBatch gametestharnessbatch) {
            GameTestHarnessTestCommand.say(this.source, "Starting batch: " + gametestharnessbatch.name());
        }

        @Override
        public void testBatchFinished(GameTestHarnessBatch gametestharnessbatch) {}
    }

    public static record c(WorldServer level, GameTestHarnessCollector tracker) implements GameTestHarnessListener {

        @Override
        public void testStructureLoaded(GameTestHarnessInfo gametestharnessinfo) {}

        @Override
        public void testPassed(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner) {
            showTestSummaryIfAllDone(this.level, this.tracker);
        }

        @Override
        public void testFailed(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessRunner gametestharnessrunner) {
            showTestSummaryIfAllDone(this.level, this.tracker);
        }

        @Override
        public void testAddedForRerun(GameTestHarnessInfo gametestharnessinfo, GameTestHarnessInfo gametestharnessinfo1, GameTestHarnessRunner gametestharnessrunner) {
            this.tracker.addTestToTrack(gametestharnessinfo1);
        }

        private static void showTestSummaryIfAllDone(WorldServer worldserver, GameTestHarnessCollector gametestharnesscollector) {
            if (gametestharnesscollector.isDone()) {
                GameTestHarnessTestCommand.say(worldserver, "GameTest done! " + gametestharnesscollector.getTotalCount() + " tests were run", EnumChatFormat.WHITE);
                if (gametestharnesscollector.hasFailedRequired()) {
                    GameTestHarnessTestCommand.say(worldserver, gametestharnesscollector.getFailedRequiredCount() + " required tests failed :(", EnumChatFormat.RED);
                } else {
                    GameTestHarnessTestCommand.say(worldserver, "All required tests passed :)", EnumChatFormat.GREEN);
                }

                if (gametestharnesscollector.hasFailedOptional()) {
                    GameTestHarnessTestCommand.say(worldserver, gametestharnesscollector.getFailedOptionalCount() + " optional tests failed", EnumChatFormat.GRAY);
                }
            }

        }
    }

    public static class a {

        private final TestFinder<GameTestHarnessTestCommand.a> finder;

        public a(TestFinder<GameTestHarnessTestCommand.a> testfinder) {
            this.finder = testfinder;
        }

        public int reset() {
            GameTestHarnessTestCommand.stopTests();
            return GameTestHarnessTestCommand.toGameTestInfos(this.finder.source(), RetryOptions.noRetries(), this.finder).map(GameTestHarnessTestCommand::resetGameTestInfo).toList().isEmpty() ? 0 : 1;
        }

        private <T> void logAndRun(Stream<T> stream, ToIntFunction<T> tointfunction, Runnable runnable, Consumer<Integer> consumer) {
            int i = stream.mapToInt(tointfunction).sum();

            if (i == 0) {
                runnable.run();
            } else {
                consumer.accept(i);
            }

        }

        public int clear() {
            GameTestHarnessTestCommand.stopTests();
            CommandListenerWrapper commandlistenerwrapper = this.finder.source();
            WorldServer worldserver = commandlistenerwrapper.getLevel();

            GameTestHarnessRunner.clearMarkers(worldserver);
            this.logAndRun(this.finder.findStructureBlockPos(), (blockposition) -> {
                TileEntityStructure tileentitystructure = (TileEntityStructure) worldserver.getBlockEntity(blockposition);

                if (tileentitystructure == null) {
                    return 0;
                } else {
                    StructureBoundingBox structureboundingbox = GameTestHarnessStructures.getStructureBoundingBox(tileentitystructure);

                    GameTestHarnessStructures.clearSpaceForStructure(structureboundingbox, worldserver);
                    return 1;
                }
            }, () -> {
                GameTestHarnessTestCommand.say(worldserver, "Could not find any structures to clear", EnumChatFormat.RED);
            }, (integer) -> {
                GameTestHarnessTestCommand.say(commandlistenerwrapper, "Cleared " + integer + " structures");
            });
            return 1;
        }

        public int export() {
            MutableBoolean mutableboolean = new MutableBoolean(true);
            CommandListenerWrapper commandlistenerwrapper = this.finder.source();
            WorldServer worldserver = commandlistenerwrapper.getLevel();

            this.logAndRun(this.finder.findStructureBlockPos(), (blockposition) -> {
                TileEntityStructure tileentitystructure = (TileEntityStructure) worldserver.getBlockEntity(blockposition);

                if (tileentitystructure == null) {
                    GameTestHarnessTestCommand.say(worldserver, "Structure block entity could not be found", EnumChatFormat.RED);
                    mutableboolean.setFalse();
                    return 0;
                } else {
                    if (GameTestHarnessTestCommand.saveAndExportTestStructure(commandlistenerwrapper, tileentitystructure) != 0) {
                        mutableboolean.setFalse();
                    }

                    return 1;
                }
            }, () -> {
                GameTestHarnessTestCommand.say(worldserver, "Could not find any structures to export", EnumChatFormat.RED);
            }, (integer) -> {
                GameTestHarnessTestCommand.say(commandlistenerwrapper, "Exported " + integer + " structures");
            });
            return mutableboolean.getValue() ? 0 : 1;
        }

        int verify() {
            GameTestHarnessTestCommand.stopTests();
            CommandListenerWrapper commandlistenerwrapper = this.finder.source();
            WorldServer worldserver = commandlistenerwrapper.getLevel();
            BlockPosition blockposition = GameTestHarnessTestCommand.createTestPositionAround(commandlistenerwrapper);
            Collection<GameTestHarnessInfo> collection = Stream.concat(GameTestHarnessTestCommand.toGameTestInfos(commandlistenerwrapper, RetryOptions.noRetries(), this.finder), GameTestHarnessTestCommand.toGameTestInfo(commandlistenerwrapper, RetryOptions.noRetries(), this.finder, 0)).toList();
            boolean flag = true;

            GameTestHarnessRunner.clearMarkers(worldserver);
            GameTestHarnessRegistry.forgetFailedTests();
            Collection<GameTestHarnessBatch> collection1 = new ArrayList();
            Iterator iterator = collection.iterator();

            while (iterator.hasNext()) {
                GameTestHarnessInfo gametestharnessinfo = (GameTestHarnessInfo) iterator.next();
                EnumBlockRotation[] aenumblockrotation = EnumBlockRotation.values();
                int i = aenumblockrotation.length;

                for (int j = 0; j < i; ++j) {
                    EnumBlockRotation enumblockrotation = aenumblockrotation[j];
                    Collection<GameTestHarnessInfo> collection2 = new ArrayList();

                    for (int k = 0; k < 100; ++k) {
                        GameTestHarnessInfo gametestharnessinfo1 = new GameTestHarnessInfo(gametestharnessinfo.getTestFunction(), enumblockrotation, worldserver, new RetryOptions(1, true));

                        collection2.add(gametestharnessinfo1);
                    }

                    GameTestHarnessBatch gametestharnessbatch = GameTestBatchFactory.toGameTestBatch(collection2, gametestharnessinfo.getTestFunction().batchName(), (long) enumblockrotation.ordinal());

                    collection1.add(gametestharnessbatch);
                }
            }

            StructureGridSpawner structuregridspawner = new StructureGridSpawner(blockposition, 10, true);
            GameTestHarnessRunner gametestharnessrunner = GameTestHarnessRunner.a.fromBatches(collection1, worldserver).batcher(GameTestBatchFactory.fromGameTestInfo(100)).newStructureSpawner(structuregridspawner).existingStructureSpawner(structuregridspawner).haltOnError(true).build();

            return GameTestHarnessTestCommand.trackAndStartRunner(commandlistenerwrapper, worldserver, gametestharnessrunner);
        }

        public int run(RetryOptions retryoptions, int i, int j) {
            GameTestHarnessTestCommand.stopTests();
            CommandListenerWrapper commandlistenerwrapper = this.finder.source();
            WorldServer worldserver = commandlistenerwrapper.getLevel();
            BlockPosition blockposition = GameTestHarnessTestCommand.createTestPositionAround(commandlistenerwrapper);
            Collection<GameTestHarnessInfo> collection = Stream.concat(GameTestHarnessTestCommand.toGameTestInfos(commandlistenerwrapper, retryoptions, this.finder), GameTestHarnessTestCommand.toGameTestInfo(commandlistenerwrapper, retryoptions, this.finder, i)).toList();

            if (collection.isEmpty()) {
                GameTestHarnessTestCommand.say(commandlistenerwrapper, "No tests found");
                return 0;
            } else {
                GameTestHarnessRunner.clearMarkers(worldserver);
                GameTestHarnessRegistry.forgetFailedTests();
                GameTestHarnessTestCommand.say(commandlistenerwrapper, "Running " + collection.size() + " tests...");
                GameTestHarnessRunner gametestharnessrunner = GameTestHarnessRunner.a.fromInfo(collection, worldserver).newStructureSpawner(new StructureGridSpawner(blockposition, j, false)).build();

                return GameTestHarnessTestCommand.trackAndStartRunner(commandlistenerwrapper, worldserver, gametestharnessrunner);
            }
        }

        public int run(int i, int j) {
            return this.run(RetryOptions.noRetries(), i, j);
        }

        public int run(int i) {
            return this.run(RetryOptions.noRetries(), i, 8);
        }

        public int run(RetryOptions retryoptions, int i) {
            return this.run(retryoptions, i, 8);
        }

        public int run(RetryOptions retryoptions) {
            return this.run(retryoptions, 0, 8);
        }

        public int run() {
            return this.run(RetryOptions.noRetries());
        }

        public int locate() {
            GameTestHarnessTestCommand.say(this.finder.source(), "Started locating test structures, this might take a while..");
            MutableInt mutableint = new MutableInt(0);
            BlockPosition blockposition = BlockPosition.containing(this.finder.source().getPosition());

            this.finder.findStructureBlockPos().forEach((blockposition1) -> {
                TileEntityStructure tileentitystructure = (TileEntityStructure) this.finder.source().getLevel().getBlockEntity(blockposition1);

                if (tileentitystructure != null) {
                    EnumDirection enumdirection = tileentitystructure.getRotation().rotate(EnumDirection.NORTH);
                    BlockPosition blockposition2 = tileentitystructure.getBlockPos().relative(enumdirection, 2);
                    int i = (int) enumdirection.getOpposite().toYRot();
                    String s = String.format("/tp @s %d %d %d %d 0", blockposition2.getX(), blockposition2.getY(), blockposition2.getZ(), i);
                    int j = blockposition.getX() - blockposition1.getX();
                    int k = blockposition.getZ() - blockposition1.getZ();
                    int l = MathHelper.floor(MathHelper.sqrt((float) (j * j + k * k)));
                    IChatMutableComponent ichatmutablecomponent = ChatComponentUtils.wrapInSquareBrackets(IChatBaseComponent.translatable("chat.coordinates", blockposition1.getX(), blockposition1.getY(), blockposition1.getZ())).withStyle((chatmodifier) -> {
                        return chatmodifier.withColor(EnumChatFormat.GREEN).withClickEvent(new ChatClickable(ChatClickable.EnumClickAction.SUGGEST_COMMAND, s)).withHoverEvent(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, IChatBaseComponent.translatable("chat.coordinates.tooltip")));
                    });
                    IChatMutableComponent ichatmutablecomponent1 = IChatBaseComponent.literal("Found structure at: ").append((IChatBaseComponent) ichatmutablecomponent).append(" (distance: " + l + ")");

                    this.finder.source().sendSuccess(() -> {
                        return ichatmutablecomponent1;
                    }, false);
                    mutableint.increment();
                }
            });
            int i = mutableint.intValue();

            if (i == 0) {
                GameTestHarnessTestCommand.say(this.finder.source().getLevel(), "No such test structure found", EnumChatFormat.RED);
                return 0;
            } else {
                GameTestHarnessTestCommand.say(this.finder.source().getLevel(), "Finished locating, found " + i + " structure(s)", EnumChatFormat.GREEN);
                return 1;
            }
        }
    }
}
