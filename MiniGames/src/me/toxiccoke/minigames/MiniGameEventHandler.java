package me.toxiccoke.minigames;

import org.bukkit.ChatColor;
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
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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
	// exploding arrows
	public void projectileHit(ProjectileHitEvent e) {
		Projectile proj = e.getEntity();

		if (proj instanceof Arrow) {
			Arrow arrow = (Arrow) proj;
			// if (arrow.getShooter() instanceof Player) {
			// Player p = (Player) arrow.getShooter();
			arrow.getWorld().createExplosion(arrow.getLocation(), (int) (Math.random() * 2));
			// }
		}
	}

	// Disable Riding players
	@EventHandler(priority = EventPriority.LOW)
	public void onInteract(PlayerInteractEntityEvent event) {
		Player p = event.getPlayer();
		if (event.getRightClicked() == null || !(event.getRightClicked() instanceof Player)) return;
		if (p.getVehicle() != null) return;
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
		if (attacker instanceof Player) main: for (MiniGameWorld m : MiniGameLobby.lobby.games)
			for (MiniGamePlayer gp : m.getPlayers())
				if (gp.player.equals(((Player) attacker).getName())) {
					// Don't let the player attack if they are in the game lobby
			if (!m.allowDamage(gp)) {
				event.setCancelled(true);
				gp.getPlayer().sendMessage(ChatColor.GOLD + "You cannot attack here!");
				break main;
			}
		}
		if (!(victim instanceof Player) && !(attacker instanceof Player)) return;
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
		if (event.getCause() == DamageCause.ENTITY_ATTACK) return;// handled by
																	// EntityDamageByEntityEvent
		Entity t = event.getEntity();
		if (!(t instanceof Player)) return;
		Player player = (Player) t;

		if (((Damageable) player).getHealth() - event.getDamage() > 0) return;
		for (MiniGameWorld m : MiniGameLobby.lobby.games)
			for (MiniGamePlayer gp : m.getPlayers())
				if (gp.player.equals(player.getName())) {
					m.notifyDeath(gp, event.getCause());
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
