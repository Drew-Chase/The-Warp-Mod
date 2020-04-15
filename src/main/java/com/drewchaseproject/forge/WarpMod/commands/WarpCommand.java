package com.drewchaseproject.forge.WarpMod.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.drewchaseproject.forge.WarpMod.WarpMod;
import com.drewchaseproject.forge.WarpMod.WarpMod.LogType;
import com.drewchaseproject.forge.WarpMod.Objects.Warp;
import com.drewchaseproject.forge.WarpMod.Objects.Warps;
import com.drewchaseproject.forge.WarpMod.commands.util.Teleport;
import com.drewchaseproject.forge.WarpMod.config.ConfigHandler;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import scala.reflect.internal.util.Origins.OneLine;

/**
 * Main Warp Command
 * 
 * @author Drew Chase
 *
 */
@SuppressWarnings("all")
public class WarpCommand implements ICommand {

//	public Map<Entry<EntityPlayer, String>, Entry<Entry<BlockPos, Entry<Float, Float>>, Integer>> warps = new HashMap<Entry<EntityPlayer, String>, Entry<Entry<BlockPos, Entry<Float, Float>>, Integer>>();
//	public Map<String, Entry<Entry<BlockPos, Entry<Float, Float>>, Integer>> public_warps = new HashMap<String, Entry<Entry<BlockPos, Entry<Float, Float>>, Integer>>();
	public Warps warps = new Warps(), public_warps = new Warps();

	public BufferedReader br;
	public BufferedWriter bw;
	public String FileName = "warps.conf";
	public String FileLocation = "config/Warps/";
	public WarpCommand instance;
	private EntityPlayer player = null;
	private String[] subcommands = new String[] { "set", "random", "map", "list", "help", "reload", "me", "remove", "rename", "invite", "accept", "spawn" };
	private final ArrayList<String> remove_text = new ArrayList<String>();

	/**
	 * Warp Command Constructor.
	 */
	public WarpCommand() {
		instance = this;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer)
			player = (EntityPlayer) sender;
		if (args.length != 0) {

			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
					getHelp();
				}
				if (args[0].equalsIgnoreCase("list")) {
					listWarps();
				}

				if (args[0].equalsIgnoreCase("map")) {
					mapWarps();
				}

				if (args[0].equalsIgnoreCase("spawn")) {
					warpToSpawn();
				}
				for (Warp warp : warps.getWarps()) {
					if (args[0].equalsIgnoreCase(warp.getName())) {
						warpTo(warp);
					}
				}

				if (args[0].equalsIgnoreCase("random")) {
					warpRandom(600);
				}
			}

			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("set")) {
					setWarp(args[1]);
				}

				if (args[0].equalsIgnoreCase("remove")) {
					remove(args[1]);
				}

				if (args[0].equalsIgnoreCase("random")) {
					int range = 0;
					try {
						range = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						sendMessage(TextFormatting.RED + args[1] + " Could NOT be Understood as a Whole Number");
					} catch (Exception e) {
						sendMessage(TextFormatting.RED + "Unknown Error when warping random with a range of " + args[1]);
					}
					if (range != 0)
						warpRandom(range);
				}

				if (args[0].equalsIgnoreCase("me")) {
					for (EntityPlayer player : getOnlinePlayers()) {
						if (args[1].equalsIgnoreCase(player.getDisplayNameString())) {
							warpTo(player);
						}
					}
				}
				for (EntityPlayer player : getOnlinePlayers()) {
					if (args[0].equalsIgnoreCase(player.getDisplayNameString()) && args[1].equalsIgnoreCase("me")) {
						warpToMe(player);
					}
				}

			}

			if (args.length == 3) {

				if (args[0].equalsIgnoreCase("invite")) {
					for (EntityPlayer player : getOnlinePlayers()) {
						if (args[2].equalsIgnoreCase(player.getDisplayNameString())) {
							invite(warps.getWarp(args[1]), player);
							break;
						}
					}
				}

				if (args[0].equalsIgnoreCase("rename")) {
					rename(args[1], args[2]);
				}
				if (args[0].equalsIgnoreCase("set")) {
					if (args[2].equalsIgnoreCase("-p")) {
						setPublicWarp(args[1]);
					}
				}
				if (args[0].equalsIgnoreCase("list")) {
					if (args[2].equalsIgnoreCase("-p")) {
						listPublicWarps();
					}
				}
				if (args[0].equalsIgnoreCase("map")) {
					if (args[2].equalsIgnoreCase("-p")) {
						mapPublicWarps();
					}
				}

				if (args[0].equalsIgnoreCase("remove")) {
					if (args[2].equalsIgnoreCase("-p")) {
						removePublic(args[1]);
					}
				}
			}

			if (args.length == 4) {
				if (args[0].equalsIgnoreCase("rename")) {
					if (args[3].equalsIgnoreCase("-p")) {
						renamePublic(args[1], args[2]);
					}
				}
			}

		} else {
			sendMessage(TextFormatting.GOLD + "Type /warp help");
		}
	}

	/**
	 * Registers all forms of the command
	 * 
	 * @param dispatcher
	 */
//	public void register(CommandDispatcher<CommandSource> dispatcher) {
//		dispatcher.register(Commands.literal("warp").then(Commands.literal("open").executes(context -> OpenGUI(context.getSource())))
//				.then(
//						Commands.literal("spawn").executes(context -> warpTo(context.getSource())))
//				.then(
//						Commands.literal("accept")
//								.executes(context -> AcceptWarp(context.getSource())))
//				.then(
//						Commands.literal("reload")
//								.then(Commands.literal("config")
//										.executes(context -> reloadConfig())))
//				.then(
//						Commands.argument("Warp Name", StringArgumentType.word())
//								.suggests(WARP_SUGGESTIONS)
//								.executes(context -> warpTo(context.getSource(), StringArgumentType.getString(context, "Warp Name"))))
//				.then(
//						Commands.literal("set")
//								.then(Commands.argument("Name", StringArgumentType.word())
//										.then(Commands.literal("-p")
//												.executes(context -> setPublicWarp(context.getSource(), StringArgumentType.getString(context, "Name"))))
//										.executes(context -> setWarp(context.getSource(), StringArgumentType.getString(context, "Name")))))
//				.then(
//						Commands.literal("list")
//								.then(Commands.literal("-p")
//										.executes(context -> listPublicWarps(context.getSource())))
//								.then(Commands.argument("PlayerName", StringArgumentType.greedyString())
//										.executes(context -> listWarps(context.getSource(), StringArgumentType.getString(context, "PlayerName"))))
//								.executes(context -> listWarps(context.getSource())))
//				.then(
//						Commands.literal("map")
//								.then(Commands.literal("-p")
//										.executes(context -> mapPublicWarps(context.getSource())))
//								.then(Commands.argument("PlayerName", StringArgumentType.greedyString())
//										.executes(context -> mapWarps(context.getSource(), StringArgumentType.getString(context, "PlayerName"))))
//								.executes(context -> mapWarps(context.getSource())))
//				.then(
//						Commands.literal("random")
//								.then(Commands.argument("range", IntegerArgumentType.integer())
//										.executes(context -> warpRandom(context.getSource(), IntegerArgumentType.getInteger(context, "range"))))
//								.executes(context -> warpRandom(context.getSource(), 600)))
//				.then(
//						Commands.argument("player", EntityArgument.player())
//								.then(Commands.literal("me")
//										.executes(context -> warpToMe(context.getSource(), EntityArgument.getPlayer(context, "player")))))
//				.then(
//						Commands.literal("me")
//								.then(Commands.argument("player", EntityArgument.player())
//										.executes(context -> warpTo(context.getSource(), EntityArgument.getPlayer(context, "player")))))
//				.then(
//						Commands.literal("remove")
//								.then(Commands.argument("Warp Name", StringArgumentType.greedyString())
//										.suggests(WARP_SUGGESTIONS)
//										.executes(context -> remove(context.getSource(), StringArgumentType.getString(context, "Warp Name")))))
//				.then(
//						Commands.literal("rename")
//								.then(Commands.argument("Old Warp Name", StringArgumentType.word())
//										.suggests(WARP_SUGGESTIONS)
//										.then(Commands.argument("New Warp Name", StringArgumentType.word())
//												.executes(context -> rename(context.getSource(), StringArgumentType.getString(context, "Old Warp Name"), StringArgumentType.getString(context, "New Warp Name"))))))
//				.then(
//						Commands.literal("invite")
//								.then(Commands.argument("Warp Name", StringArgumentType.word())
//										.suggests(WARP_SUGGESTIONS)
//										.then(Commands.argument("player", EntityArgument.player())
//												.executes(context -> invite(context.getSource(), StringArgumentType.getString(context, "Warp Name"), EntityArgument.getPlayer(context, "player"))))))
//
//		);
//
//	}

//	private int OpenGUI() {
//		ServerEntityPlayer player;
//		try {
//			player = source.asPlayer();
//		} catch (CommandSyntaxException e) {
//			player = null;
//		}
//
//		if (player != null) {
//			return 0;
//		}
//
//		return 1;
//	}

	private int AcceptWarp() {
		return 1;
	}

	private int reloadConfig() {
		ConfigHandler.readConfig();
		return 1;
	}

	public boolean isAllowed(String... errorMessage) {
		return isAllowed(false, errorMessage);
	}

	public boolean isAllowed(boolean global, String... errorMessage) {
		if (getPlayer().getServer().isSinglePlayer())
			return true;
		if (getPlayer() != null) {
			ConfigHandler.readConfig();
			if (!global) {
				if (ConfigHandler.areAllPlayersAllowed()) {
					return true;
				} else if (ConfigHandler.getAllowedPlayers().contains(getPlayer().getDisplayNameString())) {
					return true;
				}
				for (String s : ConfigHandler.getAllowedPlayers()) {
					if (s.equalsIgnoreCase(getPlayer().getDisplayNameString()))
						return true;
				}
			} else {
				if (ConfigHandler.areAllPlayersAllowedPublic()) {
					return true;
				} else if (ConfigHandler.getAllowedPublicPlayers().contains(getPlayer().getDisplayNameString())) {
					return true;
				}
				for (String s : ConfigHandler.getAllowedPublicPlayers()) {
					if (s.equalsIgnoreCase(getPlayer().getDisplayNameString()))
						return true;
				}
			}
		} else if (getPlayer() == null) {
			return true;
		}
		if (errorMessage.length > 0) {
			for (String text : errorMessage) {
				sendMessage(TextFormatting.RED + text);
			}
		}
		return false;
	}

	private int setPublicWarp(String name) {
		EntityPlayer player = getPlayer();
		name = name.toLowerCase();

		// Is player
		if (player != null && ConfigHandler.getPublicWarpsAllowed() && (ConfigHandler.areAllPlayersAllowedPublic() || ConfigHandler.getAllowedPublicPlayers().contains(player.getDisplayNameString()))) {
			if (!isRemote()) {
				if (isAllowed(true, "You do not have permissions to create public warps")) {
					importAllWarps(player);
					int x, y, z, dim;
					float pitch, yaw;
					String displayName = player.getDisplayNameString();
					x = player.getPosition().getX();
					y = player.getPosition().getY();
					z = player.getPosition().getZ();
					dim = player.getEntityWorld().provider.getDimension();
					pitch = player.prevRotationPitch;
					yaw = player.prevCameraYaw;
					BlockPos pos = new BlockPos(x, y, z);
					SimpleEntry<Float, Float> rot = new SimpleEntry<Float, Float>(yaw, pitch);
					SimpleEntry<BlockPos, Entry<Float, Float>> pos_rot = new SimpleEntry<BlockPos, Entry<Float, Float>>(pos, rot);
					boolean added = false;
					if (public_warps.getWarp(name) != null) {
						added = public_warps.addWarp(new Warp(name, pos, dim, player, yaw, pitch));
						sendMessage(TextFormatting.GOLD + "Public Warp Overwritten: " + TextFormatting.GREEN + name);
					} else {
						added = public_warps.addWarp(new Warp(name, pos, dim, player, yaw, pitch));
						sendMessage(TextFormatting.GOLD + "Public Warp Created: " + TextFormatting.GREEN + name);
					}
					added = public_warps.addWarp(new Warp(name, pos, dim, player, yaw, pitch));
					if (!added) {
						sendMessage(TextFormatting.RED + "Could not add Warp " + name);
					}
					exportPublicWarps();
				}
			}
		} else if (!ConfigHandler.getPublicWarpsAllowed()) {
			sendMessage(TextFormatting.RED + "Public Warps Aren't Allowed, Making Private One");
			setWarp(name);
		} else if (player != null && !(ConfigHandler.areAllPlayersAllowedPublic() || ConfigHandler.getAllowedPublicPlayers().contains(player.getDisplayNameString()))) {
			sendMessage(TextFormatting.RED + "You Are NOT Allowed to create public warps, Making Private One");
			setWarp(name);
		} else if (player == null) { // is console
			WarpMod.log(LogType.Warning, "Are you in a console!  This command has to be run as a player");
		}

		return 1;
	}

	/**
	 * Creates a warp with the players current information.
	 * 
	 * @param source
	 * @param name
	 * @return
	 */
	private int setWarp(String name) {
		EntityPlayer player = getPlayer();
		name = name.toLowerCase();
		// Is player
		if (player != null) {
			if (!isRemote()) {
				for (String value : subcommands) {
					if (name.equalsIgnoreCase(value)) {
						sendMessage(TextFormatting.RED + value + TextFormatting.LIGHT_PURPLE + " is a keyword used in TheWarpMod");
						return 1;
					}
				}
				if (isAllowed(false, "You do not have permissions to create warps")) {
					importAllWarps(player);
					int x, y, z, dim;
					float pitch, yaw;
					String displayName = player.getDisplayNameString();
					x = player.getPosition().getX();
					y = player.getPosition().getY();
					z = player.getPosition().getZ();
					dim = player.getEntityWorld().provider.getDimension();
					pitch = player.rotationPitch;
					yaw = player.rotationYaw;
					BlockPos pos = new BlockPos(x, y, z);
					boolean added = false;
					if (warps.getWarp(name) != null && warps.getWarp(name).getPlayer().equals(player)) {
						added = warps.addWarp(new Warp(name, pos, dim, player, yaw, pitch));
						sendMessage(TextFormatting.GOLD + "Warp Overwritten: " + TextFormatting.GREEN + name);
					} else if (warps.getWarp(name) == null) {
						added = warps.addWarp(new Warp(name, pos, dim, player, yaw, pitch));
						sendMessage(TextFormatting.GOLD + "Warp Created: " + TextFormatting.GREEN + name);
					}
					added = warps.addWarp(new Warp(name, pos, dim, player, yaw, pitch));
					if (!added)
						sendMessage(TextFormatting.RED + "Could not add Warp " + name);
					exportAllWarps(player);
				}
			}
		} else { // is console
			WarpMod.log(LogType.Warning, "Are you in a console!  This command has to be run as a player");
		}

		return 1;
	}

	/**
	 * Imports all Warps
	 * 
	 * @param player
	 * @return
	 */
	public List<String> importAllWarps(EntityPlayer player) {
		List<String> name = new ArrayList<String>();

		if (isAllowed(false))
			name.addAll(importWarps(player, 0));
		if (isAllowed(true))
			name.addAll(importPublicWarps());

		for (String s : name) {
			WarpMod.log(LogType.Debug, "Imported " + s + " for " + player.getDisplayNameString());
		}
		return name;
	}

	public void exportAllWarps(EntityPlayer player) {
		WarpMod.log(LogType.Debug, public_warps.toString());
		export(player);
		if (isAllowed(true))
			exportPublicWarps();
		importAllWarps(player);
		WarpMod.log(LogType.Debug, public_warps.toString());
	}

	public int warpToSpawn() {
		EntityPlayer player = getPlayer();
		if (player != null) {
			if (!isRemote()) {
				player = (EntityPlayer) player;
				double x = (double) player.world.getSpawnPoint().getX(), y = (double) player.world.getSpawnPoint().getY(), z = (double) player.world.getSpawnPoint().getZ();
				int dimension = 0;
				BlockPos oldPos = player.getPosition();
				int oldDim = player.getEntityWorld().provider.getDimension();
				float yaw = player.rotationYaw;
				float pitch = player.rotationPitch;
				Teleport.teleportToDimension(player, dimension, x, y, z, yaw, pitch);
				sendMessage(TextFormatting.GOLD + "Warped to: " + TextFormatting.GREEN + "Spawn");
				back(oldPos, yaw, pitch, oldDim, player);
			}
		}

		return 1;
	}

	/**
	 * Warps player to predefined warps.
	 * 
	 * @param name
	 * @param source
	 * @return
	 */
	public int warpTo(Warp warp) {
		EntityPlayer playerIn = getPlayer();
		if (playerIn != null) {
			if (!isRemote(playerIn)) {
				EntityPlayer player = (EntityPlayer) playerIn;
				importAllWarps(player);
				if (warp != null && isAllowed(false)) {
					if (warp.getPlayer().equals(player)) {
						double x = (double) warp.getX(), y = (double) warp.getY(), z = (double) warp.getZ();
						int dimension = warp.getDimension();
						BlockPos oldPos = player.getPosition();
						int oldDim = player.getEntityWorld().provider.getDimension();
						float yaw = warp.getYaw();
						float pitch = warp.getPitch();
						Teleport.teleportToDimension(player, dimension, x, y, z, yaw, pitch);
						sendMessage(TextFormatting.GOLD + "Warped to: " + TextFormatting.GREEN + warp.getName());
						back(oldPos, playerIn.prevCameraYaw, playerIn.prevRotationPitch, oldDim, playerIn);
					}
				} else if (public_warps.get(warp) != null && isAllowed(true)) {
					double x = (double) warp.getX(), y = (double) warp.getY(), z = (double) warp.getZ();
					int dimension = warp.getDimension();
					BlockPos oldPos = player.getPosition();
					int oldDim = player.getEntityWorld().provider.getDimension();
					float yaw = warp.getYaw();
					float pitch = warp.getPitch();
					Teleport.teleportToDimension(player, dimension, x, y, z, yaw, pitch);
					sendMessage(TextFormatting.GOLD + "Warped to: " + TextFormatting.GREEN + warp.getName());
					back(oldPos, playerIn.prevCameraYaw, playerIn.prevRotationPitch, oldDim, playerIn);
				} else {
					sendMessage(TextFormatting.RED + "Warp Not Found: " + TextFormatting.RED + warp.getName().toUpperCase());
				}
			}
		} else
			return 0;
		return 1;
	}

	/**
	 * Warps current player to remote player.
	 * 
	 * @param source
	 * @param playerTo
	 * @return
	 */
	public int warpTo(EntityPlayer playerTo) {
		EntityPlayer playerIn = getPlayer();

		int i = 0;
		for (EntityPlayer p : getOnlinePlayers()) {
			if (p.getDisplayNameString().equalsIgnoreCase(player.getDisplayNameString()))
				continue;
			else
				i++;
		}

		if (i == 0) {
			sendMessage(TextFormatting.LIGHT_PURPLE + "" + TextFormatting.UNDERLINE + TextFormatting.BOLD + "You Know You're all alone right?!?!?!?");
			return 1;
		}

		if (playerIn != null) {
			if (!isRemote(playerIn)) {
				EntityPlayer player = (EntityPlayer) playerIn;
				if (isAllowed(false, "You do not have permissions to use the warp mod")) {
					double x = (double) playerTo.getPosition().getX(), y = (double) playerTo.getPosition().getY(), z = (double) playerTo.getPosition().getZ();
					int dimension = playerTo.dimension;
					BlockPos oldPos = player.getPosition();
					int oldDim = player.getEntityWorld().provider.getDimension();
					float yaw = playerTo.prevCameraYaw;
					float pitch = playerTo.prevRotationPitch;
					String name = playerTo.getDisplayNameString();
					Teleport.teleportToDimension(player, dimension, x, y, z, yaw, pitch);
					sendMessage(TextFormatting.GOLD + "Warped to: " + TextFormatting.GREEN + name);
					back(oldPos, playerIn.cameraYaw, playerIn.prevRotationPitch, oldDim, playerIn);
				}
			}
		} else
			return 0;
		return 1;
	}

	public int warpToMe(EntityPlayer playerTo) {
		EntityPlayer player = getPlayer();
		int i = 0;
		for (EntityPlayer p : getOnlinePlayers()) {
			if (p.getDisplayNameString().equalsIgnoreCase(player.getDisplayNameString()))
				continue;
			else
				i++;
		}

		if (i == 0) {
			sendMessage(TextFormatting.LIGHT_PURPLE + "" + TextFormatting.UNDERLINE + TextFormatting.BOLD + "You Know You're all alone right?!?!?!?");
			return 1;
		}

		if (player != null && !isRemote()) {
			if (isAllowed(false, "You do not have permissions to use the warp mod")) {
				double x = (double) player.getPosition().getX(), y = (double) player.getPosition().getY(), z = (double) player.getPosition().getZ();
				int dimension = player.dimension;
				BlockPos oldPos = playerTo.getPosition();
				int oldDim = playerTo.dimension;
				float yaw = player.prevCameraYaw, pitch = player.prevRotationPitch;
				String name = player.getDisplayNameString();
				Teleport.teleportToDimension(playerTo, dimension, x, y, z, yaw, pitch);
				back(oldPos, playerTo.prevCameraYaw, playerTo.prevRotationPitch, oldDim, playerTo);
				sendMessage(playerTo, TextFormatting.RED + "You are being forced into a locked room with " + TextFormatting.LIGHT_PURPLE + name);
			}
		}

		return 1;
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
			if (isAllowed(false, "You do not have permissions to use the warp mod")) {
				try {

					importAllWarps(player);
					warps.addWarp(new Warp("back", pos, dimension, player, yaw, pitch));
					sendMessage(TextFormatting.GREEN + "Back Warped Saved: type " + TextFormatting.GOLD + "\"/warp back\"" + TextFormatting.GREEN + " to go back");
					exportAllWarps(player);
				} catch (Exception e) {
					WarpMod.log(LogType.Error, "ERROR: " + e.getMessage() + "\n" + e.getStackTrace());
				}
			}
		}
	}

	/**
	 * Imports the warps from file to a variable. Returns a Array of warp names.
	 * 
	 * @param player
	 * @return
	 */
	private List<String> importWarps(EntityPlayer player, int count) {
		List<String> warps = new ArrayList<String>();

		if (!isRemote()) {
			FileReader file;
			FileName = player.getDisplayNameString() + "_" + player.getServer().getFolderName() + ".conf";
			try {
				file = new FileReader(FileLocation + FileName);
				br = new BufferedReader(file);
			} catch (FileNotFoundException e) {
				if (count < 3) {

					try {
						TextWriter("", false, player.getDisplayNameString());
						WarpMod.log(LogType.Debug, "File Not Found Creating it");
						return warps;
					} catch (IOException e1) {
						e1.printStackTrace();
						return warps;
					}
				} else
					return importWarps(player, count++);
			} catch (Exception e) {
				e.printStackTrace();
				return warps;
			}

			try {
				String line = br.readLine();
				while (line != null) {
					try {
						String[] text = line.split(":");
						String name = text[0];
						char[] abcs = ("ABCDFGHIJKLMNOPQRSTUVWXYZ" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase() + "!@#$%^&*()_=+?<>,/\\[]{};:\"\'|~` ").toCharArray();
						for (char char_value : abcs) {
							String value = char_value + "";
							text[2] = text[2].replace(value, "");
							text[3] = text[3].replace(value, "");
							text[4] = text[4].replace(value, "");
							text[5] = text[5].replace(value, "");
							text[6] = text[6].replace(value, "");
							text[7] = text[7].replace(value, "");
						}
						BlockPos pos = new BlockPos(Double.parseDouble(text[2]), Double.parseDouble(text[3]), Double.parseDouble(text[4]));
						int dimension = Integer.parseInt(text[5]);
						float yaw = Float.parseFloat(text[6]);
						float pitch = Float.parseFloat(text[7]);
						Warp warp = new Warp(name, pos, dimension, player, yaw, pitch);
						if (this.warps.addWarp(warp)) {
							warps.add(name);
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
						WarpMod.log(LogType.Error, "This Returned NULL: MESSAGE->" + e.getMessage() + " | CAUSE->" + e.getCause());
						e.printStackTrace();
						break;
					} catch (ArrayIndexOutOfBoundsException e) {

					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
					line = br.readLine();
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
				WarpMod.log(LogType.Debug, "\n\n\nFile Doesn't Exist And Couln't be Created!!!\n\n\n");
			} catch (NullPointerException e) {
				e.printStackTrace();
				WarpMod.log(LogType.Debug, "\n\n\n " + e.getLocalizedMessage() + "-->> was null for some reason \n\n\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return warps;

	}

	private List<String> importPublicWarps() {
		List<String> warps = new ArrayList<String>();

		FileReader file;
		FileName = "public_" + getPlayer().getServer().getFolderName() + ".conf";
		try {
			file = new FileReader(FileLocation + FileName);
			br = new BufferedReader(file);
		} catch (FileNotFoundException e) {
			try {
				FileWriter write = new FileWriter(FileLocation + FileName);
				WarpMod.log(LogType.Debug, "File Not Found Creating it");
				return warps;
			} catch (IOException e1) {
				e1.printStackTrace();
				return warps;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return warps;
		}

		try {
			String line = br.readLine();
			while (line != null) {
				try {
					String[] text = line.split(":");
					char[] abcs = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase() + "!@#$%^&*()_=+?<>,/\\[]{};:\"\'|~` ").toCharArray();
					for (char char_value : abcs) {
						String value = char_value + "";
						text[2] = text[2].replace(value, "");
						text[3] = text[3].replace(value, "");
						text[4] = text[4].replace(value, "");
						text[5] = text[5].replace(value, "");
						text[6] = text[6].replace(value, "");
						text[7] = text[7].replace(value, "");
					}
					String name = text[0];
					BlockPos pos = new BlockPos(Double.parseDouble(text[2].replace("X", "").replace("Y", "").replace("Z", "").replaceAll("World", "")), Double.parseDouble(text[3].replace("X", "").replace("Y", "").replace("Z", "").replaceAll("World", "")), Double.parseDouble(text[4].replace("X", "").replace("Y", "").replace("Z", "").replaceAll("World", "")));
					int dimension = Integer.parseInt(text[5].replaceAll("World", "").replaceAll("Rotation", "").replaceAll("Yaw", "").replaceAll("Pitch", "").replace(" ", ""));
					float yaw = Float.parseFloat(text[6].replaceAll("World", "").replaceAll("Pitch", "").replaceAll("Yaw", "").replace(" ", ""));
					float pitch = Float.parseFloat(text[7].replaceAll("World", "").replaceAll("Yaw", "").replaceAll("Pitch", "").replace(" ", ""));
					Warp warp = new Warp(name, pos, dimension, player, yaw, pitch);
//						if (public_warps.get(warp) == null)
					if (public_warps.addWarp(warp))
						warps.add(name);
				} catch (NullPointerException e) {
					WarpMod.log(LogType.Error, "This Returned NULL: MESSAGE->" + e.getMessage() + " | CAUSE->" + e.getCause());
					e.printStackTrace();
					break;
				} catch (ArrayIndexOutOfBoundsException e) {

				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			WarpMod.log(LogType.Debug, "\n\n\nFile Doesn't Exist And Couln't be Created!!!\n\n\n");
		} catch (NullPointerException e) {
			e.printStackTrace();
			WarpMod.log(LogType.Debug, "\n\n\n " + e.getLocalizedMessage() + "-->> was null for some reason \n\n\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (getPlayer() != null) {
			if (!warps.isEmpty()) {
				remove_text.clear();
				remove_text.addAll(warps);
				remove_text.add("*");
			}
		}
		return warps;

	}

	private int listWarps(String playerName) {
		List<String> names = new ArrayList<String>();
		EntityPlayer player = null;
		for (EntityPlayer playerList : warps.getPlayers()) {
			if (playerList.getDisplayName().equals(playerName))
				player = playerList;
		}
		// Is Console
		if (player != null) {
			importAllWarps(player);
			if (warps.getWarps().isEmpty() && public_warps.getWarps().isEmpty()) {
				sendMessage(TextFormatting.RED + "No Warps");
				return 1;
			}

			if (warps.isEmpty(player)) {
				sendMessage(TextFormatting.RED + "No Warps");
				return 1;
			}

			warps.getWarps(player).forEach((n) -> names.add(n.getName()));
			public_warps.getWarps().forEach((n) -> names.add(n.getName()));

			String value = names.toString().replace("[", "").replace("]", "");
			sendMessage(null, TextFormatting.GOLD + value);
		} else if (player == null) {
			WarpMod.log(LogType.Error, "Player Doesn't Exist or Is not Currently Loaded.");
			return 0;
		}

		return 1;
	}

	/**
	 * Creates a Human-Readable list of warps
	 * 
	 * @param source
	 * @return
	 */
	private int listWarps() {
		EntityPlayer player = getPlayer();
		List<String> names = new ArrayList<String>();

		// Is Player
		if (player != null) {
			if (!isRemote()) {
				if (isAllowed(false, "You do not have permissions to use the warp mod")) {
					importAllWarps(player);
					if (warps.isEmpty(player)) {
						sendMessage(TextFormatting.RED + "No Warps");
						return 1;
					}
					warps.getWarps(player).forEach((n) -> names.add(n.getName()));
					public_warps.getWarps().forEach((n) -> names.add(n.getName()));

					String value = names.toString().replace("[", "").replace("]", "");
					sendMessage(TextFormatting.GOLD + value);
				}
			} else
				return 0;
		} else {
			// Is Console
		}

		return 1;
	}

	private int listPublicWarps() {
		EntityPlayer player = getPlayer();
		List<String> names = new ArrayList<String>();

		// Is Player
		if (player != null) {
			if (!isRemote()) {
				if (isAllowed(false, "You do not have permissions to use public warps")) {
					importAllWarps(player);
					if (public_warps.getWarps().isEmpty()) {
						sendMessage(TextFormatting.RED + "No Warps");
						return 1;
					}

					for (Warp warp : public_warps.getWarps()) {
						names.add(warp.getName());
					}

					String value = names.toString().replace("[", "").replace("]", "");
					sendMessage(TextFormatting.GOLD + value);
				}
			} else
				return 0;
		} else
			return 0;

		return 1;
	}

	public int mapWarps(String playerName) {
		List<String> names = new ArrayList<String>();

		EntityPlayer player = null;
		for (EntityPlayer players : warps.getPlayers()) {
			if (players.getDisplayName().equals(playerName))
				player = players;
		}

		// Is Console
		if (player != null) {

			String value = "";
			try {
				importAllWarps(player);
				if (warps.getWarps().isEmpty() && public_warps.getWarps().isEmpty()) {
					sendMessage(TextFormatting.RED + "No Warps");
					return 1;
				}

				for (Warp warp : warps.getWarps()) {
					if (warp.getPlayer().equals(player)) {
						float x = warp.getX(), y = warp.getY(), z = warp.getZ();
						int dimensionId = warp.getDimension();
						String name = warp.getName();
						value += TextFormatting.GREEN + name + TextFormatting.GOLD + ":{X: " + TextFormatting.GREEN + x + TextFormatting.GOLD + ", Y: " + TextFormatting.GREEN + y + TextFormatting.GOLD + ", Z: " + TextFormatting.GREEN + z + TextFormatting.GOLD + ", Dimension: " + TextFormatting.GREEN + dimensionId + TextFormatting.GOLD + "}*";
						names.add(value);
					}
				}

				for (Warp warp : public_warps.getWarps()) {
					float x = warp.getX(), y = warp.getY(), z = warp.getZ();
					int dimensionId = warp.getDimension();
					String name = warp.getName();

					value += TextFormatting.GREEN + name + TextFormatting.GOLD + ":{X: " + TextFormatting.GREEN + x + TextFormatting.GOLD + ", Y: " + TextFormatting.GREEN + y + TextFormatting.GOLD + ", Z: " + TextFormatting.GREEN + z + TextFormatting.GOLD + ", Dimension: " + TextFormatting.GREEN + dimensionId + TextFormatting.GOLD + "}*";
					names.add(value);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			value = value.replace("*", "\n");
			sendMessage(TextFormatting.GOLD + value);

		} else if (player == null) {
			WarpMod.log(LogType.Error, "Player Doesn't Exist or Is not Currently Loaded.");
			return 0;
		}
		return 1;
	}

	/**
	 * Creates a verbose list of warps.
	 * 
	 * @param source
	 * @return
	 */
	public int mapWarps() {
		EntityPlayer player = getPlayer();
		List<String> names = new ArrayList<String>();

		// Is Player
		if (player != null) {
			if (!isRemote()) {
				if (isAllowed(false, "You do not have permissions to use the warp mod")) {
					String value = "";
					try {
						importAllWarps(player);
						if (warps.getWarps().isEmpty() && public_warps.getWarps().isEmpty()) {
							sendMessage(TextFormatting.RED + "No Warps");
							return 1;
						}

						for (Warp warp : warps.getWarps()) {
							if (warp.getPlayer().equals(player)) {
								float x = warp.getX(), y = warp.getY(), z = warp.getZ();
								int dimensionId = warp.getDimension();
								String name = warp.getName();
								value += TextFormatting.GREEN + name + TextFormatting.GOLD + ":{X: " + TextFormatting.GREEN + x + TextFormatting.GOLD + ", Y: " + TextFormatting.GREEN + y + TextFormatting.GOLD + ", Z: " + TextFormatting.GREEN + z + TextFormatting.GOLD + ", Dimension: " + TextFormatting.GREEN + dimensionId + TextFormatting.GOLD + "}*";
								names.add(value);
							}
						}

						for (Warp warp : public_warps.getWarps()) {
							float x = warp.getX(), y = warp.getY(), z = warp.getZ();
							int dimensionId = warp.getDimension();
							String name = warp.getName();

							value += TextFormatting.GREEN + name + TextFormatting.GOLD + ":{X: " + TextFormatting.GREEN + x + TextFormatting.GOLD + ", Y: " + TextFormatting.GREEN + y + TextFormatting.GOLD + ", Z: " + TextFormatting.GREEN + z + TextFormatting.GOLD + ", Dimension: " + TextFormatting.GREEN + dimensionId + TextFormatting.GOLD + "}*";
							names.add(value);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

					value = value.replace("*", "\n");
					sendMessage(TextFormatting.GOLD + value);
				}
			} else {
				sendMessage(TextFormatting.RED + "Request was remote");
				return 0;
			}
		} else {
			sendMessage(TextFormatting.RED + "Player doesn't exist... You don't exist...");
			return 0;
		}

		return 1;
	}

	public int mapPublicWarps() {
		EntityPlayer player = getPlayer();
		List<String> names = new ArrayList<String>();

		// Is Player
		if (player != null) {
			if (!isRemote()) {
				if (isAllowed(false, "You do not have permissions to use public warps")) {
					String value = "";
					try {
						importAllWarps(player);
						if (public_warps.getWarps().isEmpty()) {
							sendMessage(TextFormatting.RED + "No Warps");
							return 1;
						}

						for (Warp warp : public_warps.getWarps()) {
							float x = warp.getX(), y = warp.getY(), z = warp.getZ();
							int dimensionId = warp.getDimension();
							String name = warp.getName();

							value += TextFormatting.GREEN + name + TextFormatting.GOLD + ":{X: " + TextFormatting.GREEN + x + TextFormatting.GOLD + ", Y: " + TextFormatting.GREEN + y + TextFormatting.GOLD + ", Z: " + TextFormatting.GREEN + z + TextFormatting.GOLD + ", Dimension: " + TextFormatting.GREEN + dimensionId + TextFormatting.GOLD + "}*";
							names.add(value);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					value = value.replace("*", "\n");
					sendMessage(TextFormatting.GOLD + value);
				}
			} else {
				sendMessage(TextFormatting.RED + "Request was remote");
				return 0;
			}
		} else {
			sendMessage(TextFormatting.RED + "Player doesn't exist... You don't exist...");
			return 0;
		}

		return 1;
	}

	/**
	 * Gets all online players.
	 * 
	 * @param source
	 * @return
	 */
	private List<EntityPlayer> getOnlinePlayers() {
		List<EntityPlayer> players = new ArrayList<EntityPlayer>();
		try {
			for (String name : getPlayer().getServer().getOnlinePlayerNames()) {
				players.add((EntityPlayer) getPlayer().getEntityWorld().getPlayerEntityByName(name));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return players;
	}

	public int remove(String name) {
		EntityPlayer player = getPlayer();
		if (player != null && !isRemote()) {
			if (isAllowed(false, "You do not have permissions to use the warp mod")) {
				importAllWarps(player);
				if (name.equals("*")) {
					for (Warp warp : warps.getWarps()) {
						if (warp.getPlayer().equals(player))
							warps.removeWarp(warp);
					}
					sendMessage(TextFormatting.LIGHT_PURPLE + "Removed all Privately Saved Warps");
					exportAllWarps(player);
					return 1;
				} else if (name.endsWith("*") && !(name.replace("*", "").isEmpty())) {

					for (Warp warp : warps.getWarps()) {
						name = name.replace("*", "");
						if (warp.getName().contains(name)) {
							warps.removeWarp(warp);

							exportAllWarps(player);
							sendMessage(TextFormatting.LIGHT_PURPLE + "Removed " + warp.getName().toUpperCase() + " from your Privately Saved Warps");
						}
					}

					return 1;
				}

				for (Warp warp : warps.getWarps()) {
					if (warp.getName().equals(name)) {
						warps.removeWarp(warp);

						sendMessage(TextFormatting.LIGHT_PURPLE + "Removed " + TextFormatting.GOLD + warp.getName().toUpperCase() + TextFormatting.LIGHT_PURPLE + " from the Privately Saved Warps");
						exportAllWarps(player);
						return 1;
					}
				}

				if (removePublic(name) == 0) {
					sendMessage(TextFormatting.LIGHT_PURPLE + name.toUpperCase() + TextFormatting.RED + " was not found!");
				}
			}
		} else if (isRemote()) {
			WarpMod.log(LogType.Warning, "Player is Remote.");
			return 0;
		} else if (player == null) {
			sendMessage(TextFormatting.RED + "Player is null");
			WarpMod.log(LogType.Error, "Player is null");
			return 0;
		}
		return 1;
	}

	public int removePublic(String name) {
		EntityPlayer player = getPlayer();
		if (player != null && !isRemote()) {
			if (isAllowed(true, "You do not have permissions to use public warps")) {
				importAllWarps(player);
				if (name.equals("*")) {
					public_warps.getWarps().clear();
					sendMessage(TextFormatting.LIGHT_PURPLE + "Removed all Public Warps");
					exportAllWarps(player);
					return 1;
				} else if (name.endsWith("*") && !(name.replace("*", "").isEmpty())) {

					for (Warp warp : public_warps.getWarps()) {
						name = name.replace("*", "");
						if (warp.getName().contains("*")) {
							public_warps.removeWarp(warp);

							exportAllWarps(player);
							sendMessage(TextFormatting.LIGHT_PURPLE + "Removed " + TextFormatting.GOLD + warp.getName().toUpperCase() + TextFormatting.LIGHT_PURPLE + " from the Publicly Saved Warps");
						}
					}

					sendMessage(TextFormatting.LIGHT_PURPLE + "No Warps were Removed");
					return 1;
				}

				for (Warp warp : public_warps.getWarps()) {
					if (warp.getName().equalsIgnoreCase(name)) {
						public_warps.removeWarp(warp);

						sendMessage(TextFormatting.LIGHT_PURPLE + "Removed " + TextFormatting.GOLD + warp.getName().toUpperCase() + TextFormatting.LIGHT_PURPLE + " from the Publicly Saved Warps");
						exportAllWarps(player);
						return 1;
					}
				}

			}
		} else if (isRemote()) {
			WarpMod.log(LogType.Warning, "Player is Remote.");
			return 0;
		} else if (player == null) {
			sendMessage(TextFormatting.RED + "Player is null");
			WarpMod.log(LogType.Error, "Player is null");
			return 0;
		}
		sendMessage(TextFormatting.LIGHT_PURPLE + name.toUpperCase() + TextFormatting.RED + " was not found!");
		return 0;
	}

	/**
	 * Renames warps.
	 * 
	 * @author Drew Chase
	 * @param oldName
	 * @param newName
	 */
	public int rename(String oldName, String newName) {
		EntityPlayer playerIn = getPlayer();
		if (newName.isEmpty()) {
			sendMessage(TextFormatting.RED + "The New Name Cannot be Blank");
			return 0;
		}
		if (playerIn != null) {
			if (!isRemote(playerIn)) {
				if (isAllowed(false, "You do not have permissions to use the warp mod")) {
					try {
						if (warps.getWarp(oldName) != null && warps.getWarp(oldName).getPlayer().equals(player)) {
							warps.renameWarp(warps.getWarp(oldName), newName);
							exportAllWarps(player);
							sendMessage(TextFormatting.GOLD + oldName + TextFormatting.GREEN + " renamed to " + TextFormatting.GOLD + newName);
						} else {
							renamePublic(oldName, newName);
						}
					} catch (Exception e) {
						sendMessage(playerIn, TextFormatting.RED + "An Error Has Occurred in the Rename Method");
						e.printStackTrace();
						return 0;
					}
				}
			} else
				return 0;
		} else
			return 0;
		return 1;
	}

	public int renamePublic(String oldName, String newName) {
		EntityPlayer playerIn = getPlayer();
		if (playerIn != null) {
			if (!isRemote(playerIn)) {
				if (isAllowed(false, "You do not have permissions to edit public warps")) {
					try {
						Warp warp = public_warps.getWarp(oldName);
						if (warp != null) {
							public_warps.renameWarp(warp, newName);
						}

						exportAllWarps(player);
						sendMessage(TextFormatting.GOLD + oldName + TextFormatting.GREEN + " renamed to " + TextFormatting.GOLD + newName);
					} catch (Exception e) {
						sendMessage(playerIn, TextFormatting.RED + "An Error Has Occurred in the Rename Method");
						e.printStackTrace();
						return 0;
					}
				}
			} else
				return 0;
		} else
			return 0;
		return 1;
	}

	/**
	 * @author Drew Chase
	 * @return a human readable list of all commands
	 */
	public int getHelp() {
		List<String> s = new ArrayList<String>();
		s.add("[-p] if public warp");
		s.add("/warp set <name> [-p]");
		s.add("/warp invite <name> <to-player>");
		s.add("/warp map [-p]");
		s.add("/warp list [-p]");
		s.add("/warp <name>");
		s.add("/warp random <max-distance(optional)>");
		s.add("/warp remove <name> [-p]");
		s.add("/warp remove *");
		s.add("/warp rename <old name> <new name>");
		s.add("/warp <player> me");
		s.add("/warp me <player>");
		s.add("Private Warps will always be exacuted first.");
		s.add("Public Warps have to be removed one by one and can't use the * to remove all");
		s.add("\n---Config Section---");
		s.add("/warp-config add <config-handler> <player> ~ Adds a Player to a permission group");
		s.add("/warp-config set <config-handler> <true/false> ~ Sets the value of the selected config");
		s.add("/warp-config get <config-handler> ~ Gets the current value of selected config");
		String value = TextFormatting.LIGHT_PURPLE + "-----The Warp Mod HELP-----\n";
		if (player != null && !isRemote()) {
			for (String i : s) {
				value += TextFormatting.GOLD + i + "\n";
			}
		}

		sendMessage(TextFormatting.AQUA + value);
		return 1;
	}

	/**
	 * Safely warps a player randomly
	 * 
	 * @param range
	 * @param playerIn
	 * @author Drew Chase
	 */
	public int warpRandom(int range) {
		EntityPlayer player = getPlayer();

		if (player != null) {
			if (!isRemote()) {
				if (isAllowed(false, "You do not have permissions to use the warp mod")) {
					Random ran = new Random();
					int var = ran.nextInt(range);
					double x, y, z;
					x = player.getPosition().getX() + var;
					y = player.world.getHeight();
					z = player.getPosition().getZ() + var;
					BlockPos pos = new BlockPos(x, y, z);
					// Makes sure that the player is on solid ground

					while (player.world.getBlockState(pos).getBlock().equals(Blocks.AIR) || player.world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
						if (y <= 10) {
							sendMessage(TextFormatting.RED + "There is no safe place to land");
							return 1;
						}
						y--;
						pos = new BlockPos(x, y, z);
					}
					if (player.world.getBlockState(pos).getBlock().equals(Blocks.WATER) || player.world.getBlockState(pos).getBlock().equals(Blocks.LAVA) || player.world.getBlockState(pos).getBlock().equals(Blocks.AIR) || player.world.getBlockState(pos).getBlock().equals(Blocks.AIR)) {
						if (range < 20)
							warpRandom(600);
						else
							warpRandom(range / 2);
						return 1;
					}
					// Increases the players y so that they are above ground
					y += 2;
					back(player.getPosition(), player.cameraYaw, player.prevRotationPitch, player.getEntityWorld().provider.getDimension(), player);
					float yaw = player.cameraYaw;
					float pitch = player.prevRotationPitch;
					Teleport.teleportToDimension(player, player.getEntityWorld().provider.getDimension(), x, y, z, yaw, pitch);
					sendMessage(TextFormatting.AQUA + "Warping " + var + " blocks away!");
				}
			} else
				return 0;
		} else
			return 0;
		return 1;
	}

	/**
	 * Copies a warp from your list to another players
	 * 
	 * @author Drew Chase
	 * @param name
	 * @param playerIn
	 */
	public int invite(Warp warp, EntityPlayer playerIn) {
		EntityPlayer playerOut = getPlayer();

		int i = 0;
		for (EntityPlayer p : getOnlinePlayers()) {
			if (p.getDisplayNameString().equalsIgnoreCase(player.getDisplayNameString()))
				continue;
			else
				i++;
		}

		if (i == 0) {
			sendMessage(TextFormatting.LIGHT_PURPLE + "" + TextFormatting.UNDERLINE + TextFormatting.BOLD + "You Know You're all alone right?!?!?!?");
			return 1;
		}

		if (playerOut != null) {
			importAllWarps(playerOut);
			if (!isRemote(playerOut)) {
				if (isAllowed(false, "You do not have permissions to use the warp mod")) {
					if (warp != null && warp.getPlayer().equals(playerOut)) {
						Warp copy = warp;
						copy.setPlayer(playerIn);
						if (!(warps.get(warp) != null && warps.get(warp).getPlayer().equals(playerOut)))
							warps.addWarp(copy);
						else {
							copy.setName(warp.getName() + "_from_" + playerOut.getDisplayNameString());
							warps.addWarp(copy);
						}
					}

					exportAllWarps(playerIn);
					sendMessage(playerIn, TextFormatting.GREEN + "You've Been Invited to " + TextFormatting.GOLD + warp.getName() + TextFormatting.GREEN + " from " + TextFormatting.GOLD + playerOut.getDisplayNameString());
					sendMessage(playerOut, TextFormatting.GOLD + "Successfully Invited " + TextFormatting.GREEN + playerIn.getDisplayNameString() + TextFormatting.GOLD + " to " + TextFormatting.GREEN + warp.getName());
				}
			} else
				return 0;
		} else
			return 0;
		return 1;
	}

	/**
	 * Checks if the request is remote
	 * 
	 * @param player
	 * @return
	 */
	public boolean isRemote() {
		try {
			return getPlayer().getEntityWorld().isRemote;
		} catch (NullPointerException e) {
			return false;
		}
	}

	public boolean isRemote(EntityPlayer player) {
		try {
			return player.getEntityWorld().isRemote;
		} catch (NullPointerException e) {
			return false;
		}
	}

	/**
	 * Exports the warps to file
	 * 
	 * @param player
	 */
	public void export(EntityPlayer player) {
		if (!isRemote()) {
			if (isAllowed(false)) {
				int index = 0;
				// Counting how many warps the player has
				for (Warp warp : warps.getWarps()) {
					if (warp.getPlayer().equals(player)) {
						index++;
					}
				}
				String[] warpString = new String[index];
				index = 0;
				for (Warp warp : warps.getWarps()) {
					if (warp.getPlayer().equals(player)) {
						int x = warp.getX(), y = warp.getY(), z = warp.getZ();
						int dimension = warp.getDimension();
						String name = warp.getName();
						float yaw = warp.getYaw();
						float pitch = warp.getPitch();
						warpString[index] = name + ": X:" + x + " Y:" + y + " Z:" + z + " World:" + dimension + " Yaw:" + yaw + " Pitch:" + pitch;
						index++;
					}
				}
				try {
					TextWriter(Arrays.toString(warpString).replace(", ", "\n").replace("[", "").replace("]", ""), true, player.getDisplayNameString());
				} catch (IOException e) {
					sendMessage(TextFormatting.RED + "COULD NOT CREATE OR CONNECT TO WARP CONFIG");
					WarpMod.log(LogType.Error, "ERROR: " + e.getMessage());
					e.printStackTrace();
				} catch (Exception e) {
					sendMessage(TextFormatting.RED + "ERROR: " + e.getMessage());
					WarpMod.log(LogType.Error, "ERROR: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	public void exportPublicWarps() {
		if (!isRemote()) {
			int index = public_warps.getWarps().size();
			String[] warpString = new String[index];
			index = 0;
			for (Warp warp : public_warps.getWarps()) {
				String name = warp.getName();
				int x = warp.getX(), y = warp.getY(), z = warp.getZ();
				int dimension = warp.getDimension();
				float yaw = warp.getYaw();
				float pitch = warp.getPitch();
				warpString[index] = name + ": X:" + x + " Y:" + y + " Z:" + z + " World:" + dimension + " Yaw:" + yaw + " Pitch:" + pitch;
				index++;
			}
			try {
				TextWriter(Arrays.toString(warpString).replace(", ", "\n").replace("[", "").replace("]", ""), true, "public");
			} catch (IOException e) {
				sendMessage(TextFormatting.RED + "COULD NOT CREATE OR CONNECT TO PUBLIC WARP CONFIG");
				WarpMod.log(LogType.Error, "ERROR: " + e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				sendMessage(TextFormatting.RED + "ERROR: " + e.getMessage());
				WarpMod.log(LogType.Error, "ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Writes to config
	 * 
	 * @param text
	 * @param newLine
	 * @param player
	 * @throws IOException
	 */
	public void TextWriter(String text, boolean newLine, String name) throws IOException {
		if (!isRemote()) {
			File f = new File(FileLocation);

			try {
				if (f.mkdirs()) {
					WarpMod.log(LogType.Debug, "Warp File Created in " + f.getAbsolutePath());
				}
			} catch (Exception e) {
				WarpMod.log(LogType.Error, "Couldn't Create File");
				e.printStackTrace();
			}
			FileName = name + "_" + getPlayer().getServer().getFolderName() + ".conf";

			try {
				bw = new BufferedWriter(new FileWriter(FileLocation + FileName, false));
				if (!text.isEmpty())
					bw.write(text);
				if (newLine)
					bw.newLine();
				bw.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (bw != null)
					bw.close();
			}
		}

	}

//	public void setPlayer(EntityPlayer player) {
//		ConfigHandler.readConfig();
//		this.player = (EntityPlayer) player;
//	}

	public EntityPlayer getPlayer() {
		return this.player;
	}

	public void setPlayer(EntityPlayer value) {
		player = value;
	}

	/**
	 * Sends message to current player
	 * 
	 * @param player
	 * @param message
	 */
	private void sendMessage(Object message) {
		WarpMod.sendMessage(getPlayer(), message);
	}

	private void sendMessage(EntityPlayer player, Object message) {
		WarpMod.sendMessage(player, message);
	}

	@Override
	public int compareTo(ICommand arg0) {
		return 0;
	}

	@Override
	public String getName() {
		return "warp";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "";
	}

	@Override
	public List<String> getAliases() {
		return new ArrayList<String>();
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		if (sender instanceof EntityPlayer)
			setPlayer((EntityPlayer) sender);
		List<String> value = new ArrayList<String>();
		if (args.length == 1) {
			if (args[0].startsWith("set"))
				value.add("set");
			if (args[0].startsWith("random"))
				value.add("random");
			if (args[0].startsWith("rename"))
				value.add("rename");
			if (args[0].startsWith("spawn"))
				value.add("spawn");
			if (args[0].startsWith("remove"))
				value.add("remove");
			if (args[0].startsWith("me"))
				value.add("me");
			for (EntityPlayer player : getOnlinePlayers())
				if (args[0].startsWith(player.getDisplayNameString()))
					value.add(player.getDisplayNameString());
			for (Warp warp : warps.getWarps())
				if (args[0].startsWith(warp.getName()))
					value.add(warp.getName());
		}

		if (args.length == 2) {

			if (args[0].equalsIgnoreCase("me"))
				for (EntityPlayer player : getOnlinePlayers())
					if (args[1].startsWith(player.getDisplayNameString()))
						value.add(player.getDisplayNameString());

			if (args[0].equalsIgnoreCase("rename"))
				for (Warp warp : warps.getWarps())
					if (args[1].startsWith(warp.getName()))
						value.add(warp.getName());

			if (args[0].equalsIgnoreCase("remove"))
				for (Warp warp : warps.getWarps())
					if (args[1].startsWith(warp.getName()))
						value.add(warp.getName());

			for (EntityPlayer player : getOnlinePlayers())
				if (args[0].equalsIgnoreCase(player.getDisplayNameString()))
					value.add("me");

			if (args[0].equalsIgnoreCase("invite"))
				for (Warp warp : warps.getWarps())
					if (args[1].startsWith(warp.getName()))
						value.add(warp.getName());

		}
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("invite")) {
				for (Warp warp : warps.getWarps()) {
					if (args[1].equalsIgnoreCase(warp.getName())) {
						for (EntityPlayer player : getOnlinePlayers()) {
							if (args[1].startsWith(player.getDisplayNameString()))
								value.add(player.getDisplayNameString());
						}
					}
				}
			}
		}

		return value;

	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

}