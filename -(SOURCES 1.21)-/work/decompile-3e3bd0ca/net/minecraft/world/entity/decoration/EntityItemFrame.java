package net.minecraft.world.entity.decoration;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.EntityTrackerEntry;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemWorldMap;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockDiodeAbstract;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.apache.commons.lang3.Validate;

public class EntityItemFrame extends EntityHanging {

    public static final DataWatcherObject<ItemStack> DATA_ITEM = DataWatcher.defineId(EntityItemFrame.class, DataWatcherRegistry.ITEM_STACK);
    public static final DataWatcherObject<Integer> DATA_ROTATION = DataWatcher.defineId(EntityItemFrame.class, DataWatcherRegistry.INT);
    public static final int NUM_ROTATIONS = 8;
    private static final float DEPTH = 0.0625F;
    private static final float WIDTH = 0.75F;
    private static final float HEIGHT = 0.75F;
    public float dropChance;
    public boolean fixed;

    public EntityItemFrame(EntityTypes<? extends EntityItemFrame> entitytypes, World world) {
        super(entitytypes, world);
        this.dropChance = 1.0F;
    }

    public EntityItemFrame(World world, BlockPosition blockposition, EnumDirection enumdirection) {
        this(EntityTypes.ITEM_FRAME, world, blockposition, enumdirection);
    }

    public EntityItemFrame(EntityTypes<? extends EntityItemFrame> entitytypes, World world, BlockPosition blockposition, EnumDirection enumdirection) {
        super(entitytypes, world, blockposition);
        this.dropChance = 1.0F;
        this.setDirection(enumdirection);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityItemFrame.DATA_ITEM, ItemStack.EMPTY);
        datawatcher_a.define(EntityItemFrame.DATA_ROTATION, 0);
    }

    @Override
    public void setDirection(EnumDirection enumdirection) {
        Validate.notNull(enumdirection);
        this.direction = enumdirection;
        if (enumdirection.getAxis().isHorizontal()) {
            this.setXRot(0.0F);
            this.setYRot((float) (this.direction.get2DDataValue() * 90));
        } else {
            this.setXRot((float) (-90 * enumdirection.getAxisDirection().getStep()));
            this.setYRot(0.0F);
        }

        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    protected AxisAlignedBB calculateBoundingBox(BlockPosition blockposition, EnumDirection enumdirection) {
        float f = 0.46875F;
        Vec3D vec3d = Vec3D.atCenterOf(blockposition).relative(enumdirection, -0.46875D);
        EnumDirection.EnumAxis enumdirection_enumaxis = enumdirection.getAxis();
        double d0 = enumdirection_enumaxis == EnumDirection.EnumAxis.X ? 0.0625D : 0.75D;
        double d1 = enumdirection_enumaxis == EnumDirection.EnumAxis.Y ? 0.0625D : 0.75D;
        double d2 = enumdirection_enumaxis == EnumDirection.EnumAxis.Z ? 0.0625D : 0.75D;

        return AxisAlignedBB.ofSize(vec3d, d0, d1, d2);
    }

    @Override
    public boolean survives() {
        if (this.fixed) {
            return true;
        } else if (!this.level().noCollision((Entity) this)) {
            return false;
        } else {
            IBlockData iblockdata = this.level().getBlockState(this.pos.relative(this.direction.getOpposite()));

            return !iblockdata.isSolid() && (!this.direction.getAxis().isHorizontal() || !BlockDiodeAbstract.isDiode(iblockdata)) ? false : this.level().getEntities((Entity) this, this.getBoundingBox(), EntityItemFrame.HANGING_ENTITY).isEmpty();
        }
    }

    @Override
    public void move(EnumMoveType enummovetype, Vec3D vec3d) {
        if (!this.fixed) {
            super.move(enummovetype, vec3d);
        }

    }

    @Override
    public void push(double d0, double d1, double d2) {
        if (!this.fixed) {
            super.push(d0, d1, d2);
        }

    }

    @Override
    public void kill() {
        this.removeFramedMap(this.getItem());
        super.kill();
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        if (this.fixed) {
            return !damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !damagesource.isCreativePlayer() ? false : super.hurt(damagesource, f);
        } else if (this.isInvulnerableTo(damagesource)) {
            return false;
        } else if (!damagesource.is(DamageTypeTags.IS_EXPLOSION) && !this.getItem().isEmpty()) {
            if (!this.level().isClientSide) {
                this.dropItem(damagesource.getEntity(), false);
                this.gameEvent(GameEvent.BLOCK_CHANGE, damagesource.getEntity());
                this.playSound(this.getRemoveItemSound(), 1.0F, 1.0F);
            }

            return true;
        } else {
            return super.hurt(damagesource, f);
        }
    }

    public SoundEffect getRemoveItemSound() {
        return SoundEffects.ITEM_FRAME_REMOVE_ITEM;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d0) {
        double d1 = 16.0D;

        d1 *= 64.0D * getViewScale();
        return d0 < d1 * d1;
    }

    @Override
    public void dropItem(@Nullable Entity entity) {
        this.playSound(this.getBreakSound(), 1.0F, 1.0F);
        this.dropItem(entity, true);
        this.gameEvent(GameEvent.BLOCK_CHANGE, entity);
    }

    public SoundEffect getBreakSound() {
        return SoundEffects.ITEM_FRAME_BREAK;
    }

    @Override
    public void playPlacementSound() {
        this.playSound(this.getPlaceSound(), 1.0F, 1.0F);
    }

    public SoundEffect getPlaceSound() {
        return SoundEffects.ITEM_FRAME_PLACE;
    }

    private void dropItem(@Nullable Entity entity, boolean flag) {
        if (!this.fixed) {
            ItemStack itemstack = this.getItem();

            this.setItem(ItemStack.EMPTY);
            if (!this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                if (entity == null) {
                    this.removeFramedMap(itemstack);
                }

            } else {
                if (entity instanceof EntityHuman) {
                    EntityHuman entityhuman = (EntityHuman) entity;

                    if (entityhuman.hasInfiniteMaterials()) {
                        this.removeFramedMap(itemstack);
                        return;
                    }
                }

                if (flag) {
                    this.spawnAtLocation(this.getFrameItemStack());
                }

                if (!itemstack.isEmpty()) {
                    itemstack = itemstack.copy();
                    this.removeFramedMap(itemstack);
                    if (this.random.nextFloat() < this.dropChance) {
                        this.spawnAtLocation(itemstack);
                    }
                }

            }
        }
    }

    private void removeFramedMap(ItemStack itemstack) {
        MapId mapid = this.getFramedMapId(itemstack);

        if (mapid != null) {
            WorldMap worldmap = ItemWorldMap.getSavedData(mapid, this.level());

            if (worldmap != null) {
                worldmap.removedFromFrame(this.pos, this.getId());
                worldmap.setDirty(true);
            }
        }

        itemstack.setEntityRepresentation((Entity) null);
    }

    public ItemStack getItem() {
        return (ItemStack) this.getEntityData().get(EntityItemFrame.DATA_ITEM);
    }

    @Nullable
    public MapId getFramedMapId(ItemStack itemstack) {
        return (MapId) itemstack.get(DataComponents.MAP_ID);
    }

    public boolean hasFramedMap() {
        return this.getItem().has(DataComponents.MAP_ID);
    }

    public void setItem(ItemStack itemstack) {
        this.setItem(itemstack, true);
    }

    public void setItem(ItemStack itemstack, boolean flag) {
        if (!itemstack.isEmpty()) {
            itemstack = itemstack.copyWithCount(1);
        }

        this.onItemChanged(itemstack);
        this.getEntityData().set(EntityItemFrame.DATA_ITEM, itemstack);
        if (!itemstack.isEmpty()) {
            this.playSound(this.getAddItemSound(), 1.0F, 1.0F);
        }

        if (flag && this.pos != null) {
            this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }

    }

    public SoundEffect getAddItemSound() {
        return SoundEffects.ITEM_FRAME_ADD_ITEM;
    }

    @Override
    public SlotAccess getSlot(int i) {
        return i == 0 ? SlotAccess.of(this::getItem, this::setItem) : super.getSlot(i);
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        if (datawatcherobject.equals(EntityItemFrame.DATA_ITEM)) {
            this.onItemChanged(this.getItem());
        }

    }

    private void onItemChanged(ItemStack itemstack) {
        if (!itemstack.isEmpty() && itemstack.getFrame() != this) {
            itemstack.setEntityRepresentation(this);
        }

        this.recalculateBoundingBox();
    }

    public int getRotation() {
        return (Integer) this.getEntityData().get(EntityItemFrame.DATA_ROTATION);
    }

    public void setRotation(int i) {
        this.setRotation(i, true);
    }

    private void setRotation(int i, boolean flag) {
        this.getEntityData().set(EntityItemFrame.DATA_ROTATION, i % 8);
        if (flag && this.pos != null) {
            this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }

    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        if (!this.getItem().isEmpty()) {
            nbttagcompound.put("Item", this.getItem().save(this.registryAccess()));
            nbttagcompound.putByte("ItemRotation", (byte) this.getRotation());
            nbttagcompound.putFloat("ItemDropChance", this.dropChance);
        }

        nbttagcompound.putByte("Facing", (byte) this.direction.get3DDataValue());
        nbttagcompound.putBoolean("Invisible", this.isInvisible());
        nbttagcompound.putBoolean("Fixed", this.fixed);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        ItemStack itemstack;

        if (nbttagcompound.contains("Item", 10)) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Item");

            itemstack = (ItemStack) ItemStack.parse(this.registryAccess(), nbttagcompound1).orElse(ItemStack.EMPTY);
        } else {
            itemstack = ItemStack.EMPTY;
        }

        ItemStack itemstack1 = this.getItem();

        if (!itemstack1.isEmpty() && !ItemStack.matches(itemstack, itemstack1)) {
            this.removeFramedMap(itemstack1);
        }

        this.setItem(itemstack, false);
        if (!itemstack.isEmpty()) {
            this.setRotation(nbttagcompound.getByte("ItemRotation"), false);
            if (nbttagcompound.contains("ItemDropChance", 99)) {
                this.dropChance = nbttagcompound.getFloat("ItemDropChance");
            }
        }

        this.setDirection(EnumDirection.from3DDataValue(nbttagcompound.getByte("Facing")));
        this.setInvisible(nbttagcompound.getBoolean("Invisible"));
        this.fixed = nbttagcompound.getBoolean("Fixed");
    }

    @Override
    public EnumInteractionResult interact(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        boolean flag = !this.getItem().isEmpty();
        boolean flag1 = !itemstack.isEmpty();

        if (this.fixed) {
            return EnumInteractionResult.PASS;
        } else if (!this.level().isClientSide) {
            if (!flag) {
                if (flag1 && !this.isRemoved()) {
                    if (itemstack.is(Items.FILLED_MAP)) {
                        WorldMap worldmap = ItemWorldMap.getSavedData(itemstack, this.level());

                        if (worldmap != null && worldmap.isTrackedCountOverLimit(256)) {
                            return EnumInteractionResult.FAIL;
                        }
                    }

                    this.setItem(itemstack);
                    this.gameEvent(GameEvent.BLOCK_CHANGE, entityhuman);
                    itemstack.consume(1, entityhuman);
                }
            } else {
                this.playSound(this.getRotateItemSound(), 1.0F, 1.0F);
                this.setRotation(this.getRotation() + 1);
                this.gameEvent(GameEvent.BLOCK_CHANGE, entityhuman);
            }

            return EnumInteractionResult.CONSUME;
        } else {
            return !flag && !flag1 ? EnumInteractionResult.PASS : EnumInteractionResult.SUCCESS;
        }
    }

    public SoundEffect getRotateItemSound() {
        return SoundEffects.ITEM_FRAME_ROTATE_ITEM;
    }

    public int getAnalogOutput() {
        return this.getItem().isEmpty() ? 0 : this.getRotation() % 8 + 1;
    }

    @Override
    public Packet<PacketListenerPlayOut> getAddEntityPacket(EntityTrackerEntry entitytrackerentry) {
        return new PacketPlayOutSpawnEntity(this, this.direction.get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(PacketPlayOutSpawnEntity packetplayoutspawnentity) {
        super.recreateFromPacket(packetplayoutspawnentity);
        this.setDirection(EnumDirection.from3DDataValue(packetplayoutspawnentity.getData()));
    }

    @Override
    public ItemStack getPickResult() {
        ItemStack itemstack = this.getItem();

        return itemstack.isEmpty() ? this.getFrameItemStack() : itemstack.copy();
    }

    protected ItemStack getFrameItemStack() {
        return new ItemStack(Items.ITEM_FRAME);
    }

    @Override
    public float getVisualRotationYInDegrees() {
        EnumDirection enumdirection = this.getDirection();
        int i = enumdirection.getAxis().isVertical() ? 90 * enumdirection.getAxisDirection().getStep() : 0;

        return (float) MathHelper.wrapDegrees(180 + enumdirection.get2DDataValue() * 90 + this.getRotation() * 45 + i);
    }
}
