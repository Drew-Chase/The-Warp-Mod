package chase.minecraft.architectury.warpmod.client.renderer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class RenderUtils
{
	private static final Minecraft client = Minecraft.getInstance();
	public static final Matrix4f lastProjMat = new Matrix4f();
	public static final Matrix4f lastModMat = new Matrix4f();
	public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
	
	public static Vector3d worldSpaceToScreenSpace(Vector3d pos)
	{
		Camera camera = client.getEntityRenderDispatcher().camera;
		int displayHeight = client.getWindow().getHeight();
		int[] viewport = new int[4];
		GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
		Vector3f target = new Vector3f();
		double dx = pos.x - camera.getPosition().x;
		double dy = pos.y - camera.getPosition().y;
		double dz = pos.z - camera.getPosition().z;
		
		Vector4f transformedCoordinates = new Vector4f((float) dx, (float) dy, (float) dz, 1.f).mul(lastWorldSpaceMatrix);
		
		Matrix4f matrixProj = new Matrix4f(lastProjMat);
		Matrix4f matrixModel = new Matrix4f(lastModMat);
		matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
		
		return new Vector3d(target.x / client.getWindow().getGuiScale(), (displayHeight - target.y) / client.getWindow().getGuiScale(), target.z);
	}
	
	public static boolean screenSpaceCoordinateIsVisible(Vector3d pos)
	{
		return pos != null && (pos.z > -1 && pos.z < 1);
	}
}
