package chase.minecraft.architectury.warpmod.client.gui.waypoint;

import chase.minecraft.architectury.warpmod.client.renderer.RenderUtils;
import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.WaypointIcons;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("all")
public class WaypointOverlay
{
	private int currentSize = WaypointIcons.SIZE;
	@NotNull
	private final Minecraft minecraft;
	private EntityRenderDispatcher renderManager;
	@NotNull
	private final Warp warp;
	private double distanceToWarp = 0d;
	private int color = 0xFF_FF_FF;
	private float r, g, b;
	
	public WaypointOverlay(@NotNull Warp warp)
	{
		this.minecraft = Minecraft.getInstance();
		this.warp = warp;
		renderManager = minecraft.getEntityRenderDispatcher();
	}
	
	
	/**
	 * This function renders a waypoint icon and label on the screen if it is visible.
	 *
	 * @param poseStack A matrix stack used for rendering transformations and positioning of the waypoint icon and label.
	 */
	public void render(GuiGraphics graphics)
	{
		PoseStack poseStack = graphics.pose();
		
		Vec3 screenSpace = RenderUtils.worldSpaceToScreenSpace(warp.getPosition());
		ResourceLocation warpDim = warp.getDimension();
		ResourceLocation playerDim = minecraft.player.level().dimension().location();
		boolean isVisible = warpDim.equals(playerDim) && RenderUtils.screenSpaceCoordinateIsVisible(screenSpace);
		distanceToWarp = minecraft.getEntityRenderDispatcher().camera.getPosition().distanceTo(new Vec3(warp.getX(), warp.getY(), warp.getZ()));
		
		color = warp.getColor().getColor();
		if (isVisible)
		{
			int x = (int) (screenSpace.x - (currentSize / 2));
			int y = (int) (screenSpace.y - ((currentSize / 2) + minecraft.font.lineHeight + 20));
			
			poseStack.pushPose();
			r = FastColor.ARGB32.red(color) / 255f;
			g = FastColor.ARGB32.green(color) / 255f;
			b = FastColor.ARGB32.blue(color) / 255f;
			RenderSystem.disableDepthTest();
			RenderSystem.depthMask(false);
			RenderSystem.setShaderColor(r, g, b, 1f);
			if (warp.getIcon() == null) RenderSystem.setShaderTexture(0, WaypointIcons.DEFAULT);
			else RenderSystem.setShaderTexture(0, warp.getIcon());
			
			renderIcon(graphics, x, y);
			if (RenderUtils.isLookingAt(warp.getPosition()))
				renderLabel(graphics, x, y);
			
			RenderSystem.depthMask(true);
			RenderSystem.enableDepthTest();
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			poseStack.popPose();
		}
	}
	
	/**
	 * This function renders an icon at a specified position using a texture.
	 *
	 * @param poseStack The PoseStack is a matrix stack used for rendering transformations in Minecraft. It is used to store and apply transformations such as translation, rotation, and scaling to the rendered objects.
	 * @param x         The x-coordinate of the top-left corner of the area where the texture will be rendered.
	 * @param y         The y parameter is the vertical position where the icon will be rendered on the screen.
	 */
	private void renderIcon(GuiGraphics graphics, int x, int y)
	{
		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();
		calcScale();
		graphics.blit(warp.getIcon(), x + ((WaypointIcons.SIZE - currentSize) / 2), y + (WaypointIcons.SIZE - currentSize), 0, 0, currentSize, currentSize, currentSize, currentSize);
		poseStack.popPose();
	}
	
	
	/**
	 * This function renders a label with a background and centered text.
	 *
	 * @param graphics The PoseStack is a matrix stack used for rendering transformations in Minecraft. It is used to store and apply transformations such as translation, rotation, and scaling to the rendered objects.
	 * @param x        The x-coordinate of the position where the label will be rendered on the screen.
	 * @param y        The y parameter is the vertical coordinate of where the label will be rendered on the screen.
	 */
	private void renderLabel(GuiGraphics graphics, int x, int y)
	{
		String text = getDisplayName();
		renderLabelBackground(graphics, x, y, minecraft.font.width(text));
		
		// Draws Text
		graphics.drawCenteredString(minecraft.font, text, x + WaypointIcons.SIZE / 2, y + WaypointIcons.SIZE + 5, color);
	}
	
	/**
	 * This function renders a background for a label with a given position and text width.
	 *
	 * @param graphics  A PoseStack is a matrix stack used for rendering transformations in Minecraft. It is used to store and apply transformations such as translation, rotation, and scaling to the rendered objects.
	 * @param x         The x-coordinate of the label's center point.
	 * @param y         The y-coordinate of the top of the label background.
	 * @param textWidth The width of the text that will be rendered on the label background.
	 */
	private void renderLabelBackground(GuiGraphics graphics, int x, int y, int textWidth)
	{
		// Draw Background
		final int padding = 2;
		RenderSystem.setShaderColor(0f, 0f, 0f, .5f);
		int startX = x - (padding + (textWidth / 2)) + (WaypointIcons.SIZE / 2);
		int endX = (int) (startX + (padding * 2) + textWidth);
		graphics.fill(startX, y + WaypointIcons.SIZE, endX, y + WaypointIcons.SIZE + minecraft.font.lineHeight + 10, -0x00_00_00_FF);
		RenderSystem.setShaderColor(r, g, b, 1f);
	}
	
	/**
	 * This function calculates the scale of an object based on its distance to a warp point.
	 *
	 * @return The method `calcScale()` returns a `float` value which represents the calculated scale based on the distance to a warp and a maximum render distance.
	 */
	private void calcScale()
	{
		int maxDistance = 100;
		int minSize = 16;
		int maxSize = WaypointIcons.SIZE;
		if (distanceToWarp > maxDistance) currentSize = minSize;
		else
		{
			currentSize = (int) (maxSize - ((maxSize - minSize) * (distanceToWarp / maxDistance)));
		}
	}
	
	/**
	 * This function returns a formatted string with the name of a warp and its distance, with a maximum length of 10 characters for the name.
	 *
	 * @return A formatted string that includes the name of a warp and its distance in meters, with a maximum length of 10 characters for the name and an ellipsis added if the name is longer than that.
	 */
	private String getDisplayName()
	{
		final int maxTextLength = 10;
		String name = warp.getName();
		if (name.length() > maxTextLength + 3)
		{
			name = name.substring(0, maxTextLength);
			name += "...";
		}
		return "%s (%dM)".formatted(name, (int) distanceToWarp);
	}
	
	
}
