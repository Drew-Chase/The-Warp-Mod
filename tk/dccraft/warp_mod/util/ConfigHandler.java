package tk.dccraft.warp_mod.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import tk.dccraft.warp_mod.WarpMod;

/**
 * Handles a custom (non-forge) configuration system
 * 
 * @author Drew Chase
 *
 */
@SuppressWarnings("all")
public class ConfigHandler {

	private static BufferedWriter bw;
	private static BufferedReader br;
	private static String folderLocation = "";
	private static boolean specatorMode = false, opAllowed = false, creativeAllowed = false, gamemode_override = false, debug = false, allow_all_players = false;
	private static ArrayList<String> players = new ArrayList<>();

	/**
	 * Loads the config file if available.
	 * 
	 * @param fileName
	 * @author Drew Chase
	 */
	public static void loadConfig(String fileName) {
		try {
			br = new BufferedReader(new FileReader(getFolderLocation() + fileName));
			WarpMod.instance.consoleMessage("Loading Config " + fileName);
			String line = br.readLine();
			String text = "";
			while (line != null) {
				for (char s : line.toCharArray()) {
					if (s == '#') {
						line = br.readLine();
						break;
					}
				}
				if (line.startsWith("#")) {
					line = br.readLine();
					continue;
				} else if (line.startsWith("spectate mode: ")) {
					text = line.replaceAll("spectate mode: ", "");
					if (text.equalsIgnoreCase("true"))
						setIsSpecatatorAllowed(true);
					else
						setIsSpecatatorAllowed(false);
				} else if (line.startsWith("debug-mode: ")) {
					text = line.replaceAll("debug-mode: ", "");
					if (text.equalsIgnoreCase("true"))
						setDebugMode(true);
					else
						setDebugMode(false);
				} else if (line.startsWith("ops allowed: ")) {
					text = line.replaceAll("ops allowed: ", "");
					if (text.equalsIgnoreCase("true"))
						setIsOpAllowed(true);
					else
						setIsOpAllowed(false);
				} else if (line.startsWith("creative allowed: ")) {
					text = line.replaceAll("creative allowed: ", "");
					if (text.equalsIgnoreCase("true"))
						setIsCreativeAllowed(true);
					else
						setIsCreativeAllowed(false);
				} else if (line.startsWith("gamemode override: ")) {
					text = line.replaceAll("gamemode override: ", "");
					if (text.equalsIgnoreCase("true"))
						setOverrideGameMode(true);
					else
						setOverrideGameMode(false);
				} else if (line.startsWith("allowed-players:[")) {
					text = line.replaceAll("allowed-players:[", "").replace("\"", "").replace("]", "");
					
				}

				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			saveConfig(getConfigFileName(), "#only if gamemode override is enabled\nspectate mode: true\ndebug-mode: false\nops allowed: false\n#only if gamemode override is enabled\ncreative allowed: false\n#If the gamemode override is false then spectate and creative override won't work\ngamemode override: true\n#What Players Are Allowed to Use the mod\nallowed-players:[\"*\"]");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * reloads the current config if needed.
	 * 
	 * @author Drew Chase
	 */
	public static void reloadConfig() {
		loadConfig(getConfigFileName());
	}

	/**
	 * Creates the config file if it doesn't exist or is otherwise corrupted
	 * 
	 * @param fileName
	 * @param fileContents
	 * @author Drew Chase
	 */
	public static void saveConfig(String fileName, String fileContents) {

		File f = new File(getFolderLocation());

		if (f.mkdirs())
			WarpMod.instance.consoleMessage("Creating File in " + f.getAbsolutePath());

		try {
			bw = new BufferedWriter(new FileWriter(getFolderLocation() + fileName, false));
			bw.write(fileContents);
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
					WarpMod.instance.consoleMessage("Saving Config: " + fileName);
					loadConfig(fileName);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static boolean getAreAllPlayersAllowed() {
		return allow_all_players;
	}

	/**
	 * 
	 * @return rather the mod will override the current gamemode commands
	 */
	public static boolean getOverrideGameMode() {
		return gamemode_override;
	}

	/**
	 * Only works if gamemode override is enabled
	 * 
	 * @return if spectate mode is allowed
	 */
	public static boolean getIsSpecatorModeAllowed() {
		return specatorMode;
	}

	/**
	 * 
	 * @return if ops are allowed
	 */
	public static boolean getIsOpAllowed() {
		return opAllowed;
	}

	/**
	 * 
	 * @return if creative mod is allowed
	 */
	public static boolean getIsCreativeAllowed() {
		return creativeAllowed;
	}

	/**
	 * 
	 * @return if verbose logging is enabled
	 */
	public static boolean getDebugMode() {
		return debug;
	}

	/**
	 * Sets if all players are allowed to use the mod
	 * 
	 * @param value
	 */
	public static void setAreAllPlayersAllowed(boolean value) {
		allow_all_players = value;
	}

	/**
	 * Sets the value of gamemode override
	 * 
	 * @param value
	 */
	public static void setOverrideGameMode(boolean value) {
		WarpMod.instance.consoleMessage("Gamemode Override is set to " + value);
		gamemode_override = value;
	}

	/**
	 * Sets the value of Spectator mode
	 * 
	 * @param value
	 */
	public static void setIsSpecatatorAllowed(boolean value) {
		WarpMod.instance.consoleMessage("Spectate Mode is set to " + value);
		specatorMode = value;
	}

	/**
	 * Sets the value of if ops are allowed
	 * 
	 * @param value
	 */
	public static void setIsOpAllowed(boolean value) {
		WarpMod.instance.consoleMessage("Opps Allowed is set to " + value);
		opAllowed = value;
	}

	/**
	 * Sets the value of if verbose logging is enabled
	 * 
	 * @param value
	 */
	public static void setDebugMode(boolean value) {
		if (value)
			WarpMod.instance.consoleMessage("Debug Mode is enabled");
		debug = value;
	}

	/**
	 * Sets the value of if creative mod is allowed
	 * 
	 * @param value
	 */
	public static void setIsCreativeAllowed(boolean value) {
		WarpMod.instance.consoleMessage("Creative Mode Allowed is set to " + value);
		creativeAllowed = value;
	}

	/**
	 * 
	 * @return gets the config file name
	 */
	public static String getConfigFileName() {
		return "config.conf";
	}

	/**
	 * 
	 * @return the location of the config file
	 */
	public static String getFolderLocation() {
		if (folderLocation.isEmpty())
			setFolderLocation("config/Warps/");
		return folderLocation;
	}

	/**
	 * Sets the location of the config file
	 * 
	 * @param value
	 */
	public static void setFolderLocation(String value) {
		folderLocation = value;
	}

}
