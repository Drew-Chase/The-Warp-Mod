package chase.minecraft.architectury.warpmod.mixin;

import chase.minecraft.architectury.warpmod.client.gui.waypoint.WaypointOverlay;
import chase.minecraft.architectury.warpmod.client.renderer.RenderProfiler;
import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.Warps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("all")
@Mixin(Gui.class)
public abstract class InGameHudMixin
{
	@Shadow
	@Final
	protected Minecraft minecraft;
	
	@Inject(at = @At("HEAD"), method = "render")
	public void render(PoseStack poseStack, float partialTicks, CallbackInfo cbi)
	{
		
		RenderProfiler.begin("HUD");
		for (Warp warp : Warps.fromPlayer(minecraft.player).getWarps())
		{
			WaypointOverlay overlay = new WaypointOverlay(warp);
			overlay.render(poseStack);
		}
		RenderProfiler.pop();
	}
}
