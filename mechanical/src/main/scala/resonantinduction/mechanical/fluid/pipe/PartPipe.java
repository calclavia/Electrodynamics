package resonantinduction.mechanical.fluid.pipe;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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
import resonant.api.grid.INode;
import resonant.core.ResonantEngine;
import resonant.lib.type.EvictingList;
import resonant.lib.utility.WorldUtility;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.grid.fluid.FluidPressureNode;
import resonantinduction.core.grid.fluid.IPressureNodeProvider;
import resonantinduction.core.prefab.part.PartFramedNode;
import resonantinduction.mechanical.Mechanical;
import resonantinduction.mechanical.energy.grid.MechanicalNodeFrame;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.IconTransformation;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Translation;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.ControlKeyModifer;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TSlottedPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Fluid transport pipe
 * 
 * @author Calclavia, Darkguardsman */
public class PartPipe extends PartFramedNode<EnumPipeMaterial, FluidPressureNode, IPressureNodeProvider> implements IPressureNodeProvider, TSlottedPart, JNormalOcclusion, IHollowConnect
{
    protected final FluidTank tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
    /** Computes the average fluid for client to render. */
    private EvictingList<Integer> averageTankData = new EvictingList<Integer>(20);
    private boolean markPacket = true;
    private PipeNodeFrame frame = null;

    public PartPipe()
    {
        super(null);
        material = EnumPipeMaterial.values()[0];
        requiresInsulation = false;
        node = new PipePressureNode(this);
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

        if (frame != null)
        {
            frame.update();
        }
    }

    /** Sends fluid level to the client to be used in the renderer */
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
        RenderUtils.renderBlock(sides[6], 0, new Translation(x(), y(), z()), new IconTransformation(ResonantInduction.blockIndustrialStone.getIcon(0, 0)), null);
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

    @Override
    public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack itemStack)
    {
        if (ResonantEngine.runningAsDev)
        {
            if (itemStack != null && !world().isRemote)
            {
                if (itemStack.getItem().itemID == Item.stick.itemID)
                {
                    //Set the nodes debug mode
                    if (ControlKeyModifer.isControlDown(player))
                    {
                        //Opens a debug GUI
                        if (frame == null)
                        {
                            frame = new PipeNodeFrame(this);
                            frame.showDebugFrame();
                        } //Closes the debug GUI
                        else
                        {
                            frame.closeDebugFrame();
                            frame = null;
                        }
                    }
                }
            }
        }
        return super.activate(player, hit, itemStack);
    }
    
    @Override
    public void onWorldSeparate()
    {
        super.onWorldSeparate();
        if (frame != null)
        {
            frame.closeDebugFrame();
        }
    }
}