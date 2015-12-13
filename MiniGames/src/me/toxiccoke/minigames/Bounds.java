package me.toxiccoke.minigames;

public class Bounds {

	int x1, x2, y1, y2, z1, z2;

	public Bounds(int x1, int x2, int y1, int y2, int z1, int z2) {
		this.x1 = Math.min(x1, x2);
		this.x2 = Math.max(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.y2 = Math.max(y1, y2);
		this.z1 = Math.min(z1, z2);
		this.z2 = Math.max(z1, z2);
	}

	public boolean contains(int x, int y, int z) {
		return (x >= x1 && x <= x2)  && (y >= y1 && y <= y2) && (z >= z1 && z <= z2);
	}
}
