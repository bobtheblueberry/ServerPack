package me.toxiccoke.tokenshop;

import java.util.ArrayList;

import me.toxiccoke.servercore.FriendAPI;
import me.toxiccoke.servercore.FriendAPI.Person;
import me.toxiccoke.servercore.FriendAPI.Person.Value;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class HatHandler implements Listener {

	private static final int	MOB_SIZE		= 2;
	private static final String	KEY				= "mob_hat";
	private static final Value	NO_HAT			= new Value(0);
	private static final Value	SLIME_HAT		= new Value(1);
	private static final Value	MAGMACUBE_HAT	= new Value(2);

	private static void loadHat(final Player p) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(TokenShop.plugin, new Runnable() {
			public void run() {
				Person per = FriendAPI.get(p.getName());
				if (per == null) return;
				Value v = per.getVal(KEY);
				if (v == null) return;
				if (v.equals(SLIME_HAT)) setSlimeHat(p);
				else if (v.equals(MAGMACUBE_HAT)) setMagmacubeHat(p);
			}
		}, 10L);
	}
	
	@EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
		loadHat(event.getPlayer());
	}
    	 
	// Riding players
	@EventHandler
	public void onInteract(PlayerInteractEntityEvent e) {
		Player p = e.getPlayer();
		if (e.getRightClicked() == null || !(e.getRightClicked() instanceof Player)) return;
		if (p.getVehicle() != null) return;
		Player target = (Player) e.getRightClicked();
		// no riding your rider
		if (p.getPassenger() != null && p.getPassenger() instanceof Player && ((Player)p.getPassenger()).getName() == target.getName())
		{
			p.sendMessage(ChatColor.YELLOW + "You can't do that!");
			return;
		}
		scheduleHat(target, p);
		p.sendMessage(ChatColor.YELLOW + "You're riding " + target.getName());
	}
	
	//remove mob hat
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		Entity passenger = p.getPassenger();
		if (passenger == null || (passenger.getType() != EntityType.SLIME && passenger.getType() != EntityType.MAGMA_CUBE))
			return;
		passenger.remove();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent evt) {
		Entity t = evt.getPlayer().getPassenger();
		if (t == null || (t.getType() != EntityType.SLIME && t.getType() != EntityType.MAGMA_CUBE)) return;
		t.remove();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt) {
		loadHat(evt.getPlayer());
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent evt) {
		Entity e = evt.getEntity();
		if (e == null) return;
		EntityType t = e.getType();
		if (t != EntityType.SLIME && t != EntityType.MAGMA_CUBE && t != EntityType.PLAYER) return;
		for (Player p : Bukkit.getServer().getOnlinePlayers())
			if (p.getPassenger() != null && p.getPassenger().getEntityId() == e.getEntityId()) evt.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent evt) {
		// prevent hat mobs from damaging players
		Entity e = evt.getEntity();
		Entity hat = evt.getDamager();
		if (e == null || hat == null) return;
		if (hat.getType() != EntityType.SLIME && hat.getType() != EntityType.MAGMA_CUBE)
			return;
		if (!(e instanceof Player))
			return;
		for (Player p : Bukkit.getServer().getOnlinePlayers())
			if (p.getPassenger() != null && p.getPassenger().getEntityId() == hat.getEntityId()) {evt.setCancelled(true);return;}
	}

	public static void setSlimeHat(Player p) {
		if (p.getPassenger() != null && p.getPassenger().getType() == EntityType.SLIME) return;
		/* SPAWN SLIME */
		Slime slime = (Slime) p.getWorld().spawnEntity(getLocation(p), EntityType.SLIME);
		slime.setSize(MOB_SIZE);
		slime.getWorld().playEffect(slime.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);

		FriendAPI.getAdd(p.getName()).setVal(KEY, SLIME_HAT);
		scheduleHat(p, slime);
	}

	public static void setMagmacubeHat(Player p) {
		if (p.getPassenger() != null && p.getPassenger().getType() == EntityType.MAGMA_CUBE) return;
		/* SPAWN MAGMACUBE */
		MagmaCube magmacube = (MagmaCube) p.getWorld().spawnEntity(getLocation(p), EntityType.MAGMA_CUBE);
		magmacube.setSize(MOB_SIZE);
		magmacube.setMaxHealth(100.0D);
		magmacube.getWorld().playEffect(getLocation(p), Effect.STEP_SOUND, Material.REDSTONE_WIRE);

		FriendAPI.getAdd(p.getName()).setVal(KEY, MAGMACUBE_HAT);
		scheduleHat(p, magmacube);
	}

	public static void resetRider(Player p) {
		if (p.getPassenger() == null) return;
		if (!(p.getPassenger() instanceof Player)) p.getPassenger().remove();
		p.eject();
		Person per = FriendAPI.get(p.getName());
		if (per != null) per.setVal(KEY, NO_HAT);
	}

	/**
	 * If there is a mob on player {@link p}'s head then it is removed.
	 * 
	 * @param p
	 */
	public static void resetHat(Player p) {
		if (p.getPassenger() == null) return;
		if (p.getPassenger() instanceof Player) return;
		resetRider(p);
	}

	@EventHandler
	public static void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (p.getItemInHand() == null) return;
		ItemStack itemInHand = p.getItemInHand();
		if (itemInHand.getType().equals(Material.ROTTEN_FLESH)) resetRider(p);
		else if (itemInHand.getType().equals(Material.MAGMA_CREAM)) setMagmacubeHat(p);
		else if (itemInHand.getType().equals(Material.SLIME_BALL)) setSlimeHat(p);
	}

	private static Location getLocation(Player p) {
		return p.getLocation().add(0.0D, 1.5D, 0.0D);
	}

	private static Entity checkHead(Player p) {
		final Entity e = p.getPassenger();
		if (e == null) return null;
		if (e instanceof Player) { return null; }
		e.remove();
		return e;
	}

	private static void scheduleHat(final Player p, final Entity e) {
		checkHead(p);
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(TokenShop.plugin, new Runnable() {
			@Override
			public void run() {
				p.setPassenger(e);
			}
		}, 3L);
	}
}