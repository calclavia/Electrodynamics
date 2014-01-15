package resonantinduction.mechanical.network;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.net.IConnectable;

/**
 * Applied to machines that connect to a mech network
 * 
 * @author Darkguardsman
 */
public interface IMechanical extends IConnectable
{
	public void setPower(long torque, float speed);

	/** Called by the network when its torque value changes. */
	public void onTorqueChange(ForgeDirection side, int speed);

	/** Gets the force on the side, zero is ignored, neg is input force, pos is output force */
	public int getForceSide(ForgeDirection side);
}
