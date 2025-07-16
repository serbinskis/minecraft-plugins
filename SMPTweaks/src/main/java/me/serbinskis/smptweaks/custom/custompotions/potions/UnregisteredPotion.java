package me.serbinskis.smptweaks.custom.custompotions.potions;

import me.serbinskis.smptweaks.utils.ReflectionUtils;

public class UnregisteredPotion extends CustomPotion {
    private final Class<? extends CustomPotion> customPotion;

    private UnregisteredPotion(Class<? extends CustomPotion> customPotion) {
        super(null, null, null, null);
        this.customPotion = customPotion;
    }

    public CustomPotion getCustomPotion() {
        return (CustomPotion) ReflectionUtils.newInstance(customPotion, null, null);
    }

    public String getName() {
        return getCustomPotion().getName();
    }

    public static UnregisteredPotion create(Class<? extends CustomPotion> customPotion) {
        return new UnregisteredPotion(customPotion);
    }
}
