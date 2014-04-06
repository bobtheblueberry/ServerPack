package me.toxiccoke.servercore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import me.toxiccoke.io.StreamDecoder;
import me.toxiccoke.io.StreamEncoder;
import me.toxiccoke.servercore.FriendAPI.Person.Value;

import org.bukkit.Bukkit;

public class FriendAPI {

	static LinkedList<Person>			people		= new LinkedList<Person>();

	static LinkedList<FriendRequest>	requests	= new LinkedList<FriendRequest>();

	static private boolean				changed;

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
		}, 0L, 600L);// save every 30 seconds if needed
	}

	private FriendAPI() {}

	static public LinkedList<String> getFriends(String player) {
		Person p = get(player);
		if (p == null) return new LinkedList<String>();
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
			if (f.sender.equals(sender) && f.confirmer.equals(confirmer)) return; // already
																					// added
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
			if (p.name.equals(p1)) for (String f : p.friends)
				if (f.equals(p2)) return true;
		return false;
	}

	static public Person get(String person) {
		for (Person p : people)
			if (p.name.equals(person)) return p;
		return null;
	}

	static public Person getAdd(String person) {
		Person p = get(person);
		if (p == null) {
			p = new Person(person);
			people.add(p);
		}
		return p;

	}

	static public FriendRequest getRequest(String confirmer) {
		for (FriendRequest r : requests)
			if (confirmer.equals(r.confirmer)) return r;
		return null;
	}

	static public void unfriend(String p1, String p2) {
		Person person1 = get(p1);
		if (person1 != null) person1.friends.remove(p2);
		Person person2 = get(p2);
		if (person2 != null) person2.friends.remove(p1);
		changed = true;
	}

	static public class FriendRequest {
		String	confirmer;
		String	sender;

		public FriendRequest(String sender, String confirmer) {
			this.sender = sender;
			this.confirmer = confirmer;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (!(o instanceof FriendRequest)) return false;
			FriendRequest other = (FriendRequest) o;
			return other.sender.equals(this.sender) && other.confirmer.equals(this.confirmer);
		}
	}

	static public class Person {
		private LinkedList<String>		friends;
		private String					name;
		/**
		 * if true then requests to add this person as a friend should not be
		 * allowed
		 */
		private boolean					requestsDisabled;
		private HashMap<String, Value>	values;

		public Value getVal(String key) {
			return Value.copy(values.get(key));
		}

		public void setVal(String key, Value value) {
			values.put(key, value);
			changed = true;
		}

		public Person(String name) {
			this.name = name;
			friends = new LinkedList<String>();
			values = new HashMap<String, Value>();
		}

		public void setRequestsDisabled(boolean disabled) {
			if (disabled == requestsDisabled) return;
			requestsDisabled = disabled;
			changed = true;
		}

		public boolean isRequestsDisabled() {
			return requestsDisabled;
		}

		public String[] getFriends() {
			return friends.toArray(new String[friends.size()]);
		}

		@Override
		public boolean equals(Object o) {
			return (o == null) ? false : o.toString().equals(name);
		}

		public static class Value {
			public String		sVal;
			public int			iVal;
			public boolean		bVal;
			public double		dVal;
			public DATA_TYPE	type;

			public static enum DATA_TYPE {
				STRING, INTEGER, DOUBLE, BOOLEAN
			};

			private Value() {}

			public Value(String val) {
				type = DATA_TYPE.STRING;
				this.sVal = val;
			}

			public Value(int val) {
				type = DATA_TYPE.INTEGER;
				this.iVal = val;
			}

			public Value(double val) {
				type = DATA_TYPE.DOUBLE;
				this.dVal = val;
			}

			public Value(boolean val) {
				type = DATA_TYPE.BOOLEAN;
				this.bVal = val;
			}

			public boolean equals(Object o) {
				if (o == null || (!(o instanceof Value))) return false;
				Value v = (Value) o;
				return v.type == this.type && v.bVal == this.bVal && v.dVal == this.dVal && v.iVal == this.iVal
						&& ((v.sVal != null) ? v.sVal.equals(sVal) : true);
			}

			private static Value copy(Value or) {
				if (or == null)
					return null;
				Value v = new Value();
				v.bVal = or.bVal;
				v.dVal = or.dVal;
				v.iVal = or.iVal;
				v.sVal = or.sVal;
				v.type = or.type;
				return v;
			}
		}

	}

	private static File getFile() {
		return new File(Commands.plugin.getDataFolder().getParent() + File.separator + "friends.dat");
	}

	static public void loadFriends() {
		people.clear();
		File f = getFile();
		if (!f.exists()) return;
		try {
			StreamDecoder e = new StreamDecoder(f);
			int version = e.read();
			int n = e.read4();
			int i = 0;
			while (i++ < n) {
				Person p = new Person(e.readStr());
				p.requestsDisabled = e.readBool();
				int fr = e.read4();
				for (int a = 0; a < fr; a++)
					p.friends.add(e.readStr());
				if (version > 1) {
					int total = e.read4();
					for (int ii = 0; ii < total; ii++) {
						String key = e.readStr();
						int type = e.read();
						Value v = null;
						switch (type) {
						case 0: {
							v = new Value(e.read4());
							break;
						}
						case 1: {
							v = new Value(e.readBool());
							break;
						}
						case 2: {
							v = new Value(e.readD());
							break;
						}
						case 3: {
							v = new Value(e.readStr());
							break;
						}
						}
						p.values.put(key, v);
					}
				}
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
			e.write(2);// Version 2
			e.write4(people.size());
			for (Person p : people) {
				e.writeStr(p.name);
				e.writeBool(p.requestsDisabled);
				e.write4(p.friends.size());
				for (String s : p.friends)
					e.writeStr(s);
				e.write4(p.values.size());
				Iterator<Entry<String, Value>> it = p.values.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Value> ent = it.next();
					e.writeStr(ent.getKey());
					Value v = ent.getValue();
					switch (v.type) {
					case INTEGER: {
						e.write(0);
						e.write4(v.iVal);
						break;
					}
					case BOOLEAN: {
						e.write(1);
						e.writeBool(v.bVal);
						break;
					}
					case DOUBLE: {
						e.write(2);
						e.writeD(v.dVal);
						break;
					}
					case STRING: {
						e.write(3);
						e.writeStr(v.sVal);
						break;
					}
					}
				}
			}
			e.close();
		} catch (IOException e) {
			System.err.println("Cannot save friends");
			e.printStackTrace();
		}
	}

}
