package net.minecraft.world.effect;

import com.google.common.collect.ComparisonChain;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.MathHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import org.slf4j.Logger;

public class MobEffect implements Comparable<MobEffect> {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int INFINITE_DURATION = -1;
    public static final int MIN_AMPLIFIER = 0;
    public static final int MAX_AMPLIFIER = 255;
    public static final Codec<MobEffect> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(MobEffectList.CODEC.fieldOf("id").forGetter(MobEffect::getEffect), MobEffect.b.MAP_CODEC.forGetter(MobEffect::asDetails)).apply(instance, MobEffect::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, MobEffect> STREAM_CODEC = StreamCodec.composite(MobEffectList.STREAM_CODEC, MobEffect::getEffect, MobEffect.b.STREAM_CODEC, MobEffect::asDetails, MobEffect::new);
    private final Holder<MobEffectList> effect;
    private int duration;
    private int amplifier;
    private boolean ambient;
    private boolean visible;
    private boolean showIcon;
    @Nullable
    private MobEffect hiddenEffect;
    private final MobEffect.a blendState;

    public MobEffect(Holder<MobEffectList> holder) {
        this(holder, 0, 0);
    }

    public MobEffect(Holder<MobEffectList> holder, int i) {
        this(holder, i, 0);
    }

    public MobEffect(Holder<MobEffectList> holder, int i, int j) {
        this(holder, i, j, false, true);
    }

    public MobEffect(Holder<MobEffectList> holder, int i, int j, boolean flag, boolean flag1) {
        this(holder, i, j, flag, flag1, flag1);
    }

    public MobEffect(Holder<MobEffectList> holder, int i, int j, boolean flag, boolean flag1, boolean flag2) {
        this(holder, i, j, flag, flag1, flag2, (MobEffect) null);
    }

    public MobEffect(Holder<MobEffectList> holder, int i, int j, boolean flag, boolean flag1, boolean flag2, @Nullable MobEffect mobeffect) {
        this.blendState = new MobEffect.a();
        this.effect = holder;
        this.duration = i;
        this.amplifier = MathHelper.clamp(j, 0, 255);
        this.ambient = flag;
        this.visible = flag1;
        this.showIcon = flag2;
        this.hiddenEffect = mobeffect;
    }

    public MobEffect(MobEffect mobeffect) {
        this.blendState = new MobEffect.a();
        this.effect = mobeffect.effect;
        this.setDetailsFrom(mobeffect);
    }

    private MobEffect(Holder<MobEffectList> holder, MobEffect.b mobeffect_b) {
        this(holder, mobeffect_b.duration(), mobeffect_b.amplifier(), mobeffect_b.ambient(), mobeffect_b.showParticles(), mobeffect_b.showIcon(), (MobEffect) mobeffect_b.hiddenEffect().map((mobeffect_b1) -> {
            return new MobEffect(holder, mobeffect_b1);
        }).orElse((Object) null));
    }

    private MobEffect.b asDetails() {
        return new MobEffect.b(this.getAmplifier(), this.getDuration(), this.isAmbient(), this.isVisible(), this.showIcon(), Optional.ofNullable(this.hiddenEffect).map(MobEffect::asDetails));
    }

    public float getBlendFactor(EntityLiving entityliving, float f) {
        return this.blendState.getFactor(entityliving, f);
    }

    public ParticleParam getParticleOptions() {
        return ((MobEffectList) this.effect.value()).createParticleOptions(this);
    }

    void setDetailsFrom(MobEffect mobeffect) {
        this.duration = mobeffect.duration;
        this.amplifier = mobeffect.amplifier;
        this.ambient = mobeffect.ambient;
        this.visible = mobeffect.visible;
        this.showIcon = mobeffect.showIcon;
    }

    public boolean update(MobEffect mobeffect) {
        if (!this.effect.equals(mobeffect.effect)) {
            MobEffect.LOGGER.warn("This method should only be called for matching effects!");
        }

        boolean flag = false;

        if (mobeffect.amplifier > this.amplifier) {
            if (mobeffect.isShorterDurationThan(this)) {
                MobEffect mobeffect1 = this.hiddenEffect;

                this.hiddenEffect = new MobEffect(this);
                this.hiddenEffect.hiddenEffect = mobeffect1;
            }

            this.amplifier = mobeffect.amplifier;
            this.duration = mobeffect.duration;
            flag = true;
        } else if (this.isShorterDurationThan(mobeffect)) {
            if (mobeffect.amplifier == this.amplifier) {
                this.duration = mobeffect.duration;
                flag = true;
            } else if (this.hiddenEffect == null) {
                this.hiddenEffect = new MobEffect(mobeffect);
            } else {
                this.hiddenEffect.update(mobeffect);
            }
        }

        if (!mobeffect.ambient && this.ambient || flag) {
            this.ambient = mobeffect.ambient;
            flag = true;
        }

        if (mobeffect.visible != this.visible) {
            this.visible = mobeffect.visible;
            flag = true;
        }

        if (mobeffect.showIcon != this.showIcon) {
            this.showIcon = mobeffect.showIcon;
            flag = true;
        }

        return flag;
    }

    private boolean isShorterDurationThan(MobEffect mobeffect) {
        return !this.isInfiniteDuration() && (this.duration < mobeffect.duration || mobeffect.isInfiniteDuration());
    }

    public boolean isInfiniteDuration() {
        return this.duration == -1;
    }

    public boolean endsWithin(int i) {
        return !this.isInfiniteDuration() && this.duration <= i;
    }

    public int mapDuration(Int2IntFunction int2intfunction) {
        return !this.isInfiniteDuration() && this.duration != 0 ? int2intfunction.applyAsInt(this.duration) : this.duration;
    }

    public Holder<MobEffectList> getEffect() {
        return this.effect;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getAmplifier() {
        return this.amplifier;
    }

    public boolean isAmbient() {
        return this.ambient;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean showIcon() {
        return this.showIcon;
    }

    public boolean tick(EntityLiving entityliving, Runnable runnable) {
        if (this.hasRemainingDuration()) {
            int i = this.isInfiniteDuration() ? entityliving.tickCount : this.duration;

            if (((MobEffectList) this.effect.value()).shouldApplyEffectTickThisTick(i, this.amplifier) && !((MobEffectList) this.effect.value()).applyEffectTick(entityliving, this.amplifier)) {
                entityliving.removeEffect(this.effect);
            }

            this.tickDownDuration();
            if (this.duration == 0 && this.hiddenEffect != null) {
                this.setDetailsFrom(this.hiddenEffect);
                this.hiddenEffect = this.hiddenEffect.hiddenEffect;
                runnable.run();
            }
        }

        this.blendState.tick(this);
        return this.hasRemainingDuration();
    }

    private boolean hasRemainingDuration() {
        return this.isInfiniteDuration() || this.duration > 0;
    }

    private int tickDownDuration() {
        if (this.hiddenEffect != null) {
            this.hiddenEffect.tickDownDuration();
        }

        return this.duration = this.mapDuration((i) -> {
            return i - 1;
        });
    }

    public void onEffectStarted(EntityLiving entityliving) {
        ((MobEffectList) this.effect.value()).onEffectStarted(entityliving, this.amplifier);
    }

    public void onMobRemoved(EntityLiving entityliving, Entity.RemovalReason entity_removalreason) {
        ((MobEffectList) this.effect.value()).onMobRemoved(entityliving, this.amplifier, entity_removalreason);
    }

    public void onMobHurt(EntityLiving entityliving, DamageSource damagesource, float f) {
        ((MobEffectList) this.effect.value()).onMobHurt(entityliving, this.amplifier, damagesource, f);
    }

    public String getDescriptionId() {
        return ((MobEffectList) this.effect.value()).getDescriptionId();
    }

    public String toString() {
        String s;
        String s1;

        if (this.amplifier > 0) {
            s = this.getDescriptionId();
            s1 = s + " x " + (this.amplifier + 1) + ", Duration: " + this.describeDuration();
        } else {
            s = this.getDescriptionId();
            s1 = s + ", Duration: " + this.describeDuration();
        }

        if (!this.visible) {
            s1 = s1 + ", Particles: false";
        }

        if (!this.showIcon) {
            s1 = s1 + ", Show Icon: false";
        }

        return s1;
    }

    private String describeDuration() {
        return this.isInfiniteDuration() ? "infinite" : Integer.toString(this.duration);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof MobEffect)) {
            return false;
        } else {
            MobEffect mobeffect = (MobEffect) object;

            return this.duration == mobeffect.duration && this.amplifier == mobeffect.amplifier && this.ambient == mobeffect.ambient && this.visible == mobeffect.visible && this.showIcon == mobeffect.showIcon && this.effect.equals(mobeffect.effect);
        }
    }

    public int hashCode() {
        int i = this.effect.hashCode();

        i = 31 * i + this.duration;
        i = 31 * i + this.amplifier;
        i = 31 * i + (this.ambient ? 1 : 0);
        i = 31 * i + (this.visible ? 1 : 0);
        i = 31 * i + (this.showIcon ? 1 : 0);
        return i;
    }

    public NBTBase save() {
        return (NBTBase) MobEffect.CODEC.encodeStart(DynamicOpsNBT.INSTANCE, this).getOrThrow();
    }

    @Nullable
    public static MobEffect load(NBTTagCompound nbttagcompound) {
        DataResult dataresult = MobEffect.CODEC.parse(DynamicOpsNBT.INSTANCE, nbttagcompound);
        Logger logger = MobEffect.LOGGER;

        Objects.requireNonNull(logger);
        return (MobEffect) dataresult.resultOrPartial(logger::error).orElse((Object) null);
    }

    public int compareTo(MobEffect mobeffect) {
        boolean flag = true;

        return (this.getDuration() <= 32147 || mobeffect.getDuration() <= 32147) && (!this.isAmbient() || !mobeffect.isAmbient()) ? ComparisonChain.start().compareFalseFirst(this.isAmbient(), mobeffect.isAmbient()).compareFalseFirst(this.isInfiniteDuration(), mobeffect.isInfiniteDuration()).compare(this.getDuration(), mobeffect.getDuration()).compare(((MobEffectList) this.getEffect().value()).getColor(), ((MobEffectList) mobeffect.getEffect().value()).getColor()).result() : ComparisonChain.start().compare(this.isAmbient(), mobeffect.isAmbient()).compare(((MobEffectList) this.getEffect().value()).getColor(), ((MobEffectList) mobeffect.getEffect().value()).getColor()).result();
    }

    public void onEffectAdded(EntityLiving entityliving) {
        ((MobEffectList) this.effect.value()).onEffectAdded(entityliving, this.amplifier);
    }

    public boolean is(Holder<MobEffectList> holder) {
        return this.effect.equals(holder);
    }

    public void copyBlendState(MobEffect mobeffect) {
        this.blendState.copyFrom(mobeffect.blendState);
    }

    public void skipBlending() {
        this.blendState.setImmediate(this);
    }

    private static class a {

        private float factor;
        private float factorPreviousFrame;

        a() {}

        public void setImmediate(MobEffect mobeffect) {
            this.factor = computeTarget(mobeffect);
            this.factorPreviousFrame = this.factor;
        }

        public void copyFrom(MobEffect.a mobeffect_a) {
            this.factor = mobeffect_a.factor;
            this.factorPreviousFrame = mobeffect_a.factorPreviousFrame;
        }

        public void tick(MobEffect mobeffect) {
            this.factorPreviousFrame = this.factor;
            int i = getBlendDuration(mobeffect);

            if (i == 0) {
                this.factor = 1.0F;
            } else {
                float f = computeTarget(mobeffect);

                if (this.factor != f) {
                    float f1 = 1.0F / (float) i;

                    this.factor += MathHelper.clamp(f - this.factor, -f1, f1);
                }

            }
        }

        private static float computeTarget(MobEffect mobeffect) {
            boolean flag = !mobeffect.endsWithin(getBlendDuration(mobeffect));

            return flag ? 1.0F : 0.0F;
        }

        private static int getBlendDuration(MobEffect mobeffect) {
            return ((MobEffectList) mobeffect.getEffect().value()).getBlendDurationTicks();
        }

        public float getFactor(EntityLiving entityliving, float f) {
            if (entityliving.isRemoved()) {
                this.factorPreviousFrame = this.factor;
            }

            return MathHelper.lerp(f, this.factorPreviousFrame, this.factor);
        }
    }

    private static record b(int amplifier, int duration, boolean ambient, boolean showParticles, boolean showIcon, Optional<MobEffect.b> hiddenEffect) {

        public static final MapCodec<MobEffect.b> MAP_CODEC = MapCodec.recursive("MobEffectInstance.Details", (codec) -> {
            return RecordCodecBuilder.mapCodec((instance) -> {
                return instance.group(ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("amplifier", 0).forGetter(MobEffect.b::amplifier), Codec.INT.optionalFieldOf("duration", 0).forGetter(MobEffect.b::duration), Codec.BOOL.optionalFieldOf("ambient", false).forGetter(MobEffect.b::ambient), Codec.BOOL.optionalFieldOf("show_particles", true).forGetter(MobEffect.b::showParticles), Codec.BOOL.optionalFieldOf("show_icon").forGetter((mobeffect_b) -> {
                    return Optional.of(mobeffect_b.showIcon());
                }), codec.optionalFieldOf("hidden_effect").forGetter(MobEffect.b::hiddenEffect)).apply(instance, MobEffect.b::create);
            });
        });
        public static final StreamCodec<ByteBuf, MobEffect.b> STREAM_CODEC = StreamCodec.recursive((streamcodec) -> {
            return StreamCodec.composite(ByteBufCodecs.VAR_INT, MobEffect.b::amplifier, ByteBufCodecs.VAR_INT, MobEffect.b::duration, ByteBufCodecs.BOOL, MobEffect.b::ambient, ByteBufCodecs.BOOL, MobEffect.b::showParticles, ByteBufCodecs.BOOL, MobEffect.b::showIcon, streamcodec.apply(ByteBufCodecs::optional), MobEffect.b::hiddenEffect, MobEffect.b::new);
        });

        private static MobEffect.b create(int i, int j, boolean flag, boolean flag1, Optional<Boolean> optional, Optional<MobEffect.b> optional1) {
            return new MobEffect.b(i, j, flag, flag1, (Boolean) optional.orElse(flag1), optional1);
        }
    }
}
