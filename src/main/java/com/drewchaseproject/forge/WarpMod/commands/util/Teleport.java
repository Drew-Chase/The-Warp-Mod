package com.drewchaseproject.forge.WarpMod.commands.util;

import com.drewchaseproject.forge.WarpMod.Objects.Warp;
import com.drewchaseproject.forge.WarpMod.util.WarpPlayer;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Teleporter;
import net.minecraft.world.server.ServerWorld;

/**
 * A Custom interdimensional teleporting class
 * 
 * @author Drew Chase
 *
 */
@SuppressWarnings("all")
public class Teleport extends Teleporter {
	private static ServerWorld world;
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
	public static void teleportToDimension(ServerPlayerEntity player, Warp warp) {
		teleportToDimension(player, warp.getX(), warp.getY(), warp.getZ(), warp.getYaw(), warp.getPitch(), warp.getDimensionResourceLocation());
	}

	public static void teleportToDimension(ServerPlayerEntity player, double x, double y, double z, float pitch, float yaw, ResourceLocation location) {
		world = player.getServerWorld();
		if (!location.toString().equalsIgnoreCase(new WarpPlayer(player).getDimensionResourceLocation().toString())) {
			player.getServer().getWorlds().forEach(wld -> {
				if (wld.getDimensionKey().getLocation().equals(location))
					world = wld;
			});
		}
		player.teleport(world, x + .5, y, z + .5, yaw, pitch);
	}

}
