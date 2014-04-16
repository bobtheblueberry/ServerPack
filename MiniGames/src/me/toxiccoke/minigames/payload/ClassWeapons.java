package me.toxiccoke.minigames.payload;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ClassWeapons {

	private ClassWeapons() {}

	public static void flamethrower(PayloadPlayer pl) {
		if (!takeAmmo(pl, 1))
			return;
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
				plr.sendMessage(ChatColor.RED + "Burnt by " + player.getName());
				plr.setFireTicks(80);
				plr.damage(0.5);
			}
	}

	public static void airblast(PayloadPlayer pl) {
		if (!takeAmmo(pl, 10))
			return;
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
		Vector velocity = new Vector(cx, 1.2, cz);
		Location nl = new Location(player.getLocation().getWorld(), x, y, z);
		ArrayList<PayloadPlayer> players = getPlayersWithinDistance(pl.game, nl, dist);
		for (PayloadPlayer p : players) {
			Player plr = p.getPlayer();
			if (p.team.team != pl.team.team) {
				pl.dealtDmg = true;
				plr.setVelocity(velocity);
				plr.sendMessage(ChatColor.RED + "Airblasted by " + player.getName());
			} else if (plr.getFireTicks() > 0) {
				plr.sendMessage(ChatColor.GREEN + "Extinguished by " + player.getName());
				plr.setFireTicks(0);
			}
		}
	}

	public static void minigun(PayloadPlayer pl) {
		if (!takeAmmo(pl, 1))
			return;
		pl.dealtDmg = true;
		Player p = pl.getPlayer();
		Location l = p.getLocation().add(0, 1, 0);
		ItemStack stack = new ItemStack(Material.ANVIL);
		Item item = p.getWorld().dropItem(l, stack);
		Vector velocity = p.getLocation().getDirection();
		velocity.add(new Vector((Math.random() - 0.5) / 8, (Math.random() - 0.5) / 8, (Math.random() - 0.5) / 8));
		item.setVelocity(velocity.multiply(2));
		pl.game.bullets.add(new Bullet(pl, item, 1));
	}

	public static void scattergun(PayloadPlayer pl) {
		if (!takeAmmo(pl, 1))
			return;
		pl.dealtDmg = true;
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

	private static boolean takeAmmo(PayloadPlayer p, int ammo) {
		int a = p.getAmmo();
		if (a >= ammo)
			p.setAmmo(a - ammo);
		else {
			p.getPlayer().sendMessage(ChatColor.GREEN + "Not Enough Ammunition!");
			return false;
		}
		return true;
	}
	
	public static void damage(PayloadPlayer damager, PayloadPlayer damagee, float damage) {
		CustomDamager.damage(damager, damagee, damage);
	}
}
