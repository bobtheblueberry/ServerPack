package me.toxiccoke.minigames;

public class Bounds {

	int x1,x2,z1,z2;
	
	public Bounds(int x1,int x2, int z1, int z2) 
	{
		this.x1 = x1;
		this.x2 = x2;
		this.z1 = z1;
		this.z2 = z2;
	}
	
	public boolean contains(int x, int z) {
		return (x >= x1 && x <= x2) && (z >= z1 && z <= z2);
	}
}
