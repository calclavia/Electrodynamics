package resonantinduction.core.recipe;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import resonantinduction.blocks.BlockBasalt;
import resonantinduction.core.misc.EnumTool;
import resonantinduction.core.resource.ItemOreDirv;
import resonantinduction.core.resource.ItemParts.Parts;
import resonantinduction.mechanics.processor.BlockProcessor;
import resonantinduction.transport.crate.BlockCrate;
import resonantinduction.transport.fluid.pipes.FluidPartsMaterial;

import com.builtbroken.minecraft.EnumMaterial;
import com.builtbroken.minecraft.EnumOrePart;
import com.builtbroken.minecraft.helpers.ColorCode;
import com.builtbroken.minecraft.interfaces.IToolReadOut.EnumTools;
import com.builtbroken.minecraft.recipes.MachineRecipeHandler;
import com.builtbroken.minecraft.recipes.ProcessorType;

import cpw.mods.fml.common.registry.GameRegistry;

public class RecipeLoader
{

	public static Block blockConveyorBelt;
	public static Block blockManipulator;
	public static BlockCrate blockCrate;
	public static Block blockImprinter;
	public static Block blockEncoder;
	public static Block blockDetector;
	public static Block blockRejector;
	public static Block blockArmbot;
	public static Block blockTurntable;
	public static Block processorMachine;
	public static Block blockAdvancedHopper;
	public static Block blockPipe;
	public static Block blockTank;
	public static Block blockPumpMachine;
	public static Block blockRod;
	public static Block blockGenerator;
	public static Block blockReleaseValve;
	public static Block blockSink;
	public static Block blockDrain;
	public static Block blockConPump;
	public static Block blockHeater;
	public static Block blockPiston;
	public static Block blockBoiler;
	public static Block blockWasteLiquid;
	public static Block blockOilLiquid;
	public static Block blockFuelLiquid;
	public static Block blockOre, blockDebug, blockWire;
	public static Block blockStainGlass;
	public static Block blockColorSand;
	public static Block blockBasalt;
	public static Block blockGlowGlass;
	public static Block blockSteamGen, blockSolar, blockBatBox;
	public static Block blockGas;
	public static Block frackingPipe;
	public static Block laserSentry = null;

	public static Item itemImprint;
	public static Item itemDisk;
	public static Item itemFluidCan;
	public static Item itemTool;
	public static Item itemParts;
	public static Item itemMetals;
	public static Item battery;
	public static Item wrench;
	public static Item itemGlowingSand;
	public static Item itemDiggingTool;
	public static Item itemVehicleTest;
	public static Item itemMPWire;

	private static RecipeLoader instance;

	public static final String COPPER_WIRE = "wireCopper";

	public static RecipeLoader instance()
	{
		if (instance == null)
		{
			instance = new RecipeLoader();
		}
		return instance;
	}

	public void loadRecipes()
	{
		this.setupAutomationRecipes();
		this.createUERecipes();

		this.registerPipes();
		this.registerTanks();

		if (blockPumpMachine != null)
			GameRegistry.addRecipe(new ShapedOreRecipe(blockPumpMachine, "IGI", "VMV", "IGI", 'G', EnumMaterial.IRON.getOreName(EnumOrePart.GEARS), 'I', EnumMaterial.IRON.getOreName(EnumOrePart.PLATES), 'V', Parts.Valve.name, 'M', "motor"));
		if (blockConPump != null)
			GameRegistry.addRecipe(new ShapedOreRecipe(blockConPump, "@G@", "GPG", "@G@", '@', EnumMaterial.STEEL.getOreName(EnumOrePart.PLATES), 'P', blockPumpMachine, 'G', EnumMaterial.STEEL.getOreName(EnumOrePart.GEARS)));
		if (blockDrain != null)
			GameRegistry.addRecipe(new ShapelessOreRecipe(blockDrain, Block.dispenser, Parts.Valve.name));
		if (blockReleaseValve != null)
			GameRegistry.addRecipe(new ShapedOreRecipe(blockReleaseValve, "RGR", "TVT", "RGR", 'G', EnumMaterial.IRON.getOreName(EnumOrePart.GEARS), 'T', EnumMaterial.IRON.getOreName(EnumOrePart.TUBE), 'V', Parts.Valve.name, 'R', Item.redstone));
		// sink
		// GameRegistry.addRecipe(new ItemStack(blockSink, 1), new Object[] { "I I", "SIS", "SPS",
		// 'P', new ItemStack(blockPipe, 1), 'I', Item.ingotIron, 'S', Block.stone });

		if (itemParts != null)
		{
			// seal
			GameRegistry.addRecipe(new ItemStack(itemParts, 16, Parts.Seal.ordinal()), new Object[] { "LL", "LL", 'L', Item.leather });
			// slime steal
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 4, Parts.GasSeal.ordinal()), " # ", "#S#", " # ", '#', Parts.Seal.name, 'S', Item.slimeBall));
			// part valve
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 1, Parts.Valve.ordinal()), "PLP", 'P', EnumMaterial.IRON.getOreName(EnumOrePart.TUBE), 'L', Block.lever));
			// Basic Circuit
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 1, Parts.CircuitBasic.ordinal()), "!#!", "#@#", "!#!", '@', EnumMaterial.COPPER.getOreName(EnumOrePart.PLATES), '#', Block.glass, '!', COPPER_WIRE));
			// Advanced Circuit
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 1, Parts.CircuitAdvanced.ordinal()), "!#!", "#@#", "!#!", '@', EnumMaterial.COPPER.getOreName(EnumOrePart.PLATES), '#', Item.redstone, '!', COPPER_WIRE));
			// Elite Circuit
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 1, Parts.CircuitElite.ordinal()), "!#!", "#@#", "!#!", '@', EnumMaterial.GOLD.getOreName(EnumOrePart.PLATES), '#', Item.redstone, '!', COPPER_WIRE));

			// unfinished tank
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 1, Parts.Tank.ordinal()), " # ", "# #", " # ", '#', EnumMaterial.BRONZE.getOreName(EnumOrePart.INGOTS)));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 1, Parts.Tank.ordinal()), " # ", "# #", " # ", '#', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS)));
			// Motor
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 1, Parts.Motor.ordinal()), "@!@", "!#!", "@!@", '!', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS), '#', Item.ingotIron, '@', Parts.COIL.name));
			// Laser Diode
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 4, Parts.LASER.ordinal()), " G ", "!S!", " C ", '!', COPPER_WIRE, 'G', Block.glass, 'S', Block.sand, 'C', Parts.CircuitBasic.name));
			// Coil
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemParts, 8, Parts.COIL.ordinal()), "WWW", "W W", "WWW", 'W', COPPER_WIRE));

		}
		if (itemMetals instanceof ItemOreDirv)
		{
			// Alt salvaging item list
			MachineRecipeHandler.newAltProcessorOutput(ProcessorType.GRINDER, Block.wood, EnumMaterial.getStack(itemMetals, EnumMaterial.WOOD, EnumOrePart.DUST, 3));
			MachineRecipeHandler.newAltProcessorOutput(ProcessorType.GRINDER, Block.planks, EnumMaterial.getStack(itemMetals, EnumMaterial.WOOD, EnumOrePart.DUST, 1));

			MachineRecipeHandler.newAltProcessorOutput(ProcessorType.CRUSHER, Block.wood, EnumMaterial.getStack(itemMetals, EnumMaterial.WOOD, EnumOrePart.SCRAPS, 3));
			MachineRecipeHandler.newAltProcessorOutput(ProcessorType.CRUSHER, Block.planks, EnumMaterial.getStack(itemMetals, EnumMaterial.WOOD, EnumOrePart.SCRAPS, 1));

			// Stone recipes
			MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, Block.stone, EnumMaterial.getStack(itemMetals, EnumMaterial.STONE, EnumOrePart.DUST, 1));

			// Wood recipes
			MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, Block.wood, EnumMaterial.getStack(itemMetals, EnumMaterial.WOOD, EnumOrePart.DUST, 3));
			MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, Block.planks, EnumMaterial.getStack(itemMetals, EnumMaterial.WOOD, EnumOrePart.DUST, 1));
			MachineRecipeHandler.newProcessorRecipe(ProcessorType.CRUSHER, Block.wood, EnumMaterial.getStack(itemMetals, EnumMaterial.WOOD, EnumOrePart.SCRAPS, 3));
			MachineRecipeHandler.newProcessorRecipe(ProcessorType.CRUSHER, Block.planks, EnumMaterial.getStack(itemMetals, EnumMaterial.WOOD, EnumOrePart.SCRAPS, 1));

			// Gold Recipes
			MachineRecipeHandler.newProcessorRecipe(ProcessorType.CRUSHER, Block.blockIron, EnumMaterial.getStack(itemMetals, EnumMaterial.IRON, EnumOrePart.SCRAPS, 8));
			MachineRecipeHandler.newProcessorRecipe(ProcessorType.CRUSHER, Block.oreIron, EnumMaterial.getStack(itemMetals, EnumMaterial.IRON, EnumOrePart.RUBBLE, 1));
			// Iron Recipes
			MachineRecipeHandler.newProcessorRecipe(ProcessorType.CRUSHER, Block.blockGold, EnumMaterial.getStack(itemMetals, EnumMaterial.GOLD, EnumOrePart.SCRAPS, 8));
			MachineRecipeHandler.newProcessorRecipe(ProcessorType.CRUSHER, Block.oreGold, EnumMaterial.getStack(itemMetals, EnumMaterial.GOLD, EnumOrePart.RUBBLE, 1));
			// Dust recipes
			GameRegistry.addShapelessRecipe(EnumMaterial.getStack(itemMetals, EnumMaterial.STEEL, EnumOrePart.DUST, 1), EnumMaterial.getStack(itemMetals, EnumMaterial.IRON, EnumOrePart.DUST, 1), new ItemStack(Item.coal, 1, 0), new ItemStack(Item.coal, 1, 0));
			GameRegistry.addShapelessRecipe(EnumMaterial.getStack(itemMetals, EnumMaterial.STEEL, EnumOrePart.DUST, 1), EnumMaterial.getStack(itemMetals, EnumMaterial.IRON, EnumOrePart.DUST, 1), new ItemStack(Item.coal, 1, 1), new ItemStack(Item.coal, 1, 1));
			GameRegistry.addShapelessRecipe(EnumMaterial.getStack(itemMetals, EnumMaterial.STEEL, EnumOrePart.DUST, 1), EnumMaterial.getStack(itemMetals, EnumMaterial.IRON, EnumOrePart.DUST, 1), new ItemStack(Item.coal, 1, 1), new ItemStack(Item.coal, 1, 0));
			GameRegistry.addShapelessRecipe(EnumMaterial.getStack(itemMetals, EnumMaterial.BRONZE, EnumOrePart.DUST, 4), EnumMaterial.getStack(itemMetals, EnumMaterial.COPPER, EnumOrePart.DUST, 1), EnumMaterial.getStack(itemMetals, EnumMaterial.COPPER, EnumOrePart.DUST, 1), EnumMaterial.getStack(itemMetals, EnumMaterial.COPPER, EnumOrePart.DUST, 1), EnumMaterial.getStack(itemMetals, EnumMaterial.TIN, EnumOrePart.DUST, 1));

			// Ore material recipe loop
			for (EnumMaterial mat : EnumMaterial.values())
			{
				// Dust recipes
				if (mat.shouldCreateItem(EnumOrePart.DUST))
				{
					FurnaceRecipes.smelting().addSmelting(mat.getStack(itemMetals, EnumOrePart.DUST, 1).itemID, mat.getStack(itemMetals, EnumOrePart.DUST, 1).getItemDamage(), mat.getStack(itemMetals, EnumOrePart.INGOTS, 1), 0.6f);
					MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, mat.getStack(itemMetals, EnumOrePart.RUBBLE, 1), mat.getStack(itemMetals, EnumOrePart.DUST, 1), 1, 4);
					MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, mat.getStack(itemMetals, EnumOrePart.SCRAPS, 1), mat.getStack(itemMetals, EnumOrePart.DUST, 1));
					MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, mat.getStack(itemMetals, EnumOrePart.INGOTS, 1), mat.getStack(itemMetals, EnumOrePart.DUST, 1));
					MachineRecipeHandler.newAltProcessorOutput(ProcessorType.GRINDER, mat.getStack(itemMetals, EnumOrePart.INGOTS, 1), mat.getStack(itemMetals, EnumOrePart.DUST, 1));
					MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, mat.getStack(itemMetals, EnumOrePart.PLATES, 1), mat.getStack(itemMetals, EnumOrePart.DUST, 1), 2, 4);
					MachineRecipeHandler.newAltProcessorOutput(ProcessorType.GRINDER, mat.getStack(itemMetals, EnumOrePart.PLATES, 1), mat.getStack(itemMetals, EnumOrePart.DUST, 3));
					MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, mat.getStack(itemMetals, EnumOrePart.ROD, 1), mat.getStack(itemMetals, EnumOrePart.DUST, 1));
					MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, mat.getStack(itemMetals, EnumOrePart.TUBE, 1), mat.getStack(itemMetals, EnumOrePart.DUST, 1));
				}

				// Salvaging recipe

				if (mat.shouldCreateItem(EnumOrePart.SCRAPS))
				{
					FurnaceRecipes.smelting().addSmelting(mat.getStack(itemMetals, EnumOrePart.SCRAPS, 1).itemID, mat.getStack(itemMetals, EnumOrePart.SCRAPS, 1).getItemDamage(), mat.getStack(itemMetals, EnumOrePart.INGOTS, 1), 0.6f);
					MachineRecipeHandler.newProcessorRecipe(ProcessorType.CRUSHER, mat.getStack(itemMetals, EnumOrePart.PLATES, 1), mat.getStack(itemMetals, EnumOrePart.SCRAPS, 3));
					MachineRecipeHandler.newAltProcessorOutput(ProcessorType.CRUSHER, mat.getStack(itemMetals, EnumOrePart.PLATES, 1), mat.getStack(itemMetals, EnumOrePart.SCRAPS, 3));
					MachineRecipeHandler.newProcessorRecipe(ProcessorType.CRUSHER, mat.getStack(itemMetals, EnumOrePart.RUBBLE, 1), mat.getStack(itemMetals, EnumOrePart.SCRAPS, 1), 1, 5);
					MachineRecipeHandler.newAltProcessorOutput(ProcessorType.CRUSHER, mat.getStack(itemMetals, EnumOrePart.INGOTS, 1), mat.getStack(itemMetals, EnumOrePart.SCRAPS, 1));
					MachineRecipeHandler.newProcessorRecipe(ProcessorType.CRUSHER, mat.getStack(itemMetals, EnumOrePart.ROD, 1), mat.getStack(itemMetals, EnumOrePart.SCRAPS, 1));
					MachineRecipeHandler.newProcessorRecipe(ProcessorType.CRUSHER, mat.getStack(itemMetals, EnumOrePart.TUBE, 1), mat.getStack(itemMetals, EnumOrePart.SCRAPS, 1));
				}
				if (mat.shouldCreateItem(EnumOrePart.TUBE))
				{
					GameRegistry.addRecipe(new ShapedOreRecipe(mat.getStack(itemMetals, EnumOrePart.TUBE, 6), "I", "I", "I", 'I', mat.getOreName(EnumOrePart.INGOTS)));
					GameRegistry.addRecipe(new ShapedOreRecipe(mat.getStack(itemMetals, EnumOrePart.TUBE, 1), "I", 'I', mat.getOreName(EnumOrePart.ROD)));
				}
				if (mat.shouldCreateItem(EnumOrePart.ROD))
				{
					GameRegistry.addRecipe(new ShapedOreRecipe(mat.getStack(itemMetals, EnumOrePart.ROD, 4), "I", "I", 'I', mat.getOreName(EnumOrePart.INGOTS)));
				}
				if (mat.shouldCreateItem(EnumOrePart.PLATES))
				{
					GameRegistry.addRecipe(new ShapedOreRecipe(mat.getStack(itemMetals, EnumOrePart.PLATES, 1), "II", "II", 'I', mat.getOreName(EnumOrePart.INGOTS)));
				}
				if (mat.shouldCreateItem(EnumOrePart.GEARS))
				{
					GameRegistry.addRecipe(new ShapedOreRecipe(mat.getStack(itemMetals, EnumOrePart.GEARS, 4), " I ", "IRI", " I ", 'I', mat.getOreName(EnumOrePart.INGOTS), 'R', mat.shouldCreateItem(EnumOrePart.ROD) ? mat.getOreName(EnumOrePart.ROD) : Item.stick));
				}

			}
		}
		if (itemTool != null)
		{
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemTool, 1, EnumTools.PIPE_GUAGE.ordinal()), "TVT", " T ", 'T', EnumMaterial.IRON.getOreName(EnumOrePart.TUBE), 'V', Parts.Valve.name));

			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemTool, 1, EnumTools.MULTI_METER.ordinal()), "PGP", "WCW", "PRP", 'P', Block.planks, 'G', Block.glass, 'C', Parts.CircuitBasic.name, 'R', COPPER_WIRE, 'W', Parts.COIL.name));
		}
		if (wrench != null)
		{
			GameRegistry.addRecipe(new ShapedOreRecipe(wrench, "S S", " S ", " S ", 'S', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS)));
		}
		if (blockSolar != null)
		{
			GameRegistry.addRecipe(new ShapedOreRecipe(blockSolar, "GGG", "SCS", "SWS", 'G', Block.glass, 'W', "copperWire", 'C', Parts.CircuitBasic.name, 'S', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS)));
		}
		if (blockWire != null)
		{
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockWire, 16, 0), "III", "WWW", "III", 'I', Block.cloth, 'W', EnumMaterial.COPPER.getOreName(EnumOrePart.INGOTS)));
		}
		if (itemDiggingTool != null)
		{

			for (EnumMaterial mat : EnumMaterial.values())
			{
				if (mat.shouldCreateTool())
				{
					GameRegistry.addRecipe(new ShapedOreRecipe(EnumTool.PICKAX.getTool(mat), "III", " S ", " S ", 'I', mat.getOreName(EnumOrePart.INGOTS), 'S', Item.stick));
					GameRegistry.addRecipe(new ShapedOreRecipe(EnumTool.HOE.getTool(mat), "II ", " S ", " S ", 'I', mat.getOreName(EnumOrePart.INGOTS), 'S', Item.stick));
					GameRegistry.addRecipe(new ShapedOreRecipe(EnumTool.SPADE.getTool(mat), " I ", " S ", " S ", 'I', mat.getOreName(EnumOrePart.INGOTS), 'S', Item.stick));
					GameRegistry.addRecipe(new ShapedOreRecipe(EnumTool.AX.getTool(mat), "II ", "IS ", " S ", 'I', mat.getOreName(EnumOrePart.INGOTS), 'S', Item.stick));
					// GameRegistry.addRecipe(new ShapedOreRecipe(mat.getTool(EnumTool.SHEAR),
					// "III", " S ", 'I', mat.getStack(EnumOrePart.INGOTS, 1)));
				}
			}
		}

		for (ColorCode code : ColorCode.values())
		{
			// Stained Glass //
			if (blockColorSand != null)
			{

				if (blockStainGlass != null)
				{
					FurnaceRecipes.smelting().addSmelting(blockColorSand.blockID, code.ordinal(), new ItemStack(blockStainGlass, 1, code.ordinal()), 10F);
					MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, new ItemStack(blockStainGlass, 1, code.ordinal()), new ItemStack(blockColorSand.blockID, 1, code.ordinal()));
				}
				GameRegistry.addShapelessRecipe(new ItemStack(blockColorSand.blockID, 1, code.ordinal()), new Object[] { new ItemStack(blockColorSand, 1, code.ordinal()) });
				MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, new ItemStack(blockColorSand, 1, code.ordinal()), new ItemStack(itemGlowingSand, 1, code.ordinal()));

			}

			// Glowing Glass //
			if (itemGlowingSand != null)
			{
				if (blockGlowGlass != null)
				{
					FurnaceRecipes.smelting().addSmelting(itemGlowingSand.itemID, code.ordinal(), new ItemStack(blockGlowGlass, 1, code.ordinal()), 10F);
					MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, new ItemStack(blockGlowGlass, 1, code.ordinal()), new ItemStack(itemGlowingSand, 1, code.ordinal()));
				}
				if (blockColorSand != null)
				{
					GameRegistry.addShapelessRecipe(new ItemStack(itemGlowingSand, 1, code.ordinal()), new ItemStack(blockColorSand.blockID, 1, code.ordinal()), Item.redstone);
					GameRegistry.addShapelessRecipe(new ItemStack(itemGlowingSand, 1, code.ordinal()), new ItemStack(blockColorSand.blockID, 1, code.ordinal()), Item.glowstone);
					MachineRecipeHandler.banProcessingOfItem(new ItemStack(itemGlowingSand, 1, code.ordinal()));
				}
			}

			// Colored Sand //
			if (blockColorSand != null)
			{
				GameRegistry.addRecipe(new ItemStack(blockColorSand, 8, code.ordinal()), "SSS", "SDS", "SSS", 'S', Block.sand, 'D', new ItemStack(Item.dyePowder, 1, code.ordinal()));

			}
		}
		// Extra Block //
		if (blockBasalt != null)
		{
			GameRegistry.addShapelessRecipe(new ItemStack(blockBasalt, 1, BlockBasalt.block.COBBLE.ordinal()), new ItemStack(blockBasalt, 1, BlockBasalt.block.STONE.ordinal()));
			GameRegistry.addShapelessRecipe(new ItemStack(blockBasalt, 1, BlockBasalt.block.MOSSY.ordinal()), new ItemStack(blockBasalt, 1, BlockBasalt.block.BRICK.ordinal()), Block.vine);
			GameRegistry.addShapelessRecipe(new ItemStack(blockBasalt, 2, BlockBasalt.block.CRACKED.ordinal()), new ItemStack(blockBasalt, 1, BlockBasalt.block.BRICK.ordinal()), new ItemStack(blockBasalt, 1, BlockBasalt.block.BRICK.ordinal()));
			GameRegistry.addRecipe(new ItemStack(blockBasalt, 4, BlockBasalt.block.BRICK.ordinal()), "SS", "SS", 'S', new ItemStack(blockBasalt, 1, BlockBasalt.block.STONE.ordinal()));
			GameRegistry.addRecipe(new ItemStack(blockBasalt, 8, BlockBasalt.block.CHISILED.ordinal()), "SSS", "S S", "SSS", 'S', new ItemStack(blockBasalt, 1, BlockBasalt.block.STONE.ordinal()));

			GameRegistry.addShapelessRecipe(new ItemStack(blockBasalt, 2, BlockBasalt.block.COBBLE.ordinal()), Block.cobblestone, new ItemStack(blockBasalt, 1, BlockBasalt.block.STONE.ordinal()));
			GameRegistry.addSmelting(Block.stone.blockID, new ItemStack(blockBasalt, 1, BlockBasalt.block.STONE.ordinal()), 1f);
		}
	}

	private void createUERecipes()
	{

	}

	private void setupAutomationRecipes()
	{
		// Imprint
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemImprint, 2), "RPI", 'P', Item.paper, 'R', Item.redstone, 'I', new ItemStack(Item.dyePowder, 1, 0)));
		// Imprinter
		GameRegistry.addRecipe(new ShapedOreRecipe(blockImprinter, "SSS", "GPG", "WCW", 'G', EnumMaterial.WOOD.getOreName(EnumOrePart.GEARS), 'S', Item.ingotIron, 'C', Block.chest, 'W', Block.workbench, 'P', Block.pistonBase));
		// Crate
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 0), "TST", "S S", "TST", 'S', Item.ingotIron, 'T', Block.planks));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 1), "SC", "SS", 'C', new ItemStack(blockCrate, 1, 0), 'S', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 1), "TST", "S S", "TST", 'S', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS), 'T', Block.wood));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 2), "SC", "SS", 'C', new ItemStack(blockCrate, 1, 1), 'S', EnumMaterial.STEEL.getOreName(EnumOrePart.PLATES)));
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCrate, 1, 2), "TST", "S S", "TST", 'S', EnumMaterial.STEEL.getOreName(EnumOrePart.PLATES), 'T', Block.wood));
		// Armbot
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockArmbot), "II ", "SIS", "MCM", 'S', EnumMaterial.STEEL.getOreName(EnumOrePart.PLATES), 'C', Parts.CircuitAdvanced.name, 'I', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS), 'M', Parts.Motor.name));
		// Disk
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemDisk), " I ", "ICI", " I ", 'I', Item.paper, 'C', EnumMaterial.COPPER.getOreName(EnumOrePart.INGOTS)));
		// Encoder
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockEncoder), "SPS", "SSS", "SCS", 'P', Block.pistonBase, 'S', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS), 'C', Parts.CircuitAdvanced.name));
		// Detector
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockDetector), "SES", "SCS", "S S", 'S', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS), 'C', Parts.CircuitBasic.name, 'E', Item.enderPearl));
		// Conveyor Belt
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockConveyorBelt, 10, 0), "III", "MCM", 'I', Item.ingotIron, 'C', Parts.CircuitBasic.name, 'M', Parts.Motor.name));
		// Rejector
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockRejector), "CPC", "@R@", '@', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS), 'R', Item.redstone, 'P', Block.pistonBase, 'C', Parts.CircuitBasic.name));
		// Turntable
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockTurntable), "IMI", "ICI", 'M', Parts.Motor.name, 'C', Parts.CircuitBasic.name, 'I', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS)));
		// Manipulator
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(blockManipulator, 2, 0), Block.dispenser, Parts.CircuitBasic.name));
		if (processorMachine != null)
		{
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(processorMachine, 1, BlockProcessor.ProcessorData.GRINDER.startMeta), "SSS", "PMP", "SCS", 'M', Parts.Motor.name, 'P', EnumMaterial.STEEL.getOreName(EnumOrePart.PLATES), 'S', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS), 'C', Parts.CircuitBasic.name));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(processorMachine, 1, BlockProcessor.ProcessorData.CRUSHER.startMeta), "SPS", "RPR", "SCS", 'R', Block.pistonBase, 'P', EnumMaterial.STEEL.getOreName(EnumOrePart.PLATES), 'S', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS), 'C', Parts.CircuitBasic.name));
		}
	}

	public void registerTanks()
	{
		if (blockTank != null)
		{
			GameRegistry.addRecipe(new ItemStack(blockTank, 1, FluidPartsMaterial.GLASS.getMeta()), "SXS", "X X", "SXS", 's', Item.silk, 'X', Block.glass);
			GameRegistry.addRecipe(new ItemStack(blockTank, 1, FluidPartsMaterial.WOOD.getMeta()), "LXL", "X X", "LXL", 'L', Block.wood, 'X', Block.glass);
			GameRegistry.addRecipe(new ItemStack(blockTank, 1, FluidPartsMaterial.STONE.getMeta()), "SXS", "X X", "SXS", 'S', Block.stone, 'X', Block.glass);
			GameRegistry.addRecipe(new ItemStack(blockTank, 1, FluidPartsMaterial.IRON.getMeta()), "IXI", "X X", "IXI", 'I', Item.ingotIron, 'X', Block.glass);

			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockTank, 1, FluidPartsMaterial.TIN.getMeta()), "IXI", "X X", "IXI", 'I', EnumMaterial.TIN.getOreName(EnumOrePart.INGOTS), 'X', Block.glass));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockTank, 1, FluidPartsMaterial.COPPER.getMeta()), "IXI", "X X", "IXI", 'I', EnumMaterial.COPPER.getOreName(EnumOrePart.INGOTS), 'X', Block.glass));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockTank, 1, FluidPartsMaterial.BRONZE.getMeta()), "IXI", "X X", "IXI", 'I', EnumMaterial.BRONZE.getOreName(EnumOrePart.INGOTS), 'X', Block.glass));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockTank, 1, FluidPartsMaterial.GOLD.getMeta()), "IXI", "X X", "IXI", 'I', Item.ingotGold, 'X', Block.glass));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockTank, 1, FluidPartsMaterial.STEEL.getMeta()), "IXI", "X X", "IXI", 'I', EnumMaterial.STEEL.getOreName(EnumOrePart.INGOTS), 'X', Block.glass));

			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockTank, 1, FluidPartsMaterial.OBBY.getMeta()), "IXI", "XEX", "IXI", 'I', Block.obsidian, 'X', Block.glass, 'E', Item.enderPearl));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockTank, 1, FluidPartsMaterial.HELL.getMeta()), "IXI", "XBX", "IXI", 'I', Block.obsidian, 'X', Block.netherBrick, 'B', Item.blazePowder));
		}
	}

	public void registerPipes()
	{
		if (blockPipe != null)
		{
			GameRegistry.addRecipe(FluidPartsMaterial.WOOD.getStack(2), "LLL", "WWW", "LLL", 'W', Block.planks, 'L', Block.wood);
			GameRegistry.addRecipe(FluidPartsMaterial.STONE.getStack(2), "SSS", "CCC", "SSS", 'S', Block.stone, 'C', Block.cobblestone);
			GameRegistry.addRecipe(new ShapelessOreRecipe(FluidPartsMaterial.IRON.getStack(2), EnumMaterial.IRON.getOreName(EnumOrePart.TUBE), Parts.Seal.name));
			GameRegistry.addRecipe(new ShapelessOreRecipe(FluidPartsMaterial.GOLD.getStack(2), EnumMaterial.GOLD.getOreName(EnumOrePart.TUBE), Parts.Seal.name));
			GameRegistry.addRecipe(new ShapelessOreRecipe(FluidPartsMaterial.TIN.getStack(2), EnumMaterial.TIN.getOreName(EnumOrePart.TUBE), Parts.Seal.name));
			GameRegistry.addRecipe(new ShapelessOreRecipe(FluidPartsMaterial.COPPER.getStack(2), EnumMaterial.COPPER.getOreName(EnumOrePart.TUBE), Parts.Seal.name));
			GameRegistry.addRecipe(new ShapelessOreRecipe(FluidPartsMaterial.BRONZE.getStack(2), EnumMaterial.BRONZE.getOreName(EnumOrePart.TUBE), Parts.Seal.name));
			GameRegistry.addRecipe(new ShapelessOreRecipe(FluidPartsMaterial.STEEL.getStack(2), EnumMaterial.STEEL.getOreName(EnumOrePart.TUBE), Parts.Seal.name));
			GameRegistry.addRecipe(new ShapelessOreRecipe(FluidPartsMaterial.OBBY.getStack(2), new Object[] { EnumMaterial.OBBY.getOreName(EnumOrePart.TUBE), Block.netherBrick, Block.netherBrick }));
			GameRegistry.addRecipe(new ShapedOreRecipe(FluidPartsMaterial.HELL.getStack(4), new Object[] { "OOO", "BNB", "OOO", 'N', Block.netherBrick, 'B', Item.blazeRod, 'O', Block.obsidian }));

			for (FluidPartsMaterial mat : FluidPartsMaterial.values())
			{
				for (ColorCode color : ColorCode.values())
				{
					GameRegistry.addRecipe(mat.getStack(color), new Object[] { " X ", "XIX", " X ", 'I', new ItemStack(Item.dyePowder, 1, color.ordinal()), 'X', blockPipe });
					GameRegistry.addShapelessRecipe(mat.getStack(), new Object[] { mat.getStack(color) });
				}
			}
		}
	}

	public static void parseOreNames(Configuration config)
	{
		if (config.get("Ore", "processOreDictionary", true, "Scans the ore dictionary and adds recipes for other mod's items").getBoolean(true))
		{
			for (EnumMaterial mat : EnumMaterial.values())
			{
				List<ItemStack> ingots = OreDictionary.getOres(mat.getOreName(EnumOrePart.INGOTS));
				List<ItemStack> plates = OreDictionary.getOres(mat.getOreName(EnumOrePart.PLATES));
				List<ItemStack> dusts = OreDictionary.getOres(mat.getOreName(EnumOrePart.DUST));
				List<ItemStack> ores = OreDictionary.getOres("ore" + mat.simpleName);
				if (config.get("Ore", "OverrideDustSmelthing", true, "Forces all mods to use the same ingot for dust smelting").getBoolean(true))
				{
					for (ItemStack du : dusts)
					{
						if (mat.shouldCreateItem(EnumOrePart.INGOTS))
						{
							FurnaceRecipes.smelting().addSmelting(du.itemID, du.getItemDamage(), mat.getStack(itemMetals, EnumOrePart.INGOTS, 1), 0.6f);
						}
					}
				}

				for (ItemStack ing : ingots)
				{
					if (mat.shouldCreateItem(EnumOrePart.DUST))
					{
						MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, ing, mat.getStack(itemMetals, EnumOrePart.DUST, 1));
						MachineRecipeHandler.newAltProcessorOutput(ProcessorType.GRINDER, ing, mat.getStack(itemMetals, EnumOrePart.DUST, 1));
					}
					if (mat.shouldCreateItem(EnumOrePart.SCRAPS))
					{
						MachineRecipeHandler.newAltProcessorOutput(ProcessorType.CRUSHER, ing, mat.getStack(itemMetals, EnumOrePart.SCRAPS, 1));
						MachineRecipeHandler.newProcessorRecipe(ProcessorType.CRUSHER, ing, mat.getStack(itemMetals, EnumOrePart.SCRAPS, 1));
					}
					if (mat.shouldCreateItem(EnumOrePart.INGOTS) && config.get("Ore", "AddIngotConversionRecipe", true, "Creates a recipe to convert other mods ingots into AL ingots").getBoolean(true))
					{
						GameRegistry.addShapelessRecipe(mat.getStack(itemMetals, EnumOrePart.INGOTS, 1), new Object[] { ing });
					}
				}
				for (ItemStack pla : plates)
				{
					if (mat.shouldCreateItem(EnumOrePart.DUST))
					{

						MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, pla, mat.getStack(itemMetals, EnumOrePart.DUST, 1));
						MachineRecipeHandler.newAltProcessorOutput(ProcessorType.GRINDER, pla, mat.getStack(itemMetals, EnumOrePart.DUST, 1));

					}
					if (mat.shouldCreateItem(EnumOrePart.SCRAPS))
					{

						MachineRecipeHandler.newProcessorRecipe(ProcessorType.CRUSHER, pla, mat.getStack(itemMetals, EnumOrePart.SCRAPS, 1));
						MachineRecipeHandler.newAltProcessorOutput(ProcessorType.CRUSHER, pla, mat.getStack(itemMetals, EnumOrePart.SCRAPS, 1));

					}
				}
				for (ItemStack ore : ores)
				{
					if (mat.shouldCreateItem(EnumOrePart.RUBBLE))
					{
						MachineRecipeHandler.newProcessorRecipe(ProcessorType.CRUSHER, ore, mat.getStack(itemMetals, EnumOrePart.RUBBLE, 1), 1, 2);
					}
					if (mat.shouldCreateItem(EnumOrePart.DUST))
					{
						MachineRecipeHandler.newProcessorRecipe(ProcessorType.GRINDER, ore, mat.getStack(itemMetals, EnumOrePart.DUST, 1), 1, 3);
					}
					if (mat.shouldCreateItem(EnumOrePart.INGOTS) && config.get("Ore", "OverrideOreSmelthing", true, "Forces other mods ores to smelt as one ingot").getBoolean(true))
					{
						FurnaceRecipes.smelting().addSmelting(ore.itemID, ore.getItemDamage(), mat.getStack(itemMetals, EnumOrePart.INGOTS, 1), 0.6f);
					}
				}

			}
		}
	}
}
