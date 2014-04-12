package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.team.TwoTeamPlayer;

import org.bukkit.entity.Player;

public class PayloadPlayer extends TwoTeamPlayer<PayloadTeam> {

	PayloadTeam team;
	PayloadClass playerClass;
	PayloadGame game;
	boolean dead;
	boolean respawning;
	boolean classChange;
	
	public PayloadPlayer(PayloadGame g, Player p, PayloadTeam t, PayloadClass cl) {
		super(p);
		this.game = g;
		this.team = t;
		this.playerClass = cl;
	}

	@Override
	public PayloadTeam getTeam() {
		return team;
	}

	@Override
	public void setTeam(PayloadTeam newTeam) {
		team = newTeam;
	}

	public void respawn() {
		respawning = true;
		game.respawn(this);
	}
	
}
