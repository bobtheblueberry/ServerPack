package me.toxiccoke.minigames.bomber;

import java.util.LinkedList;

import me.toxiccoke.minigames.Bounds;
import me.toxiccoke.minigames.GameEndTimer;
import me.toxiccoke.minigames.MiniGamePlayer;
import me.toxiccoke.minigames.MiniGameWorld;
import me.toxiccoke.minigames.SkullUtils;
import me.toxiccoke.minigames.bomber.BomberTeam.TeamType;
import me.toxiccoke.tokenshop.TokenShop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class BomberGameWorld extends MiniGameWorld {

	class Leader {
		String	name;
		int		score;

		public Leader(String name, int score) {
			this.name = name;
			this.score = score;
		}

		public String toString() {
			return "Leader(" + name + "," + score + ")";
		}
	}

	protected LinkedList<BomberGamePlayer>	players;
	private BomberLobbyTimer				lobbyTimer;
	private GameEndTimer					endTimer;
	boolean									isStarted;
	private BomberTeam						red, blue;
	private Team							redTeam, blueTeam;
	private OfflinePlayer					redScore, blueScore;
	private Objective						objective;
	private Scoreboard						board;
	private volatile BomberGamePlayer		bomber;
	private int								gamelength	= 4;

	private volatile Leader					leader1, leader2, leader3;

	public BomberGameWorld(String worldName) {
		super("Bomber", worldName);
		load();
		players = new LinkedList<BomberGamePlayer>();
		red = new BomberTeam(this, TeamType.RED);
		blue = new BomberTeam(this, TeamType.BLUE);
		initScoreboard();
	}

	@Override
	public boolean allowDamage(MiniGamePlayer gp) {
		return isStarted;
	}

	private void balanceTeams() {
		int redCount = 0, bluCount = 0;
		for (BomberGamePlayer p : players)
			if (p.team.team == TeamType.BLUE)
				bluCount++;
			else redCount++;
		if (Math.abs(redCount - bluCount) > 1) {
			BomberGamePlayer plr = getRandomPlayer(TeamType.RED);
			if (plr == null)
				return;
			Player p = plr.getPlayer();
			removePlayerFromScoreboard(plr);
			if (redCount > bluCount) {
				plr.team = blue;
				p.sendMessage(ChatColor.GOLD + "You were switched to the blue team to balance the teams.");
			} else {
				plr.team = red;
				plr.getPlayer().sendMessage(ChatColor.GOLD + "You were switched to the red team to balance the teams.");
			}
			initPlayer(plr);
			spawn(plr);
			balanceTeams();
		}
	}

	@Override
	public boolean canBreakBlock(MiniGamePlayer p, BlockBreakEvent event) {
		return canDestroy(event.getBlock().getType(), event.getBlock().getLocation());
	}

	private boolean canDestroy(Material m, Location l) {

		if (m == Material.LEAVES || m == Material.LEAVES_2 || m == Material.GLOWSTONE || m == Material.WOOL)
			return false;
		boolean bounds = true;
		Bounds bound = getBounds();
		if (bound != null) {
			bounds = bound.contains(l.getBlockX(), l.getBlockZ());
		}
		return isStarted && bounds && l.getY() < heightLimit;
	}

	@Override
	public boolean canExplodeBlock(Block b, Entity e) {
		if (e != null && e.getType() == EntityType.FIREBALL)
			return false;
		return canDestroy(b.getType(), b.getLocation());
	}

	@Override
	public boolean canPlaceBlock(MiniGamePlayer p, BlockPlaceEvent event) {
		return isStarted && event.getBlock().getLocation().getY() < heightLimit;
	}

	private void checkNoPlayers() {
		if (players.size() > 1) {
			balanceTeams();
		}
		if (players.size() < 1)
			reset();
	}

	private void doKillPoints(Player killer, Player victim, boolean stealHp) {
		killer.sendMessage(ChatColor.GOLD + "You scored 1 point for killing " + victim.getDisplayName());
		// Steal some of their health
		if (stealHp) {
			double steal = (((Damageable) victim).getHealth() / 2);

			killer.sendMessage(ChatColor.RED + "+" + Math.round(steal));
			double newHealth = ((Damageable) killer).getHealth() + steal;
			killer.setHealth((newHealth > 20) ? 20 : newHealth);
		}
		getPlayer(killer.getName()).addScore(1);
		updateScore();
	}

	private void endGame() {
		sendPlayersMessage(ChatColor.GOLD + "Game has ended!");
		String bluMsg, redMsg;
		int bs = blue.getScore(), rs = red.getScore();
		if (bs == rs)
			bluMsg = redMsg = "Scores tied at " + rs + " points.";
		else if (bs > rs) {
			bluMsg = "You won the game by " + (bs - rs) + " points!";
			redMsg = "You lost the game by " + (bs - rs) + " points!";
		} else {
			redMsg = "You won the game by " + (rs - bs) + " points!";
			bluMsg = "You lost the game by " + (rs - bs) + " points!";
		}
		for (BomberGamePlayer plr : players) {
			Player p = plr.getPlayer();
			if (plr.team.team == TeamType.RED)
				p.sendMessage(ChatColor.GOLD + redMsg);
			else p.sendMessage(ChatColor.GOLD + bluMsg);
			checkLeader(plr);
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
		if (minutes > 1)
			sendPlayersMessage(ChatColor.GOLD + "Game ending in " + minutes + " minutes.");
		else if (minutes > 0)
			sendPlayersMessage(ChatColor.GOLD + "Game ending in " + minutes + " minute.");
		else endGame();
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
		lam.setColor(Color.fromRGB((r == 0) ? (int) (Math.random() * 256) : r, (g == 0) ? (int) (Math.random() * 256)
				: g, (b == 0) ? (int) (Math.random() * 256) : b));
		is[3].setItemMeta(lam);
		return is;
	}

	public BomberGamePlayer getPlayer(String player) {
		for (BomberGamePlayer p : players)
			if (p.getName().equals(player))
				return p;
		return null;
	}

	@Override
	public int getPlayerCount() {
		return players.size();
	}

	@Override
	public LinkedList<? extends MiniGamePlayer> getPlayers() {
		return players;
	}

	private BomberGamePlayer getRandomPlayer(TeamType t) {
		LinkedList<BomberGamePlayer> temp = new LinkedList<BomberGamePlayer>();
		for (BomberGamePlayer p : players)
			if (p.team.team == t)
				temp.add(p);
		if (temp.size() == 0)
			return null;
		return temp.get((int) (Math.random() * temp.size()));
	}

	public void spawn(BomberGamePlayer p) {
		if (!isStarted) {
			TokenShop.teleportAdvanced(p.getPlayer(), lobbyLocation);
			return;
		}
		Location l;
		if (p.team.team == TeamType.BLUE)
			l = spawnLocations.get(0);
		else l = spawnLocations.get(1);
		TokenShop.teleportAdvanced(p.getPlayer(), l);
	}

	private BomberTeam getTeam() {
		int redCount = 0, bluCount = 0;
		for (BomberGamePlayer p : players)
			if (p.team.team == TeamType.BLUE)
				bluCount++;
			else redCount++;
		if (redCount == bluCount)
			return (Math.random() * 2) < 1 ? red : blue;

		if (redCount > bluCount)
			return blue;
		else return red;
	}

	@SuppressWarnings("deprecation")
	private void initPlayer(BomberGamePlayer plr) {
		Player p = plr.getPlayer();
		Inventory i = p.getInventory();
		ItemStack[] s = new ItemStack[] { new ItemStack(Material.IRON_SWORD, 1), new ItemStack(Material.BOW, 1),
				new ItemStack(Material.ARROW, 25) };
		i.addItem(s);
		updateArmor(p, plr.team.team);
		p.updateInventory();
		p.setHealth(((Damageable) p).getMaxHealth());
		p.setGameMode(GameMode.ADVENTURE);
		if (plr.team.team == TeamType.BLUE) {
			blueTeam.addPlayer(p);
			String name = ChatColor.DARK_BLUE + p.getName();
			if (name.length() > 16)
				name = name.substring(0, 15);
			p.setPlayerListName(name);
		} else {
			redTeam.addPlayer(p);
			String name = ChatColor.DARK_RED + p.getName();
			if (name.length() > 16)
				name = name.substring(0, 15);
			p.setPlayerListName(name);
		}
		if (isStarted)
			plr.startGame();
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

	@Override
	public boolean isFull() {
		return getPlayerCount() >= maxplayers;
	}

	@Override
	public boolean isJoinable() {
		return !broken && spawnLocations.size() == 2 && lobbyLocation != null;
	}

	@Override
	public boolean join(Player p) {
		if (broken || lobbyLocation == null || spawnLocations.size() < 2 || p == null)
			return false;
		boolean isInGame = false;
		for (BomberGamePlayer bp : players)
			if (bp.getName().equals(p.getName())) {
				isInGame = true;
				break;
			}

		BomberGamePlayer bgp;
		boolean joined = false;
		if (!isInGame) {
			if (players.size() >= maxplayers)
				return false;
			BomberTeam t = getTeam();
			bgp = new BomberGamePlayer(p, t);
			players.add(bgp);
			initPlayer(bgp);
			joined = true;
		} else bgp = getPlayer(p.getName());

		if (isStarted) {
			p.setScoreboard(board);
			p.setFoodLevel(20);
		}

		spawn(bgp);
		p.sendMessage(ChatColor.YELLOW + "Joined Bomber! World: " + ChatColor.GREEN + worldName);
		if (bgp.team.team == TeamType.BLUE)
			p.sendMessage(ChatColor.DARK_BLUE + "You are in the blue team");
		else p.sendMessage(ChatColor.DARK_RED + "You are in the red team");
		if (joined) {
			// lobby timer
			if (players.size() == minplayers && lobbyTimer == null && !isStarted) {
				lobbyTimer = new BomberLobbyTimer(this);
			} else if (players.size() == maxplayers) {
				lobbyTimer.countdown = 10;
				startGame();
			}
		}
		return true;
	}

	private void load() {
		YamlConfiguration yml = super.getLoadYML();

		if (yml.contains("leader1.name")) {
			leader1 = new Leader(yml.getString("leader1.name"), yml.getInt("leader1.score"));
		}
		if (yml.contains("leader2.name")) {
			leader2 = new Leader(yml.getString("leader2.name"), yml.getInt("leader2.score"));
		}
		if (yml.contains("leader3.name")) {
			leader3 = new Leader(yml.getString("leader3.name"), yml.getInt("leader3.score"));
		}
	}

	public void lobbyUpdate(int secondsLeft) {
		setPlayerXp(secondsLeft);
		if (secondsLeft <= 0) {
			startGame();
		} else if (secondsLeft % 10 == 0) {
			sendPlayersMessage(ChatColor.YELLOW + "Game starting in " + secondsLeft + " seconds...");
		}
	}

	@Override
	public void notifyDeath(MiniGamePlayer gp, Entity damager, DamageCause cause) {
		Player p = gp.getPlayer();
		String enemy = null;
		if (damager instanceof Player) {
			Player dmg = (Player) damager;
			p.sendMessage(ChatColor.GOLD + "Killed by " + dmg.getDisplayName());
			if (getPlayer(dmg.getName()).team.team == ((BomberGamePlayer) gp).team.team)
				return; // 'friendly' fire

			doKillPoints(dmg, p, true);
			enemy = dmg.getName();
		} else if (damager instanceof Projectile) {
			Projectile dmg = (Projectile) damager;
			if (dmg.getShooter() instanceof Player) {
				Player dmger = (Player) dmg.getShooter();
				if (dmger.getName().equals(gp.getName())) {
					enemy = "himself";
				} else {
					if (dmg instanceof Arrow)
						enemy = dmger.getName() + "'s Arrow";
					else if (dmg instanceof Fireball)
						enemy = dmger.getName() + "'s Fireball";
					else enemy = dmger.getName() + "'s Projectile";
					doKillPoints(dmger, p, false);
				}
			}
		}

		undeath(p);
		spawn((BomberGamePlayer) gp);
		sendPlayersMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + p.getName() + " was killed by " + enemy);
	}

	@Override
	public void notifyDeath(MiniGamePlayer gp, EntityDamageEvent e) {
		Player p = gp.getPlayer();
		String cause = e.getCause().toString();
		boolean custom = false;
		if ((e.getCause() == DamageCause.BLOCK_EXPLOSION || e.getCause() == DamageCause.PROJECTILE)) {
			if (bomber != null) {
				if (!bomber.getName().equals(gp.getName())) {
					cause = bomber.getName() + "'s explosive arrow";
					if (bomber.team.team != ((BomberGamePlayer) gp).team.team) {
						// no points for friendly fire
						Player b = bomber.getPlayer();
						doKillPoints(b, p, false);
					}
				} else cause = "their own explosive arrow";
			} else cause = "Herobrine's explosive arrow";
		} else if (e.getCause() == DamageCause.FIRE || e.getCause() == DamageCause.FIRE_TICK
				|| e.getCause() == DamageCause.LAVA) {
			custom = true;
			cause = " burned to death";
		} else if (e.getCause() == DamageCause.SUICIDE) {
			custom = true;
			cause = " bid farewell, cruel world";
		} else if (e.getCause() == DamageCause.FALL) {
			custom = true;
			cause = " fell to their death";
		}
		undeath(p);
		spawn((BomberGamePlayer) gp);
		if (custom)
			sendPlayersMessage(ChatColor.GRAY + p.getName() + cause);
		else sendPlayersMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + p.getName() + " was killed by " + cause);
	}

	public void notifyLeaveCommand(MiniGamePlayer player) {
		Player p = player.getPlayer();
		p.sendMessage(ChatColor.GOLD + "Leaving Bomber");
		removePlayer(player);
	}

	@Override
	public void notifyQuitGame(MiniGamePlayer gp) {
		removePlayer(gp);
	}

	@Override
	public void projectileHit(MiniGamePlayer p, ProjectileHitEvent event) {
		if (!isStarted)
			return;
		Arrow arrow = (Arrow) event.getEntity();
		float random = (float) arrow.getVelocity().length() / 3;
		if (Math.random() * 2 < 1) {
			if (Math.random() * 10 < 1)
				random = 2 + (float) Math.random();
			bomber = (BomberGamePlayer) p;
			arrow.getWorld().createExplosion(arrow.getLocation(), random, Math.random() * 6 < 1);
		}
		return;

	}

	private void removePlayer(MiniGamePlayer gp) {
		players.remove(gp);
		gp.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		removePlayerFromScoreboard((BomberGamePlayer) gp);
		gp.restorePlayer();
		checkNoPlayers();
		updateScore();
	}

	private void removePlayerFromScoreboard(BomberGamePlayer plr) {
		if (plr.team.team == TeamType.BLUE) {
			blueTeam.removePlayer(Bukkit.getOfflinePlayer(plr.getName()));
		} else {
			redTeam.removePlayer(Bukkit.getOfflinePlayer(plr.getName()));
		}
	}

	public void reset() {
		updateScore();
		isStarted = false;
		players.clear();
		if (lobbyTimer != null)
			lobbyTimer.cancelTimer();
		if (endTimer != null)
			endTimer.cancelTimer();
		lobbyTimer = null;
		endTimer = null;
		bomber = null;
		super.reset();
	}

	public void save() {
		YamlConfiguration yml = super.getSaveYML();
		if (leader1 != null) {
			yml.set("leader1.name", leader1.name);
			yml.set("leader1.score", leader1.score);
		}
		if (leader2 != null) {
			yml.set("leader2.name", leader2.name);
			yml.set("leader2.score", leader2.score);
		}
		if (leader3 != null) {
			yml.set("leader3.name", leader3.name);
			yml.set("leader3.score", leader3.score);
		}

		// add other stuff here
		super.save(yml);
	}

	private void sendPlayersMessage(String msg) {
		for (BomberGamePlayer p : players)
			p.getPlayer().sendMessage(msg);
	}

	private void setPlayerXp(int levels) {
		for (MiniGamePlayer p : players)
			p.getPlayer().setLevel(levels);
	}

	private void startGame() {
		if (isStarted)
			return;
		isStarted = true;
		for (BomberGamePlayer p : players) {
			Player pl = p.getPlayer();
			pl.setScoreboard(board);
			pl.setFoodLevel(20);
			p.startGame();
			spawn(p);
		}
		sendPlayersMessage(ChatColor.YELLOW + "Game Started!");
		endTimer = new GameEndTimer(this, gamelength);
	}

	private void undeath(Player p) {
		p.setHealth(((Damageable) p.getPlayer()).getMaxHealth());
		p.setFoodLevel(20);
		p.setFireTicks(0);
	}

	private void updateArmor(Player p, TeamType t) {
		ItemStack[] is = getColoredArmor((t == TeamType.BLUE) ? 0 : 255, 0, (t == TeamType.BLUE) ? 255 : 0);
		p.getInventory().setArmorContents(is);
	}

	private void checkLeader(MiniGamePlayer p) {
		int score = p.getScore();
		if (leader1 == null || score > leader1.score) {
			leader1 = new Leader(p.getName(), score);
			updateLeaderboard();
			save();
		} else if (leader2 == null || score > leader2.score) {
			leader2 = new Leader(p.getName(), score);
			updateLeaderboard();
			save();
		} else if (leader3 == null || score > leader3.score) {
			leader3 = new Leader(p.getName(), score);
			updateLeaderboard();
			save();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void updateLeaderboard() {
		if (leaderboard == null)
			return;
		BlockFace f = BlockFace.SOUTH;
		if (leader1 != null) {
			SkullUtils.PlaceSkull(leaderboard.clone().add(0, 2, 0), leader1.name, f);
			Block s = leaderboard.getWorld().getBlockAt(leaderboard.clone().add(0, 1, 1));
			s.setType(Material.WALL_SIGN);
			s.setData((byte) 3);
			Sign sign = (Sign) s.getState();
			sign.setLine(0, "1st");
			sign.setLine(1, leader1.name);
			sign.setLine(2, leader1.score + " Kills");
			sign.update();
		} else {
			SkullUtils.PlaceSkull(leaderboard.clone().add(0, 2, 0), "Herobrine", f);
			Block s = leaderboard.getWorld().getBlockAt(leaderboard.clone().add(0, 1, 1));
			s.setType(Material.WALL_SIGN);
			s.setData((byte) 3);
			Sign sign = (Sign) s.getState();
			sign.setLine(0, "1st");
			sign.setLine(1, "Herobrine");
			sign.setLine(2, "Kills: " + ChatColor.MAGIC + "9001");
			sign.update();
		}

		if (leader2 != null) {
			SkullUtils.PlaceSkull(leaderboard.clone().add(-1, 1, 0), leader2.name, f);
			Block s = leaderboard.getWorld().getBlockAt(leaderboard.clone().add(-1, 0, 1));
			s.setType(Material.WALL_SIGN);
			s.setData((byte) 3);
			Sign sign = (Sign) s.getState();
			sign.setLine(0, "2nd");
			sign.setLine(1, leader2.name);
			sign.setLine(2, leader2.score + " Kills");
			sign.update();
		} else SkullUtils.PlaceSkull(leaderboard.clone().add(-1, 1, 0), "Herobrine", f);

		if (leader3 != null) {
			SkullUtils.PlaceSkull(leaderboard.clone().add(1, 1, 0), leader3.name, f);
			Block s = leaderboard.getWorld().getBlockAt(leaderboard.clone().add(1, 0, 1));
			s.setType(Material.WALL_SIGN);
			s.setData((byte) 3);
			Sign sign = (Sign) s.getState();
			sign.setLine(0, "3rd");
			sign.setLine(1, leader3.name);
			sign.setLine(2, leader3.score + " Kills");
			sign.update();
		} else SkullUtils.PlaceSkull(leaderboard.clone().add(1, 1, 0), "Herobrine", f);
	}

	private void updateScore() {
		objective.getScore(redScore).setScore(red.getScore());
		objective.getScore(blueScore).setScore(blue.getScore());
	}

	public boolean canPlayerHunger(MiniGamePlayer p) {
		return false;
	}

	@Override
	public void onPlayerInteract(MiniGamePlayer gp, PlayerInteractEvent event) {
		if (!isStarted)
			return;
		ItemStack i = event.getItem();
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (i == null || i.getType() != Material.IRON_SWORD)
			return;
		BomberGamePlayer bp = (BomberGamePlayer) gp;
		if (!bp.canFireball())
			return;
		bp.fireball();
		Player player = event.getPlayer();
		Location loc = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(2))
				.toLocation(player.getWorld(), player.getLocation().getYaw(), player.getLocation().getPitch());
		player.getWorld().spawn(loc, Fireball.class).setShooter(player);
	}
}
