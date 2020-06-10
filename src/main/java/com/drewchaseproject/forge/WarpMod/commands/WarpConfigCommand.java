package com.drewchaseproject.forge.WarpMod.commands;

import java.util.ArrayList;
import java.util.List;

import com.drewchaseproject.forge.WarpMod.WarpMod;
import com.drewchaseproject.forge.WarpMod.config.ConfigHandler;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

public final class WarpConfigCommand implements ICommand {

	EntityPlayer player;

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			player = (EntityPlayer) sender;

			if (player != null && !player.getServer().isSinglePlayer()) {
				if (args.length != 0) {
					if (args[0].equalsIgnoreCase("set") && args.length == 3) {
						if (args[1].equalsIgnoreCase("debug")) {
							if (args[2].equalsIgnoreCase("true")) {
								setDebug(true);
							} else if (args[2].equalsIgnoreCase("false")) {
								setDebug(false);
							} else {
								sendMessage(TextFormatting.RED + "Debug can NOT be set to " + args[2] + ".  It has to be true or false");
							}
						}

						if (args[1].equalsIgnoreCase("public-allowed")) {
							if (args[2].equalsIgnoreCase("true")) {
								setPublicAllowed(true);
							} else if (args[2].equalsIgnoreCase("false")) {
								setPublicAllowed(false);
							} else {
								sendMessage(TextFormatting.RED + "public-allowed can NOT be set to " + args[2] + ".  It has to be true or false");
							}
						}
					}

					if (args[0].equalsIgnoreCase("add") && args.length == 3) {

						if (args[1].equalsIgnoreCase("config-editors")) {
							EntityPlayerMP editor = null;
							for (EntityPlayerMP player : getPlayer().getServer().getEntityWorld().getMinecraftServer().getPlayerList().getPlayers()) {
								if (args[2].equalsIgnoreCase(player.getDisplayNameString())) {
									editor = player;
									break;
								}
							}
							addConfigEditor(editor);
						}

						if (args[1].equalsIgnoreCase("allowed-players")) {
							if (args[2].equalsIgnoreCase("*") || args[2].equalsIgnoreCase("@a")) {
								addAllowedPlayer((EntityPlayerMP[]) getPlayer().getServer().getEntityWorld().getMinecraftServer().getPlayerList().getPlayers().toArray());
							} else {
								EntityPlayerMP allowed = null;
								for (EntityPlayerMP player : getPlayer().getServer().getEntityWorld().getMinecraftServer().getPlayerList().getPlayers()) {
									if (args[2].equalsIgnoreCase(player.getDisplayNameString())) {
										allowed = player;
										break;
									}
								}
								addAllowedPlayer(allowed);
							}
						}

						if (args[1].equalsIgnoreCase("allowed-public-players")) {
							addAllowedPublicPlayers((EntityPlayerMP[]) getPlayer().getServer().getEntityWorld().getMinecraftServer().getPlayerList().getPlayers().toArray());
						} else {

							EntityPlayerMP allowed = null;
							for (EntityPlayerMP player : getPlayer().getServer().getEntityWorld().getMinecraftServer().getPlayerList().getPlayers()) {
								if (args[2].equalsIgnoreCase(player.getDisplayNameString())) {
									allowed = player;
									break;
								}
							}
							addAllowedPublicPlayers(allowed);
						}
					}

					if (args[0].equalsIgnoreCase("remove") && args.length == 3) {

						if (args[1].equalsIgnoreCase("config-editors")) {
							EntityPlayerMP editor = null;
							for (EntityPlayerMP player : getPlayer().getServer().getEntityWorld().getMinecraftServer().getPlayerList().getPlayers()) {
								if (args[2].equalsIgnoreCase(player.getDisplayNameString())) {
									editor = player;
									break;
								}
							}
							removeConfigEditor(editor);
						}

						if (args[1].equalsIgnoreCase("allowed-players")) {
							if (args[2].equalsIgnoreCase("*") || args[2].equalsIgnoreCase("@a")) {
								clearAllowedPlayers();
							} else {
								EntityPlayerMP allowed = null;
								for (EntityPlayerMP player : getPlayer().getServer().getEntityWorld().getMinecraftServer().getPlayerList().getPlayers()) {
									if (args[2].equalsIgnoreCase(player.getDisplayNameString())) {
										allowed = player;
										break;
									}
								}
								removeAllowedPlayers(allowed);
							}
						}

						if (args[1].equalsIgnoreCase("allowed-public-players")) {
							clearAllowedPublicPlayers();
						} else {
							EntityPlayerMP allowed = null;
							for (EntityPlayerMP player : getPlayer().getServer().getEntityWorld().getMinecraftServer().getPlayerList().getPlayers()) {
								if (args[2].equalsIgnoreCase(player.getDisplayNameString())) {
									allowed = player;
									break;
								}
							}
							removeAllowedPublicPlayers(allowed);
						}
					}

					if (args[0].equalsIgnoreCase("get") && args.length == 2) {

						if (args[1].equalsIgnoreCase("debug")) {
							getDebugMode();
						}

						if (args[1].equalsIgnoreCase("public-allowed")) {
							getPublicWarps();
						}

						if (args[1].equalsIgnoreCase("config-editors")) {
							getConfigEditor();
						}

						if (args[1].equalsIgnoreCase("allowed-players")) {
							getAllowedPlayers();
						}

						if (args[1].equalsIgnoreCase("allowed-public-players")) {
							getAllowedPublicPlayers();
						}
					}
				}
			} else if (player == null) {
				return;
			} else if (player.getServer().isSinglePlayer()) {
				sendMessage(TextFormatting.RED + "warp-config command is not available in single-player".toUpperCase());
			}
		}
	}

	private int getDebugMode() {
		if (isAllowed()) {
			String value = ConfigHandler.getDebugMode() ? "Enabled" : "Disabled";
			sendMessage(TextFormatting.AQUA + "Debug Mode is " + TextFormatting.GOLD + value);
		}
		return 1;
	}

	private int getPublicWarps() {
		if (isAllowed()) {
			String value = ConfigHandler.getPublicWarpsAllowed() ? "Enabled" : "Disabled";
			sendMessage(TextFormatting.AQUA + "Public Warps are " + TextFormatting.GOLD + value);
		}
		return 1;
	}

	private int getConfigEditor() {
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (String s : ConfigHandler.getAllowedConfigPlayers()) {
				if (index == (ConfigHandler.getAllowedConfigPlayers().size() - 1))
					value += s;
				else
					value += s + ", ";
				index++;
			}
			if (index > 0)
				sendMessage(TextFormatting.GOLD + value + TextFormatting.AQUA + " are Allowed to Edit Config");
			else
				sendMessage(TextFormatting.GOLD + "No Players are Allowed to Edit Config\nHmm How are you using this Command");
		}
		return 1;
	}

	private int getAllowedPublicPlayers() {
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (String s : ConfigHandler.getAllowedPublicPlayers()) {
				if (index == (ConfigHandler.getAllowedPublicPlayers().size() - 1))
					value += s;
				else
					value += s + ", ";
				index++;
			}
			if (index > 0)
				sendMessage(TextFormatting.GOLD + value + TextFormatting.AQUA + " are Allowed to Make Public Warps");
			else
				sendMessage(TextFormatting.GOLD + "No Players Are Allowed to Make Public Warps!");
		}
		return 1;
	}

	private int getAllowedPlayers() {
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (String s : ConfigHandler.getAllowedPlayers()) {
				if (index == (ConfigHandler.getAllowedPlayers().size() - 1))
					value += s;
				else
					value += s + ", ";
				index++;
			}
			if (index > 0)
				sendMessage(TextFormatting.GOLD + value + TextFormatting.AQUA + " are Allowed to Use The Warp Mod");
			else
				sendMessage(TextFormatting.GOLD + "No Players Are Allowed!");
		}
		return 1;
	}

	private int removeConfigEditor(EntityPlayerMP... players) {
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (EntityPlayer player : players) {
				if (index == (players.length - 1))
					value += player.getDisplayName().toString();
				else
					value += player.getDisplayName().toString() + ", ";
				ConfigHandler.removeAllowedConfigPlayers(player.getDisplayName().toString());
				index++;
			}
			sendMessage(TextFormatting.AQUA + "Removed " + TextFormatting.GOLD + value + TextFormatting.AQUA + " to Allowed Config Editors Players");
			sendMessage(TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
		return 1;
	}

	private int removeAllowedPublicPlayers(EntityPlayerMP... players) {
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (EntityPlayer player : players) {
				if (index == (players.length - 1))
					value += player.getDisplayName().toString();
				else
					value += player.getDisplayName().toString() + ", ";
				ConfigHandler.removeAllowedPublicPlayer(player.getDisplayName().toString());
				index++;
			}
			sendMessage(TextFormatting.AQUA + "Added " + TextFormatting.GOLD + value + TextFormatting.AQUA + " to Allowed Public Players");
			sendMessage(TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
		return 1;
	}

	private int removeAllowedPlayers(EntityPlayerMP... players) {
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (EntityPlayer player : players) {
				if (index == (players.length - 1))
					value += player.getDisplayName().toString();
				else
					value += player.getDisplayName().toString() + ", ";
				ConfigHandler.removeAllowedPlayers(player.getDisplayName().toString());
				index++;
			}
			sendMessage(TextFormatting.AQUA + "Added " + TextFormatting.GOLD + value + TextFormatting.AQUA + " to Allowed Players");
			sendMessage(TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
		return 1;
	}

	private void clearAllowedPlayers() {
		if (isAllowed()) {
			ConfigHandler.clearAllowedPlayers();
			sendMessage(TextFormatting.AQUA + "Removed all Allowed Players");
			sendMessage(TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
	}

	private void clearAllowedPublicPlayers() {
		if (isAllowed()) {
			ConfigHandler.clearAllowedPublicPlayers();
			sendMessage(TextFormatting.AQUA + "Removed all Allowed Players");
			sendMessage(TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
	}

	private boolean isAllowed() {
		if (getPlayer().getServer().isSinglePlayer())
			return true;
		if (getPlayer() == null)
			return true;
		ConfigHandler.readConfig();
		for (String s : ConfigHandler.getAllowedConfigPlayers()) {
			if (s.equalsIgnoreCase(getPlayer().getDisplayName().toString())) {
				return true;
			}
		}
		sendMessage(TextFormatting.RED + "You Do not Have Permissions to use this Command\nContact Server Admin or consult the mods config to edit.");
		return false;
	}

	private int addConfigEditor(EntityPlayerMP... players) {
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (EntityPlayer player : players) {
				if (index == (players.length - 1))
					value += player.getDisplayName().toString();
				else
					value += player.getDisplayName().toString() + ", ";
				ConfigHandler.addAllowedConfigPlayers(player.getDisplayName().toString());
				index++;
			}
			sendMessage(TextFormatting.AQUA + "Added " + TextFormatting.GOLD + value + TextFormatting.AQUA + " to Allowed Config Editors Players");
			sendMessage(TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
		return 1;
	}

	private int addAllowedPublicPlayers(EntityPlayerMP... players) {
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (EntityPlayer player : players) {
				if (index == (players.length - 1))
					value += player.getDisplayName().toString();
				else
					value += player.getDisplayName().toString() + ", ";
				ConfigHandler.addAllowedPublicPlayer(player.getDisplayName().toString());
				index++;
			}
			sendMessage(TextFormatting.AQUA + "Added " + TextFormatting.GOLD + value + TextFormatting.AQUA + " to Allowed Public Players");
			sendMessage(TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
		return 1;
	}

	private int addAllowedPlayer(EntityPlayerMP... players) {
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (EntityPlayer player : players) {
				if (index == (players.length - 1))
					value += player.getDisplayName().toString();
				else
					value += player.getDisplayName().toString() + ", ";
				ConfigHandler.addAllowedPlayers(player.getDisplayName().toString());
				index++;
			}
			sendMessage(TextFormatting.AQUA + "Added " + TextFormatting.GOLD + value + TextFormatting.AQUA + " to Allowed Players");
			sendMessage(TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
		return 1;
	}

	private int setPublicAllowed(boolean bool) {
		if (isAllowed()) {
			sendMessage(TextFormatting.AQUA + "Set Public Warps Allowed to " + TextFormatting.GOLD + bool);
			sendMessage(TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.setPublicWarpsAllowed(bool);
			ConfigHandler.writeConfig();
		}
		return 1;
	}

	/**
	 * Sets the Debug Mode Config
	 * 
	 * @param source
	 * @param bool
	 * @return
	 */
	private int setDebug(boolean bool) {
		if (isAllowed()) {
			sendMessage(TextFormatting.AQUA + "Set Debug Mode to " + TextFormatting.GOLD + bool);
			sendMessage(TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.setDebugMode(bool);
			ConfigHandler.writeConfig();
		}

		return 1;
	}

	private EntityPlayer getPlayer() {
		return this.player;
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

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public String getName() {
		return "warp-config";
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
		List<String> value = new ArrayList<String>();
		if (args.length == 1) {
			if ("get".startsWith(args[0]))
				value.add("get");
			if ("set".startsWith(args[0]))
				value.add("set");
			if ("add".startsWith(args[0]))
				value.add("add");
			if ("remove".startsWith(args[0]))
				value.add("remove");
		}

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("get")) {
				if ("debug".startsWith(args[1]))
					value.add("debug");
				if ("public-allowed".startsWith(args[1]))
					value.add("public-allowed");
				if ("allowed-public-players".startsWith(args[1]))
					value.add("allowed-public-players");
				if ("allowed-players".startsWith(args[1]))
					value.add("allowed-players");
				if ("config-editors".startsWith(args[1]))
					value.add("config-editors");
			}

			if (args[0].equalsIgnoreCase("set")) {
				if ("debug".startsWith(args[1]))
					value.add("debug");
				if ("public-allowed".startsWith(args[1]))
					value.add("public-allowed");
			}

			if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
				if ("allowed-public-players".startsWith(args[1]))
					value.add("allowed-public-players");
				if ("allowed-players".startsWith(args[1]))
					value.add("allowed-players");
				if ("config-editors".startsWith(args[1]))
					value.add("config-editors");
			}
		}
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("set")) {
				if (args[1].equalsIgnoreCase("debug") || args[1].equalsIgnoreCase("public-allowed")) {
					if ("true".startsWith(args[2]))
						value.add("true");
					else
						value.add("false");
				}
			}
			if (args[0].equalsIgnoreCase("remove")) {
				value.add("*");
				for (EntityPlayer player : server.getPlayerList().getPlayers()) {
					if (player.getDisplayNameString().startsWith(args[2]))
						value.add(player.getDisplayNameString());
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
