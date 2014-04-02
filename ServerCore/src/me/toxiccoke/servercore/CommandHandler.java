package me.toxiccoke.servercore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class CommandHandler implements CommandExecutor {
	@EventHandler
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player");
			return true;
		}
		Player p = (Player) sender;
		World world = p.getWorld();
		String cName = cmd.getName().toLowerCase();
		// gm command
		if (cName.equals("gm")) {
			gm(p, args);
			return true;
			// heal command
		} else if (cmd.getName().equalsIgnoreCase("heal")) {
			if (!sender.hasPermission("sc.heal")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You don't have access to this command");
				return true;
			}
			if (args.length == 0) {
				p.setHealth(20.0D);
				p.setFoodLevel(20);
				p.sendMessage(ChatColor.GRAY + "Healed!!");
			} else if (args.length == 1) {
				Player targetPlayer = p.getServer().getPlayer(args[0]);
				targetPlayer.sendMessage(ChatColor.GRAY + "Healed!");
				p.sendMessage(ChatColor.GRAY + "You healed " + ChatColor.GRAY + targetPlayer.getDisplayName());
				return true;
			}
			// fly command
		} else if (cmd.getName().equalsIgnoreCase("fly")) {
			if (!sender.hasPermission("sc.fly")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			boolean fly = p.getAllowFlight();
			if (fly) p.sendMessage(ChatColor.GRAY + "Fly disabled");
			else p.sendMessage(ChatColor.GRAY + "Fly enabled");
			p.setAllowFlight(!fly);
			return true;
			// tp command
		} else if (cmd.getName().equalsIgnoreCase("tp")) {
			if (!sender.hasPermission("sc.tp")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			if (args.length == 0) {
				p.sendMessage(ChatColor.RED + " Please specify a player");
				return true;
			} else if (args.length == 1) {
				Player targetPlayer = p.getServer().getPlayer(args[0]);
				if (targetPlayer == null) {
					p.sendMessage(ChatColor.GRAY + "Unknown Player: " + args[0]);
					return true;
				}
				if (p.teleport(targetPlayer.getLocation())) p.sendMessage(ChatColor.GRAY + "You have teleported to "
						+ ChatColor.GRAY + targetPlayer.getDisplayName());
				else {
					p.sendMessage(ChatColor.GRAY + "Teleportation failed (unknown reason)");

					org.bukkit.event.player.PlayerTeleportEvent event = new org.bukkit.event.player.PlayerTeleportEvent(
							p, p.getLocation(), targetPlayer.getLocation(), TeleportCause.PLUGIN);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled()) p.sendMessage("canceled " + event.getPlayer());
					else p.sendMessage("not canceled");
				}
				return true;
			} else if (args.length == 2) {
				Player targetPlayer = p.getServer().getPlayer(args[0]);
				if (targetPlayer == null) {
					p.sendMessage(ChatColor.GRAY + "Unknown Player: " + args[0]);
					return true;
				}
				Player targetPlayer1 = p.getServer().getPlayer(args[1]);
				if (targetPlayer1 == null) {
					p.sendMessage(ChatColor.GRAY + "Unknown Player: " + args[1]);
					return true;
				}
				if (targetPlayer.teleport(targetPlayer1.getLocation())) targetPlayer.sendMessage(ChatColor.GRAY
						+ "You have been teleported to " + ChatColor.GRAY + targetPlayer1.getDisplayName());
				return true;
			}
			// warn command
		} else if (cmd.getName().equalsIgnoreCase("warn")) {
			if (!sender.hasPermission("sc.warn")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			if (args.length == 0) {
				p.sendMessage(ChatColor.RED + "Too Few Arguments");
				return true;
			} else if (args.length == 1) {
				Player targetPlayer = p.getServer().getPlayer(args[0]);
				p.sendMessage(ChatColor.GRAY + "You have warned " + ChatColor.GRAY + targetPlayer.getDisplayName());
				targetPlayer.sendMessage(ChatColor.GRAY + "You have been WARNED by " + ChatColor.GRAY
						+ p.getDisplayName());
				targetPlayer.sendMessage(ChatColor.RED + "Next warning will result in ban/kick");
				return true;
			}
			// day command
		} else if (cmd.getName().equalsIgnoreCase("day")) {
			if (!sender.hasPermission("sc.day")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			p.sendMessage(ChatColor.GRAY + "You have set the time to day");
			world.setTime(1500);
			Bukkit.broadcastMessage(ChatColor.GRAY + p.getDisplayName() + ChatColor.GRAY
					+ " Set the world time to day ");
			return true;
			// night command
		} else if (cmd.getName().equalsIgnoreCase("night")) {
			if (!sender.hasPermission("sc.night")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			if (args.length == 0) {
				p.sendMessage(ChatColor.GRAY + " Has set the time to night");
				world.setTime(13000);
				Bukkit.broadcastMessage(ChatColor.GRAY + p.getDisplayName() + ChatColor.GRAY
						+ " Set the world time to night ");
				return true;
			}
			// sun command
		} else if (cmd.getName().equalsIgnoreCase("sun")) {
			if (!sender.hasPermission("sc.sun")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			p.getLocation().getWorld().setStorm(false);
			Bukkit.broadcastMessage(ChatColor.GRAY + p.getDisplayName() + ChatColor.GRAY
					+ " Set the world weather to sun ");

			return true;
			// rain command
		} else if (cmd.getName().equalsIgnoreCase("rain")) {
			if (!sender.hasPermission("sc.rain")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			p.getLocation().getWorld().setStorm(true);
			Bukkit.broadcastMessage(ChatColor.GRAY + p.getDisplayName() + ChatColor.GRAY
					+ " Set the world weather to rain ");
			return true;
			// commands setspawn
		} else if (cmd.getName().equalsIgnoreCase("setspawn")) {
			if (!sender.hasPermission("sc.setspawn")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			getConfig().set("spawn.world", p.getLocation().getWorld().getName());
			getConfig().set("spawn.x", p.getLocation().getX());
			getConfig().set("spawn.y", p.getLocation().getY());
			getConfig().set("spawn.z", p.getLocation().getZ());
			Commands.plugin.saveConfig();
			p.sendMessage(ChatColor.GRAY + "Spawn set!");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("spawn")) {
			if (!sender.hasPermission("sc.spawn")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			if (getConfig().getConfigurationSection("spawn") == null) {
				p.sendMessage(ChatColor.RED + "The spawn has not yet been set!");
				return true;
			}
			World w = Bukkit.getServer().getWorld(getConfig().getString("spawn.world"));
			double x = getConfig().getDouble("spawn.x");
			double y = getConfig().getDouble("spawn.y");
			double z = getConfig().getDouble("spawn.z");
			p.teleport(new Location(w, x, y, z));
			p.sendMessage(ChatColor.GRAY + "Welcome to the spawn!");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("kick")) {
			if (!sender.hasPermission("sc.kick")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "Please specify a player!");
				return true;
			}
			Player target = Bukkit.getServer().getPlayer(args[0]);
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Could not find player " + args[0] + "!");
				return true;
			}
			target.kickPlayer(ChatColor.RED + "You have been kicked!");
			Bukkit.getServer().getPluginManager().callEvent(new EnforcerEvent(target, Type.KICK));
			Bukkit.getServer().broadcastMessage(
					ChatColor.GRAY + "Player " + target.getName() + " has been kicked by " + ChatColor.GRAY
							+ sender.getName() + "!");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("ban")) {
			if (!sender.hasPermission("sc.ban")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "Please specify a player!");
				return true;
			}
			Player target = Bukkit.getServer().getPlayer(args[0]);
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Could not find player " + args[0] + "!");
				return true;
			}
			target.kickPlayer(ChatColor.RED + "You have been banned!");
			target.setBanned(true);
			Bukkit.getServer().getPluginManager().callEvent(new EnforcerEvent(target, Type.BAN));
			Bukkit.getServer().broadcastMessage(
					ChatColor.GRAY + "Player " + target.getName() + " has been banned by " + ChatColor.GRAY
							+ sender.getName() + "!");
			return true;
			// poke command
		} else if (cmd.getName().equalsIgnoreCase("poke")) {
			if (!sender.hasPermission("sc.poke")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			if (args.length == 0) {
				p.sendMessage(ChatColor.RED + "Please specify player u would like to poke ");
			} else if (args.length == 1) {
				Player targetPlayer = p.getServer().getPlayer(args[0]);
				if (targetPlayer == null) {
					sender.sendMessage(ChatColor.RED + "Could not find player: " + ChatColor.GRAY + args[0]);
					return true;
				} else if (targetPlayer.getName().equals(p.getName())) {
					sender.sendMessage(ChatColor.RED + "You have poked yourself");
					p.damage(1000.0);
					return true;
				}
				p.sendMessage(ChatColor.GRAY + "You have poked " + ChatColor.GRAY + targetPlayer.getDisplayName());
				targetPlayer.sendMessage(ChatColor.RED + "POKE!!!");
				targetPlayer.sendMessage(ChatColor.GRAY + "You have been poked by " + ChatColor.GRAY
						+ p.getDisplayName());
				return true;
			}

			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Only players can get nicknames!");
				return true;
			}
			// nickname command
		} else if (cmd.getName().equalsIgnoreCase("nick")) {
			if (!sender.hasPermission("sc.nick")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			if (args.length == 0) {
				p.sendMessage(ChatColor.GOLD + "Nickname reset");
				p.setDisplayName(p.getName());
				return true;
			}

			String nick = "";
			for (String arg : args) {
				nick += arg + " ";
			}
			nick = ChatColor.translateAlternateColorCodes('&', nick.substring(0, nick.length() - 1)) + ChatColor.RESET;
			p.setDisplayName(nick);
			p.sendMessage(ChatColor.GRAY + "You have changed your nickname to " + nick);
			this.getConfig().set(p.getName(), nick);
			Commands.plugin.saveConfig();
			return true;
		} else
		// help command
		if (cmd.getName().equalsIgnoreCase("help")) {
			if (!sender.hasPermission("sc.help")) {
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]"
						+ ChatColor.RED + "You dont have acsses to this command");
				return true;
			}
			p.sendMessage(ChatColor.YELLOW + "Help & Information:");
			p.sendMessage(ChatColor.YELLOW + "WebSite: http://aquilamc.comze.com/");
			p.sendMessage(ChatColor.YELLOW + "Commands:");
			p.sendMessage(ChatColor.YELLOW + "/lobby - Takes you back to the lobby");
			p.sendMessage(ChatColor.YELLOW + "/setlobby - Sets lobby spawn");
			p.sendMessage(ChatColor.YELLOW + "/heal - Heals a specifyed player");
			p.sendMessage(ChatColor.YELLOW + "/tp - Teleports to a player");
			p.sendMessage(ChatColor.YELLOW + "/msg - Messages a specifyed player");
			p.sendMessage(ChatColor.YELLOW + "/fly - Enableds flymode");
			p.sendMessage(ChatColor.YELLOW + "/gm <0/1/2> - Sets gamemode of player");
			p.sendMessage(ChatColor.YELLOW + "/party help - Brings up a help menu for friends system");
			p.sendMessage(ChatColor.YELLOW + "/friend help - Brings up a help menu for party system");
			p.sendMessage(ChatColor.YELLOW + "/warn - Warns a player on there behaviour");
			p.sendMessage(ChatColor.YELLOW + "/kick - Kicks a player from the server");
			p.sendMessage(ChatColor.YELLOW + "/ban - perm bans a player from the server");
			p.sendMessage(ChatColor.YELLOW + "/day - Sets world time to day");
			p.sendMessage(ChatColor.YELLOW + "/night - Sets world time to night");
			p.sendMessage(ChatColor.YELLOW + "/sun - Sets world weather to sunny ");
			p.sendMessage(ChatColor.YELLOW + "/rain - Sets world weather to rain");
			p.sendMessage(ChatColor.YELLOW + "/nick - Sets a nicknames to a player");
			p.sendMessage(ChatColor.YELLOW
					+ "/poke - Poke the  specifyed player to tell the player your trying to speak to them.");
			p.sendMessage(ChatColor.YELLOW + "/v - Vanishes u from players");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("vanish")) {
			if (!Commands.plugin.vanished.contains(p)) {
				for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
					pl.hidePlayer(p);
				}
				Commands.plugin.vanished.add(p);
				p.sendMessage(ChatColor.GRAY + "You have been vanished!");
				return true;
			} else {
				for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
					pl.showPlayer(p);
				}
				Commands.plugin.vanished.remove(p);
				p.sendMessage(ChatColor.GRAY + "You have been unvanished!");
				return true;
			}

		}
		return false;

	}

	private void gm(Player p, String[] args) {
		if (args.length == 0) {
			p.sendMessage(ChatColor.RED + "Too Few Arguments");
			p.sendMessage(ChatColor.GRAY + "/gm <0/1/2> <player (optional)>");
		} else if (args.length == 1) {
			int mode = Integer.parseInt(args[0]);
			if (mode == 0) {
				p.setGameMode(GameMode.SURVIVAL);
				p.sendMessage(ChatColor.GRAY + "You GameMode Has Been Set To" + ChatColor.DARK_GREEN + " Survival");
			} else if (mode == 1) {
				p.setGameMode(GameMode.CREATIVE);
				p.sendMessage(ChatColor.GRAY + "You GameMode Has Been Set To" + ChatColor.DARK_GREEN + " Creative");
			} else if (mode == 2) {
				p.setGameMode(GameMode.ADVENTURE);
				p.sendMessage(ChatColor.GRAY + "You GameMode Has Been Set To" + ChatColor.DARK_GREEN + " Adventure");
			} else {
				p.sendMessage(ChatColor.DARK_RED + "" + mode + ChatColor.RED + " Is Not A Available Gamemode");
			}
		} else if (args.length == 2) {
			int mode = Integer.parseInt(args[0]);
			String targetPlayer = args[1];
			if (Bukkit.getPlayer(targetPlayer) != null) {
				Player targetP = Bukkit.getPlayer(targetPlayer);
				if (mode == 0) {
					targetP.setGameMode(GameMode.SURVIVAL);
					targetP.sendMessage(ChatColor.GRAY + "Your GameMode Has Been Set To" + ChatColor.DARK_GREEN
							+ " Survival");
					p.sendMessage(ChatColor.GRAY + "You Set " + ChatColor.GRAY + targetP.getName() + ChatColor.GRAY
							+ " Gamemode to " + ChatColor.DARK_GREEN + "Survival");
				} else if (mode == 1) {
					targetP.setGameMode(GameMode.CREATIVE);
					targetP.sendMessage(ChatColor.GRAY + "Your GameMode Has Been Set To" + ChatColor.DARK_GREEN
							+ " Creative");
					p.sendMessage(ChatColor.GRAY + "You Set " + ChatColor.GRAY + targetP.getName() + ChatColor.GRAY
							+ " Gamemode to " + ChatColor.DARK_GREEN + "Creative");
				} else if (mode == 2) {
					targetP.setGameMode(GameMode.ADVENTURE);
					targetP.sendMessage(ChatColor.GRAY + "Your GameMode Has Been Set To" + ChatColor.DARK_GREEN
							+ " Adventure");
					p.sendMessage(ChatColor.GRAY + "You Set " + ChatColor.GRAY + targetP.getName() + ChatColor.GRAY
							+ " Gamemode to " + ChatColor.DARK_GREEN + "Adventure");
				} else {
					p.sendMessage(ChatColor.DARK_RED + "" + mode + ChatColor.RED + "Is Not A Available Gamemode");
				}
			} else {
				p.sendMessage(ChatColor.DARK_RED + targetPlayer + ChatColor.RED + "Is Not Online");
			}
		} else {
			p.sendMessage(ChatColor.RED + "Too Many Arguments!");
			p.sendMessage(ChatColor.GRAY + "/gm <1/2/3> <Player (optional)>");
		}
	}

	FileConfiguration getConfig() {
		return Commands.plugin.getConfig();
	}
}
