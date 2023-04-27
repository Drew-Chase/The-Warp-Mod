package chase.minecraft.architectury.warpmod.worldmap;

import java.util.Timer;
import java.util.TimerTask;

public class  WorldmapThread extends Thread implements Runnable
{
	private static final WorldmapThread instance = new WorldmapThread();
	
	protected WorldmapThread()
	{
		super("Warp Map Rendering Thread");
		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				WorldmapThread.instance.start();
			}
		};
		timer.scheduleAtFixedRate(task, 5000, 0);
	}
	
	@Override
	public void run()
	{
		super.run();
		MapRegions.getInstance().update();
	}
	
	
	public static WorldmapThread getInstance()
	{
		return instance;
	}
}
