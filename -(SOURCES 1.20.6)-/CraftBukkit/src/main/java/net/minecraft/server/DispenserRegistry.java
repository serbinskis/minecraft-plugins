package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandDispatcher;
import net.minecraft.commands.arguments.selector.options.PlayerSelector;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.IDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.LocaleLanguage;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeDefaults;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockComposter;
import net.minecraft.world.level.block.BlockFire;
import org.slf4j.Logger;

// CraftBukkit start
import net.minecraft.util.datafix.fixes.DataConverterFlattenData;
import net.minecraft.util.datafix.fixes.DataConverterMaterialId;
import net.minecraft.util.datafix.fixes.DataConverterSpawnEgg;
// CraftBukkit end

public class DispenserRegistry {

    public static final PrintStream STDOUT = System.out;
    private static volatile boolean isBootstrapped;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final AtomicLong bootstrapDuration = new AtomicLong(-1L);

    public DispenserRegistry() {}

    public static void bootStrap() {
        if (!DispenserRegistry.isBootstrapped) {
            // CraftBukkit start
            String name = DispenserRegistry.class.getSimpleName();
            switch (name) {
                case "DispenserRegistry":
                    break;
                case "Bootstrap":
                    System.err.println("***************************************************************************");
                    System.err.println("*** WARNING: This server jar may only be used for development purposes. ***");
                    System.err.println("***************************************************************************");
                    break;
                default:
                    System.err.println("**********************************************************************");
                    System.err.println("*** WARNING: This server jar is unsupported, use at your own risk. ***");
                    System.err.println("**********************************************************************");
                    break;
            }
            // CraftBukkit end
            DispenserRegistry.isBootstrapped = true;
            Instant instant = Instant.now();

            if (BuiltInRegistries.REGISTRY.keySet().isEmpty()) {
                throw new IllegalStateException("Unable to load registries");
            } else {
                BlockFire.bootStrap();
                BlockComposter.bootStrap();
                if (EntityTypes.getKey(EntityTypes.PLAYER) == null) {
                    throw new IllegalStateException("Failed loading EntityTypes");
                } else {
                    PlayerSelector.bootStrap();
                    IDispenseBehavior.bootStrap();
                    CauldronInteraction.bootStrap();
                    BuiltInRegistries.bootStrap();
                    CreativeModeTabs.validate();
                    wrapStreams();
                    DispenserRegistry.bootstrapDuration.set(Duration.between(instant, Instant.now()).toMillis());
                }
                // CraftBukkit start - easier than fixing the decompile
                DataConverterFlattenData.register(1008, "{Name:'minecraft:oak_sign',Properties:{rotation:'0'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'0'}}");
                DataConverterFlattenData.register(1009, "{Name:'minecraft:oak_sign',Properties:{rotation:'1'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'1'}}");
                DataConverterFlattenData.register(1010, "{Name:'minecraft:oak_sign',Properties:{rotation:'2'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'2'}}");
                DataConverterFlattenData.register(1011, "{Name:'minecraft:oak_sign',Properties:{rotation:'3'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'3'}}");
                DataConverterFlattenData.register(1012, "{Name:'minecraft:oak_sign',Properties:{rotation:'4'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'4'}}");
                DataConverterFlattenData.register(1013, "{Name:'minecraft:oak_sign',Properties:{rotation:'5'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'5'}}");
                DataConverterFlattenData.register(1014, "{Name:'minecraft:oak_sign',Properties:{rotation:'6'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'6'}}");
                DataConverterFlattenData.register(1015, "{Name:'minecraft:oak_sign',Properties:{rotation:'7'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'7'}}");
                DataConverterFlattenData.register(1016, "{Name:'minecraft:oak_sign',Properties:{rotation:'8'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'8'}}");
                DataConverterFlattenData.register(1017, "{Name:'minecraft:oak_sign',Properties:{rotation:'9'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'9'}}");
                DataConverterFlattenData.register(1018, "{Name:'minecraft:oak_sign',Properties:{rotation:'10'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'10'}}");
                DataConverterFlattenData.register(1019, "{Name:'minecraft:oak_sign',Properties:{rotation:'11'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'11'}}");
                DataConverterFlattenData.register(1020, "{Name:'minecraft:oak_sign',Properties:{rotation:'12'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'12'}}");
                DataConverterFlattenData.register(1021, "{Name:'minecraft:oak_sign',Properties:{rotation:'13'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'13'}}");
                DataConverterFlattenData.register(1022, "{Name:'minecraft:oak_sign',Properties:{rotation:'14'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'14'}}");
                DataConverterFlattenData.register(1023, "{Name:'minecraft:oak_sign',Properties:{rotation:'15'}}", "{Name:'minecraft:standing_sign',Properties:{rotation:'15'}}");
                DataConverterMaterialId.ITEM_NAMES.put(323, "minecraft:oak_sign");

                DataConverterFlattenData.register(1440, "{Name:\'minecraft:portal\',Properties:{axis:\'x\'}}", new String[]{"{Name:\'minecraft:portal\',Properties:{axis:\'x\'}}"});

                DataConverterMaterialId.ITEM_NAMES.put(409, "minecraft:prismarine_shard");
                DataConverterMaterialId.ITEM_NAMES.put(410, "minecraft:prismarine_crystals");
                DataConverterMaterialId.ITEM_NAMES.put(411, "minecraft:rabbit");
                DataConverterMaterialId.ITEM_NAMES.put(412, "minecraft:cooked_rabbit");
                DataConverterMaterialId.ITEM_NAMES.put(413, "minecraft:rabbit_stew");
                DataConverterMaterialId.ITEM_NAMES.put(414, "minecraft:rabbit_foot");
                DataConverterMaterialId.ITEM_NAMES.put(415, "minecraft:rabbit_hide");
                DataConverterMaterialId.ITEM_NAMES.put(416, "minecraft:armor_stand");

                DataConverterMaterialId.ITEM_NAMES.put(423, "minecraft:mutton");
                DataConverterMaterialId.ITEM_NAMES.put(424, "minecraft:cooked_mutton");
                DataConverterMaterialId.ITEM_NAMES.put(425, "minecraft:banner");
                DataConverterMaterialId.ITEM_NAMES.put(426, "minecraft:end_crystal");
                DataConverterMaterialId.ITEM_NAMES.put(427, "minecraft:spruce_door");
                DataConverterMaterialId.ITEM_NAMES.put(428, "minecraft:birch_door");
                DataConverterMaterialId.ITEM_NAMES.put(429, "minecraft:jungle_door");
                DataConverterMaterialId.ITEM_NAMES.put(430, "minecraft:acacia_door");
                DataConverterMaterialId.ITEM_NAMES.put(431, "minecraft:dark_oak_door");
                DataConverterMaterialId.ITEM_NAMES.put(432, "minecraft:chorus_fruit");
                DataConverterMaterialId.ITEM_NAMES.put(433, "minecraft:chorus_fruit_popped");
                DataConverterMaterialId.ITEM_NAMES.put(434, "minecraft:beetroot");
                DataConverterMaterialId.ITEM_NAMES.put(435, "minecraft:beetroot_seeds");
                DataConverterMaterialId.ITEM_NAMES.put(436, "minecraft:beetroot_soup");
                DataConverterMaterialId.ITEM_NAMES.put(437, "minecraft:dragon_breath");
                DataConverterMaterialId.ITEM_NAMES.put(438, "minecraft:splash_potion");
                DataConverterMaterialId.ITEM_NAMES.put(439, "minecraft:spectral_arrow");
                DataConverterMaterialId.ITEM_NAMES.put(440, "minecraft:tipped_arrow");
                DataConverterMaterialId.ITEM_NAMES.put(441, "minecraft:lingering_potion");
                DataConverterMaterialId.ITEM_NAMES.put(442, "minecraft:shield");
                DataConverterMaterialId.ITEM_NAMES.put(443, "minecraft:elytra");
                DataConverterMaterialId.ITEM_NAMES.put(444, "minecraft:spruce_boat");
                DataConverterMaterialId.ITEM_NAMES.put(445, "minecraft:birch_boat");
                DataConverterMaterialId.ITEM_NAMES.put(446, "minecraft:jungle_boat");
                DataConverterMaterialId.ITEM_NAMES.put(447, "minecraft:acacia_boat");
                DataConverterMaterialId.ITEM_NAMES.put(448, "minecraft:dark_oak_boat");
                DataConverterMaterialId.ITEM_NAMES.put(449, "minecraft:totem_of_undying");
                DataConverterMaterialId.ITEM_NAMES.put(450, "minecraft:shulker_shell");
                DataConverterMaterialId.ITEM_NAMES.put(452, "minecraft:iron_nugget");
                DataConverterMaterialId.ITEM_NAMES.put(453, "minecraft:knowledge_book");

                DataConverterSpawnEgg.ID_TO_ENTITY[23] = "Arrow";
                // CraftBukkit end
            }
        }
    }

    private static <T> void checkTranslations(Iterable<T> iterable, Function<T, String> function, Set<String> set) {
        LocaleLanguage localelanguage = LocaleLanguage.getInstance();

        iterable.forEach((object) -> {
            String s = (String) function.apply(object);

            if (!localelanguage.has(s)) {
                set.add(s);
            }

        });
    }

    private static void checkGameruleTranslations(final Set<String> set) {
        final LocaleLanguage localelanguage = LocaleLanguage.getInstance();

        GameRules.visitGameRuleTypes(new GameRules.GameRuleVisitor() {
            @Override
            public <T extends GameRules.GameRuleValue<T>> void visit(GameRules.GameRuleKey<T> gamerules_gamerulekey, GameRules.GameRuleDefinition<T> gamerules_gameruledefinition) {
                if (!localelanguage.has(gamerules_gamerulekey.getDescriptionId())) {
                    set.add(gamerules_gamerulekey.getId());
                }

            }
        });
    }

    public static Set<String> getMissingTranslations() {
        Set<String> set = new TreeSet();

        checkTranslations(BuiltInRegistries.ATTRIBUTE, AttributeBase::getDescriptionId, set);
        checkTranslations(BuiltInRegistries.ENTITY_TYPE, EntityTypes::getDescriptionId, set);
        checkTranslations(BuiltInRegistries.MOB_EFFECT, MobEffectList::getDescriptionId, set);
        checkTranslations(BuiltInRegistries.ITEM, Item::getDescriptionId, set);
        checkTranslations(BuiltInRegistries.ENCHANTMENT, Enchantment::getDescriptionId, set);
        checkTranslations(BuiltInRegistries.BLOCK, Block::getDescriptionId, set);
        checkTranslations(BuiltInRegistries.CUSTOM_STAT, (minecraftkey) -> {
            String s = minecraftkey.toString();

            return "stat." + s.replace(':', '.');
        }, set);
        checkGameruleTranslations(set);
        return set;
    }

    public static void checkBootstrapCalled(Supplier<String> supplier) {
        if (!DispenserRegistry.isBootstrapped) {
            throw createBootstrapException(supplier);
        }
    }

    private static RuntimeException createBootstrapException(Supplier<String> supplier) {
        try {
            String s = (String) supplier.get();

            return new IllegalArgumentException("Not bootstrapped (called from " + s + ")");
        } catch (Exception exception) {
            IllegalArgumentException illegalargumentexception = new IllegalArgumentException("Not bootstrapped (failed to resolve location)");

            illegalargumentexception.addSuppressed(exception);
            return illegalargumentexception;
        }
    }

    public static void validate() {
        checkBootstrapCalled(() -> {
            return "validate";
        });
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            getMissingTranslations().forEach((s) -> {
                DispenserRegistry.LOGGER.error("Missing translations: {}", s);
            });
            CommandDispatcher.validate();
        }

        AttributeDefaults.validate();
    }

    private static void wrapStreams() {
        if (DispenserRegistry.LOGGER.isDebugEnabled()) {
            System.setErr(new DebugOutputStream("STDERR", System.err));
            System.setOut(new DebugOutputStream("STDOUT", DispenserRegistry.STDOUT));
        } else {
            System.setErr(new RedirectStream("STDERR", System.err));
            System.setOut(new RedirectStream("STDOUT", DispenserRegistry.STDOUT));
        }

    }

    public static void realStdoutPrintln(String s) {
        DispenserRegistry.STDOUT.println(s);
    }
}
