package com.builtbroken.minecraft.recipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.builtbroken.common.Pair;

/**
 * Enum of machines that support a simple A -> B processor recipe format. More complex machine will
 * have there own recipe handlers
 * 
 * @author Darkguardsman
 */
public enum ProcessorType
{
	/** Pistons that smash the object */
	CRUSHER(),
	/** Several disks that shred the item up */
	GRINDER(),
	/** Grinds the edge or surface of the item sharpening it */
	SHARPENING_STONE(),
	/** Breaks down an item carefully giving an almost complete output of item used to craft it */
	SALVAGER();
	public HashMap<Pair<Integer, Integer>, ProcessorRecipe> recipes = new HashMap();
	public HashMap<Pair<Integer, Integer>, ItemStack> altOutput = new HashMap();
	public List<Pair<Integer, Integer>> banList = new ArrayList();
}
