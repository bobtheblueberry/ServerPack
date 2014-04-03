package me.toxiccoke.minigames;

import me.toxiccoke.tokenshop.TokenShop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class MiniGamePlayer {

	protected String	player;
	OriginalPlayer		originalPlayer;

	public MiniGamePlayer(Player p) {
		this.player = p.getName();
		this.originalPlayer = new OriginalPlayer(p);
		p.getInventory().setArmorContents(null);
		p.getInventory().clear();
		
	}

	public abstract Material getFeetParticle();

	public Player getPlayer() {
		return Bukkit.getPlayer(player);
	}

	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof MiniGamePlayer)) return false;
		return ((MiniGamePlayer) o).player.equals(player);
	}

	class OriginalPlayer {
		Location	location;
		double		health;
		float		hunger;
		float		xp;
		ItemStack[]	inv;
		ItemStack[]	armor;

		public OriginalPlayer(Player p) {
			inv = p.getInventory().getContents();
			armor = p.getInventory().getArmorContents();
			location = p.getLocation();
			health = ((Damageable) p).getHealth();
			hunger = p.getExhaustion();
			xp = p.getExp();
		}

		private void restorePlayer(Player p) {
			p.getInventory().setContents(inv);
			p.getInventory().setArmorContents(armor);
			p.setHealth(health);
			p.setExhaustion(hunger);
			p.setExp(xp);
			TokenShop.teleportAdvanced(p, location);
		}

	}

	public void restorePlayer() {
		originalPlayer.restorePlayer(getPlayer());
	}
}
