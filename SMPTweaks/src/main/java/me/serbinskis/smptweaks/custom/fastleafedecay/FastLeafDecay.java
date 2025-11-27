package me.serbinskis.smptweaks.custom.fastleafedecay;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import org.bukkit.Bukkit;

public class FastLeafDecay extends CustomTweak {
    public FastLeafDecay() {
        super(FastLeafDecay.class, true, false);
        this.setGameRule("doFastLeafDecay", true, false);
        this.setDescription("Leafs will decay much faster.");
    }

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new Events(), Main.getPlugin());
    }
}