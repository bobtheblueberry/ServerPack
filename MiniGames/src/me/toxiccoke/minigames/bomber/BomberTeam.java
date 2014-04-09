package me.toxiccoke.minigames.bomber;

import me.toxiccoke.minigames.team.TeamType;
import me.toxiccoke.minigames.team.TwoTeamGame;
import me.toxiccoke.minigames.team.TwoTeamTeam;

import org.bukkit.event.Listener;

public class BomberTeam extends TwoTeamTeam<BomberPlayer> implements Listener {

	BomberGame	world;

	public BomberTeam(BomberGame world, TeamType type) {
		super(type);
		this.world = world;
	}

	@Override
	public TwoTeamGame<BomberPlayer, ? extends TwoTeamTeam<BomberPlayer>> getGame() {
		return world;
	}

}
