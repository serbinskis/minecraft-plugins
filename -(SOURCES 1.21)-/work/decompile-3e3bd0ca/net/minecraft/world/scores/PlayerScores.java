package net.minecraft.world.scores;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;

class PlayerScores {

    private final Reference2ObjectOpenHashMap<ScoreboardObjective, ScoreboardScore> scores = new Reference2ObjectOpenHashMap(16, 0.5F);

    PlayerScores() {}

    @Nullable
    public ScoreboardScore get(ScoreboardObjective scoreboardobjective) {
        return (ScoreboardScore) this.scores.get(scoreboardobjective);
    }

    public ScoreboardScore getOrCreate(ScoreboardObjective scoreboardobjective, Consumer<ScoreboardScore> consumer) {
        return (ScoreboardScore) this.scores.computeIfAbsent(scoreboardobjective, (object) -> {
            ScoreboardScore scoreboardscore = new ScoreboardScore();

            consumer.accept(scoreboardscore);
            return scoreboardscore;
        });
    }

    public boolean remove(ScoreboardObjective scoreboardobjective) {
        return this.scores.remove(scoreboardobjective) != null;
    }

    public boolean hasScores() {
        return !this.scores.isEmpty();
    }

    public Object2IntMap<ScoreboardObjective> listScores() {
        Object2IntMap<ScoreboardObjective> object2intmap = new Object2IntOpenHashMap();

        this.scores.forEach((scoreboardobjective, scoreboardscore) -> {
            object2intmap.put(scoreboardobjective, scoreboardscore.value());
        });
        return object2intmap;
    }

    void setScore(ScoreboardObjective scoreboardobjective, ScoreboardScore scoreboardscore) {
        this.scores.put(scoreboardobjective, scoreboardscore);
    }

    Map<ScoreboardObjective, ScoreboardScore> listRawScores() {
        return Collections.unmodifiableMap(this.scores);
    }
}
