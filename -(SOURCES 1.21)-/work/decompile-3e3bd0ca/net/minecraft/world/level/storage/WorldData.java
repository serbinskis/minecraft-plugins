package net.minecraft.world.level.storage;

import java.util.Locale;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelHeightAccessor;

public interface WorldData {

    BlockPosition getSpawnPos();

    float getSpawnAngle();

    long getGameTime();

    long getDayTime();

    boolean isThundering();

    boolean isRaining();

    void setRaining(boolean flag);

    boolean isHardcore();

    GameRules getGameRules();

    EnumDifficulty getDifficulty();

    boolean isDifficultyLocked();

    default void fillCrashReportCategory(CrashReportSystemDetails crashreportsystemdetails, LevelHeightAccessor levelheightaccessor) {
        crashreportsystemdetails.setDetail("Level spawn location", () -> {
            return CrashReportSystemDetails.formatLocation(levelheightaccessor, this.getSpawnPos());
        });
        crashreportsystemdetails.setDetail("Level time", () -> {
            return String.format(Locale.ROOT, "%d game time, %d day time", this.getGameTime(), this.getDayTime());
        });
    }
}
