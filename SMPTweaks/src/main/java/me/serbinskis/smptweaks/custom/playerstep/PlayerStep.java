package me.serbinskis.smptweaks.custom.playerstep;

import me.serbinskis.smptweaks.Main;
import me.serbinskis.smptweaks.tweaks.CustomTweak;
import me.serbinskis.smptweaks.utils.PersistentUtils;
import me.serbinskis.smptweaks.utils.TaskUtils;
import me.serbinskis.smptweaks.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.Random;
import java.util.stream.Stream;

public class PlayerStep extends CustomTweak {
    public static CustomTweak tweak;
    public static String TAG_PLAYER_STEP = "isPlayerStep";
    public static String STEP_CHARACTER = "⬛";
    public static float STEP_SCALE = 1.3f;
    public static float STEP_MIN_INTERVAL = 0.9f;
    public static float STEP_RANDOM_OFFSET = 0.256f;
    public static float STEP_BASE_DISTANCE = 0.24f;
    public static long STEP_REMOVE_INTERVAL = 15 * 20L;

    public PlayerStep() {
        super(PlayerStep.class, true);
        this.setGameRule("doPlayerSteps", false, false);
        this.setDescription("Add player steps for atmosphere.");
        PlayerStep.tweak = this;
    }

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new Events(), Main.plugin);
    }

    public static boolean isPlayerStepNearby(Location location) {
        Collection<Entity> nearbyEntities = Utils.getNearbyEntities(location, EntityType.TEXT_DISPLAY, STEP_MIN_INTERVAL, false);
        Stream<TextDisplay> textDisplayStream = nearbyEntities.stream().map(TextDisplay.class::cast);
        return textDisplayStream.anyMatch(textDisplay -> PersistentUtils.hasPersistentDataBoolean(textDisplay, TAG_PLAYER_STEP));
    }

    public static TextDisplay spawnPlayerStep(Location location, boolean leftFoot) {
        double randomZ = -STEP_RANDOM_OFFSET + new Random().nextDouble() * (STEP_RANDOM_OFFSET * 2);
        double offsetX = Math.cos(Math.toRadians(location.getYaw())) * STEP_BASE_DISTANCE * (leftFoot ? -1 : 1);
        double offsetZ = Math.sin(Math.toRadians(location.getYaw())) * STEP_BASE_DISTANCE * (leftFoot ? -1 : 1);

        float randomScale = STEP_SCALE + (STEP_SCALE * 0.15f * new Random().nextFloat());
        double finalX = location.getX() + offsetX;
        double finalZ = location.getZ() + offsetZ + randomZ;
        double finalY = location.getY();

        Location spawnLocation = new Location(location.getWorld(), finalX, finalY, finalZ);
        Transformation transformation = new Transformation(new Vector3f(0), new AxisAngle4f(0, 0, 0, 0), new Vector3f(randomScale), new AxisAngle4f(0, 0, 0, 0));
        TextDisplay stepEntity = (TextDisplay) location.getWorld().spawnEntity(spawnLocation, EntityType.TEXT_DISPLAY);
        stepEntity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        stepEntity.setRotation(location.getYaw(), -90.0f);
        stepEntity.setTransformation(transformation);
        stepEntity.setText(STEP_CHARACTER);
        stepEntity.setTextOpacity((byte) 70);
        stepEntity.setPersistent(false);
        PersistentUtils.setPersistentDataBoolean(stepEntity, TAG_PLAYER_STEP, true);
        TaskUtils.scheduleSyncDelayedTask(stepEntity::remove, STEP_REMOVE_INTERVAL);
        return stepEntity;
    }
}
