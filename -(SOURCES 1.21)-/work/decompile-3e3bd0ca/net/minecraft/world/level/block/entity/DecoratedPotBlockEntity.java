package net.minecraft.world.level.block.entity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.ticks.ContainerSingleItem;

public class DecoratedPotBlockEntity extends TileEntity implements RandomizableContainer, ContainerSingleItem.a {

    public static final String TAG_SHERDS = "sherds";
    public static final String TAG_ITEM = "item";
    public static final int EVENT_POT_WOBBLES = 1;
    public long wobbleStartedAtTick;
    @Nullable
    public DecoratedPotBlockEntity.a lastWobbleStyle;
    public PotDecorations decorations;
    private ItemStack item;
    @Nullable
    protected ResourceKey<LootTable> lootTable;
    protected long lootTableSeed;

    public DecoratedPotBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.DECORATED_POT, blockposition, iblockdata);
        this.item = ItemStack.EMPTY;
        this.decorations = PotDecorations.EMPTY;
    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        this.decorations.save(nbttagcompound);
        if (!this.trySaveLootTable(nbttagcompound) && !this.item.isEmpty()) {
            nbttagcompound.put("item", this.item.save(holderlookup_a));
        }

    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        this.decorations = PotDecorations.load(nbttagcompound);
        if (!this.tryLoadLootTable(nbttagcompound)) {
            if (nbttagcompound.contains("item", 10)) {
                this.item = (ItemStack) ItemStack.parse(holderlookup_a, nbttagcompound.getCompound("item")).orElse(ItemStack.EMPTY);
            } else {
                this.item = ItemStack.EMPTY;
            }
        }

    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        return this.saveCustomOnly(holderlookup_a);
    }

    public EnumDirection getDirection() {
        return (EnumDirection) this.getBlockState().getValue(BlockProperties.HORIZONTAL_FACING);
    }

    public PotDecorations getDecorations() {
        return this.decorations;
    }

    public void setFromItem(ItemStack itemstack) {
        this.applyComponentsFromItemStack(itemstack);
    }

    public ItemStack getPotAsItem() {
        ItemStack itemstack = Items.DECORATED_POT.getDefaultInstance();

        itemstack.applyComponents(this.collectComponents());
        return itemstack;
    }

    public static ItemStack createDecoratedPotItem(PotDecorations potdecorations) {
        ItemStack itemstack = Items.DECORATED_POT.getDefaultInstance();

        itemstack.set(DataComponents.POT_DECORATIONS, potdecorations);
        return itemstack;
    }

    @Nullable
    @Override
    public ResourceKey<LootTable> getLootTable() {
        return this.lootTable;
    }

    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> resourcekey) {
        this.lootTable = resourcekey;
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long i) {
        this.lootTableSeed = i;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.a datacomponentmap_a) {
        super.collectImplicitComponents(datacomponentmap_a);
        datacomponentmap_a.set(DataComponents.POT_DECORATIONS, this.decorations);
        datacomponentmap_a.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(this.item)));
    }

    @Override
    protected void applyImplicitComponents(TileEntity.b tileentity_b) {
        super.applyImplicitComponents(tileentity_b);
        this.decorations = (PotDecorations) tileentity_b.getOrDefault(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY);
        this.item = ((ItemContainerContents) tileentity_b.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)).copyOne();
    }

    @Override
    public void removeComponentsFromTag(NBTTagCompound nbttagcompound) {
        super.removeComponentsFromTag(nbttagcompound);
        nbttagcompound.remove("sherds");
        nbttagcompound.remove("item");
    }

    @Override
    public ItemStack getTheItem() {
        this.unpackLootTable((EntityHuman) null);
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int i) {
        this.unpackLootTable((EntityHuman) null);
        ItemStack itemstack = this.item.split(i);

        if (this.item.isEmpty()) {
            this.item = ItemStack.EMPTY;
        }

        return itemstack;
    }

    @Override
    public void setTheItem(ItemStack itemstack) {
        this.unpackLootTable((EntityHuman) null);
        this.item = itemstack;
    }

    @Override
    public TileEntity getContainerBlockEntity() {
        return this;
    }

    public void wobble(DecoratedPotBlockEntity.a decoratedpotblockentity_a) {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 1, decoratedpotblockentity_a.ordinal());
        }
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (this.level != null && i == 1 && j >= 0 && j < DecoratedPotBlockEntity.a.values().length) {
            this.wobbleStartedAtTick = this.level.getGameTime();
            this.lastWobbleStyle = DecoratedPotBlockEntity.a.values()[j];
            return true;
        } else {
            return super.triggerEvent(i, j);
        }
    }

    public static enum a {

        POSITIVE(7), NEGATIVE(10);

        public final int duration;

        private a(final int i) {
            this.duration = i;
        }
    }
}
