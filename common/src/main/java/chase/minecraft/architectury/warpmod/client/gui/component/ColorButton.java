package chase.minecraft.architectury.warpmod.client.gui.component;

import chase.minecraft.architectury.warpmod.client.gui.waypoint.WaypointColor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ColorButton extends AbstractButton
{
	private int currentColorIndex;
	private WaypointColor color;
	private final Consumer<WaypointColor> onValueChange;
	
	public ColorButton(int x, int y, int width, int height, WaypointColor color, Consumer<WaypointColor> onValueChange)
	{
		super(x, y, width, height, Component.literal(color.getName()).withStyle(ChatFormatting.WHITE));
		this.color = color;
		this.currentColorIndex = 0;
		this.onValueChange = onValueChange;
		for (int i = 0; i < WaypointColor.values().length; i++)
		{
			if (color == WaypointColor.values()[i])
			{
				currentColorIndex = i;
				break;
			}
		}
	}
	
	@Override
	public void onPress()
	{
	}
	
	@Override
	public boolean mouseClicked(double x, double y, int mouseButton)
	{
		if (this.active && this.visible && this.clicked(x, y))
		{
			if (mouseButton == 0)
			{
				this.playDownSound(Minecraft.getInstance().getSoundManager());
				color = getNextColor();
				onValueChange.accept(color);
				return true;
			} else if (mouseButton == 1)
			{
				this.playDownSound(Minecraft.getInstance().getSoundManager());
				color = getPreviousColor();
				onValueChange.accept(color);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		if (this.visible)
		{
			this.setMessage(Component.literal(color.getName()));
			this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
			this.renderWidget(graphics, mouseX, mouseY, partialTicks);
			if (isHovered)
			{
//				renderTooltip(graphics, mouseX, mouseY, partialTicks);
			}
		}
	}
	
	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		int color = this.color.getColor();
		float r = FastColor.ARGB32.red(color) / 255f;
		float g = FastColor.ARGB32.green(color) / 255f;
		float b = FastColor.ARGB32.blue(color) / 255f;
		float a = 1f;
		if (isHovered())
		{
			RenderSystem.setShaderColor(r, g, b, 1f);
		} else
		{
			RenderSystem.setShaderColor(r, g, b, .7f);
		}
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF_FF_FF_FF);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		int textColor = this.active ? 16777215 : 10526880;
		this.renderString(graphics, minecraft.font, textColor | Mth.ceil(this.alpha * 255.0F) << 24);
	}
	
	public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		int color = this.color.getColor();
		float r = FastColor.ARGB32.red(color) / 255f;
		float g = FastColor.ARGB32.green(color) / 255f;
		float b = FastColor.ARGB32.blue(color) / 255f;
		float a = 1f;
		
		RenderSystem.setShaderColor(r, g, b, 1f);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		graphics.fill(getX(), getY(), mouseX + width, mouseY + height, 0xFF_FF_FF_FF);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		int textColor = this.active ? 16777215 : 10526880;
		this.renderString(graphics, minecraft.font, textColor | Mth.ceil(this.alpha * 255.0F) << 24);
	}
	
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput)
	{
	}
	
	public WaypointColor getNextColor()
	{
		if (currentColorIndex == WaypointColor.values().length - 1)
		{
			currentColorIndex = 0;
		} else
		{
			currentColorIndex++;
		}
		return WaypointColor.values()[currentColorIndex];
	}
	
	public WaypointColor getPreviousColor()
	{
		if (currentColorIndex == 0)
		{
			currentColorIndex = WaypointColor.values().length - 1;
		} else
		{
			currentColorIndex--;
		}
		return WaypointColor.values()[currentColorIndex];
	}
}
