package me.toxiccoke.minigames.payload;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class Bullet {

	private Item			item;
	private boolean			dead;
	private PayloadPlayer	shooter;
	private float			damage;

	public Bullet(PayloadPlayer shooter, Item item, float damage) {
		this.item = item;
		this.shooter = shooter;
		this.damage = damage;
	}

	public boolean isDead() {
		return dead;
	}

	private Location	future;

	public void update(PayloadGame game) {
		if (dead)
			return;
		Vector itemv = item.getVelocity();
		double len = itemv.length();
		PayloadPlayer p;
		boolean pCollide = false;
		Location l = item.getLocation();
		if (len > 1) {
			double tot = 1 + ((int) len);
			for (double i = 1; i <= tot * 2; i++) {
				double m = i / (tot * 2);
				Vector v = new Vector(itemv.getX() * m, itemv.getY() * m, itemv.getZ() * m);
				if ((p = collisionCheck(l.clone().add(v), game)) != null) {
					dead = true;
					pCollide  = true;
					collide(p);
				}
			}
		} else {
			if ((p = collisionCheck(l, game)) != null) {
				dead = true;
				pCollide = true;
				collide(p);
			}
		}
		if (pCollide || (future != null && !compareLocations(future, item.getLocation())) || len < 0.5) {
			if (!pCollide)
				surfaceCollisionFirework(item.getLocation());
			dead = true;
			item.remove();
		}
		future = item.getLocation().add(item.getVelocity());
	}

	public boolean compareLocations(Location l1, Location l2) {
		if (l1.getX() != l2.getX() || l1.getZ() != l2.getZ())
			return false;
		if (l1.getY() == l2.getY())
			return false;
		return true;
	}

	private void collide(PayloadPlayer p) {
		if (p.team.team == shooter.team.team)
			return;
		playerCollisionFirework(p.getPlayer());
		ClassWeapons.damage(shooter, p, damage);
	}

	private void playerCollisionFirework(Player p) {
		Firework f = (Firework) p.getWorld().spawn(p.getLocation(), Firework.class);
		FireworkMeta fm = f.getFireworkMeta();
		fm.addEffect(FireworkEffect.builder().flicker(false).trail(false).with(Type.BURST).withColor(Color.RED).withFade(Color.ORANGE).build());
		 fm.setPower(2);
		f.setFireworkMeta(fm);
	}

	private void surfaceCollisionFirework(Location l ) {
		Firework f = (Firework) l.getWorld().spawn(l, Firework.class);
		FireworkMeta fm = f.getFireworkMeta();
		fm.addEffect(FireworkEffect.builder().flicker(false).trail(false).with(Type.BURST).withColor(Color.RED).withFade(Color.ORANGE).build());
		 fm.setPower(1);
		f.setFireworkMeta(fm);
	}

	private PayloadPlayer collisionCheck(Location l, PayloadGame game) {
		for (PayloadPlayer p : game.players) {
			if (p == shooter)
				continue;
			Location playerL = p.getPlayer().getLocation();
			if (distance2D(l, playerL) < 0.5 && checkY(playerL, l))
				return p;
		}
		return null;
	}

	private boolean checkY(Location playerL, Location bulletL) {
		double bullet = bulletL.getY();
		double y = playerL.getY();
		return bullet < y + 2 && bullet > y - 0.1;
	}

	private double distance2D(Location l1, Location l2) {
		double dx = l1.getX() - l2.getX();
		double dz = l1.getZ() - l2.getZ();
		// We should avoid Math.pow or Math.hypot due to performance reasons
		return Math.sqrt(dx * dx + dz * dz);
	}
}
