package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.GameLobby;
import me.toxiccoke.minigames.GameWorld;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayloadCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player");
			return true;
		}
		if (!command.getName().equalsIgnoreCase("padmin"))
			return false;
		Player p = (Player) sender;
		// /padmin track badwater
		if (args.length < 1) {
			p.sendMessage(ChatColor.RED + "/" + label + " [track|addcheckpoint|bars|healthpack|ammopack] <World>");
			return true;
		} else if (args.length < 2) {
			p.sendMessage(ChatColor.RED + "/" + label + " " + args[0] + " <World>");
			return true;
		}
		PayloadGame game = null;
		for (GameWorld<?> g : GameLobby.lobby.games)
			if (g instanceof PayloadGame && g.getWorldName().equalsIgnoreCase(args[1])) {
				game = (PayloadGame) g;
				break;
			}
		if (game == null) {
			p.sendMessage(ChatColor.RED + "Unknown world: " + args[1]);
			return true;
		}
		
		if (args[0].equals("track")) {
			p.sendMessage(ChatColor.GREEN + "Click the first track in the index");
			PayloadEventHandler.minetrackSet = p;
			PayloadEventHandler.game = game;
			return true;
		} else if (args[0].equals("addcheckpoint")) {
			game.checkpoints.add(p.getLocation().getBlock().getLocation());
			game.checkedpoints = new boolean[game.checkpoints.size()];
			game.save();
			p.sendMessage(ChatColor.RED + "Waypoint added");
			return true;
		} else if (args[0].equals("bars")) {
			if (PayloadEventHandler.barSet != null) {
				p.sendMessage(ChatColor.RED + "Done!");
				PayloadEventHandler.game.save();
				PayloadEventHandler.barSet = null;
				return true;
			}
			PayloadEventHandler.barSet = p;
			PayloadEventHandler.game = game;
			p.sendMessage(ChatColor.RED + "Click the blocks you want to add, then do /" + label +" bars " + args[1]);
			return true;
		} else if (args[0].equals("healthpack")) {
			game.healthpacks.add(p.getLocation().getBlock().getLocation());
			game.save();
			p.sendMessage(ChatColor.RED + "Health pack added");
			return true;
		}  else if (args[0].equals("ammopack")) {
			game.ammopacks.add(p.getLocation().getBlock().getLocation());
			game.save();
			p.sendMessage(ChatColor.RED + "Ammo pack added");
			return true;
		} 

		return true;
	}

}
