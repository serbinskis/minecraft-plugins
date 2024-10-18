package me.serbinskis.smptweaks.library.customblocks.custom;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.library.customblocks.blocks.CustomBlock;
import me.serbinskis.smptweaks.utils.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import java.net.UnknownHostException;
import java.util.Arrays;

public class PlayerResetBlock  extends CustomBlock {
    public PlayerResetBlock() {
        super("players_reset_block", Material.STRUCTURE_VOID);
        this.setCustomName(Main.SYM_COLOR + "rPlayers Reset Block (Admin Block)");
        this.setGlowing(ChatColor.WHITE);
    }

    @Override
    public void create(Block block, boolean new_block) {
        block.setType(Material.AIR);

        for (OfflinePlayer offlinePlayer : Arrays.stream(Bukkit.getOfflinePlayers()).toList()) {
            if (offlinePlayer.getLocation() == null) { continue; }
            if (!offlinePlayer.getLocation().getWorld().equals(block.getWorld())) { continue; }
            if (offlinePlayer.getPlayer() != null) { continue; }
            try { ReflectionUtils.resetOfflinePlayer(block.getLocation(), offlinePlayer); } catch (UnknownHostException ignored) {}
        }
    }
}