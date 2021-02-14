package com.drewchaseproject.forge.WarpMod.Objects;

import com.drewchaseproject.forge.WarpMod.util.WarpPlayer;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class Warp {

	public enum WarpType {
		Private,
		Public,
		General
	}

	private BlockPos pos;
	private float yaw, pitch;
	private String name;
	private ServerPlayerEntity player;
	private ServerWorld world;
	private ResourceLocation location;

	/**
	 * Creates a Warp Object
	 * 
	 * @param name
	 * @param pos
	 * @param dim
	 * @param player
	 * @param yaw
	 * @param pitch
	 */
	public Warp(String name, BlockPos pos, ServerPlayerEntity player, float yaw, float pitch, ServerWorld world, ResourceLocation location) {
		setPos(pos);
		setName(name);
		setPlayer(player);
		setYaw(yaw);
		setPitch(pitch);
		setServerWorld(world);
		setDimensionResourceLocation(location);
	}

	public Warp(String name, BlockPos pos, ServerPlayerEntity player2, ServerWorld world, ResourceLocation location) {
		setPos(pos);
		setName(name);
		setPlayer(player2);
		setYaw(0f);
		setPitch(0f);
		setServerWorld(world);
		setDimensionResourceLocation(location);
	}
	
	public Warp(String name,  ServerPlayerEntity player) {
		setPos(player.getPosition());
		setName(name);
		setPlayer(player);
		setYaw(player.getPitchYaw().x);
		setPitch(player.getPitchYaw().y);
		setServerWorld(player.getServerWorld());
		setDimensionResourceLocation(new WarpPlayer(player).getDimensionResourceLocation());
	}
	

	/**
	 * @return the pos
	 */
	public BlockPos getPos() {
		return pos;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(BlockPos pos) {
		this.pos = pos;
	}

	public void setServerWorld(ServerWorld value) {
		this.world = value;
	}

	public ServerWorld getServerWorld() {
		return world;
	}

	public void setDimensionResourceLocation(ResourceLocation value) {
		this.location = value;
	}

	public ResourceLocation getDimensionResourceLocation() {
		return location;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return getPos().getX();
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return getPos().getY();
	}

	/**
	 * @return the z
	 */
	public int getZ() {
		return getPos().getZ();
	}

	/**
	 * @return the yaw
	 */
	public float getYaw() {
		return yaw;
	}

	/**
	 * @param yaw the yaw to set
	 */
	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	/**
	 * @return the pitch
	 */
	public float getPitch() {
		return pitch;
	}

	/**
	 * @param pitch the pitch to set
	 */
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the player
	 */
	public ServerPlayerEntity getPlayer() {
		return player;
	}

	/**
	 * @param player2 the player to set
	 */
	public void setPlayer(ServerPlayerEntity player2) {
		this.player = player2;
	}

	@Override
	public boolean equals(Object obj) {
		try {
			if (obj instanceof Warp) {
				Warp j = (Warp) obj;
				return (j.getName().equals(getName()) && j.getPlayer().equals(getPlayer()) && j.getServerWorld() == getServerWorld() && j.getX() == getX() && j.getY() == getY() && j.getZ() == getZ());
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

}
