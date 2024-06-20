package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3D;

public record DamageItem(LevelBasedValue amount) implements EnchantmentEntityEffect {

    public static final MapCodec<DamageItem> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(LevelBasedValue.CODEC.fieldOf("amount").forGetter((damageitem) -> {
            return damageitem.amount;
        })).apply(instance, DamageItem::new);
    });

    @Override
    public void apply(WorldServer worldserver, int i, EnchantedItemInUse enchantediteminuse, Entity entity, Vec3D vec3d) {
        EntityLiving entityliving = enchantediteminuse.owner();
        EntityPlayer entityplayer;

        if (entityliving instanceof EntityPlayer entityplayer1) {
            entityplayer = entityplayer1;
        } else {
            entityplayer = null;
        }

        EntityPlayer entityplayer2 = entityplayer;

        enchantediteminuse.itemStack().hurtAndBreak((int) this.amount.calculate(i), worldserver, entityplayer2, enchantediteminuse.onBreak());
    }

    @Override
    public MapCodec<DamageItem> codec() {
        return DamageItem.CODEC;
    }
}
