package me.toxiccoke.minigames.util;

import java.lang.reflect.Field;

import net.minecraft.server.v1_7_R1.DataWatcher;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_7_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_7_R1.PacketPlayOutSpawnEntityLiving;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

// shiddy fried code

public class BarAPI {
	public static final int	ENTITY_ID	= 1234;

	public static void displayText(final String text, final Player player, int health) {
		PacketPlayOutSpawnEntityLiving mobPacket = getMobPacket(text, player.getLocation().clone().add(0, -300, 0),
				health);
		sendPacket(player, mobPacket);

		// mobPacket = getMobPacket(text, player.getLocation(), health);
		// sendPacket(player, mobPacket);
		// DataWatcher watcher2 = getWatcher(text, 300);
		// PacketPlayOutEntityMetadata metaPacket2 =
		// getMetadataPacket(watcher2);
		// sendPacket(player, metaPacket2);
	}

	public static void setBarHealth(Player player, int health) {
		DataWatcher watcher = getWatcher(null, health);
		PacketPlayOutEntityMetadata metaPacket2 = getMetadataPacket(watcher);
		sendPacket(player, metaPacket2);
	}
	public static void setBarText(Player player, String text) {
		DataWatcher watcher = getWatcher(text, -1);
		PacketPlayOutEntityMetadata metaPacket2 = getMetadataPacket(watcher);
		sendPacket(player, metaPacket2);
	}
	private static PacketPlayOutEntityDestroy getDestroyEntityPacket() {
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy();

		Field a = getField(packet.getClass(), "a");
		a.setAccessible(true);
		try {
			a.set(packet, new int[] { ENTITY_ID });
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return packet;
	}

	private static Field getField(Class<?> cl, String field_name) {
		try {
			Field field = cl.getDeclaredField(field_name);
			return field;
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static PacketPlayOutEntityMetadata getMetadataPacket(DataWatcher watcher) {
		PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata();

		Field a = getField(metaPacket.getClass(), "a");
		a.setAccessible(true);
		try {
			a.set(metaPacket, ENTITY_ID);
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}

		try {
			Field b = PacketPlayOutEntityMetadata.class.getDeclaredField("b");
			b.setAccessible(true);
			b.set(metaPacket, watcher.c());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return metaPacket;
	}

	// Accessing packets
	private static PacketPlayOutSpawnEntityLiving getMobPacket(String text, Location loc, int health) {
		PacketPlayOutSpawnEntityLiving mobPacket = new PacketPlayOutSpawnEntityLiving();

		try {
			Field a = getField(mobPacket.getClass(), "a");
			a.setAccessible(true);
			a.set(mobPacket, ENTITY_ID);

			Field b = getField(mobPacket.getClass(), "b");
			b.setAccessible(true);
			b.set(mobPacket, (byte) 63);// ender dragon

			Field c = getField(mobPacket.getClass(), "c");
			c.setAccessible(true);
			c.set(mobPacket, (int) Math.floor(loc.getBlockX() * 32.0D));

			Field d = getField(mobPacket.getClass(), "d");
			d.setAccessible(true);
			d.set(mobPacket, (int) Math.floor(loc.getBlockY() * 32.0D));

			Field e = getField(mobPacket.getClass(), "e");
			e.setAccessible(true);
			e.set(mobPacket, (int) Math.floor(loc.getBlockZ() * 32.0D));

			Field f = getField(mobPacket.getClass(), "f");
			f.setAccessible(true);
			f.set(mobPacket, (byte) 0);

			Field g = getField(mobPacket.getClass(), "g");
			g.setAccessible(true);
			g.set(mobPacket, (byte) 0);

			Field h = getField(mobPacket.getClass(), "h");
			h.setAccessible(true);
			h.set(mobPacket, (byte) 0);

			Field i = getField(mobPacket.getClass(), "i");
			i.setAccessible(true);
			i.set(mobPacket, (byte) 0);

			Field j = getField(mobPacket.getClass(), "j");
			j.setAccessible(true);
			j.set(mobPacket, (byte) 0);

			Field k = getField(mobPacket.getClass(), "k");
			k.setAccessible(true);
			k.set(mobPacket, (byte) 0);

		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}

		DataWatcher watcher = getWatcher(text, health);

		try {
			Field t = PacketPlayOutSpawnEntityLiving.class.getDeclaredField("l");
			t.setAccessible(true);
			t.set(mobPacket, watcher);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return mobPacket;
	}

	private static DataWatcher getWatcher(String text, int health) {
		DataWatcher watcher = new DataWatcher(null);

		watcher.a(0, (Byte) (byte) 0x20); // Flags, 0x20 = invisible
		if (health >= 0)
		watcher.a(6, (Float) (float) health);
		if (text != null)
		watcher.a(10, text); // Entity name
		watcher.a(11, (Byte) (byte) 1); // Show name, 1 = show, 0 = don't show
		watcher.a(16, (Integer) (int) health); // Dragon health, 200 = full
		// health
		return watcher;
	}

	public static void destroy(Player player) {
		PacketPlayOutEntityDestroy destroyEntityPacket = getDestroyEntityPacket();
		sendPacket(player, destroyEntityPacket);
	}

	private static void sendPacket(Player player, Packet packet) {
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

		entityPlayer.playerConnection.sendPacket(packet);
	}
}
