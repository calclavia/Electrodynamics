package resonantinduction.mechanical.process.grinder;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.Reference;
import calclavia.lib.prefab.block.BlockRotatable;
import calclavia.lib.render.block.BlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A block used to build machines.
 * 
 * @author Calclavia
 * 
 */
public class BlockGrindingWheel extends BlockRotatable implements ITileEntityProvider
{
	public BlockGrindingWheel(int id)
	{
		super(id, Material.wood);
		setTextureName(Reference.PREFIX + "material_steel_dark");
		setBlockBounds(0.05f, 0.05f, 0.05f, 0.95f, 0.95f, 0.95f);
		rotationMask = Byte.parseByte("111111", 2);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.ID;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		if (entity instanceof EntityItem)
		{
			((EntityItem) entity).age--;
		}

		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileGrinderWheel)
		{
			TileGrinderWheel tile = (TileGrinderWheel) tileEntity;

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

						if (!TileGrinderWheel.timer.containsKey((EntityItem) entity))
						{
							TileGrinderWheel.timer.put((EntityItem) entity, TileGrinderWheel.PROCESS_TIME);
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

			}

			if (tile.getAngularVelocity() != 0)
			{
				// Move entity based on the direction of the block.
				ForgeDirection dir = this.getDirection(world, x, y, z);
				dir = ForgeDirection.getOrientation(!(dir.ordinal() % 2 == 0) ? dir.ordinal() - 1 : dir.ordinal()).getOpposite();
				float speed = tile.getAngularVelocity() / 20;
				entity.addVelocity(dir.offsetX * speed, Math.random() * speed, dir.offsetZ * speed);
			}
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
