package com.drewchaseproject.forge.WarpMod.util;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.server.ServerWorld;

public class WarpPlayer extends ServerPlayerEntity implements IContainerListener {

	private ServerWorld worldIn;

	private ServerPlayerEntity serverEntity;

	public WarpPlayer(MinecraftServer server, ServerWorld worldIn, GameProfile profile, PlayerInteractionManager interactionManagerIn) {
		super(server, worldIn, profile, interactionManagerIn);
		this.worldIn = worldIn;
	}

	public WarpPlayer(ServerPlayerEntity player) {
		super(player.server, player.getServerWorld(), player.getGameProfile(), player.interactionManager);
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
		return world.getDimensionKey().getLocation();
	}

	public DimensionType getDimension() {
		return world.getDimensionType();
	}

	public ServerPlayerEntity getServerEntity() {
		return serverEntity;
	}

}
