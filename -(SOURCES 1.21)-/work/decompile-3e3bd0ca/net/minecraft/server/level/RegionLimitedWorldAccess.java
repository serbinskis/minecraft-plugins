package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.ReportedException;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.SectionPosition;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ITileEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.IChunkProvider;
import net.minecraft.world.level.chunk.status.ChunkDependencies;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.TickListWorldGen;
import org.slf4j.Logger;

public class RegionLimitedWorldAccess implements GeneratorAccessSeed {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final StaticCache2D<GenerationChunkHolder> cache;
    private final IChunkAccess center;
    private final WorldServer level;
    private final long seed;
    private final WorldData levelData;
    private final RandomSource random;
    private final DimensionManager dimensionType;
    private final TickListWorldGen<Block> blockTicks = new TickListWorldGen<>((blockposition) -> {
        return this.getChunk(blockposition).getBlockTicks();
    });
    private final TickListWorldGen<FluidType> fluidTicks = new TickListWorldGen<>((blockposition) -> {
        return this.getChunk(blockposition).getFluidTicks();
    });
    private final BiomeManager biomeManager;
    private final ChunkStep generatingStep;
    @Nullable
    private Supplier<String> currentlyGenerating;
    private final AtomicLong subTickCount = new AtomicLong();
    private static final MinecraftKey WORLDGEN_REGION_RANDOM = MinecraftKey.withDefaultNamespace("worldgen_region_random");

    public RegionLimitedWorldAccess(WorldServer worldserver, StaticCache2D<GenerationChunkHolder> staticcache2d, ChunkStep chunkstep, IChunkAccess ichunkaccess) {
        this.generatingStep = chunkstep;
        this.cache = staticcache2d;
        this.center = ichunkaccess;
        this.level = worldserver;
        this.seed = worldserver.getSeed();
        this.levelData = worldserver.getLevelData();
        this.random = worldserver.getChunkSource().randomState().getOrCreateRandomFactory(RegionLimitedWorldAccess.WORLDGEN_REGION_RANDOM).at(this.center.getPos().getWorldPosition());
        this.dimensionType = worldserver.dimensionType();
        this.biomeManager = new BiomeManager(this, BiomeManager.obfuscateSeed(this.seed));
    }

    public boolean isOldChunkAround(ChunkCoordIntPair chunkcoordintpair, int i) {
        return this.level.getChunkSource().chunkMap.isOldChunkAround(chunkcoordintpair, i);
    }

    public ChunkCoordIntPair getCenter() {
        return this.center.getPos();
    }

    @Override
    public void setCurrentlyGenerating(@Nullable Supplier<String> supplier) {
        this.currentlyGenerating = supplier;
    }

    @Override
    public IChunkAccess getChunk(int i, int j) {
        return this.getChunk(i, j, ChunkStatus.EMPTY);
    }

    @Nullable
    @Override
    public IChunkAccess getChunk(int i, int j, ChunkStatus chunkstatus, boolean flag) {
        int k = this.center.getPos().getChessboardDistance(i, j);
        ChunkStatus chunkstatus1 = k >= this.generatingStep.directDependencies().size() ? null : this.generatingStep.directDependencies().get(k);
        GenerationChunkHolder generationchunkholder;

        if (chunkstatus1 != null) {
            generationchunkholder = (GenerationChunkHolder) this.cache.get(i, j);
            if (chunkstatus.isOrBefore(chunkstatus1)) {
                IChunkAccess ichunkaccess = generationchunkholder.getChunkIfPresentUnchecked(chunkstatus1);

                if (ichunkaccess != null) {
                    return ichunkaccess;
                }
            }
        } else {
            generationchunkholder = null;
        }

        CrashReport crashreport = CrashReport.forThrowable(new IllegalStateException("Requested chunk unavailable during world generation"), "Exception generating new chunk");
        CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Chunk request details");

        crashreportsystemdetails.setDetail("Requested chunk", (Object) String.format(Locale.ROOT, "%d, %d", i, j));
        crashreportsystemdetails.setDetail("Generating status", () -> {
            return this.generatingStep.targetStatus().getName();
        });
        Objects.requireNonNull(chunkstatus);
        crashreportsystemdetails.setDetail("Requested status", chunkstatus::getName);
        crashreportsystemdetails.setDetail("Actual status", () -> {
            return generationchunkholder == null ? "[out of cache bounds]" : generationchunkholder.getPersistedStatus().getName();
        });
        crashreportsystemdetails.setDetail("Maximum allowed status", () -> {
            return chunkstatus1 == null ? "null" : chunkstatus1.getName();
        });
        ChunkDependencies chunkdependencies = this.generatingStep.directDependencies();

        Objects.requireNonNull(chunkdependencies);
        crashreportsystemdetails.setDetail("Dependencies", chunkdependencies::toString);
        crashreportsystemdetails.setDetail("Requested distance", (Object) k);
        ChunkCoordIntPair chunkcoordintpair = this.center.getPos();

        Objects.requireNonNull(chunkcoordintpair);
        crashreportsystemdetails.setDetail("Generating chunk", chunkcoordintpair::toString);
        throw new ReportedException(crashreport);
    }

    @Override
    public boolean hasChunk(int i, int j) {
        int k = this.center.getPos().getChessboardDistance(i, j);

        return k < this.generatingStep.directDependencies().size();
    }

    @Override
    public IBlockData getBlockState(BlockPosition blockposition) {
        return this.getChunk(SectionPosition.blockToSectionCoord(blockposition.getX()), SectionPosition.blockToSectionCoord(blockposition.getZ())).getBlockState(blockposition);
    }

    @Override
    public Fluid getFluidState(BlockPosition blockposition) {
        return this.getChunk(blockposition).getFluidState(blockposition);
    }

    @Nullable
    @Override
    public EntityHuman getNearestPlayer(double d0, double d1, double d2, double d3, Predicate<Entity> predicate) {
        return null;
    }

    @Override
    public int getSkyDarken() {
        return 0;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.biomeManager;
    }

    @Override
    public Holder<BiomeBase> getUncachedNoiseBiome(int i, int j, int k) {
        return this.level.getUncachedNoiseBiome(i, j, k);
    }

    @Override
    public float getShade(EnumDirection enumdirection, boolean flag) {
        return 1.0F;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Override
    public boolean destroyBlock(BlockPosition blockposition, boolean flag, @Nullable Entity entity, int i) {
        IBlockData iblockdata = this.getBlockState(blockposition);

        if (iblockdata.isAir()) {
            return false;
        } else {
            if (flag) {
                TileEntity tileentity = iblockdata.hasBlockEntity() ? this.getBlockEntity(blockposition) : null;

                Block.dropResources(iblockdata, this.level, blockposition, tileentity, entity, ItemStack.EMPTY);
            }

            return this.setBlock(blockposition, Blocks.AIR.defaultBlockState(), 3, i);
        }
    }

    @Nullable
    @Override
    public TileEntity getBlockEntity(BlockPosition blockposition) {
        IChunkAccess ichunkaccess = this.getChunk(blockposition);
        TileEntity tileentity = ichunkaccess.getBlockEntity(blockposition);

        if (tileentity != null) {
            return tileentity;
        } else {
            NBTTagCompound nbttagcompound = ichunkaccess.getBlockEntityNbt(blockposition);
            IBlockData iblockdata = ichunkaccess.getBlockState(blockposition);

            if (nbttagcompound != null) {
                if ("DUMMY".equals(nbttagcompound.getString("id"))) {
                    if (!iblockdata.hasBlockEntity()) {
                        return null;
                    }

                    tileentity = ((ITileEntity) iblockdata.getBlock()).newBlockEntity(blockposition, iblockdata);
                } else {
                    tileentity = TileEntity.loadStatic(blockposition, iblockdata, nbttagcompound, this.level.registryAccess());
                }

                if (tileentity != null) {
                    ichunkaccess.setBlockEntity(tileentity);
                    return tileentity;
                }
            }

            if (iblockdata.hasBlockEntity()) {
                RegionLimitedWorldAccess.LOGGER.warn("Tried to access a block entity before it was created. {}", blockposition);
            }

            return null;
        }
    }

    @Override
    public boolean ensureCanWrite(BlockPosition blockposition) {
        int i = SectionPosition.blockToSectionCoord(blockposition.getX());
        int j = SectionPosition.blockToSectionCoord(blockposition.getZ());
        ChunkCoordIntPair chunkcoordintpair = this.getCenter();
        int k = Math.abs(chunkcoordintpair.x - i);
        int l = Math.abs(chunkcoordintpair.z - j);

        if (k <= this.generatingStep.blockStateWriteRadius() && l <= this.generatingStep.blockStateWriteRadius()) {
            if (this.center.isUpgrading()) {
                LevelHeightAccessor levelheightaccessor = this.center.getHeightAccessorForGeneration();

                if (blockposition.getY() < levelheightaccessor.getMinBuildHeight() || blockposition.getY() >= levelheightaccessor.getMaxBuildHeight()) {
                    return false;
                }
            }

            return true;
        } else {
            SystemUtils.logAndPauseIfInIde("Detected setBlock in a far chunk [" + i + ", " + j + "], pos: " + String.valueOf(blockposition) + ", status: " + String.valueOf(this.generatingStep.targetStatus()) + (this.currentlyGenerating == null ? "" : ", currently generating: " + (String) this.currentlyGenerating.get()));
            return false;
        }
    }

    @Override
    public boolean setBlock(BlockPosition blockposition, IBlockData iblockdata, int i, int j) {
        if (!this.ensureCanWrite(blockposition)) {
            return false;
        } else {
            IChunkAccess ichunkaccess = this.getChunk(blockposition);
            IBlockData iblockdata1 = ichunkaccess.setBlockState(blockposition, iblockdata, false);

            if (iblockdata1 != null) {
                this.level.onBlockStateChange(blockposition, iblockdata1, iblockdata);
            }

            if (iblockdata.hasBlockEntity()) {
                if (ichunkaccess.getPersistedStatus().getChunkType() == ChunkType.LEVELCHUNK) {
                    TileEntity tileentity = ((ITileEntity) iblockdata.getBlock()).newBlockEntity(blockposition, iblockdata);

                    if (tileentity != null) {
                        ichunkaccess.setBlockEntity(tileentity);
                    } else {
                        ichunkaccess.removeBlockEntity(blockposition);
                    }
                } else {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();

                    nbttagcompound.putInt("x", blockposition.getX());
                    nbttagcompound.putInt("y", blockposition.getY());
                    nbttagcompound.putInt("z", blockposition.getZ());
                    nbttagcompound.putString("id", "DUMMY");
                    ichunkaccess.setBlockEntityNbt(nbttagcompound);
                }
            } else if (iblockdata1 != null && iblockdata1.hasBlockEntity()) {
                ichunkaccess.removeBlockEntity(blockposition);
            }

            if (iblockdata.hasPostProcess(this, blockposition)) {
                this.markPosForPostprocessing(blockposition);
            }

            return true;
        }
    }

    private void markPosForPostprocessing(BlockPosition blockposition) {
        this.getChunk(blockposition).markPosForPostprocessing(blockposition);
    }

    @Override
    public boolean addFreshEntity(Entity entity) {
        int i = SectionPosition.blockToSectionCoord(entity.getBlockX());
        int j = SectionPosition.blockToSectionCoord(entity.getBlockZ());

        this.getChunk(i, j).addEntity(entity);
        return true;
    }

    @Override
    public boolean removeBlock(BlockPosition blockposition, boolean flag) {
        return this.setBlock(blockposition, Blocks.AIR.defaultBlockState(), 3);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.level.getWorldBorder();
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    /** @deprecated */
    @Deprecated
    @Override
    public WorldServer getLevel() {
        return this.level;
    }

    @Override
    public IRegistryCustom registryAccess() {
        return this.level.registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return this.level.enabledFeatures();
    }

    @Override
    public WorldData getLevelData() {
        return this.levelData;
    }

    @Override
    public DifficultyDamageScaler getCurrentDifficultyAt(BlockPosition blockposition) {
        if (!this.hasChunk(SectionPosition.blockToSectionCoord(blockposition.getX()), SectionPosition.blockToSectionCoord(blockposition.getZ()))) {
            throw new RuntimeException("We are asking a region for a chunk out of bound");
        } else {
            return new DifficultyDamageScaler(this.level.getDifficulty(), this.level.getDayTime(), 0L, this.level.getMoonBrightness());
        }
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return this.level.getServer();
    }

    @Override
    public IChunkProvider getChunkSource() {
        return this.level.getChunkSource();
    }

    @Override
    public long getSeed() {
        return this.seed;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return this.blockTicks;
    }

    @Override
    public LevelTickAccess<FluidType> getFluidTicks() {
        return this.fluidTicks;
    }

    @Override
    public int getSeaLevel() {
        return this.level.getSeaLevel();
    }

    @Override
    public RandomSource getRandom() {
        return this.random;
    }

    @Override
    public int getHeight(HeightMap.Type heightmap_type, int i, int j) {
        return this.getChunk(SectionPosition.blockToSectionCoord(i), SectionPosition.blockToSectionCoord(j)).getHeight(heightmap_type, i & 15, j & 15) + 1;
    }

    @Override
    public void playSound(@Nullable EntityHuman entityhuman, BlockPosition blockposition, SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1) {}

    @Override
    public void addParticle(ParticleParam particleparam, double d0, double d1, double d2, double d3, double d4, double d5) {}

    @Override
    public void levelEvent(@Nullable EntityHuman entityhuman, int i, BlockPosition blockposition, int j) {}

    @Override
    public void gameEvent(Holder<GameEvent> holder, Vec3D vec3d, GameEvent.a gameevent_a) {}

    @Override
    public DimensionManager dimensionType() {
        return this.dimensionType;
    }

    @Override
    public boolean isStateAtPosition(BlockPosition blockposition, Predicate<IBlockData> predicate) {
        return predicate.test(this.getBlockState(blockposition));
    }

    @Override
    public boolean isFluidAtPosition(BlockPosition blockposition, Predicate<Fluid> predicate) {
        return predicate.test(this.getFluidState(blockposition));
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entitytypetest, AxisAlignedBB axisalignedbb, Predicate<? super T> predicate) {
        return Collections.emptyList();
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AxisAlignedBB axisalignedbb, @Nullable Predicate<? super Entity> predicate) {
        return Collections.emptyList();
    }

    @Override
    public List<EntityHuman> players() {
        return Collections.emptyList();
    }

    @Override
    public int getMinBuildHeight() {
        return this.level.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    @Override
    public long nextSubTickCount() {
        return this.subTickCount.getAndIncrement();
    }
}
