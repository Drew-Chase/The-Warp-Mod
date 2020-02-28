package com.drewchaseproject.forge.WarpMod;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.drewchaseproject.forge.WarpMod.config.ConfigHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/**
 * Main Class for the Warp Mod
 * 
 * @author Drew Chase
 *
 */
@Mod(WarpMod.MOD_ID)
@SuppressWarnings("all")
public class WarpMod {

	public static WarpMod instance;

	public static final String MOD_ID = "warp_mod";
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();

	public WarpMod() {
		instance = this;
		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		DistExecutor.runForDist(() -> () -> new SideProxy.Client(), () -> () -> new SideProxy.Server());
		ConfigHandler ch = new ConfigHandler();
		ch.readConfig();
		if (ch.getDebugMode()) {
			if (!ch.getAllowedPublicPlayers().isEmpty() && !ch.areAllPlayersAllowedPublic()) {
				for (String s : ch.getAllowedPublicPlayers()) {
					WarpMod.log(LogType.Warning, s + " is allowed for public");
				}
			} else if (ch.areAllPlayersAllowedPublic()) {
				WarpMod.log(LogType.Warning, "All Public Players are Allowed");
			} else if (ch.getAllowedPublicPlayers().isEmpty()) {
				WarpMod.log(LogType.Warning, "No Public Players are Allowed");
			}

			if (!ch.getAllowedPlayers().isEmpty() && !ch.areAllPlayersAllowed()) {
				for (String s : ch.getAllowedPublicPlayers()) {
					WarpMod.log(LogType.Warning, s + " is allowed");
				}
			} else if (ch.areAllPlayersAllowed()) {
				WarpMod.log(LogType.Warning, "All Players are Allowed");
			} else if (ch.getAllowedPlayers().isEmpty()) {
				WarpMod.log(LogType.Warning, "No Players are Allowed");
			}

		}
	}

	public enum LogType {
		Debug,
		Error,
		Warning,
		Info
	}

	public static void log(LogType type, String message) {
		if (type == LogType.Info) {
			LOGGER.info(message);
		}
		if (ConfigHandler.getDebugMode()) {
			if (type == LogType.Debug) {
				LOGGER.debug(message);
			} else if (type == LogType.Warning) {
				LOGGER.warn(message);
			} else if (type == LogType.Error) {
				LOGGER.error(message);
			}
		}
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

	/**
	 * Sends message to current player
	 * 
	 * @param player
	 * @param message
	 */
	public static void sendMessage(Entity player, Object message) {
		if (player != null && player instanceof PlayerEntity) {
			((PlayerEntity) player).sendMessage(new StringTextComponent(message + ""));
		} else {
			WarpMod.log(LogType.Info, "" + message);
		}
	}

}
