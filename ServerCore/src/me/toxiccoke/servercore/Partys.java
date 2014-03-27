package me.toxiccoke.servercore;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Partys implements CommandExecutor {

	LinkedList<Party> parties;
	LinkedList<PartyInvite> invites;

	public Partys() {
		parties = new LinkedList<Party>();
		invites = new LinkedList<Partys.PartyInvite>();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player");
			return true;
		}
		Player p = (Player) sender;
		String cName = cmd.getName().toLowerCase();
		if (!cName.equals("party"))
			return false;

		if (args.length == 0) {
			p.sendMessage("Do /party help for commands");
			Party party = getParty(p);
			if (party != null)
				list(party, p);
			return true;

		} else {
			String arg1 = args[0];
			if (arg1.equals("help")) {
				help(p);
				return true;
			} else if (arg1.equals("create")) {

				boolean owner = false;
				boolean inParty = false;
				for (Party part : parties)
					if (part.owner.getName().equals(p.getName())) {
						owner = true;
						break;
					} else
						for (Player s : part.players)
							if (s.getName().equals(p.getName())) {
								inParty = true;
								break;
							}
				if (owner) {
					p.sendMessage("You already have a party! Do /party disband");
					return true;
				} else if (inParty) {
					p.sendMessage("You cannot create a party because you are in a party already. Do /party leave");
					return true;
				}
				// create a party
				Party newParty = new Party(p);
				parties.add(newParty);
				p.sendMessage("Party created");
				return true;
			} else if (arg1.equals("invite")) {
				if (!isInParty(p)) {
					p.sendMessage("You are not in a party!");
					return true;
				}
				if (args.length < 2) {
					p.sendMessage("Proper usage is /party invte [player]");
					return true;
				}
				String name = args[1];
				OfflinePlayer invi = Bukkit.getServer().getOfflinePlayer(name);
				if (invi == null) {
					p.sendMessage("Unknown player: " + name);
					return true;
				}
				if (p.getName().equals(invi.getName())) {
					p.sendMessage("You can't invite yourself");
					return true;
				}

				if (!invi.isOnline()) {
					p.sendMessage(name + " is not online");
					return true;
				}
				Player invited = Bukkit.getServer().getPlayer(name);
				// Delete old invite
				PartyInvite i = getInvite(invited);
				if (i != null)
					invites.remove(i);
				invites.add(new PartyInvite(invited, getParty(p)));
				invited.sendMessage("You have been invited to join " + p.getName() + "'s party");
				invited.sendMessage("To join " + p.getName() + "'s party, type /party accept");
				return true;

			} else if (arg1.equals("leave")) {
				Party party = null;
				boolean owner = false;
				for (Party part : parties)
					if (part.owner.getName().equals(p.getName())) {
						party = part;
						owner = true;
						break;
					} else
						for (Player s : part.players)
							if (s.getName().equals(p.getName())) {
								party = part;
								break;
							}
				if (party == null) {
					p.sendMessage("You do not belong to a party");
					return true;
				}
				if (owner) {
					p.sendMessage("Use /party disband");
					return true;
				}
				party.players.remove(p);
				p.sendMessage("You have left the party");
				for (Player friend : party.players)
					if (friend.isOnline())
						friend.sendMessage(p.getName() + " has left the party");
				if (party.owner.isOnline())
					party.owner.sendMessage(p.getName() + " has left the party");

				return true;

			} else if (arg1.equals("list")) {
				Party party = getParty(p);
				if (party == null)
					p.sendMessage("You are not in a party!");
				else
					list(party, p);
				return true;
			} else if (arg1.equals("accept")) {
				PartyInvite i = getInvite(p);
				if (i == null) {
					p.sendMessage("You have not been invited to join a party");
					return true;
				}
				// can't join a party if you are in one
				if (isInParty(p)) {
					p.sendMessage("You are already in a party. To leave, do /party leave");
					return true;
				}
				// The party may have been disbanded
				boolean disbanded = true;
				for (Party party : parties)
					if (party.equals(i.party)) {
						disbanded = false;
						break;
					}
				if (disbanded) {
					p.sendMessage("That party has been disbanded");
					return true;
				}
				invites.remove(i);
				i.party.players.add(p);
				p.sendMessage("You have joined a party");
				list(i.party, p);
				for (Player friend : i.party.players)
					if (friend.isOnline())
						friend.sendMessage(p + " has join the party");
				if (i.party.owner.isOnline())
					i.party.owner.sendMessage(p + " has join the party");

				return true;
			} else if (arg1.equals("disband")) {
				for (Party part : parties)
					if (part.owner.getName().equals(p.getName())) {
						for (Player member : part.players)
							if (member.isOnline())
								member.sendMessage("Your party has been disbanded");
						parties.remove(part);
						p.sendMessage("Party disbanded");
						return true;
					}
				p.sendMessage("You do not own a party");
				return true;
			}

		}
		return false;

	}

	private void list(Party party, Player p) {
		StringBuilder s = new StringBuilder("Party: ");
		if (party.owner.isOnline())
			s.append(ChatColor.RED);
		else
			s.append(ChatColor.GRAY);
		s.append(party.owner.getName());
		for (Player player : party.players) {
			s.append(" ");
			if (player.isOnline())
				s.append(ChatColor.YELLOW);
			else
				s.append(ChatColor.GRAY);
			s.append(player.getName());
		}
		p.sendMessage(s.toString());
	}

	private PartyInvite getInvite(Player plr) {
		for (PartyInvite i : invites) {
			if (i.player.getName().equals(plr.getName()))
				return i;
		}
		return null;
	}

	private boolean isInParty(Player plr) {
		return getParty(plr) != null;
	}

	private Party getParty(Player plr) {
		for (Party part : parties)
			if (part.owner.getName().equals(plr.getName()))
				return part;
			else
				for (Player s : part.players)
					if (s.getName().equals(plr.getName()))
						return part;

		return null;
	}

	private void help(Player p) {
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

	class Party {
		ArrayList<Player> players;
		Player owner;

		public Party(Player owner) {
			this.owner = owner;
			players = new ArrayList<Player>();
		}
	}

	class PartyInvite {
		Player player;
		Party party;

		public PartyInvite(Player plr, Party p) {
			this.player = plr;
			this.party = p;
		}
	}
}
