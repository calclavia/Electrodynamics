package resonantinduction.mechanical.turbine;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenOcean;
import net.minecraft.world.biome.BiomeGenPlains;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;
import resonantinduction.mechanical.gear.PartGearShaft;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.IMechanicalNetwork;
import resonantinduction.mechanical.network.MechanicalNetwork;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.turbine.TileTurbine;

/**
 * The vertical wind turbine collects airflow.
 * The horizontal wind turbine collects steam from steam power plants.
 * 
 * @author Calclavia
 * 
 */
public class TileWindTurbine extends TileTurbine implements IMechanical
{
	@Override
	public void invalidate()
	{
		getNetwork().split(this);
		super.invalidate();
	}

	@Override
	public void updateEntity()
	{
		/**
		 * If this is a vertical turbine.
		 */
		if (getDirection().offsetY == 0)
		{
			maxPower = 100;
			getMultiBlock().get().power += getWindPower();
		}
		else
		{
			maxPower = 1000;
		}

		if (!getMultiBlock().isConstructed())
			torque = defaultTorque / 6;
		else
			torque = defaultTorque / 2;

		super.updateEntity();
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return false;
	}

	public long getWindPower()
	{
		BiomeGenBase biome = worldObj.getBiomeGenForCoords(xCoord, zCoord);
		boolean hasBonus = biome instanceof BiomeGenOcean || biome instanceof BiomeGenPlains || biome == BiomeGenBase.river;

		if (!worldObj.canBlockSeeTheSky(xCoord, yCoord + 4, zCoord))
			return 0;

		return (long) (((((float) yCoord + 4) / 256) * maxPower) + (hasBonus ? 80 : 0)) * (worldObj.isRaining() ? 2 : 1);
	}

	@Override
	public void playSound()
	{
		if (this.ticks % 18 == 0)
		{
			// this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord,
			// "atomicscience:turbine", 0.6f, (float) (0.7f + (0.2 * ((double) this.power / (double)
			// this.getMaxPower()))));
		}
	}

	/**
	 * Mechanical Methods
	 * 
	 * @return The connections.
	 */
	@Override
	public Object[] getConnections()
	{
		Object[] connections = new Object[6];

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tile = new Vector3(this).translate(dir).getTileEntity(worldObj);

			if (tile instanceof IMechanical)
			{
				IMechanical mech = ((IMechanical) tile).getInstance(dir.getOpposite());

				// Don't connect with shafts
				if (mech != null && !(mech instanceof PartGearShaft) && canConnect(dir, this) && mech.canConnect(dir.getOpposite(), this))
				{
					connections[dir.ordinal()] = mech;
					getNetwork().merge(mech.getNetwork());
				}
			}
		}

		return connections;
	}

	private IMechanicalNetwork network;

	@Override
	public IMechanicalNetwork getNetwork()
	{
		if (this.network == null)
		{
			this.network = new MechanicalNetwork();
			this.network.addConnector(this);
		}
		return this.network;
	}

	@Override
	public void setNetwork(IMechanicalNetwork network)
	{
		this.network = network;
	}

	@Override
	public float getRatio(ForgeDirection dir, Object source)
	{
		return 0.5f;
	}

	@Override
	public float getAngularVelocity()
	{
		return angularVelocity;
	}

	@Override
	public void setAngularVelocity(float velocity)
	{
		this.angularVelocity = velocity;
	}

	@Override
	public void setTorque(long torque)
	{
		this.torque = torque;
	}

	@Override
	public boolean inverseRotation(ForgeDirection dir, IMechanical with)
	{
		return true;
	}

	@Override
	public IMechanical getInstance(ForgeDirection dir)
	{
		return (IMechanical) getMultiBlock().get();
	}

	@Override
	public boolean canConnect(ForgeDirection from, Object sourcen)
	{
		return from == getDirection().getOpposite();
	}
}
