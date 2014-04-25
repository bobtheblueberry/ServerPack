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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.ItemStack;

public class PayloadEventHandler implements Listener {

	protected static Player			minetrackSet;
	protected static PayloadGame	game;

	protected static Player			barSet;

	private PayloadGame getGame(int x, int y) {
		for (GameWorld<?> m : GameLobby.lobby.games) {
			if (!(m instanceof PayloadGame))
				continue;
			Bounds b = m.getBounds();
			if (b == null)
				continue;
			if (b.contains(x, y))
				return (PayloadGame) m;
		}
		return null;
	}

	protected static PayloadPlayer getPlayer(Player p) {
		for (GameWorld<?> m : GameLobby.lobby.games) {
			if (!(m instanceof PayloadGame))
				continue;
			for (GamePlayer gp : m.getPlayers())
				if (gp.equals(p)) { return (PayloadPlayer) gp; }
		}
		return null;
	}

	private void messWithPlayer(Player p) {
		p.sendMessage(ChatColor.BLUE + "[Rails] Weeeeeee");
		tpPlayer(p, 0, game.trackulator);
	}

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

	@EventHandler
	public void onEntityCombust(EntityCombustEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();
		PayloadPlayer pp = getPlayer(player);
		if (pp == null)
			return;
		if (!pp.game.canPlayerCombust(pp))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();
		PayloadPlayer pp = getPlayer(player);
		if (pp == null)
			return;
		if (!pp.game.canPlayerTakeDamage(pp))
			event.setCancelled(true);
	}

	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		if (event == null || event.getEntity() == null)
			return;
		Location vehicleLoc = event.getEntity().getLocation();
		PayloadGame game = getGame(vehicleLoc.getBlockX(), vehicleLoc.getBlockZ());
		if (game != null)
			game.explode(event);
	}

	// disable fishing minecarts
	@EventHandler
	public void onFish(PlayerFishEvent event) {
		Player player = event.getPlayer();
		Entity e = event.getCaught();
		if (e == null || event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY || !(e instanceof Minecart))
			return;
		PayloadPlayer pp = getPlayer(player);
		if (pp != null)
			event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		ItemStack itemInHand = event.getPlayer().getItemInHand();
		if (itemInHand == null)
			return;
		PayloadPlayer pp = getPlayer(event.getPlayer());
		if (pp == null)
			return;
		if (itemInHand.getType() == Material.COMMAND) {
			pp.getPlayer().chat("/class");
			return;
		}
		if (pp.dead)
			return;
		if (pp.getPlayerClass() == PayloadClass.PYRO) {
			if (itemInHand.getType() == Material.FIRE) {
				if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
					ClassWeapons.flamethrower(pp);
				else ClassWeapons.airblast(pp);
			}
		} else if (pp.getPlayerClass() == PayloadClass.SOLDIER) {
			if (itemInHand.getType() == Material.HOPPER && pp.weaponTimer.canFire()) {
				if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					ClassWeapons.rocket(pp);
					pp.weaponTimer.fire();
				}
			}
		} else if (pp.getPlayerClass() == PayloadClass.MEDIC) {
			if (itemInHand.getType() == Material.FISHING_ROD) {
				if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
					;// FIXME MediGun
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		PayloadPlayer pp = getPlayer(player);
		if (pp == null)
			return;
		if (!pp.game.canPlayerMove(pp))
			event.setCancelled(true);
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

	@EventHandler
	public void onRegainHealth(EntityRegainHealthEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED)
			return;
		Player player = (Player) event.getEntity();
		PayloadPlayer pp = getPlayer(player);
		if (pp == null)
			return;
		if (!pp.game.canPlayerHealFromHunger(pp))
			event.setCancelled(true);
	}

	@EventHandler
	public void onVehicleCollideEntity(VehicleEntityCollisionEvent event) {
		int x = event.getVehicle().getLocation().getBlockX();
		int y = event.getVehicle().getLocation().getBlockZ();
		PayloadGame game = getGame(x, y);
		if (game != null)
			game.vehicleCollision(event);
	}

	@EventHandler
	public void onVehicleDamage(VehicleDamageEvent event) {
		Location vehicleLoc = event.getVehicle().getLocation();
		PayloadGame game = getGame(vehicleLoc.getBlockX(), vehicleLoc.getBlockZ());
		if (game != null)
			game.vehicleDamage(event);
	}

	@EventHandler
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		Location vehicleLoc = event.getVehicle().getLocation();
		PayloadGame game = getGame(vehicleLoc.getBlockX(), vehicleLoc.getBlockZ());
		if (game != null)
			game.vehicleDestroy(event);
	}

	@EventHandler
	public void onVehiclePlace(VehicleCreateEvent event) {
		int x = event.getVehicle().getLocation().getBlockX();
		int y = event.getVehicle().getLocation().getBlockZ();

		PayloadGame game = getGame(x, y);
		if (game != null)
			game.vehicleCreated(event);

	}

	@EventHandler
	public void onVehicleUpdate(VehicleUpdateEvent event) {
		Location vehicleLoc = event.getVehicle().getLocation();
		PayloadGame game = getGame(vehicleLoc.getBlockX(), vehicleLoc.getBlockZ());
		if (game != null)
			game.vehicleUpdate(event);
	}

	@EventHandler
	public void onVehicleMove(VehicleMoveEvent event) {
		Location vehicleLoc = event.getVehicle().getLocation();
		PayloadGame game = getGame(vehicleLoc.getBlockX(), vehicleLoc.getBlockZ());
		if (game != null)
			game.vehicleMove(event);
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		Item i = event.getItem();
		int x = i.getLocation().getBlockX();
		int z = i.getLocation().getBlockZ();
		PayloadGame g = getGame(x, z);
		if (g == null)
			return;
		PayloadPlayer p = getPlayer(event.getPlayer());
		if (p != null)
			p.game.pickupItem(p, event);
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if (p == null)
			return;
		PayloadPlayer pl = getPlayer(p);
		if (pl == null)
			return;
		pl.game.dropItem(pl, e);
	}

	@EventHandler
	public void entityShootBow(EntityShootBowEvent event) {
		if (event.getEntity() == null || !(event.getEntity() instanceof Player))
			return;
		PayloadPlayer player = getPlayer((Player) event.getEntity());
		if (player == null)
			return;
		player.game.bowShoot(player, event);
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
		else p.sendMessage(ChatColor.BLUE + "[Rails] Finished");
	}

}
