package net.minecraft.server.commands;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.CriterionConditionValue;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentAnchor;
import net.minecraft.commands.arguments.ArgumentCriterionValue;
import net.minecraft.commands.arguments.ArgumentDimension;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.ArgumentMinecraftKeyRegistered;
import net.minecraft.commands.arguments.ArgumentNBTKey;
import net.minecraft.commands.arguments.ArgumentScoreboardObjective;
import net.minecraft.commands.arguments.ArgumentScoreholder;
import net.minecraft.commands.arguments.HeightmapTypeArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.SlotsArgument;
import net.minecraft.commands.arguments.blocks.ArgumentBlockPredicate;
import net.minecraft.commands.arguments.coordinates.ArgumentPosition;
import net.minecraft.commands.arguments.coordinates.ArgumentRotation;
import net.minecraft.commands.arguments.coordinates.ArgumentRotationAxis;
import net.minecraft.commands.arguments.coordinates.ArgumentVec3;
import net.minecraft.commands.arguments.item.ArgumentItemPredicate;
import net.minecraft.commands.arguments.item.ArgumentTag;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.execution.tasks.FallthroughTask;
import net.minecraft.commands.execution.tasks.IsolatedCall;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.commands.synchronization.CompletionProviders;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.SectionPosition;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.server.bossevents.BossBattleCustom;
import net.minecraft.server.commands.data.CommandData;
import net.minecraft.server.commands.data.CommandDataAccessor;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.ScoreboardObjective;

public class CommandExecute {

    private static final int MAX_TEST_AREA = 32768;
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((object, object1) -> {
        return IChatBaseComponent.translatableEscape("commands.execute.blocks.toobig", object, object1);
    });
    private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType(IChatBaseComponent.translatable("commands.execute.conditional.fail"));
    private static final DynamicCommandExceptionType ERROR_CONDITIONAL_FAILED_COUNT = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("commands.execute.conditional.fail_count", object);
    });
    @VisibleForTesting
    public static final Dynamic2CommandExceptionType ERROR_FUNCTION_CONDITION_INSTANTATION_FAILURE = new Dynamic2CommandExceptionType((object, object1) -> {
        return IChatBaseComponent.translatableEscape("commands.execute.function.instantiationFailure", object, object1);
    });
    private static final SuggestionProvider<CommandListenerWrapper> SUGGEST_PREDICATE = (commandcontext, suggestionsbuilder) -> {
        ReloadableServerRegistries.b reloadableserverregistries_b = ((CommandListenerWrapper) commandcontext.getSource()).getServer().reloadableRegistries();

        return ICompletionProvider.suggestResource((Iterable) reloadableserverregistries_b.getKeys(Registries.PREDICATE), suggestionsbuilder);
    };

    public CommandExecute() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher, CommandBuildContext commandbuildcontext) {
        LiteralCommandNode<CommandListenerWrapper> literalcommandnode = commanddispatcher.register((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("execute").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        }));

        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("execute").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(net.minecraft.commands.CommandDispatcher.literal("run").redirect(commanddispatcher.getRoot()))).then(addConditionals(literalcommandnode, net.minecraft.commands.CommandDispatcher.literal("if"), true, commandbuildcontext))).then(addConditionals(literalcommandnode, net.minecraft.commands.CommandDispatcher.literal("unless"), false, commandbuildcontext))).then(net.minecraft.commands.CommandDispatcher.literal("as").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.entities()).fork(literalcommandnode, (commandcontext) -> {
            List<CommandListenerWrapper> list = Lists.newArrayList();
            Iterator iterator = ArgumentEntity.getOptionalEntities(commandcontext, "targets").iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();

                list.add(((CommandListenerWrapper) commandcontext.getSource()).withEntity(entity));
            }

            return list;
        })))).then(net.minecraft.commands.CommandDispatcher.literal("at").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.entities()).fork(literalcommandnode, (commandcontext) -> {
            List<CommandListenerWrapper> list = Lists.newArrayList();
            Iterator iterator = ArgumentEntity.getOptionalEntities(commandcontext, "targets").iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();

                list.add(((CommandListenerWrapper) commandcontext.getSource()).withLevel((WorldServer) entity.level()).withPosition(entity.position()).withRotation(entity.getRotationVector()));
            }

            return list;
        })))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("store").then(wrapStores(literalcommandnode, net.minecraft.commands.CommandDispatcher.literal("result"), true))).then(wrapStores(literalcommandnode, net.minecraft.commands.CommandDispatcher.literal("success"), false)))).then(((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("positioned").then(net.minecraft.commands.CommandDispatcher.argument("pos", ArgumentVec3.vec3()).redirect(literalcommandnode, (commandcontext) -> {
            return ((CommandListenerWrapper) commandcontext.getSource()).withPosition(ArgumentVec3.getVec3(commandcontext, "pos")).withAnchor(ArgumentAnchor.Anchor.FEET);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("as").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.entities()).fork(literalcommandnode, (commandcontext) -> {
            List<CommandListenerWrapper> list = Lists.newArrayList();
            Iterator iterator = ArgumentEntity.getOptionalEntities(commandcontext, "targets").iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();

                list.add(((CommandListenerWrapper) commandcontext.getSource()).withPosition(entity.position()));
            }

            return list;
        })))).then(net.minecraft.commands.CommandDispatcher.literal("over").then(net.minecraft.commands.CommandDispatcher.argument("heightmap", HeightmapTypeArgument.heightmap()).redirect(literalcommandnode, (commandcontext) -> {
            Vec3D vec3d = ((CommandListenerWrapper) commandcontext.getSource()).getPosition();
            WorldServer worldserver = ((CommandListenerWrapper) commandcontext.getSource()).getLevel();
            double d0 = vec3d.x();
            double d1 = vec3d.z();

            if (!worldserver.hasChunk(SectionPosition.blockToSectionCoord(d0), SectionPosition.blockToSectionCoord(d1))) {
                throw ArgumentPosition.ERROR_NOT_LOADED.create();
            } else {
                int i = worldserver.getHeight(HeightmapTypeArgument.getHeightmap(commandcontext, "heightmap"), MathHelper.floor(d0), MathHelper.floor(d1));

                return ((CommandListenerWrapper) commandcontext.getSource()).withPosition(new Vec3D(d0, (double) i, d1));
            }
        }))))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("rotated").then(net.minecraft.commands.CommandDispatcher.argument("rot", ArgumentRotation.rotation()).redirect(literalcommandnode, (commandcontext) -> {
            return ((CommandListenerWrapper) commandcontext.getSource()).withRotation(ArgumentRotation.getRotation(commandcontext, "rot").getRotation((CommandListenerWrapper) commandcontext.getSource()));
        }))).then(net.minecraft.commands.CommandDispatcher.literal("as").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.entities()).fork(literalcommandnode, (commandcontext) -> {
            List<CommandListenerWrapper> list = Lists.newArrayList();
            Iterator iterator = ArgumentEntity.getOptionalEntities(commandcontext, "targets").iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();

                list.add(((CommandListenerWrapper) commandcontext.getSource()).withRotation(entity.getRotationVector()));
            }

            return list;
        }))))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("facing").then(net.minecraft.commands.CommandDispatcher.literal("entity").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.entities()).then(net.minecraft.commands.CommandDispatcher.argument("anchor", ArgumentAnchor.anchor()).fork(literalcommandnode, (commandcontext) -> {
            List<CommandListenerWrapper> list = Lists.newArrayList();
            ArgumentAnchor.Anchor argumentanchor_anchor = ArgumentAnchor.getAnchor(commandcontext, "anchor");
            Iterator iterator = ArgumentEntity.getOptionalEntities(commandcontext, "targets").iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();

                list.add(((CommandListenerWrapper) commandcontext.getSource()).facing(entity, argumentanchor_anchor));
            }

            return list;
        }))))).then(net.minecraft.commands.CommandDispatcher.argument("pos", ArgumentVec3.vec3()).redirect(literalcommandnode, (commandcontext) -> {
            return ((CommandListenerWrapper) commandcontext.getSource()).facing(ArgumentVec3.getVec3(commandcontext, "pos"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("align").then(net.minecraft.commands.CommandDispatcher.argument("axes", ArgumentRotationAxis.swizzle()).redirect(literalcommandnode, (commandcontext) -> {
            return ((CommandListenerWrapper) commandcontext.getSource()).withPosition(((CommandListenerWrapper) commandcontext.getSource()).getPosition().align(ArgumentRotationAxis.getSwizzle(commandcontext, "axes")));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("anchored").then(net.minecraft.commands.CommandDispatcher.argument("anchor", ArgumentAnchor.anchor()).redirect(literalcommandnode, (commandcontext) -> {
            return ((CommandListenerWrapper) commandcontext.getSource()).withAnchor(ArgumentAnchor.getAnchor(commandcontext, "anchor"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("in").then(net.minecraft.commands.CommandDispatcher.argument("dimension", ArgumentDimension.dimension()).redirect(literalcommandnode, (commandcontext) -> {
            return ((CommandListenerWrapper) commandcontext.getSource()).withLevel(ArgumentDimension.getDimension(commandcontext, "dimension"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("summon").then(net.minecraft.commands.CommandDispatcher.argument("entity", ResourceArgument.resource(commandbuildcontext, Registries.ENTITY_TYPE)).suggests(CompletionProviders.SUMMONABLE_ENTITIES).redirect(literalcommandnode, (commandcontext) -> {
            return spawnEntityAndRedirect((CommandListenerWrapper) commandcontext.getSource(), ResourceArgument.getSummonableEntityType(commandcontext, "entity"));
        })))).then(createRelationOperations(literalcommandnode, net.minecraft.commands.CommandDispatcher.literal("on"))));
    }

    private static ArgumentBuilder<CommandListenerWrapper, ?> wrapStores(LiteralCommandNode<CommandListenerWrapper> literalcommandnode, LiteralArgumentBuilder<CommandListenerWrapper> literalargumentbuilder, boolean flag) {
        literalargumentbuilder.then(net.minecraft.commands.CommandDispatcher.literal("score").then(net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentScoreholder.scoreHolders()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(net.minecraft.commands.CommandDispatcher.argument("objective", ArgumentScoreboardObjective.objective()).redirect(literalcommandnode, (commandcontext) -> {
            return storeValue((CommandListenerWrapper) commandcontext.getSource(), ArgumentScoreholder.getNamesWithDefaultWildcard(commandcontext, "targets"), ArgumentScoreboardObjective.getObjective(commandcontext, "objective"), flag);
        }))));
        literalargumentbuilder.then(net.minecraft.commands.CommandDispatcher.literal("bossbar").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("id", ArgumentMinecraftKeyRegistered.id()).suggests(CommandBossBar.SUGGEST_BOSS_BAR).then(net.minecraft.commands.CommandDispatcher.literal("value").redirect(literalcommandnode, (commandcontext) -> {
            return storeValue((CommandListenerWrapper) commandcontext.getSource(), CommandBossBar.getBossBar(commandcontext), true, flag);
        }))).then(net.minecraft.commands.CommandDispatcher.literal("max").redirect(literalcommandnode, (commandcontext) -> {
            return storeValue((CommandListenerWrapper) commandcontext.getSource(), CommandBossBar.getBossBar(commandcontext), false, flag);
        }))));
        Iterator iterator = CommandData.TARGET_PROVIDERS.iterator();

        while (iterator.hasNext()) {
            CommandData.c commanddata_c = (CommandData.c) iterator.next();

            commanddata_c.wrap(literalargumentbuilder, (argumentbuilder) -> {
                return argumentbuilder.then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("path", ArgumentNBTKey.nbtPath()).then(net.minecraft.commands.CommandDispatcher.literal("int").then(net.minecraft.commands.CommandDispatcher.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalcommandnode, (commandcontext) -> {
                    return storeData((CommandListenerWrapper) commandcontext.getSource(), commanddata_c.access(commandcontext), ArgumentNBTKey.getPath(commandcontext, "path"), (i) -> {
                        return NBTTagInt.valueOf((int) ((double) i * DoubleArgumentType.getDouble(commandcontext, "scale")));
                    }, flag);
                })))).then(net.minecraft.commands.CommandDispatcher.literal("float").then(net.minecraft.commands.CommandDispatcher.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalcommandnode, (commandcontext) -> {
                    return storeData((CommandListenerWrapper) commandcontext.getSource(), commanddata_c.access(commandcontext), ArgumentNBTKey.getPath(commandcontext, "path"), (i) -> {
                        return NBTTagFloat.valueOf((float) ((double) i * DoubleArgumentType.getDouble(commandcontext, "scale")));
                    }, flag);
                })))).then(net.minecraft.commands.CommandDispatcher.literal("short").then(net.minecraft.commands.CommandDispatcher.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalcommandnode, (commandcontext) -> {
                    return storeData((CommandListenerWrapper) commandcontext.getSource(), commanddata_c.access(commandcontext), ArgumentNBTKey.getPath(commandcontext, "path"), (i) -> {
                        return NBTTagShort.valueOf((short) ((int) ((double) i * DoubleArgumentType.getDouble(commandcontext, "scale"))));
                    }, flag);
                })))).then(net.minecraft.commands.CommandDispatcher.literal("long").then(net.minecraft.commands.CommandDispatcher.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalcommandnode, (commandcontext) -> {
                    return storeData((CommandListenerWrapper) commandcontext.getSource(), commanddata_c.access(commandcontext), ArgumentNBTKey.getPath(commandcontext, "path"), (i) -> {
                        return NBTTagLong.valueOf((long) ((double) i * DoubleArgumentType.getDouble(commandcontext, "scale")));
                    }, flag);
                })))).then(net.minecraft.commands.CommandDispatcher.literal("double").then(net.minecraft.commands.CommandDispatcher.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalcommandnode, (commandcontext) -> {
                    return storeData((CommandListenerWrapper) commandcontext.getSource(), commanddata_c.access(commandcontext), ArgumentNBTKey.getPath(commandcontext, "path"), (i) -> {
                        return NBTTagDouble.valueOf((double) i * DoubleArgumentType.getDouble(commandcontext, "scale"));
                    }, flag);
                })))).then(net.minecraft.commands.CommandDispatcher.literal("byte").then(net.minecraft.commands.CommandDispatcher.argument("scale", DoubleArgumentType.doubleArg()).redirect(literalcommandnode, (commandcontext) -> {
                    return storeData((CommandListenerWrapper) commandcontext.getSource(), commanddata_c.access(commandcontext), ArgumentNBTKey.getPath(commandcontext, "path"), (i) -> {
                        return NBTTagByte.valueOf((byte) ((int) ((double) i * DoubleArgumentType.getDouble(commandcontext, "scale"))));
                    }, flag);
                }))));
            });
        }

        return literalargumentbuilder;
    }

    private static CommandListenerWrapper storeValue(CommandListenerWrapper commandlistenerwrapper, Collection<ScoreHolder> collection, ScoreboardObjective scoreboardobjective, boolean flag) {
        ScoreboardServer scoreboardserver = commandlistenerwrapper.getServer().getScoreboard();

        return commandlistenerwrapper.withCallback((flag1, i) -> {
            Iterator iterator = collection.iterator();

            while (iterator.hasNext()) {
                ScoreHolder scoreholder = (ScoreHolder) iterator.next();
                ScoreAccess scoreaccess = scoreboardserver.getOrCreatePlayerScore(scoreholder, scoreboardobjective);
                int j = flag ? i : (flag1 ? 1 : 0);

                scoreaccess.set(j);
            }

        }, CommandResultCallback::chain);
    }

    private static CommandListenerWrapper storeValue(CommandListenerWrapper commandlistenerwrapper, BossBattleCustom bossbattlecustom, boolean flag, boolean flag1) {
        return commandlistenerwrapper.withCallback((flag2, i) -> {
            int j = flag1 ? i : (flag2 ? 1 : 0);

            if (flag) {
                bossbattlecustom.setValue(j);
            } else {
                bossbattlecustom.setMax(j);
            }

        }, CommandResultCallback::chain);
    }

    private static CommandListenerWrapper storeData(CommandListenerWrapper commandlistenerwrapper, CommandDataAccessor commanddataaccessor, ArgumentNBTKey.g argumentnbtkey_g, IntFunction<NBTBase> intfunction, boolean flag) {
        return commandlistenerwrapper.withCallback((flag1, i) -> {
            try {
                NBTTagCompound nbttagcompound = commanddataaccessor.getData();
                int j = flag ? i : (flag1 ? 1 : 0);

                argumentnbtkey_g.set(nbttagcompound, (NBTBase) intfunction.apply(j));
                commanddataaccessor.setData(nbttagcompound);
            } catch (CommandSyntaxException commandsyntaxexception) {
                ;
            }

        }, CommandResultCallback::chain);
    }

    private static boolean isChunkLoaded(WorldServer worldserver, BlockPosition blockposition) {
        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(blockposition);
        Chunk chunk = worldserver.getChunkSource().getChunkNow(chunkcoordintpair.x, chunkcoordintpair.z);

        return chunk == null ? false : chunk.getFullStatus() == FullChunkStatus.ENTITY_TICKING && worldserver.areEntitiesLoaded(chunkcoordintpair.toLong());
    }

    private static ArgumentBuilder<CommandListenerWrapper, ?> addConditionals(CommandNode<CommandListenerWrapper> commandnode, LiteralArgumentBuilder<CommandListenerWrapper> literalargumentbuilder, boolean flag, CommandBuildContext commandbuildcontext) {
        ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) literalargumentbuilder.then(net.minecraft.commands.CommandDispatcher.literal("block").then(net.minecraft.commands.CommandDispatcher.argument("pos", ArgumentPosition.blockPos()).then(addConditional(commandnode, net.minecraft.commands.CommandDispatcher.argument("block", ArgumentBlockPredicate.blockPredicate(commandbuildcontext)), flag, (commandcontext) -> {
            return ArgumentBlockPredicate.getBlockPredicate(commandcontext, "block").test(new ShapeDetectorBlock(((CommandListenerWrapper) commandcontext.getSource()).getLevel(), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), true));
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("biome").then(net.minecraft.commands.CommandDispatcher.argument("pos", ArgumentPosition.blockPos()).then(addConditional(commandnode, net.minecraft.commands.CommandDispatcher.argument("biome", ResourceOrTagArgument.resourceOrTag(commandbuildcontext, Registries.BIOME)), flag, (commandcontext) -> {
            return ResourceOrTagArgument.getResourceOrTag(commandcontext, "biome", Registries.BIOME).test(((CommandListenerWrapper) commandcontext.getSource()).getLevel().getBiome(ArgumentPosition.getLoadedBlockPos(commandcontext, "pos")));
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("loaded").then(addConditional(commandnode, net.minecraft.commands.CommandDispatcher.argument("pos", ArgumentPosition.blockPos()), flag, (commandcontext) -> {
            return isChunkLoaded(((CommandListenerWrapper) commandcontext.getSource()).getLevel(), ArgumentPosition.getBlockPos(commandcontext, "pos"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("dimension").then(addConditional(commandnode, net.minecraft.commands.CommandDispatcher.argument("dimension", ArgumentDimension.dimension()), flag, (commandcontext) -> {
            return ArgumentDimension.getDimension(commandcontext, "dimension") == ((CommandListenerWrapper) commandcontext.getSource()).getLevel();
        })))).then(net.minecraft.commands.CommandDispatcher.literal("score").then(net.minecraft.commands.CommandDispatcher.argument("target", ArgumentScoreholder.scoreHolder()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("targetObjective", ArgumentScoreboardObjective.objective()).then(net.minecraft.commands.CommandDispatcher.literal("=").then(net.minecraft.commands.CommandDispatcher.argument("source", ArgumentScoreholder.scoreHolder()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(addConditional(commandnode, net.minecraft.commands.CommandDispatcher.argument("sourceObjective", ArgumentScoreboardObjective.objective()), flag, (commandcontext) -> {
            return checkScore(commandcontext, (i, j) -> {
                return i == j;
            });
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("<").then(net.minecraft.commands.CommandDispatcher.argument("source", ArgumentScoreholder.scoreHolder()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(addConditional(commandnode, net.minecraft.commands.CommandDispatcher.argument("sourceObjective", ArgumentScoreboardObjective.objective()), flag, (commandcontext) -> {
            return checkScore(commandcontext, (i, j) -> {
                return i < j;
            });
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("<=").then(net.minecraft.commands.CommandDispatcher.argument("source", ArgumentScoreholder.scoreHolder()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(addConditional(commandnode, net.minecraft.commands.CommandDispatcher.argument("sourceObjective", ArgumentScoreboardObjective.objective()), flag, (commandcontext) -> {
            return checkScore(commandcontext, (i, j) -> {
                return i <= j;
            });
        }))))).then(net.minecraft.commands.CommandDispatcher.literal(">").then(net.minecraft.commands.CommandDispatcher.argument("source", ArgumentScoreholder.scoreHolder()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(addConditional(commandnode, net.minecraft.commands.CommandDispatcher.argument("sourceObjective", ArgumentScoreboardObjective.objective()), flag, (commandcontext) -> {
            return checkScore(commandcontext, (i, j) -> {
                return i > j;
            });
        }))))).then(net.minecraft.commands.CommandDispatcher.literal(">=").then(net.minecraft.commands.CommandDispatcher.argument("source", ArgumentScoreholder.scoreHolder()).suggests(ArgumentScoreholder.SUGGEST_SCORE_HOLDERS).then(addConditional(commandnode, net.minecraft.commands.CommandDispatcher.argument("sourceObjective", ArgumentScoreboardObjective.objective()), flag, (commandcontext) -> {
            return checkScore(commandcontext, (i, j) -> {
                return i >= j;
            });
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("matches").then(addConditional(commandnode, net.minecraft.commands.CommandDispatcher.argument("range", ArgumentCriterionValue.intRange()), flag, (commandcontext) -> {
            return checkScore(commandcontext, ArgumentCriterionValue.b.getRange(commandcontext, "range"));
        }))))))).then(net.minecraft.commands.CommandDispatcher.literal("blocks").then(net.minecraft.commands.CommandDispatcher.argument("start", ArgumentPosition.blockPos()).then(net.minecraft.commands.CommandDispatcher.argument("end", ArgumentPosition.blockPos()).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("destination", ArgumentPosition.blockPos()).then(addIfBlocksConditional(commandnode, net.minecraft.commands.CommandDispatcher.literal("all"), flag, false))).then(addIfBlocksConditional(commandnode, net.minecraft.commands.CommandDispatcher.literal("masked"), flag, true))))))).then(net.minecraft.commands.CommandDispatcher.literal("entity").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("entities", ArgumentEntity.entities()).fork(commandnode, (commandcontext) -> {
            return expect(commandcontext, flag, !ArgumentEntity.getOptionalEntities(commandcontext, "entities").isEmpty());
        })).executes(createNumericConditionalHandler(flag, (commandcontext) -> {
            return ArgumentEntity.getOptionalEntities(commandcontext, "entities").size();
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("predicate").then(addConditional(commandnode, net.minecraft.commands.CommandDispatcher.argument("predicate", ResourceOrIdArgument.lootPredicate(commandbuildcontext)).suggests(CommandExecute.SUGGEST_PREDICATE), flag, (commandcontext) -> {
            return checkCustomPredicate((CommandListenerWrapper) commandcontext.getSource(), ResourceOrIdArgument.getLootPredicate(commandcontext, "predicate"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("function").then(net.minecraft.commands.CommandDispatcher.argument("name", ArgumentTag.functions()).suggests(CommandFunction.SUGGEST_FUNCTION).fork(commandnode, new CommandExecute.d(flag))))).then(((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("items").then(net.minecraft.commands.CommandDispatcher.literal("entity").then(net.minecraft.commands.CommandDispatcher.argument("entities", ArgumentEntity.entities()).then(net.minecraft.commands.CommandDispatcher.argument("slots", SlotsArgument.slots()).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("item_predicate", ArgumentItemPredicate.itemPredicate(commandbuildcontext)).fork(commandnode, (commandcontext) -> {
            return expect(commandcontext, flag, countItems(ArgumentEntity.getEntities(commandcontext, "entities"), SlotsArgument.getSlots(commandcontext, "slots"), ArgumentItemPredicate.getItemPredicate(commandcontext, "item_predicate")) > 0);
        })).executes(createNumericConditionalHandler(flag, (commandcontext) -> {
            return countItems(ArgumentEntity.getEntities(commandcontext, "entities"), SlotsArgument.getSlots(commandcontext, "slots"), ArgumentItemPredicate.getItemPredicate(commandcontext, "item_predicate"));
        }))))))).then(net.minecraft.commands.CommandDispatcher.literal("block").then(net.minecraft.commands.CommandDispatcher.argument("pos", ArgumentPosition.blockPos()).then(net.minecraft.commands.CommandDispatcher.argument("slots", SlotsArgument.slots()).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("item_predicate", ArgumentItemPredicate.itemPredicate(commandbuildcontext)).fork(commandnode, (commandcontext) -> {
            return expect(commandcontext, flag, countItems((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), SlotsArgument.getSlots(commandcontext, "slots"), ArgumentItemPredicate.getItemPredicate(commandcontext, "item_predicate")) > 0);
        })).executes(createNumericConditionalHandler(flag, (commandcontext) -> {
            return countItems((CommandListenerWrapper) commandcontext.getSource(), ArgumentPosition.getLoadedBlockPos(commandcontext, "pos"), SlotsArgument.getSlots(commandcontext, "slots"), ArgumentItemPredicate.getItemPredicate(commandcontext, "item_predicate"));
        })))))));
        Iterator iterator = CommandData.SOURCE_PROVIDERS.iterator();

        while (iterator.hasNext()) {
            CommandData.c commanddata_c = (CommandData.c) iterator.next();

            literalargumentbuilder.then(commanddata_c.wrap(net.minecraft.commands.CommandDispatcher.literal("data"), (argumentbuilder) -> {
                return argumentbuilder.then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("path", ArgumentNBTKey.nbtPath()).fork(commandnode, (commandcontext) -> {
                    return expect(commandcontext, flag, checkMatchingData(commanddata_c.access(commandcontext), ArgumentNBTKey.getPath(commandcontext, "path")) > 0);
                })).executes(createNumericConditionalHandler(flag, (commandcontext) -> {
                    return checkMatchingData(commanddata_c.access(commandcontext), ArgumentNBTKey.getPath(commandcontext, "path"));
                })));
            }));
        }

        return literalargumentbuilder;
    }

    private static int countItems(Iterable<? extends Entity> iterable, SlotRange slotrange, Predicate<ItemStack> predicate) {
        int i = 0;
        Iterator iterator = iterable.iterator();

        while (iterator.hasNext()) {
            Entity entity = (Entity) iterator.next();
            IntList intlist = slotrange.slots();

            for (int j = 0; j < intlist.size(); ++j) {
                int k = intlist.getInt(j);
                SlotAccess slotaccess = entity.getSlot(k);
                ItemStack itemstack = slotaccess.get();

                if (predicate.test(itemstack)) {
                    i += itemstack.getCount();
                }
            }
        }

        return i;
    }

    private static int countItems(CommandListenerWrapper commandlistenerwrapper, BlockPosition blockposition, SlotRange slotrange, Predicate<ItemStack> predicate) throws CommandSyntaxException {
        int i = 0;
        IInventory iinventory = ItemCommands.getContainer(commandlistenerwrapper, blockposition, ItemCommands.ERROR_SOURCE_NOT_A_CONTAINER);
        int j = iinventory.getContainerSize();
        IntList intlist = slotrange.slots();

        for (int k = 0; k < intlist.size(); ++k) {
            int l = intlist.getInt(k);

            if (l >= 0 && l < j) {
                ItemStack itemstack = iinventory.getItem(l);

                if (predicate.test(itemstack)) {
                    i += itemstack.getCount();
                }
            }
        }

        return i;
    }

    private static Command<CommandListenerWrapper> createNumericConditionalHandler(boolean flag, CommandExecute.b commandexecute_b) {
        return flag ? (commandcontext) -> {
            int i = commandexecute_b.test(commandcontext);

            if (i > 0) {
                ((CommandListenerWrapper) commandcontext.getSource()).sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.execute.conditional.pass_count", i);
                }, false);
                return i;
            } else {
                throw CommandExecute.ERROR_CONDITIONAL_FAILED.create();
            }
        } : (commandcontext) -> {
            int i = commandexecute_b.test(commandcontext);

            if (i == 0) {
                ((CommandListenerWrapper) commandcontext.getSource()).sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.execute.conditional.pass");
                }, false);
                return 1;
            } else {
                throw CommandExecute.ERROR_CONDITIONAL_FAILED_COUNT.create(i);
            }
        };
    }

    private static int checkMatchingData(CommandDataAccessor commanddataaccessor, ArgumentNBTKey.g argumentnbtkey_g) throws CommandSyntaxException {
        return argumentnbtkey_g.countMatching(commanddataaccessor.getData());
    }

    private static boolean checkScore(CommandContext<CommandListenerWrapper> commandcontext, CommandExecute.e commandexecute_e) throws CommandSyntaxException {
        ScoreHolder scoreholder = ArgumentScoreholder.getName(commandcontext, "target");
        ScoreboardObjective scoreboardobjective = ArgumentScoreboardObjective.getObjective(commandcontext, "targetObjective");
        ScoreHolder scoreholder1 = ArgumentScoreholder.getName(commandcontext, "source");
        ScoreboardObjective scoreboardobjective1 = ArgumentScoreboardObjective.getObjective(commandcontext, "sourceObjective");
        ScoreboardServer scoreboardserver = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getScoreboard();
        ReadOnlyScoreInfo readonlyscoreinfo = scoreboardserver.getPlayerScoreInfo(scoreholder, scoreboardobjective);
        ReadOnlyScoreInfo readonlyscoreinfo1 = scoreboardserver.getPlayerScoreInfo(scoreholder1, scoreboardobjective1);

        return readonlyscoreinfo != null && readonlyscoreinfo1 != null ? commandexecute_e.test(readonlyscoreinfo.value(), readonlyscoreinfo1.value()) : false;
    }

    private static boolean checkScore(CommandContext<CommandListenerWrapper> commandcontext, CriterionConditionValue.IntegerRange criterionconditionvalue_integerrange) throws CommandSyntaxException {
        ScoreHolder scoreholder = ArgumentScoreholder.getName(commandcontext, "target");
        ScoreboardObjective scoreboardobjective = ArgumentScoreboardObjective.getObjective(commandcontext, "targetObjective");
        ScoreboardServer scoreboardserver = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getScoreboard();
        ReadOnlyScoreInfo readonlyscoreinfo = scoreboardserver.getPlayerScoreInfo(scoreholder, scoreboardobjective);

        return readonlyscoreinfo == null ? false : criterionconditionvalue_integerrange.matches(readonlyscoreinfo.value());
    }

    private static boolean checkCustomPredicate(CommandListenerWrapper commandlistenerwrapper, Holder<LootItemCondition> holder) {
        WorldServer worldserver = commandlistenerwrapper.getLevel();
        LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.ORIGIN, commandlistenerwrapper.getPosition()).withOptionalParameter(LootContextParameters.THIS_ENTITY, commandlistenerwrapper.getEntity()).create(LootContextParameterSets.COMMAND);
        LootTableInfo loottableinfo = (new LootTableInfo.Builder(lootparams)).create(Optional.empty());

        loottableinfo.pushVisitedElement(LootTableInfo.createVisitedEntry((LootItemCondition) holder.value()));
        return ((LootItemCondition) holder.value()).test(loottableinfo);
    }

    private static Collection<CommandListenerWrapper> expect(CommandContext<CommandListenerWrapper> commandcontext, boolean flag, boolean flag1) {
        return (Collection) (flag1 == flag ? Collections.singleton((CommandListenerWrapper) commandcontext.getSource()) : Collections.emptyList());
    }

    private static ArgumentBuilder<CommandListenerWrapper, ?> addConditional(CommandNode<CommandListenerWrapper> commandnode, ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder, boolean flag, CommandExecute.c commandexecute_c) {
        return argumentbuilder.fork(commandnode, (commandcontext) -> {
            return expect(commandcontext, flag, commandexecute_c.test(commandcontext));
        }).executes((commandcontext) -> {
            if (flag == commandexecute_c.test(commandcontext)) {
                ((CommandListenerWrapper) commandcontext.getSource()).sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.execute.conditional.pass");
                }, false);
                return 1;
            } else {
                throw CommandExecute.ERROR_CONDITIONAL_FAILED.create();
            }
        });
    }

    private static ArgumentBuilder<CommandListenerWrapper, ?> addIfBlocksConditional(CommandNode<CommandListenerWrapper> commandnode, ArgumentBuilder<CommandListenerWrapper, ?> argumentbuilder, boolean flag, boolean flag1) {
        return argumentbuilder.fork(commandnode, (commandcontext) -> {
            return expect(commandcontext, flag, checkRegions(commandcontext, flag1).isPresent());
        }).executes(flag ? (commandcontext) -> {
            return checkIfRegions(commandcontext, flag1);
        } : (commandcontext) -> {
            return checkUnlessRegions(commandcontext, flag1);
        });
    }

    private static int checkIfRegions(CommandContext<CommandListenerWrapper> commandcontext, boolean flag) throws CommandSyntaxException {
        OptionalInt optionalint = checkRegions(commandcontext, flag);

        if (optionalint.isPresent()) {
            ((CommandListenerWrapper) commandcontext.getSource()).sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.execute.conditional.pass_count", optionalint.getAsInt());
            }, false);
            return optionalint.getAsInt();
        } else {
            throw CommandExecute.ERROR_CONDITIONAL_FAILED.create();
        }
    }

    private static int checkUnlessRegions(CommandContext<CommandListenerWrapper> commandcontext, boolean flag) throws CommandSyntaxException {
        OptionalInt optionalint = checkRegions(commandcontext, flag);

        if (optionalint.isPresent()) {
            throw CommandExecute.ERROR_CONDITIONAL_FAILED_COUNT.create(optionalint.getAsInt());
        } else {
            ((CommandListenerWrapper) commandcontext.getSource()).sendSuccess(() -> {
                return IChatBaseComponent.translatable("commands.execute.conditional.pass");
            }, false);
            return 1;
        }
    }

    private static OptionalInt checkRegions(CommandContext<CommandListenerWrapper> commandcontext, boolean flag) throws CommandSyntaxException {
        return checkRegions(((CommandListenerWrapper) commandcontext.getSource()).getLevel(), ArgumentPosition.getLoadedBlockPos(commandcontext, "start"), ArgumentPosition.getLoadedBlockPos(commandcontext, "end"), ArgumentPosition.getLoadedBlockPos(commandcontext, "destination"), flag);
    }

    private static OptionalInt checkRegions(WorldServer worldserver, BlockPosition blockposition, BlockPosition blockposition1, BlockPosition blockposition2, boolean flag) throws CommandSyntaxException {
        StructureBoundingBox structureboundingbox = StructureBoundingBox.fromCorners(blockposition, blockposition1);
        StructureBoundingBox structureboundingbox1 = StructureBoundingBox.fromCorners(blockposition2, blockposition2.offset(structureboundingbox.getLength()));
        BlockPosition blockposition3 = new BlockPosition(structureboundingbox1.minX() - structureboundingbox.minX(), structureboundingbox1.minY() - structureboundingbox.minY(), structureboundingbox1.minZ() - structureboundingbox.minZ());
        int i = structureboundingbox.getXSpan() * structureboundingbox.getYSpan() * structureboundingbox.getZSpan();

        if (i > 32768) {
            throw CommandExecute.ERROR_AREA_TOO_LARGE.create(32768, i);
        } else {
            IRegistryCustom iregistrycustom = worldserver.registryAccess();
            int j = 0;

            for (int k = structureboundingbox.minZ(); k <= structureboundingbox.maxZ(); ++k) {
                for (int l = structureboundingbox.minY(); l <= structureboundingbox.maxY(); ++l) {
                    for (int i1 = structureboundingbox.minX(); i1 <= structureboundingbox.maxX(); ++i1) {
                        BlockPosition blockposition4 = new BlockPosition(i1, l, k);
                        BlockPosition blockposition5 = blockposition4.offset(blockposition3);
                        IBlockData iblockdata = worldserver.getBlockState(blockposition4);

                        if (!flag || !iblockdata.is(Blocks.AIR)) {
                            if (iblockdata != worldserver.getBlockState(blockposition5)) {
                                return OptionalInt.empty();
                            }

                            TileEntity tileentity = worldserver.getBlockEntity(blockposition4);
                            TileEntity tileentity1 = worldserver.getBlockEntity(blockposition5);

                            if (tileentity != null) {
                                if (tileentity1 == null) {
                                    return OptionalInt.empty();
                                }

                                if (tileentity1.getType() != tileentity.getType()) {
                                    return OptionalInt.empty();
                                }

                                if (!tileentity.components().equals(tileentity1.components())) {
                                    return OptionalInt.empty();
                                }

                                NBTTagCompound nbttagcompound = tileentity.saveCustomOnly(iregistrycustom);
                                NBTTagCompound nbttagcompound1 = tileentity1.saveCustomOnly(iregistrycustom);

                                if (!nbttagcompound.equals(nbttagcompound1)) {
                                    return OptionalInt.empty();
                                }
                            }

                            ++j;
                        }
                    }
                }
            }

            return OptionalInt.of(j);
        }
    }

    private static RedirectModifier<CommandListenerWrapper> expandOneToOneEntityRelation(Function<Entity, Optional<Entity>> function) {
        return (commandcontext) -> {
            CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
            Entity entity = commandlistenerwrapper.getEntity();

            return (Collection) (entity == null ? List.of() : (Collection) ((Optional) function.apply(entity)).filter((entity1) -> {
                return !entity1.isRemoved();
            }).map((entity1) -> {
                return List.of(commandlistenerwrapper.withEntity(entity1));
            }).orElse(List.of()));
        };
    }

    private static RedirectModifier<CommandListenerWrapper> expandOneToManyEntityRelation(Function<Entity, Stream<Entity>> function) {
        return (commandcontext) -> {
            CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
            Entity entity = commandlistenerwrapper.getEntity();

            if (entity == null) {
                return List.of();
            } else {
                Stream stream = ((Stream) function.apply(entity)).filter((entity1) -> {
                    return !entity1.isRemoved();
                });

                Objects.requireNonNull(commandlistenerwrapper);
                return stream.map(commandlistenerwrapper::withEntity).toList();
            }
        };
    }

    private static LiteralArgumentBuilder<CommandListenerWrapper> createRelationOperations(CommandNode<CommandListenerWrapper> commandnode, LiteralArgumentBuilder<CommandListenerWrapper> literalargumentbuilder) {
        return (LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) literalargumentbuilder.then(net.minecraft.commands.CommandDispatcher.literal("owner").fork(commandnode, expandOneToOneEntityRelation((entity) -> {
            Optional optional;

            if (entity instanceof OwnableEntity ownableentity) {
                optional = Optional.ofNullable(ownableentity.getOwner());
            } else {
                optional = Optional.empty();
            }

            return optional;
        })))).then(net.minecraft.commands.CommandDispatcher.literal("leasher").fork(commandnode, expandOneToOneEntityRelation((entity) -> {
            Optional optional;

            if (entity instanceof Leashable leashable) {
                optional = Optional.ofNullable(leashable.getLeashHolder());
            } else {
                optional = Optional.empty();
            }

            return optional;
        })))).then(net.minecraft.commands.CommandDispatcher.literal("target").fork(commandnode, expandOneToOneEntityRelation((entity) -> {
            Optional optional;

            if (entity instanceof Targeting targeting) {
                optional = Optional.ofNullable(targeting.getTarget());
            } else {
                optional = Optional.empty();
            }

            return optional;
        })))).then(net.minecraft.commands.CommandDispatcher.literal("attacker").fork(commandnode, expandOneToOneEntityRelation((entity) -> {
            Optional optional;

            if (entity instanceof Attackable attackable) {
                optional = Optional.ofNullable(attackable.getLastAttacker());
            } else {
                optional = Optional.empty();
            }

            return optional;
        })))).then(net.minecraft.commands.CommandDispatcher.literal("vehicle").fork(commandnode, expandOneToOneEntityRelation((entity) -> {
            return Optional.ofNullable(entity.getVehicle());
        })))).then(net.minecraft.commands.CommandDispatcher.literal("controller").fork(commandnode, expandOneToOneEntityRelation((entity) -> {
            return Optional.ofNullable(entity.getControllingPassenger());
        })))).then(net.minecraft.commands.CommandDispatcher.literal("origin").fork(commandnode, expandOneToOneEntityRelation((entity) -> {
            Optional optional;

            if (entity instanceof TraceableEntity traceableentity) {
                optional = Optional.ofNullable(traceableentity.getOwner());
            } else {
                optional = Optional.empty();
            }

            return optional;
        })))).then(net.minecraft.commands.CommandDispatcher.literal("passengers").fork(commandnode, expandOneToManyEntityRelation((entity) -> {
            return entity.getPassengers().stream();
        })));
    }

    private static CommandListenerWrapper spawnEntityAndRedirect(CommandListenerWrapper commandlistenerwrapper, Holder.c<EntityTypes<?>> holder_c) throws CommandSyntaxException {
        Entity entity = CommandSummon.createEntity(commandlistenerwrapper, holder_c, commandlistenerwrapper.getPosition(), new NBTTagCompound(), true);

        return commandlistenerwrapper.withEntity(entity);
    }

    public static <T extends ExecutionCommandSource<T>> void scheduleFunctionConditionsAndTest(T t0, List<T> list, Function<T, T> function, IntPredicate intpredicate, ContextChain<T> contextchain, @Nullable NBTTagCompound nbttagcompound, ExecutionControl<T> executioncontrol, CommandExecute.a<T, Collection<net.minecraft.commands.functions.CommandFunction<T>>> commandexecute_a, ChainModifiers chainmodifiers) {
        List<T> list1 = new ArrayList(list.size());

        Collection collection;

        try {
            collection = (Collection) commandexecute_a.get(contextchain.getTopContext().copyFor(t0));
        } catch (CommandSyntaxException commandsyntaxexception) {
            t0.handleError(commandsyntaxexception, chainmodifiers.isForked(), executioncontrol.tracer());
            return;
        }

        int i = collection.size();

        if (i != 0) {
            List<InstantiatedFunction<T>> list2 = new ArrayList(i);

            Iterator iterator;

            try {
                iterator = collection.iterator();

                while (iterator.hasNext()) {
                    net.minecraft.commands.functions.CommandFunction<T> net_minecraft_commands_functions_commandfunction = (net.minecraft.commands.functions.CommandFunction) iterator.next();

                    try {
                        list2.add(net_minecraft_commands_functions_commandfunction.instantiate(nbttagcompound, t0.dispatcher()));
                    } catch (FunctionInstantiationException functioninstantiationexception) {
                        throw CommandExecute.ERROR_FUNCTION_CONDITION_INSTANTATION_FAILURE.create(net_minecraft_commands_functions_commandfunction.id(), functioninstantiationexception.messageComponent());
                    }
                }
            } catch (CommandSyntaxException commandsyntaxexception1) {
                t0.handleError(commandsyntaxexception1, chainmodifiers.isForked(), executioncontrol.tracer());
            }

            iterator = list.iterator();

            while (iterator.hasNext()) {
                T t1 = (ExecutionCommandSource) iterator.next();
                T t2 = (ExecutionCommandSource) function.apply(t1.clearCallbacks());
                CommandResultCallback commandresultcallback = (flag, j) -> {
                    if (intpredicate.test(j)) {
                        list1.add(t1);
                    }

                };

                executioncontrol.queueNext(new IsolatedCall<>((executioncontrol1) -> {
                    Iterator iterator1 = list2.iterator();

                    while (iterator1.hasNext()) {
                        InstantiatedFunction<T> instantiatedfunction = (InstantiatedFunction) iterator1.next();

                        executioncontrol1.queueNext((new CallFunction<>(instantiatedfunction, executioncontrol1.currentFrame().returnValueConsumer(), true)).bind(t2));
                    }

                    executioncontrol1.queueNext(FallthroughTask.instance());
                }, commandresultcallback));
            }

            ContextChain<T> contextchain1 = contextchain.nextStage();
            String s = contextchain.getTopContext().getInput();

            executioncontrol.queueNext(new BuildContexts.a<>(s, contextchain1, chainmodifiers, t0, list1));
        }
    }

    @FunctionalInterface
    private interface c {

        boolean test(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException;
    }

    @FunctionalInterface
    private interface b {

        int test(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException;
    }

    private static class d implements CustomModifierExecutor.a<CommandListenerWrapper> {

        private final IntPredicate check;

        d(boolean flag) {
            this.check = flag ? (i) -> {
                return i != 0;
            } : (i) -> {
                return i == 0;
            };
        }

        public void apply(CommandListenerWrapper commandlistenerwrapper, List<CommandListenerWrapper> list, ContextChain<CommandListenerWrapper> contextchain, ChainModifiers chainmodifiers, ExecutionControl<CommandListenerWrapper> executioncontrol) {
            CommandExecute.scheduleFunctionConditionsAndTest(commandlistenerwrapper, list, CommandFunction::modifySenderForExecution, this.check, contextchain, (NBTTagCompound) null, executioncontrol, (commandcontext) -> {
                return ArgumentTag.getFunctions(commandcontext, "name");
            }, chainmodifiers);
        }
    }

    @FunctionalInterface
    private interface e {

        boolean test(int i, int j);
    }

    @FunctionalInterface
    public interface a<T, R> {

        R get(CommandContext<T> commandcontext) throws CommandSyntaxException;
    }
}
