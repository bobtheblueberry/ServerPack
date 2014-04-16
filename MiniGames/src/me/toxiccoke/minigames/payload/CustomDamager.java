package me.toxiccoke.minigames.payload;

import java.lang.reflect.Field;

import net.minecraft.server.v1_7_R3.DamageSource;
import net.minecraft.server.v1_7_R3.EntityLiving;
import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.ItemStack;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class CustomDamager extends CraftPlayer {

	private PayloadPlayer	player, source;

	private CustomDamager(CraftServer server, EntityPlayer entity) {
		super(server, entity);
	}

	public PayloadPlayer getPayloadPlayer() {
		return player;
	}

	public PayloadPlayer getDamager() {
		return source;
	}

	private void doDamage(double amount) {
		DamageSource reason = DamageSource.mobAttack(new FakeEntity());
		entity.damageEntity(reason, (float) amount);
	}

	public static void damage(PayloadPlayer source, PayloadPlayer player, float damage) {
		CraftServer server = (CraftServer) Bukkit.getServer();

		CustomDamager dmg = new CustomDamager(server, getEntity(player));
		dmg.player = player;
		dmg.source = source;
		dmg.doDamage(damage);
	}

	private static EntityPlayer getEntity(PayloadPlayer player) {
		Player p = player.getPlayer();
		EntityPlayer entity;
		try {
			Class<?> c = p.getClass().getSuperclass().getSuperclass().getSuperclass();
			Field f = c.getDeclaredField("entity");
			f.setAccessible(true);
			entity = (EntityPlayer) f.get(p);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			System.out.println("[MiniGames]Error! Cannot use reflect to damage players " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		return entity;
	}

	public class FakeEntity extends EntityLiving {

		public FakeEntity() {
			super(null);

		}

		public PayloadPlayer getPlayer() {
			return getPayloadPlayer();
		}

		public PayloadPlayer getSource() {
			return getSource();
		}
		
		public CraftEntity getBukkitEntity() {
			return null;
		}
		@Override
		public ItemStack bd() {
			return null;
		}

		@Override
		public ItemStack[] getEquipment() {
			return null;
		}

		@Override
		public ItemStack getEquipment(int arg0) {
			return null;
		}

		@Override
		public void setEquipment(int arg0, ItemStack arg1) {}

	}
}
