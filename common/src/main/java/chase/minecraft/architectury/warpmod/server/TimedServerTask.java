package chase.minecraft.architectury.warpmod.server;

import org.jetbrains.annotations.Nullable;

import java.util.TimerTask;

@SuppressWarnings("unused")
public class TimedServerTask extends TimerTask
{
	
	private final Runnable _onCompletion;
	private final String _name;
	private boolean _hasCanceled;
	
	public TimedServerTask(String name, @Nullable Runnable onCompletion)
	{
		_hasCanceled = false;
		_onCompletion = onCompletion;
		_name = name;
	}
	
	/**
	 * This function returns the name of an object as a string.
	 *
	 * @return The method `getName()` is returning a `String` value, which is the value of the private variable `_name`.
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * This function returns a boolean value indicating whether a cancellation has occurred.
	 *
	 * @return The method is returning a boolean value, which indicates whether a cancellation has been requested or not. If `_hasCanceled` is true, it means that the operation has been canceled, and the method will return true. Otherwise, it will return false.
	 */
	public boolean isCanceled()
	{
		return _hasCanceled;
	}
	
	
	/**
	 * Runs the onCompletion action if the task hasn't been canceled
	 */
	@Override
	public void run()
	{
		if (!_hasCanceled)
		{
			_hasCanceled = true;
			if (_onCompletion != null)
			{
				_onCompletion.run();
			}
		}
		TimedServerTasks.Instance.remove(_name);
	}
	
	/**
	 * Cancels the execution of the task
	 *
	 * @return The method is returning a boolean value, which is the result of calling the `cancel()` method of the superclass.
	 */
	@Override
	public boolean cancel()
	{
		_hasCanceled = true;
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
}
