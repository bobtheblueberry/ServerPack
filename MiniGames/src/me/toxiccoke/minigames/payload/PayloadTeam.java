package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.team.TeamType;
import me.toxiccoke.minigames.team.TwoTeamGame;
import me.toxiccoke.minigames.team.TwoTeamTeam;

public class PayloadTeam extends TwoTeamTeam {
	PayloadGame	world;

	public PayloadTeam(PayloadGame world, TeamType type) {
		super(type);
		this.world = world;
	}

	@Override
	public TwoTeamGame getGame() {
		return world;
	}
}
