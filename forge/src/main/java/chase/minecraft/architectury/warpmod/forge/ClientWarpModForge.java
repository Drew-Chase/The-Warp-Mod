package chase.minecraft.architectury.warpmod.forge;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.client.WarpModClient;
import net.minecraft.network.Connection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WarpMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientWarpModForge extends WarpModClient
{
	@SubscribeEvent
	static void clientTick(TickEvent.ClientTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END)
		{
			onClientTick();
		}
	}
	
	@SubscribeEvent
	static void logginIn(ClientPlayerNetworkEvent.LoggingIn event)
	{
		Connection connection = event.getConnection();
		if (connection != null)
			onServerLogin(connection);
	}
	
	@SubscribeEvent
	static void logginOut(ClientPlayerNetworkEvent.LoggingOut event)
	{
		Connection connection = event.getConnection();
		if (connection != null)
			onServerLogout(connection);
	}
	
}
