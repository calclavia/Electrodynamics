package resonatninduction.tilenetwork;

import net.minecraftforge.common.ForgeDirection;

/**
 * Used on tiles that want control over what can connect to there device. It is suggest that other
 * interfaces for connection be routed threw this to reduce the need to change things
 * 
 * @author DarkGuardsman
 */
public interface ITileConnector
{
	/** Can this tile connect on the given side */
	public boolean canTileConnect(Connection type, ForgeDirection dir);

	/** Types of connections */
	public static enum Connection
	{
		/** Energy from BC, UE, IC2 */
		Eletricity(),
		/** Fluids from anything including BC pipes, DM pipes, Mek pipes */
		FLUIDS(),
		/** Force mainly from rotating rods */
		FORCE(),
		/** Hydraulic pressure from DM pipe */
		FLUID_PRESSURE(), AIR_PRESSURE(),
		/** Item pipe */
		ITEMS(),
		/** Data line input */
		DATA(),
		/** Another tile entity */
		TILE(),
		/** Network of tile entities */
		NETWORK(),
		/** Thermal connection */
		HEAT(),
		/** Wire containing several wires of unknown color */
		MULTI_WIRE(),
		/** Bundle of pipes containing several colored pipes */
		MULTI_PIPE(),
		/** Device that contains several networks that can be of any type */
		MULTI_NETWORK();
	}
}
