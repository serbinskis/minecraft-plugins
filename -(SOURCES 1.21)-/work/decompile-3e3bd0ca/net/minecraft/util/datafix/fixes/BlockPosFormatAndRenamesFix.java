package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class BlockPosFormatAndRenamesFix extends DataFix {

    private static final List<String> PATROLLING_MOBS = List.of("minecraft:witch", "minecraft:ravager", "minecraft:pillager", "minecraft:illusioner", "minecraft:evoker", "minecraft:vindicator");

    public BlockPosFormatAndRenamesFix(Schema schema) {
        super(schema, false);
    }

    private Typed<?> fixFields(Typed<?> typed, Map<String, String> map) {
        return typed.update(DSL.remainderFinder(), (dynamic) -> {
            Entry entry;

            for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); dynamic = dynamic.renameAndFixField((String) entry.getKey(), (String) entry.getValue(), ExtraDataFixUtils::fixBlockPos)) {
                entry = (Entry) iterator.next();
            }

            return dynamic;
        });
    }

    private <T> Dynamic<T> fixMapSavedData(Dynamic<T> dynamic) {
        return dynamic.update("frames", (dynamic1) -> {
            return dynamic1.createList(dynamic1.asStream().map((dynamic2) -> {
                dynamic2 = dynamic2.renameAndFixField("Pos", "pos", ExtraDataFixUtils::fixBlockPos);
                dynamic2 = dynamic2.renameField("Rotation", "rotation");
                dynamic2 = dynamic2.renameField("EntityId", "entity_id");
                return dynamic2;
            }));
        }).update("banners", (dynamic1) -> {
            return dynamic1.createList(dynamic1.asStream().map((dynamic2) -> {
                dynamic2 = dynamic2.renameField("Pos", "pos");
                dynamic2 = dynamic2.renameField("Color", "color");
                dynamic2 = dynamic2.renameField("Name", "name");
                return dynamic2;
            }));
        });
    }

    public TypeRewriteRule makeRule() {
        List<TypeRewriteRule> list = new ArrayList();

        this.addEntityRules(list);
        this.addBlockEntityRules(list);
        list.add(this.fixTypeEverywhereTyped("BlockPos format for map frames", this.getInputSchema().getType(DataConverterTypes.SAVED_DATA_MAP_DATA), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return dynamic.update("data", this::fixMapSavedData);
            });
        }));
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.ITEM_STACK);

        list.add(this.fixTypeEverywhereTyped("BlockPos format for compass target", type, ItemStackTagFix.createFixer(type, "minecraft:compass"::equals, (dynamic) -> {
            return dynamic.update("LodestonePos", ExtraDataFixUtils::fixBlockPos);
        })));
        return TypeRewriteRule.seq(list);
    }

    private void addEntityRules(List<TypeRewriteRule> list) {
        list.add(this.createEntityFixer(DataConverterTypes.ENTITY, "minecraft:bee", Map.of("HivePos", "hive_pos", "FlowerPos", "flower_pos")));
        list.add(this.createEntityFixer(DataConverterTypes.ENTITY, "minecraft:end_crystal", Map.of("BeamTarget", "beam_target")));
        list.add(this.createEntityFixer(DataConverterTypes.ENTITY, "minecraft:wandering_trader", Map.of("WanderTarget", "wander_target")));
        Iterator iterator = BlockPosFormatAndRenamesFix.PATROLLING_MOBS.iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            list.add(this.createEntityFixer(DataConverterTypes.ENTITY, s, Map.of("PatrolTarget", "patrol_target")));
        }

        list.add(this.fixTypeEverywhereTyped("BlockPos format in Leash for mobs", this.getInputSchema().getType(DataConverterTypes.ENTITY), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return dynamic.renameAndFixField("Leash", "leash", ExtraDataFixUtils::fixBlockPos);
            });
        }));
    }

    private void addBlockEntityRules(List<TypeRewriteRule> list) {
        list.add(this.createEntityFixer(DataConverterTypes.BLOCK_ENTITY, "minecraft:beehive", Map.of("FlowerPos", "flower_pos")));
        list.add(this.createEntityFixer(DataConverterTypes.BLOCK_ENTITY, "minecraft:end_gateway", Map.of("ExitPortal", "exit_portal")));
    }

    private TypeRewriteRule createEntityFixer(TypeReference typereference, String s, Map<String, String> map) {
        String s1 = "BlockPos format in " + String.valueOf(map.keySet()) + " for " + s + " (" + typereference.typeName() + ")";
        OpticFinder<?> opticfinder = DSL.namedChoice(s, this.getInputSchema().getChoiceType(typereference, s));

        return this.fixTypeEverywhereTyped(s1, this.getInputSchema().getType(typereference), (typed) -> {
            return typed.updateTyped(opticfinder, (typed1) -> {
                return this.fixFields(typed1, map);
            });
        });
    }
}
