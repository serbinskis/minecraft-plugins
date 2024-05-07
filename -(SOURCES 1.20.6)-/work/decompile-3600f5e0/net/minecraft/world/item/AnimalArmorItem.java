package net.minecraft.world.item;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;

public class AnimalArmorItem extends ItemArmor {

    private final MinecraftKey textureLocation;
    @Nullable
    private final MinecraftKey overlayTextureLocation;
    private final AnimalArmorItem.a bodyType;

    public AnimalArmorItem(Holder<ArmorMaterial> holder, AnimalArmorItem.a animalarmoritem_a, boolean flag, Item.Info item_info) {
        super(holder, ItemArmor.a.BODY, item_info);
        this.bodyType = animalarmoritem_a;
        MinecraftKey minecraftkey = (MinecraftKey) animalarmoritem_a.textureLocator.apply(((ResourceKey) holder.unwrapKey().orElseThrow()).location());

        this.textureLocation = minecraftkey.withSuffix(".png");
        if (flag) {
            this.overlayTextureLocation = minecraftkey.withSuffix("_overlay.png");
        } else {
            this.overlayTextureLocation = null;
        }

    }

    public MinecraftKey getTexture() {
        return this.textureLocation;
    }

    @Nullable
    public MinecraftKey getOverlayTexture() {
        return this.overlayTextureLocation;
    }

    public AnimalArmorItem.a getBodyType() {
        return this.bodyType;
    }

    @Override
    public SoundEffect getBreakingSound() {
        return this.bodyType.breakingSound;
    }

    @Override
    public boolean isEnchantable(ItemStack itemstack) {
        return false;
    }

    public static enum a {

        EQUESTRIAN((minecraftkey) -> {
            return minecraftkey.withPath((s) -> {
                return "textures/entity/horse/armor/horse_armor_" + s;
            });
        }, SoundEffects.ITEM_BREAK), CANINE((minecraftkey) -> {
            return minecraftkey.withPath("textures/entity/wolf/wolf_armor");
        }, SoundEffects.WOLF_ARMOR_BREAK);

        final Function<MinecraftKey, MinecraftKey> textureLocator;
        final SoundEffect breakingSound;

        private a(final Function function, final SoundEffect soundeffect) {
            this.textureLocator = function;
            this.breakingSound = soundeffect;
        }
    }
}
