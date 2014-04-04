package me.toxiccoke.minigames;

import me.toxiccoke.tokenshop.TokenShop;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
		p.setExhaustion(0);
		p.setFoodLevel(20);
		p.setFireTicks(0);
		p.setExp(0);
		p.setLevel(0);
		p.setHealth(((Damageable)p).getMaxHealth());
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
		int		hunger;
		int xpLevel;
		float		xp;
		ItemStack[]	inv;
		ItemStack[]	armor;
		GameMode gm;

		public OriginalPlayer(Player p) {
			inv = p.getInventory().getContents();
			armor = p.getInventory().getArmorContents();
			location = p.getLocation();
			health = ((Damageable) p).getHealth();
			hunger = p.getFoodLevel();
			xp = p.getExp();
			xpLevel = p.getLevel();
			gm = p.getGameMode();
		}

		private void restorePlayer(Player p) {
			p.getInventory().setContents(inv);
			p.getInventory().setArmorContents(armor);
			p.setHealth(health);
			p.setFoodLevel(hunger);
			p.setExp(xp);
			p.setLevel(xpLevel);
			p.setGameMode(gm);
			TokenShop.teleportAdvanced(p, location);
		}

	}

	public void restorePlayer() {
		originalPlayer.restorePlayer(getPlayer());
	}
}
