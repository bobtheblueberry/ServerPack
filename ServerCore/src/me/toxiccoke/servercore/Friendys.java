package me.toxiccoke.servercore;

import me.toxiccoke.servercore.FriendAPI.FriendRequest;
import me.toxiccoke.servercore.FriendAPI.Person;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Friendys implements CommandExecutor, Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		// Tell their friends if they are online
		Person person = FriendAPI.get(p.getName());
		if (person == null)
			return;
		for (String s : person.getFriends()) {
			Player friend = Bukkit.getServer().getPlayer(s);
			if (friend == null || !friend.isOnline())
				continue;
			friend.sendMessage(ChatColor.GREEN + "[" + ChatColor.RED + "+" + ChatColor.GREEN + "]" + ChatColor.YELLOW
					+ p.getName());
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		// Tell their friends if they are online
		Person person = FriendAPI.get(p.getName());
		if (person == null)
			return;
		for (String s : person.getFriends()) {
			Player friend = Bukkit.getServer().getPlayer(s);
			if (friend == null || !friend.isOnline())
				continue;

			friend.sendMessage(ChatColor.GREEN + "[" + ChatColor.BLUE + "-" + ChatColor.GREEN + "]" + ChatColor.YELLOW
					+ p.getName());
		}
	}
	

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player");
			return true;
		}
		Player p = (Player) sender;
		String cName = cmd.getName().toLowerCase();
		if (!cName.equals("friend"))
			return false;
		if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
			help(p);
			return true;
		}
		String arg1 = args[0].toLowerCase();
		if (arg1.equals("list")) {
			list(p);
			return true;
		} else if (arg1.equals("accept")) {
			FriendRequest req = FriendAPI.getRequest(p.getName());
			if (req == null) {
				p.sendMessage(ChatColor.GOLD + "You do not have a pending friend request");
				return true;
			}
			FriendAPI.acceptRequest(req);
			p.sendMessage(req.sender + ChatColor.GOLD + " has been added to your friends list");
			Player sdr = Bukkit.getServer().getPlayer(req.sender);
			if (sdr != null)
				sdr.sendMessage(req.confirmer + ChatColor.GOLD + " has confirmed your friend request");
			return true;
		} else if (arg1.equals("add")) {
			if (args.length != 2) {
				p.sendMessage(ChatColor.GOLD + "Proper usage is �e/friend add (player)");
				return true;
			}
			Player c = Bukkit.getServer().getPlayer(args[1]);
			if (c == null || !c.isOnline()) {
				p.sendMessage(ChatColor.GOLD + "Unknown Player: " + ChatColor.GRAY + args[1]);
				return true;
			}
			if (c.equals(p)) {
				p.sendMessage(ChatColor.GOLD + "You can't add yourself as a friend");
				return true;
			}
			if (FriendAPI.friends(p.getName(), c.getName())) {
				p.sendMessage(ChatColor.GOLD + "You are already friends with " + c.getName());
				return true;
			}
			Person person = FriendAPI.get(c.getName());
			if (person != null && person.isRequestsDisabled()) {
				p.sendMessage(c.getName() + ChatColor.GOLD + " has friend requests disabled");
				return true;
			}
			FriendAPI.addRequest(p.getName(), c.getName());
			p.sendMessage(ChatColor.GOLD + "Friend Request sent");
			c.sendMessage(p.getName() + ChatColor.GOLD + " has requested that you become friends");
			c.sendMessage(ChatColor.GOLD + "Type �e/friend accept�6 to accept or �e/friend deny�6 to deny");
			return true;
		} else if (arg1.equals("remove") || arg1.equals("delete")) {
			if (args.length != 2) {
				p.sendMessage(ChatColor.GOLD + "Proper usage is �e/friend remove (player)");
				return true;
			}
			String sourcat = args[1];
			if (!FriendAPI.friends(p.getName(), sourcat)) {
				p.sendMessage(ChatColor.GOLD + "You are not friends with " + sourcat);
				return true;
			}
			FriendAPI.unfriend(p.getName(), sourcat);

			p.sendMessage(ChatColor.GOLD + "You have parted ways with " + sourcat);
			Player scat = Bukkit.getServer().getPlayer(sourcat);
			if (scat != null && scat.isOnline())
				scat.sendMessage(p.getName() + ChatColor.GOLD + " has unfriended you.");
			return true;
		} else if (arg1.equals("deny")) {
			FriendRequest req = FriendAPI.getRequest(p.getName());
			if (req == null) {
				p.sendMessage(ChatColor.GOLD + "You do not have a pending friend request");
				return true;
			}
			FriendAPI.denyRequest(req);
			p.sendMessage(req.sender + "'s " + ChatColor.GOLD + " friend request was denied.");
			Player sdr = Bukkit.getServer().getPlayer(req.sender);
			if (sdr != null)
				sdr.sendMessage(req.confirmer + ChatColor.GOLD + " denied your request to become friends");
			return true;
		} else if (arg1.equals("disable")) {
			FriendAPI.getAdd(p.getName()).setRequestsDisabled(true);
			p.sendMessage(ChatColor.GOLD + "Friend Requests disabled.");
			return true;
		} else if (arg1.equals("enable")) {
			FriendAPI.getAdd(p.getName()).setRequestsDisabled(false);
			p.sendMessage(ChatColor.GOLD + "Friend Requests enabled.");
			return true;
		}
		help(p);
		return true;
	}

	private void help(Player p) {
		p.sendMessage(ChatColor.GOLD + "-----------------------------------------------------");
		p.sendMessage(ChatColor.GREEN + "Friend Commands");
		Person per = FriendAPI.get(p.getName());
		String msg = (per != null && per.isRequestsDisabled()) ? "disabled" : "enabled";
		p.sendMessage(ChatColor.YELLOW + "Friend requests are currently " + msg);

		p.sendMessage(ChatColor.YELLOW + "/friend help" + ChatColor.BLUE + " - Prints this help message");
		p.sendMessage(ChatColor.YELLOW + "/friend list" + ChatColor.BLUE + " - Lists your friends");
		p.sendMessage(ChatColor.YELLOW + "/friend accept" + ChatColor.BLUE + " - Accept friend request");
		p.sendMessage(ChatColor.YELLOW + "/friend deny" + ChatColor.BLUE + " - Deny friend request");
		p.sendMessage(ChatColor.YELLOW + "/friend add [name]" + ChatColor.BLUE + " - Adds a friend");
		p.sendMessage(ChatColor.YELLOW + "/friend remove [name]" + ChatColor.BLUE + " - Removes a friend");
		p.sendMessage(ChatColor.YELLOW + "/friend disable" + ChatColor.BLUE
				+ " - Disables other players from adding you as a friend");
		p.sendMessage(ChatColor.YELLOW + "/friend enable" + ChatColor.BLUE + " - Re-enter the social world");
		p.sendMessage(ChatColor.GOLD + "-----------------------------------------------------");
	}

	private void list(Player p) {
		StringBuilder s = new StringBuilder(ChatColor.GOLD + "Friends:");
		for (String f : FriendAPI.getFriends(p.getName())) {
			s.append(" ");
			Player player = Bukkit.getServer().getPlayer(f);
			if (player != null && player.isOnline())
				s.append(ChatColor.BLUE);
			else
				s.append(ChatColor.GRAY);
			s.append(f);
		}
		p.sendMessage(s.toString());
	}
}
