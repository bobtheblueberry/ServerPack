package org.corey.autorankup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;

import org.bukkit.OfflinePlayer;

public class PlayTimeAPI {

	static LinkedList<PlayTime> players = new LinkedList<PlayTime>();

	private PlayTimeAPI() {
	}

	public static PlayTime getPlayer(OfflinePlayer p) {
		for (PlayTime t : players)
			if (p.getUniqueId().equals(t.player))
				return t;
		PlayTime pt = new PlayTime(p);
		players.add(pt);
		return pt;
	}
	
	public static PlayTime getDontAddPlayer(OfflinePlayer p) {
		for (PlayTime t : players)
			if (p.getUniqueId().equals(t.player))
				return t;
		return null;
	}

	static private File getFile() {
		File parent = RankupPlugin.plugin.getDataFolder();
		if (!parent.exists())
			parent.mkdirs();
		File f = new File(parent, "playtime.dat");
		return f;
	}

	protected static void save() {
		try {
			ObjectOutputStream out = new ObjectOutputStream(
					new BufferedOutputStream(new FileOutputStream(getFile())));
			write4(out, players.size());
			for (PlayTime t : players)
				out.writeObject(t);
			out.close();
		} catch (IOException e) {
			System.err.println("Error! Cannot save player time datafile "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	protected static void load() {
		File f = getFile();
		if (!f.exists())
			return;
		players.clear();
		try {
			ObjectInputStream in = new ObjectInputStream(
					new BufferedInputStream(new FileInputStream(f)));
			int size = read4(in);
			for (int i = 0; i < size; i++)
				players.add((PlayTime) in.readObject());
			in.close();
		} catch (IOException | ClassNotFoundException exc) {
			System.err.println("Error! Cannot load player time datafile "
					+ exc.getMessage());
			exc.printStackTrace();
		}
	}

	private static void write4(OutputStream s, int val) throws IOException {
		s.write(val & 255);
		s.write((val >>> 8) & 255);
		s.write((val >>> 16) & 255);
		s.write((val >>> 24) & 255);
	}

	private static int read4(InputStream i) throws IOException {
		int a = i.read();
		int b = i.read();
		int c = i.read();
		int d = i.read();
		return (a | (b << 8) | (c << 16) | (d << 24));
	}

}
