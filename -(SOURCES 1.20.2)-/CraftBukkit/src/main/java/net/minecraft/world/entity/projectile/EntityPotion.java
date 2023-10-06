package net.minecraft.world.entity.projectile;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAreaEffectCloud;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.item.alchemy.PotionUtil;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.BlockCampfire;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.MovingObjectPositionEntity;

// CraftBukkit start
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
// CraftBukkit end

public class EntityPotion extends EntityProjectileThrowable implements ItemSupplier {

    public static final double SPLASH_RANGE = 4.0D;
    private static final double SPLASH_RANGE_SQ = 16.0D;
    public static final Predicate<EntityLiving> WATER_SENSITIVE_OR_ON_FIRE = (entityliving) -> {
        return entityliving.isSensitiveToWater() || entityliving.isOnFire();
    };

    public EntityPotion(EntityTypes<? extends EntityPotion> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityPotion(World world, EntityLiving entityliving) {
        super(EntityTypes.POTION, entityliving, world);
    }

    public EntityPotion(World world, double d0, double d1, double d2) {
        super(EntityTypes.POTION, d0, d1, d2, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    protected void onHitBlock(MovingObjectPositionBlock movingobjectpositionblock) {
        super.onHitBlock(movingobjectpositionblock);
        if (!this.level().isClientSide) {
            ItemStack itemstack = this.getItem();
            PotionRegistry potionregistry = PotionUtil.getPotion(itemstack);
            List<MobEffect> list = PotionUtil.getMobEffects(itemstack);
            boolean flag = potionregistry == Potions.WATER && list.isEmpty();
            EnumDirection enumdirection = movingobjectpositionblock.getDirection();
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos();
            BlockPosition blockposition1 = blockposition.relative(enumdirection);

            if (flag) {
                this.dowseFire(blockposition1);
                this.dowseFire(blockposition1.relative(enumdirection.getOpposite()));
                Iterator iterator = EnumDirection.EnumDirectionLimit.HORIZONTAL.iterator();

                while (iterator.hasNext()) {
                    EnumDirection enumdirection1 = (EnumDirection) iterator.next();

                    this.dowseFire(blockposition1.relative(enumdirection1));
                }
            }

        }
    }

    @Override
    protected void onHit(MovingObjectPosition movingobjectposition) {
        super.onHit(movingobjectposition);
        if (!this.level().isClientSide) {
            ItemStack itemstack = this.getItem();
            PotionRegistry potionregistry = PotionUtil.getPotion(itemstack);
            List<MobEffect> list = PotionUtil.getMobEffects(itemstack);
            boolean flag = potionregistry == Potions.WATER && list.isEmpty();

            if (flag) {
                this.applyWater();
            } else if (true || !list.isEmpty()) { // CraftBukkit - Call event even if no effects to apply
                if (this.isLingering()) {
                    this.makeAreaOfEffectCloud(itemstack, potionregistry);
                } else {
                    this.applySplash(list, movingobjectposition.getType() == MovingObjectPosition.EnumMovingObjectType.ENTITY ? ((MovingObjectPositionEntity) movingobjectposition).getEntity() : null);
                }
            }

            int i = potionregistry.hasInstantEffects() ? 2007 : 2002;

            this.level().levelEvent(i, this.blockPosition(), PotionUtil.getColor(itemstack));
            this.discard();
        }
    }

    private void applyWater() {
        AxisAlignedBB axisalignedbb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<EntityLiving> list = this.level().getEntitiesOfClass(EntityLiving.class, axisalignedbb, EntityPotion.WATER_SENSITIVE_OR_ON_FIRE);
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            EntityLiving entityliving = (EntityLiving) iterator.next();
            double d0 = this.distanceToSqr((Entity) entityliving);

            if (d0 < 16.0D) {
                if (entityliving.isSensitiveToWater()) {
                    entityliving.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 1.0F);
                }

                if (entityliving.isOnFire() && entityliving.isAlive()) {
                    entityliving.extinguishFire();
                }
            }
        }

        List<Axolotl> list1 = this.level().getEntitiesOfClass(Axolotl.class, axisalignedbb);
        Iterator iterator1 = list1.iterator();

        while (iterator1.hasNext()) {
            Axolotl axolotl = (Axolotl) iterator1.next();

            axolotl.rehydrate();
        }

    }

    private void applySplash(List<MobEffect> list, @Nullable Entity entity) {
        AxisAlignedBB axisalignedbb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<EntityLiving> list1 = this.level().getEntitiesOfClass(EntityLiving.class, axisalignedbb);
        Map<LivingEntity, Double> affected = new HashMap<LivingEntity, Double>(); // CraftBukkit

        if (!list1.isEmpty()) {
            Entity entity1 = this.getEffectSource();
            Iterator iterator = list1.iterator();

            while (iterator.hasNext()) {
                EntityLiving entityliving = (EntityLiving) iterator.next();

                if (entityliving.isAffectedByPotions()) {
                    double d0 = this.distanceToSqr((Entity) entityliving);

                    if (d0 < 16.0D) {
                        double d1;

                        if (entityliving == entity) {
                            d1 = 1.0D;
                        } else {
                            d1 = 1.0D - Math.sqrt(d0) / 4.0D;
                        }

                        // CraftBukkit start
                        affected.put((LivingEntity) entityliving.getBukkitEntity(), d1);
                    }
                }
            }
        }

        org.bukkit.event.entity.PotionSplashEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callPotionSplashEvent(this, affected);
        if (!event.isCancelled() && list != null && !list.isEmpty()) { // do not process effects if there are no effects to process
            Entity entity1 = this.getEffectSource();
            for (LivingEntity victim : event.getAffectedEntities()) {
                if (!(victim instanceof CraftLivingEntity)) {
                    continue;
                }

                EntityLiving entityliving = ((CraftLivingEntity) victim).getHandle();
                double d1 = event.getIntensity(victim);
                // CraftBukkit end

                Iterator iterator1 = list.iterator();

                while (iterator1.hasNext()) {
                    MobEffect mobeffect = (MobEffect) iterator1.next();
                    MobEffectList mobeffectlist = mobeffect.getEffect();
                    // CraftBukkit start - Abide by PVP settings - for players only!
                    if (!this.level().pvpMode && this.getOwner() instanceof EntityPlayer && entityliving instanceof EntityPlayer && entityliving != this.getOwner()) {
                        if (mobeffectlist == MobEffects.MOVEMENT_SLOWDOWN || mobeffectlist == MobEffects.DIG_SLOWDOWN || mobeffectlist == MobEffects.HARM || mobeffectlist == MobEffects.BLINDNESS
                                || mobeffectlist == MobEffects.HUNGER || mobeffectlist == MobEffects.WEAKNESS || mobeffectlist == MobEffects.POISON) {
                            continue;
                        }
                    }
                    // CraftBukkit end

                    if (mobeffectlist.isInstantenous()) {
                        mobeffectlist.applyInstantenousEffect(this, this.getOwner(), entityliving, mobeffect.getAmplifier(), d1);
                    } else {
                        int i = mobeffect.mapDuration((j) -> {
                            return (int) (d1 * (double) j + 0.5D);
                        });
                        MobEffect mobeffect1 = new MobEffect(mobeffectlist, i, mobeffect.getAmplifier(), mobeffect.isAmbient(), mobeffect.isVisible());

                        if (!mobeffect1.endsWithin(20)) {
                            entityliving.addEffect(mobeffect1, entity1, org.bukkit.event.entity.EntityPotionEffectEvent.Cause.POTION_SPLASH); // CraftBukkit
                        }
                    }
                }
            }
        }

    }

    private void makeAreaOfEffectCloud(ItemStack itemstack, PotionRegistry potionregistry) {
        EntityAreaEffectCloud entityareaeffectcloud = new EntityAreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        Entity entity = this.getOwner();

        if (entity instanceof EntityLiving) {
            entityareaeffectcloud.setOwner((EntityLiving) entity);
        }

        entityareaeffectcloud.setRadius(3.0F);
        entityareaeffectcloud.setRadiusOnUse(-0.5F);
        entityareaeffectcloud.setWaitTime(10);
        entityareaeffectcloud.setRadiusPerTick(-entityareaeffectcloud.getRadius() / (float) entityareaeffectcloud.getDuration());
        entityareaeffectcloud.setPotion(potionregistry);
        Iterator iterator = PotionUtil.getCustomEffects(itemstack).iterator();

        while (iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            entityareaeffectcloud.addEffect(new MobEffect(mobeffect));
        }

        NBTTagCompound nbttagcompound = itemstack.getTag();

        if (nbttagcompound != null && nbttagcompound.contains("CustomPotionColor", 99)) {
            entityareaeffectcloud.setFixedColor(nbttagcompound.getInt("CustomPotionColor"));
        }

        // CraftBukkit start
        org.bukkit.event.entity.LingeringPotionSplashEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callLingeringPotionSplashEvent(this, entityareaeffectcloud);
        if (!(event.isCancelled() || entityareaeffectcloud.isRemoved())) {
            this.level().addFreshEntity(entityareaeffectcloud);
        } else {
            entityareaeffectcloud.discard();
        }
        // CraftBukkit end
    }

    public boolean isLingering() {
        return this.getItem().is(Items.LINGERING_POTION);
    }

    private void dowseFire(BlockPosition blockposition) {
        IBlockData iblockdata = this.level().getBlockState(blockposition);

        if (iblockdata.is(TagsBlock.FIRE)) {
            // CraftBukkit start
            if (CraftEventFactory.callEntityChangeBlockEvent(this, blockposition, Blocks.AIR.defaultBlockState())) {
                this.level().destroyBlock(blockposition, false, this);
            }
            // CraftBukkit end
        } else if (AbstractCandleBlock.isLit(iblockdata)) {
            // CraftBukkit start
            if (CraftEventFactory.callEntityChangeBlockEvent(this, blockposition, iblockdata.setValue(AbstractCandleBlock.LIT, false))) {
                AbstractCandleBlock.extinguish((EntityHuman) null, iblockdata, this.level(), blockposition);
            }
            // CraftBukkit end
        } else if (BlockCampfire.isLitCampfire(iblockdata)) {
            // CraftBukkit start
            if (CraftEventFactory.callEntityChangeBlockEvent(this, blockposition, iblockdata.setValue(BlockCampfire.LIT, false))) {
                this.level().levelEvent((EntityHuman) null, 1009, blockposition, 0);
                BlockCampfire.dowse(this.getOwner(), this.level(), blockposition, iblockdata);
                this.level().setBlockAndUpdate(blockposition, (IBlockData) iblockdata.setValue(BlockCampfire.LIT, false));
            }
            // CraftBukkit end
        }

    }
}
