package me.toxiccoke.servercore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import org.bukkit.Bukkit;

import me.toxiccoke.io.StreamDecoder;
import me.toxiccoke.io.StreamEncoder;

public class FriendAPI {

	static LinkedList<Person> people = new LinkedList<Person>();

	static LinkedList<FriendRequest> requests = new LinkedList<FriendRequest>();

	static private boolean changed;

	public static void init() {
		loadFriends();
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Commands.plugin, new Runnable() {
			@Override
			public void run() {
				if (changed) {
					saveFriends();
					changed = false;
				}
			}
		}, 0L, 2400L);//save every 2 minutes if needed
	}

	private FriendAPI() {
	}

	static public LinkedList<String> getFriends(String player) {
		Person p = get(player);
		if (p == null)
			return new LinkedList<String>();
		return p.friends;
	}

	static public void acceptRequest(FriendRequest r) {
		friend(r.confirmer, r.sender);
		requests.remove(r);
		changed = true;
	}

	static public void denyRequest(FriendRequest r) {
		requests.remove(r);
	}

	static public void addRequest(String sender, String confirmer) {
		for (FriendRequest f : requests)
			if (f.sender.equals(sender) && f.confirmer.equals(confirmer))
				return; // already added
			else if (f.confirmer.equals(confirmer)) {
				// You can only have 1 pending friend request
				requests.remove(f);
				break;
			}
		requests.add(new FriendRequest(sender, confirmer));
	}

	static public void friend(String p1, String p2) {
		getAdd(p1).friends.add(p2);
		getAdd(p2).friends.add(p1);
		changed = true;
	}

	static public boolean friends(String p1, String p2) {
		for (Person p : people)
			if (p.name.equals(p1))
				for (String f : p.friends)
					if (f.equals(p2))
						return true;
		return false;
	}

	static public Person get(String person) {
		for (Person p : people)
			if (p.name.equals(person))
				return p;
		return null;
	}

	static private Person getAdd(String person) {
		Person p = get(person);
		if (p == null) {
			p = new Person(person);
			people.add(p);
		}
		return p;

	}

	static public FriendRequest getRequest(String confirmer) {
		for (FriendRequest r : requests)
			if (confirmer.equals(r.confirmer))
				return r;
		return null;
	}

	static public void unfriend(String p1, String p2) {
		Person person1 = get(p1);
		if (person1 != null)
			person1.friends.remove(p2);
		Person person2 = get(p2);
		if (person2 != null)
			person2.friends.remove(p1);
		changed = true;
	}

	static public class FriendRequest {
		String confirmer;
		String sender;

		public FriendRequest(String sender, String confirmer) {
			this.sender = sender;
			this.confirmer = confirmer;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (!(o instanceof FriendRequest))
				return false;
			FriendRequest other = (FriendRequest) o;
			return other.sender.equals(this.sender) && other.confirmer.equals(this.confirmer);
		}
	}

	static public class Person {
		LinkedList<String> friends;
		String name;

		public Person(String name) {
			this.name = name;
			friends = new LinkedList<String>();
		}

		@Override
		public boolean equals(Object o) {
			return (o == null) ? false : o.toString().equals(name);
		}

	}

	private static File getFile() {
		return new File(Commands.plugin.getDataFolder().getParent() + File.separator + "friends.dat");
	}

	static public void loadFriends() {
		people.clear();
		File f = getFile();
		if (!f.exists())
			return;
		try {
			StreamDecoder e = new StreamDecoder(f);
			int n = e.read4();
			int i = 0;
			while (i++ < n) {
				Person p = new Person(e.readStr());
				System.out.println(p.name);
				int fr = e.read4();
				for (int a = 0; a < fr; a++)
					p.friends.add(e.readStr());
				people.add(p);
			}
			e.close();
		} catch (IOException e) {
			System.err.println("Cannot load friends");
			e.printStackTrace();
		}
	}

	static public void saveFriends() {
		try {
			StreamEncoder e = new StreamEncoder(getFile());
			e.write4(people.size());
			for (Person p : people) {
				e.writeStr(p.name);
				e.write4(p.friends.size());
				for (String s : p.friends)
					e.writeStr(s);
			}
			e.close();
		} catch (IOException e) {
			System.err.println("Cannot save friends");
			e.printStackTrace();
		}
	}

}
