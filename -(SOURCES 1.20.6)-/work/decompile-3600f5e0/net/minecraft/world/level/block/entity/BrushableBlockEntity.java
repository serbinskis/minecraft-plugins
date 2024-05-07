package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public class BrushableBlockEntity extends TileEntity {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LOOT_TABLE_TAG = "LootTable";
    private static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
    private static final String HIT_DIRECTION_TAG = "hit_direction";
    private static final String ITEM_TAG = "item";
    private static final int BRUSH_COOLDOWN_TICKS = 10;
    private static final int BRUSH_RESET_TICKS = 40;
    private static final int REQUIRED_BRUSHES_TO_BREAK = 10;
    private int brushCount;
    private long brushCountResetsAtTick;
    private long coolDownEndsAtTick;
    public ItemStack item;
    @Nullable
    private EnumDirection hitDirection;
    @Nullable
    public ResourceKey<LootTable> lootTable;
    public long lootTableSeed;

    public BrushableBlockEntity(BlockPosition blockposition, IBlockData iblockdata) {
        super(TileEntityTypes.BRUSHABLE_BLOCK, blockposition, iblockdata);
        this.item = ItemStack.EMPTY;
    }

    public boolean brush(long i, EntityHuman entityhuman, EnumDirection enumdirection) {
        if (this.hitDirection == null) {
            this.hitDirection = enumdirection;
        }

        this.brushCountResetsAtTick = i + 40L;
        if (i >= this.coolDownEndsAtTick && this.level instanceof WorldServer) {
            this.coolDownEndsAtTick = i + 10L;
            this.unpackLootTable(entityhuman);
            int j = this.getCompletionState();

            if (++this.brushCount >= 10) {
                this.brushingCompleted(entityhuman);
                return true;
            } else {
                this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 2);
                int k = this.getCompletionState();

                if (j != k) {
                    IBlockData iblockdata = this.getBlockState();
                    IBlockData iblockdata1 = (IBlockData) iblockdata.setValue(BlockProperties.DUSTED, k);

                    this.level.setBlock(this.getBlockPos(), iblockdata1, 3);
                }

                return false;
            }
        } else {
            return false;
        }
    }

    public void unpackLootTable(EntityHuman entityhuman) {
        if (this.lootTable != null && this.level != null && !this.level.isClientSide() && this.level.getServer() != null) {
            LootTable loottable = this.level.getServer().reloadableRegistries().getLootTable(this.lootTable);

            if (entityhuman instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entityhuman;

                CriterionTriggers.GENERATE_LOOT.trigger(entityplayer, this.lootTable);
            }

            LootParams lootparams = (new LootParams.a((WorldServer) this.level)).withParameter(LootContextParameters.ORIGIN, Vec3D.atCenterOf(this.worldPosition)).withLuck(entityhuman.getLuck()).withParameter(LootContextParameters.THIS_ENTITY, entityhuman).create(LootContextParameterSets.CHEST);
            ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams, this.lootTableSeed);
            ItemStack itemstack;

            switch (objectarraylist.size()) {
                case 0:
                    itemstack = ItemStack.EMPTY;
                    break;
                case 1:
                    itemstack = (ItemStack) objectarraylist.get(0);
                    break;
                default:
                    BrushableBlockEntity.LOGGER.warn("Expected max 1 loot from loot table {}, but got {}", this.lootTable.location(), objectarraylist.size());
                    itemstack = (ItemStack) objectarraylist.get(0);
            }

            this.item = itemstack;
            this.lootTable = null;
            this.setChanged();
        }
    }

    private void brushingCompleted(EntityHuman entityhuman) {
        if (this.level != null && this.level.getServer() != null) {
            this.dropContent(entityhuman);
            IBlockData iblockdata = this.getBlockState();

            this.level.levelEvent(3008, this.getBlockPos(), Block.getId(iblockdata));
            Block block = this.getBlockState().getBlock();
            Block block1;

            if (block instanceof BrushableBlock) {
                BrushableBlock brushableblock = (BrushableBlock) block;

                block1 = brushableblock.getTurnsInto();
            } else {
                block1 = Blocks.AIR;
            }

            this.level.setBlock(this.worldPosition, block1.defaultBlockState(), 3);
        }
    }

    private void dropContent(EntityHuman entityhuman) {
        if (this.level != null && this.level.getServer() != null) {
            this.unpackLootTable(entityhuman);
            if (!this.item.isEmpty()) {
                double d0 = (double) EntityTypes.ITEM.getWidth();
                double d1 = 1.0D - d0;
                double d2 = d0 / 2.0D;
                EnumDirection enumdirection = (EnumDirection) Objects.requireNonNullElse(this.hitDirection, EnumDirection.UP);
                BlockPosition blockposition = this.worldPosition.relative(enumdirection, 1);
                double d3 = (double) blockposition.getX() + 0.5D * d1 + d2;
                double d4 = (double) blockposition.getY() + 0.5D + (double) (EntityTypes.ITEM.getHeight() / 2.0F);
                double d5 = (double) blockposition.getZ() + 0.5D * d1 + d2;
                EntityItem entityitem = new EntityItem(this.level, d3, d4, d5, this.item.split(this.level.random.nextInt(21) + 10));

                entityitem.setDeltaMovement(Vec3D.ZERO);
                this.level.addFreshEntity(entityitem);
                this.item = ItemStack.EMPTY;
            }

        }
    }

    public void checkReset() {
        if (this.level != null) {
            if (this.brushCount != 0 && this.level.getGameTime() >= this.brushCountResetsAtTick) {
                int i = this.getCompletionState();

                this.brushCount = Math.max(0, this.brushCount - 2);
                int j = this.getCompletionState();

                if (i != j) {
                    this.level.setBlock(this.getBlockPos(), (IBlockData) this.getBlockState().setValue(BlockProperties.DUSTED, j), 3);
                }

                boolean flag = true;

                this.brushCountResetsAtTick = this.level.getGameTime() + 4L;
            }

            if (this.brushCount == 0) {
                this.hitDirection = null;
                this.brushCountResetsAtTick = 0L;
                this.coolDownEndsAtTick = 0L;
            } else {
                this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 2);
            }

        }
    }

    private boolean tryLoadLootTable(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.contains("LootTable", 8)) {
            this.lootTable = ResourceKey.create(Registries.LOOT_TABLE, new MinecraftKey(nbttagcompound.getString("LootTable")));
            this.lootTableSeed = nbttagcompound.getLong("LootTableSeed");
            return true;
        } else {
            return false;
        }
    }

    private boolean trySaveLootTable(NBTTagCompound nbttagcompound) {
        if (this.lootTable == null) {
            return false;
        } else {
            nbttagcompound.putString("LootTable", this.lootTable.location().toString());
            if (this.lootTableSeed != 0L) {
                nbttagcompound.putLong("LootTableSeed", this.lootTableSeed);
            }

            return true;
        }
    }

    @Override
    public NBTTagCompound getUpdateTag(HolderLookup.a holderlookup_a) {
        NBTTagCompound nbttagcompound = super.getUpdateTag(holderlookup_a);

        if (this.hitDirection != null) {
            nbttagcompound.putInt("hit_direction", this.hitDirection.ordinal());
        }

        if (!this.item.isEmpty()) {
            nbttagcompound.put("item", this.item.save(holderlookup_a));
        }

        return nbttagcompound;
    }

    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return PacketPlayOutTileEntityData.create(this);
    }

    @Override
    protected void loadAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.loadAdditional(nbttagcompound, holderlookup_a);
        if (!this.tryLoadLootTable(nbttagcompound) && nbttagcompound.contains("item")) {
            this.item = (ItemStack) ItemStack.parse(holderlookup_a, nbttagcompound.getCompound("item")).orElse(ItemStack.EMPTY);
        } else {
            this.item = ItemStack.EMPTY;
        }

        if (nbttagcompound.contains("hit_direction")) {
            this.hitDirection = EnumDirection.values()[nbttagcompound.getInt("hit_direction")];
        }

    }

    @Override
    protected void saveAdditional(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        super.saveAdditional(nbttagcompound, holderlookup_a);
        if (!this.trySaveLootTable(nbttagcompound) && !this.item.isEmpty()) {
            nbttagcompound.put("item", this.item.save(holderlookup_a));
        }

    }

    public void setLootTable(ResourceKey<LootTable> resourcekey, long i) {
        this.lootTable = resourcekey;
        this.lootTableSeed = i;
    }

    private int getCompletionState() {
        return this.brushCount == 0 ? 0 : (this.brushCount < 3 ? 1 : (this.brushCount < 6 ? 2 : 3));
    }

    @Nullable
    public EnumDirection getHitDirection() {
        return this.hitDirection;
    }

    public ItemStack getItem() {
        return this.item;
    }
}
