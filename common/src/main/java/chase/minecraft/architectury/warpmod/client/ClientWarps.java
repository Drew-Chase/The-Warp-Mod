package chase.minecraft.architectury.warpmod.client;

import chase.minecraft.architectury.warpmod.WarpMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class ClientWarps
{
	public record ClientWarp(String name, double x, double y, double z, float pitch, float yaw, ResourceLocation dimension)
	{
		public CompoundTag toNBT()
		{
			CompoundTag tag = new CompoundTag();
			tag.putString("name", name());
			tag.putDouble("x", x());
			tag.putDouble("y", y());
			tag.putDouble("z", z());
			tag.putFloat("pitch", pitch());
			tag.putFloat("yaw", yaw());
			tag.putString("dim", dimension.toString());
			return tag;
		}
	}
	
	public static ClientWarps Instance = new ClientWarps();
	private final HashMap<String, ClientWarp> _warps;
	
	private ClientWarps()
	{
		Instance = this;
		_warps = new HashMap<>();
	}
	
	@NotNull
	public ClientWarp[] getWarps()
	{
		return _warps.values().stream().sorted((f, s) -> f.name.compareTo(s.name)).toList().toArray(new ClientWarp[0]);
	}
	
	@Nullable
	public ClientWarp get(String name)
	{
		if (exists(name))
			return _warps.get(name);
		return null;
	}
	
	public boolean exists(String name)
	{
		return _warps.containsKey(name);
	}
	
	
	public void fromNBT(@NotNull CompoundTag tag)
	{
		_warps.clear();
		try
		{
			ListTag listTag = tag.getList("warps", CompoundTag.TAG_COMPOUND);
			WarpMod.log.info(String.format("Loading Warp NBT: %d", listTag.size()));
			for (int i = 0; i < listTag.size(); i++)
			{
				CompoundTag ct = listTag.getCompound(i);
				
				String name = ct.getString("name");
				double x = ct.getDouble("x");
				double y = ct.getDouble("y");
				double z = ct.getDouble("z");
				float yaw = ct.getFloat("yaw");
				float pitch = ct.getFloat("pitch");
				ResourceLocation dim = new ResourceLocation(ct.getString("dim"));
				ClientWarp warp = new ClientWarp(name, x, y, z, pitch, yaw, dim);
				
				_warps.put(warp.name, warp);
			}
		} catch (Exception e)
		{
			WarpMod.log.error(String.format("Unable to load Player Warp NBT: %s", e.getMessage()));
		}
		WarpMod.log.info(String.format("%d Warps found!", _warps.size()));
	}
	
	
}
