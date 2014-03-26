package me.toxiccoke.servercore;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Events extends JavaPlugin implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		e.setJoinMessage(ChatColor.DARK_GRAY + p.getDisplayName()
				+ ChatColor.GRAY + " has joined");
		p.sendMessage(ChatColor.GOLD + "    Welcome to AquilaMC Lobby    ");
		p.sendMessage(ChatColor.GREEN + "    Please read the" + ChatColor.RED
				+ " Rules");
		p.sendMessage(ChatColor.GREEN + "    Also check out our "
				+ ChatColor.BLUE + "Website");

		Firework f = (Firework) e.getPlayer().getWorld()
				.spawn(e.getPlayer().getLocation(), Firework.class);

		FireworkMeta fm = f.getFireworkMeta();
		fm.addEffect(FireworkEffect.builder().flicker(false).trail(true)
				.with(Type.CREEPER).withColor(Color.GREEN).withFade(Color.BLUE)
				.build());
		fm.setPower(3);
		f.setFireworkMeta(fm);

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		e.setQuitMessage(" ");
	}
}
