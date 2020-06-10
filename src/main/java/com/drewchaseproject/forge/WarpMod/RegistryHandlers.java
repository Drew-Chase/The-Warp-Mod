package com.drewchaseproject.forge.WarpMod;

import com.drewchaseproject.forge.WarpMod.commands.WarpCommand;
import com.drewchaseproject.forge.WarpMod.commands.WarpConfigCommand;
import com.drewchaseproject.forge.WarpMod.commands.util.CommandHandler.PlayerDeathHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@SuppressWarnings("all")
@EventBusSubscriber
public class RegistryHandlers {

	public static void server(FMLServerStartingEvent event) {
		event.registerServerCommand(new WarpConfigCommand());
		event.registerServerCommand(new WarpCommand());
		MinecraftForge.EVENT_BUS.register(new PlayerDeathHandler());
	}

}
