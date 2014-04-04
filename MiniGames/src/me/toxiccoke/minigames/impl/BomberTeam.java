package me.toxiccoke.minigames.impl;

import org.bukkit.event.Listener;

public class BomberTeam implements Listener {

	public enum TeamType {
		RED, BLUE
	}

	public TeamType	team;
	public int score;
	
	public BomberTeam(TeamType type) {
		this.team = type;
	}

}