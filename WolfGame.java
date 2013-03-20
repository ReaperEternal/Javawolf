/*
 * Import classes
 */
package Javawolf;

import java.util.Random;
import java.lang.*;
import java.util.*;

/**
 * @author Reaper Eternal
 *
 */
public class WolfGame {
	// Channel and server associated with this instance of the game
	private String assocChannel = null;
	//private String assocServer = null;
	// Player list
	private WolfPlayer[] players = null;
	// Config list
	private static WolfConfig[] configs = new WolfConfig[64];;
	private static int nCfgs = 0;
	// Votelist
	private int[] votes = null;
	// Maximum player count
	public static final int MAX_WOLFPLAYERS = 32;
	// Player count
	private int playernum = 0;
	
	// Whether game is in progress
	private boolean isRunning = false;
	// Whether it is nighttime
	private boolean isNight = false;
	
	/* Guardian angel constants */
	// Chance the GA will die from guarding a wolf.
	private double dieguardingwolfpct = .50;
	// Chance the GA will die if guarding a victim.
	private double dieguardingvictimpct = .25;
	// Chance the GA will become a wolf if guarding the victim.
	private double infectguardingvictimpct = .10;
	// Chance the victim will die if guarded.
	private double guardedvictimdiepct = .25;
	// Chance the victim will become a wolf if guarded.
	private double guardedvictiminfectpct = .10;
	
	/* Detective constants */
	// Chance the detective will drop his papers.
	private double detectivefumblepct = .4;
	
	/* Gunner constants */
	// Chance the gunner will miss
	private double gunnermisspct = .20;
	// Chance the gunner will explode
	private double gunnerexplodepct = .14;
	// Chance gunner will kill the target
	private double gunnerheadshotpct = .20;
	// Chance wolf will find a gun when killing the gunner
	private double wolffindgunpct = .33;
	
	/* Sorcerer constants */
	// Chance victim will know he has been cursed
	private double sorcerervictimnoticecursepct = .33;
	
	// Time of start of day/night
	private long starttime = 0;
	// <Timer> to end day/night if nobody acts
	private Timer timer = null;
	
	/**
	 * Creates the game
	 * 
	 * @param chan : Channel associated with this game
	 */
	public WolfGame(String chan, String server) {
		// sets the associated channel and server
		assocChannel = chan;
		//assocServer = server;
		// creates the lists
		players = new WolfPlayer[MAX_WOLFPLAYERS];
		votes = new int[MAX_WOLFPLAYERS];
	}
	
	/**
	 * Parses commands to the game
	 * 
	 * @param cmd
	 */
	public void parseCommand(String cmd, String[] args, String nick, String user, String host) {
		// lowercase
		cmd = cmd.toLowerCase();
		
		if(cmd.compareTo("join") == 0) {
			// Joins the game
			join(nick, user, host);
		} else if((cmd.compareTo("lynch") == 0) || (cmd.compareTo("vote") == 0)) {
			// Used for lynching a player
			if(args == null) return;
			if(args.length < 1) return;
			lynch(args[1], nick, user, host);
		} else if(cmd.compareTo("retract") == 0) {
			// Retracts your vote
			lynch(null, nick, user, host);
		} else if((cmd.compareTo("leave") == 0) || (cmd.compareTo("quit") == 0)) {
			// Leaves the game
			leave(nick, user, host);
		} else if(cmd.compareTo("start") == 0) {
			// Starts the game
			startgame(nick, user, host);
		} else if(cmd.compareTo("kill") == 0) {
			// Used by wolves to kill players
			if(args == null) return;
			if(args.length < 1) return;
			kill(args[1], nick, user, host);
		} else if(cmd.compareTo("see") == 0) {
			// Used by seers to see players
			if(args == null) return;
			if(args.length < 1) return;
			see(args[1], nick, user, host);
		} else if(cmd.compareTo("visit") == 0) {
			// Used by seers to see players
			if(args == null) return;
			if(args.length < 1) return;
			visit(args[1], nick, user, host);
		} else if(cmd.compareTo("shoot") == 0) {
			// Used by gunners to shoot players
			if(args == null) return;
			if(args.length < 1) return;
			shoot(args[1], nick, user, host);
		} else if(cmd.compareTo("id") == 0) {
			// Used by detectives to id players
			if(args == null) return;
			if(args.length < 1) return;
			id(args[1], nick, user, host);
		} else if(cmd.compareTo("observe") == 0) {
			// Used by werecrows to observe players
			if(args == null) return;
			if(args.length < 1) return;
			observe(args[1], nick, user, host);
		} else if(cmd.compareTo("guard") == 0) {
			// Used by guardian angels to guard players
			if(args == null) return;
			if(args.length < 1) return;
			guard(args[1], nick, user, host);
		} else if(cmd.compareTo("raise") == 0) {
			// Used by mediums to raise players
			if(args == null) return;
			if(args.length < 1) return;
			raise(args[1], nick, user, host);
		} else if(cmd.compareTo("curse") == 0) {
			// Used by sorcerers to curse players
			if(args == null) return;
			if(args.length < 1) return;
			curse(args[1], nick, user, host);
		} else if(cmd.compareTo("fendgame") == 0) {
			// Mod command: Forces the game to end.
			if(admincheck(host)) {
				fendgame(nick, user, host);
			} else {
				privmsg(nick, "You are not a moderator.");
			}
		} else if(cmd.compareTo("fquit") == 0) {
			// Mod command: Forces the game to end.
			if(admincheck(host)) {
				fquit(nick, user, host);
			} else {
				privmsg(nick, "You are not an admin.");
			}
		} else if(cmd.compareTo("op") == 0) {
			// Admin command: Ops you.
			if(admincheck(host)) {
				op(nick, user, host);
			} else {
				privmsg(nick, "You are not an admin.");
			}
		} else if(cmd.compareTo("deop") == 0) {
			// Admin command: Deops a player.
			if(args == null) return;
			if(args.length < 1) return;
			if(admincheck(host)) {
				deop(args[1], nick, user, host);
			} else {
				privmsg(nick, "You are not an admin.");
			}
		} else if(cmd.compareTo("cmdchar") == 0) {
			// Admin command: Changes command character prefix.
			if(args == null) return;
			if(args.length < 1) return;
			if(admincheck(host)) {
				change_cmdchar(args[1], nick, user, host);
			} else {
				privmsg(nick, "You are not an admin.");
			}
		} else if(cmd.compareTo("stats") == 0) {
			// Who is left in the game?
			stats(nick, user, host);
		} else {
			// Allows the wolf team to chat privately
			int plidx = getPlayer(nick, user, host);
			if(plidx != -1) {
				// Don't let dead people chat
				if(players[plidx].isAlive) {
					if(players[plidx].isWolf || players[plidx].isTraitor || players[plidx].isWerecrow) {
						String s = "";
						int m = 0;
						while(m < args.length) {
							s = s + " " + args[m]; // concatenate
							m++;
						}
						wolfmsg(nick + " says: \"" + s + "\"");
					}
				}
			}
		}
	}
	
	/**
	 * Curses a villager
	 * 
	 * @param who
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void curse(String who, String nick, String user, String host) {
		if(who == null) return; // Sanity check
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the CURSE command");
		// Is the game even running?
		if(!isRunning) {
			privmsg(nick, "No game is running.");
			return;
		}
		
		// Is the player even playing?
		int plidx = getPlayer(nick, user, host);
		if(plidx == -1) {
			privmsg(nick, "You aren't even playing.");
			return;
		}
		// Is the player a sorcerer?
		if(!players[plidx].isSorcerer) {
			privmsg(nick, "Only sorcerers can curse other players.");
			return;
		}
		// Is it night?
		if(!isNight) {
			privmsg(nick, "You may only curse people at night.");
			return;
		}
		// Has the player not yet cursed someone?
		if(players[plidx].cursed >= 0) {
			privmsg(nick, "You have already cursed someone this night.");
			return;
		}
		// Get the target
		int targidx = getPlayer(who);
		// Is the target playing?
		if(targidx == -1) {
			privmsg(nick, who + " is not playing.");
			return;
		}
		// Is the target alive? 
		if(!players[targidx].isAlive) {
			privmsg(nick, players[targidx].getNick() + " is already dead.");
			return;
		}
		// Is the target a wolf?
		if(players[targidx].isWolf || players[targidx].isWerecrow) {
			privmsg(nick, "You don't see the point in cursing your wolf friends.");
			return;
		}
		// the curse falls
		players[plidx].cursed = targidx;
		players[targidx].isCursed = true;
		// let you know
		privmsg(nick, "Your fingers move nimbly as you cast the dark enchantment. \u0002" +
			players[targidx].getNick() + "\u0002 has become cursed!");
		// chance of the target knowing his cursed status
		Random rand = new Random();
		if(rand.nextDouble() < sorcerervictimnoticecursepct) {
			privmsg(players[targidx].getNick(), "You feel the mark of Cain fall upon you....");
		}
		
		// checks for the end of the night
		if(checkEndNight()) endNight();
	}
	
	/**
	 * Used by mediums to raise dead players.
	 * 
	 * @param who
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void raise(String who, String nick, String user, String host) {
		if(who == null) return; // Sanity check
		// Is the game even running?
		if(!isRunning) {
			privmsg(nick, "No game is running.");
			return;
		}
		
		// Is the player even playing?
		int plidx = getPlayer(nick, user, host);
		if(plidx == -1) {
			privmsg(nick, "You aren't even playing.");
			return;
		}
		// Is the player a medium?
		if(!players[plidx].isMedium) {
			privmsg(nick, "Only mediums can raise other players.");
			return;
		}
		// Is it day?
		if(isNight) {
			privmsg(nick, "You may only raise players during the day.");
			return;
		}
		// Has the player not yet raised anyone?
		if(players[plidx].raised >= 0) {
			privmsg(nick, "You have already raised someone today.");
			return;
		}
		// Gets the target
		int targidx = getPlayer(who);
		// Is the target playing?
		if(targidx == -1) {
			privmsg(nick, who + " is not playing.");
			return;
		}
		// Is the target dead? 
		if(players[targidx].isAlive) {
			privmsg(nick, players[targidx].getNick() + " is still alive and can be consulted normally.");
			return;
		}
		// raise him
		chanmsg("\u0002" + players[plidx].getNick() + "\u0002 has cast a seance! The spirit of \u0002" +
			players[targidx].getNick() + "\u0002 is raised for the day.");
		voice(players[targidx].getNick()); // Make him able to chat again.
		players[plidx].raised = targidx;
		
		// checks for the end of the night
		if(checkEndNight()) endNight();
	}
	
	/**
	 * Guards a player from attacks by the wolves.
	 * 
	 * @param who
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void guard(String who, String nick, String user, String host) {
		if(who == null) return; // Sanity check
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the GUARD command");
		// Is the game even running?
		if(!isRunning) {
			privmsg(nick, "No game is running.");
			return;
		}
		
		// Is the player even playing?
		int plidx = getPlayer(nick, user, host);
		if(plidx == -1) {
			privmsg(nick, "You aren't even playing.");
			return;
		}
		// Is the player a guardian angel?
		if(!players[plidx].isAngel) {
			privmsg(nick, "Only guardian angels can guard other players.");
			return;
		}
		// Is it night?
		if(!isNight) {
			privmsg(nick, "You may only guard during the night.");
			return;
		}
		// Has the player not yet guarded?
		if(players[plidx].guarded >= 0) {
			privmsg(nick, "You are already guarding \u0002" + players[players[plidx].guarded].getNick() + "\u0002 tonight.");
			return;
		}
		int targidx = getPlayer(who);
		// Is the target playing?
		if(targidx == -1) {
			privmsg(nick, who + " is not playing.");
			return;
		}
		// Is the target alive? 
		if(!players[targidx].isAlive) {
			privmsg(nick, players[targidx].getNick() + " is already dead.");
			return;
		}
		// Guards the target.
		privmsg(nick, "You are guarding \u0002" + players[targidx].getNick() + "\u0002 tonight. Farewell.");
		privmsg(players[targidx].getNick(), "You can sleep well tonight, for a guardian angel is protecting you.");
		players[plidx].guarded = targidx;
		
		// checks for the end of the night
		if(checkEndNight()) endNight();
	}
	
	/**
	 * Observes a player to determine whether or not he leaves the house.
	 * 
	 * @param who
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void observe(String who, String nick, String user, String host) {
		if(who == null) return; // Sanity check
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the OBSERVE command");
		// Is the game even running?
		if(!isRunning) {
			privmsg(nick, "No game is running.");
			return;
		}
		
		// Is the player even playing?
		int plidx = getPlayer(nick, user, host);
		if(plidx == -1) {
			privmsg(nick, "You aren't even playing.");
			return;
		}
		// Is the player a werecrow?
		if(!players[plidx].isWerecrow) {
			privmsg(nick, "Only werecrows can observe other players.");
			return;
		}
		// Is it night?
		if(!isNight) {
			privmsg(nick, "You may only observe during the night.");
			return;
		}
		// Has the player not yet observed?
		if(players[plidx].observed >= 0) {
			privmsg(nick, "You are already observing \u0002" + players[players[plidx].observed].getNick() + "\u0002 tonight.");
			return;
		}
		// Get target
		int targidx = getPlayer(who);
		// Is the target playing?
		if(targidx == -1) {
			privmsg(nick, who + " is not playing.");
			return;
		}
		// Is the target alive? 
		if(!players[targidx].isAlive) {
			privmsg(nick, players[targidx].getNick() + " is already dead.");
			return;
		}
		// Observe the targetted player.
		privmsg(nick, "You change into a large black crow and fly off to see whether \u0002" + players[targidx].getNick() +
			"\u0002 remains in bed all night.");
		players[plidx].observed = targidx;
		
		// checks for the end of the night
		if(checkEndNight()) endNight();
	}
	
	/**
	 * Identifies a player's role.
	 * 
	 * @param who
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void id(String who, String nick, String user, String host) {
		if(who == null) return; // Sanity check
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the ID command");
		// Is the game even running?
		if(!isRunning) {
			privmsg(nick, "No game is running.");
			return;
		}
		
		// Is the player even playing?
		int plidx = getPlayer(nick, user, host);
		if(plidx == -1) {
			privmsg(nick, "You aren't even playing.");
			return;
		}
		// Is the player a detective?
		if(!players[plidx].isDetective) {
			privmsg(nick, "Only detectives can id other players.");
		}
		// Is it day?
		if(isNight) {
			privmsg(nick, "You may only identify players during the day.");
			return;
		}
		// Has the player not yet ided anyone?
		if(players[plidx].ided >= 0) {
			privmsg(nick, "You have already identified someone today.");
			return;
		}
		int targidx = getPlayer(who);
		// Is the target playing?
		if(targidx == -1) {
			privmsg(nick, who + " is not playing.");
			return;
		}
		// Is the target alive? 
		if(!players[targidx].isAlive) {
			privmsg(nick, players[targidx].getNick() + " is already dead.");
			return;
		}
		// id his role
		privmsg(nick, "The results of your investigation return: \u0002" + players[targidx].getNick() +
			"\u0002 is a \u0002" + players[targidx].getDisplayedRole() + "\u0002!");
		players[plidx].ided = targidx;
		// drop papers?
		Random rand = new Random();
		if(rand.nextDouble() < detectivefumblepct) {
			// notify the wolves of the detective
			wolfmsg("\u0002" + players[plidx].getNick() + "\u0002 drops a paper revealing s/he is a \u0002detective\u0002!");
		}
	}
	
	/**
	 * Shoots a player.
	 * 
	 * @param who
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void shoot(String who, String nick, String user, String host) {
		if(who == null) return; // Sanity check
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the SHOOT command");
		// Is the game even running?
		if(!isRunning) {
			privmsg(nick, "No game is running.");
			return;
		}
		
		// Is the player even playing?
		int plidx = getPlayer(nick, user, host);
		if(plidx == -1) {
			privmsg(nick, "You aren't even playing.");
			return;
		}
		// Is the player a seer?
		if(!players[plidx].isGunner) {
			privmsg(nick, "You don't have a gun.");
			return;
		}
		// Is it day?
		if(isNight) {
			privmsg(nick, "You can only shoot during the day.");
			return;
		}
		int targidx = getPlayer(who);
		// Is the target playing?
		if(targidx == -1) {
			privmsg(nick, who + " is not playing.");
			return;
		} else if(targidx == plidx) {
			// lol, don't suicide
			privmsg(nick, "You're holding it the wrong way!");
			return;
		}
		// Is the target alive? 
		if(!players[targidx].isAlive) {
			privmsg(nick, players[targidx].getNick() + " is already dead.");
			return;
		}
		// Do you have any bullets left?
		if(players[plidx].numBullets == 0) {
			privmsg(nick, "You have no more bullets remaining.");
			return;
		}
		
		// He can now fire.
		chanmsg("\u0002" + players[plidx].getNick() + "\u0002 raises his/her gun and fires at \u0002" +
			players[targidx].getNick() + "\u0002!");
		players[plidx].numBullets--; // Uses a bullet.
		// Does he explode?
		Random rand = new Random();
		if(rand.nextDouble() < gunnerexplodepct) {
			// Boom! You lose.
			chanmsg("\u0002" + players[plidx].getNick() + "\u0002 should have cleaned his/her gun better. " +
				"The gun explodes and kills him/her!");
			chanmsg("It appears s/he was a \u0002" + players[plidx].getDisplayedRole() + "\u0002.");
			players[plidx].isAlive = false;
			devoice(nick);
			if(checkForEnding()) return; // Oops. Game over. Shouldn't have blown up.
			else checkForLynching(); // Does a lynching occur now?
			return;
		}
		
		// Wolf teammates will deliberately miss other wolves.
		if((players[plidx].isWolf || players[plidx].isTraitor || players[plidx].isWerecrow) &&
			(players[targidx].isWolf || players[targidx].isWerecrow)) {
			chanmsg("\u0002" + players[plidx].getNick() + "\u0002 is a lousy shooter! S/he missed!");
			return;
		}
		// Missed target? (Drunks have 3x the miss chance.)
		if(rand.nextDouble() < (players[plidx].isDrunk ? gunnermisspct*3 : gunnermisspct)) {
			chanmsg("\u0002" + players[plidx].getNick() + "\u0002 is a lousy shooter! S/he missed!");
			return;
		}
		// We hit the target.
		// Is the shot person a wolf?
		if(players[targidx].isWolf || players[targidx].isWerecrow) {
			chanmsg("\u0002" + players[targidx].getNick() + "\u0002 is a \u0002" + 
				players[targidx].getDisplayedRole() + "\u0002 and is dying from the silver bullet!");
			devoice(players[targidx].getNick());
			players[targidx].isAlive = false;
		} else {
			// Was it a headshot?
			if(rand.nextDouble() < gunnerheadshotpct) {
				players[targidx].isAlive = false;
				chanmsg("\u0002" + players[targidx].getNick() + "\u0002 was not a wolf but was accidentally " +
					"fatally injured! It appears that s/he was a \u0002" + players[targidx].getDisplayedRole() +
					"\u0002.");
				devoice(players[targidx].getNick());
			} else {
				// Injured a villager, but did not kill him.
				players[targidx].canVote = false;
				chanmsg("\u0002" + players[targidx].getNick() + "\u0002 was a villager and is injured by the " +
					"silver bullet. S/he will be resting in bed for the rest of the day but will recover fully.");
			}
		}
		// Does the game end as a result of the shooting?
		if(checkForEnding()) return;
		else checkForLynching(); // Does a lynching occur now?
	}
	
	/**
	 * Prints out the current stats of the game.
	 * 
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void stats(String nick, String user, String host) {
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the STATS command");
		
		// Is the player even playing?
		int plidx = getPlayer(nick, user, host);
		if(plidx == -1) {
			privmsg(nick, "You aren't even playing.");
			return;
		}
		// Is the game even running? If not, just give player count and return.
		if(!isRunning) {
			chanmsg("There are \u0002" + playernum + "\u0002 unknown players in the game right now.");
			return;
		}
		// Is the player alive?
		if(!players[plidx].isAlive) {
			privmsg(nick, "You have died already and thus cannot use the stats command.");
			return;
		}
		// counts
		int seercount = 0;
		int drunkcount = 0;
		int angelcount = 0;
		int harlotcount = 0;
		int detectivecount = 0;
		int gunnercount = 0;
		int wolfcount = 0;
		int traitorcount = 0;
		int werecrowcount = 0;
		int sorcerercount = 0;
		int livingcount = 0; // total living players
		// count them
		int m = 0;
		while(m < playernum) {
			if(players[m].isAlive) {
				if(players[m].isSeer) seercount++;
				if(players[m].isDrunk) drunkcount++;
				if(players[m].isAngel) angelcount++;
				if(players[m].isHarlot) harlotcount++;
				if(players[m].isDetective) detectivecount++;
				if(players[m].isGunner) gunnercount++;
				if(players[m].isWolf) wolfcount++;
				if(players[m].isTraitor) traitorcount++;
				if(players[m].isWerecrow) werecrowcount++;
				if(players[m].isSorcerer) sorcerercount++;
				livingcount++;
			}
			m++;
		}
		// display results
		String str = "There are \u0002" + livingcount + " villagers\u0002 remaining.";
		// wolves
		if(wolfcount == 1) str = str + " There is \u0002a wolf\u0002.";
		else if(wolfcount > 1) str = str + " There are \u0002" + wolfcount + " wolves\u0002.";
		if(traitorcount == 1) str = str + " There is \u0002a traitor\u0002.";
		else if(traitorcount > 1) str = str + " There are \u0002" + traitorcount + " traitors\u0002.";
		if(werecrowcount == 1) str = str + " There is \u0002a werecrow\u0002.";
		else if(werecrowcount > 1) str = str + " There are \u0002" + werecrowcount + " werecrows\u0002.";
		if(sorcerercount == 1) str = str + " There is \u0002a sorcerer\u0002.";
		else if(sorcerercount > 1) str = str + " There are \u0002" + sorcerercount + " sorcerers\u0002.";
		// villagers
		if(seercount == 1) str = str + " There is \u0002a seer\u0002.";
		else if(seercount > 1) str = str + " There are \u0002" + seercount + " seers\u0002.";
		if(drunkcount == 1) str = str + " There is \u0002a village drunk\u0002.";
		else if(drunkcount > 1) str = str + " There are \u0002" + drunkcount + " village drunks\u0002.";
		if(harlotcount == 1) str = str + " There is \u0002a  harlot\u0002.";
		else if(harlotcount > 1) str = str + " There are \u0002" + harlotcount + " harlots\u0002.";
		if(angelcount == 1) str = str + " There is \u0002a guardian angel\u0002.";
		else if(angelcount > 1) str = str + " There are \u0002" + angelcount + " guardian angels\u0002.";
		if(detectivecount == 1) str = str + " There is \u0002a detective\u0002.";
		else if(detectivecount > 1) str = str + " There are \u0002" + detectivecount + " detectives\u0002.";
		if(gunnercount == 1) str = str + " There is \u0002a gunner\u0002.";
		else if(gunnercount > 1) str = str + " There are \u0002" + gunnercount + " gunners\u0002.";
		// send to channel
		chanmsg(str);
		
		// checks for the end of the night
		if(checkEndNight()) endNight();
	}
	
	/**
	 * Visits a player
	 * 
	 * @param who
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void visit(String who, String nick, String user, String host) {
		if(who == null) return; // Sanity check
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the VISIT command");
		// Is the game even running?
		if(!isRunning) {
			privmsg(nick, "No game is running.");
			return;
		}
		
		// Is the player even playing?
		int plidx = getPlayer(nick, user, host);
		if(plidx == -1) {
			privmsg(nick, "You aren't even playing.");
			return;
		}
		// Is the player a harlot?
		if(!players[plidx].isHarlot) {
			privmsg(nick, "Only harlots can visit other players.");
			return;
		}
		// Is it night?
		if(!isNight) {
			privmsg(nick, "You may only visit other players during the night.");
			return;
		}
		// Has the player not yet visited?
		if(players[plidx].visited >= 0) {
			privmsg(nick, "You have already visited " + players[players[plidx].visited].getNick() + " tonight.");
			return;
		}
		// get target
		int targidx = getPlayer(who);
		// Is the target playing?
		if(targidx == -1) {
			privmsg(nick, who + " is not playing.");
			return;
		}
		// Is the target alive? 
		if(!players[targidx].isAlive) {
			privmsg(nick, "Eww! " + players[targidx].getNick() + " is already dead.");
		}
		// Visiting yourself?
		if(targidx == plidx) {
			// Notify harlot
			privmsg(nick, "You decide to stay home for the night.");
			players[plidx].visited = plidx;
		} else {
			// Notify both players
			players[plidx].visited = targidx;
			privmsg(players[plidx].getNick(), "You are spending the night with \u0002" + players[targidx].getNick() + 
				"\u0002. Have a good time!");
			privmsg(players[targidx].getNick(), "\u0002" + players[plidx].getNick() + "\u0002, a \u0002harlot\u0002, " +
				"has come to spend the night with you. Have a good time!");
		}
		
		// checks for the end of the night
		if(checkEndNight()) endNight();
	}
	
	/**
	 * Sees the player.
	 * 
	 * @param who
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void see(String who, String nick, String user, String host) {
		if(who == null) return; // Sanity check
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the SEE command");
		// Is the game even running?
		if(!isRunning) {
			privmsg(nick, "No game is running.");
			return;
		}
		
		// Is the player even playing?
		int plidx = getPlayer(nick, user, host);
		if(plidx == -1) {
			privmsg(nick, "You aren't even playing.");
			return;
		}
		// Is the player a seer?
		if(!players[plidx].isSeer) {
			privmsg(nick, "Only seers can see other players.");
			return;
		}
		// Is it night?
		if(!isNight) {
			privmsg(nick, "Visions may only be had during the night.");
			return;
		}
		// Has the player not yet seen?
		if(players[plidx].seen >= 0) {
			privmsg(nick, "You have already had a vision this night.");
			return;
		}
		// get target
		int targidx = getPlayer(who);
		// Is the target playing?
		if(targidx == -1) {
			privmsg(nick, who + " is not playing.");
			return;
		}
		// Is the target alive? 
		if(!players[targidx].isAlive) {
			privmsg(nick, players[targidx].getNick() + " is already dead.");
			return;
		}
		// Is the target cursed?
		if(players[targidx].isCursed) {
			// Cursed check comes before traitor check because cursed traitors can happen. Sucks for traitor.
			privmsg(nick, "You have a vision; in this vision you see that \u0002" + players[targidx].getNick() +
				"\u0002 is a \u0002wolf\u0002!");
			players[plidx].seen = targidx;
		} else if(players[targidx].isTraitor) {
			privmsg(nick, "You have a vision; in this vision you see that \u0002" + players[targidx].getNick() +
				"\u0002 is a \u0002villager\u0002!");
			players[plidx].seen = targidx;
		} else {
			privmsg(nick, "You have a vision; in this vision you see that \u0002" + players[targidx].getNick() +
				"\u0002 is a \u0002" + players[targidx].getDisplayedRole() + "\u0002!");
			players[plidx].seen = targidx;
		}
		
		// checks for the end of the night
		if(checkEndNight()) endNight();
	}
	
	/**
	 * Kills the specified player
	 * 
	 * @param who
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void kill(String who, String nick, String user, String host) {
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the KILL command");
		if(who == null) return; // Sanity check
		// Is the game even running?
		if(!isRunning) {
			privmsg(nick, "No game is running.");
			return;
		}
		
		// Is the player even playing?
		int plidx = getPlayer(nick, user, host);
		if(plidx == -1) {
			privmsg(nick, "You aren't even playing.");
			return;
		}
		// Is the player alive?
		if(!players[plidx].isAlive) {
			privmsg(nick, "Dead players aren't going to be killing people anytime soon.");
			return;
		}
		// Is the player a wolf?
		if(!players[plidx].isWolf && !players[plidx].isWerecrow) {
			privmsg(nick, "Only wolves can kill other players.");
			return;
		}
		// Is it night?
		if(!isNight) {
			privmsg(nick, "Killing may only be done during the night.");
			return;
		}
		// gets the target
		int targidx = getPlayer(who);
		// Is the target even playing?
		if(targidx == -1) {
			privmsg(nick, who + " is not playing.");
			return;
		}
		// Is the target alive? 
		if(!players[targidx].isAlive) {
			privmsg(nick, players[targidx].getNick() + " is already dead.");
			return;
		}
		// Did you target yourself?
		if(plidx == targidx) {
			privmsg(nick, "Suicide is bad. Don't do it.");
			return;
		}
		// Is the target a wolf?
		if(players[targidx].isWolf || players[targidx].isWerecrow) {
			privmsg(nick, "You may not target other wolves.");
			return;
		}
		// Add your vote.
		votes[targidx]++;
		if(players[plidx].voted >= 0) votes[players[plidx].voted]--;
		players[plidx].voted = targidx;
		// tells the wolves
		wolfmsg("\u0002" + players[plidx].getNick() + "\u0002 has selected \u0002" +
			players[targidx].getNick() + "\u0002 to be killed.");
		
		// checks for the end of the night
		if(checkEndNight()) endNight();
	}
	
	/**
	 * Lynches the specified player
	 * 
	 * @param who
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void lynch(String who, String nick, String user, String host) {
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the LYNCH / RETRACT command");
		// Is the game even running?
		if(!isRunning) {
			privmsg(nick, "No game is running.");
			return;
		}
		
		// Game is running, is it night?
		if(isNight) {
			privmsg(nick, "You can only lynch during the day.");
			return;
		}
		
		// gets the voter
		int plidx = getPlayer(nick, user, host);
		// Is the voter playing?
		if(plidx == -1) {
			privmsg(nick, "You aren't even playing.");
			return;
		}
		// Dead people may not vote
		if(!players[plidx].isAlive) {
			privmsg(nick, "Dead players may not vote.");
			return;
		}
		// Was this a retraction?
		if(who == null) {
			if(players[plidx].voted >= 0) {
				// retracts the vote
				votes[players[plidx].voted]--;
				players[plidx].voted = -1;
				chanmsg("\u0002" + nick + "\u0002 has retracted his/her vote.");
			} else {
				// nobody was voted for
				privmsg(nick, "You haven't voted for anybody.");
			}
			return;
		}
		
		// gets the votee
		int targidx = getPlayer(who);
		// Is the target playing?
		if(targidx == -1) {
			privmsg(nick, who + " is not playing.");
			return;
		}
		// You also cannot vote for dead people
		if(!players[targidx].isAlive) {
			privmsg(nick, "He's already dead! Leave the body in its grave.");
			return;
		}
		// removes the old vote
		if(players[plidx].voted >= 0) {
			// retracts the vote
			votes[players[plidx].voted]--;
		}
		// adds the new vote
		players[plidx].voted = targidx;
		votes[targidx]++;
		chanmsg("\u0002" + nick + "\u0002 has voted to lynch \u0002" + players[targidx].getNick() + "\u0002!");
		
		// Checks to see if a lynching occurs.
		checkForLynching();
	}
	
	/**
	 * Joins the game
	 * 
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void join(String nick, String user, String host) {
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the JOIN command");
		// Is the game already in progress?
		if(isRunning) {
			privmsg(nick, "The game is already in progress. Please wait patiently for it to end.");
			return;
		}
		
		// Joins the game.
		if(addPlayer(nick, user, host)) {
			chanmsg("\u0002" + nick + "\u0002 has joined the game.");
			voice(nick);
		} else {
			privmsg(nick, "You cannot join the game at this time.");
		}
	}
	
	/**
	 * Leaves the game
	 * 
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void leave(String nick, String user, String host) {
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the LEAVE command");
		// get who this is
		int plidx = getPlayer(nick, user, host);
		if(plidx == -1) {
			// not even in the game...
			privmsg(nick, "You aren't playing.");
			return;
		}
		// Is the game already in progress?
		if(isRunning) {
			players[plidx].isAlive = false;
			// kill him
			chanmsg("\u0002" + nick + "\u0002 ate toxic berries and died. It appears s/he was a \u0002" + players[plidx].getDisplayedRole() + "\u0002.");
			devoice(nick);
			// Does a something occur now that there is one less player?
			if(players[plidx].voted >= 0) votes[players[plidx].voted]--; // remove his vote
			players[plidx].resetActions(); // reset any actions he's taken
			if(checkForEnding()) return; // Does game end?
			if(isNight) {
				if(checkEndNight()) endNight(); // Does night end?
			} else {
				checkForLynching(); // Does day end?
			}
		} else {
			if(rmvPlayer(nick, user, host)) {
				chanmsg("\u0002" + nick + "\u0002 left the game.");
				devoice(nick);
			} else {
				privmsg(nick, "You cannot leave the game at this time.");
			}
		}
	}
	
	/**
	 * Starts the game.
	 * 
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void startgame(String nick, String user, String host) {
		// Has the game already begun?
		if(isRunning) {
			privmsg(nick, "The game has already begun.");
			return;
		}
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the START command");
		// Is the starter even in the game?
		int plidx = getPlayer(nick, user, host);
		if(plidx == -1) {
			// not even in the game...
			privmsg(nick, "You aren't even playing.");
			return;
		}
		
		// 4+ people?
		if(playernum < 4) {
			chanmsg("You need at least four players to begin.");
			return;
		}

		System.out.println("[CONSOLE] : Getting config....");
		// Gets the configuration
		WolfConfig wc = getConfig(playernum);
		if(wc == null) {
			chanmsg("Configuration error: " + playernum + " players is not supported.");
			System.err.println("[CONSOLE] : " + playernum + " is an unsupported player count.");
			return;
		}

		// game started; begin during night
		isRunning = true;
		isNight = true;
		
		// Welcome the players to the game & sets them alive.
		System.out.println("[CONSOLE] : Welcoming players....");
		String namelist = "";
		long gamestart = System.currentTimeMillis();
		int m = 0;
		while(m < playernum) {
			players[m].isAlive = true;
			players[m].lastaction = gamestart;
			namelist = namelist + "\u0002" + players[m].getNick() + "\u0002";
			if(m < playernum - 2) namelist = namelist + ", ";
			else if(m == playernum - 2) namelist = namelist + ", and ";
			m++;
		}
		chanmsg(namelist + ". Welcome to wolfgame as hosted by javawolf, a java implementation of the party game Mafia.");
		
		// Assign the roles
		System.out.println("[CONSOLE] : Assigning roles....");
		// Set up the number of each
		int wolfcount = wc.wolfcount;
		int traitorcount = wc.traitorcount;
		int werecrowcount = wc.werecrowcount;
		int sorcerercount = wc.sorcerercount;
		int seercount = wc.seercount;
		int harlotcount = wc.harlotcount;
		int drunkcount = wc.drunkcount;
		int angelcount = wc.angelcount;
		int detectivecount = wc.detectivecount;
		int mediumcount = wc.mediumcount;
		int cursedcount = wc.cursedcount;
		int gunnercount = wc.gunnercount;
		// randomly pick players to assign the wolf roles
		Random rand = new Random();
		int pidx = 0;
		while(wolfcount > 0) {
			pidx = (int)Math.floor(rand.nextDouble()*playernum);
			if(players[pidx].countMainRoles() == 0) {
				players[pidx].isWolf = true;
				wolfcount--;
			}
		}
		while(traitorcount > 0) {
			pidx = (int)Math.floor(rand.nextDouble()*playernum);
			if(players[pidx].countMainRoles() == 0) {
				players[pidx].isTraitor = true;
				traitorcount--;
			}
		}
		while(werecrowcount > 0) {
			pidx = (int)Math.floor(rand.nextDouble()*playernum);
			if(players[pidx].countMainRoles() == 0) {
				players[pidx].isWerecrow = true;
				werecrowcount--;
			}
		}
		while(sorcerercount > 0) {
			pidx = (int)Math.floor(rand.nextDouble()*playernum);
			if(players[pidx].countMainRoles() == 0) {
				players[pidx].isSorcerer = true;
				sorcerercount--;
			}
		}
		// now assign the main roles
		while(seercount > 0) {
			pidx = (int)Math.floor(rand.nextDouble()*playernum);
			if(players[pidx].countMainRoles() == 0) {
				players[pidx].isSeer = true;
				seercount--;
			}
		}
		while(drunkcount > 0) {
			pidx = (int)Math.floor(rand.nextDouble()*playernum);
			if(players[pidx].countMainRoles() == 0) {
				players[pidx].isDrunk = true;
				drunkcount--;
			}
		}
		while(harlotcount > 0) {
			pidx = (int)Math.floor(rand.nextDouble()*playernum);
			if(players[pidx].countMainRoles() == 0) {
				players[pidx].isHarlot = true;
				harlotcount--;
			}
		}
		while(angelcount > 0) {
			pidx = (int)Math.floor(rand.nextDouble()*playernum);
			if(players[pidx].countMainRoles() == 0) {
				players[pidx].isAngel = true;
				angelcount--;
			}
		}
		while(detectivecount > 0) {
			pidx = (int)Math.floor(rand.nextDouble()*playernum);
			if(players[pidx].countMainRoles() == 0) {
				players[pidx].isDetective = true;
				detectivecount--;
			}
		}
		while(mediumcount > 0) {
			pidx = (int)Math.floor(rand.nextDouble()*playernum);
			if(players[pidx].countMainRoles() == 0) {
				players[pidx].isMedium = true;
				mediumcount--;
			}
		}
		// now the secondary roles
		while(cursedcount > 0) {
			pidx = (int)Math.floor(rand.nextDouble()*playernum);
			if(!players[pidx].isDrunk && !players[pidx].isSeer && !players[pidx].isWolf && !players[pidx].isWerecrow) {
				players[pidx].isCursed = true;
				cursedcount--;
			}
		}
		while(gunnercount > 0) {
			pidx = (int)Math.floor(rand.nextDouble()*playernum);
			if(!players[pidx].isTraitor && !players[pidx].isWerecrow && !players[pidx].isWolf && !players[pidx].isSorcerer) {
				players[pidx].isGunner = true;
				gunnercount--;
				players[pidx].numBullets = (int)Math.floor(playernum / 9) + 1;
				if(players[pidx].isDrunk) players[pidx].numBullets *= 3; // drunk gets 3x normal bullet count
			}
		}
		// Start the night.
		chanmute();
		startNight();
	}
	
	/**
	 * Checks to see if a lynching occurs
	 */
	private void checkForLynching() {
		// tally the votes
		int[] voteresults = tallyvotes();
		int nVotes = voteresults[0];
		int ind = voteresults[1];
		int votercount = countVoters();
		
		// check to see if a lynching occurs
		if((votercount - nVotes) < nVotes) {
			// Notify players
			chanmsg("Resigned to his/her fate, \u0002" + players[ind].getNick() + "\u0002 is led to the gallows. After death, it is discovered " +
				"that s/he was a \u0002" + players[ind].getDisplayedRole() + "\u0002.");
			players[ind].isAlive = false; // kill lynched player
			devoice(players[ind].getNick());
			// Now that a lynching has occurred, end the day if the game is still running
			boolean ended = checkForEnding();
			if(!ended) endDay();
		}
	}
	
	/**
	 * Checks to see if the night ended.
	 * 
	 * @return
	 */
	private boolean checkEndNight() {
		// Check to see if all roles have acted.
		int m = 0;
		while(m < playernum) {
			// Dead players don't count
			if(!players[m].isAlive) {
				m++;
				continue;
			}
			if(players[m].isSeer && (players[m].seen == -1)) return false; // Seer didn't see
			if(players[m].isHarlot && (players[m].visited == -1)) return false; // Harlot didn't visit
			if(players[m].isWolf && (players[m].voted == -1)) return false; // Wolf didn't kill
			if(players[m].isAngel && (players[m].guarded == -1)) return false; // Angel hasn't guarded
			if(players[m].isWerecrow && (players[m].voted == -1)) return false; // Crow hasn't killed
			if(players[m].isWerecrow && (players[m].observed == -1)) return false; // Crow hasn't observed
			if(players[m].isSorcerer && (players[m].cursed == -1)) return false; // Sorcerer hasn't cursed
			m++;
		}
		
		// Everyone has acted. Night ends.
		return true;
	}
	
	/**
	 * Checks to see if the villagers or wolves win.
	 * Returns true if the game ended; false otherwise.
	 * 
	 * @return
	 */
	private boolean checkForEnding() {
		int wolfteamcount = countWolfteam(); // number of wolf teammates
		int wolfcount = countWolves(); // number of wolves (turns traitors into wolves)
		int votingcount = countVoters(); // number of voting players
		
		// Are there any wolves left?
		if(wolfteamcount == 0) {
			chanmsg("Game over! All the wolves are dead! The villagers chop them up, barbeque them, and enjoy a hearty meal!");
			// broadcast roles
			String msg = "";
			int m = 0;
			while(m < playernum) {
				// append to string
				if(players[m].isSeer) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002seer\u0002. ";
				if(players[m].isDrunk) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002village drunk\u0002. ";
				if(players[m].isHarlot) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002harlot\u0002. ";
				if(players[m].isAngel) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002guardian angel\u0002. ";
				if(players[m].isDetective) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002detective\u0002. ";
				if(players[m].isGunner) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002gunner\u0002. ";
				if(players[m].isCursed) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002cursed villager\u0002. ";
				if(players[m].isWolf) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002wolf\u0002. ";
				if(players[m].isTraitor) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002traitor\u0002. ";
				if(players[m].isWerecrow) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002werecrow\u0002. ";
				m++;
			}
			chanmsg(msg);
			// game is over
			channelCleanup();
			return true;
		}
		// Do the wolves eat the players?
		if((votingcount - wolfteamcount) <= wolfteamcount) {
			chanmsg("Game over! There are the same number of wolves as voting villagers! The wolves eat everyone and win.");
			// broadcast roles
			String msg = "";
			int m = 0;
			while(m < playernum) {
				// append to string
				if(players[m].isSeer) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002seer\u0002. ";
				if(players[m].isDrunk) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002village drunk\u0002. ";
				if(players[m].isHarlot) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002harlot\u0002. ";
				if(players[m].isAngel) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002guardian angel\u0002. ";
				if(players[m].isDetective) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002detective\u0002. ";
				if(players[m].isGunner) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002gunner\u0002. ";
				if(players[m].isCursed) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002cursed villager\u0002. ";
				if(players[m].isWolf) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002wolf\u0002. ";
				if(players[m].isTraitor) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002traitor\u0002. ";
				if(players[m].isWerecrow) msg = msg + "\u0002" + players[m].getNick() + "\u0002 was a \u0002werecrow\u0002. ";
				m++;
			}
			chanmsg(msg);
			// game is over
			channelCleanup();
			return true;
		}
		// Do we need to turn the traitors into wolves?
		// This happens if there are no wolves but still wolf teammates (traitors) left.
		if(wolfcount == 0) {
			int m = 0;
			while(m < playernum) {
				if(players[m].isTraitor || players[m].isSorcerer) {
					// change status
					players[m].isWolf = true;
					players[m].isTraitor = false;
					players[m].isSorcerer = false;
					// Let them know in dramatic style :p
					privmsg(players[m].getNick(), "HOOOWWWWWLLLLLLL! You have become...a wolf! It is up to you to avenge your fallen leaders!");
				}
				m++;
			}
			// Let everyone know
			chanmsg("\u0002The villagers, in the midst of their rejoicing, are terrified as a loud howl breaks out. The wolves are not gone!\u0002");
		}
		// Game is still running
		return false;
	}
	
	/**
	 * Counts the number of players left on the wolfteam
	 * 
	 * @return
	 */
	private int countWolfteam() {
		int nWolves = 0;
		
		int m = 0;
		while(m < playernum) {
			// only consider living wolves
			if(players[m].isAlive) {
				if(players[m].isWolf || players[m].isTraitor || players[m].isWerecrow || players[m].isSorcerer) nWolves++;
			}
			m++;
		}
		
		return nWolves;
	}
	
	/**
	 * Counts the number of wolves left.
	 * Used for turning traitors into wolves.
	 * 
	 * @return
	 */
	private int countWolves() {
		int nWolves = 0;
		
		int m = 0;
		while(m < playernum) {
			// only consider living wolves
			if(players[m].isAlive) {
				if(players[m].isWolf || players[m].isWerecrow) nWolves++;
			}
			m++;
		}
		
		return nWolves;
	}
	
	/**
	 * Counts the number of voting players left
	 * 
	 * @return
	 */
	private int countVoters() {
		int nVoters = 0;
		
		int m = 0;
		while(m < playernum) {
			// only consider living players
			if(players[m].isAlive && players[m].canVote) nVoters++;
			m++;
		}
		
		return nVoters;
	}
	
	/**
	 * Starts the night
	 */
	private void startNight() {
		System.out.println("[CONSOLE] : Starting the night.");
		// Start timing the night
		starttime = System.currentTimeMillis();
		// reset the votes
		votes = new int[MAX_WOLFPLAYERS];
		// PM all the players their roles
		String wolflist = "| ";
		int m = 0;
		while(m < playernum) {
			if(players[m].isWolf) wolflist = wolflist + players[m].getNick() + " (wolf) |";
			if(players[m].isTraitor) wolflist = wolflist + players[m].getNick() + " (traitor) |";
			if(players[m].isWerecrow) wolflist = wolflist + players[m].getNick() + " (werecrow) |";
			if(players[m].isSorcerer) wolflist = wolflist + players[m].getNick() + " (sorcerer) |";
			m++;
		}
		m = 0;
		while(m < playernum) {
			// ignore dead people
			if(players[m].isAlive) {
				// evil roles
				if(players[m].isWolf) {
					privmsg(players[m].getNick(), "You are a \u0002wolf\u0002. Use \"" + 
						Javawolf.cmdchar + "kill <name>\" to kill a villager once per night.");
					privmsg(players[m].getNick(), "Wolf list " + wolflist);
				}
				if(players[m].isTraitor) {
					privmsg(players[m].getNick(), "You are a \u0002traitor\u0002. You are on the side of " +
						"the wolves, except that you are, for all intents and purposes, a villager. Only detectives can identify you. " +
						"If all the wolves die, you will become a wolf yourself.");
					privmsg(players[m].getNick(), "Wolf list " + wolflist);
				}
				if(players[m].isWerecrow) {
					privmsg(players[m].getNick(), "You are a \u0002werecrow\u0002. Use \"" + Javawolf.cmdchar + "kill <name>\" to kill a villager " +
						"once per night. You may also observe a player to see whether s/he stays in bed all night with \"" +
						Javawolf.cmdchar + "observe <name>\".");
					privmsg(players[m].getNick(), "Wolf list " + wolflist);
				}
				if(players[m].isSorcerer) {
					privmsg(players[m].getNick(), "You are a \u0002sorcerer\u0002. Use \"" + Javawolf.cmdchar + "curse <name>\" to curse a villager " +
						"each night. The seer will then see the cursed villager as a wolf! If all the wolves die, you will become a wolf yourself.");
					privmsg(players[m].getNick(), "Wolf list " + wolflist);
				}
				// good roles
				if(players[m].isSeer) privmsg(players[m].getNick(), "You are a \u0002seer\u0002. Use \"" + 
						Javawolf.cmdchar + "see <name>\" to see the role of a villager once per night. Be warned that traitors will " +
						"still appear as villagers, and cursed villagers will appear to be wolves. Use your judgment.");
				if(players[m].isDrunk) privmsg(players[m].getNick(), "You have been drinking too much! You are a \u0002village drunk\u0002! " +
					"You don't do anything special, but people are more likely to believe that you are not a wolf.");
				if(players[m].isHarlot) privmsg(players[m].getNick(), "You are a \u0002harlot\u0002. You may visit any player during the night. " +
					"If you visit a wolf or the victim of the wolves, you will die. If you are attacked while you are out visiting, " +
					"you will survive. Use \"" + Javawolf.cmdchar + "visit <name>\" to visit a player.");
				if(players[m].isAngel) privmsg(players[m].getNick(), "You are a \u0002guardian angel\u0002. You may choose one player to guard " +
					"per night. If you guard the victim, s/he will likely live. If you guard a wolf, you may die. Use \"" + Javawolf.cmdchar +
					"guard <name>\" to guard someone.");
				if(players[m].isDetective) privmsg(players[m].getNick(), "You are a \u0002detective\u0002. You act during the day, and you can even " +
					"identify traitors. Use \"" + Javawolf.cmdchar + "id <name>\" to identify someone. Be careful when iding, because " +
					"your identity might be revealed to the wolves.");
				if(players[m].isMedium) privmsg(players[m].getNick(), "You are a \u0002medium\u0002. Once per day, you can choose to raise a " +
					"player from the dead to consult with him or her. However, the spirit will be unable to use any powers. Use \"" +
					Javawolf.cmdchar + "raise <name>\" to raise a player.");
				if(players[m].isGunner) {
					privmsg(players[m].getNick(), "You hold a gun that shoots special silver bullets. If you shoot a wolf, s/he will die. " +
						"If you shoot a villager, s/he will most likely live. Use \"" + Javawolf.cmdchar + "shoot <name>\" to shoot.");
					privmsg(players[m].getNick(), "You have " + players[m].numBullets + " bullets remaining.");
				}
			}
			// Next player
			m++;
		}
		// Announce the night to the players
		chanmsg("It is now night, and the villagers all retire to their beds. The full moon rises in the east, casting long, eerie " +
			"shadows across the village. A howl breaks out, as the wolves come out to slay someone.");
		chanmsg("Please check for private messages from me. If you have none, simply sit back and wait patiently for morning.");
		// Schedule a warning event.
		timer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				warnNightEnding();
			}
		};
		timer.schedule(task, 1000*90);
	}
	
	/**
	 * Warns the channel that the night will be ending soon.
	 */
	private void warnNightEnding() {
		chanmsg("\u0002The sky to the east begins to lighten, and several villagers wake up, hearing the panting of " +
			"wolves in the village. The full moon begins to set, casting eerie, lengthening shadows towards the dawn. " +
			"The night is now almost spent, and the wolves will be forced to return to human form soon.\u0002");
		// force the night to end
		TimerTask task = new TimerTask() {
			public void run() {
				endNight();
			}
		};
		timer.schedule(task, 1000*30);
	}
	
	private void warnDayEnding() {
		chanmsg("\u0002As the sun sinks inexorably towards the tops of the towering pine trees turning the forest trees " + 
			"to the west into a series of black silhouettes projected against the flaming sky, the villagers are reminded " +
			"that very little time remains for them to lynch someone. If they cannot reach a decision before the sun sets, " +
			"the majority will carry the vote.\u0002");
		// force the night to end
		TimerTask task = new TimerTask() {
			public void run() {
				chanmsg("\u0002While the villagers continue their debating, the sun reaches the horizon, forcing them to " +
					"conclude their deliberations!\u0002");
				endDay();
			}
		};
		timer.schedule(task, 1000*120);
	}
	
	/**
	 * Ends the night
	 */
	private void endNight() {
		System.out.println("[CONSOLE] : Ending the night.");
		timer.cancel();
		int m = 0;
		Random rand = new Random();
		// Posts the time the night took
		long millis = System.currentTimeMillis() - starttime;
		int secs = (int)(millis / 1000);
		int mins = (int)(secs / 60);
		secs = secs % 60;
		chanmsg("Night lasted \u0002" + mins + ":" + (secs < 10 ? "0" + secs : secs) + "\u0002.");
		// Make the wolf list
		String wolflist = "| ";
		m = 0;
		while(m < playernum) {
			if(players[m].isWolf) wolflist = wolflist + players[m].getNick() + " (wolf) |";
			if(players[m].isTraitor) wolflist = wolflist + players[m].getNick() + " (traitor) |";
			if(players[m].isWerecrow) wolflist = wolflist + players[m].getNick() + " (werecrow) |";
			m++;
		}
		// Announce the end of the night 
		chanmsg("Dawn breaks! The villagers wake up and look around.");
		// Tally up the votes and announce the kill
		int[] voteresults = tallyvotes();
		int ind = voteresults[1];
		if(ind == -1) {
			// LOL
			chanmsg("The wolves were unable to decide who to kill last night. As a result, all villagers have survived.");
		} else {
			// Wolves attack someone
			// Is the target protected by a guardian angel?
			boolean successfullyguarded = false;
			m = 0;
			while(m < playernum) {
				// Is the player a guardian angel?
				if(players[m].isAngel && players[m].isAlive) {
					// Did the player guard the victim?
					if(players[m].guarded == ind) {
						// Does the angel die in the attempt?
						if(rand.nextDouble() < dieguardingvictimpct) {
							chanmsg("\u0002" + players[m].getNick() + "\u0002, a \u0002" + players[m].getDisplayedRole() + 
									"\u0002, tried to defend the victim, but died in the attempt.");
								players[m].isAlive = false;
								devoice(players[m].getNick());
						} else if(rand.nextDouble() < guardedvictimdiepct) {
							// Angel failed in the defense.
							chanmsg("A guardian angel tried to protect \u0002" + players[ind].getNick() + 
								"\u0002, but s/he was unable to stop the attacks of the wolves and was forced to retreat.");
						} else {
							// successful guarding
							successfullyguarded = true;
							// Does the angel become a wolf though?
							if(rand.nextDouble() < infectguardingvictimpct) {
								privmsg(players[m].getNick(), "While defending the victim, you were slightly injured in the fray. " +
									"You feel a change coming over you...you have become a \u0002wolf\u0002!");
								privmsg(players[m].getNick(), wolflist);
							}
							// Does the victim turn into a wolf? Obviously, wolfteam won't change.
							if(!players[ind].isWolf && !players[ind].isTraitor && !players[ind].isWerecrow) {
								if(rand.nextDouble() < guardedvictiminfectpct) {
									privmsg(players[ind].getNick(), "Although a guardian angel saved you, you were slightly injured in the fray. " +
										"You feel a change coming over you...you have become a \u0002wolf\u0002!");
									privmsg(players[ind].getNick(), wolflist);
								}
							}
						}
					}
				}
				m++;
			}
			if(!successfullyguarded) {
				// Is the target a harlot who visited someone?
				if(players[ind].isHarlot && (players[ind].visited >= 0) && (players[ind].visited != ind)) {
					// Harlot visited someone else
					chanmsg("The wolves' selected victim was a harlot, but she wasn't home.");
				} else {
					chanmsg("The dead body of \u0002" + players[ind].getNick() + "\u0002, a \u0002" + players[ind].getDisplayedRole() + 
						"\u0002, is found. Those remaining mourn his/her death.");
					players[ind].isAlive = false;
					devoice(players[ind].getNick());
					// Wolves might find a gun
					if(players[ind].isGunner && (players[ind].numBullets > 0)) {
						if(rand.nextDouble() < wolffindgunpct) {
							// randomly pick a wolf / traitor to give the gun to
							int[] wolfidxs = new int[MAX_WOLFPLAYERS];
							int nWolves = 0;
							m = 0;
							while(m < playernum) {
								if(players[m].isWolf || players[m].isTraitor || players[m].isWerecrow) {
									wolfidxs[nWolves] = m;
									nWolves++;
								}
								m++;
							}
							int pickedwolf = wolfidxs[(int)Math.floor(rand.nextDouble()*nWolves)];
							players[pickedwolf].isGunner = true;
							players[pickedwolf].numBullets = players[ind].numBullets; // give him however many bullets remained
							// notify him
							privmsg(players[pickedwolf].getNick(), "You find \u0002" + players[ind].getNick() +
								"'s\u0002 gun with " + players[ind].numBullets + " bullets! Use \"" + Javawolf.cmdchar + 
								"shoot <name>\" to shoot a player. You will deliberately miss other wolves.");
						}
					}
				}
				// Did any harlots visit the victim?
				m = 0;
				while(m < playernum) {
					if(players[m].isHarlot && players[m].isAlive) {
						if(players[m].visited == ind) {
							// Harlot visited victim and died
							chanmsg(players[m].getNick() + ", a harlot, made the unfortunate mistake of visiting the victim last night " +
								"and is now dead.");
							players[m].isAlive = false;
							devoice(players[m].getNick());
						}
					}
					// next player
					m++;
				}
			} else {
				chanmsg("\u0002" + players[ind].getNick() + "\u0002 was attacked by the wolves last night, but luckily, a " +
					"guardian angel protected him/her.");
			}
		}
		// Did any harlots visit wolves?
		m = 0;
		while(m < playernum) {
			if(players[m].isHarlot && players[m].isAlive) {
				if(players[players[m].visited].isWolf || players[players[m].visited].isWerecrow) {
					// Harlot visited wolf and died
					chanmsg("\u0002" + players[m].getNick() + "\u0002, a \u0002harlot\u0002, made the unfortunate mistake of " +
						"visiting a wolf last night and is now dead.");
					players[m].isAlive = false;
					devoice(players[m].getNick());
				}
			}
			// next player
			m++;
		}
		// Did any guardian angels guard wolves?
		m = 0;
		while(m < playernum) {
			// Is the player a guardian angel?
			if(players[m].isAngel && players[m].isAlive) {
				if(players[players[m].guarded].isWolf || players[players[m].guarded].isWerecrow) {
					// Guarded a wolf
					if(rand.nextDouble() < dieguardingwolfpct) {
						chanmsg("\u0002" + players[m].getNick() + "\u0002, a \u0002guardian angel\u0002, made the unfortunate mistake of " +
							"guarding a wolf last night, tried to escape, but failed. S/he is found dead.");
						players[m].isAlive = false;
						devoice(players[m].getNick());
					}
				}
			}
			m++;
		}
		// Werecrows' observations return now
		m = 0;
		while(m < playernum) {
			if(players[m].isAlive) {
				if(players[m].isWerecrow) {
					if(players[m].observed != -1) {
						if(players[players[m].observed].isAlive) {
							// Did the target stay in bed?
							if((players[players[m].observed].isSeer && (players[players[m].observed].seen != -1)) ||
								(players[players[m].observed].isHarlot && (players[players[m].observed].visited != -1)) ||
								(players[players[m].observed].isAngel && (players[players[m].observed].guarded != -1))) {
								privmsg(players[m].getNick(), "\u0002" + players[players[m].observed].getNick() + "\u0002 left his/her bed last night.");
							} else {
								privmsg(players[m].getNick(), "\u0002" + players[players[m].observed].getNick() + "\u0002 remained in bed all night.");
							}
							// Did anyone "visit" the target?
							int n = 0;
							while(n < playernum) {
								if(players[n].isAlive) {
									if(players[n].isAngel) {
										if(players[n].guarded == players[m].observed)
											privmsg(players[m].getNick(), "\u0002" + players[n].getNick() +
												"\u0002, a \u0002guardian angel\u0002, guarded \u0002" + 
												players[players[m].observed].getNick() + "\u0002 last night.");
									} else if(players[n].isSeer) {
										if(players[n].seen == players[m].observed)
											privmsg(players[m].getNick(), "\u0002" + players[n].getNick() +
												"\u0002, a \u0002seer\u0002, saw \u0002" + 
												players[players[m].observed].getNick() + "\u0002 last night.");
									} else if(players[n].isHarlot) {
										if(players[n].visited == players[m].observed)
											privmsg(players[m].getNick(), "\u0002" + players[n].getNick() +
												"\u0002, a \u0002harlot\u0002, visited \u0002" + 
												players[players[m].observed].getNick() + "\u0002 last night.");
									}
								}
								n++;
							}
						}
					}
				}
			}
			m++;
		}
		// Reset the players' actions and votes
		votes = new int[MAX_WOLFPLAYERS];
		m = 0;
		while(m < playernum) {
			players[m].resetActions();
			m++;
		}
		// Check to see if the game ended
		if(checkForEnding()) return; // no need to go on
		// Let players know they can now lynch people.
		chanmsg("The villagers must now decide who to lynch. Use \"" + Javawolf.cmdchar + "lynch <name>\" to vote for someone.");
		isNight = false;
		// Start timing the day
		timer = new Timer();
		starttime = System.currentTimeMillis();
		TimerTask task = new TimerTask() {
			public void run() {
				warnDayEnding();
			}
		};
		timer.schedule(task, 600*1000);
	}
	
	/**
	 * Ends the day
	 */
	private void endDay() {
		if(isRunning && !isNight) {
			timer.cancel();
			// Posts the time the day took
			long millis = System.currentTimeMillis() - starttime;
			int secs = (int)(millis / 1000);
			int mins = (int)(secs / 60);
			secs = secs % 60;
			chanmsg("Night lasted \u0002" + mins + ":" + (secs < 10 ? "0" + secs : secs) + "\u0002.");
			// Kill any raised spirits again
			int m = 0;
			while(m < playernum) {
				if(players[m].isMedium && (players[m].raised != -1)) {
					chanmsg("As dusk falls, the spirit of \u0002" + players[players[m].raised].getNick() + "\u0002 returns to rest.");
					devoice(players[players[m].raised].getNick());
				}
				// also reset actions taken
				players[m].resetActions();
				m++;
			}
			// Starts the night
			isNight = true;
			startNight();
		}
	}
	
	/**
	 * Tallies the votes. First value is the number of votes, second value is the person most
	 * voted for. The second value is -1 when a tie occurs. 
	 * 
	 * @return
	 */
	private int[] tallyvotes() {
		int nVotes = -1; // number of votes
		int indVoted = -1; // who has that number
		
		int m = 0;
		while(m < playernum) {
			if(votes[m] > nVotes) {
				nVotes = votes[m];
				indVoted = m;
			} else if(votes[m] == nVotes) {
				indVoted = -1; // tie
			}
			m++;
		}
		
		// return the values
		int[] retvals = new int[2];
		retvals[0] = nVotes;
		retvals[1] = indVoted;
		return retvals;
	}
	
	/**
	 * Changes the command character
	 * 
	 * @param cmdchar
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void change_cmdchar(String cmdchar, String nick, String user, String host) {
		// notify channel
		chanmsg("\u0002" + nick + "\u0002 has changed the command prefix to: '" + cmdchar + "'.");
		Javawolf.cmdchar = cmdchar;
	}
	
	/**
	 * Kills the bot
	 * 
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void fquit(String nick, String user, String host) {
		// notify channel
		chanmsg("\u0002" + nick + "\u0002 has forced the game to end.");
		// clean up channel
		channelCleanup();
		// shuts down
		quitirc("Requested by " + nick);
		System.out.println("[CONSOLE] : " + nick + "!" + user + "@" + host + " has shut down this bot.");
		System.exit(0);
	}
	
	/**
	 * Forces the game to end.
	 * 
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void fendgame(String nick, String user, String host) {
		// Logs the command
		System.out.println("[CONSOLE] : " + nick + " issued the FENDGAME command");
		// notify channel
		chanmsg("\u0002" + nick + "\u0002 has forced the game to end.");
		// clean up channel
		channelCleanup();
	}
	
	/**
	 * Cleans up the channel after a game finishes.
	 */
	private void channelCleanup() {
		// clear the timer
		timer.cancel();
		// unmute the channel
		chanunmute();
		// devoice the players
		int m = 0;
		while(m < playernum) {
			if(isRunning && players[m].isAlive) devoice(players[m].getNick());
			else if(!isRunning) devoice(players[m].getNick());
			m++;
		}
		// make new players
		players = new WolfPlayer[MAX_WOLFPLAYERS];
		votes = new int[MAX_WOLFPLAYERS];
		playernum = 0;
		isRunning = false;
		isNight = false;
	}
	
	/**
	 * Gets the index for which player matches the nick!user@host
	 * 
	 * @param nick
	 * @param user
	 * @param host
	 * @return
	 */
	private int getPlayer(String nick, String user, String host) {
		int m = 0;
		while(m < playernum) {
			if(players[m].identmatch(nick, user, host)) {
				return m; // found him
			}
			m++;
		}
		// no such player
		return -1;
	}
	
	/**
	 * Gets the player index who matches <nick>
	 * 
	 * @param nick
	 * @return
	 */
	private int getPlayer(String nick) {
		int matched_player = -1;
		int m = 0;
		while(m < playernum) {
			if(players[m].getNick().contentEquals(nick)) {
				return m; // exact match; return immediately
			} else if(players[m].getNick().toLowerCase().startsWith(nick.toLowerCase())) {
				if(matched_player == -1) matched_player = m; // partial match
				else matched_player = -2; // multiple matches
			}
			m++;
		}
		// return the player
		if(matched_player == -2) return -1;
		else return matched_player;
	}
	
	/**
	 * A player changes his nick
	 * 
	 * @param oldNick
	 * @param user
	 * @param host
	 * @param newNick
	 */
	public void changeNick(String oldNick, String user, String host, String newNick) {
		// get him
		int plidx = getPlayer(oldNick, user, host);
		if(plidx != -1) {
			players[plidx].setNick(newNick);
		}
	}
	
	/**
	 * A player left the channel
	 * 
	 * @param nick
	 * @param user
	 * @param host
	 */
	public void playerLeftChannel(String nick, String user, String host) {
		// get him
		int plidx = getPlayer(nick, user, host);
		if(plidx != -1) {
			if(isRunning) {
				// Game is running, kill the player
				if(players[plidx].isAlive) {
					players[plidx].isAlive = false;
					chanmsg("\u0002" + players[plidx].getNick() + "\u0002 died of an unknown disease. It appears s/he was a \u0002" +
						players[plidx].getDisplayedRole() + "\u0002.");
					devoice(players[plidx].getNick());
					// Does something happen now?
					if(!checkForEnding()) {
						if(isNight) {
							checkEndNight();
						} else {
							checkForLynching();
						}
					}
				}
			} else {
				// No game; delete said player
				rmvPlayer(nick, user, host);
			}
		}
	}
	
	/**
	 * Player kicked from channel
	 * 
	 * @param nick
	 */
	public void playerKickedFromChannel(String nick) {
		// get him
		int plidx = getPlayer(nick);
		if(plidx != -1) {
			if(isRunning) {
				if(players[plidx].isAlive) {
					// Game is running, kill the player
					players[plidx].isAlive = false;
					chanmsg("\u0002" + players[plidx].getNick() + "\u0002 was kicked off a cliff. It appears s/he was a \u0002" +
						players[plidx].getDisplayedRole() + "\u0002.");
					devoice(players[plidx].getNick());
					// Does something happen now?
					if(!checkForEnding()) {
						if(isNight) {
							checkEndNight();
						} else {
							checkForLynching();
						}
					}
				}
			} else {
				// No game; delete said player
				rmvPlayer(nick, players[plidx].getUser(), players[plidx].getHost());
			}
		}
	}
	
	/**
	 * Resets the idletime when somebody says something.
	 * 
	 * @param nick
	 * @param user
	 * @param host
	 */
	public void resetidle(String nick, String user, String host) {
		int plidx = getPlayer(nick, user, host);
		if(plidx != -1) {
			players[plidx].lastaction = System.currentTimeMillis();
		}
	}
	
	/**
	 * 
	 * @param nick
	 * @param user
	 * @param host
	 * @return
	 */
	private boolean addPlayer(String nick, String user, String host) {
		// Does this guy already exist?
		if(getPlayer(nick, user, host) >= 0) return false;
		// only add the player if enough slots
		if(playernum < MAX_WOLFPLAYERS) {
			players[playernum] = new WolfPlayer(nick, user, host, System.currentTimeMillis());
			playernum++;
			return true;
		}
		// couldn't add
		return false;
	}
	
	/**
	 * Removes a player before the game begins.
	 * DO NOT CALL THIS WHILE THE GAME IS RUNNING!
	 * Simply kill said player instead.
	 * 
	 * @param nick
	 * @param user
	 * @param host
	 * @return
	 */
	private boolean rmvPlayer(String nick, String user, String host) {
		// DO NOT DELETE PLAYERS DURING THE GAME!!!
		// If a player is removed during the game, the votes will be corrupted
		if(isRunning) {
			System.err.println("INTERNAL ERROR : <rmvPlayer> called while the game is running!");
			return false;
		}
		
		// removes the player
		int plidx = getPlayer(nick, user, host);
		if(plidx >= 0) {
			// rotate them down
			int m = plidx;
			while(m < playernum-1) {
				players[m] = players[m+1];
				m++;
			}
			players[playernum-1] = null; // delete last entry
			playernum--;
			// player is gone
			return true;
		} else {
			System.err.println("[CONSOLE] : Could not find \"" + nick + "!" + user + "@" + host + "\"!");
			return false;
		}
	}
	
	/**
	 * Broadcasts messages to the wolf team
	 * 
	 * @param msg
	 */
	private void wolfmsg(String msg) {
		int m = 0;
		while(m < playernum) {
			if(players[m].isWolf || players[m].isTraitor || players[m].isWerecrow) privmsg(players[m].getNick(), msg);
			m++;
		}
	}
	
	/**
	 * Messages the channel
	 * 
	 * @param msg
	 */
	private void chanmsg(String msg) {
		Javawolf.wolfbot.sendMessage(assocChannel, msg);
	}
	
	/**
	 * Checks to see if the given user is a bot admin
	 * 
	 * @param host
	 * @return
	 */
	private boolean admincheck(String host) {
		return Javawolf.trustedHosts.contains(host);
	}
	
	/**
	 * Messages a player
	 * 
	 * @param nick
	 * @param msg
	 */
	private void privmsg(String nick, String msg) {
		Javawolf.wolfbot.sendMessage(nick, msg);
	}
	
	/**
	 * Mutes the channel
	 */
	private void chanmute() {
		Javawolf.wolfbot.setMode(assocChannel, "+m");
	}
	
	/**
	 * Unmutes the channel
	 */
	private void chanunmute() {
		Javawolf.wolfbot.setMode(assocChannel, "-m");
	}
	
	/**
	 * Voices the given player
	 * 
	 * @param nick
	 */
	private void voice(String nick) {
		Javawolf.wolfbot.voice(assocChannel, nick);
	}
	
	/**
	 * Devoices the given player
	 * 
	 * @param nick
	 */
	private void devoice(String nick) {
		Javawolf.wolfbot.deVoice(assocChannel, nick);
	}
	
	/**
	 * Ops the speaker.
	 * 
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void op(String nick, String user, String host) {
		Javawolf.wolfbot.op(assocChannel, nick);
	}
	
	/**
	 * Deops the target.
	 * 
	 * @param who
	 * @param nick
	 * @param user
	 * @param host
	 */
	private void deop(String who, String nick, String user, String host) {
		if(who == null) return; // Sanity check
		Javawolf.wolfbot.deOp(assocChannel, who);
	}
	
	/**
	 * Leaves the server
	 * 
	 * @param reason
	 */
	private void quitirc(String reason) {
		Javawolf.wolfbot.quitServer(reason);
	}
	
	/**
	 * Adds a player configuration
	 * 
	 * @param wc
	 */
	public static void addconfig(WolfConfig wc) {
		configs[nCfgs] = wc;
		nCfgs++;
	}
	
	/**
	 * Gets the configuration for the given number of players.
	 * 
	 * @param num
	 * @return
	 */
	private WolfConfig getConfig(int num) {
		int m = 0;
		while(m < nCfgs) {
			if(configs[m] == null) {
				// some bug
				System.err.println("[CONSOLE] : ERROR : Config #" + m + " is null!");
				return null;
			}
			if((num <= configs[m].high) && (num >= configs[m].low)) {
				return configs[m];
			}
			m++;
		}
		// configuration error
		return null;
	}
}

/**
 * Used for configuring the roles for the given number of players.
 * 
 * @author Reaper Eternal
 *
 */
class WolfConfig {
	// Minimum number of players needed to have this setup
	public int low = -1;
	// Maximum number of players needed to have this setup
	public int high = -1;
	// Wolf team roles
	public int wolfcount = 0;
	public int traitorcount = 0;
	public int werecrowcount = 0;
	public int sorcerercount = 0;
	// Village primary roles
	public int seercount = 0;
	public int harlotcount = 0;
	public int drunkcount = 0;
	public int angelcount = 0;
	public int detectivecount = 0;
	public int mediumcount = 0;
	// Village secondary roles
	public int cursedcount = 0;
	public int gunnercount = 0;
	
	// null constructor
	public WolfConfig() { }
}

