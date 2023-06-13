package net.minecraft.world.level;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.item.EntityTNTPrimed;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentProtection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockFireAbstract;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import net.minecraft.world.entity.boss.EntityComplexPart;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.Location;
import org.bukkit.event.block.BlockExplodeEvent;
// CraftBukkit end

public class Explosion {

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private static final int MAX_DROPS_PER_COMBINED_STACK = 16;
    private final boolean fire;
    private final Explosion.Effect blockInteraction;
    private final RandomSource random;
    private final World level;
    private final double x;
    private final double y;
    private final double z;
    @Nullable
    public final Entity source;
    private final float radius;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final ObjectArrayList<BlockPosition> toBlow;
    private final Map<EntityHuman, Vec3D> hitPlayers;
    public boolean wasCanceled = false; // CraftBukkit - add field

    public Explosion(World world, @Nullable Entity entity, double d0, double d1, double d2, float f, List<BlockPosition> list) {
        this(world, entity, d0, d1, d2, f, false, Explosion.Effect.DESTROY_WITH_DECAY, list);
    }

    public Explosion(World world, @Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag, Explosion.Effect explosion_effect, List<BlockPosition> list) {
        this(world, entity, d0, d1, d2, f, flag, explosion_effect);
        this.toBlow.addAll(list);
    }

    public Explosion(World world, @Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag, Explosion.Effect explosion_effect) {
        this(world, entity, (DamageSource) null, (ExplosionDamageCalculator) null, d0, d1, d2, f, flag, explosion_effect);
    }

    public Explosion(World world, @Nullable Entity entity, @Nullable DamageSource damagesource, @Nullable ExplosionDamageCalculator explosiondamagecalculator, double d0, double d1, double d2, float f, boolean flag, Explosion.Effect explosion_effect) {
        this.random = RandomSource.create();
        this.toBlow = new ObjectArrayList();
        this.hitPlayers = Maps.newHashMap();
        this.level = world;
        this.source = entity;
        this.radius = (float) Math.max(f, 0.0); // CraftBukkit - clamp bad values
        this.x = d0;
        this.y = d1;
        this.z = d2;
        this.fire = flag;
        this.blockInteraction = explosion_effect;
        this.damageSource = damagesource == null ? world.damageSources().explosion(this) : damagesource;
        this.damageCalculator = explosiondamagecalculator == null ? this.makeDamageCalculator(entity) : explosiondamagecalculator;
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
        return (ExplosionDamageCalculator) (entity == null ? Explosion.EXPLOSION_DAMAGE_CALCULATOR : new ExplosionDamageCalculatorEntity(entity));
    }

    public static float getSeenPercent(Vec3D vec3d, Entity entity) {
        AxisAlignedBB axisalignedbb = entity.getBoundingBox();
        double d0 = 1.0D / ((axisalignedbb.maxX - axisalignedbb.minX) * 2.0D + 1.0D);
        double d1 = 1.0D / ((axisalignedbb.maxY - axisalignedbb.minY) * 2.0D + 1.0D);
        double d2 = 1.0D / ((axisalignedbb.maxZ - axisalignedbb.minZ) * 2.0D + 1.0D);
        double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
        double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;

        if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D) {
            int i = 0;
            int j = 0;

            for (double d5 = 0.0D; d5 <= 1.0D; d5 += d0) {
                for (double d6 = 0.0D; d6 <= 1.0D; d6 += d1) {
                    for (double d7 = 0.0D; d7 <= 1.0D; d7 += d2) {
                        double d8 = MathHelper.lerp(d5, axisalignedbb.minX, axisalignedbb.maxX);
                        double d9 = MathHelper.lerp(d6, axisalignedbb.minY, axisalignedbb.maxY);
                        double d10 = MathHelper.lerp(d7, axisalignedbb.minZ, axisalignedbb.maxZ);
                        Vec3D vec3d1 = new Vec3D(d8 + d3, d9, d10 + d4);

                        if (entity.level().clip(new RayTrace(vec3d1, vec3d, RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.NONE, entity)).getType() == MovingObjectPosition.EnumMovingObjectType.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float) i / (float) j;
        } else {
            return 0.0F;
        }
    }

    public void explode() {
        // CraftBukkit start
        if (this.radius < 0.1F) {
            return;
        }
        // CraftBukkit end
        this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3D(this.x, this.y, this.z));
        Set<BlockPosition> set = Sets.newHashSet();
        boolean flag = true;

        int i;
        int j;

        for (int k = 0; k < 16; ++k) {
            for (i = 0; i < 16; ++i) {
                for (j = 0; j < 16; ++j) {
                    if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
                        double d0 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                        double d1 = (double) ((float) i / 15.0F * 2.0F - 1.0F);
                        double d2 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;
                        float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                        double d4 = this.x;
                        double d5 = this.y;
                        double d6 = this.z;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPosition blockposition = BlockPosition.containing(d4, d5, d6);
                            IBlockData iblockdata = this.level.getBlockState(blockposition);
                            Fluid fluid = this.level.getFluidState(blockposition);

                            if (!this.level.isInWorldBounds(blockposition)) {
                                break;
                            }

                            Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockposition, iblockdata, fluid);

                            if (optional.isPresent()) {
                                f -= ((Float) optional.get() + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockposition, iblockdata, f)) {
                                set.add(blockposition);
                            }

                            d4 += d0 * 0.30000001192092896D;
                            d5 += d1 * 0.30000001192092896D;
                            d6 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        this.toBlow.addAll(set);
        float f2 = this.radius * 2.0F;

        i = MathHelper.floor(this.x - (double) f2 - 1.0D);
        j = MathHelper.floor(this.x + (double) f2 + 1.0D);
        int l = MathHelper.floor(this.y - (double) f2 - 1.0D);
        int i1 = MathHelper.floor(this.y + (double) f2 + 1.0D);
        int j1 = MathHelper.floor(this.z - (double) f2 - 1.0D);
        int k1 = MathHelper.floor(this.z + (double) f2 + 1.0D);
        List<Entity> list = this.level.getEntities(this.source, new AxisAlignedBB((double) i, (double) l, (double) j1, (double) j, (double) i1, (double) k1));
        Vec3D vec3d = new Vec3D(this.x, this.y, this.z);

        for (int l1 = 0; l1 < list.size(); ++l1) {
            Entity entity = (Entity) list.get(l1);

            if (!entity.ignoreExplosion()) {
                double d7 = Math.sqrt(entity.distanceToSqr(vec3d)) / (double) f2;

                if (d7 <= 1.0D) {
                    double d8 = entity.getX() - this.x;
                    double d9 = (entity instanceof EntityTNTPrimed ? entity.getY() : entity.getEyeY()) - this.y;
                    double d10 = entity.getZ() - this.z;
                    double d11 = Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10);

                    if (d11 != 0.0D) {
                        d8 /= d11;
                        d9 /= d11;
                        d10 /= d11;
                        double d12 = (double) getSeenPercent(vec3d, entity);
                        double d13 = (1.0D - d7) * d12;

                        // CraftBukkit start

                        // Special case ender dragon only give knockback if no damage is cancelled
                        // Thinks to note:
                        // - Setting a velocity to a ComplexEntityPart is ignored (and therefore not needed)
                        // - Damaging ComplexEntityPart while forward the damage to EntityEnderDragon
                        // - Damaging EntityEnderDragon does nothing
                        // - EntityEnderDragon hitbock always covers the other parts and is therefore always present
                        if (entity instanceof EntityComplexPart) {
                            continue;
                        }

                        CraftEventFactory.entityDamage = source;
                        entity.lastDamageCancelled = false;

                        if (entity instanceof EntityEnderDragon) {
                            for (EntityComplexPart entityComplexPart : ((EntityEnderDragon) entity).subEntities) {
                                // Calculate damage separately for each EntityComplexPart
                                double d7part;
                                if (list.contains(entityComplexPart) && (d7part = Math.sqrt(entityComplexPart.distanceToSqr(vec3d)) / f2) <= 1.0D) {
                                    double d13part = (1.0D - d7part) * getSeenPercent(vec3d, entityComplexPart);
                                    entityComplexPart.hurt(this.getDamageSource(), (float) ((int) ((d13part * d13part + d13part) / 2.0D * 7.0D * (double) f2 + 1.0D)));
                                }
                            }
                        } else {
                            entity.hurt(this.getDamageSource(), (float) ((int) ((d13 * d13 + d13) / 2.0D * 7.0D * (double) f2 + 1.0D)));
                        }

                        CraftEventFactory.entityDamage = null;
                        if (entity.lastDamageCancelled) { // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Skip entity if damage event was cancelled
                            continue;
                        }
                        // CraftBukkit end
                        double d14;

                        if (entity instanceof EntityLiving) {
                            EntityLiving entityliving = (EntityLiving) entity;

                            d14 = EnchantmentProtection.getExplosionKnockbackAfterDampener(entityliving, d13);
                        } else {
                            d14 = d13;
                        }

                        d8 *= d14;
                        d9 *= d14;
                        d10 *= d14;
                        Vec3D vec3d1 = new Vec3D(d8, d9, d10);

                        entity.setDeltaMovement(entity.getDeltaMovement().add(vec3d1));
                        if (entity instanceof EntityHuman) {
                            EntityHuman entityhuman = (EntityHuman) entity;

                            if (!entityhuman.isSpectator() && (!entityhuman.isCreative() || !entityhuman.getAbilities().flying)) {
                                this.hitPlayers.put(entityhuman, vec3d1);
                            }
                        }
                    }
                }
            }
        }

    }

    public void finalizeExplosion(boolean flag) {
        if (this.level.isClientSide) {
            this.level.playLocalSound(this.x, this.y, this.z, SoundEffects.GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        boolean flag1 = this.interactsWithBlocks();

        if (flag) {
            if (this.radius >= 2.0F && flag1) {
                this.level.addParticle(Particles.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            } else {
                this.level.addParticle(Particles.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            }
        }

        if (flag1) {
            ObjectArrayList<Pair<ItemStack, BlockPosition>> objectarraylist = new ObjectArrayList();
            boolean flag2 = this.getIndirectSourceEntity() instanceof EntityHuman;

            SystemUtils.shuffle(this.toBlow, this.level.random);
            ObjectListIterator objectlistiterator = this.toBlow.iterator();
            // CraftBukkit start
            org.bukkit.World bworld = this.level.getWorld();
            org.bukkit.entity.Entity explode = this.source == null ? null : this.source.getBukkitEntity();
            Location location = new Location(bworld, this.x, this.y, this.z);

            List<org.bukkit.block.Block> blockList = new ObjectArrayList<>();
            for (int i1 = this.toBlow.size() - 1; i1 >= 0; i1--) {
                BlockPosition cpos = this.toBlow.get(i1);
                org.bukkit.block.Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());
                if (!bblock.getType().isAir()) {
                    blockList.add(bblock);
                }
            }

            boolean cancelled;
            List<org.bukkit.block.Block> bukkitBlocks;
            float yield;

            if (explode != null) {
                EntityExplodeEvent event = new EntityExplodeEvent(explode, location, blockList, this.blockInteraction == Explosion.Effect.DESTROY_WITH_DECAY ? 1.0F / this.radius : 1.0F);
                this.level.getCraftServer().getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
                yield = event.getYield();
            } else {
                BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blockList, this.blockInteraction == Explosion.Effect.DESTROY_WITH_DECAY ? 1.0F / this.radius : 1.0F);
                this.level.getCraftServer().getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
                yield = event.getYield();
            }

            this.toBlow.clear();

            for (org.bukkit.block.Block bblock : bukkitBlocks) {
                BlockPosition coords = new BlockPosition(bblock.getX(), bblock.getY(), bblock.getZ());
                toBlow.add(coords);
            }

            if (cancelled) {
                this.wasCanceled = true;
                return;
            }
            // CraftBukkit end
            objectlistiterator = this.toBlow.iterator();

            while (objectlistiterator.hasNext()) {
                BlockPosition blockposition = (BlockPosition) objectlistiterator.next();
                IBlockData iblockdata = this.level.getBlockState(blockposition);
                Block block = iblockdata.getBlock();
                // CraftBukkit start - TNTPrimeEvent
                if (block instanceof net.minecraft.world.level.block.BlockTNT) {
                    Entity sourceEntity = source == null ? null : source;
                    BlockPosition sourceBlock = sourceEntity == null ? BlockPosition.containing(this.x, this.y, this.z) : null;
                    if (!CraftEventFactory.callTNTPrimeEvent(this.level, blockposition, org.bukkit.event.block.TNTPrimeEvent.PrimeCause.EXPLOSION, sourceEntity, sourceBlock)) {
                        this.level.sendBlockUpdated(blockposition, Blocks.AIR.defaultBlockState(), iblockdata, 3); // Update the block on the client
                        continue;
                    }
                }
                // CraftBukkit end

                if (!iblockdata.isAir()) {
                    BlockPosition blockposition1 = blockposition.immutable();

                    this.level.getProfiler().push("explosion_blocks");
                    if (block.dropFromExplosion(this)) {
                        World world = this.level;

                        if (world instanceof WorldServer) {
                            WorldServer worldserver = (WorldServer) world;
                            TileEntity tileentity = iblockdata.hasBlockEntity() ? this.level.getBlockEntity(blockposition) : null;
                            LootParams.a lootparams_a = (new LootParams.a(worldserver)).withParameter(LootContextParameters.ORIGIN, Vec3D.atCenterOf(blockposition)).withParameter(LootContextParameters.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParameters.BLOCK_ENTITY, tileentity).withOptionalParameter(LootContextParameters.THIS_ENTITY, this.source);

                            if (yield < 1.0F) { // CraftBukkit - add yield
                                lootparams_a.withParameter(LootContextParameters.EXPLOSION_RADIUS, 1.0F / yield); // CraftBukkit - add yield
                            }

                            iblockdata.spawnAfterBreak(worldserver, blockposition, ItemStack.EMPTY, flag2);
                            iblockdata.getDrops(lootparams_a).forEach((itemstack) -> {
                                addBlockDrops(objectarraylist, itemstack, blockposition1);
                            });
                        }
                    }

                    this.level.setBlock(blockposition, Blocks.AIR.defaultBlockState(), 3);
                    block.wasExploded(this.level, blockposition, this);
                    this.level.getProfiler().pop();
                }
            }

            objectlistiterator = objectarraylist.iterator();

            while (objectlistiterator.hasNext()) {
                Pair<ItemStack, BlockPosition> pair = (Pair) objectlistiterator.next();

                Block.popResource(this.level, (BlockPosition) pair.getSecond(), (ItemStack) pair.getFirst());
            }
        }

        if (this.fire) {
            ObjectListIterator objectlistiterator1 = this.toBlow.iterator();

            while (objectlistiterator1.hasNext()) {
                BlockPosition blockposition2 = (BlockPosition) objectlistiterator1.next();

                if (this.random.nextInt(3) == 0 && this.level.getBlockState(blockposition2).isAir() && this.level.getBlockState(blockposition2.below()).isSolidRender(this.level, blockposition2.below())) {
                    // CraftBukkit start - Ignition by explosion
                    if (!org.bukkit.craftbukkit.event.CraftEventFactory.callBlockIgniteEvent(this.level, blockposition2.getX(), blockposition2.getY(), blockposition2.getZ(), this).isCancelled()) {
                        this.level.setBlockAndUpdate(blockposition2, BlockFireAbstract.getState(this.level, blockposition2));
                    }
                    // CraftBukkit end
                }
            }
        }

    }

    public boolean interactsWithBlocks() {
        return this.blockInteraction != Explosion.Effect.KEEP;
    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPosition>> objectarraylist, ItemStack itemstack, BlockPosition blockposition) {
        if (itemstack.isEmpty()) return; // CraftBukkit - SPIGOT-5425
        int i = objectarraylist.size();

        for (int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPosition> pair = (Pair) objectarraylist.get(j);
            ItemStack itemstack1 = (ItemStack) pair.getFirst();

            if (EntityItem.areMergable(itemstack1, itemstack)) {
                ItemStack itemstack2 = EntityItem.merge(itemstack1, itemstack, 16);

                objectarraylist.set(j, Pair.of(itemstack2, (BlockPosition) pair.getSecond()));
                if (itemstack.isEmpty()) {
                    return;
                }
            }
        }

        objectarraylist.add(Pair.of(itemstack, blockposition));
    }

    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    public Map<EntityHuman, Vec3D> getHitPlayers() {
        return this.hitPlayers;
    }

    @Nullable
    public EntityLiving getIndirectSourceEntity() {
        if (this.source == null) {
            return null;
        } else {
            Entity entity = this.source;

            if (entity instanceof EntityTNTPrimed) {
                EntityTNTPrimed entitytntprimed = (EntityTNTPrimed) entity;

                return entitytntprimed.getOwner();
            } else {
                entity = this.source;
                if (entity instanceof EntityLiving) {
                    EntityLiving entityliving = (EntityLiving) entity;

                    return entityliving;
                } else {
                    entity = this.source;
                    if (entity instanceof IProjectile) {
                        IProjectile iprojectile = (IProjectile) entity;

                        entity = iprojectile.getOwner();
                        if (entity instanceof EntityLiving) {
                            EntityLiving entityliving1 = (EntityLiving) entity;

                            return entityliving1;
                        }
                    }

                    return null;
                }
            }
        }
    }

    @Nullable
    public Entity getDirectSourceEntity() {
        return this.source;
    }

    public void clearToBlow() {
        this.toBlow.clear();
    }

    public List<BlockPosition> getToBlow() {
        return this.toBlow;
    }

    public static enum Effect {

        KEEP, DESTROY, DESTROY_WITH_DECAY;

        private Effect() {}
    }
}
