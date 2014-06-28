package me.toxiccoke.minigames;

import java.util.ArrayList;
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
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.getName().equals(sender)) {
					chat.setCancelled(true);
					for (GamePlayer mgp : m.getPlayers()) {
						String msg = gp.getTeamColor() + ChatColor.stripColor(gp.getPlayer().getDisplayName()) + ": " + ChatColor.GRAY + chat.getMessage();
						Bukkit.getLogger().log(Level.INFO, m.getGameName() + "." + m.getWorldName() + " " + msg);
						mgp.getPlayer().sendMessage(msg);
					}
					return;
				}
	}

	private boolean allowCommand(String cmd) {
		String key = "allowed-commands";
		List<String> cmds = MiniGamesPlugin.plugin.getConfig().getStringList(key);
		if (cmds == null || cmds.size() == 0) {
			cmds = new ArrayList<String>();
			cmds.add("tell");
			cmds.add("msg");
			cmds.add("leave");
			cmds.add("r");
			cmds.add("m");
			cmds.add("reply");
			cmds.add("say");
			cmds.add("party");
			cmds.add("friend");
			cmds.add("class");
			MiniGamesPlugin.plugin.getConfig().set(key, cmds);
			MiniGamesPlugin.plugin.saveConfig();
		}
		for (String s : cmds)
			if (s.startsWith(cmd))
				return true;
		return false;
	}

	// disable commands
	@EventHandler
	public void onPreEvent(PlayerCommandPreprocessEvent event) {
		String cmd = event.getMessage().toLowerCase();
		if (cmd.length() < 2)
			return;
		if (allowCommand(cmd.substring(1)))
			return;
		Player sender = event.getPlayer();
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.getName().equals(sender)) {
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
				if (gp.player.equals(event.getEntity())) {
					if (!m.canPlayerHunger(gp))
						event.setCancelled(true);
					return;
				}
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event) {
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.equals(event.getPlayer())) {
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
				if (gp.player.equals(event.getPlayer())) {
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
					if (gp.player.equals(p)) {
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
			for (Block b : blocks)
				event.blockList().remove(b);

		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.equals(p)) {
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
				if (gp.player.equals(event.getPlayer())) {
					event.setCancelled(true);
					return;
				}
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		// diable removing armor
		if ((event.getSlot() == 39 || event.getSlot() == 38 || event.getSlot() == 37 || event.getSlot() == 36) && event.getInventory().getHolder() instanceof HumanEntity)
			for (GameWorld<?> m : GameLobby.lobby.games)
				for (GamePlayer gp : m.getPlayers())
					if (gp.player.equals(event.getWhoClicked())) {
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
		/*
		else if (attacker instanceof HumanEntity) {
			CraftHumanEntity ch = (CraftHumanEntity)attacker;
			Field f;
			try {
				f = ch.getClass().getSuperclass().getSuperclass().getDeclaredField("entity");
				f.setAccessible(true);
				FakeEntity e = (FakeEntity) f.get(ch);
				at = e.getDamager().getPlayer();
			} catch (NoSuchFieldException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			}
		}*/
		else if (!other) {
			ProjectileSource ps = ((Projectile) attacker).getShooter();
			if (ps instanceof Player)
				at = (Player) ps;
			else other = true;
		}
		if (!other)
			main: for (GameWorld<? extends GamePlayer> m : GameLobby.lobby.games)
				for (GamePlayer gp : m.getPlayers())
					if (gp.player.equals(at)) {
						// Don't let the player attack if they are in the game
						// lobby
						if (!m.allowDamage(gp, event))
							event.setCancelled(true);
						break main;
					}

		Player v = (Player) victim;
		if (((Damageable) victim).getHealth() - event.getDamage() <= 0)
			for (GameWorld<?> m : GameLobby.lobby.games)
				for (GamePlayer gp : m.getPlayers())
					if (gp.player.equals(v)) {
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
		for (GameWorld<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.player.equals(player)) { return gp; }
		return null;
	}
}
