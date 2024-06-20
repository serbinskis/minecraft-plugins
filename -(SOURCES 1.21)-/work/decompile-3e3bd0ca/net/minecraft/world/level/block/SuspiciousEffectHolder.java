package net.minecraft.world.level.block;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemBlock;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.IMaterial;

public interface SuspiciousEffectHolder {

    SuspiciousStewEffects getSuspiciousEffects();

    static List<SuspiciousEffectHolder> getAllEffectHolders() {
        return (List) BuiltInRegistries.ITEM.stream().map(SuspiciousEffectHolder::tryGet).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Nullable
    static SuspiciousEffectHolder tryGet(IMaterial imaterial) {
        Item item = imaterial.asItem();

        if (item instanceof ItemBlock itemblock) {
            Block block = itemblock.getBlock();

            if (block instanceof SuspiciousEffectHolder suspiciouseffectholder) {
                return suspiciouseffectholder;
            }
        }

        Item item1 = imaterial.asItem();

        if (item1 instanceof SuspiciousEffectHolder suspiciouseffectholder1) {
            return suspiciouseffectholder1;
        } else {
            return null;
        }
    }
}
