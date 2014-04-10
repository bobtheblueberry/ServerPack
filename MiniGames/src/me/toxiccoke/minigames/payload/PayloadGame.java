package me.toxiccoke.minigames.payload;

import java.util.ArrayList;
import java.util.LinkedList;

import me.toxiccoke.minigames.GameEndTimer;
import me.toxiccoke.minigames.GamePlayer;
import me.toxiccoke.minigames.MiniGamesPlugin;
import me.toxiccoke.minigames.team.TeamType;
import me.toxiccoke.minigames.team.TwoTeamGame;
import me.toxiccoke.tokenshop.TokenShop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class PayloadGame extends TwoTeamGame<PayloadPlayer, PayloadTeam> {

	protected LinkedList<PayloadPlayer>	players;
	private boolean						isStarted;
	private PayloadTeam					red, blue;
	private GameEndTimer				endTimer;
	private Location					minecartSpawn;
	protected Minetrackulator			trackulator;

	// blu spawn 1, blu spawn 2, red spawn

	Minecart							minecart;

	public PayloadGame(String worldName) {
		super("Payload", worldName);
		players = new LinkedList<PayloadPlayer>();
		trackulator = new Minetrackulator(this);
		trackulator.load();
		load();
		red = new PayloadTeam(this, TeamType.RED);
		blue = new PayloadTeam(this, TeamType.BLUE);
		initScoreboard();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(MiniGamesPlugin.plugin, new MinecartUpdater(this), 10, 5);
	}

	@Override
	public boolean allowDamage(GamePlayer gp) {
		return isStarted;
	}

	@Override
	public boolean canBreakBlock(GamePlayer p, BlockBreakEvent event) {
		return true;
	}

	@Override
	public boolean canExplodeBlock(Block b, Entity e) {
		return false;
	}

	@Override
	public boolean canPlaceBlock(GamePlayer p, BlockPlaceEvent event) {
		return true;
	}

	@Override
	public boolean canPlayerHunger(GamePlayer player) {
		return false;
	}

	private void checkNoPlayers() {
		if (players.size() > 1)
			balanceTeams();
		if (players.size() < 1)
			reset();
	}

	@Override
	protected void endGame() {
		for (PayloadPlayer p : players) {
			unPlayer(p);
		}
		reset();
	}

	@Override
	protected PayloadTeam getBlue() {
		return blue;
	}

	private ItemStack getItem(Material m, String name, String lore) {
		return getItem(m, name, lore, 0);
	}

	private ItemStack getItem(Material m, String name, String lore, int damage) {
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

	private int getPlayersWithinDistance(Location l, int dist) {
		int a = 0;
		for (PayloadPlayer player : players) {
			// world is not checked
			double distance = Minetrackulator.getDistance(player.getPlayer().getLocation(), l);
			if (distance <= dist)
				a++;
		}
		return a;
	}

	@Override
	protected PayloadTeam getRed() {
		return red;
	}

	@Override
	protected void initPlayer(PayloadPlayer player) {
		super.initPlayer(player);
		Player p = player.getPlayer();
		if (player.playerClass == PayloadClass.PYRO) {
			p.getInventory().addItem(
					getItem(Material.FIRE, ChatColor.RED + "Flame Thrower", ChatColor.GREEN + "10 Ammo per second"));
		} else if (player.playerClass == PayloadClass.ENGINEER) {
			p.getInventory()
					.addItem(
							getItem(Material.DISPENSER, ChatColor.RED + "Sentry Gun", ChatColor.GREEN
									+ "20 Arrows per Second"));
		} else if (player.playerClass == PayloadClass.MEDIC) {
			p.getInventory().addItem(
					getItem(Material.FISHING_ROD, ChatColor.RED + "Medi-Gun", ChatColor.GREEN + "1/2 Heart per Second",
							Short.MAX_VALUE));
		} else if (player.playerClass == PayloadClass.HEAVY) {
			p.getInventory().addItem(
					getItem(Material.BOW, ChatColor.RED + "Sasha", ChatColor.GREEN + "Fires 60 Rounds per Second",
							Short.MAX_VALUE));
		} else if (player.playerClass == PayloadClass.SNIPER) {
			p.getInventory().addItem(
					getItem(Material.BOW, ChatColor.RED + "Sniper Rifle", ChatColor.GREEN
							+ "Deals 20 Damage on head shots", Short.MAX_VALUE));
		} else if (player.playerClass == PayloadClass.SCOUT) {
			p.getInventory().addItem(
					getItem(Material.RED_ROSE, ChatColor.RED + "Scattergun", ChatColor.GREEN + "Lame", 2));
		}
		p.getInventory().addItem(new ItemStack(Material.COOKIE, 32));
	}

	@Override
	public boolean isFull() {
		return players.size() >= maxplayers;
	}

	@Override
	public boolean isJoinable() {
		return spawnLocations.size() > 2 && lobbyLocation != null;
	}

	@Override
	public boolean join(Player p) {
		PayloadPlayer plr = new PayloadPlayer(p, /* getTeam() */blue, PayloadClass.PYRO);

		players.add(plr);
		if (players.size() == minplayers)
			startGame();
		return true;
	}

	private void load() {
		YamlConfiguration yml = super.getLoadYML();
		if (yml.contains("minecart.world")) {
			minecartSpawn = new Location(Bukkit.getWorld(yml.getString("minecart.world")), yml.getDouble("minecart.x"),
					yml.getDouble("minecart.y"), yml.getDouble("minecart.z"));
		}
	}

	@Override
	public void notifyDeath(GamePlayer gp, Entity damager, DamageCause cause) {
		undeath(gp);
	}

	@Override
	public void notifyDeath(GamePlayer gp, EntityDamageEvent e) {
		undeath(gp);
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

	public void reset() {
		if (minecart != null)
			minecart.remove();
		minecart = null;
		if (endTimer != null)
			endTimer.cancelTimer();
		endTimer = null;
		updateScore();
		isStarted = false;
		players.clear();
		super.reset();
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
		super.save(yml);
	}

	private void setLore(ItemMeta is, String lore) {
		ArrayList<String> l = new ArrayList<String>(1);
		l.add(lore);
		is.setLore(l);
	}

	@Override
	protected void spawn(PayloadPlayer p) {
		if (spawnLocations.size() < 3)
			return;
		if (p.getTeam().team == TeamType.BLUE)
			TokenShop.teleportAdvanced(p.getPlayer(), spawnLocations.get(0));
		else TokenShop.teleportAdvanced(p.getPlayer(), spawnLocations.get(2));
	}

	private void startGame() {
		isStarted = true;
		sendPlayersMessage(ChatColor.GOLD + "Game Started");
		initMinecart();
		endTimer = new GameEndTimer(this, 10);
		for (PayloadPlayer p : players) {
			p.getPlayer().setScoreboard(board);
			initPlayer(p);
			spawn(p);
		}
	}

	private void undeath(GamePlayer p) {
		sendPlayersMessage(ChatColor.DARK_GRAY + p.getName() + " died.");
		spawn((PayloadPlayer) p);
	}

	private void unPlayer(GamePlayer gp) {
		gp.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		removePlayerFromScoreboard((PayloadPlayer) gp);
		gp.restorePlayer();
		checkNoPlayers();
		updateScore();
	}

	private void initMinecart() {
		if (minecartSpawn == null)
			return;
		minecart = (Minecart) minecartSpawn.getWorld().spawnEntity(minecartSpawn, EntityType.MINECART_MOB_SPAWNER);
		minecart.setSlowWhenEmpty(true);
	}

	protected void updateCart() {
		if (!isStarted || minecart == null)
			return;
		double dist = getPlayersWithinDistance(minecart.getLocation(), 3);
		Vector v;
		if (dist > 0)
			v = trackulator.getVector(minecart.getLocation(), dist / 10);
		else {
			if (trackulator.headingTowardsEnd(minecart))
				v = trackulator.getVector(minecart.getLocation(), minecart.getVelocity().length() * 0.5);
			else return;
		}
		minecart.setVelocity(v);
	}

	public void vehicleCollision(VehicleEntityCollisionEvent event) {
		if (event.getVehicle() != minecart)
			 return;
		event.setCancelled(true);
		event.setCollisionCancelled(true);
		event.setPickupCancelled(true);
	}

	public void vehicleCreated(VehicleCreateEvent event) {
		if (event.getVehicle() instanceof Minecart) {
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
		minecart.setDerailedVelocityMod(new Vector());
	}

	@Override
	protected boolean isStarted() {
		return isStarted;
	}

	@Override
	protected void updateArmor(PayloadPlayer player) {
		// TODO Auto-generated method stub

	}
}
