package mffs.api.fortron;

import nova.core.fluid.Fluid;
import nova.core.fluid.FluidFactory;
import nova.core.fluid.Tank;
import nova.core.fluid.TankProvider;
import nova.core.game.Game;

import java.util.Optional;

/**
 * Blocks that can handle Fortron fluid energy.
 */
public interface Fortron extends TankProvider {

	public static final String fortronID = "fortron";

	public static final FluidFactory fortronFactory = Game.instance.fluidManager.getFluidFactory(Fortron.fortronID).get();

	Tank getFortronTank();

	/**
	 * @return Gets the amount of fortron stored.
	 */
	default int getFortron() {
		return getFortronTank().getFluidAmount();
	}

	/**
	 * @return Gets the maximum possible amount of fortron that can be stored.
	 */
	default int getFortronCapacity() {
		return getFortronTank().getFluidCapacity();
	}

	/**
	 * @return Gets the empty space left in the Fortron device.
	 */
	default int getFortronEmpty() {
		return getFortronCapacity() - getFortron();
	}

	/**
	 * Called to use and consume fortron energy from this storage unit.
	 * @param energy - Amount of fortron energy to use.
	 * @param doUse - True if actually using, false if just simulating.
	 * @return joules - The amount of energy that was actually provided.
	 */
	default int addFortron(int energy, boolean doUse) {
		Optional<Fluid> fluid = getFortronTank().removeFluid(energy, !doUse);
		if (fluid.isPresent()) {
			return fluid.get().amount();
		}

		return 0;
	}

	/**
	 * Called to use and give fortron energy from this storage unit.
	 * @param energy - Amount of fortron energy to give.
	 * @param doUse - True if actually using, false if just simulating.
	 * @return joules - The amount of energy that was actually transfered.
	 */
	default int removeFortron(int energy, boolean doUse) {
		return getFortronTank().addFluid(fortronFactory.makeFluid().withAmount(energy), !doUse);
	}
}
