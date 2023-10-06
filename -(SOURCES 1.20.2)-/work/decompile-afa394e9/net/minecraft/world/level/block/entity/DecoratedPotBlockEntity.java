package net.minecraft.world.level.block.entity;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;

public class DecoratedPotBlockEntity extends TileEntity {

    public static final String TAG_SHERDS = "sherds";
    public DecoratedPotBlockEntity.Decoration decorations;

    public DecoratedPotBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.DECORATED_POT, blockposition, iblockdata);
        this.decorations = DecoratedPotBlockEntity.Decoration.EMPTY;
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound) {
        super.saveAdditional(nbttagcompound);
        this.decorations.save(nbttagcompound);
    }

    @Override
    public void load(NBTTagCompound nbttagcompound) {
        super.load(nbttagcompound);
        this.decorations = DecoratedPotBlockEntity.Decoration.load(nbttagcompound);
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public EnumDirection getDirection() {
        return (EnumDirection) this.getBlockState().getValue(BlockProperties.HORIZONTAL_FACING);
    }

    public DecoratedPotBlockEntity.Decoration getDecorations() {
        return this.decorations;
    }

    public void setFromItem(ItemStack itemstack) {
        this.decorations = DecoratedPotBlockEntity.Decoration.load(ItemBlock.getBlockEntityData(itemstack));
    }

    public ItemStack getItem() {
        return createDecoratedPotItem(this.decorations);
    }

    public static ItemStack createDecoratedPotItem(DecoratedPotBlockEntity.Decoration decoratedpotblockentity_decoration) {
        ItemStack itemstack = Items.DECORATED_POT.getDefaultInstance();
        NBTTagCompound nbttagcompound = decoratedpotblockentity_decoration.save(new NBTTagCompound());

        ItemBlock.setBlockEntityData(itemstack, TileEntityTypes.DECORATED_POT, nbttagcompound);
        return itemstack;
    }

    public static record Decoration(Item back, Item left, Item right, Item front) {

        public static final DecoratedPotBlockEntity.Decoration EMPTY = new DecoratedPotBlockEntity.Decoration(Items.BRICK, Items.BRICK, Items.BRICK, Items.BRICK);

        public NBTTagCompound save(NBTTagCompound nbttagcompound) {
            if (this.equals(DecoratedPotBlockEntity.Decoration.EMPTY)) {
                return nbttagcompound;
            } else {
                NBTTagList nbttaglist = new NBTTagList();

                this.sorted().forEach((item) -> {
                    nbttaglist.add(NBTTagString.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
                });
                nbttagcompound.put("sherds", nbttaglist);
                return nbttagcompound;
            }
        }

        public Stream<Item> sorted() {
            return Stream.of(this.back, this.left, this.right, this.front);
        }

        public static DecoratedPotBlockEntity.Decoration load(@Nullable NBTTagCompound nbttagcompound) {
            if (nbttagcompound != null && nbttagcompound.contains("sherds", 9)) {
                NBTTagList nbttaglist = nbttagcompound.getList("sherds", 8);

                return new DecoratedPotBlockEntity.Decoration(itemFromTag(nbttaglist, 0), itemFromTag(nbttaglist, 1), itemFromTag(nbttaglist, 2), itemFromTag(nbttaglist, 3));
            } else {
                return DecoratedPotBlockEntity.Decoration.EMPTY;
            }
        }

        private static Item itemFromTag(NBTTagList nbttaglist, int i) {
            if (i >= nbttaglist.size()) {
                return Items.BRICK;
            } else {
                NBTBase nbtbase = nbttaglist.get(i);

                return (Item) BuiltInRegistries.ITEM.get(MinecraftKey.tryParse(nbtbase.getAsString()));
            }
        }
    }
}
