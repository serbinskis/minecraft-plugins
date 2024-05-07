package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.particles.ParticleParamRedstone;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.phys.MovingObjectPositionBlock;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityInteractEvent;
// CraftBukkit end

public class BlockRedstoneOre extends Block {

    public static final MapCodec<BlockRedstoneOre> CODEC = simpleCodec(BlockRedstoneOre::new);
    public static final BlockStateBoolean LIT = BlockRedstoneTorch.LIT;

    @Override
    public MapCodec<BlockRedstoneOre> codec() {
        return BlockRedstoneOre.CODEC;
    }

    public BlockRedstoneOre(BlockBase.Info blockbase_info) {
        super(blockbase_info);
        this.registerDefaultState((IBlockData) this.defaultBlockState().setValue(BlockRedstoneOre.LIT, false));
    }

    @Override
    protected void attack(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman) {
        interact(iblockdata, world, blockposition, entityhuman); // CraftBukkit - add entityhuman
        super.attack(iblockdata, world, blockposition, entityhuman);
    }

    @Override
    public void stepOn(World world, BlockPosition blockposition, IBlockData iblockdata, Entity entity) {
        if (!entity.isSteppingCarefully()) {
            // CraftBukkit start
            if (entity instanceof EntityHuman) {
                org.bukkit.event.player.PlayerInteractEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerInteractEvent((EntityHuman) entity, org.bukkit.event.block.Action.PHYSICAL, blockposition, null, null, null);
                if (!event.isCancelled()) {
                    interact(world.getBlockState(blockposition), world, blockposition, entity); // add entity
                }
            } else {
                EntityInteractEvent event = new EntityInteractEvent(entity.getBukkitEntity(), world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
                world.getCraftServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    interact(world.getBlockState(blockposition), world, blockposition, entity); // add entity
                }
            }
            // CraftBukkit end
        }

        super.stepOn(world, blockposition, iblockdata, entity);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemstack, IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman, EnumHand enumhand, MovingObjectPositionBlock movingobjectpositionblock) {
        if (world.isClientSide) {
            spawnParticles(world, blockposition);
        } else {
            interact(iblockdata, world, blockposition, entityhuman); // CraftBukkit - add entityhuman
        }

        return itemstack.getItem() instanceof ItemBlock && (new BlockActionContext(entityhuman, enumhand, itemstack, movingobjectpositionblock)).canPlace() ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.SUCCESS;
    }

    private static void interact(IBlockData iblockdata, World world, BlockPosition blockposition, Entity entity) { // CraftBukkit - add Entity
        spawnParticles(world, blockposition);
        if (!(Boolean) iblockdata.getValue(BlockRedstoneOre.LIT)) {
            // CraftBukkit start
            if (!CraftEventFactory.callEntityChangeBlockEvent(entity, blockposition, iblockdata.setValue(BlockRedstoneOre.LIT, true))) {
                return;
            }
            // CraftBukkit end
            world.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockRedstoneOre.LIT, true), 3);
        }

    }

    @Override
    protected boolean isRandomlyTicking(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockRedstoneOre.LIT);
    }

    @Override
    protected void randomTick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(BlockRedstoneOre.LIT)) {
            // CraftBukkit start
            if (CraftEventFactory.callBlockFadeEvent(worldserver, blockposition, iblockdata.setValue(BlockRedstoneOre.LIT, false)).isCancelled()) {
                return;
            }
            // CraftBukkit end
            worldserver.setBlock(blockposition, (IBlockData) iblockdata.setValue(BlockRedstoneOre.LIT, false), 3);
        }

    }

    @Override
    protected void spawnAfterBreak(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, ItemStack itemstack, boolean flag) {
        super.spawnAfterBreak(iblockdata, worldserver, blockposition, itemstack, flag);
        // CraftBukkit start - Delegated to getExpDrop
    }

    @Override
    public int getExpDrop(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, ItemStack itemstack, boolean flag) {
        if (flag && EnchantmentManager.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) == 0) {
            int i = 1 + worldserver.random.nextInt(5);

            // this.popExperience(worldserver, blockposition, i);
            return i;
        }

        return 0;
        // CraftBukkit end
    }

    @Override
    public void animateTick(IBlockData iblockdata, World world, BlockPosition blockposition, RandomSource randomsource) {
        if ((Boolean) iblockdata.getValue(BlockRedstoneOre.LIT)) {
            spawnParticles(world, blockposition);
        }

    }

    private static void spawnParticles(World world, BlockPosition blockposition) {
        double d0 = 0.5625D;
        RandomSource randomsource = world.random;
        EnumDirection[] aenumdirection = EnumDirection.values();
        int i = aenumdirection.length;

        for (int j = 0; j < i; ++j) {
            EnumDirection enumdirection = aenumdirection[j];
            BlockPosition blockposition1 = blockposition.relative(enumdirection);

            if (!world.getBlockState(blockposition1).isSolidRender(world, blockposition1)) {
                EnumDirection.EnumAxis enumdirection_enumaxis = enumdirection.getAxis();
                double d1 = enumdirection_enumaxis == EnumDirection.EnumAxis.X ? 0.5D + 0.5625D * (double) enumdirection.getStepX() : (double) randomsource.nextFloat();
                double d2 = enumdirection_enumaxis == EnumDirection.EnumAxis.Y ? 0.5D + 0.5625D * (double) enumdirection.getStepY() : (double) randomsource.nextFloat();
                double d3 = enumdirection_enumaxis == EnumDirection.EnumAxis.Z ? 0.5D + 0.5625D * (double) enumdirection.getStepZ() : (double) randomsource.nextFloat();

                world.addParticle(ParticleParamRedstone.REDSTONE, (double) blockposition.getX() + d1, (double) blockposition.getY() + d2, (double) blockposition.getZ() + d3, 0.0D, 0.0D, 0.0D);
            }
        }

    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockRedstoneOre.LIT);
    }
}
