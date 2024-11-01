package team.sailboat.commons.fan.eazi;

import java.util.Arrays;

class HandleTable_In
{
	static final int NULL_HANDLE = -1;
	/* status codes indicating whether object has associated exception */
	private static final byte STATUS_OK = 1;
	private static final byte STATUS_UNKNOWN = 2;
	private static final byte STATUS_EXCEPTION = 3;

	/** array mapping handle -> object status */
	byte[] status;
	/** array mapping handle -> object/exception (depending on status) */
	Object[] entries;
	/** array mapping handle -> list of dependent handles (if any) */
	HandleList[] deps;
	/** lowest unresolved dependency */
	int lowDep = -1;
	/** number of handles in table */
	int size = 0;

	/**
	 * Creates handle table with the given initial capacity.
	 */
	HandleTable_In(int initialCapacity)
	{
		status = new byte[initialCapacity];
		entries = new Object[initialCapacity];
		deps = new HandleList[initialCapacity];
	}

	/**
	 * Assigns next available handle to given object, and returns assigned
	 * handle.  Once object has been completely deserialized (and all
	 * dependencies on other objects identified), the handle should be
	 * "closed" by passing it to finish().
	 */
	int assign(Object obj)
	{
		if (size >= entries.length)
		{
			grow();
		}
		status[size] = STATUS_UNKNOWN;
		entries[size] = obj;
		return size++;
	}

	/**
	 * Registers a dependency (in exception status) of one handle on
	 * another.  The dependent handle must be "open" (i.e., assigned, but
	 * not finished yet).  No action is taken if either dependent or target
	 * handle is NULL_HANDLE.
	 */
	void markDependency(int dependent, int target)
	{
		if (dependent == NULL_HANDLE || target == NULL_HANDLE)
		{
			return;
		}
		switch (status[dependent])
		{

		case STATUS_UNKNOWN:
			switch (status[target])
			{
			case STATUS_OK:
				// ignore dependencies on objs with no exception
				break;

			case STATUS_EXCEPTION:
				// eagerly propagate exception
				markException(dependent,
						(ClassNotFoundException) entries[target]);
				break;

			case STATUS_UNKNOWN:
				// add to dependency list of target
				if (deps[target] == null)
				{
					deps[target] = new HandleList();
				}
				deps[target].add(dependent);

				// remember lowest unresolved target seen
				if (lowDep < 0 || lowDep > target)
				{
					lowDep = target;
				}
				break;

			default:
				throw new InternalError();
			}
			break;

		case STATUS_EXCEPTION:
			break;

		default:
			throw new InternalError();
		}
	}

	/**
	 * Associates a ClassNotFoundException (if one not already associated)
	 * with the currently active handle and propagates it to other
	 * referencing objects as appropriate.  The specified handle must be
	 * "open" (i.e., assigned, but not finished yet).
	 */
	void markException(int handle, ClassNotFoundException ex)
	{
		switch (status[handle])
		{
		case STATUS_UNKNOWN:
			status[handle] = STATUS_EXCEPTION;
			entries[handle] = ex;

			// propagate exception to dependents
			HandleList dlist = deps[handle];
			if (dlist != null)
			{
				int ndeps = dlist.size();
				for (int i = 0; i < ndeps; i++)
				{
					markException(dlist.get(i), ex);
				}
				deps[handle] = null;
			}
			break;

		case STATUS_EXCEPTION:
			break;

		default:
			throw new InternalError();
		}
	}

	/**
	 * Marks given handle as finished, meaning that no new dependencies
	 * will be marked for handle.  Calls to the assign and finish methods
	 * must occur in LIFO order.
	 */
	void finish(int handle)
	{
		int end;
		if (lowDep < 0)
		{
			// no pending unknowns, only resolve current handle
			end = handle + 1;
		}
		else if (lowDep >= handle)
		{
			// pending unknowns now clearable, resolve all upward handles
			end = size;
			lowDep = -1;
		}
		else
		{
			// unresolved backrefs present, can't resolve anything yet
			return;
		}

		// change STATUS_UNKNOWN -> STATUS_OK in selected span of handles
		for (int i = handle; i < end; i++)
		{
			switch (status[i])
			{
			case STATUS_UNKNOWN:
				status[i] = STATUS_OK;
				deps[i] = null;
				break;

			case STATUS_OK:
			case STATUS_EXCEPTION:
				break;

			default:
				throw new InternalError();
			}
		}
	}

	/**
	 * Assigns a new object to the given handle.  The object previously
	 * associated with the handle is forgotten.  This method has no effect
	 * if the given handle already has an exception associated with it.
	 * This method may be called at any time after the handle is assigned.
	 */
	void setObject(int handle, Object obj)
	{
		switch (status[handle])
		{
		case STATUS_UNKNOWN:
		case STATUS_OK:
			entries[handle] = obj;
			break;

		case STATUS_EXCEPTION:
			break;

		default:
			throw new InternalError();
		}
	}

	/**
	 * Looks up and returns object associated with the given handle.
	 * Returns null if the given handle is NULL_HANDLE, or if it has an
	 * associated ClassNotFoundException.
	 */
	Object lookupObject(int handle)
	{
		return (handle != NULL_HANDLE && status[handle] != STATUS_EXCEPTION) ?
				entries[handle] : null;
	}

	/**
	 * Looks up and returns ClassNotFoundException associated with the
	 * given handle.  Returns null if the given handle is NULL_HANDLE, or
	 * if there is no ClassNotFoundException associated with the handle.
	 */
	ClassNotFoundException lookupException(int handle)
	{
		return (handle != NULL_HANDLE && status[handle] == STATUS_EXCEPTION) ?
				(ClassNotFoundException) entries[handle] : null;
	}

	/**
	 * Resets table to its initial state.
	 */
	void clear()
	{
		Arrays.fill(status, 0, size, (byte) 0);
		Arrays.fill(entries, 0, size, null);
		Arrays.fill(deps, 0, size, null);
		lowDep = -1;
		size = 0;
	}

	/**
	 * Returns number of handles registered in table.
	 */
	int size()
	{
		return size;
	}

	/**
	 * Expands capacity of internal arrays.
	 */
	private void grow()
	{
		int newCapacity = (entries.length << 1) + 1;

		byte[] newStatus = new byte[newCapacity];
		Object[] newEntries = new Object[newCapacity];
		HandleList[] newDeps = new HandleList[newCapacity];

		System.arraycopy(status, 0, newStatus, 0, size);
		System.arraycopy(entries, 0, newEntries, 0, size);
		System.arraycopy(deps, 0, newDeps, 0, size);

		status = newStatus;
		entries = newEntries;
		deps = newDeps;
	}

	private static class HandleList
	{
		private int[] list = new int[4];
		private int size = 0;

		public HandleList()
		{
		}

		public void add(int handle)
		{
			if (size >= list.length)
			{
				int[] newList = new int[list.length << 1];
				System.arraycopy(list, 0, newList, 0, list.length);
				list = newList;
			}
			list[size++] = handle;
		}

		public int get(int index)
		{
			if (index >= size)
			{
				throw new ArrayIndexOutOfBoundsException();
			}
			return list[index];
		}

		public int size()
		{
			return size;
		}
	}
}
