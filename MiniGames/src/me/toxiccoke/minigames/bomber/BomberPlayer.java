package me.toxiccoke.minigames.bomber;

import me.toxiccoke.minigames.MiniGamesPlugin;
import me.toxiccoke.minigames.team.TwoTeamPlayer;
import me.toxiccoke.minigames.team.TwoTeamTeam;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BomberPlayer extends TwoTeamPlayer implements Runnable {

	BomberTeam	team;
	private boolean canFireball;

	public BomberPlayer(Player p, BomberTeam t) {
		super(p);
		this.team = t;
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

	@Override
	public TwoTeamTeam getTeam() {
		return team;
	}

	@Override
	public void setTeam(TwoTeamTeam newTeam) {
		team = (BomberTeam)newTeam;
	}
}