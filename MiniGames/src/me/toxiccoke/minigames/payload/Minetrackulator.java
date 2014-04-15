package me.toxiccoke.minigames.payload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import me.toxiccoke.io.StreamDecoder;
import me.toxiccoke.io.StreamEncoder;
import me.toxiccoke.minigames.MiniGamesPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

public class Minetrackulator {

	protected ArrayList<Block>	rails;
	private PayloadGame			game;

	public Minetrackulator(PayloadGame game) {
		rails = new ArrayList<Block>();
		this.game = game;
	}

	private File getFile() {
		File parent = MiniGamesPlugin.plugin.getDataFolder();
		if (!parent.exists())
			parent.mkdirs();
		File file = new File(parent, game.getWorldName() + ".minetrack");
		return file;
	}

	public void save() {
		if (rails.size() == 0)
			return;
		try {
			StreamEncoder e = new StreamEncoder(getFile());
			e.write(1);// version
			e.write4(rails.size());
			e.writeStr(rails.get(0).getWorld().getName());
			for (int i = 0; i < rails.size(); i++) {
				Location l = rails.get(i).getLocation();
				e.write4(l.getBlockX());
				e.write4(l.getBlockY());
				e.write4(l.getBlockZ());
			}
			e.close();
		} catch (IOException e) {
			System.err.println("cannot save minetrack data");
			e.printStackTrace();
		}
	}

	public void load() {
		File f = getFile();
		if (!f.exists())
			return;
		try {
			StreamDecoder in = new StreamDecoder(f);
			in.read();// version
			int railsnum = in.read4();
			String worldname = in.readStr();
			World w = Bukkit.getWorld(worldname);
			if (w == null) {
				System.err.println("minetrakulator:load: no such world: " + worldname);
				in.close();
				return;
			}
			rails.clear();
			for (int i = 0; i < railsnum; i++)
				rails.add(new Location(w, in.read4(), in.read4(), in.read4()).getBlock());
			in.close();
		} catch (IOException e) {
			System.err.println("cannot load minetrack data");
			e.printStackTrace();
		}
	}

	private boolean compareLocations(Location l1, Location l2) {
		if (l1.getX() != l2.getX() || l1.getZ() != l2.getZ())
			return false;
		int i1 = (int) Math.round(l1.getY() / 8);// hack fix
		int i2 = (int) Math.round(l2.getY() / 8);
		return i1 == i2;
	}

	public double getCartPosition(Minecart m) {
		double i = 0;
		Location l = m.getLocation();
		for (Block b : rails)
			if (compareLocations(b.getLocation(), l.getBlock().getLocation()))
				break;
			else i++;
		return (i + 1) / rails.size();
	}

	public Vector getVector(Location l, double speed) {
		Block rail = null;
		int i = 0;
		for (Block b : rails)
			if (compareLocations(b.getLocation(), l.getBlock().getLocation())) {
				rail = b;
				break;
			} else i++;
		if (rail == null) { return null; }
		Vector v;
		// last rail
		Location next;
		if (i + 1 >= rails.size()) {
			Block prev = rails.get(rails.size() - 2);
			next = l.clone().add(rail.getX() - prev.getX(), 0, rail.getZ() - prev.getZ());
		} else next = rails.get(i + 1).getLocation();

		Vector v1, v2;
		double change = 0;
		if (rail.getY() != next.getY()) {
			change = speed;
			if (rail.getY() > next.getY())
				change = -change;
		}
		if (rail.getX() == next.getX()) {
			v1 = new Vector(0, change, speed);
			v2 = new Vector(0, change, -speed);
		} else// if (rail.getZ() == next.getZ())
		{
			v1 = new Vector(speed, change, 0);
			v2 = new Vector(-speed, change, 0);
		}

		// find out which one is closed to the next rail
		double d1 = getDistance(next, l.clone().add(v1));
		double d2 = getDistance(next, l.clone().add(v2));
		if (d1 <= d2)
			v = v1;
		else v = v2;

		return v;
	}

	public static double getDistance(Location l1, Location l2) {
		double dx = l1.getX() - l2.getX();
		double dy = l1.getY() - l2.getY();
		double dz = l1.getZ() - l2.getZ();
		// We should avoid Math.pow or Math.hypot due to performance reasons
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public boolean headingTowardsEnd(Minecart m) {
		Location current = m.getLocation();
		Location future = m.getLocation().clone().add(m.getVelocity());
		return isCloserToEnd(future, current);
	}

	/**
	 * 
	 * Returns true if l1 is closer to the end than l2
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	public boolean isCloserToEnd(Location l1, Location l2) {
		Location current = l2;
		Location future = l1;
		int d = -1;
		for (int i = 0; i < rails.size(); i++)
			if (compareLocations(rails.get(i).getLocation(), current.getBlock().getLocation())) {
				d = i;
				break;
			}
		if (d < 0) { return false; }
		Block track = rails.get(d);
		Location next;
		if (d + 1 == rails.size()) {
			Block prev = rails.get(rails.size() - 2);
			next = current.clone().add(track.getX() - prev.getX(), 0, track.getZ() - prev.getZ());
		} else next = rails.get(d + 1).getLocation();
		double nDist = getDistance(future, next);
		double oDist = getDistance(current, next);

		return nDist < oDist;
	}

	public void trackulate(Location l) {
		rails.clear();
		Block b = l.getBlock();
		if (b != null && isRail(b)) {
			rails.add(b);
			Block previous = b;
			// previous2 prevents us from going in circles
			Block previous2 = null;
			while (true) {
				Block next = nextRail(previous, previous2);
				if (next == null)
					break;
				rails.add(next);
				previous2 = previous;
				previous = next;
			}
		} else {
			System.out.println("not a rail block " + b);
		}
	}

	private Block nextRail(Block l, Block p2) {
		/*
		 * N E S W 1 + N E S W 1 - N E S W
		 */

		Block b = l.getLocation().clone().add(1, 0, 0).getBlock();
		if (b != null && isRail(b) && !b.equals(p2))
			return b;
		b = l.getLocation().clone().add(-1, 0, 0).getBlock();
		if (b != null && isRail(b) && !b.equals(p2))
			return b;
		b = l.getLocation().clone().add(0, 0, 1).getBlock();
		if (b != null && isRail(b) && !b.equals(p2))
			return b;
		b = l.getLocation().clone().add(0, 0, -1).getBlock();
		if (b != null && isRail(b) && !b.equals(p2))
			return b;

		b = l.getLocation().clone().add(1, 1, 0).getBlock();
		if (b != null && isRail(b) && !b.equals(p2))
			return b;
		b = l.getLocation().clone().add(-1, 1, 0).getBlock();
		if (b != null && isRail(b) && !b.equals(p2))
			return b;
		b = l.getLocation().clone().add(0, 1, 1).getBlock();
		if (b != null && isRail(b) && !b.equals(p2))
			return b;
		b = l.getLocation().clone().add(0, 1, -1).getBlock();
		if (b != null && isRail(b) && !b.equals(p2))
			return b;

		b = l.getLocation().clone().add(1, -1, 0).getBlock();
		if (b != null && isRail(b) && !b.equals(p2))
			return b;
		b = l.getLocation().clone().add(-1, -1, 0).getBlock();
		if (b != null && isRail(b) && !b.equals(p2))
			return b;
		b = l.getLocation().clone().add(0, -1, 1).getBlock();
		if (b != null && isRail(b) && !b.equals(p2))
			return b;
		b = l.getLocation().clone().add(0, -1, -1).getBlock();
		if (b != null && isRail(b) && !b.equals(p2))
			return b;

		return null;
	}

	private boolean isRail(Block b) {
		Material m = b.getType();
		return m == Material.RAILS || m == Material.ACTIVATOR_RAIL || m == Material.DETECTOR_RAIL || m == Material.POWERED_RAIL;
	}

}
