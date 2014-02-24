package resonantinduction.quantum.gate;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import resonantinduction.electrical.tesla.ITesla;
import resonantinduction.electrical.tesla.TeslaGrid;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;
import calclavia.lib.prefab.tile.TileElectrical;

/**
 * @author Calclavia, Archadia
 */
public class TileQuantumGate extends TileElectrical implements ITesla
{
	private long lastFrequencyCheck = 0;
	private int frequency = 0;

	public TileQuantumGate()
	{
		energy = new EnergyStorageHandler(100000);
		ioMap = 0;
	}

	@Override
	public void initiate()
	{
		super.initiate();
		TeslaGrid.instance().register(this);
		
		if (!worldObj.isRemote)
		{
			QuantumGateManager.addAnchor(this);
		}
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (canFunction() && ticks % 60 == 0)
		{
			AxisAlignedBB bounds = AxisAlignedBB.getAABBPool().getAABB(xCoord - 1, yCoord - 4, zCoord - 1, xCoord + 2, yCoord + 2, zCoord + 2);
			List<Entity> entities = worldObj.getEntitiesWithinAABB(Entity.class, bounds);

			for (Entity entity : entities)
			{
				if (entity instanceof EntityPlayer)
					if (entity.isSneaking())
						continue;

				doTeleport(entity);
			}
		}
	}

	public boolean canFunction()
	{
		return energy.isFull();
	}

	@Override
	public void validate()
	{
		
		super.validate();
	}

	@Override
	public void invalidate()
	{
		if (!worldObj.isRemote)
		{
			QuantumGateManager.remAnchor(this);
		}
		
		TeslaGrid.instance().unregister(this);
		super.invalidate();
	}

	public void doTeleport(Entity entity)
	{
		VectorWorld teleportSpot = null;

		if (getFrequency() != -1)
		{
			TileQuantumGate teleporter = QuantumGateManager.getClosestWithFrequency(new VectorWorld(this), getFrequency(), this);

			if (teleporter != null)
			{
				teleportSpot = new VectorWorld(teleporter).translate(0.5, 2, 0.5);
			}
		}

		if (teleportSpot != null)
		{
			QuantumGateManager.moveEntity(entity, teleportSpot);
		}
	}

	/** @return -1 if the teleporter is unable to teleport. */
	public int getFrequency()
	{
		if (System.currentTimeMillis() - this.lastFrequencyCheck > 10)
		{
			this.lastFrequencyCheck = System.currentTimeMillis();
			this.frequency = 0;

			for (int i = 4; i > 0; i--)
			{
				Vector3 position = new Vector3(xCoord, yCoord - i, this.zCoord);

				Block block = Block.blocksList[this.worldObj.getBlockId((int) position.x, (int) position.y, (int) position.z)];

				if (block instanceof BlockGlyph)
				{
					int metadata = this.worldObj.getBlockMetadata((int) position.x, (int) position.y, (int) position.z);
					this.frequency += Math.pow(BlockGlyph.MAX_GLYPH, i - 2) * metadata;
				}
				else
				{
					return -1;
				}
			}
		}

		return frequency;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

	}

	@Override
	public long teslaTransfer(long transferEnergy, boolean doTransfer)
	{
		return energy.receiveEnergy(transferEnergy, doTransfer);
	}

	@Override
	public boolean canTeslaTransfer(TileEntity transferTile)
	{
		return true;
	}
}
