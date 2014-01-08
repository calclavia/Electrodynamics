package resonantinduction.assemblyline.fluid.prefab;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;

import com.builtbroken.minecraft.interfaces.IToolReadOut;
import com.builtbroken.minecraft.prefab.TileEntityAdvanced;
import com.builtbroken.minecraft.tilenetwork.ITileConnector;
import com.builtbroken.minecraft.tilenetwork.prefab.NetworkTileEntities;

public abstract class TileEntityFluidDevice extends TileEntityAdvanced implements IToolReadOut, ITileConnector
{
    public Random random = new Random();

    @Override
    public void invalidate()
    {
        super.invalidate();
        NetworkTileEntities.invalidate(this);
    }

    @Override
    public String getMeterReading(EntityPlayer user, ForgeDirection side, EnumTools tool)
    {
        if (tool != null && tool == EnumTools.PIPE_GUAGE)
        {
            return " IndirectlyPower:" + this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
        }
        return null;
    }
}
