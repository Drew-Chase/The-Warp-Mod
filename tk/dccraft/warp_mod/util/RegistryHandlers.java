package tk.dccraft.warp_mod.util;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import tk.dccraft.warp_mod.commands.WarpCommand;

/**
 * Handles various Registries
 * 
 * @author Drew Chase
 *
 */
@EventBusSubscriber
public class RegistryHandlers {

	/**
	 * Registers commands
	 * 
	 * @param event
	 */
	public static void serverRegistries(FMLServerStartingEvent event) {
		event.registerServerCommand(new WarpCommand());
	}

}
