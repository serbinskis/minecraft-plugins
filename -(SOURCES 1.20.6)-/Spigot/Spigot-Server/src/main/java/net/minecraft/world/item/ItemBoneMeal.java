package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockCoralFanWallAbstract;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IBlockFragilePlantElement;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;

public class ItemBoneMeal extends Item {

    public static final int GRASS_SPREAD_WIDTH = 3;
    public static final int GRASS_SPREAD_HEIGHT = 1;
    public static final int GRASS_COUNT_MULTIPLIER = 3;

    public ItemBoneMeal(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        // CraftBukkit start - extract bonemeal application logic to separate, static method
        return applyBonemeal(itemactioncontext);
    }

    public static EnumInteractionResult applyBonemeal(ItemActionContext itemactioncontext) {
        // CraftBukkit end
        World world = itemactioncontext.getLevel();
        BlockPosition blockposition = itemactioncontext.getClickedPos();
        BlockPosition blockposition1 = blockposition.relative(itemactioncontext.getClickedFace());

        if (growCrop(itemactioncontext.getItemInHand(), world, blockposition)) {
            if (!world.isClientSide) {
                if (itemactioncontext.getPlayer() != null) itemactioncontext.getPlayer().gameEvent(GameEvent.ITEM_INTERACT_FINISH); // CraftBukkit - SPIGOT-7518
                world.levelEvent(1505, blockposition, 15);
            }

            return EnumInteractionResult.sidedSuccess(world.isClientSide);
        } else {
            IBlockData iblockdata = world.getBlockState(blockposition);
            boolean flag = iblockdata.isFaceSturdy(world, blockposition, itemactioncontext.getClickedFace());

            if (flag && growWaterPlant(itemactioncontext.getItemInHand(), world, blockposition1, itemactioncontext.getClickedFace())) {
                if (!world.isClientSide) {
                    if (itemactioncontext.getPlayer() != null) itemactioncontext.getPlayer().gameEvent(GameEvent.ITEM_INTERACT_FINISH); // CraftBukkit - SPIGOT-7518
                    world.levelEvent(1505, blockposition1, 15);
                }

                return EnumInteractionResult.sidedSuccess(world.isClientSide);
            } else {
                return EnumInteractionResult.PASS;
            }
        }
    }

    public static boolean growCrop(ItemStack itemstack, World world, BlockPosition blockposition) {
        IBlockData iblockdata = world.getBlockState(blockposition);
        Block block = iblockdata.getBlock();

        if (block instanceof IBlockFragilePlantElement iblockfragileplantelement) {
            if (iblockfragileplantelement.isValidBonemealTarget(world, blockposition, iblockdata)) {
                if (world instanceof WorldServer) {
                    if (iblockfragileplantelement.isBonemealSuccess(world, world.random, blockposition, iblockdata)) {
                        iblockfragileplantelement.performBonemeal((WorldServer) world, world.random, blockposition, iblockdata);
                    }

                    itemstack.shrink(1);
                }

                return true;
            }
        }

        return false;
    }

    public static boolean growWaterPlant(ItemStack itemstack, World world, BlockPosition blockposition, @Nullable EnumDirection enumdirection) {
        if (world.getBlockState(blockposition).is(Blocks.WATER) && world.getFluidState(blockposition).getAmount() == 8) {
            if (!(world instanceof WorldServer)) {
                return true;
            } else {
                RandomSource randomsource = world.getRandom();
                int i = 0;

                while (i < 128) {
                    BlockPosition blockposition1 = blockposition;
                    IBlockData iblockdata = Blocks.SEAGRASS.defaultBlockState();
                    int j = 0;

                    while (true) {
                        if (j < i / 16) {
                            blockposition1 = blockposition1.offset(randomsource.nextInt(3) - 1, (randomsource.nextInt(3) - 1) * randomsource.nextInt(3) / 2, randomsource.nextInt(3) - 1);
                            if (!world.getBlockState(blockposition1).isCollisionShapeFullBlock(world, blockposition1)) {
                                ++j;
                                continue;
                            }
                        } else {
                            Holder<BiomeBase> holder = world.getBiome(blockposition1);

                            if (holder.is(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL)) {
                                if (i == 0 && enumdirection != null && enumdirection.getAxis().isHorizontal()) {
                                    iblockdata = (IBlockData) BuiltInRegistries.BLOCK.getRandomElementOf(TagsBlock.WALL_CORALS, world.random).map((holder1) -> {
                                        return ((Block) holder1.value()).defaultBlockState();
                                    }).orElse(iblockdata);
                                    if (iblockdata.hasProperty(BlockCoralFanWallAbstract.FACING)) {
                                        iblockdata = (IBlockData) iblockdata.setValue(BlockCoralFanWallAbstract.FACING, enumdirection);
                                    }
                                } else if (randomsource.nextInt(4) == 0) {
                                    iblockdata = (IBlockData) BuiltInRegistries.BLOCK.getRandomElementOf(TagsBlock.UNDERWATER_BONEMEALS, world.random).map((holder1) -> {
                                        return ((Block) holder1.value()).defaultBlockState();
                                    }).orElse(iblockdata);
                                }
                            }

                            if (iblockdata.is(TagsBlock.WALL_CORALS, (blockbase_blockdata) -> {
                                return blockbase_blockdata.hasProperty(BlockCoralFanWallAbstract.FACING);
                            })) {
                                for (int k = 0; !iblockdata.canSurvive(world, blockposition1) && k < 4; ++k) {
                                    iblockdata = (IBlockData) iblockdata.setValue(BlockCoralFanWallAbstract.FACING, EnumDirection.EnumDirectionLimit.HORIZONTAL.getRandomDirection(randomsource));
                                }
                            }

                            if (iblockdata.canSurvive(world, blockposition1)) {
                                IBlockData iblockdata1 = world.getBlockState(blockposition1);

                                if (iblockdata1.is(Blocks.WATER) && world.getFluidState(blockposition1).getAmount() == 8) {
                                    world.setBlock(blockposition1, iblockdata, 3);
                                } else if (iblockdata1.is(Blocks.SEAGRASS) && randomsource.nextInt(10) == 0) {
                                    ((IBlockFragilePlantElement) Blocks.SEAGRASS).performBonemeal((WorldServer) world, randomsource, blockposition1, iblockdata1);
                                }
                            }
                        }

                        ++i;
                        break;
                    }
                }

                itemstack.shrink(1);
                return true;
            }
        } else {
            return false;
        }
    }

    public static void addGrowthParticles(GeneratorAccess generatoraccess, BlockPosition blockposition, int i) {
        IBlockData iblockdata = generatoraccess.getBlockState(blockposition);
        Block block = iblockdata.getBlock();

        if (block instanceof IBlockFragilePlantElement) {
            IBlockFragilePlantElement iblockfragileplantelement = (IBlockFragilePlantElement) block;
            BlockPosition blockposition1 = iblockfragileplantelement.getParticlePos(blockposition);

            switch (iblockfragileplantelement.getType()) {
                case NEIGHBOR_SPREADER:
                    ParticleUtils.spawnParticles(generatoraccess, blockposition1, i * 3, 3.0D, 1.0D, false, Particles.HAPPY_VILLAGER);
                    break;
                case GROWER:
                    ParticleUtils.spawnParticleInBlock(generatoraccess, blockposition1, i, Particles.HAPPY_VILLAGER);
            }
        } else if (iblockdata.is(Blocks.WATER)) {
            ParticleUtils.spawnParticles(generatoraccess, blockposition, i * 3, 3.0D, 1.0D, false, Particles.HAPPY_VILLAGER);
        }

    }
}
