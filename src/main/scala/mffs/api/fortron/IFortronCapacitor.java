package mffs.api.fortron;

import java.util.Set;

/**
 * Applied to the Fortron Capacitor TileEntity. Extends IFortronFrequency
 * @author Calclavia
 */
public interface IFortronCapacitor {
	public Set<IFortronFrequency> getFrequencyDevices();

	public int getTransmissionRange();

	public int getTransmissionRate();
}
