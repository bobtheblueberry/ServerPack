package me.toxiccoke.minigames;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public abstract class MiniGameWorld {

	protected MiniGame						game;
	protected int							MAX_PLAYERS	= 24;
	protected LinkedList<MiniGamePlayer>	players;
	protected String						schematic;
	protected Location						pasteLocation, signLocation;
	protected boolean						broken;
	protected String						worldName;

	public MiniGameWorld(MiniGame game, String name) {
		players = new LinkedList<MiniGamePlayer>();
		this.game = game;
		this.worldName = name;
	}

	public String getGameName() {
		return game.getName();
	}

	public String getWorldName() {
		return worldName;
	}

	public abstract int getPlayerCount();

	public int getMaxPlayers() {
		return MAX_PLAYERS;
	}

	public abstract boolean isJoinable();

	public abstract boolean join(Player p);

	public abstract void save();

	protected YamlConfiguration getSaveYML() {
		YamlConfiguration yml = new YamlConfiguration();
		yml.set("MiniGameName", getGameName());
		yml.set("world.name", getWorldName());
		yml.set("world.schematic", schematic);
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

		return yml;
	}

	public Sign getSign() {
		if (signLocation == null) return null;
		Block b = Bukkit.getServer().getWorld(signLocation.getWorld().getName()).getBlockAt(signLocation);
		if (b == null) return null;
		if (b.getState() instanceof Sign) return (Sign) b.getState();
		return null;
	}
}
