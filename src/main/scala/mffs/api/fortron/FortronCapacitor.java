package mffs.api.fortron;

import java.util.Set;

/**
 * Applied to the Fortron Capacitor TileEntity.
 * @author Calclavia
 */
public interface FortronCapacitor extends FortronFrequency {
	public Set<FortronFrequency> getFrequencyDevices();

	public int getTransmissionRange();

	public int getTransmissionRate();
}
