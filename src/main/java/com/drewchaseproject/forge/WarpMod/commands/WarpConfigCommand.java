package com.drewchaseproject.forge.WarpMod.commands;

import java.util.Collection;

import com.drewchaseproject.forge.WarpMod.WarpMod;
import com.drewchaseproject.forge.WarpMod.config.ConfigHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TextFormatting;

public final class WarpConfigCommand {

	PlayerEntity player;

	public void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("warp-config")
						.then(Commands.literal("set")
								.then(Commands.literal("debug")
										.then(Commands.argument("debug", BoolArgumentType.bool())
												.executes(context -> setDebug(context.getSource(), BoolArgumentType.getBool(context, "debug")))))
								.then(Commands.literal("public-allowed")
										.then(Commands.argument("public-allowed", BoolArgumentType.bool())
												.executes(context -> setPublicAllowed(context.getSource(), BoolArgumentType.getBool(context, "public-allowed"))))))
						.then(Commands.literal("add")
								.then(Commands.literal("allowed-players")
										.then(Commands.argument("player-allowed", EntityArgument.players())
												.executes(context -> addAllowedPlayers(context.getSource(), EntityArgument.getPlayers(context, "player-allowed")))))
								.then(Commands.literal("allowed-public-players")
										.then(Commands.argument("public-player-allowed", EntityArgument.players())
												.executes(context -> addAllowedPublicPlayers(context.getSource(), EntityArgument.getPlayers(context, "public-player-allowed")))))
								.then(Commands.literal("config-editors")
										.then(Commands.argument("config", EntityArgument.players())
												.executes(context -> addConfigEditor(context.getSource(), EntityArgument.getPlayers(context, "config"))))))
						.then(Commands.literal("remove")
								.then(Commands.literal("allowed-players")
										.then(Commands.argument("player-allowed", EntityArgument.players())
												.executes(context -> removeAllowedPlayers(context.getSource(), EntityArgument.getPlayers(context, "player-allowed")))))
								.then(Commands.literal("allowed-public-players")
										.then(Commands.argument("public-player-allowed", EntityArgument.players())
												.executes(context -> removeAllowedPublicPlayers(context.getSource(), EntityArgument.getPlayers(context, "public-player-allowed")))))
								.then(Commands.literal("config-editors")
										.then(Commands.argument("config", EntityArgument.players())
												.executes(context -> removeConfigEditor(context.getSource(), EntityArgument.getPlayers(context, "config"))))))
						.then(Commands.literal("get")
								.then(Commands.literal("public-warps")
										.executes(context -> getPublicWarps(context.getSource())))
								.then(Commands.literal("debug")
										.executes(context -> getDebugMode(context.getSource())))
								.then(Commands.literal("allowed-players")
										.executes(context -> getAllowedPlayers(context.getSource())))
								.then(Commands.literal("allowed-public-players")
										.executes(context -> getAllowedPublicPlayers(context.getSource())))
								.then(Commands.literal("config-editors")
										.executes(context -> getConfigEditor(context.getSource())))));
	}

	private int getDebugMode(CommandSource source) {
		if (source.getEntity() instanceof PlayerEntity) {
			PlayerEntity player1 = (PlayerEntity) source.getEntity();
			setPlayer(player1);
		} else {
			setPlayer(null);
		}
		if (isAllowed()) {
			String value = ConfigHandler.getDebugMode() ? "Enabled" : "Disabled";
			sendMessage(source.getEntity(), TextFormatting.AQUA + "Debug Mode is " + TextFormatting.GOLD + value);
		}
		return 1;
	}

	private int getPublicWarps(CommandSource source) {
		if (source.getEntity() instanceof PlayerEntity) {
			PlayerEntity player1 = (PlayerEntity) source.getEntity();
			setPlayer(player1);
		} else {
			setPlayer(null);
		}
		if (isAllowed()) {
			String value = ConfigHandler.getPublicWarpsAllowed() ? "Enabled" : "Disabled";
			sendMessage(source.getEntity(), TextFormatting.AQUA + "Public Warps are " + TextFormatting.GOLD + value);
		}
		return 1;
	}

	private int getConfigEditor(CommandSource source) {
		if (source.getEntity() instanceof PlayerEntity) {
			PlayerEntity player1 = (PlayerEntity) source.getEntity();
			setPlayer(player1);
		} else {
			setPlayer(null);
		}
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
				sendMessage(source.getEntity(), TextFormatting.GOLD + value + TextFormatting.AQUA + " are Allowed to Edit Config");
			else
				sendMessage(source.getEntity(), TextFormatting.GOLD + "No Players are Allowed to Edit Config\nHmm How are you using this Command");
		}
		return 1;
	}

	private int getAllowedPublicPlayers(CommandSource source) {
		if (source.getEntity() instanceof PlayerEntity) {
			PlayerEntity player1 = (PlayerEntity) source.getEntity();
			setPlayer(player1);
		} else {
			setPlayer(null);
		}
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
				sendMessage(source.getEntity(), TextFormatting.GOLD + value + TextFormatting.AQUA + " are Allowed to Make Public Warps");
			else
				sendMessage(source.getEntity(), TextFormatting.GOLD + "No Players Are Allowed to Make Public Warps!");
		}
		return 1;
	}

	private int getAllowedPlayers(CommandSource source) {
		if (source.getEntity() instanceof PlayerEntity) {
			PlayerEntity player1 = (PlayerEntity) source.getEntity();
			setPlayer(player1);
		} else {
			setPlayer(null);
		}
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
				sendMessage(source.getEntity(), TextFormatting.GOLD + value + TextFormatting.AQUA + " are Allowed to Use The Warp Mod");
			else
				sendMessage(source.getEntity(), TextFormatting.GOLD + "No Players Are Allowed!");
		}
		return 1;
	}

	private int removeConfigEditor(CommandSource source, Collection<ServerPlayerEntity> players) {
		if (source.getEntity() instanceof PlayerEntity) {
			PlayerEntity player1 = (PlayerEntity) source.getEntity();
			setPlayer(player1);
		} else {
			setPlayer(null);
		}
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (ServerPlayerEntity player : players) {
				if (index == (players.size() - 1))
					value += player.getDisplayName().getString();
				else
					value += player.getDisplayName().getString() + ", ";
				ConfigHandler.removeAllowedConfigPlayers(player.getDisplayName().getString());
				index++;
			}
			sendMessage(source.getEntity(), TextFormatting.AQUA + "Removed " + TextFormatting.GOLD + value + TextFormatting.AQUA + " to Allowed Config Editors Players");
			sendMessage(source.getEntity(), TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
		return 1;
	}

	private int removeAllowedPublicPlayers(CommandSource source, Collection<ServerPlayerEntity> players) {
		if (source.getEntity() instanceof PlayerEntity) {
			PlayerEntity player1 = (PlayerEntity) source.getEntity();
			setPlayer(player1);
		} else {
			setPlayer(null);
		}
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (ServerPlayerEntity player : players) {
				if (index == (players.size() - 1))
					value += player.getDisplayName().getString();
				else
					value += player.getDisplayName().getString() + ", ";
				ConfigHandler.removeAllowedPublicPlayer(player.getDisplayName().getString());
				index++;
			}
			sendMessage(source.getEntity(), TextFormatting.AQUA + "Added " + TextFormatting.GOLD + value + TextFormatting.AQUA + " to Allowed Public Players");
			sendMessage(source.getEntity(), TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
		return 1;
	}

	private int removeAllowedPlayers(CommandSource source, Collection<ServerPlayerEntity> players) {
		if (source.getEntity() instanceof PlayerEntity) {
			PlayerEntity player1 = (PlayerEntity) source.getEntity();
			setPlayer(player1);
		} else {
			setPlayer(null);
		}
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (ServerPlayerEntity player : players) {
				if (index == (players.size() - 1))
					value += player.getDisplayName().getString();
				else
					value += player.getDisplayName().getString() + ", ";
				ConfigHandler.removeAllowedPlayers(player.getDisplayName().getString());
				index++;
			}
			sendMessage(source.getEntity(), TextFormatting.AQUA + "Added " + TextFormatting.GOLD + value + TextFormatting.AQUA + " to Allowed Players");
			sendMessage(source.getEntity(), TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
		return 1;
	}

	private boolean isAllowed() {
		if (getPlayer() == null)
			return true;
		for (String s : ConfigHandler.getAllowedConfigPlayers()) {
			if (s.equalsIgnoreCase(getPlayer().getDisplayName().getString())) {
				return true;
			}
		}
		sendMessage(getPlayer(), TextFormatting.RED + "You Do not Have Permissions to use this Command\nContact Server Admin or consult the mods config to edit.");
		return false;
	}

	private int addConfigEditor(CommandSource source, Collection<ServerPlayerEntity> players) {
		if (source.getEntity() instanceof PlayerEntity) {
			PlayerEntity player1 = (PlayerEntity) source.getEntity();
			setPlayer(player1);
		} else {
			setPlayer(null);
		}
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (ServerPlayerEntity player : players) {
				if (index == (players.size() - 1))
					value += player.getDisplayName().getString();
				else
					value += player.getDisplayName().getString() + ", ";
				ConfigHandler.addAllowedConfigPlayers(player.getDisplayName().getString());
				index++;
			}
			sendMessage(source.getEntity(), TextFormatting.AQUA + "Added " + TextFormatting.GOLD + value + TextFormatting.AQUA + " to Allowed Config Editors Players");
			sendMessage(source.getEntity(), TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
		return 1;
	}

	private int addAllowedPublicPlayers(CommandSource source, Collection<ServerPlayerEntity> players) {
		if (source.getEntity() instanceof PlayerEntity) {
			PlayerEntity player1 = (PlayerEntity) source.getEntity();
			setPlayer(player1);
		} else {
			setPlayer(null);
		}
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (ServerPlayerEntity player : players) {
				if (index == (players.size() - 1))
					value += player.getDisplayName().getString();
				else
					value += player.getDisplayName().getString() + ", ";
				ConfigHandler.addAllowedPublicPlayer(player.getDisplayName().getString());
				index++;
			}
			sendMessage(source.getEntity(), TextFormatting.AQUA + "Added " + TextFormatting.GOLD + value + TextFormatting.AQUA + " to Allowed Public Players");
			sendMessage(source.getEntity(), TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
		return 1;
	}

	private int addAllowedPlayers(CommandSource source, Collection<ServerPlayerEntity> players) {
		if (source.getEntity() instanceof PlayerEntity) {
			PlayerEntity player1 = (PlayerEntity) source.getEntity();
			setPlayer(player1);
		} else {
			setPlayer(null);
		}
		if (isAllowed()) {
			String value = "";
			int index = 0;
			for (ServerPlayerEntity player : players) {
				if (index == (players.size() - 1))
					value += player.getDisplayName().getString();
				else
					value += player.getDisplayName().getString() + ", ";
				ConfigHandler.addAllowedPlayers(player.getDisplayName().getString());
				index++;
			}
			sendMessage(source.getEntity(), TextFormatting.AQUA + "Added " + TextFormatting.GOLD + value + TextFormatting.AQUA + " to Allowed Players");
			sendMessage(source.getEntity(), TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.writeConfig();
		}
		return 1;
	}

	private int setPublicAllowed(CommandSource source, boolean bool) {
		if (source.getEntity() instanceof PlayerEntity) {
			PlayerEntity player1 = (PlayerEntity) source.getEntity();
			setPlayer(player1);
		} else {
			setPlayer(null);
		}
		if (isAllowed()) {
			sendMessage(player, TextFormatting.AQUA + "Set Public Warps Allowed to " + TextFormatting.GOLD + bool);
			sendMessage(player, TextFormatting.GREEN + "Config Updated!");
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
	private int setDebug(CommandSource source, boolean bool) {
		PlayerEntity player1;
		if (source.getEntity() instanceof PlayerEntity) {
			player1 = (PlayerEntity) source.getEntity();
			setPlayer(player1);
		} else {
			player1 = null;
			setPlayer(null);
		}
		if (isAllowed()) {
			sendMessage(player, TextFormatting.AQUA + "Set Debug Mode to " + TextFormatting.GOLD + bool);
			sendMessage(player, TextFormatting.GREEN + "Config Updated!");
			ConfigHandler.setDebugMode(bool);
			ConfigHandler.writeConfig();
		}

		return 1;
	}

	private void setPlayer(PlayerEntity player) {
		ConfigHandler.readConfig();
		this.player = player;
	}

	private PlayerEntity getPlayer() {
		return this.player;
	}

	/**
	 * Sends message to current player
	 * 
	 * @param player
	 * @param message
	 */
	private void sendMessage(Entity player, Object message) {
		WarpMod.sendMessage(player, message);
	}
}
