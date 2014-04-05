package me.toxiccoke.minigames.impl;

import java.util.LinkedList;

import me.toxiccoke.minigames.GameEndTimer;
import me.toxiccoke.minigames.MiniGamePlayer;
import me.toxiccoke.minigames.MiniGameWorld;
import me.toxiccoke.minigames.impl.BomberTeam.TeamType;
import me.toxiccoke.tokenshop.TokenShop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
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
			initPlayer(bgp, t.team);
			joined = true;
		} else bgp = getPlayer(p.getName());

		if (!isStarted) TokenShop.teleportAdvanced(p, lobbyLocation);
		else {
			p.setScoreboard(board);
			p.setFoodLevel(20);
			TokenShop.teleportAdvanced(p, getSpawn(getPlayer(p.getName()).team.team));
		}

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
	private void initPlayer(BomberGamePlayer plr, TeamType t) {
		Player p = plr.getPlayer();
		Inventory i = p.getInventory();
		ItemStack[] s = new ItemStack[] { new ItemStack(Material.IRON_SWORD, 1), new ItemStack(Material.BOW, 1),
				new ItemStack(Material.ARROW, 25), new ItemStack(Material.APPLE, 5) };
		i.addItem(s);
		updateArmor(p, t);
		p.updateInventory();
		p.setHealth(((Damageable) p).getMaxHealth());
		p.setGameMode(GameMode.ADVENTURE);
		if (plr.team.team == TeamType.BLUE) blueTeam.addPlayer(p);
		else redTeam.addPlayer(p);

	}

	private void updateArmor(Player p, TeamType t) {
		ItemStack[] is = getColoredArmor((t == TeamType.BLUE) ? 0 : 255, 0, (t == TeamType.BLUE) ? 255 : 0);
		p.getInventory().setArmorContents(is);
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
		endTimer = new GameEndTimer(this, 4);
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

	private void removePlayer(MiniGamePlayer gp) {
		players.remove(gp);
		gp.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		removePlayerFromScoreboard((BomberGamePlayer) gp);
		gp.restorePlayer();
		checkNoPlayers();
	}

	private void checkNoPlayers() {
		if (players.size() > 1) {
			balanceTeams();
		}
		if (players.size() < 1) reset();
	}

	private void balanceTeams() {
		int redCount = 0, bluCount = 0;
		for (BomberGamePlayer p : players)
			if (p.team.team == TeamType.BLUE) bluCount++;
			else redCount++;
		if (Math.abs(redCount - bluCount) > 1) {
			BomberGamePlayer plr = getRandomPlayer(TeamType.RED);
			if (plr == null) return;
			Player p = plr.getPlayer();
			if (redCount > bluCount) {
				plr.team = blue;
				p.sendMessage(ChatColor.GOLD + "You were switched to the blue team to balance the teams.");
			} else {
				plr.team = red;
				plr.getPlayer().sendMessage(ChatColor.GOLD + "You were switched to the red team to balance the teams.");
			}
			updateArmor(p, plr.team.team);
			TokenShop.teleportAdvanced(p, getSpawn(plr.team.team));
			balanceTeams();
		}
	}

	private BomberGamePlayer getRandomPlayer(TeamType t) {
		LinkedList<BomberGamePlayer> temp = new LinkedList<BomberGamePlayer>();
		for (BomberGamePlayer p : players)
			if (p.team.team == t) temp.add(p);
		if (temp.size() == 0) return null;
		return temp.get((int) (Math.random() * temp.size()));
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
		String bluMsg, redMsg;
		int bs = blue.getScore(), rs = red.getScore();
		if (bs == rs) bluMsg = redMsg = "Scores tied at " + rs + " points.";
		else if (bs > rs) {
			bluMsg = "You won the game by " + (bs - rs) + " points!";
			redMsg = "You lost the game by " + (bs - rs) + " points!";
		} else {
			redMsg = "You won the game by " + (rs - bs) + " points!";
			bluMsg = "You lost the game by " + (rs - bs) + " points!";
		}
		for (BomberGamePlayer plr : players) {
			Player p = plr.getPlayer();
			if (plr.team.team == TeamType.RED) p.sendMessage(ChatColor.GOLD + redMsg);
			else p.sendMessage(ChatColor.GOLD + bluMsg);
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			plr.restorePlayer();
			removePlayerFromScoreboard(plr);
		}
		players.clear();
		updateScore();
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

	private void removePlayerFromScoreboard(BomberGamePlayer plr) {
		if (plr.team.team == TeamType.BLUE) {
			blueTeam.removePlayer(Bukkit.getOfflinePlayer(plr.getName()));
		} else {
			redTeam.removePlayer(Bukkit.getOfflinePlayer(plr.getName()));
		}
	}

	private ItemStack[] getColoredArmor(int r, int g, int b) {
		ItemStack[] is = new ItemStack[4];

		is[0] = new ItemStack(Material.LEATHER_BOOTS, 1);
		LeatherArmorMeta lam = (LeatherArmorMeta) is[0].getItemMeta();
		lam.setColor(Color.fromRGB(r, g, b));
		is[0].setItemMeta(lam);
		is[1] = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		lam = (LeatherArmorMeta) is[1].getItemMeta();
		lam.setColor(Color.fromRGB(r, g, b));
		is[1].setItemMeta(lam);
		is[2] = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		lam = (LeatherArmorMeta) is[2].getItemMeta();
		lam.setColor(Color.fromRGB(r, g, b));
		is[2].setItemMeta(lam);
		is[3] = new ItemStack(Material.LEATHER_HELMET, 1);
		lam = (LeatherArmorMeta) is[3].getItemMeta();
		lam.setColor(Color.fromRGB(r, g, b));
		is[3].setItemMeta(lam);
		return is;
	}

	@Override
	public boolean canPlaceBlock(MiniGamePlayer p, BlockPlaceEvent event) {
		return isStarted && event.getBlock().getLocation().getY() < heightLimit;
	}

	@Override
	public boolean canBreakBlock(MiniGamePlayer p, BlockBreakEvent event) {
		Material m = event.getBlock().getType();
		if (m == Material.LEAVES || m == Material.LEAVES_2 || m == Material.GLOWSTONE || m == Material.STONE
				|| m == Material.STAINED_GLASS) return false;
		return isStarted && event.getBlock().getLocation().getY() < heightLimit;
	}
}
