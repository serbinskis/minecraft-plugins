package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class MapBannerBlockPosFormatFix extends DataFix {

    public MapBannerBlockPosFormatFix(Schema schema) {
        super(schema, false);
    }

    private static <T> Dynamic<T> fixMapSavedData(Dynamic<T> dynamic) {
        return dynamic.update("banners", (dynamic1) -> {
            return dynamic1.createList(dynamic1.asStream().map((dynamic2) -> {
                return dynamic2.update("Pos", ExtraDataFixUtils::fixBlockPos);
            }));
        });
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("MapBannerBlockPosFormatFix", this.getInputSchema().getType(DataConverterTypes.SAVED_DATA_MAP_DATA), (typed) -> {
            return typed.update(DSL.remainderFinder(), (dynamic) -> {
                return dynamic.update("data", MapBannerBlockPosFormatFix::fixMapSavedData);
            });
        });
    }
}
