package resonantinduction.mechanical.fluid.pipe;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.fluid.IPressurizedNode;
import resonantinduction.core.fluid.PressureNetwork;
import resonantinduction.core.prefab.part.PartFramedConnection;
import resonantinduction.mechanical.Mechanical;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.IconTransformation;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Translation;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TSlottedPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartPipe extends PartFramedConnection<EnumPipeMaterial, IPressurizedNode, PressureNetwork> implements IPressurizedNode, TSlottedPart, JNormalOcclusion, IHollowConnect
{
	protected FluidTank tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
	private int pressure;
	private boolean markPacket = true;

	public PartPipe(int typeID)
	{
		super();
		material = EnumPipeMaterial.values()[typeID];
		requiresInsulation = false;
	}

	public PartPipe()
	{
		this(EnumPipeMaterial.COPPER.ordinal());
	}

	@Override
	public String getType()
	{
		return "resonant_induction_pipe";
	}

	@Override
	public void update()
	{
		super.update();

		if (!world().isRemote && markPacket)
		{
			sendFluidUpdate();
			markPacket = false;
		}
	}

	public void sendFluidUpdate()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		tank.writeToNBT(nbt);
		tile().getWriteStream(this).writeByte(3).writeNBTTagCompound(nbt);
	}

	@Override
	public void read(MCDataInput packet, int packetID)
	{
		if (packetID == 3)
		{
			tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
			tank.readFromNBT(packet.readNBTTagCompound());
		}
		else
		{
			super.read(packet, packetID);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(codechicken.lib.vec.Vector3 pos, float frame, int pass)
	{
		RenderPipe.INSTANCE.render(this, pos.x, pos.y, pos.z, frame);
	}

	@Override
	public void setMaterial(int i)
	{
		setMaterial(EnumPipeMaterial.values()[i]);
	}

	@Override
	protected ItemStack getItem()
	{
		return new ItemStack(Mechanical.itemPipe);
	}

	/** Fluid network methods. */
	@Override
	public PressureNetwork getNetwork()
	{
		if (this.network == null)
		{
			this.network = new PressureNetwork();
			this.network.addConnector(this);
		}

		return this.network;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (!world().isRemote)
		{
			if (doFill)
			{
				markPacket = true;
			}

			return tank.fill(resource, doFill);
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if (!world().isRemote)
		{
			if (doDrain)
			{
				markPacket = true;
			}

			return tank.drain(resource.amount, doDrain);
		}
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if (!world().isRemote)
		{
			if (doDrain)
			{
				markPacket = true;
			}

			return tank.drain(maxDrain, doDrain);
		}
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] { tank.getInfo() };
	}

	@Override
	public void onFluidChanged()
	{
	}

	@Override
	public FluidTank getInternalTank()
	{
		if (this.tank == null)
		{
			this.tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
		}

		return this.tank;
	}

	@Override
	protected boolean canConnectTo(TileEntity tile, ForgeDirection dir)
	{
		Object obj = tile instanceof TileMultipart ? ((TileMultipart) tile).partMap(ForgeDirection.UNKNOWN.ordinal()) : tile;

		if (obj instanceof PartPipe)
		{
			if (this.getMaterial() == ((PartPipe) obj).getMaterial())
			{
				return getColor() == ((PartPipe) obj).getColor() || (getColor() == DEFAULT_COLOR || ((PartPipe) obj).getColor() == DEFAULT_COLOR);
			}

			return false;
		}

		return tile instanceof IFluidHandler;
	}

	@Override
	protected IPressurizedNode getConnector(TileEntity tile)
	{
		if (tile instanceof IPressurizedNode)
			if (((IPressurizedNode) tile).getInstance(ForgeDirection.UNKNOWN) instanceof IPressurizedNode)
				return (IPressurizedNode) ((IPressurizedNode) tile).getInstance(ForgeDirection.UNKNOWN);

		return null;
	}

	@Override
	public int getPressure(ForgeDirection dir)
	{
		return pressure;
	}

	@Override
	public void setPressure(int amount)
	{
		pressure = amount;
	}

	@Override
	public int getMaxFlowRate()
	{
		return 100;
	}

	@Override
	public void drawBreaking(RenderBlocks renderBlocks)
	{
		CCRenderState.reset();
		RenderUtils.renderBlock(sides[6], 0, new Translation(x(), y(), z()), new IconTransformation(ResonantInduction.blockMachinePart.getIcon(0, 0)), null);
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		tank.writeToNBT(nbt);
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		tank.readFromNBT(nbt);
	}

	@Override
	public boolean canFlow()
	{
		return true;
	}

}