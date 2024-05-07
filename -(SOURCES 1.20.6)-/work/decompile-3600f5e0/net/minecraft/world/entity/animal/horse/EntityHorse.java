package net.minecraft.world.entity.animal.horse;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.IInventory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifiable;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.SoundEffectType;

public class EntityHorse extends EntityHorseAbstract implements VariantHolder<HorseColor> {

    private static final DataWatcherObject<Integer> DATA_ID_TYPE_VARIANT = DataWatcher.defineId(EntityHorse.class, DataWatcherRegistry.INT);
    private static final EntitySize BABY_DIMENSIONS = EntityTypes.HORSE.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0F, EntityTypes.HORSE.getHeight() + 0.125F, 0.0F)).scale(0.5F);

    public EntityHorse(EntityTypes<? extends EntityHorse> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    protected void randomizeAttributes(RandomSource randomsource) {
        AttributeModifiable attributemodifiable = this.getAttribute(GenericAttributes.MAX_HEALTH);

        Objects.requireNonNull(randomsource);
        attributemodifiable.setBaseValue((double) generateMaxHealth(randomsource::nextInt));
        attributemodifiable = this.getAttribute(GenericAttributes.MOVEMENT_SPEED);
        Objects.requireNonNull(randomsource);
        attributemodifiable.setBaseValue(generateSpeed(randomsource::nextDouble));
        attributemodifiable = this.getAttribute(GenericAttributes.JUMP_STRENGTH);
        Objects.requireNonNull(randomsource);
        attributemodifiable.setBaseValue(generateJumpStrength(randomsource::nextDouble));
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityHorse.DATA_ID_TYPE_VARIANT, 0);
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putInt("Variant", this.getTypeVariant());
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.setTypeVariant(nbttagcompound.getInt("Variant"));
    }

    private void setTypeVariant(int i) {
        this.entityData.set(EntityHorse.DATA_ID_TYPE_VARIANT, i);
    }

    private int getTypeVariant() {
        return (Integer) this.entityData.get(EntityHorse.DATA_ID_TYPE_VARIANT);
    }

    public void setVariantAndMarkings(HorseColor horsecolor, HorseStyle horsestyle) {
        this.setTypeVariant(horsecolor.getId() & 255 | horsestyle.getId() << 8 & '\uff00');
    }

    @Override
    public HorseColor getVariant() {
        return HorseColor.byId(this.getTypeVariant() & 255);
    }

    public void setVariant(HorseColor horsecolor) {
        this.setTypeVariant(horsecolor.getId() & 255 | this.getTypeVariant() & -256);
    }

    public HorseStyle getMarkings() {
        return HorseStyle.byId((this.getTypeVariant() & '\uff00') >> 8);
    }

    @Override
    public void containerChanged(IInventory iinventory) {
        ItemStack itemstack = this.getBodyArmorItem();

        super.containerChanged(iinventory);
        ItemStack itemstack1 = this.getBodyArmorItem();

        if (this.tickCount > 20 && this.isBodyArmorItem(itemstack1) && itemstack != itemstack1) {
            this.playSound(SoundEffects.HORSE_ARMOR, 0.5F, 1.0F);
        }

    }

    @Override
    protected void playGallopSound(SoundEffectType soundeffecttype) {
        super.playGallopSound(soundeffecttype);
        if (this.random.nextInt(10) == 0) {
            this.playSound(SoundEffects.HORSE_BREATHE, soundeffecttype.getVolume() * 0.6F, soundeffecttype.getPitch());
        }

    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.HORSE_AMBIENT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.HORSE_DEATH;
    }

    @Nullable
    @Override
    protected SoundEffect getEatingSound() {
        return SoundEffects.HORSE_EAT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.HORSE_HURT;
    }

    @Override
    protected SoundEffect getAngrySound() {
        return SoundEffects.HORSE_ANGRY;
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
            }

            return super.mobInteract(entityhuman, enumhand);
        } else {
            return super.mobInteract(entityhuman, enumhand);
        }
    }

    @Override
    public boolean canMate(EntityAnimal entityanimal) {
        return entityanimal == this ? false : (!(entityanimal instanceof EntityHorseDonkey) && !(entityanimal instanceof EntityHorse) ? false : this.canParent() && ((EntityHorseAbstract) entityanimal).canParent());
    }

    @Nullable
    @Override
    public EntityAgeable getBreedOffspring(WorldServer worldserver, EntityAgeable entityageable) {
        if (entityageable instanceof EntityHorseDonkey) {
            EntityHorseMule entityhorsemule = (EntityHorseMule) EntityTypes.MULE.create(worldserver);

            if (entityhorsemule != null) {
                this.setOffspringAttributes(entityageable, entityhorsemule);
            }

            return entityhorsemule;
        } else {
            EntityHorse entityhorse = (EntityHorse) entityageable;
            EntityHorse entityhorse1 = (EntityHorse) EntityTypes.HORSE.create(worldserver);

            if (entityhorse1 != null) {
                int i = this.random.nextInt(9);
                HorseColor horsecolor;

                if (i < 4) {
                    horsecolor = this.getVariant();
                } else if (i < 8) {
                    horsecolor = entityhorse.getVariant();
                } else {
                    horsecolor = (HorseColor) SystemUtils.getRandom((Object[]) HorseColor.values(), this.random);
                }

                int j = this.random.nextInt(5);
                HorseStyle horsestyle;

                if (j < 2) {
                    horsestyle = this.getMarkings();
                } else if (j < 4) {
                    horsestyle = entityhorse.getMarkings();
                } else {
                    horsestyle = (HorseStyle) SystemUtils.getRandom((Object[]) HorseStyle.values(), this.random);
                }

                entityhorse1.setVariantAndMarkings(horsecolor, horsestyle);
                this.setOffspringAttributes(entityageable, entityhorse1);
            }

            return entityhorse1;
        }
    }

    @Override
    public boolean canWearBodyArmor() {
        return true;
    }

    @Override
    public boolean isBodyArmorItem(ItemStack itemstack) {
        Item item = itemstack.getItem();
        boolean flag;

        if (item instanceof AnimalArmorItem animalarmoritem) {
            if (animalarmoritem.getBodyType() == AnimalArmorItem.a.EQUESTRIAN) {
                flag = true;
                return flag;
            }
        }

        flag = false;
        return flag;
    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity) {
        RandomSource randomsource = worldaccess.getRandom();
        HorseColor horsecolor;

        if (groupdataentity instanceof EntityHorse.a) {
            horsecolor = ((EntityHorse.a) groupdataentity).variant;
        } else {
            horsecolor = (HorseColor) SystemUtils.getRandom((Object[]) HorseColor.values(), randomsource);
            groupdataentity = new EntityHorse.a(horsecolor);
        }

        this.setVariantAndMarkings(horsecolor, (HorseStyle) SystemUtils.getRandom((Object[]) HorseStyle.values(), randomsource));
        return super.finalizeSpawn(worldaccess, difficultydamagescaler, enummobspawn, (GroupDataEntity) groupdataentity);
    }

    @Override
    public EntitySize getDefaultDimensions(EntityPose entitypose) {
        return this.isBaby() ? EntityHorse.BABY_DIMENSIONS : super.getDefaultDimensions(entitypose);
    }

    public static class a extends EntityAgeable.a {

        public final HorseColor variant;

        public a(HorseColor horsecolor) {
            super(true);
            this.variant = horsecolor;
        }
    }
}
