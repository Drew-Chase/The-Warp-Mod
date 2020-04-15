package com.drewchaseproject.forge.WarpMod.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class Warp {

	public enum WarpType {
		Private,
		Public,
		General
	}

	private BlockPos pos;
	private int Dimension;
	private float yaw, pitch;
	private String name;
	private EntityPlayer player;

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
	public Warp(String name, BlockPos pos, int dim, EntityPlayer player, float yaw, float pitch) {
		setPos(pos);
		setName(name);
		setPlayer(player);
		setDimension(dim);
		setYaw(yaw);
		setPitch(pitch);
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
	 * @return the dimension
	 */
	public int getDimension() {
		return Dimension;
	}

	/**
	 * @param dimension the dimension to set
	 */
	public void setDimension(int dimension) {
		Dimension = dimension;
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
	public EntityPlayer getPlayer() {
		return player;
	}

	/**
	 * @param player the player to set
	 */
	public void setPlayer(EntityPlayer player) {
		this.player = player;
	}

	@Override
	public boolean equals(Object obj) {
		try {
			if (obj instanceof Warp) {
				Warp j = (Warp) obj;
				return (j.getName().equals(getName()) && j.getPlayer().equals(getPlayer()) && j.getDimension() == getDimension() && j.getX() == getX() && j.getY() == getY() && j.getZ() == getZ());
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

}
