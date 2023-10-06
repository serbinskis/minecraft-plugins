package net.minecraft.world.effect;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeMapBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifiable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class MobEffectList {

    private final Map<AttributeBase, AttributeModifierTemplate> attributeModifiers = Maps.newHashMap();
    private final MobEffectInfo category;
    private final int color;
    @Nullable
    private String descriptionId;
    private Supplier<MobEffect.a> factorDataFactory = () -> {
        return null;
    };
    private final Holder.c<MobEffectList> builtInRegistryHolder;

    protected MobEffectList(MobEffectInfo mobeffectinfo, int i) {
        this.builtInRegistryHolder = BuiltInRegistries.MOB_EFFECT.createIntrusiveHolder(this);
        this.category = mobeffectinfo;
        this.color = i;
    }

    public Optional<MobEffect.a> createFactorData() {
        return Optional.ofNullable((MobEffect.a) this.factorDataFactory.get());
    }

    public void applyEffectTick(EntityLiving entityliving, int i) {}

    public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity1, EntityLiving entityliving, int i, double d0) {
        this.applyEffectTick(entityliving, i);
    }

    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        return false;
    }

    public void onEffectStarted(EntityLiving entityliving, int i) {}

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

    public MobEffectList addAttributeModifier(AttributeBase attributebase, String s, double d0, AttributeModifier.Operation attributemodifier_operation) {
        this.attributeModifiers.put(attributebase, new MobEffectList.a(UUID.fromString(s), d0, attributemodifier_operation));
        return this;
    }

    public MobEffectList setFactorDataFactory(Supplier<MobEffect.a> supplier) {
        this.factorDataFactory = supplier;
        return this;
    }

    public Map<AttributeBase, AttributeModifierTemplate> getAttributeModifiers() {
        return this.attributeModifiers;
    }

    public void removeAttributeModifiers(AttributeMapBase attributemapbase) {
        Iterator iterator = this.attributeModifiers.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<AttributeBase, AttributeModifierTemplate> entry = (Entry) iterator.next();
            AttributeModifiable attributemodifiable = attributemapbase.getInstance((AttributeBase) entry.getKey());

            if (attributemodifiable != null) {
                attributemodifiable.removeModifier(((AttributeModifierTemplate) entry.getValue()).getAttributeModifierId());
            }
        }

    }

    public void addAttributeModifiers(AttributeMapBase attributemapbase, int i) {
        Iterator iterator = this.attributeModifiers.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<AttributeBase, AttributeModifierTemplate> entry = (Entry) iterator.next();
            AttributeModifiable attributemodifiable = attributemapbase.getInstance((AttributeBase) entry.getKey());

            if (attributemodifiable != null) {
                attributemodifiable.removeModifier(((AttributeModifierTemplate) entry.getValue()).getAttributeModifierId());
                attributemodifiable.addPermanentModifier(((AttributeModifierTemplate) entry.getValue()).create(i));
            }
        }

    }

    public boolean isBeneficial() {
        return this.category == MobEffectInfo.BENEFICIAL;
    }

    /** @deprecated */
    @Deprecated
    public Holder.c<MobEffectList> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    private class a implements AttributeModifierTemplate {

        private final UUID id;
        private final double amount;
        private final AttributeModifier.Operation operation;

        public a(UUID uuid, double d0, AttributeModifier.Operation attributemodifier_operation) {
            this.id = uuid;
            this.amount = d0;
            this.operation = attributemodifier_operation;
        }

        @Override
        public UUID getAttributeModifierId() {
            return this.id;
        }

        @Override
        public AttributeModifier create(int i) {
            return new AttributeModifier(this.id, MobEffectList.this.getDescriptionId() + " " + i, this.amount * (double) (i + 1), this.operation);
        }
    }
}
