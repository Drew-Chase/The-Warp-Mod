package chase.minecraft.architectury.warpmod.client.gui.component;

import chase.minecraft.architectury.warpmod.networking.WarpNetworking;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

import static chase.minecraft.architectury.warpmod.client.gui.GUIFactory.createButton;

public class PlayerListComponent extends ContainerObjectSelectionList<PlayerListComponent.Entry>
{
	private final Screen parent;
	
	public PlayerListComponent(Screen parent)
	{
		super(Minecraft.getInstance(), (int) (parent.width), parent.height + 15, 30, parent.height - 32, 30);
		this.parent = parent;
		if (PacketSender.c2s().canSend(WarpNetworking.LIST))
		{
			PacketSender.c2s().send(WarpNetworking.LIST, new FriendlyByteBuf(Unpooled.buffer()));
		}
		
		refreshEntries();
		
	}
	
	@Override
	protected int getScrollbarPosition()
	{
		return width - 10;
	}
	
	@Override
	public int getRowWidth()
	{
		return width - 25;
	}
	
	public void refreshEntries()
	{
		assert minecraft.player != null;
		ClientPacketListener clientPacketListener = minecraft.player.connection;
		PlayerInfo[] players = clientPacketListener.getOnlinePlayers().toArray(new PlayerInfo[0]);
		
		for (PlayerInfo info : players)
		{
			addEntry(new PlayerListComponent.PlayerEntry(info));
		}
	}
	
	public static abstract class Entry extends ContainerObjectSelectionList.Entry<Entry>
	{
		abstract void refreshEntry();
	}
	
	public class PlayerEntry extends Entry
	{
		private final PlayerInfo playerInfo;
		private final ImmutableList<Button> buttons;
		@NotNull
		private final Component label;
		
		PlayerEntry(PlayerInfo info)
		{
			this.playerInfo = info;
			label = Component.literal(info.getProfile().getName());
			
			
			buttons = ImmutableList.of(
					
					createButton(0, 0, 70, 20, Component.translatable("warpmod.teleport"), button ->
					{
						assert Minecraft.getInstance().player != null;
						Minecraft.getInstance().player.connection.sendCommand("warp %s".formatted(label.getString()));
						Minecraft.getInstance().setScreen(null);
					}),
					
					createButton(0, 0, 70, 20, Component.translatable("warpmod.track"), button ->
					{
						button.setFocused(false);
					})
			);
		}
		
		
		@Override
		public void render(@NotNull PoseStack poseStack, int x, int y, int uk, int widgetWidth, int widgetHeight, int mouseX, int mouseY, boolean isHovering, float partialTicks)
		{
			int parentWidth = PlayerListComponent.this.width;
			
			// Render Label
			poseStack.pushPose();
			Supplier<ResourceLocation> skinGetter = playerInfo::getSkinLocation;
			RenderSystem.setShaderTexture(0, skinGetter.get());
			PlayerFaceRenderer.draw(poseStack, 5, (int) (y + ((float) PlayerListComponent.this.minecraft.font.lineHeight / 2)), 24);
			poseStack.popPose();
			
			PlayerListComponent.this.minecraft.font.draw(poseStack, this.label, 24 + 10, (y + ((float) PlayerListComponent.this.minecraft.font.lineHeight / 2)), 0xFF_FF_FF);
			
			// Render Buttons
			int buttonPadding = 4;
			int buttonLastX = widgetWidth;
			for (int i = 0; i < buttons.size(); i++)
			{
				Button button = buttons.get(i);
				
				buttonLastX -= 70 + buttonPadding;
				button.setX(buttonLastX);
				
				button.setY(y);
				button.render(poseStack, mouseX, mouseY, partialTicks);
			}
		}
		
		@Override
		public List<? extends GuiEventListener> children()
		{
			return buttons;
		}
		
		@Override
		public List<? extends NarratableEntry> narratables()
		{
			return buttons;
		}
		
		
		@Override
		void refreshEntry()
		{
		
		}
	}
}
