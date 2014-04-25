package me.toxiccoke.minigames.bomber;

import java.util.LinkedList;

import me.toxiccoke.minigames.Bounds;
import me.toxiccoke.minigames.GameEndTimer;
import me.toxiccoke.minigames.GamePlayer;
import me.toxiccoke.minigames.MiniGamesPlugin;
import me.toxiccoke.minigames.team.TeamType;
import me.toxiccoke.minigames.team.TwoTeamGame;
import me.toxiccoke.tokenshop.TokenShop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class BomberGame extends TwoTeamGame<BomberPlayer, BomberTeam> {

	protected LinkedList<BomberPlayer>	players;
	private BomberLobbyTimer			lobbyTimer;
	private GameEndTimer				endTimer;
	boolean								isStarted;
	private BomberTeam					red, blue;
	private volatile BomberPlayer		bomber;
	private int							gamelength	= 10;

	// spawn is blue,red,(blue,red)
	public BomberGame(String worldName) {
		super("Bomber", worldName);
		load();
		players = new LinkedList<BomberPlayer>();
		red = new BomberTeam(this, TeamType.RED);
		blue = new BomberTeam(this, TeamType.BLUE);
		initScoreboard();
	}

	@Override
	public boolean allowDamage(GamePlayer gp, EntityDamageByEntityEvent event) {
		if (!isStarted)
			gp.getPlayer().sendMessage(ChatColor.GOLD + "You cannot attack here!");
		return isStarted;
	}

	@Override
	public boolean canBreakBlock(GamePlayer p, BlockBreakEvent event) {
		return canDestroy(event.getBlock().getType(), event.getBlock().getLocation());
	}

	private boolean canDestroy(Material m, Location l) {

		if (m == Material.LEAVES || m == Material.LEAVES_2 || m == Material.GLOWSTONE || m == Material.WOOL || m == Material.LADDER)
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
	public boolean canPlaceBlock(GamePlayer p, BlockPlaceEvent event) {
		return isStarted && event.getBlock().getLocation().getY() < heightLimit;
	}

	private void checkNoPlayers() {
		if (players.size() > 1) {
			balanceTeams();
		}
		if (players.size() < 1)
			reset();
	}

	@SuppressWarnings("deprecation")
	protected void initPlayer(BomberPlayer plr) {
		super.initPlayer(plr);
		Player p = plr.getPlayer();
		Inventory i = p.getInventory();
		ItemStack bow = new ItemStack(Material.BOW, 1);
		bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		bow.addEnchantment(Enchantment.DURABILITY, 3);
		ItemStack[] s = new ItemStack[] { new ItemStack(Material.IRON_SWORD, 1), bow,
				new ItemStack(Material.ARROW, 64) };
		i.addItem(s);
		updateArmor(plr);
		p.updateInventory();
		p.setGameMode(GameMode.ADVENTURE);
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

	protected void endGame() {
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
		for (BomberPlayer plr : players) {
			Player p = plr.getPlayer();
			if (plr.team.team == TeamType.RED)
				p.sendMessage(ChatColor.GOLD + redMsg);
			else p.sendMessage(ChatColor.GOLD + bluMsg);
			checkLeader(plr);
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			plr.restorePlayer();
			removePlayerFromScoreboard(plr);
			plr.leaveGame();
		}
		players.clear();
		updateScore();
		reset();
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

	@Override
	public int getPlayerCount() {
		return players.size();
	}

	@Override
	public LinkedList<? extends BomberPlayer> getPlayers() {
		return players;
	}

	public void spawn(BomberPlayer p) {
		if (!isStarted) {
			TokenShop.teleportAdvanced(p.getPlayer(), lobbyLocation);
			return;
		}
		Location l;
		int i;
		if (p.getTeam().team == TeamType.BLUE)
			i = 0;
		else i = 1;
		// allow 2 spawn locations
		if (spawnLocations.size() > 3)
			if (Math.random() < 0.5)
				i += 2;
		l = spawnLocations.get(i);
		TokenShop.teleportAdvanced(p.getPlayer(), l);
	}

	@Override
	public boolean isJoinable() {
		return spawnLocations.size() >= 2 && lobbyLocation != null;
	}

	@Override
	public boolean join(Player p) {
		if (lobbyLocation == null || spawnLocations.size() < 2 || p == null)
			return false;
		boolean isInGame = false;
		for (BomberPlayer bp : players)
			if (bp.equals(p)) {
				isInGame = true;
				break;
			}

		BomberPlayer bgp;
		boolean joined = false;
		if (!isInGame) {
			if (players.size() >= maxplayers)
				return false;
			BomberTeam t = (BomberTeam) getTeam();
			bgp = new BomberPlayer(this, p, t);
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
		super.getLoadYML();
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
	public void notifyDeath(GamePlayer gp, Entity damager, DamageCause cause) {
		Player p = gp.getPlayer();
		String enemy = null;
		if (damager instanceof Player) {
			Player dmg = (Player) damager;
			p.sendMessage(ChatColor.GOLD + "Killed by " + dmg.getDisplayName());
			if (getPlayer(dmg.getName()).team.team == ((BomberPlayer) gp).team.team)
				return; // 'friendly' fire

			doKillPoints(dmg, p, true);
			enemy = dmg.getName();
		} else if (damager instanceof Projectile) {
			Projectile dmg = (Projectile) damager;
			if (dmg.getShooter() instanceof Player) {
				Player dmger = (Player) dmg.getShooter();
				if (dmger.equals(gp)) {
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
		spawn((BomberPlayer) gp);
		sendPlayersMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + p.getName() + " was killed by " + enemy);
	}

	@Override
	public void notifyDeath(GamePlayer gp, EntityDamageEvent e) {
		Player p = gp.getPlayer();
		String cause = e.getCause().toString();
		boolean custom = false;
		if ((e.getCause() == DamageCause.BLOCK_EXPLOSION || e.getCause() == DamageCause.PROJECTILE)) {
			if (bomber != null) {
				if (!bomber.equals(gp)) {
					cause = bomber.getName() + "'s explosive arrow";
					if (bomber.team.team != ((BomberPlayer) gp).team.team) {
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
		spawn((BomberPlayer) gp);
		undeath(p);
		if (custom)
			sendPlayersMessage(ChatColor.GRAY + p.getName() + cause);
		else sendPlayersMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + p.getName() + " was killed by " + cause);
	}

	@Override
	public void notifyLeaveCommand(GamePlayer player) {
		Player p = player.getPlayer();
		p.sendMessage(ChatColor.GOLD + "Leaving Bomber");
		removePlayer(player);
		player.leaveGame();
	}

	@Override
	public void notifyQuitGame(GamePlayer gp) {
		removePlayer(gp);
		gp.leaveGame();
	}

	@Override
	public void projectileHit(GamePlayer p, ProjectileHitEvent event) {
		if (!isStarted)
			return;
		Arrow arrow = (Arrow) event.getEntity();
		float random = (float) arrow.getVelocity().length() / 3;
		if (Math.random() * 2 < 1) {
			if (Math.random() * 10 < 1)
				random = 2 + (float) Math.random();
			bomber = (BomberPlayer) p;
			arrow.getWorld().createExplosion(arrow.getLocation(), random, Math.random() * 6 < 1);
		}
		return;

	}

	private void removePlayer(GamePlayer gp) {
		players.remove(gp);
		gp.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		removePlayerFromScoreboard((BomberPlayer) gp);
		gp.restorePlayer();
		checkNoPlayers();
		updateScore();
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
		// add other stuff here
		super.save(yml);
	}

	private void setPlayerXp(int levels) {
		for (BomberPlayer p : players)
			p.getPlayer().setLevel(levels);
	}

	private void startGame() {
		if (isStarted)
			return;
		isStarted = true;
		for (BomberPlayer p : players) {
			Player pl = p.getPlayer();
			pl.setScoreboard(board);
			pl.setFoodLevel(20);
			p.startGame();
			spawn(p);
		}
		sendPlayersMessage(ChatColor.YELLOW + "Game Started!");
		endTimer = new GameEndTimer(this, gamelength);
	}

	private void undeath(final Player p) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, new Runnable() {

			@Override
			public void run() {
				p.setHealth(((Damageable) p.getPlayer()).getMaxHealth());
				p.setFoodLevel(20);
				p.setFireTicks(0);
			}
		}, 4);
	}

	protected void updateArmor(BomberPlayer p) {
		ItemStack[] is = getColoredArmor((p.getTeam().team == TeamType.BLUE) ? 0 : 255, 0,
				(p.getTeam().team == TeamType.BLUE) ? 255 : 0);
		p.getPlayer().getInventory().setArmorContents(is);
	}

	@Override
	public boolean canPlayerHunger(GamePlayer p) {
		return false;
	}

	@Override
	public void onPlayerInteract(GamePlayer gp, PlayerInteractEvent event) {
		if (!isStarted)
			return;
		ItemStack i = event.getItem();
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (i == null || i.getType() != Material.IRON_SWORD)
			return;
		BomberPlayer bp = (BomberPlayer) gp;
		if (!bp.canFireball())
			return;
		bp.fireball();
		Player player = event.getPlayer();
		Location loc = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(2))
				.toLocation(player.getWorld(), player.getLocation().getYaw(), player.getLocation().getPitch());
		player.getWorld().spawn(loc, Fireball.class).setShooter(player);
	}

	@Override
	protected BomberTeam getRed() {
		return red;
	}

	@Override
	protected BomberTeam getBlue() {
		return blue;
	}

	@Override
	protected boolean isStarted() {
		return isStarted;
	}
}
