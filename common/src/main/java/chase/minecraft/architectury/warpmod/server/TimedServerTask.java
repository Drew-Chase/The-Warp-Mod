package chase.minecraft.architectury.warpmod.server;

import javax.annotation.Nullable;
import java.util.TimerTask;
@SuppressWarnings("unused")
public class TimedServerTask extends TimerTask
{
	
	public String getName()
	{
		return _name;
	}
	private boolean _hasCanceled;
	private final Runnable _onCompletion;
	
	private final String _name;
	public TimedServerTask(String name, @Nullable Runnable onCompletion)
	{
		_hasCanceled = false;
		_onCompletion = onCompletion;
		_name = name;
	}
	
	public boolean isCanceled()
	{
		return _hasCanceled;
	}
	
	
	/**
	 * Action performed when task completes
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
	
	@Override
	public boolean cancel()
	{
		_hasCanceled = true;
		return super.cancel();
	}
	
	public boolean cancelAndRun()
	{
		run();
		return cancel();
	}
}
