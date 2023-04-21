package chase.minecraft.architectury.warpmod.utils;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Random;

/**
 * The MathUtils class contains methods for rounding numbers to a specified number of decimal places and generating random 2D positions within a given range around a center point.
 */
public class MathUtils
{
	/**
	 * The function rounds a given double value to a specified number of decimal places.
	 *
	 * @param from     The number that needs to be rounded to a certain number of decimal places.
	 * @param decimals The number of decimal places to round the input number to.
	 * @return The method is returning a double value that is rounded to the specified number of decimal places.
	 */
	public static double round(double from, int decimals)
	{
		return (Math.floor((from * Math.pow(10, decimals))) / Math.pow(10, decimals));
	}
	
	/**
	 * The function rounds a given float number to a specified number of decimal places.
	 *
	 * @param from     The number that needs to be rounded to a certain number of decimal places.
	 * @param decimals The number of decimal places to round the float value to.
	 * @return The method is returning a float value that is rounded to the specified number of decimal places.
	 */
	public static float round(float from, int decimals)
	{
		return (float) (Math.floor((from * Math.pow(10, decimals))) / (float) Math.pow(10, decimals));
	}
	
	/**
	 * The function returns a random 2D position within a specified range around a given center point.
	 *
	 * @param center A Vec3 object representing the center point of the 2D space from which a random position will be generated.
	 * @param max    The maximum distance from the center point in the x and y directions that the random 2D position can be generated.
	 * @return The method is returning a 2D vector position (Vec2) that is randomly generated within a certain range (specified by the "max" parameter) around a given 3D center point (specified by the "center" parameter). The method is using another method called "getRandom2DPosition" with additional parameters to generate the random position.
	 */
	public static Vec2 getRandom2DPosition(Vec3 center, int max)
	{
		return getRandom2DPosition(center, 0, max, true);
	}
	
	/**
	 * This function returns a random 2D position within a given range around a center point in 3D space.
	 *
	 * @param center A Vec3 object representing the center point of the 2D space from which a random position will be generated.
	 * @param min    The minimum distance from the center in the x and y directions for the random 2D position.
	 * @param max    The maximum distance from the center in the x and y directions to generate a random 2D position.
	 * @return The method is returning a 2D vector (Vec2) representing a random position within a specified range (min to max) around a given center point (Vec3). The fourth parameter (true) indicates whether the position should be restricted to within a circle centered at the given center point.
	 */
	public static Vec2 getRandom2DPosition(Vec3 center, int min, int max)
	{
		return getRandom2DPosition(center, min, max, true);
	}
	
	/**
	 * This function generates a random 2D position around a given center point within a specified range, with the option to include negative values.
	 *
	 * @param center          A Vec3 object representing the center point around which the random 2D position will be generated.
	 * @param min             The minimum value for the x and y coordinates of the generated random position.
	 * @param max             The maximum value for the randomly generated x and y coordinates.
	 * @param includeNegative A boolean value that determines whether the generated x and y values can be negative or not. If set to true, the generated values can be negative. If set to false, the generated values will always be positive.
	 * @return A Vec2 object representing a randomly generated 2D position around a given center point, with the x and y coordinates determined by a random integer between a given minimum and maximum value, and the option to include negative values.
	 */
	public static Vec2 getRandom2DPosition(Vec3 center, int min, int max, boolean includeNegative)
	{
		Random random = new Random();
		int x, y;
		boolean negX = random.nextBoolean(), negY = random.nextBoolean();
		if (max == min)
			x = y = max;
		else
		{
			x = random.nextInt(min, max);
			y = random.nextInt(min, max);
		}
		if (negX)
			x *= -1;
		if (negY)
			y *= -1;
		return new Vec2((float) (center.x + x), (float) (center.y + y));
	}
	
	public static boolean isWithin2DBounds(Vector2f position, Vector4f bounds)
	{
		return position.x >= bounds.x && position.y >= bounds.y && position.x <= bounds.z && position.y <= bounds.w;
	}
}
