package chase.minecraft.architectury.warpmod.mixin;

import chase.minecraft.architectury.warpmod.client.ClientWarps;
import chase.minecraft.architectury.warpmod.client.gui.waypoint.WaypointOverlay;
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
	public void render(PoseStack matrixStack, float partialTicks, CallbackInfo cbi)
	{
		ClientWarps.ClientWarp warp = ClientWarps.Instance.getWarps()[0];
		WaypointOverlay overlay = new WaypointOverlay(minecraft, warp);
		overlay.render(matrixStack, partialTicks);
	}
}
