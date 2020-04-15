package com.drewchaseproject.forge.WarpMod.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.drewchaseproject.forge.WarpMod.WarpMod;
import com.drewchaseproject.forge.WarpMod.WarpMod.LogType;

/**
 * Custom (non-fml) ConfigHandler
 * 
 * @author Drew Chase
 *
 */
@SuppressWarnings("all")
public class ConfigHandler {
	private static BufferedReader br;
	private static BufferedWriter bw;
	private static String fileName;
	private static boolean publicwarps = false, debug = false;
	private static List<String> public_players = new ArrayList<String>(), all_players = new ArrayList<String>(), all_config_players = new ArrayList<String>();

	public static void writeConfig() {
		if (getFileName().isEmpty())
			setFileName("settings.conf");
		File f = new File(getFolderName());
		try {
			if (f.mkdirs()) {
				WarpMod.log(LogType.Debug, "Default Warp Settings File was created in " + f.getAbsolutePath());
			}
		} catch (Exception e) {
			WarpMod.log(LogType.Error, "Couldn't Create Default Warp Settings File");
			e.printStackTrace();
		}

		try {
			bw = new BufferedWriter(new FileWriter(getFolderName() + getFileName(), false));
			for (String s : config()) {
				bw.write(s);
			}
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

//		readConfig();

	}

	public static List<String> config() {
		List<String> l = new ArrayList<>();
		String value = "";
		l.add("#All Configurations can be controlled with-in the game using the \"/warp-config\" Command\n#You can add your username below under \"players allowed to change config:\" section or in server console with the \"/warp-config add config-editors LittleBilly101\"\n\n");
		l.add("#Sets if public warps are allowed.\n#Public warps are created by the allowed player typing /warp set NAME_OF_WARP -p\n");
		l.add("\npublic-warps-allowed:" + getPublicWarpsAllowed());
		l.add("\n#Sets players that are allowed to create public warps.\n#Ex: (allowed-players-public:[\"LittleBilly101\", \"LittleBilly102\"])\n#You can also use * to signify all players are allowed\n#Ex (allowed-players:[\"*\"])\n");

		value = "";
		for (String s : getAllowedPublicPlayers()) {
			if (getAllowedPublicPlayers().indexOf(s) == (getAllowedPublicPlayers().size() - 1))
				value += "\"" + s + "\"";
			else
				value += "\"" + s + "\",";
		}
		l.add("\nallowed-players-public:[" + value + "]\n\n");
		l.add("#Sets if the Mod verbosly states all of its moves\n");
		l.add("\ndebug-mode:" + getDebugMode() + "\n");
		l.add("\n\n#Are All Players Allowed to use the Mod\n#Ex: (allowed-players:[\"LittleBilly101\", \"LittleBilly102\"]\n#You can also use * to signify all players allowed\n#Ex (allowed-players:[\"*\"])\n");

		value = "";
		for (String s : getAllowedPlayers()) {
			if (getAllowedPlayers().indexOf(s) == (getAllowedPlayers().size() - 1))
				value += "\"" + s + "\"";
			else
				value += "\"" + s + "\",";
		}
		l.add("\nallowed-players:[" + value + "]");

		value = "";
		for (String s : getAllowedConfigPlayers()) {
			if (getAllowedConfigPlayers().indexOf(s) == (getAllowedConfigPlayers().size() - 1))
				value += "\"" + s + "\"";
			else
				value += "\"" + s + "\",";
		}
		l.add("\n\n#Players allowed to change the config within game using /warp-config command\n#The * Wildcard can NOT be used in this situation\n#Ex (players allowed to change config:[\"LittleBilly101]\"");
		l.add("\nplayers allowed to change config:[" + value + "]\n");

		return l;
	}

	public static void readConfig() {
		if (getFileName() == null || getFileName().isEmpty())
			setFileName("settings.conf");
		if (!new File(getFolderName() + getFileName()).exists())
			writeConfig();
		try {
			br = new BufferedReader(new FileReader(getFolderName() + getFileName()));
			WarpMod.log(LogType.Debug, "Loading Config " + fileName);
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
				} else if (line.startsWith("public-warps-allowed:")) {
					text = line.replaceAll("public-warps-allowed:", "").replace(" ", "");
					if (text.equalsIgnoreCase("true"))
						setPublicWarpsAllowed(true);
					else
						setPublicWarpsAllowed(false);
				} else if (line.startsWith("allowed-players-public:")) {
					text = line.replaceAll("allowed-players-public:", "");
					if (text.equalsIgnoreCase("[]")) {
						line = br.readLine();
						continue;
					} else {
						text = text.replace("[", "").replace("]", "").replace("\"", "").replace(" ", "");
						String name[] = text.split(",");
						for (String j : name) {
							addAllowedPublicPlayer(j);
						}
					}

				} else if (line.startsWith("debug-mode:")) {
					text = line.replaceAll("debug-mode:", "");
					if (text.equalsIgnoreCase("true"))
						setDebugMode(true);
					else
						setDebugMode(false);

				} else if (line.startsWith("allowed-players:")) {
					text = line.replaceAll("allowed-players:", "");
					if (text.equalsIgnoreCase("[]")) {
						line = br.readLine();
						continue;
					} else {
						text = text.replace("[", "").replace("]", "").replace(" ", "").replace("\"", "");
						String name[] = text.split(",");
						for (String j : name) {
							addAllowedPlayers(j);
						}
					}
				} else if (line.startsWith("players allowed to change config:")) {
					text = line.replaceAll("players allowed to change config:", "");
					if (text.equalsIgnoreCase("[]")) {
						line = br.readLine();
						continue;
					} else {
						text = text.replace("[", "").replace("]", "").replace(" ", "").replace("\"", "");
						String name[] = text.split(",");
						for (String j : name) {
							addAllowedConfigPlayers(j);
						}
					}

				}

				line = br.readLine();
			}
			writeConfig();
		} catch (FileNotFoundException e) {
			writeConfig();
			e.printStackTrace();
		} catch (Exception e) {
			writeConfig();
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

	public static boolean getDebugMode() {
		return debug;
	}

	public static void setDebugMode(boolean value) {
		debug = value;
	}

	public static void setPublicWarpsAllowed(boolean value) {
		publicwarps = value;
	}

	public static boolean getPublicWarpsAllowed() {
		return publicwarps;
	}

	public static boolean areAllPlayersAllowedPublic() {
		for (String s : getAllowedPublicPlayers()) {
			if (s.equalsIgnoreCase("*"))
				return true;
		}
		return false;
	}

	public static void addAllowedPublicPlayer(String value) {
		public_players.add(value);
		WarpMod.log(LogType.Debug, value + " is Allowed To Make Public Warps");
	}

	public static List<String> getAllowedPublicPlayers() {
		return public_players;
	}

	public static List<String> getAllowedPlayers() {
		return all_players;
	}

	public static List<String> getAllowedConfigPlayers() {
		return all_config_players;
	}

	public static void addAllowedPlayers(String value) {
		if (!all_players.contains(value))
			all_players.add(value);
		WarpMod.log(LogType.Debug, value + " is Allowed To Use The Warp Mod");
	}

	public static void addAllowedConfigPlayers(String value) {
		if (!all_config_players.contains(value))
			all_config_players.add(value);
		WarpMod.log(LogType.Debug, value + " is Allowed To Use The Warp Mod");
	}

	public static boolean areAllPlayersAllowed() {
		for (String s : getAllowedPlayers()) {
			if (s.equalsIgnoreCase("*"))
				return true;
		}
		return false;
	}

	public static String getFolderName() {
		return "config/Warps/";
	}

	public static String getFileName() {
		return fileName;
	}

	public static void setFileName(String value) {
		fileName = value;
	}

	public static void removeAllowedConfigPlayers(String value) {
		if (all_config_players.contains(value))
			all_config_players.remove(all_config_players.indexOf(value));
	}

	public static void removeAllowedPublicPlayer(String value) {
		if (all_players.contains(value))
			public_players.remove(public_players.indexOf(value));
	}

	public static void removeAllowedPlayers(String value) {
		all_players.remove(all_players.indexOf(value));
	}

	public static void clearAllowedPlayers() {
		all_players.clear();
	}

	public static void clearAllowedPublicPlayers() {
		public_players.clear();
	}

}
