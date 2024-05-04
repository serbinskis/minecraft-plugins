package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.SystemUtils;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class MobEffectIdFix extends DataFix {

    private static final Int2ObjectMap<String> ID_MAP = (Int2ObjectMap) SystemUtils.make(new Int2ObjectOpenHashMap(), (int2objectopenhashmap) -> {
        int2objectopenhashmap.put(1, "minecraft:speed");
        int2objectopenhashmap.put(2, "minecraft:slowness");
        int2objectopenhashmap.put(3, "minecraft:haste");
        int2objectopenhashmap.put(4, "minecraft:mining_fatigue");
        int2objectopenhashmap.put(5, "minecraft:strength");
        int2objectopenhashmap.put(6, "minecraft:instant_health");
        int2objectopenhashmap.put(7, "minecraft:instant_damage");
        int2objectopenhashmap.put(8, "minecraft:jump_boost");
        int2objectopenhashmap.put(9, "minecraft:nausea");
        int2objectopenhashmap.put(10, "minecraft:regeneration");
        int2objectopenhashmap.put(11, "minecraft:resistance");
        int2objectopenhashmap.put(12, "minecraft:fire_resistance");
        int2objectopenhashmap.put(13, "minecraft:water_breathing");
        int2objectopenhashmap.put(14, "minecraft:invisibility");
        int2objectopenhashmap.put(15, "minecraft:blindness");
        int2objectopenhashmap.put(16, "minecraft:night_vision");
        int2objectopenhashmap.put(17, "minecraft:hunger");
        int2objectopenhashmap.put(18, "minecraft:weakness");
        int2objectopenhashmap.put(19, "minecraft:poison");
        int2objectopenhashmap.put(20, "minecraft:wither");
        int2objectopenhashmap.put(21, "minecraft:health_boost");
        int2objectopenhashmap.put(22, "minecraft:absorption");
        int2objectopenhashmap.put(23, "minecraft:saturation");
        int2objectopenhashmap.put(24, "minecraft:glowing");
        int2objectopenhashmap.put(25, "minecraft:levitation");
        int2objectopenhashmap.put(26, "minecraft:luck");
        int2objectopenhashmap.put(27, "minecraft:unluck");
        int2objectopenhashmap.put(28, "minecraft:slow_falling");
        int2objectopenhashmap.put(29, "minecraft:conduit_power");
        int2objectopenhashmap.put(30, "minecraft:dolphins_grace");
        int2objectopenhashmap.put(31, "minecraft:bad_omen");
        int2objectopenhashmap.put(32, "minecraft:hero_of_the_village");
        int2objectopenhashmap.put(33, "minecraft:darkness");
    });
    private static final Set<String> MOB_EFFECT_INSTANCE_CARRIER_ITEMS = Set.of("minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow");

    public MobEffectIdFix(Schema schema) {
        super(schema, false);
    }

    private static <T> Optional<Dynamic<T>> getAndConvertMobEffectId(Dynamic<T> dynamic, String s) {
        Optional optional = dynamic.get(s).asNumber().result().map((number) -> {
            return (String) MobEffectIdFix.ID_MAP.get(number.intValue());
        });

        Objects.requireNonNull(dynamic);
        return optional.map(dynamic::createString);
    }

    private static <T> Dynamic<T> updateMobEffectIdField(Dynamic<T> dynamic, String s, Dynamic<T> dynamic1, String s1) {
        Optional<Dynamic<T>> optional = getAndConvertMobEffectId(dynamic, s);

        return dynamic1.replaceField(s, s1, optional);
    }

    private static <T> Dynamic<T> updateMobEffectIdField(Dynamic<T> dynamic, String s, String s1) {
        return updateMobEffectIdField(dynamic, s, dynamic, s1);
    }

    private static <T> Dynamic<T> updateMobEffectInstance(Dynamic<T> dynamic) {
        dynamic = updateMobEffectIdField(dynamic, "Id", "id");
        dynamic = dynamic.renameField("Ambient", "ambient");
        dynamic = dynamic.renameField("Amplifier", "amplifier");
        dynamic = dynamic.renameField("Duration", "duration");
        dynamic = dynamic.renameField("ShowParticles", "show_particles");
        dynamic = dynamic.renameField("ShowIcon", "show_icon");
        Optional<Dynamic<T>> optional = dynamic.get("HiddenEffect").result().map(MobEffectIdFix::updateMobEffectInstance);

        return dynamic.replaceField("HiddenEffect", "hidden_effect", optional);
    }

    private static <T> Dynamic<T> updateMobEffectInstanceList(Dynamic<T> dynamic, String s, String s1) {
        Optional<Dynamic<T>> optional = dynamic.get(s).asStreamOpt().result().map((stream) -> {
            return dynamic.createList(stream.map(MobEffectIdFix::updateMobEffectInstance));
        });

        return dynamic.replaceField(s, s1, optional);
    }

    private static <T> Dynamic<T> updateSuspiciousStewEntry(Dynamic<T> dynamic, Dynamic<T> dynamic1) {
        dynamic1 = updateMobEffectIdField(dynamic, "EffectId", dynamic1, "id");
        Optional<Dynamic<T>> optional = dynamic.get("EffectDuration").result();

        return dynamic1.replaceField("EffectDuration", "duration", optional);
    }

    private static <T> Dynamic<T> updateSuspiciousStewEntry(Dynamic<T> dynamic) {
        return updateSuspiciousStewEntry(dynamic, dynamic);
    }

    private Typed<?> updateNamedChoice(Typed<?> typed, TypeReference typereference, String s, Function<Dynamic<?>, Dynamic<?>> function) {
        Type<?> type = this.getInputSchema().getChoiceType(typereference, s);
        Type<?> type1 = this.getOutputSchema().getChoiceType(typereference, s);

        return typed.updateTyped(DSL.namedChoice(s, type), type1, (typed1) -> {
            return typed1.update(DSL.remainderFinder(), function);
        });
    }

    private TypeRewriteRule blockEntityFixer() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.BLOCK_ENTITY);

        return this.fixTypeEverywhereTyped("BlockEntityMobEffectIdFix", type, (typed) -> {
            typed = this.updateNamedChoice(typed, DataConverterTypes.BLOCK_ENTITY, "minecraft:beacon", (dynamic) -> {
                dynamic = updateMobEffectIdField(dynamic, "Primary", "primary_effect");
                return updateMobEffectIdField(dynamic, "Secondary", "secondary_effect");
            });
            return typed;
        });
    }

    private static <T> Dynamic<T> fixMooshroomTag(Dynamic<T> dynamic) {
        Dynamic<T> dynamic1 = dynamic.emptyMap();
        Dynamic<T> dynamic2 = updateSuspiciousStewEntry(dynamic, dynamic1);

        if (!dynamic2.equals(dynamic1)) {
            dynamic = dynamic.set("stew_effects", dynamic.createList(Stream.of(dynamic2)));
        }

        return dynamic.remove("EffectId").remove("EffectDuration");
    }

    private static <T> Dynamic<T> fixArrowTag(Dynamic<T> dynamic) {
        return updateMobEffectInstanceList(dynamic, "CustomPotionEffects", "custom_potion_effects");
    }

    private static <T> Dynamic<T> fixAreaEffectCloudTag(Dynamic<T> dynamic) {
        return updateMobEffectInstanceList(dynamic, "Effects", "effects");
    }

    private static Dynamic<?> updateLivingEntityTag(Dynamic<?> dynamic) {
        return updateMobEffectInstanceList(dynamic, "ActiveEffects", "active_effects");
    }

    private TypeRewriteRule entityFixer() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ENTITY);

        return this.fixTypeEverywhereTyped("EntityMobEffectIdFix", type, (typed) -> {
            typed = this.updateNamedChoice(typed, DataConverterTypes.ENTITY, "minecraft:mooshroom", MobEffectIdFix::fixMooshroomTag);
            typed = this.updateNamedChoice(typed, DataConverterTypes.ENTITY, "minecraft:arrow", MobEffectIdFix::fixArrowTag);
            typed = this.updateNamedChoice(typed, DataConverterTypes.ENTITY, "minecraft:area_effect_cloud", MobEffectIdFix::fixAreaEffectCloudTag);
            typed = typed.update(DSL.remainderFinder(), MobEffectIdFix::updateLivingEntityTag);
            return typed;
        });
    }

    private TypeRewriteRule playerFixer() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.PLAYER);

        return this.fixTypeEverywhereTyped("PlayerMobEffectIdFix", type, (typed) -> {
            return typed.update(DSL.remainderFinder(), MobEffectIdFix::updateLivingEntityTag);
        });
    }

    private static <T> Dynamic<T> fixSuspiciousStewTag(Dynamic<T> dynamic) {
        Optional<Dynamic<T>> optional = dynamic.get("Effects").asStreamOpt().result().map((stream) -> {
            return dynamic.createList(stream.map(MobEffectIdFix::updateSuspiciousStewEntry));
        });

        return dynamic.replaceField("Effects", "effects", optional);
    }

    private TypeRewriteRule itemStackFixer() {
        OpticFinder<Pair<String, String>> opticfinder = DSL.fieldFinder("id", DSL.named(DataConverterTypes.ITEM_NAME.typeName(), DataConverterSchemaNamed.namespacedString()));
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ITEM_STACK);
        OpticFinder<?> opticfinder1 = type.findField("tag");

        return this.fixTypeEverywhereTyped("ItemStackMobEffectIdFix", type, (typed) -> {
            Optional<Pair<String, String>> optional = typed.getOptional(opticfinder);

            if (optional.isPresent()) {
                String s = (String) ((Pair) optional.get()).getSecond();

                if (s.equals("minecraft:suspicious_stew")) {
                    return typed.updateTyped(opticfinder1, (typed1) -> {
                        return typed1.update(DSL.remainderFinder(), MobEffectIdFix::fixSuspiciousStewTag);
                    });
                }

                if (MobEffectIdFix.MOB_EFFECT_INSTANCE_CARRIER_ITEMS.contains(s)) {
                    return typed.updateTyped(opticfinder1, (typed1) -> {
                        return typed1.update(DSL.remainderFinder(), (dynamic) -> {
                            return updateMobEffectInstanceList(dynamic, "CustomPotionEffects", "custom_potion_effects");
                        });
                    });
                }
            }

            return typed;
        });
    }

    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq(this.blockEntityFixer(), new TypeRewriteRule[]{this.entityFixer(), this.playerFixer(), this.itemStackFixer()});
    }
}
