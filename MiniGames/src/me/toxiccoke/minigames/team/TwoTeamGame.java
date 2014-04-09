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

public abstract class TwoTeamGame<E extends TwoTeamPlayer<T>, T extends TwoTeamTeam<E>> extends GameWorld<E> {

	protected Team	redTeam, blueTeam;
	protected OfflinePlayer	redScore, blueScore;
	protected Scoreboard	board;
	protected Objective		objective;

	public TwoTeamGame(String gameName, String worldName) {
		super(gameName, worldName);
		initScoreboard();
	}

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
		for (E p : getPlayers())
			if (p.getTeam().team == TeamType.BLUE)
				bluCount++;
			else redCount++;
		if (Math.abs(redCount - bluCount) > 1) {
			E plr = getRandomPlayer(TeamType.RED);
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

	protected E getRandomPlayer(TeamType t) {
		LinkedList<E> temp = new LinkedList<E>();
		for (E p : getPlayers())
			if (p.getTeam().team == t)
				temp.add(p);
		if (temp.size() == 0)
			return null;
		return temp.get((int) (Math.random() * temp.size()));
	}

	protected T getTeam() {
		int redCount = 0, bluCount = 0;
		for (E p : getPlayers())
			if (p.getTeam().team == TeamType.BLUE)
				bluCount++;
			else redCount++;
		if (redCount == bluCount)
			return (Math.random() * 2) < 1 ? getRed() : getBlue();

		if (redCount > bluCount)
			return getBlue();
		else return getRed();
	}

	protected void removePlayerFromScoreboard(E plr) {
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

	protected void checkLeader(E p) {
		int score = p.getScore();
		if (leader1 == null || score > leader1.score) {
			leader1 = new Leader(p.getName(), score);
			updateLeaderboard();
			save();
		} else if (leader2 == null || score > leader2.score) {
			if (leader1.name.equals(p.getName()))
				return;
			leader2 = new Leader(p.getName(), score);
			updateLeaderboard();
			save();
		} else if (leader3 == null || score > leader3.score) {
			if (leader1.name.equals(p.getName()) || leader2.name.equals(p.getName()))
				return;
			leader3 = new Leader(p.getName(), score);
			updateLeaderboard();
			save();
		}
	}
	protected abstract void initPlayer(E p);
	protected abstract void spawn(E p);
	protected abstract T getRed();
	protected abstract T getBlue();
}

