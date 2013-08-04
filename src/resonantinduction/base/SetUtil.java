package resonantinduction.base;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
}
