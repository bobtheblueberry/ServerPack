package me.toxiccoke.minigames.payload;
public class CustomDamager {}
/* 

import java.lang.reflect.Field;

import net.minecraft.server.v1_7_R3.ChunkCoordinates;
import net.minecraft.server.v1_7_R3.DamageSource;
import net.minecraft.server.v1_7_R3.EntityHuman;
import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.IChatBaseComponent;
import net.minecraft.server.v1_7_R3.ItemStack;
import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
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
		DamageSource reason = DamageSource.playerAttack(new FakeEntity(getGameProfile()));
		entity.damageEntity(reason, (float) amount);
	}

	public static void damage(PayloadPlayer source, PayloadPlayer player, float damage) {
		CraftServer server = (CraftServer) Bukkit.getServer();

		CustomDamager dmg = new CustomDamager(server, getEntity(player));
		dmg.player = player;
		dmg.source = source;
		dmg.doDamage(damage);
	}

	private EntityPlayer getEntity() {
		return getEntity(player);
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

	private GameProfile getGameProfile() {
		EntityPlayer p = getEntity();
		Field f;
		try {
			f = p.getClass().getSuperclass().getDeclaredField("i");
			f.setAccessible(true);
			return (GameProfile) f.get(p);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public class FakeEntity extends EntityHuman {

		public FakeEntity(GameProfile prof) {
			super(getEntity().world, prof);
		}

		public PayloadPlayer getPlayer() {
			return getPayloadPlayer();
		}

		public PayloadPlayer getDamager() {
			return CustomDamager.this.getDamager();
		}

		@Override
		public ItemStack bd() {
			return getEntity().bd();
		}

		@Override
		public ItemStack[] getEquipment() {
			return getEntity().getEquipment();
		}

		@Override
		public ItemStack getEquipment(int arg0) {
			return getEntity().getEquipment(arg0);
		}

		@Override
		public void setEquipment(int arg0, ItemStack arg1) {
			getEntity().setEquipment(arg0, arg1);
		}

		@Override
		public boolean a(int arg0, String arg1) {
			return getEntity().a(arg0, arg1);
		}

		@Override
		public ChunkCoordinates getChunkCoordinates() {
			return getEntity().getChunkCoordinates();
		}

		@Override
		public void sendMessage(IChatBaseComponent arg0) {
			getEntity().sendMessage(arg0);
		}

	}
}
*/