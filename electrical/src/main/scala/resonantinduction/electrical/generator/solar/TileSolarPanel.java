package resonantinduction.electrical.generator.solar;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import resonant.lib.content.module.TileRender;
import resonant.lib.prefab.vector.Cuboid;
import resonant.lib.utility.ConnectedTextureRenderer;
import resonantinduction.core.Reference;
import resonantinduction.core.Settings;
import resonantinduction.electrical.battery.TileEnergyDistribution;
import universalelectricity.api.energy.EnergyStorageHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileSolarPanel extends TileEnergyDistribution
{
	@SideOnly(Side.CLIENT)
	public static Icon sideIcon, bottomIcon;
	

	public TileSolarPanel()
	{
		super(Material.iron);
		energy = new EnergyStorageHandler(Settings.SOLAR_ENERGY * 20);
		ioMap = 728;
		textureName = "solarPanel_top";
		bounds = new Cuboid(0, 0, 0, 1, 0.3f, 1);
		isOpaqueCube = false;
		normalRender = false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconReg)
	{
		sideIcon = iconReg.registerIcon(Reference.PREFIX + "solarPanel_side");
		bottomIcon = iconReg.registerIcon(Reference.PREFIX + "solarPanel_bottom");
		super.registerIcons(iconReg);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side == 0)
		{
			return bottomIcon;
		}
		else if (side == 1)
		{
			return getIcon();
		}

		return sideIcon;
	}

	@Override
	public void updateEntity()
	{
		if (!this.worldObj.isRemote)
		{
			if (this.worldObj.canBlockSeeTheSky(xCoord, yCoord + 1, zCoord) && !this.worldObj.provider.hasNoSky)
			{
				if (this.worldObj.isDaytime())
				{
					if (!(this.worldObj.isThundering() || this.worldObj.isRaining()))
					{
						this.energy.receiveEnergy(Settings.SOLAR_ENERGY, true);
						markDistributionUpdate |= produce() > 0;
					}
				}
			}
		}

		super.updateEntity();
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected TileRender newRenderer()
	{
		return new ConnectedTextureRenderer(this, Reference.PREFIX + "tankEdge");
	}
}
