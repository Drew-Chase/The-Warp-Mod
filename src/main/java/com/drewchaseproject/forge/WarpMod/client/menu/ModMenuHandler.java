package com.drewchaseproject.forge.WarpMod.client.menu;

import com.drewchaseproject.forge.WarpMod.SideProxy.Client;
import com.drewchaseproject.forge.WarpMod.client.menu.gui.WarpModGUI;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("all")
public class ModMenuHandler {

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onEvent(KeyInputEvent event) {
		KeyBinding[] keyBindings = Client.bindings;

		if (keyBindings[0].isPressed()) {
			// DEBUG
			// do stuff for this key binding here
			Minecraft.getInstance().displayGuiScreen(new WarpModGUI());
			// remember you may need to send packet to server
		}
	}

}
