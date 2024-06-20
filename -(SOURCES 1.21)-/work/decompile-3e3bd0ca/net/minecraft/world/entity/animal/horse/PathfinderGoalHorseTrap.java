package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.entity.EntityLightning;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.monster.EntitySkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;

public class PathfinderGoalHorseTrap extends PathfinderGoal {

    private final EntityHorseSkeleton horse;

    public PathfinderGoalHorseTrap(EntityHorseSkeleton entityhorseskeleton) {
        this.horse = entityhorseskeleton;
    }

    @Override
    public boolean canUse() {
        return this.horse.level().hasNearbyAlivePlayer(this.horse.getX(), this.horse.getY(), this.horse.getZ(), 10.0D);
    }

    @Override
    public void tick() {
        WorldServer worldserver = (WorldServer) this.horse.level();
        DifficultyDamageScaler difficultydamagescaler = worldserver.getCurrentDifficultyAt(this.horse.blockPosition());

        this.horse.setTrap(false);
        this.horse.setTamed(true);
        this.horse.setAge(0);
        EntityLightning entitylightning = (EntityLightning) EntityTypes.LIGHTNING_BOLT.create(worldserver);

        if (entitylightning != null) {
            entitylightning.moveTo(this.horse.getX(), this.horse.getY(), this.horse.getZ());
            entitylightning.setVisualOnly(true);
            worldserver.addFreshEntity(entitylightning);
            EntitySkeleton entityskeleton = this.createSkeleton(difficultydamagescaler, this.horse);

            if (entityskeleton != null) {
                entityskeleton.startRiding(this.horse);
                worldserver.addFreshEntityWithPassengers(entityskeleton);

                for (int i = 0; i < 3; ++i) {
                    EntityHorseAbstract entityhorseabstract = this.createHorse(difficultydamagescaler);

                    if (entityhorseabstract != null) {
                        EntitySkeleton entityskeleton1 = this.createSkeleton(difficultydamagescaler, entityhorseabstract);

                        if (entityskeleton1 != null) {
                            entityskeleton1.startRiding(entityhorseabstract);
                            entityhorseabstract.push(this.horse.getRandom().triangle(0.0D, 1.1485D), 0.0D, this.horse.getRandom().triangle(0.0D, 1.1485D));
                            worldserver.addFreshEntityWithPassengers(entityhorseabstract);
                        }
                    }
                }

            }
        }
    }

    @Nullable
    private EntityHorseAbstract createHorse(DifficultyDamageScaler difficultydamagescaler) {
        EntityHorseSkeleton entityhorseskeleton = (EntityHorseSkeleton) EntityTypes.SKELETON_HORSE.create(this.horse.level());

        if (entityhorseskeleton != null) {
            entityhorseskeleton.finalizeSpawn((WorldServer) this.horse.level(), difficultydamagescaler, EnumMobSpawn.TRIGGERED, (GroupDataEntity) null);
            entityhorseskeleton.setPos(this.horse.getX(), this.horse.getY(), this.horse.getZ());
            entityhorseskeleton.invulnerableTime = 60;
            entityhorseskeleton.setPersistenceRequired();
            entityhorseskeleton.setTamed(true);
            entityhorseskeleton.setAge(0);
        }

        return entityhorseskeleton;
    }

    @Nullable
    private EntitySkeleton createSkeleton(DifficultyDamageScaler difficultydamagescaler, EntityHorseAbstract entityhorseabstract) {
        EntitySkeleton entityskeleton = (EntitySkeleton) EntityTypes.SKELETON.create(entityhorseabstract.level());

        if (entityskeleton != null) {
            entityskeleton.finalizeSpawn((WorldServer) entityhorseabstract.level(), difficultydamagescaler, EnumMobSpawn.TRIGGERED, (GroupDataEntity) null);
            entityskeleton.setPos(entityhorseabstract.getX(), entityhorseabstract.getY(), entityhorseabstract.getZ());
            entityskeleton.invulnerableTime = 60;
            entityskeleton.setPersistenceRequired();
            if (entityskeleton.getItemBySlot(EnumItemSlot.HEAD).isEmpty()) {
                entityskeleton.setItemSlot(EnumItemSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            }

            this.enchant(entityskeleton, EnumItemSlot.MAINHAND, difficultydamagescaler);
            this.enchant(entityskeleton, EnumItemSlot.HEAD, difficultydamagescaler);
        }

        return entityskeleton;
    }

    private void enchant(EntitySkeleton entityskeleton, EnumItemSlot enumitemslot, DifficultyDamageScaler difficultydamagescaler) {
        ItemStack itemstack = entityskeleton.getItemBySlot(enumitemslot);

        itemstack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        EnchantmentManager.enchantItemFromProvider(itemstack, entityskeleton.level().registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficultydamagescaler, entityskeleton.getRandom());
        entityskeleton.setItemSlot(enumitemslot, itemstack);
    }
}
