package me.toxiccoke.minigames.team;

public abstract class TwoTeamTeam {

	public TeamType	team;

	public int getScore() {
		int i = 0;
		for (TwoTeamPlayer gp : getGame().getTeamPlayers())
			if (gp.getTeam().team == team) i += gp.getScore();
		return i;
	}

	public TwoTeamTeam(TeamType type) {
		this.team = type;
	}
	
	public abstract TwoTeamGame getGame();
	
}
