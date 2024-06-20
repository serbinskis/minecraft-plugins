package net.minecraft.world.entity.animal.horse;

import java.util.Objects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeModifiable;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Blocks;

public abstract class EntityHorseChestedAbstract extends EntityHorseAbstract {

    private static final DataWatcherObject<Boolean> DATA_ID_CHEST = DataWatcher.defineId(EntityHorseChestedAbstract.class, DataWatcherRegistry.BOOLEAN);
    private final EntitySize babyDimensions;

    protected EntityHorseChestedAbstract(EntityTypes<? extends EntityHorseChestedAbstract> entitytypes, World world) {
        super(entitytypes, world);
        this.canGallop = false;
        this.babyDimensions = entitytypes.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0F, entitytypes.getHeight() - 0.15625F, 0.0F)).scale(0.5F);
    }

    @Override
    protected void randomizeAttributes(RandomSource randomsource) {
        AttributeModifiable attributemodifiable = this.getAttribute(GenericAttributes.MAX_HEALTH);

        Objects.requireNonNull(randomsource);
        attributemodifiable.setBaseValue((double) generateMaxHealth(randomsource::nextInt));
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityHorseChestedAbstract.DATA_ID_CHEST, false);
    }

    public static AttributeProvider.Builder createBaseChestedHorseAttributes() {
        return createBaseHorseAttributes().add(GenericAttributes.MOVEMENT_SPEED, 0.17499999701976776D).add(GenericAttributes.JUMP_STRENGTH, 0.5D);
    }

    public boolean hasChest() {
        return (Boolean) this.entityData.get(EntityHorseChestedAbstract.DATA_ID_CHEST);
    }

    public void setChest(boolean flag) {
        this.entityData.set(EntityHorseChestedAbstract.DATA_ID_CHEST, flag);
    }

    @Override
    public EntitySize getDefaultDimensions(EntityPose entitypose) {
        return this.isBaby() ? this.babyDimensions : super.getDefaultDimensions(entitypose);
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.hasChest()) {
            if (!this.level().isClientSide) {
                this.spawnAtLocation((IMaterial) Blocks.CHEST);
            }

            this.setChest(false);
        }

    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putBoolean("ChestedHorse", this.hasChest());
        if (this.hasChest()) {
            NBTTagList nbttaglist = new NBTTagList();

            for (int i = 1; i < this.inventory.getContainerSize(); ++i) {
                ItemStack itemstack = this.inventory.getItem(i);

                if (!itemstack.isEmpty()) {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                    nbttagcompound1.putByte("Slot", (byte) (i - 1));
                    nbttaglist.add(itemstack.save(this.registryAccess(), nbttagcompound1));
                }
            }

            nbttagcompound.put("Items", nbttaglist);
        }

    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.setChest(nbttagcompound.getBoolean("ChestedHorse"));
        this.createInventory();
        if (this.hasChest()) {
            NBTTagList nbttaglist = nbttagcompound.getList("Items", 10);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompound(i);
                int j = nbttagcompound1.getByte("Slot") & 255;

                if (j < this.inventory.getContainerSize() - 1) {
                    this.inventory.setItem(j + 1, (ItemStack) ItemStack.parse(this.registryAccess(), nbttagcompound1).orElse(ItemStack.EMPTY));
                }
            }
        }

        this.syncSaddleToClients();
    }

    @Override
    public SlotAccess getSlot(int i) {
        return i == 499 ? new SlotAccess() {
            @Override
            public ItemStack get() {
                return EntityHorseChestedAbstract.this.hasChest() ? new ItemStack(Items.CHEST) : ItemStack.EMPTY;
            }

            @Override
            public boolean set(ItemStack itemstack) {
                if (itemstack.isEmpty()) {
                    if (EntityHorseChestedAbstract.this.hasChest()) {
                        EntityHorseChestedAbstract.this.setChest(false);
                        EntityHorseChestedAbstract.this.createInventory();
                    }

                    return true;
                } else if (itemstack.is(Items.CHEST)) {
                    if (!EntityHorseChestedAbstract.this.hasChest()) {
                        EntityHorseChestedAbstract.this.setChest(true);
                        EntityHorseChestedAbstract.this.createInventory();
                    }

                    return true;
                } else {
                    return false;
                }
            }
        } : super.getSlot(i);
    }

    @Override
    public EnumInteractionResult mobInteract(EntityHuman entityhuman, EnumHand enumhand) {
        boolean flag = !this.isBaby() && this.isTamed() && entityhuman.isSecondaryUseActive();

        if (!this.isVehicle() && !flag) {
            ItemStack itemstack = entityhuman.getItemInHand(enumhand);

            if (!itemstack.isEmpty()) {
                if (this.isFood(itemstack)) {
                    return this.fedFood(entityhuman, itemstack);
                }

                if (!this.isTamed()) {
                    this.makeMad();
                    return EnumInteractionResult.sidedSuccess(this.level().isClientSide);
                }

                if (!this.hasChest() && itemstack.is(Items.CHEST)) {
                    this.equipChest(entityhuman, itemstack);
                    return EnumInteractionResult.sidedSuccess(this.level().isClientSide);
                }
            }

            return super.mobInteract(entityhuman, enumhand);
        } else {
            return super.mobInteract(entityhuman, enumhand);
        }
    }

    private void equipChest(EntityHuman entityhuman, ItemStack itemstack) {
        this.setChest(true);
        this.playChestEquipsSound();
        itemstack.consume(1, entityhuman);
        this.createInventory();
    }

    protected void playChestEquipsSound() {
        this.playSound(SoundEffects.DONKEY_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
    }

    @Override
    public int getInventoryColumns() {
        return this.hasChest() ? 5 : 0;
    }
}
