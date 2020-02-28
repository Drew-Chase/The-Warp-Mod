package com.drewchaseproject.forge.WarpMod.client.menu.gui;

import java.util.ArrayList;
import java.util.List;

import com.drewchaseproject.forge.WarpMod.SideProxy.Client;
import com.drewchaseproject.forge.WarpMod.WarpMod;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseClickedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.config.GuiButtonExt;
/**
 * Warp GUI
 * @author Drew Chase
 *
 */
@SuppressWarnings("all")
public class WarpModGUI extends Screen {

	private final ResourceLocation texture = WarpMod.getId("textures/gui/menu.png");
	int guiWidth = 154, guiHeight = 175;

	public WarpModGUI() {
		super(new StringTextComponent("Warp Menu"));
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		renderBackground();
		Minecraft.getInstance().getTextureManager().bindTexture(texture);
		drawString(font, "Warp Menu", (width / 2) - font.getStringWidth("Warp Menu (WIP)"), 15, 0xffffff);
		GlStateManager.pushMatrix();
		{
			GlStateManager.enableAlphaTest();
			GlStateManager.enableBlend();
			GlStateManager.color4f(1, 1, 1, .5f);
			Minecraft.getInstance().getTextureManager().bindTexture(texture);
//			GuiUtils.drawTexturedModalRect(centerX, centerY, 0, 0, guiWidth, guiHeight, 0); 
		}
		GlStateManager.popMatrix();
		super.render(mouseX, mouseY, partialTicks);

	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(int keycode, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (keycode == Client.bindings[0].getKey().getKeyCode()) {
			onClose();
			return true;
		}
		// TODO Auto-generated method stub
		return super.keyPressed(keycode, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	protected void init() {
		super.init();
		buttons.clear();
		List<Button> btn = new ArrayList<Button>();
		
		GuiButtonExt list = new GuiButtonExt((width / 2) - 75, 75, 100, 20, "List", new IPressable() {

			@Override
			public void onPress(Button b) {
				System.out.println("lol");
				b.onPress();
			}

		}) {
			@Override
			public void onClick(double p_onClick_1_, double p_onClick_3_) {
				super.onClick(p_onClick_1_, p_onClick_3_);
				System.out.println("lol");
			}
		};
		btn.add(list);
		buttons.add(list);

	}
}
