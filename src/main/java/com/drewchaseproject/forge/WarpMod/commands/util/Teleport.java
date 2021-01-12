package com.drewchaseproject.forge.WarpMod.commands.util;

import com.drewchaseproject.forge.WarpMod.Objects.Warp;
import com.drewchaseproject.forge.WarpMod.util.WarpPlayer;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
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
	public static void teleportToDimension(WarpPlayer player, Warp warp) {
		teleportToDimension(player, warp.getX(), warp.getY(), warp.getZ(), warp.getYaw(), warp.getPitch(), warp.getDimensionResourceLocation());
	}

	public static void teleportToDimension(WarpPlayer player, double x, double y, double z, float pitch, float yaw, ResourceLocation location) {
		world = player.getServerWorld();
		if (!location.toString().equalsIgnoreCase(player.getDimensionResourceLocation().toString())) {
			player.getServer().getWorlds().forEach(wld -> {
				if (wld.getDimensionKey().getLocation().equals(location))
					world = wld;
			});
		}
		player.getServerEntity().teleport(world, x, y, z, yaw, pitch);
	}

}
