package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ToggleTooltips extends LootItemFunctionConditional {

    private static final Map<DataComponentType<?>, ToggleTooltips.a<?>> TOGGLES = (Map) Stream.of(new ToggleTooltips.a<>(DataComponents.TRIM, ArmorTrim::withTooltip), new ToggleTooltips.a<>(DataComponents.DYED_COLOR, DyedItemColor::withTooltip), new ToggleTooltips.a<>(DataComponents.ENCHANTMENTS, ItemEnchantments::withTooltip), new ToggleTooltips.a<>(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments::withTooltip), new ToggleTooltips.a<>(DataComponents.UNBREAKABLE, Unbreakable::withTooltip), new ToggleTooltips.a<>(DataComponents.CAN_BREAK, AdventureModePredicate::withTooltip), new ToggleTooltips.a<>(DataComponents.CAN_PLACE_ON, AdventureModePredicate::withTooltip), new ToggleTooltips.a<>(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers::withTooltip), new ToggleTooltips.a<>(DataComponents.JUKEBOX_PLAYABLE, JukeboxPlayable::withTooltip)).collect(Collectors.toMap(ToggleTooltips.a::type, (toggletooltips_a) -> {
        return toggletooltips_a;
    }));
    private static final Codec<ToggleTooltips.a<?>> TOGGLE_CODEC = BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().comapFlatMap((datacomponenttype) -> {
        ToggleTooltips.a<?> toggletooltips_a = (ToggleTooltips.a) ToggleTooltips.TOGGLES.get(datacomponenttype);

        return toggletooltips_a != null ? DataResult.success(toggletooltips_a) : DataResult.error(() -> {
            return "Can't toggle tooltip visiblity for " + String.valueOf(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(datacomponenttype));
        });
    }, ToggleTooltips.a::type);
    public static final MapCodec<ToggleTooltips> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(Codec.unboundedMap(ToggleTooltips.TOGGLE_CODEC, Codec.BOOL).fieldOf("toggles").forGetter((toggletooltips) -> {
            return toggletooltips.values;
        })).apply(instance, ToggleTooltips::new);
    });
    private final Map<ToggleTooltips.a<?>, Boolean> values;

    private ToggleTooltips(List<LootItemCondition> list, Map<ToggleTooltips.a<?>, Boolean> map) {
        super(list);
        this.values = map;
    }

    @Override
    protected ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        this.values.forEach((toggletooltips_a, obool) -> {
            toggletooltips_a.applyIfPresent(itemstack, obool);
        });
        return itemstack;
    }

    @Override
    public LootItemFunctionType<ToggleTooltips> getType() {
        return LootItemFunctions.TOGGLE_TOOLTIPS;
    }

    private static record a<T>(DataComponentType<T> type, ToggleTooltips.b<T> setter) {

        public void applyIfPresent(ItemStack itemstack, boolean flag) {
            T t0 = itemstack.get(this.type);

            if (t0 != null) {
                itemstack.set(this.type, this.setter.withTooltip(t0, flag));
            }

        }
    }

    @FunctionalInterface
    private interface b<T> {

        T withTooltip(T t0, boolean flag);
    }
}
