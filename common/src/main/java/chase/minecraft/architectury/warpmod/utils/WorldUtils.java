package chase.minecraft.architectury.warpmod.utils;

import chase.minecraft.architectury.warpmod.enums.SaftyCheckResponse;
import chase.minecraft.architectury.warpmod.server.RepeatingServerTasks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.Objects;

/**
 * The WorldUtils class contains various static methods for calculating Minecraft's World information.
 */
public class WorldUtils
{
	/**
	 * This function calculates a random location within a certain range around a given center point in a Minecraft server level, ensuring that the location is safe to spawn in.
	 *
	 * @param level  A ServerLevel object representing the game world in which the random position will be calculated.
	 * @param center A Vec3 representing the center point around which the random position will be generated.
	 * @param min    The minimum distance from the center point that the random position can be generated at.
	 * @param max    The maximum distance from the center point that the random position can be generated at.
	 * @return A Vector4f object is being returned, which contains the randomly generated location (as a Vector3f) and the distance from the center point.
	 */
	public static Vector4f calculateRandom(ServerLevel level, Vec3 center, int min, int max)
	{
		final int maxTries = 10000;
		
		for (int i = 0; i < maxTries; i++)
		{
			Vec2 posXY = MathUtils.getRandom2DPosition(center, min, max);
			int x = (int) posXY.x;
			int z = (int) posXY.y;
			int y = level.getLogicalHeight() - 4;
			BlockPos pos = BlockPos.containing(center);
			SaftyCheckResponse safe = SaftyCheckResponse.AIR;
			while (safe == SaftyCheckResponse.AIR && pos.getY() > level.getMinBuildHeight())
			{
				safe = isSafe(level, pos);
				if (safe != SaftyCheckResponse.SAFE)
				{
					pos = pos.below();
					y--;
				} else
				{
					Vec3 loc = new Vec3(x, y, z);
					float distance = (float) center.distanceTo(loc);
					return new Vector4f(loc.toVector3f(), distance);
				}
			}
		}
		return new Vector4f(center.toVector3f(), 0f);
	}
	
	/**
	 * The function checks if a block position is safe to stand on in a Minecraft server level.
	 *
	 * @param level    The Minecraft server level in which the block is located.
	 * @param blockPos The position of the block being checked for safety.
	 * @return The method is returning a SaftyCheckResponse, which is an enum that can have one of three values: SAFE, UNSAFE, or AIR.
	 */
	public static SaftyCheckResponse isSafe(ServerLevel level, BlockPos blockPos)
	{
		BlockPos belowPos = blockPos.below();
		BlockState blockState = level.getBlockState(belowPos);
		BlockState headBlockState = level.getBlockState(blockPos.above());
		BlockState feetBlockState = level.getBlockState(blockPos);
		boolean foundLiquid = feetBlockState.getMaterial().isLiquid() || feetBlockState.getMaterial() == Material.FIRE || headBlockState.getMaterial().isLiquid() || headBlockState.getMaterial() == Material.FIRE || blockState.getMaterial().isLiquid() || blockState.getMaterial() == Material.FIRE;
		boolean legSafe = !feetBlockState.getMaterial().blocksMotion() && !feetBlockState.getMaterial().isLiquid() && feetBlockState.getMaterial() != Material.FIRE;
		boolean headSafe = !headBlockState.getMaterial().blocksMotion() && !headBlockState.getMaterial().isLiquid() && headBlockState.getMaterial() != Material.FIRE;
		if (foundLiquid)
		{
			return SaftyCheckResponse.UNSAFE;
		}
		if (blockState.getMaterial().blocksMotion() && headSafe && legSafe && level.isInWorldBounds(blockPos))
		{
			return SaftyCheckResponse.SAFE;
		}
		return SaftyCheckResponse.AIR;
	}
	
	/**
	 * Gets the level from the resource location
	 *
	 * @param server - the minecraft server
	 * @param id     - the resource location of the dimension
	 * @return the dimension
	 */
	@Nullable
	public static ServerLevel getLevelFromID(@Nullable MinecraftServer server, ResourceLocation id)
	{
		if (server != null)
		{
			for (ServerLevel level : server.getAllLevels())
			{
				if (level.dimension().location().equals(id))
				{
					return level;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * This Java function teleports a server player to a random location within a specified range.
	 *
	 * @param player The player that will be teleported randomly within a certain range of coordinates.
	 * @param min    The minimum distance (in blocks) that the player can be teleported away from their current location.
	 * @param max    The maximum distance (in blocks) that the player can be teleported randomly from their current location.
	 * @return The method `teleportRandom` is returning a boolean value. The value being returned is the result of calling the method `teleportRandom` with the parameters `player`, `min`, `max`, and `true`.
	 */
	public static boolean teleportRandom(ServerPlayer player, int min, int max)
	{
		return teleportRandom(player, min, max, true);
	}
	
	/**
	 * The function teleports a player to a random location within a specified range and returns true if successful.
	 *
	 * @param player      The player that is being teleported.
	 * @param min         The minimum distance (in blocks) that the player can be teleported away from their current position.
	 * @param max         The maximum distance (in blocks) that the player can be teleported away from their current position.
	 * @param sendMessage A boolean value that determines whether or not to send a message to the player about the teleportation. If set to true, a message will be sent. If set to false, no message will be sent.
	 * @return The method returns a boolean value, either true or false.
	 */
	public static boolean teleportRandom(ServerPlayer player, int min, int max, boolean sendMessage)
	{
		Vector4f cords = calculateRandom(player.getLevel(), player.getEyePosition(), min, max);
		int x = (int) cords.x;
		int y = (int) cords.y;
		int z = (int) cords.z;
		int distance = (int) cords.w;
		if (distance != 0)
		{
			if (sendMessage)
				player.sendSystemMessage(Component.literal(String.format("%sTeleported %s%d%s blocks away", ChatFormatting.GREEN, ChatFormatting.GOLD, distance, ChatFormatting.GREEN)));
			player.teleportTo(x, y, z);
			return true;
		} else
		{
			if (sendMessage)
				player.sendSystemMessage(Component.literal("Failed to find a safe place to land."));
			return false;
		}
	}
	
	/**
	 * The function calculates the travel distance between two players in a game.
	 *
	 * @param fromPlayer The player who is starting the travel.
	 * @param toPlayer   The "toPlayer" parameter is an instance of the Player class, which represents a player in the game. It contains information about the player's position, such as their X, Y, and Z coordinates. The method "calculateTravel" takes this parameter as input and uses the player's position to
	 * @return The method `calculateTravel` is returning a `Component` object.
	 */
	public static Component calculateTravel(Player fromPlayer, Player toPlayer)
	{
		return calculateTravel(fromPlayer, toPlayer.getX(), toPlayer.getY(), toPlayer.getZ());
	}
	
	/**
	 * The function calculates the relative rotation angle between a player and a target location and creates a progress bar using Minecraft's chat component system based on the percentage of how much an object is offscreen to the left or right.
	 *
	 * @param player A Player object representing the player for whom the progress bar is being calculated.
	 * @param x      The x-coordinate of the target location that the player is traveling towards.
	 * @param y      The parameter "y" is not used in the given code and therefore has no significance in this context.
	 * @param z      The z-coordinate of the target location that the player is traveling towards.
	 * @return The method is returning a progress bar as a Minecraft chat component.
	 */
	public static Component calculateTravel(Player player, double x, double y, double z)
	{
		
		// This is calculating the relative rotation angle between a player and a target location (specified by x and z coordinates). It first calculates the angle between the target location and the player's position using the Math.atan2() method. It then converts this angle from radians to degrees and ensures that it is within the range of 0 to 360 degrees.
		double dx = x - player.getX();
		double dz = z - player.getZ();
		
		float angle = (float) Math.toDegrees(Math.atan2(dz, dx));
		if (angle < 0)
			angle += 360;
		
		float relativeRotation = angle - player.getYRot();
		if (relativeRotation > 180)
			relativeRotation -= 360;
		else if (relativeRotation < -180)
			relativeRotation += 360;
		relativeRotation -= 90;
		if (relativeRotation < 0)
			relativeRotation += 360;
		
		// This is calculating the percentage of how much an object is offscreen to the left or right based on its relative rotation angle. It first defines the leftmost and rightmost angles that are considered offscreen. Then it checks if the object is offscreen to the left or right based on its relative rotation angle. Finally, it calculates the percentage of how much the object is offscreen by adding the rightmost angle to the relative rotation angle and dividing it by 180.
		int leftMostAngle = 270;
		int rightMostAngle = 90;
		boolean offscreenLeft = relativeRotation < leftMostAngle && relativeRotation > 180;
		boolean offscreenRight = relativeRotation <= 180 && relativeRotation >= rightMostAngle;
		boolean offscreen = offscreenLeft || offscreenRight;
		
		double percentage = 0;
		double tmp = relativeRotation;
		if (relativeRotation > rightMostAngle)
			tmp -= 360;
		
		tmp += rightMostAngle;
		percentage = tmp / 180;
		
		// This is creating a progress bar using Minecraft's chat component system. The progress bar consists of a series of dashes ("-") with a marker (a green asterisk) indicating the progress. The progress is determined by a percentage value, and the number of dashes in the progress bar is determined by the "segments" variable. If the progress is offscreen (i.e. less than 0 or greater than 1), the progress bar is not displayed. The resulting progress bar is stored in the "comp" variable.
		MutableComponent comp = Component.empty();
		comp.append(ChatFormatting.GOLD + "[");
		Component marker = Component.literal(ChatFormatting.GREEN + "*" + ChatFormatting.WHITE);
		if (offscreenLeft)
			comp.append(marker);
		int segments = 29;
		boolean markerEverAdded = false;
		for (int i = 0; i <= segments; i++)
		{
			boolean markerAdded = false;
			if (!offscreen && percentage > 0)
			{
				if (Math.round(percentage * segments) == i)
				{
					comp.append(marker);
					markerAdded = true;
					markerEverAdded = true;
				}
			}
			if (!markerAdded)
			{
				if (offscreen)
				{
					if (i != segments)
					{
						comp.append(ChatFormatting.WHITE + "-");
					}
				} else
				{
					comp.append(ChatFormatting.WHITE + "-");
				}
			}
		}
		if (offscreenRight)
			comp.append(marker);
		if (!offscreen && !markerEverAdded)
			comp.append(marker);
		comp.append(ChatFormatting.GOLD + "]");
		
		return comp;
	}
	
	/**
	 * This function removes a custom boss event and cancels a repeating server task for a specific player.
	 *
	 * @param player The player for whom the travel bar needs to be removed.
	 */
	public static void removeTravelBar(Player player)
	{
		try
		{
			CustomBossEvents bossEvents = Objects.requireNonNull(player.getServer()).getCustomBossEvents();
			ResourceLocation compassBar = new ResourceLocation("warpmod", player.getDisplayName().getString().toLowerCase().replace(" ", "_"));
			CustomBossEvent event = bossEvents.get(compassBar) == null ? bossEvents.create(compassBar, Component.empty()) : bossEvents.get(compassBar);
			assert event != null;
			event.removeAllPlayers();
			RepeatingServerTasks.Instance.get(player.getDisplayName().getString()).cancel();
		} catch (Exception e)
		{
		}
	}
	
	public static String getLevelName(ResourceLocation level)
	{
		return level.getPath().replaceAll("_", " ").toUpperCase();
	}
	
}
