package com.resonant.wrapper.lib.utility;

import nova.core.block.Block;
import nova.core.fluid.Fluid;
import nova.core.fluid.FluidBlock;
import nova.core.fluid.SidedTankProvider;
import nova.core.fluid.component.Tank;
import nova.core.util.Direction;
import nova.core.world.World;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Fluid interaction helper methods.
 * @author Calclavia
 */
public class FluidUtility {

	public static int getFluidAmountFromBlock(World world, Vector3D vector) {
		Optional<Fluid> fluid = getFluidFromBlock(world, vector);
		return fluid.isPresent() ? fluid.get().amount() : 0;
	}

	public static Optional<Fluid> getFluidFromBlock(World world, Vector3D pos) {
		Optional<Block> block = world.getBlock(pos);

		if (block.isPresent() && block.get() instanceof FluidBlock) {
			return ((FluidBlock) block.get()).getFluid();
		}

		return Optional.empty();
	}

	public static Set<Tank> getTank(World world, Vector3D pos, Direction from) {
		Optional<Block> block = world.getBlock(pos);

		if (block.isPresent() && block.get() instanceof SidedTankProvider) {
			return ((SidedTankProvider) block.get()).getTank(from);
		}

		return Collections.emptySet();
	}

	/**
	 * Gets the average filled percentage of several tanks.
	 */
	public static double getAverageFilledPercentage(Tank... tanks) {
		int amount = 0;
		int capacity = 0;

		for (Tank tank : tanks) {
			amount += tank.getFluidAmount();
			capacity += tank.getFluidCapacity();
		}

		if (capacity > 0) {
			return (double) amount / (double) capacity;
		}

		return 0;
	}

	public static double getAverageFilledPercentage(World world, Vector3D pos, Direction... sides) {
		return getAverageFilledPercentage(world, pos, null, 0, sides);
	}

	public static double getAverageFilledPercentage(World world, Vector3D pos, Class classMask, double defaultFill, Direction... sides) {
		double fullness = defaultFill;
		int count = 1;

		for (Direction side : sides) {
			Optional<Block> block = world.getBlock(pos);

			if (block.isPresent() && block.get() instanceof SidedTankProvider && (classMask == null || classMask.isAssignableFrom(block.get().getClass()))) {
				Set<Tank> tanks = ((SidedTankProvider) block.get()).getTank(side);

				for (Tank tank : tanks) {
					fullness += getAverageFilledPercentage(tank);
					count++;
				}
			}
		}

		return Math.max(0, Math.min(1, fullness / count));
	}

	/**
	 * Drains a block of fluid in the world
	 * @param doDrain - do the action
	 * @return Fluid drained from the block
	 */
	public static Optional<Fluid> drainBlock(World world, Vector3D pos, boolean doDrain) {
		Optional<Block> block = world.getBlock(pos);

		if (block.isPresent() && block.get() instanceof FluidBlock) {
			return ((FluidBlock) block.get()).drain(doDrain);
		}

		return Optional.empty();
	}

	/**
	 * Fills a position in the world with a specific fluid.
	 * @return The amount of fluid used.
	 */
	public static int fillBlock(World world, Vector3D pos, Fluid fluid, boolean doFill) {
		if (fluid.amount() >= Fluid.bucketVolume && fluid.getBlockFactory().isPresent()) {
			Optional<Block> block = world.getBlock(pos);
			if (!block.isPresent()) {
				if (doFill) {
					world.setBlock(pos, fluid.getBlockFactory().get());
				}
				return Fluid.bucketVolume;
			} else {
				//TODO: Handle replaceable blocks
			}
		}
		return 0;
	}

	public static int fillTanks(List<Tank> tanks, Fluid resource, boolean doFill) {
		int totalFilled = 0;
		Fluid fill = resource.clone();

		for (Tank tank : tanks) {
			if (fill.amount() > 0) {
				int filled = tank.addFluid(fill, doFill);
				totalFilled += filled;
				fill.remove(filled);
			} else {
				break;
			}
		}

		return totalFilled;
	}

	public static Optional<Fluid> drainTanks(List<Tank> tanks, int amount, boolean doDrain) {
		Optional<Fluid> drain = Optional.empty();

		for (Tank tank : tanks) {
			if (drain.isPresent() && drain.get().amount() >= amount) {
				break;
			}

			Optional<Fluid> drained = tank.removeFluid(amount, false);

			if (drained.isPresent()) {
				if (!drain.isPresent()) {
					drain = drained;
					tank.removeFluid(amount, doDrain);
				} else if (drain.get().equals(drained)) {
					drain.get().add(drained.get().amount());
					tank.removeFluid(amount, doDrain);
				}
			}
		}

		return drain;
	}
}
