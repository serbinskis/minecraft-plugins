package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.Clearable;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockJukeBox;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.ticks.ContainerSingleItem;

public class TileEntityJukeBox extends TileEntity implements Clearable, ContainerSingleItem.a {

    public static final String SONG_ITEM_TAG_ID = "RecordItem";
    public static final String TICKS_SINCE_SONG_STARTED_TAG_ID = "ticks_since_song_started";
    private ItemStack item;
    private final JukeboxSongPlayer jukeboxSongPlayer;

    public TileEntityJukeBox(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.JUKEBOX, blockposition, iblockdata);
        this.item = ItemStack.EMPTY;
        this.jukeboxSongPlayer = new JukeboxSongPlayer(this::onSongChanged, this.getBlockPos());
    }

    public JukeboxSongPlayer getSongPlayer() {
        return this.jukeboxSongPlayer;
    }

    public void onSongChanged() {
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.setChanged();
    }

    private void notifyItemChangedInJukebox(boolean flag) {
        if (this.level != null && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.setBlock(this.getBlockPos(), (IBlockData) this.getBlockState().setValue(BlockJukeBox.HAS_RECORD, flag), 2);
            this.level.gameEvent((Holder) GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.a.of(this.getBlockState()));
        }
    }

    public void popOutTheItem() {
        if (this.level != null && !this.level.isClientSide) {
            BlockPosition blockposition = this.getBlockPos();
            ItemStack itemstack = this.getTheItem();

            if (!itemstack.isEmpty()) {
                this.removeTheItem();
                Vec3D vec3d = Vec3D.atLowerCornerWithOffset(blockposition, 0.5D, 1.01D, 0.5D).offsetRandom(this.level.random, 0.7F);
                ItemStack itemstack1 = itemstack.copy();
                EntityItem entityitem = new EntityItem(this.level, vec3d.x(), vec3d.y(), vec3d.z(), itemstack1);

                entityitem.setDefaultPickUpDelay();
                this.level.addFreshEntity(entityitem);
            }
        }
    }

    public static void tick(World world, BlockPosition blockposition, IBlockData iblockdata, TileEntityJukeBox tileentityjukebox) {
        tileentityjukebox.jukeboxSongPlayer.tick(world, iblockdata);
    }

    public int getComparatorOutput() {
        return (Integer) JukeboxSong.fromStack(this.level.registryAccess(), this.item).map(Holder::value).map(JukeboxSong::comparatorOutput).orElse(0);
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        if (nbttagcompound.contains("RecordItem", 10)) {
            this.item = (ItemStack) ItemStack.parse(holderlookup_a, nbttagcompound.getCompound("RecordItem")).orElse(ItemStack.EMPTY);
        } else {
            this.item = ItemStack.EMPTY;
        }

        if (nbttagcompound.contains("ticks_since_song_started", 4)) {
            JukeboxSong.fromStack(holderlookup_a, this.item).ifPresent((holder) -> {
                this.jukeboxSongPlayer.setSongWithoutPlaying(holder, nbttagcompound.getLong("ticks_since_song_started"));
            });
        }

    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        if (!this.getTheItem().isEmpty()) {
            nbttagcompound.put("RecordItem", this.getTheItem().save(holderlookup_a));
        }

        if (this.jukeboxSongPlayer.getSong() != null) {
            nbttagcompound.putLong("ticks_since_song_started", this.jukeboxSongPlayer.getTicksSinceSongStarted());
        }

    }

    @Override
    public ItemStack getTheItem() {
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int i) {
        ItemStack itemstack = this.item;

        this.setTheItem(ItemStack.EMPTY);
        return itemstack;
    }

    @Override
    public void setTheItem(ItemStack itemstack) {
        this.item = itemstack;
        boolean flag = !this.item.isEmpty();
        Optional<Holder<JukeboxSong>> optional = JukeboxSong.fromStack(this.level.registryAccess(), this.item);

        this.notifyItemChangedInJukebox(flag);
        if (flag && optional.isPresent()) {
            this.jukeboxSongPlayer.play(this.level, (Holder) optional.get());
        } else {
            this.jukeboxSongPlayer.stop(this.level, this.getBlockState());
        }

    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public TileEntity getContainerBlockEntity() {
        return this;
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemstack) {
        return itemstack.has(DataComponents.JUKEBOX_PLAYABLE) && this.getItem(i).isEmpty();
    }

    @Override
    public boolean canTakeItem(IInventory iinventory, int i, ItemStack itemstack) {
        return iinventory.hasAnyMatching(ItemStack::isEmpty);
    }

    @VisibleForTesting
    public void setSongItemWithoutPlaying(ItemStack itemstack) {
        this.item = itemstack;
        JukeboxSong.fromStack(this.level.registryAccess(), itemstack).ifPresent((holder) -> {
            this.jukeboxSongPlayer.setSongWithoutPlaying(holder, 0L);
        });
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.setChanged();
    }

    @VisibleForTesting
    public void tryForcePlaySong() {
        JukeboxSong.fromStack(this.level.registryAccess(), this.getTheItem()).ifPresent((holder) -> {
            this.jukeboxSongPlayer.play(this.level, holder);
        });
    }
}
