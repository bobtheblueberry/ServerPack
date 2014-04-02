package me.toxiccoke.minigames.impl;

import java.util.LinkedList;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scoreboard.Team;

import me.toxiccoke.minigames.MiniGamePlayer;
import me.toxiccoke.minigames.MiniGameWorld;
import me.toxiccoke.minigames.impl.BomberTeam.TeamType;

public class BomberGameWorld extends MiniGameWorld {

	private LinkedList<BomberGamePlayer>	players;

	public BomberGameWorld(BomberGame g, String worldName) {
		super(g, worldName);
		load();
		players = new LinkedList<BomberGamePlayer>();
		MAX_PLAYERS = 8;
	}

	private void load() {
		// YamlConfiguration yml =
		super.getLoadYML();
	}

	public void save() {
		YamlConfiguration yml = super.getSaveYML();
		// add other stuff here
		super.save(yml);
	}

	@Override
	public int getPlayerCount() {
		return players.size();
	}

	@Override
	public boolean isJoinable() {
		return !broken && spawnLocations.size() > 0;
	}

	@Override
	public boolean isFull() {
		return getPlayerCount() == MAX_PLAYERS;
	}

	@Override
	public boolean join(Player p) {
		if (broken || lobbyLocation == null || spawnLocations.size() < 2) return false;
		boolean isInGame = false;
		for (BomberGamePlayer bp : players)
			if (bp.getName().equals(p.getName())) {
				isInGame = true;
				break;
			}

		BomberGamePlayer bgp;
		if (!isInGame) {
			BomberTeam t = getTeam();
			bgp = new BomberGamePlayer(p, t);
			players.add(bgp);
		} else bgp = getPlayer(p.getName());

		 p.teleport(getSpawn(bgp.team.team));
		//p.teleport(lobbyLocation);
		p.setGameMode(GameMode.ADVENTURE);
		p.sendMessage(ChatColor.YELLOW + "Joined Bomber");
		if (bgp.team.team == TeamType.BLUE) p.sendMessage(ChatColor.DARK_BLUE + "You are in the blue team");
		else p.sendMessage(ChatColor.DARK_RED + "You are in the red team");
		return true;
	}

	public BomberGamePlayer getPlayer(String name) {
		for (BomberGamePlayer p : players)
			if (p.getName().equals(name)) return p;
		return null;
	}

	public Location getSpawn(TeamType t) {
		if (t == TeamType.BLUE) return spawnLocations.get(0);
		return spawnLocations.get(1);
	}

	private BomberTeam getTeam() {
		int redCount = 0, bluCount = 0;
		for (BomberGamePlayer p : players)
			if (p.team.team == TeamType.BLUE) bluCount++;
			else redCount++;
		if (redCount == bluCount) return new BomberTeam((Math.random() * 2) < 1 ? TeamType.BLUE : TeamType.RED);

		if (redCount > bluCount) return new BomberTeam(TeamType.BLUE);
		else return new BomberTeam(TeamType.RED);
	}

	@Override
	public LinkedList<? extends MiniGamePlayer> getPlayers() {
		return players;
	}

	@Override
	public void notifyDeath(MiniGamePlayer gp, Entity damager, DamageCause cause) {
		gp.getPlayer().sendMessage(ChatColor.GOLD + "You died!");
		if (damager instanceof Player) {
			Player dmg = (Player) damager;
			gp.getPlayer().sendMessage(ChatColor.GOLD + "Killed by " + dmg.getDisplayName());
			dmg.sendMessage(ChatColor.GOLD + "You scored 1 point for killing " + gp.getPlayer().getDisplayName());
		}

		gp.getPlayer().setHealth(((Damageable) gp.getPlayer()).getMaxHealth());
		gp.getPlayer().teleport(getSpawn(((BomberGamePlayer) gp).team.team));
	}

	@Override
	public void notifyDeath(MiniGamePlayer gp, DamageCause cause) {
		gp.getPlayer().sendMessage(ChatColor.GOLD + "You died!");
		gp.getPlayer().setHealth(((Damageable) gp.getPlayer()).getMaxHealth());
		gp.getPlayer().teleport(getSpawn(((BomberGamePlayer) gp).team.team));
	}

}
