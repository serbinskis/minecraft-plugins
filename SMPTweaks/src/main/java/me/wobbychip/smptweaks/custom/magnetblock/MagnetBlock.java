package me.wobbychip.smptweaks.custom.magnetblock;

import me.wobbychip.smptweaks.library.customblocks.CustomBlocks;
import me.wobbychip.smptweaks.tweaks.CustomTweak;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class MagnetBlock extends CustomTweak {
    public static CustomTweak tweak;

    public MagnetBlock() {
        super(MagnetBlock.class, false, false);
        this.setConfigs(List.of("config.yml"));
        this.setGameRule("doMagnets", true, false);
        this.setDescription("Adds magnetic block that collects items.");
        this.setReloadable(true);
        MagnetBlock.tweak = this;
    }

    public void onEnable() {
        this.onReload();
        CustomBlocks.registerBlock(new me.wobbychip.smptweaks.custom.magnetblock.block.MagnetBlock());
    }

    public void onReload() {
        FileConfiguration config = this.getConfig(0).getConfig();
        me.wobbychip.smptweaks.custom.magnetblock.block.MagnetBlock.DISTANCE = config.getDouble("distance");
        me.wobbychip.smptweaks.custom.magnetblock.block.MagnetBlock.SPEED = config.getDouble("speed");
    }
}
