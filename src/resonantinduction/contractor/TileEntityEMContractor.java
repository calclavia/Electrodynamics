package resonantinduction.contractor;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityEMContractor extends TileEntity
{
	public static int MAX_REACH = 40;
	
	public ForgeDirection facing = ForgeDirection.UP;
	
	/**
	 * true = suck, false = push
	 */
	public boolean suck;
	
	@Override
	public void updateEntity()
	{
		AxisAlignedBB box = null;
		
		if(!worldObj.isRemote)
		{
			switch(facing)
			{
				case DOWN:
					box = AxisAlignedBB.getBoundingBox(xCoord, Math.max(yCoord-MAX_REACH, 1), zCoord, xCoord+1, yCoord, zCoord+1);
					break;
				case UP:
					box = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, Math.min(yCoord+MAX_REACH, 255), zCoord+1);
					break;
				case NORTH:
					box = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+MAX_REACH, yCoord+1, zCoord+1);
					break;
				case SOUTH:
					box = AxisAlignedBB.getBoundingBox(xCoord-MAX_REACH, yCoord, zCoord, xCoord, yCoord+1, zCoord+1);
					break;
				case WEST:
					box = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, yCoord+1, zCoord+MAX_REACH);
					break;
				case EAST:
					box = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord-MAX_REACH, xCoord+1, yCoord+1, zCoord);
					break;
			}
			
			List list = worldObj.getEntitiesWithinAABB(EntityPlayer.class, box);
			
			System.out.println(facing.ordinal());
			
			if(!list.isEmpty())
			{
				System.out.println("Good!");
			}
		}
	}
	
	public void incrementFacing()
	{
		int newOrdinal = facing.ordinal() < 5 ? facing.ordinal()+1 : 0;
		facing = ForgeDirection.getOrientation(newOrdinal);
	}
}
