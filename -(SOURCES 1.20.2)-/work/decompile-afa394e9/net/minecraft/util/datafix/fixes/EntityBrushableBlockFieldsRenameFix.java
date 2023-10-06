package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class EntityBrushableBlockFieldsRenameFix extends DataConverterNamedEntity {

    public EntityBrushableBlockFieldsRenameFix(Schema schema) {
        super(schema, false, "EntityBrushableBlockFieldsRenameFix", DataConverterTypes.BLOCK_ENTITY, "minecraft:brushable_block");
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic) {
        return this.renameField(this.renameField(dynamic, "loot_table", "LootTable"), "loot_table_seed", "LootTableSeed");
    }

    private Dynamic<?> renameField(Dynamic<?> dynamic, String s, String s1) {
        Optional<? extends Dynamic<?>> optional = dynamic.get(s).result();
        Optional<? extends Dynamic<?>> optional1 = optional.map((dynamic1) -> {
            return dynamic.remove(s).set(s1, dynamic1);
        });

        return (Dynamic) DataFixUtils.orElse(optional1, dynamic);
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixTag);
    }
}
