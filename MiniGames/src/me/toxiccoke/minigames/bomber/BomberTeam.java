package me.toxiccoke.minigames.bomber;

import org.bukkit.event.Listener;

public class BomberTeam implements Listener {

	public int getScore() {
		int i = 0;
		for (BomberPlayer gp : world.players)
			if (gp.team.team == team) i += gp.getScore();
		return i;
	}

	public enum TeamType {
		RED, BLUE
	}

	public TeamType	team;
	BomberWorld	world;

	public BomberTeam(BomberWorld world, TeamType type) {
		this.team = type;
		this.world = world;
	}
}