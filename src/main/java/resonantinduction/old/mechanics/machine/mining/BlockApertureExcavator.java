package resonantinduction.old.mechanics.machine.mining;

import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import resonantinduction.core.prefab.block.BlockRI;
import calclavia.lib.content.IExtraInfo.IExtraBlockInfo;

import com.builtbroken.common.Pair;

/** @author Archadia */
public class BlockApertureExcavator extends BlockRI implements IExtraBlockInfo
{

    public BlockApertureExcavator()
    {
        super("Machine_ApertureExcavator");
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        return new TileApertureExcavator();
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair<String, Class<? extends TileEntity>>("TileApertureExcavator", TileApertureExcavator.class));
    }

    @Override
    public boolean hasExtraConfigs()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void loadExtraConfigs(Configuration config)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void loadOreNames()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
    {
        // TODO Auto-generated method stub
        
    }
}
