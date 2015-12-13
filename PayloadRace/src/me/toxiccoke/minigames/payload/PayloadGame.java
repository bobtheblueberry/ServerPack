package me.toxiccoke.minigames.payload;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import me.toxiccoke.minigames.GameEndTimer;
import me.toxiccoke.minigames.GamePlayer;
import me.toxiccoke.minigames.MiniGamesPlugin;
import me.toxiccoke.minigames.payload.CustomDamager.FakeEntity;
import me.toxiccoke.minigames.team.TeamType;
import me.toxiccoke.minigames.team.TwoTeamGame;
import me.toxiccoke.minigames.util.BarAPI;

public class PayloadGame extends TwoTeamGame<PayloadPlayer, PayloadTeam> {

	private static final int			GAME_TIME		= 10;																			// minutes
	private static final double			MINECART_SPEED	= 0.1;
	private static final int			ENDGAME_TIME	= 10;																			// seconds
	protected LinkedList<PayloadPlayer>	players;
	private boolean						isStarted;
	private PayloadTeam					red, blue;
	private GameEndTimer				endTimer;
	private ItemRefresher				itemRefresher;
	private Location					minecartSpawn;
	protected Minetrackulator			trackulator;
	protected ArrayList<Location>		checkpoints, bars, healthpacks, ammopacks;
	protected ItemPack[]				healthitems, ammoitems;
	protected boolean[]					checkedpoints;
	private String						bossBarTitle	= ChatColor.GREEN + "Payload Cart " + ChatColor.GRAY + "» " + ChatColor.YELLOW;
	protected SetupTimer				setup;
	private EndofGameTimer				EOGTimer;
	protected LinkedList<Bullet>		bullets;
	private TickUpdater				tickUpdater;

	// blu spawn 1, blu spawn 2, red spawn

	Minecart							minecart;

	public PayloadGame(String worldName) {
		super("Payload", worldName);
		players = new LinkedList<PayloadPlayer>();
		checkpoints = new ArrayList<Location>();
		bars = new ArrayList<Location>();
		trackulator = new Minetrackulator(this);
		trackulator.load();
		load();
		red = new PayloadTeam(this, TeamType.RED);
		blue = new PayloadTeam(this, TeamType.BLUE);
		initScoreboard();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(MiniGamesPlugin.plugin, new MinecartUpdater(this), 10, 5);
		bullets = new LinkedList<Bullet>();
	}

	@Override
	public boolean allowDamage(GamePlayer gp, EntityDamageByEntityEvent event) {
		if (!(gp instanceof PayloadPlayer))
			return false;
		PayloadPlayer pp = (PayloadPlayer) gp;
		if (!gp.dead && !pp.team.lost && pp.getPlayerClass() == PayloadClass.SCOUT) {
			event.setDamage(2);
			return true;
		}
		Entity e = event.getDamager();
		if (e instanceof HumanEntity && !(e instanceof Player)) { return true; }
		return false;
	}

	@Override
	public boolean canBreakBlock(GamePlayer p, BlockBreakEvent event) {
		return false;
	}

	@Override
	public boolean canExplodeBlock(Block b, Entity e) {
		return false;
	}

	@Override
	public boolean canPlaceBlock(GamePlayer p, BlockPlaceEvent event) {
		return false;
	}

	@Override
	public boolean canPlayerHunger(GamePlayer player) {
		return false;
	}

	public void bowShoot(GamePlayer player, EntityShootBowEvent event) {
		event.setCancelled(true);
	}

	private void checkNoPlayers() {
		if (players.size() > 1)
			balanceTeams();
		if (players.size() < 1)
			reset();
	}

	@Override
	protected void endGame() {
		attackLost();
	}

	protected void actuallyEndGame() {
		for (PayloadPlayer p : players) {
			unPlayer(p);
		}
		players.clear();
		reset();
	}

	@Override
	protected PayloadTeam getBlue() {
		return blue;
	}

	private ItemStack getItem(Material m, String name, String... lore) {
		return getItem(m, 0, name, lore);
	}

	private ItemStack getItem(Material m, int damage, String name, String... lore) {
		ItemStack is = new ItemStack(m, 1, (short) damage);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		setLore(im, lore);
		is.setItemMeta(im);
		return is;
	}

	@Override
	public int getPlayerCount() {
		return players.size();
	}

	@Override
	public LinkedList<PayloadPlayer> getPlayers() {
		return players;
	}

	private ArrayList<PayloadPlayer> getBluPlayersWithinDistance(Location l, double dist) {
		ArrayList<PayloadPlayer> list = new ArrayList<PayloadPlayer>();
		for (PayloadPlayer player : players) {
			if (player.team.team == TeamType.RED || player.dead)
				continue;
			double distance = Minetrackulator.getDistance(player.getPlayer().getLocation(), l);
			if (distance <= dist)
				list.add(player);
		}
		return list;
	}

	@Override
	protected PayloadTeam getRed() {
		return red;
	}

	@Override
	protected void initPlayer(PayloadPlayer player) {
		super.initPlayer(player);
		Player p = player.getPlayer();
		updateArmor(player);
		p.setGameMode(GameMode.ADVENTURE);
		changeClass(player);
		player.setAmmo(300);
	}

	@Override
	public boolean isFull() {
		return players.size() >= maxplayers;
	}

	@Override
	public boolean isJoinable() {
		return spawnLocations.size() > 2 && lobbyLocation != null && EOGTimer == null;
	}

	@Override
	public boolean join(final Player p) {
		if (EOGTimer != null) {
			p.sendMessage(ChatColor.GOLD + "Wait for round to finish before joining");
			return false;
		}
		PayloadPlayer plr = new PayloadPlayer(this, p, (players.size() == 0) ? blue : getTeam(), PayloadClass.SOLDIER);
		players.add(plr);
		joinPlayer(plr);
		if (!isStarted && players.size() == minplayers)
			startGame();
		Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, new Runnable() {
			@Override
			public void run() {
				p.openInventory(ClassMenu.getMenu());
			}
		}, 4);
		return true;
	}

	// red lost
	private void defendLost() {
		if (blue.lost)
			return; // too late
		notifyWinningTeam(TeamType.BLUE);
		red.lost = true;
		endGame(ENDGAME_TIME);
	}

	// blue lost
	private void attackLost() {
		notifyWinningTeam(TeamType.RED);
		blue.lost = true;
		EOGTimer = new EndofGameTimer(this, ENDGAME_TIME);
	}

	private void endGame(int seconds) {
		endTimer.cancelTimer();
		EOGTimer = new EndofGameTimer(this, ENDGAME_TIME);
	}

	private void notifyWinningTeam(TeamType t) {
		for (PayloadPlayer p : players) {
			Player pl = p.getPlayer();
			if (p.team.team == t) {
				pl.sendMessage(ChatColor.DARK_RED + "===============");
				pl.sendMessage(ChatColor.RED + "Mission Accomplished!");
				pl.sendMessage(ChatColor.DARK_RED + "===============");
			} else {
				pl.sendMessage(ChatColor.DARK_BLUE + "===============");
				pl.sendMessage(ChatColor.BLUE + "Mission Failed!");
				pl.sendMessage(ChatColor.DARK_BLUE + "===============");
				pl.getInventory().clear();
			}
		}
	}

	private void load() {
		YamlConfiguration yml = super.getLoadYML();
		if (yml.contains("minecart.world")) {
			minecartSpawn = new Location(Bukkit.getWorld(yml.getString("minecart.world")), yml.getDouble("minecart.x"), yml.getDouble("minecart.y"), yml.getDouble("minecart.z"));
		}
		checkpoints = getLocations(yml, "checkpoint");
		checkedpoints = new boolean[checkpoints.size()];
		bars = getLocations(yml, "bar");
		healthpacks = getLocations(yml, "healthpack");
		ammopacks = getLocations(yml, "ammopack");
	}

	private ArrayList<Location> getLocations(YamlConfiguration yml, String name) {
		World w = null;
		ArrayList<Location> list = new ArrayList<Location>();
		int bp = yml.getInt(name + ".size");
		for (int i = 0; i < bp; i++) {
			if (i == 0)
				w = Bukkit.getWorld(yml.getString(name + ".world"));
			list.add(new Location(w, yml.getDouble(name + "." + i + ".x"), yml.getDouble(name + "." + i + ".y"), yml.getDouble(name + "." + i + ".z")));
		}
		return list;
	}

	@Override
	public void notifyDeath(GamePlayer gp, Entity damager, DamageCause cause) {
		PayloadPlayer p = (PayloadPlayer) gp;
		if (damager instanceof HumanEntity && !(damager instanceof Player)) {
			CraftHumanEntity ch = (CraftHumanEntity) damager;
			Field f;
			PayloadPlayer d = null;
			try {
				f = ch.getClass().getSuperclass().getSuperclass().getDeclaredField("entity");
				f.setAccessible(true);
				FakeEntity e = (FakeEntity) f.get(ch);
				d = e.getDamager();
			} catch (NoSuchFieldException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			}
			if (d == null) {
				System.out.println("error! notify death/damager is null");
				return;
			}
			sendPlayersMessage(ChatColor.DARK_GRAY + gp.getName() + " was killed by " + d.getName());
			d.getPlayer().sendMessage(ChatColor.GOLD + "You killed " + p.getName());
			if (d.team.team != p.team.team) {
				d.addScore(1);
				updateScore();
			} else {
				sendPlayersMessage(ChatColor.DARK_GRAY + gp.getName() + " was killed by friendly fire");
			}
		} else if (damager instanceof Player) {
			PayloadPlayer pp = getPlayer(((Player) damager).getName());
			if (pp == null) {
				sendPlayersMessage(ChatColor.DARK_GRAY + gp.getName() + " was killed by hacker " + ((Player) damager).getName());
			} else {
				if (pp.team.team != p.team.team) {
					pp.addScore(1);
					updateScore();
				}
				sendPlayersMessage(ChatColor.DARK_GRAY + gp.getName() + " was killed by " + pp.getName());
				pp.getPlayer().sendMessage(ChatColor.GOLD + "You killed " + p.getName());
				updateScore();
			}
		} else {
			sendPlayersMessage(ChatColor.DARK_GRAY + gp.getName() + " was killed by Herobrine");
		}
		death(gp);
	}

	@Override
	public void notifyDeath(GamePlayer gp, EntityDamageEvent e) {
		PayloadPlayer pp = (PayloadPlayer) gp;
		if (e.getCause() == DamageCause.FIRE_TICK && pp.getBurner() != null) {
			// pyro
			if (pp.team.team != pp.getBurner().team.team) {
				pp.getBurner().addScore(1);
				updateScore();
			}
			sendPlayersMessage(ChatColor.DARK_GRAY + gp.getName() + " was toasted by " + pp.getBurner().getName());
			pp.getBurner().getPlayer().sendMessage(ChatColor.GOLD + "You killed " + gp.getName());
			updateScore();
		} else if (e.getCause() == DamageCause.FIRE || e.getCause() == DamageCause.LAVA || e.getCause() == DamageCause.FIRE_TICK) {
			sendPlayersMessage(ChatColor.DARK_GRAY + gp.getName() + " was burned to death");
		} else sendPlayersMessage(ChatColor.DARK_GRAY + gp.getName() + " was killed by " + e.getCause().toString().substring(0, 1).toUpperCase() + e.getCause().toString().substring(1).toLowerCase());
		death(gp);
	}

	@Override
	public void notifyLeaveCommand(GamePlayer gp) {
		gp.getPlayer().sendMessage(ChatColor.GOLD + "Leaving " + worldName);
		players.remove(gp);
		unPlayer(gp);
	}

	@Override
	public void notifyQuitGame(GamePlayer gp) {
		players.remove(gp);
		unPlayer(gp);
	}

	@Override
	public void reset() {
		for (ItemPack p : ammoitems)
			if (p != null)
				p.remove();
		for (ItemPack p : healthitems)
			if (p != null)
				p.remove();
		if (setup != null)
			setup.cancel();
		if (minecart != null)
			minecart.remove();
		minecart = null;
		if (endTimer != null)
			endTimer.cancelTimer();
		if (itemRefresher != null)
			itemRefresher.cancel();
		if (tickUpdater != null)
			tickUpdater.cancel();
		EOGTimer = null;
		tickUpdater = null;
		bullets.clear();
		itemRefresher = null;
		endTimer = null;
		red.lost = false;
		blue.lost = false;
		updateScore();
		isStarted = false;
		players.clear();
		for (Location l : bars)
			l.getBlock().setType(Material.IRON_FENCE);
		checkedpoints = new boolean[checkpoints.size()];
	}

	@Override
	public void save() {
		YamlConfiguration yml = super.getSaveYML();
		if (minecartSpawn != null) {
			yml.set("minecart.world", minecartSpawn.getWorld().getName());
			yml.set("minecart.x", minecartSpawn.getX());
			yml.set("minecart.y", minecartSpawn.getY());
			yml.set("minecart.z", minecartSpawn.getZ());
		}
		saveLocations(yml, checkpoints, "checkpoint");
		saveLocations(yml, bars, "bar");
		saveLocations(yml, healthpacks, "healthpack");
		saveLocations(yml, ammopacks, "ammopack");

		super.save(yml);
	}

	private void saveLocations(YamlConfiguration yml, List<Location> list, String name) {
		for (int i = 0; i < list.size(); i++) {
			Location l = list.get(i);
			yml.set(name + ".size", list.size());
			if (i == 0)
				yml.set(name + ".world", l.getWorld().getName());
			yml.set(name + "." + i + ".x", l.getX());
			yml.set(name + "." + i + ".y", l.getY());
			yml.set(name + "." + i + ".z", l.getZ());
		}
	}

	private void setLore(ItemMeta is, String... lore) {
		ArrayList<String> l = new ArrayList<String>(lore.length);
		for (String ll : lore)
			l.add(ll);
		is.setLore(l);
	}

	@Override
	protected void spawn(PayloadPlayer p) {
		if (spawnLocations.size() < 3)
			return;
		if (p.getTeam().team == TeamType.BLUE)
			p.getPlayer().teleport(spawnLocations.get(0));
		else p.getPlayer().teleport(spawnLocations.get(2));
	}

	private void startGame() {
		isStarted = true;
		sendPlayersMessage(ChatColor.GOLD + "Game Started");
		initMinecart();
		endTimer = new GameEndTimer(this, GAME_TIME);
		sendPlayersMessage(ChatColor.GREEN + "Setup ends in 10 seconds");
		setup = new SetupTimer(this, 10);
		tickUpdater = new TickUpdater(this);
		// init health/ammo kits
		healthitems = new ItemPack[healthpacks.size()];
		ammoitems = new ItemPack[ammopacks.size()];
		for (int i = 0; i < healthpacks.size(); i++)
			healthitems[i] = new ItemPack(healthpacks.get(i), Material.GOLDEN_APPLE, 1);
		for (int i = 0; i < ammopacks.size(); i++)
			ammoitems[i] = new ItemPack(ammopacks.get(i), Material.ARROW, 0, 24);
	}

	private void joinPlayer(PayloadPlayer p) {
		Player plr = p.getPlayer();
		plr.setScoreboard(board);
		initPlayer(p);
		spawn(p);
		BarAPI.displayText(bossBarTitle, plr, 0);
	}

	/**
	 * Called after the red team's setup time is finished
	 */
	protected void setupDone() {
		for (Location l : bars)
			l.getBlock().setType(Material.AIR);
		sendPlayersMessage(ChatColor.RED + "Game has started!");
	}

	private int getCheckpointsDone() {
		int i = 0;
		for (boolean b : checkedpoints)
			if (b)
				i++;
		return i;
	}

	private void death(GamePlayer p) {
		PayloadPlayer plr = (PayloadPlayer) p;
		plr.dead = true;
		final Player player = p.getPlayer();
		// remove potions
		for (PotionEffect t : player.getActivePotionEffects())
			player.removePotionEffect(t.getType());
		// make them invisible
		for (PayloadPlayer pp : players) {
			if (pp == p)
				continue;
			pp.getPlayer().hidePlayer(player);
		}
		player.setHealth(20);
		player.setFireTicks(0);

		player.sendMessage(ChatColor.GOLD + "You Died!");
		player.sendMessage(ChatColor.GOLD + "Respawning in 15 seconds.");
		new RespawnTimer(plr, 15);
	}

	private void unPlayer(GamePlayer gp) {
		gp.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		removePlayerFromScoreboard((PayloadPlayer) gp);
		gp.restorePlayer();
		checkNoPlayers();
		updateScore();
		BarAPI.destroy(gp.getPlayer());
		gp.leaveGame();
	}

	private void initMinecart() {
		if (minecartSpawn == null)
			return;
		minecart = (Minecart) minecartSpawn.getWorld().spawnEntity(minecartSpawn, EntityType.MINECART_TNT);
		minecart.setSlowWhenEmpty(true);
	}

	private void cartTick(PayloadPlayer player) {
		if (player.dead)
			return;
		Player p = player.getPlayer();
		int ticks = p.getFireTicks();
		if (ticks > 0)
			p.setFireTicks(Math.max(0, ticks - 3));
		if (p.getHealth() < 20)
			p.setHealth(Math.min(p.getHealth() + 0.1, p.getMaxHealth()));
		if (Math.random() * 2 < 1)
			player.setAmmo(player.getAmmo() + 1);
	}

	Location	oldMinecartLocation;

	protected void updateCart() {
		if (!isStarted || minecart == null || blue.lost)
			return;
		if (setup != null && setup.count > 0)
			return;
		ArrayList<PayloadPlayer> list = getBluPlayersWithinDistance(minecart.getLocation(), 3);
		double dist = list.size();
		for (PayloadPlayer p : list)
			cartTick(p);
		int h = (int) (trackulator.getCartPosition(minecart) * 200);
		for (PayloadPlayer p : players)
			BarAPI.setBarHealth(p.getPlayer(), h);

		Vector v = minecart.getVelocity();
		if (dist > 0)
			v = trackulator.getVector(minecart.getLocation(), dist * MINECART_SPEED);
		else if (trackulator.headingTowardsEnd(minecart))
			v = trackulator.getVector(minecart.getLocation(), minecart.getVelocity().length() * 0.5);

		// Hack fix to keep minecarts from being blocked by players
		if (list.size() > 0 && oldMinecartLocation != null && minecart.getLocation().equals(oldMinecartLocation))
			minecart.teleport(minecart.getLocation().add(v.clone().multiply(5)), TeleportCause.PLUGIN);

		minecart.setVelocity(v);
		oldMinecartLocation = minecart.getLocation();

	}

	protected void checkpoint(Location l) {
		// check waypoints
		for (int i = 0; i < checkpoints.size(); i++)
			if (l.equals(checkpoints.get(i))) {
				checkedpoints[i] = true;
				break;
			}

		sendPlayersMessage(ChatColor.GREEN + "Checkpoint reached. Time has been added");
		endTimer.cancelTimer();
		endTimer = new GameEndTimer(this, endTimer.getCountdown() + 2);
		String title = bossBarTitle + getCheckpointsDone();
		for (PayloadPlayer p : players)
			BarAPI.setBarText(p.getPlayer(), title);
	}

	public void vehicleCollision(VehicleEntityCollisionEvent event) {
		if (event.getVehicle() != minecart)
			return;
		event.setCancelled(true);
	}

	public void vehicleCreated(VehicleCreateEvent event) {
		if (event.getVehicle() instanceof Minecart && event.getVehicle() != minecart) {
			minecartSpawn = event.getVehicle().getLocation();
			save();
		}
	}

	public void vehicleDestroy(VehicleDestroyEvent event) {
		if (event.getVehicle() == minecart)
			event.setCancelled(true);
	}

	public void vehicleDamage(VehicleDamageEvent event) {
		if (event.getVehicle() == minecart)
			event.setCancelled(true);
	}

	public void vehicleUpdate(VehicleUpdateEvent event) {
		if (event.getVehicle() != minecart)
			return;
	}

	public void vehicleMove(VehicleMoveEvent event) {
		if (event.getVehicle() != minecart)
			return;
	}

	public boolean canPlayerHealFromHunger(GamePlayer p) {
		return false;
	}

	public void pickupItem(PayloadPlayer p, PlayerPickupItemEvent event) {
		Material type = event.getItem().getItemStack().getType();
		if (type == Material.GOLDEN_APPLE) {
			Player pl = event.getPlayer();
			pl.setHealth(Math.min(pl.getHealth() + 8, pl.getMaxHealth()));
			pl.setFireTicks(0);
			respawnHealthItem(event.getItem());
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.AQUA + "\u2665\u2665\u2665\u2665 Health added!");
		} else if (type == Material.ARROW) {
			respawnAmmoItem(event.getItem());
		}

	}

	private void respawnHealthItem(Item item) {
		for (ItemPack p : healthitems)
			if (p.getItem().equals(item)) {
				respawnLater(p);
				return;
			}
	}

	private void respawnAmmoItem(Item item) {
		for (ItemPack p : ammoitems)
			if (p.getItem().equals(item)) {
				respawnLater(p);
				return;
			}
	}

	private void respawnLater(final ItemPack p) {
		p.remove();
		p.respawning = true;
		Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, new Runnable() {

			@Override
			public void run() {
				p.respawn();
			}
		}, 400);
	}

	@Override
	protected boolean isStarted() {
		return isStarted;
	}

	@Override
	protected void updateArmor(PayloadPlayer p) {
		ItemStack[] is = getColoredArmor((p.getTeam().team == TeamType.BLUE) ? 0 : 255, 0, (p.getTeam().team == TeamType.BLUE) ? 255 : 0);
		p.getPlayer().getInventory().setArmorContents(is);
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
		lam.setColor(Color.fromRGB((r == 0) ? (int) (Math.random() * 256) : r, (g == 0) ? (int) (Math.random() * 256) : g, (b == 0) ? (int) (Math.random() * 256) : b));
		is[3].setItemMeta(lam);
		return is;
	}

	public void respawn(final PayloadPlayer payloadPlayer) {
		spawn(payloadPlayer);
		// wait 4 ticks for the teleportation to work (Hats slow tping down)
		Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, new Runnable() {

			@Override
			public void run() {
				Player p = payloadPlayer.getPlayer();
				payloadPlayer.dead = false;
				payloadPlayer.respawning = false;
				for (PayloadPlayer pp : players) {
					if (pp == payloadPlayer)
						continue;
					pp.getPlayer().showPlayer(p);
				}
				if (payloadPlayer.classChange) {
					changeClass(payloadPlayer);
				}
				payloadPlayer.setAmmo(100);
				if (payloadPlayer.getPlayerClass() == PayloadClass.SCOUT)
					doScoutPotions(p);
			}
		}, 4);
	}

	private void changeClass(PayloadPlayer player) {
		player.getPlayer().getInventory().clear();
		Player p = player.getPlayer();

		for (PotionEffect t : p.getActivePotionEffects())
			p.removePotionEffect(t.getType());
		if (player.classChange)
			player.setPlayerClass(player.tempClass);
		player.tempClass = null;
		player.classChange = false;
		p.sendMessage(ChatColor.GREEN + "You are now playing as " + player.getPlayerClass().toString().substring(0, 1) + player.getPlayerClass().toString().substring(1).toLowerCase());
		if (player.getPlayerClass() == PayloadClass.PYRO) {
			p.getInventory().addItem(
					getItem(Material.FIRE, ChatColor.RED + "Flame Thrower", ChatColor.RED + "Left Click: Airblast", ChatColor.RED + "Right Click: Burn nearby enemies", ChatColor.GRAY
							+ "2 Ammo per second"));
		} else if (player.getPlayerClass() == PayloadClass.ENGINEER) {
			p.getInventory().addItem(getItem(Material.DISPENSER, ChatColor.RED + "Sentry Gun", ChatColor.GREEN + "20 Arrows per Second"));
		} else if (player.getPlayerClass() == PayloadClass.MEDIC) {
			p.getInventory().addItem(getItem(Material.FISHING_ROD, ChatColor.RED + "Medi-Gun", ChatColor.GREEN + "1/2 Heart per Second"));
		} else if (player.getPlayerClass() == PayloadClass.SOLDIER) {
			p.getInventory().addItem(getItem(Material.HOPPER, ChatColor.RED + "Rocket Launcher", ChatColor.GREEN + "Blast 'em"));
		} else if (player.getPlayerClass() == PayloadClass.SNIPER) {
			p.getInventory().addItem(getItem(Material.BOW, ChatColor.RED + "Sniper Rifle", ChatColor.GREEN + "Deals 20 Damage on head shots"));
		} else if (player.getPlayerClass() == PayloadClass.SCOUT) {
			p.getInventory().addItem(getItem(Material.DIAMOND_SPADE, 0, ChatColor.RED + "Scout Shovel", ChatColor.GREEN + "Lame"));
			doScoutPotions(p);
		}

		p.getInventory().setItem(8, getItem(Material.COMMAND, ChatColor.YELLOW + "Change Class", ChatColor.GREEN + "Get Classy"));
	}

	private void doScoutPotions(Player p) {
		p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, 1));
		p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 4));
		p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 4));
	}

	public boolean canPlayerTakeDamage(PayloadPlayer gp) {
		if (gp.dead) // bug workaround BUKKIT-4890
			gp.getPlayer().setFireTicks(0);
		return !gp.dead;
	}

	public boolean canPlayerMove(PayloadPlayer gp) {
		if (gp.respawning && ((Entity) gp.getPlayer()).isOnGround())
			return false;
		return true;
	}

	public boolean canPlayerCombust(PayloadPlayer gp) {
		return !gp.dead;
	}

	public void explode(EntityExplodeEvent event) {
		if (event.getEntity() != minecart)
			return;
		defendLost();
	}

	public void dropItem(PayloadPlayer pl, PlayerDropItemEvent e) {
		e.setCancelled(true);
	}
}
