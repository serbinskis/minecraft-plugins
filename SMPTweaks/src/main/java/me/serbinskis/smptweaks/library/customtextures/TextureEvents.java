package me.serbinskis.smptweaks.library.customtextures;

import me.serbinskis.smptweaks.library.customblocks.CustomBlocks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class TextureEvents implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        if ((CustomBlocks.getSize() == 0) || (CustomTextures.RESOURCE_PACK_URL == null)) { return; }
        CustomTextures.updateCdnRedirect();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if ((CustomTextures.getSize() == 0) || (CustomTextures.RESOURCE_PACK_CDN_URL == null) || (CustomTextures.RESOURCE_PACK_HASH.length == 0)) { return; }
        event.getPlayer().addResourcePack(CustomTextures.RESOURCE_PACK_UUID, CustomTextures.RESOURCE_PACK_CDN_URL, CustomTextures.RESOURCE_PACK_HASH, CustomTextures.RESOURCE_PACK_PROMPT, true);
    }
}
