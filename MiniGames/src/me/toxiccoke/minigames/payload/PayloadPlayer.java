package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.team.TwoTeamPlayer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PayloadPlayer extends TwoTeamPlayer<PayloadTeam> {

	PayloadTeam		team;
	PayloadClass	playerClass;
	PayloadClass	tempClass;
	PayloadGame		game;
	boolean			respawning;
	boolean			classChange;
	boolean			dealtDmg;

	public PayloadPlayer(PayloadGame g, Player p, PayloadTeam t, PayloadClass cl) {
		super(p);
		this.game = g;
		this.team = t;
		this.playerClass = cl;
	}

	@Override
	public PayloadTeam getTeam() {
		return team;
	}

	@Override
	public void setTeam(PayloadTeam newTeam) {
		team = newTeam;
	}

	public void respawn() {
		respawning = true;
		dealtDmg = false;
		game.respawn(this);
	}

	public int getAmmo() {
		int a = 0;
		Inventory i = getPlayer().getInventory();
		for (ItemStack s : i.getContents())
			if (s != null && s.getType() == Material.ARROW)
				a += s.getAmount();
		return a;
	}

	@SuppressWarnings("deprecation")
	public void setAmmo(int ammo) {
		int a = getAmmo();
		if (ammo < 0)
			ammo = 0;
		Inventory inv = getPlayer().getInventory();
		if (ammo > a) { // give ammo
			for (int i = inv.getSize() - 1; i > 0; i--) {
				ItemStack s = inv.getItem(i);
				if (s != null && s.getType() == Material.ARROW) {
					int amount = s.getAmount();
					if (amount >= 64)
						continue;
					inv.setItem(i, new ItemStack(Material.ARROW, Math.min(64, amount + ammo - a)));
					a += (s.getAmount() - amount);
					if (ammo >= a) {
						if (a > ammo)
							System.out.println("error " + (a - ammo));
						getPlayer().updateInventory();
						return;
					}
				}
			}
			int stacks = ((ammo - a) % 64) + 1;
			for (int j = 0; j < stacks; j++) {
				int numb = Math.min(64, ammo - a);
				addAmmo(inv, numb);
				a += numb;
				if (a == ammo)
					break;
			}
		} else if (ammo < a) {
			while (a != ammo) {
				// take at most 64 arrows from a stack
				int amount = Math.min(64, a - ammo);
				a -= removeAmmo(inv, amount);
				a -= amount;
				if (a <= ammo)
					break;
			}

		}
		getPlayer().updateInventory();
	}

	private void addAmmo(Inventory inv, int numb) {
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack is = inv.getItem(i);
			if (is == null || is.getType() == Material.AIR) {
				inv.setItem(i, new ItemStack(Material.ARROW, numb));
				return;
			}
		}
	}

	private int removeAmmo(Inventory inv, int numb) {
		for (int i = inv.getSize() - 1; i > 0; i--) {
			ItemStack is = inv.getItem(i);
			if (is != null && is.getType() == Material.ARROW) {
				int amount = is.getAmount() - numb;
				if (amount > 0)
					inv.setItem(i, new ItemStack(Material.ARROW, amount));
				else inv.setItem(i, null);
				if (amount <= 0)
					return amount; // if you have 3 arrows and take 5, this will
									// return -2
				return 0;
			}
		}
		return 0;
	}
}
