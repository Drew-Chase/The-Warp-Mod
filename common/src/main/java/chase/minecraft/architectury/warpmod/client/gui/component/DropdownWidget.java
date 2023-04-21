package chase.minecraft.architectury.warpmod.client.gui.component;

import chase.minecraft.architectury.warpmod.utils.MathUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.function.Consumer;

public class DropdownWidget<T> extends AbstractButton
{
	private final Consumer<T> onValueChange;
	private final HashMap<T, String> items;
	private T selectedItem;
	private final T initialValue;
	@Nullable
	private T hoveredItem;
	
	public DropdownWidget(int x, int y, int width, int height, T initialValue, HashMap<T, String> items, Consumer<T> onValueChange)
	{
		super(x, y, width, height, Component.empty());
		this.onValueChange = onValueChange;
		this.items = items;
		this.initialValue = selectedItem = initialValue;
		hoveredItem = null;
		
		
	}
	
	public DropdownWidget(int x, int y, int width, int height, T initialValue, T[] items, Consumer<T> onValueChange)
	{
		super(x, y, width, height, Component.empty());
		HashMap<T, String> elements = new HashMap<>();
		for (T item : items)
		{
			elements.put(item, item.toString());
		}
		this.onValueChange = onValueChange;
		this.items = elements;
		this.initialValue = selectedItem = initialValue;
		hoveredItem = null;
	}
	
	@Override
	public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		if (this.visible)
		{
			this.setMessage(Component.literal(items.get(selectedItem)));
			this.isHovered = MathUtils.isWithin2DBounds(new Vector2f(mouseX, mouseY), new Vector4f(getX(), getY(), width + getX(), height + getY())) || hoveredItem != null;
			this.renderWidget(poseStack, mouseX, mouseY, partialTicks);
			if (isFocused())
			{
				renderDropdownItems(poseStack, mouseX, mouseY);
			}
		}
		
	}
	
	
	@Override
	public void renderWidget(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if (isHovered())
		{
			RenderSystem.setShaderColor(.1f, .1f, .1f, 1f);
		} else
		{
			RenderSystem.setShaderColor(.08f, .08f, .08f, 1f);
		}
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		fill(poseStack, getX(), getY(), getX() + width, getY() + height, 0xFF_FF_FF_FF);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		int textColor = this.active ? 16777215 : 10526880;
		this.renderString(poseStack, minecraft.font, textColor | Mth.ceil(this.alpha * 255.0F) << 24);
	}
	
	public void renderDropdownItems(@NotNull PoseStack poseStack, int mouseX, int mouseY)
	{
		int itemHeight = 15;
		Font font = Minecraft.getInstance().font;
		int y = getY() + getHeight() + (font.lineHeight / 2);
		int maxHeight = y + (itemHeight * (items.size() - 1)) + (font.lineHeight / 2);
		RenderSystem.setShaderColor(.1f, .1f, .1f, 1f);
		fill(poseStack, getX(), getY() + height, getX() + width, maxHeight, 0xFF_FF_FF_FF);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		boolean subitemHover = false;
		for (int i = 0; i < items.size(); i++)
		{
			T item = items.keySet().stream().toList().get(i);
			if (item.equals(selectedItem))
				continue;
			
			Vector4f bounds = new Vector4f(new Vector4f(getX(), y, width + getX(), y + itemHeight));
			if (MathUtils.isWithin2DBounds(new Vector2f(mouseX, mouseY), bounds))
			{
				RenderSystem.setShaderColor(.3f, .3f, .3f, 1f);
				fill(poseStack, (int) bounds.x, (int) bounds.y, (int) bounds.z, (int) bounds.w, 0xFF_FF_FF_FF);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				hoveredItem = item;
				subitemHover = true;
			}
			if (!subitemHover)
				hoveredItem = null;
			String displayName = items.get(item);
			AbstractWidget.drawString(poseStack, font, displayName, getX() + (width / 2) - (font.width(displayName) / 2), y + (itemHeight / 2) - (font.lineHeight / 2), 0xFF_FF_FF);
			y += itemHeight + 1;
		}
	}
	
	@Override
	public void onPress()
	{
	
	}
	
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput)
	{
	
	}
	
	@Override
	public boolean mouseScrolled(double d, double e, double f)
	{
		return super.mouseScrolled(d, e, f);
	}
	
	@Override
	public boolean mouseClicked(double x, double y, int mouseButton)
	{
		if (isHovered)
		{
			if (mouseButton == 0)
			{
				if (hoveredItem != null)
				{
					selectedItem = hoveredItem;
					hoveredItem = null;
					this.setMessage(Component.literal(items.get(selectedItem)));
					onValueChange.accept(selectedItem);
					setFocused(false);
				} else
				{
					setFocused(!isFocused());
				}
			} else if (mouseButton == 1)
			{
				setFocused(false);
				reset();
			} else
			{
				setFocused(false);
			}
			playDownSound(Minecraft.getInstance().getSoundManager());
		} else
		{
			setFocused(false);
		}
		return false;
	}
	
	public void reset()
	{
		selectedItem = initialValue;
	}
	
	
}
