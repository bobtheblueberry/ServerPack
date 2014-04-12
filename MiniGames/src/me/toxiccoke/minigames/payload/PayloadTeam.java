package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.team.TeamType;
import me.toxiccoke.minigames.team.TwoTeamGame;
import me.toxiccoke.minigames.team.TwoTeamTeam;

public class PayloadTeam extends TwoTeamTeam<PayloadPlayer> {
	
	PayloadGame world;
	boolean lost;
	
	public PayloadTeam(PayloadGame world, TeamType type) {
		super(type);
		this.world = world;
	}

	@Override
	public TwoTeamGame<PayloadPlayer, ? extends TwoTeamTeam<PayloadPlayer>> getGame() {
		return world;
	}
}
