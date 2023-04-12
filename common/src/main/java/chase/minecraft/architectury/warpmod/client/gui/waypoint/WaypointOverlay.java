package chase.minecraft.architectury.warpmod.client.gui.waypoint;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.client.ClientWarps;
import chase.minecraft.architectury.warpmod.client.renderer.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

@SuppressWarnings("all")
public class WaypointOverlay extends GuiComponent
{
	
	public static final ResourceLocation WAYPOINT_TEXTURE = WarpMod.id("textures/gui/waypoint.png");
	public static final int TEXTURE_SIZE = 32;
	@NotNull
	private final Minecraft minecraft;
	private EntityRenderDispatcher renderManager;
	@NotNull
	private final ClientWarps.ClientWarp warp;
	
	public WaypointOverlay(@NotNull ClientWarps.ClientWarp warp)
	{
		this.minecraft = Minecraft.getInstance();
		this.warp = warp;
		renderManager = minecraft.getEntityRenderDispatcher();
	}
	
	
	public void render(PoseStack matrixStack)
	{
		Vector3d warpPos = new Vector3d(warp.x(), warp.y(), warp.z());
		Vector3d screenSpace = RenderUtils.worldSpaceToScreenSpace(warpPos);
		boolean isVisible = RenderUtils.screenSpaceCoordinateIsVisible(screenSpace);
		int x = (int) screenSpace.x;
		int y = (int) screenSpace.y;
		int dist = (int) minecraft.getEntityRenderDispatcher().camera.getPosition().distanceTo(new Vec3(warp.x(), warp.y(), warp.z()));
		
		String text = "%s (%dM)".formatted(warp.name(), dist);
		int textWidth = minecraft.font.width(text);
		
		int color = 0xFF00FF;
		float r = FastColor.ARGB32.red(color) / 255f;
		float g = FastColor.ARGB32.green(color) / 255f;
		float b = FastColor.ARGB32.blue(color) / 255f;
		float a = FastColor.ARGB32.alpha(color) / 255f;
		if (isVisible)
		{
			RenderSystem.disableDepthTest();
			RenderSystem.depthMask(false);
			RenderSystem.setShaderColor(r, g, b, 1f);
			RenderSystem.setShaderTexture(0, WAYPOINT_TEXTURE);
			x -= (TEXTURE_SIZE / 2);
			y -= ((TEXTURE_SIZE / 2) + minecraft.font.lineHeight + 20);
			blit(matrixStack, x, y, 0, 0, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE); // This draws the waypoint texture
			
			RenderSystem.setShaderColor(0f, 0f, 0f, .5f);
			int startX = x - (textWidth - (textWidth / 2));
			int endX = startX + textWidth + (textWidth / 2);
			fill(matrixStack, startX, y + 32, endX, y + 32 + minecraft.font.lineHeight + 10, -0x00_00_00_FF);
			RenderSystem.setShaderColor(r, g, b, 1f);
			drawCenteredString(matrixStack, minecraft.font, text, x + 16, y + 32 + 5, color);
			
			RenderSystem.depthMask(true);
			RenderSystem.enableDepthTest();
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}
	}
	
	
}
