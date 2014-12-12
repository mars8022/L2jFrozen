/*
 * L2jFrozen Project - www.l2jfrozen.com 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfrozen.gameserver.geo.util;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

import javolution.util.FastList;

@SuppressWarnings("unchecked")
public final class L2Arrays
{
	private L2Arrays()
	{
	}
	
	public static int countNull(final Object[] array)
	{
		if (array == null)
			return 0;
		
		int nullCount = 0;
		
		for (final Object obj : array)
			if (obj == null)
				nullCount++;
		
		return nullCount;
	}
	
	public static int countNotNull(final Object[] array)
	{
		return array == null ? 0 : array.length - countNull(array);
	}
	
	/**
	 * @param <T>
	 * @param array to remove null elements from
	 * @return an array without null elements - can be the same, if the original contains no null elements
	 * @throws NullPointerException if array is null
	 */
	public static <T> T[] compact(final T[] array)
	{
		final int newSize = countNotNull(array);
		
		if (array.length == newSize)
			return array;
		
		final T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), newSize);
		
		int index = 0;
		
		for (final T t : array)
			if (t != null)
				result[index++] = t;
		
		return result;
	}
	
	/**
	 * @param <T>
	 * @param array to create a list from
	 * @return a List&lt;T&gt;, which will NOT throw ConcurrentModificationException, if an element gets removed inside a foreach loop, and supports addition
	 */
	public static <T> List<T> asForeachSafeList(final T... array)
	{
		return asForeachSafeList(true, array);
	}
	
	/**
	 * @param <T>
	 * @param allowAddition determines that list MUST support add operation or not
	 * @param array to create a list from
	 * @return a List&lt;T&gt;, which will NOT throw ConcurrentModificationException, if an element gets removed inside a foreach loop, and supports addition if required
	 */
	public static <T> List<T> asForeachSafeList(final boolean allowAddition, final T... array)
	{
		final int newSize = countNotNull(array);
		
		if (newSize == 0 && !allowAddition)
			return L2Collections.emptyList();
		
		if (newSize <= 8)
			return new CopyOnWriteArrayList<>(compact(array));
		
		final List<T> result = new FastList<>(newSize);
		
		for (final T t : array)
			if (t != null)
				result.add(t);
		
		return result;
	}
	
	public static <T> Iterable<T> iterable(final Object[] array)
	{
		return new NullFreeArrayIterable<>(array);
	}
	
	public static <T> Iterable<T> iterable(final Object[] array, final boolean allowNull)
	{
		if (allowNull)
			return new ArrayIterable<>(array);
		return new NullFreeArrayIterable<>(array);
	}
	
	private static class ArrayIterable<T> implements Iterable<T>
	{
		protected final Object[] _array;
		
		protected ArrayIterable(final Object[] array)
		{
			_array = array;
		}
		
		@Override
		public Iterator<T> iterator()
		{
			return new ArrayIterator<>(_array);
		}
	}
	
	protected static final class NullFreeArrayIterable<T> extends ArrayIterable<T>
	{
		protected NullFreeArrayIterable(final Object[] array)
		{
			super(array);
		}
		
		@Override
		public Iterator<T> iterator()
		{
			return new NullFreeArrayIterator<>(_array);
		}
	}
	
	public static <T> Iterator<T> iterator(final Object[] array)
	{
		return new NullFreeArrayIterator<>(array);
	}
	
	public static <T> Iterator<T> iterator(final Object[] array, final boolean allowNull)
	{
		if (allowNull)
			return new ArrayIterator<>(array);
		return new NullFreeArrayIterator<>(array);
	}
	
	private static class ArrayIterator<T> implements Iterator<T>
	{
		private final Object[] _array;
		
		private int _index;
		
		protected ArrayIterator(final Object[] array)
		{
			_array = array;
		}
		
		boolean allowElement(final Object obj)
		{
			return true;
		}
		
		@Override
		public final boolean hasNext()
		{
			for (;;)
			{
				if (_array.length <= _index)
					return false;
				
				if (allowElement(_array[_index]))
					return true;
				
				_index++;
			}
		}
		
		@Override
		public final T next()
		{
			if (!hasNext())
				throw new NoSuchElementException();
			
			return (T) _array[_index++];
		}
		
		@Override
		public final void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	private static final class NullFreeArrayIterator<T> extends ArrayIterator<T>
	{
		protected NullFreeArrayIterator(final Object[] array)
		{
			super(array);
		}
		
		@Override
		boolean allowElement(final Object obj)
		{
			return obj != null;
		}
	}
}
