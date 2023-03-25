package me.wobbychip.smptweaks.custom.holograms;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.TextDisplay.TextAligment;

import me.wobbychip.smptweaks.utils.PersistentUtils;
import me.wobbychip.smptweaks.utils.Utils;

public class Hologram {
	private static String TAG_IS_HOLOGRAM = "tag_is_hologram";
	private static String TAG_POSITION = "tag_hologram_position";
	private static String TAG_DISPLAY = "tag_hologram_display";
	private TextDisplay display;
	private Interaction interaction;
	private int y;

	private Hologram(TextDisplay display, Interaction interaction, int y) {
		this.display = display;
		this.interaction = interaction;
		this.y = y;
	}

	public int getY() {
		return y;
	}

	public Location getLocation() {
		int x = interaction.getLocation().getBlockX();
		int z = interaction.getLocation().getBlockZ();
		return new Location(interaction.getLocation().getWorld(), x, y, z);
	}

	public TextDisplay getDisplay() {
		return display;
	}

	private TextDisplay setDisplay(TextDisplay display) {
		return this.display = display;
	}

	public Interaction getInteraction() {
		return interaction;
	}

	private Interaction setInteraction(Interaction interaction) {
		return this.interaction = interaction;
	}

	public static Hologram get(Interaction interaction) {
		if (interaction == null) { return null; }
		if (!PersistentUtils.hasPersistentDataBoolean(interaction, TAG_IS_HOLOGRAM)) { return null; }
		if (!PersistentUtils.hasPersistentDataInteger(interaction, TAG_POSITION)) { return null; }
		if (!PersistentUtils.hasPersistentDataString(interaction, TAG_DISPLAY)) { return null; }

		UUID uuid = UUID.fromString(PersistentUtils.getPersistentDataString(interaction, TAG_DISPLAY));
		Entity entity = Utils.getEntityByUniqueId(uuid);
		if ((entity == null) || (entity.getType() != EntityType.TEXT_DISPLAY)) { return null; }

		int y = PersistentUtils.getPersistentDataInteger(interaction, TAG_POSITION);
		return new Hologram((TextDisplay) entity, interaction, y);
	}

	public static Hologram create(Location location, String text) {
		location = location.getBlock().getLocation().add(0.5, 0, 0.5);
		TextDisplay display = (TextDisplay) location.getWorld().spawnEntity(location, EntityType.TEXT_DISPLAY);
		display.setInvulnerable(true);
		display.setSeeThrough(true);
		display.setLineWidth(Integer.MAX_VALUE);
		display.setBillboard(Billboard.VERTICAL);
		display.setAlignment(TextAligment.CENTER);
		display.setText(text);

		Interaction interaction = (Interaction) location.getWorld().spawnEntity(location, EntityType.INTERACTION);
		PersistentUtils.setPersistentDataBoolean(interaction, TAG_IS_HOLOGRAM, true);
		PersistentUtils.setPersistentDataInteger(interaction, TAG_POSITION, location.getBlockY());
		PersistentUtils.setPersistentDataString(interaction, TAG_DISPLAY, display.getUniqueId().toString());

		Hologram hologram = new Hologram(display, interaction, location.getBlockY());
		float height = (text.split("\r\n|\r|\n").length * 0.25f); //By default 4 lines can fit in 1 block
		interaction.setInteractionHeight((height > 1f) ? height : 1f);
		display.setViewRange(text.isBlank() ? 0f : 1f);

		interaction.teleport(hologram.getLocation().add(0.5, (0.5-(interaction.getInteractionHeight()/2)), 0.5));
		display.teleport(hologram.getLocation().add(0.5, (0.5-(height/2)), 0.5));
		return hologram;
	}

	public void remove() {
		display.remove();
		interaction.remove();
	}

	public String getText() {
		return display.getText();
	}

	public void setText(String text) {
		this.remove();
		Hologram hologram = Hologram.create(this.getLocation(), text);
		this.setDisplay(hologram.getDisplay());
		this.setInteraction(hologram.getInteraction());
	}
}
