package me.toxiccoke.servercore;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Partys extends JavaPlugin implements Listener{
	 public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
	       Player p = (Player) sender;
		
		if(cmd.getName().equalsIgnoreCase("party help")){
			if(!sender.hasPermission("party.help")){
	    		   sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GOLD + "AquilaMc" + ChatColor.GRAY + "]" + ChatColor.RED + "You dont have acsses to this command");
			if(args.length == 0){
				p.sendMessage(ChatColor.GOLD + "-----------------------------------------------------");
				p.sendMessage(ChatColor.GREEN + "Party Commands");
				p.sendMessage(ChatColor.YELLOW + "/party help" + ChatColor.BLUE + " - Prints this help message");
				p.sendMessage(ChatColor.YELLOW + "/party create" + ChatColor.BLUE + " - Creates a game party");
				p.sendMessage(ChatColor.YELLOW + "/party invite" + ChatColor.BLUE + " - Invites players to your party");
				p.sendMessage(ChatColor.YELLOW + "/party leave" + ChatColor.BLUE + " - Leaves the party that you are in");
				p.sendMessage(ChatColor.YELLOW + "/party list" + ChatColor.BLUE + " - Lists the players in your game party");
				p.sendMessage(ChatColor.YELLOW + "/party accept" + ChatColor.BLUE + " - Accepts to join a party");
				p.sendMessage(ChatColor.YELLOW + "/party disband" + ChatColor.BLUE + " - Destroys your game party");
				p.sendMessage(ChatColor.GOLD + "-----------------------------------------------------");
				
			}
		}
		}
		return false;
		
	}

}

