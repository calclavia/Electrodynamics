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
import universalelectricity.api.CompatibilityModule;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;

/**
 * @author Calclavia
 * 
 */
public abstract class PartAdvancedWire extends PartConductor
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
		if (obj instanceof PartFlatWire)
		{
			PartFlatWire wire = (PartFlatWire) obj;

			if (wire.getMaterial() == getMaterial())
			{
				if (this.isInsulated() && wire.isInsulated())
				{
					return this.getColor() == wire.getColor();
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

	/**
	 * Material Methods
	 */
	public EnumWireMaterial getMaterial()
	{
		return this.material;
	}

	public void setMaterial(EnumWireMaterial material)
	{
		this.material = material;
	}

	public void setMaterial(int id)
	{
		this.setMaterial(EnumWireMaterial.values()[id]);
	}

	public int getMaterialID()
	{
		return this.material.ordinal();
	}

	/**
	 * Insulation Methods
	 */
	public void setInsulated(boolean insulated)
	{
		this.isInsulated = insulated;
		this.dyeID = DEFAULT_COLOR;
	}

	public void setInsulated(int dyeColour)
	{
		isInsulated = true;
		dyeID = dyeColour;
		tile().notifyPartChange(this);
	}

	public boolean isInsulated()
	{
		return this.isInsulated;
	}

	/**
	 * Wire Coloring Methods
	 */
	public int getColor()
	{
		return this.isInsulated ? this.dyeID : -1;
	}

	public void setColor(int dye)
	{
		this.dyeID = dye;
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
				setColor(item.getItemDamage());
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
		this.setMaterial(packet.readByte());
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
		setMaterial(nbt.getInteger("typeID"));
		this.isInsulated = nbt.getBoolean("isInsulated");
		this.dyeID = nbt.getInteger("dyeID");
	}

}
