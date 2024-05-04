package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EntityEndermite;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionEntity;

// CraftBukkit start
import org.bukkit.Bukkit;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
// CraftBukkit end

public class EntityEnderPearl extends EntityProjectileThrowable {

    public EntityEnderPearl(EntityTypes<? extends EntityEnderPearl> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityEnderPearl(World world, EntityLiving entityliving) {
        super(EntityTypes.ENDER_PEARL, entityliving, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void onHitEntity(MovingObjectPositionEntity movingobjectpositionentity) {
        super.onHitEntity(movingobjectpositionentity);
        movingobjectpositionentity.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onHit(MovingObjectPosition movingobjectposition) {
        super.onHit(movingobjectposition);

        for (int i = 0; i < 32; ++i) {
            this.level().addParticle(Particles.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0D, this.getZ(), this.random.nextGaussian(), 0.0D, this.random.nextGaussian());
        }

        if (!this.level().isClientSide && !this.isRemoved()) {
            Entity entity = this.getOwner();

            if (entity instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entity;

                if (entityplayer.connection.isAcceptingMessages() && entityplayer.level() == this.level() && !entityplayer.isSleeping()) {
                    // CraftBukkit start - Fire PlayerTeleportEvent
                    org.bukkit.craftbukkit.entity.CraftPlayer player = entityplayer.getBukkitEntity();
                    org.bukkit.Location location = getBukkitEntity().getLocation();
                    location.setPitch(player.getLocation().getPitch());
                    location.setYaw(player.getLocation().getYaw());

                    PlayerTeleportEvent teleEvent = new PlayerTeleportEvent(player, player.getLocation(), location, PlayerTeleportEvent.TeleportCause.ENDER_PEARL);
                    Bukkit.getPluginManager().callEvent(teleEvent);

                    if (!teleEvent.isCancelled() && entityplayer.connection.isAcceptingMessages()) {
                        if (this.random.nextFloat() < 0.05F && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                            EntityEndermite entityendermite = (EntityEndermite) EntityTypes.ENDERMITE.create(this.level());

                            if (entityendermite != null) {
                                entityendermite.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                                this.level().addFreshEntity(entityendermite, CreatureSpawnEvent.SpawnReason.ENDER_PEARL);
                            }
                        }

                        if (entity.isPassenger()) {
                            entity.stopRiding();
                        }

                        entityplayer.connection.teleport(teleEvent.getTo());
                        entity.resetFallDistance();
                        entity.hurt(this.damageSources().fall().customEntityDamager(this), 5.0F); // CraftBukkit
                    }
                    // CraftBukkit end
                    this.level().playSound((EntityHuman) null, this.getX(), this.getY(), this.getZ(), SoundEffects.PLAYER_TELEPORT, SoundCategory.PLAYERS);
                }
            } else if (entity != null) {
                entity.teleportTo(this.getX(), this.getY(), this.getZ());
                entity.resetFallDistance();
            }

            this.discard(EntityRemoveEvent.Cause.HIT); // CraftBukkit - add Bukkit remove cause
        }

    }

    @Override
    public void tick() {
        Entity entity = this.getOwner();

        if (entity instanceof EntityPlayer && !entity.isAlive() && this.level().getGameRules().getBoolean(GameRules.RULE_ENDER_PEARLS_VANISH_ON_DEATH)) {
            this.discard(EntityRemoveEvent.Cause.DESPAWN); // CraftBukkit - add Bukkit remove cause
        } else {
            super.tick();
        }

    }

    @Nullable
    @Override
    public Entity changeDimension(WorldServer worldserver) {
        Entity entity = this.getOwner();

        if (entity != null && worldserver != null && entity.level().dimension() != worldserver.dimension()) { // CraftBukkit - SPIGOT-6113
            this.setOwner((Entity) null);
        }

        return super.changeDimension(worldserver);
    }
}
