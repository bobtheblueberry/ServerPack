package me.toxiccoke.minigames.impl;

import me.toxiccoke.minigames.MiniGamePlayer;
import me.toxiccoke.minigames.impl.BomberTeam.TeamType;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BomberGamePlayer extends MiniGamePlayer {

	BomberTeam	team;

	public BomberGamePlayer(Player p, BomberTeam t) {
		super(p);
		this.team = t;
	}

	@Override
	public Material getFeetParticle() {
		if (team.team == TeamType.BLUE)
			return Material.LAPIS_BLOCK;
		else
			return Material.REDSTONE_WIRE;
	}
	
	public ChatColor getTeamColor() {
		if (team.team == TeamType.RED)
			return ChatColor.RED;
		return ChatColor.BLUE;
	}
 }
