package me.toxiccoke.minigames.impl;

import org.bukkit.event.Listener;

public class BomberTeam implements Listener {

	public int getScore() {
		int i = 0;
		for (BomberGamePlayer gp : world.players)
			if (gp.team.team == team) i += gp.getScore();
		return i;
	}

	public enum TeamType {
		RED, BLUE
	}

	public TeamType	team;
	public int		score;
	BomberGameWorld	world;

	public BomberTeam(BomberGameWorld world, TeamType type) {
		this.team = type;
		this.world = world;
	}
}