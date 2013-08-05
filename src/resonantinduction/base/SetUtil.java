package resonantinduction.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetUtil 
{
	/**
	 * Returns a new set with the reverse of the provided set's order.
	 * @param set - set to perform the operation on
	 * @return new set with the previous set's order reversed
	 */
	public static <V> Set<V> inverse(Set<V> set)
	{
		Set<V> toReturn = new HashSet<V>();
		List list = Arrays.asList(set.toArray());
		
		for(int i = list.size()-1; i >= 0; i--)
		{
			toReturn.add((V)list.get(i));
		}
		
		return toReturn;
	}
	
	/**
	 * Returns a copy of the provided set, removing all objects after the defined cap index.
	 * @param set - set to perform the operation on
	 * @param cap - maximum amount of objects this set can store
	 * @return new set with previous set's objects other than those after the cap
	 */
	public static <V> Set<V> cap(Set<V> set, int cap)
	{
		Set<V> toReturn = new HashSet<V>();
		
		if(set.size() <= cap)
		{
			toReturn = copy(set);
		}
		else {
			int count = 0;
			
			for(V obj : set)
			{
				count++;
				
				toReturn.add(obj);
				
				if(count == cap)
				{
					break;
				}
			}
		}
		
		return toReturn;
	}
	
	public static <V> Set<V> copy(Set<V> set)
	{
		Set<V> toReturn = new HashSet<V>();
		
		for(V obj : set)
		{
			toReturn.add(obj);
		}
		
		return toReturn;
	}
	
	/**
	 * Returns a new set created by merging two others together.
	 * @param setOne - a set to be used in the merge
	 * @param setTwo - a set to be used in the merge
	 * @return new merged set
	 */
	public static <V> Set<V> merge(Set<V> setOne, Set<V> setTwo)
	{
		Set<V> newSet = new HashSet<V>();
		
		for(V obj : setOne)
		{
			newSet.add(obj);
		}
		
		for(V obj : setTwo)
		{
			newSet.add(obj);
		}
		
		return newSet;
	}
	
	/**
	 * Splits a set into the defined amount of new sets, and returns them in an ArrayList.
	 * @param set - set to split
	 * @param divide - how many new sets should be created
	 * @return sets split by the defined amount
	 */
	public static <V> ArrayList<Set<V>> split(Set<V> set, int divide)
	{
		int remain = set.size()%divide;
		int size = (set.size()/divide)-remain;
		
		ArrayList<Set<V>> toReturn = new ArrayList<Set<V>>();
		
		for(int i = 0; i < divide; i++)
		{
			toReturn.add(i, new HashSet<V>());
		}
		
		for(Set<V> iterSet : toReturn)
		{
			Set<V> removed = new HashSet<V>();
			
			int toAdd = size;
			
			if(remain > 0)
			{
				remain--;
				toAdd++;
			}
			
			for(V obj : set)
			{
				if(toAdd == 0)
				{
					break;
				}
				
				iterSet.add(obj);
				removed.add(obj);
				toAdd--;
			}
			
			for(V obj : removed)
			{
				set.remove(obj);
			}
		}
		
		return toReturn;
	}
	
	/**
	 * Returns a List of a Set, while maintaining all it's objects.
	 * @param set - set to turn into a list
	 * @return list with defined set's objects
	 */
	public static <V> List<V> asList(Set<V> set)
	{
		return (List<V>)Arrays.asList(set.toArray());
	}
}
