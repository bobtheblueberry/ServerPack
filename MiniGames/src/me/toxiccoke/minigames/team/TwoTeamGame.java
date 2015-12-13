package me.toxiccoke.minigames.team;

import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import me.toxiccoke.minigames.GameArena;
import me.toxiccoke.minigames.MiniGamesPlugin;

public abstract class TwoTeamGame<E extends TwoTeamPlayer<T>, T extends TwoTeamTeam<E>> extends GameArena<E> {

	protected Team	redTeam, blueTeam;
	protected String	redScore, blueScore;
	protected Scoreboard	board;
	protected Objective		objective,objective2;

	public TwoTeamGame(String gameName, String arenaName) {
		super(gameName, arenaName);
		initScoreboard();
	}

	protected void initScoreboard() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();
		redTeam = board.registerNewTeam("Red");
		redTeam.setDisplayName(ChatColor.DARK_RED + "Red");
		redTeam.setCanSeeFriendlyInvisibles(true);
		redTeam.setAllowFriendlyFire(false);
		redTeam.setPrefix(ChatColor.RED + "");
		blueTeam = board.registerNewTeam("Blue");
		blueTeam.setDisplayName(ChatColor.DARK_BLUE + "Blue");
		blueTeam.setCanSeeFriendlyInvisibles(true);
		blueTeam.setAllowFriendlyFire(false);
		blueTeam.setPrefix(ChatColor.BLUE + "");
		objective = board.registerNewObjective("score", "trigger");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.GREEN + "Score");
		objective2 = board.registerNewObjective("player score", "trigger");
		objective2.setDisplaySlot(DisplaySlot.BELOW_NAME);
		objective2.setDisplayName(ChatColor.YELLOW + "Kills");
		
		
		// Get a fake offline player
		blueScore = ChatColor.BLUE + "Blue Kills:";
		redScore = ChatColor.RED + "Red Kills:";
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

			E plr;
			if (redCount > bluCount) {
				plr = getRandomPlayer(TeamType.RED);
				if (plr == null)
					return;
				plr.setTeam(getBlue());
				plr.getPlayer().sendMessage(ChatColor.GOLD + "You were switched to the blue team to balance the teams.");
			} else {
				plr = getRandomPlayer(TeamType.BLUE);
				if (plr == null)
					return;
				plr.setTeam(getRed());
				plr.getPlayer().sendMessage(ChatColor.GOLD + "You were switched to the red team to balance the teams.");
			}
			removePlayerFromScoreboard(plr);
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
	
	public E getPlayer(String player) {
		for (E p : getPlayers())
			if (p.equals(player))
				return p;
		return null;
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
			blueTeam.removeEntry(plr.getName());
		} else {
			redTeam.removeEntry(plr.getName());
		}
	}

	protected void updateScore() {
		objective.getScore(redScore).setScore(getRed().getScore());
		objective.getScore(blueScore).setScore(getBlue().getScore());
		for (E p : getPlayers())
			objective2.getScore(p.getName()).setScore(p.getScore());
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



	protected void initPlayer(E plr) {
		Player p = plr.getPlayer();
		p.setHealth(((Damageable) p).getMaxHealth());
		if (plr.getTeam().team == TeamType.BLUE) {
			blueTeam.addEntry(p.getName());
			String name = ChatColor.DARK_BLUE + p.getName();
			if (name.length() > 16)
				name = name.substring(0, 15);
			p.setPlayerListName(name);
		} else {
			redTeam.addEntry(p.getName());
			String name = ChatColor.DARK_RED + p.getName();
			if (name.length() > 16)
				name = name.substring(0, 15);
			p.setPlayerListName(name);
		}
		if (isStarted())
			plr.startGame();
	}

	protected void sendTeamMessage(String message, T team) {
		for (E player : getPlayers(team))
			player.getPlayer().sendMessage(message);
	}

	protected ArrayList<E> getPlayers(T team) {
		ArrayList<E> players = new ArrayList<E>();
		for (E player : getPlayers())
			if (player.getTeam().equals(team))
				players.add(player);
		return players;
	}

	protected abstract void spawn(E p);

	protected abstract T getRed();

	protected abstract T getBlue();

	protected abstract boolean isStarted();

	protected abstract void updateArmor(E player);
}
