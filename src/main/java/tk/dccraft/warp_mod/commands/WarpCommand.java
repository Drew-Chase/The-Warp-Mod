package tk.dccraft.warp_mod.commands;

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

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import tk.dccraft.warp_mod.Main;
import tk.dccraft.warp_mod.commands.util.Teleport;
import tk.dccraft.warp_mod.util.ConfigHandler;
import tk.dccraft.warp_mod.util.WarpUtilities;

/**
 * 
 * Base command structure
 * 
 * @author Drew Chase
 *
 */

@SuppressWarnings("all")
public class WarpCommand extends WarpUtilities implements ICommand {
	public static WarpCommand instance;

	MinecraftServer server;

	@Override
	public String getName() {
		return "warp";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "warp help";
	}

	@Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		/**
		 * Checks if the command sender is a player
		 */
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			// sets the current player
			setPlayer(player);
			getWarps(player);
			if (!isRemote()) {
				setPlayer(player);
				getWarps(player);
				if (args.length == 0) {
					sendMessage(TextFormatting.GREEN + "Type /warp help");
				}

				if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
					sendMessage(getHelp());
				}

				if (args[0].equalsIgnoreCase("cmd")) {
					for (String s : getCommands()) {
						sendMessage(TextFormatting.AQUA + s);
					}
					return;
				}

				if (args[0].equalsIgnoreCase("reload")) {
					ConfigHandler.reloadConfig();
					getWarps(getPlayer());
					sendMessage("Warp Config Reloaded");
				}

				if (args[0].equalsIgnoreCase("set")) {
					addCommand("set");
					if (args[1].equalsIgnoreCase("back")) {
						sendMessage(new TextComponentString(TextFormatting.RED + "back is a pre-allocated warp name"));
						return;
					}
					createWarp(args[1].toLowerCase(), player);
					return;
				}

				if (args[0].equalsIgnoreCase("remove")) {
					if (args[1].isEmpty()) {
						sendMessage(new TextComponentString(TextFormatting.RED + "/warp remove <name>"));
					} else if (args[1].equalsIgnoreCase("*") || args[1].equalsIgnoreCase("all")) {
						for (String s : getWarps(player)) {
							removeWarp(s, player);
						}
						return;
					} else {
						for (String s : getWarps(player)) {
							if (args[1].equalsIgnoreCase(s)) {
								removeWarp(args[1], player);
								return;
							}
							if (args[1].contains(s) && args[1].endsWith("*")) {
								removeWarp(s, player);
							}
						}
						return;
					}
				}

				if (args[0].equalsIgnoreCase("list")) {
					if (args.length == 2) {
						for (EntityPlayerMP p : getOnlinePlayers()) {
							if (args[1].equalsIgnoreCase(p.getDisplayNameString())) {
								if (getWarps(p).isEmpty()) {
									sendMessage(TextFormatting.GOLD + p.getDisplayNameString() + TextFormatting.GREEN + " has No Warps Saved");
									return;
								}
								String s = "";
								for (String name : getWarps(p)) {
									if (getWarps(p).get(getWarps(p).size() - 1).equals(name))
										s += name;
									else
										s += name + ", ";
								}
								return;
							}
						}
						sendMessage(TextFormatting.RED + "Something Went Wrong");
						return;
					}
					if (getWarps(player).isEmpty()) {
						sendMessage(TextFormatting.GOLD + "No Warps Saved");
						return;
					}
					String s = "";
					for (String name : getWarps(player)) {
						if (getWarps(player).get(getWarps(player).size() - 1).equals(name))
							s += name;
						else
							s += name + ", ";
					}
					sendMessage(TextFormatting.GOLD + s);
					return;
				}

				if (args[0].equalsIgnoreCase("invite")) {
					if (args.length >= 3) {
						for (EntityPlayerMP p : getOnlinePlayers()) {
							for (String s : getWarps(player)) {
								if (args[1].equalsIgnoreCase(s) && args[2].equalsIgnoreCase(p.getDisplayNameString())) {
									invite(s, p);
									sendMessage(TextFormatting.GOLD + p.getDisplayNameString() + TextFormatting.AQUA + " was invited to " + TextFormatting.GOLD + s);
									return;
								}
							}

							if (!getOnlinePlayers().iterator().hasNext()) {
								sendMessage(TextFormatting.RED + "Player's not Online");
								return;
							}

							if (!getWarps(player).iterator().hasNext()) {
								sendMessage(TextFormatting.RED + "Warp Doesn't Exist");
								return;
							}

						}
					}
					sendMessage(TextFormatting.RED + "Syntax Error has Occurred: " + TextFormatting.DARK_PURPLE + "/warp invite <warp name:" + args[1] + "> <player name:" + args[2] + ">");
					return;
				}

				if (args[0].equalsIgnoreCase("random")) {
					if (args.length == 2) {
						int range = 0;
						try {
							range = Integer.parseInt(args[1]);
							warpRandom(range);
							return;
						} catch (NumberFormatException e) {
							sendMessage(TextFormatting.RED + "/warp random <int>");
							return;
						} catch (Exception e) {
							warpRandom(600);
							sendMessage(TextFormatting.RED + "Something Went Wrong");
							return;
						}
					} else {
						warpRandom(600);
						return;
					}
				}

				if (args[0].equalsIgnoreCase("grab")) {
					if (args.length == 3) {
						for (EntityPlayerMP p : getOnlinePlayers()) {
							if (args[1].equalsIgnoreCase(p.getDisplayNameString())) {
								for (String s : getWarps(p)) {
									if (args[2].equalsIgnoreCase(s)) {
										grab(s, p);
									}
								}
							}
						}
					}
				}

				if (args[0].equalsIgnoreCase("rename")) {
					if (args.length == 3) {
						for (String s : getWarps(player)) {
							if (args[1].equalsIgnoreCase(s)) {
								rename(s, args[2]);
								return;
							}
						}
						sendMessage(TextFormatting.GOLD + args[1] + TextFormatting.RED + " doesn't exist");
						return;
					}
					sendMessage(TextFormatting.GOLD + (args.length + "") + TextFormatting.RED + " is the length");
					return;
				}

				if (args[0].equalsIgnoreCase("force_export")) {
					export(player);
					return;
				}

				if (args[0].equalsIgnoreCase("me")) {
					if (server.getPlayerList().getPlayers().size() > 1) {
						for (EntityPlayer p : server.getPlayerList().getPlayers()) {
							if (args[1].equalsIgnoreCase(p.getDisplayNameString())) {
								float yaw = p.cameraYaw;
								float pitch = p.cameraPitch;
								Teleport.teleportToDimension(player, p.getEntityWorld().provider.getDimension(), p.getPosition().getX(), p.getPosition().getY(), p.getPosition().getZ(), yaw, pitch);
								return;
							}
						}
					} else {
						sendMessage(new TextComponentString(TextFormatting.GREEN + "Huh.... You Know You're all alone right?"));
						return;
					}
				}
				if (args.length > 1 && args[1].equalsIgnoreCase("me")) {
					if (server.getPlayerList().getPlayers().size() > 1) {
						for (EntityPlayer p : server.getPlayerList().getPlayers()) {
							addCommand(p.getDisplayNameString());
							if (args[0].equalsIgnoreCase(p.getDisplayNameString())) {
								float yaw = p.cameraYaw;
								float pitch = p.cameraPitch;
								Teleport.teleportToDimension(p, player.getEntityWorld().provider.getDimension(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), yaw, pitch);
								return;
							}
						}
					} else {
						sendMessage(new TextComponentString(TextFormatting.GREEN + "Huh.... You Know You're all alone right?"));
						return;
					}
				}
				if (args.length == 1) {
					for (String name : getWarps(player)) {
						if (args[0].equalsIgnoreCase(name)) {
							warpTo(name.toLowerCase(), player);
							return;
						}
					}
					for (String s : getCommands()) {
						if (args[0].equalsIgnoreCase(s))
							return;
					}
					if (args[0].equalsIgnoreCase("map")) {
						Map(args);
						return;
					}
					sendMessage(TextFormatting.RED + "Warp Doesn't Exist");
				}
			}
		} else {
			if (args[0].equalsIgnoreCase("reload")) {
				for (EntityPlayerMP p : getOnlinePlayers()) {
					getWarps(p);
				}
				ConfigHandler.reloadConfig();
				return;
			}
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("list")) {
					for (EntityPlayerMP p : getOnlinePlayers()) {
						if (args[1].equalsIgnoreCase(p.getDisplayNameString())) {
							for (String s : getWarps(p)) {
								if (!s.equalsIgnoreCase(getWarps(p).get(getWarps(p).size() - 1))) {
									Main.instance.consoleMessage(s + ", ");
								}
								Main.instance.consoleMessage(s);
							}
						}
					}
					return;
				}
			}
			Main.instance.consoleMessage("Sender isn't a Player");
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		List<String> list = new ArrayList<String>();
		if (sender instanceof EntityPlayer) {
			setPlayer((EntityPlayer) sender);
			getCommands().clear();
			addCommand("set", "rename", "random", "map", "list", "invite", "remove", "me", "reload", "force_export");
			if (args.length > 0) {
				if (args.length == 1) {
					for (String s : getWarps(getPlayer())) {
						if (s.startsWith(args[0]))
							list.add(s);
					}
					for (String s : getCommands()) {
						if (s.startsWith(args[0]))
							list.add(s);
					}

					for (EntityPlayerMP p : getOnlinePlayers()) {
						if (p.getDisplayNameString().startsWith(args[0])) {
							list.add(p.getDisplayNameString());
						}
					}

					return list;
				} else if (args.length == 2) {
					if (args[0].equalsIgnoreCase("rename")) {
						for (String s : getWarps(getPlayer())) {
							if (s.startsWith(args[1])) {
								list.add(s);
							}
						}
						return list;
					}

					if (args[0].equalsIgnoreCase("list")) {
						for (EntityPlayerMP p : getOnlinePlayers()) {
							if (p.getDisplayNameString().startsWith(args[1])) {
								list.add(p.getDisplayNameString());
							}
						}
						return list;
					}

					for (EntityPlayerMP p : getOnlinePlayers()) {
						if (args[0].equalsIgnoreCase(p.getDisplayNameString())) {
							list.add("me");
							return list;
						}
					}

					if (args[0].equalsIgnoreCase("me")) {
						for (EntityPlayerMP p : getOnlinePlayers()) {
							if (p.getDisplayNameString().startsWith(args[1])) {
								list.add(p.getDisplayNameString());
							}
						}
						return list;
					}

					if (args[0].equalsIgnoreCase("invite")) {
						for (String s : getWarps(getPlayer())) {
							if (s.startsWith(args[1])) {
								list.add(s);
							}
						}
						return list;
					}

					if (args[0].equalsIgnoreCase("remove")) {
						for (String s : getWarps(getPlayer())) {
							if (s.startsWith(args[1])) {
								list.add(s);
							}
						}
						if ("*".startsWith(args[1]))
							list.add("*");
						if ("all".startsWith(args[1]))
							list.add("all");
						return list;
					}
				} else if (args.length == 3) {

					if (args[0].equalsIgnoreCase("invite")) {
						for (EntityPlayerMP p : getOnlinePlayers()) {
							if (p.getDisplayNameString().startsWith(args[2])) {
								list.add(p.getDisplayNameString());
							}
						}
						return list;
					}
				}
			}
		} else {
			return Arrays.asList("No... Something went wrong");
		}
		return list;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	@Override
	public int compareTo(ICommand arg0) {
		return 0;
	}

}
