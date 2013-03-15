/*
 * Import our classes...
 */
import java.io.*;
import java.util.*;
import org.jibble.*;
import org.jibble.pircbot.*;


/**
 * @author Reaper Eternal
 *
 */
public class Javawolf extends PircBot {
	// Object for sending events
	public static Javawolf wolfbot = null;
	// Channel to enter
	private String channel = null;
	// Login string
	private static String login_str = null;
	// Command character
	public static String cmdchar = "!";
	// Trusted players
	public static List<String> trustedHosts = null;
	// Our game
	private WolfGame game = null;
	
	// logging
	private static final int LOG_CONSOLE = 0;
	private static final int LOG_PRIVMSG = 1;
	private static final int LOG_PUBMSG  = 2;
	private static final int LOG_NOTICE  = 3;
	private static final int LOG_GAME    = 4;
	
	/**
	 * Creates the wolfbot
	 * 
	 * @param server
	 * @param port
	 * @param channel_to_join
	 * @param username
	 * @param nick
	 */
	public Javawolf(String server, int port, String channel_to_join, String username, String nick) {
		// grr, stupid hack
		Javawolf.wolfbot = this;
		// connect
		boolean connected = false;
		while(!connected) {
			this.setName(nick);
			this.setLogin(username);
			try {
				this.connect(server, port);
				connected = true;
				System.out.println("[CONSOLE] : Launching game....");
			} catch(NickAlreadyInUseException e) {
				nick = nick + "_";
				connected = false;
			} catch(IrcException e) {
				
			} catch(IOException e) {
				
			}
			// wait
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {
				
			}
		}
		// log in
		this.sendMessage("NickServ", login_str);
		
		channel = channel_to_join;
	}

	/**
	 * Main entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// initialize variables
		String cfgSrv = null, cfgChan = null, cfgUser = null, cfgNick = null, cfgLine = null, variable, value;
		int cfgPort = 0;
		BufferedReader cfg = null;
		trustedHosts = new ArrayList<String>();
		// Reads and parses the configuration file
		try {
			cfg = new BufferedReader(new FileReader("Javawolf.ini"));
		} catch(FileNotFoundException e) {
			System.err.println("[STARTUP] : Could not load configuration file. Aborting.");
			System.err.println("[STARTUP] : " + e.getMessage());
			System.exit(1);
		}
		try {
			while((cfgLine = cfg.readLine()) != null) {
				if(cfgLine.startsWith("#")) continue; // comments
				if(cfgLine == "") continue; // empty lines
				// Split along the first ':' character
				int charLoc = cfgLine.indexOf(":");
				if(charLoc != -1) {
					// retrieve what is being set
					variable = cfgLine.substring(0, charLoc-1).trim().toLowerCase();
					value = cfgLine.substring(charLoc+1).trim();
					// set variable
					if(variable.compareTo("nick") == 0) {
						// nickname
						cfgNick = value;
					} else if(variable.compareTo("username") == 0) {
						// username
						cfgUser = value;
					} else if(variable.compareTo("login") == 0) {
						// username
						login_str = value;
					} else if(variable.compareTo("server") == 0) {
						// username
						cfgSrv = value;
					} else if(variable.compareTo("port") == 0) {
						// username
						try {
							cfgPort = Integer.parseInt(value);
						} catch(NumberFormatException e) {
							System.err.println("[STARTUP] : Could not parse port: \"" + value + "\"!");
							System.exit(1);
						}
					} else if(variable.compareTo("channel") == 0) {
						// username
						cfgChan = value;
					} else if(variable.compareTo("admin") == 0) {
						// username
						trustedHosts.add(value);
					} else {
						// unknown variable
						System.out.println("[STARTUP] : Unknown variable \"" + variable + "\".");
					}
				}
			}
		} catch(IOException e) {
			System.err.println("[STARTUP] : Error processing configuration file. Aborting.");
			System.exit(1);
		}
		// create the wolfbot
		new Javawolf(cfgSrv, cfgPort, cfgChan, cfgUser, cfgNick);
	}
	
	@Override
	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
		message = message.trim();
		String[] args = message.split(" ");
		String cmd = args[0];
		game.parseCommand(cmd, args, sender, login, hostname);
	}
	
	@Override
	protected void onJoin(String channel, String sender, String login, String hostname) {
		if(sender.contentEquals("Javawolf")) {
			this.sendMessage(channel, "Welcome to javawolf! use " + cmdchar + "join to begin a game.");
			// create the game
			logEvent("Generating game....", LOG_CONSOLE, null);
			game = new WolfGame(channel, "gibson.freenode.net");
		}
	}
	
	@Override
	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		if(message.startsWith(cmdchar)) {
			// now parse the command
			if(game != null) {
				message = message.trim();
				String[] args = message.split(" ");
				String cmd = args[0].substring(1);
				game.parseCommand(cmd, args, sender, login, hostname);
			} else {
				// WTH?
				System.err.println("[GAME STATE ERROR] : Could not pass command. Game set to null!");
			}
		}
	}
	
	@Override
	protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
		if(sourceNick.contains("NickServ")) {
			if(notice.contains("You are now identified")) {
				// authenticated with NickServ; join
				System.out.println("[CONSOLE] : Joining " + channel);
				this.joinChannel(channel);
				this.sendMessage("ChanServ", "OP ##javawolf Javawolf");
			}
		}
	}
	
	/**
	 * Logs events
	 * @param event
	 * @param type
	 * @param who
	 */
	public static void logEvent(String event, int type, String who) {
		if(type == LOG_CONSOLE) {
			// console events
			System.out.println("[CONSOLE] : " + event);
		} else if(type == LOG_GAME) {
			// game events
			System.out.println("### " + who + " ### has " + event + ". ###");
		}
	}
}
