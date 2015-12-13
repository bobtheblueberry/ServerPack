package me.toxiccoke.minigames;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import me.monowii.mwschematics.Utils;
import me.toxiccoke.minigames.util.SkullUtils;

public abstract class GameArena<P extends GamePlayer> {

	protected String gameName;
	protected String schematic;
	protected Location pasteLocation, signLocation, lobbyLocation, bounds1, bounds2, leaderboard;
	protected String arenaName;
	protected ArrayList<Location> redSpawnLocations;
	protected ArrayList<Location> blueSpawnLocations;
	protected ArrayList<Material> indestructibles;
	protected int minplayers = 2, maxplayers = 6;
	protected int gamelength = 10;
	protected Leader leader1, leader2, leader3;

	protected class Leader {
		public String name;
		public int score;

		public Leader(String name, int score) {
			this.name = name;
			this.score = score;
		}

		public String toString() {
			return "Leader(" + name + "," + score + ")";
		}
	}

	public GameArena(String gameName, String arenaName) {
		this.gameName = gameName;
		this.arenaName = arenaName;

		redSpawnLocations = new ArrayList<Location>();
		blueSpawnLocations = new ArrayList<Location>();
		indestructibles = new ArrayList<Material>();
	}

	public String getGameName() {
		return gameName;
	}

	public String getArenaName() {
		return arenaName;
	}

	public abstract int getPlayerCount();

	public int getMaxPlayers() {
		return maxplayers;
	}

	public abstract boolean isJoinable();

	public boolean isFull() {
		return getPlayerCount() >= maxplayers;
	}

	public abstract boolean join(Player p);

	public abstract LinkedList<? extends P> getPlayers();

	public abstract void save();

	public void reset() {
		if (pasteLocation == null)
			return;
		if (schematic == null)
			return;
		Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, new Runnable() {

			@Override
			public void run() {
				File f = getSchematicFile();
				if (!f.isFile() || !f.exists())
					System.err.println("Improper file " + f.getPath());
				Utils.copySchematic(pasteLocation, f, false, true);
			}
		});
	}

	protected File getSchematicFile() {

		File f = new File(MiniGamesPlugin.plugin.getDataFolder().getParentFile().toString() + File.separator
				+ "WorldEdit" + File.separator + "schematics", schematic);
		File g = f;
		if (!g.exists()) {
			g = new File(f.getParent(), schematic + ".schematic");
		}
		if (!g.exists())
			g = new File(f.getParent(), schematic + ".mce");
		if (!g.exists())
			g = new File(f.getParent(), schematic + ".medit");
		// at least we tried
		return g;
	}

	public abstract boolean canPlaceBlock(GamePlayer p, BlockPlaceEvent event);

	public abstract boolean canBreakBlock(GamePlayer p, BlockBreakEvent event);

	public abstract boolean canExplodeBlock(Block b, Entity e);

	public abstract boolean canPlayerHunger(GamePlayer player);

	public void onPlayerInteract(GamePlayer p, PlayerInteractEvent e) {
	}

	public void projectileHit(GamePlayer p, ProjectileHitEvent event) {
	}

	public World getWorld() {
		return (pasteLocation == null) ? null : pasteLocation.getWorld();
	}

	protected YamlConfiguration getSaveYML() {
		YamlConfiguration yml = new YamlConfiguration();
		yml.set("MiniGameName", getGameName());
		yml.set("arena.name", getArenaName());
		yml.set("arena.schematic", schematic);
		yml.set("minplayers", minplayers);
		yml.set("maxplayers", maxplayers);
		yml.set("gamelength", gamelength);
		String[] indes = new String[indestructibles.size()];
		for (int i = 0; i < indes.length; i++)
			indes[i] = indestructibles.get(i).name();
		yml.set("indestructibles", indes);
		if (pasteLocation != null) {
			yml.set("paste.world", pasteLocation.getWorld().getName());
			yml.set("paste.x", pasteLocation.getBlockX());
			yml.set("paste.y", pasteLocation.getBlockY());
			yml.set("paste.z", pasteLocation.getBlockZ());
		}
		if (signLocation != null) {
			yml.set("sign.world", signLocation.getWorld().getName());
			yml.set("sign.x", signLocation.getBlockX());
			yml.set("sign.y", signLocation.getBlockY());
			yml.set("sign.z", signLocation.getBlockZ());
		}
		if (bounds1 != null) {
			yml.set("b1.world", bounds1.getWorld().getName());
			yml.set("b1.x", bounds1.getBlockX());
			yml.set("b1.y", bounds1.getBlockY());
			yml.set("b1.z", bounds1.getBlockZ());
		}
		if (bounds2 != null) {
			yml.set("b2.world", bounds2.getWorld().getName());
			yml.set("b2.x", bounds2.getBlockX());
			yml.set("b2.y", bounds2.getBlockY());
			yml.set("b2.z", bounds2.getBlockZ());
		}
		if (leaderboard != null) {
			yml.set("leaderboard.world", leaderboard.getWorld().getName());
			yml.set("leaderboard.x", leaderboard.getBlockX());
			yml.set("leaderboard.y", leaderboard.getBlockY());
			yml.set("leaderboard.z", leaderboard.getBlockZ());
		}
		int i = 0;
		yml.set("rspawns", redSpawnLocations.size());
		for (Location l : redSpawnLocations) {
			yml.set("rspawn" + i + ".world", l.getWorld().getName());
			yml.set("rspawn" + i + ".x", l.getBlockX());
			yml.set("rspawn" + i + ".y", l.getBlockY());
			yml.set("rspawn" + i + ".z", l.getBlockZ());
			yml.set("rspawn" + i + ".yaw", l.getYaw());
			yml.set("rspawn" + i + ".pitch", l.getPitch());

			i++;
		}
		i = 0;
		yml.set("bspawns", blueSpawnLocations.size());
		for (Location l : blueSpawnLocations) {
			yml.set("bspawn" + i + ".world", l.getWorld().getName());
			yml.set("bspawn" + i + ".x", l.getBlockX());
			yml.set("bspawn" + i + ".y", l.getBlockY());
			yml.set("bspawn" + i + ".z", l.getBlockZ());
			yml.set("bspawn" + i + ".yaw", l.getYaw());
			yml.set("bspawn" + i + ".pitch", l.getPitch());
			i++;
		}
		if (lobbyLocation != null) {
			yml.set("lobby.world", lobbyLocation.getWorld().getName());
			yml.set("lobby.x", lobbyLocation.getX());
			yml.set("lobby.y", lobbyLocation.getY());
			yml.set("lobby.z", lobbyLocation.getZ());
			yml.set("lobby.yaw", lobbyLocation.getYaw());
			yml.set("lobby.pitch", lobbyLocation.getPitch());
		}
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
		return yml;
	}

	protected void save(YamlConfiguration yml) {
		File folder = MiniGamesPlugin.plugin.getDataFolder();
		if (!folder.exists())
			folder.mkdirs();
		File f = new File(folder, getGameName() + getArenaName() + ".yml");
		try {
			yml.save(f);
		} catch (IOException e) {
			System.out.println("Cannot save data for " + f);
			e.printStackTrace();
		}
	}

	private World getWorld(String name) {
		if (name == null)
			return null;
		return Bukkit.getServer().getWorld(name);
	}

	@SuppressWarnings("deprecation")
	protected YamlConfiguration getLoadYML() {
		File f = new File(MiniGamesPlugin.plugin.getDataFolder(), getGameName() + getArenaName() + ".yml");
		if (!f.exists())
			return null;
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
		if (!yml.contains("arena.name"))
			return null; // didn't load
							// anything
		maxplayers = yml.getInt("maxplayers");
		minplayers = yml.getInt("minplayers");
		gamelength = yml.getInt("gamelength");
		indestructibles.clear();
		Iterator<String> it = yml.getStringList("indestructibles").iterator();
		while (it.hasNext()) {
			String s = it.next();
			Material m = Material.getMaterial(s);
			if (m == null) {
				// it may be listed as a number
				try {
					m = Material.getMaterial(Integer.parseInt(s));
				} catch (NumberFormatException exc) {
					Bukkit.getLogger().log(Level.WARNING, "Unknwon indestructible material for Bomber PVP: " + s, exc);
				}
			}
			if (m != null)
				indestructibles.add(m);
		}
		arenaName = yml.getString("arena.name");
		schematic = yml.getString("arena.schematic");
		if (yml.contains("leader1.name")) {
			leader1 = new Leader(yml.getString("leader1.name"), yml.getInt("leader1.score"));
		}
		if (yml.contains("leader2.name")) {
			leader2 = new Leader(yml.getString("leader2.name"), yml.getInt("leader2.score"));
		}
		if (yml.contains("leader3.name")) {
			leader3 = new Leader(yml.getString("leader3.name"), yml.getInt("leader3.score"));
		}

		World w = getWorld(yml.getString("paste.world"));
		if (w != null)
			pasteLocation = new Location(w, yml.getInt("paste.x"), yml.getInt("paste.y"), yml.getInt("paste.z"));
		else {
			System.err.println("Cannot find world " + yml.getString("paste.world"));
		}
		if (yml.contains("b1.world")) {
			bounds1 = new Location(getWorld(yml.getString("b1.world")), yml.getInt("b1.x"), yml.getInt("b1.y"),
					yml.getInt("b1.z"));
		}
		if (yml.contains("b2.world")) {
			bounds2 = new Location(getWorld(yml.getString("b2.world")), yml.getInt("b2.x"), yml.getInt("b2.y"),
					yml.getInt("b2.z"));
		}

		w = getWorld(yml.getString("sign.world"));
		if (w != null)
			signLocation = new Location(w, yml.getInt("sign.x"), yml.getInt("sign.y"), yml.getInt("sign.z"));
		else
			System.err.println("Cannot find world " + w);

		w = getWorld(yml.getString("lobby.world"));
		if (w != null)
			lobbyLocation = new Location(w, yml.getDouble("lobby.x"), yml.getDouble("lobby.y"),
					yml.getDouble("lobby.z"), (float) yml.getDouble("lobby.yaw"), (float) yml.getDouble("lobby.pitch"));
		w = getWorld(yml.getString("leaderboard.world"));
		if (w != null)
			leaderboard = new Location(w, yml.getInt("leaderboard.x"), yml.getInt("leaderboard.y"),
					yml.getInt("leaderboard.z"));

		int spawns = yml.getInt("rspawns");
		if (spawns > 0)
			redSpawnLocations.clear();
		for (int i = 0; i < spawns; i++) {
			Location l = new Location(getWorld(yml.getString("rspawn" + i + ".world")),
					yml.getDouble("rspawn" + i + ".x"), yml.getDouble("rspawn" + i + ".y"),
					yml.getDouble("rspawn" + i + ".z"), (float) yml.getDouble("rspawn" + i + ".yaw"),
					(float) yml.getDouble("rspawn" + i + ".pitch"));
			redSpawnLocations.add(l);
		}
		spawns = yml.getInt("bspawns");
		if (spawns > 0)
			blueSpawnLocations.clear();
		for (int i = 0; i < spawns; i++) {
			Location l = new Location(getWorld(yml.getString("bspawn" + i + ".world")),
					yml.getDouble("bspawn" + i + ".x"), yml.getDouble("bspawn" + i + ".y"),
					yml.getDouble("bspawn" + i + ".z"), (float) yml.getDouble("bspawn" + i + ".yaw"),
					(float) yml.getDouble("bspawn" + i + ".pitch"));
			blueSpawnLocations.add(l);
		}

		return yml;
	}

	public Sign getSign() {
		if (signLocation == null)
			return null;
		Block b = Bukkit.getServer().getWorld(signLocation.getWorld().getName()).getBlockAt(signLocation);
		if (b == null)
			return null;

		if (b.getState() instanceof Sign)
			return (Sign) b.getState();
		return null;
	}

	String[] signText = null;

	protected String[] getSignText() {
		return signText;
	}

	protected void setSignText(String[] text) {
		signText = text;
	}

	public abstract void notifyDeath(GamePlayer gp, Entity damager, DamageCause cause);

	public abstract void notifyDeath(GamePlayer gp, EntityDamageEvent e);

	public abstract void notifyQuitGame(GamePlayer gp);

	public void endUpdate(int minutes) {
		if (minutes > 1)
			sendPlayersMessage(ChatColor.GOLD + "Game ending in " + minutes + " minutes.");
		else if (minutes > 0)
			sendPlayersMessage(ChatColor.GOLD + "Game ending in " + minutes + " minute.");
		else
			endGame();
	}

	public void sendPlayersMessage(String msg) {
		for (P p : getPlayers())
			p.getPlayer().sendMessage(msg);
	}

	protected abstract void endGame();

	public void updateLeaderboard() {
		if (leaderboard == null)
			return;
		BlockFace f = BlockFace.SOUTH;

		org.bukkit.material.Sign sss = new org.bukkit.material.Sign(Material.WALL_SIGN);
		sss.setFacingDirection(f);
		if (leader1 != null) {
			SkullUtils.PlaceSkull(leaderboard.clone().add(0, 2, 0), leader1.name, f);
			Block s = leaderboard.getWorld().getBlockAt(leaderboard.clone().add(0, 1, 1));
			s.setType(Material.WALL_SIGN);
			Sign sign = (Sign) s.getState();

			sign.setData(sss);
			sign.setLine(0, "1st");
			sign.setLine(1, leader1.name);
			sign.setLine(2, leader1.score + " Kills");
			sign.update();
		} else {
			SkullUtils.PlaceSkull(leaderboard.clone().add(0, 2, 0), "Herobrine", f);
			Block s = leaderboard.getWorld().getBlockAt(leaderboard.clone().add(0, 1, 1));
			s.setType(Material.WALL_SIGN);
			Sign sign = (Sign) s.getState();
			sign.setData(sss);
			sign.setLine(0, "1st");
			sign.setLine(1, "Herobrine");
			sign.setLine(2, "Kills: " + ChatColor.MAGIC + "9001");
			sign.update();
		}

		if (leader2 != null) {
			SkullUtils.PlaceSkull(leaderboard.clone().add(-1, 1, 0), leader2.name, f);
			Block s = leaderboard.getWorld().getBlockAt(leaderboard.clone().add(-1, 0, 1));
			s.setType(Material.WALL_SIGN);
			Sign sign = (Sign) s.getState();
			sign.setData(sss);
			sign.setLine(0, "2nd");
			sign.setLine(1, leader2.name);
			sign.setLine(2, leader2.score + " Kills");
			sign.update();
		} else
			SkullUtils.PlaceSkull(leaderboard.clone().add(-1, 1, 0), "Herobrine", f);

		if (leader3 != null) {
			SkullUtils.PlaceSkull(leaderboard.clone().add(1, 1, 0), leader3.name, f);
			Block s = leaderboard.getWorld().getBlockAt(leaderboard.clone().add(1, 0, 1));
			s.setType(Material.WALL_SIGN);
			Sign sign = (Sign) s.getState();
			sign.setData(sss);
			sign.setLine(0, "3rd");
			sign.setLine(1, leader3.name);
			sign.setLine(2, leader3.score + " Kills");
			sign.update();
		} else
			SkullUtils.PlaceSkull(leaderboard.clone().add(1, 1, 0), "Herobrine", f);
	}

	public abstract void notifyLeaveCommand(GamePlayer gp);

	public abstract boolean allowDamage(GamePlayer gp, EntityDamageByEntityEvent event);

	public Bounds getBounds() {
		if (bounds1 == null || bounds2 == null)
			return null;
		return new Bounds(bounds1.getBlockX(), bounds2.getBlockX(), bounds1.getBlockY(), bounds2.getBlockY(),
				bounds1.getBlockZ(), bounds2.getBlockZ());
	}

	public Bounds getExcessBounds() {
		if (bounds1 == null || bounds2 == null)
			return null;
		int x1 = Math.min(bounds1.getBlockX(), bounds2.getBlockX());
		int x2 = Math.max(bounds1.getBlockX(), bounds2.getBlockX());
		int y1 = Math.min(bounds1.getBlockY(), bounds2.getBlockY());
		int y2 = Math.max(bounds1.getBlockY(), bounds2.getBlockY());
		int z1 = Math.min(bounds1.getBlockZ(), bounds2.getBlockZ());
		int z2 = Math.max(bounds1.getBlockZ(), bounds2.getBlockZ());
		return new Bounds(x1 - 1, x2 + 1, y1 - 1, y2 + 1, z1 - 1, z2 + 1);
	}
}
