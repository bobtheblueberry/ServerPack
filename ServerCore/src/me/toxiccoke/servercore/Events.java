package me.toxiccoke.servercore;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public class Events implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Bukkit.getLogger().log(Level.INFO, e.getPlayer().getName() + " Connected");
		e.setJoinMessage(null);
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
		e.setQuitMessage(null);
		Bukkit.getLogger().log(Level.INFO, e.getPlayer().getName() + " Disconnected");
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		CommandHandler.instance.addBack(p);
		p.sendMessage(ChatColor.GOLD + "Do /back to return to the place you died");
		p.sendMessage(ChatColor.GREEN + "You're a complete loser!");
		p.sendMessage("https://www.youtube.com/watch?v=BvUZijEuNDQ");
	}
}

