package com.drewchaseproject.forge.WarpMod.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.drewchaseproject.forge.WarpMod.WarpMod;
import com.drewchaseproject.forge.WarpMod.WarpMod.LogType;
import com.drewchaseproject.forge.WarpMod.Objects.Warp;
import com.drewchaseproject.forge.WarpMod.Objects.Warps;
import com.drewchaseproject.forge.WarpMod.commands.util.Teleport;
import com.drewchaseproject.forge.WarpMod.config.ConfigHandler;
import com.drewchaseproject.forge.WarpMod.util.WarpPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;

/**
 * Main Warp Command
 * 
 * @author Drew Chase
 *
 */
@SuppressWarnings("all")
public final class WarpCommand {

	public Warps warps = new Warps(), public_warps = new Warps();

	public BufferedReader br;
	public BufferedWriter bw;
	public String FileName = "warps.conf";
	public String FileLocation = "config/Warps/";
	public WarpCommand instance;
	private ServerPlayerEntity player;
	private String[] subcommands = new String[] { "set", "random", "map", "list", "help", "reload", "me", "remove", "rename", "invite", "accept", "spawn" };
	private final ArrayList<String> remove_text = new ArrayList<String>();
	private final SuggestionProvider<CommandSource> WARP_SUGGESTIONS = (context, builder) -> ISuggestionProvider.suggest(importAllWarps(context.getSource().asPlayer()), builder), COMMAND_SUGGESTIONS = (context, builder) -> ISuggestionProvider.suggest(subcommands, builder);
	private SuggestionProvider<CommandSource> WARP_REMOVE_SUGGESTIONS;

	/**
	 * Warp Command Constructor.
	 */
	public WarpCommand() {
		instance = this;
	}

	/**
	 * Registers all forms of the command
	 * 
	 * @param dispatcher
	 */
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("warp")
//				.then(Commands.literal("test").executes(context -> test(context.getSource())))
				.then(
						Commands.literal("spawn").executes(context -> warpTo(context.getSource())))
//				.then(
//						Commands.literal("accept")
//								.executes(context -> AcceptWarp(context.getSource())))
//				.then(
//						Commands.literal("reload")
//								.then(Commands.literal("config")
//										.executes(context -> reloadConfig())))
				.then(
						Commands.argument("Warp Name", StringArgumentType.word())
								.suggests(WARP_SUGGESTIONS)
								.executes(context -> warpTo(context.getSource(), StringArgumentType.getString(context, "Warp Name"))))
				.then(
						Commands.literal("set")
								.then(Commands.argument("Name", StringArgumentType.word())
										.then(Commands.literal("-p")
												.executes(context -> setPublicWarp(context.getSource(), StringArgumentType.getString(context, "Name"))))
										.executes(context -> setWarp(context.getSource(), StringArgumentType.getString(context, "Name")))))
				.then(
						Commands.literal("list")
								.then(Commands.literal("-p")
										.executes(context -> listPublicWarps(context.getSource())))
								.then(Commands.argument("PlayerName", StringArgumentType.greedyString())
										.executes(context -> listWarps(context.getSource(), StringArgumentType.getString(context, "PlayerName"))))
								.executes(context -> listWarps(context.getSource())))
				.then(
						Commands.literal("map")
								.then(Commands.literal("-p")
										.executes(context -> mapPublicWarps(context.getSource())))
								.then(Commands.argument("PlayerName", StringArgumentType.greedyString())
										.executes(context -> mapWarps(context.getSource(), StringArgumentType.getString(context, "PlayerName"))))
								.executes(context -> mapWarps(context.getSource())))
				.then(
						Commands.literal("random")
								.then(Commands.argument("range", IntegerArgumentType.integer())
										.executes(context -> warpRandom(context.getSource(), IntegerArgumentType.getInteger(context, "range"))))
								.executes(context -> warpRandom(context.getSource(), 600)))
				.then(
						Commands.argument("player", EntityArgument.player())
								.then(Commands.literal("me")
										.executes(context -> warpToMe(context.getSource(), (ServerPlayerEntity) EntityArgument.getPlayer(context, "player")))))
				.then(
						Commands.literal("me")
								.then(Commands.argument("player", EntityArgument.player())
										.executes(context -> warpTo(context.getSource(), (ServerPlayerEntity) EntityArgument.getPlayer(context, "player")))))
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
										.then(Commands.argument("player", EntityArgument.player())
												.executes(context -> invite(context.getSource(), StringArgumentType.getString(context, "Warp Name"), (ServerPlayerEntity) EntityArgument.getPlayer(context, "player"))))))

		);

	}

	private int test(CommandSource source) {
		ServerPlayerEntity player = null;
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			return 0;
		}
		sendMessage(player, String.format("\n======================="
				+ "\nSave File Name \"%s\""
				+ "\n=======================\n",
				player.getServer().getWorldIconFile().getParentFile().getName()));
		return 1;
	}

	private int AcceptWarp(CommandSource source) {
		return 1;
	}

	private int reloadConfig() {
		ConfigHandler.readConfig();
		return 1;
	}

	public boolean isAllowed(boolean global, String... errorMessage) {
		if (getPlayer().getServer().isSinglePlayer())
			return true;
		ConfigHandler.readConfig();
		if (getPlayer() != null) {
			if (!global) {
				if (ConfigHandler.areAllPlayersAllowed()) {
					return true;
				} else if (ConfigHandler.getAllowedPlayers().contains(getPlayer().getDisplayName().getString())) {
					return true;
				}
				for (String s : ConfigHandler.getAllowedPlayers()) {
					if (s.equalsIgnoreCase(getPlayer().getDisplayName().getString()))
						return true;
				}
			} else {
				if (ConfigHandler.areAllPlayersAllowedPublic()) {
					return true;
				} else if (ConfigHandler.getAllowedPublicPlayers().contains(getPlayer().getDisplayName().getString())) {
					return true;
				}
				for (String s : ConfigHandler.getAllowedPublicPlayers()) {
					if (s.equalsIgnoreCase(getPlayer().getDisplayName().getString()))
						return true;
				}
			}
		} else if (getPlayer() == null) {
			return true;
		}
		if (errorMessage.length > 0) {
			for (String text : errorMessage) {
				sendMessage(getPlayer(), TextFormatting.RED + text);
			}
		}
		return false;
	}

	private int setPublicWarp(CommandSource source, String name) {
		ServerPlayerEntity player;
		name = name.toLowerCase();
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			player = null;
		}

		// Is player
		if (player != null && ConfigHandler.getPublicWarpsAllowed() && (ConfigHandler.areAllPlayersAllowedPublic() || ConfigHandler.getAllowedPublicPlayers().contains(player.getDisplayName().getString()))) {
			if (!isRemote(player)) {
				setPlayer(player);
				if (isAllowed(true, "You do not have permissions to create public warps")) {
					importAllWarps(player);
					int x = (int) player.getPosX(), y = (int) player.getPosY(), z = (int) player.getPosZ();
//					float pitch, yaw;
					String displayName = player.getDisplayName().getString();
					BlockPos pos = new BlockPos(x, y, z);
					boolean added = false;
					if (public_warps.getWarp(name) != null) {
						added = public_warps.addWarp(new Warp(name, player));
						sendMessage(player, TextFormatting.GOLD + "Public Warp Overwritten: " + TextFormatting.GREEN + name);
					} else {
						added = public_warps.addWarp(new Warp(name, player));
						sendMessage(player, TextFormatting.GOLD + "Public Warp Created: " + TextFormatting.GREEN + name);
					}
					added = public_warps.addWarp(new Warp(name, player));
					if (!added) {
						sendMessage(player, TextFormatting.RED + "Could not add Warp " + name);
					}
					exportPublicWarps();
				}
			}
		} else if (!ConfigHandler.getPublicWarpsAllowed()) {
			sendMessage(player, TextFormatting.RED + "Public Warps Aren't Allowed, Making Private One");
			setWarp(source, name);
		} else if (player != null && !(ConfigHandler.areAllPlayersAllowedPublic() || ConfigHandler.getAllowedPublicPlayers().contains(player.getDisplayName().getString()))) {
			sendMessage(player, TextFormatting.RED + "You Are NOT Allowed to create public warps, Making Private One");
			setWarp(source, name);
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
	private int setWarp(CommandSource source, String name) {
		ServerPlayerEntity player = null;
		name = name.toLowerCase();
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			player = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Is player
		if (player != null) {
			if (!isRemote(player)) {
				setPlayer(player);
				try {
					for (String value : subcommands) {
						if (name.equalsIgnoreCase(value)) {
							sendMessage(player, TextFormatting.RED + value + TextFormatting.LIGHT_PURPLE + " is a keyword used in The Warp Mod");
							return 1;
						}
					}
					if (isAllowed(false, "You do not have permissions to create warps")) {
						importAllWarps(player);
						int x, y, z, dim;
						float pitch, yaw;
						String displayName = player.getDisplayName().getString();
						x = player.getPosition().getX();
						y = player.getPosition().getY();
						z = player.getPosition().getZ();

						pitch = player.getPitchYaw().y;
						yaw = player.getPitchYaw().x;

						BlockPos pos = new BlockPos(x, y, z);
						boolean added = false;
						if (warps.getWarp(name) != null && warps.getWarp(name).getPlayer().equals(player)) {
							try {
								added = warps.addWarp(new Warp(name, player));
								sendMessage(player, TextFormatting.GOLD + "Warp Overwritten: " + TextFormatting.GREEN + name);
							} catch (NullPointerException e) {
								if (name == null)
									System.out.println("Name was null");
								if (player == null)
									System.out.println("Player was null");
							} catch (Exception e) {

							}
						} else if (warps.getWarp(name) == null) {
							try {
								added = warps.addWarp(new Warp(name, player));
								sendMessage(player, TextFormatting.GOLD + "Warp Created: " + TextFormatting.GREEN + name);
							} catch (NullPointerException e) {
								if (name == null)
									System.out.println("Name was null");
								if (player == null)
									System.out.println("Player was null");
							} catch (Exception e) {

							}
						}
						added = warps.addWarp(new Warp(name, player));
						if (!added)
							sendMessage(player, TextFormatting.RED + "Could not add Warp " + name);
						exportAllWarps(player);
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
					System.out.println(e.getCause());
				} catch (Exception e) {
					System.out.println(e.getCause());
					e.printStackTrace();
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
	public List<String> importAllWarps(ServerPlayerEntity player) {
		setPlayer(player);
		List<String> name = new ArrayList<String>();
		name.addAll(importWarps(player, 0));
		if (isAllowed(true))
			name.addAll(importPublicWarps());
		else
			public_warps.empty(player, public_warps.getWarps());

		for (String s : name) {
			WarpMod.log(LogType.Debug, "Imported " + s + " for " + player.getDisplayName().getString());
		}
		return name;
	}

	public void exportAllWarps(ServerPlayerEntity player) {
		setPlayer(player);
		WarpMod.log(LogType.Debug, public_warps.toString());
		export(player);
		if (isAllowed(true))
			exportPublicWarps();
		importAllWarps(player);
		WarpMod.log(LogType.Debug, public_warps.toString());
	}

	public int warpTo(CommandSource source) {
		ServerPlayerEntity player = null;
		try {
			player = source.asPlayer();
		} catch (Exception e) {
			WarpMod.log(LogType.Error, "ERROR: " + e.getMessage() + "\n" + e.getStackTrace().toString());
			return 0;
		}
		if (player != null) {
			if (!isRemote(player)) {
				setPlayer(player);
				double x = (double) new WarpPlayer(player).getWorldSpawn().getX(), y = (double) new WarpPlayer(player).getWorldSpawn().getY(), z = (double) new WarpPlayer(player).getWorldSpawn().getZ();
				BlockPos oldPos = player.getPosition();
				float pitch = player.getPitchYaw().y;
				float yaw = player.getPitchYaw().x;
				ResourceLocation dimension = new WarpPlayer(player).getDimensionResourceLocation();
				Teleport.teleportToDimension(player, x, y, z, yaw, pitch, new WarpPlayer(player).getDimensionResourceLocation());
				sendMessage(player, TextFormatting.GOLD + "Warped to: " + TextFormatting.GREEN + "Spawn");

				back(oldPos, yaw, pitch, dimension, player);
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
	public int warpTo(CommandSource source, String name) {
		ServerPlayerEntity playerIn;
		name = name.toLowerCase();
		try {
			playerIn = source.asPlayer();
		} catch (CommandSyntaxException e) {
			WarpMod.log(LogType.Error, e.getStackTrace().toString());
			playerIn = null;
			return 0;
		}
		if (playerIn != null) {
			if (!isRemote(playerIn)) {
				try {
					ServerPlayerEntity player = playerIn;
					setPlayer(player);
					importAllWarps(player);
					double x = player.getPosX(), y = player.getPosY(), z = player.getPosZ();
					ResourceLocation dimension = new WarpPlayer(player).getDimensionResourceLocation();
					BlockPos oldPos = new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ());
					ResourceLocation oldDim = new WarpPlayer(player).getDimensionResourceLocation();
					float pitch = player.getPitchYaw().y;
					float yaw = player.getPitchYaw().x;
					if (warps.getWarp(name) != null && isAllowed(false)) {
						Warp warp = warps.getWarp(name);
						if (warp.getPlayer().equals(player)) {
							Teleport.teleportToDimension(player, warp);

							back(oldPos, yaw, pitch, oldDim, player);
							sendMessage(player, TextFormatting.GOLD + "Warped to: " + TextFormatting.GREEN + name);
						}
					} else if (public_warps.getWarp(name) != null && isAllowed(true)) {
						Warp warp = public_warps.getWarp(name);
						Teleport.teleportToDimension(player, warp);
						sendMessage(player, TextFormatting.GOLD + "Warped to: " + TextFormatting.GREEN + name);
						back(oldPos, yaw, pitch, oldDim, player);
					} else {
						sendMessage(player, TextFormatting.RED + "Warp Not Found: " + TextFormatting.RED + name.toUpperCase());
					}
				} catch (Exception e) {
					e.printStackTrace();
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
	public int warpTo(CommandSource source, ServerPlayerEntity playerTo) {
		ServerPlayerEntity playerIn;
		try {
			playerIn = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			playerIn = null;
			return 0;
		}

		int i = 0;
		for (ServerPlayerEntity p : getOnlinePlayers(source)) {
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
				ServerPlayerEntity player = playerIn;
				setPlayer(player);
				if (isAllowed(false, "You do not have permissions to use the warp mod")) {
					double x = playerTo.getPosX(), y = playerTo.getPosY(), z = playerTo.getPosZ();
					ResourceLocation dimension = new WarpPlayer(playerTo).getDimensionResourceLocation();
					BlockPos oldPos = new BlockPos(player.getPosX(), player.getPosY(), player.getPosZ());
					ResourceLocation oldDim = new WarpPlayer(playerTo).getDimensionResourceLocation();
					float pitch = player.getPitchYaw().y;
					float yaw = player.getPitchYaw().x;
					String name = playerTo.getDisplayName().getString();
					Teleport.teleportToDimension(player, x, y, z, yaw, pitch, new WarpPlayer(playerTo).getDimensionResourceLocation());
					sendMessage(player, TextFormatting.GOLD + "Warped to: " + TextFormatting.GREEN + name);

					back(oldPos, yaw, pitch, oldDim, playerTo);
				}
			}
		} else
			return 0;
		return 1;
	}

	public int warpToMe(CommandSource source, ServerPlayerEntity playerTo) {
		ServerPlayerEntity player;
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			player = null;
			return 0;
		}
		int i = 0;
		for (ServerPlayerEntity p : getOnlinePlayers(source)) {
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
			setPlayer(player);
			if (isAllowed(false, "You do not have permissions to use the warp mod")) {
				double x = (double) player.getPosX(), y = (double) player.getPosY(), z = (double) player.getPosZ();
				ResourceLocation dimension = new WarpPlayer(playerTo).getDimensionResourceLocation();
				BlockPos oldPos = new BlockPos(playerTo.getPosX(), playerTo.getPosY(), playerTo.getPosZ());
				ResourceLocation oldDim = new WarpPlayer(playerTo).getDimensionResourceLocation();
				float pitch = player.getPitchYaw().y;
				float yaw = player.getPitchYaw().x;
				String name = player.getDisplayName().getString();
				Teleport.teleportToDimension(playerTo, x, y, z, yaw, pitch, dimension);
				back(oldPos, yaw, pitch, oldDim, playerTo);
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
	public void back(BlockPos pos, float yaw, float pitch, ResourceLocation location, ServerPlayerEntity player) {
		if (!isRemote(player)) {
			setPlayer(player);
			if (isAllowed(false, "You do not have permissions to use the warp mod")) {
				try {
					importAllWarps(player);
					warps.addWarp(new Warp("back", pos, player, yaw, pitch, player.getServerWorld(), location));
					sendMessage(player, TextFormatting.GREEN + "Back Warped Saved: type " + TextFormatting.GOLD + "\"/warp back\"" + TextFormatting.GREEN + " to go back");
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
	private List<String> importWarps(ServerPlayerEntity player, int count) {
		List<String> warps = new ArrayList<String>();

		if (!isRemote(player)) {
			setPlayer(player);
			FileReader file;
			if (getPlayer().server.isDedicatedServer()) {
				FileName = String.format("%s.conf", player.getDisplayName().getString());
			} else {
				FileName = String.format("%s_%s.conf", player.getDisplayName().getString(), player.getServer().getWorldIconFile().getParentFile().getName());
			}
			try {
				file = new FileReader(FileLocation + FileName);
				br = new BufferedReader(file);
			} catch (FileNotFoundException e) {
				if (count < 3) {

					try {
						TextWriter("", false, player.getDisplayName().getString());
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
							text[6] = text[6].replace(value, "");
							text[7] = text[7].replace(value, "");
						}
						for (int i = 0; i < text.length; i++) {
							text[i] = text[i].replace("*01", ":");
						}
						BlockPos pos = new BlockPos(Double.parseDouble(text[2]), Double.parseDouble(text[3]), Double.parseDouble(text[4]));
						ResourceLocation location = new ResourceLocation(text[5].split(" ")[0].replace("Yaw", ""));
						float yaw = Float.parseFloat(text[6]);
						float pitch = Float.parseFloat(text[7]);
						Warp warp = new Warp(name, player);
						warp.setDimensionResourceLocation(location);
						warp.setPos(pos);
						warp.setYaw(yaw);
						warp.setPitch(pitch);
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
		if (getPlayer().server.isDedicatedServer()) {
			FileName = "public.conf";
		} else {
			FileName = String.format("public_%s.conf", player.getServer().getWorldIconFile().getParentFile().getName());
		}
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
						text[6] = text[6].replace(value, "");
						text[7] = text[7].replace(value, "");
					}
					for (int i = 0; i < text.length; i++) {
						text[i] = text[i].replace("*01", ":");
					}
					String name = text[0];
					BlockPos pos = new BlockPos(Double.parseDouble(text[2].replace("X", "").replace("Y", "").replace("Z", "").replace("World", "")), Double.parseDouble(text[3].replace("X", "").replace("Y", "").replace("Z", "").replace("World", "")), Double.parseDouble(text[4].replace("X", "").replace("Y", "").replace("Z", "").replace("World", "")));
					ResourceLocation location = new ResourceLocation(text[5].replace("X", "").replace("Y", "").replace("Z", "").replace("World", "").replace("Yaw", "").replace("Pitch", ""));
					float yaw = Float.parseFloat(text[6].replaceAll("World", "").replaceAll("Pitch", "").replaceAll("Yaw", "").replace(" ", ""));
					float pitch = Float.parseFloat(text[7].replaceAll("World", "").replaceAll("Yaw", "").replaceAll("Pitch", "").replace(" ", ""));
					Warp warp = new Warp(name, player);
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
				WARP_REMOVE_SUGGESTIONS = (context, builder) -> ISuggestionProvider.suggest(remove_text, builder);
			}
		}
		return warps;

	}

	private int listWarps(CommandSource source, String playerName) {
		try {
			List<String> names = new ArrayList<String>();
			ServerPlayerEntity player = null;
			for (ServerPlayerEntity playerList : warps.getPlayers()) {
				if (playerList.getDisplayName().equals(playerName))
					player = playerList;
			}
			// Is Console
			if (!(source.getEntity() instanceof ServerPlayerEntity) && player != null) {
				importAllWarps(player);
				if (warps.getWarps().isEmpty() && public_warps.getWarps().isEmpty()) {
					sendMessage(player, TextFormatting.RED + "No Warps");
					return 1;
				}

				if (warps.isEmpty(player)) {
					sendMessage(player, TextFormatting.RED + "No Warps");
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
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;
	}

	/**
	 * Creates a Human-Readable list of warps
	 * 
	 * @param source
	 * @return
	 */
	private int listWarps(CommandSource source) {
		ServerPlayerEntity player;
		List<String> names = new ArrayList<String>();
		try {
			player = source.asPlayer();
		} catch (Exception e) {
			player = null;
		}

		// Is Player
		if (player != null) {
			if (!isRemote(player)) {
				setPlayer(player);
				if (isAllowed(false, "You do not have permissions to use the warp mod")) {
					importAllWarps(player);
					if (warps.isEmpty(player)) {
						sendMessage(player, TextFormatting.RED + "No Warps");
						return 1;
					}
					warps.getWarps(player).forEach((n) -> names.add(n.getName()));
					public_warps.getWarps().forEach((n) -> names.add(n.getName()));

					String value = names.toString().replace("[", "").replace("]", "");
					sendMessage(player, TextFormatting.GOLD + value);
				}
			} else
				return 0;
		} else {
			// Is Console
		}
		return 1;
	}

	private int listPublicWarps(CommandSource source) {
		ServerPlayerEntity player;
		List<String> names = new ArrayList<String>();
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			player = null;
		}

		// Is Player
		if (player != null) {
			if (!isRemote(player)) {
				setPlayer(player);
				if (isAllowed(false, "You do not have permissions to use public warps")) {
					importAllWarps(player);
					if (public_warps.getWarps().isEmpty()) {
						sendMessage(player, TextFormatting.RED + "No Warps");
						return 1;
					}

					for (Warp warp : public_warps.getWarps()) {
						names.add(warp.getName());
					}

					String value = names.toString().replace("[", "").replace("]", "");
					sendMessage(player, TextFormatting.GOLD + value);
				}
			} else
				return 0;
		} else
			return 0;

		return 1;
	}

	public int mapWarps(CommandSource source, String playerName) {
		List<String> names = new ArrayList<String>();

		ServerPlayerEntity player = null;
		for (ServerPlayerEntity players : warps.getPlayers()) {
			if (players.getDisplayName().equals(playerName))
				player = players;
		}

		// Is Console
		if (!(source.getEntity() instanceof ServerPlayerEntity) && player != null) {

			String value = "";
			try {
				importAllWarps(player);
				if (warps.getWarps().isEmpty() && public_warps.getWarps().isEmpty()) {
					sendMessage(player, TextFormatting.RED + "No Warps");
					return 1;
				}

				for (Warp warp : warps.getWarps()) {
					if (warp.getPlayer().equals(player)) {
						float x = warp.getX(), y = warp.getY(), z = warp.getZ();
						String name = warp.getName();
						value += TextFormatting.GREEN + name + TextFormatting.GOLD + ":{X: " + TextFormatting.GREEN + x + TextFormatting.GOLD + ", Y: " + TextFormatting.GREEN + y + TextFormatting.GOLD + ", Z: " + TextFormatting.GREEN + z + TextFormatting.GOLD + ", Dimension: " + TextFormatting.GREEN + TextFormatting.GOLD + "}*";
						names.add(value);
					}
				}

				for (Warp warp : public_warps.getWarps()) {
					float x = warp.getX(), y = warp.getY(), z = warp.getZ();
					String name = warp.getName();

					value += TextFormatting.GREEN + name + TextFormatting.GOLD + ":{X: " + TextFormatting.GREEN + x + TextFormatting.GOLD + ", Y: " + TextFormatting.GREEN + y + TextFormatting.GOLD + ", Z: " + TextFormatting.GREEN + z + TextFormatting.GOLD + ", Dimension: " + TextFormatting.GREEN + TextFormatting.GOLD + "}*";
					names.add(value);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			value = value.replace("*", "\n");
			sendMessage(player, TextFormatting.GOLD + value);

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
	public int mapWarps(CommandSource source) {
		ServerPlayerEntity player;
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
				setPlayer(player);
				if (isAllowed(false, "You do not have permissions to use the warp mod")) {
					String value = "";
					try {
						importAllWarps(player);
						if (warps.getWarps().isEmpty() && public_warps.getWarps().isEmpty()) {
							sendMessage(player, TextFormatting.RED + "No Warps");
							return 1;
						}

						for (Warp warp : warps.getWarps()) {
							if (warp.getPlayer().equals(player)) {
								float x = warp.getX(), y = warp.getY(), z = warp.getZ();
								String name = warp.getName();
								value += TextFormatting.GREEN + name + TextFormatting.GOLD + ":{X: " + TextFormatting.GREEN + x + TextFormatting.GOLD + ", Y: " + TextFormatting.GREEN + y + TextFormatting.GOLD + ", Z: " + TextFormatting.GREEN + z + TextFormatting.GOLD + ", Dimension: " + TextFormatting.GREEN + warp.getDimensionResourceLocation() + TextFormatting.GOLD + "}*";
								names.add(value);
							}
						}

						for (Warp warp : public_warps.getWarps()) {
							float x = warp.getX(), y = warp.getY(), z = warp.getZ();
							String name = warp.getName();
							value += TextFormatting.GREEN + name + TextFormatting.GOLD + ":{X: " + TextFormatting.GREEN + x + TextFormatting.GOLD + ", Y: " + TextFormatting.GREEN + y + TextFormatting.GOLD + ", Z: " + TextFormatting.GREEN + z + TextFormatting.GOLD + ", Dimension: " + TextFormatting.GREEN + warp.getDimensionResourceLocation() + TextFormatting.GOLD + "}*";
							names.add(value);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

					value = value.replace("*", "\n");
					sendMessage(player, TextFormatting.GOLD + value);
				}
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

	public int mapPublicWarps(CommandSource source) {
		ServerPlayerEntity player;
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
				setPlayer(player);
				if (isAllowed(false, "You do not have permissions to use public warps")) {
					String value = "";
					try {
						importAllWarps(player);
						if (public_warps.getWarps().isEmpty()) {
							sendMessage(player, TextFormatting.RED + "No Warps");
							return 1;
						}

						for (Warp warp : public_warps.getWarps()) {
							float x = warp.getX(), y = warp.getY(), z = warp.getZ();
							String name = warp.getName();

							value += TextFormatting.GREEN + name + TextFormatting.GOLD + ":{X: " + TextFormatting.GREEN + x + TextFormatting.GOLD + ", Y: " + TextFormatting.GREEN + y + TextFormatting.GOLD + ", Z: " + TextFormatting.GREEN + z + TextFormatting.GOLD + ", Dimension: " + TextFormatting.GREEN + TextFormatting.GOLD + "}*";
							names.add(value);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					value = value.replace("*", "\n");
					sendMessage(player, TextFormatting.GOLD + value);
				}
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
	private List<ServerPlayerEntity> getOnlinePlayers(CommandSource source) {
		List<ServerPlayerEntity> players = new ArrayList<ServerPlayerEntity>();
		try {
			for (ServerPlayerEntity p : source.asPlayer().getServerWorld().getPlayers()) {
				players.add(p);
			}
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		return players;
	}

	public int remove(CommandSource source, String name) {
		ServerPlayerEntity player;
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			player = null;
		}
		if (player != null && !isRemote(player)) {
			setPlayer(player);
			if (isAllowed(false, "You do not have permissions to use the warp mod")) {
				importAllWarps(player);
				if (name.equals("*")) {
					for (Warp warp : warps.getWarps()) {
						if (warp.getPlayer().equals(player))
							warps.removeWarp(warp);
					}
					sendMessage(player, TextFormatting.LIGHT_PURPLE + "Removed all Privately Saved Warps");
					exportAllWarps(player);
					return 1;
				} else if (name.endsWith("*") && !(name.replace("*", "").isEmpty())) {

					for (Warp warp : warps.getWarps()) {
						name = name.replace("*", "");
						if (warp.getName().contains(name)) {
							warps.removeWarp(warp);

							exportAllWarps(player);
							sendMessage(player, TextFormatting.LIGHT_PURPLE + "Removed " + warp.getName().toUpperCase() + " from your Privately Saved Warps");
						}
					}

					return 1;
				}

				for (Warp warp : warps.getWarps()) {
					if (warp.getName().equals(name)) {
						warps.removeWarp(warp);

						sendMessage(player, TextFormatting.LIGHT_PURPLE + "Removed " + TextFormatting.GOLD + warp.getName().toUpperCase() + TextFormatting.LIGHT_PURPLE + " from the Privately Saved Warps");
						exportAllWarps(player);
						return 1;
					}
				}

				if (removePublic(source, name) == 0) {
					sendMessage(player, TextFormatting.LIGHT_PURPLE + name.toUpperCase() + TextFormatting.RED + " was not found!");
				}
			}
		} else if (isRemote(player)) {
			WarpMod.log(LogType.Warning, "Player is Remote.");
			return 0;
		} else if (player == null) {
			sendMessage(player, TextFormatting.RED + "Player is null");
			WarpMod.log(LogType.Error, "Player is null");
			return 0;
		}
		return 1;
	}

	public int removePublic(CommandSource source, String name) {
		ServerPlayerEntity player;
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			player = null;
		}
		if (player != null && !isRemote(player)) {
			setPlayer(player);
			if (isAllowed(true, "You do not have permissions to use public warps")) {
				importAllWarps(player);
				if (name.equals("*")) {
					public_warps.getWarps().clear();
					sendMessage(player, TextFormatting.LIGHT_PURPLE + "Removed all Public Warps");
					exportAllWarps(player);
					return 1;
				} else if (name.endsWith("*") && !(name.replace("*", "").isEmpty())) {

					for (Warp warp : public_warps.getWarps()) {
						name = name.replace("*", "");
						if (warp.getName().contains("*")) {
							public_warps.removeWarp(warp);

							exportAllWarps(player);
							sendMessage(player, TextFormatting.LIGHT_PURPLE + "Removed " + TextFormatting.GOLD + warp.getName().toUpperCase() + TextFormatting.LIGHT_PURPLE + " from the Publicly Saved Warps");
						}
					}

					sendMessage(player, TextFormatting.LIGHT_PURPLE + "No Warps were Removed");
					return 1;
				}

				for (Warp warp : public_warps.getWarps()) {
					if (warp.getName().equalsIgnoreCase(name)) {
						public_warps.removeWarp(warp);

						sendMessage(player, TextFormatting.LIGHT_PURPLE + "Removed " + TextFormatting.GOLD + warp.getName().toUpperCase() + TextFormatting.LIGHT_PURPLE + " from the Publicly Saved Warps");
						exportAllWarps(player);
						return 1;
					}
				}

			}
		} else if (isRemote(player)) {
			WarpMod.log(LogType.Warning, "Player is Remote.");
			return 0;
		} else if (player == null) {
			sendMessage(player, TextFormatting.RED + "Player is null");
			WarpMod.log(LogType.Error, "Player is null");
			return 0;
		}
		sendMessage(player, TextFormatting.LIGHT_PURPLE + name.toUpperCase() + TextFormatting.RED + " was not found!");
		return 0;
	}

	/**
	 * Renames warps.
	 * 
	 * @author Drew Chase
	 * @param oldName
	 * @param newName
	 */
	public int rename(CommandSource source, String oldName, String newName) {
		ServerPlayerEntity playerIn;
		try {
			playerIn = source.asPlayer();
		} catch (CommandSyntaxException e1) {
			e1.printStackTrace();
			playerIn = null;
		}
		if (playerIn != null) {
			if (!isRemote(playerIn)) {
				setPlayer(player);
				if (isAllowed(false, "You do not have permissions to use the warp mod")) {
					try {

						if (warps.getWarp(oldName) != null && warps.getWarp(oldName).getPlayer().equals(player)) {
							warps.renameWarp(warps.getWarp(oldName), newName);
							exportAllWarps(player);
							sendMessage(player, TextFormatting.GOLD + oldName + TextFormatting.GREEN + " renamed to " + TextFormatting.GOLD + newName);
						} else {
							renamePublic(source, oldName, newName);
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

	public int renamePublic(CommandSource source, String oldName, String newName) {
		ServerPlayerEntity playerIn;
		try {
			playerIn = source.asPlayer();
		} catch (CommandSyntaxException e1) {
			e1.printStackTrace();
			playerIn = null;
		}
		if (playerIn != null) {
			if (!isRemote(playerIn)) {
				setPlayer(player);
				if (isAllowed(false, "You do not have permissions to edit public warps")) {
					try {
						Warp warp = public_warps.getWarp(oldName);
						if (warp != null) {
							public_warps.renameWarp(warp, newName);
						}

						exportAllWarps(player);
						sendMessage(player, TextFormatting.GOLD + oldName + TextFormatting.GREEN + " renamed to " + TextFormatting.GOLD + newName);
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
	public int getHelp(CommandSource source) {
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
		ServerPlayerEntity player;
		try {
			player = (ServerPlayerEntity) (ServerPlayerEntity) source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			player = null;
		}
		if (player instanceof ServerPlayerEntity)
			setPlayer(player);
		else
			setPlayer(null);
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
		ServerPlayerEntity player;
		try {
			player = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			player = null;
		}
		if (player instanceof ServerPlayerEntity) {
			setPlayer(player);
		} else {
			player = null;
			setPlayer(null);
		}

		if (player != null) {
			if (!isRemote(player)) {
				if (isAllowed(false, "You do not have permissions to use the warp mod")) {
					Random ran = new Random();
					int var = ran.nextInt(range - 100) + 100;
					double x, y, z;
					x = player.getPosX() + (ran.nextBoolean() ? var * -1 : var);
					y = player.world.getHeight();
					z = player.getPosZ() + (ran.nextBoolean() ? var * -1 : var);
					BlockPos pos = new BlockPos(x, y, z);
					// Makes sure that the player is on solid ground

					while (player.world.getBlockState(pos).getBlock().equals(Blocks.AIR) || player.world.getBlockState(pos).getBlock().equals(Blocks.VOID_AIR)) {
						if (y <= 0) {
							sendMessage(player, TextFormatting.RED + "There is no safe place to land");
							return 1;
						}
						y--;
						pos = new BlockPos(x, y, z);
					}
					if (player.world.getBlockState(pos).getBlock().equals(Blocks.WATER) || player.world.getBlockState(pos).getBlock().equals(Blocks.LAVA) || player.world.getBlockState(pos).getBlock().equals(Blocks.AIR) || player.world.getBlockState(pos).getBlock().equals(Blocks.VOID_AIR)) {
						if (range < 20)
							warpRandom(source, 600);
						else
							warpRandom(source, range / 2);
						return 1;
					}
					// Increases the players y so that they are above ground
					y += 2;

					float pitch = player.getPitchYaw().y;
					float yaw = player.getPitchYaw().x;
					BlockPos oldPos = player.getPosition();
					Teleport.teleportToDimension(player, x, y, z, yaw, pitch, new WarpPlayer(player).getDimensionResourceLocation());
					back(oldPos, yaw, pitch, new WarpPlayer(player).getDimensionResourceLocation(), player);
					sendMessage(player, TextFormatting.AQUA + "Warping " + var + " blocks away!");
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
	public int invite(CommandSource source, String name, ServerPlayerEntity playerIn) {
		ServerPlayerEntity playerOut;
		try {
			playerOut = source.asPlayer();
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
			playerOut = null;
			return 0;
		}

		int i = 0;
		for (ServerPlayerEntity p : getOnlinePlayers(source)) {
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
			importAllWarps(playerOut);
			if (!isRemote(playerOut)) {
				setPlayer(playerOut);
				if (isAllowed(false, "You do not have permissions to use the warp mod")) {
					Warp warp = warps.getWarp(name);
					if (warp != null && warp.getPlayer().equals(playerOut)) {
						Warp copy = warp;
						copy.setPlayer(playerIn);
						if (!(warps.getWarp(name) != null && warps.getWarp(name).getPlayer().equals(playerIn)))
							warps.addWarp(copy);
						else {
							copy.setName(name + "_from_" + playerOut.getDisplayName().getString());
							warps.addWarp(copy);
						}
					}

					exportAllWarps(playerIn);
					sendMessage(playerIn, TextFormatting.GREEN + "You've Been Invited to " + TextFormatting.GOLD + name + TextFormatting.GREEN + " from " + TextFormatting.GOLD + playerOut.getDisplayName().getString());
					sendMessage(playerOut, TextFormatting.GOLD + "Successfully Invited " + TextFormatting.GREEN + playerIn.getDisplayName().getString() + TextFormatting.GOLD + " to " + TextFormatting.GREEN + name);
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
	public boolean isRemote(ServerPlayerEntity player) {
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
	public void export(ServerPlayerEntity player) {
		if (!isRemote(player)) {
			setPlayer(player);
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
						String name = warp.getName();
						float yaw = warp.getYaw();
						float pitch = warp.getPitch();
						warpString[index] = name.replace(":", "*01") + ": X:" + x + " Y:" + y + " Z:" + z + " World:" + warp.getDimensionResourceLocation().toString().replace(":", "*01") + " Yaw:" + yaw + " Pitch:" + pitch;
						index++;
					}
				}
				try {
					TextWriter(Arrays.toString(warpString).replace(", ", "\n").replace("[", "").replace("]", ""), true, player.getDisplayName().getString());
				} catch (IOException e) {
					sendMessage(player, TextFormatting.RED + "COULD NOT CREATE OR CONNECT TO WARP CONFIG");
					WarpMod.log(LogType.Error, "ERROR: " + e.getMessage());
					e.printStackTrace();
				} catch (Exception e) {
					sendMessage(player, TextFormatting.RED + "ERROR: " + e.getMessage());
					WarpMod.log(LogType.Error, "ERROR: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	public void exportPublicWarps() {
		if (!isRemote(player)) {
			int index = public_warps.getWarps().size();
			String[] warpString = new String[index];
			index = 0;
			for (Warp warp : public_warps.getWarps()) {
				String name = warp.getName();
				int x = warp.getX(), y = warp.getY(), z = warp.getZ();
				float yaw = warp.getYaw();
				float pitch = warp.getPitch();
				warpString[index] = name.replace(":", "*01") + ": X:" + x + " Y:" + y + " Z:" + z + " World:" + warp.getDimensionResourceLocation().toString().replace(":", "*01") + " Yaw:" + yaw + " Pitch:" + pitch;
				index++;
			}
			try {
				TextWriter(Arrays.toString(warpString).replace(", ", "\n").replace("[", "").replace("]", ""), true, "public");
			} catch (IOException e) {
				sendMessage(player, TextFormatting.RED + "COULD NOT CREATE OR CONNECT TO PUBLIC WARP CONFIG");
				WarpMod.log(LogType.Error, "ERROR: " + e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				sendMessage(player, TextFormatting.RED + "ERROR: " + e.getMessage());
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
		if (!isRemote(getPlayer())) {
			File f = new File(FileLocation);

			try {
				if (f.mkdirs()) {
					WarpMod.log(LogType.Debug, "Warp File Created in " + f.getAbsolutePath());
				}
			} catch (Exception e) {
				WarpMod.log(LogType.Error, "Couldn't Create File");
				e.printStackTrace();
			}
			if (getPlayer().server.isDedicatedServer()) {
				FileName = String.format("%s.conf", name);
			} else {
				FileName = String.format("%s_%s.conf", name, player.getServer().getWorldIconFile().getParentFile().getName());
			}

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

	public void setPlayer(ServerPlayerEntity player) {
		ConfigHandler.readConfig();
		this.player = player;
	}

	public ServerPlayerEntity getPlayer() {
		return this.player;
	}

	/**
	 * Sends message to current player
	 * 
	 * @param player
	 * @param message
	 */
	private void sendMessage(ServerPlayerEntity player, Object message) {
		WarpMod.sendMessage(player, message);
	}

}