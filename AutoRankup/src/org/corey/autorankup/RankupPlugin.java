package org.corey.autorankup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.data.Group;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RankupPlugin extends JavaPlugin implements Listener {

	public static RankupPlugin	plugin;
	private GroupManager		groupManager;

	ArrayList<PromoteRank>		ranks;

	public void onEnable() {
		plugin = this;
		PlayTimeAPI.load();
		getServer().getPluginManager().registerEvents(new RankupListener(), this);
		getServer().getPluginManager().registerEvents(this, this);

		loadRanks();

		for (Player p : Bukkit.getOnlinePlayers())
			PlayTimeAPI.getPlayer(p).login();
	}

	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers())
			PlayTimeAPI.getPlayer(p).logout();
		PlayTimeAPI.save();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginEnable(final PluginEnableEvent event) {
		getGroupManager();
	}

	private void getGroupManager() {
		final PluginManager pluginManager = plugin.getServer().getPluginManager();
		final Plugin GMplugin = pluginManager.getPlugin("GroupManager");
		if (GMplugin != null && GMplugin.isEnabled()) {
			groupManager = (GroupManager) GMplugin;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginDisable(PluginDisableEvent event) {
		if (groupManager != null) {
			if (event.getPlugin().getDescription().getName().equals("GroupManager")) {
				groupManager = null;
			}
		}
	}

	public String getGroup(final Player base) {
		final AnjoPermissionsHandler handler = groupManager.getWorldsHolder().getWorldPermissions(base);
		if (handler == null) { return null; }
		return handler.getGroup(base.getName());
	}

	public boolean setGroup(final Player base, final String group) {
		final OverloadedWorldHolder handler = groupManager.getWorldsHolder().getWorldData(base);
		if (handler == null) { return false; }
		handler.getUser(base.getName()).setGroup(handler.getGroup(group));
		return true;
	}

	public List<String> getGroups(final Player base) {
		final AnjoPermissionsHandler handler = groupManager.getWorldsHolder().getWorldPermissions(base);
		if (handler == null) { return null; }
		return Arrays.asList(handler.getGroups(base.getName()));
	}

	public boolean groupExists(Player base, String group) {
		final OverloadedWorldHolder handler = groupManager.getWorldsHolder().getWorldData(base);
		if (handler == null) { return false; }
		for (Group g : handler.getGroupList())
			if (g.getName().equals(group))
				return true;
		return false;
	}
	
	private void loadRanks() {
		ranks = new ArrayList<PromoteRank>();
		File parent = getDataFolder();
		if (!parent.exists())
			parent.mkdirs();
		File f = new File(parent, "rank.txt");
		if (!f.exists()) {
			// copy default one to the right place
			copy(getResource("rank.txt"), f);
		}
		try {
			Scanner sc = new Scanner(f);
			while (true) {
				try {
					String[] s = sc.nextLine().split("\\|");

					int time = Integer.parseInt(s[0]);
					ranks.add(new PromoteRank(time * 60, s[1], s[2]));
				} catch (NoSuchElementException exc) {
					break;
				}
			}
			sc.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String cmd = command.getName().toLowerCase();
		for (int i = 0; i < args.length; i++)
			args[i] = args[i].toLowerCase();
		if (cmd.equals("promote")) {
			if (groupManager == null) {
				getGroupManager();
				if (groupManager == null) {
					sender.sendMessage(ChatColor.RED + "Error! GroupManager not loaded! Derp!");
					return true;
				}
			}
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Command must not be run from console");
				return true;
			}
			Player plr = (Player) sender;
			PlayTime t = PlayTimeAPI.getPlayer(plr);
			checkPromote(plr, t.getMinutes());
			return true;
		} else if (cmd.equals("playtime")) {
			PlayTime t = null;
			if (args.length == 0) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Specify a user");
					return true;
				} else {
					t = PlayTimeAPI.getPlayer((Player) sender);
				}
			} else {
				// look for the player on the server
				OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
				if (p != null)
					t = PlayTimeAPI.getDontAddPlayer(p);
				if (p == null || t == null) {
					sender.sendMessage(ChatColor.RED + "Cannot find player " + args[0]);
					return true;
				}
			}
			OfflinePlayer p = Bukkit.getServer().getOfflinePlayer(t.player);
			sender.sendMessage(ChatColor.GOLD + "Player: " + p.getName());
			sender.sendMessage(ChatColor.GOLD + "Play Time: " + t.getHours() + " hours " + (t.getMinutes() % 60) + " minutes");
			sender.sendMessage(ChatColor.GOLD + "Current Session: " + t.getCurrentMinutes() + " minutes");
			return true;
		}
		return false;
	}

	private void checkPromote(Player player, long minutes) {
		List<String> groups = getGroups(player);
		PromoteRank cur = getRank(player, groups);
		if (cur != null)
			player.sendMessage(ChatColor.GOLD + "Your Group: " + cur.rank);
		PromoteRank nrank = null;

		for (PromoteRank pr : ranks) {
			if (pr == cur)
				continue;
			if (cur != null) {
				// find the one with the lowest minutes
				if (nrank != null) {
					if (pr.minutes > cur.minutes && pr.minutes < nrank.minutes)
						nrank = pr;
				} else if (pr.minutes > cur.minutes)
					nrank = pr;
			} else {
				// find the one with the lowest minutes
				if (nrank != null) {
					if (pr.minutes < nrank.minutes)
						nrank = pr;
				} else nrank = pr;
			}

		}
		if (nrank != null) {
			if (minutes >= nrank.minutes) {
				if (!groupExists(player, nrank.rank))
					player.sendMessage(ChatColor.RED + "GroupManager Error: No such group: " + nrank.rank);
				else {
					setGroup(player, nrank.rank);
					player.sendMessage(ChatColor.GREEN + nrank.message);
				}
			} else player.sendMessage(ChatColor.GOLD + "You need " + (nrank.minutes - minutes) + " more minutes to get " + nrank.rank);
		} else player.sendMessage(ChatColor.GOLD + "You are the highest rank!");
	}

	private PromoteRank getRank(Player player, List<String> groups) {
		for (String g : groups)
			for (PromoteRank r : ranks)
				if (r.rank.equalsIgnoreCase(g))
					return r;
		return null;
	}
}
