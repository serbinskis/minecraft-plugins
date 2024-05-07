package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.Particles;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.level.World;

public abstract class EntityIllagerWizard extends EntityIllagerAbstract {

    private static final DataWatcherObject<Byte> DATA_SPELL_CASTING_ID = DataWatcher.defineId(EntityIllagerWizard.class, DataWatcherRegistry.BYTE);
    protected int spellCastingTickCount;
    private EntityIllagerWizard.Spell currentSpell;

    protected EntityIllagerWizard(EntityTypes<? extends EntityIllagerWizard> entitytypes, World world) {
        super(entitytypes, world);
        this.currentSpell = EntityIllagerWizard.Spell.NONE;
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntityIllagerWizard.DATA_SPELL_CASTING_ID, (byte) 0);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.spellCastingTickCount = nbttagcompound.getInt("SpellTicks");
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putInt("SpellTicks", this.spellCastingTickCount);
    }

    @Override
    public EntityIllagerAbstract.a getArmPose() {
        return this.isCastingSpell() ? EntityIllagerAbstract.a.SPELLCASTING : (this.isCelebrating() ? EntityIllagerAbstract.a.CELEBRATING : EntityIllagerAbstract.a.CROSSED);
    }

    public boolean isCastingSpell() {
        return this.level().isClientSide ? (Byte) this.entityData.get(EntityIllagerWizard.DATA_SPELL_CASTING_ID) > 0 : this.spellCastingTickCount > 0;
    }

    public void setIsCastingSpell(EntityIllagerWizard.Spell entityillagerwizard_spell) {
        this.currentSpell = entityillagerwizard_spell;
        this.entityData.set(EntityIllagerWizard.DATA_SPELL_CASTING_ID, (byte) entityillagerwizard_spell.id);
    }

    public EntityIllagerWizard.Spell getCurrentSpell() {
        return !this.level().isClientSide ? this.currentSpell : EntityIllagerWizard.Spell.byId((Byte) this.entityData.get(EntityIllagerWizard.DATA_SPELL_CASTING_ID));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.spellCastingTickCount > 0) {
            --this.spellCastingTickCount;
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && this.isCastingSpell()) {
            EntityIllagerWizard.Spell entityillagerwizard_spell = this.getCurrentSpell();
            float f = (float) entityillagerwizard_spell.spellColor[0];
            float f1 = (float) entityillagerwizard_spell.spellColor[1];
            float f2 = (float) entityillagerwizard_spell.spellColor[2];
            float f3 = this.yBodyRot * 0.017453292F + MathHelper.cos((float) this.tickCount * 0.6662F) * 0.25F;
            float f4 = MathHelper.cos(f3);
            float f5 = MathHelper.sin(f3);
            double d0 = 0.6D * (double) this.getScale();
            double d1 = 1.8D * (double) this.getScale();

            this.level().addParticle(ColorParticleOption.create(Particles.ENTITY_EFFECT, f, f1, f2), this.getX() + (double) f4 * d0, this.getY() + d1, this.getZ() + (double) f5 * d0, 0.0D, 0.0D, 0.0D);
            this.level().addParticle(ColorParticleOption.create(Particles.ENTITY_EFFECT, f, f1, f2), this.getX() - (double) f4 * d0, this.getY() + d1, this.getZ() - (double) f5 * d0, 0.0D, 0.0D, 0.0D);
        }

    }

    protected int getSpellCastingTime() {
        return this.spellCastingTickCount;
    }

    protected abstract SoundEffect getCastingSoundEvent();

    public static enum Spell {

        NONE(0, 0.0D, 0.0D, 0.0D), SUMMON_VEX(1, 0.7D, 0.7D, 0.8D), FANGS(2, 0.4D, 0.3D, 0.35D), WOLOLO(3, 0.7D, 0.5D, 0.2D), DISAPPEAR(4, 0.3D, 0.3D, 0.8D), BLINDNESS(5, 0.1D, 0.1D, 0.2D);

        private static final IntFunction<EntityIllagerWizard.Spell> BY_ID = ByIdMap.continuous((entityillagerwizard_spell) -> {
            return entityillagerwizard_spell.id;
        }, values(), ByIdMap.a.ZERO);
        final int id;
        final double[] spellColor;

        private Spell(final int i, final double d0, final double d1, final double d2) {
            this.id = i;
            this.spellColor = new double[]{d0, d1, d2};
        }

        public static EntityIllagerWizard.Spell byId(int i) {
            return (EntityIllagerWizard.Spell) EntityIllagerWizard.Spell.BY_ID.apply(i);
        }
    }

    protected abstract class PathfinderGoalCastSpell extends PathfinderGoal {

        protected int attackWarmupDelay;
        protected int nextAttackTickCount;

        protected PathfinderGoalCastSpell() {}

        @Override
        public boolean canUse() {
            EntityLiving entityliving = EntityIllagerWizard.this.getTarget();

            return entityliving != null && entityliving.isAlive() ? (EntityIllagerWizard.this.isCastingSpell() ? false : EntityIllagerWizard.this.tickCount >= this.nextAttackTickCount) : false;
        }

        @Override
        public boolean canContinueToUse() {
            EntityLiving entityliving = EntityIllagerWizard.this.getTarget();

            return entityliving != null && entityliving.isAlive() && this.attackWarmupDelay > 0;
        }

        @Override
        public void start() {
            this.attackWarmupDelay = this.adjustedTickDelay(this.getCastWarmupTime());
            EntityIllagerWizard.this.spellCastingTickCount = this.getCastingTime();
            this.nextAttackTickCount = EntityIllagerWizard.this.tickCount + this.getCastingInterval();
            SoundEffect soundeffect = this.getSpellPrepareSound();

            if (soundeffect != null) {
                EntityIllagerWizard.this.playSound(soundeffect, 1.0F, 1.0F);
            }

            EntityIllagerWizard.this.setIsCastingSpell(this.getSpell());
        }

        @Override
        public void tick() {
            --this.attackWarmupDelay;
            if (this.attackWarmupDelay == 0) {
                this.performSpellCasting();
                EntityIllagerWizard.this.playSound(EntityIllagerWizard.this.getCastingSoundEvent(), 1.0F, 1.0F);
            }

        }

        protected abstract void performSpellCasting();

        protected int getCastWarmupTime() {
            return 20;
        }

        protected abstract int getCastingTime();

        protected abstract int getCastingInterval();

        @Nullable
        protected abstract SoundEffect getSpellPrepareSound();

        protected abstract EntityIllagerWizard.Spell getSpell();
    }

    protected class b extends PathfinderGoal {

        public b() {
            this.setFlags(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean canUse() {
            return EntityIllagerWizard.this.getSpellCastingTime() > 0;
        }

        @Override
        public void start() {
            super.start();
            EntityIllagerWizard.this.navigation.stop();
        }

        @Override
        public void stop() {
            super.stop();
            EntityIllagerWizard.this.setIsCastingSpell(EntityIllagerWizard.Spell.NONE);
        }

        @Override
        public void tick() {
            if (EntityIllagerWizard.this.getTarget() != null) {
                EntityIllagerWizard.this.getLookControl().setLookAt(EntityIllagerWizard.this.getTarget(), (float) EntityIllagerWizard.this.getMaxHeadYRot(), (float) EntityIllagerWizard.this.getMaxHeadXRot());
            }

        }
    }
}
