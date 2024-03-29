package chase.minecraft.architectury.warpmod;

import chase.minecraft.architectury.warpmod.client.WarpModClient;
import chase.minecraft.architectury.warpmod.commands.WarpCommand;
import chase.minecraft.architectury.warpmod.networking.ServerNetworking;
import chase.minecraft.architectury.warpmod.utils.WorldUtils;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The WarpMod class declares a constant variable and a static logger, and registers the WarpCommand with the CommandRegistrationEvent in Java.
 */
@SuppressWarnings("all")
public class WarpMod
{
	// Declaring a constant variable named `MOD_ID` with the value "warpmod". The variable is marked as `public` so it can be accessed from other classes, `static` so it can be accessed without creating an instance of the class, and `final` so its value cannot be changed once it is initialized. This is a common practice in Minecraft mods to identify the mod by its unique ID.
	public static final String MOD_ID = "warpmod";
	// This line of code is declaring a static variable named `log` of type `Logger` and initializing it with the `getLogger()` method from the `LogManager` class. The `getLogger()` method takes a string argument that represents the name of the logger. In this case, the name of the logger is "WarpMod". This logger can be used to log messages and errors throughout the mod.
	public static Logger log = LogManager.getLogger("WarpMod");
	
	/**
	 * This function registers the WarpCommand with the CommandRegistrationEvent in Java.
	 */
	public static void init()
	{
		CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) ->
		{
			WarpCommand.register(dispatcher);
		});
		ServerNetworking.init();
		
		PlayerEvent.CHANGE_DIMENSION.register((player, oldLevel, newLevel) ->
		{
			WorldUtils.removeTravelBar(player);
		});
	}
	
	/**
	 * Allows the creation of Resource Location with the warp namespace.
	 *
	 * @param name
	 * @return
	 */
	public static ResourceLocation id(String name)
	{
		return new ResourceLocation(MOD_ID, name);
	}
	
}