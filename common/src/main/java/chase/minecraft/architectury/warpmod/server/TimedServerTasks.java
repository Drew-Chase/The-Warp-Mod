package chase.minecraft.architectury.warpmod.server;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Timer;

@SuppressWarnings("unused")
/**
 * The TimedServerTasks class manages a collection of timed server tasks with the ability to create, remove, and retrieve tasks.
 */
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
	
	/**
	 * The function checks if a given name exists as a key in a map of tasks.
	 *
	 * @param name The name of the task that we want to check if it exists in the `_tasks` map. The method returns `true` if the task exists in the map, and `false` otherwise.
	 * @return The method `exists` is returning a boolean value. It returns `true` if the `_tasks` map contains a key that matches the `name` parameter, and `false` otherwise.
	 */
	public boolean exists(String name)
	{
		return _tasks.containsKey(name);
	}
	
	/**
	 * This function checks if a task with a given name exists and if it has been canceled.
	 *
	 * @param name A string representing the name of a task.
	 * @return If the task with the given name exists, the method returns a boolean value indicating whether the task is canceled or not. If the task does not exist, the method returns true.
	 */
	public boolean isCanceled(String name)
	{
		if (exists(name))
		{
			return _tasks.get(name).isCanceled();
		}
		return true;
	}
	
	/**
	 * This function creates a timed server task with a given name and duration, and an optional onCompletion runnable, and returns a boolean indicating success or failure.
	 *
	 * @param name A string representing the name of the task to be created.
	 * @param duration The duration parameter is a long value representing the time in milliseconds for which the TimedServerTask should run before it is completed.
	 * @param onCompletion The onCompletion parameter is a Runnable object that represents a task to be executed when the timed server task is completed. It is an optional parameter and can be set to null if no action is required upon completion of the task.
	 * @return The method `create` returns a boolean value. It returns `true` if a new `TimedServerTask` is successfully created and added to the `_tasks` map, and `false` if a task with the same name already exists in the map.
	 */
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
	
	/**
	 * This function creates an object with a given name and duration in minutes, and returns a boolean value indicating success or failure.
	 *
	 * @param name A String representing the name of the object being created.
	 * @param durationMinutes durationMinutes is an integer parameter that represents the duration of an event in minutes. It is used in a method called "create" which creates an event with a given name and duration. If the method is called with only the name and durationMinutes parameters, the third parameter (which is optional) is
	 * @return The `create` method is returning a boolean value. The value being returned is the result of calling the `create` method with three parameters: `name`, `durationMinutes`, and `null`.
	 */
	public boolean create(String name, int durationMinutes)
	{
		return create(name, durationMinutes, null);
	}
	
	/**
	 * This function removes a task with the given name if it exists in a collection of tasks.
	 *
	 * @param name The parameter "name" is a String that represents the name of a task to be removed from a collection of tasks. The method checks if the task exists in the collection and removes it if it does.
	 */
	public void remove(String name)
	{
		if (exists(name))
		{
			_tasks.remove(name);
		}
	}
	
	
	/**
	 * This function returns a TimedServerTask object with the given name if it exists, otherwise it returns null.
	 *
	 * @param name A string representing the name of the TimedServerTask object that is being searched for in the _tasks HashMap.
	 * @return This method returns a TimedServerTask object with the given name if it exists in the _tasks map, otherwise it returns null.
	 */
	public @Nullable TimedServerTask get(String name)
	{
		if (exists(name))
		{
			return _tasks.get(name);
		}
		return null;
	}
}
