package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DataConverterBedBlock extends DataFix {

    public DataConverterBedBlock(Schema schema, boolean flag) {
        super(schema, flag);
    }

    public TypeRewriteRule makeRule() {
        Type<?> type = this.getOutputSchema().getType(DataConverterTypes.CHUNK);
        Type<?> type1 = type.findFieldType("Level");
        Type<?> type2 = type1.findFieldType("TileEntities");

        if (!(type2 instanceof ListType<?> listtype)) {
            throw new IllegalStateException("Tile entity type is not a list type.");
        } else {
            return this.cap(type1, listtype);
        }
    }

    private <TE> TypeRewriteRule cap(Type<?> type, ListType<TE> listtype) {
        Type<TE> type1 = listtype.getElement();
        OpticFinder<?> opticfinder = DSL.fieldFinder("Level", type);
        OpticFinder<List<TE>> opticfinder1 = DSL.fieldFinder("TileEntities", listtype);
        boolean flag = true;

        return TypeRewriteRule.seq(this.fixTypeEverywhere("InjectBedBlockEntityType", this.getInputSchema().findChoiceType(DataConverterTypes.BLOCK_ENTITY), this.getOutputSchema().findChoiceType(DataConverterTypes.BLOCK_ENTITY), (dynamicops) -> {
            return (pair) -> {
                return pair;
            };
        }), this.fixTypeEverywhereTyped("BedBlockEntityInjecter", this.getOutputSchema().getType(DataConverterTypes.CHUNK), (typed) -> {
            Typed<?> typed1 = typed.getTyped(opticfinder);
            Dynamic<?> dynamic = (Dynamic) typed1.get(DSL.remainderFinder());
            int i = dynamic.get("xPos").asInt(0);
            int j = dynamic.get("zPos").asInt(0);
            List<TE> list = Lists.newArrayList((Iterable) typed1.getOrCreate(opticfinder1));
            List<? extends Dynamic<?>> list1 = dynamic.get("Sections").asList(Function.identity());
            Iterator iterator = list1.iterator();

            while (iterator.hasNext()) {
                Dynamic<?> dynamic1 = (Dynamic) iterator.next();
                int k = dynamic1.get("Y").asInt(0);

                Streams.mapWithIndex(dynamic1.get("Blocks").asIntStream(), (l, i1) -> {
                    if (416 == (l & 255) << 4) {
                        int j1 = (int) i1;
                        int k1 = j1 & 15;
                        int l1 = j1 >> 8 & 15;
                        int i2 = j1 >> 4 & 15;
                        Map<Dynamic<?>, Dynamic<?>> map = Maps.newHashMap();

                        map.put(dynamic1.createString("id"), dynamic1.createString("minecraft:bed"));
                        map.put(dynamic1.createString("x"), dynamic1.createInt(k1 + (i << 4)));
                        map.put(dynamic1.createString("y"), dynamic1.createInt(l1 + (k << 4)));
                        map.put(dynamic1.createString("z"), dynamic1.createInt(i2 + (j << 4)));
                        map.put(dynamic1.createString("color"), dynamic1.createShort((short) 14));
                        return map;
                    } else {
                        return null;
                    }
                }).forEachOrdered((map) -> {
                    if (map != null) {
                        list.add(((Pair) type1.read(dynamic1.createMap(map)).result().orElseThrow(() -> {
                            return new IllegalStateException("Could not parse newly created bed block entity.");
                        })).getFirst());
                    }

                });
            }

            return !list.isEmpty() ? typed.set(opticfinder, typed1.set(opticfinder1, list)) : typed;
        }));
    }
}
