package net.minecraft.world.scores;

import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;

public class ScoreboardScore implements ReadOnlyScoreInfo {

    private static final String TAG_SCORE = "Score";
    private static final String TAG_LOCKED = "Locked";
    private static final String TAG_DISPLAY = "display";
    private static final String TAG_FORMAT = "format";
    private int value;
    private boolean locked = true;
    @Nullable
    private IChatBaseComponent display;
    @Nullable
    private NumberFormat numberFormat;

    public ScoreboardScore() {}

    @Override
    public int value() {
        return this.value;
    }

    public void value(int i) {
        this.value = i;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean flag) {
        this.locked = flag;
    }

    @Nullable
    public IChatBaseComponent display() {
        return this.display;
    }

    public void display(@Nullable IChatBaseComponent ichatbasecomponent) {
        this.display = ichatbasecomponent;
    }

    @Nullable
    @Override
    public NumberFormat numberFormat() {
        return this.numberFormat;
    }

    public void numberFormat(@Nullable NumberFormat numberformat) {
        this.numberFormat = numberformat;
    }

    public NBTTagCompound write(HolderLookup.a holderlookup_a) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.putInt("Score", this.value);
        nbttagcompound.putBoolean("Locked", this.locked);
        if (this.display != null) {
            nbttagcompound.putString("display", IChatBaseComponent.ChatSerializer.toJson(this.display, holderlookup_a));
        }

        if (this.numberFormat != null) {
            NumberFormatTypes.CODEC.encodeStart(holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), this.numberFormat).ifSuccess((nbtbase) -> {
                nbttagcompound.put("format", nbtbase);
            });
        }

        return nbttagcompound;
    }

    public static ScoreboardScore read(NBTTagCompound nbttagcompound, HolderLookup.a holderlookup_a) {
        ScoreboardScore scoreboardscore = new ScoreboardScore();

        scoreboardscore.value = nbttagcompound.getInt("Score");
        scoreboardscore.locked = nbttagcompound.getBoolean("Locked");
        if (nbttagcompound.contains("display", 8)) {
            scoreboardscore.display = IChatBaseComponent.ChatSerializer.fromJson(nbttagcompound.getString("display"), holderlookup_a);
        }

        if (nbttagcompound.contains("format", 10)) {
            NumberFormatTypes.CODEC.parse(holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), nbttagcompound.get("format")).ifSuccess((numberformat) -> {
                scoreboardscore.numberFormat = numberformat;
            });
        }

        return scoreboardscore;
    }
}
