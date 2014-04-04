package me.toxiccoke.minigames.impl;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.toxiccoke.minigames.GameEndTimer;
import me.toxiccoke.minigames.MiniGamePlayer;
import me.toxiccoke.minigames.MiniGameWorld;
import me.toxiccoke.minigames.MiniGamesPlugin;
import me.toxiccoke.minigames.impl.BomberTeam.TeamType;
import me.toxiccoke.tokenshop.TokenShop;

public class BomberGameWorld extends MiniGameWorld {

	private LinkedList<BomberGamePlayer>	players;
	private BomberLobbyTimer				lobbyTimer;
	private GameEndTimer					endTimer;
	boolean									isStarted;

	public BomberGameWorld(String worldName) {
		super("Bomber", worldName);
		load();
		players = new LinkedList<BomberGamePlayer>();
		MAX_PLAYERS = 8;
	}

	private void load() {
		// YamlConfiguration yml =
		super.getLoadYML();
	}

	public void save() {
		YamlConfiguration yml = super.getSaveYML();
		// add other stuff here
		super.save(yml);
	}

	@Override
	public int getPlayerCount() {
		return players.size();
	}

	@Override
	public boolean isJoinable() {
		return !broken && spawnLocations.size() == 2 && lobbyLocation != null;
	}

	@Override
	public boolean isFull() {
		return getPlayerCount() == MAX_PLAYERS;
	}

	@Override
	public boolean join(Player p) {
		if (broken || lobbyLocation == null || spawnLocations.size() < 2) return false;
		boolean isInGame = false;
		for (BomberGamePlayer bp : players)
			if (bp.getName().equals(p.getName())) {
				isInGame = true;
				break;
			}

		BomberGamePlayer bgp;
		boolean joined = false;
		if (!isInGame) {
			if (players.size() >= MAX_PLAYERS) return false;
			BomberTeam t = getTeam();
			bgp = new BomberGamePlayer(p, t);
			players.add(bgp);
			initPlayer(p, t.team);
			joined = true;
		} else bgp = getPlayer(p.getName());

		if (!isStarted) TokenShop.teleportAdvanced(p, lobbyLocation);
		else TokenShop.teleportAdvanced(p, getSpawn(getPlayer(p.getName()).team.team));
		p.sendMessage(ChatColor.YELLOW + "Joined Bomber! World: " + ChatColor.GREEN + worldName);
		if (bgp.team.team == TeamType.BLUE) p.sendMessage(ChatColor.DARK_BLUE + "You are in the blue team");
		else p.sendMessage(ChatColor.DARK_RED + "You are in the red team");
		if (joined) {
			// lobby timer
			if (players.size() == 2 && lobbyTimer == null) {
				lobbyTimer = new BomberLobbyTimer(this);
			} else if (players.size() == MAX_PLAYERS) {
				lobbyTimer.countdown = 10;
				startGame();
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	private void initPlayer(final Player p, TeamType t) {
		Inventory i = p.getInventory();
		ItemStack[] s = new ItemStack[] { new ItemStack(Material.DIAMOND_SWORD, 1), new ItemStack(Material.BOW, 1),
				new ItemStack(Material.ARROW, 25), new ItemStack(Material.APPLE, 12) };
		i.addItem(s);
		p.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, (t == TeamType.BLUE) ? (short) 11 : (short) 14));
		p.updateInventory();
		p.setHealth(((Damageable) p).getMaxHealth());
		Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, new Runnable() {
			
			@Override
			public void run() {
				p.setGameMode(GameMode.ADVENTURE);
			}
		},10);
	}

	private void startGame() {
		if (isStarted) return;

		for (BomberGamePlayer p : players) {
			p.getPlayer().setFoodLevel(20);
			TokenShop.teleportAdvanced(p.getPlayer(), getSpawn(p.team.team));
		}
		sendPlayersMessage(ChatColor.YELLOW + "Game Started!");
		endTimer = new GameEndTimer(this, 1);
		isStarted = true;
	}

	public BomberGamePlayer getPlayer(String name) {
		for (BomberGamePlayer p : players)
			if (p.getName().equals(name)) return p;
		return null;
	}

	public Location getSpawn(TeamType t) {
		if (t == TeamType.BLUE) return spawnLocations.get(0);
		return spawnLocations.get(1);
	}

	private BomberTeam getTeam() {
		int redCount = 0, bluCount = 0;
		for (BomberGamePlayer p : players)
			if (p.team.team == TeamType.BLUE) bluCount++;
			else redCount++;
		if (redCount == bluCount) return new BomberTeam((Math.random() * 2) < 1 ? TeamType.BLUE : TeamType.RED);

		if (redCount > bluCount) return new BomberTeam(TeamType.BLUE);
		else return new BomberTeam(TeamType.RED);
	}

	@Override
	public LinkedList<? extends MiniGamePlayer> getPlayers() {
		return players;
	}

	@Override
	public void notifyDeath(MiniGamePlayer gp, Entity damager, DamageCause cause) {
		Player p = gp.getPlayer();
		p.sendMessage(ChatColor.GOLD + "You died!");
		String enemy = null;
		if (damager instanceof Player) {
			Player dmg = (Player) damager;
			p.sendMessage(ChatColor.GOLD + "Killed by " + dmg.getDisplayName());
			dmg.sendMessage(ChatColor.GOLD + "You scored 1 point for killing " + p.getDisplayName());
			enemy = dmg.getName();
		}

		undeath(p);
		TokenShop.teleportAdvanced(p, getSpawn(((BomberGamePlayer) gp).team.team));
		sendPlayersMessage(ChatColor.ITALIC + "" + ChatColor.GRAY + p.getName() + " was killed by " + enemy);
	}

	@Override
	public void notifyDeath(MiniGamePlayer gp, DamageCause cause) {
		Player p = gp.getPlayer();
		p.sendMessage(ChatColor.GOLD + "You died mysteriously!");
		undeath(p);
		TokenShop.teleportAdvanced(p, getSpawn(((BomberGamePlayer) gp).team.team));
		sendPlayersMessage(ChatColor.ITALIC + "" + ChatColor.GRAY + p.getName() + " died.");
	}

	private void undeath(Player p) {
		p.setHealth(((Damageable) p.getPlayer()).getMaxHealth());
		p.setFoodLevel(20);
	}

	public void lobbyUpdate(int secondsLeft) {
		setPlayerXp(secondsLeft);
		if (secondsLeft <= 0) {
			startGame();
		} else if (secondsLeft % 10 == 0) {
			sendPlayersMessage(ChatColor.YELLOW + "Game starting in " + secondsLeft + " seconds...");
		}
	}

	private void setPlayerXp(int levels) {
		for (MiniGamePlayer p : players)
			p.getPlayer().setLevel(levels);
	}

	@Override
	public void notifyQuitGame(MiniGamePlayer gp) {
		removePlayer(gp);
	}

	private void removePlayer(MiniGamePlayer gp) {
		players.remove(gp);
		gp.restorePlayer();
		checkNoPlayers();
	}

	private void checkNoPlayers() {
		if (players.size() > 0) return;
		reset();
	}

	public void reset() {
		isStarted = false;
		players.clear();
		if (lobbyTimer != null) lobbyTimer.cancelTimer();
		if (endTimer != null) endTimer.cancelTimer();
		lobbyTimer = null;
		endTimer = null;
		super.reset();
	}

	private void sendPlayersMessage(String msg) {
		for (BomberGamePlayer p : players)
			p.getPlayer().sendMessage(msg);
	}

	private void endGame() {
		sendPlayersMessage(ChatColor.GOLD + "Game has ended!");
		for (BomberGamePlayer plr : players) {
			plr.getPlayer().sendMessage("You lost. Everyone loses.");
			plr.restorePlayer();
		}
		reset();
	}

	@Override
	public void endUpdate(int minutes) {
		if (minutes > 1) sendPlayersMessage(ChatColor.GOLD + "Game ending in " + minutes + " minutes.");
		else if (minutes > 0) sendPlayersMessage(ChatColor.GOLD + "Game ending in " + minutes + " minute.");
		else endGame();
	}

	public void notifyLeaveCommand(MiniGamePlayer player) {
		Player p = player.getPlayer();
		p.sendMessage(ChatColor.GOLD + "Leaving Bomber");
		removePlayer(player);
	}

	@Override
	public boolean allowDamage(MiniGamePlayer gp) {
		return isStarted;
	}

}
