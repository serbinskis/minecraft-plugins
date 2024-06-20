package net.minecraft.world.entity.decoration;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;

public class PaintingVariants {

    public static final ResourceKey<PaintingVariant> KEBAB = create("kebab");
    public static final ResourceKey<PaintingVariant> AZTEC = create("aztec");
    public static final ResourceKey<PaintingVariant> ALBAN = create("alban");
    public static final ResourceKey<PaintingVariant> AZTEC2 = create("aztec2");
    public static final ResourceKey<PaintingVariant> BOMB = create("bomb");
    public static final ResourceKey<PaintingVariant> PLANT = create("plant");
    public static final ResourceKey<PaintingVariant> WASTELAND = create("wasteland");
    public static final ResourceKey<PaintingVariant> POOL = create("pool");
    public static final ResourceKey<PaintingVariant> COURBET = create("courbet");
    public static final ResourceKey<PaintingVariant> SEA = create("sea");
    public static final ResourceKey<PaintingVariant> SUNSET = create("sunset");
    public static final ResourceKey<PaintingVariant> CREEBET = create("creebet");
    public static final ResourceKey<PaintingVariant> WANDERER = create("wanderer");
    public static final ResourceKey<PaintingVariant> GRAHAM = create("graham");
    public static final ResourceKey<PaintingVariant> MATCH = create("match");
    public static final ResourceKey<PaintingVariant> BUST = create("bust");
    public static final ResourceKey<PaintingVariant> STAGE = create("stage");
    public static final ResourceKey<PaintingVariant> VOID = create("void");
    public static final ResourceKey<PaintingVariant> SKULL_AND_ROSES = create("skull_and_roses");
    public static final ResourceKey<PaintingVariant> WITHER = create("wither");
    public static final ResourceKey<PaintingVariant> FIGHTERS = create("fighters");
    public static final ResourceKey<PaintingVariant> POINTER = create("pointer");
    public static final ResourceKey<PaintingVariant> PIGSCENE = create("pigscene");
    public static final ResourceKey<PaintingVariant> BURNING_SKULL = create("burning_skull");
    public static final ResourceKey<PaintingVariant> SKELETON = create("skeleton");
    public static final ResourceKey<PaintingVariant> DONKEY_KONG = create("donkey_kong");
    public static final ResourceKey<PaintingVariant> EARTH = create("earth");
    public static final ResourceKey<PaintingVariant> WIND = create("wind");
    public static final ResourceKey<PaintingVariant> WATER = create("water");
    public static final ResourceKey<PaintingVariant> FIRE = create("fire");
    public static final ResourceKey<PaintingVariant> BAROQUE = create("baroque");
    public static final ResourceKey<PaintingVariant> HUMBLE = create("humble");
    public static final ResourceKey<PaintingVariant> MEDITATIVE = create("meditative");
    public static final ResourceKey<PaintingVariant> PRAIRIE_RIDE = create("prairie_ride");
    public static final ResourceKey<PaintingVariant> UNPACKED = create("unpacked");
    public static final ResourceKey<PaintingVariant> BACKYARD = create("backyard");
    public static final ResourceKey<PaintingVariant> BOUQUET = create("bouquet");
    public static final ResourceKey<PaintingVariant> CAVEBIRD = create("cavebird");
    public static final ResourceKey<PaintingVariant> CHANGING = create("changing");
    public static final ResourceKey<PaintingVariant> COTAN = create("cotan");
    public static final ResourceKey<PaintingVariant> ENDBOSS = create("endboss");
    public static final ResourceKey<PaintingVariant> FERN = create("fern");
    public static final ResourceKey<PaintingVariant> FINDING = create("finding");
    public static final ResourceKey<PaintingVariant> LOWMIST = create("lowmist");
    public static final ResourceKey<PaintingVariant> ORB = create("orb");
    public static final ResourceKey<PaintingVariant> OWLEMONS = create("owlemons");
    public static final ResourceKey<PaintingVariant> PASSAGE = create("passage");
    public static final ResourceKey<PaintingVariant> POND = create("pond");
    public static final ResourceKey<PaintingVariant> SUNFLOWERS = create("sunflowers");
    public static final ResourceKey<PaintingVariant> TIDES = create("tides");

    public PaintingVariants() {}

    public static void bootstrap(BootstrapContext<PaintingVariant> bootstrapcontext) {
        register(bootstrapcontext, PaintingVariants.KEBAB, 1, 1);
        register(bootstrapcontext, PaintingVariants.AZTEC, 1, 1);
        register(bootstrapcontext, PaintingVariants.ALBAN, 1, 1);
        register(bootstrapcontext, PaintingVariants.AZTEC2, 1, 1);
        register(bootstrapcontext, PaintingVariants.BOMB, 1, 1);
        register(bootstrapcontext, PaintingVariants.PLANT, 1, 1);
        register(bootstrapcontext, PaintingVariants.WASTELAND, 1, 1);
        register(bootstrapcontext, PaintingVariants.POOL, 2, 1);
        register(bootstrapcontext, PaintingVariants.COURBET, 2, 1);
        register(bootstrapcontext, PaintingVariants.SEA, 2, 1);
        register(bootstrapcontext, PaintingVariants.SUNSET, 2, 1);
        register(bootstrapcontext, PaintingVariants.CREEBET, 2, 1);
        register(bootstrapcontext, PaintingVariants.WANDERER, 1, 2);
        register(bootstrapcontext, PaintingVariants.GRAHAM, 1, 2);
        register(bootstrapcontext, PaintingVariants.MATCH, 2, 2);
        register(bootstrapcontext, PaintingVariants.BUST, 2, 2);
        register(bootstrapcontext, PaintingVariants.STAGE, 2, 2);
        register(bootstrapcontext, PaintingVariants.VOID, 2, 2);
        register(bootstrapcontext, PaintingVariants.SKULL_AND_ROSES, 2, 2);
        register(bootstrapcontext, PaintingVariants.WITHER, 2, 2);
        register(bootstrapcontext, PaintingVariants.FIGHTERS, 4, 2);
        register(bootstrapcontext, PaintingVariants.POINTER, 4, 4);
        register(bootstrapcontext, PaintingVariants.PIGSCENE, 4, 4);
        register(bootstrapcontext, PaintingVariants.BURNING_SKULL, 4, 4);
        register(bootstrapcontext, PaintingVariants.SKELETON, 4, 3);
        register(bootstrapcontext, PaintingVariants.EARTH, 2, 2);
        register(bootstrapcontext, PaintingVariants.WIND, 2, 2);
        register(bootstrapcontext, PaintingVariants.WATER, 2, 2);
        register(bootstrapcontext, PaintingVariants.FIRE, 2, 2);
        register(bootstrapcontext, PaintingVariants.DONKEY_KONG, 4, 3);
        register(bootstrapcontext, PaintingVariants.BAROQUE, 2, 2);
        register(bootstrapcontext, PaintingVariants.HUMBLE, 2, 2);
        register(bootstrapcontext, PaintingVariants.MEDITATIVE, 1, 1);
        register(bootstrapcontext, PaintingVariants.PRAIRIE_RIDE, 1, 2);
        register(bootstrapcontext, PaintingVariants.UNPACKED, 4, 4);
        register(bootstrapcontext, PaintingVariants.BACKYARD, 3, 4);
        register(bootstrapcontext, PaintingVariants.BOUQUET, 3, 3);
        register(bootstrapcontext, PaintingVariants.CAVEBIRD, 3, 3);
        register(bootstrapcontext, PaintingVariants.CHANGING, 4, 2);
        register(bootstrapcontext, PaintingVariants.COTAN, 3, 3);
        register(bootstrapcontext, PaintingVariants.ENDBOSS, 3, 3);
        register(bootstrapcontext, PaintingVariants.FERN, 3, 3);
        register(bootstrapcontext, PaintingVariants.FINDING, 4, 2);
        register(bootstrapcontext, PaintingVariants.LOWMIST, 4, 2);
        register(bootstrapcontext, PaintingVariants.ORB, 4, 4);
        register(bootstrapcontext, PaintingVariants.OWLEMONS, 3, 3);
        register(bootstrapcontext, PaintingVariants.PASSAGE, 4, 2);
        register(bootstrapcontext, PaintingVariants.POND, 3, 4);
        register(bootstrapcontext, PaintingVariants.SUNFLOWERS, 3, 3);
        register(bootstrapcontext, PaintingVariants.TIDES, 3, 3);
    }

    private static void register(BootstrapContext<PaintingVariant> bootstrapcontext, ResourceKey<PaintingVariant> resourcekey, int i, int j) {
        bootstrapcontext.register(resourcekey, new PaintingVariant(i, j, resourcekey.location()));
    }

    private static ResourceKey<PaintingVariant> create(String s) {
        return ResourceKey.create(Registries.PAINTING_VARIANT, MinecraftKey.withDefaultNamespace(s));
    }
}
