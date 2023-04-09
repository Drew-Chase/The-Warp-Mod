package chase.minecraft.architectury.warpmod.client.gui.component;

import chase.minecraft.architectury.warpmod.client.ClientWarps;
import chase.minecraft.architectury.warpmod.client.gui.EditWarpScreen;
import chase.minecraft.architectury.warpmod.client.gui.WarpScreen;
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
public class WarpList extends ContainerObjectSelectionList<WarpList.Entry>
{
	private final WarpScreen _parent;
	
	public WarpList(WarpScreen parent)
	{
		super(Minecraft.getInstance(), (int) (parent.width), parent.height + 15, 30, parent.height - 32, 30);
		_parent = parent;
		Player player = this.minecraft.player;
		if (PacketSender.c2s().canSend(WarpNetworking.LIST))
		{
			PacketSender.c2s().send(WarpNetworking.LIST, new FriendlyByteBuf(Unpooled.buffer()));
		}
		for (ClientWarps.ClientWarp warp : ClientWarps.Instance.getWarps())
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
		for (ClientWarps.ClientWarp warp : ClientWarps.Instance.getWarps())
		{
			addEntry(new WarpEntry(warp));
		}
		children().forEach(Entry::refreshEntry);
	}
	
	@Environment(EnvType.CLIENT)
	public class WarpEntry extends Entry
	{
		private final ImmutableList<Button> _buttons;
		private final Component _name;
		private final ClientWarps.ClientWarp _warp;
		
		WarpEntry(ClientWarps.ClientWarp warp)
		{
			_warp = warp;
			_name = Component.literal(warp.name());
			
			_buttons = ImmutableList.of(
					
					createButton(0, 0, 70, 20, Component.translatable("warpmod.teleport"), button ->
					{
						FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
						buf.writeInt(warp.name().length());
						buf.writeCharSequence(warp.name(), Charset.defaultCharset());
						PacketSender.c2s().send(WarpNetworking.TELEPORT, buf);
						minecraft.setScreen(null);
					}),
					
					createButton(0, 0, 50, 20, Component.translatable("warpmod.remove"), button ->
					{
						FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
						buf.writeInt(warp.name().length());
						buf.writeCharSequence(warp.name(), Charset.defaultCharset());
						PacketSender.c2s().send(WarpNetworking.REMOVE, buf);
						
						if (PacketSender.c2s().canSend(WarpNetworking.LIST))
						{
							PacketSender.c2s().send(WarpNetworking.LIST, new FriendlyByteBuf(Unpooled.buffer()));
						}
					}),
					createButton(0, 0, 50, 20, Component.translatable("warpmod.edit"), button ->
					{
						minecraft.setScreen(new EditWarpScreen(WarpList.this._parent, _warp));
					}));
			
			
			refreshEntry();
			
		}
		
		@Override
		public void render(@NotNull PoseStack matrixStack, int x, int y, int uk, int widgetWidth, int widgetHeight, int mouseX, int mouseY, boolean isHovering, float partialTicks)
		{
			int parentWidth = WarpList.this.width;
			
			// Render Label
			WarpList.this.minecraft.font.draw(matrixStack, this._name, 20, (y + ((float) WarpList.this.minecraft.font.lineHeight / 2)), 0xFF_FF_FF);
			
			// Render Buttons
			int buttonPadding = 4;
			int buttonLastX = widgetWidth;
			for (int i = 0; i < _buttons.size(); i++)
			{
				Button button = _buttons.get(i);
				
				buttonLastX -= 50 + buttonPadding;
				button.setX(buttonLastX);
				
				button.setY(y);
				button.render(matrixStack, mouseX, mouseY, partialTicks);
			}
		}
		
		@Override
		public List<? extends GuiEventListener> children()
		{
			return _buttons;
			
		}
		
		@Override
		public List<? extends NarratableEntry> narratables()
		{
			return _buttons;
		}
		
		@Override
		void refreshEntry()
		{
		
		}
	}
	
	@Environment(EnvType.CLIENT)
	public static abstract class Entry extends ContainerObjectSelectionList.Entry<Entry>
	{
		abstract void refreshEntry();
	}
}
