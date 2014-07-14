package resonantinduction.quantum.gate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import resonant.api.IExternalInventory;
import resonant.api.IExternalInventoryBox;
import resonant.api.blocks.IBlockFrequency;
import resonant.lib.utility.inventory.ExternalInventory;
import resonantinduction.electrical.Electrical;
import universalelectricity.api.vector.VectorWorld;
import calclavia.api.mffs.fortron.FrequencyGrid;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TSlottedPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartQuantumGlyph extends JCuboidPart implements TSlottedPart, JNormalOcclusion, IQuantumGate, IExternalInventory
{
	public static final int MAX_GLYPH = 4;
	static final Cuboid6[] bounds = new Cuboid6[15];

	static
	{
		float expansion = -0.02f;

		bounds[7] = new Cuboid6(0, 0, 0, 0.5, 0.5, 0.5).expand(expansion);
		bounds[9] = new Cuboid6(0, 0, 0.5, 0.5, 0.5, 1).expand(expansion);
		bounds[11] = new Cuboid6(0.5, 0, 0, 1, 0.5, 0.5).expand(expansion);
		bounds[13] = new Cuboid6(0.5, 0, 0.5, 1, 0.5, 1).expand(expansion);

		bounds[8] = new Cuboid6(0, 0.5, 0, 0.5, 1, 0.5).expand(expansion);
		bounds[10] = new Cuboid6(0, 0.5, 0.5, 0.5, 1, 1).expand(expansion);
		bounds[12] = new Cuboid6(0.5, 0.5, 0, 1, 1, 0.5).expand(expansion);
		bounds[14] = new Cuboid6(0.5, 0.5, 0.5, 1, 1, 1).expand(expansion);
	}

	private byte slot;
	byte number;
	int ticks;

	public void preparePlacement(int side, int itemDamage)
	{
		this.slot = (byte) side;
		this.number = (byte) itemDamage;
	}

	@Override
	public void onWorldJoin()
	{
		if (((IQuantumGate) tile()).getFrequency() != -1)
		{
			FrequencyGrid.instance().register((IQuantumGate) tile());
		}
	}

	@Override
	public void preRemove()
	{
		FrequencyGrid.instance().unregister((IQuantumGate) tile());
	}

	@Override
	public void onEntityCollision(Entity entity)
	{
		if (!world().isRemote)
		{
			if (entity instanceof EntityPlayer)
				if (!((EntityPlayer) entity).isSneaking())
					return;

			transport(entity);
		}
	}

	@Override
	public void transport(Entity entity)
	{
		if (ticks % 10 == 0 && ((IQuantumGate) tile()).getFrequency() != -1)
		{
			Set<IBlockFrequency> frequencyBlocks = FrequencyGrid.instance().get(((IQuantumGate) tile()).getFrequency());
			List<IQuantumGate> gates = new ArrayList<IQuantumGate>();

			for (IBlockFrequency frequencyBlock : frequencyBlocks)
			{
				if (frequencyBlock instanceof IQuantumGate)
				{
					gates.add((IQuantumGate) frequencyBlock);
				}
			}

			gates.remove(tile());

			if (gates.size() > 0)
			{
				IQuantumGate gate = gates.get(gates.size() > 1 ? entity.worldObj.rand.nextInt(gates.size() - 1) : 0);
				VectorWorld position = new VectorWorld((TileEntity) gate).translate(0.5, 2, 0.5);
				if (QuantumGateManager.moveEntity(entity, position))
					world().playSoundAtEntity(entity, "mob.endermen.portal", 1.0F, 1.0F);
			}
		}
	}

	@Override
	public void update()
	{
		if (ticks == 0)
			FrequencyGrid.instance().register((IQuantumGate) tile());

		ticks++;

		if (world().isRemote)
		{
			int frequency = ((IBlockFrequency) tile()).getFrequency();

			if (frequency > 0)
			{
				float deviation = 1;
				// Spawn particle effects.
				universalelectricity.api.vector.Vector3 center = new universalelectricity.api.vector.Vector3(x() + 0.5, y() + 0.5, z() + 0.5);
				Electrical.proxy.renderElectricShock(world(), center, center.clone().translate(Math.random() * deviation - deviation / 2, Math.random() * deviation - deviation / 2, Math.random() * deviation - deviation / 2));
			}
		}
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack itemStack)
	{
		if (player.isSneaking())
		{
			if (!world().isRemote)
			{
				transport(player);
				return true;
			}
		}
		else
		{
			int frequency = ((IBlockFrequency) tile()).getFrequency();

			if (frequency > -1)
			{
				if (!world().isRemote)
				{
					System.out.println(getStackInSlot(0));
					player.addChatMessage("Quantum Gate Frequency: " + frequency);
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public String getType()
	{
		return "resonant_induction_quantum_glyph";
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		RenderQuantumGlyph.INSTANCE.render(this, pos.x, pos.y, pos.z);
	}

	@Override
	public Cuboid6 getBounds()
	{
		if (slot < bounds.length)
			if (bounds[slot] != null)
				return bounds[slot];

		return new Cuboid6(0, 0, 0, 0.5, 0.5, 0.5);
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		return Arrays.asList(new Cuboid6[] { getBounds() });
	}

	@Override
	public int getSlotMask()
	{
		return 1 << slot;
	}

	protected ItemStack getItem()
	{
		return new ItemStack(Electrical.itemQuantumGlyph, 1, number);
	}

	@Override
	public Iterable<ItemStack> getDrops()
	{
		List<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(getItem());
		return drops;
	}

	@Override
	public ItemStack pickItem(MovingObjectPosition hit)
	{
		return getItem();
	}

	/** Packet Code. */
	@Override
	public void readDesc(MCDataInput packet)
	{
		load(packet.readNBTTagCompound());
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		// packet.writeByte(0);
		NBTTagCompound nbt = new NBTTagCompound();
		save(nbt);
		packet.writeNBTTagCompound(nbt);
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		slot = nbt.getByte("side");
		number = nbt.getByte("number");

		if (nbt.hasKey("frequency"))
		{
			int frequency = nbt.getInteger("frequency");

			if (frequency != -1)
			{
				ExternalInventory savedInventory = new ExternalInventory(null, this, 1);
				savedInventory.load(nbt);
				quantumInventories.put(frequency, savedInventory);

				FluidTank tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
				tank.readFromNBT(nbt);
				quantumTanks.put(frequency, tank);
			}
		}
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		nbt.setByte("side", slot);
		nbt.setByte("number", number);

		if (tile() != null)
		{
			int frequency = ((IQuantumGate) tile()).getFrequency();
			nbt.setInteger("frequency", frequency);

			if (frequency != -1)
			{
				getInventory().save(nbt);
				getQuantumTank().writeToNBT(nbt);
			}
		}
	}

	@Override
	public int getFrequency()
	{
		return number;
	}

	@Override
	public void setFrequency(int frequency)
	{

	}

	/**
	 * Synced Fluid
	 */
	static final HashMap<Integer, FluidTank> quantumTanks = new HashMap<Integer, FluidTank>();

	@Override
	public FluidTank getQuantumTank()
	{
		int frequency = ((IQuantumGate) tile()).getFrequency();

		if (frequency > -1)
		{
			if (!quantumTanks.containsKey(frequency))
				quantumTanks.put(frequency, new FluidTank(FluidContainerRegistry.BUCKET_VOLUME));

			return quantumTanks.get(frequency);
		}

		return null;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (((IQuantumGate) tile()).getFrequency() != -1)
			return getQuantumTank().fill(resource, doFill);
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if (((IQuantumGate) tile()).getFrequency() != -1)
			return getQuantumTank().drain(resource.amount, doDrain);
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if (((IQuantumGate) tile()).getFrequency() != -1)
			return getQuantumTank().drain(maxDrain, doDrain);
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return ((IQuantumGate) tile()).getFrequency() != -1;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return ((IQuantumGate) tile()).getFrequency() != -1;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] { getQuantumTank().getInfo() };
	}

	/**
	 * Inventory Capabilities
	 */
	static final HashMap<Integer, IExternalInventoryBox> quantumInventories = new HashMap<Integer, IExternalInventoryBox>();

	@Override
	public IExternalInventoryBox getInventory()
	{
		int frequency = ((IQuantumGate) tile()).getFrequency();

		if (frequency > -1)
		{
			if (!quantumInventories.containsKey(frequency))
				quantumInventories.put(frequency, new ExternalInventory(null, this, 1));

			return quantumInventories.get(frequency);
		}

		return null;
	}

	@Override
	public int getSizeInventory()
	{
		return 1;// getInventory().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		if (getInventory() == null)
			return null;
		return getInventory().getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		return this.getInventory().decrStackSize(i, j);
	}

	public void incrStackSize(int slot, ItemStack itemStack)
	{
		if (getStackInSlot(slot) == null)
		{
			setInventorySlotContents(slot, itemStack.copy());
		}
		else if (getStackInSlot(slot).isItemEqual(itemStack))
		{
			getStackInSlot(slot).stackSize += itemStack.stackSize;
		}

		onInventoryChanged();
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		if (getInventory() == null)
			return null;
		return this.getInventory().getStackInSlotOnClosing(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemStack)
	{
		if (getInventory() != null)
			this.getInventory().setInventorySlotContents(i, itemStack);
	}

	@Override
	public String getInvName()
	{
		return "Quantum Gate";
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit()
	{
		if (getInventory() == null)
			return 0;
		return this.getInventory().getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		if (getInventory() == null)
			return false;
		return this.getInventory().isUseableByPlayer(entityplayer);
	}

	@Override
	public void openChest()
	{

	}

	@Override
	public void closeChest()
	{
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if (getInventory() == null)
			return false;
		return getInventory().isItemValidForSlot(i, itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		if (getInventory() == null)
			return new int[0];
		return getInventory().getAccessibleSlotsFromSide(var1);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		if (getInventory() == null)
			return false;
		return getInventory().canInsertItem(i, itemstack, j);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		if (getInventory() == null)
			return false;
		return getInventory().canExtractItem(i, itemstack, j);
	}

	@Override
	public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
	{
		return getInventory() != null;
	}

	@Override
	public boolean canRemove(ItemStack stack, int slot, ForgeDirection side)
	{
		return getInventory() != null;
	}

	@Override
	public void onInventoryChanged()
	{

	}
}
