package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import org.slf4j.Logger;

public class DataConverterSavedDataUUID extends DataConverterUUIDBase {

    private static final Logger LOGGER = LogUtils.getLogger();

    public DataConverterSavedDataUUID(Schema schema) {
        super(schema, DataConverterTypes.SAVED_DATA_RAIDS);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("SavedDataUUIDFix", this.getInputSchema().getType(this.typeReference), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return dynamic.update("data", (dynamic1) -> {
                    return dynamic1.update("Raids", (dynamic2) -> {
                        return dynamic2.createList(dynamic2.asStream().map((dynamic3) -> {
                            return dynamic3.update("HeroesOfTheVillage", (dynamic4) -> {
                                return dynamic4.createList(dynamic4.asStream().map((dynamic5) -> {
                                    return (Dynamic) createUUIDFromLongs(dynamic5, "UUIDMost", "UUIDLeast").orElseGet(() -> {
                                        DataConverterSavedDataUUID.LOGGER.warn("HeroesOfTheVillage contained invalid UUIDs.");
                                        return dynamic5;
                                    });
                                }));
                            });
                        }));
                    });
                });
            });
        });
    }
}
