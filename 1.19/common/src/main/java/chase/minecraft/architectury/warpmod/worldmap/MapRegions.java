package chase.minecraft.architectury.warpmod.worldmap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.Collection;
import java.util.HashMap;

public class MapRegions
{
	private static MapRegions instance = new MapRegions();
	private final HashMap<String, MapRegion> regions;
	
	protected MapRegions()
	{
		instance = this;
		regions = new HashMap<>();
	}
	
	public void add(BlockPos pos)
	{
		MapRegion region = new MapRegion(pos);
		regions.put(region.toString(), region);
	}
	
	public MapRegion getOrAdd(BlockPos pos)
	{
		ChunkPos chunkPos = new ChunkPos(pos);
		String reg = chunkPos.getRegionX() + "," + chunkPos.getRegionZ();
		if (regions.containsKey(reg))
		{
			MapRegion region = regions.get(reg);
			region.update();
			return region;
		}
		MapRegion region = new MapRegion(pos);
		regions.put(region.toString(), region);
		return region;
	}
	
	public Collection<MapRegion> get()
	{
		return regions.values();
	}
	
	public void update()
	{
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		assert player != null;
		getOrAdd(player.getOnPos());
		get().forEach(MapRegion::update);
	}
	
	
	public static MapRegions getInstance()
	{
		return instance;
	}
	
}
