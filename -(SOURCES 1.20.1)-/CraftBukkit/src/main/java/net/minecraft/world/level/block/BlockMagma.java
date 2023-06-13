package net.minecraft.world.level.block;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockMagma extends Block {

    private static final int BUBBLE_COLUMN_CHECK_DELAY = 20;

    public BlockMagma(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public void stepOn(World world, BlockPosition blockposition, IBlockData iblockdata, Entity entity) {
        if (!entity.isSteppingCarefully() && entity instanceof EntityLiving && !EnchantmentManager.hasFrostWalker((EntityLiving) entity)) {
            org.bukkit.craftbukkit.event.CraftEventFactory.blockDamage = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()); // CraftBukkit
            entity.hurt(world.damageSources().hotFloor(), 1.0F);
            org.bukkit.craftbukkit.event.CraftEventFactory.blockDamage = null; // CraftBukkit
        }

        super.stepOn(world, blockposition, iblockdata, entity);
    }

    @Override
    public void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, RandomSource randomsource) {
        BlockBubbleColumn.updateColumn(worldserver, blockposition.above(), iblockdata);
    }

    @Override
    public IBlockData updateShape(IBlockData iblockdata, EnumDirection enumdirection, IBlockData iblockdata1, GeneratorAccess generatoraccess, BlockPosition blockposition, BlockPosition blockposition1) {
        if (enumdirection == EnumDirection.UP && iblockdata1.is(Blocks.WATER)) {
            generatoraccess.scheduleTick(blockposition, (Block) this, 20);
        }

        return super.updateShape(iblockdata, enumdirection, iblockdata1, generatoraccess, blockposition, blockposition1);
    }

    @Override
    public void onPlace(IBlockData iblockdata, World world, BlockPosition blockposition, IBlockData iblockdata1, boolean flag) {
        world.scheduleTick(blockposition, (Block) this, 20);
    }
}
