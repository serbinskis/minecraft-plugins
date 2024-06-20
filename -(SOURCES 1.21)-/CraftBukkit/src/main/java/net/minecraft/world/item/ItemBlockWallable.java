package net.minecraft.world.item;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.world.item.context.BlockActionContext;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.shapes.VoxelShapeCollision;
// CraftBukkit start
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.block.BlockCanBuildEvent;
// CraftBukkit end

public class ItemBlockWallable extends ItemBlock {

    public final Block wallBlock;
    private final EnumDirection attachmentDirection;

    public ItemBlockWallable(Block block, Block block1, Item.Info item_info, EnumDirection enumdirection) {
        super(block, item_info);
        this.wallBlock = block1;
        this.attachmentDirection = enumdirection;
    }

    protected boolean canPlace(IWorldReader iworldreader, IBlockData iblockdata, BlockPosition blockposition) {
        return iblockdata.canSurvive(iworldreader, blockposition);
    }

    @Nullable
    @Override
    protected IBlockData getPlacementState(BlockActionContext blockactioncontext) {
        IBlockData iblockdata = this.wallBlock.getStateForPlacement(blockactioncontext);
        IBlockData iblockdata1 = null;
        World world = blockactioncontext.getLevel();
        BlockPosition blockposition = blockactioncontext.getClickedPos();
        EnumDirection[] aenumdirection = blockactioncontext.getNearestLookingDirections();
        int i = aenumdirection.length;

        for (int j = 0; j < i; ++j) {
            EnumDirection enumdirection = aenumdirection[j];

            if (enumdirection != this.attachmentDirection.getOpposite()) {
                IBlockData iblockdata2 = enumdirection == this.attachmentDirection ? this.getBlock().getStateForPlacement(blockactioncontext) : iblockdata;

                if (iblockdata2 != null && this.canPlace(world, iblockdata2, blockposition)) {
                    iblockdata1 = iblockdata2;
                    break;
                }
            }
        }

        // CraftBukkit start
        if (iblockdata1 != null) {
            boolean defaultReturn = world.isUnobstructed(iblockdata1, blockposition, VoxelShapeCollision.empty());
            org.bukkit.entity.Player player = (blockactioncontext.getPlayer() instanceof EntityPlayer) ? (org.bukkit.entity.Player) blockactioncontext.getPlayer().getBukkitEntity() : null;

            BlockCanBuildEvent event = new BlockCanBuildEvent(CraftBlock.at(world, blockposition), player, CraftBlockData.fromData(iblockdata1), defaultReturn);
            blockactioncontext.getLevel().getCraftServer().getPluginManager().callEvent(event);

            return (event.isBuildable()) ? iblockdata1 : null;
        } else {
            return null;
        }
        // CraftBukkit end
    }

    @Override
    public void registerBlocks(Map<Block, Item> map, Item item) {
        super.registerBlocks(map, item);
        map.put(this.wallBlock, item);
    }
}
