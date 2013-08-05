/**
 * 
 */
package resonantinduction.battery;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import resonantinduction.api.IBattery;
import resonantinduction.base.BlockBase;
import resonantinduction.render.BlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A block that detects power.
 * 
 * @author Calclavia
 * 
 */
public class BlockBattery extends BlockBase implements ITileEntityProvider
{
	public BlockBattery(int id)
	{
		super("battery", id);
		this.func_111022_d(ResonantInduction.PREFIX + "machine");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float xClick, float yClick, float zClick)
	{
		if (entityPlayer.isSneaking())
		{
			world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.ROTATION_MATRIX[world.getBlockMetadata(x, y, z)][side], 3);
		}
		else
		{
			if (entityPlayer.getCurrentEquippedItem() != null)
			{
				if (entityPlayer.getCurrentEquippedItem().getItem() instanceof IBattery)
				{
					if (side != 0 && side != 1)
					{
						/**
						 * Place cells into block. 2 Dimensional Click Zone
						 */
						float xHit = 0;
						float yHit = yClick;

						if (side == 2 || side == 3)
						{
							xHit = xClick;

							if (side == 2)
							{
								xHit = 1 - xHit;
							}
						}
						else if (side == 4 || side == 5)
						{
							xHit = zClick;

							if (side == 5)
							{
								xHit = 1 - xHit;
							}
						}

						// Convert to quadrant coords.
						xHit -= 0.5f;
						yHit -= 0.5f;

						// Quadrant 1
						if (xHit > 0 && yHit > 0)
						{

						}// Quadrant 2
						if (xHit > 0 && yHit < 0)
						{

						}// Quadrant 3
						if (xHit < 0 && yHit < 0)
						{

						}// Quadrant 4
						if (xHit < 0 && yHit > 0)
						{

						}
					}
				}
			}
		}
		
		if(!world.isRemote)
		{
			entityPlayer.openGui(ResonantInduction.INSTNACE, 0, world, x, y, z);
		}

		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id)
	{
		if (!world.isRemote)
		{
			if (id == blockID)
			{
				TileEntityBattery battery = (TileEntityBattery) world.getBlockTileEntity(x, y, z);

				battery.update();
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
	{
		if (!world.isRemote)
		{
			TileEntityBattery battery = (TileEntityBattery) world.getBlockTileEntity(x, y, z);

			battery.update();
		}
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

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return BlockRenderingHandler.INSTANCE.getRenderId();
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityBattery();
	}
}
