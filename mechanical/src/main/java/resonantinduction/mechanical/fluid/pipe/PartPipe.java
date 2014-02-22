package resonantinduction.mechanical.fluid.pipe;

import net.minecraft.client.renderer.RenderBlocks;
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
import resonantinduction.api.mechanical.fluid.IFluidNetwork;
import resonantinduction.api.mechanical.fluid.IFluidPipe;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.prefab.fluid.PipeNetwork;
import resonantinduction.core.prefab.part.PartFramedConnection;
import resonantinduction.mechanical.Mechanical;
import universalelectricity.api.energy.IConductor;
import calclavia.lib.utility.WrenchUtility;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.IconTransformation;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Translation;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.JIconHitEffects;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TSlottedPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartPipe extends PartFramedConnection<EnumPipeMaterial, IFluidPipe, IFluidNetwork> implements IFluidPipe, TSlottedPart, JNormalOcclusion, IHollowConnect, JIconHitEffects
{
	protected FluidTank tank = new FluidTank(1 * FluidContainerRegistry.BUCKET_VOLUME);
	private boolean isExtracting = false;

	public PartPipe()
	{
		super();
		material = EnumPipeMaterial.COPPER;
	}

	public PartPipe(int typeID)
	{
		material = EnumPipeMaterial.values()[typeID];
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

		if (!world().isRemote)
		{
			if (isExtracting && getNetwork().getTank().getFluidAmount() < getNetwork().getTank().getCapacity())
			{

				for (int i = 0; i < this.getConnections().length; i++)
				{
					Object obj = this.getConnections()[i];

					if (obj instanceof IFluidHandler)
					{
						FluidStack drain = ((IFluidHandler) obj).drain(ForgeDirection.getOrientation(i).getOpposite(), getMaxFlowRate(), true);
						fill(null, drain, true);
					}
				}
			}
		}
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
		if (!world().isRemote)
			System.out.println(getNetwork());
		if (WrenchUtility.isUsableWrench(player, player.getCurrentEquippedItem(), x(), y(), z()))
		{
			if (!world().isRemote)
			{
				isExtracting = !isExtracting;
				player.addChatMessage("Pipe extraction mode: " + isExtracting);
				WrenchUtility.damageWrench(player, player.getCurrentEquippedItem(), x(), y(), z());
			}
			return true;
		}

		return super.activate(player, part, item);
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
	public IFluidNetwork getNetwork()
	{
		if (this.network == null)
		{
			this.network = new PipeNetwork();
			this.network.addConnector(this);
		}
		return this.network;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return getNetwork().fill(this, from, resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
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
		return this.getNetwork().getTankInfo();
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
		return tile instanceof IFluidHandler;
	}

	@Override
	protected IFluidPipe getConnector(TileEntity tile)
	{
		if (tile instanceof IFluidPipe)
			if (((IFluidPipe) tile).getInstance(ForgeDirection.UNKNOWN) instanceof IFluidPipe)
				return (IFluidPipe) ((IFluidPipe) tile).getInstance(ForgeDirection.UNKNOWN);

		return null;
	}

	@Override
	public int getPressureIn(ForgeDirection side)
	{
		return 0;
	}

	@Override
	public int getPressure()
	{
		if (this.getNetwork() != null)
		{
			return this.getNetwork().getPressure();
		}
		return 0;
	}

	@Override
	public void onWrongPressure(ForgeDirection side, int pressure)
	{

	}

	@Override
	public int getMaxPressure()
	{
		return 1000;
	}

	@Override
	public int getMaxFlowRate()
	{
		return FluidContainerRegistry.BUCKET_VOLUME;
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
		nbt.setBoolean("isExtracting", isExtracting);
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		isExtracting = nbt.getBoolean("isExtracting");
	}

	@Override
	public boolean canFlow()
	{
		return !isExtracting;
	}

}