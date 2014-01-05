package resonantinduction.machine.grinder;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.base.BlockRotatableBase;

/**
 * A block used to build machines.
 * 
 * @author Calclavia
 * 
 */
public class BlockGrinderWheel extends BlockRotatableBase implements ITileEntityProvider
{
	public BlockGrinderWheel(int id)
	{
		super("grindingWheel", id);
		this.setBlockBounds(0.05f, 0.05f, 0.05f, 0.95f, 0.95f, 0.95f);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		TileGrinderWheel tile = (TileGrinderWheel) world.getBlockTileEntity(x, y, z);

		if (tile.canWork())
		{
			if (entity instanceof EntityItem)
			{
				if (tile.canGrind(((EntityItem) entity).getEntityItem()))
				{
					if (tile.grindingItem == null)
					{
						tile.grindingItem = (EntityItem) entity;
					}

					if (!tile.getTimer().containsKey((EntityItem) entity))
					{
						tile.getTimer().put((EntityItem) entity, TileGrinderWheel.DEFAULT_TIME);
					}
				}
				else
				{
					entity.setPosition(entity.posX, entity.posY - 1.2, entity.posZ);
				}
			}
			else
			{
				entity.attackEntityFrom(DamageSource.cactus, 2);
			}

			// Move entity based on the direction of the block.
			ForgeDirection dir = this.getDirection(world, x, y, z);
			entity.motionX += dir.offsetX * 0.1;
			entity.motionZ += dir.offsetZ * 0.1;
			entity.motionY += 0.3;
			entity.isAirBorne = true;
		}
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
	public TileEntity createNewTileEntity(World world)
	{
		return new TileGrinderWheel();
	}
}
