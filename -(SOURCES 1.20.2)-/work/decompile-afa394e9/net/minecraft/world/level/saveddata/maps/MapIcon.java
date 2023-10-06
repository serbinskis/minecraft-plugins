package net.minecraft.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.INamable;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.material.MaterialMapColor;

public record MapIcon(MapIcon.Type type, byte x, byte y, byte rot, @Nullable IChatBaseComponent name) {

    public byte getImage() {
        return this.type.getIcon();
    }

    public boolean renderOnFrame() {
        return this.type.isRenderedOnFrame();
    }

    public static enum Type implements INamable {

        PLAYER("player", false, true), FRAME("frame", true, true), RED_MARKER("red_marker", false, true), BLUE_MARKER("blue_marker", false, true), TARGET_X("target_x", true, false), TARGET_POINT("target_point", true, false), PLAYER_OFF_MAP("player_off_map", false, true), PLAYER_OFF_LIMITS("player_off_limits", false, true), MANSION("mansion", true, 5393476, false, true), MONUMENT("monument", true, 3830373, false, true), BANNER_WHITE("banner_white", true, true), BANNER_ORANGE("banner_orange", true, true), BANNER_MAGENTA("banner_magenta", true, true), BANNER_LIGHT_BLUE("banner_light_blue", true, true), BANNER_YELLOW("banner_yellow", true, true), BANNER_LIME("banner_lime", true, true), BANNER_PINK("banner_pink", true, true), BANNER_GRAY("banner_gray", true, true), BANNER_LIGHT_GRAY("banner_light_gray", true, true), BANNER_CYAN("banner_cyan", true, true), BANNER_PURPLE("banner_purple", true, true), BANNER_BLUE("banner_blue", true, true), BANNER_BROWN("banner_brown", true, true), BANNER_GREEN("banner_green", true, true), BANNER_RED("banner_red", true, true), BANNER_BLACK("banner_black", true, true), RED_X("red_x", true, false), DESERT_VILLAGE("village_desert", true, MaterialMapColor.COLOR_LIGHT_GRAY.col, false, true), PLAINS_VILLAGE("village_plains", true, MaterialMapColor.COLOR_LIGHT_GRAY.col, false, true), SAVANNA_VILLAGE("village_savanna", true, MaterialMapColor.COLOR_LIGHT_GRAY.col, false, true), SNOWY_VILLAGE("village_snowy", true, MaterialMapColor.COLOR_LIGHT_GRAY.col, false, true), TAIGA_VILLAGE("village_taiga", true, MaterialMapColor.COLOR_LIGHT_GRAY.col, false, true), JUNGLE_TEMPLE("jungle_temple", true, MaterialMapColor.COLOR_LIGHT_GRAY.col, false, true), SWAMP_HUT("swamp_hut", true, MaterialMapColor.COLOR_LIGHT_GRAY.col, false, true);

        public static final Codec<MapIcon.Type> CODEC = INamable.fromEnum(MapIcon.Type::values);
        private final String name;
        private final byte icon;
        private final boolean renderedOnFrame;
        private final int mapColor;
        private final boolean isExplorationMapElement;
        private final boolean trackCount;

        private Type(String s, boolean flag, boolean flag1) {
            this(s, flag, -1, flag1, false);
        }

        private Type(String s, boolean flag, int i, boolean flag1, boolean flag2) {
            this.name = s;
            this.trackCount = flag1;
            this.icon = (byte) this.ordinal();
            this.renderedOnFrame = flag;
            this.mapColor = i;
            this.isExplorationMapElement = flag2;
        }

        public byte getIcon() {
            return this.icon;
        }

        public boolean isExplorationMapElement() {
            return this.isExplorationMapElement;
        }

        public boolean isRenderedOnFrame() {
            return this.renderedOnFrame;
        }

        public boolean hasMapColor() {
            return this.mapColor >= 0;
        }

        public int getMapColor() {
            return this.mapColor;
        }

        public static MapIcon.Type byIcon(byte b0) {
            return values()[MathHelper.clamp(b0, 0, values().length - 1)];
        }

        public boolean shouldTrackCount() {
            return this.trackCount;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
