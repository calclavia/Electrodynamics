package resonantinduction.base;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

public class SetUtil 
{
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
	
	public static <V> Set<V> cap(Set<V> set, int cap)
	{
		Set<V> toReturn = new HashSet<V>();
		
		if(set.size() <= cap)
		{
			toReturn = set;
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
	
	public static <V> Set<V>[] split(Set<V> set, int divide)
	{
		int remain = set.size()%divide;
		int size = (set.size()/divide)-remain;
		
		Set<V>[] toReturn = new HashSet[divide]; 
		
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
}
