package resonantinduction.api;

import java.util.LinkedHashSet;
import java.util.Set;

public class OreDetectionBlackList 
{
	private static final Set<String> oreBlackList = new LinkedHashSet<String>();
	private static final Set<String> ingotBlackList = new LinkedHashSet<String>();
	
	public static void addOre(String s)
	{
		oreBlackList.add(s);
	}
	public static void addIngot(String s)
	{
		ingotBlackList.add(s);
	}
	
	public static boolean isOreBlackListed(String s)
	{
		return oreBlackList.contains(s);
	}
	public static boolean isIngotBlackListed(String s)
	{
		return ingotBlackList.contains(s);
	}
}
