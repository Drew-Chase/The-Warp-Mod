package chase.minecraft.architectury.warpmod.client.gui.waypoint;

import org.jetbrains.annotations.Nullable;

public enum WaypointColor
{
	WHITE(0xFFFFFF),
	RED(0xFF3C3C),
	ORANGE(0xFFA53B),
	LIME(0xC0FF3B),
	GREEN(0x5ce24c),
	EMERALD(0x3BFF89),
	LIGHT_BLUE(0x3BE7FF),
	BLUE(0x3B7EFF),
	PURPLE(0x853BFF),
	MAGENTA(0xE43BFF),
	PINK(0xFF3BD4),
	HOT_PINK(0xFF3B6A),
	YELLOW(0xFBFF3B),
	DARK_RED(0xFF0000),
	DARK_ORANGE(0xFF8A00),
	DARK_LIME(0xADFF00),
	DARK_EMERALD(0x00FF66),
	DARK_BLUE(0x0057FF),
	DARK_PURPLE(0x6100FF),
	;
	private final int color;
	private static String[] colors = new String[0];
	
	WaypointColor(int color)
	{
		this.color = color;
	}
	
	public int getColor()
	{
		return this.color;
	}
	
	public String getName()
	{
		return this.name().replaceAll("_", " ").toUpperCase();
	}
	
	@Nullable
	public static WaypointColor getByName(String name)
	{
		for (WaypointColor color : values())
		{
			if (color.getName().equalsIgnoreCase(name))
			{
				return color;
			}
		}
		return null;
	}
	
	public static String[] getColorNames()
	{
		if (colors.length != 0)
			return colors;
		colors = new String[values().length];
		for (int i = 0; i < colors.length; i++)
		{
			colors[i] = values()[i].getName();
		}
		return colors;
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
}
