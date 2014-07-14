package resonantinduction.core.prefab.part;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.BlockColored;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import resonantinduction.core.MultipartUtility;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;

/**
 * @author Calclavia
 * 
 */
public abstract class PartColorableMaterial<M extends Enum> extends TraitPart
{
	public static final int DEFAULT_COLOR = 15;
	public int color = DEFAULT_COLOR;

	public M material;
	public boolean isInsulated = false;
	public boolean requiresInsulation = true;
	protected final Item insulationType;

	public PartColorableMaterial(Item insulationType)
	{
		this.insulationType = insulationType;
	}

	/**
	 * Material Methods
	 */
	public M getMaterial()
	{
		return material;
	}

	public void setMaterial(M material)
	{
		this.material = material;
	}

	public abstract void setMaterial(int i);

	public int getMaterialID()
	{
		return material.ordinal();
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
		return isInsulated || !requiresInsulation ? color : -1;
	}

	public void setColor(int dye)
	{
		if (isInsulated || !requiresInsulation)
		{
			this.color = dye;

			if (!world().isRemote)
			{
				tile().notifyPartChange(this);
				onPartChanged(this);
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
			int dyeColor = MultipartUtility.isDye(itemStack);

			if (dyeColor != -1 && (isInsulated() || !requiresInsulation))
			{
				if (!player.capabilities.isCreativeMode && requiresInsulation)
				{
					player.inventory.decrStackSize(player.inventory.currentItem, 1);
				}

				this.setColor(dyeColor);
				return true;
			}
			else if (requiresInsulation)
			{
				if (itemStack.getItem() == insulationType)
				{
					if (this.isInsulated())
					{
						if (!world().isRemote && player.capabilities.isCreativeMode)
						{
							tile().dropItems(Collections.singletonList(new ItemStack(insulationType, 1, BlockColored.getBlockFromDye(color))));
						}

						this.setInsulated(false);
						return true;
					}
					else
					{
						if (!player.capabilities.isCreativeMode)
						{
							player.inventory.decrStackSize(player.inventory.currentItem, 1);
						}

						this.setInsulated(BlockColored.getDyeFromBlock(itemStack.getItemDamage()));
						return true;
					}
				}
				else if (itemStack.getItem() instanceof ItemShears && isInsulated())
				{
					if (!world().isRemote && !player.capabilities.isCreativeMode)
					{
						tile().dropItems(Collections.singletonList(new ItemStack(insulationType, 1, BlockColored.getBlockFromDye(color))));
					}

					this.setInsulated(false);
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public Iterable<ItemStack> getDrops()
	{
		List<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(getItem());

		if (requiresInsulation && isInsulated)
		{
			drops.add(new ItemStack(insulationType, 1, BlockColored.getBlockFromDye(color)));
		}

		return drops;
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
