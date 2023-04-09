package chase.minecraft.architectury.warpmod.fabric;

import chase.minecraft.architectury.warpmod.client.WarpModClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ClientWarpModFabric extends WarpModClient implements ClientModInitializer
{
	/**
	 * Runs the mod initializer on the client environment.
	 */
	@Override
	public void onInitializeClient()
	{
		WarpModClient.init();
		ClientTickEvents.END_CLIENT_TICK.register(client -> onClientTick());
		ClientPlayConnectionEvents.JOIN.register((listener, sender, minecraft) -> onServerLogin(listener.getConnection()));
		ClientPlayConnectionEvents.DISCONNECT.register((listener, minecraft) -> onServerLogout(listener.getConnection()));
	}
}
