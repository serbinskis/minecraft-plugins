package net.minecraft.world.item.alchemy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ColorUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record PotionContents(Optional<Holder<PotionRegistry>> potion, Optional<Integer> customColor, List<MobEffect> customEffects) {

    public static final PotionContents EMPTY = new PotionContents(Optional.empty(), Optional.empty(), List.of());
    private static final IChatBaseComponent NO_EFFECT = IChatBaseComponent.translatable("effect.none").withStyle(EnumChatFormat.GRAY);
    private static final int EMPTY_COLOR = -524040;
    private static final int BASE_POTION_COLOR = -13083194;
    private static final Codec<PotionContents> FULL_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(BuiltInRegistries.POTION.holderByNameCodec().optionalFieldOf("potion").forGetter(PotionContents::potion), Codec.INT.optionalFieldOf("custom_color").forGetter(PotionContents::customColor), MobEffect.CODEC.listOf().optionalFieldOf("custom_effects", List.of()).forGetter(PotionContents::customEffects)).apply(instance, PotionContents::new);
    });
    public static final Codec<PotionContents> CODEC = Codec.withAlternative(PotionContents.FULL_CODEC, BuiltInRegistries.POTION.holderByNameCodec(), PotionContents::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, PotionContents> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(Registries.POTION).apply(ByteBufCodecs::optional), PotionContents::potion, ByteBufCodecs.INT.apply(ByteBufCodecs::optional), PotionContents::customColor, MobEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), PotionContents::customEffects, PotionContents::new);

    public PotionContents(Holder<PotionRegistry> holder) {
        this(Optional.of(holder), Optional.empty(), List.of());
    }

    public static ItemStack createItemStack(Item item, Holder<PotionRegistry> holder) {
        ItemStack itemstack = new ItemStack(item);

        itemstack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
        return itemstack;
    }

    public boolean is(Holder<PotionRegistry> holder) {
        return this.potion.isPresent() && ((Holder) this.potion.get()).is(holder) && this.customEffects.isEmpty();
    }

    public Iterable<MobEffect> getAllEffects() {
        return (Iterable) (this.potion.isEmpty() ? this.customEffects : (this.customEffects.isEmpty() ? ((PotionRegistry) ((Holder) this.potion.get()).value()).getEffects() : Iterables.concat(((PotionRegistry) ((Holder) this.potion.get()).value()).getEffects(), this.customEffects)));
    }

    public void forEachEffect(Consumer<MobEffect> consumer) {
        Iterator iterator;
        MobEffect mobeffect;

        if (this.potion.isPresent()) {
            iterator = ((PotionRegistry) ((Holder) this.potion.get()).value()).getEffects().iterator();

            while (iterator.hasNext()) {
                mobeffect = (MobEffect) iterator.next();
                consumer.accept(new MobEffect(mobeffect));
            }
        }

        iterator = this.customEffects.iterator();

        while (iterator.hasNext()) {
            mobeffect = (MobEffect) iterator.next();
            consumer.accept(new MobEffect(mobeffect));
        }

    }

    public PotionContents withPotion(Holder<PotionRegistry> holder) {
        return new PotionContents(Optional.of(holder), this.customColor, this.customEffects);
    }

    public PotionContents withEffectAdded(MobEffect mobeffect) {
        return new PotionContents(this.potion, this.customColor, SystemUtils.copyAndAdd(this.customEffects, (Object) mobeffect));
    }

    public int getColor() {
        return this.customColor.isPresent() ? (Integer) this.customColor.get() : getColor(this.getAllEffects());
    }

    public static int getColor(Holder<PotionRegistry> holder) {
        return getColor((Iterable) ((PotionRegistry) holder.value()).getEffects());
    }

    public static int getColor(Iterable<MobEffect> iterable) {
        return getColorOptional(iterable).orElse(-13083194);
    }

    public static OptionalInt getColorOptional(Iterable<MobEffect> iterable) {
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;
        Iterator iterator = iterable.iterator();

        while (iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            if (mobeffect.isVisible()) {
                int i1 = ((MobEffectList) mobeffect.getEffect().value()).getColor();
                int j1 = mobeffect.getAmplifier() + 1;

                i += j1 * ColorUtil.b.red(i1);
                j += j1 * ColorUtil.b.green(i1);
                k += j1 * ColorUtil.b.blue(i1);
                l += j1;
            }
        }

        if (l == 0) {
            return OptionalInt.empty();
        } else {
            return OptionalInt.of(ColorUtil.b.color(i / l, j / l, k / l));
        }
    }

    public boolean hasEffects() {
        return !this.customEffects.isEmpty() ? true : this.potion.isPresent() && !((PotionRegistry) ((Holder) this.potion.get()).value()).getEffects().isEmpty();
    }

    public List<MobEffect> customEffects() {
        return Lists.transform(this.customEffects, MobEffect::new);
    }

    public void addPotionTooltip(Consumer<IChatBaseComponent> consumer, float f, float f1) {
        addPotionTooltip(this.getAllEffects(), consumer, f, f1);
    }

    public static void addPotionTooltip(Iterable<MobEffect> iterable, Consumer<IChatBaseComponent> consumer, float f, float f1) {
        List<Pair<Holder<AttributeBase>, AttributeModifier>> list = Lists.newArrayList();
        boolean flag = true;

        Iterator iterator;
        IChatMutableComponent ichatmutablecomponent;
        Holder holder;

        for (iterator = iterable.iterator(); iterator.hasNext(); consumer.accept(ichatmutablecomponent.withStyle(((MobEffectList) holder.value()).getCategory().getTooltipFormatting()))) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            flag = false;
            ichatmutablecomponent = IChatBaseComponent.translatable(mobeffect.getDescriptionId());
            holder = mobeffect.getEffect();
            ((MobEffectList) holder.value()).createModifiers(mobeffect.getAmplifier(), (holder1, attributemodifier) -> {
                list.add(new Pair(holder1, attributemodifier));
            });
            if (mobeffect.getAmplifier() > 0) {
                ichatmutablecomponent = IChatBaseComponent.translatable("potion.withAmplifier", ichatmutablecomponent, IChatBaseComponent.translatable("potion.potency." + mobeffect.getAmplifier()));
            }

            if (!mobeffect.endsWithin(20)) {
                ichatmutablecomponent = IChatBaseComponent.translatable("potion.withDuration", ichatmutablecomponent, MobEffectUtil.formatDuration(mobeffect, f, f1));
            }
        }

        if (flag) {
            consumer.accept(PotionContents.NO_EFFECT);
        }

        if (!list.isEmpty()) {
            consumer.accept(CommonComponents.EMPTY);
            consumer.accept(IChatBaseComponent.translatable("potion.whenDrank").withStyle(EnumChatFormat.DARK_PURPLE));
            iterator = list.iterator();

            while (iterator.hasNext()) {
                Pair<Holder<AttributeBase>, AttributeModifier> pair = (Pair) iterator.next();
                AttributeModifier attributemodifier = (AttributeModifier) pair.getSecond();
                double d0 = attributemodifier.amount();
                double d1;

                if (attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE && attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    d1 = attributemodifier.amount();
                } else {
                    d1 = attributemodifier.amount() * 100.0D;
                }

                if (d0 > 0.0D) {
                    consumer.accept(IChatBaseComponent.translatable("attribute.modifier.plus." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1), IChatBaseComponent.translatable(((AttributeBase) ((Holder) pair.getFirst()).value()).getDescriptionId())).withStyle(EnumChatFormat.BLUE));
                } else if (d0 < 0.0D) {
                    d1 *= -1.0D;
                    consumer.accept(IChatBaseComponent.translatable("attribute.modifier.take." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1), IChatBaseComponent.translatable(((AttributeBase) ((Holder) pair.getFirst()).value()).getDescriptionId())).withStyle(EnumChatFormat.RED));
                }
            }
        }

    }
}
