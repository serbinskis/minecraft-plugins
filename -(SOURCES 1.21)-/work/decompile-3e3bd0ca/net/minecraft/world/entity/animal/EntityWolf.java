package net.minecraft.world.entity.animal;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleParamItem;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeRange;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTameableAnimal;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.IEntityAngerable;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.PathfinderGoalAvoidTarget;
import net.minecraft.world.entity.ai.goal.PathfinderGoalBeg;
import net.minecraft.world.entity.ai.goal.PathfinderGoalBreed;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFollowOwner;
import net.minecraft.world.entity.ai.goal.PathfinderGoalLeapAtTarget;
import net.minecraft.world.entity.ai.goal.PathfinderGoalLookAtPlayer;
import net.minecraft.world.entity.ai.goal.PathfinderGoalMeleeAttack;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomLookaround;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomStrollLand;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSit;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalHurtByTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalOwnerHurtByTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalOwnerHurtTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalRandomTargetNonTamed;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalUniversalAngerReset;
import net.minecraft.world.entity.animal.horse.EntityHorseAbstract;
import net.minecraft.world.entity.animal.horse.EntityLlama;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.monster.EntityCreeper;
import net.minecraft.world.entity.monster.EntityGhast;
import net.minecraft.world.entity.monster.EntitySkeletonAbstract;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.food.FoodInfo;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.EnumArmorMaterial;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDye;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3D;

public class EntityWolf extends EntityTameableAnimal implements IEntityAngerable, VariantHolder<Holder<WolfVariant>> {

    private static final DataWatcherObject<Boolean> DATA_INTERESTED_ID = DataWatcher.defineId(EntityWolf.class, DataWatcherRegistry.BOOLEAN);
    private static final DataWatcherObject<Integer> DATA_COLLAR_COLOR = DataWatcher.defineId(EntityWolf.class, DataWatcherRegistry.INT);
    private static final DataWatcherObject<Integer> DATA_REMAINING_ANGER_TIME = DataWatcher.defineId(EntityWolf.class, DataWatcherRegistry.INT);
    private static final DataWatcherObject<Holder<WolfVariant>> DATA_VARIANT_ID = DataWatcher.defineId(EntityWolf.class, DataWatcherRegistry.WOLF_VARIANT);
    public static final Predicate<EntityLiving> PREY_SELECTOR = (entityliving) -> {
        EntityTypes<?> entitytypes = entityliving.getType();

        return entitytypes == EntityTypes.SHEEP || entitytypes == EntityTypes.RABBIT || entitytypes == EntityTypes.FOX;
    };
    private static final float START_HEALTH = 8.0F;
    private static final float TAME_HEALTH = 40.0F;
    private static final float ARMOR_REPAIR_UNIT = 0.125F;
    private float interestedAngle;
    private float interestedAngleO;
    private boolean isWet;
    private boolean isShaking;
    private float shakeAnim;
    private float shakeAnimO;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeRange.rangeOfSeconds(20, 39);
    @Nullable
    private UUID persistentAngerTarget;

    public EntityWolf(EntityTypes<? extends EntityWolf> entitytypes, World world) {
        super(entitytypes, world);
        this.setTame(false, false);
        this.setPathfindingMalus(PathType.POWDER_SNOW, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, -1.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PathfinderGoalFloat(this));
        this.goalSelector.addGoal(1, new EntityTameableAnimal.a(1.5D, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.addGoal(2, new PathfinderGoalSit(this));
        this.goalSelector.addGoal(3, new EntityWolf.a<>(this, EntityLlama.class, 24.0F, 1.5D, 1.5D));
        this.goalSelector.addGoal(4, new PathfinderGoalLeapAtTarget(this, 0.4F));
        this.goalSelector.addGoal(5, new PathfinderGoalMeleeAttack(this, 1.0D, true));
        this.goalSelector.addGoal(6, new PathfinderGoalFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.goalSelector.addGoal(7, new PathfinderGoalBreed(this, 1.0D));
        this.goalSelector.addGoal(8, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.addGoal(9, new PathfinderGoalBeg(this, 8.0F));
        this.goalSelector.addGoal(10, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.addGoal(10, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.addGoal(1, new PathfinderGoalOwnerHurtByTarget(this));
        this.targetSelector.addGoal(2, new PathfinderGoalOwnerHurtTarget(this));
        this.targetSelector.addGoal(3, (new PathfinderGoalHurtByTarget(this, new Class[0])).setAlertOthers());
        this.targetSelector.addGoal(4, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new PathfinderGoalRandomTargetNonTamed<>(this, EntityAnimal.class, false, EntityWolf.PREY_SELECTOR));
        this.targetSelector.addGoal(6, new PathfinderGoalRandomTargetNonTamed<>(this, EntityTurtle.class, false, EntityTurtle.BABY_ON_LAND_SELECTOR));
        this.targetSelector.addGoal(7, new PathfinderGoalNearestAttackableTarget<>(this, EntitySkeletonAbstract.class, false));
        this.targetSelector.addGoal(8, new PathfinderGoalUniversalAngerReset<>(this, true));
    }

    public MinecraftKey getTexture() {
        WolfVariant wolfvariant = (WolfVariant) this.getVariant().value();

        return this.isTame() ? wolfvariant.tameTexture() : (this.isAngry() ? wolfvariant.angryTexture() : wolfvariant.wildTexture());
    }

    @Override
    public Holder<WolfVariant> getVariant() {
        return (Holder) this.entityData.get(EntityWolf.DATA_VARIANT_ID);
    }

    public void setVariant(Holder<WolfVariant> holder) {
        this.entityData.set(EntityWolf.DATA_VARIANT_ID, holder);
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityInsentient.createMobAttributes().add(GenericAttributes.MOVEMENT_SPEED, 0.30000001192092896D).add(GenericAttributes.MAX_HEALTH, 8.0D).add(GenericAttributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        IRegistryCustom iregistrycustom = this.registryAccess();
        IRegistry<WolfVariant> iregistry = iregistrycustom.registryOrThrow(Registries.WOLF_VARIANT);
        DataWatcherObject datawatcherobject = EntityWolf.DATA_VARIANT_ID;
        Optional optional = iregistry.getHolder(WolfVariants.DEFAULT);

        Objects.requireNonNull(iregistry);
        datawatcher_a.define(datawatcherobject, (Holder) optional.or(iregistry::getAny).orElseThrow());
        datawatcher_a.define(EntityWolf.DATA_INTERESTED_ID, false);
        datawatcher_a.define(EntityWolf.DATA_COLLAR_COLOR, EnumColor.RED.getId());
        datawatcher_a.define(EntityWolf.DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(SoundEffects.WOLF_STEP, 0.15F, 1.0F);
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putByte("CollarColor", (byte) this.getCollarColor().getId());
        this.getVariant().unwrapKey().ifPresent((resourcekey) -> {
            nbttagcompound.putString("variant", resourcekey.location().toString());
        });
        this.addPersistentAngerSaveData(nbttagcompound);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        Optional.ofNullable(MinecraftKey.tryParse(nbttagcompound.getString("variant"))).map((minecraftkey) -> {
            return ResourceKey.create(Registries.WOLF_VARIANT, minecraftkey);
        }).flatMap((resourcekey) -> {
            return this.registryAccess().registryOrThrow(Registries.WOLF_VARIANT).getHolder(resourcekey);
        }).ifPresent(this::setVariant);
        if (nbttagcompound.contains("CollarColor", 99)) {
            this.setCollarColor(EnumColor.byId(nbttagcompound.getInt("CollarColor")));
        }

        this.readPersistentAngerSaveData(this.level(), nbttagcompound);
    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity) {
        Holder<BiomeBase> holder = worldaccess.getBiome(this.blockPosition());
        Holder holder1;

        if (groupdataentity instanceof EntityWolf.b entitywolf_b) {
            holder1 = entitywolf_b.type;
        } else {
            holder1 = WolfVariants.getSpawnVariant(this.registryAccess(), holder);
            groupdataentity = new EntityWolf.b(holder1);
        }

        this.setVariant(holder1);
        return super.finalizeSpawn(worldaccess, difficultydamagescaler, enummobspawn, (GroupDataEntity) groupdataentity);
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return this.isAngry() ? SoundEffects.WOLF_GROWL : (this.random.nextInt(3) == 0 ? (this.isTame() && this.getHealth() < 20.0F ? SoundEffects.WOLF_WHINE : SoundEffects.WOLF_PANT) : SoundEffects.WOLF_AMBIENT);
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return this.canArmorAbsorb(damagesource) ? SoundEffects.WOLF_ARMOR_DAMAGE : SoundEffects.WOLF_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.WOLF_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.isWet && !this.isShaking && !this.isPathFinding() && this.onGround()) {
            this.isShaking = true;
            this.shakeAnim = 0.0F;
            this.shakeAnimO = 0.0F;
            this.level().broadcastEntityEvent(this, (byte) 8);
        }

        if (!this.level().isClientSide) {
            this.updatePersistentAnger((WorldServer) this.level(), true);
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (this.isAlive()) {
            this.interestedAngleO = this.interestedAngle;
            if (this.isInterested()) {
                this.interestedAngle += (1.0F - this.interestedAngle) * 0.4F;
            } else {
                this.interestedAngle += (0.0F - this.interestedAngle) * 0.4F;
            }

            if (this.isInWaterRainOrBubble()) {
                this.isWet = true;
                if (this.isShaking && !this.level().isClientSide) {
                    this.level().broadcastEntityEvent(this, (byte) 56);
                    this.cancelShake();
                }
            } else if ((this.isWet || this.isShaking) && this.isShaking) {
                if (this.shakeAnim == 0.0F) {
                    this.playSound(SoundEffects.WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                    this.gameEvent(GameEvent.ENTITY_ACTION);
                }

                this.shakeAnimO = this.shakeAnim;
                this.shakeAnim += 0.05F;
                if (this.shakeAnimO >= 2.0F) {
                    this.isWet = false;
                    this.isShaking = false;
                    this.shakeAnimO = 0.0F;
                    this.shakeAnim = 0.0F;
                }

                if (this.shakeAnim > 0.4F) {
                    float f = (float) this.getY();
                    int i = (int) (MathHelper.sin((this.shakeAnim - 0.4F) * 3.1415927F) * 7.0F);
                    Vec3D vec3d = this.getDeltaMovement();

                    for (int j = 0; j < i; ++j) {
                        float f1 = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
                        float f2 = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;

                        this.level().addParticle(Particles.SPLASH, this.getX() + (double) f1, (double) (f + 0.8F), this.getZ() + (double) f2, vec3d.x, vec3d.y, vec3d.z);
                    }
                }
            }

        }
    }

    private void cancelShake() {
        this.isShaking = false;
        this.shakeAnim = 0.0F;
        this.shakeAnimO = 0.0F;
    }

    @Override
    public void die(DamageSource damagesource) {
        this.isWet = false;
        this.isShaking = false;
        this.shakeAnimO = 0.0F;
        this.shakeAnim = 0.0F;
        super.die(damagesource);
    }

    public boolean isWet() {
        return this.isWet;
    }

    public float getWetShade(float f) {
        return Math.min(0.75F + MathHelper.lerp(f, this.shakeAnimO, this.shakeAnim) / 2.0F * 0.25F, 1.0F);
    }

    public float getBodyRollAngle(float f, float f1) {
        float f2 = (MathHelper.lerp(f, this.shakeAnimO, this.shakeAnim) + f1) / 1.8F;

        if (f2 < 0.0F) {
            f2 = 0.0F;
        } else if (f2 > 1.0F) {
            f2 = 1.0F;
        }

        return MathHelper.sin(f2 * 3.1415927F) * MathHelper.sin(f2 * 3.1415927F * 11.0F) * 0.15F * 3.1415927F;
    }

    public float getHeadRollAngle(float f) {
        return MathHelper.lerp(f, this.interestedAngleO, this.interestedAngle) * 0.15F * 3.1415927F;
    }

    @Override
    public int getMaxHeadXRot() {
        return this.isInSittingPose() ? 20 : super.getMaxHeadXRot();
    }

    @Override
    public boolean hurt(DamageSource damagesource, float f) {
        if (this.isInvulnerableTo(damagesource)) {
            return false;
        } else {
            if (!this.level().isClientSide) {
                this.setOrderedToSit(false);
            }

            return super.hurt(damagesource, f);
        }
    }

    @Override
    public boolean canUseSlot(EnumItemSlot enumitemslot) {
        return true;
    }

    @Override
    protected void actuallyHurt(DamageSource damagesource, float f) {
        if (!this.canArmorAbsorb(damagesource)) {
            super.actuallyHurt(damagesource, f);
        } else {
            ItemStack itemstack = this.getBodyArmorItem();
            int i = itemstack.getDamageValue();
            int j = itemstack.getMaxDamage();

            itemstack.hurtAndBreak(MathHelper.ceil(f), this, EnumItemSlot.BODY);
            if (Crackiness.WOLF_ARMOR.byDamage(i, j) != Crackiness.WOLF_ARMOR.byDamage(this.getBodyArmorItem())) {
                this.playSound(SoundEffects.WOLF_ARMOR_CRACK);
                World world = this.level();

                if (world instanceof WorldServer) {
                    WorldServer worldserver = (WorldServer) world;

                    worldserver.sendParticles(new ParticleParamItem(Particles.ITEM, Items.ARMADILLO_SCUTE.getDefaultInstance()), this.getX(), this.getY() + 1.0D, this.getZ(), 20, 0.2D, 0.1D, 0.2D, 0.1D);
                }
            }

        }
    }

    private boolean canArmorAbsorb(DamageSource damagesource) {
        return this.hasArmor() && !damagesource.is(DamageTypeTags.BYPASSES_WOLF_ARMOR);
    }

    @Override
    protected void applyTamingSideEffects() {
        if (this.isTame()) {
            this.getAttribute(GenericAttributes.MAX_HEALTH).setBaseValue(40.0D);
            this.setHealth(40.0F);
        } else {
            this.getAttribute(GenericAttributes.MAX_HEALTH).setBaseValue(8.0D);
        }

    }

    @Override
    protected void hurtArmor(DamageSource damagesource, float f) {
        this.doHurtEquipment(damagesource, f, new EnumItemSlot[]{EnumItemSlot.BODY});
    }

    @Override
    public EnumInteractionResult mobInteract(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        Item item = itemstack.getItem();

        if (this.level().isClientSide && (!this.isBaby() || !this.isFood(itemstack))) {
            boolean flag = this.isOwnedBy(entityhuman) || this.isTame() || itemstack.is(Items.BONE) && !this.isTame() && !this.isAngry();

            return flag ? EnumInteractionResult.CONSUME : EnumInteractionResult.PASS;
        } else if (this.isTame()) {
            if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                itemstack.consume(1, entityhuman);
                FoodInfo foodinfo = (FoodInfo) itemstack.get(DataComponents.FOOD);
                float f = foodinfo != null ? (float) foodinfo.nutrition() : 1.0F;

                this.heal(2.0F * f);
                return EnumInteractionResult.sidedSuccess(this.level().isClientSide());
            } else {
                if (item instanceof ItemDye) {
                    ItemDye itemdye = (ItemDye) item;

                    if (this.isOwnedBy(entityhuman)) {
                        EnumColor enumcolor = itemdye.getDyeColor();

                        if (enumcolor != this.getCollarColor()) {
                            this.setCollarColor(enumcolor);
                            itemstack.consume(1, entityhuman);
                            return EnumInteractionResult.SUCCESS;
                        }

                        return super.mobInteract(entityhuman, enumhand);
                    }
                }

                if (itemstack.is(Items.WOLF_ARMOR) && this.isOwnedBy(entityhuman) && this.getBodyArmorItem().isEmpty() && !this.isBaby()) {
                    this.setBodyArmorItem(itemstack.copyWithCount(1));
                    itemstack.consume(1, entityhuman);
                    return EnumInteractionResult.SUCCESS;
                } else {
                    ItemStack itemstack1;

                    if (itemstack.is(Items.SHEARS) && this.isOwnedBy(entityhuman) && this.hasArmor() && (!EnchantmentManager.has(this.getBodyArmorItem(), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || entityhuman.isCreative())) {
                        itemstack.hurtAndBreak(1, entityhuman, getSlotForHand(enumhand));
                        this.playSound(SoundEffects.ARMOR_UNEQUIP_WOLF);
                        itemstack1 = this.getBodyArmorItem();
                        this.setBodyArmorItem(ItemStack.EMPTY);
                        this.spawnAtLocation(itemstack1);
                        return EnumInteractionResult.SUCCESS;
                    } else if (((RecipeItemStack) ((ArmorMaterial) EnumArmorMaterial.ARMADILLO.value()).repairIngredient().get()).test(itemstack) && this.isInSittingPose() && this.hasArmor() && this.isOwnedBy(entityhuman) && this.getBodyArmorItem().isDamaged()) {
                        itemstack.shrink(1);
                        this.playSound(SoundEffects.WOLF_ARMOR_REPAIR);
                        itemstack1 = this.getBodyArmorItem();
                        int i = (int) ((float) itemstack1.getMaxDamage() * 0.125F);

                        itemstack1.setDamageValue(Math.max(0, itemstack1.getDamageValue() - i));
                        return EnumInteractionResult.SUCCESS;
                    } else {
                        EnumInteractionResult enuminteractionresult = super.mobInteract(entityhuman, enumhand);

                        if (!enuminteractionresult.consumesAction() && this.isOwnedBy(entityhuman)) {
                            this.setOrderedToSit(!this.isOrderedToSit());
                            this.jumping = false;
                            this.navigation.stop();
                            this.setTarget((EntityLiving) null);
                            return EnumInteractionResult.SUCCESS_NO_ITEM_USED;
                        } else {
                            return enuminteractionresult;
                        }
                    }
                }
            }
        } else if (itemstack.is(Items.BONE) && !this.isAngry()) {
            itemstack.consume(1, entityhuman);
            this.tryToTame(entityhuman);
            return EnumInteractionResult.SUCCESS;
        } else {
            return super.mobInteract(entityhuman, enumhand);
        }
    }

    private void tryToTame(EntityHuman entityhuman) {
        if (this.random.nextInt(3) == 0) {
            this.tame(entityhuman);
            this.navigation.stop();
            this.setTarget((EntityLiving) null);
            this.setOrderedToSit(true);
            this.level().broadcastEntityEvent(this, (byte) 7);
        } else {
            this.level().broadcastEntityEvent(this, (byte) 6);
        }

    }

    @Override
    public void handleEntityEvent(byte b0) {
        if (b0 == 8) {
            this.isShaking = true;
            this.shakeAnim = 0.0F;
            this.shakeAnimO = 0.0F;
        } else if (b0 == 56) {
            this.cancelShake();
        } else {
            super.handleEntityEvent(b0);
        }

    }

    public float getTailAngle() {
        if (this.isAngry()) {
            return 1.5393804F;
        } else if (this.isTame()) {
            float f = this.getMaxHealth();
            float f1 = (f - this.getHealth()) / f;

            return (0.55F - f1 * 0.4F) * 3.1415927F;
        } else {
            return 0.62831855F;
        }
    }

    @Override
    public boolean isFood(ItemStack itemstack) {
        return itemstack.is(TagsItem.WOLF_FOOD);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 8;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return (Integer) this.entityData.get(EntityWolf.DATA_REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int i) {
        this.entityData.set(EntityWolf.DATA_REMAINING_ANGER_TIME, i);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(EntityWolf.PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID uuid) {
        this.persistentAngerTarget = uuid;
    }

    public EnumColor getCollarColor() {
        return EnumColor.byId((Integer) this.entityData.get(EntityWolf.DATA_COLLAR_COLOR));
    }

    public boolean hasArmor() {
        return this.getBodyArmorItem().is(Items.WOLF_ARMOR);
    }

    public void setCollarColor(EnumColor enumcolor) {
        this.entityData.set(EntityWolf.DATA_COLLAR_COLOR, enumcolor.getId());
    }

    @Nullable
    @Override
    public EntityWolf getBreedOffspring(WorldServer worldserver, EntityAgeable entityageable) {
        EntityWolf entitywolf = (EntityWolf) EntityTypes.WOLF.create(worldserver);

        if (entitywolf != null && entityageable instanceof EntityWolf entitywolf1) {
            if (this.random.nextBoolean()) {
                entitywolf.setVariant(this.getVariant());
            } else {
                entitywolf.setVariant(entitywolf1.getVariant());
            }

            if (this.isTame()) {
                entitywolf.setOwnerUUID(this.getOwnerUUID());
                entitywolf.setTame(true, true);
                if (this.random.nextBoolean()) {
                    entitywolf.setCollarColor(this.getCollarColor());
                } else {
                    entitywolf.setCollarColor(entitywolf1.getCollarColor());
                }
            }
        }

        return entitywolf;
    }

    public void setIsInterested(boolean flag) {
        this.entityData.set(EntityWolf.DATA_INTERESTED_ID, flag);
    }

    @Override
    public boolean canMate(EntityAnimal entityanimal) {
        if (entityanimal == this) {
            return false;
        } else if (!this.isTame()) {
            return false;
        } else if (!(entityanimal instanceof EntityWolf)) {
            return false;
        } else {
            EntityWolf entitywolf = (EntityWolf) entityanimal;

            return !entitywolf.isTame() ? false : (entitywolf.isInSittingPose() ? false : this.isInLove() && entitywolf.isInLove());
        }
    }

    public boolean isInterested() {
        return (Boolean) this.entityData.get(EntityWolf.DATA_INTERESTED_ID);
    }

    @Override
    public boolean wantsToAttack(EntityLiving entityliving, EntityLiving entityliving1) {
        if (!(entityliving instanceof EntityCreeper) && !(entityliving instanceof EntityGhast) && !(entityliving instanceof EntityArmorStand)) {
            if (entityliving instanceof EntityWolf) {
                EntityWolf entitywolf = (EntityWolf) entityliving;

                return !entitywolf.isTame() || entitywolf.getOwner() != entityliving1;
            } else {
                if (entityliving instanceof EntityHuman) {
                    EntityHuman entityhuman = (EntityHuman) entityliving;

                    if (entityliving1 instanceof EntityHuman) {
                        EntityHuman entityhuman1 = (EntityHuman) entityliving1;

                        if (!entityhuman1.canHarmPlayer(entityhuman)) {
                            return false;
                        }
                    }
                }

                if (entityliving instanceof EntityHorseAbstract) {
                    EntityHorseAbstract entityhorseabstract = (EntityHorseAbstract) entityliving;

                    if (entityhorseabstract.isTamed()) {
                        return false;
                    }
                }

                boolean flag;

                if (entityliving instanceof EntityTameableAnimal) {
                    EntityTameableAnimal entitytameableanimal = (EntityTameableAnimal) entityliving;

                    if (entitytameableanimal.isTame()) {
                        flag = false;
                        return flag;
                    }
                }

                flag = true;
                return flag;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean canBeLeashed() {
        return !this.isAngry();
    }

    @Override
    public Vec3D getLeashOffset() {
        return new Vec3D(0.0D, (double) (0.6F * this.getEyeHeight()), (double) (this.getBbWidth() * 0.4F));
    }

    public static boolean checkWolfSpawnRules(EntityTypes<EntityWolf> entitytypes, GeneratorAccess generatoraccess, EnumMobSpawn enummobspawn, BlockPosition blockposition, RandomSource randomsource) {
        return generatoraccess.getBlockState(blockposition.below()).is(TagsBlock.WOLVES_SPAWNABLE_ON) && isBrightEnoughToSpawn(generatoraccess, blockposition);
    }

    private class a<T extends EntityLiving> extends PathfinderGoalAvoidTarget<T> {

        private final EntityWolf wolf;

        public a(final EntityWolf entitywolf, final Class oclass, final float f, final double d0, final double d1) {
            super(entitywolf, oclass, f, d0, d1);
            this.wolf = entitywolf;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && this.toAvoid instanceof EntityLlama ? !this.wolf.isTame() && this.avoidLlama((EntityLlama) this.toAvoid) : false;
        }

        private boolean avoidLlama(EntityLlama entityllama) {
            return entityllama.getStrength() >= EntityWolf.this.random.nextInt(5);
        }

        @Override
        public void start() {
            EntityWolf.this.setTarget((EntityLiving) null);
            super.start();
        }

        @Override
        public void tick() {
            EntityWolf.this.setTarget((EntityLiving) null);
            super.tick();
        }
    }

    public static class b extends EntityAgeable.a {

        public final Holder<WolfVariant> type;

        public b(Holder<WolfVariant> holder) {
            super(false);
            this.type = holder;
        }
    }
}
