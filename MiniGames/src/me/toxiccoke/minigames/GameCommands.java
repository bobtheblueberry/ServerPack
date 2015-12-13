package me.toxiccoke.minigames;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class GameCommands implements CommandExecutor {

	private boolean commandBomber(Player p, String cmd, String label, String[] args) {
		if (args.length < 1) {
			p.sendMessage(ChatColor.RED + "/" + label + " <list|create|delete>");
			return true;
		}
		String arg1 = args[0].toLowerCase();
		if (arg1.startsWith("list")) {
			if (GameLobby.lobby.games.size() > 0) {
				p.sendMessage(ChatColor.GRAY + "MiniGame Arenas ----->");
				// list minigames
				for (GameArena<?> s : GameLobby.lobby.games)
					p.sendMessage(ChatColor.RED + "MiniGame: " + ChatColor.YELLOW + s.getGameName() + ChatColor.RED
							+ " Arena: " + ChatColor.YELLOW + s.getArenaName());
			} else {
				p.sendMessage(ChatColor.RED
						+ "No minigames have been yet configured. Use /bomber create to add a new minigame.");
			}
			return true;
		} else if (arg1.startsWith("add") || arg1.startsWith("create") || arg1.startsWith("new")) {
			if (args.length != 2) {
				p.sendMessage(ChatColor.RED + "Usage: /bomber create <arena name>");
				return true;
			} else {
				String an = args[1];
				GameArena<? extends GamePlayer> ga = GameLobby.lobby.getArena(an);
				if (ga != null) {
					p.sendMessage(ChatColor.RED + "Game arena with name <" + an + "> already exists.");
					return true;
				}
				GameLobby.lobby.createNewArena(an);
				p.sendMessage(ChatColor.GREEN + "Game arena created with name " + an);
				return true;
			}
		} else if (arg1.startsWith("remove") || arg1.startsWith("delete")) {
			if (args.length != 2) {
				p.sendMessage(ChatColor.RED + "Usage: /bomber delete <arena name>");
				return true;
			}
			String an = args[1];
			GameArena<? extends GamePlayer> ga = GameLobby.lobby.getArena(an);
			if (ga == null) {
				p.sendMessage(ChatColor.RED + "No such arena <" + an + ">");
				return true;
			}
			if (GameLobby.lobby.removeArena(an))
				p.sendMessage(ChatColor.YELLOW + "Removed arena " + an);
			else
				p.sendMessage(ChatColor.RED + " Failed to remove arena " + an);
			return true;
		}
		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
			return true;
		}
		Player p = (Player) sender;
		String cmd = command.getName().toLowerCase();
		if (cmd.equals("leave")) {
			leave(p, args);
			return true;
		}
		if (cmd.equals("bomber")) {
			return commandBomber(p, cmd, label, args);
		}
		if (!cmd.equals("bconfig"))
			return false;

		if (args.length == 0) {
			showHelpMsg(p, label);
			return true;
		}
		GameArena<?> minigame = GameLobby.lobby.getArena(args[0]);
		if (minigame == null) {
			p.sendMessage(ChatColor.RED + "Cannot find minigame arena <" + args[0]
					+ ">. Do /bomber list to show minigame arenas.");
			return true;
		}
		if (args.length < 2) {
			showHelpMsg(p, label, minigame.arenaName);
			return true;
		}
		String operation = args[1].toLowerCase();
		if (operation.equals("info")) {
			p.sendMessage(ChatColor.GRAY + "Mini-game Type: " + ChatColor.YELLOW + minigame.gameName);
			p.sendMessage(ChatColor.GRAY + "Arena name: " + ChatColor.YELLOW + minigame.arenaName);
			p.sendMessage(
					ChatColor.GRAY + "Minimum players needed to start game: " + ChatColor.YELLOW + minigame.minplayers);
			p.sendMessage(
					ChatColor.GRAY + "Maximum players allowed in game: " + ChatColor.YELLOW + minigame.maxplayers);
			p.sendMessage(ChatColor.GRAY + "Game length: " + ChatColor.YELLOW + minigame.gamelength);
			p.sendMessage(ChatColor.GRAY + "Number of red team spawn locations: " + ChatColor.YELLOW
					+ minigame.redSpawnLocations.size());
			p.sendMessage(ChatColor.GRAY + "Number of blue team spawn locations: " + ChatColor.YELLOW
					+ minigame.blueSpawnLocations.size());
			p.sendMessage(ChatColor.GRAY + "Schematic file: " + ChatColor.YELLOW + minigame.schematic);
			p.sendMessage(ChatColor.GRAY + "Join game sign location: " + ChatColor.YELLOW + minigame.signLocation);
			p.sendMessage(ChatColor.GRAY + "Leaderboard Location: " + ChatColor.YELLOW + minigame.leaderboard);

			p.sendMessage(ChatColor.GRAY + "Lobby Location: " + ChatColor.YELLOW + minigame.lobbyLocation);
			p.sendMessage(ChatColor.GRAY + "Game bounds 1: " + ChatColor.YELLOW + minigame.bounds1);
			p.sendMessage(ChatColor.GRAY + "Game bounds 2: " + ChatColor.YELLOW + minigame.bounds2);
			p.sendMessage(ChatColor.GRAY + "Schematic paste Location: " + ChatColor.YELLOW + minigame.pasteLocation);
			return true;
		} else if (operation.equals("setsign")) {
			Block b = getTargetBlock(p, 5);
			if (b == null || !(b.getState() instanceof Sign)) {
				p.sendMessage(ChatColor.RED + "You are not looking at a sign");
				return true;
			}
			minigame.signLocation = b.getLocation();
			minigame.save();
			minigame.setSignText(null);// trigger reset
			p.sendMessage(ChatColor.BLUE + "Sign Changed");
			return true;
		} else if (operation.equals("setpaste")) {
			Location l = p.getLocation();
			minigame.pasteLocation = new Location(l.getWorld(), (int) Math.floor(l.getX()), (int) Math.floor(l.getY()),
					(int) Math.floor(l.getZ()));
			minigame.save();
			p.sendMessage(ChatColor.BLUE + "Set paste location to " + getLocationString(minigame.pasteLocation));
			return true;
		} else if (operation.equals("tp")) {
			if (minigame.pasteLocation == null) {
				p.sendMessage(ChatColor.RED + "This game arena has no paste location set");
				return true;
			}

			p.teleport(minigame.pasteLocation);
			p.sendMessage(ChatColor.BLUE + "Teleported to paste location");
			return true;
		} else if (operation.equals("reset")) {
			if (minigame.pasteLocation == null) {
				p.sendMessage(ChatColor.RED + "This game arena has no paste location set");
				return true;
			}
			minigame.reset();
			p.sendMessage(ChatColor.BLUE + "Minigame reset");
			return true;
		} else if (operation.equals("schematic")) {
			if (args.length < 3) {
				p.sendMessage(ChatColor.RED + "Specify a schematic name");
				return true;
			}
			String sc = "";
			for (int i = 2; i < args.length; i++)
				sc = sc + args[i];
			minigame.schematic = sc;
			minigame.save();
			p.sendMessage(ChatColor.BLUE + "Schematic set to " + sc);
			p.sendMessage(ChatColor.BLUE + "Full path name " + minigame.getSchematicFile().getAbsolutePath());
			return true;
		} else if (operation.equals("addredspawn") || operation.equals("addbluespawn")) {
			java.util.List<Location> list;
			boolean blue = false;
			if (operation.equals("addredspawn"))
				list = minigame.redSpawnLocations;
			else {
				list = minigame.blueSpawnLocations;
				blue = true;
			}

			Location l = p.getLocation(), n;
			list.add(n = new Location(l.getWorld(), Math.round(l.getX()), Math.round(l.getY()), Math.round(l.getZ()),
					l.getYaw(), l.getPitch()));
			minigame.save();
			p.sendMessage(ChatColor.GRAY + "Added spawn location for the "
					+ ((blue) ? (ChatColor.BLUE + "blue team ") : (ChatColor.RED + "red team ")) + ChatColor.GRAY
					+ "at " + getLocationString(n));

			return true;
		} else if (operation.equals("resetspawn")) {
			minigame.blueSpawnLocations.clear();
			minigame.redSpawnLocations.clear();
			minigame.save();
			p.sendMessage(ChatColor.RED + "All Spawn locations removed for <" + minigame.arenaName + ">");
			return true;
		} else if (operation.equals("setlobby")) {

			Location l = p.getLocation();
			minigame.lobbyLocation = new Location(l.getWorld(), Math.round(l.getX()), Math.round(l.getY()),
					Math.round(l.getZ()), l.getYaw(), l.getPitch());
			minigame.save();
			p.sendMessage(ChatColor.BLUE + "Set lobby location " + getLocationString(minigame.lobbyLocation));
			return true;

		} else if (operation.equals("setbounds1")) {
			Location l = p.getLocation();
			minigame.bounds1 = new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
			minigame.save();
			p.sendMessage(ChatColor.BLUE + "Set bounds1 to " + getLocationString(minigame.bounds1));
			return true;
		} else if (operation.equals("setbounds2")) {
			Location l = p.getLocation();
			minigame.bounds2 = new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
			minigame.save();
			p.sendMessage(ChatColor.BLUE + "Set bounds2 to " + getLocationString(minigame.bounds2));
			return true;
		} else if (operation.equals("setleaderboard")) {
			Location l = p.getLocation();
			minigame.leaderboard = new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
			minigame.save();
			p.sendMessage(ChatColor.BLUE + "Set leaderboard location to " + getLocationString(minigame.leaderboard));
			minigame.updateLeaderboard();
			return true;
		} else if (operation.equals("setminplayers") || operation.equals("setmaxplayers")
				|| operation.equals("setgamelength")) {
			if (args.length < 3) {
				p.sendMessage(ChatColor.RED + "Specify a number");
				return true;
			}
			int i;
			try {
				i = Integer.parseInt(args[2]);
			} catch (NumberFormatException exc) {
				p.sendMessage(ChatColor.RED + args[2] + " is not a number");
				return true;
			}
			if (operation.equals("setminplayers")) {
				minigame.minplayers = i;
				p.sendMessage(ChatColor.BLUE + "Set minimum players to " + i);
			} else if (operation.equals("setmaxplayers")) {
				minigame.maxplayers = i;
				p.sendMessage(ChatColor.BLUE + "Set maximum players to " + i);
			} else {// setgamelength
				minigame.gamelength = i;
				p.sendMessage(ChatColor.BLUE + "Set game length to " + i);
			}
			minigame.save();
			return true;
		}
		showHelpMsg(p, label);
		return true;
	}

	private void showHelpMsg(Player p, String label) {
		showHelpMsg(p, label, "<Arena>");
	}

	private void showHelpMsg(Player p, String label, String arena) {
		p.sendMessage(ChatColor.RED + "/" + label + " " + arena
				+ " <info | setsign | setpaste | addbluespawn | addredspawn | resetspawn | setlobby | tp | reset | schematic "
				+ "| setbounds1 | setbounds2 | setmaxplayers | setminplayers | setleaderboard | setgamelength>");
	}

	private String getLocationString(Location l) {
		return "World: " + l.getWorld().getName() + " X: " + l.getX() + " Y: " + l.getY() + " Z: " + l.getZ();
	}

	private Block getTargetBlock(Player player, int range) {
		Location loc = player.getEyeLocation();
		Vector dir = loc.getDirection().normalize();

		for (int i = 0; i <= range; i++) {
			Block b = loc.add(dir).getBlock();
			if (b != null && b.getType() != Material.AIR)
				return b;
		}

		return null;
	}

	private void leave(Player p, String[] args) {
		for (GameArena<?> m : GameLobby.lobby.games)
			for (GamePlayer gp : m.getPlayers())
				if (gp.equals(p.getName())) {
					m.notifyLeaveCommand(gp);
				}
	}
}
