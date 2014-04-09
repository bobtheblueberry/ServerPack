package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.team.TwoTeamPlayer;

import org.bukkit.entity.Player;

public class PayloadPlayer extends TwoTeamPlayer<PayloadTeam> {

	PayloadTeam team;
	
	public PayloadPlayer(Player p, PayloadTeam t) {
		super(p);
		this.team = t;
	}

	@Override
	public PayloadTeam getTeam() {
		return team;
	}

	@Override
	public void setTeam(PayloadTeam newTeam) {
		team = newTeam;
	}
}
