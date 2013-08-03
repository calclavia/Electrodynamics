package resonantinduction.contractor;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityEMContractor extends TileEntity
{
	public static int MAX_REACH = 40;
	
	private ForgeDirection facing = ForgeDirection.UP;
	
	public AxisAlignedBB operationBounds;
	
	/**
	 * true = suck, false = push
	 */
	public boolean suck;
	
	@Override
	public void updateEntity()
	{
		if(!worldObj.isRemote)
		{
			if(operationBounds != null)
			{
				List list = worldObj.getEntitiesWithinAABB(Entity.class, operationBounds);
				
				if(!list.isEmpty())
				{
					System.out.println("Good!");
				}
			}
		}
	}
	
	public void updateBounds()
	{
		switch(facing)
		{
			case DOWN:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, Math.max(yCoord-MAX_REACH, 1), zCoord, xCoord+1, yCoord, zCoord+1);
				break;
			case UP:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, Math.min(yCoord+MAX_REACH, 255), zCoord+1);
				break;
			case NORTH:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+MAX_REACH, yCoord+1, zCoord+1);
				break;
			case SOUTH:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord-MAX_REACH, yCoord, zCoord, xCoord, yCoord+1, zCoord+1);
				break;
			case WEST:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, yCoord+1, zCoord+MAX_REACH);
				break;
			case EAST:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord-MAX_REACH, xCoord+1, yCoord+1, zCoord);
				break;
		}
	}
	
	public void incrementFacing()
	{
		int newOrdinal = facing.ordinal() < 5 ? facing.ordinal()+1 : 0;
		facing = ForgeDirection.getOrientation(newOrdinal);
		
		updateBounds();
	}
	
	public ForgeDirection getFacing()
	{
		return facing;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
		super.readFromNBT(nbtTags);
		
		facing = ForgeDirection.getOrientation(nbtTags.getInteger("facing"));
		suck = nbtTags.getBoolean("suck");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
    	super.writeToNBT(nbtTags);
    	
    	nbtTags.setInteger("facing", facing.ordinal());
    	nbtTags.setBoolean("suck", suck);
    }
}
