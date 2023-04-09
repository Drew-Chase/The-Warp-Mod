package chase.minecraft.architectury.warpmod.client.gui.waypoint;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.client.ClientWarps;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

public class WaypointOverlay extends GuiComponent
{
	
	@NotNull
	private final Minecraft _minecraft;
	@NotNull
	private final ClientWarps.ClientWarp _warp;
	public static final ResourceLocation WAYPOINT_TEXTURE = WarpMod.id("textures/gui/waypoint.png");
	public static final int TEXTURE_SIZE = 32;
	
	public WaypointOverlay(@NotNull Minecraft minecraft, @NotNull ClientWarps.ClientWarp warp)
	{
		_minecraft = minecraft;
		_warp = warp;
	}
	
	public void render(PoseStack matrixStack, float partialTicks)
	{
		Vector2f xy = calc(partialTicks);
		WarpMod.log.info("X: %f, Y: %f".formatted(xy.x, xy.y));
		int x = (int) xy.x;
		int y = (int) xy.y;
		
		int dist = (int) new Vec3(_minecraft.player.getX(), _minecraft.player.getY(), _minecraft.player.getZ()).distanceTo(new Vec3(_warp.x(), _warp.y(), _warp.z()));
		
		String text = "%s (%dM)".formatted(_warp.name(), dist);
		int textWidth = _minecraft.font.width(text);
		
		int color = 0xFF00FF;
		float r = FastColor.ARGB32.red(color) / 255f;
		float g = FastColor.ARGB32.green(color) / 255f;
		float b = FastColor.ARGB32.blue(color) / 255f;
		float a = FastColor.ARGB32.alpha(color) / 255f;
		
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.setShaderColor(r, g, b, 1f);
		RenderSystem.setShaderTexture(0, WAYPOINT_TEXTURE);
		blit(matrixStack, x, y, 0, 0, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE); // This draws the waypoint texture
		
		RenderSystem.setShaderColor(0f, 0f, 0f, .5f);
		int startX = x - (textWidth - (textWidth / 2));
		int endX = startX + textWidth + (textWidth / 2);
		fill(matrixStack, startX, y + 32, endX, y + 32 + _minecraft.font.lineHeight + 10, -0x00_00_00_FF);
		RenderSystem.setShaderColor(r, g, b, 1f);
		drawCenteredString(matrixStack, _minecraft.font, text, x + 16, y + 32 + 5, color);
		
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}
	
	private Vector2f calc(float partialTicks)
	{
		// Getting the gui scale adjusted width and height
		Window window = _minecraft.getWindow();
		int width = window.getGuiScaledWidth();
		int height = window.getGuiScaledHeight();
		
		// Calculating the center of the screen
		double screenCenterX = width / 2.0;
		double screenCenterY = height / 2.0;
		
		// Getting the player from minecraft
		LocalPlayer player = _minecraft.player;
		assert player != null;
		
		// The players looking angle
		float pitch = player.getXRot();
		float yaw = player.getYRot();
		
		// The players field of view
		double fov = Math.toRadians(player.getFieldOfViewModifier() * 70f);
		float pitchRadians = (float) Math.toRadians(pitch);
		float yawRadians = (float) Math.toRadians(yaw);
		double verticalPixelPerRad = screenCenterY / fov;
		double horizontalPixelPerRad = screenCenterX / fov;
		
		
		// The players position
		double px = player.getX();
		double py = player.getY();
		double pz = player.getZ();
		
		// The warps position
		double wx = _warp.x();
		double wy = _warp.y();
		double wz = _warp.z();
		
		// Getting the delta between the players position and the position of the warp
		double dx = wx - px;
		double dy = wy - py;
		double dz = wz - pz;

	// Calculate the distance between the player and the warp
		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		
		// TODO: Calculate the screen space position of the warp point relative to the players position and rotation
		
		return new Vector2f(0,0);
		
		
	}
	
	
}
