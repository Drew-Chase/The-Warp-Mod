package chase.minecraft.architectury.warpmod;

import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.Warps;
import chase.minecraft.architectury.warpmod.data.enums.WarpCreationResponseType;
import chase.minecraft.architectury.warpmod.server.TimedServerTask;
import chase.minecraft.architectury.warpmod.server.TimedServerTasks;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;

import java.util.Objects;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.DimensionArgument.dimension;
import static net.minecraft.commands.arguments.EntityArgument.player;
import static net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos;
import static net.minecraft.commands.arguments.coordinates.RotationArgument.rotation;
@SuppressWarnings("all")
public class WarpCommand
{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(
				literal("warp")
						.then(argument("name", string())
								.suggests((ctx, s) -> Warps.fromPlayer(ctx.getSource().getPlayer()).GetSuggestions(s))
								.executes(ctx ->
								{
									String name = StringArgumentType.getString(ctx, "name");
									if (ctx.getSource().isPlayer())
									{
										for (ServerPlayer player : ctx.getSource().getPlayer().getServer().getPlayerList().getPlayers())
										{
											if (player.getDisplayName().getString().equals(name))
											{
												return teleportTo(ctx, player) ? 1 : 0;
											}
										}
									}
									return teleportTo(ctx, name) ? 1 : 0;
								})
						)
						.then(argument("player", player())
								.executes(ctx -> teleportTo(ctx, EntityArgument.getPlayer(ctx, "player")) ? 1 : 0)
						)
						.then(literal("set")
								.then(argument("name", string())
										.suggests((ctx, s) -> Warps.fromPlayer(ctx.getSource().getPlayer()).GetSuggestions(s))
										.then(argument("location", blockPos())
												.then(argument("rotation", rotation())
														.then(argument("dimension", dimension())
																.executes(ctx ->
																{
																	ServerLevel level = DimensionArgument.getDimension(ctx, "dimension");
																	BlockPos pos = BlockPosArgument.getBlockPos(ctx, "location");
																	Vec2 rot = RotationArgument.getRotation(ctx, "rotation").getRotation(ctx.getSource());
																	return createWarp(ctx, StringArgumentType.getString(ctx, "name"), pos.getX(), pos.getY(), pos.getZ(), rot.y, rot.x, level) ? 1 : 0;
																})
														)
														.executes(ctx ->
														{
															BlockPos pos = BlockPosArgument.getBlockPos(ctx, "location");
															Vec2 rot = RotationArgument.getRotation(ctx, "rotation").getRotation(ctx.getSource());
															return createWarp(ctx, StringArgumentType.getString(ctx, "name"), pos.getX(), pos.getY(), pos.getZ(), rot.y, rot.x) ? 1 : 0;
														})
												)
												.executes(ctx ->
												{
													BlockPos pos = BlockPosArgument.getBlockPos(ctx, "location");
													return createWarp(ctx, StringArgumentType.getString(ctx, "name"), pos.getX(), pos.getY(), pos.getZ()) ? 1 : 0;
												})
										)
										.executes(ctx -> createWarp(ctx, StringArgumentType.getString(ctx, "name")) ? 1 : 0)
								)
						).then(literal("list")
								.then(argument("player", player())
										.executes(ctx -> listWarps(ctx, EntityArgument.getPlayer(ctx, "player")) ? 1 : 0))
								.executes(ctx -> listWarps(ctx) ? 1 : 0)
						).then(literal("remove")
								.then(argument("name", string())
										.suggests((ctx, s) -> Warps.fromPlayer(ctx.getSource().getPlayer()).GetSuggestions(s))
										.executes(ctx -> removeWarp(ctx, StringArgumentType.getString(ctx, "name")) ? 1 : 0)
								)
						).then(literal("random")
								.then(argument("max", integer())
										.then(argument("min", integer())
												.executes(ctx -> teleportToRandom(ctx, IntegerArgumentType.getInteger(ctx, "max"), IntegerArgumentType.getInteger(ctx, "min")) ? 1 : 0)
										)
										.executes(ctx -> teleportToRandom(ctx, IntegerArgumentType.getInteger(ctx, "max")) ? 1 : 0)
								)
								.executes(ctx -> teleportToRandom(ctx) ? 1 : 0)
						).then(literal("rename")
								.then(argument("old", string())
										.then(argument("new", string())
												.executes(ctx -> renameWarp(ctx, StringArgumentType.getString(ctx, "old"), StringArgumentType.getString(ctx, "new"), false) ? 1 : 0)
										).then(argument("overwrite", bool())
												.executes(ctx -> renameWarp(ctx, StringArgumentType.getString(ctx, "old"), StringArgumentType.getString(ctx, "new"), BoolArgumentType.getBool(ctx, "overwrite")) ? 1 : 0)
										)
								)
						).then(literal("spawn")
								.executes(ctx -> teleportToSpawn(ctx) ? 1 : 0)
						).then(literal("invite")
								.then(argument("player", player())
										.then(argument("warp", string())
												.suggests((ctx, builder) -> Warps.fromPlayer(ctx.getSource().getPlayer()).GetSuggestions(builder))
												.executes(ctx -> invite(ctx, StringArgumentType.getString(ctx, "warp"), EntityArgument.getPlayer(ctx, "player")) ? 1 : 0)
										)
								)
						).then(literal("accept")
								.then(argument("player", player())
										.then(argument("code", string())
												.executes(ctx -> acceptWarp(ctx, StringArgumentType.getString(ctx, "code"), EntityArgument.getPlayer(ctx, "player")) ? 1 : 0)
										)
								)
						)
		);
		
	}
	
	
	/**
	 * If the command is run by a player, and the warp exists, teleport the player to the warp
	 *
	 * @param context The command context, which contains the command source, arguments, and other information.
	 * @param name    The name of the warp to teleport to
	 * @return A boolean value.
	 */
	private static boolean teleportTo(CommandContext<CommandSourceStack> context, String name)
	{
		if (context.getSource().isPlayer())
		{
			Warps warps = Warps.fromPlayer(context.getSource().getPlayer());
			if (!warps.Exists(name))
			{
				context.getSource().sendFailure(Component.literal(String.format("Warp does NOT exist: %s", name)));
				return false;
			}
			warps.Get(name).teleportTo();
			context.getSource().sendSuccess(Component.literal(String.format("%sWarped to: %s%s", ChatFormatting.GREEN, ChatFormatting.GOLD, name)), false);
			return true;
		}
		context.getSource().sendFailure(Component.literal("Command needs to be run as player"));
		return false;
	}
	
	/**
	 * "If the command source is a player, send a warp invite to the target player, and return true. Otherwise, return false."
	 *
	 * The first thing we do is check if the command source is a player. If it is, we get the player object from the command source. If the player object is null, we send a failure message to the command source and return false
	 *
	 * @param context The command context.
	 * @param toPlayer The player to teleport to.
	 * @return A boolean value.
	 */
	private static boolean teleportTo(CommandContext<CommandSourceStack> context, ServerPlayer toPlayer)
	{
		if (context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Unable to find player!"));
				return false;
			}
			
			String inviteCode = System.currentTimeMillis() + "";
			ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp accept %s %s".formatted(player.getDisplayName().getString(), inviteCode));
			HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("%sWarp %s%s%s to you.".formatted(ChatFormatting.GREEN, ChatFormatting.GOLD, player.getDisplayName().getString(), ChatFormatting.GREEN)));
			Component clickText = Component.literal("[ACCEPT]").withStyle(style -> style.withColor(ChatFormatting.GOLD).withClickEvent(click).withHoverEvent(hover));
			toPlayer.sendSystemMessage(Component.literal("%s%s%s wants to warp to you! ".formatted(ChatFormatting.GOLD, player.getDisplayName().getString(), ChatFormatting.GREEN)).append(clickText));
			
			TimedServerTasks.Instance.create(inviteCode, 20*1000, () ->
			{
				Component canceledText = Component.literal("Warp invite has expired!").withStyle(ChatFormatting.RED);
				toPlayer.sendSystemMessage(canceledText);
				player.sendSystemMessage(canceledText);
			});
			
			return true;
			
		}
		return false;
	}
	
	/**
	 * If the command is run by a player, teleport them to the spawn point
	 *
	 * @param context The context of the command. This is used to get the source of the command, the arguments, and the
	 *                command itself.
	 * @return A boolean
	 */
	private static boolean teleportToSpawn(CommandContext<CommandSourceStack> context)
	{
		if (context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Player was not found!"));
				return false;
			}
			Warp.createBack(player);
			
			BlockPos pos = player.getLevel().getSharedSpawnPos();
			player.teleportTo(Objects.requireNonNull(player.getServer()).overworld(), pos.getX() + .5, pos.getY(), pos.getZ() + .5, player.getXRot(), player.getYRot());
			context.getSource().sendSuccess(Component.literal(String.format("%sWarped to: %sSpawn", ChatFormatting.GREEN, ChatFormatting.GOLD)), false);
			return true;
		}
		context.getSource().sendFailure(Component.literal("Command needs to be run as player"));
		return false;
	}
	
	/**
	 * "This function teleports the player to a random location within 100 blocks of their current location."
	 * <p>
	 * The first line of the function is the function's signature. It tells us the function's name, the type of data it
	 * returns, and the type of data it takes as input
	 *
	 * @param context The command context.
	 * @return A boolean value.
	 */
	private static boolean teleportToRandom(CommandContext<CommandSourceStack> context)
	{
		return teleportToRandom(context, 500);
	}
	
	/**
	 * If the max distance is less than 25, set the min distance to max distance - 1, otherwise set the min distance to 25.
	 * If the max distance is 0, send a failure message and return false, otherwise return the result of calling
	 * teleportToRandom with the max and min distances.
	 *
	 * @param context     The command context.
	 * @param maxDistance The maximum distance the player can be teleported.
	 * @return A boolean value
	 */
	private static boolean teleportToRandom(CommandContext<CommandSourceStack> context, int maxDistance)
	{
		int min = 25;
		if (maxDistance <= min && maxDistance > 1)
		{
			min = maxDistance - 1;
		}
		if (maxDistance == 0)
		{
			context.getSource().sendFailure(Component.literal("Max distance cannot be ZERO"));
			return false;
		}
		return teleportToRandom(context, maxDistance, min);
	}
	
	/**
	 * "If the command source is a player, teleport the player to a random location within the specified distance."
	 * <p>
	 * The first thing we do is check if the command source is a player. If it is, we get the player object from the
	 * command source. Then we generate a random number between the minimum and maximum distance. Finally, we teleport the
	 * player to a random location within the specified distance
	 *
	 * @param context     The command context.
	 * @param maxDistance The maximum distance the player can be teleported.
	 * @param minDistance The minimum distance the player will be teleported.
	 * @return A boolean value.
	 */
	private static boolean teleportToRandom(CommandContext<CommandSourceStack> context, int maxDistance, int minDistance)
	{
		if (context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Player was not found!"));
				return false;
			}
			int dist = Warp.teleportRandom(player, maxDistance, minDistance);
			boolean success = dist != 0;
			if (!success)
			{
				context.getSource().sendFailure(Component.literal("Failed to find a safe place to land."));
			} else
			{
				context.getSource().sendSuccess(Component.literal(String.format("%sTeleported %s%d%s blocks away", ChatFormatting.GREEN, ChatFormatting.GOLD, dist, ChatFormatting.GREEN)), false);
			}
			return success;
		}
		context.getSource().sendFailure(Component.literal("Command must be run as a player."));
		return false;
	}
	
	
	/**
	 * If the command source is a player, create a warp with the given name at the player's location, and add it to the
	 * player's warp list
	 *
	 * @param context The context of the command. This is used to get the source of the command, which is the player who
	 *                sent the command.
	 * @param name    The name of the warp
	 * @return A boolean
	 */
	private static boolean createWarp(CommandContext<CommandSourceStack> context, String name)
	{
		if (context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Player was not found!"));
				return false;
			}
			return createWarp(context, name, player.getBlockX(), player.getBlockY(), player.getBlockZ(), player.getYRot(), player.getXRot());
		} else
		{
			context.getSource().sendFailure(Component.literal("Command has to be run as a Player!"));
		}
		return false;
	}
	
	/**
	 * If the command was run by a player, get the player's rotation and call the other createWarp function
	 *
	 * @param context The command context, which contains the command source, arguments, and other information.
	 * @param name The name of the warp
	 * @param x The x coordinate of the warp
	 * @param y The yaw of the player.
	 * @param z The z coordinate of the warp
	 * @return A boolean value.
	 */
	private static boolean createWarp(CommandContext<CommandSourceStack> context, String name, int x, int y, int z)
	{
		if (context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Player was not found!"));
				return false;
			}
			return createWarp(context, name, x, y, z, player.getYRot(), player.getXRot());
		} else
		{
			context.getSource().sendFailure(Component.literal("Command has to be run as a Player!"));
		}
		return false;
	}
	
	/**
	 * If the command is run by a player, get the player, if the player is not null, create a warp with the player's level
	 *
	 * @param context The command context, which contains the command source, arguments, and other information.
	 * @param name The name of the warp
	 * @param x The x coordinate of the warp
	 * @param y The yaw of the warp.
	 * @param z The z coordinate of the warp
	 * @param yaw The yaw of the player.
	 * @param pitch The angle of the player's view up and down.
	 * @return A boolean value.
	 */
	private static boolean createWarp(CommandContext<CommandSourceStack> context, String name, int x, int y, int z, float yaw, float pitch)
	{
		
		if (context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Player was not found!"));
				return false;
			}
			return createWarp(context, name, x, y, z, yaw, pitch, player.getLevel());
		} else
		{
			context.getSource().sendFailure(Component.literal("Command has to be run as a Player!"));
		}
		return false;
	}
	
	/**
	 * It creates a warp, and if it fails, it sends a message to the player
	 *
	 * @param context The context of the command. This is used to get the source of the command, which is the player who ran the command.
	 * @param name The name of the warp
	 * @param x The x coordinate of the warp
	 * @param y The y coordinate of the warp
	 * @param z The z coordinate of the warp
	 * @param yaw The yaw of the player when they warp to this location.
	 * @param pitch The pitch of the player when they warp to the location
	 * @param dimension The dimension the warp is in.
	 * @return A boolean value.
	 */
	private static boolean createWarp(CommandContext<CommandSourceStack> context, String name, int x, int y, int z, float yaw, float pitch, ServerLevel dimension)
	{
		if (context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Player was not found!"));
				return false;
			}
			Warp warp = Warp.create(name, x, y, z, yaw, pitch, player, dimension, false);
			WarpCreationResponseType response = Warps.fromPlayer(player).createAddOrUpdate(warp);
			if (response == WarpCreationResponseType.FailureDueToDuplicate)
			{
				context.getSource().sendFailure(Component.literal(String.format("Warp already exists with name: %s", name)));
				return false;
			} else if (response == WarpCreationResponseType.FailureDueToInvalidPermissions)
			{
				context.getSource().sendFailure(Component.literal("You do not have the proper permission to create a warp"));
				return false;
			} else if (response == WarpCreationResponseType.Success)
			{
				context.getSource().sendSuccess(Component.literal(String.format("%sCreated Warp: %s%s", ChatFormatting.GREEN, ChatFormatting.GOLD, name)), false);
				return true;
			} else if (response == WarpCreationResponseType.Overwritten)
			{
				context.getSource().sendSuccess(Component.literal(String.format("%sOverwrote Warp: %s%s", ChatFormatting.GREEN, ChatFormatting.GOLD, name)), false);
				return true;
			}
		} else
		{
			context.getSource().sendFailure(Component.literal("Command has to be run as a Player!"));
		}
		return false;
	}
	
	/**
	 * If the command is being run by a player, and the warp exists, and the new name doesn't exist, or the new name exists,
	 * and we're overwriting, then rename the warp
	 *
	 * @param context   The context of the command.
	 * @param name      The name of the warp to rename
	 * @param new_name  The new name of the warp
	 * @param overwrite If the new name already exists, should it be overwritten?
	 * @return A boolean value.
	 */
	private static boolean renameWarp(CommandContext<CommandSourceStack> context, String name, String new_name,
	                                  boolean overwrite)
	{
		if (context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Player was not found!"));
				return false;
			}
			Warps warps = Warps.fromPlayer(player);
			if (!warps.Exists(name))
			{
				context.getSource().sendFailure(Component.literal("Warp does NOT exist!"));
				return false;
			}
			if (warps.Exists(new_name))
			{
				if (!overwrite)
				{
					context.getSource().sendFailure(Component.literal(String.format("Warp already exists: %s%s", ChatFormatting.GOLD, new_name)));
					return false;
				}
			}
			Warp old = warps.Get(name);
			Warp.create(new_name, old.getX(), old.getY(), old.getZ(), old.getYaw(), old.getPitch(), player, true);
			context.getSource().sendSuccess(Component.literal(String.format("%sWarp renamed %s%s %s-> %s%s", ChatFormatting.GREEN, ChatFormatting.GOLD, name, ChatFormatting.GOLD, ChatFormatting.GREEN, new_name)), false);
			return true;
		}
		
		context.getSource().sendFailure(Component.literal("Command needs to be run as a Player!"));
		return false;
	}
	
	/**
	 * If the command is run by a player, remove the warp with the given name, and send a message to the player
	 *
	 * @param context The context of the command.
	 * @param name    The name of the warp to remove
	 * @return A boolean value.
	 */
	private static boolean removeWarp(CommandContext<CommandSourceStack> context, String name)
	{
		if (context.getSource().isPlayer())
		{
			if (Warps.fromPlayer(context.getSource().getPlayer()).Remove(name))
			{
				context.getSource().sendSuccess(Component.literal(String.format("%sWarp removed: %s%s", ChatFormatting.GREEN, ChatFormatting.GOLD, name)), false);
			} else
			{
				context.getSource().sendFailure(Component.literal(String.format("Warp does NOT exist: %s%s", ChatFormatting.GOLD, name)));
			}
			return true;
		}
		
		context.getSource().sendFailure(Component.literal("Command has to be run as a Player!"));
		return false;
	}
	
	/**
	 * If the command source is a player, list the warps for that player
	 *
	 * @param context The context of the command. This is used to get the source of the command, the arguments, and the
	 *                command itself.
	 * @return A boolean value.
	 */
	private static boolean listWarps(CommandContext<CommandSourceStack> context)
	{
		if (context.getSource().isPlayer())
		{
			return listWarps(context, context.getSource().getPlayer());
		}
		context.getSource().sendFailure(Component.literal("Command requires player name."));
		return false;
	}
	
	/**
	 * It gets the warps from the player, creates a component, loops through the warps, and appends the warp information to
	 * the component. Then it sends the component to the player
	 *
	 * @param context The command context.
	 * @param player  The player who executed the command.
	 * @return A boolean value.
	 */
	private static boolean listWarps(CommandContext<CommandSourceStack> context, ServerPlayer player)
	{
		if (context.getSource().isPlayer())
		{
			ServerPlayer requestingPlayer = context.getSource().getPlayer();
			if (requestingPlayer != null)
			{
				if (!requestingPlayer.getDisplayName().getString().equals(player.getDisplayName().getString()))
				{
					if (!requestingPlayer.hasPermissions(4))
					{
						context.getSource().sendFailure(Component.literal("You do NOT have permissions to view other players warps."));
						return false;
					}
				}
			}
		}
		Warps warps = Warps.fromPlayer(player);
		MutableComponent component = Component.literal("");
		ChatFormatting primary = ChatFormatting.GOLD;
		ChatFormatting secondary = ChatFormatting.GREEN;
		for (Warp warp : warps.GetWarps())
		{
			ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/warp %s", warp.getName()));
			HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Warps you to %s".formatted(warp.getName())));
			component.append(Component.literal("%s: ".formatted(warp.getName())).withStyle(style -> style.applyFormat(primary).withHoverEvent(hover).withClickEvent(click)));
			component.append(Component.literal("%s[X: %s%d%s, Y: %s%d%s, Z: %s%d%s]".formatted(primary, secondary, warp.getX(), primary, secondary, warp.getY(), primary, secondary, warp.getZ(), primary)));
			component.append(Component.literal(" %sDIM: %s\n".formatted(primary, warp.getLevelResourceLocation().getPath())));
		}
		context.getSource().sendSuccess(component, false);
		return true;
	}
	
	/**
	 * "If the command source is a player, and the player has a warp with the given name, then send a clickable message to the given player inviting them to the warp."
	 *
	 * The first thing we do is check if the command source is a player. If it isn't, we send a failure message to the command source and return false
	 *
	 * @param context The command context.
	 * @param warpName The name of the warp to invite the player to.
	 * @param toPlayer The player to send the invite to.
	 * @return A boolean value.
	 */
	private static boolean invite(CommandContext<CommandSourceStack> context, String warpName, ServerPlayer toPlayer)
	{
		if (context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Unable to find player!"));
				return false;
			}
			Warps playerWarp = Warps.fromPlayer(player);
			if (playerWarp.Exists(warpName))
			{
				Warp warp = playerWarp.Get(warpName);
				ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp set %s %d %d %d %f %f %s".formatted(warp.getName(), warp.getX(), warp.getY(), warp.getZ(), warp.getYaw(), warp.getPitch(), warp.getLevelResourceLocation().toString()));
				HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("%sAdd warp %s%s%s to your list.".formatted(ChatFormatting.GREEN, ChatFormatting.GOLD, warpName, ChatFormatting.GREEN)));
				Component clickText = Component.literal("[ACCEPT]").withStyle(style -> style.withColor(ChatFormatting.GOLD).withClickEvent(click).withHoverEvent(hover));
				toPlayer.sendSystemMessage(Component.literal("You have been invited to warp ").withStyle(ChatFormatting.GREEN).append(clickText));
				context.getSource().sendSuccess(Component.literal("%s%s%s has been invited to %s%s".formatted(ChatFormatting.GOLD, toPlayer.getDisplayName().getString(), ChatFormatting.GREEN, ChatFormatting.GREEN, warpName)), false);
				return true;
			} else
			{
				context.getSource().sendFailure(Component.literal("Warp doesn't exist: %s".formatted(warpName)));
			}
		} else
		{
			context.getSource().sendFailure(Component.literal("You can only run this command as a player!"));
		}
		return false;
	}
	
	/**
	 * If the command source is a player, and the invite code exists, and the invite code hasn't expired, then teleport the player to the player who sent the invite
	 *
	 * @param context The command context, this is used to get the command source, and send messages to the player.
	 * @param inviteCode The invite code that was generated when the player sent the invite.
	 * @param toPlayer The player that is being warped to.
	 * @return A boolean value.
	 */
	private static boolean acceptWarp(CommandContext<CommandSourceStack> context, String inviteCode, ServerPlayer toPlayer)
	{
		if (context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Unable to find player!"));
			} else
			{
				if (TimedServerTasks.Instance.exists(inviteCode))
				{
					TimedServerTask task = Objects.requireNonNull(TimedServerTasks.Instance.get(inviteCode));
					if (!task.isCanceled())
					{
						Warp.createBack(toPlayer);
						toPlayer.teleportTo(player.getLevel(), player.getBlockX(), player.getBlockY(), player.getBlockZ(), player.getYRot(), player.getXRot());
						task.cancel();
						toPlayer.sendSystemMessage(Component.literal("%sWarped to %s%s".formatted(ChatFormatting.GREEN, ChatFormatting.GOLD, player.getDisplayName().getString())));
						return true;
					} else
					{
						context.getSource().sendFailure(Component.literal("Invitation has Expired!"));
					}
				} else
				{
					context.getSource().sendFailure(Component.literal("Invitation has Expired!"));
				}
			}
		} else
		{
			context.getSource().sendFailure(Component.literal("This needs to be run as a player!"));
		}
		
		return false;
	}
}
