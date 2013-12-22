package resonantinduction.wire.part;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import resonantinduction.wire.EnumWireMaterial;
import resonantinduction.wire.IAdvancedConductor;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.energy.IConductor;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;

/**
 * @author Calclavia
 * 
 */
public abstract class PartWireBase extends PartConductor implements IAdvancedConductor
{
	public static final int DEFAULT_COLOR = 16;
	public int dyeID = DEFAULT_COLOR;
	public EnumWireMaterial material = EnumWireMaterial.COPPER;
	public boolean isInsulated = false;

	/**
	 * INTERNAL USE.
	 * Can this conductor connect with an external object?
	 */
	@Override
	public boolean canConnectTo(Object obj)
	{
		if (obj instanceof IAdvancedConductor)
		{
			IAdvancedConductor wire = (IAdvancedConductor) obj;

			if (wire.getMaterial() == getMaterial())
			{
				if (this.isInsulated() && wire.isInsulated())
				{
					return this.getInsulationColor() == wire.getInsulationColor();
				}

				return true;
			}
		}

		return CompatibilityModule.isHandler(obj);
	}

	@Override
	public long getEnergyLoss()
	{
		/**
		 * TODO: FIX THIS!
		 */
		return (int) (this.getMaterial().resistance * 1000);
	}

	@Override
	public long getEnergyCapacitance()
	{
		return this.getMaterial().maxAmps;
	}

	@Override
	public EnumWireMaterial getMaterial()
	{
		return material;
	}

	public void setMaterialFromID(int id)
	{
		material = EnumWireMaterial.values()[id];
	}

	public int getMaterialID()
	{
		return material.ordinal();
	}

	@Override
	public boolean isInsulated()
	{
		return isInsulated;
	}

	@Override
	public int getInsulationColor()
	{
		return isInsulated ? dyeID : -1;
	}

	@Override
	public void setInsulationColor(int dye)
	{
		dyeID = dye;
		tile().notifyPartChange(this);
	}

	@Override
	public void setInsulated(boolean insulated)
	{
		isInsulated = insulated;
		dyeID = DEFAULT_COLOR;
		tile().notifyPartChange(this);
	}

	public void setInsulated(int dyeColour)
	{
		isInsulated = true;
		dyeID = dyeColour;
		tile().notifyPartChange(this);
	}

	public void setInsulated()
	{
		setInsulated(true);
	}

	public void setDye(int dye)
	{
		dyeID = dye;
		tile().notifyPartChange(this);
	}

	/**
	 * Changes the wire's color.
	 */
	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
		if (item != null)
		{
			if (item.itemID == Item.dyePowder.itemID && isInsulated())
			{
				setDye(item.getItemDamage());
				return true;
			}
			else if (item.itemID == Block.cloth.blockID)
			{
				if (isInsulated() && !world().isRemote)
				{
					tile().dropItems(Collections.singletonList(new ItemStack(Block.cloth, 1, BlockColored.getBlockFromDye(dyeID))));
				}

				setInsulated(BlockColored.getDyeFromBlock(item.getItemDamage()));
				player.inventory.decrStackSize(player.inventory.currentItem, 1);
				return true;
			}
			else if ((item.itemID == Item.shears.itemID || item.getItem() instanceof ItemShears) && isInsulated())
			{
				if (!world().isRemote)
				{
					tile().dropItems(Collections.singletonList(new ItemStack(Block.cloth, 1, BlockColored.getBlockFromDye(dyeID))));
				}

				setInsulated(false);
				return true;
			}
		}

		return false;
	}

	protected ItemStack getItem()
	{
		return EnumWireMaterial.values()[getMaterialID()].getWire();
	}

	@Override
	public Iterable<ItemStack> getDrops()
	{
		List<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(getItem());

		if (this.isInsulated)
		{
			drops.add(new ItemStack(Block.cloth, 1, BlockColored.getBlockFromDye(dyeID)));
		}

		return drops;
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit)
	{
		return getItem();
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		this.setMaterialFromID(packet.readByte());
		this.dyeID = packet.readByte();
		this.isInsulated = packet.readBoolean();
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte((byte) this.getMaterialID());
		packet.writeByte((byte) this.dyeID);
		packet.writeBoolean(this.isInsulated);
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setInteger("typeID", getMaterialID());
		nbt.setBoolean("isInsulated", isInsulated);
		nbt.setInteger("dyeID", dyeID);
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		setMaterialFromID(nbt.getInteger("typeID"));
		this.isInsulated = nbt.getBoolean("isInsulated");
		this.dyeID = nbt.getInteger("dyeID");
	}

}
