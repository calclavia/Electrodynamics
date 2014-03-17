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
import resonantinduction.core.grid.fluid.FluidPressureNode;
import resonantinduction.core.grid.fluid.IPressureNodeProvider;
import resonantinduction.core.prefab.part.PartFramedNode;
import resonantinduction.mechanical.Mechanical;
import calclavia.lib.java.EvictingList;
import calclavia.lib.utility.WorldUtility;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.IconTransformation;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Translation;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TSlottedPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartPipe extends PartFramedNode<EnumPipeMaterial, FluidPressureNode, IPressureNodeProvider> implements IPressureNodeProvider, TSlottedPart, JNormalOcclusion, IHollowConnect
{
	protected final FluidTank tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
	/**
	 * Computes the average fluid for client to render.
	 */
	private EvictingList<Integer> averageTankData = new EvictingList<Integer>(20);
	private boolean markPacket = true;

	public PartPipe()
	{
		super(null);
		material = EnumPipeMaterial.values()[0];
		requiresInsulation = false;

		node = new FluidPressureNode(this)
		{
			@Override
			public void doRecache()
			{

				connections.clear();

				if (world() != null)
				{
					byte previousConnections = getAllCurrentConnections();
					currentConnections = 0;

					for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
					{
						TileEntity tile = position().translate(dir).getTileEntity(world());

						if (tile instanceof IFluidHandler)
						{
							if (tile instanceof IPressureNodeProvider)
							{
								FluidPressureNode check = ((IPressureNodeProvider) tile).getNode(FluidPressureNode.class, dir.getOpposite());

								if (check != null && canConnect(dir, check) && check.canConnect(dir.getOpposite(), this))
								{
									currentConnections = WorldUtility.setEnableSide(currentConnections, dir, true);
									connections.put(check, dir);

								}
							}
							else if (canConnect(dir, tile))
							{
								currentConnections = WorldUtility.setEnableSide(currentConnections, dir, true);
								connections.put(tile, dir);
							}
						}
					}

					/** Only send packet updates if visuallyConnected changed. */
					if (!world().isRemote && previousConnections != currentConnections)
					{
						sendConnectionUpdate();
					}
				}
			}

			@Override
			public boolean canConnect(ForgeDirection from, Object source)
			{
				if (!isBlockedOnSide(from))
				{
					if (source instanceof FluidPressureNode)
					{
						FluidPressureNode otherNode = (FluidPressureNode) source;

						if (otherNode.parent instanceof PartPipe)
						{
							PartPipe otherPipe = (PartPipe) otherNode.parent;

							if (!otherPipe.isBlockedOnSide(from.getOpposite()) && getMaterial() == otherPipe.getMaterial())
							{
								return getColor() == otherPipe.getColor() || (getColor() == DEFAULT_COLOR || otherPipe.getColor() == DEFAULT_COLOR);
							}

							return false;
						}
					}

					return super.canConnect(from, source) || source instanceof IFluidHandler;
				}

				return false;
			}
		};

	}

	@Override
	public void setMaterial(int i)
	{
		setMaterial(EnumPipeMaterial.values()[i]);

	}

	@Override
	public void setMaterial(EnumPipeMaterial material)
	{
		this.material = material;
		node.maxFlowRate = getMaterial().maxFlowRate;
		node.maxPressure = getMaterial().maxPressure;
		tank.setCapacity(node.maxFlowRate);
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

		averageTankData.add(tank.getFluidAmount());

		if (!world().isRemote && markPacket)
		{
			sendFluidUpdate();
			markPacket = false;
		}
	}

	public void sendFluidUpdate()
	{
		NBTTagCompound nbt = new NBTTagCompound();

		int averageAmount = 0;

		if (averageTankData.size() > 0)
		{
			for (int i = 0; i < averageTankData.size(); i++)
			{
				averageAmount += averageTankData.get(i);
			}

			averageAmount /= averageTankData.size();
		}

		FluidTank tempTank = tank.getFluid() != null ? new FluidTank(tank.getFluid().getFluid(), averageAmount, tank.getCapacity()) : new FluidTank(tank.getCapacity());
		tempTank.writeToNBT(nbt);
		tile().getWriteStream(this).writeByte(3).writeInt(tank.getCapacity()).writeNBTTagCompound(nbt);
	}

	@Override
	public void read(MCDataInput packet, int packetID)
	{
		if (packetID == 3)
		{
			tank.setCapacity(packet.readInt());
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
	protected ItemStack getItem()
	{
		return new ItemStack(Mechanical.itemPipe, 1, getMaterialID());
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
		return drain(from, resource.amount, doDrain);
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
	public FluidTank getPressureTank()
	{
		return tank;
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
		node.maxFlowRate = getMaterial().maxFlowRate;
		node.maxPressure = getMaterial().maxPressure;
	}

	@Override
	public void onFluidChanged()
	{
	}
}