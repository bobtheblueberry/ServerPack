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
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public abstract class MiniGameWorld {

	protected String				gameName;
	protected int					MAX_PLAYERS	= 24;
	protected String				schematic;
	protected Location				pasteLocation, signLocation, lobbyLocation;
	protected boolean				broken;
	protected String				worldName;
	protected ArrayList<Location>	spawnLocations;
	protected int					heightLimit	= 1000;

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
		return MAX_PLAYERS;
	}

	public abstract boolean isJoinable();

	public abstract boolean isFull();

	public abstract boolean join(Player p);

	public abstract LinkedList<? extends MiniGamePlayer> getPlayers();

	public abstract void save();

	public void reset() {
		if (pasteLocation == null) return;
		if (schematic == null) return;
		Utils.copySchematic(pasteLocation, new File(MiniGamesPlugin.plugin.getDataFolder(), schematic), false, true);
	}
	
	public abstract boolean canPlaceBlock(MiniGamePlayer p, BlockPlaceEvent event);
	public abstract boolean canBreakBlock(MiniGamePlayer p, BlockBreakEvent event);

	protected YamlConfiguration getSaveYML() {
		YamlConfiguration yml = new YamlConfiguration();
		yml.set("MiniGameName", getGameName());
		yml.set("world.name", getWorldName());
		yml.set("world.schematic", schematic);
		yml.set("world.heightlimit", heightLimit);
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
		if (!folder.exists()) folder.mkdirs();
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
		if (!f.exists()) return null;
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
		if (yml.getString("world.name") == null) return null; // didn't load
																// anything
		worldName = yml.getString("world.name");
		schematic = yml.getString("world.schematic");
		if (yml.contains("world.heightlimit")) heightLimit = yml.getInt("world.heightlimit");
		String world = yml.getString("world.world");
		if (world != null) {
			World w = Bukkit.getServer().getWorld(world);
			if (w != null) pasteLocation = new Location(w, yml.getInt("world.x"), yml.getInt("world.y"),
					yml.getInt("world.z"));
			else {
				System.err.println("Cannot find world " + w);
				broken = true;
			}
		}
		world = yml.getString("sign.world");
		if (world != null) {
			World w = Bukkit.getServer().getWorld(world);
			if (w != null) signLocation = new Location(w, yml.getInt("sign.x"), yml.getInt("sign.y"),
					yml.getInt("sign.z"));
			else System.err.println("Cannot find world " + w);
		}
		world = yml.getString("lobby.world");
		if (world != null) {
			World w = Bukkit.getServer().getWorld(world);
			if (w != null) lobbyLocation = new Location(w, yml.getDouble("lobby.x"), yml.getDouble("lobby.y"),
					yml.getDouble("lobby.z"), (float) yml.getDouble("lobby.yaw"), (float) yml.getDouble("lobby.pitch"));
			else System.err.println("Cannot find world " + w);
		}
		int spawns = yml.getInt("spawns");
		if (spawns > 0) spawnLocations.clear();
		for (int i = 0; i < spawns; i++) {
			Location l = new Location(Bukkit.getServer().getWorld(yml.getString("world.world")), yml.getDouble("spawn"
					+ i + ".x"), yml.getDouble("spawn" + i + ".y"), yml.getDouble("spawn" + i + ".z"),
					(float) yml.getDouble("spawn" + i + ".yaw"), (float) yml.getDouble("spawn" + i + ".pitch"));
			spawnLocations.add(l);
		}

		return yml;
	}

	public Sign getSign() {
		if (signLocation == null) return null;
		Block b = Bukkit.getServer().getWorld(signLocation.getWorld().getName()).getBlockAt(signLocation);
		if (b == null) return null;
		if (b.getState() instanceof Sign) return (Sign) b.getState();
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

	public abstract void notifyDeath(MiniGamePlayer gp, DamageCause cause);

	public abstract void notifyQuitGame(MiniGamePlayer gp);

	public abstract void endUpdate(int minutesLeft);

	public abstract void notifyLeaveCommand(MiniGamePlayer gp);

	public abstract boolean allowDamage(MiniGamePlayer gp);
}
