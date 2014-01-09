package resonantinduction.old.mechanics.machine.mining;

import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.Settings;
import resonantinduction.old.client.render.BlockRenderHelper;
import calclavia.lib.prefab.block.BlockMachine;

import com.builtbroken.common.Pair;

/**
 * Mining laser Prototype mainly used for crafting but can be used in the same way as Excavator.
 * Creates four lasers from the side it is pointing in to mine away blocks
 * 
 * @author DarkGuardsman
 */
public class BlockMiningLaser extends BlockMachine
{
	public BlockMiningLaser()
	{
		super(Settings.CONFIGURATION, "LaserMiner", Material.iron);
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileMiningLaser();
	}

	@Override
	public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
	{
		list.add(new Pair<String, Class<? extends TileEntity>>("TileMiningLaser", TileMiningLaser.class));
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

}
