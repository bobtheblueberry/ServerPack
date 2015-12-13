package me.toxiccoke.minigames;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public abstract class GamePlayer {

	protected Player	player;
	OriginalPlayer		originalPlayer;
	protected int		score;
	private boolean		inGame	= true;
	public boolean		dead;
	public GameArena<?>	game;

	public GamePlayer(Player p, GameArena<?> game) {
		this.player = p;
		this.originalPlayer = new OriginalPlayer(p);
		this.game = game;
		p.getInventory().setArmorContents(null);
		p.getInventory().clear();
		p.setExhaustion(0);
		p.setFoodLevel(20);
		p.setFireTicks(0);
		p.setExp(0);
		p.setLevel(0);
		p.setHealth(((Damageable) p).getMaxHealth());
		// remove potions
		for (PotionEffect t : p.getActivePotionEffects())
			p.removePotionEffect(t.getType());

	}

	public void leaveGame() {
		inGame = false;
	}

	public boolean isInGame() {
		return inGame;
	}

	public String getName() {
		return player.getName();
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
		return player;
	}

	public void startGame() {}

	class OriginalPlayer {
		Location	location;
		double		health;
		int			hunger;
		int			xpLevel;
		float		xp;
		ItemStack[]	inv;
		ItemStack[]	armor;
		GameMode	gm;
		String		tabName;

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
			// remove potions
			for (PotionEffect t : p.getActivePotionEffects())
				p.removePotionEffect(t.getType());
			p.getInventory().setContents(inv);
			p.getInventory().setArmorContents(armor);
			p.setHealth(health);
			p.setFoodLevel(hunger);
			p.setExp(xp);
			p.setLevel(xpLevel);
			p.setGameMode(gm);
			p.teleport(location);
			p.setPlayerListName(tabName);
			// remove arrows
			try {
				((CraftPlayer) p).getHandle().getDataWatcher().watch(9, (byte) 0);
			} catch (Exception exc) {}
		}

	}

	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o instanceof String)
			return player != null && o.equals(getName());
		if (o instanceof Player)
			return player.getName().equals(((Player)o).getName());
		if (o instanceof GamePlayer)
			return ((GamePlayer) o).player.getName().equals(player.getName());
		return false;
	}

	public void restorePlayer() {
		originalPlayer.restorePlayer(getPlayer());
	}
}
