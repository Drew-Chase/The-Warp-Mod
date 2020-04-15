package com.drewchaseproject.forge.WarpMod;

import com.drewchaseproject.forge.WarpMod.commands.WarpCommand;
import com.drewchaseproject.forge.WarpMod.commands.WarpConfigCommand;
import com.google.common.eventbus.Subscribe;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("all")
@EventBusSubscriber
public class RegistryHandlers {

	public static void server(FMLServerStartingEvent event) {
		event.registerServerCommand(new WarpConfigCommand());
		event.registerServerCommand(new WarpCommand());
	}

}
