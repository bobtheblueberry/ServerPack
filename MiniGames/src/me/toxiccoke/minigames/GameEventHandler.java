package me.toxiccoke.minigames;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.projectiles.ProjectileSource;

public class GameEventHandler implements Listener {

	// Mini Game Chat
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent chat) {
		Player sender = chat.getPlayer();
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.getName().equals(sender.getName())) {
					chat.setCancelled(true);
					for (GamePlayer mgp : m.getPlayers())
						mgp.getPlayer().sendMessage(
								gp.getTeamColor() + ChatColor.stripColor(gp.getPlayer().getDisplayName()) + ": "
										+ ChatColor.GRAY + chat.getMessage());
					return;
				}

	}

	@EventHandler
	public void onVehicleUpdate(VehicleUpdateEvent event) {
		Location vehicleLoc = event.getVehicle().getLocation().getBlock().getLocation();
		for (GameWorld<?> w : GameLobby.lobby.games) {
			Bounds b = w.getExcessBounds();
			if (b == null)
				continue;
			if (w.getExcessBounds().contains(vehicleLoc.getBlockX(), vehicleLoc.getBlockZ())) {
				w.vehicleUpdate(event);
				return;
			}
		}
	}

	// disable commands
	@EventHandler
	public void onPreEvent(PlayerCommandPreprocessEvent event) {
		String cmd = event.getMessage().toLowerCase();
		if (cmd.length() < 2)
			return;
		if (cmd.startsWith("tell", 1) || cmd.startsWith("msg", 1) || cmd.startsWith("leave", 1)
				|| cmd.startsWith("r", 1) || cmd.startsWith("m", 1) || cmd.startsWith("reply", 1)
				|| cmd.startsWith("say", 1) || cmd.startsWith("party", 1) || cmd.startsWith("friend", 1))
			return;
		Player sender = event.getPlayer();
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.getName().equals(sender.getName())) {
					sender.sendMessage(ChatColor.GOLD + "Do /leave to leave the game");
					event.setCancelled(true);
					return;
				}
	}

	// no hunger
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.equals(event.getEntity().getName())) {
					if (!m.canPlayerHunger(gp))
						event.setCancelled(true);
					return;
				}
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event) {
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.equals(event.getPlayer().getName())) {
					if (!m.canBreakBlock(gp, event)) {
						event.setCancelled(true);
					}
					return;
				}
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event) {
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
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
			for (GameWorld<?> m : GameLobby.lobby.games)
				for (GamePlayer gp : m.getPlayers())
					if (gp.player.equals(p.getName())) {
						m.projectileHit(gp, event);
						return;
					}
		}
	}

	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		for (GameWorld<?> m : GameLobby.lobby.games) {

			Bounds bounds = m.getExcessBounds();
			if (bounds == null)
				continue;
			ArrayList<Block> blocks = new ArrayList<Block>();

			if (bounds.contains(event.getLocation().getBlockX(), event.getLocation().getBlockZ()))
				for (Block b : event.blockList()) {
					event.setYield(1);
					if (!m.canExplodeBlock(b, event.getEntity()))
						blocks.add(b);
				}
			else if (m.worldName.equalsIgnoreCase("amazon"))
				System.out.println(getLocationString(event.getLocation()) + " not in " + bounds);
			for (Block b : blocks)
				event.blockList().remove(b);

		}
	}

	private String getLocationString(Location l) {
		return " X: " + l.getBlockX() + " Y: " + l.getBlockY() + " Z: " + l.getBlockZ();
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.equals(p.getName())) {
					m.onPlayerInteract(gp, event);
					return;
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
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
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
			for (GameWorld<?> m : GameLobby.lobby.games)
				for (GamePlayer gp : m.getPlayers())
					if (gp.player.equals(event.getWhoClicked().getName())) {
						event.setCancelled(true);
						return;
					}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		Entity victim = event.getEntity();
		Entity attacker = event.getDamager();
		boolean other = false;
		if (!(attacker instanceof Player) && !(attacker instanceof Projectile))
			other = true;
		Player at = null;
		if (attacker instanceof Player)
			at = ((Player) attacker);
		else if (!other) {
			ProjectileSource ps = ((Projectile) attacker).getShooter();
			if (ps instanceof Player)
				at = (Player) ps;
			else other = true;
		}
		if (!other)
			main: for (GameWorld<? extends GamePlayer> m : GameLobby.lobby.games)
				for (GamePlayer gp : m.getPlayers())
					if (gp.player.equals(at.getName())) {
						// Don't let the player attack if they are in the game
						// lobby
						if (!m.allowDamage(gp)) {
							event.setCancelled(true);
							at.sendMessage(ChatColor.GOLD + "You cannot attack here!");
							break main;
						}
					}
		if (!(victim instanceof Player))
			return;

		Player v = (Player) victim;
		if (((Damageable) victim).getHealth() - event.getDamage() <= 0)
			for (GameWorld<?> m : GameLobby.lobby.games)
				for (GamePlayer gp : m.getPlayers())
					if (gp.player.equals(v.getName())) {
						// Respawn player instead of having them die
						m.notifyDeath(gp, event.getDamager(), event.getCause());
						event.setCancelled(true);
						return;
					}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.PROJECTILE
				|| event.getCause() == DamageCause.ENTITY_EXPLOSION)
			return;// handled by
					// EntityDamageByEntityEvent
		Entity t = event.getEntity();
		if (!(t instanceof Player))
			return;
		Player player = (Player) t;

		if (((Damageable) player).getHealth() - event.getDamage() > 0)
			return;
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.equals(player.getName())) {
					m.notifyDeath(gp, event);
					event.setCancelled(true);
					return;
				}
	}

	@EventHandler
	public void onEntityQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.equals(player.getName())) {
					m.notifyQuitGame(gp);
					return;
				}
	}
}
