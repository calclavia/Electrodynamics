/**
 * 
 */
package resonantinduction.battery;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import resonantinduction.base.BlockBase;
import resonantinduction.multimeter.TileEntityMultimeter;

/**
 * A block that detects power.
 * 
 * @author Calclavia
 * 
 */
public class BlockBattery extends BlockBase implements ITileEntityProvider
{
	private Icon machineIcon;

	public BlockBattery(int id)
	{
		super("battery", id, Material.iron);
	}

	@Override
	public Icon getIcon(int side, int metadata)
	{
		if (side == metadata)
		{
			return this.blockIcon;
		}

		return this.machineIcon;
	}

	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		super.registerIcons(iconRegister);
		this.machineIcon = iconRegister.registerIcon(ResonantInduction.PREFIX + "machine");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float par7, float par8, float par9)
	{
		if (entityPlayer.isSneaking())
		{
			world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.ROTATION_MATRIX[world.getBlockMetadata(x, y, z)][side], 3);
		}
		else
		{

		}

		return true;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityBattery();
	}
}
