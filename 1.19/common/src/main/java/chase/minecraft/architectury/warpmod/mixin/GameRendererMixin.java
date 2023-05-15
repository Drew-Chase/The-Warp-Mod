package chase.minecraft.architectury.warpmod.mixin;

import chase.minecraft.architectury.warpmod.client.gui.GUIFactory;
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
		GUIFactory.PostGameOverlay(poseStack);
	}
}
