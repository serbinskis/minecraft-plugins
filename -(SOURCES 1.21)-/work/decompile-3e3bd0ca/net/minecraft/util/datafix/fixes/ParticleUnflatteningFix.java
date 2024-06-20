package net.minecraft.util.datafix.fixes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.datafix.schemas.DataConverterSchemaNamed;
import org.slf4j.Logger;

public class ParticleUnflatteningFix extends DataFix {

    private static final Logger LOGGER = LogUtils.getLogger();

    public ParticleUnflatteningFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(DataConverterTypes.PARTICLE);
        Type<?> type1 = this.getOutputSchema().getType(DataConverterTypes.PARTICLE);

        return this.writeFixAndRead("ParticleUnflatteningFix", type, type1, this::fix);
    }

    private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        Optional<String> optional = dynamic.asString().result();

        if (optional.isEmpty()) {
            return dynamic;
        } else {
            String s = (String) optional.get();
            String[] astring = s.split(" ", 2);
            String s1 = DataConverterSchemaNamed.ensureNamespaced(astring[0]);
            Dynamic<T> dynamic1 = dynamic.createMap(Map.of(dynamic.createString("type"), dynamic.createString(s1)));
            Dynamic dynamic2;

            switch (s1) {
                case "minecraft:item":
                    dynamic2 = astring.length > 1 ? this.updateItem(dynamic1, astring[1]) : dynamic1;
                    break;
                case "minecraft:block":
                case "minecraft:block_marker":
                case "minecraft:falling_dust":
                case "minecraft:dust_pillar":
                    dynamic2 = astring.length > 1 ? this.updateBlock(dynamic1, astring[1]) : dynamic1;
                    break;
                case "minecraft:dust":
                    dynamic2 = astring.length > 1 ? this.updateDust(dynamic1, astring[1]) : dynamic1;
                    break;
                case "minecraft:dust_color_transition":
                    dynamic2 = astring.length > 1 ? this.updateDustTransition(dynamic1, astring[1]) : dynamic1;
                    break;
                case "minecraft:sculk_charge":
                    dynamic2 = astring.length > 1 ? this.updateSculkCharge(dynamic1, astring[1]) : dynamic1;
                    break;
                case "minecraft:vibration":
                    dynamic2 = astring.length > 1 ? this.updateVibration(dynamic1, astring[1]) : dynamic1;
                    break;
                case "minecraft:shriek":
                    dynamic2 = astring.length > 1 ? this.updateShriek(dynamic1, astring[1]) : dynamic1;
                    break;
                default:
                    dynamic2 = dynamic1;
            }

            return dynamic2;
        }
    }

    private <T> Dynamic<T> updateItem(Dynamic<T> dynamic, String s) {
        int i = s.indexOf("{");
        Dynamic<T> dynamic1 = dynamic.createMap(Map.of(dynamic.createString("Count"), dynamic.createInt(1)));

        if (i == -1) {
            dynamic1 = dynamic1.set("id", dynamic.createString(s));
        } else {
            dynamic1 = dynamic1.set("id", dynamic.createString(s.substring(0, i)));
            NBTTagCompound nbttagcompound = parseTag(s.substring(i));

            if (nbttagcompound != null) {
                dynamic1 = dynamic1.set("tag", (new Dynamic(DynamicOpsNBT.INSTANCE, nbttagcompound)).convert(dynamic.getOps()));
            }
        }

        return dynamic.set("item", dynamic1);
    }

    @Nullable
    private static NBTTagCompound parseTag(String s) {
        try {
            return MojangsonParser.parseTag(s);
        } catch (Exception exception) {
            ParticleUnflatteningFix.LOGGER.warn("Failed to parse tag: {}", s, exception);
            return null;
        }
    }

    private <T> Dynamic<T> updateBlock(Dynamic<T> dynamic, String s) {
        int i = s.indexOf("[");
        Dynamic<T> dynamic1 = dynamic.emptyMap();

        if (i == -1) {
            dynamic1 = dynamic1.set("Name", dynamic.createString(DataConverterSchemaNamed.ensureNamespaced(s)));
        } else {
            dynamic1 = dynamic1.set("Name", dynamic.createString(DataConverterSchemaNamed.ensureNamespaced(s.substring(0, i))));
            Map<Dynamic<T>, Dynamic<T>> map = parseBlockProperties(dynamic, s.substring(i));

            if (!map.isEmpty()) {
                dynamic1 = dynamic1.set("Properties", dynamic.createMap(map));
            }
        }

        return dynamic.set("block_state", dynamic1);
    }

    private static <T> Map<Dynamic<T>, Dynamic<T>> parseBlockProperties(Dynamic<T> dynamic, String s) {
        try {
            Map<Dynamic<T>, Dynamic<T>> map = new HashMap();
            StringReader stringreader = new StringReader(s);

            stringreader.expect('[');
            stringreader.skipWhitespace();

            while (stringreader.canRead() && stringreader.peek() != ']') {
                stringreader.skipWhitespace();
                String s1 = stringreader.readString();

                stringreader.skipWhitespace();
                stringreader.expect('=');
                stringreader.skipWhitespace();
                String s2 = stringreader.readString();

                stringreader.skipWhitespace();
                map.put(dynamic.createString(s1), dynamic.createString(s2));
                if (stringreader.canRead()) {
                    if (stringreader.peek() != ',') {
                        break;
                    }

                    stringreader.skip();
                }
            }

            stringreader.expect(']');
            return map;
        } catch (Exception exception) {
            ParticleUnflatteningFix.LOGGER.warn("Failed to parse block properties: {}", s, exception);
            return Map.of();
        }
    }

    private static <T> Dynamic<T> readVector(Dynamic<T> dynamic, StringReader stringreader) throws CommandSyntaxException {
        float f = stringreader.readFloat();

        stringreader.expect(' ');
        float f1 = stringreader.readFloat();

        stringreader.expect(' ');
        float f2 = stringreader.readFloat();
        Stream stream = Stream.of(f, f1, f2);

        Objects.requireNonNull(dynamic);
        return dynamic.createList(stream.map(dynamic::createFloat));
    }

    private <T> Dynamic<T> updateDust(Dynamic<T> dynamic, String s) {
        try {
            StringReader stringreader = new StringReader(s);
            Dynamic<T> dynamic1 = readVector(dynamic, stringreader);

            stringreader.expect(' ');
            float f = stringreader.readFloat();

            return dynamic.set("color", dynamic1).set("scale", dynamic.createFloat(f));
        } catch (Exception exception) {
            ParticleUnflatteningFix.LOGGER.warn("Failed to parse particle options: {}", s, exception);
            return dynamic;
        }
    }

    private <T> Dynamic<T> updateDustTransition(Dynamic<T> dynamic, String s) {
        try {
            StringReader stringreader = new StringReader(s);
            Dynamic<T> dynamic1 = readVector(dynamic, stringreader);

            stringreader.expect(' ');
            float f = stringreader.readFloat();

            stringreader.expect(' ');
            Dynamic<T> dynamic2 = readVector(dynamic, stringreader);

            return dynamic.set("from_color", dynamic1).set("to_color", dynamic2).set("scale", dynamic.createFloat(f));
        } catch (Exception exception) {
            ParticleUnflatteningFix.LOGGER.warn("Failed to parse particle options: {}", s, exception);
            return dynamic;
        }
    }

    private <T> Dynamic<T> updateSculkCharge(Dynamic<T> dynamic, String s) {
        try {
            StringReader stringreader = new StringReader(s);
            float f = stringreader.readFloat();

            return dynamic.set("roll", dynamic.createFloat(f));
        } catch (Exception exception) {
            ParticleUnflatteningFix.LOGGER.warn("Failed to parse particle options: {}", s, exception);
            return dynamic;
        }
    }

    private <T> Dynamic<T> updateVibration(Dynamic<T> dynamic, String s) {
        try {
            StringReader stringreader = new StringReader(s);
            float f = (float) stringreader.readDouble();

            stringreader.expect(' ');
            float f1 = (float) stringreader.readDouble();

            stringreader.expect(' ');
            float f2 = (float) stringreader.readDouble();

            stringreader.expect(' ');
            int i = stringreader.readInt();
            Dynamic<T> dynamic1 = dynamic.createIntList(IntStream.of(new int[]{MathHelper.floor(f), MathHelper.floor(f1), MathHelper.floor(f2)}));
            Dynamic<T> dynamic2 = dynamic.createMap(Map.of(dynamic.createString("type"), dynamic.createString("minecraft:block"), dynamic.createString("pos"), dynamic1));

            return dynamic.set("destination", dynamic2).set("arrival_in_ticks", dynamic.createInt(i));
        } catch (Exception exception) {
            ParticleUnflatteningFix.LOGGER.warn("Failed to parse particle options: {}", s, exception);
            return dynamic;
        }
    }

    private <T> Dynamic<T> updateShriek(Dynamic<T> dynamic, String s) {
        try {
            StringReader stringreader = new StringReader(s);
            int i = stringreader.readInt();

            return dynamic.set("delay", dynamic.createInt(i));
        } catch (Exception exception) {
            ParticleUnflatteningFix.LOGGER.warn("Failed to parse particle options: {}", s, exception);
            return dynamic;
        }
    }
}
