package me.toxiccoke.tokenshop;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Pet {
	int refCode;
	int price;
	String displayName;
	int mobId;


	public Pet(String name, int price, int ref, int mobId) {
		this.price = price;
		this.refCode = ref;
		this.mobId = mobId;
		this.displayName = name;
	}

	public ItemStack getEgg() {
		return new ItemStack(Material.MONSTER_EGG, 1, (short)mobId);
	}
	public static ArrayList<Pet> pets;
	static {
		pets = new ArrayList<Pet>();
		pets.add(new Pet("§aWolf", 1500, 0, 95));
		pets.add(new Pet("§aCat", 1500, 1, 98));
		pets.add(new Pet("§aCreeper", 1500, 2, 50));
		pets.add(new Pet("§aSpider", 1500, 3, 52));
		pets.add(new Pet("§aSilver Fish", 1500, 4, 60));
		pets.add(new Pet("§aEnderman", 1500, 5, 58));
		pets.add(new Pet("§aBat", 1500, 6, 65));
		pets.add(new Pet("§aSnow Golem", 2000, 7, 97));
		pets.add(new Pet("§aIron Golem", 5000, 8, 99));
	}
	public static Pet getPet(String name) {
		for (Pet p : pets)
			if (p.displayName.equals(name))
				return p;
		return null;
	}
}
