package me.toxiccoke.minigames.payload;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ClassWeapons {

	private ClassWeapons() {}

	public static void flamethrower(PayloadPlayer pl) {
		Player player = pl.getPlayer();
		double x = player.getLocation().getX();
		double y = player.getLocation().getY();
		double z = player.getLocation().getZ();
		double yaw = player.getLocation().getYaw() + 90;
		double dist = 1.5;
		x += Math.cos(yaw * Math.PI / 180) * dist;
		z += Math.sin(yaw * Math.PI / 180) * dist;
		Location nl = new Location(player.getLocation().getWorld(), x, y, z);
		ArrayList<PayloadPlayer> players = getPlayersWithinDistance(pl.game, nl, dist);
		for (PayloadPlayer p : players)
			if (p.team.team != pl.team.team) {
				pl.dealtDmg = true;
				Player plr = p.getPlayer();
				plr.setFireTicks(80);
				plr.damage(0.5);
			}
	}

	public static void airblast(PayloadPlayer pl) {
		Player player = pl.getPlayer();
		double x = player.getLocation().getX();
		double y = player.getLocation().getY();
		double z = player.getLocation().getZ();
		double yaw = player.getLocation().getYaw() + 90;
		double dist = 1.5;
		double cx = Math.cos(yaw * Math.PI / 180) * dist;
		double cz = Math.sin(yaw * Math.PI / 180) * dist;
		x += cx;
		z += cz;
		Vector velocity = new Vector(cx, 1.5, cz);
		Location nl = new Location(player.getLocation().getWorld(), x, y, z);
		ArrayList<PayloadPlayer> players = getPlayersWithinDistance(pl.game, nl, dist);
		for (PayloadPlayer p : players)
			if (p.team.team != pl.team.team) {
				pl.dealtDmg = true;
				p.getPlayer().setVelocity(velocity);
			} else p.getPlayer().setFireTicks(0);
	}

	private static ArrayList<PayloadPlayer> getPlayersWithinDistance(PayloadGame game, Location l, double dist) {
		ArrayList<PayloadPlayer> list = new ArrayList<PayloadPlayer>();
		for (PayloadPlayer player : game.players) {
			double distance = player.getPlayer().getLocation().distance(l);
			if (distance <= dist && !player.dead)
				list.add(player);
		}
		return list;
	}
}