package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.SectionPosition;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EntityComplexPart;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewer;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.crafting.CraftingManager;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockFireAbstract;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.IChunkProvider;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.WorldDataMutable;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.scores.Scoreboard;

// CraftBukkit start
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.level.border.IWorldBorderListener;
import net.minecraft.world.level.dimension.WorldDimension;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CapturedBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.util.CraftSpawnCategory;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.block.BlockPhysicsEvent;
// CraftBukkit end

public abstract class World implements GeneratorAccess, AutoCloseable {

    public static final Codec<ResourceKey<World>> RESOURCE_KEY_CODEC = ResourceKey.codec(Registries.DIMENSION);
    public static final ResourceKey<World> OVERWORLD = ResourceKey.create(Registries.DIMENSION, new MinecraftKey("overworld"));
    public static final ResourceKey<World> NETHER = ResourceKey.create(Registries.DIMENSION, new MinecraftKey("the_nether"));
    public static final ResourceKey<World> END = ResourceKey.create(Registries.DIMENSION, new MinecraftKey("the_end"));
    public static final int MAX_LEVEL_SIZE = 30000000;
    public static final int LONG_PARTICLE_CLIP_RANGE = 512;
    public static final int SHORT_PARTICLE_CLIP_RANGE = 32;
    public static final int MAX_BRIGHTNESS = 15;
    public static final int TICKS_PER_DAY = 24000;
    public static final int MAX_ENTITY_SPAWN_Y = 20000000;
    public static final int MIN_ENTITY_SPAWN_Y = -20000000;
    protected final List<TickingBlockEntity> blockEntityTickers = Lists.newArrayList();
    protected final NeighborUpdater neighborUpdater;
    private final List<TickingBlockEntity> pendingBlockEntityTickers = Lists.newArrayList();
    private boolean tickingBlockEntities;
    public final Thread thread;
    private final boolean isDebug;
    private int skyDarken;
    protected int randValue = RandomSource.create().nextInt();
    protected final int addend = 1013904223;
    protected float oRainLevel;
    public float rainLevel;
    protected float oThunderLevel;
    public float thunderLevel;
    public final RandomSource random = RandomSource.create();
    /** @deprecated */
    @Deprecated
    private final RandomSource threadSafeRandom = RandomSource.createThreadSafe();
    private final Holder<DimensionManager> dimensionTypeRegistration;
    public final WorldDataMutable levelData;
    private final Supplier<GameProfilerFiller> profiler;
    public final boolean isClientSide;
    private final WorldBorder worldBorder;
    private final BiomeManager biomeManager;
    private final ResourceKey<World> dimension;
    private final IRegistryCustom registryAccess;
    private final DamageSources damageSources;
    private long subTickCount;

    // CraftBukkit start Added the following
    private final CraftWorld world;
    public boolean pvpMode;
    public org.bukkit.generator.ChunkGenerator generator;

    public boolean preventPoiUpdated = false; // CraftBukkit - SPIGOT-5710
    public boolean captureBlockStates = false;
    public boolean captureTreeGeneration = false;
    public Map<BlockPosition, CapturedBlockState> capturedBlockStates = new java.util.LinkedHashMap<>();
    public Map<BlockPosition, TileEntity> capturedTileEntities = new HashMap<>();
    public List<EntityItem> captureDrops;
    public final it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<SpawnCategory> ticksPerSpawnCategory = new it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap<>();
    public boolean populating;

    public CraftWorld getWorld() {
        return this.world;
    }

    public CraftServer getCraftServer() {
        return (CraftServer) Bukkit.getServer();
    }

    public abstract ResourceKey<WorldDimension> getTypeKey();

    protected World(WorldDataMutable worlddatamutable, ResourceKey<World> resourcekey, IRegistryCustom iregistrycustom, Holder<DimensionManager> holder, Supplier<GameProfilerFiller> supplier, boolean flag, boolean flag1, long i, int j, org.bukkit.generator.ChunkGenerator gen, org.bukkit.generator.BiomeProvider biomeProvider, org.bukkit.World.Environment env) {
        this.generator = gen;
        this.world = new CraftWorld((WorldServer) this, gen, biomeProvider, env);

        // CraftBukkit Ticks things
        for (SpawnCategory spawnCategory : SpawnCategory.values()) {
            if (CraftSpawnCategory.isValidForLimits(spawnCategory)) {
                this.ticksPerSpawnCategory.put(spawnCategory, (long) this.getCraftServer().getTicksPerSpawns(spawnCategory));
            }
        }

        // CraftBukkit end
        this.profiler = supplier;
        this.levelData = worlddatamutable;
        this.dimensionTypeRegistration = holder;
        final DimensionManager dimensionmanager = (DimensionManager) holder.value();

        this.dimension = resourcekey;
        this.isClientSide = flag;
        if (dimensionmanager.coordinateScale() != 1.0D) {
            this.worldBorder = new WorldBorder() { // CraftBukkit - decompile error
                @Override
                public double getCenterX() {
                    return super.getCenterX(); // CraftBukkit
                }

                @Override
                public double getCenterZ() {
                    return super.getCenterZ(); // CraftBukkit
                }
            };
        } else {
            this.worldBorder = new WorldBorder();
        }

        this.thread = Thread.currentThread();
        this.biomeManager = new BiomeManager(this, i);
        this.isDebug = flag1;
        this.neighborUpdater = new CollectingNeighborUpdater(this, j);
        this.registryAccess = iregistrycustom;
        this.damageSources = new DamageSources(iregistrycustom);
        // CraftBukkit start
        getWorldBorder().world = (WorldServer) this;
        // From PlayerList.setPlayerFileData
        getWorldBorder().addListener(new IWorldBorderListener() {
            @Override
            public void onBorderSizeSet(WorldBorder worldborder, double d0) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderSizePacket(worldborder), worldborder.world);
            }

            @Override
            public void onBorderSizeLerping(WorldBorder worldborder, double d0, double d1, long i) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderLerpSizePacket(worldborder), worldborder.world);
            }

            @Override
            public void onBorderCenterSet(WorldBorder worldborder, double d0, double d1) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderCenterPacket(worldborder), worldborder.world);
            }

            @Override
            public void onBorderSetWarningTime(WorldBorder worldborder, int i) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderWarningDelayPacket(worldborder), worldborder.world);
            }

            @Override
            public void onBorderSetWarningBlocks(WorldBorder worldborder, int i) {
                getCraftServer().getHandle().broadcastAll(new ClientboundSetBorderWarningDistancePacket(worldborder), worldborder.world);
            }

            @Override
            public void onBorderSetDamagePerBlock(WorldBorder worldborder, double d0) {}

            @Override
            public void onBorderSetDamageSafeZOne(WorldBorder worldborder, double d0) {}
        });
        // CraftBukkit end
    }

    @Override
    public boolean isClientSide() {
        return this.isClientSide;
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return null;
    }

    public boolean isInWorldBounds(BlockPosition blockposition) {
        return !this.isOutsideBuildHeight(blockposition) && isInWorldBoundsHorizontal(blockposition);
    }

    public static boolean isInSpawnableBounds(BlockPosition blockposition) {
        return !isOutsideSpawnableHeight(blockposition.getY()) && isInWorldBoundsHorizontal(blockposition);
    }

    private static boolean isInWorldBoundsHorizontal(BlockPosition blockposition) {
        return blockposition.getX() >= -30000000 && blockposition.getZ() >= -30000000 && blockposition.getX() < 30000000 && blockposition.getZ() < 30000000;
    }

    private static boolean isOutsideSpawnableHeight(int i) {
        return i < -20000000 || i >= 20000000;
    }

    public Chunk getChunkAt(BlockPosition blockposition) {
        return this.getChunk(SectionPosition.blockToSectionCoord(blockposition.getX()), SectionPosition.blockToSectionCoord(blockposition.getZ()));
    }

    @Override
    public Chunk getChunk(int i, int j) {
        return (Chunk) this.getChunk(i, j, ChunkStatus.FULL);
    }

    @Nullable
    @Override
    public IChunkAccess getChunk(int i, int j, ChunkStatus chunkstatus, boolean flag) {
        IChunkAccess ichunkaccess = this.getChunkSource().getChunk(i, j, chunkstatus, flag);

        if (ichunkaccess == null && flag) {
            throw new IllegalStateException("Should always be able to create a chunk!");
        } else {
            return ichunkaccess;
        }
    }

    @Override
    public boolean setBlock(BlockPosition blockposition, IBlockData iblockdata, int i) {
        return this.setBlock(blockposition, iblockdata, i, 512);
    }

    @Override
    public boolean setBlock(BlockPosition blockposition, IBlockData iblockdata, int i, int j) {
        // CraftBukkit start - tree generation
        if (this.captureTreeGeneration) {
            CapturedBlockState blockstate = capturedBlockStates.get(blockposition);
            if (blockstate == null) {
                blockstate = CapturedBlockState.getTreeBlockState(this, blockposition, i);
                this.capturedBlockStates.put(blockposition.immutable(), blockstate);
            }
            blockstate.setData(iblockdata);
            blockstate.setFlag(i);
            return true;
        }
        // CraftBukkit end
        if (this.isOutsideBuildHeight(blockposition)) {
            return false;
        } else if (!this.isClientSide && this.isDebug()) {
            return false;
        } else {
            Chunk chunk = this.getChunkAt(blockposition);
            Block block = iblockdata.getBlock();

            // CraftBukkit start - capture blockstates
            boolean captured = false;
            if (this.captureBlockStates && !this.capturedBlockStates.containsKey(blockposition)) {
                CapturedBlockState blockstate = CapturedBlockState.getBlockState(this, blockposition, i);
                this.capturedBlockStates.put(blockposition.immutable(), blockstate);
                captured = true;
            }
            // CraftBukkit end

            IBlockData iblockdata1 = chunk.setBlockState(blockposition, iblockdata, (i & 64) != 0, (i & 1024) == 0); // CraftBukkit custom NO_PLACE flag

            if (iblockdata1 == null) {
                // CraftBukkit start - remove blockstate if failed (or the same)
                if (this.captureBlockStates && captured) {
                    this.capturedBlockStates.remove(blockposition);
                }
                // CraftBukkit end
                return false;
            } else {
                IBlockData iblockdata2 = this.getBlockState(blockposition);

                /*
                if (iblockdata2 == iblockdata) {
                    if (iblockdata1 != iblockdata2) {
                        this.setBlocksDirty(blockposition, iblockdata1, iblockdata2);
                    }

                    if ((i & 2) != 0 && (!this.isClientSide || (i & 4) == 0) && (this.isClientSide || chunk.getFullStatus() != null && chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING))) {
                        this.sendBlockUpdated(blockposition, iblockdata1, iblockdata, i);
                    }

                    if ((i & 1) != 0) {
                        this.blockUpdated(blockposition, iblockdata1.getBlock());
                        if (!this.isClientSide && iblockdata.hasAnalogOutputSignal()) {
                            this.updateNeighbourForOutputSignal(blockposition, block);
                        }
                    }

                    if ((i & 16) == 0 && j > 0) {
                        int k = i & -34;

                        iblockdata1.updateIndirectNeighbourShapes(this, blockposition, k, j - 1);
                        iblockdata.updateNeighbourShapes(this, blockposition, k, j - 1);
                        iblockdata.updateIndirectNeighbourShapes(this, blockposition, k, j - 1);
                    }

                    this.onBlockStateChange(blockposition, iblockdata1, iblockdata2);
                }
                */

                // CraftBukkit start
                if (!this.captureBlockStates) { // Don't notify clients or update physics while capturing blockstates
                    // Modularize client and physic updates
                    notifyAndUpdatePhysics(blockposition, chunk, iblockdata1, iblockdata, iblockdata2, i, j);
                }
                // CraftBukkit end

                return true;
            }
        }
    }

    // CraftBukkit start - Split off from above in order to directly send client and physic updates
    public void notifyAndUpdatePhysics(BlockPosition blockposition, Chunk chunk, IBlockData oldBlock, IBlockData newBlock, IBlockData actualBlock, int i, int j) {
        IBlockData iblockdata = newBlock;
        IBlockData iblockdata1 = oldBlock;
        IBlockData iblockdata2 = actualBlock;
        if (iblockdata2 == iblockdata) {
            if (iblockdata1 != iblockdata2) {
                this.setBlocksDirty(blockposition, iblockdata1, iblockdata2);
            }

            if ((i & 2) != 0 && (!this.isClientSide || (i & 4) == 0) && (this.isClientSide || chunk == null || (chunk.getFullStatus() != null && chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING)))) { // allow chunk to be null here as chunk.isReady() is false when we send our notification during block placement
                this.sendBlockUpdated(blockposition, iblockdata1, iblockdata, i);
            }

            if ((i & 1) != 0) {
                this.blockUpdated(blockposition, iblockdata1.getBlock());
                if (!this.isClientSide && iblockdata.hasAnalogOutputSignal()) {
                    this.updateNeighbourForOutputSignal(blockposition, newBlock.getBlock());
                }
            }

            if ((i & 16) == 0 && j > 0) {
                int k = i & -34;

                // CraftBukkit start
                iblockdata1.updateIndirectNeighbourShapes(this, blockposition, k, j - 1); // Don't call an event for the old block to limit event spam
                CraftWorld world = ((WorldServer) this).getWorld();
                if (world != null) {
                    BlockPhysicsEvent event = new BlockPhysicsEvent(world.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), CraftBlockData.fromData(iblockdata));
                    this.getCraftServer().getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        return;
                    }
                }
                // CraftBukkit end
                iblockdata.updateNeighbourShapes(this, blockposition, k, j - 1);
                iblockdata.updateIndirectNeighbourShapes(this, blockposition, k, j - 1);
            }

            // CraftBukkit start - SPIGOT-5710
            if (!preventPoiUpdated) {
                this.onBlockStateChange(blockposition, iblockdata1, iblockdata2);
            }
            // CraftBukkit end
        }
    }
    // CraftBukkit end

    public void onBlockStateChange(BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1) {}

    @Override
    public boolean removeBlock(BlockPosition blockposition, boolean flag) {
        Fluid fluid = this.getFluidState(blockposition);

        return this.setBlock(blockposition, fluid.createLegacyBlock(), 3 | (flag ? 64 : 0));
    }

    @Override
    public boolean destroyBlock(BlockPosition blockposition, boolean flag, @Nullable Entity entity, int i) {
        IBlockData iblockdata = this.getBlockState(blockposition);

        if (iblockdata.isAir()) {
            return false;
        } else {
            Fluid fluid = this.getFluidState(blockposition);

            if (!(iblockdata.getBlock() instanceof BlockFireAbstract)) {
                this.levelEvent(2001, blockposition, Block.getId(iblockdata));
            }

            if (flag) {
                TileEntity tileentity = iblockdata.hasBlockEntity() ? this.getBlockEntity(blockposition) : null;

                Block.dropResources(iblockdata, this, blockposition, tileentity, entity, ItemStack.EMPTY);
            }

            boolean flag1 = this.setBlock(blockposition, fluid.createLegacyBlock(), 3, i);

            if (flag1) {
                this.gameEvent((Holder) GameEvent.BLOCK_DESTROY, blockposition, GameEvent.a.of(entity, iblockdata));
            }

            return flag1;
        }
    }

    public void addDestroyBlockEffect(BlockPosition blockposition, IBlockData iblockdata) {}

    public boolean setBlockAndUpdate(BlockPosition blockposition, IBlockData iblockdata) {
        return this.setBlock(blockposition, iblockdata, 3);
    }

    public abstract void sendBlockUpdated(BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1, int i);

    public void setBlocksDirty(BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1) {}

    public void updateNeighborsAt(BlockPosition blockposition, Block block) {}

    public void updateNeighborsAtExceptFromFacing(BlockPosition blockposition, Block block, EnumDirection enumdirection) {}

    public void neighborChanged(BlockPosition blockposition, Block block, BlockPosition blockposition1) {}

    public void neighborChanged(IBlockData iblockdata, BlockPosition blockposition, Block block, BlockPosition blockposition1, boolean flag) {}

    @Override
    public void neighborShapeChanged(EnumDirection enumdirection, IBlockData iblockdata, BlockPosition blockposition, BlockPosition blockposition1, int i, int j) {
        this.neighborUpdater.shapeUpdate(enumdirection, iblockdata, blockposition, blockposition1, i, j);
    }

    @Override
    public int getHeight(HeightMap.Type heightmap_type, int i, int j) {
        int k;

        if (i >= -30000000 && j >= -30000000 && i < 30000000 && j < 30000000) {
            if (this.hasChunk(SectionPosition.blockToSectionCoord(i), SectionPosition.blockToSectionCoord(j))) {
                k = this.getChunk(SectionPosition.blockToSectionCoord(i), SectionPosition.blockToSectionCoord(j)).getHeight(heightmap_type, i & 15, j & 15) + 1;
            } else {
                k = this.getMinBuildHeight();
            }
        } else {
            k = this.getSeaLevel() + 1;
        }

        return k;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.getChunkSource().getLightEngine();
    }

    @Override
    public IBlockData getBlockState(BlockPosition blockposition) {
        // CraftBukkit start - tree generation
        if (captureTreeGeneration) {
            CapturedBlockState previous = capturedBlockStates.get(blockposition);
            if (previous != null) {
                return previous.getHandle();
            }
        }
        // CraftBukkit end
        if (this.isOutsideBuildHeight(blockposition)) {
            return Blocks.VOID_AIR.defaultBlockState();
        } else {
            Chunk chunk = this.getChunk(SectionPosition.blockToSectionCoord(blockposition.getX()), SectionPosition.blockToSectionCoord(blockposition.getZ()));

            return chunk.getBlockState(blockposition);
        }
    }

    @Override
    public Fluid getFluidState(BlockPosition blockposition) {
        if (this.isOutsideBuildHeight(blockposition)) {
            return FluidTypes.EMPTY.defaultFluidState();
        } else {
            Chunk chunk = this.getChunkAt(blockposition);

            return chunk.getFluidState(blockposition);
        }
    }

    public boolean isDay() {
        return !this.dimensionType().hasFixedTime() && this.skyDarken < 4;
    }

    public boolean isNight() {
        return !this.dimensionType().hasFixedTime() && !this.isDay();
    }

    public void playSound(@Nullable Entity entity, BlockPosition blockposition, SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1) {
        EntityHuman entityhuman;

        if (entity instanceof EntityHuman entityhuman1) {
            entityhuman = entityhuman1;
        } else {
            entityhuman = null;
        }

        this.playSound(entityhuman, blockposition, soundeffect, soundcategory, f, f1);
    }

    @Override
    public void playSound(@Nullable EntityHuman entityhuman, BlockPosition blockposition, SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1) {
        this.playSound(entityhuman, (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D, soundeffect, soundcategory, f, f1);
    }

    public abstract void playSeededSound(@Nullable EntityHuman entityhuman, double d0, double d1, double d2, Holder<SoundEffect> holder, SoundCategory soundcategory, float f, float f1, long i);

    public void playSeededSound(@Nullable EntityHuman entityhuman, double d0, double d1, double d2, SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1, long i) {
        this.playSeededSound(entityhuman, d0, d1, d2, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundeffect), soundcategory, f, f1, i);
    }

    public abstract void playSeededSound(@Nullable EntityHuman entityhuman, Entity entity, Holder<SoundEffect> holder, SoundCategory soundcategory, float f, float f1, long i);

    public void playSound(@Nullable EntityHuman entityhuman, double d0, double d1, double d2, SoundEffect soundeffect, SoundCategory soundcategory) {
        this.playSound(entityhuman, d0, d1, d2, soundeffect, soundcategory, 1.0F, 1.0F);
    }

    public void playSound(@Nullable EntityHuman entityhuman, double d0, double d1, double d2, SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1) {
        this.playSeededSound(entityhuman, d0, d1, d2, soundeffect, soundcategory, f, f1, this.threadSafeRandom.nextLong());
    }

    public void playSound(@Nullable EntityHuman entityhuman, Entity entity, SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1) {
        this.playSeededSound(entityhuman, entity, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundeffect), soundcategory, f, f1, this.threadSafeRandom.nextLong());
    }

    public void playLocalSound(BlockPosition blockposition, SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1, boolean flag) {
        this.playLocalSound((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D, soundeffect, soundcategory, f, f1, flag);
    }

    public void playLocalSound(Entity entity, SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1) {}

    public void playLocalSound(double d0, double d1, double d2, SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1, boolean flag) {}

    @Override
    public void addParticle(ParticleParam particleparam, double d0, double d1, double d2, double d3, double d4, double d5) {}

    public void addParticle(ParticleParam particleparam, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5) {}

    public void addAlwaysVisibleParticle(ParticleParam particleparam, double d0, double d1, double d2, double d3, double d4, double d5) {}

    public void addAlwaysVisibleParticle(ParticleParam particleparam, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5) {}

    public float getSunAngle(float f) {
        float f1 = this.getTimeOfDay(f);

        return f1 * 6.2831855F;
    }

    public void addBlockEntityTicker(TickingBlockEntity tickingblockentity) {
        (this.tickingBlockEntities ? this.pendingBlockEntityTickers : this.blockEntityTickers).add(tickingblockentity);
    }

    protected void tickBlockEntities() {
        GameProfilerFiller gameprofilerfiller = this.getProfiler();

        gameprofilerfiller.push("blockEntities");
        this.tickingBlockEntities = true;
        if (!this.pendingBlockEntityTickers.isEmpty()) {
            this.blockEntityTickers.addAll(this.pendingBlockEntityTickers);
            this.pendingBlockEntityTickers.clear();
        }

        Iterator<TickingBlockEntity> iterator = this.blockEntityTickers.iterator();
        boolean flag = this.tickRateManager().runsNormally();

        while (iterator.hasNext()) {
            TickingBlockEntity tickingblockentity = (TickingBlockEntity) iterator.next();

            if (tickingblockentity.isRemoved()) {
                iterator.remove();
            } else if (flag && this.shouldTickBlocksAt(tickingblockentity.getPos())) {
                tickingblockentity.tick();
            }
        }

        this.tickingBlockEntities = false;
        gameprofilerfiller.pop();
    }

    public <T extends Entity> void guardEntityTick(Consumer<T> consumer, T t0) {
        try {
            consumer.accept(t0);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking entity");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Entity being ticked");

            t0.fillCrashReportCategory(crashreportsystemdetails);
            throw new ReportedException(crashreport);
        }
    }

    public boolean shouldTickDeath(Entity entity) {
        return true;
    }

    public boolean shouldTickBlocksAt(long i) {
        return true;
    }

    public boolean shouldTickBlocksAt(BlockPosition blockposition) {
        return this.shouldTickBlocksAt(ChunkCoordIntPair.asLong(blockposition));
    }

    public Explosion explode(@Nullable Entity entity, double d0, double d1, double d2, float f, World.a world_a) {
        return this.explode(entity, Explosion.getDefaultDamageSource(this, entity), (ExplosionDamageCalculator) null, d0, d1, d2, f, false, world_a, Particles.EXPLOSION, Particles.EXPLOSION_EMITTER, SoundEffects.GENERIC_EXPLODE);
    }

    public Explosion explode(@Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag, World.a world_a) {
        return this.explode(entity, Explosion.getDefaultDamageSource(this, entity), (ExplosionDamageCalculator) null, d0, d1, d2, f, flag, world_a, Particles.EXPLOSION, Particles.EXPLOSION_EMITTER, SoundEffects.GENERIC_EXPLODE);
    }

    public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damagesource, @Nullable ExplosionDamageCalculator explosiondamagecalculator, Vec3D vec3d, float f, boolean flag, World.a world_a) {
        return this.explode(entity, damagesource, explosiondamagecalculator, vec3d.x(), vec3d.y(), vec3d.z(), f, flag, world_a, Particles.EXPLOSION, Particles.EXPLOSION_EMITTER, SoundEffects.GENERIC_EXPLODE);
    }

    public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damagesource, @Nullable ExplosionDamageCalculator explosiondamagecalculator, double d0, double d1, double d2, float f, boolean flag, World.a world_a) {
        return this.explode(entity, damagesource, explosiondamagecalculator, d0, d1, d2, f, flag, world_a, Particles.EXPLOSION, Particles.EXPLOSION_EMITTER, SoundEffects.GENERIC_EXPLODE);
    }

    public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damagesource, @Nullable ExplosionDamageCalculator explosiondamagecalculator, double d0, double d1, double d2, float f, boolean flag, World.a world_a, ParticleParam particleparam, ParticleParam particleparam1, Holder<SoundEffect> holder) {
        return this.explode(entity, damagesource, explosiondamagecalculator, d0, d1, d2, f, flag, world_a, true, particleparam, particleparam1, holder);
    }

    public Explosion explode(@Nullable Entity entity, @Nullable DamageSource damagesource, @Nullable ExplosionDamageCalculator explosiondamagecalculator, double d0, double d1, double d2, float f, boolean flag, World.a world_a, boolean flag1, ParticleParam particleparam, ParticleParam particleparam1, Holder<SoundEffect> holder) {
        Explosion.Effect explosion_effect;

        switch (world_a.ordinal()) {
            case 0:
                explosion_effect = Explosion.Effect.KEEP;
                break;
            case 1:
                explosion_effect = this.getDestroyType(GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY);
                break;
            case 2:
                explosion_effect = this.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? this.getDestroyType(GameRules.RULE_MOB_EXPLOSION_DROP_DECAY) : Explosion.Effect.KEEP;
                break;
            case 3:
                explosion_effect = this.getDestroyType(GameRules.RULE_TNT_EXPLOSION_DROP_DECAY);
                break;
            case 4:
                explosion_effect = Explosion.Effect.TRIGGER_BLOCK;
                break;
            // CraftBukkit start - handle custom explosion type
            case 5:
                explosion_effect = Explosion.Effect.DESTROY;
                break;
            // CraftBukkit end
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        Explosion.Effect explosion_effect1 = explosion_effect;
        Explosion explosion = new Explosion(this, entity, damagesource, explosiondamagecalculator, d0, d1, d2, f, flag, explosion_effect1, particleparam, particleparam1, holder);

        explosion.explode();
        explosion.finalizeExplosion(flag1);
        return explosion;
    }

    private Explosion.Effect getDestroyType(GameRules.GameRuleKey<GameRules.GameRuleBoolean> gamerules_gamerulekey) {
        return this.getGameRules().getBoolean(gamerules_gamerulekey) ? Explosion.Effect.DESTROY_WITH_DECAY : Explosion.Effect.DESTROY;
    }

    public abstract String gatherChunkSourceStats();

    @Nullable
    @Override
    public TileEntity getBlockEntity(BlockPosition blockposition) {
        // CraftBukkit start
        return getBlockEntity(blockposition, true);
    }

    @Nullable
    public TileEntity getBlockEntity(BlockPosition blockposition, boolean validate) {
        if (capturedTileEntities.containsKey(blockposition)) {
            return capturedTileEntities.get(blockposition);
        }
        // CraftBukkit end
        return this.isOutsideBuildHeight(blockposition) ? null : (!this.isClientSide && Thread.currentThread() != this.thread ? null : this.getChunkAt(blockposition).getBlockEntity(blockposition, Chunk.EnumTileEntityState.IMMEDIATE));
    }

    public void setBlockEntity(TileEntity tileentity) {
        BlockPosition blockposition = tileentity.getBlockPos();

        if (!this.isOutsideBuildHeight(blockposition)) {
            // CraftBukkit start
            if (captureBlockStates) {
                capturedTileEntities.put(blockposition.immutable(), tileentity);
                return;
            }
            // CraftBukkit end
            this.getChunkAt(blockposition).addAndRegisterBlockEntity(tileentity);
        }
    }

    public void removeBlockEntity(BlockPosition blockposition) {
        if (!this.isOutsideBuildHeight(blockposition)) {
            this.getChunkAt(blockposition).removeBlockEntity(blockposition);
        }
    }

    public boolean isLoaded(BlockPosition blockposition) {
        return this.isOutsideBuildHeight(blockposition) ? false : this.getChunkSource().hasChunk(SectionPosition.blockToSectionCoord(blockposition.getX()), SectionPosition.blockToSectionCoord(blockposition.getZ()));
    }

    public boolean loadedAndEntityCanStandOnFace(BlockPosition blockposition, Entity entity, EnumDirection enumdirection) {
        if (this.isOutsideBuildHeight(blockposition)) {
            return false;
        } else {
            IChunkAccess ichunkaccess = this.getChunk(SectionPosition.blockToSectionCoord(blockposition.getX()), SectionPosition.blockToSectionCoord(blockposition.getZ()), ChunkStatus.FULL, false);

            return ichunkaccess == null ? false : ichunkaccess.getBlockState(blockposition).entityCanStandOnFace(this, blockposition, entity, enumdirection);
        }
    }

    public boolean loadedAndEntityCanStandOn(BlockPosition blockposition, Entity entity) {
        return this.loadedAndEntityCanStandOnFace(blockposition, entity, EnumDirection.UP);
    }

    public void updateSkyBrightness() {
        double d0 = 1.0D - (double) (this.getRainLevel(1.0F) * 5.0F) / 16.0D;
        double d1 = 1.0D - (double) (this.getThunderLevel(1.0F) * 5.0F) / 16.0D;
        double d2 = 0.5D + 2.0D * MathHelper.clamp((double) MathHelper.cos(this.getTimeOfDay(1.0F) * 6.2831855F), -0.25D, 0.25D);

        this.skyDarken = (int) ((1.0D - d2 * d0 * d1) * 11.0D);
    }

    public void setSpawnSettings(boolean flag, boolean flag1) {
        this.getChunkSource().setSpawnSettings(flag, flag1);
    }

    public BlockPosition getSharedSpawnPos() {
        BlockPosition blockposition = this.levelData.getSpawnPos();

        if (!this.getWorldBorder().isWithinBounds(blockposition)) {
            blockposition = this.getHeightmapPos(HeightMap.Type.MOTION_BLOCKING, BlockPosition.containing(this.getWorldBorder().getCenterX(), 0.0D, this.getWorldBorder().getCenterZ()));
        }

        return blockposition;
    }

    public float getSharedSpawnAngle() {
        return this.levelData.getSpawnAngle();
    }

    protected void prepareWeather() {
        if (this.levelData.isRaining()) {
            this.rainLevel = 1.0F;
            if (this.levelData.isThundering()) {
                this.thunderLevel = 1.0F;
            }
        }

    }

    public void close() throws IOException {
        this.getChunkSource().close();
    }

    @Nullable
    @Override
    public IBlockAccess getChunkForCollisions(int i, int j) {
        return this.getChunk(i, j, ChunkStatus.FULL, false);
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AxisAlignedBB axisalignedbb, Predicate<? super Entity> predicate) {
        this.getProfiler().incrementCounter("getEntities");
        List<Entity> list = Lists.newArrayList();

        this.getEntities().get(axisalignedbb, (entity1) -> {
            if (entity1 != entity && predicate.test(entity1)) {
                list.add(entity1);
            }

            if (entity1 instanceof EntityEnderDragon) {
                EntityComplexPart[] aentitycomplexpart = ((EntityEnderDragon) entity1).getSubEntities();
                int i = aentitycomplexpart.length;

                for (int j = 0; j < i; ++j) {
                    EntityComplexPart entitycomplexpart = aentitycomplexpart[j];

                    if (entity1 != entity && predicate.test(entitycomplexpart)) {
                        list.add(entitycomplexpart);
                    }
                }
            }

        });
        return list;
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entitytypetest, AxisAlignedBB axisalignedbb, Predicate<? super T> predicate) {
        List<T> list = Lists.newArrayList();

        this.getEntities(entitytypetest, axisalignedbb, predicate, list);
        return list;
    }

    public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> entitytypetest, AxisAlignedBB axisalignedbb, Predicate<? super T> predicate, List<? super T> list) {
        this.getEntities(entitytypetest, axisalignedbb, predicate, list, Integer.MAX_VALUE);
    }

    public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> entitytypetest, AxisAlignedBB axisalignedbb, Predicate<? super T> predicate, List<? super T> list, int i) {
        this.getProfiler().incrementCounter("getEntities");
        this.getEntities().get(entitytypetest, axisalignedbb, (entity) -> {
            if (predicate.test(entity)) {
                list.add(entity);
                if (list.size() >= i) {
                    return AbortableIterationConsumer.a.ABORT;
                }
            }

            if (entity instanceof EntityEnderDragon entityenderdragon) {
                EntityComplexPart[] aentitycomplexpart = entityenderdragon.getSubEntities();
                int j = aentitycomplexpart.length;

                for (int k = 0; k < j; ++k) {
                    EntityComplexPart entitycomplexpart = aentitycomplexpart[k];
                    T t0 = entitytypetest.tryCast(entitycomplexpart); // CraftBukkit - decompile error

                    if (t0 != null && predicate.test(t0)) {
                        list.add(t0);
                        if (list.size() >= i) {
                            return AbortableIterationConsumer.a.ABORT;
                        }
                    }
                }
            }

            return AbortableIterationConsumer.a.CONTINUE;
        });
    }

    @Nullable
    public abstract Entity getEntity(int i);

    public void blockEntityChanged(BlockPosition blockposition) {
        if (this.hasChunkAt(blockposition)) {
            this.getChunkAt(blockposition).setUnsaved(true);
        }

    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    public void disconnect() {}

    public long getGameTime() {
        return this.levelData.getGameTime();
    }

    public long getDayTime() {
        return this.levelData.getDayTime();
    }

    public boolean mayInteract(EntityHuman entityhuman, BlockPosition blockposition) {
        return true;
    }

    public void broadcastEntityEvent(Entity entity, byte b0) {}

    public void broadcastDamageEvent(Entity entity, DamageSource damagesource) {}

    public void blockEvent(BlockPosition blockposition, Block block, int i, int j) {
        this.getBlockState(blockposition).triggerEvent(this, blockposition, i, j);
    }

    @Override
    public WorldData getLevelData() {
        return this.levelData;
    }

    public GameRules getGameRules() {
        return this.levelData.getGameRules();
    }

    public abstract TickRateManager tickRateManager();

    public float getThunderLevel(float f) {
        return MathHelper.lerp(f, this.oThunderLevel, this.thunderLevel) * this.getRainLevel(f);
    }

    public void setThunderLevel(float f) {
        float f1 = MathHelper.clamp(f, 0.0F, 1.0F);

        this.oThunderLevel = f1;
        this.thunderLevel = f1;
    }

    public float getRainLevel(float f) {
        return MathHelper.lerp(f, this.oRainLevel, this.rainLevel);
    }

    public void setRainLevel(float f) {
        float f1 = MathHelper.clamp(f, 0.0F, 1.0F);

        this.oRainLevel = f1;
        this.rainLevel = f1;
    }

    public boolean isThundering() {
        return this.dimensionType().hasSkyLight() && !this.dimensionType().hasCeiling() ? (double) this.getThunderLevel(1.0F) > 0.9D : false;
    }

    public boolean isRaining() {
        return (double) this.getRainLevel(1.0F) > 0.2D;
    }

    public boolean isRainingAt(BlockPosition blockposition) {
        if (!this.isRaining()) {
            return false;
        } else if (!this.canSeeSky(blockposition)) {
            return false;
        } else if (this.getHeightmapPos(HeightMap.Type.MOTION_BLOCKING, blockposition).getY() > blockposition.getY()) {
            return false;
        } else {
            BiomeBase biomebase = (BiomeBase) this.getBiome(blockposition).value();

            return biomebase.getPrecipitationAt(blockposition) == BiomeBase.Precipitation.RAIN;
        }
    }

    @Nullable
    public abstract WorldMap getMapData(MapId mapid);

    public abstract void setMapData(MapId mapid, WorldMap worldmap);

    public abstract MapId getFreeMapId();

    public void globalLevelEvent(int i, BlockPosition blockposition, int j) {}

    public CrashReportSystemDetails fillReportDetails(CrashReport crashreport) {
        CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Affected level", 1);

        crashreportsystemdetails.setDetail("All players", () -> {
            int i = this.players().size();

            return "" + i + " total; " + String.valueOf(this.players());
        });
        IChunkProvider ichunkprovider = this.getChunkSource();

        Objects.requireNonNull(ichunkprovider);
        crashreportsystemdetails.setDetail("Chunk stats", ichunkprovider::gatherStats);
        crashreportsystemdetails.setDetail("Level dimension", () -> {
            return this.dimension().location().toString();
        });

        try {
            this.levelData.fillCrashReportCategory(crashreportsystemdetails, this);
        } catch (Throwable throwable) {
            crashreportsystemdetails.setDetailError("Level Data Unobtainable", throwable);
        }

        return crashreportsystemdetails;
    }

    public abstract void destroyBlockProgress(int i, BlockPosition blockposition, int j);

    public void createFireworks(double d0, double d1, double d2, double d3, double d4, double d5, List<FireworkExplosion> list) {}

    public abstract Scoreboard getScoreboard();

    public void updateNeighbourForOutputSignal(BlockPosition blockposition, Block block) {
        Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumDirection enumdirection = (EnumDirection) iterator.next();
            BlockPosition blockposition1 = blockposition.relative(enumdirection);

            if (this.hasChunkAt(blockposition1)) {
                IBlockData iblockdata = this.getBlockState(blockposition1);

                if (iblockdata.is(Blocks.COMPARATOR)) {
                    this.neighborChanged(iblockdata, blockposition1, block, blockposition, false);
                } else if (iblockdata.isRedstoneConductor(this, blockposition1)) {
                    blockposition1 = blockposition1.relative(enumdirection);
                    iblockdata = this.getBlockState(blockposition1);
                    if (iblockdata.is(Blocks.COMPARATOR)) {
                        this.neighborChanged(iblockdata, blockposition1, block, blockposition, false);
                    }
                }
            }
        }

    }

    @Override
    public DifficultyDamageScaler getCurrentDifficultyAt(BlockPosition blockposition) {
        long i = 0L;
        float f = 0.0F;

        if (this.hasChunkAt(blockposition)) {
            f = this.getMoonBrightness();
            i = this.getChunkAt(blockposition).getInhabitedTime();
        }

        return new DifficultyDamageScaler(this.getDifficulty(), this.getDayTime(), i, f);
    }

    @Override
    public int getSkyDarken() {
        return this.skyDarken;
    }

    public void setSkyFlashTime(int i) {}

    @Override
    public WorldBorder getWorldBorder() {
        return this.worldBorder;
    }

    public void sendPacketToServer(Packet<?> packet) {
        throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
    }

    @Override
    public DimensionManager dimensionType() {
        return (DimensionManager) this.dimensionTypeRegistration.value();
    }

    public Holder<DimensionManager> dimensionTypeRegistration() {
        return this.dimensionTypeRegistration;
    }

    public ResourceKey<World> dimension() {
        return this.dimension;
    }

    @Override
    public RandomSource getRandom() {
        return this.random;
    }

    @Override
    public boolean isStateAtPosition(BlockPosition blockposition, Predicate<IBlockData> predicate) {
        return predicate.test(this.getBlockState(blockposition));
    }

    @Override
    public boolean isFluidAtPosition(BlockPosition blockposition, Predicate<Fluid> predicate) {
        return predicate.test(this.getFluidState(blockposition));
    }

    public abstract CraftingManager getRecipeManager();

    public BlockPosition getBlockRandomPos(int i, int j, int k, int l) {
        this.randValue = this.randValue * 3 + 1013904223;
        int i1 = this.randValue >> 2;

        return new BlockPosition(i + (i1 & 15), j + (i1 >> 16 & l), k + (i1 >> 8 & 15));
    }

    public boolean noSave() {
        return false;
    }

    public GameProfilerFiller getProfiler() {
        return (GameProfilerFiller) this.profiler.get();
    }

    public Supplier<GameProfilerFiller> getProfilerSupplier() {
        return this.profiler;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.biomeManager;
    }

    public final boolean isDebug() {
        return this.isDebug;
    }

    public abstract LevelEntityGetter<Entity> getEntities();

    @Override
    public long nextSubTickCount() {
        return (long) (this.subTickCount++);
    }

    @Override
    public IRegistryCustom registryAccess() {
        return this.registryAccess;
    }

    public DamageSources damageSources() {
        return this.damageSources;
    }

    public abstract PotionBrewer potionBrewing();

    public static enum a {

        NONE, BLOCK, MOB, TNT, BLOW, STANDARD; // CraftBukkit - Add STANDARD which will always use Explosion.Effect.DESTROY

        private a() {}
    }
}
