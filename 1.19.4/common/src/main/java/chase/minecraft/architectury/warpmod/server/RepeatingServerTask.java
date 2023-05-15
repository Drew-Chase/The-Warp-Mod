package chase.minecraft.architectury.warpmod.server;

import java.util.TimerTask;

/**
 * The RepeatingServerTask class is a TimerTask that can be canceled and run, and checks if it has been canceled.
 */
public class RepeatingServerTask extends TimerTask
{
	
	// `private final Runnable _action;` is declaring a private final field named `_action` of type `Runnable` in the `RepeatingServerTask` class. This field is used to store the action that the task should perform when it is run. It is set in the constructor when a `Runnable` object is passed as a parameter. The `run()` method then calls the `run()` method of the stored `Runnable` object to execute the task.
	private final Runnable _action;
	// `private boolean canceled;` is declaring a private boolean field named `canceled` in the `RepeatingServerTask` class. This field is used to keep track of whether the task has been canceled or not. It is initially set to `false` in the constructor and can be set to `true` when the `cancel()` method is called. The `run()` method checks the value of `canceled` before executing the task to ensure that it has not been canceled.
	private boolean canceled;
	
	// This is the constructor for the `RepeatingServerTask` class. It takes a `Runnable` object as a parameter and assigns it to the `_action` field. It also sets the `canceled` field to `false`.
	public RepeatingServerTask(Runnable action)
	{
		_action = action;
		canceled = false;
	}
	
	/**
	 * This function runs a specified action only if it has not been canceled.
	 */
	@Override
	public void run()
	{
		if (!canceled)
		{
			_action.run();
		}
	}
	
	/**
	 * This function sets a boolean flag to true and returns the result of calling the superclass's cancel method.
	 *
	 * @return The method is returning a boolean value, which is the result of calling the `cancel()` method of the superclass.
	 */
	@Override
	public boolean cancel()
	{
		canceled = true;
		return super.cancel();
	}
	
	/**
	 * This function runs a task and then cancels it, returning a boolean value indicating whether the cancellation was successful.
	 *
	 * @return The method is returning a boolean value, which is the result of calling the `cancel()` method.
	 */
	public boolean cancelAndRun()
	{
		run();
		return cancel();
	}
	
	/**
	 * This function returns a boolean value indicating whether a task has been canceled or not.
	 *
	 * @return The method is returning a boolean value, which indicates whether a certain event or action has been canceled or not. The value returned is stored in the variable `canceled`.
	 */
	public boolean isCanceled()
	{
		return canceled;
	}
}
