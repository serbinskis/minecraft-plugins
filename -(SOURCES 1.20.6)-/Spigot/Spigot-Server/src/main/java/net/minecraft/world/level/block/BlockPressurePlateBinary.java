package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.BlockBase;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;

// CraftBukkit start
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.event.entity.EntityInteractEvent;
// CraftBukkit end

public class BlockPressurePlateBinary extends BlockPressurePlateAbstract {

    public static final MapCodec<BlockPressurePlateBinary> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter((blockpressureplatebinary) -> {
            return blockpressureplatebinary.type;
        }), propertiesCodec()).apply(instance, BlockPressurePlateBinary::new);
    });
    public static final BlockStateBoolean POWERED = BlockProperties.POWERED;

    @Override
    public MapCodec<BlockPressurePlateBinary> codec() {
        return BlockPressurePlateBinary.CODEC;
    }

    protected BlockPressurePlateBinary(BlockSetType blocksettype, BlockBase.Info blockbase_info) {
        super(blockbase_info, blocksettype);
        this.registerDefaultState((IBlockData) ((IBlockData) this.stateDefinition.any()).setValue(BlockPressurePlateBinary.POWERED, false));
    }

    @Override
    protected int getSignalForState(IBlockData iblockdata) {
        return (Boolean) iblockdata.getValue(BlockPressurePlateBinary.POWERED) ? 15 : 0;
    }

    @Override
    protected IBlockData setSignalForState(IBlockData iblockdata, int i) {
        return (IBlockData) iblockdata.setValue(BlockPressurePlateBinary.POWERED, i > 0);
    }

    @Override
    protected int getSignalStrength(World world, BlockPosition blockposition) {
        Class<? extends Entity> oclass; // CraftBukkit

        switch (this.type.pressurePlateSensitivity()) {
            case EVERYTHING:
                oclass = Entity.class;
                break;
            case MOBS:
                oclass = EntityLiving.class;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        Class<? extends Entity> oclass1 = oclass;

        // CraftBukkit start - Call interact event when turning on a pressure plate
        for (Entity entity : getEntities(world, BlockPressurePlateBinary.TOUCH_AABB.move(blockposition), oclass)) {
            if (this.getSignalForState(world.getBlockState(blockposition)) == 0) {
                org.bukkit.World bworld = world.getWorld();
                org.bukkit.plugin.PluginManager manager = world.getCraftServer().getPluginManager();
                org.bukkit.event.Cancellable cancellable;

                if (entity instanceof EntityHuman) {
                    cancellable = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerInteractEvent((EntityHuman) entity, org.bukkit.event.block.Action.PHYSICAL, blockposition, null, null, null);
                } else {
                    cancellable = new EntityInteractEvent(entity.getBukkitEntity(), bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
                    manager.callEvent((EntityInteractEvent) cancellable);
                }

                // We only want to block turning the plate on if all events are cancelled
                if (cancellable.isCancelled()) {
                    continue;
                }
            }

            return 15;
        }

        return 0;
        // CraftBukkit end
    }

    @Override
    protected void createBlockStateDefinition(BlockStateList.a<Block, IBlockData> blockstatelist_a) {
        blockstatelist_a.add(BlockPressurePlateBinary.POWERED);
    }
}
