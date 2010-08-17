package com.l2jfrozen.gameserver.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author l2j-free
 * @author ProGramMoS
 */

public final class RunnableStatsManager
{
	//private static final Logger _log = Logger.getLogger(RunnableStatsManager.class.getName());

	private static final Map<Class<?>, ClassStat> _classStats = new HashMap<Class<?>, ClassStat>();

	private static final class ClassStat
	{
//		private final String _className;

		private String[] _methodNames = new String[0];
		private MethodStat[] _methodStats = new MethodStat[0];

		private ClassStat(Class<?> clazz)
		{
//			_className = clazz.getName().replace("com.l2jfrozen.gameserver.", "");
			_classStats.put(clazz, this);
		}

		private MethodStat getMethodStat(String methodName, boolean synchronizedAlready)
		{
			for(int i = 0; i < _methodNames.length; i++)
				if(_methodNames[i].equals(methodName))
					return _methodStats[i];

			if(!synchronizedAlready)
			{
				synchronized (this)
				{
					return getMethodStat(methodName, true);
				}
			}

			methodName = methodName.intern();

			final MethodStat methodStat = new MethodStat(/*_className, methodName*/);

			_methodNames = (String[]) ArrayUtils.add(_methodNames, methodName);
			_methodStats = (MethodStat[]) ArrayUtils.add(_methodStats, methodStat);

			return methodStat;
		}
	}

	private static final class MethodStat
	{
		private final ReentrantLock _lock = new ReentrantLock();

//		private final String _className;
//		private final String _methodName;

		private long _count;
		private long _total;
		private long _min = Long.MAX_VALUE;
		private long _max = Long.MIN_VALUE;

//		private MethodStat(String className, String methodName)
//		{
//			_className = className;
//			_methodName = methodName;
//		}

		private void handleStats(long runTime)
		{
			_lock.lock();
			try
			{
				_count++;
				_total += runTime;
				_min = Math.min(_min, runTime);
				_max = Math.max(_max, runTime);
			}
			finally
			{
				_lock.unlock();
			}
		}
	}

	private static ClassStat getClassStat(Class<?> clazz, boolean synchronizedAlready)
	{
		ClassStat classStat = _classStats.get(clazz);

		if(classStat != null)
			return classStat;

		if(!synchronizedAlready)
		{
			synchronized (RunnableStatsManager.class)
			{
				return getClassStat(clazz, true);
			}
		}

		return new ClassStat(clazz);
	}

	public static void handleStats(Class<? extends Runnable> clazz, long runTime)
	{
		handleStats(clazz, "run()", runTime);
	}

	public static void handleStats(Class<?> clazz, String methodName, long runTime)
	{
		getClassStat(clazz, false).getMethodStat(methodName, false).handleStats(runTime);
	}
}
