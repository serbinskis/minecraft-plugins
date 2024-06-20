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

    public static final ResourceKey<DecoratedPotPattern> BLANK = create("blank");
    public static final ResourceKey<DecoratedPotPattern> ANGLER = create("angler");
    public static final ResourceKey<DecoratedPotPattern> ARCHER = create("archer");
    public static final ResourceKey<DecoratedPotPattern> ARMS_UP = create("arms_up");
    public static final ResourceKey<DecoratedPotPattern> BLADE = create("blade");
    public static final ResourceKey<DecoratedPotPattern> BREWER = create("brewer");
    public static final ResourceKey<DecoratedPotPattern> BURN = create("burn");
    public static final ResourceKey<DecoratedPotPattern> DANGER = create("danger");
    public static final ResourceKey<DecoratedPotPattern> EXPLORER = create("explorer");
    public static final ResourceKey<DecoratedPotPattern> FLOW = create("flow");
    public static final ResourceKey<DecoratedPotPattern> FRIEND = create("friend");
    public static final ResourceKey<DecoratedPotPattern> GUSTER = create("guster");
    public static final ResourceKey<DecoratedPotPattern> HEART = create("heart");
    public static final ResourceKey<DecoratedPotPattern> HEARTBREAK = create("heartbreak");
    public static final ResourceKey<DecoratedPotPattern> HOWL = create("howl");
    public static final ResourceKey<DecoratedPotPattern> MINER = create("miner");
    public static final ResourceKey<DecoratedPotPattern> MOURNER = create("mourner");
    public static final ResourceKey<DecoratedPotPattern> PLENTY = create("plenty");
    public static final ResourceKey<DecoratedPotPattern> PRIZE = create("prize");
    public static final ResourceKey<DecoratedPotPattern> SCRAPE = create("scrape");
    public static final ResourceKey<DecoratedPotPattern> SHEAF = create("sheaf");
    public static final ResourceKey<DecoratedPotPattern> SHELTER = create("shelter");
    public static final ResourceKey<DecoratedPotPattern> SKULL = create("skull");
    public static final ResourceKey<DecoratedPotPattern> SNORT = create("snort");
    private static final Map<Item, ResourceKey<DecoratedPotPattern>> ITEM_TO_POT_TEXTURE = Map.ofEntries(Map.entry(Items.BRICK, DecoratedPotPatterns.BLANK), Map.entry(Items.ANGLER_POTTERY_SHERD, DecoratedPotPatterns.ANGLER), Map.entry(Items.ARCHER_POTTERY_SHERD, DecoratedPotPatterns.ARCHER), Map.entry(Items.ARMS_UP_POTTERY_SHERD, DecoratedPotPatterns.ARMS_UP), Map.entry(Items.BLADE_POTTERY_SHERD, DecoratedPotPatterns.BLADE), Map.entry(Items.BREWER_POTTERY_SHERD, DecoratedPotPatterns.BREWER), Map.entry(Items.BURN_POTTERY_SHERD, DecoratedPotPatterns.BURN), Map.entry(Items.DANGER_POTTERY_SHERD, DecoratedPotPatterns.DANGER), Map.entry(Items.EXPLORER_POTTERY_SHERD, DecoratedPotPatterns.EXPLORER), Map.entry(Items.FLOW_POTTERY_SHERD, DecoratedPotPatterns.FLOW), Map.entry(Items.FRIEND_POTTERY_SHERD, DecoratedPotPatterns.FRIEND), Map.entry(Items.GUSTER_POTTERY_SHERD, DecoratedPotPatterns.GUSTER), Map.entry(Items.HEART_POTTERY_SHERD, DecoratedPotPatterns.HEART), Map.entry(Items.HEARTBREAK_POTTERY_SHERD, DecoratedPotPatterns.HEARTBREAK), Map.entry(Items.HOWL_POTTERY_SHERD, DecoratedPotPatterns.HOWL), Map.entry(Items.MINER_POTTERY_SHERD, DecoratedPotPatterns.MINER), Map.entry(Items.MOURNER_POTTERY_SHERD, DecoratedPotPatterns.MOURNER), Map.entry(Items.PLENTY_POTTERY_SHERD, DecoratedPotPatterns.PLENTY), Map.entry(Items.PRIZE_POTTERY_SHERD, DecoratedPotPatterns.PRIZE), Map.entry(Items.SCRAPE_POTTERY_SHERD, DecoratedPotPatterns.SCRAPE), Map.entry(Items.SHEAF_POTTERY_SHERD, DecoratedPotPatterns.SHEAF), Map.entry(Items.SHELTER_POTTERY_SHERD, DecoratedPotPatterns.SHELTER), Map.entry(Items.SKULL_POTTERY_SHERD, DecoratedPotPatterns.SKULL), Map.entry(Items.SNORT_POTTERY_SHERD, DecoratedPotPatterns.SNORT));

    public DecoratedPotPatterns() {}

    @Nullable
    public static ResourceKey<DecoratedPotPattern> getPatternFromItem(Item item) {
        return (ResourceKey) DecoratedPotPatterns.ITEM_TO_POT_TEXTURE.get(item);
    }

    private static ResourceKey<DecoratedPotPattern> create(String s) {
        return ResourceKey.create(Registries.DECORATED_POT_PATTERN, MinecraftKey.withDefaultNamespace(s));
    }

    public static DecoratedPotPattern bootstrap(IRegistry<DecoratedPotPattern> iregistry) {
        register(iregistry, DecoratedPotPatterns.ANGLER, "angler_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.ARCHER, "archer_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.ARMS_UP, "arms_up_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.BLADE, "blade_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.BREWER, "brewer_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.BURN, "burn_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.DANGER, "danger_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.EXPLORER, "explorer_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.FLOW, "flow_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.FRIEND, "friend_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.GUSTER, "guster_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.HEART, "heart_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.HEARTBREAK, "heartbreak_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.HOWL, "howl_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.MINER, "miner_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.MOURNER, "mourner_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.PLENTY, "plenty_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.PRIZE, "prize_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.SCRAPE, "scrape_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.SHEAF, "sheaf_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.SHELTER, "shelter_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.SKULL, "skull_pottery_pattern");
        register(iregistry, DecoratedPotPatterns.SNORT, "snort_pottery_pattern");
        return register(iregistry, DecoratedPotPatterns.BLANK, "decorated_pot_side");
    }

    private static DecoratedPotPattern register(IRegistry<DecoratedPotPattern> iregistry, ResourceKey<DecoratedPotPattern> resourcekey, String s) {
        return (DecoratedPotPattern) IRegistry.register(iregistry, resourcekey, new DecoratedPotPattern(MinecraftKey.withDefaultNamespace(s)));
    }
}
