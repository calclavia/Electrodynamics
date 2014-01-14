package resonantinduction.core.resource;

public enum EnumRecipePart
{
	RUBBLE("Rubble"), DUST("Dust"), INGOTS("Ingot"), PLATES("Plate"), GEARS("Gears"), TUBE("Tube"),
	ROD("Rod"), SCRAPS("Scraps"), MOLTEN("Molten");

	public String simpleName;

	private EnumRecipePart(String name)
	{
		this.simpleName = name;
	}

	/**
	 * This gets the part name based on the meta value of the ore dirv item. However can also be
	 * used to get the part name if under X value
	 */
	public static String getPartName(int meta)
	{
		int partID = meta % EnumTierMaterial.itemCountPerMaterial;
		if (partID < EnumRecipePart.values().length)
		{
			return EnumRecipePart.values()[partID].simpleName;
		}
		return "Part[" + partID + "]";
	}

	/** This gets the full name based on the metadata of the ore dirv item */
	public static String getFullName(int itemMetaData)
	{
		int matID = itemMetaData / EnumTierMaterial.itemCountPerMaterial;
		int partID = itemMetaData % EnumTierMaterial.itemCountPerMaterial;
		if (matID < EnumTierMaterial.values().length && partID < EnumRecipePart.values().length)
		{
			return EnumTierMaterial.values()[matID].simpleName + EnumRecipePart.values()[partID].simpleName;
		}
		return "OrePart[" + matID + "][" + partID + "]";
	}
}
