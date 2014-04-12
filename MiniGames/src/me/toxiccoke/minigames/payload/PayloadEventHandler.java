package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.Bounds;
import me.toxiccoke.minigames.GameLobby;
import me.toxiccoke.minigames.GamePlayer;
import me.toxiccoke.minigames.GameWorld;
import me.toxiccoke.minigames.MiniGamesPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;

public class PayloadEventHandler implements Listener {

	protected static Player			minetrackSet;
	protected static PayloadGame	game;

	protected static Player			barSet;

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		if (p == null)
			return;
		if (barSet != null) {
			game.bars.add(event.getBlock().getLocation());
			p.sendMessage(ChatColor.GREEN + "Added bar");
			event.setCancelled(true);
			return;
		}
		if (minetrackSet == null || !event.getPlayer().equals(minetrackSet))
			return;
		if (event.getBlock().getType() != Material.RAILS) {
			p.sendMessage(ChatColor.RED + "That block is not Rails! Cancelled operation.");
		} else {
			event.setCancelled(true);
			game.trackulator.trackulate(event.getBlock().getLocation());
			game.trackulator.save();
			p.sendMessage(ChatColor.GREEN + "Set track start (" + game.trackulator.rails.size() + " rails)");
			messWithPlayer(minetrackSet);
		}
		minetrackSet = null;
		game = null;
	}

	private void messWithPlayer(Player p) {
		p.sendMessage(ChatColor.BLUE + "[Rails] Weeeeeee");
		tpPlayer(p, 0, game.trackulator);
	}

	private void tpPlayer(final Player p, final int i, final Minetrackulator t) {
		Location l = t.rails.get(i).getLocation().clone().add(0.5, 0, 0.5);
		l.setYaw(p.getLocation().getYaw());
		l.setPitch(p.getLocation().getPitch());
		p.teleport(l);
		if (i + 1 < t.rails.size())
			Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, new Runnable() {

				@Override
				public void run() {
					tpPlayer(p, i + 1, t);
				}
			}, 1);
		else

		p.sendMessage(ChatColor.BLUE + "[Rails] Finished");
	}

	@EventHandler
	public void onVehiclePlace(VehicleCreateEvent event) {
		int x = event.getVehicle().getLocation().getBlockX();
		int y = event.getVehicle().getLocation().getBlockZ();
		for (GameWorld<?> m : GameLobby.lobby.games) {
			if (!(m instanceof PayloadGame))
				continue;
			Bounds b = m.getBounds();
			if (b == null)
				continue;
			if (b.contains(x, y))
				((PayloadGame) m).vehicleCreated(event);
		}
	}

	@EventHandler
	public void onVehicleCollideEntity(VehicleEntityCollisionEvent event) {
		int x = event.getVehicle().getLocation().getBlockX();
		int y = event.getVehicle().getLocation().getBlockZ();
		for (GameWorld<?> m : GameLobby.lobby.games)
			if (m instanceof PayloadGame) {
				Bounds b = m.getBounds();
				if (b == null)
					continue;
				if (b.contains(x, y))
					((PayloadGame) m).vehicleCollision(event);
			}
	}

	@EventHandler
	public void onVehicleUpdate(VehicleUpdateEvent event) {
		Location vehicleLoc = event.getVehicle().getLocation();
		for (GameWorld<?> w : GameLobby.lobby.games) {
			if (!(w instanceof PayloadGame))
				continue;
			Bounds b = w.getBounds();
			if (b == null)
				continue;
			if (b.contains(vehicleLoc.getBlockX(), vehicleLoc.getBlockZ())) {
				((PayloadGame) w).vehicleUpdate(event);
				return;
			}
		}
	}

	@EventHandler
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		Location vehicleLoc = event.getVehicle().getLocation();
		for (GameWorld<?> w : GameLobby.lobby.games) {
			if (!(w instanceof PayloadGame))
				continue;
			Bounds b = w.getBounds();
			if (b == null)
				continue;
			if (b.contains(vehicleLoc.getBlockX(), vehicleLoc.getBlockZ())) {
				((PayloadGame) w).vehicleDestroy(event);
				return;
			}
		}
	}

	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event) {
		Location vehicleLoc = event.getVehicle().getLocation();
		for (GameWorld<?> w : GameLobby.lobby.games) {
			if (!(w instanceof PayloadGame))
				continue;
			Bounds b = w.getBounds();
			if (b == null)
				continue;
			if (b.contains(vehicleLoc.getBlockX(), vehicleLoc.getBlockZ())) {
				((PayloadGame) w).vehicleDamage(event);
				return;
			}
		}
	}

	@EventHandler
	public void onRegainHealth(EntityRegainHealthEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();
		for (GameWorld<?> m : GameLobby.lobby.games) {
			if (!(m instanceof PayloadGame))
				continue;
			for (GamePlayer gp : m.getPlayers())
				if (gp.getName().equals(player.getName())) {
					if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED)
						if (!((PayloadGame) m).canPlayerHealFromHunger(gp))
							event.setCancelled(true);
					return;
				}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();
		for (GameWorld<?> m : GameLobby.lobby.games) {
			if (!(m instanceof PayloadGame))
				continue;
			for (GamePlayer gp : m.getPlayers())
				if (gp.getName().equals(player.getName())) {
					if (!((PayloadGame) m).canPlayerTakeDamage((PayloadPlayer) gp))
						event.setCancelled(true);
					return;
				}
		}
	}

	@EventHandler
	public void onEntityCombust(EntityCombustEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();
		for (GameWorld<?> m : GameLobby.lobby.games) {
			if (!(m instanceof PayloadGame))
				continue;
			for (GamePlayer gp : m.getPlayers())
				if (gp.getName().equals(player.getName())) {
					if (!((PayloadGame) m).canPlayerCombust((PayloadPlayer) gp))
						event.setCancelled(true);
					return;
				}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		for (GameWorld<?> m : GameLobby.lobby.games) {
			if (!(m instanceof PayloadGame))
				continue;
			for (GamePlayer gp : m.getPlayers())
				if (gp.getName().equals(player.getName())) {
					if (!((PayloadGame) m).canPlayerMove((PayloadPlayer) gp))
						return;
				}
		}
	}

	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		Location vehicleLoc = event.getEntity().getLocation();
		for (GameWorld<?> w : GameLobby.lobby.games) {
			if (!(w instanceof PayloadGame))
				continue;
			Bounds b = w.getBounds();
			if (b == null)
				continue;
			if (b.contains(vehicleLoc.getBlockX(), vehicleLoc.getBlockZ())) {
				((PayloadGame) w).explode(event);
				return;
			}
		}
	}

	@EventHandler
	public void onRedstone(BlockRedstoneEvent event) {
		if (event.getNewCurrent() < 1)
			return;
		Block block = event.getBlock();
		if (block.getType() != Material.DETECTOR_RAIL)
			return;
		Location l = block.getLocation();
		for (GameWorld<?> w : GameLobby.lobby.games) {
			if (!(w instanceof PayloadGame))
				continue;
			Bounds b = w.getBounds();
			if (b == null)
				continue;
			if (b.contains(l.getBlockX(), l.getBlockZ())) {
				PayloadGame pw = (PayloadGame) w;
				for (Location m : pw.checkpoints) {
					if (m.getBlock().equals(block)) {
						pw.checkpoint(m);
						return;
					}
				}
				return;
			}
		}
	}

}
