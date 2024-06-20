package net.minecraft.world.item.enchantment.effects;

import com.google.common.collect.HashMultimap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.INamable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3D;

public record EnchantmentAttributeEffect(MinecraftKey id, Holder<AttributeBase> attribute, LevelBasedValue amount, AttributeModifier.Operation operation) implements EnchantmentLocationBasedEffect {

    public static final MapCodec<EnchantmentAttributeEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(MinecraftKey.CODEC.fieldOf("id").forGetter(EnchantmentAttributeEffect::id), AttributeBase.CODEC.fieldOf("attribute").forGetter(EnchantmentAttributeEffect::attribute), LevelBasedValue.CODEC.fieldOf("amount").forGetter(EnchantmentAttributeEffect::amount), AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(EnchantmentAttributeEffect::operation)).apply(instance, EnchantmentAttributeEffect::new);
    });

    private MinecraftKey idForSlot(INamable inamable) {
        return this.id.withSuffix("/" + inamable.getSerializedName());
    }

    public AttributeModifier getModifier(int i, INamable inamable) {
        return new AttributeModifier(this.idForSlot(inamable), (double) this.amount().calculate(i), this.operation());
    }

    @Override
    public void onChangedBlock(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d, boolean flag) {
        if (flag && entity instanceof EntityLiving entityliving) {
            entityliving.getAttributes().addTransientAttributeModifiers(this.makeAttributeMap(i, enchantediteminuse.inSlot()));
        }

    }

    @Override
    public void onDeactivated(EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d, int i) {
        if (entity instanceof EntityLiving entityliving) {
            entityliving.getAttributes().removeAttributeModifiers(this.makeAttributeMap(i, enchantediteminuse.inSlot()));
        }

    }

    private HashMultimap<Holder<AttributeBase>, AttributeModifier> makeAttributeMap(int i, EnumItemSlot enumitemslot) {
        HashMultimap<Holder<AttributeBase>, AttributeModifier> hashmultimap = HashMultimap.create();

        hashmultimap.put(this.attribute, this.getModifier(i, enumitemslot));
        return hashmultimap;
    }

    @Override
    public MapCodec<EnchantmentAttributeEffect> codec() {
        return EnchantmentAttributeEffect.CODEC;
    }
}
