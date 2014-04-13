package resonantinduction.mechanical.energy.turbine;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import universalelectricity.api.energy.EnergyStorageHandler;
import calclavia.api.resonantinduction.IMechanicalNode;
import calclavia.lib.grid.INode;
import calclavia.lib.grid.INodeProvider;
import calclavia.lib.network.Synced;
import calclavia.lib.network.Synced.SyncedInput;
import calclavia.lib.network.Synced.SyncedOutput;

//TODO: MC 1.7, merge turbines in.
public class TileMechanicalTurbine extends TileTurbineBase implements INodeProvider
{
	protected MechanicalNode mechanicalNode;
	@Synced(1)
	protected double renderAngularVelocity;
	protected double renderAngle;

	protected double prevAngularVelocity;

	protected class TurbineNode extends MechanicalNode
	{
		public TurbineNode(INodeProvider parent)
		{
			super(parent);
		}

		@Override
		public boolean canConnect(ForgeDirection from, Object source)
		{
			if (source instanceof MechanicalNode && !(source instanceof TileMechanicalTurbine))
			{
				/**
				 * Face to face stick connection.
				 */
				TileEntity sourceTile = position().translate(from).getTileEntity(getWorld());

				if (sourceTile instanceof INodeProvider)
				{
					MechanicalNode sourceInstance = ((INodeProvider) sourceTile).getNode(MechanicalNode.class, from.getOpposite());
					return sourceInstance == source && from == getDirection().getOpposite();
				}
			}

			return false;
		}

		@Override
		public boolean inverseRotation(ForgeDirection dir, IMechanicalNode with)
		{
			return dir == getDirection().getOpposite();
		}

		@Override
		public float getRatio(ForgeDirection dir, IMechanicalNode with)
		{
			return getMultiBlock().isConstructed() ? multiBlockRadius - 0.5f : 0.5f;
		}
	};

	public TileMechanicalTurbine()
	{
		super();
		mechanicalNode = new TurbineNode(this);
	}

	@Override
	public void initiate()
	{
		mechanicalNode.reconstruct();
		super.initiate();
	}

	@Override
	public void invalidate()
	{
		mechanicalNode.deconstruct();
		super.invalidate();
	}

	@Override
	public void updateEntity()
	{
		if (!worldObj.isRemote)
		{
			renderAngularVelocity = (double) mechanicalNode.angularVelocity;

			if (renderAngularVelocity != prevAngularVelocity)
			{
				prevAngularVelocity = renderAngularVelocity;
				sendPowerUpdate();
			}
		}
		else
		{
			renderAngle = (renderAngle + renderAngularVelocity / 20) % (Math.PI * 2);

			// TODO: Make this neater
			onProduce();
		}

		super.updateEntity();
	}

	@Override
	public void onProduce()
	{
		if (!worldObj.isRemote)
		{
			if (mechanicalNode.torque < 0)
				torque = -Math.abs(torque);

			if (mechanicalNode.angularVelocity < 0)
				angularVelocity = -Math.abs(angularVelocity);

			mechanicalNode.apply(this, (torque - mechanicalNode.getTorque()) / 10, (angularVelocity - mechanicalNode.getAngularVelocity()) / 10);
		}
	}

	@Override
	public <N extends INode> N getNode(Class<? super N> nodeType, ForgeDirection from)
	{
		if (nodeType.isAssignableFrom(mechanicalNode.getClass()))
			return (N) ((TileMechanicalTurbine) getMultiBlock().get()).mechanicalNode;
		return null;
	}

	@Override
	@SyncedInput
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		tier = nbt.getInteger("tier");
		mechanicalNode.load(nbt);
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	@SyncedOutput
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("tier", tier);
		mechanicalNode.save(nbt);
	}
}
