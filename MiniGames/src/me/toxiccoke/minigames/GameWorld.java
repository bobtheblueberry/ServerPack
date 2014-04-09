package me.toxiccoke.minigames;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import me.monowii.mwschematics.Utils;

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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;

public abstract class GameWorld {

	protected String				gameName;
	protected String				schematic;
	protected Location				pasteLocation, signLocation, lobbyLocation, bounds1, bounds2, leaderboard;
	protected boolean				broken;
	protected String				worldName;
	protected ArrayList<Location>	spawnLocations;
	protected int					heightLimit	= 1000;
	protected int					minplayers, maxplayers;
	protected Leader				leader1, leader2, leader3;

	protected class Leader {
		public String	name;
		public int		score;

		public Leader(String name, int score) {
			this.name = name;
			this.score = score;
		}

		public String toString() {
			return "Leader(" + name + "," + score + ")";
		}
	}

	public GameWorld(String gameName, String worldName) {
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

	public boolean isFull() {
		return getPlayerCount() >= maxplayers;
	}

	public abstract boolean join(Player p);

	public abstract LinkedList<? extends GamePlayer> getPlayers();

	public abstract void save();

	public void vehicleUpdate(VehicleUpdateEvent e) {}

	public void reset() {
		if (pasteLocation == null)
			return;
		if (schematic == null)
			return;
		Utils.copySchematic(pasteLocation, new File(MiniGamesPlugin.plugin.getDataFolder(), schematic), false, true);
	}

	public abstract boolean canPlaceBlock(GamePlayer p, BlockPlaceEvent event);

	public abstract boolean canBreakBlock(GamePlayer p, BlockBreakEvent event);

	public abstract boolean canExplodeBlock(Block b, Entity e);

	public abstract boolean canPlayerHunger(GamePlayer player);

	public void onPlayerInteract(GamePlayer p, PlayerInteractEvent e) {}

	public void projectileHit(GamePlayer p, ProjectileHitEvent event) {}

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

	protected void checkLeader(GamePlayer p) {
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
		if (yml.contains("leader1.name")) {
			leader1 = new Leader(yml.getString("leader1.name"), yml.getInt("leader1.score"));
		}
		if (yml.contains("leader2.name")) {
			leader2 = new Leader(yml.getString("leader2.name"), yml.getInt("leader2.score"));
		}
		if (yml.contains("leader3.name")) {
			leader3 = new Leader(yml.getString("leader3.name"), yml.getInt("leader3.score"));
		}
		String world = yml.getString("world.world");
		if (world == null)
			return yml;
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

	public abstract void notifyDeath(GamePlayer gp, Entity damager, DamageCause cause);

	public abstract void notifyDeath(GamePlayer gp, EntityDamageEvent e);

	public abstract void notifyQuitGame(GamePlayer gp);

	public void endUpdate(int minutes) {
		if (minutes > 1)
			sendPlayersMessage(ChatColor.GOLD + "Game ending in " + minutes + " minutes.");
		else if (minutes > 0)
			sendPlayersMessage(ChatColor.GOLD + "Game ending in " + minutes + " minute.");
		else endGame();
	}

	public void sendPlayersMessage(String msg) {
		for (GamePlayer p : getPlayers())
			p.getPlayer().sendMessage(msg);
	}

	protected abstract void endGame();

	@SuppressWarnings("deprecation")
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

	public abstract void notifyLeaveCommand(GamePlayer gp);

	public abstract boolean allowDamage(GamePlayer gp);

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
