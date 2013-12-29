package resonantinduction.transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.ResonantInduction;
import resonantinduction.multimeter.PartMultimeter.DetectMode;
import universalelectricity.api.electricity.IElectricalNetwork;
import universalelectricity.api.electricity.IVoltageInput;
import universalelectricity.api.electricity.IVoltageOutput;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyInterface;
import universalelectricity.api.vector.VectorHelper;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.TextureUtils;
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
    public int placementSide;
    /** Direction this block faces */
    public int face = 0;
    /** Step the voltage up */
    private boolean stepUp;
    /** Amount to mulitply the step by (up x2. down /2) */
    public int multiplier = 2;

    public void preparePlacement(int side, int itemDamage)
    {
        this.placementSide = (byte) (side ^ 1);
    }

    @Override
    public void readDesc(MCDataInput packet)
    {
        this.placementSide = packet.readByte();
        this.face = packet.readByte();
    }

    @Override
    public void writeDesc(MCDataOutput packet)
    {
        packet.writeByte(this.placementSide);
        packet.writeByte(this.face);
    }

    public boolean stepUp()
    {
        return this.stepUp;
    }

    @Override
    public int getSlotMask()
    {
        return 1 << this.placementSide;
    }

    @Override
    public Cuboid6 getBounds()
    {
        return FaceMicroClass.aBounds()[0x10 | this.placementSide];
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
        return Arrays.asList(oBoxes[this.placementSide]);
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
        this.placementSide = nbt.getByte("side");
        this.stepUp = nbt.getBoolean("stepUp");
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        super.save(nbt);
        nbt.setByte("side", (byte) this.placementSide);
        nbt.setBoolean("stepUp", this.stepUp);
    }

    @Override
    public String getType()
    {
        return "resonant_induction_transformer";
    }

    protected ForgeDirection getFacing()
    {
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
        return 0;
    }
}