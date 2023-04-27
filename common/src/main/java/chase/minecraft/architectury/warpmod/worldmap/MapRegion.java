package chase.minecraft.architectury.warpmod.worldmap;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.utils.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MaterialColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class MapRegion
{
	public static final int SIZE = 512;
	private final int x;
	private final int y;
	private final int[] colors;
	private final ClientLevel level;
	
	public MapRegion(BlockPos centerPos)
	{
		level = Minecraft.getInstance().level;
		assert level != null;
		ChunkPos cp = new ChunkPos(centerPos);
		x = cp.getRegionX();
		y = cp.getRegionZ();
		colors = new int[SIZE * SIZE];
	}
	
	public void update()
	{
		int cx = x * SIZE;
		int cz = y * SIZE;
		BlockPos startPos = WorldUtils.getGroundPos(cx, cz);
		BlockPos currentPos = startPos;
		for (int i = 0; i < SIZE; i++)
		{
			for (int j = 0; j < SIZE; j++)
			{
				int index = j + (i * SIZE);
				if (colors[index] != 0)
				{
					continue;
				}
				if (currentPos.getY() > level.getMinBuildHeight())
				{
					MaterialColor color = level.getBlockState(currentPos).getMapColor(level, currentPos);
					int r = FastColor.ARGB32.red(color.col);
					int g = FastColor.ARGB32.green(color.col);
					int b = FastColor.ARGB32.blue(color.col);
					int alpha = 255;
					colors[index] = FastColor.ARGB32.color(alpha, r, g, b);
				} else
				{
					colors[index] = 0x00_00_00_00;
				}
				currentPos = new BlockPos(currentPos.getX(), level.getChunk(currentPos).getHeight(Heightmap.Types.MOTION_BLOCKING, currentPos.getX(), currentPos.getZ()), currentPos.getZ()).east();
			}
			currentPos = new BlockPos(startPos.getX(), level.getChunk(currentPos).getHeight(Heightmap.Types.MOTION_BLOCKING, currentPos.getX(), currentPos.getZ()), currentPos.getZ()).south();
		}
		save();
	}
	
	public void save()
	{
		WarpMod.log.info("saving region map");
		int[] pixels = getColors();
		int size = MapRegion.SIZE;
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, size, size, pixels, 0, size);
		try
		{
			Minecraft client = Minecraft.getInstance();
			assert client.level != null;
			String name = client.isSingleplayer() ? Objects.requireNonNull(client.getSingleplayerServer()).getWorldData().getLevelName() : Objects.requireNonNull(client.getCurrentServer()).ip;
			Path dir = Path.of(WorldUtils.getWarpDirectory(name).toString(), "maps", client.level.dimension().location().getPath());
			dir.toFile().mkdirs();
			File file = Path.of(dir.toString(), this + ".png").toFile();
			ImageIO.write(image, "png", file);
			WarpMod.log.info("PATH: {}", file.getAbsolutePath());
		} catch (IOException e)
		{
			WarpMod.log.error("Unable to write region file: {}", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public int[] getColors()
	{
		return colors;
	}
	
	private BlockState getCorrectStateForFluidBlock(Level level, BlockState blockState, BlockPos blockPos)
	{
		FluidState fluidState = blockState.getFluidState();
		return !fluidState.isEmpty() && !blockState.isFaceSturdy(level, blockPos, Direction.UP) ? fluidState.createLegacyBlock() : blockState;
	}
	
	@Override
	public String toString()
	{
		return x + "," + y;
	}
}
