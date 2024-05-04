package net.minecraft.world.level.block.entity;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class DecoratedPotPatterns {

    private static final String BASE_NAME = "decorated_pot_base";
    public static final ResourceKey<String> BASE = create("decorated_pot_base");
    private static final String BRICK_NAME = "decorated_pot_side";
    private static final String ANGLER_NAME = "angler_pottery_pattern";
    private static final String ARCHER_NAME = "archer_pottery_pattern";
    private static final String ARMS_UP_NAME = "arms_up_pottery_pattern";
    private static final String BLADE_NAME = "blade_pottery_pattern";
    private static final String BREWER_NAME = "brewer_pottery_pattern";
    private static final String BURN_NAME = "burn_pottery_pattern";
    private static final String DANGER_NAME = "danger_pottery_pattern";
    private static final String EXPLORER_NAME = "explorer_pottery_pattern";
    private static final String FLOW_NAME = "flow_pottery_pattern";
    private static final String FRIEND_NAME = "friend_pottery_pattern";
    private static final String GUSTER_NAME = "guster_pottery_pattern";
    private static final String HEART_NAME = "heart_pottery_pattern";
    private static final String HEARTBREAK_NAME = "heartbreak_pottery_pattern";
    private static final String HOWL_NAME = "howl_pottery_pattern";
    private static final String MINER_NAME = "miner_pottery_pattern";
    private static final String MOURNER_NAME = "mourner_pottery_pattern";
    private static final String PLENTY_NAME = "plenty_pottery_pattern";
    private static final String PRIZE_NAME = "prize_pottery_pattern";
    private static final String SCRAPE_NAME = "scrape_pottery_pattern";
    private static final String SHEAF_NAME = "sheaf_pottery_pattern";
    private static final String SHELTER_NAME = "shelter_pottery_pattern";
    private static final String SKULL_NAME = "skull_pottery_pattern";
    private static final String SNORT_NAME = "snort_pottery_pattern";
    private static final ResourceKey<String> BRICK = create("decorated_pot_side");
    private static final ResourceKey<String> ANGLER = create("angler_pottery_pattern");
    private static final ResourceKey<String> ARCHER = create("archer_pottery_pattern");
    private static final ResourceKey<String> ARMS_UP = create("arms_up_pottery_pattern");
    private static final ResourceKey<String> BLADE = create("blade_pottery_pattern");
    private static final ResourceKey<String> BREWER = create("brewer_pottery_pattern");
    private static final ResourceKey<String> BURN = create("burn_pottery_pattern");
    private static final ResourceKey<String> DANGER = create("danger_pottery_pattern");
    private static final ResourceKey<String> EXPLORER = create("explorer_pottery_pattern");
    private static final ResourceKey<String> FLOW = create("flow_pottery_pattern");
    private static final ResourceKey<String> FRIEND = create("friend_pottery_pattern");
    private static final ResourceKey<String> GUSTER = create("guster_pottery_pattern");
    private static final ResourceKey<String> HEART = create("heart_pottery_pattern");
    private static final ResourceKey<String> HEARTBREAK = create("heartbreak_pottery_pattern");
    private static final ResourceKey<String> HOWL = create("howl_pottery_pattern");
    private static final ResourceKey<String> MINER = create("miner_pottery_pattern");
    private static final ResourceKey<String> MOURNER = create("mourner_pottery_pattern");
    private static final ResourceKey<String> PLENTY = create("plenty_pottery_pattern");
    private static final ResourceKey<String> PRIZE = create("prize_pottery_pattern");
    private static final ResourceKey<String> SCRAPE = create("scrape_pottery_pattern");
    private static final ResourceKey<String> SHEAF = create("sheaf_pottery_pattern");
    private static final ResourceKey<String> SHELTER = create("shelter_pottery_pattern");
    private static final ResourceKey<String> SKULL = create("skull_pottery_pattern");
    private static final ResourceKey<String> SNORT = create("snort_pottery_pattern");
    private static final Map<Item, ResourceKey<String>> ITEM_TO_POT_TEXTURE = Map.ofEntries(Map.entry(Items.BRICK, DecoratedPotPatterns.BRICK), Map.entry(Items.ANGLER_POTTERY_SHERD, DecoratedPotPatterns.ANGLER), Map.entry(Items.ARCHER_POTTERY_SHERD, DecoratedPotPatterns.ARCHER), Map.entry(Items.ARMS_UP_POTTERY_SHERD, DecoratedPotPatterns.ARMS_UP), Map.entry(Items.BLADE_POTTERY_SHERD, DecoratedPotPatterns.BLADE), Map.entry(Items.BREWER_POTTERY_SHERD, DecoratedPotPatterns.BREWER), Map.entry(Items.BURN_POTTERY_SHERD, DecoratedPotPatterns.BURN), Map.entry(Items.DANGER_POTTERY_SHERD, DecoratedPotPatterns.DANGER), Map.entry(Items.EXPLORER_POTTERY_SHERD, DecoratedPotPatterns.EXPLORER), Map.entry(Items.FLOW_POTTERY_SHERD, DecoratedPotPatterns.FLOW), Map.entry(Items.FRIEND_POTTERY_SHERD, DecoratedPotPatterns.FRIEND), Map.entry(Items.GUSTER_POTTERY_SHERD, DecoratedPotPatterns.GUSTER), Map.entry(Items.HEART_POTTERY_SHERD, DecoratedPotPatterns.HEART), Map.entry(Items.HEARTBREAK_POTTERY_SHERD, DecoratedPotPatterns.HEARTBREAK), Map.entry(Items.HOWL_POTTERY_SHERD, DecoratedPotPatterns.HOWL), Map.entry(Items.MINER_POTTERY_SHERD, DecoratedPotPatterns.MINER), Map.entry(Items.MOURNER_POTTERY_SHERD, DecoratedPotPatterns.MOURNER), Map.entry(Items.PLENTY_POTTERY_SHERD, DecoratedPotPatterns.PLENTY), Map.entry(Items.PRIZE_POTTERY_SHERD, DecoratedPotPatterns.PRIZE), Map.entry(Items.SCRAPE_POTTERY_SHERD, DecoratedPotPatterns.SCRAPE), Map.entry(Items.SHEAF_POTTERY_SHERD, DecoratedPotPatterns.SHEAF), Map.entry(Items.SHELTER_POTTERY_SHERD, DecoratedPotPatterns.SHELTER), Map.entry(Items.SKULL_POTTERY_SHERD, DecoratedPotPatterns.SKULL), Map.entry(Items.SNORT_POTTERY_SHERD, DecoratedPotPatterns.SNORT));

    public DecoratedPotPatterns() {}

    private static ResourceKey<String> create(String s) {
        return ResourceKey.create(Registries.DECORATED_POT_PATTERNS, new MinecraftKey(s));
    }

    public static MinecraftKey location(ResourceKey<String> resourcekey) {
        return resourcekey.location().withPrefix("entity/decorated_pot/");
    }

    @Nullable
    public static ResourceKey<String> getResourceKey(Item item) {
        return (ResourceKey) DecoratedPotPatterns.ITEM_TO_POT_TEXTURE.get(item);
    }

    public static String bootstrap(IRegistry<String> iregistry) {
        IRegistry.register(iregistry, DecoratedPotPatterns.BRICK, "decorated_pot_side");
        IRegistry.register(iregistry, DecoratedPotPatterns.ANGLER, "angler_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.ARCHER, "archer_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.ARMS_UP, "arms_up_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.BLADE, "blade_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.BREWER, "brewer_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.BURN, "burn_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.DANGER, "danger_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.EXPLORER, "explorer_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.FLOW, "flow_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.FRIEND, "friend_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.GUSTER, "guster_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.HEART, "heart_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.HEARTBREAK, "heartbreak_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.HOWL, "howl_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.MINER, "miner_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.MOURNER, "mourner_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.PLENTY, "plenty_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.PRIZE, "prize_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.SCRAPE, "scrape_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.SHEAF, "sheaf_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.SHELTER, "shelter_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.SKULL, "skull_pottery_pattern");
        IRegistry.register(iregistry, DecoratedPotPatterns.SNORT, "snort_pottery_pattern");
        return (String) IRegistry.register(iregistry, DecoratedPotPatterns.BASE, "decorated_pot_base");
    }
}
