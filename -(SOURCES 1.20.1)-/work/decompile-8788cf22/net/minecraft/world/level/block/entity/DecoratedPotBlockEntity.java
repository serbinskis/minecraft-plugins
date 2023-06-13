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
    public DecoratedPotBlockEntity.a decorations;

    public DecoratedPotBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.DECORATED_POT, blockposition, iblockdata);
        this.decorations = DecoratedPotBlockEntity.a.EMPTY;
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound) {
        super.saveAdditional(nbttagcompound);
        this.decorations.save(nbttagcompound);
    }

    @Override
    public void load(NBTTagCompound nbttagcompound) {
        super.load(nbttagcompound);
        this.decorations = DecoratedPotBlockEntity.a.load(nbttagcompound);
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

    public DecoratedPotBlockEntity.a getDecorations() {
        return this.decorations;
    }

    public void setFromItem(ItemStack itemstack) {
        this.decorations = DecoratedPotBlockEntity.a.load(ItemBlock.getBlockEntityData(itemstack));
    }

    public static record a(Item back, Item left, Item right, Item front) {

        public static final DecoratedPotBlockEntity.a EMPTY = new DecoratedPotBlockEntity.a(Items.BRICK, Items.BRICK, Items.BRICK, Items.BRICK);

        public NBTTagCompound save(NBTTagCompound nbttagcompound) {
            NBTTagList nbttaglist = new NBTTagList();

            this.sorted().forEach((item) -> {
                nbttaglist.add(NBTTagString.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
            });
            nbttagcompound.put("sherds", nbttaglist);
            return nbttagcompound;
        }

        public Stream<Item> sorted() {
            return Stream.of(this.back, this.left, this.right, this.front);
        }

        public static DecoratedPotBlockEntity.a load(@Nullable NBTTagCompound nbttagcompound) {
            if (nbttagcompound != null && nbttagcompound.contains("sherds", 9)) {
                NBTTagList nbttaglist = nbttagcompound.getList("sherds", 8);

                return new DecoratedPotBlockEntity.a(itemFromTag(nbttaglist, 0), itemFromTag(nbttaglist, 1), itemFromTag(nbttaglist, 2), itemFromTag(nbttaglist, 3));
            } else {
                return DecoratedPotBlockEntity.a.EMPTY;
            }
        }

        private static Item itemFromTag(NBTTagList nbttaglist, int i) {
            if (i >= nbttaglist.size()) {
                return Items.BRICK;
            } else {
                NBTBase nbtbase = nbttaglist.get(i);

                return (Item) BuiltInRegistries.ITEM.get(new MinecraftKey(nbtbase.getAsString()));
            }
        }
    }
}
