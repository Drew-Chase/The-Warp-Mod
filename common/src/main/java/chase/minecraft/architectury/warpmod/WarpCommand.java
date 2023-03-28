package chase.minecraft.architectury.warpmod;

import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.Warps;
import chase.minecraft.architectury.warpmod.data.enums.WarpCreationResponseType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.commands.SpreadPlayersCommand;
import net.minecraft.server.level.ServerPlayer;

import java.util.Random;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
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
     * "This function teleports the player to a random location within 100 blocks of their current location."
     * <p>
     * The first line of the function is the function's signature. It tells us the function's name, the type of data it
     * returns, and the type of data it takes as input
     *
     * @param context The command context.
     * @return A boolean value.
     */
    private static boolean teleportToRandom(CommandContext<CommandSourceStack> context) {
        return teleportToRandom(context, 100);
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
            int dist = new Random().nextInt(minDistance, maxDistance);
            player.randomTeleport(dist, player.getLevel().getLogicalHeight(), dist, false);
        }
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
            Warp warp = Warp.create(name, player.getBlockX() + .5f, player.getBlockY(), player.getBlockZ() + .5f, player.getYRot(), player.getXRot(), player);
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
        MutableComponent component = Component.literal(String.format("%sList:%s\n", ChatFormatting.GOLD, ChatFormatting.WHITE));
        for (Warp warp : warps.GetWarps()) {
            component.append(String.format("%s%s:%s [x: %d, y: %d, z: %d, pitch: %d, yaw: %d]%s - %sDIM: %s,\n", ChatFormatting.AQUA, warp.getName(), ChatFormatting.LIGHT_PURPLE, (int) warp.getX(), (int) warp.getY(), (int) warp.getZ(), (int) warp.getPitch(), (int) warp.getYaw(), ChatFormatting.WHITE, ChatFormatting.GREEN, warp.getLevelResourceLocation().getPath()));
        }
        context.getSource().sendSuccess(component, false);
        return true;
    }
}
