package me.toxiccoke.minigames.payload;

import java.util.ArrayList;
import java.util.LinkedList;

import me.toxiccoke.minigames.GameEndTimer;
import me.toxiccoke.minigames.GamePlayer;
import me.toxiccoke.minigames.MiniGamesPlugin;
import me.toxiccoke.minigames.team.TeamType;
import me.toxiccoke.minigames.team.TwoTeamGame;
import me.toxiccoke.minigames.util.BarAPI;
import me.toxiccoke.tokenshop.TokenShop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class PayloadGame extends TwoTeamGame<PayloadPlayer, PayloadTeam> {

	private static final double			MINECART_SPEED	= 0.1;
	private static final int			ENDGAME_TIME	= 10;
	protected LinkedList<PayloadPlayer>	players;
	private boolean						isStarted;
	private PayloadTeam					red, blue;
	private GameEndTimer				endTimer;
	private Location					minecartSpawn;
	protected Minetrackulator			trackulator;
	protected ArrayList<Location>		checkpoints, bars;
	protected boolean[]					checkedpoints;
	private String						bossBarTitle	= ChatColor.GREEN + "Payload Cart " + ChatColor.GRAY + "» " + ChatColor.YELLOW;
	private SetupTimer					setup;

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
	}

	@Override
	public boolean allowDamage(GamePlayer gp) {
		return isStarted && !((PayloadPlayer) gp).team.lost;
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

	private ArrayList<PayloadPlayer> getPlayersWithinDistance(Location l, int dist) {
		ArrayList<PayloadPlayer> list = new ArrayList<PayloadPlayer>();
		for (PayloadPlayer player : players) {
			if (player.team.team == TeamType.RED)
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
	public boolean join(final Player p) {
		PayloadPlayer plr = new PayloadPlayer(this, p, /* getTeam() */blue, PayloadClass.PYRO);
		players.add(plr);
		joinPlayer(plr);
		if (!isStarted && players.size() == minplayers)
			startGame();
		Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, new Runnable() {
			@Override
			public void run() {
				p.openInventory(ClassMenu.getMenu(p));
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
		new EndofGameTimer(this, ENDGAME_TIME);
	}

	private void endGame(int seconds) {
		endTimer.cancelTimer();
		new EndofGameTimer(this, ENDGAME_TIME);
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

		int cp = yml.getInt("checkpoint.size");
		checkedpoints = new boolean[cp];
		World w = null;
		for (int i = 0; i < cp; i++) {
			if (i == 0)
				w = Bukkit.getWorld(yml.getString("checkpoint.world"));
			checkpoints.add(new Location(w, yml.getDouble("checkpoint." + i + ".x"), yml.getDouble("checkpoint." + i + ".y"), yml.getDouble("checkpoint." + i + ".z")));
		}

		int bp = yml.getInt("bar.size");
		for (int i = 0; i < bp; i++) {
			if (i == 0)
				w = Bukkit.getWorld(yml.getString("bar.world"));
			bars.add(new Location(w, yml.getDouble("bar." + i + ".x"), yml.getDouble("bar." + i + ".y"), yml.getDouble("bar." + i + ".z")));
		}
	}

	@Override
	public void notifyDeath(GamePlayer gp, Entity damager, DamageCause cause) {
		death(gp);
	}

	@Override
	public void notifyDeath(GamePlayer gp, EntityDamageEvent e) {
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
		if (setup != null)
			setup.cancel();
		if (minecart != null)
			minecart.remove();
		minecart = null;
		if (endTimer != null)
			endTimer.cancelTimer();
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
		yml.set("checkpoint.size", checkpoints.size());
		for (int i = 0; i < checkpoints.size(); i++) {
			Location l = checkpoints.get(i);
			if (i == 0)
				yml.set("checkpoint.world", l.getWorld().getName());
			yml.set("checkpoint." + i + ".x", l.getX());
			yml.set("checkpoint." + i + ".y", l.getY());
			yml.set("checkpoint." + i + ".z", l.getZ());
		}
		yml.set("bar.size", bars.size());
		for (int i = 0; i < bars.size(); i++) {
			Location l = bars.get(i);
			if (i == 0)
				yml.set("bar.world", l.getWorld().getName());
			yml.set("bar." + i + ".x", l.getX());
			yml.set("bar." + i + ".y", l.getY());
			yml.set("bar." + i + ".z", l.getZ());
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
		endTimer = new GameEndTimer(this, 5);
		sendPlayersMessage(ChatColor.GREEN + "Setup ends in 60 seconds");
		setup = new SetupTimer(this, 60);
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
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
		player.setHealth(20);
		player.setFireTicks(0);
		sendPlayersMessage(ChatColor.DARK_GRAY + p.getName() + " died.");
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
			p.setHealth(Math.min(p.getHealth() + 0.1,20));
		// TODO: Ammo
	}

	protected void updateCart() {
		if (!isStarted || minecart == null || blue.lost)
			return;
		if (setup != null && setup.count > 0)
			return;
		ArrayList<PayloadPlayer> list = getPlayersWithinDistance(minecart.getLocation(), 3);
		for (PayloadPlayer p : list)
			cartTick(p);
		int h = (int) (trackulator.getCartPosition(minecart) * 200);
		for (PayloadPlayer p : players)
			BarAPI.setBarHealth(p.getPlayer(), h);
		double dist = list.size();
		Vector v;
		if (dist > 0)
			v = trackulator.getVector(minecart.getLocation(), dist * MINECART_SPEED);
		else {
			if (trackulator.headingTowardsEnd(minecart))
				v = trackulator.getVector(minecart.getLocation(), minecart.getVelocity().length() * 0.5);
			else return;
		}
		if (v == null)
			return;

		minecart.setVelocity(v);
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

	public boolean canPlayerHealFromHunger(GamePlayer p) {
		return false;
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
				payloadPlayer.dead = false;
				payloadPlayer.respawning = false;
				payloadPlayer.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
				if (payloadPlayer.classChange) {
					changeClass(payloadPlayer);
				}
			}
		}, 4);
	}

	private void changeClass(PayloadPlayer player) {
		player.getPlayer().getInventory().clear();
		Player p = player.getPlayer();
		p.sendMessage(ChatColor.GREEN + "You are now playing as " + player.playerClass.toString().substring(0, 1) + player.playerClass.toString().substring(1).toLowerCase());
		if (player.playerClass == PayloadClass.PYRO) {
			p.getInventory().addItem(getItem(Material.FIRE, ChatColor.RED + "Flame Thrower", ChatColor.GREEN + "10 Ammo per second"));
		} else if (player.playerClass == PayloadClass.ENGINEER) {
			p.getInventory().addItem(getItem(Material.DISPENSER, ChatColor.RED + "Sentry Gun", ChatColor.GREEN + "20 Arrows per Second"));
		} else if (player.playerClass == PayloadClass.MEDIC) {
			p.getInventory().addItem(getItem(Material.FISHING_ROD, ChatColor.RED + "Medi-Gun", ChatColor.GREEN + "1/2 Heart per Second"));
		} else if (player.playerClass == PayloadClass.HEAVY) {
			p.getInventory().addItem(getItem(Material.BOW, ChatColor.RED + "Sasha", ChatColor.GREEN + "Fires 60 Rounds per Second"));
		} else if (player.playerClass == PayloadClass.SNIPER) {
			p.getInventory().addItem(getItem(Material.BOW, ChatColor.RED + "Sniper Rifle", ChatColor.GREEN + "Deals 20 Damage on head shots"));
		} else if (player.playerClass == PayloadClass.SCOUT) {
			p.getInventory().addItem(getItem(Material.RED_ROSE, ChatColor.RED + "Scattergun", ChatColor.GREEN + "Lame", 2));
		}

		player.classChange = false;
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
}
