package me.wobbychip.chunkloader;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleManager {
	private Player player;
	private Location pLocation;
	private String managerName;
	private double distance;
	private int locationX;
	private int locationZ;
	private int locationY;
	private int diameter;
	private int TaskID = -1;

	int[] areaBounds = {0, 0, 0}; //Start X, Z, Width
	List<double[]> areaBorder = new ArrayList<double[]>();

	public ParticleManager(Player player, Location location, int Area) {
		this.player = player;
		
		managerName = player.getUniqueId().toString() + "#" + Utilities.LocationToString(location);
		locationY = (int) location.getY();
		locationX = location.getChunk().getX();
		locationZ = location.getChunk().getZ();
		diameter = Area+(Area-1);
		distance = Math.sqrt(((((diameter+1)*16)/2)*(((diameter+1)*16)/2))*2);

		areaBounds[0] = (locationX*16)-((diameter/2)*16); //X
		areaBounds[1] = (locationZ*16)-((diameter/2)*16); //Z
		areaBounds[2] = diameter*16; //Width

		pLocation = location;
		pLocation.setY(0);
	}

	private void Draw() {
		Location location = new Location(pLocation.getWorld(), locationX*16, 0, locationZ*16);

		for (int i = -1; i <= 2; i++) {
			for (double[] a : areaBorder) {
				location.setX(a[0]);
				location.setY(locationY+i);
				location.setZ(a[1]);

				player.spawnParticle(Particle.FLAME, location, 1, 0, 0, 0, 0);
		    }
		}
	}

	private void GenerateBorder() {		
		for (int i = 0; i <= areaBounds[2]; i++) {
			areaBorder.add(new double[] {areaBounds[0]+i, areaBounds[1]});
		}

		for (int i = 1; i < areaBounds[2]; i++) {
			areaBorder.add(new double[] {areaBounds[0], areaBounds[1]+i});
		}

		for (int i = 1; i < areaBounds[2]; i++) {
			areaBorder.add(new double[] {areaBounds[0]+areaBounds[2], areaBounds[1]+i});
		}

		for (int i = 0; i <= areaBounds[2]; i++) {
			areaBorder.add(new double[] {areaBounds[0]+i, areaBounds[1]+areaBounds[2]});
		}
	}

	public void Start() {
		if (Main.particleManagers.containsKey(managerName)) {
			ParticleManager particleManager = Main.particleManagers.get(managerName);
			particleManager.Stop();
		}

		GenerateBorder();
		Main.particleManagers.put(managerName, this);
		
		TaskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.plugin, new Runnable() {
            public void run() {
            	if (!player.isOnline()) {
            		Stop();
            	}

            	Location location = player.getLocation();
            	location.setY(0);

            	if ((location.getWorld().getName() != pLocation.getWorld().getName()) || (location.distance(pLocation) > distance)) {
            		Stop();
				}

            	Draw();
            }
        }, 0L, 15L);
	}

	public void Stop() {
		areaBorder.clear();

		if (Main.particleManagers.containsKey(managerName)) {
			ParticleManager particleManager = Main.particleManagers.get(managerName);
			Main.particleManagers.remove(managerName);
			particleManager.Stop();
		}

		if (TaskID != -1) { Bukkit.getServer().getScheduler().cancelTask(TaskID); }
	}
}
