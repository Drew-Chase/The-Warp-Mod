package com.drewchaseproject.forge.WarpMod;

import com.drewchaseproject.forge.WarpMod.WarpMod.LogType;
import com.drewchaseproject.forge.WarpMod.commands.WarpCommand;
import com.drewchaseproject.forge.WarpMod.commands.WarpConfigCommand;
import com.drewchaseproject.forge.WarpMod.commands.util.CommandHandler;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@SuppressWarnings("all")
public class SideProxy {

	SideProxy() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(SideProxy::commonSetup);
		MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
		WarpMod.log(LogType.Debug, "commonSetup for " + WarpMod.MOD_ID);
		MinecraftForge.EVENT_BUS.register(new CommandHandler.PlayerDeathHandler());
	}

	private void serverStarting(FMLServerStartingEvent event) {
		new WarpCommand().register(event.getCommandDispatcher());
		new WarpConfigCommand().register(event.getCommandDispatcher());
	}

	public static class Client extends SideProxy {
		public static KeyBinding[] bindings = new KeyBinding[1];

		Client() {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
		}

		private void clientSetup(FMLClientSetupEvent event) {
//			bindings[0] = new KeyBinding("key.openModGUI", KeyEvent.VK_M, "key.category.warp_mod");
//			ClientRegistry.registerKeyBinding(bindings[0]);
//			MinecraftForge.EVENT_BUS.register(new ModMenuHandler());
		}
	}

	public static class Server extends SideProxy {
		Server() {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(Server::serverSetup);
		}

		private static void serverSetup(FMLDedicatedServerSetupEvent event) {

		}

	}

}
