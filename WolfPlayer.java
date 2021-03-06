/*
 * Includes
 */
package Javawolf;

/**
 * @author Reaper Eternal
 *
 */
public class WolfPlayer {
	// Nickname
	private String nick = null;
	// Username
	private String user = null;
	// Host
	private String host = null;
	
	// roles
	public boolean[] roles = new boolean[32];
	// role indices
	// primary villager roles
	public static final int ROLE_PRIMARY_LOWBOUND = 0;
	public static final int ROLE_SEER = 0;
	public static final int ROLE_HARLOT = 1;
	public static final int ROLE_DRUNK = 2;
	public static final int ROLE_ANGEL = 3;
	public static final int ROLE_DETECTIVE = 4;
	public static final int ROLE_MEDIUM = 5;
	public static final int ROLE_PRIMARY_HIGHBOUND = 5;
	// secondary roles
	public static final int ROLE_GUNNER = 6;
	public static final int ROLE_CURSED = 7;
	// wolf roles
	public static final int ROLE_WOLF_LOWBOUND = 8;
	public static final int ROLE_WOLF = 8;
	public static final int ROLE_TRAITOR = 9;
	public static final int ROLE_WERECROW = 10;
	public static final int ROLE_SORCERER = 11;
	public static final int ROLE_WOLF_HIGHBOUND = 11;
	// role names
	public static Role[] sz_roles = null;
	// villager roles
	/*public boolean isSeer = false;
	public boolean isHarlot = false;
	public boolean isDrunk = false;
	public boolean isAngel = false;
	public boolean isDetective = false;
	public boolean isMedium = false;
	// secondary roles
	public boolean isGunner = false;
	public boolean isCursed = false;
	// evil roles >:)
	public boolean isWolf = false;
	public boolean isTraitor = false;
	public boolean isWerecrow = false;
	public boolean isSorcerer = false;*/
	// Number of bullets left
	public int numBullets = 0;
	// in tavern?
	public boolean isInTavern = false;
	
	// --- Indices into <WolfGame.java>'s <players> array ---
	// Who he has voted for / killed
	public int voted = -1;
	// Who he has seen
	public int seen = -1;
	// Who he has visited
	public int visited = -1;
	// Who he has guarded
	public int guarded = -1;
	// Who he has id'ed
	public int ided = -1;
	// Who he has observed
	public int observed = -1;
	// Who he has raised
	public int raised = -1;
	// Who he has cursed
	public int cursed = -1;
	// Who he loves
	public int lover = -1;
	
	// Is he even alive?
	public boolean isAlive = false;
	// Can he vote?
	public boolean canVote = true;
	
	// last actions
	private static final int MAX_ACTION_STORE = 8;
	private static final int FLOOD_PROTECT_TIME = 4000; // in ms
	private long[] actiontimes = new long[MAX_ACTION_STORE];
	// Warned of idling?
	public boolean isIdleWarned = false;
	
	/**
	 * Creates the basic player
	 * 
	 * @param newNick
	 * @param newUser
	 * @param newHost
	 */
	public WolfPlayer(String newNick, String newUser, String newHost, long join) {
		// Generate player
		nick = newNick;
		user = newUser;
		host = newHost;
		this.addAction(join);
		// Assign role strings
		if(sz_roles == null) {
			sz_roles = new Role[32];
			sz_roles[ROLE_SEER] = new Role("seer", "seer", true, true, true, false, false);
			sz_roles[ROLE_HARLOT] = new Role("harlot", "harlot", true, true, true, false, false);
			sz_roles[ROLE_DRUNK] = new Role("village drunk", "village drunk", true, true, true, false, false);
			sz_roles[ROLE_ANGEL] = new Role("guardian angel", "guardian angel", true, true, true, false, false);
			sz_roles[ROLE_DETECTIVE] = new Role("detective", "detective", true, true, true, false, false);
			sz_roles[ROLE_MEDIUM] = new Role("medium", "medium", true, true, true, false, false);
			sz_roles[ROLE_GUNNER] = new Role("gunner", null, true, false, true, false, false);
			sz_roles[ROLE_CURSED] = new Role("cursed", "wolf", true, true, false, false, false);
			sz_roles[ROLE_WOLF] = new Role("wolf", "wolf", true, true, true, true, true);
			sz_roles[ROLE_TRAITOR] = new Role("traitor", null, true, false, true, false, true);
			sz_roles[ROLE_WERECROW] = new Role("werecrow", "werecrow", true, true, true, true, true);
			sz_roles[ROLE_SORCERER] = new Role("sorcerer", "sorcerer", true, true, true, false, true);
		}
	}
	
	// Is the player a match to the given player?
	public boolean identmatch(String mNick, String mUser, String mHost) {
		if((mNick.compareTo(nick) == 0) && (mUser.compareTo(user) == 0) && (mHost.compareTo(host) == 0)) {
			return true;
		} else {
			return false; // nope, not same guy
		}
	}
	
	// Does a player begin with the given nick
	public boolean nickmatch(String mNick) {
		if(mNick.compareTo(nick) == 0) {
			return true;
		} else {
			return false; // nope, not same guy
		}
	}
	
	// gets the nick
	public String getNick() {
		return nick;
	}
	
	// sets the nick
	public void setNick(String nick) {
		this.nick = nick;
	}
	
	// gets the nick
	public String getUser() {
		return user;
	}
	
	// gets the nick
	public String getHost() {
		return host;
	}
	
	/**
	 * Used to show all the roles on death.
	 * 
	 * @return
	 */
	public String getEndGameDisplayedRole() {
		// Enumerate roles
		String[] roleList = new String[32];
		int count = 0;
		int m = 0;
		while(m <= ROLE_WOLF_HIGHBOUND) {
			if(roles[m]) {
				roleList[count] = sz_roles[m].szName;
				count++;
			}
			m++;
		}
		// parse into string
		return parseEnumeratedRoles(roleList, count);
	}
	
	/**
	 * Gets the role to display to players on death.
	 * 
	 * @return
	 */
	public String getDeathDisplayedRole() {
		// Enumerate roles
		String[] roleList = new String[32];
		int count = 0;
		int m = 0;
		while(m <= ROLE_WOLF_HIGHBOUND) {
			if(roles[m] && sz_roles[m].shownOnDeath) {
				roleList[count] = sz_roles[m].szName;
				count++;
			}
			m++;
		}
		// parse into string
		return parseEnumeratedRoles(roleList, count);
	}
	
	/**
	 * Gets the role as ided by detectives.
	 * 
	 * @return
	 */
	public String getIDedRole() {
		// Enumerate roles
		String[] roleList = new String[32];
		int count = 0;
		int m = 0;
		while(m <= ROLE_WOLF_HIGHBOUND) {
			if(roles[m] && sz_roles[m].shownOnId) {
				roleList[count] = sz_roles[m].szName;
				count++;
			}
			m++;
		}
		// parse into string
		return parseEnumeratedRoles(roleList, count);
	}
	
	/**
	 * Gets the role as seen by seers.
	 * 
	 * @return
	 */
	public String getSeenRole() {
		// Enumerate roles
		String[] roleList = new String[32];
		int count = 0;
		int m = 0;
		while(m <= ROLE_WOLF_HIGHBOUND) {
			if(roles[m] && sz_roles[m].shownOnSeen) {
				roleList[count] = sz_roles[m].szName;
				count++;
				// "Wolf" always blocks vision of all other roles
				if(sz_roles[m].szName.contentEquals("wolf")) return "\u0002wolf\u0002";
			}
			m++;
		}
		// parse into string
		return parseEnumeratedRoles(roleList, count);
	}
	
	/**
	 * Parses an enumerated list of roles into a displayable string.
	 * 
	 * @param roleList
	 * @param count
	 * @return
	 */
	private String parseEnumeratedRoles(String[] roleList, int count) {
		if(count == 0) return "\u0002villager\u0002";
		String str = "";
		int m = 0;
		while(m < count) {
			str = str + "\u0002" + roleList[m] + "\u0002";
			if(m == count - 2) {
				str = str + ", and ";
			} else if(m < count - 2) {
				str = str + ", ";
			}
			m++;
		}
		
		// return the string
		return str;
	}
	
	// counts all the roles
	public int countAllRoles() {
		return this.countRoles(ROLE_PRIMARY_LOWBOUND, ROLE_WOLF_HIGHBOUND);
	}
	
	// counts the main village roles
	public int countMainRoles() {
		// Return wolf + villager count.
		return this.countVillageRoles() + this.countWolfRoles();
	}
	
	// counts the villager roles
	public int countVillageRoles() {
		return countRoles(ROLE_PRIMARY_LOWBOUND, ROLE_PRIMARY_HIGHBOUND);
	}
	
	// counts the wolf roles
	public int countWolfRoles() {
		return countRoles(ROLE_WOLF_LOWBOUND, ROLE_WOLF_HIGHBOUND);
	}
	
	/**
	 * Counts the roles between the given bounds (inclusive count).
	 * 
	 * @param low
	 * @param high
	 * @return
	 */
	private int countRoles(int low, int high) {
		int m = low;
		int count = 0;
		while(m <= high) {
			if(roles[m]) count++;
			m++;
		}
		// return it
		return count;
	}
	
	// resets the actions taken by the player
	public void resetActions() {
		voted = -1;
		seen = -1;
		visited = -1;
		guarded = -1;
		ided = -1;
		observed = -1;
		raised = -1;
		cursed = -1;
		canVote = true;
	}
	
	/**
	 * Adds an action at the given time.
	 * 
	 * @param time
	 * @return
	 */
	public boolean addAction(long time) {
		int m = 0;
		while(m <= MAX_ACTION_STORE-2) {
			actiontimes[m] = actiontimes[m+1];
			m++;
		}
		actiontimes[MAX_ACTION_STORE-1] = time;
		// Obviously, an action has just occurred.
		isIdleWarned = false;
		// Return whether flooding is occurring.
		return (actiontimes[MAX_ACTION_STORE-1] - actiontimes[0]) < FLOOD_PROTECT_TIME;
	}
	
	/**
	 * Gets the time of the last action of this player.
	 * 
	 * @return
	 */
	public long getLastAction() {
		return actiontimes[MAX_ACTION_STORE-1];
	}
}

