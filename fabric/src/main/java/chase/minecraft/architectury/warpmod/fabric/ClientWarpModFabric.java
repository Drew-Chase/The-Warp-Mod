package chase.minecraft.architectury.warpmod.fabric;

import chase.minecraft.architectury.warpmod.client.WarpModClient;
import net.fabricmc.api.ClientModInitializer;

public class ClientWarpModFabric extends WarpModClient implements ClientModInitializer
{
	/**
	 * Runs the mod initializer on the client environment.
	 */
	@Override
	public void onInitializeClient()
	{
		init();
	}
}
