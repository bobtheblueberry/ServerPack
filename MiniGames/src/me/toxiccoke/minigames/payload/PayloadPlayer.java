package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.team.TwoTeamPlayer;
import me.toxiccoke.minigames.team.TwoTeamTeam;

import org.bukkit.entity.Player;

public class PayloadPlayer extends TwoTeamPlayer {

	PayloadTeam team;
	
	public PayloadPlayer(Player p, PayloadTeam t) {
		super(p);
		this.team = t;
	}

	@Override
	public TwoTeamTeam getTeam() {
		return team;
	}

	@Override
	public void setTeam(TwoTeamTeam newTeam) {
		team = (PayloadTeam)newTeam;
	}
}
