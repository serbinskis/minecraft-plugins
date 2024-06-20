package net.minecraft.world.scores;

import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;

public enum DisplaySlot implements INamable {

    LIST(0, "list"), SIDEBAR(1, "sidebar"), BELOW_NAME(2, "below_name"), TEAM_BLACK(3, "sidebar.team.black"), TEAM_DARK_BLUE(4, "sidebar.team.dark_blue"), TEAM_DARK_GREEN(5, "sidebar.team.dark_green"), TEAM_DARK_AQUA(6, "sidebar.team.dark_aqua"), TEAM_DARK_RED(7, "sidebar.team.dark_red"), TEAM_DARK_PURPLE(8, "sidebar.team.dark_purple"), TEAM_GOLD(9, "sidebar.team.gold"), TEAM_GRAY(10, "sidebar.team.gray"), TEAM_DARK_GRAY(11, "sidebar.team.dark_gray"), TEAM_BLUE(12, "sidebar.team.blue"), TEAM_GREEN(13, "sidebar.team.green"), TEAM_AQUA(14, "sidebar.team.aqua"), TEAM_RED(15, "sidebar.team.red"), TEAM_LIGHT_PURPLE(16, "sidebar.team.light_purple"), TEAM_YELLOW(17, "sidebar.team.yellow"), TEAM_WHITE(18, "sidebar.team.white");

    public static final INamable.a<DisplaySlot> CODEC = INamable.fromEnum(DisplaySlot::values);
    public static final IntFunction<DisplaySlot> BY_ID = ByIdMap.continuous(DisplaySlot::id, values(), ByIdMap.a.ZERO);
    private final int id;
    private final String name;

    private DisplaySlot(final int i, final String s) {
        this.id = i;
        this.name = s;
    }

    public int id() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Nullable
    public static DisplaySlot teamColorToSlot(EnumChatFormat enumchatformat) {
        DisplaySlot displayslot;

        switch (enumchatformat) {
            case BLACK:
                displayslot = DisplaySlot.TEAM_BLACK;
                break;
            case DARK_BLUE:
                displayslot = DisplaySlot.TEAM_DARK_BLUE;
                break;
            case DARK_GREEN:
                displayslot = DisplaySlot.TEAM_DARK_GREEN;
                break;
            case DARK_AQUA:
                displayslot = DisplaySlot.TEAM_DARK_AQUA;
                break;
            case DARK_RED:
                displayslot = DisplaySlot.TEAM_DARK_RED;
                break;
            case DARK_PURPLE:
                displayslot = DisplaySlot.TEAM_DARK_PURPLE;
                break;
            case GOLD:
                displayslot = DisplaySlot.TEAM_GOLD;
                break;
            case GRAY:
                displayslot = DisplaySlot.TEAM_GRAY;
                break;
            case DARK_GRAY:
                displayslot = DisplaySlot.TEAM_DARK_GRAY;
                break;
            case BLUE:
                displayslot = DisplaySlot.TEAM_BLUE;
                break;
            case GREEN:
                displayslot = DisplaySlot.TEAM_GREEN;
                break;
            case AQUA:
                displayslot = DisplaySlot.TEAM_AQUA;
                break;
            case RED:
                displayslot = DisplaySlot.TEAM_RED;
                break;
            case LIGHT_PURPLE:
                displayslot = DisplaySlot.TEAM_LIGHT_PURPLE;
                break;
            case YELLOW:
                displayslot = DisplaySlot.TEAM_YELLOW;
                break;
            case WHITE:
                displayslot = DisplaySlot.TEAM_WHITE;
                break;
            case BOLD:
            case ITALIC:
            case UNDERLINE:
            case RESET:
            case OBFUSCATED:
            case STRIKETHROUGH:
                displayslot = null;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return displayslot;
    }
}
