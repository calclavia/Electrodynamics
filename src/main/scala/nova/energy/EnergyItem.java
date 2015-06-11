package nova.energy;

/**
 * An interface for items that store energy in joules.
 */
public interface EnergyItem {
	/**
	 * Adds energy to an item. Returns the quantity of energy that was accepted. This should always
	 * return 0 if the item cannot be externally charged.
	 * @param energy Maximum amount of energy to be sent into the item.
	 * @param doRecharge If false, the charge will only be simulated.
	 * @return Amount of energy that was accepted by the item.
	 */
	public double recharge(double energy, boolean doRecharge);

	/**
	 * Removes energy from an item. Returns the quantity of energy that was removed. This should
	 * always return 0 if the item cannot be externally discharged.
	 * @param energy Maximum amount of energy to be removed from the item.
	 * @param doDischarge If false, the discharge will only be simulated.
	 * @return Amount of energy that was removed from the item.
	 */
	public double discharge(double energy, boolean doDischarge);

	/**
	 * Get the amount of energy currently stored in the item.
	 */
	public double getEnergy();

	/**
	 * Sets the amount of energy in the ItemStack.
	 * @param energy - Amount of electrical energy.
	 */
	public void setEnergy(double energy);

	/**
	 * Get the max amount of energy that can be stored in the item.
	 */
	public double getEnergyCapacity();

}
