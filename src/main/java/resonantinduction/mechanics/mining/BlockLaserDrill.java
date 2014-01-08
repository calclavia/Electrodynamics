package resonantinduction.mechanics.mining;

import java.util.Set;

import resonantinduction.assemblyline.AssemblyLine;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.prefab.BlockMachine;

/** @author Archadia */
public class BlockLaserDrill extends BlockMachine
{

    public BlockLaserDrill()
    {
        super(AssemblyLine.CONFIGURATION, "Machine_LaserDrill", Material.iron);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        return new TileLaserDrill();
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair<String, Class<? extends TileEntity>>("TileLaserDrill", TileLaserDrill.class));
    }

    @Override
    public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            TileEntity ent = world.getBlockTileEntity(x, y, z);
            if (ent instanceof TileMiningLaser)
            {
                ((TileMiningLaser) ent).rotateYaw(-10);
            }
        }
        return false;
    }

    @Override
    public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            TileEntity ent = world.getBlockTileEntity(x, y, z);
            if (ent instanceof TileMiningLaser)
            {
                ((TileMiningLaser) ent).rotateYaw(10);
            }
        }
        return false;
    }
}
