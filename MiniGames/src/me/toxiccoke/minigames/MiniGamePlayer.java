package me.toxiccoke.minigames;


import me.toxiccoke.tokenshop.TokenShop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public abstract class MiniGamePlayer {

	protected String	player;
	OriginalPlayer		originalPlayer;
	protected int		score;

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
		p.setHealth(((Damageable) p).getMaxHealth());
		// remove potions
		for (PotionEffectType t : PotionEffectType.values())
			if (t != null && p.hasPotionEffect(t))
				p.removePotionEffect(t);
	}

	public String getName() {
		return player;
	}
	
	public abstract ChatColor getTeamColor();
	
	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void addScore(int add) {
		this.score += add;
	}

	public abstract Material getFeetParticle();

	public Player getPlayer() {
		return Bukkit.getPlayer(player);
	}
	
	public abstract void startGame();

	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof MiniGamePlayer)) return false;
		return ((MiniGamePlayer) o).player.equals(player);
	}

	class OriginalPlayer {
		Location	location;
		double		health;
		int			hunger;
		int			xpLevel;
		float		xp;
		ItemStack[]	inv;
		ItemStack[]	armor;
		GameMode	gm;
		String tabName;

		public OriginalPlayer(Player p) {
			inv = p.getInventory().getContents();
			armor = p.getInventory().getArmorContents();
			location = p.getLocation();
			health = ((Damageable) p).getHealth();
			hunger = p.getFoodLevel();
			xp = p.getExp();
			xpLevel = p.getLevel();
			gm = p.getGameMode();
			tabName = p.getPlayerListName();
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
			p.setPlayerListName(tabName);
			// remove arrows
			((CraftPlayer) p).getHandle().getDataWatcher().watch(9, (byte)0);
		}

	}

	public void restorePlayer() {
		originalPlayer.restorePlayer(getPlayer());
	}
}
