package chase.minecraft.architectury.warpmod.client.renderer;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
public class RenderProfiler
{
	static final Stack<Entry> s = new Stack<>();
	static final Map<String, Entry> latestTickTimes = new ConcurrentHashMap<>();
	
	public static void begin(String sec)
	{
		long start = System.nanoTime();
		s.push(new Entry(sec, start, start));
	}
	
	public static void pop()
	{
		Entry pop = s.pop();
		latestTickTimes.put(pop.name, new Entry(pop.name, pop.start, System.nanoTime()));
	}
	
	public static Entry[] getAllTickTimes()
	{
		Entry[] entries = new Entry[latestTickTimes.size()];
		String[] keys = latestTickTimes.keySet().toArray(new String[0]);
		for (int i = 0; i < keys.length; i++)
		{
			entries[i] = latestTickTimes.get(keys[i]);
		}
		latestTickTimes.clear();
		return entries;
	}
	
	public record Entry(String name, long start, long end)
	{
	}
}
