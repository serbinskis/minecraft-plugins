package net.minecraft.world.entity.projectile;

import java.util.Iterator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.PotionRegistry;
import net.minecraft.world.level.World;

public class EntityTippedArrow extends EntityArrow {

    private static final int EXPOSED_POTION_DECAY_TIME = 600;
    private static final int NO_EFFECT_COLOR = -1;
    private static final DataWatcherObject<Integer> ID_EFFECT_COLOR = DataWatcher.defineId(EntityTippedArrow.class, DataWatcherRegistry.INT);
    private static final byte EVENT_POTION_PUFF = 0;

    public EntityTippedArrow(EntityTypes<? extends EntityTippedArrow> entitytypes, World world) {
        super(entitytypes, world);
    }

    public EntityTippedArrow(World world, double d0, double d1, double d2, ItemStack itemstack) {
        super(EntityTypes.ARROW, d0, d1, d2, world, itemstack);
        this.updateColor();
    }

    public EntityTippedArrow(World world, EntityLiving entityliving, ItemStack itemstack) {
        super(EntityTypes.ARROW, entityliving, world, itemstack);
        this.updateColor();
    }

    public PotionContents getPotionContents() {
        return (PotionContents) this.getPickupItemStackOrigin().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }

    public void setPotionContents(PotionContents potioncontents) {
        this.getPickupItemStackOrigin().set(DataComponents.POTION_CONTENTS, potioncontents);
        this.updateColor();
    }

    @Override
    protected void setPickupItemStack(ItemStack itemstack) {
        super.setPickupItemStack(itemstack);
        this.updateColor();
    }

    public void updateColor() {
        PotionContents potioncontents = this.getPotionContents();

        this.entityData.set(EntityTippedArrow.ID_EFFECT_COLOR, potioncontents.equals(PotionContents.EMPTY) ? -1 : potioncontents.getColor());
    }

    public void addEffect(MobEffect mobeffect) {
        this.setPotionContents(this.getPotionContents().withEffectAdded(mobeffect));
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityTippedArrow.ID_EFFECT_COLOR, -1);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (this.inGround) {
                if (this.inGroundTime % 5 == 0) {
                    this.makeParticle(1);
                }
            } else {
                this.makeParticle(2);
            }
        } else if (this.inGround && this.inGroundTime != 0 && !this.getPotionContents().equals(PotionContents.EMPTY) && this.inGroundTime >= 600) {
            this.level().broadcastEntityEvent(this, (byte) 0);
            this.setPickupItemStack(new ItemStack(Items.ARROW));
        }

    }

    private void makeParticle(int i) {
        int j = this.getColor();

        if (j != -1 && i > 0) {
            for (int k = 0; k < i; ++k) {
                this.level().addParticle(ColorParticleOption.create(Particles.ENTITY_EFFECT, j), this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
            }

        }
    }

    public int getColor() {
        return (Integer) this.entityData.get(EntityTippedArrow.ID_EFFECT_COLOR);
    }

    @Override
    protected void doPostHurtEffects(EntityLiving entityliving) {
        super.doPostHurtEffects(entityliving);
        Entity entity = this.getEffectSource();
        PotionContents potioncontents = this.getPotionContents();
        Iterator iterator;
        MobEffect mobeffect;

        if (potioncontents.potion().isPresent()) {
            iterator = ((PotionRegistry) ((Holder) potioncontents.potion().get()).value()).getEffects().iterator();

            while (iterator.hasNext()) {
                mobeffect = (MobEffect) iterator.next();
                entityliving.addEffect(new MobEffect(mobeffect.getEffect(), Math.max(mobeffect.mapDuration((i) -> {
                    return i / 8;
                }), 1), mobeffect.getAmplifier(), mobeffect.isAmbient(), mobeffect.isVisible()), entity, org.bukkit.event.entity.EntityPotionEffectEvent.Cause.ARROW); // CraftBukkit
            }
        }

        iterator = potioncontents.customEffects().iterator();

        while (iterator.hasNext()) {
            mobeffect = (MobEffect) iterator.next();
            entityliving.addEffect(mobeffect, entity, org.bukkit.event.entity.EntityPotionEffectEvent.Cause.ARROW); // CraftBukkit
        }

    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public void handleEntityEvent(byte b0) {
        if (b0 == 0) {
            int i = this.getColor();

            if (i != -1) {
                float f = (float) (i >> 16 & 255) / 255.0F;
                float f1 = (float) (i >> 8 & 255) / 255.0F;
                float f2 = (float) (i >> 0 & 255) / 255.0F;

                for (int j = 0; j < 20; ++j) {
                    this.level().addParticle(ColorParticleOption.create(Particles.ENTITY_EFFECT, f, f1, f2), this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
                }
            }
        } else {
            super.handleEntityEvent(b0);
        }

    }
}
