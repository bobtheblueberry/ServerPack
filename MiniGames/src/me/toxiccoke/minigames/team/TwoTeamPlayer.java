package me.toxiccoke.minigames.team;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.toxiccoke.minigames.GamePlayer;

public abstract class TwoTeamPlayer extends GamePlayer {
	
	public TwoTeamPlayer(Player p) {
		super(p);
	}

	@Override
	public Material getFeetParticle() {
		if (getTeam().team == TeamType.BLUE)
			return Material.LAPIS_BLOCK;
		else
			return Material.REDSTONE_WIRE;
	}
	
	public ChatColor getTeamColor() {
		if (getTeam().team == TeamType.RED)
			return ChatColor.RED;
		return ChatColor.BLUE;
	}
	
	public abstract TwoTeamTeam getTeam();
	public abstract void setTeam(TwoTeamTeam newTeam);
}
