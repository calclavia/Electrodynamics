package resonantinduction.old.mechanics.machine.mining;

import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import resonantinduction.core.prefab.block.BlockRI;
import calclavia.lib.content.IExtraInfo.IExtraBlockInfo;

import com.builtbroken.common.Pair;

/** @author Archadia */
public class BlockFracker extends BlockRI implements IExtraBlockInfo
{

	public BlockFracker()
	{
		super("Machine_Fracker");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int a, float b, float c, float d)
	{
		// player.openGui(MechanizedMining.instance, 1, world, x, y, z);
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
