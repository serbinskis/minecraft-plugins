package net.minecraft.world.effect;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.util.ColorUtil;
import net.minecraft.util.MathHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeMapBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifiable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class MobEffectList implements FeatureElement {

    private static final int AMBIENT_ALPHA = MathHelper.floor(38.25F);
    private final Map<Holder<AttributeBase>, MobEffectList.a> attributeModifiers = new Object2ObjectOpenHashMap();
    private final MobEffectInfo category;
    private final int color;
    private final Function<MobEffect, ParticleParam> particleFactory;
    @Nullable
    private String descriptionId;
    private int blendDurationTicks;
    private Optional<SoundEffect> soundOnAdded = Optional.empty();
    private FeatureFlagSet requiredFeatures;

    protected MobEffectList(MobEffectInfo mobeffectinfo, int i) {
        this.requiredFeatures = FeatureFlags.VANILLA_SET;
        this.category = mobeffectinfo;
        this.color = i;
        this.particleFactory = (mobeffect) -> {
            int j = mobeffect.isAmbient() ? MobEffectList.AMBIENT_ALPHA : 255;

            return ColorParticleOption.create(Particles.ENTITY_EFFECT, ColorUtil.b.color(j, i));
        };
    }

    protected MobEffectList(MobEffectInfo mobeffectinfo, int i, ParticleParam particleparam) {
        this.requiredFeatures = FeatureFlags.VANILLA_SET;
        this.category = mobeffectinfo;
        this.color = i;
        this.particleFactory = (mobeffect) -> {
            return particleparam;
        };
    }

    public int getBlendDurationTicks() {
        return this.blendDurationTicks;
    }

    public boolean applyEffectTick(EntityLiving entityliving, int i) {
        return true;
    }

    public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity1, EntityLiving entityliving, int i, double d0) {
        this.applyEffectTick(entityliving, i);
    }

    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        return false;
    }

    public void onEffectStarted(EntityLiving entityliving, int i) {}

    public void onEffectAdded(EntityLiving entityliving, int i) {
        this.soundOnAdded.ifPresent((soundeffect) -> {
            entityliving.level().playSound((EntityHuman) null, entityliving.getX(), entityliving.getY(), entityliving.getZ(), soundeffect, entityliving.getSoundSource(), 1.0F, 1.0F);
        });
    }

    public void onMobRemoved(EntityLiving entityliving, int i, Entity.RemovalReason entity_removalreason) {}

    public void onMobHurt(EntityLiving entityliving, int i, DamageSource damagesource, float f) {}

    public boolean isInstantenous() {
        return false;
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = SystemUtils.makeDescriptionId("effect", BuiltInRegistries.MOB_EFFECT.getKey(this));
        }

        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public IChatBaseComponent getDisplayName() {
        return IChatBaseComponent.translatable(this.getDescriptionId());
    }

    public MobEffectInfo getCategory() {
        return this.category;
    }

    public int getColor() {
        return this.color;
    }

    public MobEffectList addAttributeModifier(Holder<AttributeBase> holder, String s, double d0, AttributeModifier.Operation attributemodifier_operation) {
        this.attributeModifiers.put(holder, new MobEffectList.a(UUID.fromString(s), d0, attributemodifier_operation));
        return this;
    }

    public MobEffectList setBlendDuration(int i) {
        this.blendDurationTicks = i;
        return this;
    }

    public void createModifiers(int i, BiConsumer<Holder<AttributeBase>, AttributeModifier> biconsumer) {
        this.attributeModifiers.forEach((holder, mobeffectlist_a) -> {
            biconsumer.accept(holder, mobeffectlist_a.create(this.getDescriptionId(), i));
        });
    }

    public void removeAttributeModifiers(AttributeMapBase attributemapbase) {
        Iterator iterator = this.attributeModifiers.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<Holder<AttributeBase>, MobEffectList.a> entry = (Entry) iterator.next();
            AttributeModifiable attributemodifiable = attributemapbase.getInstance((Holder) entry.getKey());

            if (attributemodifiable != null) {
                attributemodifiable.removeModifier(((MobEffectList.a) entry.getValue()).id());
            }
        }

    }

    public void addAttributeModifiers(AttributeMapBase attributemapbase, int i) {
        Iterator iterator = this.attributeModifiers.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<Holder<AttributeBase>, MobEffectList.a> entry = (Entry) iterator.next();
            AttributeModifiable attributemodifiable = attributemapbase.getInstance((Holder) entry.getKey());

            if (attributemodifiable != null) {
                attributemodifiable.removeModifier(((MobEffectList.a) entry.getValue()).id());
                attributemodifiable.addPermanentModifier(((MobEffectList.a) entry.getValue()).create(this.getDescriptionId(), i));
            }
        }

    }

    public boolean isBeneficial() {
        return this.category == MobEffectInfo.BENEFICIAL;
    }

    public ParticleParam createParticleOptions(MobEffect mobeffect) {
        return (ParticleParam) this.particleFactory.apply(mobeffect);
    }

    public MobEffectList withSoundOnAdded(SoundEffect soundeffect) {
        this.soundOnAdded = Optional.of(soundeffect);
        return this;
    }

    public MobEffectList requiredFeatures(FeatureFlag... afeatureflag) {
        this.requiredFeatures = FeatureFlags.REGISTRY.subset(afeatureflag);
        return this;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    private static record a(UUID id, double amount, AttributeModifier.Operation operation) {

        public AttributeModifier create(String s, int i) {
            return new AttributeModifier(this.id, s + " " + i, this.amount * (double) (i + 1), this.operation);
        }
    }
}
