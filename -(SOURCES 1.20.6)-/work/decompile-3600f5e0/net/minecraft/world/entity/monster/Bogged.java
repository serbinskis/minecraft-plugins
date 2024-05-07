package net.minecraft.world.entity.monster;

import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.IShearable;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.EntityTippedArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public class Bogged extends EntitySkeletonAbstract implements IShearable {

    private static final int HARD_ATTACK_INTERVAL = 50;
    private static final int NORMAL_ATTACK_INTERVAL = 70;
    private static final DataWatcherObject<Boolean> DATA_SHEARED = DataWatcher.defineId(Bogged.class, DataWatcherRegistry.BOOLEAN);
    public static final String SHEARED_TAG_NAME = "sheared";

    public static AttributeProvider.Builder createAttributes() {
        return EntitySkeletonAbstract.createAttributes().add(GenericAttributes.MAX_HEALTH, 16.0D);
    }

    public Bogged(EntityTypes<? extends Bogged> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(Bogged.DATA_SHEARED, false);
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putBoolean("sheared", this.isSheared());
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.setSheared(nbttagcompound.getBoolean("sheared"));
    }

    public boolean isSheared() {
        return (Boolean) this.entityData.get(Bogged.DATA_SHEARED);
    }

    public void setSheared(boolean flag) {
        this.entityData.set(Bogged.DATA_SHEARED, flag);
    }

    @Override
    protected EnumInteractionResult mobInteract(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (itemstack.is(Items.SHEARS) && this.readyForShearing()) {
            this.shear(SoundCategory.PLAYERS);
            this.gameEvent(GameEvent.SHEAR, entityhuman);
            if (!this.level().isClientSide) {
                itemstack.hurtAndBreak(1, entityhuman, getSlotForHand(enumhand));
            }

            return EnumInteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            return super.mobInteract(entityhuman, enumhand);
        }
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.BOGGED_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.BOGGED_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.BOGGED_DEATH;
    }

    @Override
    protected SoundEffect getStepSound() {
        return SoundEffects.BOGGED_STEP;
    }

    @Override
    protected EntityArrow getArrow(ItemStack itemstack, float f) {
        EntityArrow entityarrow = super.getArrow(itemstack, f);

        if (entityarrow instanceof EntityTippedArrow entitytippedarrow) {
            entitytippedarrow.addEffect(new MobEffect(MobEffects.POISON, 100));
        }

        return entityarrow;
    }

    @Override
    protected int getHardAttackInterval() {
        return 50;
    }

    @Override
    protected int getAttackInterval() {
        return 70;
    }

    @Override
    public void shear(SoundCategory soundcategory) {
        this.level().playSound((EntityHuman) null, (Entity) this, SoundEffects.BOGGED_SHEAR, soundcategory, 1.0F, 1.0F);
        this.spawnShearedMushrooms();
        this.setSheared(true);
    }

    private void spawnShearedMushrooms() {
        World world = this.level();

        if (world instanceof WorldServer worldserver) {
            if (worldserver.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                LootTable loottable = worldserver.getServer().reloadableRegistries().getLootTable(LootTables.BOGGED_SHEAR);
                LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.ORIGIN, this.position()).withParameter(LootContextParameters.THIS_ENTITY, this).create(LootContextParameterSets.SHEARING);
                ObjectListIterator objectlistiterator = loottable.getRandomItems(lootparams).iterator();

                while (objectlistiterator.hasNext()) {
                    ItemStack itemstack = (ItemStack) objectlistiterator.next();

                    this.spawnAtLocation(itemstack);
                }
            }
        }

    }

    @Override
    public boolean readyForShearing() {
        return !this.isSheared() && this.isAlive();
    }
}
