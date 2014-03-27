package me.toxiccoke.servercore;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Commands extends JavaPlugin implements Listener {

	final Logger log = Logger.getLogger("Minecraft");
	public static Commands plugin;

	
	@Override
	public void onEnable() {
		System.out.println("ServerCore Enabled");
		plugin = this;

		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new Hats(), this);
		CommandHandler ch = new CommandHandler();
		getCommand("gm").setExecutor(ch);
		getCommand("fly").setExecutor(ch);
		getCommand("heal").setExecutor(ch);
		getCommand("tp").setExecutor(ch);
		getCommand("warn").setExecutor(ch);
		getCommand("day").setExecutor(ch);
		getCommand("night").setExecutor(ch);
		getCommand("sun").setExecutor(ch);
		getCommand("rain").setExecutor(ch);
		getCommand("spawn").setExecutor(ch);
		getCommand("setspawn").setExecutor(ch);
		getCommand("ban").setExecutor(ch);
		getCommand("kick").setExecutor(ch);
		// getCommand("help").setExecutor(ch);
		getCommand("poke").setExecutor(ch);
		getCommand("v").setExecutor(ch);
		getCommand("nick").setExecutor(ch);

		Partys p = new Partys();
		getCommand("party").setExecutor(p);

	}

	ArrayList<Player> vanished = new ArrayList<Player>();

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		for (Player pl : vanished)
			if (p.getName().equals(pl.getName()))
				e.getPlayer().hidePlayer(pl);

		Object o = this.getConfig().get(p.getName());
		if (o != null)
			p.setDisplayName(o.toString());
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		vanished.remove(e.getPlayer());
	}
}