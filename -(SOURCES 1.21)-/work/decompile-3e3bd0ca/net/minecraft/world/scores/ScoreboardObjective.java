package net.minecraft.world.scores;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;

public class ScoreboardObjective {

    private final Scoreboard scoreboard;
    private final String name;
    private final IScoreboardCriteria criteria;
    public IChatBaseComponent displayName;
    private IChatBaseComponent formattedDisplayName;
    private IScoreboardCriteria.EnumScoreboardHealthDisplay renderType;
    private boolean displayAutoUpdate;
    @Nullable
    private NumberFormat numberFormat;

    public ScoreboardObjective(Scoreboard scoreboard, String s, IScoreboardCriteria iscoreboardcriteria, IChatBaseComponent ichatbasecomponent, IScoreboardCriteria.EnumScoreboardHealthDisplay iscoreboardcriteria_enumscoreboardhealthdisplay, boolean flag, @Nullable NumberFormat numberformat) {
        this.scoreboard = scoreboard;
        this.name = s;
        this.criteria = iscoreboardcriteria;
        this.displayName = ichatbasecomponent;
        this.formattedDisplayName = this.createFormattedDisplayName();
        this.renderType = iscoreboardcriteria_enumscoreboardhealthdisplay;
        this.displayAutoUpdate = flag;
        this.numberFormat = numberformat;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public String getName() {
        return this.name;
    }

    public IScoreboardCriteria getCriteria() {
        return this.criteria;
    }

    public IChatBaseComponent getDisplayName() {
        return this.displayName;
    }

    public boolean displayAutoUpdate() {
        return this.displayAutoUpdate;
    }

    @Nullable
    public NumberFormat numberFormat() {
        return this.numberFormat;
    }

    public NumberFormat numberFormatOrDefault(NumberFormat numberformat) {
        return (NumberFormat) Objects.requireNonNullElse(this.numberFormat, numberformat);
    }

    private IChatBaseComponent createFormattedDisplayName() {
        return ChatComponentUtils.wrapInSquareBrackets(this.displayName.copy().withStyle((chatmodifier) -> {
            return chatmodifier.withHoverEvent(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, IChatBaseComponent.literal(this.name)));
        }));
    }

    public IChatBaseComponent getFormattedDisplayName() {
        return this.formattedDisplayName;
    }

    public void setDisplayName(IChatBaseComponent ichatbasecomponent) {
        this.displayName = ichatbasecomponent;
        this.formattedDisplayName = this.createFormattedDisplayName();
        this.scoreboard.onObjectiveChanged(this);
    }

    public IScoreboardCriteria.EnumScoreboardHealthDisplay getRenderType() {
        return this.renderType;
    }

    public void setRenderType(IScoreboardCriteria.EnumScoreboardHealthDisplay iscoreboardcriteria_enumscoreboardhealthdisplay) {
        this.renderType = iscoreboardcriteria_enumscoreboardhealthdisplay;
        this.scoreboard.onObjectiveChanged(this);
    }

    public void setDisplayAutoUpdate(boolean flag) {
        this.displayAutoUpdate = flag;
        this.scoreboard.onObjectiveChanged(this);
    }

    public void setNumberFormat(@Nullable NumberFormat numberformat) {
        this.numberFormat = numberformat;
        this.scoreboard.onObjectiveChanged(this);
    }
}
