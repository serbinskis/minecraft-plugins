package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.util.datafix.ComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class ItemStackCustomNameToOverrideComponentFix extends DataFix {

    private static final Set<String> MAP_NAMES = Set.of("filled_map.buried_treasure", "filled_map.explorer_jungle", "filled_map.explorer_swamp", "filled_map.mansion", "filled_map.monument", "filled_map.trial_chambers", "filled_map.village_desert", "filled_map.village_plains", "filled_map.village_savanna", "filled_map.village_snowy", "filled_map.village_taiga");

    public ItemStackCustomNameToOverrideComponentFix(Schema schema) {
        super(schema, false);
    }

    public final TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ITEM_STACK);
        OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(DataConverterTypes.ITEM_NAME.typeName(), DataConverterSchemaNamed.namespacedString()));
        OpticFinder<?> opticfinder1 = type.findField("components");

        return this.fixTypeEverywhereTyped("ItemStack custom_name to item_name component fix", type, (typed) -> {
            Optional<Pair<String, String>> optional = typed.getOptional(opticfinder);
            Optional<String> optional1 = optional.map(Pair::getSecond);

            return optional1.filter((s) -> {
                return s.equals("minecraft:white_banner");
            }).isPresent() ? typed.updateTyped(opticfinder1, (typed1) -> {
                return typed1.update(DSL.remainderFinder(), ItemStackCustomNameToOverrideComponentFix::fixBanner);
            }) : (optional1.filter((s) -> {
                return s.equals("minecraft:filled_map");
            }).isPresent() ? typed.updateTyped(opticfinder1, (typed1) -> {
                return typed1.update(DSL.remainderFinder(), ItemStackCustomNameToOverrideComponentFix::fixMap);
            }) : typed);
        });
    }

    private static <T> Dynamic<T> fixMap(Dynamic<T> dynamic) {
        Set set = ItemStackCustomNameToOverrideComponentFix.MAP_NAMES;

        Objects.requireNonNull(set);
        return fixCustomName(dynamic, set::contains);
    }

    private static <T> Dynamic<T> fixBanner(Dynamic<T> dynamic) {
        return fixCustomName(dynamic, (s) -> {
            return s.equals("block.minecraft.ominous_banner");
        });
    }

    private static <T> Dynamic<T> fixCustomName(Dynamic<T> dynamic, Predicate<String> predicate) {
        OptionalDynamic<T> optionaldynamic = dynamic.get("minecraft:custom_name");
        Optional<String> optional = optionaldynamic.asString().result().flatMap(ComponentDataFixUtils::extractTranslationString).filter(predicate);

        return optional.isPresent() ? dynamic.renameField("minecraft:custom_name", "minecraft:item_name") : dynamic;
    }
}
