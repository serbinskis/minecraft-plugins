package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.PathfinderGoalCrossbowAttack;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalLookAtPlayer;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomStroll;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalHurtByTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.animal.EntityIronGolem;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.npc.EntityVillagerAbstract;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.raid.EntityRaider;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemBanner;
import net.minecraft.world.item.ItemProjectileWeapon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;

public class EntityPillager extends EntityIllagerAbstract implements ICrossbow, InventoryCarrier {

    private static final DataWatcherObject<Boolean> IS_CHARGING_CROSSBOW = DataWatcher.defineId(EntityPillager.class, DataWatcherRegistry.BOOLEAN);
    private static final int INVENTORY_SIZE = 5;
    private static final int SLOT_OFFSET = 300;
    public final InventorySubcontainer inventory = new InventorySubcontainer(5);

    public EntityPillager(EntityTypes<? extends EntityPillager> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new PathfinderGoalFloat(this));
        this.goalSelector.addGoal(2, new EntityRaider.a(this, 10.0F));
        this.goalSelector.addGoal(3, new PathfinderGoalCrossbowAttack<>(this, 1.0D, 8.0F));
        this.goalSelector.addGoal(8, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.addGoal(9, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 15.0F));
        this.targetSelector.addGoal(1, (new PathfinderGoalHurtByTarget(this, new Class[]{EntityRaider.class})).setAlertOthers());
        this.targetSelector.addGoal(2, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true));
        this.targetSelector.addGoal(3, new PathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false));
        this.targetSelector.addGoal(3, new PathfinderGoalNearestAttackableTarget<>(this, EntityIronGolem.class, true));
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityMonster.createMonsterAttributes().add(GenericAttributes.MOVEMENT_SPEED, 0.3499999940395355D).add(GenericAttributes.MAX_HEALTH, 24.0D).add(GenericAttributes.ATTACK_DAMAGE, 5.0D).add(GenericAttributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityPillager.IS_CHARGING_CROSSBOW, false);
    }

    @Override
    public boolean canFireProjectileWeapon(ItemProjectileWeapon itemprojectileweapon) {
        return itemprojectileweapon == Items.CROSSBOW;
    }

    public boolean isChargingCrossbow() {
        return (Boolean) this.entityData.get(EntityPillager.IS_CHARGING_CROSSBOW);
    }

    @Override
    public void setChargingCrossbow(boolean flag) {
        this.entityData.set(EntityPillager.IS_CHARGING_CROSSBOW, flag);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        this.writeInventoryToTag(nbttagcompound, this.registryAccess());
    }

    @Override
    public EntityIllagerAbstract.a getArmPose() {
        return this.isChargingCrossbow() ? EntityIllagerAbstract.a.CROSSBOW_CHARGE : (this.isHolding(Items.CROSSBOW) ? EntityIllagerAbstract.a.CROSSBOW_HOLD : (this.isAggressive() ? EntityIllagerAbstract.a.ATTACKING : EntityIllagerAbstract.a.NEUTRAL));
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.readInventoryFromTag(nbttagcompound, this.registryAccess());
        this.setCanPickUpLoot(true);
    }

    @Override
    public float getWalkTargetValue(BlockPosition blockposition, IWorldReader iworldreader) {
        return 0.0F;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity) {
        RandomSource randomsource = worldaccess.getRandom();

        this.populateDefaultEquipmentSlots(randomsource, difficultydamagescaler);
        this.populateDefaultEquipmentEnchantments(worldaccess, randomsource, difficultydamagescaler);
        return super.finalizeSpawn(worldaccess, difficultydamagescaler, enummobspawn, groupdataentity);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomsource, DifficultyDamageScaler difficultydamagescaler) {
        this.setItemSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
    }

    @Override
    protected void enchantSpawnedWeapon(WorldAccess worldaccess, RandomSource randomsource, DifficultyDamageScaler difficultydamagescaler) {
        super.enchantSpawnedWeapon(worldaccess, randomsource, difficultydamagescaler);
        if (randomsource.nextInt(300) == 0) {
            ItemStack itemstack = this.getMainHandItem();

            if (itemstack.is(Items.CROSSBOW)) {
                EnchantmentManager.enchantItemFromProvider(itemstack, worldaccess.registryAccess(), VanillaEnchantmentProviders.PILLAGER_SPAWN_CROSSBOW, difficultydamagescaler, randomsource);
            }
        }

    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.PILLAGER_DEATH;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.PILLAGER_HURT;
    }

    @Override
    public void performRangedAttack(EntityLiving entityliving, float f) {
        this.performCrossbowAttack(this, 1.6F);
    }

    @Override
    public InventorySubcontainer getInventory() {
        return this.inventory;
    }

    @Override
    protected void pickUpItem(EntityItem entityitem) {
        ItemStack itemstack = entityitem.getItem();

        if (itemstack.getItem() instanceof ItemBanner) {
            super.pickUpItem(entityitem);
        } else if (this.wantsItem(itemstack)) {
            this.onItemPickup(entityitem);
            ItemStack itemstack1 = this.inventory.addItem(itemstack);

            if (itemstack1.isEmpty()) {
                entityitem.discard();
            } else {
                itemstack.setCount(itemstack1.getCount());
            }
        }

    }

    private boolean wantsItem(ItemStack itemstack) {
        return this.hasActiveRaid() && itemstack.is(Items.WHITE_BANNER);
    }

    @Override
    public SlotAccess getSlot(int i) {
        int j = i - 300;

        return j >= 0 && j < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, j) : super.getSlot(i);
    }

    @Override
    public void applyRaidBuffs(WorldServer worldserver, int i, boolean flag) {
        Raid raid = this.getCurrentRaid();
        boolean flag1 = this.random.nextFloat() <= raid.getEnchantOdds();

        if (flag1) {
            ItemStack itemstack = new ItemStack(Items.CROSSBOW);
            ResourceKey resourcekey;

            if (i > raid.getNumGroups(EnumDifficulty.NORMAL)) {
                resourcekey = VanillaEnchantmentProviders.RAID_PILLAGER_POST_WAVE_5;
            } else if (i > raid.getNumGroups(EnumDifficulty.EASY)) {
                resourcekey = VanillaEnchantmentProviders.RAID_PILLAGER_POST_WAVE_3;
            } else {
                resourcekey = null;
            }

            if (resourcekey != null) {
                EnchantmentManager.enchantItemFromProvider(itemstack, worldserver.registryAccess(), resourcekey, worldserver.getCurrentDifficultyAt(this.blockPosition()), this.getRandom());
                this.setItemSlot(EnumItemSlot.MAINHAND, itemstack);
            }
        }

    }

    @Override
    public SoundEffect getCelebrateSound() {
        return SoundEffects.PILLAGER_CELEBRATE;
    }
}
