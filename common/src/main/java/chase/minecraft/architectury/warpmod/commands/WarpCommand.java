package chase.minecraft.architectury.warpmod.commands;

import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.WarpManager;
import chase.minecraft.architectury.warpmod.enums.WarpCreationResponseType;
import chase.minecraft.architectury.warpmod.server.RepeatingServerTasks;
import chase.minecraft.architectury.warpmod.server.TimedServerTask;
import chase.minecraft.architectury.warpmod.server.TimedServerTasks;
import chase.minecraft.architectury.warpmod.utils.WorldUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.DimensionArgument.dimension;
import static net.minecraft.commands.arguments.EntityArgument.player;
import static net.minecraft.commands.arguments.coordinates.RotationArgument.rotation;

/**
 * Contains all commands for warps
 */
@SuppressWarnings("all")
public class WarpCommand
{
	private static List<String> INVALID_NAMES = new ArrayList<>();
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		RequiredArgumentBuilder<CommandSourceStack, String> nameArg = argument("name", greedyString());
		LiteralArgumentBuilder<CommandSourceStack> node = literal("warp").then(nameArg.requires(ctx -> ctx.hasPermission(4)).suggests((ctx, s) -> WarpManager.fromPlayer(ctx.getSource().getPlayer()).suggestions(s)).executes(ctx ->
		{
			String name = getString(ctx, "name");
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
		})).then(argument("player", player()).executes(ctx -> teleportTo(ctx, EntityArgument.getPlayer(ctx, "player")) ? 1 : 0)).then(literal("set").then(nameArg.suggests((ctx, s) -> WarpManager.fromPlayer(ctx.getSource().getPlayer()).suggestions(s)).then(argument("location", Vec3Argument.vec3()).then(argument("rotation", rotation()).then(argument("dimension", dimension()).executes(ctx ->
		{
			ServerLevel level = DimensionArgument.getDimension(ctx, "dimension");
			Vec3 pos = Vec3Argument.getVec3(ctx, "location");
			Vec2 rot = RotationArgument.getRotation(ctx, "rotation").getRotation(ctx.getSource());
			return createWarp(ctx, getString(ctx, "name"), pos.x, pos.y, pos.z, rot.y, rot.x, level) ? 1 : 0;
		})).executes(ctx ->
		{
			Vec3 pos = Vec3Argument.getVec3(ctx, "location");
			Vec2 rot = RotationArgument.getRotation(ctx, "rotation").getRotation(ctx.getSource());
			return createWarp(ctx, getString(ctx, "name"), pos.x, pos.y, pos.z, rot.y, rot.x) ? 1 : 0;
		})).executes(ctx ->
		{
			Vec3 pos = Vec3Argument.getVec3(ctx, "location");
			return createWarp(ctx, getString(ctx, "name"), pos.x, pos.y, pos.z) ? 1 : 0;
		})).executes(ctx -> createWarp(ctx, getString(ctx, "name")) ? 1 : 0))).then(literal("list").then(argument("player", player()).executes(ctx -> listWarps(ctx, EntityArgument.getPlayer(ctx, "player")) ? 1 : 0)).executes(ctx -> listWarps(ctx) ? 1 : 0)).then(literal("remove").then(nameArg.suggests((ctx, s) -> WarpManager.fromPlayer(ctx.getSource().getPlayer()).suggestions(s)).executes(ctx -> removeWarp(ctx, getString(ctx, "name")) ? 1 : 0))).then(literal("random").then(argument("max", integer(50, 50000)).then(argument("min", integer(25, 50000)).executes(ctx -> teleportToRandom(ctx, getInteger(ctx, "max"), getInteger(ctx, "min")) ? 1 : 0)).executes(ctx -> teleportToRandom(ctx, getInteger(ctx, "max")) ? 1 : 0)).executes(ctx -> teleportToRandom(ctx) ? 1 : 0)).then(literal("rename").then(argument("old", greedyString()).suggests((ctx, s) -> WarpManager.fromPlayer(ctx.getSource().getPlayer()).suggestions(s)).then(argument("new", greedyString()).then(argument("overwrite", bool()).executes(ctx -> renameWarp(ctx, getString(ctx, "old"), getString(ctx, "new"), getBool(ctx, "overwrite")) ? 1 : 0)).executes(ctx -> renameWarp(ctx, getString(ctx, "old"), getString(ctx, "new"), false) ? 1 : 0)))).then(literal("spawn").executes(ctx -> teleportToSpawn(ctx) ? 1 : 0)).then(literal("invite").then(argument("player", player()).then(argument("warp", greedyString()).suggests((ctx, builder) -> WarpManager.fromPlayer(ctx.getSource().getPlayer()).suggestions(builder)).executes(ctx -> invite(ctx, getString(ctx, "warp"), EntityArgument.getPlayer(ctx, "player")) ? 1 : 0)))).then(literal("accept").then(argument("player", player()).then(argument("code", string()).executes(ctx -> acceptWarp(ctx, getString(ctx, "code"), EntityArgument.getPlayer(ctx, "player")) ? 1 : 0)))).then(literal("travel").then(nameArg.suggests((ctx, builder) ->
		{
			List<String> sug = new ArrayList<>();
			sug.addAll(ctx.getSource().getOnlinePlayerNames());
			sug.addAll(List.of(WarpManager.fromPlayer(ctx.getSource().getPlayer()).getWarpNames()));
			return SharedSuggestionProvider.suggest(sug, builder);
		}).then(argument("useActionBar", bool()).then(argument("rate", integer(10, 500)).executes(ctx -> travel(ctx, getString(ctx, "name"), getBool(ctx, "useActionBar"), getInteger(ctx, "rate")) ? 1 : 0)).executes(ctx -> travel(ctx, getString(ctx, "name"), getBool(ctx, "useActionBar")) ? 1 : 0)).executes(ctx -> travel(ctx, getString(ctx, "name")) ? 1 : 0)).executes(ctx -> travel(ctx, "") ? 1 : 0));
		for (CommandNode<CommandSourceStack> i : node.getArguments())
		{
			INVALID_NAMES.add(i.getName());
		}
		dispatcher.register(node);
		
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
			WarpManager warpManager = WarpManager.fromPlayer(context.getSource().getPlayer());
			if (!warpManager.exists(name))
			{
				context.getSource().sendFailure(Component.literal(String.format("Warp does NOT exist: %s", name)));
				return false;
			}
			warpManager.get(name).teleport(context.getSource().getPlayer());
			context.getSource().sendSystemMessage(Component.literal(String.format("%sWarped to: %s%s", ChatFormatting.GREEN, ChatFormatting.GOLD, name)));
			return true;
		}
		context.getSource().sendFailure(Component.literal("Command needs to be run as player"));
		return false;
	}
	
	/**
	 * "If the command source is a player, send a warp invite to the target player, and return true. Otherwise, return false."
	 * <p>
	 * The first thing we do is check if the command source is a player. If it is, we get the player object from the command source. If the player object is null, we send a failure message to the command source and return false
	 *
	 * @param context  The command context.
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
			player.sendSystemMessage(Component.literal("%s%s%s has recieved your warp request".formatted(ChatFormatting.GOLD, toPlayer.getDisplayName().getString(), ChatFormatting.GREEN)));
			TimedServerTasks.Instance.create(inviteCode, 20 * 1000, () ->
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
			WarpManager.fromPlayer(player).createBack();
			
			BlockPos pos = player.level().getSharedSpawnPos();
			player.teleportTo(Objects.requireNonNull(player.getServer()).overworld(), pos.getX() + .5, pos.getY(), pos.getZ() + .5, player.getYRot(), player.getXRot());
			context.getSource().sendSystemMessage(Component.literal(String.format("%sWarped to: %sSpawn", ChatFormatting.GREEN, ChatFormatting.GOLD)));
			
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
			if (minDistance > maxDistance)
			{
				context.getSource().sendFailure(Component.literal("Minimum can NOT be greater than the maximum!"));
				return false;
			}
			
			return WorldUtils.teleportRandom(player, minDistance, maxDistance);
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
			return createWarp(context, name, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
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
	 * @param name    The name of the warp
	 * @param x       The x coordinate of the warp
	 * @param y       The yaw of the player.
	 * @param z       The z coordinate of the warp
	 * @return A boolean value.
	 */
	private static boolean createWarp(CommandContext<CommandSourceStack> context, String name, double x, double y, double z)
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
	 * @param name    The name of the warp
	 * @param x       The x coordinate of the warp
	 * @param y       The yaw of the warp.
	 * @param z       The z coordinate of the warp
	 * @param yaw     The yaw of the player.
	 * @param pitch   The angle of the player's view up and down.
	 * @return A boolean value.
	 */
	private static boolean createWarp(CommandContext<CommandSourceStack> context, String name, double x, double y, double z, float yaw, float pitch)
	{
		
		if (context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Player was not found!"));
				return false;
			}
			return createWarp(context, name, x, y, z, yaw, pitch, player.serverLevel());
		} else
		{
			context.getSource().sendFailure(Component.literal("Command has to be run as a Player!"));
		}
		return false;
	}
	
	/**
	 * It creates a warp, and if it fails, it sends a message to the player
	 *
	 * @param context   The context of the command. This is used to get the source of the command, which is the player who ran the command.
	 * @param name      The name of the warp
	 * @param x         The x coordinate of the warp
	 * @param y         The y coordinate of the warp
	 * @param z         The z coordinate of the warp
	 * @param yaw       The yaw of the player when they warp to this location.
	 * @param pitch     The pitch of the player when they warp to the location
	 * @param dimension The dimension the warp is in.
	 * @return A boolean value.
	 */
	private static boolean createWarp(CommandContext<CommandSourceStack> context, String name, double x, double y, double z, float yaw, float pitch, ServerLevel dimension)
	{
		if (INVALID_NAMES.contains(name))
		{
			context.getSource().sendFailure(Component.literal("Invalid warp name: %s%s".formatted(ChatFormatting.GOLD, name)));
			return false;
		}
		if (context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Player was not found!"));
				return false;
			}
			Warp warp = new Warp(name, x, y, z, yaw, pitch, dimension.dimension().location(), player);
			WarpCreationResponseType response = WarpManager.fromPlayer(player).createOrUpdate(warp);
			
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
				context.getSource().sendSystemMessage(Component.literal(String.format("%sCreated Warp: %s%s", ChatFormatting.GREEN, ChatFormatting.GOLD, name)));
				return true;
			} else if (response == WarpCreationResponseType.Overwritten)
			{
				context.getSource().sendSystemMessage(Component.literal(String.format("%sOverwrote Warp: %s%s", ChatFormatting.GREEN, ChatFormatting.GOLD, name)));
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
	private static boolean renameWarp(CommandContext<CommandSourceStack> context, String name, String new_name, boolean overwrite)
	{
		if (context.getSource().isPlayer())
		{
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Player was not found!"));
				return false;
			}
			WarpManager warpManager = WarpManager.fromPlayer(player);
			if (!warpManager.exists(name))
			{
				context.getSource().sendFailure(Component.literal("Warp does NOT exist!"));
				return false;
			}
			if (warpManager.exists(new_name))
			{
				if (!overwrite)
				{
					context.getSource().sendFailure(Component.literal(String.format("Warp already exists: %s%s", ChatFormatting.GOLD, new_name)));
					return false;
				}
			}
			warpManager.rename(name, new_name);
			
			context.getSource().sendSystemMessage(Component.literal(String.format("%sWarp renamed %s%s %s-> %s%s", ChatFormatting.GREEN, ChatFormatting.GOLD, name, ChatFormatting.GOLD, ChatFormatting.GREEN, new_name)));
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
			if (WarpManager.fromPlayer(context.getSource().getPlayer()).remove(name))
			{
				context.getSource().sendSystemMessage(Component.literal(String.format("%sWarp removed: %s%s", ChatFormatting.GREEN, ChatFormatting.GOLD, name)));
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
						context.getSource().sendFailure(Component.literal("You do NOT have permissions to view other players warpManager."));
						return false;
					}
				}
			}
		}
		WarpManager warpManager = WarpManager.fromPlayer(player);
		MutableComponent component = Component.literal("");
		ChatFormatting primary = ChatFormatting.GREEN;
		ChatFormatting secondary = ChatFormatting.GOLD;
		for (Warp warp : warpManager.getWarps())
		{
			ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/warp %s", warp.getName()));
			HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("WarpManager you to %s".formatted(warp.getName())));
			component.append(Component.literal("%s: ".formatted(warp.getName())).withStyle(style -> style.applyFormat(primary).withHoverEvent(hover).withClickEvent(click)));
			component.append(Component.literal("%s[X: %s%d%s, Y: %s%d%s, Z: %s%d%s]".formatted(primary, secondary, (int) warp.getX(), primary, secondary, (int) warp.getY(), primary, secondary, (int) warp.getZ(), primary)));
			component.append(Component.literal(" %sDIM: %s%s\n".formatted(primary, secondary, warp.getDimension().getPath())));
		}
		context.getSource().sendSystemMessage(component);
		
		return true;
	}
	
	/**
	 * "If the command source is a player, and the player has a warp with the given name, then send a clickable message to the given player inviting them to the warp."
	 * <p>
	 * The first thing we do is check if the command source is a player. If it isn't, we send a failure message to the command source and return false
	 *
	 * @param context  The command context.
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
			WarpManager playerWarp = WarpManager.fromPlayer(player);
			if (playerWarp.exists(warpName))
			{
				Warp warp = playerWarp.get(warpName);
				ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp set %s %f %f %f %f %f %s".formatted(warp.getName(), warp.getX(), warp.getY(), warp.getZ(), warp.getYaw(), warp.getPitch(), warp.getDimension().toString()));
				HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("%sAdd warp %s%s%s to your list.".formatted(ChatFormatting.GREEN, ChatFormatting.GOLD, warpName, ChatFormatting.GREEN)));
				Component clickText = Component.literal("[ACCEPT]").withStyle(style -> style.withColor(ChatFormatting.GOLD).withClickEvent(click).withHoverEvent(hover));
				toPlayer.sendSystemMessage(Component.literal("You have been invited to warp ").withStyle(ChatFormatting.GREEN).append(clickText));
				context.getSource().sendSystemMessage(Component.literal("%s%s%s has been invited to %s%s".formatted(ChatFormatting.GOLD, toPlayer.getDisplayName().getString(), ChatFormatting.GREEN, ChatFormatting.GREEN, warpName)));
				
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
	 * @param context    The command context, this is used to get the command source, and send messages to the player.
	 * @param inviteCode The invite code that was generated when the player sent the invite.
	 * @param toPlayer   The player that is being warped to.
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
						WarpManager.fromPlayer(toPlayer).createBack();
						toPlayer.teleportTo(player.serverLevel(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
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
	
	/**
	 * The function "travel" takes a CommandContext and a String as input and returns a boolean value, with a default value of true.
	 *
	 * @param context The context parameter is of type CommandContext<CommandSourceStack>, which is an object that contains information about the current command execution context, such as the sender of the command, the arguments passed to the command, and the server on which the command is being executed.
	 * @param name    The name parameter is a String that represents the name of a location to travel to.
	 * @return The method `travel` is being called with three arguments: `context`, `name`, and `true`. The method returns a boolean value. In this case, the returned value is the result of calling the overloaded `travel` method with the same `context` and `name` arguments, and `true` as the third argument.
	 */
	private static boolean travel(CommandContext<CommandSourceStack> context, String name)
	{
		return travel(context, name, true);
	}
	
	/**
	 * The function "travel" takes in a command context, a name, a boolean value for using the action bar, and returns a boolean value indicating whether the travel was successful or not, with a default value of 100.
	 *
	 * @param context      The context parameter is of type CommandContext<CommandSourceStack> and represents the context in which the command is being executed. It contains information such as the sender of the command, the arguments passed to the command, and the server on which the command is being executed.
	 * @param name         The name of the travel destination.
	 * @param useActionBar The useActionBar parameter is a boolean value that determines whether or not to display travel progress in the player's action bar. If set to true, the progress will be displayed in the action bar, otherwise it will not be displayed.
	 * @return The method `travel` is being returned with the parameters `context`, `name`, `useActionBar`, and the integer value `100`. The return type of the method is a boolean value.
	 */
	private static boolean travel(CommandContext<CommandSourceStack> context, String name, boolean useActionBar)
	{
		return travel(context, name, useActionBar, 100);
	}
	
	/**
	 * This function allows a player to travel to a specified warp location or player, displaying a progress bar and distance in the action bar.
	 *
	 * @param context      The context of the command, which includes the source of the command (in this case, a player) and any arguments passed to the command.
	 * @param name         The name of the warp or player to travel to.
	 * @param useActionBar A boolean value indicating whether to display the travel progress in the player's action bar or not.
	 * @param rate         The rate parameter is the time interval (in milliseconds) at which the player's progress towards the warp destination is updated.
	 * @return The method is returning a boolean value.
	 */
	private static boolean travel(CommandContext<CommandSourceStack> context, String name, boolean useActionBar, long rate)
	{
		if (context.getSource().isPlayer())
		{
			// The above code is checking if the command sender is a player. If the sender is not a player, it sends a failure message to the sender saying "Player could not be found!" and returns false.
			ServerPlayer player = context.getSource().getPlayer();
			if (player == null)
			{
				context.getSource().sendFailure(Component.literal("Player could not be found!"));
				return false;
			}
			
			WorldUtils.removeTravelBar(player);
			
			CustomBossEvents bossEvents = context.getSource().getServer().getCustomBossEvents();
			ResourceLocation compassBar = new ResourceLocation("warpmod", player.getDisplayName().getString().toLowerCase().replace(" ", "_"));
			CustomBossEvent event = bossEvents.get(compassBar) == null ? bossEvents.create(compassBar, Component.empty()) : bossEvents.get(compassBar);
			
			if (name == "")
			{
				RepeatingServerTasks.Instance.get(player.getDisplayName().getString()).cancel();
				if (useActionBar) player.displayClientMessage(Component.literal(""), true);
				return true;
			}
			WarpManager warpManager = WarpManager.fromPlayer(player);
			boolean isPlayer = false;
			ServerPlayer otherPlayer = null;
			for (ServerPlayer p : player.getServer().getPlayerList().getPlayers())
			{
				if (p.getDisplayName().getString().equals(name))
				{
					isPlayer = true;
					otherPlayer = p;
					break;
				}
			}
			if (warpManager.exists(name) || isPlayer)
			{
				if (RepeatingServerTasks.Instance.exists(player.getDisplayName().getString()))
				{
					RepeatingServerTasks.Instance.get(player.getDisplayName().getString()).cancel();
				}
				boolean diffDim = false;
				Warp warp;
				if (!isPlayer)
				{
					warp = warpManager.get(name);
					diffDim = !warp.getDimension().equals(player.level().dimension().location());
				} else
				{
					warp = null;
					diffDim = !otherPlayer.level().dimension().location().equals(player.level().dimension().location());
				}
				
				if (diffDim)
				{
					Component c = Component.literal("Warp is not in the same dimension!").withStyle(ChatFormatting.RED);
					player.sendSystemMessage(c, true);
					if (useActionBar) player.displayClientMessage(c, true);
					return false;
				}
				event.setMax(0);
				event.setValue(0);
				event.setProgress(0);
				event.addPlayer(player);
				boolean finalIsPlayer = isPlayer;
				ServerPlayer finalOtherPlayer = otherPlayer;
				RepeatingServerTasks.Instance.create(player.getDisplayName().getString(), rate, () ->
				{
					Component compass = Component.empty();
					if (finalIsPlayer) compass = WorldUtils.calculateTravel(player, finalOtherPlayer);
					else compass = WorldUtils.calculateTravel(player, warp.getX(), warp.getY(), warp.getZ());
					if (warp.distance() > event.getMax())
					{
						event.setMax((int) warp.distance());
					}
					int newDist = (int) (event.getMax() - warp.distance());
					event.setValue(newDist < 0 ? 0 : newDist);
					event.setName(compass);
					if (useActionBar) player.displayClientMessage(Component.literal("%sTraveling to %s - %s%dM".formatted(ChatFormatting.GREEN, name, ChatFormatting.GOLD, (int) warp.distance())), true);
				});
			} else
			{
				context.getSource().sendFailure(Component.literal("No warp found: %s%s".formatted(ChatFormatting.GOLD, name)));
			}
		} else
		{
			context.getSource().sendFailure(Component.literal("This needs to be run as a player!"));
		}
		
		return false;
	}
}
