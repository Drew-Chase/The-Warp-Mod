package com.drewchaseproject.forge.WarpMod;

import com.drewchaseproject.forge.WarpMod.commands.WarpCommand;
import com.drewchaseproject.forge.WarpMod.commands.util.PlayerDeathHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class SideProxy {

	SideProxy() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(SideProxy::commonSetup);
		// Register the enqueueIMC method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(SideProxy::enqueueIMC);
		// Register the processIMC method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(SideProxy::processIMC);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
		WarpMod.LOGGER.debug("commonSetup for " + WarpMod.MOD_ID);
		MinecraftForge.EVENT_BUS.register(new PlayerDeathHandler());
	}

	private static void enqueueIMC(final InterModEnqueueEvent event) {
	}

	private static void processIMC(final InterModProcessEvent event) {
	}

	private void serverStarting(FMLServerStartingEvent event) {
//		new WarpCommand().instance.register(event.getCommandDispatcher());
		WarpCommand wc = new WarpCommand();
		wc.register(event.getCommandDispatcher());

	}

	static class Client extends SideProxy {
		Client() {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(Client::clientSetup);
		}

		private static void clientSetup(FMLClientSetupEvent event) {

		}
	}

	static class Server extends SideProxy {
		Server() {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(Server::serverSetup);
		}

		private static void serverSetup(FMLDedicatedServerSetupEvent event) {

		}

	}

}
