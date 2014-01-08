package resonantinduction.assemblyline.fluid.network;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.builtbroken.common.Pair;
import com.builtbroken.common.Triple;

/** Used to store more complex info, than A + B = C, on two FluidStack mixing behavior
 * 
 * @author DarkGuardsman */
public class FluidRecipeInfo
{
    /** A + Energy = C, simple recipe designed to tell a boiler like machine how to handle input to
     * output process */
    public static class BoilingFluidRecipe
    {
        /** Unboiled object */
        public Object boiledObject;
        /** Boiled object */
        public Object boiledResult;
        /** In kelvin tempature units only */
        public float heatLevel = 0;
        /** Energy in jouls need to turn convert A to B */
        public float energyPerMb = 1;

        public BoilingFluidRecipe(Object unboiled, Object boiled, float boilingTempature, float energyPerUnitBoiled)
        {
            this.boiledObject = unboiled;
            this.boiledResult = boiled;
            this.heatLevel = boilingTempature;
            this.energyPerMb = energyPerUnitBoiled;
        }

        @Override
        public String toString()
        {
            return "[BoilingFluidRecipe] UnboiledObject: " + (this.boiledObject != null ? this.boiledObject.toString() : "null") + " | BoiledObject: " + (this.boiledResult != null ? this.boiledResult.toString() : "null") + " | BoilingTemp: " + this.heatLevel + "k | EnergyPerUnit: " + this.energyPerMb + "j";
        }
    }

    /** Basic A + B = C recipe result that should involve fluids but can be used as a 2 item crafting
     * system if needed */
    public static class SimpleFluidRecipe
    {
        public Object recipeObjectA, recipeObjectB, recipeObjectC;
        public int ratioOfA = 1, ratioOfB = 1, ratioOfC = 1;
        /** Size compared to the largest volume that the smallest volume can be */
        public float mixingPercentMin = .1f;
        public boolean canBeReversed = false;

        /** receiving & input object must be either be an instance of a class extending Item,
         * ItemStack, Block, Fluid, FluidStack, or OreNames. Anything else and the mixing will never
         * work
         * 
         * @param receiving - receiving object that is waiting to be mixed
         * @param input - object being added to the receiving object
         * @param output - result of mixing the object together. Can be anything but not all
         * machines using this will respect all output types */
        public SimpleFluidRecipe(Object receiving, Object input, Object output)
        {
            this.recipeObjectA = receiving;
            this.recipeObjectB = input;
            this.recipeObjectC = output;
        }

        public SimpleFluidRecipe setRatio(int receivingVolume, int inputVolume, int result)
        {
            this.ratioOfA = receivingVolume;
            this.ratioOfB = inputVolume;
            this.ratioOfC = result;
            return this;
        }

        public SimpleFluidRecipe setIsReversable(boolean canBeReversed)
        {
            this.canBeReversed = canBeReversed;
            return this;
        }

        public Object getResult()
        {
            return this.recipeObjectC;
        }

        /** Can the mixing be complete in anyway. Does a basic volume check but does not check for
         * volume wasted in mixing
         * 
         * @param receiving - Object stored and waiting for mixing
         * @param input - Object being added to the receiving object
         * @return true if the process can be completed */
        public boolean canComplete(Object receiving, Object input)
        {
            int countReceiving = 0;
            int countInput = 0;
            float percent = 0;
            if (receiving != null && input != null && recipeObjectA.equals(receiving) && recipeObjectB.equals(input))
            {
                countReceiving = this.getObjectVolume(receiving);
                countInput = this.getObjectVolume(input);
                if (countReceiving > 0 && countInput > 0)
                {
                    float per = countInput / countReceiving;
                    float per2 = countReceiving / countInput;
                    percent = per > per2 ? per2 : per;
                    if (percent >= this.mixingPercentMin)
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        public int getObjectVolume(Object object)
        {
            int volume = 0;
            if (object instanceof Item)
            {
                volume = 1;
            }
            else if (object instanceof ItemStack)
            {
                volume = ((ItemStack) object).stackSize;
            }
            else if (object instanceof FluidStack)
            {
                volume = ((FluidStack) object).amount;
            }
            else if (object instanceof Fluid)
            {
                volume = 1;
            }

            return volume;
        }

        /** @param receiving - Object receiving an input object for mixing
         * @param input - Object being added to the receiving object
         * @return Triple containing values of mixing. Complex way to handle it, and may be replaced
         * later, However to prevent 4 different methods be created for mixing this is the best
         * output design. As well this doesn't consume the object but does the calculations of the
         * recipe out at the given object volumes
         * 
         * First value is amount of the first object used. Second value is the amount of the second
         * object used. Third value Pair containing object output then amount of output */
        public Triple<Integer, Integer, Pair<Object, Integer>> mix(Object receiving, Object input)
        {
            if (this.canComplete(receiving, input))
            {
                //Collect volume of each input object
                int volumeReceiving = this.getObjectVolume(receiving);
                int volumeInput = this.getObjectVolume(input);
                int volAUsed, volBUsed;

                //check if there is enough to mix even once
                if (volumeReceiving > this.ratioOfA && volumeInput > this.ratioOfB)
                {
                    //Collect ratio of each
                    int ratioA = (volumeReceiving / this.ratioOfA);
                    int ratioB = (volumeInput / this.ratioOfB);

                    //Take the least ratio value and multiply it by the ratio of the output
                    int outputVolume = ratioA > ratioB ? ratioB * this.ratioOfC : ratioA * this.ratioOfC;

                    volAUsed = (outputVolume / this.ratioOfC) * this.ratioOfA;
                    volBUsed = (outputVolume / this.ratioOfC) * this.ratioOfB;
                    return new Triple<Integer, Integer, Pair<Object, Integer>>(volAUsed, volBUsed, new Pair<Object, Integer>(this.recipeObjectC, outputVolume));
                }

            }
            return null;
        }
    }

    /** Stores the list of processes needed to complete a fluid recipe that require more than one
     * step to complete. Only used by brewing factories, and is suggest too still register result as
     * a SimpleFluidRecipe unless the result can't be stored or moved easily. */
    public static class ComplexFluidRecipe
    {
        public int numberOfSteps;
        public SimpleFluidRecipe[] stepArray;

        public ComplexFluidRecipe(int numberOfSteps)
        {
            this.numberOfSteps = numberOfSteps;
            this.stepArray = new SimpleFluidRecipe[this.numberOfSteps];
        }

        public ComplexFluidRecipe createStep(int step, SimpleFluidRecipe stepRecipe)
        {
            if (step < numberOfSteps)
            {
                stepArray[step] = stepRecipe;
            }
            return this;
        }

        public boolean canCompleteStep(int step, Object receiving, Object input)
        {
            if (this.getStep(step) != null)
            {
                return this.getStep(step).canComplete(receiving, input);
            }
            return false;
        }

        public SimpleFluidRecipe getStep(int step)
        {
            if (step < numberOfSteps)
            {
                return stepArray[step];
            }
            return null;
        }
    }
}
