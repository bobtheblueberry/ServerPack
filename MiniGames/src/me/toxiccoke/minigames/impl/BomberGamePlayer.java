package me.toxiccoke.minigames.impl;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.toxiccoke.minigames.MiniGamePlayer;
import me.toxiccoke.minigames.impl.BomberTeam.TeamType;

public class BomberGamePlayer extends MiniGamePlayer {

	BomberTeam	team;

	public BomberGamePlayer(Player p, BomberTeam t) {
		super(p);
		this.team = t;
	}

	public String getName() {
		return player;
	}

	@Override
	public Material getFeetParticle() {
		if (team.team == TeamType.BLUE)
			return Material.LAPIS_BLOCK;
		else
			return Material.REDSTONE_WIRE;
	}
}
