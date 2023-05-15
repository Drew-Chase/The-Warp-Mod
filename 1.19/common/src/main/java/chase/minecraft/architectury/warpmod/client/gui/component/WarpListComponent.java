package chase.minecraft.architectury.warpmod.client.gui.component;

import chase.minecraft.architectury.warpmod.client.WarpModClient;
import chase.minecraft.architectury.warpmod.client.gui.screen.EditWarpScreen;
import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.WarpManager;
import chase.minecraft.architectury.warpmod.networking.WarpNetworking;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.PacketSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.List;

import static chase.minecraft.architectury.warpmod.client.gui.GUIFactory.createButton;

@Environment(EnvType.CLIENT)
public class WarpListComponent extends ContainerObjectSelectionList<WarpListComponent.Entry>
{
	private final Screen parent;
	@Nullable
	private String filter;
	
	public WarpListComponent(Screen parent, @Nullable String filter)
	{
		super(Minecraft.getInstance(), parent.width, parent.height + 15, 30, parent.height - 32, 30);
		this.parent = parent;
		Player player = this.minecraft.player;
		if (PacketSender.c2s().canSend(WarpNetworking.LIST))
		{
			PacketSender.c2s().send(WarpNetworking.LIST, new FriendlyByteBuf(Unpooled.buffer()));
		}
		refreshEntries();
		this.filter = filter;
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
		WarpManager.loadClient();
		clearEntries();
		for (Warp warp : WarpManager.fromPlayer(minecraft.player).getWarps())
		{
			if (filter != null && !filter.isEmpty())
			{
				if (warp.getDimension().toString().equals(filter))
				{
					addEntry(new WarpEntry(warp));
				}
			} else
			{
				addEntry(new WarpEntry(warp));
			}
		}
	}
	
	public void filter(@Nullable String filter)
	{
		this.filter = filter;
		refreshEntries();
	}
	
	
	@Environment(EnvType.CLIENT)
	public static abstract class Entry extends ContainerObjectSelectionList.Entry<Entry>
	{
		abstract void refreshEntry();
	}
	
	@Environment(EnvType.CLIENT)
	public class WarpEntry extends Entry
	{
		private final ImmutableList<Button> buttons;
		private final Component name;
		private final Warp warp;
		
		WarpEntry(Warp warp)
		{
			this.warp = warp;
			name = Component.literal(warp.getName());
			
			buttons = ImmutableList.of(
					
					createButton(0, 0, 70, 20, Component.translatable("warpmod.teleport"), button ->
					{
						if (WarpModClient.onServer)
						{
							warp.teleport(null);
						} else
						{
							assert Minecraft.getInstance().player != null;
							Minecraft.getInstance().player.connection.sendCommand("execute in %s run tp @s %f %f %f %f %f".formatted(warp.getDimension().toString(), warp.getX(), warp.getY(), warp.getZ(), warp.getPitch(), warp.getYaw()));
							WarpManager.fromPlayer(Minecraft.getInstance().player).createBack();
						}
						Minecraft.getInstance().setScreen(null);
					}),
					
					createButton(0, 0, 50, 20, Component.translatable("warpmod.remove"), button ->
					{
						if (WarpModClient.onServer)
						{
							
							FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
							buf.writeInt(warp.getName().length());
							buf.writeCharSequence(warp.getName(), Charset.defaultCharset());
							PacketSender.c2s().send(WarpNetworking.REMOVE, buf);
							
							if (PacketSender.c2s().canSend(WarpNetworking.LIST))
							{
								PacketSender.c2s().send(WarpNetworking.LIST, new FriendlyByteBuf(Unpooled.buffer()));
							}
						} else
						{
							WarpManager.fromPlayer(minecraft.player).remove(warp.getName());
							refreshEntries();
						}
					}), createButton(0, 0, 50, 20, Component.translatable("warpmod.edit"), button ->
					{
						minecraft.setScreen(new EditWarpScreen(WarpListComponent.this.parent, this.warp));
					}));
			
			
			refreshEntry();
			
		}
		
		@Override
		public void render(@NotNull PoseStack poseStack, int x, int y, int uk, int widgetWidth, int widgetHeight, int mouseX, int mouseY, boolean isHovering, float partialTicks)
		{
			int parentWidth = WarpListComponent.this.width;
			
			if (isHovering)
			{
				RenderSystem.setShaderColor(0f, 0f, 0f, .5f);
				fill(poseStack, x, y, x + parentWidth, y + widgetHeight, 0xFF_FF_FF_FF);
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			}
			
			// Render Label
			WarpListComponent.this.minecraft.font.draw(poseStack, this.name, 20, y + minecraft.font.lineHeight, 0xFF_FF_FF);
			
			// Render Buttons
			int buttonPadding = 4;
			int buttonLastX = widgetWidth;
			if (!minecraft.isSingleplayer()) buttons.get(0).active = !WarpModClient.isOP;
			
			for (int i = 0; i < buttons.size(); i++)
			{
				Button button = buttons.get(i);
				
				buttonLastX -= 50 + buttonPadding;
				button.setX(buttonLastX);
				
				button.setY(y + (widgetHeight / 2) - (button.getHeight() / 2));
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
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
		{
			
			return super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}
}
