package net.minecraft.world.scores;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public class Scoreboard {

    public static final String HIDDEN_SCORE_PREFIX = "#";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Object2ObjectMap<String, ScoreboardObjective> objectivesByName = new Object2ObjectOpenHashMap(16, 0.5F);
    private final Reference2ObjectMap<IScoreboardCriteria, List<ScoreboardObjective>> objectivesByCriteria = new Reference2ObjectOpenHashMap();
    private final Map<String, PlayerScores> playerScores = new Object2ObjectOpenHashMap(16, 0.5F);
    private final Map<DisplaySlot, ScoreboardObjective> displayObjectives = new EnumMap(DisplaySlot.class);
    private final Object2ObjectMap<String, ScoreboardTeam> teamsByName = new Object2ObjectOpenHashMap();
    private final Object2ObjectMap<String, ScoreboardTeam> teamsByPlayer = new Object2ObjectOpenHashMap();

    public Scoreboard() {}

    @Nullable
    public ScoreboardObjective getObjective(@Nullable String s) {
        return (ScoreboardObjective) this.objectivesByName.get(s);
    }

    public ScoreboardObjective addObjective(String s, IScoreboardCriteria iscoreboardcriteria, IChatBaseComponent ichatbasecomponent, IScoreboardCriteria.EnumScoreboardHealthDisplay iscoreboardcriteria_enumscoreboardhealthdisplay, boolean flag, @Nullable NumberFormat numberformat) {
        if (this.objectivesByName.containsKey(s)) {
            throw new IllegalArgumentException("An objective with the name '" + s + "' already exists!");
        } else {
            ScoreboardObjective scoreboardobjective = new ScoreboardObjective(this, s, iscoreboardcriteria, ichatbasecomponent, iscoreboardcriteria_enumscoreboardhealthdisplay, flag, numberformat);

            ((List) this.objectivesByCriteria.computeIfAbsent(iscoreboardcriteria, (object) -> {
                return Lists.newArrayList();
            })).add(scoreboardobjective);
            this.objectivesByName.put(s, scoreboardobjective);
            this.onObjectiveAdded(scoreboardobjective);
            return scoreboardobjective;
        }
    }

    public final void forAllObjectives(IScoreboardCriteria iscoreboardcriteria, ScoreHolder scoreholder, Consumer<ScoreAccess> consumer) {
        ((List) this.objectivesByCriteria.getOrDefault(iscoreboardcriteria, Collections.emptyList())).forEach((scoreboardobjective) -> {
            consumer.accept(this.getOrCreatePlayerScore(scoreholder, scoreboardobjective, true));
        });
    }

    private PlayerScores getOrCreatePlayerInfo(String s) {
        return (PlayerScores) this.playerScores.computeIfAbsent(s, (s1) -> {
            return new PlayerScores();
        });
    }

    public ScoreAccess getOrCreatePlayerScore(ScoreHolder scoreholder, ScoreboardObjective scoreboardobjective) {
        return this.getOrCreatePlayerScore(scoreholder, scoreboardobjective, false);
    }

    public ScoreAccess getOrCreatePlayerScore(final ScoreHolder scoreholder, final ScoreboardObjective scoreboardobjective, boolean flag) {
        final boolean flag1 = flag || !scoreboardobjective.getCriteria().isReadOnly();
        PlayerScores playerscores = this.getOrCreatePlayerInfo(scoreholder.getScoreboardName());
        final MutableBoolean mutableboolean = new MutableBoolean();
        final ScoreboardScore scoreboardscore = playerscores.getOrCreate(scoreboardobjective, (scoreboardscore1) -> {
            mutableboolean.setTrue();
        });

        return new ScoreAccess() {
            @Override
            public int get() {
                return scoreboardscore.value();
            }

            @Override
            public void set(int i) {
                if (!flag1) {
                    throw new IllegalStateException("Cannot modify read-only score");
                } else {
                    boolean flag2 = mutableboolean.isTrue();

                    if (scoreboardobjective.displayAutoUpdate()) {
                        IChatBaseComponent ichatbasecomponent = scoreholder.getDisplayName();

                        if (ichatbasecomponent != null && !ichatbasecomponent.equals(scoreboardscore.display())) {
                            scoreboardscore.display(ichatbasecomponent);
                            flag2 = true;
                        }
                    }

                    if (i != scoreboardscore.value()) {
                        scoreboardscore.value(i);
                        flag2 = true;
                    }

                    if (flag2) {
                        this.sendScoreToPlayers();
                    }

                }
            }

            @Nullable
            @Override
            public IChatBaseComponent display() {
                return scoreboardscore.display();
            }

            @Override
            public void display(@Nullable IChatBaseComponent ichatbasecomponent) {
                if (mutableboolean.isTrue() || !Objects.equals(ichatbasecomponent, scoreboardscore.display())) {
                    scoreboardscore.display(ichatbasecomponent);
                    this.sendScoreToPlayers();
                }

            }

            @Override
            public void numberFormatOverride(@Nullable NumberFormat numberformat) {
                scoreboardscore.numberFormat(numberformat);
                this.sendScoreToPlayers();
            }

            @Override
            public boolean locked() {
                return scoreboardscore.isLocked();
            }

            @Override
            public void unlock() {
                this.setLocked(false);
            }

            @Override
            public void lock() {
                this.setLocked(true);
            }

            private void setLocked(boolean flag2) {
                scoreboardscore.setLocked(flag2);
                if (mutableboolean.isTrue()) {
                    this.sendScoreToPlayers();
                }

                Scoreboard.this.onScoreLockChanged(scoreholder, scoreboardobjective);
            }

            private void sendScoreToPlayers() {
                Scoreboard.this.onScoreChanged(scoreholder, scoreboardobjective, scoreboardscore);
                mutableboolean.setFalse();
            }
        };
    }

    @Nullable
    public ReadOnlyScoreInfo getPlayerScoreInfo(ScoreHolder scoreholder, ScoreboardObjective scoreboardobjective) {
        PlayerScores playerscores = (PlayerScores) this.playerScores.get(scoreholder.getScoreboardName());

        return playerscores != null ? playerscores.get(scoreboardobjective) : null;
    }

    public Collection<PlayerScoreEntry> listPlayerScores(ScoreboardObjective scoreboardobjective) {
        List<PlayerScoreEntry> list = new ArrayList();

        this.playerScores.forEach((s, playerscores) -> {
            ScoreboardScore scoreboardscore = playerscores.get(scoreboardobjective);

            if (scoreboardscore != null) {
                list.add(new PlayerScoreEntry(s, scoreboardscore.value(), scoreboardscore.display(), scoreboardscore.numberFormat()));
            }

        });
        return list;
    }

    public Collection<ScoreboardObjective> getObjectives() {
        return this.objectivesByName.values();
    }

    public Collection<String> getObjectiveNames() {
        return this.objectivesByName.keySet();
    }

    public Collection<ScoreHolder> getTrackedPlayers() {
        return this.playerScores.keySet().stream().map(ScoreHolder::forNameOnly).toList();
    }

    public void resetAllPlayerScores(ScoreHolder scoreholder) {
        PlayerScores playerscores = (PlayerScores) this.playerScores.remove(scoreholder.getScoreboardName());

        if (playerscores != null) {
            this.onPlayerRemoved(scoreholder);
        }

    }

    public void resetSinglePlayerScore(ScoreHolder scoreholder, ScoreboardObjective scoreboardobjective) {
        PlayerScores playerscores = (PlayerScores) this.playerScores.get(scoreholder.getScoreboardName());

        if (playerscores != null) {
            boolean flag = playerscores.remove(scoreboardobjective);

            if (!playerscores.hasScores()) {
                PlayerScores playerscores1 = (PlayerScores) this.playerScores.remove(scoreholder.getScoreboardName());

                if (playerscores1 != null) {
                    this.onPlayerRemoved(scoreholder);
                }
            } else if (flag) {
                this.onPlayerScoreRemoved(scoreholder, scoreboardobjective);
            }
        }

    }

    public Object2IntMap<ScoreboardObjective> listPlayerScores(ScoreHolder scoreholder) {
        PlayerScores playerscores = (PlayerScores) this.playerScores.get(scoreholder.getScoreboardName());

        return playerscores != null ? playerscores.listScores() : Object2IntMaps.emptyMap();
    }

    public void removeObjective(ScoreboardObjective scoreboardobjective) {
        this.objectivesByName.remove(scoreboardobjective.getName());
        DisplaySlot[] adisplayslot = DisplaySlot.values();
        int i = adisplayslot.length;

        for (int j = 0; j < i; ++j) {
            DisplaySlot displayslot = adisplayslot[j];

            if (this.getDisplayObjective(displayslot) == scoreboardobjective) {
                this.setDisplayObjective(displayslot, (ScoreboardObjective) null);
            }
        }

        List<ScoreboardObjective> list = (List) this.objectivesByCriteria.get(scoreboardobjective.getCriteria());

        if (list != null) {
            list.remove(scoreboardobjective);
        }

        Iterator iterator = this.playerScores.values().iterator();

        while (iterator.hasNext()) {
            PlayerScores playerscores = (PlayerScores) iterator.next();

            playerscores.remove(scoreboardobjective);
        }

        this.onObjectiveRemoved(scoreboardobjective);
    }

    public void setDisplayObjective(DisplaySlot displayslot, @Nullable ScoreboardObjective scoreboardobjective) {
        this.displayObjectives.put(displayslot, scoreboardobjective);
    }

    @Nullable
    public ScoreboardObjective getDisplayObjective(DisplaySlot displayslot) {
        return (ScoreboardObjective) this.displayObjectives.get(displayslot);
    }

    @Nullable
    public ScoreboardTeam getPlayerTeam(String s) {
        return (ScoreboardTeam) this.teamsByName.get(s);
    }

    public ScoreboardTeam addPlayerTeam(String s) {
        ScoreboardTeam scoreboardteam = this.getPlayerTeam(s);

        if (scoreboardteam != null) {
            Scoreboard.LOGGER.warn("Requested creation of existing team '{}'", s);
            return scoreboardteam;
        } else {
            scoreboardteam = new ScoreboardTeam(this, s);
            this.teamsByName.put(s, scoreboardteam);
            this.onTeamAdded(scoreboardteam);
            return scoreboardteam;
        }
    }

    public void removePlayerTeam(ScoreboardTeam scoreboardteam) {
        this.teamsByName.remove(scoreboardteam.getName());
        Iterator iterator = scoreboardteam.getPlayers().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            this.teamsByPlayer.remove(s);
        }

        this.onTeamRemoved(scoreboardteam);
    }

    public boolean addPlayerToTeam(String s, ScoreboardTeam scoreboardteam) {
        if (this.getPlayersTeam(s) != null) {
            this.removePlayerFromTeam(s);
        }

        this.teamsByPlayer.put(s, scoreboardteam);
        return scoreboardteam.getPlayers().add(s);
    }

    public boolean removePlayerFromTeam(String s) {
        ScoreboardTeam scoreboardteam = this.getPlayersTeam(s);

        if (scoreboardteam != null) {
            this.removePlayerFromTeam(s, scoreboardteam);
            return true;
        } else {
            return false;
        }
    }

    public void removePlayerFromTeam(String s, ScoreboardTeam scoreboardteam) {
        if (this.getPlayersTeam(s) != scoreboardteam) {
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + scoreboardteam.getName() + "'.");
        } else {
            this.teamsByPlayer.remove(s);
            scoreboardteam.getPlayers().remove(s);
        }
    }

    public Collection<String> getTeamNames() {
        return this.teamsByName.keySet();
    }

    public Collection<ScoreboardTeam> getPlayerTeams() {
        return this.teamsByName.values();
    }

    @Nullable
    public ScoreboardTeam getPlayersTeam(String s) {
        return (ScoreboardTeam) this.teamsByPlayer.get(s);
    }

    public void onObjectiveAdded(ScoreboardObjective scoreboardobjective) {}

    public void onObjectiveChanged(ScoreboardObjective scoreboardobjective) {}

    public void onObjectiveRemoved(ScoreboardObjective scoreboardobjective) {}

    protected void onScoreChanged(ScoreHolder scoreholder, ScoreboardObjective scoreboardobjective, ScoreboardScore scoreboardscore) {}

    protected void onScoreLockChanged(ScoreHolder scoreholder, ScoreboardObjective scoreboardobjective) {}

    public void onPlayerRemoved(ScoreHolder scoreholder) {}

    public void onPlayerScoreRemoved(ScoreHolder scoreholder, ScoreboardObjective scoreboardobjective) {}

    public void onTeamAdded(ScoreboardTeam scoreboardteam) {}

    public void onTeamChanged(ScoreboardTeam scoreboardteam) {}

    public void onTeamRemoved(ScoreboardTeam scoreboardteam) {}

    public void entityRemoved(Entity entity) {
        if (!(entity instanceof EntityHuman) && !entity.isAlive()) {
            this.resetAllPlayerScores(entity);
            this.removePlayerFromTeam(entity.getScoreboardName());
        }
    }

    protected NBTTagList savePlayerScores(HolderLookup.a holderlookup_a) {
        NBTTagList nbttaglist = new NBTTagList();

        this.playerScores.forEach((s, playerscores) -> {
            playerscores.listRawScores().forEach((scoreboardobjective, scoreboardscore) -> {
                NBTTagCompound nbttagcompound = scoreboardscore.write(holderlookup_a);

                nbttagcompound.putString("Name", s);
                nbttagcompound.putString("Objective", scoreboardobjective.getName());
                nbttaglist.add(nbttagcompound);
            });
        });
        return nbttaglist;
    }

    protected void loadPlayerScores(NBTTagList nbttaglist, HolderLookup.a holderlookup_a) {
        for (int i = 0; i < nbttaglist.size(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompound(i);
            ScoreboardScore scoreboardscore = ScoreboardScore.read(nbttagcompound, holderlookup_a);
            String s = nbttagcompound.getString("Name");
            String s1 = nbttagcompound.getString("Objective");
            ScoreboardObjective scoreboardobjective = this.getObjective(s1);

            if (scoreboardobjective == null) {
                Scoreboard.LOGGER.error("Unknown objective {} for name {}, ignoring", s1, s);
            } else {
                this.getOrCreatePlayerInfo(s).setScore(scoreboardobjective, scoreboardscore);
            }
        }

    }
}
