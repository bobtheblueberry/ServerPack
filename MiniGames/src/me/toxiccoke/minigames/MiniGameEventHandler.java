package me.toxiccoke.minigames;

import java.awt.Rectangle;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MiniGameEventHandler implements Listener {

	@EventHandler
	public void blockBreak(BlockBreakEvent event) {
		for (MiniGameWorld m : MiniGameLobby.lobby.games)
			for (MiniGamePlayer gp : m.getPlayers())
				if (gp.player.equals(event.getPlayer().getName())) {
					if (!m.canBreakBlock(gp, event)) {
						event.setCancelled(true);
					}
					return;
				}
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event) {
		for (MiniGameWorld m : MiniGameLobby.lobby.games)
			for (MiniGamePlayer gp : m.getPlayers())
				if (gp.player.equals(event.getPlayer().getName())) {
					if (!m.canPlaceBlock(gp, event)) {
						event.setBuild(false);
					}
					return;
				}
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event) {
		// exploding arrows
		Projectile proj = event.getEntity();
		if (!(proj instanceof Arrow))
			return;
		Arrow arrow = (Arrow) proj;
		if (arrow.getShooter() instanceof Player) {
			Player p = (Player) arrow.getShooter();
			for (MiniGameWorld m : MiniGameLobby.lobby.games)
				for (MiniGamePlayer gp : m.getPlayers())
					if (gp.player.equals(p.getName())) {
						m.projectileHit(gp, event);
						return;
					}
		}
	}

	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		for (MiniGameWorld m : MiniGameLobby.lobby.games) {

			Rectangle bounds = m.getExcessBounds();
			if (bounds == null)
				continue;
			ArrayList<Block> blocks = new ArrayList<Block>();

			if (bounds.contains(event.getLocation().getX(), event.getLocation().getZ()))
				for (Block b : event.blockList()) {
					event.setYield(1);
					if (!m.canExplodeBlock(b))
						blocks.add(b);
				}
			for (Block b : blocks)
				event.blockList().remove(b);

		}
	}

	// Disable Riding players
	@EventHandler(priority = EventPriority.LOW)
	public void onInteract(PlayerInteractEntityEvent event) {
		Player p = event.getPlayer();
		if (event.getRightClicked() == null || !(event.getRightClicked() instanceof Player))
			return;
		if (p.getVehicle() != null)
			return;
		for (MiniGameWorld m : MiniGameLobby.lobby.games)
			for (MiniGamePlayer gp : m.getPlayers())
				if (gp.player.equals(event.getPlayer().getName())) {
					event.setCancelled(true);
					return;
				}
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		// diable removing armor
		if ((event.getSlot() == 39 || event.getSlot() == 38 || event.getSlot() == 37 || event.getSlot() == 36)
				&& event.getInventory().getHolder() instanceof HumanEntity)
			for (MiniGameWorld m : MiniGameLobby.lobby.games)
				for (MiniGamePlayer gp : m.getPlayers())
					if (gp.player.equals(event.getWhoClicked().getName())) {
						event.setCancelled(true);
						return;
					}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		Entity attacker = event.getDamager();
		if (attacker instanceof Player || attacker instanceof Arrow) {
			Player at;
			if (attacker instanceof Player)
				at = ((Player) attacker);
			else at = (Player) ((Arrow) attacker).getShooter();
			main: for (MiniGameWorld m : MiniGameLobby.lobby.games)
				for (MiniGamePlayer gp : m.getPlayers())
					if (gp.player.equals(at.getName())) {
						// Don't let the player attack if they are in the game
						// lobby
						if (!m.allowDamage(gp)) {
							event.setCancelled(true);
							at.sendMessage(ChatColor.GOLD + "You cannot attack here!");
							break main;
						}
					}
		}
		if (!(victim instanceof Player) && !(attacker instanceof Player))
			return;
		if (victim instanceof Player && ((Damageable) victim).getHealth() - event.getDamage() < 1)
			for (MiniGameWorld m : MiniGameLobby.lobby.games)
				for (MiniGamePlayer gp : m.getPlayers())
					if (gp.player.equals(((Player) victim).getName())) {
						// Respawn player instead of having them die
						m.notifyDeath(gp, event.getDamager(), event.getCause());
						event.setCancelled(true);
						return;
					}

	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.ENTITY_ATTACK)
			return;// handled by
					// EntityDamageByEntityEvent
		Entity t = event.getEntity();
		if (!(t instanceof Player))
			return;
		Player player = (Player) t;

		if (((Damageable) player).getHealth() - event.getDamage() > 0)
			return;
		for (MiniGameWorld m : MiniGameLobby.lobby.games)
			for (MiniGamePlayer gp : m.getPlayers())
				if (gp.player.equals(player.getName())) {
					m.notifyDeath(gp, event);
					event.setCancelled(true);
					return;
				}
	}

	@EventHandler
	public void onEntityQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		for (MiniGameWorld m : MiniGameLobby.lobby.games)
			for (MiniGamePlayer gp : m.getPlayers())
				if (gp.player.equals(player.getName())) {
					m.notifyQuitGame(gp);
					return;
				}
	}
}
