package chase.minecraft.architectury.warpmod.client.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WorldmapScreen extends Screen
{
	protected WorldmapScreen()
	{
		super(Component.translatable("gui.warpmod.map.title"));
	}
}
