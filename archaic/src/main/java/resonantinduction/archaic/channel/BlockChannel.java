package resonantinduction.archaic.channel;

import resonantinduction.core.Settings;
import resonantinduction.core.prefab.fluid.BlockFluidNetwork;
import universalelectricity.api.UniversalElectricity;

/** Early tier version of the basic pipe. Open on the top, and can't support pressure.
 * 
 * @author Darkguardsman */
public class BlockChannel extends BlockFluidNetwork
{
    public BlockChannel(int id)
    {
        super(Settings.CONFIGURATION.getBlock("Channel", id).getInt(id), UniversalElectricity.machine);
    }

}
