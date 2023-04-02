package chase.minecraft.architectury.warpmod.server;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Timer;
@SuppressWarnings("unused")
public class TimedServerTasks
{
	/**
	 * Singleton pattern.
	 */
	public static TimedServerTasks Instance = new TimedServerTasks();
	
	private final HashMap<String, TimedServerTask> _tasks;
	private final Timer _timer;
	
	protected TimedServerTasks()
	{
		Instance = this;
		_tasks = new HashMap<>();
		_timer = new Timer();
	}
	
	public boolean exists(String name)
	{
		return _tasks.containsKey(name);
	}
	
	public boolean isCanceled(String name)
	{
		if (exists(name))
		{
			return _tasks.get(name).isCanceled();
		}
		return true;
	}
	
	public boolean create(String name, long duration, @Nullable Runnable onCompletion)
	{
		if (!exists(name))
		{
			TimedServerTask task = new TimedServerTask(name, onCompletion);
			_tasks.put(name, task);
			_timer.schedule(task, duration);
			return true;
		}
		
		return false;
	}
	
	public boolean create(String name, int durationMinutes)
	{
		return create(name, durationMinutes, null);
	}
	
	public void remove(String name)
	{
		if (exists(name))
		{
			_tasks.remove(name);
		}
	}
	
	
	public @Nullable TimedServerTask get(String name)
	{
		if (exists(name))
		{
			return _tasks.get(name);
		}
		return null;
	}
}
