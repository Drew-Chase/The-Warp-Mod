package chase.minecraft.architectury.warpmod.server;

import java.util.HashMap;
import java.util.Timer;

/**
 * The `RepeatingServerTasks` class is a singleton class in Java that allows for the creation, retrieval, and removal of repeating server tasks.
 */
public class RepeatingServerTasks
{
	// This line of code is creating a static instance of the `RepeatingServerTasks` class and initializing it with a new instance of the `RepeatingServerTasks` class. This static instance can be accessed from other parts of the code without needing to create a new instance of the `RepeatingServerTasks` class. It is a common pattern used for creating singleton classes in Java.
	public static RepeatingServerTasks Instance = new RepeatingServerTasks();
	private final Timer _timer;
	private final HashMap<String, RepeatingServerTask> _tasks;
	
	// This is a private constructor for the `RepeatingServerTasks` class. It initializes the `Instance` variable to reference the current instance of the class, creates a new `HashMap` object to store the tasks, and creates a new `Timer` object to schedule the tasks. The constructor is private to ensure that only one instance of the `RepeatingServerTasks` class can be created, as it is intended to be a singleton class.
	private RepeatingServerTasks()
	{
		Instance = this;
		_tasks = new HashMap<>();
		_timer = new Timer();
	}
	
	/**
	 * This Java function creates a repeating server task with a given name, rate, and action to be executed.
	 *
	 * @param name   The name of the task being created.
	 * @param rate   The rate parameter is a long value that represents the time interval in milliseconds between each execution of the action Runnable in the RepeatingServerTask. The task will be executed repeatedly at a fixed rate specified by this parameter.
	 * @param action "action" is a Runnable object that represents the task to be executed repeatedly by the RepeatingServerTask. It is a block of code that can be executed by calling the "run()" method of the Runnable object. The "action" parameter is passed to the "create" method to specify the
	 */
	public void create(String name, long rate, Runnable action)
	{
		RepeatingServerTask task = new RepeatingServerTask(() ->
		{
			action.run();
			if (_tasks.get(name).isCanceled())
			{
				remove(name);
			}
		});
		_tasks.put(name, task);
		_timer.scheduleAtFixedRate(task, 0, rate);
	}
	
	/**
	 * This function returns a RepeatingServerTask object based on its name.
	 *
	 * @param name The parameter "name" is a String that represents the name of a RepeatingServerTask object that is stored in a HashMap called "_tasks". The method "get" retrieves the RepeatingServerTask object associated with the given name from the HashMap.
	 * @return A `RepeatingServerTask` object associated with the given `name` key from the `_tasks` map.
	 */
	public RepeatingServerTask get(String name)
	{
		return _tasks.get(name);
	}
	
	/**
	 * The function checks if a task with a given name exists in a collection of tasks.
	 *
	 * @param name The name of the task that we want to check if it exists in the `_tasks` map. The method returns `true` if the task exists in the map, and `false` otherwise.
	 * @return The method `exists` is returning a boolean value. It returns `true` if the `_tasks` map contains a key that matches the `name` parameter, and `false` otherwise.
	 */
	public boolean exists(String name)
	{
		return _tasks.containsKey(name);
	}
	
	/**
	 * This function removes a task with the given name if it exists.
	 *
	 * @param name The name of the task to be removed from the list of tasks.
	 */
	public void remove(String name)
	{
		if (exists(name))
		{
			get(name).cancel();
			_tasks.remove(name);
		}
	}
}
