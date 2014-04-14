package org.corey.autorankup;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.OfflinePlayer;

public class PlayTime implements Serializable {
	private static final long	serialVersionUID	= 8645098772722367950L;
	UUID						player;
	private long				totalTime;
	private transient long		loginTime;

	public PlayTime(OfflinePlayer p) {
		player = p.getUniqueId();
		loginTime = System.currentTimeMillis();
		totalTime = 0;
	}

	public long getMinutes() {
		if (loginTime == 0)
			return TimeUnit.MILLISECONDS.toMinutes(totalTime);
		else
			return TimeUnit.MILLISECONDS.toMinutes(totalTime + (System.currentTimeMillis() - loginTime));
	}

	public long getCurrentMinutes() {
		if (loginTime == 0)
			return 0;
		return TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - loginTime);
	}

	public long getHours() {
		return getMinutes() / 60;
	}

	public void logout() {
		totalTime += (System.currentTimeMillis() - loginTime);
		loginTime = 0;
	}

	public void login() {
		loginTime = System.currentTimeMillis();
	}
}
