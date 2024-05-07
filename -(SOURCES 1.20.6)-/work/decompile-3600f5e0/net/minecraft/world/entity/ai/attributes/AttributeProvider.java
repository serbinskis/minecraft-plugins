package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;

public class AttributeProvider {

    private final Map<Holder<AttributeBase>, AttributeModifiable> instances;

    AttributeProvider(Map<Holder<AttributeBase>, AttributeModifiable> map) {
        this.instances = map;
    }

    private AttributeModifiable getAttributeInstance(Holder<AttributeBase> holder) {
        AttributeModifiable attributemodifiable = (AttributeModifiable) this.instances.get(holder);

        if (attributemodifiable == null) {
            throw new IllegalArgumentException("Can't find attribute " + holder.getRegisteredName());
        } else {
            return attributemodifiable;
        }
    }

    public double getValue(Holder<AttributeBase> holder) {
        return this.getAttributeInstance(holder).getValue();
    }

    public double getBaseValue(Holder<AttributeBase> holder) {
        return this.getAttributeInstance(holder).getBaseValue();
    }

    public double getModifierValue(Holder<AttributeBase> holder, UUID uuid) {
        AttributeModifier attributemodifier = this.getAttributeInstance(holder).getModifier(uuid);

        if (attributemodifier == null) {
            String s = String.valueOf(uuid);

            throw new IllegalArgumentException("Can't find modifier " + s + " on attribute " + holder.getRegisteredName());
        } else {
            return attributemodifier.amount();
        }
    }

    @Nullable
    public AttributeModifiable createInstance(Consumer<AttributeModifiable> consumer, Holder<AttributeBase> holder) {
        AttributeModifiable attributemodifiable = (AttributeModifiable) this.instances.get(holder);

        if (attributemodifiable == null) {
            return null;
        } else {
            AttributeModifiable attributemodifiable1 = new AttributeModifiable(holder, consumer);

            attributemodifiable1.replaceFrom(attributemodifiable);
            return attributemodifiable1;
        }
    }

    public static AttributeProvider.Builder builder() {
        return new AttributeProvider.Builder();
    }

    public boolean hasAttribute(Holder<AttributeBase> holder) {
        return this.instances.containsKey(holder);
    }

    public boolean hasModifier(Holder<AttributeBase> holder, UUID uuid) {
        AttributeModifiable attributemodifiable = (AttributeModifiable) this.instances.get(holder);

        return attributemodifiable != null && attributemodifiable.getModifier(uuid) != null;
    }

    public static class Builder {

        private final com.google.common.collect.ImmutableMap.Builder<Holder<AttributeBase>, AttributeModifiable> builder = ImmutableMap.builder();
        private boolean instanceFrozen;

        public Builder() {}

        private AttributeModifiable create(Holder<AttributeBase> holder) {
            AttributeModifiable attributemodifiable = new AttributeModifiable(holder, (attributemodifiable1) -> {
                if (this.instanceFrozen) {
                    throw new UnsupportedOperationException("Tried to change value for default attribute instance: " + holder.getRegisteredName());
                }
            });

            this.builder.put(holder, attributemodifiable);
            return attributemodifiable;
        }

        public AttributeProvider.Builder add(Holder<AttributeBase> holder) {
            this.create(holder);
            return this;
        }

        public AttributeProvider.Builder add(Holder<AttributeBase> holder, double d0) {
            AttributeModifiable attributemodifiable = this.create(holder);

            attributemodifiable.setBaseValue(d0);
            return this;
        }

        public AttributeProvider build() {
            this.instanceFrozen = true;
            return new AttributeProvider(this.builder.buildKeepingLast());
        }
    }
}
