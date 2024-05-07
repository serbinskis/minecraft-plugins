package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.Clearable;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemRecord;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockJukeBox;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.ticks.ContainerSingleItem;

// CraftBukkit start
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
// CraftBukkit end

public class TileEntityJukeBox extends TileEntity implements Clearable, ContainerSingleItem.a {

    private static final int SONG_END_PADDING = 20;
    private ItemStack item;
    private int ticksSinceLastEvent;
    public long tickCount;
    public long recordStartedTick;
    public boolean isPlaying;
    // CraftBukkit start - add fields and methods
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;
    public boolean opened;

    @Override
    public List<ItemStack> getContents() {
        return Collections.singletonList(item);
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public void setMaxStackSize(int size) {
        maxStack = size;
    }

    @Override
    public Location getLocation() {
        if (level == null) return null;
        return new org.bukkit.Location(level.getWorld(), worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
    }
    // CraftBukkit end

    public TileEntityJukeBox(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.JUKEBOX, blockposition, iblockdata);
        this.item = ItemStack.EMPTY;
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        if (nbttagcompound.contains("RecordItem", 10)) {
            this.item = (ItemStack) ItemStack.parse(holderlookup_a, nbttagcompound.getCompound("RecordItem")).orElse(ItemStack.EMPTY);
        } else {
            this.item = ItemStack.EMPTY;
        }

        this.isPlaying = nbttagcompound.getBoolean("IsPlaying");
        this.recordStartedTick = nbttagcompound.getLong("RecordStartTick");
        this.tickCount = nbttagcompound.getLong("TickCount");
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        if (!this.getTheItem().isEmpty()) {
            nbttagcompound.put("RecordItem", this.getTheItem().save(holderlookup_a));
        }

        nbttagcompound.putBoolean("IsPlaying", this.isPlaying);
        nbttagcompound.putLong("RecordStartTick", this.recordStartedTick);
        nbttagcompound.putLong("TickCount", this.tickCount);
    }

    public boolean isRecordPlaying() {
        return !this.getTheItem().isEmpty() && this.isPlaying;
    }

    private void setHasRecordBlockState(@Nullable Entity entity, boolean flag) {
        if (this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.setBlock(this.getBlockPos(), (IBlockData) this.getBlockState().setValue(BlockJukeBox.HAS_RECORD, flag), 2);
            this.level.gameEvent((Holder) GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.a.of(entity, this.getBlockState()));
        }

    }

    @VisibleForTesting
    public void startPlaying() {
        this.recordStartedTick = this.tickCount;
        this.isPlaying = true;
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.level.levelEvent((EntityHuman) null, 1010, this.getBlockPos(), Item.getId(this.getTheItem().getItem()));
        this.setChanged();
    }

    private void stopPlaying() {
        this.isPlaying = false;
        this.level.gameEvent((Holder) GameEvent.JUKEBOX_STOP_PLAY, this.getBlockPos(), GameEvent.a.of(this.getBlockState()));
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.level.levelEvent(1011, this.getBlockPos(), 0);
        this.setChanged();
    }

    private void tick(World world, BlockPosition blockposition, IBlockData iblockdata) {
        ++this.ticksSinceLastEvent;
        if (this.isRecordPlaying()) {
            Item item = this.getTheItem().getItem();

            if (item instanceof ItemRecord) {
                ItemRecord itemrecord = (ItemRecord) item;

                if (this.shouldRecordStopPlaying(itemrecord)) {
                    this.stopPlaying();
                } else if (this.shouldSendJukeboxPlayingEvent()) {
                    this.ticksSinceLastEvent = 0;
                    world.gameEvent((Holder) GameEvent.JUKEBOX_PLAY, blockposition, GameEvent.a.of(iblockdata));
                    this.spawnMusicParticles(world, blockposition);
                }
            }
        }

        ++this.tickCount;
    }

    private boolean shouldRecordStopPlaying(ItemRecord itemrecord) {
        return this.tickCount >= this.recordStartedTick + (long) itemrecord.getLengthInTicks() + 20L;
    }

    private boolean shouldSendJukeboxPlayingEvent() {
        return this.ticksSinceLastEvent >= 20;
    }

    @Override
    public ItemStack getTheItem() {
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int i) {
        ItemStack itemstack = this.item;

        this.item = ItemStack.EMPTY;
        if (!itemstack.isEmpty()) {
            this.setHasRecordBlockState((Entity) null, false);
            this.stopPlaying();
        }

        return itemstack;
    }

    @Override
    public void setTheItem(ItemStack itemstack) {
        if (itemstack.is(TagsItem.MUSIC_DISCS) && this.level != null) {
            this.item = itemstack;
            this.setHasRecordBlockState((Entity) null, true);
            this.startPlaying();
        } else if (itemstack.isEmpty()) {
            this.splitTheItem(1);
        }

    }

    @Override
    public int getMaxStackSize() {
        return maxStack; // CraftBukkit
    }

    @Override
    public TileEntity getContainerBlockEntity() {
        return this;
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemstack) {
        return itemstack.is(TagsItem.MUSIC_DISCS) && this.getItem(i).isEmpty();
    }

    @Override
    public boolean canTakeItem(IInventory iinventory, int i, ItemStack itemstack) {
        return iinventory.hasAnyMatching(ItemStack::isEmpty);
    }

    private void spawnMusicParticles(World world, BlockPosition blockposition) {
        if (world instanceof WorldServer worldserver) {
            Vec3D vec3d = Vec3D.atBottomCenterOf(blockposition).add(0.0D, 1.2000000476837158D, 0.0D);
            float f = (float) world.getRandom().nextInt(4) / 24.0F;

            worldserver.sendParticles(Particles.NOTE, vec3d.x(), vec3d.y(), vec3d.z(), 0, (double) f, 0.0D, 0.0D, 1.0D);
        }

    }

    public void popOutRecord() {
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

    public static void playRecordTick(World world, BlockPosition blockposition, IBlockData iblockdata, TileEntityJukeBox tileentityjukebox) {
        tileentityjukebox.tick(world, blockposition, iblockdata);
    }

    @VisibleForTesting
    public void setRecordWithoutPlaying(ItemStack itemstack) {
        this.item = itemstack;
        // CraftBukkit start - add null check for level
        if (level != null) {
            this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        }
        // CraftBukkit end
        this.setChanged();
    }
}
