package com.calclavia.edx.mffs.api.fortron;

import nova.core.fluid.Fluid;
import nova.core.fluid.SidedTankProvider;
import nova.core.fluid.component.Tank;
import nova.core.game.Game;

import java.util.Optional;

/**
 * Blocks that can handle Fortron fluid energy.
 */
public interface Fortron extends SidedTankProvider {
	public static final String fortronID = "fortron";

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
	 * Adds fortron to this object
	 * @param energy - Amount of fortron.
	 * @param doUse - True if actually using, false if just simulating.
	 * @return The amount of fortron that was added.
	 */
	default int addFortron(int energy, boolean doUse) {
		return getFortronTank().addFluid(Game.fluids().getFactory(fortronID).get().makeFluid().withAmount(energy), !doUse);
	}

	/**
	 * Removes fortron from this object
	 * @param energy - Amount of fortron energy to give.
	 * @param doUse - True if actually using, false if just simulating.
	 * @return The amount of fortron that was removed.
	 */
	default int removeFortron(int energy, boolean doUse) {
		Optional<Fluid> fluid = getFortronTank().removeFluid(energy, !doUse);
		if (fluid.isPresent()) {
			return fluid.get().amount();
		}

		return 0;
	}

}
