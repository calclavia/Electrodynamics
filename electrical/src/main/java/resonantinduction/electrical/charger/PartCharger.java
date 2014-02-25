package resonantinduction.electrical.charger;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.prefab.part.PartFace;
import resonantinduction.electrical.Electrical;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.energy.IEnergyInterface;
import calclavia.lib.utility.WrenchUtility;
import calclavia.lib.utility.inventory.ExternalInventory;
import calclavia.lib.utility.inventory.IExternalInventory;
import calclavia.lib.utility.inventory.IExternalInventoryBox;
import calclavia.lib.utility.inventory.InventoryUtility;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TODO: This DOES NOT WORK when @UniversalClass is not annotated. Flat wires seem to only interact
 * with TE multiparts.
 * 
 * @author Calclavia
 * 
 */
@UniversalClass
public class PartCharger extends PartFace implements IExternalInventory, ISidedInventory, IEnergyInterface
{
	private long lastPacket;

	@Override
	public void readDesc(MCDataInput packet)
	{
		super.readDesc(packet);
		getInventory().load(packet.readNBTTagCompound());
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		super.writeDesc(packet);
		NBTTagCompound nbt = new NBTTagCompound();
		getInventory().save(nbt);
		packet.writeNBTTagCompound(nbt);
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
		if (WrenchUtility.isUsableWrench(player, player.inventory.getCurrentItem(), x(), y(), z()))
		{
			if (!world().isRemote)
			{
				WrenchUtility.damageWrench(player, player.inventory.getCurrentItem(), x(), y(), z());
				facing = (byte) ((facing + 1) % 4);
				sendDescUpdate();
				tile().notifyPartChange(this);
			}

			return true;
		}

		if (item != null)
		{
			if (getStackInSlot(0) == null && item != null && CompatibilityModule.isHandler(item.getItem()))
			{
				setInventorySlotContents(0, item);
				player.inventory.setInventorySlotContents(player.inventory.currentItem, null);

				if (!world().isRemote)
					sendDescUpdate();

				return true;
			}
		}

		if (getStackInSlot(0) != null)
		{
			InventoryUtility.dropItemStack(world(), new universalelectricity.api.vector.Vector3(player), getStackInSlot(0), 0);
			setInventorySlotContents(0, null);
			if (!world().isRemote)
				sendDescUpdate();
		}

		return true;
	}

	@Override
	public boolean canConnect(ForgeDirection direction, Object obj)
	{
		return obj instanceof IEnergyInterface;
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		long energyUsed = 0;
		long energyLeft = receive;
		for (int i = 0; i < this.getSizeInventory(); i++)
		{
			long input = CompatibilityModule.chargeItem(this.getStackInSlot(i), energyLeft, true);
			energyUsed += input;
			energyLeft -= input;
			if (energyLeft <= 0)
				break;
		}

		if (!world().isRemote && energyUsed > 0 && System.currentTimeMillis() - this.lastPacket >= 50)
		{
			this.lastPacket = System.currentTimeMillis();
			sendDescUpdate();
		}
		return energyUsed;
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
	{
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		if (pass == 0)
		{
			RenderCharger.INSTANCE.render(this, pos.x, pos.y, pos.z);
		}
	}

	@Override
	protected ItemStack getItem()
	{
		return new ItemStack(Electrical.itemCharger);
	}

	@Override
	public String getType()
	{
		return "resonant_induction_charger";
	}

	@Override
	public Iterable<ItemStack> getDrops()
	{
		List<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(getItem());

		for (int i = 0; i < getSizeInventory(); i++)
			if (getStackInSlot(i) != null)
				drops.add(getStackInSlot(i));

		return drops;
	}

	/**
	 * Save and load
	 */
	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		this.getInventory().load(nbt);
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		this.getInventory().save(nbt);
	}

	/**
	 * Inventory Methods
	 */
	protected IExternalInventoryBox inventory;
	protected int maxSlots = 1;

	@Override
	public IExternalInventoryBox getInventory()
	{
		if (inventory == null)
		{
			inventory = new ExternalInventory(null, this, this.maxSlots);
		}

		return inventory;
	}

	@Override
	public int getSizeInventory()
	{
		return this.getInventory().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return this.getInventory().getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		return this.getInventory().decrStackSize(i, j);
	}

	public void incrStackSize(int slot, ItemStack itemStack)
	{
		if (this.getStackInSlot(slot) == null)
		{
			setInventorySlotContents(slot, itemStack.copy());
		}
		else if (this.getStackInSlot(slot).isItemEqual(itemStack))
		{
			getStackInSlot(slot).stackSize += itemStack.stackSize;
		}

		onInventoryChanged();
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return this.getInventory().getStackInSlotOnClosing(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.getInventory().setInventorySlotContents(i, itemstack);

	}

	@Override
	public String getInvName()
	{
		return "";
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return this.getInventory().getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return this.getInventory().isUseableByPlayer(entityplayer);
	}

	@Override
	public void openChest()
	{
		this.getInventory().openChest();

	}

	@Override
	public void closeChest()
	{
		this.getInventory().closeChest();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return this.getInventory().isItemValidForSlot(i, itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return this.getInventory().getAccessibleSlotsFromSide(var1);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return this.getInventory().canInsertItem(i, itemstack, j);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return this.getInventory().canExtractItem(i, itemstack, j);
	}

	@Override
	public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
	{
		return false;
	}

	@Override
	public boolean canRemove(ItemStack stack, int slot, ForgeDirection side)
	{
		if (slot >= this.getSizeInventory())
		{
			return false;
		}
		return true;
	}

	@Override
	public void onInventoryChanged()
	{

	}

}
