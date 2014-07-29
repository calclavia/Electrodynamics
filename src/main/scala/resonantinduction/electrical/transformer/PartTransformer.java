package resonantinduction.electrical.transformer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.lib.utility.WrenchUtility;
import resonantinduction.core.prefab.part.PartFace;
import resonantinduction.electrical.Electrical;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.core.grid.INode;
import universalelectricity.api.core.grid.INodeProvider;
import universalelectricity.api.core.grid.electric.IElectricNode;
import universalelectricity.core.UniversalElectricity;
import universalelectricity.core.grid.node.ElectricNode;

/**
 * TODO: We can't use face parts, need to use thicker ones. Also, transformer is currently NO-OP
 * 
 * @author Calclavia
 * 
 */
@UniversalClass
public class PartTransformer extends PartFace implements INodeProvider
{

	/** Step the voltage up */
	private boolean stepUp = true;

	/** Amount to mulitply the step by (up x2. down /2) */
	public byte multiplier = 2;

    protected ElectricNode node;

	@Override
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

	@Override
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
		nbt.setBoolean("stepUp", stepUp);
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
	public long getVoltageOutput(ForgeDirection from)
	{
		if (from == getAbsoluteFacing().getOpposite())
		{
			TileEntity inputTile = VectorHelper.getTileEntityFromSide(this.world(), new universalelectricity.core.transform.vector.Vector3(x(), y(), z()), getAbsoluteFacing());

			long inputVoltage = UniversalElectricity.DEFAULT_VOLTAGE;

			if (inputTile instanceof IConductor)
			{
				IConductor conductor = (IConductor) ((IConductor) inputTile).getInstance(placementSide);

				if (conductor != null)
					if (conductor.getNetwork() instanceof IElectricalNetwork)
						inputVoltage = ((IElectricalNetwork) conductor.getNetwork()).getVoltage();
			}
			else if (inputTile instanceof IVoltageOutput)
			{
				inputVoltage = ((IVoltageOutput) inputTile).getVoltageOutput(from);
			}

			if (inputVoltage <= 0)
				inputVoltage = 240;

			if (this.stepUp())
				return inputVoltage * (this.multiplier + 2);
			else
				return inputVoltage / (this.multiplier + 2);
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

		stepUp = !stepUp;

		if (!world().isRemote)
			player.addChatMessage("Transformer set to step " + (stepUp ? "up" : "down") + ".");

		return true;
	}

    @Override
    public INode getNode(Class<INode> nodeType, ForgeDirection from) {
        if(nodeType.isAssignableFrom(IElectricNode.class))
        {
            if (node == null) {
                node = new ElectricNode(this);
            }
            return node;
        }
        return null;
    }
}