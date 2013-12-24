package resonantinduction.wire.part;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import resonantinduction.wire.EnumWireMaterial;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.energy.IConductor;
import calclavia.lib.prefab.CustomDamageSource;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;

/**
 * @author Calclavia
 * 
 */
public abstract class PartAdvancedWire extends PartConductor
{
	public static final int DEFAULT_COLOR = 15;
	public int color = DEFAULT_COLOR;

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

			if (this.getMaterial() == wire.getMaterial())
			{
				if (this.isInsulated() && wire.isInsulated())
				{
					return this.getColor() == wire.getColor() || (this.getColor() == DEFAULT_COLOR || wire.getColor() == DEFAULT_COLOR);
				}

				return true;
			}
		}
		else if (!(obj instanceof IConductor))
		{
			return CompatibilityModule.isHandler(obj);
		}

		return false;
	}

	@Override
	public void onEntityCollision(Entity entity)
	{
		if (!this.isInsulated() && this.getNetwork().getLastBuffer() > 0)
		{
			entity.attackEntityFrom(CustomDamageSource.electrocution, this.getNetwork().getLastBuffer());
		}
	}

	@Override
	public float getResistance()
	{
		return this.getMaterial().resistance;
	}

	@Override
	public long getCurrentCapacity()
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
		this.color = DEFAULT_COLOR;

		if (!this.world().isRemote)
		{
			tile().notifyPartChange(this);
			this.sendInsulationUpdate();
		}
	}

	public void setInsulated(int dyeColour)
	{
		this.isInsulated = true;
		this.color = dyeColour;

		if (!this.world().isRemote)
		{
			tile().notifyPartChange(this);
			this.sendInsulationUpdate();
			this.sendColorUpdate();
		}
	}

	public boolean isInsulated()
	{
		return this.isInsulated;
	}

	public void sendInsulationUpdate()
	{
		tile().getWriteStream(this).writeByte(1).writeBoolean(this.isInsulated);
	}

	/**
	 * Wire Coloring Methods
	 */
	public int getColor()
	{
		return this.isInsulated ? this.color : -1;
	}

	public void setColor(int dye)
	{
		if (this.isInsulated)
		{
			this.color = dye;

			if (!this.world().isRemote)
			{
				tile().notifyPartChange(this);
				this.sendColorUpdate();
			}
		}
	}

	public void sendColorUpdate()
	{
		tile().getWriteStream(this).writeByte(2).writeInt(this.color);
	}

	/**
	 * Changes the wire's color.
	 */
	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (itemStack.itemID == Item.dyePowder.itemID && this.isInsulated())
			{
				this.setColor(itemStack.getItemDamage());
				return true;
			}
			else if (itemStack.itemID == Block.cloth.blockID)
			{
				if (this.isInsulated())
				{
					if (!world().isRemote)
					{
						tile().dropItems(Collections.singletonList(new ItemStack(Block.cloth, 1, BlockColored.getBlockFromDye(color))));
					}

					this.setInsulated(false);
					return true;
				}
				else
				{
					this.setInsulated(BlockColored.getDyeFromBlock(itemStack.getItemDamage()));
					player.inventory.decrStackSize(player.inventory.currentItem, 1);
					return true;
				}
			}
			else if ((itemStack.itemID == Item.shears.itemID || itemStack.getItem() instanceof ItemShears) && isInsulated())
			{
				if (!world().isRemote)
				{
					tile().dropItems(Collections.singletonList(new ItemStack(Block.cloth, 1, BlockColored.getBlockFromDye(color))));
				}

				this.setInsulated(false);
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
			drops.add(new ItemStack(Block.cloth, 1, BlockColored.getBlockFromDye(color)));
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
		this.color = packet.readByte();
		this.isInsulated = packet.readBoolean();
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte((byte) this.getMaterialID());
		packet.writeByte((byte) this.color);
		packet.writeBoolean(this.isInsulated);
	}

	public void read(MCDataInput packet, int packetID)
	{
		switch (packetID)
		{
			case 1:
				this.isInsulated = packet.readBoolean();
				this.tile().markRender();
				break;
			case 2:
				this.color = packet.readInt();
				this.tile().markRender();
				break;
		}
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setInteger("typeID", getMaterialID());
		nbt.setBoolean("isInsulated", isInsulated);
		nbt.setInteger("dyeID", color);
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		setMaterial(nbt.getInteger("typeID"));
		this.isInsulated = nbt.getBoolean("isInsulated");
		this.color = nbt.getInteger("dyeID");
	}

}
