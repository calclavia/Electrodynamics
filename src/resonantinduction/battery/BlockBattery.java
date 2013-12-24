/**
 * 
 */
package resonantinduction.battery;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import resonantinduction.api.ICapacitor;
import resonantinduction.base.BlockBase;
import resonantinduction.base.ListUtil;
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
		this.setTextureName(ResonantInduction.PREFIX + "machine");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
	{
		if (!world.isRemote)
		{
			System.out.println(((TileBattery) world.getBlockTileEntity(x, y, z)).structure.hashCode());
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
				TileBattery battery = (TileBattery) world.getBlockTileEntity(x, y, z);
				battery.updateStructure();
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
	{
		if (!world.isRemote)
		{
			TileBattery battery = (TileBattery) world.getBlockTileEntity(x, y, z);
			battery.updateStructure();
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
		return new TileBattery();
	}
}
