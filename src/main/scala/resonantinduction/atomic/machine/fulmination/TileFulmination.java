package resonantinduction.atomic.machine.fulmination;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraftforge.common.util.ForgeDirection;
import resonantinduction.core.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import resonant.lib.content.prefab.java.TileElectric;

/** Fulmination TileEntity */
public class TileFulmination extends TileElectric
{
    private static final long DIAN = 10000000000000L;

    public TileFulmination()
    {
        super(Material.iron);
        energy().setCapacity(DIAN * 2);
        this.blockHardness(10);
        this.blockResistance(25000);
    }

    @Override
    public void initiate()
    {
        super.initiate();
        FulminationHandler.INSTANCE.register(this);
    }

    @Override
    public void update()
    {
        super.update();
        // Slowly lose energy.
        energy().extractEnergy(10, true);
    }

    @Override
    public void invalidate()
    {
        FulminationHandler.INSTANCE.unregister(this);
        super.initiate();
    }
}
