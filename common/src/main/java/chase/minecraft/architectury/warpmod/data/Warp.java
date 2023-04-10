package chase.minecraft.architectury.warpmod.data;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.server.RepeatingServerTasks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Random;

/**
 * Warp object holds name, position, and player, and add it to the player's warps.
 */
@SuppressWarnings("unused")
public class Warp
{
	
	private final ServerPlayer _player;
	private String _name, _displayName;
	private double _x, _z, _y;
	private float _yaw, _pitch;
	private ServerLevel _dimension;
	
	private Warp(String name, double x, double y, double z, float yaw, float pitch, ServerPlayer player)
	{
		_x = x;
		_y = y;
		_z = z;
		_yaw = yaw;
		_pitch = pitch;
		_displayName = _name = name;
		_player = player;
		_dimension = player.getLevel();
	}
	
	private Warp(String name, double x, double y, double z, float yaw, float pitch, ServerPlayer player, ServerLevel level)
	{
		this(name, x, y, z, yaw, pitch, player);
		_dimension = level;
	}
	
	private Warp(String name, double x, double y, double z, float yaw, float pitch, ServerPlayer player, ResourceLocation level)
	{
		this(name, x, y, z, yaw, pitch, player);
		for (ServerLevel l : Objects.requireNonNull(player.getServer()).getAllLevels())
		{
			if (l.dimension().location().equals(level))
			{
				_dimension = l;
			}
		}
	}
	
	/**
	 * It teleports the player to a random location within the specified distance
	 *
	 * @param player      The player to teleport
	 * @param maxDistance The maximum distance the player can be teleported.
	 * @param minDistance The minimum distance from the player to teleport to.
	 * @return A Vec2 object
	 */
	public static int teleportRandom(ServerPlayer player, int maxDistance, int minDistance)
	{
		Warp.createBack(player);
		int currentX = player.getBlockX();
		int currentZ = player.getBlockZ();
		
		Vec2 vec = GetRandomCords(player, maxDistance, minDistance);
		int randX = (int) vec.x;
		int randZ = (int) vec.y;
		int y = player.getLevel().getLogicalHeight() - 4;
		int dist = Math.abs((currentX - randX) + (currentZ - randZ));
		
		boolean isSafe = false;
		int maxTries = 10000;
		// Finding a random location within a certain distance of the player.
		for (int i = 0; i < maxTries; i++)
		{
			WarpMod.log.info("Warping random...");
			
			Level level = player.getLevel();
			
			BlockPos blockPos = BlockPos.containing(randX, y, randZ);
			// Checking if the block is safe to spawn on.
			while (!isSafe && blockPos.getY() > level.getMinBuildHeight())
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
					isSafe = false;
					break;
				}
				if (blockState.getMaterial().blocksMotion() && headSafe && legSafe && level.isInWorldBounds(blockPos))
				{
					isSafe = true;
				} else
				{
					--y;
					blockPos = belowPos;
				}
			}
			
			if (!isSafe)
			{
				// Trying to find a random location within a certain distance of the player.
				vec = GetRandomCords(player, maxDistance, minDistance);
				randX = (int) vec.x;
				randZ = (int) vec.y;
				dist = Math.abs((currentX - randX) + (currentZ - randZ));
				
				y = player.getLevel().getLogicalHeight();
				continue;
			}
			break;
		}
		Vec3 playerPos = new Vec3(player.getBlockX(), player.getBlockY(), player.getBlockZ());
		if (isSafe) player.teleportTo(randX + .5, y, randZ + .5);
		
		return isSafe ? (int) playerPos.distanceTo(new Vec3(player.getBlockX(), player.getBlockY(), player.getBlockZ())) + 1 : 0;
	}
	
	/**
	 * It returns a random coordinate within a specified range of the player
	 *
	 * @param player      The player to teleport
	 * @param maxDistance The maximum distance from the player that the coordinates can be.
	 * @param minDistance The minimum distance from the player that the coordinates can be.
	 * @return A Vec2 object with the x and z coordinates of a random location within the specified range.
	 */
	private static Vec2 GetRandomCords(Player player, int maxDistance, int minDistance)
	{
		Random rand = new Random();
		int randX, randZ;
		boolean negX, negZ;
		negX = rand.nextBoolean();
		negZ = rand.nextBoolean();
		if (maxDistance == minDistance)
		{
			randX = randZ = maxDistance;
		} else
		{
			randX = rand.nextInt(minDistance, maxDistance);
			randZ = rand.nextInt(minDistance, maxDistance);
		}
		if (negX) randX *= -1;
		if (negZ) randZ *= -1;
		
		return new Vec2(player.getBlockX() + randX, player.getBlockZ() + randZ);
	}
	
	/**
	 * The function calculates the travel parameters for a player to travel from one player's location to another player's location.
	 *
	 * @param fromPlayer A Player object representing the player who is initiating the warp travel.
	 * @param toPlayer   The "toPlayer" parameter is an instance of the Player class representing the player to whom the travel is being calculated. It contains information about the player's current location, including their X, Y, and Z coordinates.
	 * @return The method `calculateTravel` is returning an object of type `WarpTravelParameters`.
	 */
	public static WarpTravelParemeters calculateTravel(Player fromPlayer, Player toPlayer)
	{
		return calculateTravel(fromPlayer, toPlayer.getX(), toPlayer.getY(), toPlayer.getZ());
	}
	
	/**
	 * The function calculates the travel parameters for a player to warp to a specific location, including distance and a visual representation of the direction.
	 *
	 * @param player A Player object representing the player who is traveling.
	 * @param x      The x-coordinate of the destination location the player wants to travel to.
	 * @param y      The "y" parameter represents the y-coordinate of the destination location for a warp travel calculation.
	 * @param z      The z-coordinate of the destination location for the warp travel.
	 * @return The method is returning an instance of the WarpTravelParemeters class, which contains the distance between the player and the specified coordinates, as well as a mutable component that represents a visual indicator of the direction the player needs to travel in order to reach the coordinates.
	 */
	public static WarpTravelParemeters calculateTravel(Player player, double x, double y, double z)
	{
		int distance = (int) new Vec3(player.getX(), player.getY(), player.getZ()).distanceTo(new Vec3(x, y, z));
		
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
		
		return new WarpTravelParemeters(distance, comp);
	}
	
	/**
	 * Create a warp named 'back' at the player's current location, and set it to be a temporary warp.
	 *
	 * @param player The player who is creating the warp.
	 * @return A Warp object.
	 */
	public static Warp createBack(ServerPlayer player)
	{
		ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp back");
		HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to warp back"));
		Component backComp = Component.literal("BACK").withStyle(style ->
				style
						.withColor(ChatFormatting.GOLD)
						.withClickEvent(click)
						.withHoverEvent(hover)
		);
		Component msg = Component.literal("Created ").withStyle(ChatFormatting.GREEN).append(backComp).append(" warp").withStyle(ChatFormatting.GREEN);
		player.sendSystemMessage(msg);
		return Warp.create("back", player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player, true);
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
	
	/**
	 * It takes a CompoundTag, and a Player, and returns a Warp
	 *
	 * @param tag    The tag that was sent from the client.
	 * @param player The player who is warping.
	 * @return A Warp object
	 */
	public static Warp fromTag(CompoundTag tag, ServerPlayer player)
	{
		String name = tag.getString("name");
		double x = tag.getDouble("x");
		double y = tag.getDouble("y");
		double z = tag.getDouble("z");
		float yaw = tag.getFloat("yaw");
		float pitch = tag.getFloat("pitch");
		ResourceLocation dim = new ResourceLocation(tag.getString("dim")); // minecraft:the_nether
		return new Warp(name, x, y, z, yaw, pitch, player, dim);
	}
	
	/**
	 * Create a new warp with the given name, position, and player, and add it to the player's warps.
	 *
	 * @param name   The name of the warp.
	 * @param x      The x coordinate of the warp
	 * @param y      The y coordinate of the warp.
	 * @param z      The z coordinate of the warp.
	 * @param player The player who created the warp
	 * @return A new warp object
	 */
	public static Warp create(String name, double x, double y, double z, float yaw, float pitch, ServerPlayer player, boolean add)
	{
		Warp warp = new Warp(name, x, y, z, yaw, pitch, player);
		if (add)
		{
			Warps playersWarps = Warps.fromPlayer(player);
			playersWarps.createAddOrUpdate(warp);
		}
		return warp;
	}
	
	/**
	 * Create a new warp with the given name, position, and player, and add it to the player's warps
	 *
	 * @param name   The name of the warp.
	 * @param x      The x coordinate of the warp
	 * @param y      The y coordinate of the warp.
	 * @param z      The z coordinate of the warp
	 * @param player The player who created the warp
	 * @param level  The level the warp is in.
	 * @return A new Warp object
	 */
	public static Warp create(String name, double x, double y, double z, float yaw, float pitch, ServerPlayer player, ResourceLocation level, boolean add)
	{
		Warp warp = new Warp(name, x, y, z, yaw, pitch, player, level);
		if (add)
		{
			Warps playersWarps = Warps.fromPlayer(player);
			playersWarps.createAddOrUpdate(warp);
		}
		return warp;
	}
	
	/**
	 * "Create a new warp with the given name, position, and player, and add it to the player's warps."
	 * <p>
	 * The first thing we do is create a new Warp object with the given name, position, and player
	 *
	 * @param name   The name of the warp.
	 * @param x      The x coordinate of the warp
	 * @param y      The y coordinate of the warp
	 * @param z      The z coordinate of the warp.
	 * @param player The player who created the warp
	 * @param level  The level the warp is in
	 * @return A new Warp object.
	 */
	public static Warp create(String name, double x, double y, double z, float yaw, float pitch, ServerPlayer player, ServerLevel level, boolean add)
	{
		Warp warp = new Warp(name, x, y, z, yaw, pitch, player, level);
		if (add)
		{
			Warps playersWarps = Warps.fromPlayer(player);
			playersWarps.createAddOrUpdate(warp);
		}
		return warp;
	}
	
	public void update(double x, double y, double z, float pitch, float yaw, ResourceLocation dimension)
	{
		this._x = x;
		this._y = y;
		this._z = z;
		this._pitch = pitch;
		this._yaw = yaw;
		for (ServerLevel l : Objects.requireNonNull(_player.getServer()).getAllLevels())
		{
			if (l.dimension().location().equals(dimension))
			{
				this._dimension = l;
			}
		}
	}
	
	/**
	 * Returns the dimension that the warp resides in.
	 *
	 * @return The dimension
	 */
	public ServerLevel getLevel()
	{
		return _dimension;
	}
	
	/**
	 * Returns the resource location of the dimension
	 *
	 * @return The location of the dimension.
	 */
	public ResourceLocation getLevelResourceLocation()
	{
		return _dimension.dimension().location();
	}
	
	/**
	 * Gets the name of the warp
	 *
	 * @return the warp name
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Gets the X coordinate of the warp
	 *
	 * @return the x coordinate
	 */
	public double getX()
	{
		return _x;
	}
	
	/**
	 * Gets the Y coordinate of the warp.
	 *
	 * @return the y coordinate
	 */
	public double getY()
	{
		return _y;
	}
	
	/**
	 * Gets the z coordinate of the warp
	 *
	 * @return the z coordinate
	 */
	public double getZ()
	{
		return _z;
	}
	
	/**
	 * Gets the players head yaw.
	 *
	 * @return yaw
	 */
	public float getYaw()
	{
		return _yaw;
	}
	
	/**
	 * Gets the players head pitch
	 *
	 * @return pitch
	 */
	public float getPitch()
	{
		return _pitch;
	}
	
	/**
	 * Gets the <b>Player</b> that owns the warp.
	 *
	 * @return the <b>Player</b>
	 */
	public Player getPlayer()
	{
		return _player;
	}
	
	/**
	 * It returns a CompoundTag that contains the name, x, y, z, and dimension of the entity
	 *
	 * @return A CompoundTag
	 */
	public CompoundTag toNBT()
	{
		CompoundTag tag = new CompoundTag();
		tag.putString("name", getName());
		tag.putDouble("x", getX());
		tag.putDouble("y", getY());
		tag.putDouble("z", getZ());
		tag.putFloat("yaw", getYaw());
		tag.putFloat("pitch", getPitch());
		tag.putString("dim", getLevelResourceLocation().toString());
		return tag;
	}
	
	/**
	 * This function checks if the player's current dimension matches the level's resource location.
	 *
	 * @return The method is returning a boolean value, which is determined by whether the location of the player's current dimension matches the location of the level resource location.
	 */
	public boolean sameDimension()
	{
		return getPlayer().getLevel().dimension().location().equals(getLevelResourceLocation());
	}
	
	public void rename(String name)
	{
		_name = name;
	}
	
	/**
	 * Teleports the player to the warp location.
	 */
	public void teleportTo()
	{
		Warp back = createBack(_player);
		_player.teleportTo(getLevel(), getX(), getY(), getZ(), getYaw(), getPitch());
		Warps.fromPlayer(_player).createAddOrUpdate(back);
	}
	
	/**
	 * This function calculates the warp travel parameters for a player's coordinates.
	 *
	 * @return The method `calculateTravel()` is returning an object of type `WarpTravelParameters`. The `calculateTravel()` method is overloaded and in this specific case, it is calling another version of the same method with four parameters: the player object, and three coordinates (_x, _y, _z). The returned `WarpTravelParameters` object contains information about the calculated travel distance and time.
	 */
	public WarpTravelParemeters calculateTravel()
	{
		return calculateTravel(getPlayer(), _x, _y, _z);
	}
	
}
