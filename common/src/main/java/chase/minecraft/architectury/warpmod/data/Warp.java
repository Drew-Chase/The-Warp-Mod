package chase.minecraft.architectury.warpmod.data;

import chase.minecraft.architectury.warpmod.WarpMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec2;

import java.util.Objects;
import java.util.Random;

/**
 * Warp object holds name, position, and player, and add it to the player's warps.
 */
@SuppressWarnings("unused")
public class Warp
{
	
	private final String _name;
	private final int _x, _y, _z;
	private final float _yaw, _pitch;
	private final ServerPlayer _player;
	private ServerLevel _dimension;
	
	private Warp(String name, int x, int y, int z, float yaw, float pitch, ServerPlayer player)
	{
		_x = x;
		_y = y;
		_z = z;
		_yaw = yaw;
		_pitch = pitch;
		_name = name;
		_player = player;
		_dimension = player.getLevel();
	}
	
	private Warp(String name, int x, int y, int z, float yaw, float pitch, ServerPlayer player, ServerLevel level)
	{
		this(name, x, y, z, yaw, pitch, player);
		_dimension = level;
	}
	
	private Warp(String name, int x, int y, int z, float yaw, float pitch, ServerPlayer player, ResourceLocation level)
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
	public int getX()
	{
		return _x;
	}
	
	/**
	 * Gets the Y coordinate of the warp.
	 *
	 * @return the y coordinate
	 */
	public int getY()
	{
		return _y;
	}
	
	/**
	 * Gets the z coordinate of the warp
	 *
	 * @return the z coordinate
	 */
	public int getZ()
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
	 * Gets the <b>BlockPos</b> of the warp
	 *
	 * @return the <b>BlockPos</b>
	 */
	public BlockPos getPos()
	{
		return new BlockPos(_x, _y, _z);
	}
	
	/**
	 * Gets the <b>ServerPlayer</b> that owns the warp.
	 *
	 * @return the <b>ServerPlayer</b>
	 */
	public ServerPlayer getPlayer()
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
		tag.putInt("x", getX());
		tag.putInt("y", getY());
		tag.putInt("z", getZ());
		tag.putFloat("yaw", getYaw());
		tag.putFloat("pitch", getPitch());
		tag.putString("dim", getLevelResourceLocation().toString());
		return tag;
	}
	
	/**
	 * Teleports the player to the warp location.
	 */
	public void teleportTo()
	{
		Warp back = createBack(_player);
		_player.teleportTo(getLevel(), getX() + .5, getY(), getZ() + .5, getYaw(), getPitch());
		Warps.fromPlayer(_player).createAddOrUpdate(back);
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
			
			ServerLevel level = player.getLevel();
			
			BlockPos blockPos = BlockPos.containing(randX, y, randZ);
			// Checking if the block is safe to spawn on.
			while (!isSafe && blockPos.getY() > level.getMinBuildHeight())
			{
				BlockPos belowPos = blockPos.below();
				BlockState blockState = level.getBlockState(belowPos);
				BlockState headBlockState = level.getBlockState(blockPos.above());
				BlockState feetBlockState = level.getBlockState(blockPos);
				boolean legSafe = !feetBlockState.getMaterial().blocksMotion() && !feetBlockState.getMaterial().isLiquid() && feetBlockState.getMaterial() != Material.FIRE;
				boolean headSafe = !headBlockState.getMaterial().blocksMotion() && !headBlockState.getMaterial().isLiquid() && headBlockState.getMaterial() != Material.FIRE;
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
		if (isSafe)
			player.teleportTo(randX + .5, y + 1, randZ + .5);
		return isSafe ? dist / 2 : 0;
	}
	
	
	/**
	 * It returns a random coordinate within a specified range of the player
	 *
	 * @param player      The player to teleport
	 * @param maxDistance The maximum distance from the player that the coordinates can be.
	 * @param minDistance The minimum distance from the player that the coordinates can be.
	 * @return A Vec2 object with the x and z coordinates of a random location within the specified range.
	 */
	private static Vec2 GetRandomCords(ServerPlayer player, int maxDistance, int minDistance)
	{
		Random rand = new Random();
		int randX, randZ;
		boolean negX, negZ;
		negX = rand.nextBoolean();
		negZ = rand.nextBoolean();
		randX = rand.nextInt(minDistance, maxDistance);
		if (negX)
			randX *= -1;
		randZ = rand.nextInt(minDistance, maxDistance);
		if (negZ)
			randZ *= -1;
		
		return new Vec2(player.getBlockX() + randX, player.getBlockZ() + randZ);
	}
	
	/**
	 * Create a warp named 'back' at the player's current location, and set it to be a temporary warp.
	 *
	 * @param player The player who is creating the warp.
	 * @return A Warp object.
	 */
	public static Warp createBack(ServerPlayer player)
	{
		return Warp.create("back", player.getBlockX(), player.getBlockY(), player.getBlockZ(), player.getYRot(), player.getXRot(), player, true);
	}
	
	/**
	 * It takes a CompoundTag, and a ServerPlayer, and returns a Warp
	 *
	 * @param tag    The tag that was sent from the client.
	 * @param player The player who is warping.
	 * @return A Warp object
	 */
	public static Warp fromTag(CompoundTag tag, ServerPlayer player)
	{
		String name = tag.getString("name");
		int x = tag.getInt("x");
		int y = tag.getInt("y");
		int z = tag.getInt("z");
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
	public static Warp create(String name, int x, int y, int z, float yaw, float pitch, ServerPlayer player, boolean add)
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
	public static Warp create(String name, int x, int y, int z, float yaw, float pitch, ServerPlayer player, ResourceLocation level, boolean add)
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
	public static Warp create(String name, int x, int y, int z, float yaw, float pitch, ServerPlayer player, ServerLevel level, boolean add)
	{
		Warp warp = new Warp(name, x, y, z, yaw, pitch, player, level);
		if (add)
		{
			Warps playersWarps = Warps.fromPlayer(player);
			playersWarps.createAddOrUpdate(warp);
		}
		return warp;
	}
	
}
