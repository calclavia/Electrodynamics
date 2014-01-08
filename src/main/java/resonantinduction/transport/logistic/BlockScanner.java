package resonantinduction.transport.logistic;

import java.util.Set;

import resonantinduction.AssemblyLine;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.api.UniversalElectricity;

import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.prefab.BlockMachine;

/** @author Archadia */
public class BlockScanner extends BlockMachine
{

    public BlockScanner()
    {
        super(ResonantInductionTransport.CONFIGURATION, "Machine_OreScanner", UniversalElectricity.machine);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        return new TileScanner();
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair<String, Class<? extends TileEntity>>("TileOreScanner", TileScanner.class));
    }
}
