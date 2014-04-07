package me.toxiccoke.tokenshop;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Hat {

	Material mat;
	int refCode;
	int price;
	boolean special;
	String displayName;

	public Hat(String name, Material m, int price, int ref) {
		this(name, m, price, ref, false);
	}

	public Hat(String name, Material m, int price, int ref, boolean special) {
		this.mat = m;
		this.price = price;
		this.refCode = ref;
		this.special = special;
		this.displayName = name;
	}

	public ItemStack hat() {
		return new ItemStack(mat, 1);
	}

	public static String removeHatLabel = "§aRemove Hat";
	public static ArrayList<Hat> hats;
	static {
		hats = new ArrayList<Hat>();
		hats.add(new Hat("§aJukebox Hat", Material.JUKEBOX, 450, 0));
		hats.add(new Hat("§aPumpkin Hat", Material.PUMPKIN, 450, 1));
		hats.add(new Hat("§aCactus Hat", Material.CACTUS, 450, 2));
		hats.add(new Hat("§aTNT Hat", Material.TNT, 450, 3));
		hats.add(new Hat("§aAnvil Hat", Material.ANVIL, 450, 4));
		hats.add(new Hat("§aPortal Hat", Material.PORTAL, 450, 5));
		hats.add(new Hat("§aCage Hat", Material.MOB_SPAWNER, 450, 6));
		hats.add(new Hat("§4Slime Hat", Material.SLIME_BALL, 1500, 8, true));
		hats.add(new Hat("§4Magmacube Hat", Material.MAGMA_CREAM, 1500, 9, true));

	}

	public static Hat getHat(String name) {
		for (Hat h : hats)
			if (h.displayName.equals(name))
				return h;
		return null;
	}
	
	public static Hat getHat(int ref) {
		for (Hat h : hats)
			if (h.refCode == ref)
				return h;
		return null;
	}
}
