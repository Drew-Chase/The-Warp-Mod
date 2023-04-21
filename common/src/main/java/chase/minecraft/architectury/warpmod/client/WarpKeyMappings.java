package chase.minecraft.architectury.warpmod.client;

import chase.minecraft.architectury.warpmod.client.gui.screen.EditWarpScreen;
import chase.minecraft.architectury.warpmod.client.gui.screen.WarpListScreen;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
// This is an enum in Java that defines a key mapping for a warp mod in Minecraft. It creates a new `KeyMapping` object with a localized name, a key code, and a category. It also stores a `Runnable` object that will be executed when the key is pressed. The enum has methods to register the key mapping, check if the key has been clicked, and execute the `Runnable` object when the key is pressed.
public enum WarpKeyMappings
{
	OPEN_WARPS("open_warps_screen", InputConstants.KEY_U, () ->
	{
		Minecraft client = Minecraft.getInstance();
		client.setScreen(new WarpListScreen(null));
	}),
	CREATE_WARP("open_create_warps_screen", InputConstants.KEY_B, () ->
	{
		Minecraft client = Minecraft.getInstance();
		client.setScreen(new EditWarpScreen(null));
	})
	
//	,
//	OPEN_PLAYER_SCREEN("open_player_list_screen", InputConstants.KEY_P, () ->
//	{
//		Minecraft client = Minecraft.getInstance();
//		if (client.isSingleplayer())
//		{
//			assert client.player != null;
//			client.player.displayClientMessage(Component.literal("Players Warp Screen is only available in multiplayer!"), true);
//		} else
//			client.setScreen(new PlayerListScreen(null));
//	})
	
	;
	private static final String WARP_KEYBINDING_CATEGORY = "key.categories.warpmod";
	private final Runnable _consumed;
	private final KeyMapping _key;
	
	// This is a constructor for the `WarpKeyMappings` enum. It takes three parameters: `id` (a string), `keycode` (an integer), and `whenConsumed` (a `Runnable` object). It creates a new `KeyMapping` object with a localized name based on the `id` parameter, a key code based on the `keycode` parameter, and a category of "key.categories.warpmod". It also stores the `whenConsumed` parameter as a `Runnable` object in the `_consumed` field.
	WarpKeyMappings(String id, int keycode, Runnable whenConsumed)
	{
		_key = new KeyMapping("key.warpmod.%s".formatted(id), keycode, WARP_KEYBINDING_CATEGORY);
		_consumed = whenConsumed;
	}
	
	/**
	 * This function registers a key mapping in Java.
	 */
	public void register()
	{
		KeyMappingRegistry.register(_key);
	}
	
	/**
	 * This function checks if a key has been clicked and if so, it executes a certain action.
	 */
	public void tick()
	{
		if (_key.consumeClick())
			execute();
	}
	
	/**
	 * This function executes the "run" method of the "_consumed" object.
	 */
	public void execute()
	{
		_consumed.run();
	}
}
