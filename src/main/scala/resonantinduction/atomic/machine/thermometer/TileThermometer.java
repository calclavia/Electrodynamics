package resonantinduction.atomic.machine.thermometer;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import resonant.content.prefab.itemblock.ItemBlockSaved;
import resonant.content.prefab.java.TileAdvanced;
import resonant.engine.grid.thermal.ThermalGrid;
import resonant.lib.network.Synced;
import resonant.lib.network.discriminator.PacketAnnotation;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.Reference;
import universalelectricity.core.transform.vector.Vector3;
import universalelectricity.core.transform.vector.VectorWorld;

import java.util.ArrayList;

/**
 * Thermometer TileEntity
 */
@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
public class TileThermometer extends TileAdvanced implements SimpleComponent
{
	public static final int MAX_THRESHOLD = 5000;
	private static IIcon iconSide;

	@Synced
	public float detectedTemperature = 295;

	@Synced
	public float previousDetectedTemperature = 295;

	@Synced
	public Vector3 trackCoordinate;

	@Synced
	private int threshold = 1000;

	@Synced
	private boolean isProvidingPower = false;

	public TileThermometer()
	{
		super(Material.piston);
		canProvidePower(true);
		normalRender(false);
		forceStandardRender(true);
		itemBlock(ItemBlockThermometer.class);
	}

	@Override
	public IIcon getIcon(int side, int meta)
	{
		return side == 1 || side == 0 ? super.getIcon(side, meta) : iconSide;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		super.registerIcons(iconRegister);
		iconSide = iconRegister.registerIcon(Reference.prefix() + "machine");
	}

	@Override
	public boolean use(EntityPlayer player, int side, Vector3 hit)
	{
		if (player.isSneaking())
		{
			setThreshold(getThershold() + 100);
		}
		else
		{
			setThreshold(getThershold() - 100);
		}

		return true;
	}

	@Override
	public boolean configure(EntityPlayer player, int side, Vector3 hit)
	{
		if (player.isSneaking())
		{
			setThreshold(getThershold() - 10);
		}
		else
		{
			setThreshold(getThershold() + 10);
		}
		return true;
	}

	@Override
	public int getStrongRedstonePower(IBlockAccess access, int side)
	{
		return isProvidingPower ? 15 : 0;
	}

	@Override
	public ArrayList<ItemStack> getDrops(int metadata, int fortune)
	{
		return new ArrayList<ItemStack>();
	}

	@Override
	public void onRemove(Block block, int par6)
	{
		ItemStack stack = ItemBlockSaved.getItemStackWithNBT(getBlockType(), world(), x(), y(), z());
		InventoryUtility.dropItemStack(world(), center(), stack);
	}

	@Override
	public void update()
	{
		super.updateEntity();

		// Server only operation.
		if (!worldObj.isRemote)
		{
			// Every ten ticks.
			if (ticks() % 10 == 0)
			{
				// Grab temperature from target or from ourselves.
				if (trackCoordinate != null)
				{
					detectedTemperature = ThermalGrid.getTemperature(new VectorWorld(world(), trackCoordinate));
				}
				else
				{
					detectedTemperature = ThermalGrid.getTemperature(new VectorWorld(this));
				}

				// Send update packet if temperature is different or over temperature threshold.
				if (detectedTemperature != previousDetectedTemperature || isProvidingPower != this.isOverThreshold())
				{
					previousDetectedTemperature = detectedTemperature;
					isProvidingPower = isOverThreshold();
					notifyChange();
					sendPacket(getDescPacket());
				}
			}
		}
	}

	@Override
	public PacketAnnotation getDescPacket()
	{
		return new PacketAnnotation(this);
	}

	public void setTrack(Vector3 track)
	{
		trackCoordinate = track;
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		threshold = nbt.getInteger("threshold");

		if (nbt.hasKey("trackCoordinate"))
		{
			trackCoordinate = new Vector3(nbt.getCompoundTag("trackCoordinate"));
		}
		else
		{
			trackCoordinate = null;
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("threshold", threshold);

		if (trackCoordinate != null)
		{
			nbt.setTag("trackCoordinate", this.trackCoordinate.writeNBT(new NBTTagCompound()));
		}
	}

	public int getThershold()
	{
		return threshold;
	}

	public void setThreshold(int newThreshold)
	{
		threshold = newThreshold % MAX_THRESHOLD;

		if (threshold <= 0)
		{
			threshold = MAX_THRESHOLD;
		}

		markUpdate();
	}

	public boolean isOverThreshold()
	{
		return detectedTemperature >= getThershold();
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getTemperature(Context context, Arguments args)
	{
		return new Object[]
				{ this.detectedTemperature };
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getWarningTemperature(Context context, Arguments args)
	{
		return new Object[]
				{ this.getThershold() };
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] isAboveWarningTemperature(Context context, Arguments args)
	{
		return new Object[]
				{ this.isOverThreshold() };
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] setWarningTemperature(Context context, Arguments args)
	{
		if (args.count() <= 0)
		{
			throw new IllegalArgumentException("Not enough Arguments. Must provide one argument");
		}
		if (args.count() >= 2)
		{
			throw new IllegalArgumentException("Too many Arguments. Must provide one argument");
		}
		if (!(args.isInteger(0)))
		{
			throw new IllegalArgumentException("Invalid Argument. Must provide an Integer");
		}
		synchronized (this)
		{
			this.setThreshold(args.checkInteger(0));
		}
		return new Object[]
				{ this.threshold == args.checkInteger(0) };
	}

	@Override
	public String getComponentName()
	{
		return "AS Thermometer";
	}
}
