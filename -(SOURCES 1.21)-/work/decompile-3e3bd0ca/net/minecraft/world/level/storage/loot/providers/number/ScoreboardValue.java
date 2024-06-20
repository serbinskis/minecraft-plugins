package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.providers.score.ContextScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProvider;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.ScoreboardObjective;

public record ScoreboardValue(ScoreboardNameProvider target, String score, float scale) implements NumberProvider {

    public static final MapCodec<ScoreboardValue> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ScoreboardNameProviders.CODEC.fieldOf("target").forGetter(ScoreboardValue::target), Codec.STRING.fieldOf("score").forGetter(ScoreboardValue::score), Codec.FLOAT.fieldOf("scale").orElse(1.0F).forGetter(ScoreboardValue::scale)).apply(instance, ScoreboardValue::new);
    });

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.SCORE;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return this.target.getReferencedContextParams();
    }

    public static ScoreboardValue fromScoreboard(LootTableInfo.EntityTarget loottableinfo_entitytarget, String s) {
        return fromScoreboard(loottableinfo_entitytarget, s, 1.0F);
    }

    public static ScoreboardValue fromScoreboard(LootTableInfo.EntityTarget loottableinfo_entitytarget, String s, float f) {
        return new ScoreboardValue(ContextScoreboardNameProvider.forTarget(loottableinfo_entitytarget), s, f);
    }

    @Override
    public float getFloat(LootTableInfo loottableinfo) {
        ScoreHolder scoreholder = this.target.getScoreHolder(loottableinfo);

        if (scoreholder == null) {
            return 0.0F;
        } else {
            ScoreboardServer scoreboardserver = loottableinfo.getLevel().getScoreboard();
            ScoreboardObjective scoreboardobjective = scoreboardserver.getObjective(this.score);

            if (scoreboardobjective == null) {
                return 0.0F;
            } else {
                ReadOnlyScoreInfo readonlyscoreinfo = scoreboardserver.getPlayerScoreInfo(scoreholder, scoreboardobjective);

                return readonlyscoreinfo == null ? 0.0F : (float) readonlyscoreinfo.value() * this.scale;
            }
        }
    }
}
