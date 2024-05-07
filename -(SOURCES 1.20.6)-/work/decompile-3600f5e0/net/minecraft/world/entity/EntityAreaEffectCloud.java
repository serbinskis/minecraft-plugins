package net.minecraft.world.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.ColorUtil;
import net.minecraft.util.MathHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.level.World;
import net.minecraft.world.level.material.EnumPistonReaction;
import org.slf4j.Logger;

public class EntityAreaEffectCloud extends Entity implements TraceableEntity {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TIME_BETWEEN_APPLICATIONS = 5;
    private static final DataWatcherObject<Float> DATA_RADIUS = DataWatcher.defineId(EntityAreaEffectCloud.class, DataWatcherRegistry.FLOAT);
    private static final DataWatcherObject<Boolean> DATA_WAITING = DataWatcher.defineId(EntityAreaEffectCloud.class, DataWatcherRegistry.BOOLEAN);
    private static final DataWatcherObject<ParticleParam> DATA_PARTICLE = DataWatcher.defineId(EntityAreaEffectCloud.class, DataWatcherRegistry.PARTICLE);
    private static final float MAX_RADIUS = 32.0F;
    private static final float MINIMAL_RADIUS = 0.5F;
    private static final float DEFAULT_RADIUS = 3.0F;
    public static final float DEFAULT_WIDTH = 6.0F;
    public static final float HEIGHT = 0.5F;
    public PotionContents potionContents;
    private final Map<Entity, Integer> victims;
    private int duration;
    public int waitTime;
    public int reapplicationDelay;
    public int durationOnUse;
    public float radiusOnUse;
    public float radiusPerTick;
    @Nullable
    private EntityLiving owner;
    @Nullable
    private UUID ownerUUID;

    public EntityAreaEffectCloud(EntityTypes<? extends EntityAreaEffectCloud> entitytypes, World world) {
        super(entitytypes, world);
        this.potionContents = PotionContents.EMPTY;
        this.victims = Maps.newHashMap();
        this.duration = 600;
        this.waitTime = 20;
        this.reapplicationDelay = 20;
        this.noPhysics = true;
    }

    public EntityAreaEffectCloud(World world, double d0, double d1, double d2) {
        this(EntityTypes.AREA_EFFECT_CLOUD, world);
        this.setPos(d0, d1, d2);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityAreaEffectCloud.DATA_RADIUS, 3.0F);
        datawatcher_a.define(EntityAreaEffectCloud.DATA_WAITING, false);
        datawatcher_a.define(EntityAreaEffectCloud.DATA_PARTICLE, ColorParticleOption.create(Particles.ENTITY_EFFECT, -1));
    }

    public void setRadius(float f) {
        if (!this.level().isClientSide) {
            this.getEntityData().set(EntityAreaEffectCloud.DATA_RADIUS, MathHelper.clamp(f, 0.0F, 32.0F));
        }

    }

    @Override
    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();

        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    public float getRadius() {
        return (Float) this.getEntityData().get(EntityAreaEffectCloud.DATA_RADIUS);
    }

    public void setPotionContents(PotionContents potioncontents) {
        this.potionContents = potioncontents;
        this.updateColor();
    }

    public void updateColor() {
        ParticleParam particleparam = (ParticleParam) this.entityData.get(EntityAreaEffectCloud.DATA_PARTICLE);

        if (particleparam instanceof ColorParticleOption colorparticleoption) {
            int i = this.potionContents.equals(PotionContents.EMPTY) ? 0 : this.potionContents.getColor();

            this.entityData.set(EntityAreaEffectCloud.DATA_PARTICLE, ColorParticleOption.create(colorparticleoption.getType(), ColorUtil.b.opaque(i)));
        }

    }

    public void addEffect(MobEffect mobeffect) {
        this.setPotionContents(this.potionContents.withEffectAdded(mobeffect));
    }

    public ParticleParam getParticle() {
        return (ParticleParam) this.getEntityData().get(EntityAreaEffectCloud.DATA_PARTICLE);
    }

    public void setParticle(ParticleParam particleparam) {
        this.getEntityData().set(EntityAreaEffectCloud.DATA_PARTICLE, particleparam);
    }

    protected void setWaiting(boolean flag) {
        this.getEntityData().set(EntityAreaEffectCloud.DATA_WAITING, flag);
    }

    public boolean isWaiting() {
        return (Boolean) this.getEntityData().get(EntityAreaEffectCloud.DATA_WAITING);
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int i) {
        this.duration = i;
    }

    @Override
    public void tick() {
        super.tick();
        boolean flag = this.isWaiting();
        float f = this.getRadius();

        if (this.level().isClientSide) {
            if (flag && this.random.nextBoolean()) {
                return;
            }

            ParticleParam particleparam = this.getParticle();
            int i;
            float f1;

            if (flag) {
                i = 2;
                f1 = 0.2F;
            } else {
                i = MathHelper.ceil(3.1415927F * f * f);
                f1 = f;
            }

            for (int j = 0; j < i; ++j) {
                float f2 = this.random.nextFloat() * 6.2831855F;
                float f3 = MathHelper.sqrt(this.random.nextFloat()) * f1;
                double d0 = this.getX() + (double) (MathHelper.cos(f2) * f3);
                double d1 = this.getY();
                double d2 = this.getZ() + (double) (MathHelper.sin(f2) * f3);

                if (particleparam.getType() == Particles.ENTITY_EFFECT) {
                    if (flag && this.random.nextBoolean()) {
                        this.level().addAlwaysVisibleParticle(ColorParticleOption.create(Particles.ENTITY_EFFECT, -1), d0, d1, d2, 0.0D, 0.0D, 0.0D);
                    } else {
                        this.level().addAlwaysVisibleParticle(particleparam, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                    }
                } else if (flag) {
                    this.level().addAlwaysVisibleParticle(particleparam, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                } else {
                    this.level().addAlwaysVisibleParticle(particleparam, d0, d1, d2, (0.5D - this.random.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - this.random.nextDouble()) * 0.15D);
                }
            }
        } else {
            if (this.tickCount >= this.waitTime + this.duration) {
                this.discard();
                return;
            }

            boolean flag1 = this.tickCount < this.waitTime;

            if (flag != flag1) {
                this.setWaiting(flag1);
            }

            if (flag1) {
                return;
            }

            if (this.radiusPerTick != 0.0F) {
                f += this.radiusPerTick;
                if (f < 0.5F) {
                    this.discard();
                    return;
                }

                this.setRadius(f);
            }

            if (this.tickCount % 5 == 0) {
                this.victims.entrySet().removeIf((entry) -> {
                    return this.tickCount >= (Integer) entry.getValue();
                });
                if (!this.potionContents.hasEffects()) {
                    this.victims.clear();
                } else {
                    List<MobEffect> list = Lists.newArrayList();

                    if (this.potionContents.potion().isPresent()) {
                        Iterator iterator = ((PotionRegistry) ((Holder) this.potionContents.potion().get()).value()).getEffects().iterator();

                        while (iterator.hasNext()) {
                            MobEffect mobeffect = (MobEffect) iterator.next();

                            list.add(new MobEffect(mobeffect.getEffect(), mobeffect.mapDuration((k) -> {
                                return k / 4;
                            }), mobeffect.getAmplifier(), mobeffect.isAmbient(), mobeffect.isVisible()));
                        }
                    }

                    list.addAll(this.potionContents.customEffects());
                    List<EntityLiving> list1 = this.level().getEntitiesOfClass(EntityLiving.class, this.getBoundingBox());

                    if (!list1.isEmpty()) {
                        Iterator iterator1 = list1.iterator();

                        while (iterator1.hasNext()) {
                            EntityLiving entityliving = (EntityLiving) iterator1.next();

                            if (!this.victims.containsKey(entityliving) && entityliving.isAffectedByPotions()) {
                                double d3 = entityliving.getX() - this.getX();
                                double d4 = entityliving.getZ() - this.getZ();
                                double d5 = d3 * d3 + d4 * d4;

                                if (d5 <= (double) (f * f)) {
                                    this.victims.put(entityliving, this.tickCount + this.reapplicationDelay);
                                    Iterator iterator2 = list.iterator();

                                    while (iterator2.hasNext()) {
                                        MobEffect mobeffect1 = (MobEffect) iterator2.next();

                                        if (((MobEffectList) mobeffect1.getEffect().value()).isInstantenous()) {
                                            ((MobEffectList) mobeffect1.getEffect().value()).applyInstantenousEffect(this, this.getOwner(), entityliving, mobeffect1.getAmplifier(), 0.5D);
                                        } else {
                                            entityliving.addEffect(new MobEffect(mobeffect1), this);
                                        }
                                    }

                                    if (this.radiusOnUse != 0.0F) {
                                        f += this.radiusOnUse;
                                        if (f < 0.5F) {
                                            this.discard();
                                            return;
                                        }

                                        this.setRadius(f);
                                    }

                                    if (this.durationOnUse != 0) {
                                        this.duration += this.durationOnUse;
                                        if (this.duration <= 0) {
                                            this.discard();
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public float getRadiusOnUse() {
        return this.radiusOnUse;
    }

    public void setRadiusOnUse(float f) {
        this.radiusOnUse = f;
    }

    public float getRadiusPerTick() {
        return this.radiusPerTick;
    }

    public void setRadiusPerTick(float f) {
        this.radiusPerTick = f;
    }

    public int getDurationOnUse() {
        return this.durationOnUse;
    }

    public void setDurationOnUse(int i) {
        this.durationOnUse = i;
    }

    public int getWaitTime() {
        return this.waitTime;
    }

    public void setWaitTime(int i) {
        this.waitTime = i;
    }

    public void setOwner(@Nullable EntityLiving entityliving) {
        this.owner = entityliving;
        this.ownerUUID = entityliving == null ? null : entityliving.getUUID();
    }

    @Nullable
    @Override
    public EntityLiving getOwner() {
        if (this.owner == null && this.ownerUUID != null && this.level() instanceof WorldServer) {
            Entity entity = ((WorldServer) this.level()).getEntity(this.ownerUUID);

            if (entity instanceof EntityLiving) {
                this.owner = (EntityLiving) entity;
            }
        }

        return this.owner;
    }

    @Override
    protected void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        this.tickCount = nbttagcompound.getInt("Age");
        this.duration = nbttagcompound.getInt("Duration");
        this.waitTime = nbttagcompound.getInt("WaitTime");
        this.reapplicationDelay = nbttagcompound.getInt("ReapplicationDelay");
        this.durationOnUse = nbttagcompound.getInt("DurationOnUse");
        this.radiusOnUse = nbttagcompound.getFloat("RadiusOnUse");
        this.radiusPerTick = nbttagcompound.getFloat("RadiusPerTick");
        this.setRadius(nbttagcompound.getFloat("Radius"));
        if (nbttagcompound.hasUUID("Owner")) {
            this.ownerUUID = nbttagcompound.getUUID("Owner");
        }

        RegistryOps<NBTBase> registryops = this.registryAccess().createSerializationContext(DynamicOpsNBT.INSTANCE);

        if (nbttagcompound.contains("Particle", 10)) {
            Particles.CODEC.parse(registryops, nbttagcompound.get("Particle")).resultOrPartial((s) -> {
                EntityAreaEffectCloud.LOGGER.warn("Failed to parse area effect cloud particle options: '{}'", s);
            }).ifPresent(this::setParticle);
        }

        if (nbttagcompound.contains("potion_contents")) {
            PotionContents.CODEC.parse(registryops, nbttagcompound.get("potion_contents")).resultOrPartial((s) -> {
                EntityAreaEffectCloud.LOGGER.warn("Failed to parse area effect cloud potions: '{}'", s);
            }).ifPresent(this::setPotionContents);
        }

    }

    @Override
    protected void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        nbttagcompound.putInt("Age", this.tickCount);
        nbttagcompound.putInt("Duration", this.duration);
        nbttagcompound.putInt("WaitTime", this.waitTime);
        nbttagcompound.putInt("ReapplicationDelay", this.reapplicationDelay);
        nbttagcompound.putInt("DurationOnUse", this.durationOnUse);
        nbttagcompound.putFloat("RadiusOnUse", this.radiusOnUse);
        nbttagcompound.putFloat("RadiusPerTick", this.radiusPerTick);
        nbttagcompound.putFloat("Radius", this.getRadius());
        RegistryOps<NBTBase> registryops = this.registryAccess().createSerializationContext(DynamicOpsNBT.INSTANCE);

        nbttagcompound.put("Particle", (NBTBase) Particles.CODEC.encodeStart(registryops, this.getParticle()).getOrThrow());
        if (this.ownerUUID != null) {
            nbttagcompound.putUUID("Owner", this.ownerUUID);
        }

        if (!this.potionContents.equals(PotionContents.EMPTY)) {
            NBTBase nbtbase = (NBTBase) PotionContents.CODEC.encodeStart(registryops, this.potionContents).getOrThrow();

            nbttagcompound.put("potion_contents", nbtbase);
        }

    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        if (EntityAreaEffectCloud.DATA_RADIUS.equals(datawatcherobject)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(datawatcherobject);
    }

    @Override
    public EnumPistonReaction getPistonPushReaction() {
        return EnumPistonReaction.IGNORE;
    }

    @Override
    public EntitySize getDimensions(EntityPose entitypose) {
        return EntitySize.scalable(this.getRadius() * 2.0F, 0.5F);
    }
}
