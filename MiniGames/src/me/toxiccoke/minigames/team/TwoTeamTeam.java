package me.toxiccoke.minigames.team;

public abstract class TwoTeamTeam<E extends TwoTeamPlayer<? extends TwoTeamTeam<E>>> {

	public TeamType	team;

	public int getScore() {
		int i = 0;
		for (E gp : getGame().getPlayers())
			if (gp.getTeam().team == team) i += gp.getScore();
		return i;
	}

	public TwoTeamTeam(TeamType type) {
		this.team = type;
	}
	
	public abstract TwoTeamGame<E,? extends TwoTeamTeam<E>> getGame();
	
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof TwoTeamTeam))
			return false;
		TwoTeamTeam<?> t = (TwoTeamTeam<?>)o;
		return t.team == this.team;
	}
}
