package resonantinduction.energy.transformer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;
import universalelectricity.api.electricity.IElectricalNetwork;
import universalelectricity.api.electricity.IVoltageInput;
import universalelectricity.api.electricity.IVoltageOutput;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyInterface;
import universalelectricity.api.vector.VectorHelper;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.FaceMicroClass;
import codechicken.multipart.JCuboidPart;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TFacePart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartTransformer extends JCuboidPart implements JNormalOcclusion, TFacePart, IVoltageOutput, IEnergyInterface
{
	public static Cuboid6[][] oBoxes = new Cuboid6[6][2];

	static
	{
		oBoxes[0][0] = new Cuboid6(1 / 8D, 0, 0, 7 / 8D, 1 / 8D, 1);
		oBoxes[0][1] = new Cuboid6(0, 0, 1 / 8D, 1, 1 / 8D, 7 / 8D);
		for (int s = 1; s < 6; s++)
		{
			Transformation t = Rotation.sideRotations[s].at(Vector3.center);
			oBoxes[s][0] = oBoxes[0][0].copy().apply(t);
			oBoxes[s][1] = oBoxes[0][1].copy().apply(t);
		}
	}
	/** Side of the block this is placed on */
	public ForgeDirection placementSide;
	/** Direction this block faces */
	public byte face = 0;
	/** Step the voltage up */
	private boolean stepUp;
	/** Amount to mulitply the step by (up x2. down /2) */
	public int multiplier = 2;

	public void preparePlacement(int side, int itemDamage)
	{
		this.placementSide = ForgeDirection.getOrientation((byte) (side ^ 1));
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		this.placementSide = ForgeDirection.getOrientation(packet.readByte());
		this.face = packet.readByte();
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeByte(this.placementSide.ordinal());
		packet.writeByte(this.face);
	}

	public boolean stepUp()
	{
		return this.stepUp;
	}

	@Override
	public int getSlotMask()
	{
		return 1 << this.placementSide.ordinal();
	}

	@Override
	public Cuboid6 getBounds()
	{
		return FaceMicroClass.aBounds()[0x10 | this.placementSide.ordinal()];
	}

	@Override
	public int redstoneConductionMap()
	{
		return 0;
	}

	@Override
	public boolean solid(int arg0)
	{
		return true;
	}

	@Override
	public Iterable<Cuboid6> getOcclusionBoxes()
	{
		return Arrays.asList(oBoxes[this.placementSide.ordinal()]);
	}

	protected ItemStack getItem()
	{
		return new ItemStack(ResonantInduction.itemTransformer);
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

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		if (pass == 0)
		{
			RenderTransformer.render(this, pos.x, pos.y, pos.z);
		}
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		this.placementSide = ForgeDirection.getOrientation(nbt.getByte("side"));
		this.stepUp = nbt.getBoolean("stepUp");
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setByte("side", (byte) this.placementSide.ordinal());
		nbt.setBoolean("stepUp", this.stepUp);
	}

	@Override
	public String getType()
	{
		return "resonant_induction_transformer";
	}

	protected ForgeDirection getFacing()
	{
		if (this.placementSide != ForgeDirection.UP && this.placementSide != ForgeDirection.DOWN)
		{
			switch (this.face)
			{
				case 0:
					return ForgeDirection.UP;
				case 1:
					switch (this.placementSide)
					{
						case NORTH:
							return ForgeDirection.EAST;
						case SOUTH:
							return ForgeDirection.WEST;
						case EAST:
							return ForgeDirection.SOUTH;
						case WEST:
							return ForgeDirection.NORTH;
					}
				case 2:
					return ForgeDirection.DOWN;
				case 3:
					switch (this.placementSide)
					{
						case NORTH:
							return ForgeDirection.WEST;
						case SOUTH:
							return ForgeDirection.EAST;
						case EAST:
							return ForgeDirection.NORTH;
						case WEST:
							return ForgeDirection.SOUTH;
					}
			}
		}
		else
		{
			switch (this.face)
			{
				case 0:
					return ForgeDirection.NORTH;
				case 1:
					return ForgeDirection.EAST;
				case 2:
					return ForgeDirection.SOUTH;
				case 3:
					return ForgeDirection.WEST;
			}
		}
		return ForgeDirection.NORTH;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return direction == getFacing() || direction == getFacing().getOpposite();
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		if (from == this.getFacing().getOpposite())
		{
			TileEntity entity = VectorHelper.getTileEntityFromSide(this.world(), new universalelectricity.api.vector.Vector3(this.x(), this.y(), this.z()), this.getFacing());
			if (entity instanceof IEnergyInterface)
			{
				if (entity instanceof IVoltageInput)
				{
					long voltage = this.getVoltageOutput(from.getOpposite());
					if (voltage != ((IVoltageInput) entity).getVoltageInput(from))
					{
						((IVoltageInput) entity).onWrongVoltage(from, voltage);
					}
				}
				return ((IEnergyInterface) entity).onReceiveEnergy(from, receive, doReceive);
			}

		}
		return 0;
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
	{
		return 0;
	}

	@Override
	public long getVoltageOutput(ForgeDirection side)
	{
		if (side == this.getFacing())
		{
			TileEntity entity = VectorHelper.getTileEntityFromSide(this.world(), new universalelectricity.api.vector.Vector3(this.x(), this.y(), this.z()), this.getFacing().getOpposite());
			if (entity instanceof IConductor && ((IConductor) entity).getNetwork() instanceof IElectricalNetwork)
			{
				long voltage = ((IElectricalNetwork) ((IConductor) entity).getNetwork()).getVoltage();
				if (this.stepUp())
				{
					return voltage * this.multiplier;
				}
				else if (voltage > 0)
				{
					return voltage / this.multiplier;
				}
			}
			else if (entity instanceof IVoltageOutput)
			{
				return ((IVoltageOutput) entity).getVoltageOutput(side);
			}
		}
		return 0;
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item)
	{

		if (this.isUsableWrench(player, player.inventory.getCurrentItem(), x(), y(), z()))
		{
			if (this.world().isRemote)
			{
				return true;
			}
			this.damageWrench(player, player.inventory.getCurrentItem(), x(), y(), z());
			if (this.face < 3)
				this.face++;
			else
				this.face = 0;
			this.sendDescUpdate();

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				tile().notifyNeighborChange(dir.ordinal());
			}
			return true;
		}
		return false;
	}

	public boolean isUsableWrench(EntityPlayer entityPlayer, ItemStack itemStack, int x, int y, int z)
	{
		if (entityPlayer != null && itemStack != null)
		{
			Class wrenchClass = itemStack.getItem().getClass();

			/** UE and Buildcraft */
			try
			{
				Method methodCanWrench = wrenchClass.getMethod("canWrench", EntityPlayer.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
				return (Boolean) methodCanWrench.invoke(itemStack.getItem(), entityPlayer, x, y, z);
			}
			catch (NoClassDefFoundError e)
			{
			}
			catch (Exception e)
			{
			}

			/** Industrialcraft */
			try
			{
				if (wrenchClass == Class.forName("ic2.core.item.tool.ItemToolWrench") || wrenchClass == Class.forName("ic2.core.item.tool.ItemToolWrenchElectric"))
				{
					return itemStack.getItemDamage() < itemStack.getMaxDamage();
				}
			}
			catch (Exception e)
			{
			}
		}

		return false;
	}

	/**
	 * This function damages a wrench. Works with Buildcraft and Industrialcraft wrenches.
	 * 
	 * @return True if damage was successfull.
	 */
	public boolean damageWrench(EntityPlayer entityPlayer, ItemStack itemStack, int x, int y, int z)
	{
		if (this.isUsableWrench(entityPlayer, itemStack, x, y, z))
		{
			Class wrenchClass = itemStack.getItem().getClass();

			/** UE and Buildcraft */
			try
			{
				Method methodWrenchUsed = wrenchClass.getMethod("wrenchUsed", EntityPlayer.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
				methodWrenchUsed.invoke(itemStack.getItem(), entityPlayer, x, y, z);
				return true;
			}
			catch (Exception e)
			{
			}

			/** Industrialcraft */
			try
			{
				if (wrenchClass == Class.forName("ic2.core.item.tool.ItemToolWrench") || wrenchClass == Class.forName("ic2.core.item.tool.ItemToolWrenchElectric"))
				{
					Method methodWrenchDamage = wrenchClass.getMethod("damage", ItemStack.class, Integer.TYPE, EntityPlayer.class);
					methodWrenchDamage.invoke(itemStack.getItem(), itemStack, 1, entityPlayer);
					return true;
				}
			}
			catch (Exception e)
			{
			}
		}

		return false;
	}
}