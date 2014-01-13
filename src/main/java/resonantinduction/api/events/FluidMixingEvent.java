package resonantinduction.api.events;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidStack;
import universalelectricity.api.vector.Vector3;

public class FluidMixingEvent extends FluidEvent
{

	public FluidMixingEvent(FluidStack fluid, World world, Vector3 vec)
	{
		super(fluid, world, vec.intX(), vec.intY(), vec.intZ());
	}

	@Cancelable
	/**Called before a fluid is mixed with something else, normally another fluid. You can use this event to cancel the mixing or change its output */
	public static class PreMixEvent extends FluidMixingEvent
	{
		public final Object input;
		public Object output;

		public PreMixEvent(World world, Vector3 vec, FluidStack fluid, Object input, Object output)
		{
			super(fluid, world, vec);
			this.input = input;
			this.output = output;
		}

	}

	@Cancelable
	/**Called right when the fluid is mixed with an object. This is the last chance to cancel the mixing. As well this can be used to cause a different outcome */
	public static class MixEvent extends FluidMixingEvent
	{
		public final Object input;
		public Object output;

		public MixEvent(World world, Vector3 vec, FluidStack fluid, Object input, Object output)
		{
			super(fluid, world, vec);
			this.input = input;
			this.output = output;
		}

	}

	/**
	 * Called when a mixer has gone threw all the recipes and not found one for the fluid and input.
	 * Use this to hook into this can create a new recipe without registering it with the mixing
	 * class
	 */
	public static class MixingRecipeCall extends FluidMixingEvent
	{
		public final Object input;
		public Object output;

		public MixingRecipeCall(World world, Vector3 vec, FluidStack fluid, Object input)
		{
			super(fluid, world, vec);
			this.input = input;
		}
	}

	public static final void fireEvent(FluidMixingEvent event)
	{
		MinecraftForge.EVENT_BUS.post(event);
	}

}
