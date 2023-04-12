package chase.minecraft.architectury.warpmod.mixin;

import chase.minecraft.architectury.warpmod.client.renderer.RenderProfiler;
import chase.minecraft.architectury.warpmod.client.renderer.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
	
	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderLevel")
	void postWorldRenderer(float tickDelta, long limitTime, PoseStack poseStack, CallbackInfo cb)
	{
		RenderProfiler.begin("Level");
		RenderUtils.lastProjMat.set(RenderSystem.getProjectionMatrix());
		RenderUtils.lastModMat.set(RenderSystem.getModelViewMatrix());
		RenderUtils.lastWorldSpaceMatrix.set(poseStack.last().pose());

		RenderProfiler.pop();
	}
}
