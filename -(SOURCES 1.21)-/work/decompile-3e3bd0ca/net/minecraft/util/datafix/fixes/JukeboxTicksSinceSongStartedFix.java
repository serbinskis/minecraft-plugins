package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class JukeboxTicksSinceSongStartedFix extends DataConverterNamedEntity {

    public JukeboxTicksSinceSongStartedFix(Schema schema) {
        super(schema, false, "JukeboxTicksSinceSongStartedFix", DataConverterTypes.BLOCK_ENTITY, "minecraft:jukebox");
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic) {
        long i = dynamic.get("TickCount").asLong(0L) - dynamic.get("RecordStartTick").asLong(0L);
        Dynamic<?> dynamic1 = dynamic.remove("IsPlaying").remove("TickCount").remove("RecordStartTick");

        return i > 0L ? dynamic1.set("ticks_since_song_started", dynamic.createLong(i)) : dynamic1;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixTag);
    }
}
