package team.sailboat.commons.fan.event;


public class TEventTable
{
	int[] types;
	ITListener[] listeners;
	int level;
	static final int GROW_SIZE = 4;

	public ITListener[] getListeners(int eventType)
	{
		if (types == null)
			return new ITListener[0];
		int count = 0;
		for (int i = 0; i < types.length; i++)
		{
			if (types[i] == eventType)
				count++;
		}
		if (count == 0)
			return new ITListener[0];
		ITListener[] result = new ITListener[count];
		count = 0;
		for (int i = 0; i < types.length; i++)
		{
			if (types[i] == eventType)
			{
				result[count++] = listeners[i];
			}
		}
		return result;
	}

	public void hook(int eventType, ITListener listener)
	{
		if (types == null)
			types = new int[GROW_SIZE];
		if (listeners == null)
			listeners = new ITListener[GROW_SIZE];
		int length = types.length, index = length - 1;
		while (index >= 0)
		{
			if (types[index] != 0)
				break;
			--index;
		}
		index++;
		if (index == length)
		{
			int[] newTypes = new int[length + GROW_SIZE];
			System.arraycopy(types, 0, newTypes, 0, length);
			types = newTypes;
			ITListener[] newListeners = new ITListener[length + GROW_SIZE];
			System.arraycopy(listeners, 0, newListeners, 0, length);
			listeners = newListeners;
		}
		types[index] = eventType;
		listeners[index] = listener;
	}

	public boolean hooks(int eventType)
	{
		if (types == null)
			return false;
		for (int i = 0; i < types.length; i++)
		{
			if (types[i] == eventType)
				return true;
		}
		return false;
	}

	public void sendEvent(TEvent event)
	{
		if (types == null)
			return;
		level += level >= 0 ? 1 : -1;
		try
		{
			for (int i = 0; i < types.length; i++)
			{
				if (types[i] == event.type)
				{
					ITListener listener = listeners[i];
					if (listener != null)
						listener.handle(event);
				}
			}
		}
		finally
		{
			boolean compact = level < 0;
			level -= level >= 0 ? 1 : -1;
			if (compact && level == 0)
			{
				int index = 0;
				for (int i = 0; i < types.length; i++)
				{
					if (types[i] != 0)
					{
						types[index] = types[i];
						listeners[index] = listeners[i];
						index++;
					}
				}
				for (int i = index; i < types.length; i++)
				{
					types[i] = 0;
					listeners[i] = null;
				}
			}
		}
	}

	public int size()
	{
		if (types == null)
			return 0;
		int count = 0;
		for (int i = 0; i < types.length; i++)
		{
			if (types[i] != 0)
				count++;
		}
		return count;
	}

	void remove(int index)
	{
		if (level == 0)
		{
			int end = types.length - 1;
			System.arraycopy(types, index + 1, types, index, end - index);
			System.arraycopy(listeners, index + 1, listeners, index, end - index);
			index = end;
		}
		else
		{
			if (level > 0)
				level = -level;
		}
		types[index] = 0;
		listeners[index] = null;
	}

	public void unhook(int eventType, ITListener listener)
	{
		if (types == null)
			return;
		for (int i = 0; i < types.length; i++)
		{
			if (types[i] == eventType && listeners[i] == listener)
			{
				remove(i);
				return;
			}
		}
	}
}
