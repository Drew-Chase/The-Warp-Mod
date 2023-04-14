package chase.minecraft.architectury.warpmod.client.gui.screen;

import chase.minecraft.architectury.warpmod.client.gui.component.PlayerListComponent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import static chase.minecraft.architectury.warpmod.client.gui.GUIFactory.createButton;

@SuppressWarnings("all")
public class PlayerListScreen extends Screen
{
	@Nullable
	private final Screen parent;
	private PlayerListComponent playerListComponent;
	
	public PlayerListScreen(@Nullable Screen parent)
	{
		super(Component.translatable("gui.warpmod.list.players.title"));
		this.parent = parent;
	}
	
	@Override
	protected void init()
	{
		playerListComponent = addWidget(new PlayerListComponent(this));
		addRenderableWidget(createButton((width / 2) - 110, height - 25, 100, 20, Component.translatable("gui.warpmod.list.title"), w ->
		{
			minecraft.setScreen(new WarpListScreen(this));
		}));
		addRenderableWidget(createButton((width / 2) + 10, height - 25, 100, 20, CommonComponents.GUI_DONE, w ->
		{
			minecraft.setScreen(parent);
		}));
	}
	
	@Override
	public void render(PoseStack poseStack, int x, int y, float partialTicks)
	{
		renderBackground(poseStack);
		playerListComponent.render(poseStack, x, y, partialTicks);
		drawCenteredString(poseStack, font, title.getString(), (width / 2) - (font.width(title.getString()) / 2), font.lineHeight, ChatFormatting.WHITE.getColor());
		super.render(poseStack, x, y, partialTicks);
	}
}
