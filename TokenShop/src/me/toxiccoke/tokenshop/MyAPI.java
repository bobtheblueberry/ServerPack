package me.toxiccoke.tokenshop;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import code.husky.Database;
import code.husky.mysql.MySQL;
import code.husky.sqlite.SQLite;

import java.sql.*;

public class MyAPI {
	private final static String table = "CoinEconomy2";

	private MyAPI() {
	}

	public static void giveCoins(Player p, int i) {
		int coins = getCoinCount(p) + i;
		setCoins(p, coins, false);
		p.sendMessage(ChatColor.GOLD + "§6" + "[AquilaMc] " + "§a" + i
				+ "§c Coins added to your bank");
	}

	public static void takeCoins(Player p, int i) {
		int coins = getCoinCount(p) - i;
		setCoins(p, coins, false);
		p.sendMessage(ChatColor.GOLD + "§6" + "[AquilaMc] " + "§a" + i
				+ "§c Coins removed from your bank");
	}

	public static void setCoins(Player p, int i) {
		setCoins(p, i, true);
	}

	public static boolean hasEnough(Player p, int i) {
		if (getCoinCount(p) >= i)
			return true;
		return false;
	}

	private static void setCoins(Player p, int coins, boolean msg) {
		try {
			Connection db = getConnection();
			String exec = "UPDATE " + table + " SET coins = " + coins
					+ " WHERE player = '" + p.getName() + "'";
			db.createStatement().execute(exec);
			db.close();
			TokenShop.showScoreboard(p);
		} catch (SQLException exc) {
			System.out.println("cannot change coin count for " + p.getName());
			System.out.println(exc.getMessage());
			exc.printStackTrace();
		}
		if (!msg)
			return;
		p.sendMessage(ChatColor.GOLD + "§6" + "[AquilaMc] " + "§c"
				+ " New coin balance: §a" + coins);
	}

	public static int getCoinCount(Player p) {
		try {
			Connection db = getConnection();
			String exec = "SELECT coins FROM " + table + " WHERE player = '"
					+ p.getName() + "'";
			ResultSet rs = db.createStatement().executeQuery(exec);
			if (rs.next()) {
				int i = rs.getInt("coins");
				db.close();
				return i;
			} else {
				checkPlayer(p.getName());
			}
			return 0;
		} catch (SQLException exc) {
			System.out.println("cannot get coin count for " + p.getName());
			System.out.println(exc.getMessage());
			exc.printStackTrace();
		}

		return 0;
	}

	private static void setValue(String player, String name, int index,
			boolean value) {
		int val = getVal(player, name);
		try {
			Connection db = getConnection();
			BooleanArray32 b = new BooleanArray32(val);
			b.set(index, value);
			String exec = "UPDATE " + table + " SET " + name + " = " + b.values
					+ " WHERE player = '" + player + "'";
			db.createStatement().execute(exec);
			db.close();
		} catch (SQLException exc) {
			System.out.println("cannot execute SQL set value");
			System.out.println(exc.getMessage());
			exc.printStackTrace();
		}
	}

	protected static int getVal(String player, String name) {
		try {
			Connection db = getConnection();
			String exec = "SELECT " + name + " FROM " + table
					+ " WHERE player = '" + player + "'";
			ResultSet rs = db.createStatement().executeQuery(exec);
			if (rs.next()) {
				int i = rs.getInt(name);
				db.close();
				return i;
			} else {
				checkPlayer(player);
			}
		} catch (SQLException exc) {
			System.out.println("cannot execute SQL");
			System.out.println(exc.getMessage());
			exc.printStackTrace();
		}
		return 0;
	}

	private static boolean hasValue(String player, String name, int index) {
		return new BooleanArray32(getVal(player, name)).get(index);
	}

	public static boolean hasToy(String player, int index) {
		return hasValue(player, "toys", index);
	}

	public static boolean hasHat(String player, int index) {
		return hasValue(player, "hats", index);
	}

	public static boolean hasPet(String player, int index) {
		return hasValue(player, "pets", index);
	}

	public static void setPet(String player, int index, boolean value) {
		setValue(player, "pets", index, value);
	}

	public static void setToy(String player, int index, boolean value) {
		setValue(player, "toys", index, value);
	}

	public static void setHat(String player, int index, boolean value) {
		setValue(player, "hats", index, value);
	}

	public static boolean init() {

		String sqlCreate = "CREATE TABLE IF NOT EXISTS " + table
				+ "  (player           VARCHAR(30),"
				+ "   coins            INTEGER, "
				+ "   pets            INTEGER DEFAULT 0,"
				+ "   toys            INTEGER DEFAULT 0,"
				+ "   hats            INTEGER DEFAULT 0)";

		try {
			Connection db = getConnection();
			if (db == null) {
				System.out.println("DB is null");
				return false;
			}
			db.createStatement().execute(sqlCreate);
			db.close();
			System.out
					.println("Economy successfully connected to the database");
		} catch (Throwable e) {
			System.out.println("Economy failed to connected to the database");
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;

	}

	/**
	 * Adds a player to the database if he is not there
	 * 
	 * @param name
	 *            player's name
	 */
	public static void checkPlayer(String name) {
		try {
			Connection db = getConnection();
			String exec = "SELECT coins FROM " + table + " WHERE player = '"
					+ name + "'";
			ResultSet rs = db.createStatement().executeQuery(exec);
			if (rs.next()) {
				db.close();
				return;
			} else
				db.createStatement().executeUpdate(
						"INSERT INTO " + table + " (player,coins) VALUES ('"
								+ name + "', 0)");
			db.close();
		} catch (SQLException e) {
			System.out.println("cannot check player");
			e.printStackTrace();
		}

	}

	private static String userName = "root";
	private static String password = "";

	private static boolean useMysql = false;

	public static Connection getConnection() throws SQLException {

		Database sql;
		if (useMysql) {
			sql = new MySQL(TokenShop.plugin, "localhost", "3306", "Economy",
					userName, password);
		} else {
			sql = new SQLite(TokenShop.plugin, "tokens");

		}
		return sql.openConnection();
	}

	public static class BooleanArray32 {
		private int values;

		public BooleanArray32(int v) {
			this.values = v;
		}

		public boolean get(int pos) {
			return (values & (1 << pos)) != 0;
		}

		public void set(int pos, boolean value) {
			int mask = 1 << pos;
			values = (values & ~mask) | (value ? mask : 0);
		}

		public int getValues() {
			return values;
		}

		public int compare(BooleanArray32 b2) {
			return countBits(b2.values & values);
		}

		// From http://graphics.stanford.edu/~seander/bithacks.html
		// Disclaimer: I did not fully double check whether this works for
		// Java's signed ints
		public static int countBits(int v) {
			v = v - ((v >>> 1) & 0x55555555); // reuse input as temporary
			v = (v & 0x33333333) + ((v >>> 2) & 0x33333333); // temp
			return ((v + (v >>> 4) & 0xF0F0F0F) * 0x1010101) >>> 24;
		}
	}
}