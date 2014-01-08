package resonantinduction.api.fluid;

import java.util.HashMap;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.builtbroken.common.Pair;

public class FluidMasterList
{
    public static HashMap<String, Float> moltenFluids = new HashMap();
    /** Map containing items to FluidStack for melting down. Anything not in the list will be turned
     * into slag. */
    public static HashMap<Pair<Integer, Integer>, FluidStack> meltDownMap = new HashMap();

    public static final Fluid WATER = FluidRegistry.WATER;
    public static final Fluid LAVA = FluidRegistry.LAVA;

    static
    {
        //http://www.engineeringtoolbox.com/melting-temperature-metals-d_860.html
        moltenFluids.put("lava", 1200f);
        moltenFluids.put("molten-iron", 1200f);
        moltenFluids.put("molten-gold", 1063f);
        moltenFluids.put("molten-silver", 1000f);
    }

    /** Registers a fluid, by fluid name, as a molten fluids so pipes will interact with it different
     * 
     * @param name - fluid name
     * @param heatValue - temperature of the fluid */
    public static void registerMoltenFluid(String name, float heatValue)
    {
        if (name != null && heatValue > 0)
        {
            moltenFluids.put(name, heatValue);
        }
    }

    /** Try to only register very simple items as a reverse recipe system will be used to get to the
     * items used to craft the object
     * 
     * @param id - item id
     * @param meta - item meta
     * @param stack - fluid stack to return */
    public static void registerMeltDown(int id, int meta, FluidStack stack)
    {
        if (id > 0 && stack != null)
        {
            meltDownMap.put(new Pair<Integer, Integer>(id, meta), stack);
        }
    }

    public static boolean isMolten(Fluid fluid)
    {
        return fluid != null && moltenFluids.containsKey(fluid.getName());
    }

    public static float getHeatPerPass(Fluid fluid)
    {
        return moltenFluids.get(fluid.getName());
    }
}
