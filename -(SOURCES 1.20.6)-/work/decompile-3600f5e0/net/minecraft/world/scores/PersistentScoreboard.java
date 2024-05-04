package net.minecraft.world.scores;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.world.level.saveddata.PersistentBase;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;
import org.slf4j.Logger;

public class PersistentScoreboard extends PersistentBase {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String FILE_ID = "scoreboard";
    private final Scoreboard scoreboard;

    public PersistentScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public PersistentScoreboard load(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        this.loadObjectives(nbttagcompound.getList("Objectives", 10), holderlookup_a);
        this.scoreboard.loadPlayerScores(nbttagcompound.getList("PlayerScores", 10), holderlookup_a);
        if (nbttagcompound.contains("DisplaySlots", 10)) {
            this.loadDisplaySlots(nbttagcompound.getCompound("DisplaySlots"));
        }

        if (nbttagcompound.contains("Teams", 9)) {
            this.loadTeams(nbttagcompound.getList("Teams", 10), holderlookup_a);
        }

        return this;
    }

    private void loadTeams(NBTTagList nbttaglist, HolderLookup.a holderlookup_a) {
        for (int i = 0; i < nbttaglist.size(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompound(i);
            String s = nbttagcompound.getString("Name");
            ScoreboardTeam scoreboardteam = this.scoreboard.addPlayerTeam(s);
            IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.ChatSerializer.fromJson(nbttagcompound.getString("DisplayName"), holderlookup_a);

            if (ichatmutablecomponent != null) {
                scoreboardteam.setDisplayName(ichatmutablecomponent);
            }

            if (nbttagcompound.contains("TeamColor", 8)) {
                scoreboardteam.setColor(EnumChatFormat.getByName(nbttagcompound.getString("TeamColor")));
            }

            if (nbttagcompound.contains("AllowFriendlyFire", 99)) {
                scoreboardteam.setAllowFriendlyFire(nbttagcompound.getBoolean("AllowFriendlyFire"));
            }

            if (nbttagcompound.contains("SeeFriendlyInvisibles", 99)) {
                scoreboardteam.setSeeFriendlyInvisibles(nbttagcompound.getBoolean("SeeFriendlyInvisibles"));
            }

            IChatMutableComponent ichatmutablecomponent1;

            if (nbttagcompound.contains("MemberNamePrefix", 8)) {
                ichatmutablecomponent1 = IChatBaseComponent.ChatSerializer.fromJson(nbttagcompound.getString("MemberNamePrefix"), holderlookup_a);
                if (ichatmutablecomponent1 != null) {
                    scoreboardteam.setPlayerPrefix(ichatmutablecomponent1);
                }
            }

            if (nbttagcompound.contains("MemberNameSuffix", 8)) {
                ichatmutablecomponent1 = IChatBaseComponent.ChatSerializer.fromJson(nbttagcompound.getString("MemberNameSuffix"), holderlookup_a);
                if (ichatmutablecomponent1 != null) {
                    scoreboardteam.setPlayerSuffix(ichatmutablecomponent1);
                }
            }

            ScoreboardTeamBase.EnumNameTagVisibility scoreboardteambase_enumnametagvisibility;

            if (nbttagcompound.contains("NameTagVisibility", 8)) {
                scoreboardteambase_enumnametagvisibility = ScoreboardTeamBase.EnumNameTagVisibility.byName(nbttagcompound.getString("NameTagVisibility"));
                if (scoreboardteambase_enumnametagvisibility != null) {
                    scoreboardteam.setNameTagVisibility(scoreboardteambase_enumnametagvisibility);
                }
            }

            if (nbttagcompound.contains("DeathMessageVisibility", 8)) {
                scoreboardteambase_enumnametagvisibility = ScoreboardTeamBase.EnumNameTagVisibility.byName(nbttagcompound.getString("DeathMessageVisibility"));
                if (scoreboardteambase_enumnametagvisibility != null) {
                    scoreboardteam.setDeathMessageVisibility(scoreboardteambase_enumnametagvisibility);
                }
            }

            if (nbttagcompound.contains("CollisionRule", 8)) {
                ScoreboardTeamBase.EnumTeamPush scoreboardteambase_enumteampush = ScoreboardTeamBase.EnumTeamPush.byName(nbttagcompound.getString("CollisionRule"));

                if (scoreboardteambase_enumteampush != null) {
                    scoreboardteam.setCollisionRule(scoreboardteambase_enumteampush);
                }
            }

            this.loadTeamPlayers(scoreboardteam, nbttagcompound.getList("Players", 8));
        }

    }

    private void loadTeamPlayers(ScoreboardTeam scoreboardteam, NBTTagList nbttaglist) {
        for (int i = 0; i < nbttaglist.size(); ++i) {
            this.scoreboard.addPlayerToTeam(nbttaglist.getString(i), scoreboardteam);
        }

    }

    private void loadDisplaySlots(NBTTagCompound nbttagcompound) {
        Iterator iterator = nbttagcompound.getAllKeys().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();
            DisplaySlot displayslot = (DisplaySlot) DisplaySlot.CODEC.byName(s);

            if (displayslot != null) {
                String s1 = nbttagcompound.getString(s);
                ScoreboardObjective scoreboardobjective = this.scoreboard.getObjective(s1);

                this.scoreboard.setDisplayObjective(displayslot, scoreboardobjective);
            }
        }

    }

    private void loadObjectives(NBTTagList nbttaglist, HolderLookup.a holderlookup_a) {
        for (int i = 0; i < nbttaglist.size(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompound(i);
            String s = nbttagcompound.getString("CriteriaName");
            IScoreboardCriteria iscoreboardcriteria = (IScoreboardCriteria) IScoreboardCriteria.byName(s).orElseGet(() -> {
                PersistentScoreboard.LOGGER.warn("Unknown scoreboard criteria {}, replacing with {}", s, IScoreboardCriteria.DUMMY.getName());
                return IScoreboardCriteria.DUMMY;
            });
            String s1 = nbttagcompound.getString("Name");
            IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.ChatSerializer.fromJson(nbttagcompound.getString("DisplayName"), holderlookup_a);
            IScoreboardCriteria.EnumScoreboardHealthDisplay iscoreboardcriteria_enumscoreboardhealthdisplay = IScoreboardCriteria.EnumScoreboardHealthDisplay.byId(nbttagcompound.getString("RenderType"));
            boolean flag = nbttagcompound.getBoolean("display_auto_update");
            NumberFormat numberformat = (NumberFormat) NumberFormatTypes.CODEC.parse(holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), nbttagcompound.get("format")).result().orElse((Object) null);

            this.scoreboard.addObjective(s1, iscoreboardcriteria, ichatmutablecomponent, iscoreboardcriteria_enumscoreboardhealthdisplay, flag, numberformat);
        }

    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        nbttagcompound.put("Objectives", this.saveObjectives(holderlookup_a));
        nbttagcompound.put("PlayerScores", this.scoreboard.savePlayerScores(holderlookup_a));
        nbttagcompound.put("Teams", this.saveTeams(holderlookup_a));
        this.saveDisplaySlots(nbttagcompound);
        return nbttagcompound;
    }

    private NBTTagList saveTeams(HolderLookup.a holderlookup_a) {
        NBTTagList nbttaglist = new NBTTagList();
        Collection<ScoreboardTeam> collection = this.scoreboard.getPlayerTeams();
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            ScoreboardTeam scoreboardteam = (ScoreboardTeam) iterator.next();
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            nbttagcompound.putString("Name", scoreboardteam.getName());
            nbttagcompound.putString("DisplayName", IChatBaseComponent.ChatSerializer.toJson(scoreboardteam.getDisplayName(), holderlookup_a));
            if (scoreboardteam.getColor().getId() >= 0) {
                nbttagcompound.putString("TeamColor", scoreboardteam.getColor().getName());
            }

            nbttagcompound.putBoolean("AllowFriendlyFire", scoreboardteam.isAllowFriendlyFire());
            nbttagcompound.putBoolean("SeeFriendlyInvisibles", scoreboardteam.canSeeFriendlyInvisibles());
            nbttagcompound.putString("MemberNamePrefix", IChatBaseComponent.ChatSerializer.toJson(scoreboardteam.getPlayerPrefix(), holderlookup_a));
            nbttagcompound.putString("MemberNameSuffix", IChatBaseComponent.ChatSerializer.toJson(scoreboardteam.getPlayerSuffix(), holderlookup_a));
            nbttagcompound.putString("NameTagVisibility", scoreboardteam.getNameTagVisibility().name);
            nbttagcompound.putString("DeathMessageVisibility", scoreboardteam.getDeathMessageVisibility().name);
            nbttagcompound.putString("CollisionRule", scoreboardteam.getCollisionRule().name);
            NBTTagList nbttaglist1 = new NBTTagList();
            Iterator iterator1 = scoreboardteam.getPlayers().iterator();

            while (iterator1.hasNext()) {
                String s = (String) iterator1.next();

                nbttaglist1.add(NBTTagString.valueOf(s));
            }

            nbttagcompound.put("Players", nbttaglist1);
            nbttaglist.add(nbttagcompound);
        }

        return nbttaglist;
    }

    private void saveDisplaySlots(NBTTagCompound nbttagcompound) {
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        DisplaySlot[] adisplayslot = DisplaySlot.values();
        int i = adisplayslot.length;

        for (int j = 0; j < i; ++j) {
            DisplaySlot displayslot = adisplayslot[j];
            ScoreboardObjective scoreboardobjective = this.scoreboard.getDisplayObjective(displayslot);

            if (scoreboardobjective != null) {
                nbttagcompound1.putString(displayslot.getSerializedName(), scoreboardobjective.getName());
            }
        }

        if (!nbttagcompound1.isEmpty()) {
            nbttagcompound.put("DisplaySlots", nbttagcompound1);
        }

    }

    private NBTTagList saveObjectives(HolderLookup.a holderlookup_a) {
        NBTTagList nbttaglist = new NBTTagList();
        Collection<ScoreboardObjective> collection = this.scoreboard.getObjectives();

        NBTTagCompound nbttagcompound;

        for (Iterator iterator = collection.iterator(); iterator.hasNext(); nbttaglist.add(nbttagcompound)) {
            ScoreboardObjective scoreboardobjective = (ScoreboardObjective) iterator.next();

            nbttagcompound = new NBTTagCompound();
            nbttagcompound.putString("Name", scoreboardobjective.getName());
            nbttagcompound.putString("CriteriaName", scoreboardobjective.getCriteria().getName());
            nbttagcompound.putString("DisplayName", IChatBaseComponent.ChatSerializer.toJson(scoreboardobjective.getDisplayName(), holderlookup_a));
            nbttagcompound.putString("RenderType", scoreboardobjective.getRenderType().getId());
            nbttagcompound.putBoolean("display_auto_update", scoreboardobjective.displayAutoUpdate());
            NumberFormat numberformat = scoreboardobjective.numberFormat();

            if (numberformat != null) {
                NumberFormatTypes.CODEC.encodeStart(holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), numberformat).ifSuccess((nbtbase) -> {
                    nbttagcompound.put("format", nbtbase);
                });
            }
        }

        return nbttaglist;
    }
}
