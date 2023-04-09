package chase.minecraft.architectury.warpmod.data;

public class MathUtils
{
	public static double round(double from, int decimals)
	{
		return (Math.floor((from * Math.pow(10, decimals))) / Math.pow(10, decimals));
	}
	
	public static float round(float from, int decimals)
	{
		return (float) (Math.floor((from * Math.pow(10, decimals))) / (float) Math.pow(10, decimals));
	}
}
