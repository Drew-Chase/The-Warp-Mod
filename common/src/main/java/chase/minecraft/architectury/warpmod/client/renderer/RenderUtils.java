package chase.minecraft.architectury.warpmod.client.renderer;

import chase.minecraft.architectury.warpmod.utils.MathUtils;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class RenderUtils
{
	private static final Minecraft client = Minecraft.getInstance();
	public static final Matrix4f lastProjMat = new Matrix4f();
	public static final Matrix4f lastModMat = new Matrix4f();
	public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
	
	public static Vec3 worldSpaceToScreenSpace(Vec3 pos)
	{
		LocalPlayer player = Minecraft.getInstance().player;
		assert player != null;
		Vec3 camera = client.getEntityRenderDispatcher().camera.getPosition();
		int displayHeight = client.getWindow().getHeight();
		int[] viewport = new int[4];
		GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
		Vector3f target = new Vector3f();
		int renderDistance = Minecraft.getInstance().options.renderDistance().get() * 16;
		
		double dx = pos.x - camera.x;
		double dy = pos.y - camera.y;
		double dz = pos.z - camera.z;
		
		double distance = camera.distanceTo(new Vec3(pos.x, pos.y, pos.z));
		if (renderDistance < distance)
		{
			// Calculate direction vector from player to waypoint
			double directionX = dx / distance;
			double directionY = dy / distance;
			double directionZ = dz / distance;
			
			// Multiply unit direction by player render distance
			dx = directionX * renderDistance;
			dy = directionY * renderDistance;
			dz = directionZ * renderDistance;
		}
		
		
		Vector4f transformedCoordinates = new Vector4f((float) dx, (float) dy, (float) dz, 1.f).mul(lastWorldSpaceMatrix);
		
		Matrix4f matrixProj = new Matrix4f(lastProjMat);
		Matrix4f matrixModel = new Matrix4f(lastModMat);
		matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
		
		return new Vec3(target.x / client.getWindow().getGuiScale(), (displayHeight - target.y) / client.getWindow().getGuiScale(), target.z);
	}
	
	public static boolean isLookingAt(Vec3 pos)
	{
		Vec3 screenSpace = worldSpaceToScreenSpace(pos);
		if (!screenSpaceCoordinateIsVisible(screenSpace))
		{
			return false;
		}
		Window window = Minecraft.getInstance().getWindow();
		int centerX = window.getGuiScaledWidth() / 2;
		int centerY = window.getGuiScaledHeight() / 2;
		
		Vector4f bounds = new Vector4f(centerX - 32, centerY - 32, centerX + 32, centerY + 32);
		Vector2f coords = new Vector2f((float) screenSpace.x, (float) screenSpace.y);
		return MathUtils.isWithin2DBounds(coords, bounds);
	}
	
	public static boolean screenSpaceCoordinateIsVisible(Vec3 pos)
	{
		return pos != null && (pos.z > -1 && pos.z < 1);
	}
}
