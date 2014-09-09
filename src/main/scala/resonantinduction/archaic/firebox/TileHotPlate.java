package resonantinduction.archaic.firebox;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import resonant.content.spatial.block.SpatialBlock;
import resonant.lib.content.prefab.java.TileInventory;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketReceiver;
import resonantinduction.core.Reference;
import universalelectricity.core.transform.region.Cuboid;
import universalelectricity.core.transform.vector.Vector2;
import universalelectricity.core.transform.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * For smelting items.
 *
 * @author Calclavia
 */
public class TileHotPlate extends TileInventory implements IPacketReceiver
{
	public static final int MAX_SMELT_TIME = 200;
	public final int[] smeltTime = new int[] { 0, 0, 0, 0 };
	public final int[] stackSizeCache = new int[] { 0, 0, 0, 0 };
	private final int POWER = 50000;

	public TileHotPlate()
	{
		super(Material.iron);
		setSizeInventory(4);
		bounds(new Cuboid(0, 0, 0, 1, 0.2f, 1));
		normalRender(false);
		forceStandardRender(true);
		isOpaqueCube(false);
	}

	@Override
	public void update()
	{
		if (canRun())
		{
			boolean didSmelt = false;

			for (int i = 0; i < getSizeInventory(); i++)
			{
				if (canSmelt(this.getStackInSlot(i)))
				{
					if (smeltTime[i] <= 0)
					{
						/**
						 * Heat up all slots
						 */
						stackSizeCache[i] = this.getStackInSlot(i).stackSize;
						smeltTime[i] = MAX_SMELT_TIME * stackSizeCache[i];
						worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
					}
					else if (smeltTime[i] > 0)
					{
						/**
						 * Do the smelt action.
						 */
						if (--smeltTime[i] == 0)
						{
							if (!worldObj.isRemote)
							{
								ItemStack outputStack = FurnaceRecipes.smelting().getSmeltingResult(getStackInSlot(i)).copy();
								outputStack.stackSize = stackSizeCache[i];
								setInventorySlotContents(i, outputStack);
								worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
							}
						}
					}

					didSmelt = true;
				}
				else
				{
					smeltTime[i] = 0;
				}
			}
		}
	}

	public void onInventoryChanged()
	{
		//super.onInventoryChanged();

		/**
		 * Update cache calculation.
		 */
		for (int i = 0; i < getSizeInventory(); i++)
		{
			if (getStackInSlot(i) != null)
			{
				if (stackSizeCache[i] != getStackInSlot(i).stackSize)
				{
					if (smeltTime[i] > 0)
					{
						smeltTime[i] += (getStackInSlot(i).stackSize - stackSizeCache[i]) * MAX_SMELT_TIME;
					}

					stackSizeCache[i] = getStackInSlot(i).stackSize;
				}
			}
			else
			{
				stackSizeCache[i] = 0;
			}
		}

		if (worldObj != null)
		{
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	public boolean canRun()
	{

		TileEntity tileEntity = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);

		if (tileEntity instanceof TileFirebox)
		{
			if (((TileFirebox) tileEntity).isBurning())
			{
				return true;
			}
		}

		return false;
	}

	public boolean canSmelt(ItemStack stack)
	{
		return FurnaceRecipes.smelting().getSmeltingResult(stack) != null;
	}

	public boolean isSmelting()
	{
		for (int i = 0; i < getSizeInventory(); i++)
		{
			if (getSmeltTime(i) > 0)
			{
				return true;
			}
		}

		return false;
	}

	public int getSmeltTime(int i)
	{
		return smeltTime[i];
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack)
	{
		return i < getSizeInventory() && canSmelt(itemStack);
	}

	@Override
	public PacketTile getDescPacket()
	{
		return new PacketTile(this, this.getPacketData(0).toArray());
	}

	/**
	 * 1 - Description Packet
	 * 2 - Energy Update
	 * 3 - Tesla Beam
	 */
	public List getPacketData(int type)
	{
		List list = new ArrayList();
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		list.add(nbt);
		return list;
	}

	@Override
	public void read(ByteBuf data, EntityPlayer player, PacketType type)
	{
		try
		{
			this.readFromNBT(ByteBufUtils.readTag(data));
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		for (int i = 0; i < getSizeInventory(); i++)
		{
			smeltTime[i] = nbt.getInteger("smeltTime" + i);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		for (int i = 0; i < getSizeInventory(); i++)
		{
			nbt.setInteger("smeltTime" + i, smeltTime[i]);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconReg)
	{
		super.registerIcons(iconReg);
		SpatialBlock.icon().put("electricHotPlate", iconReg.registerIcon(Reference.prefix() + "electricHotPlate"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		return meta == 1 ? SpatialBlock.icon().get("electricHotPlate") : SpatialBlock.icon().get(getTextureName());
	}

	@Override
	public void click(EntityPlayer player)
	{
		if (server())
		{
			extractItem(this, 0, player);
		}
	}

	@Override
	public boolean use(EntityPlayer player, int side, Vector3 hit)
	{
		if (server())
		{
			Vector2 hitVector = new Vector2(hit.x(), hit.z());
			final double regionLength = 1d / 2d;

			/**
			 * Crafting Matrix
			 */
			matrix:
			for (int j = 0; j < 2; j++)
			{
				for (int k = 0; k < 2; k++)
				{
					Vector2 check = new Vector2(j, k).multiply(regionLength);

					if (check.distance(hitVector) < regionLength)
					{
						int slotID = j * 2 + k;
						interactCurrentItem(this, slotID, player);
						break matrix;
					}
				}
			}

			onInventoryChanged();
		}

		return true;
	}
}
