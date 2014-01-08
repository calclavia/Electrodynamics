package com.builtbroken.minecraft;

public enum EnumOrePart
{

    RUBBLE("Rubble"),
    DUST("Dust"),
    INGOTS("Ingot"),
    PLATES("Plate"),
    GEARS("Gears"),
    TUBE("Tube"),
    ROD("Rod"),
    SCRAPS("Scraps"),
    MOLTEN("Molten");

    public String simpleName;

    private EnumOrePart(String name)
    {
        this.simpleName = name;
    }

    /** This gets the part name based on the meta value of the ore dirv item. However can also be
     * used to get the part name if under X value */
    public static String getPartName(int meta)
    {
        int partID = meta % EnumMaterial.itemCountPerMaterial;
        if (partID < EnumOrePart.values().length)
        {
            return EnumOrePart.values()[partID].simpleName;
        }
        return "Part[" + partID + "]";
    }

    /** This gets the full name based on the metadata of the ore dirv item */
    public static String getFullName(int itemMetaData)
    {
        int matID = itemMetaData / EnumMaterial.itemCountPerMaterial;
        int partID = itemMetaData % EnumMaterial.itemCountPerMaterial;
        if (matID < EnumMaterial.values().length && partID < EnumOrePart.values().length)
        {
            return EnumMaterial.values()[matID].simpleName + EnumOrePart.values()[partID].simpleName;
        }
        return "OrePart[" + matID + "][" + partID + "]";
    }
}
