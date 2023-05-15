package chase.minecraft.architectury.warpmod.forge;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.client.WarpModClient;
import chase.minecraft.architectury.warpmod.client.gui.GUIFactory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = WarpMod.MOD_ID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientWarpModForge extends WarpModClient
{
	
	@SubscribeEvent
	static void renderOverlay(final RenderGuiOverlayEvent.Pre event)
	{
		GUIFactory.PreGameOverlay(event.getPoseStack());
	}
	
	@SubscribeEvent
	static void renderOverlay(final RenderGuiOverlayEvent.Post event)
	{
		GUIFactory.PostGameOverlay(event.getPoseStack());
	}
	
	
}
