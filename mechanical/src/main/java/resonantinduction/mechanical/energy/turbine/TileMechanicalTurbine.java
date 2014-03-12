package resonantinduction.mechanical.energy.turbine;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.IMechanicalNode;
import resonantinduction.core.grid.INode;
import resonantinduction.core.grid.INodeProvider;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import universalelectricity.api.energy.EnergyStorageHandler;
import calclavia.lib.network.Synced.SyncedInput;
import calclavia.lib.network.Synced.SyncedOutput;
import calclavia.lib.prefab.turbine.TileTurbine;

public class TileMechanicalTurbine extends TileTurbine implements INodeProvider
{
	protected MechanicalNode mechanicalNode;

	public TileMechanicalTurbine()
	{
		super();
		energy = new EnergyStorageHandler(0);
		mechanicalNode = new MechanicalNode(this)
		{
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
				return true;
			}

			@Override
			public float getRatio(ForgeDirection dir, IMechanicalNode with)
			{
				return getMultiBlock().isConstructed() ? multiBlockRadius - 0.5f : 0.5f;
			}
		};
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
	public void onProduce()
	{
		mechanicalNode.torque += (torque - mechanicalNode.torque) / 10;
		mechanicalNode.angularVelocity += (angularVelocity - mechanicalNode.angularVelocity) / 10;
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
