package me.toxiccoke.minigames.team;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import me.toxiccoke.minigames.GameWorld;
import me.toxiccoke.minigames.MiniGamesPlugin;

public abstract class TwoTeamGame extends GameWorld {

	protected Team	redTeam, blueTeam;
	protected OfflinePlayer	redScore, blueScore;
	protected Scoreboard	board;
	protected Objective		objective;

	public TwoTeamGame(String gameName, String worldName) {
		super(gameName, worldName);
		initScoreboard();
	}

	public abstract LinkedList<? extends TwoTeamPlayer> getTeamPlayers();

	protected void initScoreboard() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();
		redTeam = board.registerNewTeam("Red");
		redTeam.setDisplayName(ChatColor.DARK_RED + "Red");
		redTeam.setCanSeeFriendlyInvisibles(true);
		redTeam.setAllowFriendlyFire(false);
		blueTeam = board.registerNewTeam("Blue");
		blueTeam.setDisplayName(ChatColor.DARK_BLUE + "Blue");
		blueTeam.setCanSeeFriendlyInvisibles(true);
		blueTeam.setAllowFriendlyFire(false);
		objective = board.registerNewObjective("score", "trigger");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.GREEN + "Score");
		// Get a fake offline player
		blueScore = Bukkit.getOfflinePlayer(ChatColor.BLUE + "Blue Kills:");
		redScore = Bukkit.getOfflinePlayer(ChatColor.RED + "Red Kills:");
		Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, new Runnable() {
			
			@Override
			public void run() {
				updateScore();
			}
		});
	}

	protected void balanceTeams() {
		int redCount = 0, bluCount = 0;
		for (TwoTeamPlayer p : getTeamPlayers())
			if (p.getTeam().team == TeamType.BLUE)
				bluCount++;
			else redCount++;
		if (Math.abs(redCount - bluCount) > 1) {
			TwoTeamPlayer plr = getRandomPlayer(TeamType.RED);
			if (plr == null)
				return;
			Player p = plr.getPlayer();

			if (redCount > bluCount) {
				plr.setTeam( getBlue());
				p.sendMessage(ChatColor.GOLD + "You were switched to the blue team to balance the teams.");
			} else {
				plr.setTeam(getRed());
				plr.getPlayer().sendMessage(ChatColor.GOLD + "You were switched to the red team to balance the teams.");
			}
			initPlayer(plr);
			spawn(plr);
			balanceTeams();
		}
	}

	protected TwoTeamPlayer getRandomPlayer(TeamType t) {
		LinkedList<TwoTeamPlayer> temp = new LinkedList<TwoTeamPlayer>();
		for (TwoTeamPlayer p : getTeamPlayers())
			if (p.getTeam().team == t)
				temp.add(p);
		if (temp.size() == 0)
			return null;
		return temp.get((int) (Math.random() * temp.size()));
	}

	protected TwoTeamTeam getTeam() {
		int redCount = 0, bluCount = 0;
		for (TwoTeamPlayer p : getTeamPlayers())
			if (p.getTeam().team == TeamType.BLUE)
				bluCount++;
			else redCount++;
		if (redCount == bluCount)
			return (Math.random() * 2) < 1 ? getRed() : getBlue();

		if (redCount > bluCount)
			return getBlue();
		else return getRed();
	}

	protected void removePlayerFromScoreboard(TwoTeamPlayer plr) {
		if (plr.getTeam().team == TeamType.BLUE) {
			blueTeam.removePlayer(Bukkit.getOfflinePlayer(plr.getName()));
		} else {
			redTeam.removePlayer(Bukkit.getOfflinePlayer(plr.getName()));
		}
	}


	protected void updateScore() {
		objective.getScore(redScore).setScore(getRed().getScore());
		objective.getScore(blueScore).setScore(getBlue().getScore());
	}
	
	protected abstract void initPlayer(TwoTeamPlayer p);
	protected abstract void spawn(TwoTeamPlayer p);
	protected abstract TwoTeamTeam getRed();
	protected abstract TwoTeamTeam getBlue();
}

