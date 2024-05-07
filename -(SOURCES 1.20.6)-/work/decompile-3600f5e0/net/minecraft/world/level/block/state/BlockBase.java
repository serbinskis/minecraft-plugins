package net.minecraft.world.level.block.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagsFluid;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.ITileInventory;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.BlockAccessAir;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.EnumBlockSupport;
import net.minecraft.world.level.block.EnumRenderType;
import net.minecraft.world.level.block.ITileEntity;
import net.minecraft.world.level.block.SoundEffectType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.properties.BlockPropertyInstrument;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.material.EnumPistonReaction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidType;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.material.MaterialMapColor;
import net.minecraft.world.level.pathfinder.PathMode;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
import net.minecraft.world.phys.shapes.VoxelShapes;

public abstract class BlockBase implements FeatureElement {

    protected static final EnumDirection[] UPDATE_SHAPE_ORDER = new EnumDirection[]{EnumDirection.WEST, EnumDirection.EAST, EnumDirection.NORTH, EnumDirection.SOUTH, EnumDirection.DOWN, EnumDirection.UP};
    protected final boolean hasCollision;
    protected final float explosionResistance;
    protected final boolean isRandomlyTicking;
    protected final SoundEffectType soundType;
    protected final float friction;
    protected final float speedFactor;
    protected final float jumpFactor;
    protected final boolean dynamicShape;
    protected final FeatureFlagSet requiredFeatures;
    protected final BlockBase.Info properties;
    @Nullable
    protected ResourceKey<LootTable> drops;

    public BlockBase(BlockBase.Info blockbase_info) {
        this.hasCollision = blockbase_info.hasCollision;
        this.drops = blockbase_info.drops;
        this.explosionResistance = blockbase_info.explosionResistance;
        this.isRandomlyTicking = blockbase_info.isRandomlyTicking;
        this.soundType = blockbase_info.soundType;
        this.friction = blockbase_info.friction;
        this.speedFactor = blockbase_info.speedFactor;
        this.jumpFactor = blockbase_info.jumpFactor;
        this.dynamicShape = blockbase_info.dynamicShape;
        this.requiredFeatures = blockbase_info.requiredFeatures;
        this.properties = blockbase_info;
    }

    public BlockBase.Info properties() {
        return this.properties;
    }

    protected abstract MapCodec<? extends Block> codec();

    protected static <B extends Block> RecordCodecBuilder<B, BlockBase.Info> propertiesCodec() {
        return BlockBase.Info.CODEC.fieldOf("properties").forGetter(BlockBase::properties);
    }

    public static <B extends Block> MapCodec<B> simpleCodec(Function<BlockBase.Info, B> function) {
        return RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(propertiesCodec()).apply(instance, function);
        });
    }

    protected void updateIndirectNeighbourShapes(IBlockData iblockdata, GeneratorAccess generatoraccess, BlockPosition blockposition, int i, int j) {}

    protected boolean isPathfindable(IBlockData iblockdata, PathMode pathmode) {
        switch (pathmode) {
            case LAND:
                return !iblockdata.isCollisionShapeFullBlock(BlockAccessAir.INSTANCE, BlockPosition.ZERO);
            case WATER:
                return iblockdata.getFluidState().is(TagsFluid.WATER);
            case AIR:
                return !iblockdata.isCollisionShapeFullBlock(BlockAccessAir.INSTANCE, BlockPosition.ZERO);
            default:
                return false;
        }
    }

    protected IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        return iblockdata;
    }

    protected boolean skipRendering(IBlockData iblockdata, IBlockData iblockdata1, EnumDirection enumdirection) {
        return false;
    }

    protected void neighborChanged(IBlockData iblockdata, World world, BlockPosition blockposition, Block block, BlockPosition blockposition1, boolean flag) {
        PacketDebug.sendNeighborsUpdatePacket(world, blockposition);
    }

    protected void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {}

    protected void onRemove(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        if (iblockdata.hasBlockEntity() && !iblockdata.is(iblockdata1.getBlock())) {
            world.removeBlockEntity(blockposition);
        }

    }

    protected void onExplosionHit(IBlockData iblockdata, World world, BlockPosition blockposition, Explosion explosion, BiConsumer<ItemStack, BlockPosition> biconsumer) {
        if (!iblockdata.isAir() && explosion.getBlockInteraction() != Explosion.Effect.TRIGGER_BLOCK) {
            Block block = iblockdata.getBlock();
            boolean flag = explosion.getIndirectSourceEntity() instanceof EntityHuman;

            if (block.dropFromExplosion(explosion) && world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;
                TileEntity tileentity = iblockdata.hasBlockEntity() ? world.getBlockEntity(blockposition) : null;
                LootParams.a lootparams_a = (new LootParams.a(worldserver)).withParameter(LootContextParameters.ORIGIN, Vec3D.atCenterOf(blockposition)).withParameter(LootContextParameters.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParameters.BLOCK_ENTITY, tileentity).withOptionalParameter(LootContextParameters.THIS_ENTITY, explosion.getDirectSourceEntity());

                if (explosion.getBlockInteraction() == Explosion.Effect.DESTROY_WITH_DECAY) {
                    lootparams_a.withParameter(LootContextParameters.EXPLOSION_RADIUS, explosion.radius());
                }

                iblockdata.spawnAfterBreak(worldserver, blockposition, ItemStack.EMPTY, flag);
                iblockdata.getDrops(lootparams_a).forEach((itemstack) -> {
                    biconsumer.accept(itemstack, blockposition);
                });
            }

            world.setBlock(blockposition, Blocks.AIR.defaultBlockState(), 3);
            block.wasExploded(world, blockposition, explosion);
        }
    }

    protected EnumInteractionResult useWithoutItem(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        return EnumInteractionResult.PASS;
    }

    protected ItemInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected boolean triggerEvent(IBlockData iblockdata, World world, BlockPosition blockposition, int i, int j) {
        return false;
    }

    protected EnumRenderType getRenderShape(IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    protected boolean useShapeForLightOcclusion(IBlockData iblockdata) {
        return false;
    }

    protected boolean isSignalSource(IBlockData iblockdata) {
        return false;
    }

    protected Fluid getFluidState(IBlockData iblockdata) {
        return FluidTypes.EMPTY.defaultFluidState();
    }

    protected boolean hasAnalogOutputSignal(IBlockData iblockdata) {
        return false;
    }

    protected float getMaxHorizontalOffset() {
        return 0.25F;
    }

    protected float getMaxVerticalOffset() {
        return 0.2F;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    protected IBlockData rotate(IBlockData iblockdata, EnumBlockRotation enumblockrotation) {
        return iblockdata;
    }

    protected IBlockData mirror(IBlockData iblockdata, EnumBlockMirror enumblockmirror) {
        return iblockdata;
    }

    protected boolean canBeReplaced(IBlockData iblockdata, BlockActionContext blockactioncontext) {
        return iblockdata.canBeReplaced() && (blockactioncontext.getItemInHand().isEmpty() || !blockactioncontext.getItemInHand().is(this.asItem()));
    }

    protected boolean canBeReplaced(IBlockData iblockdata, FluidType fluidtype) {
        return iblockdata.canBeReplaced() || !iblockdata.isSolid();
    }

    protected List<ItemStack> getDrops(IBlockData iblockdata, LootParams.a lootparams_a) {
        ResourceKey<LootTable> resourcekey = this.getLootTable();

        if (resourcekey == LootTables.EMPTY) {
            return Collections.emptyList();
        } else {
            LootParams lootparams = lootparams_a.withParameter(LootContextParameters.BLOCK_STATE, iblockdata).create(LootContextParameterSets.BLOCK);
            WorldServer worldserver = lootparams.getLevel();
            LootTable loottable = worldserver.getServer().reloadableRegistries().getLootTable(resourcekey);

            return loottable.getRandomItems(lootparams);
        }
    }

    protected long getSeed(IBlockData iblockdata, BlockPosition blockposition) {
        return MathHelper.getSeed(blockposition);
    }

    protected VoxelShape getOcclusionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.getShape(iblockaccess, blockposition);
    }

    protected VoxelShape getBlockSupportShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return this.getCollisionShape(iblockdata, iblockaccess, blockposition, VoxelShapeCollision.empty());
    }

    protected VoxelShape getInteractionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return VoxelShapes.empty();
    }

    protected int getLightBlock(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.isSolidRender(iblockaccess, blockposition) ? iblockaccess.getMaxLightLevel() : (iblockdata.propagatesSkylightDown(iblockaccess, blockposition) ? 0 : 1);
    }

    @Nullable
    protected ITileInventory getMenuProvider(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return null;
    }

    protected boolean canSurvive(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        return true;
    }

    protected float getShadeBrightness(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.isCollisionShapeFullBlock(iblockaccess, blockposition) ? 0.2F : 1.0F;
    }

    protected int getAnalogOutputSignal(IBlockData iblockdata, World world, BlockPosition blockposition) {
        return 0;
    }

    protected VoxelShape getShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return VoxelShapes.block();
    }

    protected VoxelShape getCollisionShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return this.hasCollision ? iblockdata.getShape(iblockaccess, blockposition) : VoxelShapes.empty();
    }

    protected boolean isCollisionShapeFullBlock(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return Block.isShapeFullBlock(iblockdata.getCollisionShape(iblockaccess, blockposition));
    }

    protected boolean isOcclusionShapeFullBlock(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return Block.isShapeFullBlock(iblockdata.getOcclusionShape(iblockaccess, blockposition));
    }

    protected VoxelShape getVisualShape(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return this.getCollisionShape(iblockdata, iblockaccess, blockposition, voxelshapecollision);
    }

    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {}

    protected void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {}

    protected float getDestroyProgress(IBlockData iblockdata, EntityHuman entityhuman, IBlockAccess iblockaccess, BlockPosition blockposition) {
        float f = iblockdata.getDestroySpeed(iblockaccess, blockposition);

        if (f == -1.0F) {
            return 0.0F;
        } else {
            int i = entityhuman.hasCorrectToolForDrops(iblockdata) ? 30 : 100;

            return entityhuman.getDestroySpeed(iblockdata) / f / (float) i;
        }
    }

    protected void spawnAfterBreak(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, ItemStack itemstack, boolean flag) {}

    protected void attack(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman) {}

    protected int getSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return 0;
    }

    protected void entityInside(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) {}

    protected int getDirectSignal(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
        return 0;
    }

    public final ResourceKey<LootTable> getLootTable() {
        if (this.drops == null) {
            MinecraftKey minecraftkey = BuiltInRegistries.BLOCK.getKey(this.asBlock());

            this.drops = ResourceKey.create(Registries.LOOT_TABLE, minecraftkey.withPrefix("blocks/"));
        }

        return this.drops;
    }

    protected void onProjectileHit(World world, IBlockData iblockdata, MovingObjectPositionBlock movingobjectpositionblock, IProjectile iprojectile) {}

    protected boolean propagatesSkylightDown(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return !Block.isShapeFullBlock(iblockdata.getShape(iblockaccess, blockposition)) && iblockdata.getFluidState().isEmpty();
    }

    protected boolean isRandomlyTicking(IBlockData iblockdata) {
        return this.isRandomlyTicking;
    }

    protected SoundEffectType getSoundType(IBlockData iblockdata) {
        return this.soundType;
    }

    public abstract Item asItem();

    protected abstract Block asBlock();

    public MaterialMapColor defaultMapColor() {
        return (MaterialMapColor) this.properties.mapColor.apply(this.asBlock().defaultBlockState());
    }

    public float defaultDestroyTime() {
        return this.properties.destroyTime;
    }

    public static class Info {

        public static final Codec<BlockBase.Info> CODEC = Codec.unit(() -> {
            return of();
        });
        Function<IBlockData, MaterialMapColor> mapColor = (iblockdata) -> {
            return MaterialMapColor.NONE;
        };
        boolean hasCollision = true;
        SoundEffectType soundType;
        ToIntFunction<IBlockData> lightEmission;
        float explosionResistance;
        float destroyTime;
        boolean requiresCorrectToolForDrops;
        boolean isRandomlyTicking;
        float friction;
        float speedFactor;
        float jumpFactor;
        ResourceKey<LootTable> drops;
        boolean canOcclude;
        boolean isAir;
        boolean ignitedByLava;
        /** @deprecated */
        @Deprecated
        boolean liquid;
        /** @deprecated */
        @Deprecated
        boolean forceSolidOff;
        boolean forceSolidOn;
        EnumPistonReaction pushReaction;
        boolean spawnTerrainParticles;
        BlockPropertyInstrument instrument;
        boolean replaceable;
        BlockBase.e<EntityTypes<?>> isValidSpawn;
        BlockBase.f isRedstoneConductor;
        BlockBase.f isSuffocating;
        BlockBase.f isViewBlocking;
        BlockBase.f hasPostProcess;
        BlockBase.f emissiveRendering;
        boolean dynamicShape;
        FeatureFlagSet requiredFeatures;
        Optional<BlockBase.b> offsetFunction;

        private Info() {
            this.soundType = SoundEffectType.STONE;
            this.lightEmission = (iblockdata) -> {
                return 0;
            };
            this.friction = 0.6F;
            this.speedFactor = 1.0F;
            this.jumpFactor = 1.0F;
            this.canOcclude = true;
            this.pushReaction = EnumPistonReaction.NORMAL;
            this.spawnTerrainParticles = true;
            this.instrument = BlockPropertyInstrument.HARP;
            this.isValidSpawn = (iblockdata, iblockaccess, blockposition, entitytypes) -> {
                return iblockdata.isFaceSturdy(iblockaccess, blockposition, EnumDirection.UP) && iblockdata.getLightEmission() < 14;
            };
            this.isRedstoneConductor = (iblockdata, iblockaccess, blockposition) -> {
                return iblockdata.isCollisionShapeFullBlock(iblockaccess, blockposition);
            };
            this.isSuffocating = (iblockdata, iblockaccess, blockposition) -> {
                return iblockdata.blocksMotion() && iblockdata.isCollisionShapeFullBlock(iblockaccess, blockposition);
            };
            this.isViewBlocking = this.isSuffocating;
            this.hasPostProcess = (iblockdata, iblockaccess, blockposition) -> {
                return false;
            };
            this.emissiveRendering = (iblockdata, iblockaccess, blockposition) -> {
                return false;
            };
            this.requiredFeatures = FeatureFlags.VANILLA_SET;
            this.offsetFunction = Optional.empty();
        }

        public static BlockBase.Info of() {
            return new BlockBase.Info();
        }

        public static BlockBase.Info ofFullCopy(BlockBase blockbase) {
            BlockBase.Info blockbase_info = ofLegacyCopy(blockbase);
            BlockBase.Info blockbase_info1 = blockbase.properties;

            blockbase_info.jumpFactor = blockbase_info1.jumpFactor;
            blockbase_info.isRedstoneConductor = blockbase_info1.isRedstoneConductor;
            blockbase_info.isValidSpawn = blockbase_info1.isValidSpawn;
            blockbase_info.hasPostProcess = blockbase_info1.hasPostProcess;
            blockbase_info.isSuffocating = blockbase_info1.isSuffocating;
            blockbase_info.isViewBlocking = blockbase_info1.isViewBlocking;
            blockbase_info.drops = blockbase_info1.drops;
            return blockbase_info;
        }

        /** @deprecated */
        @Deprecated
        public static BlockBase.Info ofLegacyCopy(BlockBase blockbase) {
            BlockBase.Info blockbase_info = new BlockBase.Info();
            BlockBase.Info blockbase_info1 = blockbase.properties;

            blockbase_info.destroyTime = blockbase_info1.destroyTime;
            blockbase_info.explosionResistance = blockbase_info1.explosionResistance;
            blockbase_info.hasCollision = blockbase_info1.hasCollision;
            blockbase_info.isRandomlyTicking = blockbase_info1.isRandomlyTicking;
            blockbase_info.lightEmission = blockbase_info1.lightEmission;
            blockbase_info.mapColor = blockbase_info1.mapColor;
            blockbase_info.soundType = blockbase_info1.soundType;
            blockbase_info.friction = blockbase_info1.friction;
            blockbase_info.speedFactor = blockbase_info1.speedFactor;
            blockbase_info.dynamicShape = blockbase_info1.dynamicShape;
            blockbase_info.canOcclude = blockbase_info1.canOcclude;
            blockbase_info.isAir = blockbase_info1.isAir;
            blockbase_info.ignitedByLava = blockbase_info1.ignitedByLava;
            blockbase_info.liquid = blockbase_info1.liquid;
            blockbase_info.forceSolidOff = blockbase_info1.forceSolidOff;
            blockbase_info.forceSolidOn = blockbase_info1.forceSolidOn;
            blockbase_info.pushReaction = blockbase_info1.pushReaction;
            blockbase_info.requiresCorrectToolForDrops = blockbase_info1.requiresCorrectToolForDrops;
            blockbase_info.offsetFunction = blockbase_info1.offsetFunction;
            blockbase_info.spawnTerrainParticles = blockbase_info1.spawnTerrainParticles;
            blockbase_info.requiredFeatures = blockbase_info1.requiredFeatures;
            blockbase_info.emissiveRendering = blockbase_info1.emissiveRendering;
            blockbase_info.instrument = blockbase_info1.instrument;
            blockbase_info.replaceable = blockbase_info1.replaceable;
            return blockbase_info;
        }

        public BlockBase.Info mapColor(EnumColor enumcolor) {
            this.mapColor = (iblockdata) -> {
                return enumcolor.getMapColor();
            };
            return this;
        }

        public BlockBase.Info mapColor(MaterialMapColor materialmapcolor) {
            this.mapColor = (iblockdata) -> {
                return materialmapcolor;
            };
            return this;
        }

        public BlockBase.Info mapColor(Function<IBlockData, MaterialMapColor> function) {
            this.mapColor = function;
            return this;
        }

        public BlockBase.Info noCollission() {
            this.hasCollision = false;
            this.canOcclude = false;
            return this;
        }

        public BlockBase.Info noOcclusion() {
            this.canOcclude = false;
            return this;
        }

        public BlockBase.Info friction(float f) {
            this.friction = f;
            return this;
        }

        public BlockBase.Info speedFactor(float f) {
            this.speedFactor = f;
            return this;
        }

        public BlockBase.Info jumpFactor(float f) {
            this.jumpFactor = f;
            return this;
        }

        public BlockBase.Info sound(SoundEffectType soundeffecttype) {
            this.soundType = soundeffecttype;
            return this;
        }

        public BlockBase.Info lightLevel(ToIntFunction<IBlockData> tointfunction) {
            this.lightEmission = tointfunction;
            return this;
        }

        public BlockBase.Info strength(float f, float f1) {
            return this.destroyTime(f).explosionResistance(f1);
        }

        public BlockBase.Info instabreak() {
            return this.strength(0.0F);
        }

        public BlockBase.Info strength(float f) {
            this.strength(f, f);
            return this;
        }

        public BlockBase.Info randomTicks() {
            this.isRandomlyTicking = true;
            return this;
        }

        public BlockBase.Info dynamicShape() {
            this.dynamicShape = true;
            return this;
        }

        public BlockBase.Info noLootTable() {
            this.drops = LootTables.EMPTY;
            return this;
        }

        public BlockBase.Info dropsLike(Block block) {
            this.drops = block.getLootTable();
            return this;
        }

        public BlockBase.Info ignitedByLava() {
            this.ignitedByLava = true;
            return this;
        }

        public BlockBase.Info liquid() {
            this.liquid = true;
            return this;
        }

        public BlockBase.Info forceSolidOn() {
            this.forceSolidOn = true;
            return this;
        }

        /** @deprecated */
        @Deprecated
        public BlockBase.Info forceSolidOff() {
            this.forceSolidOff = true;
            return this;
        }

        public BlockBase.Info pushReaction(EnumPistonReaction enumpistonreaction) {
            this.pushReaction = enumpistonreaction;
            return this;
        }

        public BlockBase.Info air() {
            this.isAir = true;
            return this;
        }

        public BlockBase.Info isValidSpawn(BlockBase.e<EntityTypes<?>> blockbase_e) {
            this.isValidSpawn = blockbase_e;
            return this;
        }

        public BlockBase.Info isRedstoneConductor(BlockBase.f blockbase_f) {
            this.isRedstoneConductor = blockbase_f;
            return this;
        }

        public BlockBase.Info isSuffocating(BlockBase.f blockbase_f) {
            this.isSuffocating = blockbase_f;
            return this;
        }

        public BlockBase.Info isViewBlocking(BlockBase.f blockbase_f) {
            this.isViewBlocking = blockbase_f;
            return this;
        }

        public BlockBase.Info hasPostProcess(BlockBase.f blockbase_f) {
            this.hasPostProcess = blockbase_f;
            return this;
        }

        public BlockBase.Info emissiveRendering(BlockBase.f blockbase_f) {
            this.emissiveRendering = blockbase_f;
            return this;
        }

        public BlockBase.Info requiresCorrectToolForDrops() {
            this.requiresCorrectToolForDrops = true;
            return this;
        }

        public BlockBase.Info destroyTime(float f) {
            this.destroyTime = f;
            return this;
        }

        public BlockBase.Info explosionResistance(float f) {
            this.explosionResistance = Math.max(0.0F, f);
            return this;
        }

        public BlockBase.Info offsetType(BlockBase.EnumRandomOffset blockbase_enumrandomoffset) {
            switch (blockbase_enumrandomoffset.ordinal()) {
                case 1:
                    this.offsetFunction = Optional.of((iblockdata, iblockaccess, blockposition) -> {
                        Block block = iblockdata.getBlock();
                        long i = MathHelper.getSeed(blockposition.getX(), 0, blockposition.getZ());
                        float f = block.getMaxHorizontalOffset();
                        double d0 = MathHelper.clamp(((double) ((float) (i & 15L) / 15.0F) - 0.5D) * 0.5D, (double) (-f), (double) f);
                        double d1 = MathHelper.clamp(((double) ((float) (i >> 8 & 15L) / 15.0F) - 0.5D) * 0.5D, (double) (-f), (double) f);

                        return new Vec3D(d0, 0.0D, d1);
                    });
                    break;
                case 2:
                    this.offsetFunction = Optional.of((iblockdata, iblockaccess, blockposition) -> {
                        Block block = iblockdata.getBlock();
                        long i = MathHelper.getSeed(blockposition.getX(), 0, blockposition.getZ());
                        double d0 = ((double) ((float) (i >> 4 & 15L) / 15.0F) - 1.0D) * (double) block.getMaxVerticalOffset();
                        float f = block.getMaxHorizontalOffset();
                        double d1 = MathHelper.clamp(((double) ((float) (i & 15L) / 15.0F) - 0.5D) * 0.5D, (double) (-f), (double) f);
                        double d2 = MathHelper.clamp(((double) ((float) (i >> 8 & 15L) / 15.0F) - 0.5D) * 0.5D, (double) (-f), (double) f);

                        return new Vec3D(d1, d0, d2);
                    });
                    break;
                default:
                    this.offsetFunction = Optional.empty();
            }

            return this;
        }

        public BlockBase.Info noTerrainParticles() {
            this.spawnTerrainParticles = false;
            return this;
        }

        public BlockBase.Info requiredFeatures(FeatureFlag... afeatureflag) {
            this.requiredFeatures = FeatureFlags.REGISTRY.subset(afeatureflag);
            return this;
        }

        public BlockBase.Info instrument(BlockPropertyInstrument blockpropertyinstrument) {
            this.instrument = blockpropertyinstrument;
            return this;
        }

        public BlockBase.Info replaceable() {
            this.replaceable = true;
            return this;
        }
    }

    public interface e<A> {

        boolean test(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, A a0);
    }

    public interface b {

        Vec3D evaluate(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition);
    }

    public interface f {

        boolean test(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition);
    }

    public abstract static class BlockData extends IBlockDataHolder<Block, IBlockData> {

        private final int lightEmission;
        private final boolean useShapeForLightOcclusion;
        private final boolean isAir;
        private final boolean ignitedByLava;
        /** @deprecated */
        @Deprecated
        private final boolean liquid;
        /** @deprecated */
        @Deprecated
        private boolean legacySolid;
        private final EnumPistonReaction pushReaction;
        private final MaterialMapColor mapColor;
        public final float destroySpeed;
        private final boolean requiresCorrectToolForDrops;
        private final boolean canOcclude;
        private final BlockBase.f isRedstoneConductor;
        private final BlockBase.f isSuffocating;
        private final BlockBase.f isViewBlocking;
        private final BlockBase.f hasPostProcess;
        private final BlockBase.f emissiveRendering;
        private final Optional<BlockBase.b> offsetFunction;
        private final boolean spawnTerrainParticles;
        private final BlockPropertyInstrument instrument;
        private final boolean replaceable;
        @Nullable
        protected BlockBase.BlockData.Cache cache;
        private Fluid fluidState;
        private boolean isRandomlyTicking;

        protected BlockData(Block block, Reference2ObjectArrayMap<IBlockState<?>, Comparable<?>> reference2objectarraymap, MapCodec<IBlockData> mapcodec) {
            super(block, reference2objectarraymap, mapcodec);
            this.fluidState = FluidTypes.EMPTY.defaultFluidState();
            BlockBase.Info blockbase_info = block.properties;

            this.lightEmission = blockbase_info.lightEmission.applyAsInt(this.asState());
            this.useShapeForLightOcclusion = block.useShapeForLightOcclusion(this.asState());
            this.isAir = blockbase_info.isAir;
            this.ignitedByLava = blockbase_info.ignitedByLava;
            this.liquid = blockbase_info.liquid;
            this.pushReaction = blockbase_info.pushReaction;
            this.mapColor = (MaterialMapColor) blockbase_info.mapColor.apply(this.asState());
            this.destroySpeed = blockbase_info.destroyTime;
            this.requiresCorrectToolForDrops = blockbase_info.requiresCorrectToolForDrops;
            this.canOcclude = blockbase_info.canOcclude;
            this.isRedstoneConductor = blockbase_info.isRedstoneConductor;
            this.isSuffocating = blockbase_info.isSuffocating;
            this.isViewBlocking = blockbase_info.isViewBlocking;
            this.hasPostProcess = blockbase_info.hasPostProcess;
            this.emissiveRendering = blockbase_info.emissiveRendering;
            this.offsetFunction = blockbase_info.offsetFunction;
            this.spawnTerrainParticles = blockbase_info.spawnTerrainParticles;
            this.instrument = blockbase_info.instrument;
            this.replaceable = blockbase_info.replaceable;
        }

        private boolean calculateSolid() {
            if (((Block) this.owner).properties.forceSolidOn) {
                return true;
            } else if (((Block) this.owner).properties.forceSolidOff) {
                return false;
            } else if (this.cache == null) {
                return false;
            } else {
                VoxelShape voxelshape = this.cache.collisionShape;

                if (voxelshape.isEmpty()) {
                    return false;
                } else {
                    AxisAlignedBB axisalignedbb = voxelshape.bounds();

                    return axisalignedbb.getSize() >= 0.7291666666666666D ? true : axisalignedbb.getYsize() >= 1.0D;
                }
            }
        }

        public void initCache() {
            this.fluidState = ((Block) this.owner).getFluidState(this.asState());
            this.isRandomlyTicking = ((Block) this.owner).isRandomlyTicking(this.asState());
            if (!this.getBlock().hasDynamicShape()) {
                this.cache = new BlockBase.BlockData.Cache(this.asState());
            }

            this.legacySolid = this.calculateSolid();
        }

        public Block getBlock() {
            return (Block) this.owner;
        }

        public Holder<Block> getBlockHolder() {
            return ((Block) this.owner).builtInRegistryHolder();
        }

        /** @deprecated */
        @Deprecated
        public boolean blocksMotion() {
            Block block = this.getBlock();

            return block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING && this.isSolid();
        }

        /** @deprecated */
        @Deprecated
        public boolean isSolid() {
            return this.legacySolid;
        }

        public boolean isValidSpawn(IBlockAccess iblockaccess, BlockPosition blockposition, EntityTypes<?> entitytypes) {
            return this.getBlock().properties.isValidSpawn.test(this.asState(), iblockaccess, blockposition, entitytypes);
        }

        public boolean propagatesSkylightDown(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.cache != null ? this.cache.propagatesSkylightDown : this.getBlock().propagatesSkylightDown(this.asState(), iblockaccess, blockposition);
        }

        public int getLightBlock(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.cache != null ? this.cache.lightBlock : this.getBlock().getLightBlock(this.asState(), iblockaccess, blockposition);
        }

        public VoxelShape getFaceOcclusionShape(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
            return this.cache != null && this.cache.occlusionShapes != null ? this.cache.occlusionShapes[enumdirection.ordinal()] : VoxelShapes.getFaceShape(this.getOcclusionShape(iblockaccess, blockposition), enumdirection);
        }

        public VoxelShape getOcclusionShape(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.getBlock().getOcclusionShape(this.asState(), iblockaccess, blockposition);
        }

        public boolean hasLargeCollisionShape() {
            return this.cache == null || this.cache.largeCollisionShape;
        }

        public boolean useShapeForLightOcclusion() {
            return this.useShapeForLightOcclusion;
        }

        public int getLightEmission() {
            return this.lightEmission;
        }

        public boolean isAir() {
            return this.isAir;
        }

        public boolean ignitedByLava() {
            return this.ignitedByLava;
        }

        /** @deprecated */
        @Deprecated
        public boolean liquid() {
            return this.liquid;
        }

        public MaterialMapColor getMapColor(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.mapColor;
        }

        public IBlockData rotate(EnumBlockRotation enumblockrotation) {
            return this.getBlock().rotate(this.asState(), enumblockrotation);
        }

        public IBlockData mirror(EnumBlockMirror enumblockmirror) {
            return this.getBlock().mirror(this.asState(), enumblockmirror);
        }

        public EnumRenderType getRenderShape() {
            return this.getBlock().getRenderShape(this.asState());
        }

        public boolean emissiveRendering(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.emissiveRendering.test(this.asState(), iblockaccess, blockposition);
        }

        public float getShadeBrightness(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.getBlock().getShadeBrightness(this.asState(), iblockaccess, blockposition);
        }

        public boolean isRedstoneConductor(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.isRedstoneConductor.test(this.asState(), iblockaccess, blockposition);
        }

        public boolean isSignalSource() {
            return this.getBlock().isSignalSource(this.asState());
        }

        public int getSignal(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
            return this.getBlock().getSignal(this.asState(), iblockaccess, blockposition, enumdirection);
        }

        public boolean hasAnalogOutputSignal() {
            return this.getBlock().hasAnalogOutputSignal(this.asState());
        }

        public int getAnalogOutputSignal(World world, BlockPosition blockposition) {
            return this.getBlock().getAnalogOutputSignal(this.asState(), world, blockposition);
        }

        public float getDestroySpeed(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.destroySpeed;
        }

        public float getDestroyProgress(EntityHuman entityhuman, IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.getBlock().getDestroyProgress(this.asState(), entityhuman, iblockaccess, blockposition);
        }

        public int getDirectSignal(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
            return this.getBlock().getDirectSignal(this.asState(), iblockaccess, blockposition, enumdirection);
        }

        public EnumPistonReaction getPistonPushReaction() {
            return this.pushReaction;
        }

        public boolean isSolidRender(IBlockAccess iblockaccess, BlockPosition blockposition) {
            if (this.cache != null) {
                return this.cache.solidRender;
            } else {
                IBlockData iblockdata = this.asState();

                return iblockdata.canOcclude() ? Block.isShapeFullBlock(iblockdata.getOcclusionShape(iblockaccess, blockposition)) : false;
            }
        }

        public boolean canOcclude() {
            return this.canOcclude;
        }

        public boolean skipRendering(IBlockData iblockdata, EnumDirection enumdirection) {
            return this.getBlock().skipRendering(this.asState(), iblockdata, enumdirection);
        }

        public VoxelShape getShape(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.getShape(iblockaccess, blockposition, VoxelShapeCollision.empty());
        }

        public VoxelShape getShape(IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
            return this.getBlock().getShape(this.asState(), iblockaccess, blockposition, voxelshapecollision);
        }

        public VoxelShape getCollisionShape(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.cache != null ? this.cache.collisionShape : this.getCollisionShape(iblockaccess, blockposition, VoxelShapeCollision.empty());
        }

        public VoxelShape getCollisionShape(IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
            return this.getBlock().getCollisionShape(this.asState(), iblockaccess, blockposition, voxelshapecollision);
        }

        public VoxelShape getBlockSupportShape(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.getBlock().getBlockSupportShape(this.asState(), iblockaccess, blockposition);
        }

        public VoxelShape getVisualShape(IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
            return this.getBlock().getVisualShape(this.asState(), iblockaccess, blockposition, voxelshapecollision);
        }

        public VoxelShape getInteractionShape(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.getBlock().getInteractionShape(this.asState(), iblockaccess, blockposition);
        }

        public final boolean entityCanStandOn(IBlockAccess iblockaccess, BlockPosition blockposition, Entity entity) {
            return this.entityCanStandOnFace(iblockaccess, blockposition, entity, EnumDirection.UP);
        }

        public final boolean entityCanStandOnFace(IBlockAccess iblockaccess, BlockPosition blockposition, Entity entity, EnumDirection enumdirection) {
            return Block.isFaceFull(this.getCollisionShape(iblockaccess, blockposition, VoxelShapeCollision.of(entity)), enumdirection);
        }

        public Vec3D getOffset(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return (Vec3D) this.offsetFunction.map((blockbase_b) -> {
                return blockbase_b.evaluate(this.asState(), iblockaccess, blockposition);
            }).orElse(Vec3D.ZERO);
        }

        public boolean hasOffsetFunction() {
            return this.offsetFunction.isPresent();
        }

        public boolean triggerEvent(World world, BlockPosition blockposition, int i, int j) {
            return this.getBlock().triggerEvent(this.asState(), world, blockposition, i, j);
        }

        public void handleNeighborChanged(World world, BlockPosition blockposition, Block block, BlockPosition blockposition1, boolean flag) {
            this.getBlock().neighborChanged(this.asState(), world, blockposition, block, blockposition1, flag);
        }

        public final void updateNeighbourShapes(GeneratorAccess generatoraccess, BlockPosition blockposition, int i) {
            this.updateNeighbourShapes(generatoraccess, blockposition, i, 512);
        }

        public final void updateNeighbourShapes(GeneratorAccess generatoraccess, BlockPosition blockposition, int i, int j) {
            BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
            EnumDirection[] aenumdirection = BlockBase.UPDATE_SHAPE_ORDER;
            int k = aenumdirection.length;

            for (int l = 0; l < k; ++l) {
                EnumDirection enumdirection = aenumdirection[l];

                blockposition_mutableblockposition.setWithOffset(blockposition, enumdirection);
                generatoraccess.neighborShapeChanged(enumdirection.getOpposite(), this.asState(), blockposition_mutableblockposition, blockposition, i, j);
            }

        }

        public final void updateIndirectNeighbourShapes(GeneratorAccess generatoraccess, BlockPosition blockposition, int i) {
            this.updateIndirectNeighbourShapes(generatoraccess, blockposition, i, 512);
        }

        public void updateIndirectNeighbourShapes(GeneratorAccess generatoraccess, BlockPosition blockposition, int i, int j) {
            this.getBlock().updateIndirectNeighbourShapes(this.asState(), generatoraccess, blockposition, i, j);
        }

        public void onPlace(World world, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
            this.getBlock().onPlace(this.asState(), world, blockposition, iblockdata, flag);
        }

        public void onRemove(World world, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
            this.getBlock().onRemove(this.asState(), world, blockposition, iblockdata, flag);
        }

        public void onExplosionHit(World world, BlockPosition blockposition, Explosion explosion, BiConsumer<ItemStack, BlockPosition> biconsumer) {
            this.getBlock().onExplosionHit(this.asState(), world, blockposition, explosion, biconsumer);
        }

        public void tick(WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
            this.getBlock().tick(this.asState(), worldserver, blockposition, randomsource);
        }

        public void randomTick(WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
            this.getBlock().randomTick(this.asState(), worldserver, blockposition, randomsource);
        }

        public void entityInside(World world, BlockPosition blockposition, Entity entity) {
            this.getBlock().entityInside(this.asState(), world, blockposition, entity);
        }

        public void spawnAfterBreak(WorldServer worldserver, BlockPosition blockposition, ItemStack itemstack, boolean flag) {
            this.getBlock().spawnAfterBreak(this.asState(), worldserver, blockposition, itemstack, flag);
        }

        public List<ItemStack> getDrops(LootParams.a lootparams_a) {
            return this.getBlock().getDrops(this.asState(), lootparams_a);
        }

        public ItemInteractionResult useItemOn(ItemStack itemstack, World world, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
            return this.getBlock().useItemOn(itemstack, this.asState(), world, movingobjectpositionblock.getBlockPos(), entityhuman, enumhand, movingobjectpositionblock);
        }

        public EnumInteractionResult useWithoutItem(World world, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
            return this.getBlock().useWithoutItem(this.asState(), world, movingobjectpositionblock.getBlockPos(), entityhuman, movingobjectpositionblock);
        }

        public void attack(World world, BlockPosition blockposition, EntityHuman entityhuman) {
            this.getBlock().attack(this.asState(), world, blockposition, entityhuman);
        }

        public boolean isSuffocating(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.isSuffocating.test(this.asState(), iblockaccess, blockposition);
        }

        public boolean isViewBlocking(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.isViewBlocking.test(this.asState(), iblockaccess, blockposition);
        }

        public IBlockData updateShape(EnumDirection enumdirection, IBlockData iblockdata, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
            return this.getBlock().updateShape(this.asState(), enumdirection, iblockdata, generatoraccess, blockposition, blockposition1);
        }

        public boolean isPathfindable(PathMode pathmode) {
            return this.getBlock().isPathfindable(this.asState(), pathmode);
        }

        public boolean canBeReplaced(BlockActionContext blockactioncontext) {
            return this.getBlock().canBeReplaced(this.asState(), blockactioncontext);
        }

        public boolean canBeReplaced(FluidType fluidtype) {
            return this.getBlock().canBeReplaced(this.asState(), fluidtype);
        }

        public boolean canBeReplaced() {
            return this.replaceable;
        }

        public boolean canSurvive(IWorldReader iworldreader, BlockPosition blockposition) {
            return this.getBlock().canSurvive(this.asState(), iworldreader, blockposition);
        }

        public boolean hasPostProcess(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.hasPostProcess.test(this.asState(), iblockaccess, blockposition);
        }

        @Nullable
        public ITileInventory getMenuProvider(World world, BlockPosition blockposition) {
            return this.getBlock().getMenuProvider(this.asState(), world, blockposition);
        }

        public boolean is(TagKey<Block> tagkey) {
            return this.getBlock().builtInRegistryHolder().is(tagkey);
        }

        public boolean is(TagKey<Block> tagkey, Predicate<BlockBase.BlockData> predicate) {
            return this.is(tagkey) && predicate.test(this);
        }

        public boolean is(HolderSet<Block> holderset) {
            return holderset.contains(this.getBlock().builtInRegistryHolder());
        }

        public boolean is(Holder<Block> holder) {
            return this.is((Block) holder.value());
        }

        public Stream<TagKey<Block>> getTags() {
            return this.getBlock().builtInRegistryHolder().tags();
        }

        public boolean hasBlockEntity() {
            return this.getBlock() instanceof ITileEntity;
        }

        @Nullable
        public <T extends TileEntity> BlockEntityTicker<T> getTicker(World world, TileEntityTypes<T> tileentitytypes) {
            return this.getBlock() instanceof ITileEntity ? ((ITileEntity) this.getBlock()).getTicker(world, this.asState(), tileentitytypes) : null;
        }

        public boolean is(Block block) {
            return this.getBlock() == block;
        }

        public boolean is(ResourceKey<Block> resourcekey) {
            return this.getBlock().builtInRegistryHolder().is(resourcekey);
        }

        public Fluid getFluidState() {
            return this.fluidState;
        }

        public boolean isRandomlyTicking() {
            return this.isRandomlyTicking;
        }

        public long getSeed(BlockPosition blockposition) {
            return this.getBlock().getSeed(this.asState(), blockposition);
        }

        public SoundEffectType getSoundType() {
            return this.getBlock().getSoundType(this.asState());
        }

        public void onProjectileHit(World world, IBlockData iblockdata, MovingObjectPositionBlock movingobjectpositionblock, IProjectile iprojectile) {
            this.getBlock().onProjectileHit(world, iblockdata, movingobjectpositionblock, iprojectile);
        }

        public boolean isFaceSturdy(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
            return this.isFaceSturdy(iblockaccess, blockposition, enumdirection, EnumBlockSupport.FULL);
        }

        public boolean isFaceSturdy(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection, EnumBlockSupport enumblocksupport) {
            return this.cache != null ? this.cache.isFaceSturdy(enumdirection, enumblocksupport) : enumblocksupport.isSupporting(this.asState(), iblockaccess, blockposition, enumdirection);
        }

        public boolean isCollisionShapeFullBlock(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.cache != null ? this.cache.isCollisionShapeFullBlock : this.getBlock().isCollisionShapeFullBlock(this.asState(), iblockaccess, blockposition);
        }

        protected abstract IBlockData asState();

        public boolean requiresCorrectToolForDrops() {
            return this.requiresCorrectToolForDrops;
        }

        public boolean shouldSpawnTerrainParticles() {
            return this.spawnTerrainParticles;
        }

        public BlockPropertyInstrument instrument() {
            return this.instrument;
        }

        private static final class Cache {

            private static final EnumDirection[] DIRECTIONS = EnumDirection.values();
            private static final int SUPPORT_TYPE_COUNT = EnumBlockSupport.values().length;
            protected final boolean solidRender;
            final boolean propagatesSkylightDown;
            final int lightBlock;
            @Nullable
            final VoxelShape[] occlusionShapes;
            protected final VoxelShape collisionShape;
            protected final boolean largeCollisionShape;
            private final boolean[] faceSturdy;
            protected final boolean isCollisionShapeFullBlock;

            Cache(IBlockData iblockdata) {
                Block block = iblockdata.getBlock();

                this.solidRender = iblockdata.isSolidRender(BlockAccessAir.INSTANCE, BlockPosition.ZERO);
                this.propagatesSkylightDown = block.propagatesSkylightDown(iblockdata, BlockAccessAir.INSTANCE, BlockPosition.ZERO);
                this.lightBlock = block.getLightBlock(iblockdata, BlockAccessAir.INSTANCE, BlockPosition.ZERO);
                int i;

                if (!iblockdata.canOcclude()) {
                    this.occlusionShapes = null;
                } else {
                    this.occlusionShapes = new VoxelShape[BlockBase.BlockData.Cache.DIRECTIONS.length];
                    VoxelShape voxelshape = block.getOcclusionShape(iblockdata, BlockAccessAir.INSTANCE, BlockPosition.ZERO);
                    EnumDirection[] aenumdirection = BlockBase.BlockData.Cache.DIRECTIONS;

                    i = aenumdirection.length;

                    for (int j = 0; j < i; ++j) {
                        EnumDirection enumdirection = aenumdirection[j];

                        this.occlusionShapes[enumdirection.ordinal()] = VoxelShapes.getFaceShape(voxelshape, enumdirection);
                    }
                }

                this.collisionShape = block.getCollisionShape(iblockdata, BlockAccessAir.INSTANCE, BlockPosition.ZERO, VoxelShapeCollision.empty());
                if (!this.collisionShape.isEmpty() && iblockdata.hasOffsetFunction()) {
                    throw new IllegalStateException(String.format(Locale.ROOT, "%s has a collision shape and an offset type, but is not marked as dynamicShape in its properties.", BuiltInRegistries.BLOCK.getKey(block)));
                } else {
                    this.largeCollisionShape = Arrays.stream(EnumDirection.EnumAxis.values()).anyMatch((enumdirection_enumaxis) -> {
                        return this.collisionShape.min(enumdirection_enumaxis) < 0.0D || this.collisionShape.max(enumdirection_enumaxis) > 1.0D;
                    });
                    this.faceSturdy = new boolean[BlockBase.BlockData.Cache.DIRECTIONS.length * BlockBase.BlockData.Cache.SUPPORT_TYPE_COUNT];
                    EnumDirection[] aenumdirection1 = BlockBase.BlockData.Cache.DIRECTIONS;
                    int k = aenumdirection1.length;

                    for (i = 0; i < k; ++i) {
                        EnumDirection enumdirection1 = aenumdirection1[i];
                        EnumBlockSupport[] aenumblocksupport = EnumBlockSupport.values();
                        int l = aenumblocksupport.length;

                        for (int i1 = 0; i1 < l; ++i1) {
                            EnumBlockSupport enumblocksupport = aenumblocksupport[i1];

                            this.faceSturdy[getFaceSupportIndex(enumdirection1, enumblocksupport)] = enumblocksupport.isSupporting(iblockdata, BlockAccessAir.INSTANCE, BlockPosition.ZERO, enumdirection1);
                        }
                    }

                    this.isCollisionShapeFullBlock = Block.isShapeFullBlock(iblockdata.getCollisionShape(BlockAccessAir.INSTANCE, BlockPosition.ZERO));
                }
            }

            public boolean isFaceSturdy(EnumDirection enumdirection, EnumBlockSupport enumblocksupport) {
                return this.faceSturdy[getFaceSupportIndex(enumdirection, enumblocksupport)];
            }

            private static int getFaceSupportIndex(EnumDirection enumdirection, EnumBlockSupport enumblocksupport) {
                return enumdirection.ordinal() * BlockBase.BlockData.Cache.SUPPORT_TYPE_COUNT + enumblocksupport.ordinal();
            }
        }
    }

    public static enum EnumRandomOffset {

        NONE, XZ, XYZ;

        private EnumRandomOffset() {}
    }
}
