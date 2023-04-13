package chase.minecraft.architectury.warpmod.client.gui.component;

import chase.minecraft.architectury.warpmod.client.WarpModClient;
import chase.minecraft.architectury.warpmod.client.gui.EditWarpScreen;
import chase.minecraft.architectury.warpmod.client.gui.WarpListScreen;
import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.Warps;
import chase.minecraft.architectury.warpmod.networking.WarpNetworking;
import com.google.common.collect.ImmutableList;
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.List;

import static chase.minecraft.architectury.warpmod.client.gui.GUIFactory.createButton;

@Environment(EnvType.CLIENT)
public class WarpListComponent extends ContainerObjectSelectionList<WarpListComponent.Entry>
{
	private final WarpListScreen _parent;
	
	public WarpListComponent(WarpListScreen parent)
	{
		super(Minecraft.getInstance(), (int) (parent.width), parent.height + 15, 30, parent.height - 32, 30);
		_parent = parent;
		Player player = this.minecraft.player;
		if (PacketSender.c2s().canSend(WarpNetworking.LIST))
		{
			PacketSender.c2s().send(WarpNetworking.LIST, new FriendlyByteBuf(Unpooled.buffer()));
		}
		for (Warp warp : Warps.fromPlayer(player).getWarps())
		{
			addEntry(new WarpEntry(warp));
		}
		
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
		clearEntries();
		for (Warp warp : Warps.fromPlayer(minecraft.player).getWarps())
		{
			addEntry(new WarpEntry(warp));
		}
		children().forEach(Entry::refreshEntry);
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
							
							FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
							buf.writeInt(warp.getName().length());
							buf.writeCharSequence(warp.getName(), Charset.defaultCharset());
							PacketSender.c2s().send(WarpNetworking.TELEPORT, buf);
							minecraft.setScreen(null);
						} else
						{
							assert Minecraft.getInstance().player != null;
							Minecraft.getInstance().player.connection.sendCommand("execute");
						}
					}),
					
					createButton(0, 0, 50, 20, Component.translatable("warpmod.remove"), button ->
					{
						FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
						buf.writeInt(warp.getName().length());
						buf.writeCharSequence(warp.getName(), Charset.defaultCharset());
						PacketSender.c2s().send(WarpNetworking.REMOVE, buf);
						
						if (PacketSender.c2s().canSend(WarpNetworking.LIST))
						{
							PacketSender.c2s().send(WarpNetworking.LIST, new FriendlyByteBuf(Unpooled.buffer()));
						}
					}),
					createButton(0, 0, 50, 20, Component.translatable("warpmod.edit"), button ->
					{
						minecraft.setScreen(new EditWarpScreen(WarpListComponent.this._parent, this.warp));
					}));
			
			
			refreshEntry();
			
		}
		
		@Override
		public void render(@NotNull PoseStack matrixStack, int x, int y, int uk, int widgetWidth, int widgetHeight, int mouseX, int mouseY, boolean isHovering, float partialTicks)
		{
			int parentWidth = WarpListComponent.this.width;
			
			// Render Label
			WarpListComponent.this.minecraft.font.draw(matrixStack, this.name, 20, (y + ((float) WarpListComponent.this.minecraft.font.lineHeight / 2)), 0xFF_FF_FF);
			
			// Render Buttons
			int buttonPadding = 4;
			int buttonLastX = widgetWidth;
			buttons.get(0).active = !WarpModClient.isOP;
			for (int i = 0; i < buttons.size(); i++)
			{
				Button button = buttons.get(i);
				
				buttonLastX -= 50 + buttonPadding;
				button.setX(buttonLastX);
				
				button.setY(y);
				button.render(matrixStack, mouseX, mouseY, partialTicks);
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
