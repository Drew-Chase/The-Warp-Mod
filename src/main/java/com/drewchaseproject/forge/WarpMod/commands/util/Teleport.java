package com.drewchaseproject.forge.WarpMod.commands.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Teleporter;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

/**
 * A Custom interdimensional teleporting class
 * 
 * @author Drew Chase
 *
 */
@SuppressWarnings("all")
public class Teleport extends Teleporter {
	private final ServerWorld world;
	private double x, y, z;
	private float yaw, pitch;

	/**
	 * Gathers information for teleportation
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param yaw
	 * @param pitch
	 * @author Drew Chase
	 */
	public Teleport(ServerWorld world, double x, double y, double z, float yaw, float pitch) {
		super(world);
		this.world = world;
		this.y = y;
		this.x = x;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	/**
	 * Teleports a player to a specific position in a specific dimension
	 * 
	 * @param player
	 * @param dimension
	 * @param x
	 * @param y
	 * @param z
	 * @param yaw
	 * @param pitch
	 * @author Drew Chase
	 */
	public static void teleportToDimension(PlayerEntity player, int dimension, double x, double y, double z, float yaw, float pitch) {
		int oldDimension = player.getEntityWorld().getDimension().getType().getId();
		MinecraftServer server = player.getEntityWorld().getServer();
		DimensionType type = DimensionType.getById(dimension);
		ServerWorld worldServer = server.getWorld(type);

		if (oldDimension == dimension) {
			player.setPositionAndUpdate(x, y, z);
			player.rotationPitch = pitch;
			player.rotationYaw = yaw;
			return;
		}

		if (worldServer == null || server == null)
			throw new IllegalArgumentException("Dimension: " + dimension + " not found");
		player.changeDimension(type);
		player.setPositionAndUpdate(x, y, z);
		player.rotationPitch = pitch;
		player.rotationYaw = yaw;
	}

}
