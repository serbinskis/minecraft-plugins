package net.minecraft.gametest.framework;

import com.mojang.brigadier.context.CommandContext;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.BlockPosition;

public class TestFinder<T> implements StructureBlockPosFinder, TestFunctionFinder {

    static final TestFunctionFinder NO_FUNCTIONS = Stream::empty;
    static final StructureBlockPosFinder NO_STRUCTURES = Stream::empty;
    private final TestFunctionFinder testFunctionFinder;
    private final StructureBlockPosFinder structureBlockPosFinder;
    private final CommandListenerWrapper source;
    private final Function<TestFinder<T>, T> contextProvider;

    @Override
    public Stream<BlockPosition> findStructureBlockPos() {
        return this.structureBlockPosFinder.findStructureBlockPos();
    }

    TestFinder(CommandListenerWrapper commandlistenerwrapper, Function<TestFinder<T>, T> function, TestFunctionFinder testfunctionfinder, StructureBlockPosFinder structureblockposfinder) {
        this.source = commandlistenerwrapper;
        this.contextProvider = function;
        this.testFunctionFinder = testfunctionfinder;
        this.structureBlockPosFinder = structureblockposfinder;
    }

    T get() {
        return this.contextProvider.apply(this);
    }

    public CommandListenerWrapper source() {
        return this.source;
    }

    @Override
    public Stream<GameTestHarnessTestFunction> findTestFunctions() {
        return this.testFunctionFinder.findTestFunctions();
    }

    public static class a<T> {

        private final Function<TestFinder<T>, T> contextProvider;
        private final UnaryOperator<Supplier<Stream<GameTestHarnessTestFunction>>> testFunctionFinderWrapper;
        private final UnaryOperator<Supplier<Stream<BlockPosition>>> structureBlockPosFinderWrapper;

        public a(Function<TestFinder<T>, T> function) {
            this.contextProvider = function;
            this.testFunctionFinderWrapper = (supplier) -> {
                return supplier;
            };
            this.structureBlockPosFinderWrapper = (supplier) -> {
                return supplier;
            };
        }

        private a(Function<TestFinder<T>, T> function, UnaryOperator<Supplier<Stream<GameTestHarnessTestFunction>>> unaryoperator, UnaryOperator<Supplier<Stream<BlockPosition>>> unaryoperator1) {
            this.contextProvider = function;
            this.testFunctionFinderWrapper = unaryoperator;
            this.structureBlockPosFinderWrapper = unaryoperator1;
        }

        public TestFinder.a<T> createMultipleCopies(int i) {
            return new TestFinder.a<>(this.contextProvider, createCopies(i), createCopies(i));
        }

        private static <Q> UnaryOperator<Supplier<Stream<Q>>> createCopies(int i) {
            return (supplier) -> {
                List<Q> list = new LinkedList();
                List<Q> list1 = ((Stream) supplier.get()).toList();

                for (int j = 0; j < i; ++j) {
                    list.addAll(list1);
                }

                Objects.requireNonNull(list);
                return list::stream;
            };
        }

        private T build(CommandListenerWrapper commandlistenerwrapper, TestFunctionFinder testfunctionfinder, StructureBlockPosFinder structureblockposfinder) {
            Function function = this.contextProvider;
            UnaryOperator unaryoperator = this.testFunctionFinderWrapper;

            Objects.requireNonNull(testfunctionfinder);
            Supplier supplier = (Supplier) unaryoperator.apply(testfunctionfinder::findTestFunctions);

            Objects.requireNonNull(supplier);
            TestFunctionFinder testfunctionfinder1 = supplier::get;
            UnaryOperator unaryoperator1 = this.structureBlockPosFinderWrapper;

            Objects.requireNonNull(structureblockposfinder);
            Supplier supplier1 = (Supplier) unaryoperator1.apply(structureblockposfinder::findStructureBlockPos);

            Objects.requireNonNull(supplier1);
            return (new TestFinder<>(commandlistenerwrapper, function, testfunctionfinder1, supplier1::get)).get();
        }

        public T radius(CommandContext<CommandListenerWrapper> commandcontext, int i) {
            CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
            BlockPosition blockposition = BlockPosition.containing(commandlistenerwrapper.getPosition());

            return this.build(commandlistenerwrapper, TestFinder.NO_FUNCTIONS, () -> {
                return GameTestHarnessStructures.findStructureBlocks(blockposition, i, commandlistenerwrapper.getLevel());
            });
        }

        public T nearest(CommandContext<CommandListenerWrapper> commandcontext) {
            CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
            BlockPosition blockposition = BlockPosition.containing(commandlistenerwrapper.getPosition());

            return this.build(commandlistenerwrapper, TestFinder.NO_FUNCTIONS, () -> {
                return GameTestHarnessStructures.findNearestStructureBlock(blockposition, 15, commandlistenerwrapper.getLevel()).stream();
            });
        }

        public T allNearby(CommandContext<CommandListenerWrapper> commandcontext) {
            CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
            BlockPosition blockposition = BlockPosition.containing(commandlistenerwrapper.getPosition());

            return this.build(commandlistenerwrapper, TestFinder.NO_FUNCTIONS, () -> {
                return GameTestHarnessStructures.findStructureBlocks(blockposition, 200, commandlistenerwrapper.getLevel());
            });
        }

        public T lookedAt(CommandContext<CommandListenerWrapper> commandcontext) {
            CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();

            return this.build(commandlistenerwrapper, TestFinder.NO_FUNCTIONS, () -> {
                return GameTestHarnessStructures.lookedAtStructureBlockPos(BlockPosition.containing(commandlistenerwrapper.getPosition()), commandlistenerwrapper.getPlayer().getCamera(), commandlistenerwrapper.getLevel());
            });
        }

        public T allTests(CommandContext<CommandListenerWrapper> commandcontext) {
            return this.build((CommandListenerWrapper) commandcontext.getSource(), () -> {
                return GameTestHarnessRegistry.getAllTestFunctions().stream().filter((gametestharnesstestfunction) -> {
                    return !gametestharnesstestfunction.manualOnly();
                });
            }, TestFinder.NO_STRUCTURES);
        }

        public T allTestsInClass(CommandContext<CommandListenerWrapper> commandcontext, String s) {
            return this.build((CommandListenerWrapper) commandcontext.getSource(), () -> {
                return GameTestHarnessRegistry.getTestFunctionsForClassName(s).filter((gametestharnesstestfunction) -> {
                    return !gametestharnesstestfunction.manualOnly();
                });
            }, TestFinder.NO_STRUCTURES);
        }

        public T failedTests(CommandContext<CommandListenerWrapper> commandcontext, boolean flag) {
            return this.build((CommandListenerWrapper) commandcontext.getSource(), () -> {
                return GameTestHarnessRegistry.getLastFailedTests().filter((gametestharnesstestfunction) -> {
                    return !flag || gametestharnesstestfunction.required();
                });
            }, TestFinder.NO_STRUCTURES);
        }

        public T byArgument(CommandContext<CommandListenerWrapper> commandcontext, String s) {
            return this.build((CommandListenerWrapper) commandcontext.getSource(), () -> {
                return Stream.of(GameTestHarnessTestFunctionArgument.getTestFunction(commandcontext, s));
            }, TestFinder.NO_STRUCTURES);
        }

        public T locateByName(CommandContext<CommandListenerWrapper> commandcontext, String s) {
            CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
            BlockPosition blockposition = BlockPosition.containing(commandlistenerwrapper.getPosition());

            return this.build(commandlistenerwrapper, TestFinder.NO_FUNCTIONS, () -> {
                return GameTestHarnessStructures.findStructureByTestFunction(blockposition, 1024, commandlistenerwrapper.getLevel(), s);
            });
        }

        public T failedTests(CommandContext<CommandListenerWrapper> commandcontext) {
            return this.failedTests(commandcontext, false);
        }
    }
}
