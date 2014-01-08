package resonantinduction.fluid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import resonantinduction.fluid.network.IFluidRecipeCrafter;
import resonantinduction.fluid.network.FluidRecipeInfo.SimpleFluidRecipe;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.builtbroken.common.Pair;
import com.builtbroken.common.Triple;
import com.builtbroken.minecraft.FluidHelper;

/** Handles all kinds of process involving mixing Fluids with other fluids and/or Items, Blocks,
 * ItemStack, or Liquids
 * 
 * @author DarkGuardsman */
public class FluidCraftingHandler
{
    /** Map of results of two different liquids merging */
    public static HashMap<Pair<Object, Object>, Object> fluidMergeResults = new HashMap<Pair<Object, Object>, Object>();

    static
    {
        registerRecipe(FluidRegistry.LAVA, FluidRegistry.WATER, Block.obsidian);
        registerRecipe(FluidRegistry.WATER, FluidRegistry.LAVA, Block.cobblestone);
    }

    /** Creates a very basic A + B = C result for mixing two objects together. Suggest that the use
     * of a SimpleFluidRecipe i used instead to create a more refined fluid mixing that takes into
     * account ratios, and fluid volumes */
    public static void registerRecipe(Object a, Object b, Object c)
    {
        if (a != null && b != null && c != null)
        {
            registerFluidRecipe(new SimpleFluidRecipe(a, b, c));
        }
    }

    public static void registerFluidRecipe(SimpleFluidRecipe recipe)
    {
        if (recipe != null && recipe.recipeObjectA != null && recipe.recipeObjectB != null && recipe.recipeObjectC != null)
        {
            if (!fluidMergeResults.containsKey(new Pair<Object, Object>(recipe.recipeObjectA, recipe.recipeObjectB)))
            {
                fluidMergeResults.put(new Pair<Object, Object>(recipe.recipeObjectA, recipe.recipeObjectB), recipe);
            }
            if (recipe.canBeReversed)
            {
                if (!fluidMergeResults.containsKey(new Pair<Object, Object>(recipe.recipeObjectB, recipe.recipeObjectA)))
                {
                    fluidMergeResults.put(new Pair<Object, Object>(recipe.recipeObjectB, recipe.recipeObjectA), recipe);
                }
            }
        }
    }

    public static void loadPotionRecipes()
    {
        //TODO load the process by which a potion would be created threw fliud crafting
    }

    /** Does the fluid recipe crafting for the crafter object. Requires that the object fully use all
     * methods from the #IFluidRecipeCrafter
     * 
     * @param crafter - crafting object, recommend it be a tile but can be anything as long as the
     * method are used correctly. In some recipe cases when the setRecipeObjectContent nothing will
     * be used. If result is null assume not crafting was performed. If there is a result the
     * crafter couldn't use the output to reduce the input values. From here the IFluidRecipeCrafter
     * will need to process the output and decress the input values correctly */
    public static void craft(IFluidRecipeCrafter crafter)
    {
        Object received = crafter.getReceivingObjectStack();
        int receivedVolume = 0;
        Object input = crafter.getInputObjectStack();
        int inputVolume = 0;
        if (crafter != null && received != null && input != null)
        {
            //Trip input values so they will match the correct mapped values
            if (received instanceof FluidStack)
            {
                receivedVolume = ((FluidStack) received).amount;
                received = FluidHelper.getStack((FluidStack) received, 1);
            }
            if (received instanceof ItemStack)
            {
                receivedVolume = ((ItemStack) received).stackSize;
                ((ItemStack) received).stackSize = 1;
            }
            if (input instanceof FluidStack)
            {
                inputVolume = ((FluidStack) input).amount;
                input = FluidHelper.getStack((FluidStack) input, 1);
            }
            if (input instanceof ItemStack)
            {
                receivedVolume = ((ItemStack) input).stackSize;
                ((ItemStack) input).stackSize = 1;
            }

            //Get result
            Object result = fluidMergeResults.containsKey(new Pair<Object, Object>(crafter.getReceivingObjectStack(), crafter.getInputObjectStack()));

            //reset stack sized
            if (received instanceof FluidStack)
            {
                ((FluidStack) received).amount = receivedVolume;
            }
            if (received instanceof ItemStack)
            {
                ((ItemStack) received).stackSize = receivedVolume;
            }
            if (input instanceof FluidStack)
            {
                ((FluidStack) input).amount = inputVolume;
            }
            if (input instanceof ItemStack)
            {
                ((ItemStack) input).stackSize = inputVolume;
            }
            if (result != null)
            {
                if (result instanceof SimpleFluidRecipe)
                {
                    Triple<Integer, Integer, Pair<Object, Integer>> re = ((SimpleFluidRecipe) result).mix(crafter.getInputObjectStack(), crafter.getInputObjectStack());
                    crafter.setRecipeObjectContent(received, re.getA(), input, re.getB(), re.getC().left(), re.getC().right());
                }
            }
            crafter.setRecipeObjectContent(received, 0, input, 0, result, 0);
        }
    }

    /** Merges two fluids together that don't result in damage to the network */
    public static FluidStack mergeFluidStacks(FluidStack stackOne, FluidStack stackTwo)
    {
        FluidStack resultStack = null;

        if (stackTwo != null && stackOne != null && stackOne.isFluidEqual(stackTwo))
        {
            resultStack = stackOne.copy();
            resultStack.amount += stackTwo.amount;
        }
        else if (stackOne == null && stackTwo != null)
        {
            resultStack = stackTwo.copy();
        }
        else if (stackOne != null && stackTwo == null)
        {
            resultStack = stackOne.copy();
        }
        else if (stackTwo != null && stackOne != null && !stackOne.isFluidEqual(stackTwo))
        {
            System.out.println("preforming fluid merge event");
            Object result = fluidMergeResults.get(new Pair<Fluid, Fluid>(stackOne.getFluid(), stackTwo.getFluid()));
            /* Try to merge fluids by mod defined rules first */
            if (result != null)
            {
                System.out.println("result = " + result.toString());
                if (result instanceof Fluid)
                {
                    resultStack = new FluidStack(((Fluid) result).getID(), stackOne.amount + stackTwo.amount);
                }
                else if (result instanceof FluidStack)
                {
                    resultStack = ((FluidStack) result).copy();
                    resultStack.amount = stackOne.amount + stackTwo.amount;
                }
                else if (result instanceof String && ((String) result).startsWith("Liquid:"))
                {
                    resultStack = new FluidStack(FluidRegistry.getFluid(((String) result).replace("Liquid:", "")), stackOne.amount + stackTwo.amount);
                }
                else if (result instanceof SimpleFluidRecipe)
                {
                    Triple<Integer, Integer, Pair<Object, Integer>> re = ((SimpleFluidRecipe) result).mix(stackOne, stackTwo);
                    if (re.getC().left() instanceof FluidStack)
                    {
                        resultStack = FluidHelper.getStack((FluidStack) re.getC().left(), re.getC().right());
                    }
                    else if (re.getC().left() instanceof FluidStack)
                    {
                        resultStack = new FluidStack((Fluid) re.getC().left(), re.getC().right());
                    }
                }
            }
            if (resultStack == null)
            {
                System.out.println("Merging fluids into a waste fluid stack");
                Fluid waste = FluidRegistry.getFluid("waste");
                if (waste == null)
                {
                    System.out.println("[FluidNetworkHelper] Attempted to merge two fluids into a waste fluid stack but Forge fluid registry return null for waste. Possible that waste fluid was disabled or not registered correctly.");
                    return null;
                }
                /* If both liquids are waste then copy fluidStack lists then merge */
                if (stackTwo.fluidID == waste.getID() && stackOne.fluidID == waste.getID())
                {
                    List<FluidStack> stacks = new ArrayList<FluidStack>();
                    stacks.addAll(getStacksFromWaste(stackOne.copy()));
                    stacks.addAll(getStacksFromWaste(stackTwo.copy()));
                    resultStack = createNewWasteStack(stacks.toArray(new FluidStack[stacks.size()]));
                }
                else
                {
                    resultStack = createNewWasteStack(stackOne.copy(), stackTwo.copy());
                }
            }
        }
        return resultStack;
    }

    /** Gets the fluidStacks that make up a waste FluidStack */
    public static List<FluidStack> getStacksFromWaste(FluidStack wasteStack)
    {
        List<FluidStack> stacks = new ArrayList<FluidStack>();
        if (wasteStack.fluidID == FluidRegistry.getFluidID("waste"))
        {
            for (int i = 1; i <= wasteStack.tag.getInteger("liquids"); i++)
            {
                FluidStack readStack = FluidStack.loadFluidStackFromNBT(wasteStack.tag.getCompoundTag("Liquid" + i));
                if (readStack != null)
                {
                    stacks.add(readStack);
                }
            }
        }
        return stacks;
    }

    /** Creates a new waste stack from the listed fluidStacks */
    public static FluidStack createNewWasteStack(FluidStack... liquids)
    {
        FluidStack stack = new FluidStack(FluidRegistry.getFluid("waste"), 0);
        stack.tag = new NBTTagCompound();
        if (liquids != null)
        {
            int count = 0;
            for (int i = 0; i < liquids.length; i++)
            {
                if (liquids[i] != null)
                {
                    if (!liquids[i].getFluid().equals(stack.getFluid()))
                    {
                        count++;
                        stack.tag.setCompoundTag("Liquids" + count, liquids[i].writeToNBT(new NBTTagCompound()));
                        stack.amount += liquids[i].amount;
                    }
                    else
                    {
                        for (FluidStack loadStack : getStacksFromWaste(liquids[i]))
                        {
                            count++;
                            stack.tag.setCompoundTag("Liquids" + count, loadStack.writeToNBT(new NBTTagCompound()));
                            stack.amount += loadStack.amount;
                        }
                    }
                }
            }
            stack.tag.setInteger("liquids", count);
        }
        return stack;
    }

    /** Gets the result of the merge of the two fluids, order of merge does matter and will produce
     * different results.
     * 
     * @param stackOne - Receiving fluid, eg the one that is not moving
     * @param stackTwo - Flowing fluid, eg the one moving into the first fluid
     * @return Object result of the merge, can be anything from string, ItemStack, Item, Block, or
     * enum action */
    public static Object getMergeResult(FluidStack stackOne, FluidStack stackTwo)
    {
        FluidStack sampleStackOne, sampleStackTwo;
        if (stackOne != null && stackTwo != null && !stackOne.equals(stackTwo))
        {
            sampleStackOne = FluidHelper.getStack(stackOne, 1);
            sampleStackTwo = FluidHelper.getStack(stackTwo, 1);
            if (fluidMergeResults.containsKey(new Pair<Object, Object>(sampleStackOne, sampleStackTwo)))
            {
                return fluidMergeResults.get(new Pair<Object, Object>(sampleStackOne, sampleStackTwo));
            }
        }
        return null;
    }
}
