/**
 * 
 */
package resonantinduction.multimeter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * ItemBlock for the Multimeter
 * 
 * @author Calclavia
 * 
 */
public class ItemBlockMultimeter extends ItemBlock
{
	public ItemBlockMultimeter(int par1)
	{
		super(par1);
	}

	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (par2EntityPlayer.isSneaking())
		{
			if (!world.isRemote)
			{
				par2EntityPlayer.addChatMessage("Energy: " + TileEntityMultimeter.getDetectedEnergy(world.getBlockTileEntity(x, y, z)) + " J");
			}
			
			return true;
		}

		return super.onItemUse(par1ItemStack, par2EntityPlayer, world, x, y, z, par7, par8, par9, par10);
	}
}
