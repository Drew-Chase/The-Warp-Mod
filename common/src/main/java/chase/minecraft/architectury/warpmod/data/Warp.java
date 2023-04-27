package chase.minecraft.architectury.warpmod.data;

import chase.minecraft.architectury.warpmod.client.WarpModClient;
import chase.minecraft.architectury.warpmod.client.gui.waypoint.WaypointColor;
import chase.minecraft.architectury.warpmod.networking.WarpNetworking;
import chase.minecraft.architectury.warpmod.utils.WorldUtils;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.PacketSender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;

/**
 * Object for storing Warp locations
 */
@SuppressWarnings("all")
public class Warp
{
	private String name;
	private double x, y, z;
	private float pitch, yaw;
	private WaypointColor color = WaypointColor.WHITE;
	private ResourceLocation dimension;
	private final Player player;
	
	private ResourceLocation icon = WaypointIcons.DEFAULT;
	private boolean temporary = false;
	private boolean visible = true;
	public static Warp EMPTY = new Warp("", 0, 0, 0, 0, 0, new ResourceLocation("overworld"), null);
	
	
	public Warp(String name, double x, double y, double z, float pitch, float yaw, ResourceLocation dimension, Player player)
	{
		this(name, x, y, z, pitch, yaw, dimension, player, false, WaypointIcons.DEFAULT, WaypointColor.WHITE, true);
	}
	
	public Warp(String name, double x, double y, double z, float pitch, float yaw, ResourceLocation dimension, Player player, boolean temporary)
	{
		this(name, x, y, z, pitch, yaw, dimension, player, temporary, WaypointIcons.DEFAULT, WaypointColor.WHITE, true);
	}
	
	
	public Warp(String name, double x, double y, double z, float pitch, float yaw, ResourceLocation dimension, Player player, boolean temporary, ResourceLocation icon, WaypointColor color, boolean visible)
	{
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
		this.dimension = dimension;
		this.player = player;
		this.temporary = temporary;
		this.icon = icon;
		this.color = color;
		this.visible = visible;
	}
	
	/**
	 * @return the name of the warp
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @return the y coordinate
	 */
	public double getX()
	{
		return x;
	}
	
	/**
	 * @return the y coordinate
	 */
	public double getY()
	{
		return y;
	}
	
	/**
	 * @return the z coordinate
	 */
	public double getZ()
	{
		return z;
	}
	
	/**
	 * @return the pitch looking direction
	 */
	public float getPitch()
	{
		return pitch;
	}
	
	/**
	 * @return the yaw looking direction
	 */
	public float getYaw()
	{
		return yaw;
	}
	
	/**
	 * Gets the color of the warp, this will be used for ClientSide waypoint rendering.
	 *
	 * @return
	 */
	@NotNull
	public WaypointColor getColor()
	{
		return color;
	}
	
	/**
	 * Gets the dimensions resource location of the warp
	 *
	 * @return
	 */
	public ResourceLocation getDimension()
	{
		return dimension;
	}
	
	/**
	 * Gets the player that the warp belongs to
	 *
	 * @return
	 */
	public Player getPlayer()
	{
		return player;
	}
	
	/**
	 * Gets the resource location of the icon.
	 *
	 * @return the icons resource location
	 */
	public ResourceLocation getIcon()
	{
		return icon;
	}
	
	/**
	 * Returns if the warp is temporary.
	 *
	 * @return TRUE if the warp is temporary, FALSE otherwise.
	 */
	public boolean temporary()
	{
		return temporary;
	}
	
	/**
	 * @return if the warp is visible in the world or not.
	 */
	public boolean visible() {return visible;}
	
	/**
	 * This Java function updates the location and orientation of a resource with a given name and dimension.
	 *
	 * @param name      A string representing the name of the object being updated.
	 * @param x         The x-coordinate of the location to be updated.
	 * @param y         The parameter "y" represents the vertical position of an object in a three-dimensional space. It is a double data type, which means it can store decimal values.
	 * @param z         The z-coordinate of the location being updated.
	 * @param pitch     Pitch is the vertical angle of the player's head, measured in degrees. It determines the up and down direction of the player's view. A pitch of 0 means the player is looking straight ahead, while a pitch of 90 means the player is looking straight up.
	 * @param yaw       Yaw is a rotation around the vertical axis, measured in degrees. It determines the direction the entity is facing horizontally. A yaw of 0 means the entity is facing south, while a yaw of 90 means the entity is facing west.
	 * @param dimension The dimension parameter is a ResourceLocation object that represents the dimension in which the entity is located. A ResourceLocation is a unique identifier for a resource in Minecraft, and in this case, it is used to identify the dimension by its name. For example, the overworld dimension has the name "minecraft
	 */
	public void update(String name, double x, double y, double z, float pitch, float yaw, ResourceLocation dimension)
	{
		update(name, x, y, z, pitch, yaw, dimension, null, null);
	}
	
	/**
	 * This function updates the properties of a waypoint object with the given parameters, including name, coordinates, orientation, dimension, color, and icon.
	 *
	 * @param name      A string representing the name of the waypoint.
	 * @param x         The x-coordinate of the waypoint's location.
	 * @param y         The y parameter is a double data type representing the vertical coordinate of the waypoint location.
	 * @param z         z is a double variable representing the z-coordinate of a waypoint location in a three-dimensional space.
	 * @param pitch     The pitch angle of the waypoint in degrees. It represents the vertical angle of the waypoint from the horizon.
	 * @param yaw       The yaw parameter represents the horizontal rotation of the waypoint in degrees. It determines the direction the waypoint is facing.
	 * @param dimension ResourceLocation representing the dimension of the waypoint. A ResourceLocation is a unique identifier for a resource in Minecraft, consisting of a namespace and a path. In this case, it is used to identify the dimension of the waypoint, such as "minecraft:overworld" or "minecraft:nether".
	 * @param color     The color of the waypoint in RGB format. If no color is specified, it defaults to white (0xFF_FF_FF).
	 * @param icon      The icon parameter is a ResourceLocation object that represents the location of the icon image file for the waypoint. If no icon is specified, it will default to the WaypointIcons.DEFAULT image.
	 */
	public void update(String name, double x, double y, double z, float pitch, float yaw, ResourceLocation dimension, @Nullable WaypointColor color, @Nullable ResourceLocation icon)
	{
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
		this.dimension = dimension;
		this.color = color == null ? WaypointColor.WHITE : color;
		this.icon = icon == null ? WaypointIcons.DEFAULT : icon;
	}
	
	/**
	 * Gets the distance from the provided Vec3
	 *
	 * @param from - The location to get the distance from.
	 * @return the distance
	 */
	public double distance(Vec3 from)
	{
		return from.distanceTo(getPosition());
	}
	
	/**
	 * Gets the distance from the player
	 *
	 * @return the distance
	 */
	public double distance()
	{
		return distance(player.getEyePosition());
	}
	
	/**
	 * Gets the location of the warp
	 *
	 * @return A new instance of the Vec3 class with the x, y, and z coordinates of the current object.
	 */
	public Vec3 getPosition()
	{
		return new Vec3(x, y, z);
	}
	
	/**
	 * This function teleports the player to the warp location
	 */
	public void teleport(@Nullable ServerPlayer player)
	{
		if (player != null)
		{
			Warps.fromPlayer(player).createBack();
			ServerLevel level = WorldUtils.getLevelFromID(player.server, dimension);
			if (level != null)
			{
				player.teleportTo(level, x, y, z, pitch, yaw);
			}
			Warps.fromPlayer(player).createBack();
		} else
		{
			FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
			data.writeInt(name.length());
			data.writeCharSequence(name, Charset.defaultCharset());
			PacketSender.c2s().send(WarpNetworking.TELEPORT, data);
		}
	}
	
	/**
	 * Converts this warp to NBT data
	 *
	 * @return A CompoundTag object is being returned.
	 */
	public CompoundTag toNbt()
	{
		CompoundTag tag = new CompoundTag();
		tag.putString("name", name);
		tag.putDouble("x", x);
		tag.putDouble("y", y);
		tag.putDouble("z", z);
		tag.putFloat("pitch", pitch);
		tag.putFloat("yaw", yaw);
		tag.putString("dimension", dimension.toString());
		tag.putString("color", color.getName());
		tag.putString("icon", WaypointIcons.getName(icon));
		tag.putBoolean("temp", temporary);
		tag.putBoolean("visible", visible);
		return tag;
	}
	
	/**
	 * This function takes in a CompoundTag and a Player object, extracts specific data from the tag, and returns a new Warp object with that data.
	 *
	 * @param tag    A CompoundTag object that contains the data for the Warp object being created.
	 * @param player The player who created the warp.
	 * @return A new instance of the Warp class with the specified parameters.
	 */
	public static Warp fromTag(CompoundTag tag, Player player)
	{
		String name = tag.getString("name");
		double x = tag.getDouble("x");
		double y = tag.getDouble("y");
		double z = tag.getDouble("z");
		float yaw = tag.getFloat("yaw");
		float pitch = tag.getFloat("pitch");
		ResourceLocation dimension = new ResourceLocation(tag.getString("dimension"));
		boolean visible = true;
		boolean temp = false;
		WaypointColor color = WaypointColor.WHITE;
		ResourceLocation icon = WaypointIcons.DEFAULT;
		
		if (tag.contains("color"))
		{
			color = WaypointColor.getByName(tag.getString("color"));
		}
		if (tag.contains("icon"))
		{
			icon = WaypointIcons.getByName(tag.getString("icon"));
		}
		if (tag.contains("visible"))
		{
			visible = tag.getBoolean("visible");
		}
		if (tag.contains("temp"))
		{
			visible = tag.getBoolean("temp");
		}
		if (!WarpModClient.dimensions.contains(dimension.toString()))
		{
			WarpModClient.dimensions.add(dimension.toString());
		}
		return new Warp(name, x, y, z, yaw, pitch, dimension, player, temp, icon, color, visible);
		
	}
	
}
