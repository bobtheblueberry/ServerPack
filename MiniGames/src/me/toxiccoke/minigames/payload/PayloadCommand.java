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
			p.sendMessage(ChatColor.RED + "/" + label + " track <World>");
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
		}

		return true;
	}

}
