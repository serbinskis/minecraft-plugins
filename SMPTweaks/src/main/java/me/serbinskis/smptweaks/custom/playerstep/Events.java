package me.serbinskis.smptweaks.custom.playerstep;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Events implements Listener {
    private final HashMap<UUID, Boolean> playerStepIteration = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!PlayerStep.tweak.getGameRuleBoolean(event.getPlayer().getWorld())) { return; }
        if (!event.hasChangedPosition() && !event.hasChangedOrientation()) { return; }
        if (!player.isOnGround() || player.isInsideVehicle() || player.isInWater() || player.isInLava()) { return; }
        if (!event.getTo().clone().add(0, -0.1f, 0).getBlock().getType().isSolid()) { return; }
        if (PlayerStep.isPlayerStepNearby(event.getTo())) return;

        boolean leftFoot = playerStepIteration.getOrDefault(player.getUniqueId(), true);
        TextDisplay stepEntity = PlayerStep.spawnPlayerStep(event.getTo(), leftFoot);
        if (!stepEntity.getLocation().clone().add(0, -0.1f, 0).getBlock().getType().isSolid()) { stepEntity.remove(); }
        playerStepIteration.put(player.getUniqueId(), !leftFoot);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJumpEvent(PlayerJumpEvent event) {
        onPlayerMoveEvent(new PlayerMoveEvent(event.getPlayer(), event.getFrom(), event.getTo()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        Arrays.stream(event.getBlock().getChunk().getEntities()).filter(entity -> {
            if (!entity.getType().equals(EntityType.TEXT_DISPLAY)) { return false; }
            if (!event.getBlock().getBoundingBox().overlaps(entity.getBoundingBox().expand(0.1f))) { return false; }
            return PersistentUtils.hasPersistentDataBoolean(entity, PlayerStep.TAG_PLAYER_STEP);
        }).forEach(Entity::remove);
    }
}
