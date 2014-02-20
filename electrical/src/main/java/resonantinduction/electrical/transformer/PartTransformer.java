package resonantinduction.electrical.transformer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.prefab.part.PartFace;
import resonantinduction.electrical.Electrical;
import universalelectricity.api.electricity.IElectricalNetwork;
import universalelectricity.api.electricity.IVoltageInput;
import universalelectricity.api.electricity.IVoltageOutput;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyInterface;
import universalelectricity.api.vector.VectorHelper;
import calclavia.lib.utility.WrenchUtility;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TFacePart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TODO: We can't use face parts, need to use thicker ones. Also, transformer is currently NO-OP
 * 
 * @author Calclavia
 * 
 */
public class PartTransformer extends PartFace implements IVoltageOutput, IEnergyInterface
{

	/** Step the voltage up */
	private boolean stepUp;

	/** Amount to mulitply the step by (up x2. down /2) */
	public byte multiplier = 2;

	public void preparePlacement(int side, int facing)
	{
		this.placementSide = ForgeDirection.getOrientation(side);
		this.facing = (byte) (facing - 2);
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		super.readDesc(packet);
		multiplier = packet.readByte();
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		super.writeDesc(packet);
		packet.writeByte(multiplier);
	}

	public boolean stepUp()
	{
		return this.stepUp;
	}

	@Override
	public boolean doesTick()
	{
		return false;
	}

	protected ItemStack getItem()
	{
		return new ItemStack(Electrical.itemTransformer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float frame, int pass)
	{
		if (pass == 0)
		{
			RenderTransformer.INSTANCE.render(this, pos.x, pos.y, pos.z);
		}
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		stepUp = nbt.getBoolean("stepUp");
		multiplier = nbt.getByte("multiplier");
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setBoolean("stepUp", this.stepUp);
		nbt.setByte("multiplier", multiplier);
	}

	@Override
	public String getType()
	{
		return "resonant_induction_transformer";
	}

	@Override
	public boolean canConnect(ForgeDirection direction, Object obj)
	{
		return obj instanceof IEnergyInterface && (direction == getAbsoluteFacing() || direction == getAbsoluteFacing().getOpposite());
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
		if (WrenchUtility.isUsableWrench(player, player.inventory.getCurrentItem(), x(), y(), z()))
		{
			if (!this.world().isRemote)
			{
				if (player.isSneaking())
				{
					multiplier = (byte) ((multiplier + 1) % 3);
					sendDescUpdate();
					return true;
				}

				WrenchUtility.damageWrench(player, player.inventory.getCurrentItem(), x(), y(), z());

				facing = (byte) ((facing + 1) % 4);

				sendDescUpdate();

				tile().notifyPartChange(this);
			}

			return true;
		}

		return false;
	}

}