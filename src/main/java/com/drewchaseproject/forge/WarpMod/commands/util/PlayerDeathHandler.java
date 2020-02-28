package com.drewchaseproject.forge.WarpMod.commands.util;

import com.drewchaseproject.forge.WarpMod.commands.WarpCommand;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerDeathHandler {

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		Entity e = event.getEntity();
		if (e instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.getEntity();
			WarpCommand wc = new WarpCommand();
			BlockPos pos = player.getPosition();
			float yaw = player.prevCameraYaw, pitch = player.prevRotationPitch;
			int dimension = player.dimension.getId();
			wc.back(pos, yaw, pitch, dimension, player);
		}
	}

}
