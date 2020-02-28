package tk.dccraft.warp_mod;

import java.util.logging.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import tk.dccraft.warp_mod.util.ConfigHandler;
import tk.dccraft.warp_mod.util.PlayerEventHandler;
import tk.dccraft.warp_mod.util.References;
import tk.dccraft.warp_mod.util.RegistryHandlers;

/**
 * 
 * Main class instance
 * @author Drew Chase
 * @version 1.3.3
 *
 */
@Mod(modid = References.mod_id, name = References.mod_name, acceptableRemoteVersions = "*")
public class Main {

	@Instance
	public static Main instance;

	static Logger log;

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		log = Logger.getLogger(References.mod_id);
		ConfigHandler.loadConfig("config.cfg");
	}

	@EventHandler
	public static void postInit(FMLPostInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
	}

	@EventHandler
	public static void serverInit(FMLServerStartingEvent event) {
		RegistryHandlers.serverRegistries(event);
	}

	/**
	 * Sends a console message
	 * @param msg
	 */
	public void consoleMessage(String msg) {
		if (ConfigHandler.getDebugMode())
			log.info(msg);
	}

}
