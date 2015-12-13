package me.toxiccoke.minigames.payload.util;

import java.lang.reflect.Method;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkUtils {

	/**
	 * Explodes random firework on location
	 * 
	 * @param loc
	 *            Location to explode
	 */
	public static void playFirework(Location loc, FireworkEffect effect) {
		try {
			Firework fw = loc.getWorld().spawn(loc, Firework.class);
			Method d0 = getMethod(loc.getWorld().getClass(), "getHandle");
			Method d2 = getMethod(fw.getClass(), "getHandle");
			Object o3 = d0.invoke(loc.getWorld(), (Object[]) null);
			Object o4 = d2.invoke(fw, (Object[]) null);
			Method d1 = getMethod(o3.getClass(), "broadcastEntityEffect");
			FireworkMeta data = fw.getFireworkMeta();
			if (effect != null)
				data.addEffect(effect);
			fw.setFireworkMeta(data);
			d1.invoke(o3, new Object[] { o4, (byte) 17 });
			fw.remove();
		} catch (Exception ex) {
			// not a Beta1.4.6R0.2 Server
		}
	}

	public static void playFirework(Location loc) {
		playFirework(loc, getRandomEffect());
	}
	
	private static FireworkEffect getRandomEffect() {
		Random gen = new Random();

		return FireworkEffect
				.builder()
				.with(FireworkEffect.Type.BURST)
				.flicker(gen.nextBoolean())
				.trail(gen.nextBoolean())
				.withColor(
						Color.fromRGB(gen.nextInt(255), gen.nextInt(255),
								gen.nextInt(255)))
				.withFade(
						Color.fromRGB(gen.nextInt(255), gen.nextInt(255),
								gen.nextInt(255))).build();
	}

	private static Method getMethod(Class<?> cl, String method) {
		for (Method m : cl.getMethods())
			if (m.getName().equals(method))
				return m;
		return null;
	}

}
