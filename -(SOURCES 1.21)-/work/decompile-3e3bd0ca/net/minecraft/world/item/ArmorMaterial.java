package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.item.crafting.RecipeItemStack;

public record ArmorMaterial(Map<ItemArmor.a, Integer> defense, int enchantmentValue, Holder<SoundEffect> equipSound, Supplier<RecipeItemStack> repairIngredient, List<ArmorMaterial.a> layers, float toughness, float knockbackResistance) {

    public static final Codec<Holder<ArmorMaterial>> CODEC = BuiltInRegistries.ARMOR_MATERIAL.holderByNameCodec();

    public int getDefense(ItemArmor.a itemarmor_a) {
        return (Integer) this.defense.getOrDefault(itemarmor_a, 0);
    }

    public static final class a {

        private final MinecraftKey assetName;
        private final String suffix;
        private final boolean dyeable;
        private final MinecraftKey innerTexture;
        private final MinecraftKey outerTexture;

        public a(MinecraftKey minecraftkey, String s, boolean flag) {
            this.assetName = minecraftkey;
            this.suffix = s;
            this.dyeable = flag;
            this.innerTexture = this.resolveTexture(true);
            this.outerTexture = this.resolveTexture(false);
        }

        public a(MinecraftKey minecraftkey) {
            this(minecraftkey, "", false);
        }

        private MinecraftKey resolveTexture(boolean flag) {
            return this.assetName.withPath((s) -> {
                String s1 = this.assetName.getPath();

                return "textures/models/armor/" + s1 + "_layer_" + (flag ? 2 : 1) + this.suffix + ".png";
            });
        }

        public MinecraftKey texture(boolean flag) {
            return flag ? this.innerTexture : this.outerTexture;
        }

        public boolean dyeable() {
            return this.dyeable;
        }
    }
}
