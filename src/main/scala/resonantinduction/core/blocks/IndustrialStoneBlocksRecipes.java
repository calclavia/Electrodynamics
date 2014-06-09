package resonantinduction.core.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import resonantinduction.core.ResonantInduction;
import cpw.mods.fml.common.registry.GameRegistry;



public class IndustrialStoneBlocksRecipes  {

public static void init () {
	registerRecipes();
		
	}
	

	public static void registerRecipes() {
		// Industrial Cobblestone
		GameRegistry.addRecipe(new ItemStack(ResonantInduction.blockIndustrialStone, 8, 3),
			new Object[] { "XXX", "XCX", "XXX", 'X', Block.cobblestone, 'C', new ItemStack (Item.coal, 1, 1)});
		// Industrial Stone
		FurnaceRecipes.smelting().addSmelting(ResonantInduction.blockIndustrialStone.blockID,3, new ItemStack(ResonantInduction.blockIndustrialStone, 1, 5), 5);
		// Industrial Cracked Stone 
		FurnaceRecipes.smelting().addSmelting(ResonantInduction.blockIndustrialStone.blockID, new ItemStack(ResonantInduction.blockIndustrialStone, 1, 4), 5);
		// Industrial Mossy Stone
		GameRegistry.addRecipe(new ItemStack(ResonantInduction.blockIndustrialStone, 8, 7),
			new Object[] { "XXX", "XVX", "XXX", 'X', new ItemStack(ResonantInduction.blockIndustrialStone), 'V', Block.vine});
		// Industrial Brick
		GameRegistry.addRecipe(new ItemStack(ResonantInduction.blockIndustrialStone, 4),
			new Object[] { "XX ", "XX ", "   ", 'X', new ItemStack(ResonantInduction.blockIndustrialStone, 1, 5)});
		// Industrial Double Slab Brick
		GameRegistry.addRecipe(new ItemStack(ResonantInduction.blockIndustrialStone, 4, 1),
			new Object[] { "XXX", "XXX", "XX ", 'X', Block.stoneSingleSlab});
		// Industrial Chiseled Brick
		GameRegistry.addRecipe(new ItemStack(ResonantInduction.blockIndustrialStone,8, 2),
			new Object[] { "XXX", "X X", "XXX", 'X', new ItemStack(ResonantInduction.blockIndustrialStone, 1, 5)});
		// Dark Iron Block Recipe
		GameRegistry.addRecipe(new ItemStack(ResonantInduction.blockIndustrialStone, 5, 10),
			new Object[] { "IXI", "XXX", "IXI", 'X', new ItemStack(ResonantInduction.blockIndustrialStone, 1, 5), 'I', Item.ingotIron});
		
		// Tinted Steel  Block Recipe
		/*GameRegistry.addRecipe(new ItemStack(ResonantInduction.blockDarkStone, 4, 9),
			new Object[] { "XSX", "SSS", "XSX", 'X', new ItemStack(ResonantInduction.blockDarkStone, 1, 5), 'S', new ItemStack(, ResonantEngine.idItemIngotSteel)});*/
		
		// Steel Block Recipe
		/*GameRegistry.addRecipe(new ItemStack(ResonantInduction.blockDarkStone,5, 8),
			new Object[] { "SXS", "XXX", "SXS", 'X', new ItemStack(ResonantInduction.blockDarkStone, 1, 5), 'S', new ItemStack(, ResonantEngine.idItemIngotSteel)});*/
		

	}

	
}
