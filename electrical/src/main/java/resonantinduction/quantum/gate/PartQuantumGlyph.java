package resonantinduction.quantum.gate;

import icbm.api.IBlockFrequency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import mffs.api.fortron.FrequencyGrid;
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
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import resonantinduction.electrical.Electrical;
import universalelectricity.api.vector.VectorWorld;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TSlottedPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartQuantumGlyph extends JCuboidPart implements TSlottedPart, JNormalOcclusion, IQuantumGate
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
	public void onWorldSeparate()
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
		if (((IQuantumGate) tile()).getFrequency() != -1)
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

			gates.remove((IQuantumGate) tile());

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
					System.out.println(getQuantumTank());
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
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		nbt.setByte("side", slot);
		nbt.setByte("number", number);
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

}
