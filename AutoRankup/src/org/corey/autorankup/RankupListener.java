package org.corey.autorankup;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RankupListener implements Listener, Runnable {

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		PlayTimeAPI.getPlayer(event.getPlayer()).login();
	}
	
	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		PlayTime t = PlayTimeAPI.getPlayer(event.getPlayer());
		t.logout();
		Bukkit.getScheduler().scheduleSyncDelayedTask(RankupPlugin.plugin, this);
	}
	
	@Override
	public void run() {
		PlayTimeAPI.save();
	}
}
