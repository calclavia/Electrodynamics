package com.builtbroken.minecraft;

public enum RecipeNames
{
    INGOT_COPPER("ingotCopper"),
    INGOT_TIN("ingotTin"),
    INGOT_BRONZE("ingotBronze"),
    INGOT_STEEL("ingotSteel"),
    PLATE_COPPER("plateCopper"),
    PLATE_TIN("plateTin"),
    PLATE_BRONZE("plateBronze"),
    PLATE_STEEL("plateSteel"),
    PLATE_IRON("plateIron"),
    PLATE_GOLD("plateGold"),
    BASIC_CIRCUIT("basicCircuit"),
    ADVANCED_CIRCUIT("advancedCircuit"),
    ELITE_CIRCUIT("eliteCircuit"),
    COPPER_WIRE("wireCopper"),
    MOTOR("motor"),
    COIL_COPPER("coilCopper");

    public String name;

    private RecipeNames(String orename)
    {
        this.name = orename;
    }
}
