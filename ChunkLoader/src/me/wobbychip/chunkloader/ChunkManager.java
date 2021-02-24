package me.wobbychip.chunkloader;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class ChunkManager {
	private String locationWorld;
	private int locationX;
	private int locationZ;
	List<int[]> chunks = new ArrayList<int[]>();

	public ChunkManager(Location location) {
		locationWorld = location.getWorld().getName();
		locationX = location.getChunk().getX();
		locationZ = location.getChunk().getZ();
	}

	public ChunkManager(String locationString) {
		String[] a = locationString.split(Main.Delimiter, 0);
		locationWorld = a[0];
		locationX = (int) Math.floor(Integer.parseInt(a[1])/16);
		locationZ = (int) Math.floor(Integer.parseInt(a[3])/16);
	}

	public void SetArea(int oldArea, int newArea, boolean oldActivated, boolean newActivated) {
		//Didnt get actiavted -> do nothing
		if ((!oldActivated) && (!newActivated)) {
			return;
		}

		//Activated ChunkLoader -> add all chunks
		if ((!oldActivated) && newActivated) {
			oldArea = 0;
		}

		//Deactivated ChunkLoader -> remove all chunks
		if (oldActivated && (!newActivated)) {
			newArea = 0;
		}

		//If ChunkLoader is activated and area didnt change do nothing
		if (newArea == oldArea) {
			return;
		}
		
		if (newArea > oldArea) {
			IncreaseArea(oldArea, newArea);
		} else {
			DecreaseArea(oldArea, newArea);
		}
	}

	private void Save() {
		Main.ChunksConfig.Save();
	}

	private String GetChunkName(int X, int Z) {
		return "chunks." + locationWorld + "." + Utilities.CoordsToString(X, Z);
	}

	public boolean ExistsChunk(int X, int Z) {
		return Main.ChunksConfig.getConfig().contains(GetChunkName(X, Z));
	}

	public int GetOverlap(int X, int Z) {
		if (ExistsChunk(X, Z)) {
			return Main.ChunksConfig.getConfig().getInt(GetChunkName(X, Z));
		} else {
			return -1;
		}
	}

	private void AddChunk(int X, int Z) {
		int overlap = GetOverlap(X, Z)+1;
		Main.ChunksConfig.getConfig().set(GetChunkName(X, Z), overlap);
		chunks.add(new int[] {X, Z});
	}

	private void RemoveChunk(int X, int Z) {
		int overlap = GetOverlap(X, Z);

		if (overlap > 0) {
			Main.ChunksConfig.getConfig().set(GetChunkName(X, Z), overlap-1);
		} else {
			Main.ChunksConfig.getConfig().set(GetChunkName(X, Z), null);
			chunks.add(new int[] {X, Z});
		}
	}

	private void AddArea(int Length) {
		if (Length == 1) {
			AddChunk(locationX, locationZ);
			return;
		}

		int DivTwo = Length/2;

		for (int i = -DivTwo; i < DivTwo; i++) {
			AddChunk(locationX+i, locationZ+DivTwo);
		}

		for (int i = -DivTwo; i < DivTwo; i++) {
			AddChunk(locationX+DivTwo, locationZ-i);
		}

		for (int i = -DivTwo; i < DivTwo; i++) {
			AddChunk(locationX-i, locationZ-DivTwo);
		}

		for (int i = -DivTwo; i < DivTwo; i++) {
			AddChunk(locationX-DivTwo, locationZ+i);
		}
	}

	private void RemoveArea(int Length) {
		if (Length == 1) {
			RemoveChunk(locationX, locationZ);
			return;
		}
		
		int DivTwo = Length/2;

		for (int i = -DivTwo; i < DivTwo; i++) {
			RemoveChunk(locationX+i, locationZ+DivTwo);
		}

		for (int i = -DivTwo; i < DivTwo; i++) {
			RemoveChunk(locationX+DivTwo, locationZ-i);
		}

		for (int i = -DivTwo; i < DivTwo; i++) {
			RemoveChunk(locationX-i, locationZ-DivTwo);
		}

		for (int i = -DivTwo; i < DivTwo; i++) {
			RemoveChunk(locationX-DivTwo, locationZ+i);
		}
	}

	private void IncreaseArea(int oldArea, int newArea) {
		chunks.clear();

		for (int i = oldArea+1; i <= newArea; i++) {
			AddArea(i+(i-1));
		}

		Save();

		for (int[] chunk : chunks) {
	        Utilities.ForceLoadChunk(locationWorld, chunk[0], chunk[1]);
	    }
	}

	private void DecreaseArea(int oldArea, int newArea) {
		chunks.clear();

		for (int i = newArea+1; i <= oldArea; i++) {
			RemoveArea(i+(i-1));
		}

		Save();

		for (int[] chunk : chunks) {
	        Utilities.ForceUnloadChunk(locationWorld, chunk[0], chunk[1]);
	    }
	}
}
