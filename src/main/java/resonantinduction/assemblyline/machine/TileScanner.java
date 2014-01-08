package resonantinduction.assemblyline.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.prefab.TileEntityEnergyMachine;

/** @author Archadia, DarkCow */
public class TileScanner extends TileEntityEnergyMachine
{

    private ArrayList<Integer> validTarget = new ArrayList<Integer>();
    private Vector3 coord;
    private Vector3 coordDown;
    private Vector3 scanSize = new Vector3(15, 1, 15);
    private Vector3 scanLocation;

    public boolean enabled = true;

    public TileScanner()
    {
        super(500); //500W

    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (coord == null || this.xCoord != coord.intX() || this.yCoord != coord.intY() || this.zCoord != coord.intZ())
        {
            coord = new Vector3(this);
            coordDown = coord.clone().translate(new Vector3(0, -1, 0));
        }
        if (!worldObj.isRemote)
        {
            for (byte i = 0; i < 5 && this.canFunction(); i++)
            {
                scanArea();
            }
        }
    }

    @Override
    public boolean canFunction()
    {
        return super.canFunction() && this.enabled;
    }

    /** Scans the area for valid blocks */
    protected void scanArea()
    {
        if (scanLocation == null)
        {
            this.scanLocation = this.coordDown.clone();
        }
        @SuppressWarnings("unchecked")
        HashMap<Vector3, Pair<Integer, Integer>> blocks = BlockMapUtil.getBlocksInGrid(this.worldObj, this.coordDown, this.scanSize);
        for (Entry<Vector3, Pair<Integer, Integer>> entry : blocks.entrySet())
        {
            int blockID = entry.getValue().left();
            int meta = entry.getValue().right();
        }
        //Update pos logic
        this.scanLocation.translate(new Vector3(0, -1, 0));
        if (this.scanLocation.intY() == 0)
        {
            this.scanLocation = this.coordDown.clone();
        }
        //Do logic here to sort out the returned blocks with what you want
    }

    @Override
    public boolean canConnect(ForgeDirection dir)
    {
        return true;
    }

}