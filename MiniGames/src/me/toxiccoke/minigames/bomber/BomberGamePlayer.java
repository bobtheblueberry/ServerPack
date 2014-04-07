package me.toxiccoke.minigames.bomber;

import me.toxiccoke.minigames.MiniGamePlayer;
import me.toxiccoke.minigames.MiniGamesPlugin;
import me.toxiccoke.minigames.bomber.BomberTeam.TeamType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BomberGamePlayer extends MiniGamePlayer implements Runnable {

	BomberTeam	team;
	private boolean canFireball;

	public BomberGamePlayer(Player p, BomberTeam t) {
		super(p);
		this.team = t;
	}

	@Override
	public Material getFeetParticle() {
		if (team.team == TeamType.BLUE)
			return Material.LAPIS_BLOCK;
		else
			return Material.REDSTONE_WIRE;
	}
	
	public ChatColor getTeamColor() {
		if (team.team == TeamType.RED)
			return ChatColor.RED;
		return ChatColor.BLUE;
	}
	
	public boolean canFireball() {
		
		return canFireball;
	}
	
	public void fireball() {
		canFireball = false;
		getPlayer().setLevel(0);
		Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, this, 60);
	}
	
	public void run()
	{
		canFireball = true;
		getPlayer().setLevel(1);
	}

	@Override
	public void startGame() {
		canFireball = true;
		getPlayer().setLevel(1);
	}
}
