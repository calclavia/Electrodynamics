package resonantinduction.energy.generator;

import java.util.EnumSet;

import net.minecraftforge.common.ForgeDirection;

public class TileEntitySolarPanel extends TileEntityGenerator
{
	public TileEntitySolarPanel()
	{
		super();
	}

	@Override
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		return EnumSet.of(ForgeDirection.DOWN);
	}

	@Override
	public boolean hasFuel()
	{
		return this.JOULES_PER_TICK > 0;
	}

	@Override
	public void consumeFuel()
	{
		if (!this.worldObj.isRemote && this.ticks % BlockSolarPanel.tickRate == 0)
		{
			this.setJoulesPerSecound(0);
			if (this.worldObj.canBlockSeeTheSky(xCoord, yCoord + 1, zCoord) && !this.worldObj.provider.hasNoSky)
			{
				if (this.worldObj.isDaytime())
				{
					this.setJoulesPerTick(BlockSolarPanel.wattDay);
					if (this.worldObj.isThundering() || this.worldObj.isRaining())
					{
						this.setJoulesPerSecound(BlockSolarPanel.wattStorm);
					}
				}
				else
				{
					if (!this.worldObj.isThundering() && !this.worldObj.isRaining())
					{
						this.setJoulesPerTick(BlockSolarPanel.wattNight);
					}
				}
				this.setJoulesPerSecound(this.JOULES_PER_TICK + this.JOULES_PER_TICK * (this.worldObj.provider instanceof ISolarLevel ? (int) ((ISolarLevel) this.worldObj.provider).getSolarEnergyMultiplier() : 0));
			}
		}
	}

}
