package net.minecraft.world.level.block;

import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.IRegistry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.references.Items;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityShulkerBox;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.grower.WorldGenTreeProvider;
import net.minecraft.world.level.block.piston.BlockPiston;
import net.minecraft.world.level.block.piston.BlockPistonExtension;
import net.minecraft.world.level.block.piston.BlockPistonMoving;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockPropertyBedPart;
import net.minecraft.world.level.block.state.properties.BlockPropertyInstrument;
import net.minecraft.world.level.block.state.properties.BlockPropertyWood;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.material.EnumPistonReaction;
import net.minecraft.world.level.material.FluidTypes;
import net.minecraft.world.level.material.MaterialMapColor;

public class Blocks {

    private static final BlockBase.f NOT_CLOSED_SHULKER = (iblockdata, iblockaccess, blockposition) -> {
        TileEntity tileentity = iblockaccess.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityShulkerBox tileentityshulkerbox) {
            return tileentityshulkerbox.isClosed();
        } else {
            return true;
        }
    };
    public static final Block AIR = register("air", new BlockAir(BlockBase.Info.of().replaceable().noCollission().noLootTable().air()));
    public static final Block STONE = register("stone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block GRANITE = register("granite", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block POLISHED_GRANITE = register("polished_granite", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block DIORITE = register("diorite", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block POLISHED_DIORITE = register("polished_diorite", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block ANDESITE = register("andesite", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block POLISHED_ANDESITE = register("polished_andesite", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block GRASS_BLOCK = register("grass_block", new BlockGrass(BlockBase.Info.of().mapColor(MaterialMapColor.GRASS).randomTicks().strength(0.6F).sound(SoundEffectType.GRASS)));
    public static final Block DIRT = register("dirt", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).strength(0.5F).sound(SoundEffectType.GRAVEL)));
    public static final Block COARSE_DIRT = register("coarse_dirt", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).strength(0.5F).sound(SoundEffectType.GRAVEL)));
    public static final Block PODZOL = register("podzol", new BlockDirtSnow(BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).strength(0.5F).sound(SoundEffectType.GRAVEL)));
    public static final Block COBBLESTONE = register("cobblestone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block OAK_PLANKS = register("oak_planks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block SPRUCE_PLANKS = register("spruce_planks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block BIRCH_PLANKS = register("birch_planks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block JUNGLE_PLANKS = register("jungle_planks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block ACACIA_PLANKS = register("acacia_planks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block CHERRY_PLANKS = register("cherry_planks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_WHITE).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.CHERRY_WOOD).ignitedByLava()));
    public static final Block DARK_OAK_PLANKS = register("dark_oak_planks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block MANGROVE_PLANKS = register("mangrove_planks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block BAMBOO_PLANKS = register("bamboo_planks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.BAMBOO_WOOD).ignitedByLava()));
    public static final Block BAMBOO_MOSAIC = register("bamboo_mosaic", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.BAMBOO_WOOD).ignitedByLava()));
    public static final Block OAK_SAPLING = register("oak_sapling", new BlockSapling(WorldGenTreeProvider.OAK, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SPRUCE_SAPLING = register("spruce_sapling", new BlockSapling(WorldGenTreeProvider.SPRUCE, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BIRCH_SAPLING = register("birch_sapling", new BlockSapling(WorldGenTreeProvider.BIRCH, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block JUNGLE_SAPLING = register("jungle_sapling", new BlockSapling(WorldGenTreeProvider.JUNGLE, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ACACIA_SAPLING = register("acacia_sapling", new BlockSapling(WorldGenTreeProvider.ACACIA, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CHERRY_SAPLING = register("cherry_sapling", new BlockSapling(WorldGenTreeProvider.CHERRY, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).noCollission().randomTicks().instabreak().sound(SoundEffectType.CHERRY_SAPLING).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block DARK_OAK_SAPLING = register("dark_oak_sapling", new BlockSapling(WorldGenTreeProvider.DARK_OAK, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block MANGROVE_PROPAGULE = register("mangrove_propagule", new MangrovePropaguleBlock(WorldGenTreeProvider.MANGROVE, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BEDROCK = register("bedrock", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).strength(-1.0F, 3600000.0F).noLootTable().isValidSpawn(Blocks::never)));
    public static final Block WATER = register("water", new BlockFluids(FluidTypes.WATER, BlockBase.Info.of().mapColor(MaterialMapColor.WATER).replaceable().noCollission().strength(100.0F).pushReaction(EnumPistonReaction.DESTROY).noLootTable().liquid().sound(SoundEffectType.EMPTY)));
    public static final Block LAVA = register("lava", new BlockFluids(FluidTypes.LAVA, BlockBase.Info.of().mapColor(MaterialMapColor.FIRE).replaceable().noCollission().randomTicks().strength(100.0F).lightLevel((iblockdata) -> {
        return 15;
    }).pushReaction(EnumPistonReaction.DESTROY).noLootTable().liquid().sound(SoundEffectType.EMPTY)));
    public static final Block SAND = register("sand", new ColoredFallingBlock(new ColorRGBA(14406560), BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block SUSPICIOUS_SAND = register("suspicious_sand", new BrushableBlock(Blocks.SAND, SoundEffects.BRUSH_SAND, SoundEffects.BRUSH_SAND_COMPLETED, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.SNARE).strength(0.25F).sound(SoundEffectType.SUSPICIOUS_SAND).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block RED_SAND = register("red_sand", new ColoredFallingBlock(new ColorRGBA(11098145), BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block GRAVEL = register("gravel", new ColoredFallingBlock(new ColorRGBA(-8356741), BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.SNARE).strength(0.6F).sound(SoundEffectType.GRAVEL)));
    public static final Block SUSPICIOUS_GRAVEL = register("suspicious_gravel", new BrushableBlock(Blocks.GRAVEL, SoundEffects.BRUSH_GRAVEL, SoundEffects.BRUSH_GRAVEL_COMPLETED, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.SNARE).strength(0.25F).sound(SoundEffectType.SUSPICIOUS_GRAVEL).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block GOLD_ORE = register("gold_ore", new DropExperienceBlock(ConstantInt.of(0), BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final Block DEEPSLATE_GOLD_ORE = register("deepslate_gold_ore", new DropExperienceBlock(ConstantInt.of(0), BlockBase.Info.ofLegacyCopy(Blocks.GOLD_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE)));
    public static final Block IRON_ORE = register("iron_ore", new DropExperienceBlock(ConstantInt.of(0), BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final Block DEEPSLATE_IRON_ORE = register("deepslate_iron_ore", new DropExperienceBlock(ConstantInt.of(0), BlockBase.Info.ofLegacyCopy(Blocks.IRON_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE)));
    public static final Block COAL_ORE = register("coal_ore", new DropExperienceBlock(UniformInt.of(0, 2), BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final Block DEEPSLATE_COAL_ORE = register("deepslate_coal_ore", new DropExperienceBlock(UniformInt.of(0, 2), BlockBase.Info.ofLegacyCopy(Blocks.COAL_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE)));
    public static final Block NETHER_GOLD_ORE = register("nether_gold_ore", new DropExperienceBlock(UniformInt.of(0, 1), BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundEffectType.NETHER_GOLD_ORE)));
    public static final Block OAK_LOG = register("oak_log", log(MaterialMapColor.WOOD, MaterialMapColor.PODZOL));
    public static final Block SPRUCE_LOG = register("spruce_log", log(MaterialMapColor.PODZOL, MaterialMapColor.COLOR_BROWN));
    public static final Block BIRCH_LOG = register("birch_log", log(MaterialMapColor.SAND, MaterialMapColor.QUARTZ));
    public static final Block JUNGLE_LOG = register("jungle_log", log(MaterialMapColor.DIRT, MaterialMapColor.PODZOL));
    public static final Block ACACIA_LOG = register("acacia_log", log(MaterialMapColor.COLOR_ORANGE, MaterialMapColor.STONE));
    public static final Block CHERRY_LOG = register("cherry_log", log(MaterialMapColor.TERRACOTTA_WHITE, MaterialMapColor.TERRACOTTA_GRAY, SoundEffectType.CHERRY_WOOD));
    public static final Block DARK_OAK_LOG = register("dark_oak_log", log(MaterialMapColor.COLOR_BROWN, MaterialMapColor.COLOR_BROWN));
    public static final Block MANGROVE_LOG = register("mangrove_log", log(MaterialMapColor.COLOR_RED, MaterialMapColor.PODZOL));
    public static final Block MANGROVE_ROOTS = register("mangrove_roots", new MangroveRootsBlock(BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(0.7F).sound(SoundEffectType.MANGROVE_ROOTS).noOcclusion().isSuffocating(Blocks::never).isViewBlocking(Blocks::never).noOcclusion().ignitedByLava()));
    public static final Block MUDDY_MANGROVE_ROOTS = register("muddy_mangrove_roots", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).strength(0.7F).sound(SoundEffectType.MUDDY_MANGROVE_ROOTS)));
    public static final Block BAMBOO_BLOCK = register("bamboo_block", log(MaterialMapColor.COLOR_YELLOW, MaterialMapColor.PLANT, SoundEffectType.BAMBOO_WOOD));
    public static final Block STRIPPED_SPRUCE_LOG = register("stripped_spruce_log", log(MaterialMapColor.PODZOL, MaterialMapColor.PODZOL));
    public static final Block STRIPPED_BIRCH_LOG = register("stripped_birch_log", log(MaterialMapColor.SAND, MaterialMapColor.SAND));
    public static final Block STRIPPED_JUNGLE_LOG = register("stripped_jungle_log", log(MaterialMapColor.DIRT, MaterialMapColor.DIRT));
    public static final Block STRIPPED_ACACIA_LOG = register("stripped_acacia_log", log(MaterialMapColor.COLOR_ORANGE, MaterialMapColor.COLOR_ORANGE));
    public static final Block STRIPPED_CHERRY_LOG = register("stripped_cherry_log", log(MaterialMapColor.TERRACOTTA_WHITE, MaterialMapColor.TERRACOTTA_PINK, SoundEffectType.CHERRY_WOOD));
    public static final Block STRIPPED_DARK_OAK_LOG = register("stripped_dark_oak_log", log(MaterialMapColor.COLOR_BROWN, MaterialMapColor.COLOR_BROWN));
    public static final Block STRIPPED_OAK_LOG = register("stripped_oak_log", log(MaterialMapColor.WOOD, MaterialMapColor.WOOD));
    public static final Block STRIPPED_MANGROVE_LOG = register("stripped_mangrove_log", log(MaterialMapColor.COLOR_RED, MaterialMapColor.COLOR_RED));
    public static final Block STRIPPED_BAMBOO_BLOCK = register("stripped_bamboo_block", log(MaterialMapColor.COLOR_YELLOW, MaterialMapColor.COLOR_YELLOW, SoundEffectType.BAMBOO_WOOD));
    public static final Block OAK_WOOD = register("oak_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block SPRUCE_WOOD = register("spruce_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block BIRCH_WOOD = register("birch_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block JUNGLE_WOOD = register("jungle_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block ACACIA_WOOD = register("acacia_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block CHERRY_WOOD = register("cherry_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_GRAY).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.CHERRY_WOOD).ignitedByLava()));
    public static final Block DARK_OAK_WOOD = register("dark_oak_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block MANGROVE_WOOD = register("mangrove_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block STRIPPED_OAK_WOOD = register("stripped_oak_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block STRIPPED_SPRUCE_WOOD = register("stripped_spruce_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block STRIPPED_BIRCH_WOOD = register("stripped_birch_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block STRIPPED_JUNGLE_WOOD = register("stripped_jungle_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block STRIPPED_ACACIA_WOOD = register("stripped_acacia_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block STRIPPED_CHERRY_WOOD = register("stripped_cherry_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_PINK).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.CHERRY_WOOD).ignitedByLava()));
    public static final Block STRIPPED_DARK_OAK_WOOD = register("stripped_dark_oak_wood", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block STRIPPED_MANGROVE_WOOD = register("stripped_mangrove_wood", log(MaterialMapColor.COLOR_RED, MaterialMapColor.COLOR_RED));
    public static final Block OAK_LEAVES = register("oak_leaves", leaves(SoundEffectType.GRASS));
    public static final Block SPRUCE_LEAVES = register("spruce_leaves", leaves(SoundEffectType.GRASS));
    public static final Block BIRCH_LEAVES = register("birch_leaves", leaves(SoundEffectType.GRASS));
    public static final Block JUNGLE_LEAVES = register("jungle_leaves", leaves(SoundEffectType.GRASS));
    public static final Block ACACIA_LEAVES = register("acacia_leaves", leaves(SoundEffectType.GRASS));
    public static final Block CHERRY_LEAVES = register("cherry_leaves", new CherryLeavesBlock(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).strength(0.2F).randomTicks().sound(SoundEffectType.CHERRY_LEAVES).noOcclusion().isValidSpawn(Blocks::ocelotOrParrot).isSuffocating(Blocks::never).isViewBlocking(Blocks::never).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never)));
    public static final Block DARK_OAK_LEAVES = register("dark_oak_leaves", leaves(SoundEffectType.GRASS));
    public static final Block MANGROVE_LEAVES = register("mangrove_leaves", new MangroveLeavesBlock(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).strength(0.2F).randomTicks().sound(SoundEffectType.GRASS).noOcclusion().isValidSpawn(Blocks::ocelotOrParrot).isSuffocating(Blocks::never).isViewBlocking(Blocks::never).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never)));
    public static final Block AZALEA_LEAVES = register("azalea_leaves", leaves(SoundEffectType.AZALEA_LEAVES));
    public static final Block FLOWERING_AZALEA_LEAVES = register("flowering_azalea_leaves", leaves(SoundEffectType.AZALEA_LEAVES));
    public static final Block SPONGE = register("sponge", new BlockSponge(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).strength(0.6F).sound(SoundEffectType.SPONGE)));
    public static final Block WET_SPONGE = register("wet_sponge", new BlockWetSponge(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).strength(0.6F).sound(SoundEffectType.WET_SPONGE)));
    public static final Block GLASS = register("glass", new BlockGlassAbstract(BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion().isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)));
    public static final Block LAPIS_ORE = register("lapis_ore", new DropExperienceBlock(UniformInt.of(2, 5), BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final Block DEEPSLATE_LAPIS_ORE = register("deepslate_lapis_ore", new DropExperienceBlock(UniformInt.of(2, 5), BlockBase.Info.ofLegacyCopy(Blocks.LAPIS_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE)));
    public static final Block LAPIS_BLOCK = register("lapis_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.LAPIS).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final Block DISPENSER = register("dispenser", new BlockDispenser(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F)));
    public static final Block SANDSTONE = register("sandstone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)));
    public static final Block CHISELED_SANDSTONE = register("chiseled_sandstone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)));
    public static final Block CUT_SANDSTONE = register("cut_sandstone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)));
    public static final Block NOTE_BLOCK = register("note_block", new BlockNote(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).sound(SoundEffectType.WOOD).strength(0.8F).ignitedByLava()));
    public static final Block WHITE_BED = register("white_bed", bed(EnumColor.WHITE));
    public static final Block ORANGE_BED = register("orange_bed", bed(EnumColor.ORANGE));
    public static final Block MAGENTA_BED = register("magenta_bed", bed(EnumColor.MAGENTA));
    public static final Block LIGHT_BLUE_BED = register("light_blue_bed", bed(EnumColor.LIGHT_BLUE));
    public static final Block YELLOW_BED = register("yellow_bed", bed(EnumColor.YELLOW));
    public static final Block LIME_BED = register("lime_bed", bed(EnumColor.LIME));
    public static final Block PINK_BED = register("pink_bed", bed(EnumColor.PINK));
    public static final Block GRAY_BED = register("gray_bed", bed(EnumColor.GRAY));
    public static final Block LIGHT_GRAY_BED = register("light_gray_bed", bed(EnumColor.LIGHT_GRAY));
    public static final Block CYAN_BED = register("cyan_bed", bed(EnumColor.CYAN));
    public static final Block PURPLE_BED = register("purple_bed", bed(EnumColor.PURPLE));
    public static final Block BLUE_BED = register("blue_bed", bed(EnumColor.BLUE));
    public static final Block BROWN_BED = register("brown_bed", bed(EnumColor.BROWN));
    public static final Block GREEN_BED = register("green_bed", bed(EnumColor.GREEN));
    public static final Block RED_BED = register("red_bed", bed(EnumColor.RED));
    public static final Block BLACK_BED = register("black_bed", bed(EnumColor.BLACK));
    public static final Block POWERED_RAIL = register("powered_rail", new BlockPoweredRail(BlockBase.Info.of().noCollission().strength(0.7F).sound(SoundEffectType.METAL)));
    public static final Block DETECTOR_RAIL = register("detector_rail", new BlockMinecartDetector(BlockBase.Info.of().noCollission().strength(0.7F).sound(SoundEffectType.METAL)));
    public static final Block STICKY_PISTON = register("sticky_piston", pistonBase(true));
    public static final Block COBWEB = register("cobweb", new BlockWeb(BlockBase.Info.of().mapColor(MaterialMapColor.WOOL).sound(SoundEffectType.COBWEB).forceSolidOn().noCollission().requiresCorrectToolForDrops().strength(4.0F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SHORT_GRASS = register("short_grass", new BlockLongGrass(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XYZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block FERN = register("fern", new BlockLongGrass(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XYZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block DEAD_BUSH = register("dead_bush", new BlockDeadBush(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).replaceable().noCollission().instabreak().sound(SoundEffectType.GRASS).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SEAGRASS = register("seagrass", new SeagrassBlock(BlockBase.Info.of().mapColor(MaterialMapColor.WATER).replaceable().noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block TALL_SEAGRASS = register("tall_seagrass", new TallSeagrassBlock(BlockBase.Info.of().mapColor(MaterialMapColor.WATER).replaceable().noCollission().instabreak().sound(SoundEffectType.WET_GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block PISTON = register("piston", pistonBase(false));
    public static final Block PISTON_HEAD = register("piston_head", new BlockPistonExtension(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).strength(1.5F).noLootTable().pushReaction(EnumPistonReaction.BLOCK)));
    public static final Block WHITE_WOOL = register("white_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.SNOW).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block ORANGE_WOOL = register("orange_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block MAGENTA_WOOL = register("magenta_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_MAGENTA).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block LIGHT_BLUE_WOOL = register("light_blue_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_BLUE).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block YELLOW_WOOL = register("yellow_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block LIME_WOOL = register("lime_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GREEN).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block PINK_WOOL = register("pink_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block GRAY_WOOL = register("gray_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block LIGHT_GRAY_WOOL = register("light_gray_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GRAY).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block CYAN_WOOL = register("cyan_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block PURPLE_WOOL = register("purple_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block BLUE_WOOL = register("blue_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLUE).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block BROWN_WOOL = register("brown_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block GREEN_WOOL = register("green_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block RED_WOOL = register("red_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block BLACK_WOOL = register("black_wool", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.GUITAR).strength(0.8F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block MOVING_PISTON = register("moving_piston", new BlockPistonMoving(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).forceSolidOn().strength(-1.0F).dynamicShape().noLootTable().noOcclusion().isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never).pushReaction(EnumPistonReaction.BLOCK)));
    public static final Block DANDELION = register("dandelion", new BlockFlowers(MobEffects.SATURATION, 0.35F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block TORCHFLOWER = register("torchflower", new BlockFlowers(MobEffects.NIGHT_VISION, 5.0F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block POPPY = register("poppy", new BlockFlowers(MobEffects.NIGHT_VISION, 5.0F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BLUE_ORCHID = register("blue_orchid", new BlockFlowers(MobEffects.SATURATION, 0.35F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ALLIUM = register("allium", new BlockFlowers(MobEffects.FIRE_RESISTANCE, 4.0F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block AZURE_BLUET = register("azure_bluet", new BlockFlowers(MobEffects.BLINDNESS, 8.0F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block RED_TULIP = register("red_tulip", new BlockFlowers(MobEffects.WEAKNESS, 9.0F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ORANGE_TULIP = register("orange_tulip", new BlockFlowers(MobEffects.WEAKNESS, 9.0F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block WHITE_TULIP = register("white_tulip", new BlockFlowers(MobEffects.WEAKNESS, 9.0F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block PINK_TULIP = register("pink_tulip", new BlockFlowers(MobEffects.WEAKNESS, 9.0F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block OXEYE_DAISY = register("oxeye_daisy", new BlockFlowers(MobEffects.REGENERATION, 8.0F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CORNFLOWER = register("cornflower", new BlockFlowers(MobEffects.JUMP, 6.0F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block WITHER_ROSE = register("wither_rose", new BlockWitherRose(MobEffects.WITHER, 8.0F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block LILY_OF_THE_VALLEY = register("lily_of_the_valley", new BlockFlowers(MobEffects.POISON, 12.0F, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BROWN_MUSHROOM = register("brown_mushroom", new BlockMushroom(TreeFeatures.HUGE_BROWN_MUSHROOM, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).lightLevel((iblockdata) -> {
        return 1;
    }).hasPostProcess(Blocks::always).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block RED_MUSHROOM = register("red_mushroom", new BlockMushroom(TreeFeatures.HUGE_RED_MUSHROOM, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).hasPostProcess(Blocks::always).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block GOLD_BLOCK = register("gold_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.GOLD).instrument(BlockPropertyInstrument.BELL).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundEffectType.METAL)));
    public static final Block IRON_BLOCK = register("iron_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.METAL).instrument(BlockPropertyInstrument.IRON_XYLOPHONE).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundEffectType.METAL)));
    public static final Block BRICKS = register("bricks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block TNT = register("tnt", new BlockTNT(BlockBase.Info.of().mapColor(MaterialMapColor.FIRE).instabreak().sound(SoundEffectType.GRASS).ignitedByLava().isRedstoneConductor(Blocks::never)));
    public static final Block BOOKSHELF = register("bookshelf", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(1.5F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block CHISELED_BOOKSHELF = register("chiseled_bookshelf", new ChiseledBookShelfBlock(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(1.5F).sound(SoundEffectType.CHISELED_BOOKSHELF).ignitedByLava()));
    public static final Block MOSSY_COBBLESTONE = register("mossy_cobblestone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block OBSIDIAN = register("obsidian", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F)));
    public static final Block TORCH = register("torch", new BlockTorch(Particles.FLAME, BlockBase.Info.of().noCollission().instabreak().lightLevel((iblockdata) -> {
        return 14;
    }).sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block WALL_TORCH = register("wall_torch", new BlockTorchWall(Particles.FLAME, BlockBase.Info.of().noCollission().instabreak().lightLevel((iblockdata) -> {
        return 14;
    }).sound(SoundEffectType.WOOD).dropsLike(Blocks.TORCH).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block FIRE = register("fire", new BlockFire(BlockBase.Info.of().mapColor(MaterialMapColor.FIRE).replaceable().noCollission().instabreak().lightLevel((iblockdata) -> {
        return 15;
    }).sound(SoundEffectType.WOOL).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SOUL_FIRE = register("soul_fire", new BlockSoulFire(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_BLUE).replaceable().noCollission().instabreak().lightLevel((iblockdata) -> {
        return 10;
    }).sound(SoundEffectType.WOOL).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SPAWNER = register("spawner", new BlockMobSpawner(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F).sound(SoundEffectType.METAL).noOcclusion()));
    public static final Block OAK_STAIRS = register("oak_stairs", legacyStair(Blocks.OAK_PLANKS));
    public static final Block CHEST = register("chest", new BlockChest(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava(), () -> {
        return TileEntityTypes.CHEST;
    }));
    public static final Block REDSTONE_WIRE = register("redstone_wire", new BlockRedstoneWire(BlockBase.Info.of().noCollission().instabreak().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block DIAMOND_ORE = register("diamond_ore", new DropExperienceBlock(UniformInt.of(3, 7), BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final Block DEEPSLATE_DIAMOND_ORE = register("deepslate_diamond_ore", new DropExperienceBlock(UniformInt.of(3, 7), BlockBase.Info.ofLegacyCopy(Blocks.DIAMOND_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE)));
    public static final Block DIAMOND_BLOCK = register("diamond_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundEffectType.METAL)));
    public static final Block CRAFTING_TABLE = register("crafting_table", new BlockWorkbench(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block WHEAT = register("wheat", new BlockCrops(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.CROP).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block FARMLAND = register("farmland", new BlockSoil(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).randomTicks().strength(0.6F).sound(SoundEffectType.GRAVEL).isViewBlocking(Blocks::always).isSuffocating(Blocks::always)));
    public static final Block FURNACE = register("furnace", new BlockFurnaceFurace(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F).lightLevel(litBlockEmission(13))));
    public static final Block OAK_SIGN = register("oak_sign", new BlockFloorSign(BlockPropertyWood.OAK, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block SPRUCE_SIGN = register("spruce_sign", new BlockFloorSign(BlockPropertyWood.SPRUCE, BlockBase.Info.of().mapColor(Blocks.SPRUCE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block BIRCH_SIGN = register("birch_sign", new BlockFloorSign(BlockPropertyWood.BIRCH, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block ACACIA_SIGN = register("acacia_sign", new BlockFloorSign(BlockPropertyWood.ACACIA, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block CHERRY_SIGN = register("cherry_sign", new BlockFloorSign(BlockPropertyWood.CHERRY, BlockBase.Info.of().mapColor(Blocks.CHERRY_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block JUNGLE_SIGN = register("jungle_sign", new BlockFloorSign(BlockPropertyWood.JUNGLE, BlockBase.Info.of().mapColor(Blocks.JUNGLE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block DARK_OAK_SIGN = register("dark_oak_sign", new BlockFloorSign(BlockPropertyWood.DARK_OAK, BlockBase.Info.of().mapColor(Blocks.DARK_OAK_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block MANGROVE_SIGN = register("mangrove_sign", new BlockFloorSign(BlockPropertyWood.MANGROVE, BlockBase.Info.of().mapColor(Blocks.MANGROVE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block BAMBOO_SIGN = register("bamboo_sign", new BlockFloorSign(BlockPropertyWood.BAMBOO, BlockBase.Info.of().mapColor(Blocks.BAMBOO_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block OAK_DOOR = register("oak_door", new BlockDoor(BlockSetType.OAK, BlockBase.Info.of().mapColor(Blocks.OAK_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block LADDER = register("ladder", new BlockLadder(BlockBase.Info.of().forceSolidOff().strength(0.4F).sound(SoundEffectType.LADDER).noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block RAIL = register("rail", new BlockMinecartTrack(BlockBase.Info.of().noCollission().strength(0.7F).sound(SoundEffectType.METAL)));
    public static final Block COBBLESTONE_STAIRS = register("cobblestone_stairs", legacyStair(Blocks.COBBLESTONE));
    public static final Block OAK_WALL_SIGN = register("oak_wall_sign", new BlockWallSign(BlockPropertyWood.OAK, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).dropsLike(Blocks.OAK_SIGN).ignitedByLava()));
    public static final Block SPRUCE_WALL_SIGN = register("spruce_wall_sign", new BlockWallSign(BlockPropertyWood.SPRUCE, BlockBase.Info.of().mapColor(Blocks.SPRUCE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).dropsLike(Blocks.SPRUCE_SIGN).ignitedByLava()));
    public static final Block BIRCH_WALL_SIGN = register("birch_wall_sign", new BlockWallSign(BlockPropertyWood.BIRCH, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).dropsLike(Blocks.BIRCH_SIGN).ignitedByLava()));
    public static final Block ACACIA_WALL_SIGN = register("acacia_wall_sign", new BlockWallSign(BlockPropertyWood.ACACIA, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).dropsLike(Blocks.ACACIA_SIGN).ignitedByLava()));
    public static final Block CHERRY_WALL_SIGN = register("cherry_wall_sign", new BlockWallSign(BlockPropertyWood.CHERRY, BlockBase.Info.of().mapColor(Blocks.CHERRY_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).dropsLike(Blocks.CHERRY_SIGN).ignitedByLava()));
    public static final Block JUNGLE_WALL_SIGN = register("jungle_wall_sign", new BlockWallSign(BlockPropertyWood.JUNGLE, BlockBase.Info.of().mapColor(Blocks.JUNGLE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).dropsLike(Blocks.JUNGLE_SIGN).ignitedByLava()));
    public static final Block DARK_OAK_WALL_SIGN = register("dark_oak_wall_sign", new BlockWallSign(BlockPropertyWood.DARK_OAK, BlockBase.Info.of().mapColor(Blocks.DARK_OAK_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).dropsLike(Blocks.DARK_OAK_SIGN).ignitedByLava()));
    public static final Block MANGROVE_WALL_SIGN = register("mangrove_wall_sign", new BlockWallSign(BlockPropertyWood.MANGROVE, BlockBase.Info.of().mapColor(Blocks.MANGROVE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).dropsLike(Blocks.MANGROVE_SIGN).ignitedByLava()));
    public static final Block BAMBOO_WALL_SIGN = register("bamboo_wall_sign", new BlockWallSign(BlockPropertyWood.BAMBOO, BlockBase.Info.of().mapColor(Blocks.BAMBOO_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava().dropsLike(Blocks.BAMBOO_SIGN)));
    public static final Block OAK_HANGING_SIGN = register("oak_hanging_sign", new CeilingHangingSignBlock(BlockPropertyWood.OAK, BlockBase.Info.of().mapColor(Blocks.OAK_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block SPRUCE_HANGING_SIGN = register("spruce_hanging_sign", new CeilingHangingSignBlock(BlockPropertyWood.SPRUCE, BlockBase.Info.of().mapColor(Blocks.SPRUCE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block BIRCH_HANGING_SIGN = register("birch_hanging_sign", new CeilingHangingSignBlock(BlockPropertyWood.BIRCH, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block ACACIA_HANGING_SIGN = register("acacia_hanging_sign", new CeilingHangingSignBlock(BlockPropertyWood.ACACIA, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block CHERRY_HANGING_SIGN = register("cherry_hanging_sign", new CeilingHangingSignBlock(BlockPropertyWood.CHERRY, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_PINK).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block JUNGLE_HANGING_SIGN = register("jungle_hanging_sign", new CeilingHangingSignBlock(BlockPropertyWood.JUNGLE, BlockBase.Info.of().mapColor(Blocks.JUNGLE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block DARK_OAK_HANGING_SIGN = register("dark_oak_hanging_sign", new CeilingHangingSignBlock(BlockPropertyWood.DARK_OAK, BlockBase.Info.of().mapColor(Blocks.DARK_OAK_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block CRIMSON_HANGING_SIGN = register("crimson_hanging_sign", new CeilingHangingSignBlock(BlockPropertyWood.CRIMSON, BlockBase.Info.of().mapColor(MaterialMapColor.CRIMSON_STEM).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F)));
    public static final Block WARPED_HANGING_SIGN = register("warped_hanging_sign", new CeilingHangingSignBlock(BlockPropertyWood.WARPED, BlockBase.Info.of().mapColor(MaterialMapColor.WARPED_STEM).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F)));
    public static final Block MANGROVE_HANGING_SIGN = register("mangrove_hanging_sign", new CeilingHangingSignBlock(BlockPropertyWood.MANGROVE, BlockBase.Info.of().mapColor(Blocks.MANGROVE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block BAMBOO_HANGING_SIGN = register("bamboo_hanging_sign", new CeilingHangingSignBlock(BlockPropertyWood.BAMBOO, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava()));
    public static final Block OAK_WALL_HANGING_SIGN = register("oak_wall_hanging_sign", new WallHangingSignBlock(BlockPropertyWood.OAK, BlockBase.Info.of().mapColor(Blocks.OAK_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava().dropsLike(Blocks.OAK_HANGING_SIGN)));
    public static final Block SPRUCE_WALL_HANGING_SIGN = register("spruce_wall_hanging_sign", new WallHangingSignBlock(BlockPropertyWood.SPRUCE, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).dropsLike(Blocks.SPRUCE_HANGING_SIGN).ignitedByLava()));
    public static final Block BIRCH_WALL_HANGING_SIGN = register("birch_wall_hanging_sign", new WallHangingSignBlock(BlockPropertyWood.BIRCH, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).dropsLike(Blocks.BIRCH_HANGING_SIGN).ignitedByLava()));
    public static final Block ACACIA_WALL_HANGING_SIGN = register("acacia_wall_hanging_sign", new WallHangingSignBlock(BlockPropertyWood.ACACIA, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava().dropsLike(Blocks.ACACIA_HANGING_SIGN)));
    public static final Block CHERRY_WALL_HANGING_SIGN = register("cherry_wall_hanging_sign", new WallHangingSignBlock(BlockPropertyWood.CHERRY, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_PINK).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava().dropsLike(Blocks.CHERRY_HANGING_SIGN)));
    public static final Block JUNGLE_WALL_HANGING_SIGN = register("jungle_wall_hanging_sign", new WallHangingSignBlock(BlockPropertyWood.JUNGLE, BlockBase.Info.of().mapColor(Blocks.JUNGLE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava().dropsLike(Blocks.JUNGLE_HANGING_SIGN)));
    public static final Block DARK_OAK_WALL_HANGING_SIGN = register("dark_oak_wall_hanging_sign", new WallHangingSignBlock(BlockPropertyWood.DARK_OAK, BlockBase.Info.of().mapColor(Blocks.DARK_OAK_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava().dropsLike(Blocks.DARK_OAK_HANGING_SIGN)));
    public static final Block MANGROVE_WALL_HANGING_SIGN = register("mangrove_wall_hanging_sign", new WallHangingSignBlock(BlockPropertyWood.MANGROVE, BlockBase.Info.of().mapColor(Blocks.MANGROVE_LOG.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava().dropsLike(Blocks.MANGROVE_HANGING_SIGN)));
    public static final Block CRIMSON_WALL_HANGING_SIGN = register("crimson_wall_hanging_sign", new WallHangingSignBlock(BlockPropertyWood.CRIMSON, BlockBase.Info.of().mapColor(MaterialMapColor.CRIMSON_STEM).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).dropsLike(Blocks.CRIMSON_HANGING_SIGN)));
    public static final Block WARPED_WALL_HANGING_SIGN = register("warped_wall_hanging_sign", new WallHangingSignBlock(BlockPropertyWood.WARPED, BlockBase.Info.of().mapColor(MaterialMapColor.WARPED_STEM).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).dropsLike(Blocks.WARPED_HANGING_SIGN)));
    public static final Block BAMBOO_WALL_HANGING_SIGN = register("bamboo_wall_hanging_sign", new WallHangingSignBlock(BlockPropertyWood.BAMBOO, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).ignitedByLava().dropsLike(Blocks.BAMBOO_HANGING_SIGN)));
    public static final Block LEVER = register("lever", new BlockLever(BlockBase.Info.of().noCollission().strength(0.5F).sound(SoundEffectType.STONE).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block STONE_PRESSURE_PLATE = register("stone_pressure_plate", new BlockPressurePlateBinary(BlockSetType.STONE, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block IRON_DOOR = register("iron_door", new BlockDoor(BlockSetType.IRON, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(5.0F).noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block OAK_PRESSURE_PLATE = register("oak_pressure_plate", new BlockPressurePlateBinary(BlockSetType.OAK, BlockBase.Info.of().mapColor(Blocks.OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SPRUCE_PRESSURE_PLATE = register("spruce_pressure_plate", new BlockPressurePlateBinary(BlockSetType.SPRUCE, BlockBase.Info.of().mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BIRCH_PRESSURE_PLATE = register("birch_pressure_plate", new BlockPressurePlateBinary(BlockSetType.BIRCH, BlockBase.Info.of().mapColor(Blocks.BIRCH_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block JUNGLE_PRESSURE_PLATE = register("jungle_pressure_plate", new BlockPressurePlateBinary(BlockSetType.JUNGLE, BlockBase.Info.of().mapColor(Blocks.JUNGLE_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ACACIA_PRESSURE_PLATE = register("acacia_pressure_plate", new BlockPressurePlateBinary(BlockSetType.ACACIA, BlockBase.Info.of().mapColor(Blocks.ACACIA_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CHERRY_PRESSURE_PLATE = register("cherry_pressure_plate", new BlockPressurePlateBinary(BlockSetType.CHERRY, BlockBase.Info.of().mapColor(Blocks.CHERRY_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block DARK_OAK_PRESSURE_PLATE = register("dark_oak_pressure_plate", new BlockPressurePlateBinary(BlockSetType.DARK_OAK, BlockBase.Info.of().mapColor(Blocks.DARK_OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block MANGROVE_PRESSURE_PLATE = register("mangrove_pressure_plate", new BlockPressurePlateBinary(BlockSetType.MANGROVE, BlockBase.Info.of().mapColor(Blocks.MANGROVE_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BAMBOO_PRESSURE_PLATE = register("bamboo_pressure_plate", new BlockPressurePlateBinary(BlockSetType.BAMBOO, BlockBase.Info.of().mapColor(Blocks.BAMBOO_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block REDSTONE_ORE = register("redstone_ore", new BlockRedstoneOre(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().randomTicks().lightLevel(litBlockEmission(9)).strength(3.0F, 3.0F)));
    public static final Block DEEPSLATE_REDSTONE_ORE = register("deepslate_redstone_ore", new BlockRedstoneOre(BlockBase.Info.ofLegacyCopy(Blocks.REDSTONE_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE)));
    public static final Block REDSTONE_TORCH = register("redstone_torch", new BlockRedstoneTorch(BlockBase.Info.of().noCollission().instabreak().lightLevel(litBlockEmission(7)).sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block REDSTONE_WALL_TORCH = register("redstone_wall_torch", new BlockRedstoneTorchWall(BlockBase.Info.of().noCollission().instabreak().lightLevel(litBlockEmission(7)).sound(SoundEffectType.WOOD).dropsLike(Blocks.REDSTONE_TORCH).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block STONE_BUTTON = register("stone_button", stoneButton());
    public static final Block SNOW = register("snow", new BlockSnow(BlockBase.Info.of().mapColor(MaterialMapColor.SNOW).replaceable().forceSolidOff().randomTicks().strength(0.1F).requiresCorrectToolForDrops().sound(SoundEffectType.SNOW).isViewBlocking((iblockdata, iblockaccess, blockposition) -> {
        return (Integer) iblockdata.getValue(BlockSnow.LAYERS) >= 8;
    }).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ICE = register("ice", new BlockIce(BlockBase.Info.of().mapColor(MaterialMapColor.ICE).friction(0.98F).randomTicks().strength(0.5F).sound(SoundEffectType.GLASS).noOcclusion().isValidSpawn((iblockdata, iblockaccess, blockposition, entitytypes) -> {
        return entitytypes == EntityTypes.POLAR_BEAR;
    }).isRedstoneConductor(Blocks::never)));
    public static final Block SNOW_BLOCK = register("snow_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.SNOW).requiresCorrectToolForDrops().strength(0.2F).sound(SoundEffectType.SNOW)));
    public static final Block CACTUS = register("cactus", new BlockCactus(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).randomTicks().strength(0.4F).sound(SoundEffectType.WOOL).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CLAY = register("clay", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.CLAY).instrument(BlockPropertyInstrument.FLUTE).strength(0.6F).sound(SoundEffectType.GRAVEL)));
    public static final Block SUGAR_CANE = register("sugar_cane", new BlockReed(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block JUKEBOX = register("jukebox", new BlockJukeBox(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 6.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block OAK_FENCE = register("oak_fence", new BlockFence(BlockBase.Info.of().mapColor(Blocks.OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block NETHERRACK = register("netherrack", new BlockNetherrack(BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.4F).sound(SoundEffectType.NETHERRACK)));
    public static final Block SOUL_SAND = register("soul_sand", new BlockSlowSand(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.COW_BELL).strength(0.5F).speedFactor(0.4F).sound(SoundEffectType.SOUL_SAND).isValidSpawn(Blocks::always).isRedstoneConductor(Blocks::always).isViewBlocking(Blocks::always).isSuffocating(Blocks::always)));
    public static final Block SOUL_SOIL = register("soul_soil", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).strength(0.5F).sound(SoundEffectType.SOUL_SOIL)));
    public static final Block BASALT = register("basalt", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F).sound(SoundEffectType.BASALT)));
    public static final Block POLISHED_BASALT = register("polished_basalt", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F).sound(SoundEffectType.BASALT)));
    public static final Block SOUL_TORCH = register("soul_torch", new BlockTorch(Particles.SOUL_FIRE_FLAME, BlockBase.Info.of().noCollission().instabreak().lightLevel((iblockdata) -> {
        return 10;
    }).sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SOUL_WALL_TORCH = register("soul_wall_torch", new BlockTorchWall(Particles.SOUL_FIRE_FLAME, BlockBase.Info.of().noCollission().instabreak().lightLevel((iblockdata) -> {
        return 10;
    }).sound(SoundEffectType.WOOD).dropsLike(Blocks.SOUL_TORCH).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block GLOWSTONE = register("glowstone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.PLING).strength(0.3F).sound(SoundEffectType.GLASS).lightLevel((iblockdata) -> {
        return 15;
    }).isRedstoneConductor(Blocks::never)));
    public static final Block NETHER_PORTAL = register("nether_portal", new BlockPortal(BlockBase.Info.of().noCollission().randomTicks().strength(-1.0F).sound(SoundEffectType.GLASS).lightLevel((iblockdata) -> {
        return 11;
    }).pushReaction(EnumPistonReaction.BLOCK)));
    public static final Block CARVED_PUMPKIN = register("carved_pumpkin", new EquipableCarvedPumpkinBlock(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).strength(1.0F).sound(SoundEffectType.WOOD).isValidSpawn(Blocks::always).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block JACK_O_LANTERN = register("jack_o_lantern", new BlockPumpkinCarved(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).strength(1.0F).sound(SoundEffectType.WOOD).lightLevel((iblockdata) -> {
        return 15;
    }).isValidSpawn(Blocks::always).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CAKE = register("cake", new BlockCake(BlockBase.Info.of().forceSolidOn().strength(0.5F).sound(SoundEffectType.WOOL).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block REPEATER = register("repeater", new BlockRepeater(BlockBase.Info.of().instabreak().sound(SoundEffectType.STONE).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block WHITE_STAINED_GLASS = register("white_stained_glass", stainedGlass(EnumColor.WHITE));
    public static final Block ORANGE_STAINED_GLASS = register("orange_stained_glass", stainedGlass(EnumColor.ORANGE));
    public static final Block MAGENTA_STAINED_GLASS = register("magenta_stained_glass", stainedGlass(EnumColor.MAGENTA));
    public static final Block LIGHT_BLUE_STAINED_GLASS = register("light_blue_stained_glass", stainedGlass(EnumColor.LIGHT_BLUE));
    public static final Block YELLOW_STAINED_GLASS = register("yellow_stained_glass", stainedGlass(EnumColor.YELLOW));
    public static final Block LIME_STAINED_GLASS = register("lime_stained_glass", stainedGlass(EnumColor.LIME));
    public static final Block PINK_STAINED_GLASS = register("pink_stained_glass", stainedGlass(EnumColor.PINK));
    public static final Block GRAY_STAINED_GLASS = register("gray_stained_glass", stainedGlass(EnumColor.GRAY));
    public static final Block LIGHT_GRAY_STAINED_GLASS = register("light_gray_stained_glass", stainedGlass(EnumColor.LIGHT_GRAY));
    public static final Block CYAN_STAINED_GLASS = register("cyan_stained_glass", stainedGlass(EnumColor.CYAN));
    public static final Block PURPLE_STAINED_GLASS = register("purple_stained_glass", stainedGlass(EnumColor.PURPLE));
    public static final Block BLUE_STAINED_GLASS = register("blue_stained_glass", stainedGlass(EnumColor.BLUE));
    public static final Block BROWN_STAINED_GLASS = register("brown_stained_glass", stainedGlass(EnumColor.BROWN));
    public static final Block GREEN_STAINED_GLASS = register("green_stained_glass", stainedGlass(EnumColor.GREEN));
    public static final Block RED_STAINED_GLASS = register("red_stained_glass", stainedGlass(EnumColor.RED));
    public static final Block BLACK_STAINED_GLASS = register("black_stained_glass", stainedGlass(EnumColor.BLACK));
    public static final Block OAK_TRAPDOOR = register("oak_trapdoor", new BlockTrapdoor(BlockSetType.OAK, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava()));
    public static final Block SPRUCE_TRAPDOOR = register("spruce_trapdoor", new BlockTrapdoor(BlockSetType.SPRUCE, BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava()));
    public static final Block BIRCH_TRAPDOOR = register("birch_trapdoor", new BlockTrapdoor(BlockSetType.BIRCH, BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava()));
    public static final Block JUNGLE_TRAPDOOR = register("jungle_trapdoor", new BlockTrapdoor(BlockSetType.JUNGLE, BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava()));
    public static final Block ACACIA_TRAPDOOR = register("acacia_trapdoor", new BlockTrapdoor(BlockSetType.ACACIA, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava()));
    public static final Block CHERRY_TRAPDOOR = register("cherry_trapdoor", new BlockTrapdoor(BlockSetType.CHERRY, BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_WHITE).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava()));
    public static final Block DARK_OAK_TRAPDOOR = register("dark_oak_trapdoor", new BlockTrapdoor(BlockSetType.DARK_OAK, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava()));
    public static final Block MANGROVE_TRAPDOOR = register("mangrove_trapdoor", new BlockTrapdoor(BlockSetType.MANGROVE, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava()));
    public static final Block BAMBOO_TRAPDOOR = register("bamboo_trapdoor", new BlockTrapdoor(BlockSetType.BAMBOO, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never).ignitedByLava()));
    public static final Block STONE_BRICKS = register("stone_bricks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block MOSSY_STONE_BRICKS = register("mossy_stone_bricks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block CRACKED_STONE_BRICKS = register("cracked_stone_bricks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block CHISELED_STONE_BRICKS = register("chiseled_stone_bricks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block PACKED_MUD = register("packed_mud", new Block(BlockBase.Info.ofLegacyCopy(Blocks.DIRT).strength(1.0F, 3.0F).sound(SoundEffectType.PACKED_MUD)));
    public static final Block MUD_BRICKS = register("mud_bricks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 3.0F).sound(SoundEffectType.MUD_BRICKS)));
    public static final Block INFESTED_STONE = register("infested_stone", new BlockMonsterEggs(Blocks.STONE, BlockBase.Info.of().mapColor(MaterialMapColor.CLAY)));
    public static final Block INFESTED_COBBLESTONE = register("infested_cobblestone", new BlockMonsterEggs(Blocks.COBBLESTONE, BlockBase.Info.of().mapColor(MaterialMapColor.CLAY)));
    public static final Block INFESTED_STONE_BRICKS = register("infested_stone_bricks", new BlockMonsterEggs(Blocks.STONE_BRICKS, BlockBase.Info.of().mapColor(MaterialMapColor.CLAY)));
    public static final Block INFESTED_MOSSY_STONE_BRICKS = register("infested_mossy_stone_bricks", new BlockMonsterEggs(Blocks.MOSSY_STONE_BRICKS, BlockBase.Info.of().mapColor(MaterialMapColor.CLAY)));
    public static final Block INFESTED_CRACKED_STONE_BRICKS = register("infested_cracked_stone_bricks", new BlockMonsterEggs(Blocks.CRACKED_STONE_BRICKS, BlockBase.Info.of().mapColor(MaterialMapColor.CLAY)));
    public static final Block INFESTED_CHISELED_STONE_BRICKS = register("infested_chiseled_stone_bricks", new BlockMonsterEggs(Blocks.CHISELED_STONE_BRICKS, BlockBase.Info.of().mapColor(MaterialMapColor.CLAY)));
    public static final Block BROWN_MUSHROOM_BLOCK = register("brown_mushroom_block", new BlockHugeMushroom(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(0.2F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block RED_MUSHROOM_BLOCK = register("red_mushroom_block", new BlockHugeMushroom(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASS).strength(0.2F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block MUSHROOM_STEM = register("mushroom_stem", new BlockHugeMushroom(BlockBase.Info.of().mapColor(MaterialMapColor.WOOL).instrument(BlockPropertyInstrument.BASS).strength(0.2F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block IRON_BARS = register("iron_bars", new BlockIronBars(BlockBase.Info.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundEffectType.METAL).noOcclusion()));
    public static final Block CHAIN = register("chain", new BlockChain(BlockBase.Info.of().forceSolidOn().requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundEffectType.CHAIN).noOcclusion()));
    public static final Block GLASS_PANE = register("glass_pane", new BlockIronBars(BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block PUMPKIN = register(net.minecraft.references.Blocks.PUMPKIN, new BlockPumpkin(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.DIDGERIDOO).strength(1.0F).sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block MELON = register(net.minecraft.references.Blocks.MELON, new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GREEN).strength(1.0F).sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ATTACHED_PUMPKIN_STEM = register(net.minecraft.references.Blocks.ATTACHED_PUMPKIN_STEM, new BlockStemAttached(net.minecraft.references.Blocks.PUMPKIN_STEM, net.minecraft.references.Blocks.PUMPKIN, Items.PUMPKIN_SEEDS, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ATTACHED_MELON_STEM = register(net.minecraft.references.Blocks.ATTACHED_MELON_STEM, new BlockStemAttached(net.minecraft.references.Blocks.MELON_STEM, net.minecraft.references.Blocks.MELON, Items.MELON_SEEDS, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block PUMPKIN_STEM = register(net.minecraft.references.Blocks.PUMPKIN_STEM, new BlockStem(net.minecraft.references.Blocks.PUMPKIN, net.minecraft.references.Blocks.ATTACHED_PUMPKIN_STEM, Items.PUMPKIN_SEEDS, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.HARD_CROP).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block MELON_STEM = register(net.minecraft.references.Blocks.MELON_STEM, new BlockStem(net.minecraft.references.Blocks.MELON, net.minecraft.references.Blocks.ATTACHED_MELON_STEM, Items.MELON_SEEDS, BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.HARD_CROP).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block VINE = register("vine", new BlockVine(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).replaceable().noCollission().randomTicks().strength(0.2F).sound(SoundEffectType.VINE).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block GLOW_LICHEN = register("glow_lichen", new GlowLichenBlock(BlockBase.Info.of().mapColor(MaterialMapColor.GLOW_LICHEN).replaceable().noCollission().strength(0.2F).sound(SoundEffectType.GLOW_LICHEN).lightLevel(GlowLichenBlock.emission(7)).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block OAK_FENCE_GATE = register("oak_fence_gate", new BlockFenceGate(BlockPropertyWood.OAK, BlockBase.Info.of().mapColor(Blocks.OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava()));
    public static final Block BRICK_STAIRS = register("brick_stairs", legacyStair(Blocks.BRICKS));
    public static final Block STONE_BRICK_STAIRS = register("stone_brick_stairs", legacyStair(Blocks.STONE_BRICKS));
    public static final Block MUD_BRICK_STAIRS = register("mud_brick_stairs", legacyStair(Blocks.MUD_BRICKS));
    public static final Block MYCELIUM = register("mycelium", new BlockMycel(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).randomTicks().strength(0.6F).sound(SoundEffectType.GRASS)));
    public static final Block LILY_PAD = register("lily_pad", new BlockWaterLily(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).instabreak().sound(SoundEffectType.LILY_PAD).noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block NETHER_BRICKS = register("nether_bricks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.NETHER_BRICKS)));
    public static final Block NETHER_BRICK_FENCE = register("nether_brick_fence", new BlockFence(BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.NETHER_BRICKS)));
    public static final Block NETHER_BRICK_STAIRS = register("nether_brick_stairs", legacyStair(Blocks.NETHER_BRICKS));
    public static final Block NETHER_WART = register("nether_wart", new BlockNetherWart(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).noCollission().randomTicks().sound(SoundEffectType.NETHER_WART).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ENCHANTING_TABLE = register("enchanting_table", new BlockEnchantmentTable(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel((iblockdata) -> {
        return 7;
    }).strength(5.0F, 1200.0F)));
    public static final Block BREWING_STAND = register("brewing_stand", new BlockBrewingStand(BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(0.5F).lightLevel((iblockdata) -> {
        return 1;
    }).noOcclusion()));
    public static final Block CAULDRON = register("cauldron", new BlockCauldron(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).requiresCorrectToolForDrops().strength(2.0F).noOcclusion()));
    public static final Block WATER_CAULDRON = register("water_cauldron", new LayeredCauldronBlock(BiomeBase.Precipitation.RAIN, CauldronInteraction.WATER, BlockBase.Info.ofLegacyCopy(Blocks.CAULDRON)));
    public static final Block LAVA_CAULDRON = register("lava_cauldron", new LavaCauldronBlock(BlockBase.Info.ofLegacyCopy(Blocks.CAULDRON).lightLevel((iblockdata) -> {
        return 15;
    })));
    public static final Block POWDER_SNOW_CAULDRON = register("powder_snow_cauldron", new LayeredCauldronBlock(BiomeBase.Precipitation.SNOW, CauldronInteraction.POWDER_SNOW, BlockBase.Info.ofLegacyCopy(Blocks.CAULDRON)));
    public static final Block END_PORTAL = register("end_portal", new BlockEnderPortal(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).noCollission().lightLevel((iblockdata) -> {
        return 15;
    }).strength(-1.0F, 3600000.0F).noLootTable().pushReaction(EnumPistonReaction.BLOCK)));
    public static final Block END_PORTAL_FRAME = register("end_portal_frame", new BlockEnderPortalFrame(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).instrument(BlockPropertyInstrument.BASEDRUM).sound(SoundEffectType.GLASS).lightLevel((iblockdata) -> {
        return 1;
    }).strength(-1.0F, 3600000.0F).noLootTable()));
    public static final Block END_STONE = register("end_stone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F)));
    public static final Block DRAGON_EGG = register("dragon_egg", new BlockDragonEgg(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).strength(3.0F, 9.0F).lightLevel((iblockdata) -> {
        return 1;
    }).noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block REDSTONE_LAMP = register("redstone_lamp", new BlockRedstoneLamp(BlockBase.Info.of().lightLevel(litBlockEmission(15)).strength(0.3F).sound(SoundEffectType.GLASS).isValidSpawn(Blocks::always)));
    public static final Block COCOA = register("cocoa", new BlockCocoa(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).randomTicks().strength(0.2F, 3.0F).sound(SoundEffectType.WOOD).noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SANDSTONE_STAIRS = register("sandstone_stairs", legacyStair(Blocks.SANDSTONE));
    public static final Block EMERALD_ORE = register("emerald_ore", new DropExperienceBlock(UniformInt.of(3, 7), BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final Block DEEPSLATE_EMERALD_ORE = register("deepslate_emerald_ore", new DropExperienceBlock(UniformInt.of(3, 7), BlockBase.Info.ofLegacyCopy(Blocks.EMERALD_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE)));
    public static final Block ENDER_CHEST = register("ender_chest", new BlockEnderChest(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(22.5F, 600.0F).lightLevel((iblockdata) -> {
        return 7;
    })));
    public static final Block TRIPWIRE_HOOK = register("tripwire_hook", new BlockTripwireHook(BlockBase.Info.of().noCollission().sound(SoundEffectType.WOOD).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block TRIPWIRE = register("tripwire", new BlockTripwire(Blocks.TRIPWIRE_HOOK, BlockBase.Info.of().noCollission().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block EMERALD_BLOCK = register("emerald_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.EMERALD).instrument(BlockPropertyInstrument.BIT).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundEffectType.METAL)));
    public static final Block SPRUCE_STAIRS = register("spruce_stairs", legacyStair(Blocks.SPRUCE_PLANKS));
    public static final Block BIRCH_STAIRS = register("birch_stairs", legacyStair(Blocks.BIRCH_PLANKS));
    public static final Block JUNGLE_STAIRS = register("jungle_stairs", legacyStair(Blocks.JUNGLE_PLANKS));
    public static final Block COMMAND_BLOCK = register("command_block", new BlockCommand(false, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()));
    public static final Block BEACON = register("beacon", new BlockBeacon(BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).instrument(BlockPropertyInstrument.HAT).strength(3.0F).lightLevel((iblockdata) -> {
        return 15;
    }).noOcclusion().isRedstoneConductor(Blocks::never)));
    public static final Block COBBLESTONE_WALL = register("cobblestone_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.COBBLESTONE).forceSolidOn()));
    public static final Block MOSSY_COBBLESTONE_WALL = register("mossy_cobblestone_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.COBBLESTONE).forceSolidOn()));
    public static final Block FLOWER_POT = register("flower_pot", flowerPot(Blocks.AIR));
    public static final Block POTTED_TORCHFLOWER = register("potted_torchflower", flowerPot(Blocks.TORCHFLOWER));
    public static final Block POTTED_OAK_SAPLING = register("potted_oak_sapling", flowerPot(Blocks.OAK_SAPLING));
    public static final Block POTTED_SPRUCE_SAPLING = register("potted_spruce_sapling", flowerPot(Blocks.SPRUCE_SAPLING));
    public static final Block POTTED_BIRCH_SAPLING = register("potted_birch_sapling", flowerPot(Blocks.BIRCH_SAPLING));
    public static final Block POTTED_JUNGLE_SAPLING = register("potted_jungle_sapling", flowerPot(Blocks.JUNGLE_SAPLING));
    public static final Block POTTED_ACACIA_SAPLING = register("potted_acacia_sapling", flowerPot(Blocks.ACACIA_SAPLING));
    public static final Block POTTED_CHERRY_SAPLING = register("potted_cherry_sapling", flowerPot(Blocks.CHERRY_SAPLING));
    public static final Block POTTED_DARK_OAK_SAPLING = register("potted_dark_oak_sapling", flowerPot(Blocks.DARK_OAK_SAPLING));
    public static final Block POTTED_MANGROVE_PROPAGULE = register("potted_mangrove_propagule", flowerPot(Blocks.MANGROVE_PROPAGULE));
    public static final Block POTTED_FERN = register("potted_fern", flowerPot(Blocks.FERN));
    public static final Block POTTED_DANDELION = register("potted_dandelion", flowerPot(Blocks.DANDELION));
    public static final Block POTTED_POPPY = register("potted_poppy", flowerPot(Blocks.POPPY));
    public static final Block POTTED_BLUE_ORCHID = register("potted_blue_orchid", flowerPot(Blocks.BLUE_ORCHID));
    public static final Block POTTED_ALLIUM = register("potted_allium", flowerPot(Blocks.ALLIUM));
    public static final Block POTTED_AZURE_BLUET = register("potted_azure_bluet", flowerPot(Blocks.AZURE_BLUET));
    public static final Block POTTED_RED_TULIP = register("potted_red_tulip", flowerPot(Blocks.RED_TULIP));
    public static final Block POTTED_ORANGE_TULIP = register("potted_orange_tulip", flowerPot(Blocks.ORANGE_TULIP));
    public static final Block POTTED_WHITE_TULIP = register("potted_white_tulip", flowerPot(Blocks.WHITE_TULIP));
    public static final Block POTTED_PINK_TULIP = register("potted_pink_tulip", flowerPot(Blocks.PINK_TULIP));
    public static final Block POTTED_OXEYE_DAISY = register("potted_oxeye_daisy", flowerPot(Blocks.OXEYE_DAISY));
    public static final Block POTTED_CORNFLOWER = register("potted_cornflower", flowerPot(Blocks.CORNFLOWER));
    public static final Block POTTED_LILY_OF_THE_VALLEY = register("potted_lily_of_the_valley", flowerPot(Blocks.LILY_OF_THE_VALLEY));
    public static final Block POTTED_WITHER_ROSE = register("potted_wither_rose", flowerPot(Blocks.WITHER_ROSE));
    public static final Block POTTED_RED_MUSHROOM = register("potted_red_mushroom", flowerPot(Blocks.RED_MUSHROOM));
    public static final Block POTTED_BROWN_MUSHROOM = register("potted_brown_mushroom", flowerPot(Blocks.BROWN_MUSHROOM));
    public static final Block POTTED_DEAD_BUSH = register("potted_dead_bush", flowerPot(Blocks.DEAD_BUSH));
    public static final Block POTTED_CACTUS = register("potted_cactus", flowerPot(Blocks.CACTUS));
    public static final Block CARROTS = register("carrots", new BlockCarrots(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.CROP).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block POTATOES = register("potatoes", new BlockPotatoes(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.CROP).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block OAK_BUTTON = register("oak_button", woodenButton(BlockSetType.OAK));
    public static final Block SPRUCE_BUTTON = register("spruce_button", woodenButton(BlockSetType.SPRUCE));
    public static final Block BIRCH_BUTTON = register("birch_button", woodenButton(BlockSetType.BIRCH));
    public static final Block JUNGLE_BUTTON = register("jungle_button", woodenButton(BlockSetType.JUNGLE));
    public static final Block ACACIA_BUTTON = register("acacia_button", woodenButton(BlockSetType.ACACIA));
    public static final Block CHERRY_BUTTON = register("cherry_button", woodenButton(BlockSetType.CHERRY));
    public static final Block DARK_OAK_BUTTON = register("dark_oak_button", woodenButton(BlockSetType.DARK_OAK));
    public static final Block MANGROVE_BUTTON = register("mangrove_button", woodenButton(BlockSetType.MANGROVE));
    public static final Block BAMBOO_BUTTON = register("bamboo_button", woodenButton(BlockSetType.BAMBOO));
    public static final Block SKELETON_SKULL = register("skeleton_skull", new BlockSkull(BlockSkull.Type.SKELETON, BlockBase.Info.of().instrument(BlockPropertyInstrument.SKELETON).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SKELETON_WALL_SKULL = register("skeleton_wall_skull", new BlockSkullWall(BlockSkull.Type.SKELETON, BlockBase.Info.of().strength(1.0F).dropsLike(Blocks.SKELETON_SKULL).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block WITHER_SKELETON_SKULL = register("wither_skeleton_skull", new BlockWitherSkull(BlockBase.Info.of().instrument(BlockPropertyInstrument.WITHER_SKELETON).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block WITHER_SKELETON_WALL_SKULL = register("wither_skeleton_wall_skull", new BlockWitherSkullWall(BlockBase.Info.of().strength(1.0F).dropsLike(Blocks.WITHER_SKELETON_SKULL).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ZOMBIE_HEAD = register("zombie_head", new BlockSkull(BlockSkull.Type.ZOMBIE, BlockBase.Info.of().instrument(BlockPropertyInstrument.ZOMBIE).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ZOMBIE_WALL_HEAD = register("zombie_wall_head", new BlockSkullWall(BlockSkull.Type.ZOMBIE, BlockBase.Info.of().strength(1.0F).dropsLike(Blocks.ZOMBIE_HEAD).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block PLAYER_HEAD = register("player_head", new BlockSkullPlayer(BlockBase.Info.of().instrument(BlockPropertyInstrument.CUSTOM_HEAD).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block PLAYER_WALL_HEAD = register("player_wall_head", new BlockSkullPlayerWall(BlockBase.Info.of().strength(1.0F).dropsLike(Blocks.PLAYER_HEAD).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CREEPER_HEAD = register("creeper_head", new BlockSkull(BlockSkull.Type.CREEPER, BlockBase.Info.of().instrument(BlockPropertyInstrument.CREEPER).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CREEPER_WALL_HEAD = register("creeper_wall_head", new BlockSkullWall(BlockSkull.Type.CREEPER, BlockBase.Info.of().strength(1.0F).dropsLike(Blocks.CREEPER_HEAD).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block DRAGON_HEAD = register("dragon_head", new BlockSkull(BlockSkull.Type.DRAGON, BlockBase.Info.of().instrument(BlockPropertyInstrument.DRAGON).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block DRAGON_WALL_HEAD = register("dragon_wall_head", new BlockSkullWall(BlockSkull.Type.DRAGON, BlockBase.Info.of().strength(1.0F).dropsLike(Blocks.DRAGON_HEAD).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block PIGLIN_HEAD = register("piglin_head", new BlockSkull(BlockSkull.Type.PIGLIN, BlockBase.Info.of().instrument(BlockPropertyInstrument.PIGLIN).strength(1.0F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block PIGLIN_WALL_HEAD = register("piglin_wall_head", new PiglinWallSkullBlock(BlockBase.Info.of().strength(1.0F).dropsLike(Blocks.PIGLIN_HEAD).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ANVIL = register("anvil", new BlockAnvil(BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(5.0F, 1200.0F).sound(SoundEffectType.ANVIL).pushReaction(EnumPistonReaction.BLOCK)));
    public static final Block CHIPPED_ANVIL = register("chipped_anvil", new BlockAnvil(BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(5.0F, 1200.0F).sound(SoundEffectType.ANVIL).pushReaction(EnumPistonReaction.BLOCK)));
    public static final Block DAMAGED_ANVIL = register("damaged_anvil", new BlockAnvil(BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(5.0F, 1200.0F).sound(SoundEffectType.ANVIL).pushReaction(EnumPistonReaction.BLOCK)));
    public static final Block TRAPPED_CHEST = register("trapped_chest", new BlockChestTrapped(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block LIGHT_WEIGHTED_PRESSURE_PLATE = register("light_weighted_pressure_plate", new BlockPressurePlateWeighted(15, BlockSetType.GOLD, BlockBase.Info.of().mapColor(MaterialMapColor.GOLD).forceSolidOn().requiresCorrectToolForDrops().noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block HEAVY_WEIGHTED_PRESSURE_PLATE = register("heavy_weighted_pressure_plate", new BlockPressurePlateWeighted(150, BlockSetType.IRON, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).forceSolidOn().requiresCorrectToolForDrops().noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block COMPARATOR = register("comparator", new BlockRedstoneComparator(BlockBase.Info.of().instabreak().sound(SoundEffectType.STONE).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block DAYLIGHT_DETECTOR = register("daylight_detector", new BlockDaylightDetector(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(0.2F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block REDSTONE_BLOCK = register("redstone_block", new BlockPowered(BlockBase.Info.of().mapColor(MaterialMapColor.FIRE).requiresCorrectToolForDrops().strength(5.0F, 6.0F).sound(SoundEffectType.METAL).isRedstoneConductor(Blocks::never)));
    public static final Block NETHER_QUARTZ_ORE = register("nether_quartz_ore", new DropExperienceBlock(UniformInt.of(2, 5), BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundEffectType.NETHER_ORE)));
    public static final Block HOPPER = register("hopper", new BlockHopper(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).requiresCorrectToolForDrops().strength(3.0F, 4.8F).sound(SoundEffectType.METAL).noOcclusion()));
    public static final Block QUARTZ_BLOCK = register("quartz_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)));
    public static final Block CHISELED_QUARTZ_BLOCK = register("chiseled_quartz_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)));
    public static final Block QUARTZ_PILLAR = register("quartz_pillar", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)));
    public static final Block QUARTZ_STAIRS = register("quartz_stairs", legacyStair(Blocks.QUARTZ_BLOCK));
    public static final Block ACTIVATOR_RAIL = register("activator_rail", new BlockPoweredRail(BlockBase.Info.of().noCollission().strength(0.7F).sound(SoundEffectType.METAL)));
    public static final Block DROPPER = register("dropper", new BlockDropper(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F)));
    public static final Block WHITE_TERRACOTTA = register("white_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_WHITE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block ORANGE_TERRACOTTA = register("orange_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block MAGENTA_TERRACOTTA = register("magenta_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_MAGENTA).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block LIGHT_BLUE_TERRACOTTA = register("light_blue_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_LIGHT_BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block YELLOW_TERRACOTTA = register("yellow_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_YELLOW).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block LIME_TERRACOTTA = register("lime_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GREEN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block PINK_TERRACOTTA = register("pink_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_PINK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block GRAY_TERRACOTTA = register("gray_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block LIGHT_GRAY_TERRACOTTA = register("light_gray_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block CYAN_TERRACOTTA = register("cyan_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_CYAN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block PURPLE_TERRACOTTA = register("purple_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_PURPLE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block BLUE_TERRACOTTA = register("blue_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block BROWN_TERRACOTTA = register("brown_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_BROWN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block GREEN_TERRACOTTA = register("green_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_GREEN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block RED_TERRACOTTA = register("red_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block BLACK_TERRACOTTA = register("black_terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block WHITE_STAINED_GLASS_PANE = register("white_stained_glass_pane", new BlockStainedGlassPane(EnumColor.WHITE, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block ORANGE_STAINED_GLASS_PANE = register("orange_stained_glass_pane", new BlockStainedGlassPane(EnumColor.ORANGE, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block MAGENTA_STAINED_GLASS_PANE = register("magenta_stained_glass_pane", new BlockStainedGlassPane(EnumColor.MAGENTA, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block LIGHT_BLUE_STAINED_GLASS_PANE = register("light_blue_stained_glass_pane", new BlockStainedGlassPane(EnumColor.LIGHT_BLUE, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block YELLOW_STAINED_GLASS_PANE = register("yellow_stained_glass_pane", new BlockStainedGlassPane(EnumColor.YELLOW, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block LIME_STAINED_GLASS_PANE = register("lime_stained_glass_pane", new BlockStainedGlassPane(EnumColor.LIME, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block PINK_STAINED_GLASS_PANE = register("pink_stained_glass_pane", new BlockStainedGlassPane(EnumColor.PINK, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block GRAY_STAINED_GLASS_PANE = register("gray_stained_glass_pane", new BlockStainedGlassPane(EnumColor.GRAY, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block LIGHT_GRAY_STAINED_GLASS_PANE = register("light_gray_stained_glass_pane", new BlockStainedGlassPane(EnumColor.LIGHT_GRAY, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block CYAN_STAINED_GLASS_PANE = register("cyan_stained_glass_pane", new BlockStainedGlassPane(EnumColor.CYAN, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block PURPLE_STAINED_GLASS_PANE = register("purple_stained_glass_pane", new BlockStainedGlassPane(EnumColor.PURPLE, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block BLUE_STAINED_GLASS_PANE = register("blue_stained_glass_pane", new BlockStainedGlassPane(EnumColor.BLUE, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block BROWN_STAINED_GLASS_PANE = register("brown_stained_glass_pane", new BlockStainedGlassPane(EnumColor.BROWN, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block GREEN_STAINED_GLASS_PANE = register("green_stained_glass_pane", new BlockStainedGlassPane(EnumColor.GREEN, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block RED_STAINED_GLASS_PANE = register("red_stained_glass_pane", new BlockStainedGlassPane(EnumColor.RED, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block BLACK_STAINED_GLASS_PANE = register("black_stained_glass_pane", new BlockStainedGlassPane(EnumColor.BLACK, BlockBase.Info.of().instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion()));
    public static final Block ACACIA_STAIRS = register("acacia_stairs", legacyStair(Blocks.ACACIA_PLANKS));
    public static final Block CHERRY_STAIRS = register("cherry_stairs", legacyStair(Blocks.CHERRY_PLANKS));
    public static final Block DARK_OAK_STAIRS = register("dark_oak_stairs", legacyStair(Blocks.DARK_OAK_PLANKS));
    public static final Block MANGROVE_STAIRS = register("mangrove_stairs", legacyStair(Blocks.MANGROVE_PLANKS));
    public static final Block BAMBOO_STAIRS = register("bamboo_stairs", legacyStair(Blocks.BAMBOO_PLANKS));
    public static final Block BAMBOO_MOSAIC_STAIRS = register("bamboo_mosaic_stairs", legacyStair(Blocks.BAMBOO_MOSAIC));
    public static final Block SLIME_BLOCK = register("slime_block", new BlockSlime(BlockBase.Info.of().mapColor(MaterialMapColor.GRASS).friction(0.8F).sound(SoundEffectType.SLIME_BLOCK).noOcclusion()));
    public static final Block BARRIER = register("barrier", new BlockBarrier(BlockBase.Info.of().strength(-1.0F, 3600000.8F).mapColor(waterloggedMapColor(MaterialMapColor.NONE)).noLootTable().noOcclusion().isValidSpawn(Blocks::never).noTerrainParticles().pushReaction(EnumPistonReaction.BLOCK)));
    public static final Block LIGHT = register("light", new LightBlock(BlockBase.Info.of().replaceable().strength(-1.0F, 3600000.8F).mapColor(waterloggedMapColor(MaterialMapColor.NONE)).noLootTable().noOcclusion().lightLevel(LightBlock.LIGHT_EMISSION)));
    public static final Block IRON_TRAPDOOR = register("iron_trapdoor", new BlockTrapdoor(BlockSetType.IRON, BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(5.0F).noOcclusion().isValidSpawn(Blocks::never)));
    public static final Block PRISMARINE = register("prismarine", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block PRISMARINE_BRICKS = register("prismarine_bricks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block DARK_PRISMARINE = register("dark_prismarine", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block PRISMARINE_STAIRS = register("prismarine_stairs", legacyStair(Blocks.PRISMARINE));
    public static final Block PRISMARINE_BRICK_STAIRS = register("prismarine_brick_stairs", legacyStair(Blocks.PRISMARINE_BRICKS));
    public static final Block DARK_PRISMARINE_STAIRS = register("dark_prismarine_stairs", legacyStair(Blocks.DARK_PRISMARINE));
    public static final Block PRISMARINE_SLAB = register("prismarine_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block PRISMARINE_BRICK_SLAB = register("prismarine_brick_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block DARK_PRISMARINE_SLAB = register("dark_prismarine_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block SEA_LANTERN = register("sea_lantern", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).lightLevel((iblockdata) -> {
        return 15;
    }).isRedstoneConductor(Blocks::never)));
    public static final Block HAY_BLOCK = register("hay_block", new BlockHay(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BANJO).strength(0.5F).sound(SoundEffectType.GRASS)));
    public static final Block WHITE_CARPET = register("white_carpet", new BlockCarpet(EnumColor.WHITE, BlockBase.Info.of().mapColor(MaterialMapColor.SNOW).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block ORANGE_CARPET = register("orange_carpet", new BlockCarpet(EnumColor.ORANGE, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block MAGENTA_CARPET = register("magenta_carpet", new BlockCarpet(EnumColor.MAGENTA, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_MAGENTA).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block LIGHT_BLUE_CARPET = register("light_blue_carpet", new BlockCarpet(EnumColor.LIGHT_BLUE, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_BLUE).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block YELLOW_CARPET = register("yellow_carpet", new BlockCarpet(EnumColor.YELLOW, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block LIME_CARPET = register("lime_carpet", new BlockCarpet(EnumColor.LIME, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GREEN).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block PINK_CARPET = register("pink_carpet", new BlockCarpet(EnumColor.PINK, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block GRAY_CARPET = register("gray_carpet", new BlockCarpet(EnumColor.GRAY, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block LIGHT_GRAY_CARPET = register("light_gray_carpet", new BlockCarpet(EnumColor.LIGHT_GRAY, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GRAY).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block CYAN_CARPET = register("cyan_carpet", new BlockCarpet(EnumColor.CYAN, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block PURPLE_CARPET = register("purple_carpet", new BlockCarpet(EnumColor.PURPLE, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block BLUE_CARPET = register("blue_carpet", new BlockCarpet(EnumColor.BLUE, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLUE).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block BROWN_CARPET = register("brown_carpet", new BlockCarpet(EnumColor.BROWN, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block GREEN_CARPET = register("green_carpet", new BlockCarpet(EnumColor.GREEN, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block RED_CARPET = register("red_carpet", new BlockCarpet(EnumColor.RED, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block BLACK_CARPET = register("black_carpet", new BlockCarpet(EnumColor.BLACK, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).strength(0.1F).sound(SoundEffectType.WOOL).ignitedByLava()));
    public static final Block TERRACOTTA = register("terracotta", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.25F, 4.2F)));
    public static final Block COAL_BLOCK = register("coal_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F)));
    public static final Block PACKED_ICE = register("packed_ice", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.ICE).instrument(BlockPropertyInstrument.CHIME).friction(0.98F).strength(0.5F).sound(SoundEffectType.GLASS)));
    public static final Block SUNFLOWER = register("sunflower", new BlockTallPlantFlower(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block LILAC = register("lilac", new BlockTallPlantFlower(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ROSE_BUSH = register("rose_bush", new BlockTallPlantFlower(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block PEONY = register("peony", new BlockTallPlantFlower(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block TALL_GRASS = register("tall_grass", new BlockTallPlant(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block LARGE_FERN = register("large_fern", new BlockTallPlant(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).replaceable().noCollission().instabreak().sound(SoundEffectType.GRASS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block WHITE_BANNER = register("white_banner", new BlockBanner(EnumColor.WHITE, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block ORANGE_BANNER = register("orange_banner", new BlockBanner(EnumColor.ORANGE, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block MAGENTA_BANNER = register("magenta_banner", new BlockBanner(EnumColor.MAGENTA, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block LIGHT_BLUE_BANNER = register("light_blue_banner", new BlockBanner(EnumColor.LIGHT_BLUE, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block YELLOW_BANNER = register("yellow_banner", new BlockBanner(EnumColor.YELLOW, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block LIME_BANNER = register("lime_banner", new BlockBanner(EnumColor.LIME, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block PINK_BANNER = register("pink_banner", new BlockBanner(EnumColor.PINK, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block GRAY_BANNER = register("gray_banner", new BlockBanner(EnumColor.GRAY, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block LIGHT_GRAY_BANNER = register("light_gray_banner", new BlockBanner(EnumColor.LIGHT_GRAY, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block CYAN_BANNER = register("cyan_banner", new BlockBanner(EnumColor.CYAN, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block PURPLE_BANNER = register("purple_banner", new BlockBanner(EnumColor.PURPLE, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block BLUE_BANNER = register("blue_banner", new BlockBanner(EnumColor.BLUE, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block BROWN_BANNER = register("brown_banner", new BlockBanner(EnumColor.BROWN, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block GREEN_BANNER = register("green_banner", new BlockBanner(EnumColor.GREEN, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block RED_BANNER = register("red_banner", new BlockBanner(EnumColor.RED, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block BLACK_BANNER = register("black_banner", new BlockBanner(EnumColor.BLACK, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block WHITE_WALL_BANNER = register("white_wall_banner", new BlockBannerWall(EnumColor.WHITE, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.WHITE_BANNER).ignitedByLava()));
    public static final Block ORANGE_WALL_BANNER = register("orange_wall_banner", new BlockBannerWall(EnumColor.ORANGE, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.ORANGE_BANNER).ignitedByLava()));
    public static final Block MAGENTA_WALL_BANNER = register("magenta_wall_banner", new BlockBannerWall(EnumColor.MAGENTA, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.MAGENTA_BANNER).ignitedByLava()));
    public static final Block LIGHT_BLUE_WALL_BANNER = register("light_blue_wall_banner", new BlockBannerWall(EnumColor.LIGHT_BLUE, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.LIGHT_BLUE_BANNER).ignitedByLava()));
    public static final Block YELLOW_WALL_BANNER = register("yellow_wall_banner", new BlockBannerWall(EnumColor.YELLOW, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.YELLOW_BANNER).ignitedByLava()));
    public static final Block LIME_WALL_BANNER = register("lime_wall_banner", new BlockBannerWall(EnumColor.LIME, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.LIME_BANNER).ignitedByLava()));
    public static final Block PINK_WALL_BANNER = register("pink_wall_banner", new BlockBannerWall(EnumColor.PINK, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.PINK_BANNER).ignitedByLava()));
    public static final Block GRAY_WALL_BANNER = register("gray_wall_banner", new BlockBannerWall(EnumColor.GRAY, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.GRAY_BANNER).ignitedByLava()));
    public static final Block LIGHT_GRAY_WALL_BANNER = register("light_gray_wall_banner", new BlockBannerWall(EnumColor.LIGHT_GRAY, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.LIGHT_GRAY_BANNER).ignitedByLava()));
    public static final Block CYAN_WALL_BANNER = register("cyan_wall_banner", new BlockBannerWall(EnumColor.CYAN, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.CYAN_BANNER).ignitedByLava()));
    public static final Block PURPLE_WALL_BANNER = register("purple_wall_banner", new BlockBannerWall(EnumColor.PURPLE, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.PURPLE_BANNER).ignitedByLava()));
    public static final Block BLUE_WALL_BANNER = register("blue_wall_banner", new BlockBannerWall(EnumColor.BLUE, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.BLUE_BANNER).ignitedByLava()));
    public static final Block BROWN_WALL_BANNER = register("brown_wall_banner", new BlockBannerWall(EnumColor.BROWN, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.BROWN_BANNER).ignitedByLava()));
    public static final Block GREEN_WALL_BANNER = register("green_wall_banner", new BlockBannerWall(EnumColor.GREEN, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.GREEN_BANNER).ignitedByLava()));
    public static final Block RED_WALL_BANNER = register("red_wall_banner", new BlockBannerWall(EnumColor.RED, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.RED_BANNER).ignitedByLava()));
    public static final Block BLACK_WALL_BANNER = register("black_wall_banner", new BlockBannerWall(EnumColor.BLACK, BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(1.0F).sound(SoundEffectType.WOOD).dropsLike(Blocks.BLACK_BANNER).ignitedByLava()));
    public static final Block RED_SANDSTONE = register("red_sandstone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)));
    public static final Block CHISELED_RED_SANDSTONE = register("chiseled_red_sandstone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)));
    public static final Block CUT_RED_SANDSTONE = register("cut_red_sandstone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.8F)));
    public static final Block RED_SANDSTONE_STAIRS = register("red_sandstone_stairs", legacyStair(Blocks.RED_SANDSTONE));
    public static final Block OAK_SLAB = register("oak_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block SPRUCE_SLAB = register("spruce_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block BIRCH_SLAB = register("birch_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block JUNGLE_SLAB = register("jungle_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block ACACIA_SLAB = register("acacia_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block CHERRY_SLAB = register("cherry_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_WHITE).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.CHERRY_WOOD).ignitedByLava()));
    public static final Block DARK_OAK_SLAB = register("dark_oak_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BROWN).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block MANGROVE_SLAB = register("mangrove_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block BAMBOO_SLAB = register("bamboo_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.BAMBOO_WOOD).ignitedByLava()));
    public static final Block BAMBOO_MOSAIC_SLAB = register("bamboo_mosaic_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.BAMBOO_WOOD).ignitedByLava()));
    public static final Block STONE_SLAB = register("stone_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block SMOOTH_STONE_SLAB = register("smooth_stone_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block SANDSTONE_SLAB = register("sandstone_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block CUT_SANDSTONE_SLAB = register("cut_sandstone_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block PETRIFIED_OAK_SLAB = register("petrified_oak_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block COBBLESTONE_SLAB = register("cobblestone_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block BRICK_SLAB = register("brick_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block STONE_BRICK_SLAB = register("stone_brick_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block MUD_BRICK_SLAB = register("mud_brick_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 3.0F).sound(SoundEffectType.MUD_BRICKS)));
    public static final Block NETHER_BRICK_SLAB = register("nether_brick_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.NETHER_BRICKS)));
    public static final Block QUARTZ_SLAB = register("quartz_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block RED_SANDSTONE_SLAB = register("red_sandstone_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block CUT_RED_SANDSTONE_SLAB = register("cut_red_sandstone_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block PURPUR_SLAB = register("purpur_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_MAGENTA).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block SMOOTH_STONE = register("smooth_stone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block SMOOTH_SANDSTONE = register("smooth_sandstone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block SMOOTH_QUARTZ = register("smooth_quartz", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block SMOOTH_RED_SANDSTONE = register("smooth_red_sandstone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F)));
    public static final Block SPRUCE_FENCE_GATE = register("spruce_fence_gate", new BlockFenceGate(BlockPropertyWood.SPRUCE, BlockBase.Info.of().mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava()));
    public static final Block BIRCH_FENCE_GATE = register("birch_fence_gate", new BlockFenceGate(BlockPropertyWood.BIRCH, BlockBase.Info.of().mapColor(Blocks.BIRCH_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava()));
    public static final Block JUNGLE_FENCE_GATE = register("jungle_fence_gate", new BlockFenceGate(BlockPropertyWood.JUNGLE, BlockBase.Info.of().mapColor(Blocks.JUNGLE_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava()));
    public static final Block ACACIA_FENCE_GATE = register("acacia_fence_gate", new BlockFenceGate(BlockPropertyWood.ACACIA, BlockBase.Info.of().mapColor(Blocks.ACACIA_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava()));
    public static final Block CHERRY_FENCE_GATE = register("cherry_fence_gate", new BlockFenceGate(BlockPropertyWood.CHERRY, BlockBase.Info.of().mapColor(Blocks.CHERRY_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava()));
    public static final Block DARK_OAK_FENCE_GATE = register("dark_oak_fence_gate", new BlockFenceGate(BlockPropertyWood.DARK_OAK, BlockBase.Info.of().mapColor(Blocks.DARK_OAK_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava()));
    public static final Block MANGROVE_FENCE_GATE = register("mangrove_fence_gate", new BlockFenceGate(BlockPropertyWood.MANGROVE, BlockBase.Info.of().mapColor(Blocks.MANGROVE_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava()));
    public static final Block BAMBOO_FENCE_GATE = register("bamboo_fence_gate", new BlockFenceGate(BlockPropertyWood.BAMBOO, BlockBase.Info.of().mapColor(Blocks.BAMBOO_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava()));
    public static final Block SPRUCE_FENCE = register("spruce_fence", new BlockFence(BlockBase.Info.of().mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.WOOD)));
    public static final Block BIRCH_FENCE = register("birch_fence", new BlockFence(BlockBase.Info.of().mapColor(Blocks.BIRCH_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.WOOD)));
    public static final Block JUNGLE_FENCE = register("jungle_fence", new BlockFence(BlockBase.Info.of().mapColor(Blocks.JUNGLE_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.WOOD)));
    public static final Block ACACIA_FENCE = register("acacia_fence", new BlockFence(BlockBase.Info.of().mapColor(Blocks.ACACIA_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.WOOD)));
    public static final Block CHERRY_FENCE = register("cherry_fence", new BlockFence(BlockBase.Info.of().mapColor(Blocks.CHERRY_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.CHERRY_WOOD)));
    public static final Block DARK_OAK_FENCE = register("dark_oak_fence", new BlockFence(BlockBase.Info.of().mapColor(Blocks.DARK_OAK_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.WOOD)));
    public static final Block MANGROVE_FENCE = register("mangrove_fence", new BlockFence(BlockBase.Info.of().mapColor(Blocks.MANGROVE_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).ignitedByLava().sound(SoundEffectType.WOOD)));
    public static final Block BAMBOO_FENCE = register("bamboo_fence", new BlockFence(BlockBase.Info.of().mapColor(Blocks.BAMBOO_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.BAMBOO_WOOD).ignitedByLava()));
    public static final Block SPRUCE_DOOR = register("spruce_door", new BlockDoor(BlockSetType.SPRUCE, BlockBase.Info.of().mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BIRCH_DOOR = register("birch_door", new BlockDoor(BlockSetType.BIRCH, BlockBase.Info.of().mapColor(Blocks.BIRCH_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block JUNGLE_DOOR = register("jungle_door", new BlockDoor(BlockSetType.JUNGLE, BlockBase.Info.of().mapColor(Blocks.JUNGLE_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ACACIA_DOOR = register("acacia_door", new BlockDoor(BlockSetType.ACACIA, BlockBase.Info.of().mapColor(Blocks.ACACIA_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CHERRY_DOOR = register("cherry_door", new BlockDoor(BlockSetType.CHERRY, BlockBase.Info.of().mapColor(Blocks.CHERRY_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block DARK_OAK_DOOR = register("dark_oak_door", new BlockDoor(BlockSetType.DARK_OAK, BlockBase.Info.of().mapColor(Blocks.DARK_OAK_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block MANGROVE_DOOR = register("mangrove_door", new BlockDoor(BlockSetType.MANGROVE, BlockBase.Info.of().mapColor(Blocks.MANGROVE_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BAMBOO_DOOR = register("bamboo_door", new BlockDoor(BlockSetType.BAMBOO, BlockBase.Info.of().mapColor(Blocks.BAMBOO_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block END_ROD = register("end_rod", new BlockEndRod(BlockBase.Info.of().forceSolidOff().instabreak().lightLevel((iblockdata) -> {
        return 14;
    }).sound(SoundEffectType.WOOD).noOcclusion()));
    public static final Block CHORUS_PLANT = register("chorus_plant", new BlockChorusFruit(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).forceSolidOff().strength(0.4F).sound(SoundEffectType.WOOD).noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CHORUS_FLOWER = register("chorus_flower", new BlockChorusFlower(Blocks.CHORUS_PLANT, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).forceSolidOff().randomTicks().strength(0.4F).sound(SoundEffectType.WOOD).noOcclusion().isValidSpawn(Blocks::never).pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never)));
    public static final Block PURPUR_BLOCK = register("purpur_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_MAGENTA).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block PURPUR_PILLAR = register("purpur_pillar", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_MAGENTA).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block PURPUR_STAIRS = register("purpur_stairs", legacyStair(Blocks.PURPUR_BLOCK));
    public static final Block END_STONE_BRICKS = register("end_stone_bricks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 9.0F)));
    public static final Block TORCHFLOWER_CROP = register("torchflower_crop", new TorchflowerCropBlock(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.CROP).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block PITCHER_CROP = register("pitcher_crop", new PitcherCropBlock(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.CROP).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block PITCHER_PLANT = register("pitcher_plant", new BlockTallPlant(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.CROP).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BEETROOTS = register("beetroots", new BlockBeetroot(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundEffectType.CROP).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block DIRT_PATH = register("dirt_path", new BlockGrassPath(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).strength(0.65F).sound(SoundEffectType.GRASS).isViewBlocking(Blocks::always).isSuffocating(Blocks::always)));
    public static final Block END_GATEWAY = register("end_gateway", new BlockEndGateway(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).noCollission().lightLevel((iblockdata) -> {
        return 15;
    }).strength(-1.0F, 3600000.0F).noLootTable().pushReaction(EnumPistonReaction.BLOCK)));
    public static final Block REPEATING_COMMAND_BLOCK = register("repeating_command_block", new BlockCommand(false, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()));
    public static final Block CHAIN_COMMAND_BLOCK = register("chain_command_block", new BlockCommand(true, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()));
    public static final Block FROSTED_ICE = register("frosted_ice", new BlockIceFrost(BlockBase.Info.of().mapColor(MaterialMapColor.ICE).friction(0.98F).randomTicks().strength(0.5F).sound(SoundEffectType.GLASS).noOcclusion().isValidSpawn((iblockdata, iblockaccess, blockposition, entitytypes) -> {
        return entitytypes == EntityTypes.POLAR_BEAR;
    }).isRedstoneConductor(Blocks::never)));
    public static final Block MAGMA_BLOCK = register("magma_block", new BlockMagma(BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel((iblockdata) -> {
        return 3;
    }).strength(0.5F).isValidSpawn((iblockdata, iblockaccess, blockposition, entitytypes) -> {
        return entitytypes.fireImmune();
    }).hasPostProcess(Blocks::always).emissiveRendering(Blocks::always)));
    public static final Block NETHER_WART_BLOCK = register("nether_wart_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).strength(1.0F).sound(SoundEffectType.WART_BLOCK)));
    public static final Block RED_NETHER_BRICKS = register("red_nether_bricks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.NETHER_BRICKS)));
    public static final Block BONE_BLOCK = register("bone_block", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).instrument(BlockPropertyInstrument.XYLOPHONE).requiresCorrectToolForDrops().strength(2.0F).sound(SoundEffectType.BONE_BLOCK)));
    public static final Block STRUCTURE_VOID = register("structure_void", new BlockStructureVoid(BlockBase.Info.of().replaceable().noCollission().noLootTable().noTerrainParticles().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block OBSERVER = register("observer", new BlockObserver(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).strength(3.0F).requiresCorrectToolForDrops().isRedstoneConductor(Blocks::never)));
    public static final Block SHULKER_BOX = register("shulker_box", shulkerBox((EnumColor) null, MaterialMapColor.COLOR_PURPLE));
    public static final Block WHITE_SHULKER_BOX = register("white_shulker_box", shulkerBox(EnumColor.WHITE, MaterialMapColor.SNOW));
    public static final Block ORANGE_SHULKER_BOX = register("orange_shulker_box", shulkerBox(EnumColor.ORANGE, MaterialMapColor.COLOR_ORANGE));
    public static final Block MAGENTA_SHULKER_BOX = register("magenta_shulker_box", shulkerBox(EnumColor.MAGENTA, MaterialMapColor.COLOR_MAGENTA));
    public static final Block LIGHT_BLUE_SHULKER_BOX = register("light_blue_shulker_box", shulkerBox(EnumColor.LIGHT_BLUE, MaterialMapColor.COLOR_LIGHT_BLUE));
    public static final Block YELLOW_SHULKER_BOX = register("yellow_shulker_box", shulkerBox(EnumColor.YELLOW, MaterialMapColor.COLOR_YELLOW));
    public static final Block LIME_SHULKER_BOX = register("lime_shulker_box", shulkerBox(EnumColor.LIME, MaterialMapColor.COLOR_LIGHT_GREEN));
    public static final Block PINK_SHULKER_BOX = register("pink_shulker_box", shulkerBox(EnumColor.PINK, MaterialMapColor.COLOR_PINK));
    public static final Block GRAY_SHULKER_BOX = register("gray_shulker_box", shulkerBox(EnumColor.GRAY, MaterialMapColor.COLOR_GRAY));
    public static final Block LIGHT_GRAY_SHULKER_BOX = register("light_gray_shulker_box", shulkerBox(EnumColor.LIGHT_GRAY, MaterialMapColor.COLOR_LIGHT_GRAY));
    public static final Block CYAN_SHULKER_BOX = register("cyan_shulker_box", shulkerBox(EnumColor.CYAN, MaterialMapColor.COLOR_CYAN));
    public static final Block PURPLE_SHULKER_BOX = register("purple_shulker_box", shulkerBox(EnumColor.PURPLE, MaterialMapColor.TERRACOTTA_PURPLE));
    public static final Block BLUE_SHULKER_BOX = register("blue_shulker_box", shulkerBox(EnumColor.BLUE, MaterialMapColor.COLOR_BLUE));
    public static final Block BROWN_SHULKER_BOX = register("brown_shulker_box", shulkerBox(EnumColor.BROWN, MaterialMapColor.COLOR_BROWN));
    public static final Block GREEN_SHULKER_BOX = register("green_shulker_box", shulkerBox(EnumColor.GREEN, MaterialMapColor.COLOR_GREEN));
    public static final Block RED_SHULKER_BOX = register("red_shulker_box", shulkerBox(EnumColor.RED, MaterialMapColor.COLOR_RED));
    public static final Block BLACK_SHULKER_BOX = register("black_shulker_box", shulkerBox(EnumColor.BLACK, MaterialMapColor.COLOR_BLACK));
    public static final Block WHITE_GLAZED_TERRACOTTA = register("white_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.WHITE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block ORANGE_GLAZED_TERRACOTTA = register("orange_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block MAGENTA_GLAZED_TERRACOTTA = register("magenta_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.MAGENTA).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block LIGHT_BLUE_GLAZED_TERRACOTTA = register("light_blue_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.LIGHT_BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block YELLOW_GLAZED_TERRACOTTA = register("yellow_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.YELLOW).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block LIME_GLAZED_TERRACOTTA = register("lime_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.LIME).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block PINK_GLAZED_TERRACOTTA = register("pink_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.PINK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block GRAY_GLAZED_TERRACOTTA = register("gray_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block LIGHT_GRAY_GLAZED_TERRACOTTA = register("light_gray_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.LIGHT_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block CYAN_GLAZED_TERRACOTTA = register("cyan_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.CYAN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block PURPLE_GLAZED_TERRACOTTA = register("purple_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.PURPLE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block BLUE_GLAZED_TERRACOTTA = register("blue_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block BROWN_GLAZED_TERRACOTTA = register("brown_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.BROWN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block GREEN_GLAZED_TERRACOTTA = register("green_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.GREEN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block RED_GLAZED_TERRACOTTA = register("red_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block BLACK_GLAZED_TERRACOTTA = register("black_glazed_terracotta", new BlockGlazedTerracotta(BlockBase.Info.of().mapColor(EnumColor.BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.4F).pushReaction(EnumPistonReaction.PUSH_ONLY)));
    public static final Block WHITE_CONCRETE = register("white_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.WHITE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block ORANGE_CONCRETE = register("orange_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block MAGENTA_CONCRETE = register("magenta_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.MAGENTA).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block LIGHT_BLUE_CONCRETE = register("light_blue_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.LIGHT_BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block YELLOW_CONCRETE = register("yellow_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.YELLOW).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block LIME_CONCRETE = register("lime_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.LIME).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block PINK_CONCRETE = register("pink_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.PINK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block GRAY_CONCRETE = register("gray_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block LIGHT_GRAY_CONCRETE = register("light_gray_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.LIGHT_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block CYAN_CONCRETE = register("cyan_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.CYAN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block PURPLE_CONCRETE = register("purple_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.PURPLE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block BLUE_CONCRETE = register("blue_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block BROWN_CONCRETE = register("brown_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.BROWN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block GREEN_CONCRETE = register("green_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.GREEN).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block RED_CONCRETE = register("red_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block BLACK_CONCRETE = register("black_concrete", new Block(BlockBase.Info.of().mapColor(EnumColor.BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8F)));
    public static final Block WHITE_CONCRETE_POWDER = register("white_concrete_powder", new BlockConcretePowder(Blocks.WHITE_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.WHITE).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block ORANGE_CONCRETE_POWDER = register("orange_concrete_powder", new BlockConcretePowder(Blocks.ORANGE_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.ORANGE).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block MAGENTA_CONCRETE_POWDER = register("magenta_concrete_powder", new BlockConcretePowder(Blocks.MAGENTA_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.MAGENTA).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block LIGHT_BLUE_CONCRETE_POWDER = register("light_blue_concrete_powder", new BlockConcretePowder(Blocks.LIGHT_BLUE_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.LIGHT_BLUE).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block YELLOW_CONCRETE_POWDER = register("yellow_concrete_powder", new BlockConcretePowder(Blocks.YELLOW_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.YELLOW).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block LIME_CONCRETE_POWDER = register("lime_concrete_powder", new BlockConcretePowder(Blocks.LIME_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.LIME).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block PINK_CONCRETE_POWDER = register("pink_concrete_powder", new BlockConcretePowder(Blocks.PINK_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.PINK).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block GRAY_CONCRETE_POWDER = register("gray_concrete_powder", new BlockConcretePowder(Blocks.GRAY_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.GRAY).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block LIGHT_GRAY_CONCRETE_POWDER = register("light_gray_concrete_powder", new BlockConcretePowder(Blocks.LIGHT_GRAY_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.LIGHT_GRAY).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block CYAN_CONCRETE_POWDER = register("cyan_concrete_powder", new BlockConcretePowder(Blocks.CYAN_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.CYAN).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block PURPLE_CONCRETE_POWDER = register("purple_concrete_powder", new BlockConcretePowder(Blocks.PURPLE_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.PURPLE).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block BLUE_CONCRETE_POWDER = register("blue_concrete_powder", new BlockConcretePowder(Blocks.BLUE_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.BLUE).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block BROWN_CONCRETE_POWDER = register("brown_concrete_powder", new BlockConcretePowder(Blocks.BROWN_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.BROWN).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block GREEN_CONCRETE_POWDER = register("green_concrete_powder", new BlockConcretePowder(Blocks.GREEN_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.GREEN).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block RED_CONCRETE_POWDER = register("red_concrete_powder", new BlockConcretePowder(Blocks.RED_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.RED).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block BLACK_CONCRETE_POWDER = register("black_concrete_powder", new BlockConcretePowder(Blocks.BLACK_CONCRETE, BlockBase.Info.of().mapColor(EnumColor.BLACK).instrument(BlockPropertyInstrument.SNARE).strength(0.5F).sound(SoundEffectType.SAND)));
    public static final Block KELP = register("kelp", new BlockKelp(BlockBase.Info.of().mapColor(MaterialMapColor.WATER).noCollission().randomTicks().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block KELP_PLANT = register("kelp_plant", new BlockKelpPlant(BlockBase.Info.of().mapColor(MaterialMapColor.WATER).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block DRIED_KELP_BLOCK = register("dried_kelp_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).strength(0.5F, 2.5F).sound(SoundEffectType.GRASS)));
    public static final Block TURTLE_EGG = register("turtle_egg", new BlockTurtleEgg(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).forceSolidOn().strength(0.5F).sound(SoundEffectType.METAL).randomTicks().noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SNIFFER_EGG = register("sniffer_egg", new SnifferEggBlock(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).strength(0.5F).sound(SoundEffectType.METAL).noOcclusion()));
    public static final Block DEAD_TUBE_CORAL_BLOCK = register("dead_tube_coral_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block DEAD_BRAIN_CORAL_BLOCK = register("dead_brain_coral_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block DEAD_BUBBLE_CORAL_BLOCK = register("dead_bubble_coral_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block DEAD_FIRE_CORAL_BLOCK = register("dead_fire_coral_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block DEAD_HORN_CORAL_BLOCK = register("dead_horn_coral_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block TUBE_CORAL_BLOCK = register("tube_coral_block", new BlockCoral(Blocks.DEAD_TUBE_CORAL_BLOCK, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLUE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundEffectType.CORAL_BLOCK)));
    public static final Block BRAIN_CORAL_BLOCK = register("brain_coral_block", new BlockCoral(Blocks.DEAD_BRAIN_CORAL_BLOCK, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundEffectType.CORAL_BLOCK)));
    public static final Block BUBBLE_CORAL_BLOCK = register("bubble_coral_block", new BlockCoral(Blocks.DEAD_BUBBLE_CORAL_BLOCK, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundEffectType.CORAL_BLOCK)));
    public static final Block FIRE_CORAL_BLOCK = register("fire_coral_block", new BlockCoral(Blocks.DEAD_FIRE_CORAL_BLOCK, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundEffectType.CORAL_BLOCK)));
    public static final Block HORN_CORAL_BLOCK = register("horn_coral_block", new BlockCoral(Blocks.DEAD_HORN_CORAL_BLOCK, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundEffectType.CORAL_BLOCK)));
    public static final Block DEAD_TUBE_CORAL = register("dead_tube_coral", new BlockCoralDead(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak()));
    public static final Block DEAD_BRAIN_CORAL = register("dead_brain_coral", new BlockCoralDead(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak()));
    public static final Block DEAD_BUBBLE_CORAL = register("dead_bubble_coral", new BlockCoralDead(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak()));
    public static final Block DEAD_FIRE_CORAL = register("dead_fire_coral", new BlockCoralDead(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak()));
    public static final Block DEAD_HORN_CORAL = register("dead_horn_coral", new BlockCoralDead(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak()));
    public static final Block TUBE_CORAL = register("tube_coral", new BlockCoralPlant(Blocks.DEAD_TUBE_CORAL, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLUE).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BRAIN_CORAL = register("brain_coral", new BlockCoralPlant(Blocks.DEAD_BRAIN_CORAL, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BUBBLE_CORAL = register("bubble_coral", new BlockCoralPlant(Blocks.DEAD_BUBBLE_CORAL, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block FIRE_CORAL = register("fire_coral", new BlockCoralPlant(Blocks.DEAD_FIRE_CORAL, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block HORN_CORAL = register("horn_coral", new BlockCoralPlant(Blocks.DEAD_HORN_CORAL, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block DEAD_TUBE_CORAL_FAN = register("dead_tube_coral_fan", new BlockCoralFanAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak()));
    public static final Block DEAD_BRAIN_CORAL_FAN = register("dead_brain_coral_fan", new BlockCoralFanAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak()));
    public static final Block DEAD_BUBBLE_CORAL_FAN = register("dead_bubble_coral_fan", new BlockCoralFanAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak()));
    public static final Block DEAD_FIRE_CORAL_FAN = register("dead_fire_coral_fan", new BlockCoralFanAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak()));
    public static final Block DEAD_HORN_CORAL_FAN = register("dead_horn_coral_fan", new BlockCoralFanAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak()));
    public static final Block TUBE_CORAL_FAN = register("tube_coral_fan", new BlockCoralFan(Blocks.DEAD_TUBE_CORAL_FAN, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLUE).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BRAIN_CORAL_FAN = register("brain_coral_fan", new BlockCoralFan(Blocks.DEAD_BRAIN_CORAL_FAN, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BUBBLE_CORAL_FAN = register("bubble_coral_fan", new BlockCoralFan(Blocks.DEAD_BUBBLE_CORAL_FAN, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block FIRE_CORAL_FAN = register("fire_coral_fan", new BlockCoralFan(Blocks.DEAD_FIRE_CORAL_FAN, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block HORN_CORAL_FAN = register("horn_coral_fan", new BlockCoralFan(Blocks.DEAD_HORN_CORAL_FAN, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block DEAD_TUBE_CORAL_WALL_FAN = register("dead_tube_coral_wall_fan", new BlockCoralFanWallAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak().dropsLike(Blocks.DEAD_TUBE_CORAL_FAN)));
    public static final Block DEAD_BRAIN_CORAL_WALL_FAN = register("dead_brain_coral_wall_fan", new BlockCoralFanWallAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak().dropsLike(Blocks.DEAD_BRAIN_CORAL_FAN)));
    public static final Block DEAD_BUBBLE_CORAL_WALL_FAN = register("dead_bubble_coral_wall_fan", new BlockCoralFanWallAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak().dropsLike(Blocks.DEAD_BUBBLE_CORAL_FAN)));
    public static final Block DEAD_FIRE_CORAL_WALL_FAN = register("dead_fire_coral_wall_fan", new BlockCoralFanWallAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak().dropsLike(Blocks.DEAD_FIRE_CORAL_FAN)));
    public static final Block DEAD_HORN_CORAL_WALL_FAN = register("dead_horn_coral_wall_fan", new BlockCoralFanWallAbstract(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GRAY).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().instabreak().dropsLike(Blocks.DEAD_HORN_CORAL_FAN)));
    public static final Block TUBE_CORAL_WALL_FAN = register("tube_coral_wall_fan", new BlockCoralFanWall(Blocks.DEAD_TUBE_CORAL_WALL_FAN, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLUE).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).dropsLike(Blocks.TUBE_CORAL_FAN).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BRAIN_CORAL_WALL_FAN = register("brain_coral_wall_fan", new BlockCoralFanWall(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).dropsLike(Blocks.BRAIN_CORAL_FAN).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BUBBLE_CORAL_WALL_FAN = register("bubble_coral_wall_fan", new BlockCoralFanWall(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).dropsLike(Blocks.BUBBLE_CORAL_FAN).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block FIRE_CORAL_WALL_FAN = register("fire_coral_wall_fan", new BlockCoralFanWall(Blocks.DEAD_FIRE_CORAL_WALL_FAN, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).dropsLike(Blocks.FIRE_CORAL_FAN).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block HORN_CORAL_WALL_FAN = register("horn_coral_wall_fan", new BlockCoralFanWall(Blocks.DEAD_HORN_CORAL_WALL_FAN, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).noCollission().instabreak().sound(SoundEffectType.WET_GRASS).dropsLike(Blocks.HORN_CORAL_FAN).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SEA_PICKLE = register("sea_pickle", new BlockSeaPickle(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).lightLevel((iblockdata) -> {
        return BlockSeaPickle.isDead(iblockdata) ? 0 : 3 + 3 * (Integer) iblockdata.getValue(BlockSeaPickle.PICKLES);
    }).sound(SoundEffectType.SLIME_BLOCK).noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BLUE_ICE = register("blue_ice", new BlockHalfTransparent(BlockBase.Info.of().mapColor(MaterialMapColor.ICE).strength(2.8F).friction(0.989F).sound(SoundEffectType.GLASS)));
    public static final Block CONDUIT = register("conduit", new BlockConduit(BlockBase.Info.of().mapColor(MaterialMapColor.DIAMOND).forceSolidOn().instrument(BlockPropertyInstrument.HAT).strength(3.0F).lightLevel((iblockdata) -> {
        return 15;
    }).noOcclusion()));
    public static final Block BAMBOO_SAPLING = register("bamboo_sapling", new BlockBambooSapling(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).forceSolidOn().randomTicks().instabreak().noCollission().strength(1.0F).sound(SoundEffectType.BAMBOO_SAPLING).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BAMBOO = register("bamboo", new BlockBamboo(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).forceSolidOn().randomTicks().instabreak().strength(1.0F).sound(SoundEffectType.BAMBOO).noOcclusion().dynamicShape().offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never)));
    public static final Block POTTED_BAMBOO = register("potted_bamboo", flowerPot(Blocks.BAMBOO));
    public static final Block VOID_AIR = register("void_air", new BlockAir(BlockBase.Info.of().replaceable().noCollission().noLootTable().air()));
    public static final Block CAVE_AIR = register("cave_air", new BlockAir(BlockBase.Info.of().replaceable().noCollission().noLootTable().air()));
    public static final Block BUBBLE_COLUMN = register("bubble_column", new BlockBubbleColumn(BlockBase.Info.of().mapColor(MaterialMapColor.WATER).replaceable().noCollission().noLootTable().pushReaction(EnumPistonReaction.DESTROY).liquid().sound(SoundEffectType.EMPTY)));
    public static final Block POLISHED_GRANITE_STAIRS = register("polished_granite_stairs", legacyStair(Blocks.POLISHED_GRANITE));
    public static final Block SMOOTH_RED_SANDSTONE_STAIRS = register("smooth_red_sandstone_stairs", legacyStair(Blocks.SMOOTH_RED_SANDSTONE));
    public static final Block MOSSY_STONE_BRICK_STAIRS = register("mossy_stone_brick_stairs", legacyStair(Blocks.MOSSY_STONE_BRICKS));
    public static final Block POLISHED_DIORITE_STAIRS = register("polished_diorite_stairs", legacyStair(Blocks.POLISHED_DIORITE));
    public static final Block MOSSY_COBBLESTONE_STAIRS = register("mossy_cobblestone_stairs", legacyStair(Blocks.MOSSY_COBBLESTONE));
    public static final Block END_STONE_BRICK_STAIRS = register("end_stone_brick_stairs", legacyStair(Blocks.END_STONE_BRICKS));
    public static final Block STONE_STAIRS = register("stone_stairs", legacyStair(Blocks.STONE));
    public static final Block SMOOTH_SANDSTONE_STAIRS = register("smooth_sandstone_stairs", legacyStair(Blocks.SMOOTH_SANDSTONE));
    public static final Block SMOOTH_QUARTZ_STAIRS = register("smooth_quartz_stairs", legacyStair(Blocks.SMOOTH_QUARTZ));
    public static final Block GRANITE_STAIRS = register("granite_stairs", legacyStair(Blocks.GRANITE));
    public static final Block ANDESITE_STAIRS = register("andesite_stairs", legacyStair(Blocks.ANDESITE));
    public static final Block RED_NETHER_BRICK_STAIRS = register("red_nether_brick_stairs", legacyStair(Blocks.RED_NETHER_BRICKS));
    public static final Block POLISHED_ANDESITE_STAIRS = register("polished_andesite_stairs", legacyStair(Blocks.POLISHED_ANDESITE));
    public static final Block DIORITE_STAIRS = register("diorite_stairs", legacyStair(Blocks.DIORITE));
    public static final Block POLISHED_GRANITE_SLAB = register("polished_granite_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_GRANITE)));
    public static final Block SMOOTH_RED_SANDSTONE_SLAB = register("smooth_red_sandstone_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.SMOOTH_RED_SANDSTONE)));
    public static final Block MOSSY_STONE_BRICK_SLAB = register("mossy_stone_brick_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.MOSSY_STONE_BRICKS)));
    public static final Block POLISHED_DIORITE_SLAB = register("polished_diorite_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_DIORITE)));
    public static final Block MOSSY_COBBLESTONE_SLAB = register("mossy_cobblestone_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.MOSSY_COBBLESTONE)));
    public static final Block END_STONE_BRICK_SLAB = register("end_stone_brick_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.END_STONE_BRICKS)));
    public static final Block SMOOTH_SANDSTONE_SLAB = register("smooth_sandstone_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.SMOOTH_SANDSTONE)));
    public static final Block SMOOTH_QUARTZ_SLAB = register("smooth_quartz_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.SMOOTH_QUARTZ)));
    public static final Block GRANITE_SLAB = register("granite_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.GRANITE)));
    public static final Block ANDESITE_SLAB = register("andesite_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.ANDESITE)));
    public static final Block RED_NETHER_BRICK_SLAB = register("red_nether_brick_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.RED_NETHER_BRICKS)));
    public static final Block POLISHED_ANDESITE_SLAB = register("polished_andesite_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_ANDESITE)));
    public static final Block DIORITE_SLAB = register("diorite_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.DIORITE)));
    public static final Block BRICK_WALL = register("brick_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.BRICKS).forceSolidOn()));
    public static final Block PRISMARINE_WALL = register("prismarine_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.PRISMARINE).forceSolidOn()));
    public static final Block RED_SANDSTONE_WALL = register("red_sandstone_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.RED_SANDSTONE).forceSolidOn()));
    public static final Block MOSSY_STONE_BRICK_WALL = register("mossy_stone_brick_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.MOSSY_STONE_BRICKS).forceSolidOn()));
    public static final Block GRANITE_WALL = register("granite_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.GRANITE).forceSolidOn()));
    public static final Block STONE_BRICK_WALL = register("stone_brick_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.STONE_BRICKS).forceSolidOn()));
    public static final Block MUD_BRICK_WALL = register("mud_brick_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.MUD_BRICKS).forceSolidOn()));
    public static final Block NETHER_BRICK_WALL = register("nether_brick_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.NETHER_BRICKS).forceSolidOn()));
    public static final Block ANDESITE_WALL = register("andesite_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.ANDESITE).forceSolidOn()));
    public static final Block RED_NETHER_BRICK_WALL = register("red_nether_brick_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.RED_NETHER_BRICKS).forceSolidOn()));
    public static final Block SANDSTONE_WALL = register("sandstone_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.SANDSTONE).forceSolidOn()));
    public static final Block END_STONE_BRICK_WALL = register("end_stone_brick_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.END_STONE_BRICKS).forceSolidOn()));
    public static final Block DIORITE_WALL = register("diorite_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.DIORITE).forceSolidOn()));
    public static final Block SCAFFOLDING = register("scaffolding", new BlockScaffolding(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).noCollission().sound(SoundEffectType.SCAFFOLDING).dynamicShape().isValidSpawn(Blocks::never).pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never)));
    public static final Block LOOM = register("loom", new BlockLoom(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block BARREL = register("barrel", new BlockBarrel(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block SMOKER = register("smoker", new BlockSmoker(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F).lightLevel(litBlockEmission(13))));
    public static final Block BLAST_FURNACE = register("blast_furnace", new BlockBlastFurnace(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F).lightLevel(litBlockEmission(13))));
    public static final Block CARTOGRAPHY_TABLE = register("cartography_table", new BlockCartographyTable(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block FLETCHING_TABLE = register("fletching_table", new BlockFletchingTable(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block GRINDSTONE = register("grindstone", new BlockGrindstone(BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.STONE).pushReaction(EnumPistonReaction.BLOCK)));
    public static final Block LECTERN = register("lectern", new BlockLectern(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block SMITHING_TABLE = register("smithing_table", new BlockSmithingTable(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(2.5F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block STONECUTTER = register("stonecutter", new BlockStonecutter(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.5F)));
    public static final Block BELL = register("bell", new BlockBell(BlockBase.Info.of().mapColor(MaterialMapColor.GOLD).forceSolidOn().requiresCorrectToolForDrops().strength(5.0F).sound(SoundEffectType.ANVIL).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block LANTERN = register("lantern", new BlockLantern(BlockBase.Info.of().mapColor(MaterialMapColor.METAL).forceSolidOn().requiresCorrectToolForDrops().strength(3.5F).sound(SoundEffectType.LANTERN).lightLevel((iblockdata) -> {
        return 15;
    }).noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SOUL_LANTERN = register("soul_lantern", new BlockLantern(BlockBase.Info.of().mapColor(MaterialMapColor.METAL).forceSolidOn().requiresCorrectToolForDrops().strength(3.5F).sound(SoundEffectType.LANTERN).lightLevel((iblockdata) -> {
        return 10;
    }).noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CAMPFIRE = register("campfire", new BlockCampfire(true, 1, BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).lightLevel(litBlockEmission(15)).noOcclusion().ignitedByLava()));
    public static final Block SOUL_CAMPFIRE = register("soul_campfire", new BlockCampfire(false, 2, BlockBase.Info.of().mapColor(MaterialMapColor.PODZOL).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).lightLevel(litBlockEmission(10)).noOcclusion().ignitedByLava()));
    public static final Block SWEET_BERRY_BUSH = register("sweet_berry_bush", new BlockSweetBerryBush(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).randomTicks().noCollission().sound(SoundEffectType.SWEET_BERRY_BUSH).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block WARPED_STEM = register("warped_stem", netherStem(MaterialMapColor.WARPED_STEM));
    public static final Block STRIPPED_WARPED_STEM = register("stripped_warped_stem", netherStem(MaterialMapColor.WARPED_STEM));
    public static final Block WARPED_HYPHAE = register("warped_hyphae", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.WARPED_HYPHAE).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.STEM)));
    public static final Block STRIPPED_WARPED_HYPHAE = register("stripped_warped_hyphae", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.WARPED_HYPHAE).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.STEM)));
    public static final Block WARPED_NYLIUM = register("warped_nylium", new BlockNylium(BlockBase.Info.of().mapColor(MaterialMapColor.WARPED_NYLIUM).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.4F).sound(SoundEffectType.NYLIUM).randomTicks()));
    public static final Block WARPED_FUNGUS = register("warped_fungus", new BlockFungi(TreeFeatures.WARPED_FUNGUS_PLANTED, Blocks.WARPED_NYLIUM, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).instabreak().noCollission().sound(SoundEffectType.FUNGUS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block WARPED_WART_BLOCK = register("warped_wart_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.WARPED_WART_BLOCK).strength(1.0F).sound(SoundEffectType.WART_BLOCK)));
    public static final Block WARPED_ROOTS = register("warped_roots", new BlockRoots(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).replaceable().noCollission().instabreak().sound(SoundEffectType.ROOTS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block NETHER_SPROUTS = register("nether_sprouts", new BlockNetherSprouts(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).replaceable().noCollission().instabreak().sound(SoundEffectType.NETHER_SPROUTS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CRIMSON_STEM = register("crimson_stem", netherStem(MaterialMapColor.CRIMSON_STEM));
    public static final Block STRIPPED_CRIMSON_STEM = register("stripped_crimson_stem", netherStem(MaterialMapColor.CRIMSON_STEM));
    public static final Block CRIMSON_HYPHAE = register("crimson_hyphae", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.CRIMSON_HYPHAE).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.STEM)));
    public static final Block STRIPPED_CRIMSON_HYPHAE = register("stripped_crimson_hyphae", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.CRIMSON_HYPHAE).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.STEM)));
    public static final Block CRIMSON_NYLIUM = register("crimson_nylium", new BlockNylium(BlockBase.Info.of().mapColor(MaterialMapColor.CRIMSON_NYLIUM).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(0.4F).sound(SoundEffectType.NYLIUM).randomTicks()));
    public static final Block CRIMSON_FUNGUS = register("crimson_fungus", new BlockFungi(TreeFeatures.CRIMSON_FUNGUS_PLANTED, Blocks.CRIMSON_NYLIUM, BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instabreak().noCollission().sound(SoundEffectType.FUNGUS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SHROOMLIGHT = register("shroomlight", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_RED).strength(1.0F).sound(SoundEffectType.SHROOMLIGHT).lightLevel((iblockdata) -> {
        return 15;
    })));
    public static final Block WEEPING_VINES = register("weeping_vines", new BlockWeepingVines(BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).randomTicks().noCollission().instabreak().sound(SoundEffectType.WEEPING_VINES).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block WEEPING_VINES_PLANT = register("weeping_vines_plant", new BlockWeepingVinesPlant(BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).noCollission().instabreak().sound(SoundEffectType.WEEPING_VINES).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block TWISTING_VINES = register("twisting_vines", new BlockTwistingVines(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).randomTicks().noCollission().instabreak().sound(SoundEffectType.WEEPING_VINES).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block TWISTING_VINES_PLANT = register("twisting_vines_plant", new BlockTwistingVinesPlant(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).noCollission().instabreak().sound(SoundEffectType.WEEPING_VINES).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CRIMSON_ROOTS = register("crimson_roots", new BlockRoots(BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).replaceable().noCollission().instabreak().sound(SoundEffectType.ROOTS).offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CRIMSON_PLANKS = register("crimson_planks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.CRIMSON_STEM).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.NETHER_WOOD)));
    public static final Block WARPED_PLANKS = register("warped_planks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.WARPED_STEM).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.NETHER_WOOD)));
    public static final Block CRIMSON_SLAB = register("crimson_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.NETHER_WOOD)));
    public static final Block WARPED_SLAB = register("warped_slab", new BlockStepAbstract(BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.NETHER_WOOD)));
    public static final Block CRIMSON_PRESSURE_PLATE = register("crimson_pressure_plate", new BlockPressurePlateBinary(BlockSetType.CRIMSON, BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block WARPED_PRESSURE_PLATE = register("warped_pressure_plate", new BlockPressurePlateBinary(BlockSetType.WARPED, BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CRIMSON_FENCE = register("crimson_fence", new BlockFence(BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.NETHER_WOOD)));
    public static final Block WARPED_FENCE = register("warped_fence", new BlockFence(BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F).sound(SoundEffectType.NETHER_WOOD)));
    public static final Block CRIMSON_TRAPDOOR = register("crimson_trapdoor", new BlockTrapdoor(BlockSetType.CRIMSON, BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never)));
    public static final Block WARPED_TRAPDOOR = register("warped_trapdoor", new BlockTrapdoor(BlockSetType.WARPED, BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().isValidSpawn(Blocks::never)));
    public static final Block CRIMSON_FENCE_GATE = register("crimson_fence_gate", new BlockFenceGate(BlockPropertyWood.CRIMSON, BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F)));
    public static final Block WARPED_FENCE_GATE = register("warped_fence_gate", new BlockFenceGate(BlockPropertyWood.WARPED, BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).forceSolidOn().instrument(BlockPropertyInstrument.BASS).strength(2.0F, 3.0F)));
    public static final Block CRIMSON_STAIRS = register("crimson_stairs", legacyStair(Blocks.CRIMSON_PLANKS));
    public static final Block WARPED_STAIRS = register("warped_stairs", legacyStair(Blocks.WARPED_PLANKS));
    public static final Block CRIMSON_BUTTON = register("crimson_button", woodenButton(BlockSetType.CRIMSON));
    public static final Block WARPED_BUTTON = register("warped_button", woodenButton(BlockSetType.WARPED));
    public static final Block CRIMSON_DOOR = register("crimson_door", new BlockDoor(BlockSetType.CRIMSON, BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block WARPED_DOOR = register("warped_door", new BlockDoor(BlockSetType.WARPED, BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).strength(3.0F).noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CRIMSON_SIGN = register("crimson_sign", new BlockFloorSign(BlockPropertyWood.CRIMSON, BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).forceSolidOn().noCollission().strength(1.0F)));
    public static final Block WARPED_SIGN = register("warped_sign", new BlockFloorSign(BlockPropertyWood.WARPED, BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).forceSolidOn().noCollission().strength(1.0F)));
    public static final Block CRIMSON_WALL_SIGN = register("crimson_wall_sign", new BlockWallSign(BlockPropertyWood.CRIMSON, BlockBase.Info.of().mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).forceSolidOn().noCollission().strength(1.0F).dropsLike(Blocks.CRIMSON_SIGN)));
    public static final Block WARPED_WALL_SIGN = register("warped_wall_sign", new BlockWallSign(BlockPropertyWood.WARPED, BlockBase.Info.of().mapColor(Blocks.WARPED_PLANKS.defaultMapColor()).instrument(BlockPropertyInstrument.BASS).forceSolidOn().noCollission().strength(1.0F).dropsLike(Blocks.WARPED_SIGN)));
    public static final Block STRUCTURE_BLOCK = register("structure_block", new BlockStructure(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()));
    public static final Block JIGSAW = register("jigsaw", new BlockJigsaw(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().strength(-1.0F, 3600000.0F).noLootTable()));
    public static final Block COMPOSTER = register("composter", new BlockComposter(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(0.6F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block TARGET = register("target", new BlockTarget(BlockBase.Info.of().mapColor(MaterialMapColor.QUARTZ).strength(0.5F).sound(SoundEffectType.GRASS)));
    public static final Block BEE_NEST = register("bee_nest", new BlockBeehive(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_YELLOW).instrument(BlockPropertyInstrument.BASS).strength(0.3F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block BEEHIVE = register("beehive", new BlockBeehive(BlockBase.Info.of().mapColor(MaterialMapColor.WOOD).instrument(BlockPropertyInstrument.BASS).strength(0.6F).sound(SoundEffectType.WOOD).ignitedByLava()));
    public static final Block HONEY_BLOCK = register("honey_block", new BlockHoney(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).speedFactor(0.4F).jumpFactor(0.5F).noOcclusion().sound(SoundEffectType.HONEY_BLOCK)));
    public static final Block HONEYCOMB_BLOCK = register("honeycomb_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).strength(0.6F).sound(SoundEffectType.CORAL_BLOCK)));
    public static final Block NETHERITE_BLOCK = register("netherite_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).sound(SoundEffectType.NETHERITE_BLOCK)));
    public static final Block ANCIENT_DEBRIS = register("ancient_debris", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(30.0F, 1200.0F).sound(SoundEffectType.ANCIENT_DEBRIS)));
    public static final Block CRYING_OBSIDIAN = register("crying_obsidian", new BlockCryingObsidian(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).lightLevel((iblockdata) -> {
        return 10;
    })));
    public static final Block RESPAWN_ANCHOR = register("respawn_anchor", new BlockRespawnAnchor(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).lightLevel((iblockdata) -> {
        return BlockRespawnAnchor.getScaledChargeLevel(iblockdata, 15);
    })));
    public static final Block POTTED_CRIMSON_FUNGUS = register("potted_crimson_fungus", flowerPot(Blocks.CRIMSON_FUNGUS));
    public static final Block POTTED_WARPED_FUNGUS = register("potted_warped_fungus", flowerPot(Blocks.WARPED_FUNGUS));
    public static final Block POTTED_CRIMSON_ROOTS = register("potted_crimson_roots", flowerPot(Blocks.CRIMSON_ROOTS));
    public static final Block POTTED_WARPED_ROOTS = register("potted_warped_roots", flowerPot(Blocks.WARPED_ROOTS));
    public static final Block LODESTONE = register("lodestone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.METAL).requiresCorrectToolForDrops().strength(3.5F).sound(SoundEffectType.LODESTONE).pushReaction(EnumPistonReaction.BLOCK)));
    public static final Block BLACKSTONE = register("blackstone", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block BLACKSTONE_STAIRS = register("blackstone_stairs", legacyStair(Blocks.BLACKSTONE));
    public static final Block BLACKSTONE_WALL = register("blackstone_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.BLACKSTONE).forceSolidOn()));
    public static final Block BLACKSTONE_SLAB = register("blackstone_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.BLACKSTONE).strength(2.0F, 6.0F)));
    public static final Block POLISHED_BLACKSTONE = register("polished_blackstone", new Block(BlockBase.Info.ofLegacyCopy(Blocks.BLACKSTONE).strength(2.0F, 6.0F)));
    public static final Block POLISHED_BLACKSTONE_BRICKS = register("polished_blackstone_bricks", new Block(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE).strength(1.5F, 6.0F)));
    public static final Block CRACKED_POLISHED_BLACKSTONE_BRICKS = register("cracked_polished_blackstone_bricks", new Block(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE_BRICKS)));
    public static final Block CHISELED_POLISHED_BLACKSTONE = register("chiseled_polished_blackstone", new Block(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE).strength(1.5F, 6.0F)));
    public static final Block POLISHED_BLACKSTONE_BRICK_SLAB = register("polished_blackstone_brick_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE_BRICKS).strength(2.0F, 6.0F)));
    public static final Block POLISHED_BLACKSTONE_BRICK_STAIRS = register("polished_blackstone_brick_stairs", legacyStair(Blocks.POLISHED_BLACKSTONE_BRICKS));
    public static final Block POLISHED_BLACKSTONE_BRICK_WALL = register("polished_blackstone_brick_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE_BRICKS).forceSolidOn()));
    public static final Block GILDED_BLACKSTONE = register("gilded_blackstone", new Block(BlockBase.Info.ofLegacyCopy(Blocks.BLACKSTONE).sound(SoundEffectType.GILDED_BLACKSTONE)));
    public static final Block POLISHED_BLACKSTONE_STAIRS = register("polished_blackstone_stairs", legacyStair(Blocks.POLISHED_BLACKSTONE));
    public static final Block POLISHED_BLACKSTONE_SLAB = register("polished_blackstone_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE)));
    public static final Block POLISHED_BLACKSTONE_PRESSURE_PLATE = register("polished_blackstone_pressure_plate", new BlockPressurePlateBinary(BlockSetType.POLISHED_BLACKSTONE, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block POLISHED_BLACKSTONE_BUTTON = register("polished_blackstone_button", stoneButton());
    public static final Block POLISHED_BLACKSTONE_WALL = register("polished_blackstone_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_BLACKSTONE).forceSolidOn()));
    public static final Block CHISELED_NETHER_BRICKS = register("chiseled_nether_bricks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.NETHER_BRICKS)));
    public static final Block CRACKED_NETHER_BRICKS = register("cracked_nether_bricks", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.NETHER).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundEffectType.NETHER_BRICKS)));
    public static final Block QUARTZ_BRICKS = register("quartz_bricks", new Block(BlockBase.Info.ofLegacyCopy(Blocks.QUARTZ_BLOCK)));
    public static final Block CANDLE = register("candle", candle(MaterialMapColor.SAND));
    public static final Block WHITE_CANDLE = register("white_candle", candle(MaterialMapColor.WOOL));
    public static final Block ORANGE_CANDLE = register("orange_candle", candle(MaterialMapColor.COLOR_ORANGE));
    public static final Block MAGENTA_CANDLE = register("magenta_candle", candle(MaterialMapColor.COLOR_MAGENTA));
    public static final Block LIGHT_BLUE_CANDLE = register("light_blue_candle", candle(MaterialMapColor.COLOR_LIGHT_BLUE));
    public static final Block YELLOW_CANDLE = register("yellow_candle", candle(MaterialMapColor.COLOR_YELLOW));
    public static final Block LIME_CANDLE = register("lime_candle", candle(MaterialMapColor.COLOR_LIGHT_GREEN));
    public static final Block PINK_CANDLE = register("pink_candle", candle(MaterialMapColor.COLOR_PINK));
    public static final Block GRAY_CANDLE = register("gray_candle", candle(MaterialMapColor.COLOR_GRAY));
    public static final Block LIGHT_GRAY_CANDLE = register("light_gray_candle", candle(MaterialMapColor.COLOR_LIGHT_GRAY));
    public static final Block CYAN_CANDLE = register("cyan_candle", candle(MaterialMapColor.COLOR_CYAN));
    public static final Block PURPLE_CANDLE = register("purple_candle", candle(MaterialMapColor.COLOR_PURPLE));
    public static final Block BLUE_CANDLE = register("blue_candle", candle(MaterialMapColor.COLOR_BLUE));
    public static final Block BROWN_CANDLE = register("brown_candle", candle(MaterialMapColor.COLOR_BROWN));
    public static final Block GREEN_CANDLE = register("green_candle", candle(MaterialMapColor.COLOR_GREEN));
    public static final Block RED_CANDLE = register("red_candle", candle(MaterialMapColor.COLOR_RED));
    public static final Block BLACK_CANDLE = register("black_candle", candle(MaterialMapColor.COLOR_BLACK));
    public static final Block CANDLE_CAKE = register("candle_cake", new CandleCakeBlock(Blocks.CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CAKE).lightLevel(litBlockEmission(3))));
    public static final Block WHITE_CANDLE_CAKE = register("white_candle_cake", new CandleCakeBlock(Blocks.WHITE_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block ORANGE_CANDLE_CAKE = register("orange_candle_cake", new CandleCakeBlock(Blocks.ORANGE_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block MAGENTA_CANDLE_CAKE = register("magenta_candle_cake", new CandleCakeBlock(Blocks.MAGENTA_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block LIGHT_BLUE_CANDLE_CAKE = register("light_blue_candle_cake", new CandleCakeBlock(Blocks.LIGHT_BLUE_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block YELLOW_CANDLE_CAKE = register("yellow_candle_cake", new CandleCakeBlock(Blocks.YELLOW_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block LIME_CANDLE_CAKE = register("lime_candle_cake", new CandleCakeBlock(Blocks.LIME_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block PINK_CANDLE_CAKE = register("pink_candle_cake", new CandleCakeBlock(Blocks.PINK_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block GRAY_CANDLE_CAKE = register("gray_candle_cake", new CandleCakeBlock(Blocks.GRAY_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block LIGHT_GRAY_CANDLE_CAKE = register("light_gray_candle_cake", new CandleCakeBlock(Blocks.LIGHT_GRAY_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block CYAN_CANDLE_CAKE = register("cyan_candle_cake", new CandleCakeBlock(Blocks.CYAN_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block PURPLE_CANDLE_CAKE = register("purple_candle_cake", new CandleCakeBlock(Blocks.PURPLE_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block BLUE_CANDLE_CAKE = register("blue_candle_cake", new CandleCakeBlock(Blocks.BLUE_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block BROWN_CANDLE_CAKE = register("brown_candle_cake", new CandleCakeBlock(Blocks.BROWN_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block GREEN_CANDLE_CAKE = register("green_candle_cake", new CandleCakeBlock(Blocks.GREEN_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block RED_CANDLE_CAKE = register("red_candle_cake", new CandleCakeBlock(Blocks.RED_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block BLACK_CANDLE_CAKE = register("black_candle_cake", new CandleCakeBlock(Blocks.BLACK_CANDLE, BlockBase.Info.ofLegacyCopy(Blocks.CANDLE_CAKE)));
    public static final Block AMETHYST_BLOCK = register("amethyst_block", new AmethystBlock(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).strength(1.5F).sound(SoundEffectType.AMETHYST).requiresCorrectToolForDrops()));
    public static final Block BUDDING_AMETHYST = register("budding_amethyst", new BuddingAmethystBlock(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).randomTicks().strength(1.5F).sound(SoundEffectType.AMETHYST).requiresCorrectToolForDrops().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block AMETHYST_CLUSTER = register("amethyst_cluster", new AmethystClusterBlock(7.0F, 3.0F, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PURPLE).forceSolidOn().noOcclusion().sound(SoundEffectType.AMETHYST_CLUSTER).strength(1.5F).lightLevel((iblockdata) -> {
        return 5;
    }).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block LARGE_AMETHYST_BUD = register("large_amethyst_bud", new AmethystClusterBlock(5.0F, 3.0F, BlockBase.Info.ofLegacyCopy(Blocks.AMETHYST_CLUSTER).sound(SoundEffectType.MEDIUM_AMETHYST_BUD).lightLevel((iblockdata) -> {
        return 4;
    })));
    public static final Block MEDIUM_AMETHYST_BUD = register("medium_amethyst_bud", new AmethystClusterBlock(4.0F, 3.0F, BlockBase.Info.ofLegacyCopy(Blocks.AMETHYST_CLUSTER).sound(SoundEffectType.LARGE_AMETHYST_BUD).lightLevel((iblockdata) -> {
        return 2;
    })));
    public static final Block SMALL_AMETHYST_BUD = register("small_amethyst_bud", new AmethystClusterBlock(3.0F, 4.0F, BlockBase.Info.ofLegacyCopy(Blocks.AMETHYST_CLUSTER).sound(SoundEffectType.SMALL_AMETHYST_BUD).lightLevel((iblockdata) -> {
        return 1;
    })));
    public static final Block TUFF = register("tuff", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_GRAY).instrument(BlockPropertyInstrument.BASEDRUM).sound(SoundEffectType.TUFF).requiresCorrectToolForDrops().strength(1.5F, 6.0F)));
    public static final Block TUFF_SLAB = register("tuff_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.TUFF).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block TUFF_STAIRS = register("tuff_stairs", new BlockStairs(Blocks.TUFF.defaultBlockState(), BlockBase.Info.ofLegacyCopy(Blocks.TUFF).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block TUFF_WALL = register("tuff_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.TUFF).forceSolidOn().requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block POLISHED_TUFF = register("polished_tuff", new Block(BlockBase.Info.ofLegacyCopy(Blocks.TUFF).sound(SoundEffectType.POLISHED_TUFF).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block POLISHED_TUFF_SLAB = register("polished_tuff_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_TUFF)));
    public static final Block POLISHED_TUFF_STAIRS = register("polished_tuff_stairs", new BlockStairs(Blocks.POLISHED_TUFF.defaultBlockState(), BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_TUFF)));
    public static final Block POLISHED_TUFF_WALL = register("polished_tuff_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_TUFF).forceSolidOn()));
    public static final Block CHISELED_TUFF = register("chiseled_tuff", new Block(BlockBase.Info.ofLegacyCopy(Blocks.TUFF).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block TUFF_BRICKS = register("tuff_bricks", new Block(BlockBase.Info.ofLegacyCopy(Blocks.TUFF).sound(SoundEffectType.TUFF_BRICKS).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block TUFF_BRICK_SLAB = register("tuff_brick_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.TUFF_BRICKS)));
    public static final Block TUFF_BRICK_STAIRS = register("tuff_brick_stairs", new BlockStairs(Blocks.TUFF_BRICKS.defaultBlockState(), BlockBase.Info.ofLegacyCopy(Blocks.TUFF_BRICKS)));
    public static final Block TUFF_BRICK_WALL = register("tuff_brick_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.TUFF_BRICKS).forceSolidOn()));
    public static final Block CHISELED_TUFF_BRICKS = register("chiseled_tuff_bricks", new Block(BlockBase.Info.ofLegacyCopy(Blocks.TUFF_BRICKS)));
    public static final Block CALCITE = register("calcite", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_WHITE).instrument(BlockPropertyInstrument.BASEDRUM).sound(SoundEffectType.CALCITE).requiresCorrectToolForDrops().strength(0.75F)));
    public static final Block TINTED_GLASS = register("tinted_glass", new TintedGlassBlock(BlockBase.Info.ofLegacyCopy(Blocks.GLASS).mapColor(MaterialMapColor.COLOR_GRAY).noOcclusion().isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)));
    public static final Block POWDER_SNOW = register("powder_snow", new PowderSnowBlock(BlockBase.Info.of().mapColor(MaterialMapColor.SNOW).strength(0.25F).sound(SoundEffectType.POWDER_SNOW).dynamicShape().isRedstoneConductor(Blocks::never)));
    public static final Block SCULK_SENSOR = register("sculk_sensor", new SculkSensorBlock(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_CYAN).strength(1.5F).sound(SoundEffectType.SCULK_SENSOR).lightLevel((iblockdata) -> {
        return 1;
    }).emissiveRendering((iblockdata, iblockaccess, blockposition) -> {
        return SculkSensorBlock.getPhase(iblockdata) == SculkSensorPhase.ACTIVE;
    })));
    public static final Block CALIBRATED_SCULK_SENSOR = register("calibrated_sculk_sensor", new CalibratedSculkSensorBlock(BlockBase.Info.ofLegacyCopy(Blocks.SCULK_SENSOR)));
    public static final Block SCULK = register("sculk", new SculkBlock(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).strength(0.2F).sound(SoundEffectType.SCULK)));
    public static final Block SCULK_VEIN = register("sculk_vein", new SculkVeinBlock(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).forceSolidOn().noCollission().strength(0.2F).sound(SoundEffectType.SCULK_VEIN).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SCULK_CATALYST = register("sculk_catalyst", new SculkCatalystBlock(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).strength(3.0F, 3.0F).sound(SoundEffectType.SCULK_CATALYST).lightLevel((iblockdata) -> {
        return 6;
    })));
    public static final Block SCULK_SHRIEKER = register("sculk_shrieker", new SculkShriekerBlock(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_BLACK).strength(3.0F, 3.0F).sound(SoundEffectType.SCULK_SHRIEKER)));
    public static final Block COPPER_BLOCK = register("copper_block", new WeatheringCopperFullBlock(WeatheringCopper.a.UNAFFECTED, BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundEffectType.COPPER)));
    public static final Block EXPOSED_COPPER = register("exposed_copper", new WeatheringCopperFullBlock(WeatheringCopper.a.EXPOSED, BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GRAY)));
    public static final Block WEATHERED_COPPER = register("weathered_copper", new WeatheringCopperFullBlock(WeatheringCopper.a.WEATHERED, BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MaterialMapColor.WARPED_STEM)));
    public static final Block OXIDIZED_COPPER = register("oxidized_copper", new WeatheringCopperFullBlock(WeatheringCopper.a.OXIDIZED, BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MaterialMapColor.WARPED_NYLIUM)));
    public static final Block COPPER_ORE = register("copper_ore", new DropExperienceBlock(ConstantInt.of(0), BlockBase.Info.ofLegacyCopy(Blocks.IRON_ORE)));
    public static final Block DEEPSLATE_COPPER_ORE = register("deepslate_copper_ore", new DropExperienceBlock(ConstantInt.of(0), BlockBase.Info.ofLegacyCopy(Blocks.COPPER_ORE).mapColor(MaterialMapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundEffectType.DEEPSLATE)));
    public static final Block OXIDIZED_CUT_COPPER = register("oxidized_cut_copper", new WeatheringCopperFullBlock(WeatheringCopper.a.OXIDIZED, BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER)));
    public static final Block WEATHERED_CUT_COPPER = register("weathered_cut_copper", new WeatheringCopperFullBlock(WeatheringCopper.a.WEATHERED, BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER)));
    public static final Block EXPOSED_CUT_COPPER = register("exposed_cut_copper", new WeatheringCopperFullBlock(WeatheringCopper.a.EXPOSED, BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER)));
    public static final Block CUT_COPPER = register("cut_copper", new WeatheringCopperFullBlock(WeatheringCopper.a.UNAFFECTED, BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK)));
    public static final Block OXIDIZED_CHISELED_COPPER = register("oxidized_chiseled_copper", new WeatheringCopperFullBlock(WeatheringCopper.a.OXIDIZED, BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block WEATHERED_CHISELED_COPPER = register("weathered_chiseled_copper", new WeatheringCopperFullBlock(WeatheringCopper.a.WEATHERED, BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block EXPOSED_CHISELED_COPPER = register("exposed_chiseled_copper", new WeatheringCopperFullBlock(WeatheringCopper.a.EXPOSED, BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block CHISELED_COPPER = register("chiseled_copper", new WeatheringCopperFullBlock(WeatheringCopper.a.UNAFFECTED, BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block WAXED_OXIDIZED_CHISELED_COPPER = register("waxed_oxidized_chiseled_copper", new Block(BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_CHISELED_COPPER)));
    public static final Block WAXED_WEATHERED_CHISELED_COPPER = register("waxed_weathered_chiseled_copper", new Block(BlockBase.Info.ofFullCopy(Blocks.WEATHERED_CHISELED_COPPER)));
    public static final Block WAXED_EXPOSED_CHISELED_COPPER = register("waxed_exposed_chiseled_copper", new Block(BlockBase.Info.ofFullCopy(Blocks.EXPOSED_CHISELED_COPPER)));
    public static final Block WAXED_CHISELED_COPPER = register("waxed_chiseled_copper", new Block(BlockBase.Info.ofFullCopy(Blocks.CHISELED_COPPER)));
    public static final Block OXIDIZED_CUT_COPPER_STAIRS = register("oxidized_cut_copper_stairs", new WeatheringCopperStairBlock(WeatheringCopper.a.OXIDIZED, Blocks.OXIDIZED_CUT_COPPER.defaultBlockState(), BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_CUT_COPPER)));
    public static final Block WEATHERED_CUT_COPPER_STAIRS = register("weathered_cut_copper_stairs", new WeatheringCopperStairBlock(WeatheringCopper.a.WEATHERED, Blocks.WEATHERED_CUT_COPPER.defaultBlockState(), BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER)));
    public static final Block EXPOSED_CUT_COPPER_STAIRS = register("exposed_cut_copper_stairs", new WeatheringCopperStairBlock(WeatheringCopper.a.EXPOSED, Blocks.EXPOSED_CUT_COPPER.defaultBlockState(), BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER)));
    public static final Block CUT_COPPER_STAIRS = register("cut_copper_stairs", new WeatheringCopperStairBlock(WeatheringCopper.a.UNAFFECTED, Blocks.CUT_COPPER.defaultBlockState(), BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK)));
    public static final Block OXIDIZED_CUT_COPPER_SLAB = register("oxidized_cut_copper_slab", new WeatheringCopperSlabBlock(WeatheringCopper.a.OXIDIZED, BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_CUT_COPPER)));
    public static final Block WEATHERED_CUT_COPPER_SLAB = register("weathered_cut_copper_slab", new WeatheringCopperSlabBlock(WeatheringCopper.a.WEATHERED, BlockBase.Info.ofFullCopy(Blocks.WEATHERED_CUT_COPPER)));
    public static final Block EXPOSED_CUT_COPPER_SLAB = register("exposed_cut_copper_slab", new WeatheringCopperSlabBlock(WeatheringCopper.a.EXPOSED, BlockBase.Info.ofFullCopy(Blocks.EXPOSED_CUT_COPPER)));
    public static final Block CUT_COPPER_SLAB = register("cut_copper_slab", new WeatheringCopperSlabBlock(WeatheringCopper.a.UNAFFECTED, BlockBase.Info.ofFullCopy(Blocks.CUT_COPPER)));
    public static final Block WAXED_COPPER_BLOCK = register("waxed_copper_block", new Block(BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK)));
    public static final Block WAXED_WEATHERED_COPPER = register("waxed_weathered_copper", new Block(BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER)));
    public static final Block WAXED_EXPOSED_COPPER = register("waxed_exposed_copper", new Block(BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER)));
    public static final Block WAXED_OXIDIZED_COPPER = register("waxed_oxidized_copper", new Block(BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER)));
    public static final Block WAXED_OXIDIZED_CUT_COPPER = register("waxed_oxidized_cut_copper", new Block(BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER)));
    public static final Block WAXED_WEATHERED_CUT_COPPER = register("waxed_weathered_cut_copper", new Block(BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER)));
    public static final Block WAXED_EXPOSED_CUT_COPPER = register("waxed_exposed_cut_copper", new Block(BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER)));
    public static final Block WAXED_CUT_COPPER = register("waxed_cut_copper", new Block(BlockBase.Info.ofFullCopy(Blocks.COPPER_BLOCK)));
    public static final Block WAXED_OXIDIZED_CUT_COPPER_STAIRS = register("waxed_oxidized_cut_copper_stairs", stair(Blocks.WAXED_OXIDIZED_CUT_COPPER));
    public static final Block WAXED_WEATHERED_CUT_COPPER_STAIRS = register("waxed_weathered_cut_copper_stairs", stair(Blocks.WAXED_WEATHERED_CUT_COPPER));
    public static final Block WAXED_EXPOSED_CUT_COPPER_STAIRS = register("waxed_exposed_cut_copper_stairs", stair(Blocks.WAXED_EXPOSED_CUT_COPPER));
    public static final Block WAXED_CUT_COPPER_STAIRS = register("waxed_cut_copper_stairs", stair(Blocks.WAXED_CUT_COPPER));
    public static final Block WAXED_OXIDIZED_CUT_COPPER_SLAB = register("waxed_oxidized_cut_copper_slab", new BlockStepAbstract(BlockBase.Info.ofFullCopy(Blocks.WAXED_OXIDIZED_CUT_COPPER).requiresCorrectToolForDrops()));
    public static final Block WAXED_WEATHERED_CUT_COPPER_SLAB = register("waxed_weathered_cut_copper_slab", new BlockStepAbstract(BlockBase.Info.ofFullCopy(Blocks.WAXED_WEATHERED_CUT_COPPER).requiresCorrectToolForDrops()));
    public static final Block WAXED_EXPOSED_CUT_COPPER_SLAB = register("waxed_exposed_cut_copper_slab", new BlockStepAbstract(BlockBase.Info.ofFullCopy(Blocks.WAXED_EXPOSED_CUT_COPPER).requiresCorrectToolForDrops()));
    public static final Block WAXED_CUT_COPPER_SLAB = register("waxed_cut_copper_slab", new BlockStepAbstract(BlockBase.Info.ofFullCopy(Blocks.WAXED_CUT_COPPER).requiresCorrectToolForDrops()));
    public static final Block COPPER_DOOR = register("copper_door", new WeatheringCopperDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.UNAFFECTED, BlockBase.Info.of().mapColor(Blocks.COPPER_BLOCK.defaultMapColor()).strength(3.0F, 6.0F).noOcclusion().requiresCorrectToolForDrops().pushReaction(EnumPistonReaction.DESTROY).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block EXPOSED_COPPER_DOOR = register("exposed_copper_door", new WeatheringCopperDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.EXPOSED, BlockBase.Info.ofFullCopy(Blocks.COPPER_DOOR).mapColor(Blocks.EXPOSED_COPPER.defaultMapColor())));
    public static final Block OXIDIZED_COPPER_DOOR = register("oxidized_copper_door", new WeatheringCopperDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.OXIDIZED, BlockBase.Info.ofFullCopy(Blocks.COPPER_DOOR).mapColor(Blocks.OXIDIZED_COPPER.defaultMapColor())));
    public static final Block WEATHERED_COPPER_DOOR = register("weathered_copper_door", new WeatheringCopperDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.WEATHERED, BlockBase.Info.ofFullCopy(Blocks.COPPER_DOOR).mapColor(Blocks.WEATHERED_COPPER.defaultMapColor())));
    public static final Block WAXED_COPPER_DOOR = register("waxed_copper_door", new BlockDoor(BlockSetType.COPPER, BlockBase.Info.ofFullCopy(Blocks.COPPER_DOOR)));
    public static final Block WAXED_EXPOSED_COPPER_DOOR = register("waxed_exposed_copper_door", new BlockDoor(BlockSetType.COPPER, BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER_DOOR)));
    public static final Block WAXED_OXIDIZED_COPPER_DOOR = register("waxed_oxidized_copper_door", new BlockDoor(BlockSetType.COPPER, BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER_DOOR)));
    public static final Block WAXED_WEATHERED_COPPER_DOOR = register("waxed_weathered_copper_door", new BlockDoor(BlockSetType.COPPER, BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER_DOOR)));
    public static final Block COPPER_TRAPDOOR = register("copper_trapdoor", new WeatheringCopperTrapDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.UNAFFECTED, BlockBase.Info.of().mapColor(Blocks.COPPER_BLOCK.defaultMapColor()).strength(3.0F, 6.0F).requiresCorrectToolForDrops().noOcclusion().isValidSpawn(Blocks::never).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block EXPOSED_COPPER_TRAPDOOR = register("exposed_copper_trapdoor", new WeatheringCopperTrapDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.EXPOSED, BlockBase.Info.ofFullCopy(Blocks.COPPER_TRAPDOOR).mapColor(Blocks.EXPOSED_COPPER.defaultMapColor())));
    public static final Block OXIDIZED_COPPER_TRAPDOOR = register("oxidized_copper_trapdoor", new WeatheringCopperTrapDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.OXIDIZED, BlockBase.Info.ofFullCopy(Blocks.COPPER_TRAPDOOR).mapColor(Blocks.OXIDIZED_COPPER.defaultMapColor())));
    public static final Block WEATHERED_COPPER_TRAPDOOR = register("weathered_copper_trapdoor", new WeatheringCopperTrapDoorBlock(BlockSetType.COPPER, WeatheringCopper.a.WEATHERED, BlockBase.Info.ofFullCopy(Blocks.COPPER_TRAPDOOR).mapColor(Blocks.WEATHERED_COPPER.defaultMapColor())));
    public static final Block WAXED_COPPER_TRAPDOOR = register("waxed_copper_trapdoor", new BlockTrapdoor(BlockSetType.COPPER, BlockBase.Info.ofFullCopy(Blocks.COPPER_TRAPDOOR)));
    public static final Block WAXED_EXPOSED_COPPER_TRAPDOOR = register("waxed_exposed_copper_trapdoor", new BlockTrapdoor(BlockSetType.COPPER, BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER_TRAPDOOR)));
    public static final Block WAXED_OXIDIZED_COPPER_TRAPDOOR = register("waxed_oxidized_copper_trapdoor", new BlockTrapdoor(BlockSetType.COPPER, BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER_TRAPDOOR)));
    public static final Block WAXED_WEATHERED_COPPER_TRAPDOOR = register("waxed_weathered_copper_trapdoor", new BlockTrapdoor(BlockSetType.COPPER, BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER_TRAPDOOR)));
    public static final Block COPPER_GRATE = register("copper_grate", new WeatheringCopperGrateBlock(WeatheringCopper.a.UNAFFECTED, BlockBase.Info.of().strength(3.0F, 6.0F).sound(SoundEffectType.COPPER_GRATE).mapColor(MaterialMapColor.COLOR_ORANGE).noOcclusion().requiresCorrectToolForDrops().isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block EXPOSED_COPPER_GRATE = register("exposed_copper_grate", new WeatheringCopperGrateBlock(WeatheringCopper.a.EXPOSED, BlockBase.Info.ofFullCopy(Blocks.COPPER_GRATE).mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GRAY)));
    public static final Block WEATHERED_COPPER_GRATE = register("weathered_copper_grate", new WeatheringCopperGrateBlock(WeatheringCopper.a.WEATHERED, BlockBase.Info.ofFullCopy(Blocks.COPPER_GRATE).mapColor(MaterialMapColor.WARPED_STEM)));
    public static final Block OXIDIZED_COPPER_GRATE = register("oxidized_copper_grate", new WeatheringCopperGrateBlock(WeatheringCopper.a.OXIDIZED, BlockBase.Info.ofFullCopy(Blocks.COPPER_GRATE).mapColor(MaterialMapColor.WARPED_NYLIUM)));
    public static final Block WAXED_COPPER_GRATE = register("waxed_copper_grate", new WaterloggedTransparentBlock(BlockBase.Info.ofFullCopy(Blocks.COPPER_GRATE)));
    public static final Block WAXED_EXPOSED_COPPER_GRATE = register("waxed_exposed_copper_grate", new WaterloggedTransparentBlock(BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER_GRATE)));
    public static final Block WAXED_WEATHERED_COPPER_GRATE = register("waxed_weathered_copper_grate", new WaterloggedTransparentBlock(BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER_GRATE)));
    public static final Block WAXED_OXIDIZED_COPPER_GRATE = register("waxed_oxidized_copper_grate", new WaterloggedTransparentBlock(BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER_GRATE)));
    public static final Block COPPER_BULB = register("copper_bulb", new WeatheringCopperBulbBlock(WeatheringCopper.a.UNAFFECTED, BlockBase.Info.of().mapColor(Blocks.COPPER_BLOCK.defaultMapColor()).strength(3.0F, 6.0F).sound(SoundEffectType.COPPER_BULB).requiresCorrectToolForDrops().isRedstoneConductor(Blocks::never).lightLevel(litBlockEmission(15)).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block EXPOSED_COPPER_BULB = register("exposed_copper_bulb", new WeatheringCopperBulbBlock(WeatheringCopper.a.EXPOSED, BlockBase.Info.ofFullCopy(Blocks.COPPER_BULB).mapColor(MaterialMapColor.TERRACOTTA_LIGHT_GRAY).lightLevel(litBlockEmission(12))));
    public static final Block WEATHERED_COPPER_BULB = register("weathered_copper_bulb", new WeatheringCopperBulbBlock(WeatheringCopper.a.WEATHERED, BlockBase.Info.ofFullCopy(Blocks.COPPER_BULB).mapColor(MaterialMapColor.WARPED_STEM).lightLevel(litBlockEmission(8))));
    public static final Block OXIDIZED_COPPER_BULB = register("oxidized_copper_bulb", new WeatheringCopperBulbBlock(WeatheringCopper.a.OXIDIZED, BlockBase.Info.ofFullCopy(Blocks.COPPER_BULB).mapColor(MaterialMapColor.WARPED_NYLIUM).lightLevel(litBlockEmission(4))));
    public static final Block WAXED_COPPER_BULB = register("waxed_copper_bulb", new CopperBulbBlock(BlockBase.Info.ofFullCopy(Blocks.COPPER_BULB)));
    public static final Block WAXED_EXPOSED_COPPER_BULB = register("waxed_exposed_copper_bulb", new CopperBulbBlock(BlockBase.Info.ofFullCopy(Blocks.EXPOSED_COPPER_BULB)));
    public static final Block WAXED_WEATHERED_COPPER_BULB = register("waxed_weathered_copper_bulb", new CopperBulbBlock(BlockBase.Info.ofFullCopy(Blocks.WEATHERED_COPPER_BULB)));
    public static final Block WAXED_OXIDIZED_COPPER_BULB = register("waxed_oxidized_copper_bulb", new CopperBulbBlock(BlockBase.Info.ofFullCopy(Blocks.OXIDIZED_COPPER_BULB)));
    public static final Block LIGHTNING_ROD = register("lightning_rod", new LightningRodBlock(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).forceSolidOn().requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundEffectType.COPPER).noOcclusion()));
    public static final Block POINTED_DRIPSTONE = register("pointed_dripstone", new PointedDripstoneBlock(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_BROWN).forceSolidOn().instrument(BlockPropertyInstrument.BASEDRUM).noOcclusion().sound(SoundEffectType.POINTED_DRIPSTONE).randomTicks().strength(1.5F, 3.0F).dynamicShape().offsetType(BlockBase.EnumRandomOffset.XZ).pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never)));
    public static final Block DRIPSTONE_BLOCK = register("dripstone_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_BROWN).instrument(BlockPropertyInstrument.BASEDRUM).sound(SoundEffectType.DRIPSTONE_BLOCK).requiresCorrectToolForDrops().strength(1.5F, 1.0F)));
    public static final Block CAVE_VINES = register("cave_vines", new CaveVinesBlock(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).randomTicks().noCollission().lightLevel(CaveVines.emission(14)).instabreak().sound(SoundEffectType.CAVE_VINES).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block CAVE_VINES_PLANT = register("cave_vines_plant", new CaveVinesPlantBlock(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().lightLevel(CaveVines.emission(14)).instabreak().sound(SoundEffectType.CAVE_VINES).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SPORE_BLOSSOM = register("spore_blossom", new SporeBlossomBlock(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).instabreak().noCollission().sound(SoundEffectType.SPORE_BLOSSOM).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block AZALEA = register("azalea", new AzaleaBlock(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).forceSolidOff().instabreak().sound(SoundEffectType.AZALEA).noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block FLOWERING_AZALEA = register("flowering_azalea", new AzaleaBlock(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).forceSolidOff().instabreak().sound(SoundEffectType.FLOWERING_AZALEA).noOcclusion().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block MOSS_CARPET = register("moss_carpet", new CarpetBlock(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).strength(0.1F).sound(SoundEffectType.MOSS_CARPET).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block PINK_PETALS = register("pink_petals", new PinkPetalsBlock(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().sound(SoundEffectType.PINK_PETALS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block MOSS_BLOCK = register("moss_block", new MossBlock(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_GREEN).strength(0.1F).sound(SoundEffectType.MOSS).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BIG_DRIPLEAF = register("big_dripleaf", new BigDripleafBlock(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).forceSolidOff().strength(0.1F).sound(SoundEffectType.BIG_DRIPLEAF).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block BIG_DRIPLEAF_STEM = register("big_dripleaf_stem", new BigDripleafStemBlock(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().strength(0.1F).sound(SoundEffectType.BIG_DRIPLEAF).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block SMALL_DRIPLEAF = register("small_dripleaf", new SmallDripleafBlock(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).noCollission().instabreak().sound(SoundEffectType.SMALL_DRIPLEAF).offsetType(BlockBase.EnumRandomOffset.XYZ).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block HANGING_ROOTS = register("hanging_roots", new HangingRootsBlock(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).replaceable().noCollission().instabreak().sound(SoundEffectType.HANGING_ROOTS).offsetType(BlockBase.EnumRandomOffset.XZ).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block ROOTED_DIRT = register("rooted_dirt", new RootedDirtBlock(BlockBase.Info.of().mapColor(MaterialMapColor.DIRT).strength(0.5F).sound(SoundEffectType.ROOTED_DIRT)));
    public static final Block MUD = register("mud", new MudBlock(BlockBase.Info.ofLegacyCopy(Blocks.DIRT).mapColor(MaterialMapColor.TERRACOTTA_CYAN).isValidSpawn(Blocks::always).isRedstoneConductor(Blocks::always).isViewBlocking(Blocks::always).isSuffocating(Blocks::always).sound(SoundEffectType.MUD)));
    public static final Block DEEPSLATE = register("deepslate", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.DEEPSLATE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 6.0F).sound(SoundEffectType.DEEPSLATE)));
    public static final Block COBBLED_DEEPSLATE = register("cobbled_deepslate", new Block(BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE).strength(3.5F, 6.0F)));
    public static final Block COBBLED_DEEPSLATE_STAIRS = register("cobbled_deepslate_stairs", legacyStair(Blocks.COBBLED_DEEPSLATE));
    public static final Block COBBLED_DEEPSLATE_SLAB = register("cobbled_deepslate_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.COBBLED_DEEPSLATE)));
    public static final Block COBBLED_DEEPSLATE_WALL = register("cobbled_deepslate_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.COBBLED_DEEPSLATE).forceSolidOn()));
    public static final Block POLISHED_DEEPSLATE = register("polished_deepslate", new Block(BlockBase.Info.ofLegacyCopy(Blocks.COBBLED_DEEPSLATE).sound(SoundEffectType.POLISHED_DEEPSLATE)));
    public static final Block POLISHED_DEEPSLATE_STAIRS = register("polished_deepslate_stairs", legacyStair(Blocks.POLISHED_DEEPSLATE));
    public static final Block POLISHED_DEEPSLATE_SLAB = register("polished_deepslate_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_DEEPSLATE)));
    public static final Block POLISHED_DEEPSLATE_WALL = register("polished_deepslate_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.POLISHED_DEEPSLATE).forceSolidOn()));
    public static final Block DEEPSLATE_TILES = register("deepslate_tiles", new Block(BlockBase.Info.ofLegacyCopy(Blocks.COBBLED_DEEPSLATE).sound(SoundEffectType.DEEPSLATE_TILES)));
    public static final Block DEEPSLATE_TILE_STAIRS = register("deepslate_tile_stairs", legacyStair(Blocks.DEEPSLATE_TILES));
    public static final Block DEEPSLATE_TILE_SLAB = register("deepslate_tile_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE_TILES)));
    public static final Block DEEPSLATE_TILE_WALL = register("deepslate_tile_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE_TILES).forceSolidOn()));
    public static final Block DEEPSLATE_BRICKS = register("deepslate_bricks", new Block(BlockBase.Info.ofLegacyCopy(Blocks.COBBLED_DEEPSLATE).sound(SoundEffectType.DEEPSLATE_BRICKS)));
    public static final Block DEEPSLATE_BRICK_STAIRS = register("deepslate_brick_stairs", legacyStair(Blocks.DEEPSLATE_BRICKS));
    public static final Block DEEPSLATE_BRICK_SLAB = register("deepslate_brick_slab", new BlockStepAbstract(BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE_BRICKS)));
    public static final Block DEEPSLATE_BRICK_WALL = register("deepslate_brick_wall", new BlockCobbleWall(BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE_BRICKS).forceSolidOn()));
    public static final Block CHISELED_DEEPSLATE = register("chiseled_deepslate", new Block(BlockBase.Info.ofLegacyCopy(Blocks.COBBLED_DEEPSLATE).sound(SoundEffectType.DEEPSLATE_BRICKS)));
    public static final Block CRACKED_DEEPSLATE_BRICKS = register("cracked_deepslate_bricks", new Block(BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE_BRICKS)));
    public static final Block CRACKED_DEEPSLATE_TILES = register("cracked_deepslate_tiles", new Block(BlockBase.Info.ofLegacyCopy(Blocks.DEEPSLATE_TILES)));
    public static final Block INFESTED_DEEPSLATE = register("infested_deepslate", new InfestedRotatedPillarBlock(Blocks.DEEPSLATE, BlockBase.Info.of().mapColor(MaterialMapColor.DEEPSLATE).sound(SoundEffectType.DEEPSLATE)));
    public static final Block SMOOTH_BASALT = register("smooth_basalt", new Block(BlockBase.Info.ofLegacyCopy(Blocks.BASALT)));
    public static final Block RAW_IRON_BLOCK = register("raw_iron_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.RAW_IRON).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F)));
    public static final Block RAW_COPPER_BLOCK = register("raw_copper_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_ORANGE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F)));
    public static final Block RAW_GOLD_BLOCK = register("raw_gold_block", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.GOLD).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(5.0F, 6.0F)));
    public static final Block POTTED_AZALEA = register("potted_azalea_bush", flowerPot(Blocks.AZALEA));
    public static final Block POTTED_FLOWERING_AZALEA = register("potted_flowering_azalea_bush", flowerPot(Blocks.FLOWERING_AZALEA));
    public static final Block OCHRE_FROGLIGHT = register("ochre_froglight", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.SAND).strength(0.3F).lightLevel((iblockdata) -> {
        return 15;
    }).sound(SoundEffectType.FROGLIGHT)));
    public static final Block VERDANT_FROGLIGHT = register("verdant_froglight", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.GLOW_LICHEN).strength(0.3F).lightLevel((iblockdata) -> {
        return 15;
    }).sound(SoundEffectType.FROGLIGHT)));
    public static final Block PEARLESCENT_FROGLIGHT = register("pearlescent_froglight", new BlockRotatable(BlockBase.Info.of().mapColor(MaterialMapColor.COLOR_PINK).strength(0.3F).lightLevel((iblockdata) -> {
        return 15;
    }).sound(SoundEffectType.FROGLIGHT)));
    public static final Block FROGSPAWN = register("frogspawn", new FrogspawnBlock(BlockBase.Info.of().mapColor(MaterialMapColor.WATER).instabreak().noOcclusion().noCollission().sound(SoundEffectType.FROGSPAWN).pushReaction(EnumPistonReaction.DESTROY)));
    public static final Block REINFORCED_DEEPSLATE = register("reinforced_deepslate", new Block(BlockBase.Info.of().mapColor(MaterialMapColor.DEEPSLATE).instrument(BlockPropertyInstrument.BASEDRUM).sound(SoundEffectType.DEEPSLATE).strength(55.0F, 1200.0F)));
    public static final Block DECORATED_POT = register("decorated_pot", new DecoratedPotBlock(BlockBase.Info.of().mapColor(MaterialMapColor.TERRACOTTA_RED).strength(0.0F, 0.0F).pushReaction(EnumPistonReaction.DESTROY).noOcclusion()));
    public static final Block CRAFTER = register("crafter", new CrafterBlock(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).strength(1.5F, 3.5F).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block TRIAL_SPAWNER = register("trial_spawner", new TrialSpawnerBlock(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel((iblockdata) -> {
        return ((TrialSpawnerState) iblockdata.getValue(TrialSpawnerBlock.STATE)).lightLevel();
    }).strength(50.0F).sound(SoundEffectType.TRIAL_SPAWNER).isViewBlocking(Blocks::never).noOcclusion().requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block VAULT = register("vault", new VaultBlock(BlockBase.Info.of().mapColor(MaterialMapColor.STONE).instrument(BlockPropertyInstrument.BASEDRUM).noOcclusion().requiresCorrectToolForDrops().sound(SoundEffectType.VAULT).lightLevel((iblockdata) -> {
        return ((VaultState) iblockdata.getValue(VaultBlock.STATE)).lightLevel();
    }).strength(50.0F).isViewBlocking(Blocks::never).requiredFeatures(FeatureFlags.UPDATE_1_21)));
    public static final Block HEAVY_CORE = register("heavy_core", new HeavyCoreBlock(BlockBase.Info.of().mapColor(MaterialMapColor.METAL).instrument(BlockPropertyInstrument.SNARE).sound(SoundEffectType.HEAVY_CORE).strength(10.0F).pushReaction(EnumPistonReaction.NORMAL).explosionResistance(1200.0F).requiredFeatures(FeatureFlags.UPDATE_1_21)));

    public Blocks() {}

    private static ToIntFunction<IBlockData> litBlockEmission(int i) {
        return (iblockdata) -> {
            return (Boolean) iblockdata.getValue(BlockProperties.LIT) ? i : 0;
        };
    }

    private static Function<IBlockData, MaterialMapColor> waterloggedMapColor(MaterialMapColor materialmapcolor) {
        return (iblockdata) -> {
            return (Boolean) iblockdata.getValue(BlockProperties.WATERLOGGED) ? MaterialMapColor.WATER : materialmapcolor;
        };
    }

    private static Boolean never(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EntityTypes<?> entitytypes) {
        return false;
    }

    private static Boolean always(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EntityTypes<?> entitytypes) {
        return true;
    }

    private static Boolean ocelotOrParrot(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, EntityTypes<?> entitytypes) {
        return entitytypes == EntityTypes.OCELOT || entitytypes == EntityTypes.PARROT;
    }

    private static Block bed(EnumColor enumcolor) {
        return new BlockBed(enumcolor, BlockBase.Info.of().mapColor((iblockdata) -> {
            return iblockdata.getValue(BlockBed.PART) == BlockPropertyBedPart.FOOT ? enumcolor.getMapColor() : MaterialMapColor.WOOL;
        }).sound(SoundEffectType.WOOD).strength(0.2F).noOcclusion().ignitedByLava().pushReaction(EnumPistonReaction.DESTROY));
    }

    private static Block log(MaterialMapColor materialmapcolor, MaterialMapColor materialmapcolor1) {
        return new BlockRotatable(BlockBase.Info.of().mapColor((iblockdata) -> {
            return iblockdata.getValue(BlockRotatable.AXIS) == EnumDirection.EnumAxis.Y ? materialmapcolor : materialmapcolor1;
        }).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.WOOD).ignitedByLava());
    }

    private static Block log(MaterialMapColor materialmapcolor, MaterialMapColor materialmapcolor1, SoundEffectType soundeffecttype) {
        return new BlockRotatable(BlockBase.Info.of().mapColor((iblockdata) -> {
            return iblockdata.getValue(BlockRotatable.AXIS) == EnumDirection.EnumAxis.Y ? materialmapcolor : materialmapcolor1;
        }).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(soundeffecttype).ignitedByLava());
    }

    private static Block netherStem(MaterialMapColor materialmapcolor) {
        return new BlockRotatable(BlockBase.Info.of().mapColor((iblockdata) -> {
            return materialmapcolor;
        }).instrument(BlockPropertyInstrument.BASS).strength(2.0F).sound(SoundEffectType.STEM));
    }

    private static boolean always(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return true;
    }

    private static boolean never(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return false;
    }

    private static Block stainedGlass(EnumColor enumcolor) {
        return new BlockStainedGlass(enumcolor, BlockBase.Info.of().mapColor(enumcolor).instrument(BlockPropertyInstrument.HAT).strength(0.3F).sound(SoundEffectType.GLASS).noOcclusion().isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never));
    }

    private static Block leaves(SoundEffectType soundeffecttype) {
        return new BlockLeaves(BlockBase.Info.of().mapColor(MaterialMapColor.PLANT).strength(0.2F).randomTicks().sound(soundeffecttype).noOcclusion().isValidSpawn(Blocks::ocelotOrParrot).isSuffocating(Blocks::never).isViewBlocking(Blocks::never).ignitedByLava().pushReaction(EnumPistonReaction.DESTROY).isRedstoneConductor(Blocks::never));
    }

    private static Block shulkerBox(@Nullable EnumColor enumcolor, MaterialMapColor materialmapcolor) {
        return new BlockShulkerBox(enumcolor, BlockBase.Info.of().mapColor(materialmapcolor).forceSolidOn().strength(2.0F).dynamicShape().noOcclusion().isSuffocating(Blocks.NOT_CLOSED_SHULKER).isViewBlocking(Blocks.NOT_CLOSED_SHULKER).pushReaction(EnumPistonReaction.DESTROY));
    }

    private static Block pistonBase(boolean flag) {
        BlockBase.f blockbase_f = (iblockdata, iblockaccess, blockposition) -> {
            return !(Boolean) iblockdata.getValue(BlockPiston.EXTENDED);
        };

        return new BlockPiston(flag, BlockBase.Info.of().mapColor(MaterialMapColor.STONE).strength(1.5F).isRedstoneConductor(Blocks::never).isSuffocating(blockbase_f).isViewBlocking(blockbase_f).pushReaction(EnumPistonReaction.BLOCK));
    }

    private static Block woodenButton(BlockSetType blocksettype) {
        return new BlockButtonAbstract(blocksettype, 30, BlockBase.Info.of().noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY));
    }

    private static Block stoneButton() {
        return new BlockButtonAbstract(BlockSetType.STONE, 20, BlockBase.Info.of().noCollission().strength(0.5F).pushReaction(EnumPistonReaction.DESTROY));
    }

    private static Block flowerPot(Block block) {
        return new BlockFlowerPot(block, BlockBase.Info.of().instabreak().noOcclusion().pushReaction(EnumPistonReaction.DESTROY));
    }

    private static Block candle(MaterialMapColor materialmapcolor) {
        return new CandleBlock(BlockBase.Info.of().mapColor(materialmapcolor).noOcclusion().strength(0.1F).sound(SoundEffectType.CANDLE).lightLevel(CandleBlock.LIGHT_EMISSION).pushReaction(EnumPistonReaction.DESTROY));
    }

    /** @deprecated */
    @Deprecated
    private static Block legacyStair(Block block) {
        return new BlockStairs(block.defaultBlockState(), BlockBase.Info.ofLegacyCopy(block));
    }

    private static Block stair(Block block) {
        return new BlockStairs(block.defaultBlockState(), BlockBase.Info.ofFullCopy(block));
    }

    public static Block register(String s, Block block) {
        return (Block) IRegistry.register(BuiltInRegistries.BLOCK, s, block);
    }

    public static Block register(ResourceKey<Block> resourcekey, Block block) {
        return (Block) IRegistry.register(BuiltInRegistries.BLOCK, resourcekey, block);
    }

    public static void rebuildCache() {
        Block.BLOCK_STATE_REGISTRY.forEach(BlockBase.BlockData::initCache);
    }

    static {
        Iterator iterator = BuiltInRegistries.BLOCK.iterator();

        while (iterator.hasNext()) {
            Block block = (Block) iterator.next();
            UnmodifiableIterator unmodifiableiterator = block.getStateDefinition().getPossibleStates().iterator();

            while (unmodifiableiterator.hasNext()) {
                IBlockData iblockdata = (IBlockData) unmodifiableiterator.next();

                Block.BLOCK_STATE_REGISTRY.add(iblockdata);
                iblockdata.initCache();
            }

            block.getLootTable();
        }

    }
}
