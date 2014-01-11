package resonantinduction.old.mechanics.machine.mining;

import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import resonantinduction.core.prefab.block.BlockMachine;
import resonantinduction.old.client.render.BlockRenderHelper;

import com.builtbroken.common.Pair;

import dark.lib.IExtraInfo.IExtraBlockInfo;

/** @author Archadia */
public class BlockFrackingPipe extends BlockMachine implements IExtraBlockInfo
{

	public BlockFrackingPipe()
	{
		super("Fracking_Pipe");
	}

	@Override
	public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
	{

	}

	@Override
	public int getRenderType()
	{
		return BlockRenderHelper.instance.getRenderId();
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileFrackingPipe();
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
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        // TODO Auto-generated method stub
        
    }
}
