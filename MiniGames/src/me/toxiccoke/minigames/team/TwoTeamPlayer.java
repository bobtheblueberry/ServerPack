package me.toxiccoke.minigames.team;

import me.toxiccoke.minigames.GamePlayer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class TwoTeamPlayer<T extends TwoTeamTeam<?>> extends GamePlayer {

	public TwoTeamPlayer(Player p) {
		super(p);
	}

	@Override
	public Material getFeetParticle() {
		if (getTeam().team == TeamType.BLUE)
			return Material.LAPIS_BLOCK;
		else return Material.REDSTONE_WIRE;
	}

	public ChatColor getTeamColor() {
		if (getTeam().team == TeamType.RED)
			return ChatColor.RED;
		return ChatColor.BLUE;
	}

	public abstract T getTeam();

	public abstract void setTeam(T newTeam);
}
