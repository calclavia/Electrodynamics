package resonantinduction.contractor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class ItemBlockContractor extends ItemBlock
{
	public ItemBlockContractor(int id) 
	{
		super(id);
	}
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
    {
    	boolean place = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    	
    	if(place)
    	{
    		TileEntityEMContractor tileContractor = (TileEntityEMContractor)world.getBlockTileEntity(x, y, z);
    		tileContractor.setFacing(ForgeDirection.getOrientation(side));
    	}
    	
    	return place;
    }
}
