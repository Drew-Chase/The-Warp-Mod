package com.drewchaseproject.forge.WarpMod.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.server.ServerWorld;

public class WarpPlayer {

	private ServerWorld worldIn;

	private ServerPlayerEntity serverEntity;

	public WarpPlayer(ServerPlayerEntity player) {
		this.worldIn = player.getServerWorld();
		this.serverEntity = player;
	}

	public ServerWorld getServerWorld() {
		return worldIn;
	}

	public BlockPos getPosition() {
		return new BlockPos(serverEntity.getPosX(), serverEntity.getPosY(), serverEntity.getPosZ());
	}

	public BlockPos getWorldSpawn() {
		return getServerWorld().getSpawnPoint();
	}

	public ResourceLocation getDimensionResourceLocation() {
		return worldIn.getDimensionKey().getLocation();
	}

	public DimensionType getDimension() {
		return worldIn.getDimensionType();
	}

	public ServerPlayerEntity getServerEntity() {
		return serverEntity;
	}

}
