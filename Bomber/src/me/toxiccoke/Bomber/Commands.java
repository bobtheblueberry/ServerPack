package me.toxiccoke.Bomber;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Commands implements Listener {
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[]args){
		Player p = (Player) sender;
		
		if(cmd.getName().equalsIgnoreCase("b")){
			p.sendMessage(ChatColor.GOLD + "-----------------------------------------------------");
			p.sendMessage(ChatColor.GRAY + "[" + ChatColor.BLUE + "BomberPVP" + ChatColor.GRAY + "]");
			p.sendMessage(ChatColor.GREEN + "/bjoin -" + ChatColor.RED + " Puts you in to a bomber game");
			p.sendMessage(ChatColor.GOLD + "-----------------------------------------------------");
			
		}else if(cmd.getName().equalsIgnoreCase("bjoin")){
			int i = 0;
			for(Player player : Bukkit.getOnlinePlayers()){
				if(i < Bukkit.getOnlinePlayers().length/2){
					Team.addToTeam(TeamType.RED, player);
				}else{
					Team.addToTeam(TeamType.BLUE, player);
				
				}
				i++;
				
			}
		}else if(cmd.getName().equalsIgnoreCase("bteam")){
			sender.sendMessage(ChatColor.GRAY + Team.getTeamType(((Player)sender)).name());
				
			}
		return true;
		
	}

}

