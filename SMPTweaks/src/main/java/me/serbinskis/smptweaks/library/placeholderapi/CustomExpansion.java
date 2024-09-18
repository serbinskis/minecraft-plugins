package me.serbinskis.smptweaks.library.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CustomExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getAuthor() {
        return Main.plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getIdentifier() {
        return Main.plugin.getDescription().getName().toLowerCase();
    }

    @Override
    public @NotNull String getVersion() {
        return Main.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if ((offlinePlayer == null) || (offlinePlayer.getPlayer() == null)) { return null; }
        Player player = offlinePlayer.getPlayer();

        //Ingame use %smptweaks_day%
        if (params.equalsIgnoreCase("day")) {
            return String.valueOf(player.getWorld().getFullTime() / 24000);
        }

        //Ingame use %smptweaks_client_local_difficulty%
        if (params.equalsIgnoreCase("client_local_difficulty")) {
            return String.format("%.2f", ReflectionUtils.getLocalDifficulty(player, false));
        }

        //Ingame use %smptweaks_server_local_difficultyy%
        if (params.equalsIgnoreCase("server_local_difficulty")) {
            return String.format("%.2f", ReflectionUtils.getLocalDifficulty(player, true));
        }

        return null;
    }
}
