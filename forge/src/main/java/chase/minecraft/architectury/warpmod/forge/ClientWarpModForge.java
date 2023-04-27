package chase.minecraft.architectury.warpmod.forge;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.client.WarpModClient;
import chase.minecraft.architectury.warpmod.client.gui.GUIFactory;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = WarpMod.MOD_ID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientWarpModForge extends WarpModClient
{
	@SubscribeEvent
	public static void clientTick(final TickEvent.ClientTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END)
		{
			onClientTick();
		}
	}
	
	@SubscribeEvent
	public static void loggingIn(final ClientPlayerNetworkEvent.LoggingIn event)
	{
		Connection connection = event.getConnection();
		onServerLogin(connection);
	}
	
	@SubscribeEvent
	public static void loggingOut(final ClientPlayerNetworkEvent.LoggingOut event)
	{
		if (event.getConnection() != null)
			onServerLogout(event.getConnection());
	}
	
	@SubscribeEvent
	static void changeDimension(final EntityTravelToDimensionEvent event)
	{
		if (event.getEntity() instanceof ServerPlayer player)
		{
			WarpModClient.changeDimension(player, event.getDimension().location());
		}
	}
	
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
