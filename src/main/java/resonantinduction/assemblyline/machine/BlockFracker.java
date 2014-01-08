package resonantinduction.assemblyline.machine;

import java.util.Set;

import resonantinduction.assemblyline.AssemblyLine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.api.UniversalElectricity;

import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.prefab.BlockMachine;

/** @author Archadia */
public class BlockFracker extends BlockMachine
{

    public BlockFracker()
    {
        super(AssemblyLine.CONFIGURATION, "Machine_Fracker", UniversalElectricity.machine);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int a, float b, float c, float d)
    {
        //  player.openGui(MechanizedMining.instance, 1, world, x, y, z);
        TileFracker tile = (TileFracker) world.getBlockTileEntity(x, y, z);

        System.out.println(tile.tank.getFluidAmount());
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        return new TileFracker();
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair<String, Class<? extends TileEntity>>("TileFracker", TileFracker.class));
    }
}
