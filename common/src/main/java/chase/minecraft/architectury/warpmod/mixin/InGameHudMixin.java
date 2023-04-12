package chase.minecraft.architectury.warpmod.mixin;

import chase.minecraft.architectury.warpmod.client.ClientWarps;
import chase.minecraft.architectury.warpmod.client.gui.waypoint.WaypointOverlay;
import chase.minecraft.architectury.warpmod.client.renderer.RenderProfiler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin
{
	@Shadow
	@Final
	protected Minecraft minecraft;
	
	@Inject(at = @At("RETURN"), method = "render")
	public void render(PoseStack poseStack, float partialTicks, CallbackInfo cbi)
	{
		
		RenderProfiler.begin("HUD");
		for (ClientWarps.ClientWarp warp : ClientWarps.Instance.getWarps())
		{
			WaypointOverlay overlay = new WaypointOverlay(warp);
			overlay.render(poseStack);
		}
		RenderProfiler.pop();
	}
}
