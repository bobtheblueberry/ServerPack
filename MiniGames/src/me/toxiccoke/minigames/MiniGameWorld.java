package me.toxiccoke.minigames;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import me.monowii.mwschematics.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class MiniGameWorld {

	protected String				gameName;
	protected String				schematic;
	protected Location				pasteLocation, signLocation, lobbyLocation, bounds1, bounds2, leaderboard;
	protected boolean				broken;
	protected String				worldName;
	protected ArrayList<Location>	spawnLocations;
	protected int					heightLimit	= 1000;
	protected int					minplayers, maxplayers;

	public MiniGameWorld(String gameName, String worldName) {
		this.gameName = gameName;
		this.worldName = worldName;
		spawnLocations = new ArrayList<Location>();
	}

	public int getHeightLimit() {
		return heightLimit;
	}

	public String getGameName() {
		return gameName;
	}

	public String getWorldName() {
		return worldName;
	}

	public abstract int getPlayerCount();

	public int getMaxPlayers() {
		return maxplayers;
	}

	public abstract boolean isJoinable();

	public abstract boolean isFull();

	public abstract boolean join(Player p);

	public abstract LinkedList<? extends MiniGamePlayer> getPlayers();

	public abstract void save();

	public void reset() {
		if (pasteLocation == null)
			return;
		if (schematic == null)
			return;
		Utils.copySchematic(pasteLocation, new File(MiniGamesPlugin.plugin.getDataFolder(), schematic), false, true);
	}

	public abstract boolean canPlaceBlock(MiniGamePlayer p, BlockPlaceEvent event);

	public abstract boolean canBreakBlock(MiniGamePlayer p, BlockBreakEvent event);

	public abstract boolean canExplodeBlock(Block b, Entity e);

	public abstract boolean canPlayerHunger(MiniGamePlayer player);

	public void onPlayerInteract(MiniGamePlayer p, PlayerInteractEvent e) {}

	public void projectileHit(MiniGamePlayer p, ProjectileHitEvent event) {}

	protected YamlConfiguration getSaveYML() {
		YamlConfiguration yml = new YamlConfiguration();
		yml.set("MiniGameName", getGameName());
		yml.set("world.name", getWorldName());
		yml.set("world.schematic", schematic);
		yml.set("world.heightlimit", heightLimit);
		yml.set("minplayers", minplayers);
		yml.set("maxplayers", maxplayers);

		if (pasteLocation != null) {
			yml.set("world.world", pasteLocation.getWorld().getName());
			yml.set("world.x", pasteLocation.getBlockX());
			yml.set("world.y", pasteLocation.getBlockY());
			yml.set("world.z", pasteLocation.getBlockZ());
		}
		if (signLocation != null) {
			yml.set("sign.world", signLocation.getWorld().getName());
			yml.set("sign.x", signLocation.getBlockX());
			yml.set("sign.y", signLocation.getBlockY());
			yml.set("sign.z", signLocation.getBlockZ());
		}
		if (bounds1 != null) {
			yml.set("b1.x", bounds1.getBlockX());
			yml.set("b1.y", bounds1.getBlockY());
			yml.set("b1.z", bounds1.getBlockZ());
		}
		if (bounds2 != null) {
			yml.set("b2.x", bounds2.getBlockX());
			yml.set("b2.y", bounds2.getBlockY());
			yml.set("b2.z", bounds2.getBlockZ());
		}
		if (leaderboard != null) {
			yml.set("leaderboard.x", leaderboard.getBlockX());
			yml.set("leaderboard.y", leaderboard.getBlockY());
			yml.set("leaderboard.z", leaderboard.getBlockZ());
		}
		int i = 0;
		yml.set("spawns", spawnLocations.size());
		for (Location l : spawnLocations) {
			yml.set("spawn" + i + ".x", l.getBlockX());
			yml.set("spawn" + i + ".y", l.getBlockY());
			yml.set("spawn" + i + ".z", l.getBlockZ());
			yml.set("spawn" + i + ".yaw", l.getYaw());
			yml.set("spawn" + i + ".pitch", l.getPitch());

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
		return yml;
	}

	protected void save(YamlConfiguration yml) {
		File folder = MiniGamesPlugin.plugin.getDataFolder();
		if (!folder.exists())
			folder.mkdirs();
		File f = new File(folder, getGameName() + getWorldName() + ".yml");
		try {
			yml.save(f);
		} catch (IOException e) {
			System.out.println("Cannot save data for " + f);
			e.printStackTrace();
		}
	}

	protected YamlConfiguration getLoadYML() {
		File f = new File(MiniGamesPlugin.plugin.getDataFolder(), getGameName() + getWorldName() + ".yml");
		if (!f.exists())
			return null;
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
		if (!yml.contains("world.name"))
			return null; // didn't load
							// anything
		maxplayers = yml.getInt("maxplayers");
		minplayers = yml.getInt("minplayers");

		worldName = yml.getString("world.name");
		schematic = yml.getString("world.schematic");
		if (yml.contains("world.heightlimit"))
			heightLimit = yml.getInt("world.heightlimit");
		String world = yml.getString("world.world");
		World w = Bukkit.getServer().getWorld(world);
		if (w != null)
			pasteLocation = new Location(w, yml.getInt("world.x"), yml.getInt("world.y"), yml.getInt("world.z"));
		else {
			System.err.println("Cannot find world " + world);
			broken = true;
		}

		if (w != null) {
			if (yml.contains("b1.x")) {
				bounds1 = new Location(w, yml.getInt("b1.x"), yml.getInt("b1.y"), yml.getInt("b1.z"));
			}
			if (yml.contains("b2.x")) {
				bounds2 = new Location(w, yml.getInt("b2.x"), yml.getInt("b2.y"), yml.getInt("b2.z"));
			}
		}

		world = yml.getString("sign.world");
		if (world != null) {
			World ww = Bukkit.getServer().getWorld(world);
			if (ww != null)
				signLocation = new Location(ww, yml.getInt("sign.x"), yml.getInt("sign.y"), yml.getInt("sign.z"));
			else System.err.println("Cannot find world " + world);
		}
		world = yml.getString("lobby.world");
		if (world != null) {
			World ww = Bukkit.getServer().getWorld(world);
			if (ww != null) {
				lobbyLocation = new Location(ww, yml.getDouble("lobby.x"), yml.getDouble("lobby.y"),
						yml.getDouble("lobby.z"), (float) yml.getDouble("lobby.yaw"),
						(float) yml.getDouble("lobby.pitch"));
				if (yml.contains("leaderboard.x"))
					leaderboard = new Location(ww, yml.getInt("leaderboard.x"), yml.getInt("leaderboard.y"),
							yml.getInt("leaderboard.z"));

			} else System.err.println("Cannot find world " + ww);
		}
		int spawns = yml.getInt("spawns");
		if (spawns > 0)
			spawnLocations.clear();
		for (int i = 0; i < spawns; i++) {
			Location l = new Location(Bukkit.getServer().getWorld(yml.getString("world.world")), yml.getDouble("spawn"
					+ i + ".x"), yml.getDouble("spawn" + i + ".y"), yml.getDouble("spawn" + i + ".z"),
					(float) yml.getDouble("spawn" + i + ".yaw"), (float) yml.getDouble("spawn" + i + ".pitch"));
			spawnLocations.add(l);
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

	String[]	signText	= null;

	protected String[] getSignText() {
		return signText;
	}

	protected void setSignText(String[] text) {
		signText = text;
	}

	public abstract void notifyDeath(MiniGamePlayer gp, Entity damager, DamageCause cause);

	public abstract void notifyDeath(MiniGamePlayer gp, EntityDamageEvent e);

	public abstract void notifyQuitGame(MiniGamePlayer gp);

	public abstract void endUpdate(int minutesLeft);

	public abstract void updateLeaderboard();

	public abstract void notifyLeaveCommand(MiniGamePlayer gp);

	public abstract boolean allowDamage(MiniGamePlayer gp);

	public Bounds getBounds() {
		if (bounds1 == null || bounds2 == null)
			return null;
		int x1 = Math.min(bounds1.getBlockX(), bounds2.getBlockX());
		int x2 = Math.max(bounds1.getBlockX(), bounds2.getBlockX());
		int z1 = Math.min(bounds1.getBlockZ(), bounds2.getBlockZ());
		int z2 = Math.max(bounds1.getBlockZ(), bounds2.getBlockZ());
		return new Bounds(x1, x2, z1, z2);
	}

	public Bounds getExcessBounds() {
		if (bounds1 == null || bounds2 == null)
			return null;
		int x1 = Math.min(bounds1.getBlockX(), bounds2.getBlockX());
		int x2 = Math.max(bounds1.getBlockX(), bounds2.getBlockX());
		int z1 = Math.min(bounds1.getBlockZ(), bounds2.getBlockZ());
		int z2 = Math.max(bounds1.getBlockZ(), bounds2.getBlockZ());
		return new Bounds(x1 - 1, x2 + 1, z1 - 1, z2 + 1);
	}

}
