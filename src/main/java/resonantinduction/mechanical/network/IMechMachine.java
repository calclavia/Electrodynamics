package resonantinduction.mechanical.network;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.net.IConnectable;

/** Applied to machines that connect to a mech network
 * 
 * @author Darkguardsman */
public interface IMechMachine extends IConnectable
{
    /** Called by the network when its torque value changes. Force is not given since the network
     * operates on a can move or can't move setup. Speed is given as a represent */
    public void onTorqueChange(ForgeDirection side, int speed);
}
