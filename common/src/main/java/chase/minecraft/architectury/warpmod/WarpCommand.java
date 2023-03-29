package chase.minecraft.architectury.warpmod;

import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.Warps;
import chase.minecraft.architectury.warpmod.data.enums.WarpCreationResponseType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.commands.SpreadPlayersCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WarpCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("warp")
                        .then(argument("name", string())
                                .suggests((ctx, s) -> Warps.fromPlayer(ctx.getSource().getPlayer()).GetSuggestions(s))
                                .executes(ctx -> teleportTo(ctx, StringArgumentType.getString(ctx, "name")) ? 1 : 0)
                        )
                        .then(literal("set")
                                .then(argument("name", string())
                                        .suggests((ctx, s) -> Warps.fromPlayer(ctx.getSource().getPlayer()).GetSuggestions(s))
                                        .executes(ctx -> createWarp(ctx, StringArgumentType.getString(ctx, "name")) ? 1 : 0)
                                )
                        ).then(literal("list")
                                .then(argument("player", EntityArgument.player())
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
    private static boolean teleportTo(CommandContext<CommandSourceStack> context, String name) {
        if (context.getSource().isPlayer()) {
            Warps warps = Warps.fromPlayer(context.getSource().getPlayer());
            if (!warps.Exists(name)) {
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
     * If the command is run by a player, teleport them to the spawn point
     *
     * @param context The context of the command. This is used to get the source of the command, the arguments, and the
     * command itself.
     * @return A boolean
     */
    private static boolean teleportToSpawn(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("Player was not found!"));
                return false;
            }
            Warp.createBack(player);

            BlockPos pos = player.getLevel().getSharedSpawnPos();
            player.teleportTo(player.getServer().overworld(), pos.getX() + .5, pos.getY(), pos.getZ() + .5, player.getXRot(), player.getYRot());
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
    private static boolean teleportToRandom(CommandContext<CommandSourceStack> context) {
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
    private static boolean teleportToRandom(CommandContext<CommandSourceStack> context, int maxDistance) {
        int min = 25;
        if (maxDistance <= min && maxDistance > 1) {
            min = maxDistance - 1;
        }
        if (maxDistance == 0) {
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
    private static boolean teleportToRandom(CommandContext<CommandSourceStack> context, int maxDistance, int minDistance) {
        if (context.getSource().isPlayer()) {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("Player was not found!"));
                return false;
            }
            int dist = Warp.teleportRandom(player, maxDistance, minDistance);
            boolean success = dist != 0;
            if (!success) {
                context.getSource().sendFailure(Component.literal("Failed to find a safe place to land."));
            } else {
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
    private static boolean createWarp(CommandContext<CommandSourceStack> context, String name) {
        if (context.getSource().isPlayer()) {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("Player was not found!"));
                return false;
            }
            Warp warp = Warp.create(name, player.getBlockX(), player.getBlockY(), player.getBlockZ(), player.getYRot(), player.getXRot(), player, false);
            WarpCreationResponseType response = Warps.fromPlayer(player).createAddOrUpdate(warp);
            if (response == WarpCreationResponseType.FailureDueToDuplicate) {
                context.getSource().sendFailure(Component.literal(String.format("Warp already exists with name: %s", name)));
                return false;
            } else if (response == WarpCreationResponseType.FailureDueToInvalidPermissions) {
                context.getSource().sendFailure(Component.literal("You do not have the proper permission to create a warp"));
                return false;
            } else if (response == WarpCreationResponseType.Success) {
                context.getSource().sendSuccess(Component.literal(String.format("%sCreated Warp: %s%s", ChatFormatting.GREEN, ChatFormatting.GOLD, name)), false);
                return true;
            } else if (response == WarpCreationResponseType.Overwritten) {
                context.getSource().sendSuccess(Component.literal(String.format("%sOverwrote Warp: %s%s", ChatFormatting.GREEN, ChatFormatting.GOLD, name)), false);
                return true;
            }
        } else {
            context.getSource().sendFailure(Component.literal("Command has to be run as a Player!"));
        }
        return false;
    }

    /**
     * If the command is being run by a player, and the warp exists, and the new name doesn't exist, or the new name exists
     * and we're overwriting, then rename the warp
     *
     * @param context The context of the command.
     * @param name The name of the warp to rename
     * @param new_name The new name of the warp
     * @param overwrite If the new name already exists, should it be overwritten?
     * @return A boolean value.
     */
    private static boolean renameWarp(CommandContext<CommandSourceStack> context, String name, String new_name, boolean overwrite) {
        if (context.getSource().isPlayer()) {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("Player was not found!"));
                return false;
            }
            Warps warps = Warps.fromPlayer(player);
            if (!warps.Exists(name)) {
                context.getSource().sendFailure(Component.literal("Warp does NOT exist!"));
                return false;
            }
            if (warps.Exists(new_name)) {
                if (!overwrite) {
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
    private static boolean removeWarp(CommandContext<CommandSourceStack> context, String name) {
        if (context.getSource().isPlayer()) {
            if (Warps.fromPlayer(context.getSource().getPlayer()).Remove(name)) {
                context.getSource().sendSuccess(Component.literal(String.format("%sWarp removed: %s%s", ChatFormatting.GREEN, ChatFormatting.GOLD, name)), false);
            } else {
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
    private static boolean listWarps(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
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
    private static boolean listWarps(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        Warps warps = Warps.fromPlayer(player);
        MutableComponent component = Component.literal("");
        for (Warp warp : warps.GetWarps()) {
            component.append(Component.literal(String.format("%s%s: ", ChatFormatting.GOLD, warp.getName())));
            component.append(ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", warp.getX(), warp.getY(), warp.getZ())).withStyle((style) -> style.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/warp %s", warp.getName()))).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(String.format("Click to Warp to %s", warp.getName()))))));
            component.append(Component.literal(String.format(" %sDIM: %s\n", ChatFormatting.GOLD, warp.getLevelResourceLocation().getPath())));
        }
        context.getSource().sendSuccess(component, false);
        return true;
    }
}
