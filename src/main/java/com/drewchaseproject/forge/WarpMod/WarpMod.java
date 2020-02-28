package com.drewchaseproject.forge.WarpMod;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(WarpMod.MOD_ID)
public class WarpMod {

	public static WarpMod instance;

	public static final String MOD_ID = "warp_mod";
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();

	public WarpMod() {
		instance = this;
		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		DistExecutor.runForDist(() -> () -> new SideProxy.Client(), () -> () -> new SideProxy.Server());

	}

	public static String getVersion() {
		Optional<? extends ModContainer> o = ModList.get().getModContainerById(MOD_ID);
		if (o.isPresent())
			return o.get().getModInfo().getVersion().toString();
		return "NONE";
	}

	public static boolean isDevBuild() {
		String version = getVersion();
		return "NONE".equals(version);
	}

	public static ResourceLocation getId(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	// You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
	// Event bus for receiving Registry Events)
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
			// register a new block here
			LOGGER.info("HELLO from Register Block");
		}
	}
}
