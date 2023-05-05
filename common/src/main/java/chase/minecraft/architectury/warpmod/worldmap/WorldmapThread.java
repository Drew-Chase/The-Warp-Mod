package chase.minecraft.architectury.warpmod.worldmap;

import chase.minecraft.architectury.warpmod.WarpMod;

import java.util.Timer;

public class WorldmapThread extends Thread implements Runnable
{
	private static final WorldmapThread instance = new WorldmapThread();
	private Timer timer;
	
	protected WorldmapThread()
	{
		super("Warp Map Rendering Thread");
	}
	
	@Override
	public void start()
	{
		super.start();
		WarpMod.log.info("Thread is RUNNING!");
//		timer = new Timer();
//		timer.scheduleAtFixedRate(new TimerTask()
//		{
//			@Override
//			public void run()
//			{
//				MapRegions.getInstance().update();
//			}
//		}, 5000, 0);
	}
	
	@Override
	public void interrupt()
	{
		super.interrupt();
		timer.cancel();
	}
	
	public static WorldmapThread getInstance()
	{
		return instance;
	}
}
