package me.toxiccoke.minigames;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
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
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public class GameEventHandler implements Listener {

	protected List<String> allowedCmds;

	public GameEventHandler() {
		String key = "allowed-commands";

		allowedCmds = MiniGamesPlugin.plugin.getConfig().getStringList(key);
		if (allowedCmds.size() < 1) {
			MiniGamesPlugin.plugin.getConfig().set(key,
					new String[] { "tell", "msg", "leave", "r", "m", "reply", "say", "party" });
			allowedCmds = MiniGamesPlugin.plugin.getConfig().getStringList(key);
			MiniGamesPlugin.plugin.saveConfig();
		}
	}

	// Mini Game Chat
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent chat) {
		String message = chat.getMessage();
		if (message.startsWith("#")) {// Does this for all player not just ones
										// in minigames
			chat.setMessage(message.substring(1));
			return;
		}
		Player sender = chat.getPlayer();
		for (GameArena<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.getName().equals(sender.getName())) {
					chat.setCancelled(true);
					for (GamePlayer mgp : m.getPlayers()) {
						String msg = gp.getTeamColor() + ChatColor.stripColor(gp.getPlayer().getDisplayName()) + ": "
								+ ChatColor.GRAY + chat.getMessage();
						Bukkit.getLogger().log(Level.INFO, m.getGameName() + "." + m.getArenaName() + " " + msg);
						mgp.getPlayer().sendMessage(msg);
					}
					return;
				}
	}

	private boolean allowCommand(String cmd) {
		for (String s : allowedCmds)
			if (s.startsWith(cmd))
				return true;
		return false;
	}

	// disable commands when in minigame
	@EventHandler
	public void onPreEvent(PlayerCommandPreprocessEvent event) {
		String cmd = event.getMessage().toLowerCase();
		if (cmd.length() < 1)
			return;
		String c = cmd.substring(1);
		if (c.equals("leave") || allowCommand(c))
			return;
		Player sender = event.getPlayer();
		for (GameArena<?> m : GameLobby.lobby.games)
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
		for (GameArena<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.getName().equals(event.getEntity().getName())) {
					if (!m.canPlayerHunger(gp))
						event.setCancelled(true);
					return;
				}
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event) {
		for (GameArena<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.getName().equals(event.getPlayer().getName())) {
					if (!m.canBreakBlock(gp, event)) {
						event.setCancelled(true);
					}
					return;
				}
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event) {
		for (GameArena<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.getName().equals(event.getPlayer().getName())) {
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
			for (GameArena<?> m : GameLobby.lobby.games)
				for (GamePlayer gp : m.getPlayers())
					if (gp.player.getName().equals(p.getName())) {
						m.projectileHit(gp, event);
						return;
					}
		}
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event) {
		for (GameArena<?> m : GameLobby.lobby.games) {
			Bounds bounds = m.getExcessBounds();
			if (bounds == null)
				continue;
			LinkedList<Block> blocks = new LinkedList<Block>();
			if (bounds.contains(event.getBlock().getLocation())) {
				event.setYield(1);
				for (Block b : event.blockList()) {
					if (!m.canExplodeBlock(b, null))
						blocks.add(b);
				}
			}
			for (Block b : blocks)
				event.blockList().remove(b);
		}
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for (GameArena<?> m : GameLobby.lobby.games) {
			Bounds bounds = m.getExcessBounds();
			if (bounds == null)
				continue;
			LinkedList<Block> blocks = new LinkedList<Block>();
			if (bounds.contains(event.getLocation())) {
				event.setYield(1);
				for (Block b : event.blockList()) {
					if (!m.canExplodeBlock(b, event.getEntity()))
						blocks.add(b);
				}
			}
			for (Block b : blocks)
				event.blockList().remove(b);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		for (GameArena<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.getName().equals(p.getName())) {
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
		for (GameArena<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.getName().equals(event.getPlayer().getName())) {
					event.setCancelled(true);
					return;
				}
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		// diable removing armor
		if ((event.getSlot() == 39 || event.getSlot() == 38 || event.getSlot() == 37 || event.getSlot() == 36)
				&& event.getInventory().getHolder() instanceof HumanEntity)
			for (GameArena<?> m : GameLobby.lobby.games)
				for (GamePlayer gp : m.getPlayers())
					if (gp.player.getName().equals(event.getWhoClicked().getName())) {
						event.setCancelled(true);
						return;
					}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.isCancelled())
			return;
		Entity victim = event.getEntity();
		if (!(victim instanceof Player))
			return;
		Entity attacker = event.getDamager();
		boolean other = false;
		if (!(attacker instanceof Player) && !(attacker instanceof Projectile) && !(attacker instanceof HumanEntity))
			other = true;
		Player at = null;
		if (attacker instanceof Player)
			at = ((Player) attacker);
		else if (attacker instanceof HumanEntity) {
			/*
			 * CraftHumanEntity ch = (CraftHumanEntity) attacker; Field f; try {
			 * f = ch.getClass(). getSuperclass(). getSuperclass().
			 * getDeclaredField("entity" ); f.setAccessible(true); FakeEntity e
			 * = (FakeEntity) f.get(ch); at = e.getDamager().getPlayer( ); }
			 * catch (NoSuchFieldException e1) { e1.printStackTrace(); } catch
			 * (SecurityException e1) { e1.printStackTrace(); } catch
			 * (IllegalArgumentException e1) { e1.printStackTrace(); } catch
			 * (IllegalAccessException e1) { e1.printStackTrace(); }
			 */
		} else if (!other) {
			ProjectileSource ps = ((Projectile) attacker).getShooter();
			if (ps instanceof Player)
				at = (Player) ps;
			else
				other = true;
		}
		if (!other)
			main: for (GameArena<? extends GamePlayer> m : GameLobby.lobby.games)
				for (GamePlayer gp : m.getPlayers())
					if (gp.player.getName().equals(at.getName())) {
						// Don't let the player attack if they are in the game
						// lobby
						if (!m.allowDamage(gp, event))
							event.setCancelled(true);
						break main;
					}

		Player v = (Player) victim;
		if (((Damageable) victim).getHealth() - event.getDamage() <= 0)
			for (GameArena<?> m : GameLobby.lobby.games)
				for (GamePlayer gp : m.getPlayers())
					if (gp.player.getName().equals(v.getName())) {
						// Respawn player instead of having them die
						m.notifyDeath(gp, event.getDamager(), event.getCause());
						event.setCancelled(true);
						fixArmor(gp.getPlayer());
						return;
					}
	}

	private void fixArmor(Player p) {
		ItemStack[] armor = p.getInventory().getArmorContents();
		for (int i = 0; i < 4; i++) {
			ItemStack is = armor[i];
			if (is == null)
				continue;
			is.setDurability((short) 0);
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled())
			return;
		if (event instanceof EntityDamageByEntityEvent)
			return;// handled by
					// EntityDamageByEntityEvent
		Entity t = event.getEntity();
		if (!(t instanceof Player))
			return;
		Player player = (Player) t;

		if (((Damageable) player).getHealth() - event.getDamage() > 0)
			return;
		GamePlayer pl = getPlayer(player);
		if (pl != null) {
			pl.game.notifyDeath(pl, event);
			event.setCancelled(true);
			fixArmor(player);
			return;
		}
	}

	@EventHandler
	public void onEntityQuit(PlayerQuitEvent event) {
		GamePlayer pl = getPlayer(event.getPlayer());
		if (pl != null)
			pl.game.notifyQuitGame(pl);
	}

	private GamePlayer getPlayer(Player player) {
		for (GameArena<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.getName().equals(player.getName())) {
					return gp;
				}
		return null;
	}
}
