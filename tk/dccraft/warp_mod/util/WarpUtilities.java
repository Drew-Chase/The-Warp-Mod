package tk.dccraft.warp_mod.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import tk.dccraft.warp_mod.WarpMod;
import tk.dccraft.warp_mod.commands.util.Teleport;

/**
 * 
 * @author Drew Chase
 * @Description The backbone of the warp command
 *
 */
@SuppressWarnings("all")
public class WarpUtilities {
	/** The different names that can call the mod */
	public List<String> aliases = Arrays.asList("warp_mod", "warp", "tpx");

	public List<EntityPlayerMP> allPlayers = new ArrayList<EntityPlayerMP>();
	/** A complex hash of all data in every warp */
	public Map<Entry<EntityPlayerMP, String>, Entry<Entry<BlockPos, Entry<Float, Float>>, Integer>> warps = new HashMap<Entry<EntityPlayerMP, String>, Entry<Entry<BlockPos, Entry<Float, Float>>, Integer>>();
	public BufferedReader br;
	public BufferedWriter bw;
	public String FileName = "warps.conf", FileLocation = "config/Warps/";
	public EntityPlayer player;
	public List<String> cmdList = new ArrayList<>();

	public WarpUtilities instance = this;

	/**
	 * @deprecated
	 * @param playerIn
	 * @throws IOException
	 * @author Drew Chase
	 * @Decription Checks the player in on first join -- Unneeded!
	 */
	public void checkPlayer(EntityPlayer playerIn) throws IOException {
		FileReader file;
		FileName = "players.conf";
		String name = playerIn.getDisplayNameString();
		try {
			file = new FileReader(FileLocation + FileName);
			br = new BufferedReader(file);
		} catch (FileNotFoundException e) {
			WriteToFile(FileName, FileLocation, "", false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			String line = br.readLine();
			while (line != null) {
				try {
					if (line.equalsIgnoreCase(name)) {
						return;
					}
				} catch (NullPointerException e) {
					WarpMod.instance.consoleMessage("This Returned NULL: MESSAGE->" + e.getMessage() + " | CAUSE->" + e.getCause());
					e.printStackTrace();
					break;
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		WriteToFile(FileName, FileLocation, name, true);

	}

	/**
	 * Creates the warp players warp file
	 * 
	 * @author Drew Chase
	 * @param fileName
	 * @param fileLocation
	 * @param text
	 * @param append
	 * @throws IOException
	 */
	public void WriteToFile(String fileName, String fileLocation, String text, boolean append) throws IOException {
		File f = new File(fileLocation);

		try {
			if (f.mkdirs()) {
				WarpMod.instance.consoleMessage("Warp File Created in " + f.getAbsolutePath());
			}
		} catch (Exception e) {
			WarpMod.instance.consoleMessage("Couldn't Create File");
			e.printStackTrace();
		}
		try {
			bw = new BufferedWriter(new FileWriter(fileLocation + fileName, append));
			if (!text.isEmpty())
				bw.write(text);
			bw.newLine();
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				bw.close();
			}
		}
	}

	/**
	 * Grabs a warp from another player.
	 * 
	 * @author Drew Chase
	 * @param name
	 * @param playerIn
	 */
	public void grab(String name, EntityPlayerMP playerIn) {
		EntityPlayerMP playerOut = (EntityPlayerMP) getPlayer();
		int dim = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerIn, name)).getValue();
		float yaw = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerIn, name)).getKey().getValue().getKey();
		float pitch = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerIn, name)).getKey().getValue().getValue();
		Map.Entry<Float, Float> rot = new AbstractMap.SimpleEntry<Float, Float>(yaw, pitch);
		Map.Entry<BlockPos, Entry<Float, Float>> pos = new SimpleEntry<BlockPos, Entry<Float, Float>>(warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerIn, name)).getKey().getKey(), warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerIn, name)).getKey().getValue());
		warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerOut, name), new SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos, dim));
		export(playerOut);
		sendMessage(TextFormatting.GREEN + "You Grabbed " + TextFormatting.GOLD + name + TextFormatting.GREEN + " from " + TextFormatting.GOLD + playerIn.getDisplayNameString());
	}

	/**
	 * Renames warps.
	 * 
	 * @author Drew Chase
	 * @param oldName
	 * @param newName
	 */

	public void rename(String oldName, String newName) {
		try {
			EntityPlayerMP player = (EntityPlayerMP) getPlayer();
			int oldDim = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, oldName)).getValue();
			float yaw = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, oldName)).getKey().getValue().getKey();
			float pitch = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, oldName)).getKey().getValue().getValue();
			Map.Entry<BlockPos, Entry<Float, Float>> pos = new SimpleEntry<BlockPos, Entry<Float, Float>>(warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, oldName)).getKey().getKey(), warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, oldName)).getKey().getValue());
			warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, newName), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos, oldDim));
			warps.remove(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, oldName));

			export(getPlayer());
			sendMessage(TextFormatting.GOLD + oldName + TextFormatting.GREEN + " renamed to " + TextFormatting.GOLD + newName);
		} catch (Exception e) {
			sendMessage(TextFormatting.RED + "An Error Has Occurred in the Rename Method");
			e.printStackTrace();
		}
	}

	/**
	 * @author Drew Chase
	 * @return a human readable list of all commands
	 */
	public List<String> getHelp() {
		List<String> s = new ArrayList<String>();
		s.add("/warp set <name>");
		s.add("/warp invite <name> <to-player>");
		s.add("/warp list");
		s.add("/warp map");
		s.add("/warp list");
		s.add("/warp <name>");
		s.add("/warp random <max-distance(optional)>");
		s.add("/warp remove <name>");
		s.add("/warp remove *");
		s.add("/warp rename <old name> <new name>");
		s.add("/warp <player> me");
		s.add("/warp me <player>");
		return s;
	}

	/**
	 * Copies a warp from your list to another players
	 * 
	 * @author Drew Chase
	 * @param name
	 * @param playerIn
	 */
	public void invite(String name, EntityPlayerMP playerIn) {
		EntityPlayerMP playerOut = (EntityPlayerMP) getPlayer();
		float yaw = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerOut, name)).getKey().getValue().getKey();
		float pitch = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerOut, name)).getKey().getValue().getValue();

		Map.Entry<BlockPos, Entry<Float, Float>> pos = new SimpleEntry<BlockPos, Entry<Float, Float>>(warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerOut, name)).getKey().getKey(), warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerOut, name)).getKey().getValue());

		int dim = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>((EntityPlayerMP) getPlayer(), name)).getValue();
		if (warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerIn, name)) != null)
			warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerIn, name), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos, dim));
		else
			warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerIn, name + "_from_" + playerOut.getDisplayNameString()), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos, dim));
		export(playerIn);
		sendMessage(TextFormatting.GREEN + "You've Been Invited to " + TextFormatting.GOLD + name + TextFormatting.GREEN + " from " + TextFormatting.GOLD + playerOut.getDisplayNameString(), playerIn);
	}

	/**
	 * @author Drew Chase
	 * @return a list of all online players
	 */
	public List<EntityPlayerMP> getOnlinePlayers() {
		return getWorld().getMinecraftServer().getPlayerList().getPlayers();
	}

	/**
	 * @author Drew Chase
	 * @return the world instance (not dimension)
	 */
	public World getWorld() {
		return getPlayer().getEntityWorld();
	}

	/**
	 * Sends a message to the player
	 * 
	 * @param message
	 * @author Drew Chase
	 */
	public void sendMessage(Object message) {
		getPlayer().sendMessage(new TextComponentString(message + ""));
	}

	/**
	 * Sends a message to a specific player
	 * 
	 * @param string
	 * @param playerIn
	 * @author Drew Chase
	 */
	public void sendMessage(Object string, EntityPlayerMP playerIn) {
		playerIn.sendMessage(new TextComponentString(string + ""));
	}

	/**
	 * Safely warps a player randomly
	 * 
	 * @param range
	 * @param playerIn
	 * @author Drew Chase
	 */
	public void warpRandom(int range, EntityPlayerMP... playerIn) {
		Random ran = new Random();
		int var = ran.nextInt(range);
		double x, y, z;
		x = getPlayer().getPosition().getX() + var;
		y = getPlayer().world.getHeight();
		z = getPlayer().getPosition().getZ() + var;
		BlockPos pos = new BlockPos(x, y, z);
		// Makes sure that the player is on solid ground
		if (getPlayer().world.getBlockState(pos).getBlock().equals(Blocks.WATER) || getPlayer().world.getBlockState(pos).getBlock().equals(Blocks.LAVA))
			warpRandom(range, playerIn);

		while (getPlayer().world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
			if (y <= 10)
				warpRandom(range, playerIn);
			y--;
			pos = new BlockPos(x, y, z);
		}
		// Increases the players y so that they are above ground
		y += 2;
		back(getPlayer().getPosition(), getPlayer().cameraYaw, getPlayer().cameraPitch, getPlayer().getEntityWorld().provider.getDimension(), getPlayer());
		float yaw = getPlayer().cameraYaw;
		float pitch = getPlayer().cameraPitch;
		Teleport.teleportToDimension(getPlayer(), getPlayer().getEntityWorld().provider.getDimension(), x, y, z, yaw, pitch);
		sendMessage(TextFormatting.AQUA + "Warping " + var + " blocks away!");
	}

	/**
	 * 
	 * @return if the instance is occurring locally
	 * @author Drew Chase
	 */
	public boolean isRemote() {
		if (getPlayer() != null)
			return getPlayer().getEntityWorld().isRemote;
		return true;
	}

	/**
	 * @return the player instance
	 * @author Drew Chase
	 */
	public EntityPlayer getPlayer() {
		return player;
	}

	/**
	 * Sets the player instance
	 * 
	 * @param player
	 * @author Drew Chase
	 */
	public void setPlayer(EntityPlayer player) {
		this.player = player;
		WarpMod.instance.consoleMessage("Player set as " + player.getDisplayNameString());
	}

	/**
	 * Warps the player to the specified warp
	 * 
	 * @param name
	 * @param playerIn
	 * @author Drew Chase
	 */
	public void warpTo(String name, EntityPlayer playerIn) {
		if (!isRemote()) {
			EntityPlayerMP player = (EntityPlayerMP) playerIn;
			Entry map = new AbstractMap.SimpleEntry<EntityPlayerMP, String>((EntityPlayerMP) getPlayer(), name);
			if (warps.containsKey(map)) {
				double x = (double) warps.get(map).getKey().getKey().getX(), y = (double) warps.get(map).getKey().getKey().getY(), z = (double) warps.get(map).getKey().getKey().getZ();
				int dimension = warps.get(map).getValue();
				BlockPos oldPos = player.getPosition();
				int oldDim = player.getEntityWorld().provider.getDimension();
				float yaw = warps.get(map).getKey().getValue().getKey();
				float pitch = warps.get(map).getKey().getValue().getValue();
				Teleport.teleportToDimension(player, dimension, x, y, z, yaw, pitch);
				sendMessage(TextFormatting.GOLD + "Warped to: " + name);
				back(oldPos, playerIn.cameraYaw, playerIn.cameraPitch, oldDim, playerIn);
			} else {
				sendMessage(TextFormatting.RED + "Warp Not Found: " + name.toUpperCase());
			}
		}
	}

	/**
	 * Creates a back warp when the player warps or dies.
	 * 
	 * @param pos
	 * @param yaw
	 * @param pitch
	 * @param dimension
	 * @param player
	 * @author Drew Chase
	 */
	public void back(BlockPos pos, float yaw, float pitch, int dimension, EntityPlayer player) {
		if (!isRemote()) {
			SimpleEntry<Float, Float> rot = new SimpleEntry<Float, Float>(yaw, pitch);
			SimpleEntry<BlockPos, Entry<Float, Float>> pos_rot = new SimpleEntry<BlockPos, Entry<Float, Float>>(pos, rot);
			this.warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>((EntityPlayerMP) player, "back"), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos_rot, dimension));
			sendMessage(TextFormatting.GREEN + "Back Warped Saved: type " + TextFormatting.GOLD + "\"/warp back\"" + TextFormatting.GREEN + " to go back");
			export(player);
		}
	}

	/**
	 * adds a command to the help roaster
	 * 
	 * @param cmd
	 * @author Drew Chase
	 */
	public void addCommand(String... cmd) {
		for (String s : cmd) {
			cmdList.add(s);
		}
	}

	/**
	 * 
	 * @return a computer readable list of commands
	 * @author Drew Chase
	 */
	public List<String> getCommands() {
		return cmdList;
	}

	/**
	 * Exports the player warp information to file
	 * 
	 * @param player
	 * @author Drew Chase
	 */
	public void export(EntityPlayer player) {
		if (!isRemote()) {
			int index = 0;
			// Counting how many warps the player has
			for (Entry<EntityPlayerMP, String> name : warps.keySet()) {
				if (name.getKey().equals(player)) {
					index++;
				}
			}
			String[] warpString = new String[index];
			index = 0;
			for (Entry<EntityPlayerMP, String> name : warps.keySet()) {
				if (name.getKey().equals(player)) {
					int x = warps.get(name).getKey().getKey().getX(), y = warps.get(name).getKey().getKey().getY(), z = warps.get(name).getKey().getKey().getZ();
					int dimension = warps.get(name).getValue();

					float yaw = warps.get(name).getKey().getValue().getKey();
					float pitch = warps.get(name).getKey().getValue().getValue();
					warpString[index] = name.getValue() + ": X:" + x + " Y:" + y + " Z:" + z + " World:" + dimension + " Yaw:" + yaw + " Pitch:" + pitch;
					index++;
				}
			}
			try {
				TextWriter(Arrays.toString(warpString).replace(", ", "\n").replace("[", "").replace("]", ""), true, player);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Removes a warp from a players list
	 * 
	 * @param name
	 * @param player
	 * @author Drew Chase
	 */
	public void removeWarp(String name, EntityPlayer player) {
		warps.remove(new AbstractMap.SimpleEntry<EntityPlayerMP, String>((EntityPlayerMP) player, name));
		sendMessage(TextFormatting.GOLD + "Warp Removed: " + name);
		export(player);
	}

	/**
	 * Adds a warp to a players list based on player position and name given
	 * 
	 * @param name
	 * @param playerIn
	 * @author Drew Chase
	 */
	public void createWarp(String name, EntityPlayer playerIn) {
		name = name.toLowerCase();
		if (!isRemote()) {
			try {
				EntityPlayerMP player = (EntityPlayerMP) playerIn;
				BlockPos pos = new BlockPos(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
				int dimension = player.getEntityWorld().provider.getDimension();
				SimpleEntry<Float, Float> rot = new SimpleEntry<Float, Float>(player.cameraYaw, player.cameraPitch);
				SimpleEntry<BlockPos, Entry<Float, Float>> pos_rot = new SimpleEntry<BlockPos, Entry<Float, Float>>(pos, rot);
				if (warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, name)) != null) {
					warps.replace(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, name), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos_rot, dimension));
					sendMessage(TextFormatting.GOLD + "Warp Overwritten: " + name);
				} else {
					this.warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, name), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos_rot, dimension));
					sendMessage(TextFormatting.GOLD + "Warp Created: " + name);
				}
				export(playerIn);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates the file that contains the players warp information
	 * 
	 * @param text
	 * @param newLine
	 * @param player
	 * @throws IOException
	 * @author Drew Chase
	 */
	public void TextWriter(String text, boolean newLine, EntityPlayer player) throws IOException {
		if (!isRemote()) {
			File f = new File(FileLocation);

			try {
				if (f.mkdirs()) {
					WarpMod.instance.consoleMessage("Warp File Created in " + f.getAbsolutePath());
				}
			} catch (Exception e) {
				WarpMod.instance.consoleMessage("Couldn't Create File");
				e.printStackTrace();
			}
			FileName = player.getDisplayNameString() + "_" + player.getServer().getFolderName() + ".conf";

			try {
				bw = new BufferedWriter(new FileWriter(FileLocation + FileName, false));
				if (!text.isEmpty())
					bw.write(text);
				if (newLine)
					bw.newLine();
				bw.flush();
			} finally {
				if (bw != null) {
					bw.close();
				}
			}
		}

	}

	/**
	 * A list of warps with more information
	 * 
	 * @param args
	 */
	public void Map(String[] args) {

		if (warps == null)
			getWarps(player);
		if (getWarps(player).isEmpty()) {
			sendMessage(TextFormatting.GOLD + "No Warps Saved");
			return;
		}

		getWarps(player);

		if (args.length == 2) {
			EntityPlayerMP playerIn;
			for (EntityPlayerMP p : getOnlinePlayers()) {
				if (args[1].equalsIgnoreCase(p.getDisplayNameString())) {
					playerIn = p;
					if (getWarps(playerIn).isEmpty()) {
						sendMessage(TextFormatting.GREEN + playerIn.getDisplayNameString() + TextFormatting.GOLD + " doesn't have any warps saved".toUpperCase());
						return;
					}
					for (Entry<EntityPlayerMP, String> name : warps.keySet()) {
						if (args[1].equalsIgnoreCase(name.getValue()) && name.getKey().equals(playerIn)) {
							BlockPos pos = warps.get(name).getKey().getKey();
							int dim = warps.get(name).getValue();
							String dimension = DimensionManager.getProvider(dim).getDimensionType().getName() + "(" + dim + ")";
							float yaw = warps.get(name).getKey().getValue().getKey();
							float pitch = warps.get(name).getKey().getValue().getValue();
							sendMessage(TextFormatting.GOLD + name.getValue() + ":" + TextFormatting.RED + "{" + TextFormatting.GREEN + " X:" + pos.getX() + ", Y:" + pos.getY() + ", Z:" + pos.getZ() + ", World: " + dimension + ", Yaw: " + yaw + ", Pitch: " + pitch + TextFormatting.RED + "}");
							return;
						}
					}
				}
			}
			for (Entry<EntityPlayerMP, String> name : warps.keySet()) {
				if (args[1].equalsIgnoreCase(name.getValue()) && name.getKey().equals(player)) {
					BlockPos pos = warps.get(name).getKey().getKey();
					int dim = warps.get(name).getValue();
					String dimension = DimensionManager.getProvider(dim).getDimensionType().getName() + "(" + dim + ")";
					float yaw = warps.get(name).getKey().getValue().getKey();
					float pitch = warps.get(name).getKey().getValue().getValue();
					sendMessage(TextFormatting.GOLD + name.getValue() + ":" + TextFormatting.RED + "{" + TextFormatting.GREEN + " X:" + pos.getX() + ", Y:" + pos.getY() + ", Z:" + pos.getZ() + ", World: " + dimension + ", Yaw: " + yaw + ", Pitch: " + pitch + TextFormatting.RED + "}");
					return;
				}
			}
		}

		for (Entry<EntityPlayerMP, String> name : warps.keySet()) {
			if (name.getKey().equals(player)) {
				try {
					BlockPos pos = warps.get(name).getKey().getKey();
					int dim = warps.get(name).getValue();
					String dimension = DimensionManager.getProvider(dim).getDimensionType().getName() + "(" + dim + ")";
					String rot = "Rotation: " + "(" + warps.get(name).getKey().getValue() + ")";
					float yaw = warps.get(name).getKey().getValue().getKey();
					float pitch = warps.get(name).getKey().getValue().getValue();
					sendMessage(TextFormatting.GOLD + name.getValue() + ":" + TextFormatting.RED + "{" + TextFormatting.GREEN + " X:" + pos.getX() + ", Y:" + pos.getY() + ", Z:" + pos.getZ() + ", World: " + dimension + ", Yaw: " + yaw + ", Pitch: " + pitch + TextFormatting.RED + "}");
					export(player);
				} catch (NullPointerException e) {
					getWarps(player);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return;

	}

	/**
	 * 
	 * @param player
	 * @return a computer readable list of all warps.
	 * @author Drew Chase
	 */
	public List<String> getWarps(EntityPlayer player) {
		if (!isRemote()) {
			List<String> warps = new ArrayList<String>();
			FileReader file;
			FileName = player.getDisplayNameString() + "_" + getPlayer().getServer().getFolderName() + ".conf";
			try {
				file = new FileReader(FileLocation + FileName);
				br = new BufferedReader(file);
			} catch (FileNotFoundException e) {
				try {
					TextWriter("", false, player);
					return Arrays.asList("No Warps Saved");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				String line = br.readLine();
				while (line != null) {
					try {
						String[] text = line.split(":");
						BlockPos pos = new BlockPos(Double.parseDouble(text[2].replace("X", "").replace("Y", "").replace("Z", "").replaceAll("World", "")), Double.parseDouble(text[3].replace("X", "").replace("Y", "").replace("Z", "").replaceAll("World", "")), Double.parseDouble(text[4].replace("X", "").replace("Y", "").replace("Z", "").replaceAll("World", "")));
						int dimension = Integer.parseInt(text[5].replaceAll("World", "").replaceAll("Rotation", "").replaceAll("Yaw", "").replaceAll("Pitch", "").replace(" ", ""));
						float yaw = Float.parseFloat(text[6].replaceAll("World", "").replaceAll("Pitch", "").replaceAll("Yaw", "").replace(" ", ""));
						float pitch = Float.parseFloat(text[7].replaceAll("World", "").replaceAll("Yaw", "").replaceAll("Pitch", "").replace(" ", ""));
						SimpleEntry<Float, Float> rot = new SimpleEntry<Float, Float>(yaw, pitch);
						SimpleEntry<BlockPos, Entry<Float, Float>> pos_rot = new SimpleEntry<BlockPos, Entry<Float, Float>>(pos, rot);
						this.warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>((EntityPlayerMP) player, text[0].replace(":", "")), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos_rot, dimension));
						warps.add(text[0]);
					} catch (NullPointerException e) {
						WarpMod.instance.consoleMessage("This Returned NULL: MESSAGE->" + e.getMessage() + " | CAUSE->" + e.getCause());
						e.printStackTrace();
						break;
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
					line = br.readLine();
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return warps;
		}
		return Arrays.asList("World is Remote");
	}

	public void forceImport(EntityPlayer player) {

		if (!isRemote()) {
			List<String> warps = new ArrayList<String>();
			FileReader file;
			FileName = player.getDisplayNameString() + "_" + getPlayer().getServer().getFolderName() + ".conf";
			try {
				file = new FileReader(FileLocation + FileName);
				br = new BufferedReader(file);
			} catch (FileNotFoundException e) {
				try {
					TextWriter("", false, player);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				String line = br.readLine();
				while (line != null) {
					try {
						String[] text = line.split(":");
						BlockPos pos = new BlockPos(Double.parseDouble(text[2].replace("X", "").replace("Y", "").replace("Z", "").replaceAll("World", "")), Double.parseDouble(text[3].replace("X", "").replace("Y", "").replace("Z", "").replaceAll("World", "")), Double.parseDouble(text[4].replace("X", "").replace("Y", "").replace("Z", "").replaceAll("World", "")));
						int dimension = Integer.parseInt(text[5].replaceAll("World", "").replaceAll("Rotation", "").replaceAll("Yaw", "").replaceAll("Pitch", "").replace(" ", ""));
						float yaw = Float.parseFloat(text[6].replaceAll("World", "").replaceAll("Pitch", "").replaceAll("Yaw", "").replace(" ", ""));
						float pitch = Float.parseFloat(text[7].replaceAll("World", "").replaceAll("Yaw", "").replaceAll("Pitch", "").replace(" ", ""));
						SimpleEntry<Float, Float> rot = new SimpleEntry<Float, Float>(yaw, pitch);
						SimpleEntry<BlockPos, Entry<Float, Float>> pos_rot = new SimpleEntry<BlockPos, Entry<Float, Float>>(pos, rot);
						this.warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>((EntityPlayerMP) player, text[0].replace(":", "")), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos_rot, dimension));
						warps.add(text[0]);
						sendMessage(text[0] + " added");
						WarpMod.instance.consoleMessage(text[0] + " added");
					} catch (NullPointerException e) {
						WarpMod.instance.consoleMessage("This Returned NULL: MESSAGE->" + e.getMessage() + " | CAUSE->" + e.getCause());
						e.printStackTrace();
						break;
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
					line = br.readLine();
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			export(player);
		}

	}

}
