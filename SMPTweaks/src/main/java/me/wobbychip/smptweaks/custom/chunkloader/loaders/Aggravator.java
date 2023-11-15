package me.wobbychip.smptweaks.custom.chunkloader.loaders;

import me.wobbychip.smptweaks.custom.chunkloader.ChunkLoader;
import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;

import java.util.Collection;
import java.util.UUID;

public class Aggravator {
	public Block block;
	public UUID frameUUID;
	public FakePlayer fakePlayer;

	public Aggravator(Block block, FakePlayer fakePlayer) {
		this.block = block;
		this.fakePlayer = fakePlayer;
	}

	public void setEnabled(boolean isEnabled, Player player) {
		ItemFrame frame = Loader.getItemFrame(block.getLocation());
		if (frame == null) { return; }

		if (isEnabled) {
			if (player != null) { Utils.sendActionMessage(player, "Enabled aggravator mode."); }
			PersistentUtils.setPersistentDataBoolean(frame, ChunkLoader.isAggravator, true);
		} else {
			if (player != null) { Utils.sendActionMessage(player, "Disabled aggravator mode."); }
			PersistentUtils.removePersistentData(frame, ChunkLoader.isAggravator);
		}

		if (player != null) { player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1); }
		aggravate(isEnabled);
	}

	public boolean isEnabled() {
		ItemFrame frame = Loader.getItemFrame(block.getLocation());
		if (frame == null) { return false; }
		return PersistentUtils.hasPersistentDataBoolean(frame, ChunkLoader.isAggravator);
	}

	public void update() {
		if (!ChunkLoader.enableAggravator) { return; }
		aggravate(isEnabled());
	}

	public void remove() {
		setEnabled(false, null);
	}

	public void aggravate(boolean doAggravate) {
		if (fakePlayer.getPlayer() == null) { return; }
		if (!fakePlayer.getPlayer().isValid() && doAggravate) { return; }

		Collection<Entity> entities = Utils.getNearbyEntities(block.getLocation().add(0.5, 0.5, 0.5), null, ChunkLoader.simulationDistance, false);

		for (Entity entity : entities) {
			if (!(entity instanceof Monster)) { continue; }

			if (doAggravate) {
				aggravate((Monster) entity, fakePlayer.getPlayer());
			} else {
				deaggravate((Monster) entity, fakePlayer.getPlayer());
			}
		}
	}

	public void aggravate(Monster monster, Player player) {
		boolean isTargetSame = false;
		boolean isTargetValid = false;
		boolean doAggravate = false;

		if (monster.getTarget() != null) {
			isTargetSame = monster.getTarget().getUniqueId().equals(player.getUniqueId());
			isTargetValid = monster.getTarget().isValid();
			if (!isTargetSame) { return; }
		}

		doAggravate = !isTargetSame || !isTargetValid;
		if ((monster instanceof PigZombie) && doAggravate) { ((PigZombie) monster).setAngry(true); }
		if (doAggravate) { setTarget(monster, player); }
	}

	public void deaggravate(Monster monster, Player player) {
		boolean isTargetSame = false;

		if (monster.getTarget() != null) {
			isTargetSame = monster.getTarget().getUniqueId().equals(player.getUniqueId());
			if (!isTargetSame) { return; }
		}

		if ((monster instanceof PigZombie) && isTargetSame) { ((PigZombie) monster).setAngry(false); }
		if (isTargetSame) { monster.setTarget(null); }
	}

	public void setTarget(Monster monster, Player player) {
		double range = monster.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue();
		double distance = player.getLocation().distance(monster.getLocation());
		if (distance > range) { return; }
		monster.setTarget(player);
	}
}
