package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.SystemUtils;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.thread.ThreadedMailbox;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunkExtension;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public class ResetChunksCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    public ResetChunksCommand() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("resetchunks").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).executes((commandcontext) -> {
            return resetChunks((CommandListenerWrapper) commandcontext.getSource(), 0, true);
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("range", IntegerArgumentType.integer(0, 5)).executes((commandcontext) -> {
            return resetChunks((CommandListenerWrapper) commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "range"), true);
        })).then(net.minecraft.commands.CommandDispatcher.argument("skipOldChunks", BoolArgumentType.bool()).executes((commandcontext) -> {
            return resetChunks((CommandListenerWrapper) commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "range"), BoolArgumentType.getBool(commandcontext, "skipOldChunks"));
        }))));
    }

    private static int resetChunks(CommandListenerWrapper commandlistenerwrapper, int i, boolean flag) {
        WorldServer worldserver = commandlistenerwrapper.getLevel();
        ChunkProviderServer chunkproviderserver = worldserver.getChunkSource();

        chunkproviderserver.chunkMap.debugReloadGenerator();
        Vec3D vec3d = commandlistenerwrapper.getPosition();
        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(BlockPosition.containing(vec3d));
        int j = chunkcoordintpair.z - i;
        int k = chunkcoordintpair.z + i;
        int l = chunkcoordintpair.x - i;
        int i1 = chunkcoordintpair.x + i;

        for (int j1 = j; j1 <= k; ++j1) {
            for (int k1 = l; k1 <= i1; ++k1) {
                ChunkCoordIntPair chunkcoordintpair1 = new ChunkCoordIntPair(k1, j1);
                Chunk chunk = chunkproviderserver.getChunk(k1, j1, false);

                if (chunk != null && (!flag || !chunk.isOldNoiseGeneration())) {
                    Iterator iterator = BlockPosition.betweenClosed(chunkcoordintpair1.getMinBlockX(), worldserver.getMinBuildHeight(), chunkcoordintpair1.getMinBlockZ(), chunkcoordintpair1.getMaxBlockX(), worldserver.getMaxBuildHeight() - 1, chunkcoordintpair1.getMaxBlockZ()).iterator();

                    while (iterator.hasNext()) {
                        BlockPosition blockposition = (BlockPosition) iterator.next();

                        worldserver.setBlock(blockposition, Blocks.AIR.defaultBlockState(), 16);
                    }
                }
            }
        }

        ThreadedMailbox<Runnable> threadedmailbox = ThreadedMailbox.create(SystemUtils.backgroundExecutor(), "worldgen-resetchunks");
        long l1 = System.currentTimeMillis();
        int i2 = (i * 2 + 1) * (i * 2 + 1);
        UnmodifiableIterator unmodifiableiterator = ImmutableList.of(ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.FEATURES, ChunkStatus.INITIALIZE_LIGHT).iterator();

        long j2;

        while (unmodifiableiterator.hasNext()) {
            ChunkStatus chunkstatus = (ChunkStatus) unmodifiableiterator.next();

            j2 = System.currentTimeMillis();
            Supplier supplier = () -> {
                return Unit.INSTANCE;
            };

            Objects.requireNonNull(threadedmailbox);
            CompletableFuture<Unit> completablefuture = CompletableFuture.supplyAsync(supplier, threadedmailbox::tell);
            WorldGenContext worldgencontext = new WorldGenContext(worldserver, chunkproviderserver.getGenerator(), worldserver.getStructureManager(), chunkproviderserver.getLightEngine());

            for (int k2 = chunkcoordintpair.z - i; k2 <= chunkcoordintpair.z + i; ++k2) {
                for (int l2 = chunkcoordintpair.x - i; l2 <= chunkcoordintpair.x + i; ++l2) {
                    ChunkCoordIntPair chunkcoordintpair2 = new ChunkCoordIntPair(l2, k2);
                    Chunk chunk1 = chunkproviderserver.getChunk(l2, k2, false);

                    if (chunk1 != null && (!flag || !chunk1.isOldNoiseGeneration())) {
                        List<IChunkAccess> list = Lists.newArrayList();
                        int i3 = Math.max(1, chunkstatus.getRange());

                        for (int j3 = chunkcoordintpair2.z - i3; j3 <= chunkcoordintpair2.z + i3; ++j3) {
                            for (int k3 = chunkcoordintpair2.x - i3; k3 <= chunkcoordintpair2.x + i3; ++k3) {
                                IChunkAccess ichunkaccess = chunkproviderserver.getChunk(k3, j3, chunkstatus.getParent(), true);
                                Object object;

                                if (ichunkaccess instanceof ProtoChunkExtension) {
                                    object = new ProtoChunkExtension(((ProtoChunkExtension) ichunkaccess).getWrapped(), true);
                                } else if (ichunkaccess instanceof Chunk) {
                                    object = new ProtoChunkExtension((Chunk) ichunkaccess, true);
                                } else {
                                    object = ichunkaccess;
                                }

                                list.add(object);
                            }
                        }

                        Function function = (unit) -> {
                            Objects.requireNonNull(threadedmailbox);
                            return chunkstatus.generate(worldgencontext, threadedmailbox::tell, (ichunkaccess1) -> {
                                throw new UnsupportedOperationException("Not creating full chunks here");
                            }, list).thenApply((ichunkaccess1) -> {
                                if (chunkstatus == ChunkStatus.NOISE) {
                                    HeightMap.primeHeightmaps(ichunkaccess1, ChunkStatus.POST_FEATURES);
                                }

                                return Unit.INSTANCE;
                            });
                        };

                        Objects.requireNonNull(threadedmailbox);
                        completablefuture = completablefuture.thenComposeAsync(function, threadedmailbox::tell);
                    }
                }
            }

            MinecraftServer minecraftserver = commandlistenerwrapper.getServer();

            Objects.requireNonNull(completablefuture);
            minecraftserver.managedBlock(completablefuture::isDone);
            Logger logger = ResetChunksCommand.LOGGER;
            String s = String.valueOf(chunkstatus);

            logger.debug(s + " took " + (System.currentTimeMillis() - j2) + " ms");
        }

        long l3 = System.currentTimeMillis();

        for (int i4 = chunkcoordintpair.z - i; i4 <= chunkcoordintpair.z + i; ++i4) {
            for (int j4 = chunkcoordintpair.x - i; j4 <= chunkcoordintpair.x + i; ++j4) {
                ChunkCoordIntPair chunkcoordintpair3 = new ChunkCoordIntPair(j4, i4);
                Chunk chunk2 = chunkproviderserver.getChunk(j4, i4, false);

                if (chunk2 != null && (!flag || !chunk2.isOldNoiseGeneration())) {
                    Iterator iterator1 = BlockPosition.betweenClosed(chunkcoordintpair3.getMinBlockX(), worldserver.getMinBuildHeight(), chunkcoordintpair3.getMinBlockZ(), chunkcoordintpair3.getMaxBlockX(), worldserver.getMaxBuildHeight() - 1, chunkcoordintpair3.getMaxBlockZ()).iterator();

                    while (iterator1.hasNext()) {
                        BlockPosition blockposition1 = (BlockPosition) iterator1.next();

                        chunkproviderserver.blockChanged(blockposition1);
                    }
                }
            }
        }

        ResetChunksCommand.LOGGER.debug("blockChanged took " + (System.currentTimeMillis() - l3) + " ms");
        j2 = System.currentTimeMillis() - l1;
        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.literal(String.format(Locale.ROOT, "%d chunks have been reset. This took %d ms for %d chunks, or %02f ms per chunk", i2, j2, i2, (float) j2 / (float) i2));
        }, true);
        return 1;
    }
}
