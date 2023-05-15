package chase.minecraft.architectury.warpmod.worldmap;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Worldmap
{
	private static Worldmap instance = new Worldmap();
	private static HashMap<UUID, BlockPos> players;
	private static List<MapRegion> loadedRegions;
	
	protected Worldmap()
	{
		instance = this;
		players = new HashMap<>();
		loadedRegions = new ArrayList<>();
	}
	
	
	public void updatePlayer(UUID player, BlockPos newPos)
	{
		players.put(player, newPos);
	}
	
	@Nullable
	public BlockPos getPlayer(UUID player)
	{
		return players.getOrDefault(player, null);
	}
	
	public HashMap<UUID, BlockPos> getPlayers()
	{
		return players;
	}
	
	public static Worldmap getInstance()
	{
		return instance;
	}
}
