package me.serbinskis.smptweaks.custom.fastleafdecay;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

public class FastLeafDecay extends CustomTweak {
    public static CustomTweak tweak;

    public FastLeafDecay() {
        super(FastLeafDecay.class, true, false);
        this.setGameRule("fast_leaf_decay", true, false);
        this.setDescription("Leafs will decay much faster.");
        FastLeafDecay.tweak = this;
    }

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
    }
}