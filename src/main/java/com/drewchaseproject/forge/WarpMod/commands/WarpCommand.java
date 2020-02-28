package com.drewchaseproject.forge.WarpMod.commands;

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

import com.drewchaseproject.forge.WarpMod.WarpMod;
import com.drewchaseproject.forge.WarpMod.commands.util.Teleport;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

/**
 * Main Warp Command
 * 
 * @author Drew Chase
 *
 */
public final class WarpCommand {

	public Map<Entry<EntityPlayerMP, String>, Entry<Entry<BlockPos, Entry<Float, Float>>, Integer>> warps = new HashMap<Entry<EntityPlayerMP, String>, Entry<Entry<BlockPos, Entry<Float, Float>>, Integer>>();
	public BufferedReader br;
	public BufferedWriter bw;
	public String FileName = "warps.conf";
	public String FileLocation = "config/Warps/";
	public WarpCommand instance;
	private EntityPlayerMP player;
	private final ArrayList<String> remove_text = new ArrayList<String>();
	private final SuggestionProvider<CommandSource> WARP_SUGGESTIONS = (context, builder) -> ISuggestionProvider.suggest(importWarps(context.getSource().asPlayer()), builder);
	private SuggestionProvider<CommandSource> WARP_REMOVE_SUGGESTIONS;

	/**
	 * Warp Command Constructor.
	 */
	public WarpCommand() {
		instance = this;
		if (getPlayer() != null) {
			remove_text.addAll(importWarps(getPlayer()));
			remove_text.add("*");
			WARP_REMOVE_SUGGESTIONS = (context, builder) -> ISuggestionProvider.suggest(remove_text, builder);
		}
	}

	/**
	 * Registers all forms of the command
	 * 
	 * @param dispatcher
	 */
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("warp")
				.then(
						Commands.argument("Warp Name", StringArgumentType.word())
								.suggests(WARP_SUGGESTIONS)
								.executes(context -> warpTo(context.getSource(), StringArgumentType.getString(context, "Warp Name"))))
				.then(
						Commands.literal("set")
								.then(Commands.argument("Name", StringArgumentType.word())
										.executes(context -> setWarp(context.getSource(), StringArgumentType.getString(context, "Name")))))
				.then(
						Commands.literal("list")
								.executes(context -> listWarps(context.getSource())))
				.then(
						Commands.literal("map")
								.executes(context -> Map(context.getSource())))
				.then(
						Commands.literal("random")
								.then(Commands.argument("range", IntegerArgumentType.integer())
										.executes(context -> warpRandom(context.getSource(), IntegerArgumentType.getInteger(context, "range"))))
								.executes(context -> warpRandom(context.getSource(), 600)))
				.then(
						Commands.argument("player", EntityArgument.singlePlayer())
								.then(Commands.literal("me")
										.executes(context -> warpToMe(context.getSource(), EntityArgument.getOnePlayer(context, "player")))))
				.then(
						Commands.literal("me")
								.then(Commands.argument("player", EntityArgument.singlePlayer())
										.executes(context -> warpTo(context.getSource(), EntityArgument.getOnePlayer(context, "player")))))
				.then(
						Commands.literal("remove")
								.then(Commands.argument("Warp Name", StringArgumentType.greedyString())
										.suggests(WARP_SUGGESTIONS)
										.executes(context -> remove(context.getSource(), StringArgumentType.getString(context, "Warp Name")))))
				.then(
						Commands.literal("rename")
								.then(Commands.argument("Old Warp Name", StringArgumentType.word())
										.suggests(WARP_SUGGESTIONS)
										.then(Commands.argument("New Warp Name", StringArgumentType.word())
												.executes(context -> rename(context.getSource(), StringArgumentType.getString(context, "Old Warp Name"), StringArgumentType.getString(context, "New Warp Name"))))))
				.then(
						Commands.literal("invite")
								.then(Commands.argument("Warp Name", StringArgumentType.word())
										.suggests(WARP_SUGGESTIONS)
										.then(Commands.argument("player", EntityArgument.singlePlayer())
												.executes(context -> invite(context.getSource(), StringArgumentType.getString(context, "Warp Name"), EntityArgument.getOnePlayer(context, "player"))))))

		);

	}

	/**
	 * Creates a warp with the players current information.
	 * 
	 * @param source
	 * @param name
	 * @return
	 */
	private int setWarp(CommandSource source, String name) {
		EntityPlayer player;
		name = name.toLowerCase();
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			player = null;
		}

		// Is player
		if (player != null) {
			if (!isRemote(player)) {
				importWarps(player);
				int x, y, z, dim;
				float pitch, yaw;
				String displayName = player.getDisplayName().getString();
				x = player.getPosition().getX();
				y = player.getPosition().getY();
				z = player.getPosition().getZ();
				dim = player.getEntityWorld().getDimension().getType().getId();
				yaw = player.prevCameraYaw;
				pitch = player.prevCameraPitch;
				BlockPos pos = new BlockPos(x, y, z);
				SimpleEntry<Float, Float> rot = new SimpleEntry<Float, Float>(yaw, pitch);
				SimpleEntry<BlockPos, Entry<Float, Float>> pos_rot = new SimpleEntry<BlockPos, Entry<Float, Float>>(pos, rot);

				if (warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>((EntityPlayerMP) player, name)) != null) {
					warps.replace(new AbstractMap.SimpleEntry<EntityPlayerMP, String>((EntityPlayerMP) player, name), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos_rot, dim));
					sendMessage(player, TextFormatting.GOLD + "Warp Overwritten: " + TextFormatting.GREEN + name);
				} else {
					this.warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>((EntityPlayerMP) player, name), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos_rot, dim));
					sendMessage(player, TextFormatting.GOLD + "Warp Created: " + TextFormatting.GREEN + name);
				}

				warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>((EntityPlayerMP) player, name), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos_rot, dim));

				export(player);
//				sendMessage(player, TextFormatting.GOLD + "X: " + TextFormatting.AQUA + x + "\n" + TextFormatting.GOLD + "Y: " + TextFormatting.AQUA + y + "\n" + TextFormatting.GOLD + "Z: " + TextFormatting.AQUA + z + "\n" + TextFormatting.GOLD + "Dimension: " + TextFormatting.AQUA + dim + "\n" + TextFormatting.GOLD + "Yaw: " + TextFormatting.AQUA + yaw + "\n" + TextFormatting.GOLD + "Pitch: " + TextFormatting.AQUA + pitch);
			}
		} else { // is console
			WarpMod.LOGGER.warn("Are you in a console!  This command has to be run as a player");
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
	public int warpTo(CommandSource source, String name) {
		EntityPlayer playerIn;
		name = name.toLowerCase();
		try {
			playerIn = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			playerIn = null;
			return 0;
		}
		if (playerIn != null) {
			if (!isRemote(playerIn)) {
				EntityPlayerMP player = (EntityPlayerMP) playerIn;
				Entry map = new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, name);
				if (warps.containsKey(map)) {
					double x = (double) warps.get(map).getKey().getKey().getX(), y = (double) warps.get(map).getKey().getKey().getY(), z = (double) warps.get(map).getKey().getKey().getZ();
					int dimension = warps.get(map).getValue();
					BlockPos oldPos = player.getPosition();
					int oldDim = player.getEntityWorld().getDimension().getType().getId();
					float yaw = warps.get(map).getKey().getValue().getKey();
					float pitch = warps.get(map).getKey().getValue().getValue();
					Teleport.teleportToDimension(player, dimension, x, y, z, yaw, pitch);
					sendMessage(player, TextFormatting.GOLD + "Warped to: " + TextFormatting.GREEN + name);
					back(oldPos, playerIn.cameraYaw, playerIn.cameraPitch, oldDim, playerIn);
				} else {
					sendMessage(player, TextFormatting.RED + "Warp Not Found: " + TextFormatting.RED + name.toUpperCase());
				}
			} else
				return 0;
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
	public int warpTo(CommandSource source, EntityPlayerMP playerTo) {
		EntityPlayer playerIn;
		try {
			playerIn = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			playerIn = null;
			return 0;
		}

		int i = 0;
		for (EntityPlayerMP p : getOnlinePlayers(source)) {
			if (p.getDisplayName().getString().equalsIgnoreCase(player.getDisplayName().getString()))
				continue;
			else
				i++;
		}

		if (i == 0) {
			sendMessage(player, TextFormatting.LIGHT_PURPLE + "" + TextFormatting.UNDERLINE + TextFormatting.BOLD + "You Know You're all alone right?!?!?!?");
			return 1;
		}

		if (playerIn != null) {
			if (!isRemote(playerIn)) {
				EntityPlayerMP player = (EntityPlayerMP) playerIn;
				double x = (double) playerTo.getPosition().getX(), y = (double) playerTo.getPosition().getY(), z = (double) playerTo.getPosition().getZ();
				int dimension = playerTo.dimension.getId();
				BlockPos oldPos = player.getPosition();
				int oldDim = player.getEntityWorld().getDimension().getType().getId();
				float yaw = playerTo.prevCameraYaw;
				float pitch = playerTo.prevCameraPitch;
				String name = playerTo.getDisplayName().getString();
				Teleport.teleportToDimension(player, dimension, x, y, z, yaw, pitch);
				sendMessage(player, TextFormatting.GOLD + "Warped to: " + TextFormatting.GREEN + name);
				back(oldPos, playerIn.cameraYaw, playerIn.cameraPitch, oldDim, playerIn);
			}
		} else
			return 0;
		return 1;
	}

	public int warpToMe(CommandSource source, EntityPlayerMP playerTo) {
		EntityPlayer player;
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			player = null;
			return 0;
		}
		int i = 0;
		for (EntityPlayerMP p : getOnlinePlayers(source)) {
			if (p.getDisplayName().getString().equalsIgnoreCase(player.getDisplayName().getString()))
				continue;
			else
				i++;
		}

		if (i == 0) {
			sendMessage(player, TextFormatting.LIGHT_PURPLE + "" + TextFormatting.UNDERLINE + TextFormatting.BOLD + "You Know You're all alone right?!?!?!?");
			return 1;
		}

		if (player != null && !isRemote(player)) {
			double x = (double) player.getPosition().getX(), y = (double) player.getPosition().getY(), z = (double) player.getPosition().getZ();
			int dimension = player.dimension.getId();
			BlockPos oldPos = playerTo.getPosition();
			int oldDim = playerTo.dimension.getId();
			float yaw = player.prevCameraYaw, pitch = player.prevCameraPitch;
			String name = player.getDisplayName().getString();
			Teleport.teleportToDimension(playerTo, dimension, x, y, z, yaw, pitch);
			back(oldPos, playerTo.prevCameraYaw, playerTo.prevCameraPitch, oldDim, playerTo);
			sendMessage(playerTo, TextFormatting.RED + "You are being forced into a locked room with " + TextFormatting.LIGHT_PURPLE + name);
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
		if (!isRemote(player)) {
			SimpleEntry<Float, Float> rot = new SimpleEntry<Float, Float>(yaw, pitch);
			SimpleEntry<BlockPos, Entry<Float, Float>> pos_rot = new SimpleEntry<BlockPos, Entry<Float, Float>>(pos, rot);
			this.warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>((EntityPlayerMP) player, "back"), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos_rot, dimension));
			sendMessage(player, TextFormatting.GREEN + "Back Warped Saved: type " + TextFormatting.GOLD + "\"/warp back\"" + TextFormatting.GREEN + " to go back");
			export(player);
		}
	}

	/**
	 * Imports the warps from file to a variable. Returns a Array of warp names.
	 * 
	 * @param player
	 * @return
	 */
	private List<String> importWarps(EntityPlayer player) {
		List<String> warps = new ArrayList<String>();

		if (!isRemote(player)) {
			setPlayer(player);
			FileReader file;
			FileName = player.getDisplayName().getString() + "_" + player.getServer().getFolderName() + ".conf";
			try {
				file = new FileReader(FileLocation + FileName);
				br = new BufferedReader(file);
			} catch (FileNotFoundException e) {
				try {
					TextWriter("", false, player);
					System.out.println("File Not Found Creating it");
					e.printStackTrace();
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
						WarpMod.LOGGER.error("This Returned NULL: MESSAGE->" + e.getMessage() + " | CAUSE->" + e.getCause());
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
				System.out.println("\n\n\nFile Doesn't Exist And Couln't be Created!!!\n\n\n");
			} catch (NullPointerException e) {
				e.printStackTrace();
				System.out.println("\n\n\n " + e.getLocalizedMessage() + "-->> was null for some reason \n\n\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (getPlayer() != null) {
			if (!warps.isEmpty()) {
				remove_text.clear();
				remove_text.addAll(warps);
				remove_text.add("*");
				WARP_REMOVE_SUGGESTIONS = (context, builder) -> ISuggestionProvider.suggest(remove_text, builder);
			}
		}
		return warps;

	}

	/**
	 * Creates a Human-Readable list of warps
	 * 
	 * @param source
	 * @return
	 */
	private int listWarps(CommandSource source) {
		EntityPlayer player;
		List<String> names = new ArrayList<String>();
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			player = null;
		}

		// Is Player
		if (player != null) {
			if (!isRemote(player)) {
				importWarps(player);
				if (warps.isEmpty()) {
					sendMessage(player, TextFormatting.RED + "No Warps");
					return 1;
				}

				for (Entry<EntityPlayerMP, String> e : warps.keySet()) {
					if (e.getKey().equals((EntityPlayerMP) player)) {
						names.add(e.getValue());
					}
				}

				String value = names.toString().replace("[", "").replace("]", "");
				sendMessage(player, TextFormatting.GOLD + value);
			} else
				return 0;
		} else
			return 0;

		return 1;
	}

	/**
	 * Creates a verbose list of warps.
	 * 
	 * @param source
	 * @return
	 */
	public int Map(CommandSource source) {
		EntityPlayer player;
		List<String> names = new ArrayList<String>();
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			player = null;
		}

		// Is Player
		if (player != null) {
			if (!isRemote(player)) {
				String value = "";
				try {
					importWarps(player);
					if (warps.isEmpty()) {
						sendMessage(player, TextFormatting.RED + "No Warps");
						return 1;
					}

					for (Entry<EntityPlayerMP, String> e : warps.keySet()) {
						if (e.getKey().equals((EntityPlayerMP) player)) {
							BlockPos pos = warps.get(e).getKey().getKey();
							float x, y, z;
							x = pos.getX();
							y = pos.getY();
							z = pos.getZ();
							int dimensionId = warps.get(e).getValue();
//							String dimensionName =  DimensionType.getById(dimensionId).getRegistryType().getSimpleName();//DimensionManager.getWorld(player.getServer(), DimensionType.getById(dimensionId), false, false).getWorldType().getName();
							String name = e.getValue();
							value += TextFormatting.GREEN + name + TextFormatting.GOLD + ":{X: " + TextFormatting.GREEN + x + TextFormatting.GOLD + ", Y: " + TextFormatting.GREEN + y + TextFormatting.GOLD + ", Z: " + TextFormatting.GREEN + z + TextFormatting.GOLD + ", Dimension: " + TextFormatting.GREEN + dimensionId + TextFormatting.GOLD + "}*";
							names.add(value);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				value = value.replace("*", "\n");
				sendMessage(player, TextFormatting.GOLD + value);
			} else {
				sendMessage(player, TextFormatting.RED + "Request was remote");
				return 0;
			}
		} else {
			sendMessage(player, TextFormatting.RED + "Player doesn't exist... You don't exist...");
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
	private List<EntityPlayerMP> getOnlinePlayers(CommandSource source) {
		List<EntityPlayerMP> players = new ArrayList<EntityPlayerMP>();
		try {
			for (EntityPlayer p : source.asPlayer().getServerWorld().playerEntities) {
				players.add((EntityPlayerMP) p);
			}
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		return players;
	}

	public int remove(CommandSource source, String name) {
		EntityPlayer player;
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			player = null;
		}
		if (player != null && !isRemote(player)) {
			importWarps(player);
			if (name.equals("*")) {
				warps.clear();
				sendMessage(player, TextFormatting.LIGHT_PURPLE + "Removed all Saved Warps");
				export(player);
				return 1;
			} else if (name.endsWith("*") && !(name.replace("*", "").isEmpty())) {
				for (Entry<EntityPlayerMP, String> i : warps.keySet()) {
					name = name.replace("*", "");
					if (i.getValue().startsWith(name)) {
						warps.remove(i);
						sendMessage(player, TextFormatting.LIGHT_PURPLE + "Removed " + i + " from your Saved Warps");
					}
				}
				return 1;
			}
			for (Entry<EntityPlayerMP, String> i : warps.keySet()) {
				if (name.equalsIgnoreCase(i.getValue())) {
					warps.remove(i);
					return 1;
				}
			}
		} else
			return 0;

		sendMessage(player, TextFormatting.LIGHT_PURPLE + name.toUpperCase() + TextFormatting.RED + " was not found!");

		return 1;
	}

	/**
	 * Renames warps.
	 * 
	 * @author Drew Chase
	 * @param oldName
	 * @param newName
	 */
	public int rename(CommandSource source, String oldName, String newName) {
		EntityPlayer playerIn;
		try {
			playerIn = source.asPlayer();
		} catch (CommandSyntaxException e1) {
			e1.printStackTrace();
			playerIn = null;
		}
		if (playerIn != null) {
			if (!isRemote(playerIn)) {
				try {
					EntityPlayerMP player = (EntityPlayerMP) playerIn;
					int oldDim = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, oldName)).getValue();
					float yaw = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, oldName)).getKey().getValue().getKey();
					float pitch = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, oldName)).getKey().getValue().getValue();
					Map.Entry<BlockPos, Entry<Float, Float>> pos = new SimpleEntry<BlockPos, Entry<Float, Float>>(warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, oldName)).getKey().getKey(), warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, oldName)).getKey().getValue());
					warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, newName), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos, oldDim));
					warps.remove(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(player, oldName));

					export(player);
					sendMessage(player, TextFormatting.GOLD + oldName + TextFormatting.GREEN + " renamed to " + TextFormatting.GOLD + newName);
				} catch (Exception e) {
					sendMessage(playerIn, TextFormatting.RED + "An Error Has Occurred in the Rename Method");
					e.printStackTrace();
					return 0;
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
	public int getHelp(CommandSource source) {
		List<String> s = new ArrayList<String>();
		s.add("/warp set <name>");
		s.add("/warp invite <name> <to-player>");
		s.add("/warp map");
		s.add("/warp list");
		s.add("/warp <name>");
		s.add("/warp random <max-distance(optional)>");
		s.add("/warp remove <name>");
		s.add("/warp remove *");
		s.add("/warp rename <old name> <new name>");
		s.add("/warp <player> me");
		s.add("/warp me <player>");
		EntityPlayer player;
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			player = null;
		}
		String value = TextFormatting.LIGHT_PURPLE + "-----The Warp Mod HELP-----\n";
		if (player != null && !isRemote(player)) {
			for (String i : s) {
				value += TextFormatting.GOLD + i + "\n";
			}
		}

		sendMessage(player, TextFormatting.AQUA + value);
		return 1;
	}

	/**
	 * Safely warps a player randomly
	 * 
	 * @param range
	 * @param playerIn
	 * @author Drew Chase
	 */
	public int warpRandom(CommandSource source, int range) {
		EntityPlayer player;
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			player = null;
		}
		if (player != null) {
			if (!isRemote(player)) {
				Random ran = new Random();
				int var = ran.nextInt(range);
				double x, y, z;
				x = player.getPosition().getX() + var;
				y = player.world.getHeight();
				z = player.getPosition().getZ() + var;
				BlockPos pos = new BlockPos(x, y, z);
				// Makes sure that the player is on solid ground
				if (player.world.getBlockState(pos).getBlock().equals(Blocks.WATER) || player.world.getBlockState(pos).getBlock().equals(Blocks.LAVA)) {
					warpRandom(source, range / 2);
					return 1;
				}

				while (player.world.getBlockState(pos).getBlock().equals(Blocks.AIR) || player.world.getBlockState(pos).getBlock().equals(Blocks.VOID_AIR)) {
					if (y <= 10) {
						sendMessage(player, TextFormatting.RED + "There is no safe place to land");
						return 1;
					}
					y--;
					pos = new BlockPos(x, y, z);
				}
				// Increases the players y so that they are above ground
				y += 2;
				back(player.getPosition(), player.cameraYaw, player.cameraPitch, player.dimension.getId(), player);
				float yaw = player.cameraYaw;
				float pitch = player.cameraPitch;
				Teleport.teleportToDimension(player, player.dimension.getId(), x, y, z, yaw, pitch);
				sendMessage(player, TextFormatting.AQUA + "Warping " + var + " blocks away!");
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
	public int invite(CommandSource source, String name, EntityPlayerMP playerIn) {
		EntityPlayerMP playerOut;
		try {
			playerOut = (EntityPlayerMP) source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			playerOut = null;
			return 0;
		}

		int i = 0;
		for (EntityPlayerMP p : getOnlinePlayers(source)) {
			if (p.getDisplayName().getString().equalsIgnoreCase(player.getDisplayName().getString()))
				continue;
			else
				i++;
		}

		if (i == 0) {
			sendMessage(player, TextFormatting.LIGHT_PURPLE + "" + TextFormatting.UNDERLINE + TextFormatting.BOLD + "You Know You're all alone right?!?!?!?");
			return 1;
		}

		if (playerOut != null) {
			importWarps(playerOut);
			if (!isRemote(playerOut)) {
				float yaw = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerOut, name)).getKey().getValue().getKey();
				float pitch = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerOut, name)).getKey().getValue().getValue();

				Map.Entry<BlockPos, Entry<Float, Float>> pos = new SimpleEntry<BlockPos, Entry<Float, Float>>(warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerOut, name)).getKey().getKey(), warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerOut, name)).getKey().getValue());

				int dim = warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerOut, name)).getValue();
				if (warps.get(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerIn, name)) != null)
					warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerIn, name), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos, dim));
				else
					warps.put(new AbstractMap.SimpleEntry<EntityPlayerMP, String>(playerIn, name + "_from_" + playerOut.getDisplayName().getString()), new AbstractMap.SimpleEntry<Map.Entry<BlockPos, Entry<Float, Float>>, Integer>(pos, dim));
				export(playerIn);
				sendMessage(playerIn, TextFormatting.GREEN + "You've Been Invited to " + TextFormatting.GOLD + name + TextFormatting.GREEN + " from " + TextFormatting.GOLD + playerOut.getDisplayName().getString());
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
	public boolean isRemote(EntityPlayer player) {
		return player.getEntityWorld().isRemote;
	}

	/**
	 * Exports the warps to file
	 * 
	 * @param player
	 */
	public void export(EntityPlayer player) {
		if (!isRemote(player)) {
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
				importWarps(player);
			} catch (IOException e) {
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
	public void TextWriter(String text, boolean newLine, EntityPlayer player) throws IOException {
		if (!isRemote(player)) {
			File f = new File(FileLocation);

			try {
				if (f.mkdirs()) {
					WarpMod.LOGGER.debug("Warp File Created in " + f.getAbsolutePath());
				}
			} catch (Exception e) {
				WarpMod.LOGGER.debug("Couldn't Create File");
				e.printStackTrace();
			}
			FileName = player.getDisplayName().getString() + "_" + player.getServer().getFolderName() + ".conf";

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

	public void setPlayer(EntityPlayer player) {
		this.player = (EntityPlayerMP) player;
	}

	public EntityPlayerMP getPlayer() {
		return this.player;
	}

	/**
	 * Sends message to current player
	 * 
	 * @param player
	 * @param message
	 */
	private void sendMessage(EntityPlayer player, Object message) {
		player.sendMessage(new TextComponentString(message + ""));
	}

	/**
	 * Sends message to remote player
	 * 
	 * @param player
	 * @param message
	 */
	private void sendMessage(EntityPlayerMP player, Object message) {
		player.sendMessage(new TextComponentString(message + ""));
	}

}