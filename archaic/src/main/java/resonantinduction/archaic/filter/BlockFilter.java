package resonantinduction.archaic.filter;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import resonantinduction.core.prefab.imprint.BlockImprintable;

/**
 * Used for filtering liquid mixtures
 * 
 * @author Calclavia
 * 
 */
public class BlockFilter extends BlockImprintable
{
	public BlockFilter(int id)
	{
		super(id, Material.iron);
		setBlockBounds(0.01f, 0.01f, 0.01f, 0.99f, 0.99f, 0.99f);
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity entity)
	{
		if (entity == null)
			return;

		if (entity instanceof EntityItem)
		{
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

			if (tileEntity instanceof TileFilter)
			{
				if (((TileFilter) tileEntity).isFiltering(((EntityItem) entity).getEntityItem()))
				{
					return;
				}
			}
		}

		super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileFilter();
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

}
