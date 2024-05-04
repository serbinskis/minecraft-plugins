package net.minecraft.world.level.storage;

import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.util.UtilColor;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.WorldSettings;
import org.apache.commons.lang3.StringUtils;

public class WorldInfo implements Comparable<WorldInfo> {

    public static final IChatBaseComponent PLAY_WORLD = IChatBaseComponent.translatable("selectWorld.select");
    private final WorldSettings settings;
    private final LevelVersion levelVersion;
    private final String levelId;
    private final boolean requiresManualConversion;
    private final boolean locked;
    private final boolean experimental;
    private final Path icon;
    @Nullable
    private IChatBaseComponent info;

    public WorldInfo(WorldSettings worldsettings, LevelVersion levelversion, String s, boolean flag, boolean flag1, boolean flag2, Path path) {
        this.settings = worldsettings;
        this.levelVersion = levelversion;
        this.levelId = s;
        this.locked = flag1;
        this.experimental = flag2;
        this.icon = path;
        this.requiresManualConversion = flag;
    }

    public String getLevelId() {
        return this.levelId;
    }

    public String getLevelName() {
        return StringUtils.isEmpty(this.settings.levelName()) ? this.levelId : this.settings.levelName();
    }

    public Path getIcon() {
        return this.icon;
    }

    public boolean requiresManualConversion() {
        return this.requiresManualConversion;
    }

    public boolean isExperimental() {
        return this.experimental;
    }

    public long getLastPlayed() {
        return this.levelVersion.lastPlayed();
    }

    public int compareTo(WorldInfo worldinfo) {
        return this.getLastPlayed() < worldinfo.getLastPlayed() ? 1 : (this.getLastPlayed() > worldinfo.getLastPlayed() ? -1 : this.levelId.compareTo(worldinfo.levelId));
    }

    public WorldSettings getSettings() {
        return this.settings;
    }

    public EnumGamemode getGameMode() {
        return this.settings.gameType();
    }

    public boolean isHardcore() {
        return this.settings.hardcore();
    }

    public boolean hasCommands() {
        return this.settings.allowCommands();
    }

    public IChatMutableComponent getWorldVersionName() {
        return UtilColor.isNullOrEmpty(this.levelVersion.minecraftVersionName()) ? IChatBaseComponent.translatable("selectWorld.versionUnknown") : IChatBaseComponent.literal(this.levelVersion.minecraftVersionName());
    }

    public LevelVersion levelVersion() {
        return this.levelVersion;
    }

    public boolean shouldBackup() {
        return this.backupStatus().shouldBackup();
    }

    public boolean isDowngrade() {
        return this.backupStatus() == WorldInfo.a.DOWNGRADE;
    }

    public WorldInfo.a backupStatus() {
        WorldVersion worldversion = SharedConstants.getCurrentVersion();
        int i = worldversion.getDataVersion().getVersion();
        int j = this.levelVersion.minecraftVersion().getVersion();

        return !worldversion.isStable() && j < i ? WorldInfo.a.UPGRADE_TO_SNAPSHOT : (j > i ? WorldInfo.a.DOWNGRADE : WorldInfo.a.NONE);
    }

    public boolean isLocked() {
        return this.locked;
    }

    public boolean isDisabled() {
        return !this.isLocked() && !this.requiresManualConversion() ? !this.isCompatible() : true;
    }

    public boolean isCompatible() {
        return SharedConstants.getCurrentVersion().getDataVersion().isCompatible(this.levelVersion.minecraftVersion());
    }

    public IChatBaseComponent getInfo() {
        if (this.info == null) {
            this.info = this.createInfo();
        }

        return this.info;
    }

    private IChatBaseComponent createInfo() {
        if (this.isLocked()) {
            return IChatBaseComponent.translatable("selectWorld.locked").withStyle(EnumChatFormat.RED);
        } else if (this.requiresManualConversion()) {
            return IChatBaseComponent.translatable("selectWorld.conversion").withStyle(EnumChatFormat.RED);
        } else if (!this.isCompatible()) {
            return IChatBaseComponent.translatable("selectWorld.incompatible.info", this.getWorldVersionName()).withStyle(EnumChatFormat.RED);
        } else {
            IChatMutableComponent ichatmutablecomponent = this.isHardcore() ? IChatBaseComponent.empty().append((IChatBaseComponent) IChatBaseComponent.translatable("gameMode.hardcore").withColor(-65536)) : IChatBaseComponent.translatable("gameMode." + this.getGameMode().getName());

            if (this.hasCommands()) {
                ichatmutablecomponent.append(", ").append((IChatBaseComponent) IChatBaseComponent.translatable("selectWorld.commands"));
            }

            if (this.isExperimental()) {
                ichatmutablecomponent.append(", ").append((IChatBaseComponent) IChatBaseComponent.translatable("selectWorld.experimental").withStyle(EnumChatFormat.YELLOW));
            }

            IChatMutableComponent ichatmutablecomponent1 = this.getWorldVersionName();
            IChatMutableComponent ichatmutablecomponent2 = IChatBaseComponent.literal(", ").append((IChatBaseComponent) IChatBaseComponent.translatable("selectWorld.version")).append(CommonComponents.SPACE);

            if (this.shouldBackup()) {
                ichatmutablecomponent2.append((IChatBaseComponent) ichatmutablecomponent1.withStyle(this.isDowngrade() ? EnumChatFormat.RED : EnumChatFormat.ITALIC));
            } else {
                ichatmutablecomponent2.append((IChatBaseComponent) ichatmutablecomponent1);
            }

            ichatmutablecomponent.append((IChatBaseComponent) ichatmutablecomponent2);
            return ichatmutablecomponent;
        }
    }

    public IChatBaseComponent primaryActionMessage() {
        return WorldInfo.PLAY_WORLD;
    }

    public boolean primaryActionActive() {
        return !this.isDisabled();
    }

    public boolean canUpload() {
        return !this.requiresManualConversion() && !this.isLocked();
    }

    public boolean canEdit() {
        return !this.isDisabled();
    }

    public boolean canRecreate() {
        return !this.isDisabled();
    }

    public boolean canDelete() {
        return true;
    }

    public static enum a {

        NONE(false, false, ""), DOWNGRADE(true, true, "downgrade"), UPGRADE_TO_SNAPSHOT(true, false, "snapshot");

        private final boolean shouldBackup;
        private final boolean severe;
        private final String translationKey;

        private a(final boolean flag, final boolean flag1, final String s) {
            this.shouldBackup = flag;
            this.severe = flag1;
            this.translationKey = s;
        }

        public boolean shouldBackup() {
            return this.shouldBackup;
        }

        public boolean isSevere() {
            return this.severe;
        }

        public String getTranslationKey() {
            return this.translationKey;
        }
    }

    public static class b extends WorldInfo {

        private static final IChatBaseComponent INFO = IChatBaseComponent.translatable("recover_world.warning").withStyle((chatmodifier) -> {
            return chatmodifier.withColor(-65536);
        });
        private static final IChatBaseComponent RECOVER = IChatBaseComponent.translatable("recover_world.button");
        private final long lastPlayed;

        public b(String s, Path path, long i) {
            super((WorldSettings) null, (LevelVersion) null, s, false, false, false, path);
            this.lastPlayed = i;
        }

        @Override
        public String getLevelName() {
            return this.getLevelId();
        }

        @Override
        public IChatBaseComponent getInfo() {
            return WorldInfo.b.INFO;
        }

        @Override
        public long getLastPlayed() {
            return this.lastPlayed;
        }

        @Override
        public boolean isDisabled() {
            return false;
        }

        @Override
        public IChatBaseComponent primaryActionMessage() {
            return WorldInfo.b.RECOVER;
        }

        @Override
        public boolean primaryActionActive() {
            return true;
        }

        @Override
        public boolean canUpload() {
            return false;
        }

        @Override
        public boolean canEdit() {
            return false;
        }

        @Override
        public boolean canRecreate() {
            return false;
        }
    }

    public static class c extends WorldInfo {

        private static final IChatBaseComponent MORE_INFO_BUTTON = IChatBaseComponent.translatable("symlink_warning.more_info");
        private static final IChatBaseComponent INFO = IChatBaseComponent.translatable("symlink_warning.title").withColor(-65536);

        public c(String s, Path path) {
            super((WorldSettings) null, (LevelVersion) null, s, false, false, false, path);
        }

        @Override
        public String getLevelName() {
            return this.getLevelId();
        }

        @Override
        public IChatBaseComponent getInfo() {
            return WorldInfo.c.INFO;
        }

        @Override
        public long getLastPlayed() {
            return -1L;
        }

        @Override
        public boolean isDisabled() {
            return false;
        }

        @Override
        public IChatBaseComponent primaryActionMessage() {
            return WorldInfo.c.MORE_INFO_BUTTON;
        }

        @Override
        public boolean primaryActionActive() {
            return true;
        }

        @Override
        public boolean canUpload() {
            return false;
        }

        @Override
        public boolean canEdit() {
            return false;
        }

        @Override
        public boolean canRecreate() {
            return false;
        }
    }
}
