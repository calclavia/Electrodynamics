package resonantinduction.mechanical.fluid.pipe;

import codechicken.lib.vec.Cuboid6;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import resonant.lib.type.EvictingList;
import resonantinduction.core.prefab.part.PartFramedNode;
import resonantinduction.mechanical.Mechanical;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.render.CCRenderState;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TSlottedPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import universalelectricity.api.core.grid.INode;

import java.util.Set;

/** Fluid transport pipe
 *
 * @author Calclavia, Darkguardsman */
public class PartPipe extends PartFramedNode<EnumPipeMaterial> implements TSlottedPart, JNormalOcclusion, IFluidHandler
{
    protected final FluidTank tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
    /** Computes the average fluid for client to render. */
    private EvictingList<Integer> averageTankData = new EvictingList<Integer>(20);
    private boolean markPacket = true;

    public PartPipe()
    {
        super(null);
        setMaterial(0);
        this.setRequiresInsulation(false);
        setNode((INode) new PipePressureNode(this));
    }

    @Override
    public void setMaterial(int i)
    {
        setMaterial(EnumPipeMaterial.values()[i]);
    }

    @Override
    public int getMaterialID() {
        return 0;
    }

    @Override
    public void setMaterial(EnumPipeMaterial material)
    {
        super.setMaterial(material);
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
    public ItemStack getItem()
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
    public void drawBreaking(RenderBlocks renderBlocks)
    {
        CCRenderState.reset();
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
    public Set<Cuboid6> getOcclusionBoxes() {
        return null;
    }

    @Override
    public int getSlotMask() {
        return 0;
    }
}