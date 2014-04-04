package me.toxiccoke.minigames.impl;

import java.util.LinkedList;

import me.toxiccoke.minigames.GameEndTimer;
import me.toxiccoke.minigames.MiniGamePlayer;
import me.toxiccoke.minigames.MiniGameWorld;
import me.toxiccoke.minigames.impl.BomberTeam.TeamType;
import me.toxiccoke.tokenshop.TokenShop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class BomberGameWorld extends MiniGameWorld {

	protected LinkedList<BomberGamePlayer>	players;
	private BomberLobbyTimer				lobbyTimer;
	private GameEndTimer					endTimer;
	boolean									isStarted;
	private BomberTeam						red, blue;
	private Team							redTeam, blueTeam;
	private OfflinePlayer					redScore, blueScore;
	private Objective						objective;
	private Scoreboard						board;

	public BomberGameWorld(String worldName) {
		super("Bomber", worldName);
		load();
		players = new LinkedList<BomberGamePlayer>();
		MAX_PLAYERS = 8;
		red = new BomberTeam(this, TeamType.RED);
		blue = new BomberTeam(this, TeamType.BLUE);
		initScoreboard();
	}

	private void initScoreboard() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();
		redTeam = board.registerNewTeam("Red");
		redTeam.setDisplayName(ChatColor.DARK_RED + "Red");
		redTeam.setCanSeeFriendlyInvisibles(true);
		redTeam.setAllowFriendlyFire(false);
		blueTeam = board.registerNewTeam("Blue");
		blueTeam.setDisplayName(ChatColor.DARK_BLUE + "Blue");
		blueTeam.setCanSeeFriendlyInvisibles(true);
		blueTeam.setAllowFriendlyFire(false);
		objective = board.registerNewObjective("score", "trigger");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.GREEN + "Score");
		// Get a fake offline player
		blueScore = Bukkit.getOfflinePlayer(ChatColor.BLUE + "Blue Kills:");
		redScore = Bukkit.getOfflinePlayer(ChatColor.RED + "Red Kills:");
		updateScore();
	}

	private void updateScore() {
		objective.getScore(redScore).setScore(red.getScore());
		objective.getScore(blueScore).setScore(blue.getScore());
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
		p.setGameMode(GameMode.ADVENTURE);
	}

	private void startGame() {
		if (isStarted) return;

		for (BomberGamePlayer p : players) {
			Player pl = p.getPlayer();
			pl.setScoreboard(board);
			pl.setFoodLevel(20);
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
		if (redCount == bluCount) return (Math.random() * 2) < 1 ? red : blue;

		if (redCount > bluCount) return blue;
		else return red;
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
			// Steal some of their health
			double newHealth = ((Damageable) dmg).getHealth() + (((Damageable) p).getHealth() / 2);
			dmg.setHealth((newHealth > 20) ? 20 : newHealth);
			enemy = dmg.getName();
			getPlayer(enemy).addScore(1);
			updateScore();
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

	private void removePlayer(MiniGamePlayer gp, boolean check) {
		players.remove(gp);
		gp.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		gp.restorePlayer();
		if (check) checkNoPlayers();
	}

	private void removePlayer(MiniGamePlayer gp) {
		removePlayer(gp, true);
	}

	private void checkNoPlayers() {
		if (players.size() > 0) return;
		reset();
	}

	public void reset() {
		updateScore();
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
			removePlayer(plr, false);
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

	@Override
	public LinkedList<? extends MiniGamePlayer> getPlayers() {
		return players;
	}

}
