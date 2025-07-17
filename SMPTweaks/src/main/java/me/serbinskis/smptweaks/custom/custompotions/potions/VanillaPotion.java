package me.serbinskis.smptweaks.custom.custompotions.potions;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;

public class VanillaPotion extends CustomPotion {
    private static final Map<PotionType, VanillaPotion> cache = new HashMap<>();
    private final PotionType potionType;

    private VanillaPotion(PotionType potionType) {
        super(null, null, null, null);
        this.potionType = potionType;
    }

    @Override
    public String getName() {
        return potionType.getKey().getKey();
    }

    @Override
    public boolean isCustomPotion(ItemStack itemStack) {
        if (!(itemStack.getItemMeta() instanceof PotionMeta potionMeta)) { return false; }
        PotionType potionType = potionMeta.getBasePotionType();
        return (potionType != null) && potionType.name().equalsIgnoreCase(getName());
    }

    public static VanillaPotion create(PotionType potionType) {
        return cache.computeIfAbsent(potionType, VanillaPotion::new);
    }
}
