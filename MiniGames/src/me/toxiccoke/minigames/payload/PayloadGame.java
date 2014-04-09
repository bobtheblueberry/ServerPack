package me.toxiccoke.minigames.payload;

import java.util.LinkedList;

import me.toxiccoke.minigames.GamePlayer;
import me.toxiccoke.minigames.MiniGamesPlugin;
import me.toxiccoke.minigames.team.TeamType;
import me.toxiccoke.minigames.team.TwoTeamGame;
import me.toxiccoke.tokenshop.TokenShop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

public class PayloadGame extends TwoTeamGame<PayloadPlayer, PayloadTeam> {

	protected LinkedList<PayloadPlayer>	players;
	private boolean						isStarted;
	private PayloadTeam					red, blue;

	// blu spawn 1, blu spawn 2, red spawn

	public PayloadGame(String worldName) {
		super("Payload", worldName);
		players = new LinkedList<PayloadPlayer>();
		load();
		red = new PayloadTeam(this, TeamType.RED);
		blue = new PayloadTeam(this, TeamType.BLUE);
		initMinecart();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(MiniGamesPlugin.plugin, new MinecartUpdater(this), 10, 10);
	}

	@Override
	public int getPlayerCount() {
		return players.size();
	}

	@Override
	public boolean isJoinable() {
		return !broken && spawnLocations.size() == 3 && lobbyLocation != null;
	}

	@Override
	public boolean isFull() {
		return players.size() >= maxplayers;
	}

	@Override
	public boolean join(Player p) {
		players.add(new PayloadPlayer(p,getTeam()));
		
		isStarted = true;
		return true;
	}

	@Override
	public LinkedList<? extends GamePlayer> getPlayers() {
		return players;
	}

	@Override
	public void save() {
		save(super.getSaveYML());
	}

	private void load() {
		super.getLoadYML();
	}

	@Override
	public boolean canPlaceBlock(GamePlayer p, BlockPlaceEvent event) {
		return true;
	}

	@Override
	public boolean canBreakBlock(GamePlayer p, BlockBreakEvent event) {
		return true;
	}

	@Override
	public boolean canExplodeBlock(Block b, Entity e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canPlayerHunger(GamePlayer player) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void notifyDeath(GamePlayer gp, Entity damager, DamageCause cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyDeath(GamePlayer gp, EntityDamageEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyQuitGame(GamePlayer gp) {
		players.remove(gp);
	}

	protected void endGame() {
		for (PayloadPlayer p : players)
			p.restorePlayer();
	}

	@Override
	public void notifyLeaveCommand(GamePlayer gp) {
		players.remove(gp);
		gp.restorePlayer();
	}

	@Override
	public boolean allowDamage(GamePlayer gp) {
		return isStarted;
	}

	public void vehicleUpdate(VehicleUpdateEvent event) {
		if (event.getVehicle() instanceof Minecart) {

		}
	}

	private int getPlayersWithinDistance(Location l, int dist) {
		int a = 0;
		for (PayloadPlayer player : players) {
			// world is not checked
			double distance = getDistance(player.getPlayer().getLocation(), l);
			if (distance <= dist)
				a++;
		}
		return a;
	}

	private double getDistance(Location l1, Location l2) {
		double dx = l1.getX() - l2.getX();
		double dy = l1.getY() - l2.getY();
		double dz = l1.getZ() - l2.getZ();
		// We should avoid Math.pow or Math.hypot due to perfomance reasons
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	Minecart	minecart;

	private void initMinecart() {
		if (pasteLocation == null)
			return;
		World w = pasteLocation.getWorld();
		if (w == null)
			return;
		minecart = (Minecart) w.spawnEntity(pasteLocation, EntityType.MINECART);
		minecart.setSlowWhenEmpty(false);

	}

	protected void updateCart() {
		if (!isStarted)
			return;
		int dist = getPlayersWithinDistance(minecart.getLocation(), 5);
		if (dist < 1)
			return;
		System.out.println(minecart.getVelocity());
		Vector v = minecart.getVelocity();
		if (v.getX() < 0.05 && v.getY() < 0.05 && v.getZ() < 0.05)
			v = new Vector(.3, 0, .3);
		else v = new Vector(v.getX() * 1.01, v.getY() * 1.01, v.getZ() * 1.01);
		minecart.setVelocity(v);
	}

	@Override
	protected PayloadTeam getRed() {
		return red;
	}

	@Override
	protected PayloadTeam getBlue() {
		return blue;
	}

	@Override
	protected void initPlayer(PayloadPlayer p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void spawn(PayloadPlayer p) {
		if (spawnLocations.size() < 3)
			return;
		if (p.getTeam().team == TeamType.BLUE)
			TokenShop.teleportAdvanced(p.getPlayer(), spawnLocations.get(0));
		else
			TokenShop.teleportAdvanced(p.getPlayer(), spawnLocations.get(2));
	}

	@Override
	public LinkedList<PayloadPlayer> getTeamPlayers() {
		return players;
	}

}
