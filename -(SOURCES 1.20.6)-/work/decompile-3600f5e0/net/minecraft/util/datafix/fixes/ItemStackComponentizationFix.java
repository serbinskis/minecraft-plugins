package net.minecraft.util.datafix.fixes;

import com.google.common.base.Splitter;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.util.MathHelper;
import net.minecraft.util.datafix.ComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;

public class ItemStackComponentizationFix extends DataFix {

    private static final int HIDE_ENCHANTMENTS = 1;
    private static final int HIDE_MODIFIERS = 2;
    private static final int HIDE_UNBREAKABLE = 4;
    private static final int HIDE_CAN_DESTROY = 8;
    private static final int HIDE_CAN_PLACE = 16;
    private static final int HIDE_ADDITIONAL = 32;
    private static final int HIDE_DYE = 64;
    private static final int HIDE_UPGRADES = 128;
    private static final Set<String> POTION_HOLDER_IDS = Set.of("minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow");
    private static final Set<String> BUCKETED_MOB_IDS = Set.of("minecraft:pufferfish_bucket", "minecraft:salmon_bucket", "minecraft:cod_bucket", "minecraft:tropical_fish_bucket", "minecraft:axolotl_bucket", "minecraft:tadpole_bucket");
    private static final List<String> BUCKETED_MOB_TAGS = List.of("NoAI", "Silent", "NoGravity", "Glowing", "Invulnerable", "Health", "Age", "Variant", "HuntingCooldown", "BucketVariantTag");
    private static final Set<String> BOOLEAN_BLOCK_STATE_PROPERTIES = Set.of("attached", "bottom", "conditional", "disarmed", "drag", "enabled", "extended", "eye", "falling", "hanging", "has_bottle_0", "has_bottle_1", "has_bottle_2", "has_record", "has_book", "inverted", "in_wall", "lit", "locked", "occupied", "open", "persistent", "powered", "short", "signal_fire", "snowy", "triggered", "unstable", "waterlogged", "berries", "bloom", "shrieking", "can_summon", "up", "down", "north", "east", "south", "west", "slot_0_occupied", "slot_1_occupied", "slot_2_occupied", "slot_3_occupied", "slot_4_occupied", "slot_5_occupied", "cracked", "crafting");
    private static final Splitter PROPERTY_SPLITTER = Splitter.on(',');

    public ItemStackComponentizationFix(Schema schema) {
        super(schema, true);
    }

    private static void fixItemStack(ItemStackComponentizationFix.a itemstackcomponentizationfix_a, Dynamic<?> dynamic) {
        int i = itemstackcomponentizationfix_a.removeTag("HideFlags").asInt(0);

        itemstackcomponentizationfix_a.moveTagToComponent("Damage", "minecraft:damage", dynamic.createInt(0));
        itemstackcomponentizationfix_a.moveTagToComponent("RepairCost", "minecraft:repair_cost", dynamic.createInt(0));
        itemstackcomponentizationfix_a.moveTagToComponent("CustomModelData", "minecraft:custom_model_data");
        itemstackcomponentizationfix_a.removeTag("BlockStateTag").result().ifPresent((dynamic1) -> {
            itemstackcomponentizationfix_a.setComponent("minecraft:block_state", fixBlockStateTag(dynamic1));
        });
        itemstackcomponentizationfix_a.moveTagToComponent("EntityTag", "minecraft:entity_data");
        itemstackcomponentizationfix_a.fixSubTag("BlockEntityTag", false, (dynamic1) -> {
            String s = DataConverterSchemaNamed.ensureNamespaced(dynamic1.get("id").asString(""));

            dynamic1 = fixBlockEntityTag(itemstackcomponentizationfix_a, dynamic1, s);
            Dynamic<?> dynamic2 = dynamic1.remove("id");

            return dynamic2.equals(dynamic1.emptyMap()) ? dynamic2 : dynamic1;
        });
        itemstackcomponentizationfix_a.moveTagToComponent("BlockEntityTag", "minecraft:block_entity_data");
        if (itemstackcomponentizationfix_a.removeTag("Unbreakable").asBoolean(false)) {
            Dynamic<?> dynamic1 = dynamic.emptyMap();

            if ((i & 4) != 0) {
                dynamic1 = dynamic1.set("show_in_tooltip", dynamic.createBoolean(false));
            }

            itemstackcomponentizationfix_a.setComponent("minecraft:unbreakable", dynamic1);
        }

        fixEnchantments(itemstackcomponentizationfix_a, dynamic, "Enchantments", "minecraft:enchantments", (i & 1) != 0);
        if (itemstackcomponentizationfix_a.is("minecraft:enchanted_book")) {
            fixEnchantments(itemstackcomponentizationfix_a, dynamic, "StoredEnchantments", "minecraft:stored_enchantments", (i & 32) != 0);
        }

        itemstackcomponentizationfix_a.fixSubTag("display", false, (dynamic2) -> {
            return fixDisplay(itemstackcomponentizationfix_a, dynamic2, i);
        });
        fixAdventureModeChecks(itemstackcomponentizationfix_a, dynamic, i);
        fixAttributeModifiers(itemstackcomponentizationfix_a, dynamic, i);
        Optional<? extends Dynamic<?>> optional = itemstackcomponentizationfix_a.removeTag("Trim").result();

        if (optional.isPresent()) {
            Dynamic<?> dynamic2 = (Dynamic) optional.get();

            if ((i & 128) != 0) {
                dynamic2 = dynamic2.set("show_in_tooltip", dynamic2.createBoolean(false));
            }

            itemstackcomponentizationfix_a.setComponent("minecraft:trim", dynamic2);
        }

        if ((i & 32) != 0) {
            itemstackcomponentizationfix_a.setComponent("minecraft:hide_additional_tooltip", dynamic.emptyMap());
        }

        if (itemstackcomponentizationfix_a.is("minecraft:crossbow")) {
            itemstackcomponentizationfix_a.removeTag("Charged");
            itemstackcomponentizationfix_a.moveTagToComponent("ChargedProjectiles", "minecraft:charged_projectiles", dynamic.createList(Stream.empty()));
        }

        if (itemstackcomponentizationfix_a.is("minecraft:bundle")) {
            itemstackcomponentizationfix_a.moveTagToComponent("Items", "minecraft:bundle_contents", dynamic.createList(Stream.empty()));
        }

        if (itemstackcomponentizationfix_a.is("minecraft:filled_map")) {
            itemstackcomponentizationfix_a.moveTagToComponent("map", "minecraft:map_id");
            Map<? extends Dynamic<?>, ? extends Dynamic<?>> map = (Map) itemstackcomponentizationfix_a.removeTag("Decorations").asStream().map(ItemStackComponentizationFix::fixMapDecoration).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (dynamic3, dynamic4) -> {
                return dynamic3;
            }));

            if (!map.isEmpty()) {
                itemstackcomponentizationfix_a.setComponent("minecraft:map_decorations", dynamic.createMap(map));
            }
        }

        if (itemstackcomponentizationfix_a.is(ItemStackComponentizationFix.POTION_HOLDER_IDS)) {
            fixPotionContents(itemstackcomponentizationfix_a, dynamic);
        }

        if (itemstackcomponentizationfix_a.is("minecraft:writable_book")) {
            fixWritableBook(itemstackcomponentizationfix_a, dynamic);
        }

        if (itemstackcomponentizationfix_a.is("minecraft:written_book")) {
            fixWrittenBook(itemstackcomponentizationfix_a, dynamic);
        }

        if (itemstackcomponentizationfix_a.is("minecraft:suspicious_stew")) {
            itemstackcomponentizationfix_a.moveTagToComponent("effects", "minecraft:suspicious_stew_effects");
        }

        if (itemstackcomponentizationfix_a.is("minecraft:debug_stick")) {
            itemstackcomponentizationfix_a.moveTagToComponent("DebugProperty", "minecraft:debug_stick_state");
        }

        if (itemstackcomponentizationfix_a.is(ItemStackComponentizationFix.BUCKETED_MOB_IDS)) {
            fixBucketedMobData(itemstackcomponentizationfix_a, dynamic);
        }

        if (itemstackcomponentizationfix_a.is("minecraft:goat_horn")) {
            itemstackcomponentizationfix_a.moveTagToComponent("instrument", "minecraft:instrument");
        }

        if (itemstackcomponentizationfix_a.is("minecraft:knowledge_book")) {
            itemstackcomponentizationfix_a.moveTagToComponent("Recipes", "minecraft:recipes");
        }

        if (itemstackcomponentizationfix_a.is("minecraft:compass")) {
            fixLodestoneTracker(itemstackcomponentizationfix_a, dynamic);
        }

        if (itemstackcomponentizationfix_a.is("minecraft:firework_rocket")) {
            fixFireworkRocket(itemstackcomponentizationfix_a);
        }

        if (itemstackcomponentizationfix_a.is("minecraft:firework_star")) {
            fixFireworkStar(itemstackcomponentizationfix_a);
        }

        if (itemstackcomponentizationfix_a.is("minecraft:player_head")) {
            itemstackcomponentizationfix_a.removeTag("SkullOwner").result().ifPresent((dynamic3) -> {
                itemstackcomponentizationfix_a.setComponent("minecraft:profile", fixProfile(dynamic3));
            });
        }

    }

    private static Dynamic<?> fixBlockStateTag(Dynamic<?> dynamic) {
        Optional optional = dynamic.asMapOpt().result().map((stream) -> {
            return (Map) stream.collect(Collectors.toMap(Pair::getFirst, (pair) -> {
                String s = ((Dynamic) pair.getFirst()).asString("");
                Dynamic<?> dynamic1 = (Dynamic) pair.getSecond();
                Optional optional1;

                if (ItemStackComponentizationFix.BOOLEAN_BLOCK_STATE_PROPERTIES.contains(s)) {
                    optional1 = dynamic1.asBoolean().result();
                    if (optional1.isPresent()) {
                        return dynamic1.createString(String.valueOf(optional1.get()));
                    }
                }

                optional1 = dynamic1.asNumber().result();
                return optional1.isPresent() ? dynamic1.createString(((Number) optional1.get()).toString()) : dynamic1;
            }));
        });

        Objects.requireNonNull(dynamic);
        return (Dynamic) DataFixUtils.orElse(optional.map(dynamic::createMap), dynamic);
    }

    private static Dynamic<?> fixDisplay(ItemStackComponentizationFix.a itemstackcomponentizationfix_a, Dynamic<?> dynamic, int i) {
        itemstackcomponentizationfix_a.setComponent("minecraft:custom_name", dynamic.get("Name"));
        itemstackcomponentizationfix_a.setComponent("minecraft:lore", dynamic.get("Lore"));
        Optional<Integer> optional = dynamic.get("color").asNumber().result().map(Number::intValue);
        boolean flag = (i & 64) != 0;

        if (optional.isPresent() || flag) {
            Dynamic<?> dynamic1 = dynamic.emptyMap().set("rgb", dynamic.createInt((Integer) optional.orElse(10511680)));

            if (flag) {
                dynamic1 = dynamic1.set("show_in_tooltip", dynamic.createBoolean(false));
            }

            itemstackcomponentizationfix_a.setComponent("minecraft:dyed_color", dynamic1);
        }

        Optional<String> optional1 = dynamic.get("LocName").asString().result();

        if (optional1.isPresent()) {
            itemstackcomponentizationfix_a.setComponent("minecraft:item_name", ComponentDataFixUtils.createTranslatableComponent(dynamic.getOps(), (String) optional1.get()));
        }

        if (itemstackcomponentizationfix_a.is("minecraft:filled_map")) {
            itemstackcomponentizationfix_a.setComponent("minecraft:map_color", dynamic.get("MapColor"));
            dynamic = dynamic.remove("MapColor");
        }

        return dynamic.remove("Name").remove("Lore").remove("color").remove("LocName");
    }

    private static <T> Dynamic<T> fixBlockEntityTag(ItemStackComponentizationFix.a itemstackcomponentizationfix_a, Dynamic<T> dynamic, String s) {
        itemstackcomponentizationfix_a.setComponent("minecraft:lock", dynamic.get("Lock"));
        dynamic = dynamic.remove("Lock");
        Optional<Dynamic<T>> optional = dynamic.get("LootTable").result();

        if (optional.isPresent()) {
            Dynamic<T> dynamic1 = dynamic.emptyMap().set("loot_table", (Dynamic) optional.get());
            long i = dynamic.get("LootTableSeed").asLong(0L);

            if (i != 0L) {
                dynamic1 = dynamic1.set("seed", dynamic.createLong(i));
            }

            itemstackcomponentizationfix_a.setComponent("minecraft:container_loot", dynamic1);
            dynamic = dynamic.remove("LootTable").remove("LootTableSeed");
        }

        Dynamic dynamic2;
        Optional optional1;

        switch (s) {
            case "minecraft:skull":
                itemstackcomponentizationfix_a.setComponent("minecraft:note_block_sound", dynamic.get("note_block_sound"));
                dynamic2 = dynamic.remove("note_block_sound");
                break;
            case "minecraft:decorated_pot":
                itemstackcomponentizationfix_a.setComponent("minecraft:pot_decorations", dynamic.get("sherds"));
                optional1 = dynamic.get("item").result();
                if (optional1.isPresent()) {
                    itemstackcomponentizationfix_a.setComponent("minecraft:container", dynamic.createList(Stream.of(dynamic.emptyMap().set("slot", dynamic.createInt(0)).set("item", (Dynamic) optional1.get()))));
                }

                dynamic2 = dynamic.remove("sherds").remove("item");
                break;
            case "minecraft:banner":
                itemstackcomponentizationfix_a.setComponent("minecraft:banner_patterns", dynamic.get("patterns"));
                optional1 = dynamic.get("Base").asNumber().result();
                if (optional1.isPresent()) {
                    itemstackcomponentizationfix_a.setComponent("minecraft:base_color", dynamic.createString(BannerPatternFormatFix.fixColor(((Number) optional1.get()).intValue())));
                }

                dynamic2 = dynamic.remove("patterns").remove("Base");
                break;
            case "minecraft:shulker_box":
            case "minecraft:chest":
            case "minecraft:trapped_chest":
            case "minecraft:furnace":
            case "minecraft:ender_chest":
            case "minecraft:dispenser":
            case "minecraft:dropper":
            case "minecraft:brewing_stand":
            case "minecraft:hopper":
            case "minecraft:barrel":
            case "minecraft:smoker":
            case "minecraft:blast_furnace":
            case "minecraft:campfire":
            case "minecraft:chiseled_bookshelf":
            case "minecraft:crafter":
                List<Dynamic<T>> list = dynamic.get("Items").asList((dynamic3) -> {
                    return dynamic3.emptyMap().set("slot", dynamic3.createInt(dynamic3.get("Slot").asByte((byte) 0) & 255)).set("item", dynamic3.remove("Slot"));
                });

                if (!list.isEmpty()) {
                    itemstackcomponentizationfix_a.setComponent("minecraft:container", dynamic.createList(list.stream()));
                }

                dynamic2 = dynamic.remove("Items");
                break;
            case "minecraft:beehive":
                itemstackcomponentizationfix_a.setComponent("minecraft:bees", dynamic.get("bees"));
                dynamic2 = dynamic.remove("bees");
                break;
            default:
                dynamic2 = dynamic;
        }

        return dynamic2;
    }

    private static void fixEnchantments(ItemStackComponentizationFix.a itemstackcomponentizationfix_a, Dynamic<?> dynamic, String s, String s1, boolean flag) {
        OptionalDynamic<?> optionaldynamic = itemstackcomponentizationfix_a.removeTag(s);
        List<Pair<String, Integer>> list = optionaldynamic.asList(Function.identity()).stream().flatMap((dynamic1) -> {
            return parseEnchantment(dynamic1).stream();
        }).toList();

        if (!list.isEmpty() || flag) {
            Dynamic<?> dynamic1 = dynamic.emptyMap();
            Dynamic<?> dynamic2 = dynamic.emptyMap();

            Pair pair;

            for (Iterator iterator = list.iterator(); iterator.hasNext(); dynamic2 = dynamic2.set((String) pair.getFirst(), dynamic.createInt((Integer) pair.getSecond()))) {
                pair = (Pair) iterator.next();
            }

            dynamic1 = dynamic1.set("levels", dynamic2);
            if (flag) {
                dynamic1 = dynamic1.set("show_in_tooltip", dynamic.createBoolean(false));
            }

            itemstackcomponentizationfix_a.setComponent(s1, dynamic1);
        }

        if (optionaldynamic.result().isPresent() && list.isEmpty()) {
            itemstackcomponentizationfix_a.setComponent("minecraft:enchantment_glint_override", dynamic.createBoolean(true));
        }

    }

    private static Optional<Pair<String, Integer>> parseEnchantment(Dynamic<?> dynamic) {
        return dynamic.get("id").asString().apply2stable((s, number) -> {
            return Pair.of(s, MathHelper.clamp(number.intValue(), 0, 255));
        }, dynamic.get("lvl").asNumber()).result();
    }

    private static void fixAdventureModeChecks(ItemStackComponentizationFix.a itemstackcomponentizationfix_a, Dynamic<?> dynamic, int i) {
        fixBlockStatePredicates(itemstackcomponentizationfix_a, dynamic, "CanDestroy", "minecraft:can_break", (i & 8) != 0);
        fixBlockStatePredicates(itemstackcomponentizationfix_a, dynamic, "CanPlaceOn", "minecraft:can_place_on", (i & 16) != 0);
    }

    private static void fixBlockStatePredicates(ItemStackComponentizationFix.a itemstackcomponentizationfix_a, Dynamic<?> dynamic, String s, String s1, boolean flag) {
        Optional<? extends Dynamic<?>> optional = itemstackcomponentizationfix_a.removeTag(s).result();

        if (!optional.isEmpty()) {
            Dynamic<?> dynamic1 = dynamic.emptyMap().set("predicates", dynamic.createList(((Dynamic) optional.get()).asStream().map((dynamic2) -> {
                return (Dynamic) DataFixUtils.orElse(dynamic2.asString().map((s2) -> {
                    return fixBlockStatePredicate(dynamic2, s2);
                }).result(), dynamic2);
            })));

            if (flag) {
                dynamic1 = dynamic1.set("show_in_tooltip", dynamic.createBoolean(false));
            }

            itemstackcomponentizationfix_a.setComponent(s1, dynamic1);
        }
    }

    private static Dynamic<?> fixBlockStatePredicate(Dynamic<?> dynamic, String s) {
        int i = s.indexOf(91);
        int j = s.indexOf(123);
        int k = s.length();

        if (i != -1) {
            k = i;
        }

        if (j != -1) {
            k = Math.min(k, j);
        }

        String s1 = s.substring(0, k);
        Dynamic<?> dynamic1 = dynamic.emptyMap().set("blocks", dynamic.createString(s1.trim()));
        int l = s.indexOf(93);

        if (i != -1 && l != -1) {
            Dynamic<?> dynamic2 = dynamic.emptyMap();
            Iterable<String> iterable = ItemStackComponentizationFix.PROPERTY_SPLITTER.split(s.substring(i + 1, l));
            Iterator iterator = iterable.iterator();

            while (iterator.hasNext()) {
                String s2 = (String) iterator.next();
                int i1 = s2.indexOf(61);

                if (i1 != -1) {
                    String s3 = s2.substring(0, i1).trim();
                    String s4 = s2.substring(i1 + 1).trim();

                    dynamic2 = dynamic2.set(s3, dynamic.createString(s4));
                }
            }

            dynamic1 = dynamic1.set("state", dynamic2);
        }

        int j1 = s.indexOf(125);

        if (j != -1 && j1 != -1) {
            dynamic1 = dynamic1.set("nbt", dynamic.createString(s.substring(j, j1 + 1)));
        }

        return dynamic1;
    }

    private static void fixAttributeModifiers(ItemStackComponentizationFix.a itemstackcomponentizationfix_a, Dynamic<?> dynamic, int i) {
        List<? extends Dynamic<?>> list = itemstackcomponentizationfix_a.removeTag("AttributeModifiers").asList(ItemStackComponentizationFix::fixAttributeModifier);
        boolean flag = (i & 2) != 0;

        if (!list.isEmpty() || flag) {
            Dynamic<?> dynamic1 = dynamic.emptyMap().set("modifiers", dynamic.createList(list.stream()));

            if (flag) {
                dynamic1 = dynamic1.set("show_in_tooltip", dynamic.createBoolean(false));
            }

            itemstackcomponentizationfix_a.setComponent("minecraft:attribute_modifiers", dynamic1);
        }
    }

    private static Dynamic<?> fixAttributeModifier(Dynamic<?> dynamic) {
        Dynamic<?> dynamic1 = dynamic.emptyMap().set("name", dynamic.createString("")).set("amount", dynamic.createDouble(0.0D)).set("operation", dynamic.createString("add_value"));

        dynamic1 = Dynamic.copyField(dynamic, "AttributeName", dynamic1, "type");
        dynamic1 = Dynamic.copyField(dynamic, "Slot", dynamic1, "slot");
        dynamic1 = Dynamic.copyField(dynamic, "UUID", dynamic1, "uuid");
        dynamic1 = Dynamic.copyField(dynamic, "Name", dynamic1, "name");
        dynamic1 = Dynamic.copyField(dynamic, "Amount", dynamic1, "amount");
        dynamic1 = Dynamic.copyAndFixField(dynamic, "Operation", dynamic1, "operation", (dynamic2) -> {
            String s;

            switch (dynamic2.asInt(0)) {
                case 1:
                    s = "add_multiplied_base";
                    break;
                case 2:
                    s = "add_multiplied_total";
                    break;
                default:
                    s = "add_value";
            }

            return dynamic2.createString(s);
        });
        return dynamic1;
    }

    private static Pair<Dynamic<?>, Dynamic<?>> fixMapDecoration(Dynamic<?> dynamic) {
        Dynamic<?> dynamic1 = (Dynamic) DataFixUtils.orElseGet(dynamic.get("id").result(), () -> {
            return dynamic.createString("");
        });
        Dynamic<?> dynamic2 = dynamic.emptyMap().set("type", dynamic.createString(fixMapDecorationType(dynamic.get("type").asInt(0)))).set("x", dynamic.createDouble(dynamic.get("x").asDouble(0.0D))).set("z", dynamic.createDouble(dynamic.get("z").asDouble(0.0D))).set("rotation", dynamic.createFloat((float) dynamic.get("rot").asDouble(0.0D)));

        return Pair.of(dynamic1, dynamic2);
    }

    private static String fixMapDecorationType(int i) {
        String s;

        switch (i) {
            case 1:
                s = "frame";
                break;
            case 2:
                s = "red_marker";
                break;
            case 3:
                s = "blue_marker";
                break;
            case 4:
                s = "target_x";
                break;
            case 5:
                s = "target_point";
                break;
            case 6:
                s = "player_off_map";
                break;
            case 7:
                s = "player_off_limits";
                break;
            case 8:
                s = "mansion";
                break;
            case 9:
                s = "monument";
                break;
            case 10:
                s = "banner_white";
                break;
            case 11:
                s = "banner_orange";
                break;
            case 12:
                s = "banner_magenta";
                break;
            case 13:
                s = "banner_light_blue";
                break;
            case 14:
                s = "banner_yellow";
                break;
            case 15:
                s = "banner_lime";
                break;
            case 16:
                s = "banner_pink";
                break;
            case 17:
                s = "banner_gray";
                break;
            case 18:
                s = "banner_light_gray";
                break;
            case 19:
                s = "banner_cyan";
                break;
            case 20:
                s = "banner_purple";
                break;
            case 21:
                s = "banner_blue";
                break;
            case 22:
                s = "banner_brown";
                break;
            case 23:
                s = "banner_green";
                break;
            case 24:
                s = "banner_red";
                break;
            case 25:
                s = "banner_black";
                break;
            case 26:
                s = "red_x";
                break;
            case 27:
                s = "village_desert";
                break;
            case 28:
                s = "village_plains";
                break;
            case 29:
                s = "village_savanna";
                break;
            case 30:
                s = "village_snowy";
                break;
            case 31:
                s = "village_taiga";
                break;
            case 32:
                s = "jungle_temple";
                break;
            case 33:
                s = "swamp_hut";
                break;
            default:
                s = "player";
        }

        return s;
    }

    private static void fixPotionContents(ItemStackComponentizationFix.a itemstackcomponentizationfix_a, Dynamic<?> dynamic) {
        Dynamic<?> dynamic1 = dynamic.emptyMap();
        Optional<String> optional = itemstackcomponentizationfix_a.removeTag("Potion").asString().result().filter((s) -> {
            return !s.equals("minecraft:empty");
        });

        if (optional.isPresent()) {
            dynamic1 = dynamic1.set("potion", dynamic.createString((String) optional.get()));
        }

        dynamic1 = itemstackcomponentizationfix_a.moveTagInto("CustomPotionColor", dynamic1, "custom_color");
        dynamic1 = itemstackcomponentizationfix_a.moveTagInto("custom_potion_effects", dynamic1, "custom_effects");
        if (!dynamic1.equals(dynamic.emptyMap())) {
            itemstackcomponentizationfix_a.setComponent("minecraft:potion_contents", dynamic1);
        }

    }

    private static void fixWritableBook(ItemStackComponentizationFix.a itemstackcomponentizationfix_a, Dynamic<?> dynamic) {
        Dynamic<?> dynamic1 = fixBookPages(itemstackcomponentizationfix_a, dynamic);

        if (dynamic1 != null) {
            itemstackcomponentizationfix_a.setComponent("minecraft:writable_book_content", dynamic.emptyMap().set("pages", dynamic1));
        }

    }

    private static void fixWrittenBook(ItemStackComponentizationFix.a itemstackcomponentizationfix_a, Dynamic<?> dynamic) {
        Dynamic<?> dynamic1 = fixBookPages(itemstackcomponentizationfix_a, dynamic);
        String s = itemstackcomponentizationfix_a.removeTag("title").asString("");
        Optional<String> optional = itemstackcomponentizationfix_a.removeTag("filtered_title").asString().result();
        Dynamic<?> dynamic2 = dynamic.emptyMap();

        dynamic2 = dynamic2.set("title", createFilteredText(dynamic, s, optional));
        dynamic2 = itemstackcomponentizationfix_a.moveTagInto("author", dynamic2, "author");
        dynamic2 = itemstackcomponentizationfix_a.moveTagInto("resolved", dynamic2, "resolved");
        dynamic2 = itemstackcomponentizationfix_a.moveTagInto("generation", dynamic2, "generation");
        if (dynamic1 != null) {
            dynamic2 = dynamic2.set("pages", dynamic1);
        }

        itemstackcomponentizationfix_a.setComponent("minecraft:written_book_content", dynamic2);
    }

    @Nullable
    private static Dynamic<?> fixBookPages(ItemStackComponentizationFix.a itemstackcomponentizationfix_a, Dynamic<?> dynamic) {
        List<String> list = itemstackcomponentizationfix_a.removeTag("pages").asList((dynamic1) -> {
            return dynamic1.asString("");
        });
        Map<String, String> map = itemstackcomponentizationfix_a.removeTag("filtered_pages").asMap((dynamic1) -> {
            return dynamic1.asString("0");
        }, (dynamic1) -> {
            return dynamic1.asString("");
        });

        if (list.isEmpty()) {
            return null;
        } else {
            List<Dynamic<?>> list1 = new ArrayList(list.size());

            for (int i = 0; i < list.size(); ++i) {
                String s = (String) list.get(i);
                String s1 = (String) map.get(String.valueOf(i));

                list1.add(createFilteredText(dynamic, s, Optional.ofNullable(s1)));
            }

            return dynamic.createList(list1.stream());
        }
    }

    private static Dynamic<?> createFilteredText(Dynamic<?> dynamic, String s, Optional<String> optional) {
        Dynamic<?> dynamic1 = dynamic.emptyMap().set("raw", dynamic.createString(s));

        if (optional.isPresent()) {
            dynamic1 = dynamic1.set("filtered", dynamic.createString((String) optional.get()));
        }

        return dynamic1;
    }

    private static void fixBucketedMobData(ItemStackComponentizationFix.a itemstackcomponentizationfix_a, Dynamic<?> dynamic) {
        Dynamic<?> dynamic1 = dynamic.emptyMap();

        String s;

        for (Iterator iterator = ItemStackComponentizationFix.BUCKETED_MOB_TAGS.iterator(); iterator.hasNext(); dynamic1 = itemstackcomponentizationfix_a.moveTagInto(s, dynamic1, s)) {
            s = (String) iterator.next();
        }

        if (!dynamic1.equals(dynamic.emptyMap())) {
            itemstackcomponentizationfix_a.setComponent("minecraft:bucket_entity_data", dynamic1);
        }

    }

    private static void fixLodestoneTracker(ItemStackComponentizationFix.a itemstackcomponentizationfix_a, Dynamic<?> dynamic) {
        Optional<? extends Dynamic<?>> optional = itemstackcomponentizationfix_a.removeTag("LodestonePos").result();
        Optional<? extends Dynamic<?>> optional1 = itemstackcomponentizationfix_a.removeTag("LodestoneDimension").result();

        if (!optional.isEmpty() || !optional1.isEmpty()) {
            boolean flag = itemstackcomponentizationfix_a.removeTag("LodestoneTracked").asBoolean(true);
            Dynamic<?> dynamic1 = dynamic.emptyMap();

            if (optional.isPresent() && optional1.isPresent()) {
                dynamic1 = dynamic1.set("target", dynamic.emptyMap().set("pos", (Dynamic) optional.get()).set("dimension", (Dynamic) optional1.get()));
            }

            if (!flag) {
                dynamic1 = dynamic1.set("tracked", dynamic.createBoolean(false));
            }

            itemstackcomponentizationfix_a.setComponent("minecraft:lodestone_tracker", dynamic1);
        }
    }

    private static void fixFireworkStar(ItemStackComponentizationFix.a itemstackcomponentizationfix_a) {
        itemstackcomponentizationfix_a.fixSubTag("Explosion", true, (dynamic) -> {
            itemstackcomponentizationfix_a.setComponent("minecraft:firework_explosion", fixFireworkExplosion(dynamic));
            return dynamic.remove("Type").remove("Colors").remove("FadeColors").remove("Trail").remove("Flicker");
        });
    }

    private static void fixFireworkRocket(ItemStackComponentizationFix.a itemstackcomponentizationfix_a) {
        itemstackcomponentizationfix_a.fixSubTag("Fireworks", true, (dynamic) -> {
            Stream<? extends Dynamic<?>> stream = dynamic.get("Explosions").asStream().map(ItemStackComponentizationFix::fixFireworkExplosion);
            int i = dynamic.get("Flight").asInt(0);

            itemstackcomponentizationfix_a.setComponent("minecraft:fireworks", dynamic.emptyMap().set("explosions", dynamic.createList(stream)).set("flight_duration", dynamic.createByte((byte) i)));
            return dynamic.remove("Explosions").remove("Flight");
        });
    }

    private static Dynamic<?> fixFireworkExplosion(Dynamic<?> dynamic) {
        String s;

        switch (dynamic.get("Type").asInt(0)) {
            case 1:
                s = "large_ball";
                break;
            case 2:
                s = "star";
                break;
            case 3:
                s = "creeper";
                break;
            case 4:
                s = "burst";
                break;
            default:
                s = "small_ball";
        }

        dynamic = dynamic.set("shape", dynamic.createString(s)).remove("Type");
        dynamic = dynamic.renameField("Colors", "colors");
        dynamic = dynamic.renameField("FadeColors", "fade_colors");
        dynamic = dynamic.renameField("Trail", "has_trail");
        dynamic = dynamic.renameField("Flicker", "has_twinkle");
        return dynamic;
    }

    public static Dynamic<?> fixProfile(Dynamic<?> dynamic) {
        Optional<String> optional = dynamic.asString().result();

        if (optional.isPresent()) {
            return isValidPlayerName((String) optional.get()) ? dynamic.emptyMap().set("name", dynamic.createString((String) optional.get())) : dynamic.emptyMap();
        } else {
            String s = dynamic.get("Name").asString("");
            Optional<? extends Dynamic<?>> optional1 = dynamic.get("Id").result();
            Dynamic<?> dynamic1 = fixProfileProperties(dynamic.get("Properties"));
            Dynamic<?> dynamic2 = dynamic.emptyMap();

            if (isValidPlayerName(s)) {
                dynamic2 = dynamic2.set("name", dynamic.createString(s));
            }

            if (optional1.isPresent()) {
                dynamic2 = dynamic2.set("id", (Dynamic) optional1.get());
            }

            if (dynamic1 != null) {
                dynamic2 = dynamic2.set("properties", dynamic1);
            }

            return dynamic2;
        }
    }

    private static boolean isValidPlayerName(String s) {
        return s.length() > 16 ? false : s.chars().filter((i) -> {
            return i <= 32 || i >= 127;
        }).findAny().isEmpty();
    }

    @Nullable
    private static Dynamic<?> fixProfileProperties(OptionalDynamic<?> optionaldynamic) {
        Map<String, List<Pair<String, Optional<String>>>> map = optionaldynamic.asMap((dynamic) -> {
            return dynamic.asString("");
        }, (dynamic) -> {
            return dynamic.asList((dynamic1) -> {
                String s = dynamic1.get("Value").asString("");
                Optional<String> optional = dynamic1.get("Signature").asString().result();

                return Pair.of(s, optional);
            });
        });

        return map.isEmpty() ? null : optionaldynamic.createList(map.entrySet().stream().flatMap((entry) -> {
            return ((List) entry.getValue()).stream().map((pair) -> {
                Dynamic<?> dynamic = optionaldynamic.emptyMap().set("name", optionaldynamic.createString((String) entry.getKey())).set("value", optionaldynamic.createString((String) pair.getFirst()));
                Optional<String> optional = (Optional) pair.getSecond();

                return optional.isPresent() ? dynamic.set("signature", optionaldynamic.createString((String) optional.get())) : dynamic;
            });
        }));
    }

    protected TypeRewriteRule makeRule() {
        return this.writeFixAndRead("ItemStack componentization", this.getInputSchema().getType(DataConverterTypes.ITEM_STACK), this.getOutputSchema().getType(DataConverterTypes.ITEM_STACK), (dynamic) -> {
            Optional<? extends Dynamic<?>> optional = ItemStackComponentizationFix.a.read(dynamic).map((itemstackcomponentizationfix_a) -> {
                fixItemStack(itemstackcomponentizationfix_a, itemstackcomponentizationfix_a.tag);
                return itemstackcomponentizationfix_a.write();
            });

            return (Dynamic) DataFixUtils.orElse(optional, dynamic);
        });
    }

    private static class a {

        private final String item;
        private final int count;
        private Dynamic<?> components;
        private final Dynamic<?> remainder;
        Dynamic<?> tag;

        private a(String s, int i, Dynamic<?> dynamic) {
            this.item = DataConverterSchemaNamed.ensureNamespaced(s);
            this.count = i;
            this.components = dynamic.emptyMap();
            this.tag = dynamic.get("tag").orElseEmptyMap();
            this.remainder = dynamic.remove("tag");
        }

        public static Optional<ItemStackComponentizationFix.a> read(Dynamic<?> dynamic) {
            return dynamic.get("id").asString().apply2stable((s, number) -> {
                return new ItemStackComponentizationFix.a(s, number.intValue(), dynamic.remove("id").remove("Count"));
            }, dynamic.get("Count").asNumber()).result();
        }

        public OptionalDynamic<?> removeTag(String s) {
            OptionalDynamic<?> optionaldynamic = this.tag.get(s);

            this.tag = this.tag.remove(s);
            return optionaldynamic;
        }

        public void setComponent(String s, Dynamic<?> dynamic) {
            this.components = this.components.set(s, dynamic);
        }

        public void setComponent(String s, OptionalDynamic<?> optionaldynamic) {
            optionaldynamic.result().ifPresent((dynamic) -> {
                this.components = this.components.set(s, dynamic);
            });
        }

        public Dynamic<?> moveTagInto(String s, Dynamic<?> dynamic, String s1) {
            Optional<? extends Dynamic<?>> optional = this.removeTag(s).result();

            return optional.isPresent() ? dynamic.set(s1, (Dynamic) optional.get()) : dynamic;
        }

        public void moveTagToComponent(String s, String s1, Dynamic<?> dynamic) {
            Optional<? extends Dynamic<?>> optional = this.removeTag(s).result();

            if (optional.isPresent() && !((Dynamic) optional.get()).equals(dynamic)) {
                this.setComponent(s1, (Dynamic) optional.get());
            }

        }

        public void moveTagToComponent(String s, String s1) {
            this.removeTag(s).result().ifPresent((dynamic) -> {
                this.setComponent(s1, dynamic);
            });
        }

        public void fixSubTag(String s, boolean flag, UnaryOperator<Dynamic<?>> unaryoperator) {
            OptionalDynamic<?> optionaldynamic = this.tag.get(s);

            if (!flag || !optionaldynamic.result().isEmpty()) {
                Dynamic<?> dynamic = optionaldynamic.orElseEmptyMap();

                dynamic = (Dynamic) unaryoperator.apply(dynamic);
                if (dynamic.equals(dynamic.emptyMap())) {
                    this.tag = this.tag.remove(s);
                } else {
                    this.tag = this.tag.set(s, dynamic);
                }

            }
        }

        public Dynamic<?> write() {
            Dynamic<?> dynamic = this.tag.emptyMap().set("id", this.tag.createString(this.item)).set("count", this.tag.createInt(this.count));

            if (!this.tag.equals(this.tag.emptyMap())) {
                this.components = this.components.set("minecraft:custom_data", this.tag);
            }

            if (!this.components.equals(this.tag.emptyMap())) {
                dynamic = dynamic.set("components", this.components);
            }

            return mergeRemainder(dynamic, this.remainder);
        }

        private static <T> Dynamic<T> mergeRemainder(Dynamic<T> dynamic, Dynamic<?> dynamic1) {
            DynamicOps<T> dynamicops = dynamic.getOps();

            return (Dynamic) dynamicops.getMap(dynamic.getValue()).flatMap((maplike) -> {
                return dynamicops.mergeToMap(dynamic1.convert(dynamicops).getValue(), maplike);
            }).map((object) -> {
                return new Dynamic(dynamicops, object);
            }).result().orElse(dynamic);
        }

        public boolean is(String s) {
            return this.item.equals(s);
        }

        public boolean is(Set<String> set) {
            return set.contains(this.item);
        }

        public boolean hasComponent(String s) {
            return this.components.get(s).result().isPresent();
        }
    }
}
