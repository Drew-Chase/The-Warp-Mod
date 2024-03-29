package chase.minecraft.architectury.warpmod.client.gui.screen;

import chase.minecraft.architectury.warpmod.client.WarpModClient;
import chase.minecraft.architectury.warpmod.client.gui.GUIFactory;
import chase.minecraft.architectury.warpmod.client.gui.component.WarpListComponent;
import chase.minecraft.architectury.warpmod.networking.WarpNetworking;
import chase.minecraft.architectury.warpmod.utils.WorldUtils;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;

import static chase.minecraft.architectury.warpmod.client.gui.GUIFactory.createButton;

@SuppressWarnings("all")
/**
 * The WarpListScreen class is a GUI screen with a list of warps and a "done" button that allows the player to navigate back to the parent screen.
 */
public class WarpListScreen extends Screen
{
	
	@Nullable
	private final Screen _parent;
	private WarpListComponent warpListComponent;
	
	public WarpListScreen(@Nullable Screen parent)
	{
		super(Component.translatable("gui.warpmod.list.title"));
		_parent = parent;
	}
	
	/**
	 * This function initializes a screen with a list and a "done" button.
	 */
	@Override
	protected void init()
	{
		PacketSender.c2s().send(WarpNetworking.PING, new FriendlyByteBuf(Unpooled.buffer()));
		String filter = WorldUtils.getDimensionName(Minecraft.getInstance().player.level().dimension().location());
		warpListComponent = addWidget(new WarpListComponent(this, filter));
		addRenderableWidget(createButton((width / 2) - 110, height - 25, 100, 20, Component.translatable("warpmod.create"), w ->
		{
			minecraft.setScreen(new EditWarpScreen(this));
		}));
		addRenderableWidget(createButton((width / 2) + 10, height - 25, 100, 20, CommonComponents.GUI_DONE, w ->
		{
			minecraft.setScreen(_parent);
		}));
		LinkedHashMap<String, String> dims = new LinkedHashMap<>();
		dims.put("all", "ALL");
		WarpModClient.dimensions.forEach(dim -> dims.put(dim, WorldUtils.getDimensionName(new ResourceLocation(dim))));
		addRenderableWidget(GUIFactory.createCycleButton(Component.empty(), width - 105, 5, 100, 20, filter, dims.values().toArray(new String[0]), Component.literal("Dimension filter"), value ->
		{
			warpListComponent.filter(value);
		}, update ->
		{
			return Component.literal(update);
		}));
	}
	
	/**
	 * This function renders a GUI element with a centered title and a background.
	 *
	 * @param graphics     A matrix stack used for rendering transformations and positioning of elements on the screen.
	 * @param x            The x-coordinate of the top-left corner of the GUI screen.
	 * @param y            The y parameter in this method represents the vertical position of the top-left corner of the GUI element being rendered.
	 * @param partialTicks partialTicks is a float value that represents the amount of time that has passed since the last frame was rendered. It is used to calculate smooth animations and movements in the game. The value is usually between 0 and 1, where 0 means no time has passed and 1 means a full
	 */
	@Override
	public void render(@NotNull GuiGraphics graphics, int x, int y, float partialTicks)
	{
		renderBackground(graphics);
		warpListComponent.render(graphics, x, y, partialTicks);
		graphics.drawCenteredString(font, title.getString(), (width / 2) - (font.width(title.getString()) / 2), font.lineHeight, 0xff_ff_ff);
		super.render(graphics, x, y, partialTicks);
	}
	
	public void refresh()
	{
		if (warpListComponent != null)
			warpListComponent.refreshEntries();
	}
	
}
