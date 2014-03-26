package me.toxiccoke.tokenshop;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Toy {
	int refCode;
	int price;
	String displayName;
	Material mat;

	public Toy(String name, int price, int ref, Material m) {
		this.price = price;
		this.refCode = ref;
		this.displayName = name;
		this.mat = m;
	}

	public ItemStack mat() {
		return new ItemStack(mat, 1);
	}
	public static ArrayList<Toy> toys;
	static {
		toys = new ArrayList<Toy>();
		toys.add(new Toy("§aFireworker", 100, 0, Material.FIREWORK));
		toys.add(new Toy("§aSnowBaller", 200, 1, Material.SNOW_BALL));
		toys.add(new Toy("§aFire Launcher", 300, 2, Material.FIREBALL));
		toys.add(new Toy("§aTNT Launcher", 400, 3, Material.TNT));
	}
	public static Toy getToy(String name) {
		for (Toy p : toys)
			if (p.displayName.equals(name))
				return p;
		return null;
	}
}
