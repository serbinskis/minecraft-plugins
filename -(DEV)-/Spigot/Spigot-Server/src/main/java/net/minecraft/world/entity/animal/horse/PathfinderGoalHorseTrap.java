package net.minecraft.world.entity.animal.horse;

import net.minecraft.nbt.NBTTagCompound;
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

public class PathfinderGoalHorseTrap extends PathfinderGoal {

    private final EntityHorseSkeleton horse;

    public PathfinderGoalHorseTrap(EntityHorseSkeleton entityhorseskeleton) {
        this.horse = entityhorseskeleton;
    }

    @Override
    public boolean canUse() {
        return this.horse.level.hasNearbyAlivePlayer(this.horse.getX(), this.horse.getY(), this.horse.getZ(), 10.0D);
    }

    @Override
    public void tick() {
        WorldServer worldserver = (WorldServer) this.horse.level;
        DifficultyDamageScaler difficultydamagescaler = worldserver.getCurrentDifficultyAt(this.horse.blockPosition());

        this.horse.setTrap(false);
        this.horse.setTamed(true);
        this.horse.setAge(0);
        EntityLightning entitylightning = (EntityLightning) EntityTypes.LIGHTNING_BOLT.create(worldserver);

        entitylightning.moveTo(this.horse.getX(), this.horse.getY(), this.horse.getZ());
        entitylightning.setVisualOnly(true);
        worldserver.strikeLightning(entitylightning, org.bukkit.event.weather.LightningStrikeEvent.Cause.TRAP); // CraftBukkit
        EntitySkeleton entityskeleton = this.createSkeleton(difficultydamagescaler, this.horse);

        if (entityskeleton != null) entityskeleton.startRiding(this.horse); // CraftBukkit
        worldserver.addFreshEntityWithPassengers(entityskeleton, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.TRAP); // CraftBukkit

        for (int i = 0; i < 3; ++i) {
            EntityHorseAbstract entityhorseabstract = this.createHorse(difficultydamagescaler);
            if (entityhorseabstract == null) continue; // CraftBukkit
            EntitySkeleton entityskeleton1 = this.createSkeleton(difficultydamagescaler, entityhorseabstract);

            if (entityskeleton1 != null) entityskeleton1.startRiding(entityhorseabstract); // CraftBukkit
            entityhorseabstract.push(this.horse.getRandom().triangle(0.0D, 1.1485D), 0.0D, this.horse.getRandom().triangle(0.0D, 1.1485D));
            worldserver.addFreshEntityWithPassengers(entityhorseabstract, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.JOCKEY); // CraftBukkit
        }

    }

    private EntityHorseAbstract createHorse(DifficultyDamageScaler difficultydamagescaler) {
        EntityHorseSkeleton entityhorseskeleton = (EntityHorseSkeleton) EntityTypes.SKELETON_HORSE.create(this.horse.level);

        entityhorseskeleton.finalizeSpawn((WorldServer) this.horse.level, difficultydamagescaler, EnumMobSpawn.TRIGGERED, (GroupDataEntity) null, (NBTTagCompound) null);
        entityhorseskeleton.setPos(this.horse.getX(), this.horse.getY(), this.horse.getZ());
        entityhorseskeleton.invulnerableTime = 60;
        entityhorseskeleton.setPersistenceRequired();
        entityhorseskeleton.setTamed(true);
        entityhorseskeleton.setAge(0);
        return entityhorseskeleton;
    }

    private EntitySkeleton createSkeleton(DifficultyDamageScaler difficultydamagescaler, EntityHorseAbstract entityhorseabstract) {
        EntitySkeleton entityskeleton = (EntitySkeleton) EntityTypes.SKELETON.create(entityhorseabstract.level);

        entityskeleton.finalizeSpawn((WorldServer) entityhorseabstract.level, difficultydamagescaler, EnumMobSpawn.TRIGGERED, (GroupDataEntity) null, (NBTTagCompound) null);
        entityskeleton.setPos(entityhorseabstract.getX(), entityhorseabstract.getY(), entityhorseabstract.getZ());
        entityskeleton.invulnerableTime = 60;
        entityskeleton.setPersistenceRequired();
        if (entityskeleton.getItemBySlot(EnumItemSlot.HEAD).isEmpty()) {
            entityskeleton.setItemSlot(EnumItemSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        }

        entityskeleton.setItemSlot(EnumItemSlot.MAINHAND, EnchantmentManager.enchantItem(entityskeleton.getRandom(), this.disenchant(entityskeleton.getMainHandItem()), (int) (5.0F + difficultydamagescaler.getSpecialMultiplier() * (float) entityskeleton.getRandom().nextInt(18)), false));
        entityskeleton.setItemSlot(EnumItemSlot.HEAD, EnchantmentManager.enchantItem(entityskeleton.getRandom(), this.disenchant(entityskeleton.getItemBySlot(EnumItemSlot.HEAD)), (int) (5.0F + difficultydamagescaler.getSpecialMultiplier() * (float) entityskeleton.getRandom().nextInt(18)), false));
        return entityskeleton;
    }

    private ItemStack disenchant(ItemStack itemstack) {
        itemstack.removeTagKey("Enchantments");
        return itemstack;
    }
}
