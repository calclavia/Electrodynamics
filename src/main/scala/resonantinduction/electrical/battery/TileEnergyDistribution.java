package resonantinduction.electrical.battery;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import universalelectricity.core.transform.vector.Vector3;
import resonant.lib.content.prefab.java.TileElectric;

public class TileEnergyDistribution extends TileElectric
{
    public boolean markClientUpdate = false;
    public boolean markDistributionUpdate = false;
    public long renderEnergyAmount = 0;
    private EnergyDistributionNetwork network;

    public TileEnergyDistribution()
    {
        super(null);
    }

    public TileEnergyDistribution(Material material)
    {
        super(material);
    }

    @Override
    public void update()
    {
        super.update();

        if (!this.worldObj.isRemote)
        {
            if (markDistributionUpdate && ticks() % 5 == 0)
            {
                //TODO update node
                markDistributionUpdate = false;
            }

            if (markClientUpdate && ticks() % 5 == 0)
            {
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        }
    }

}
