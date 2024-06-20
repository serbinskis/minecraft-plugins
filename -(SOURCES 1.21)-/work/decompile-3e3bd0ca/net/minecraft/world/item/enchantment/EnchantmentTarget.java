package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import net.minecraft.util.INamable;

public enum EnchantmentTarget implements INamable {

    ATTACKER("attacker"), DAMAGING_ENTITY("damaging_entity"), VICTIM("victim");

    public static final Codec<EnchantmentTarget> CODEC = INamable.fromEnum(EnchantmentTarget::values);
    private final String id;

    private EnchantmentTarget(final String s) {
        this.id = s;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }
}
