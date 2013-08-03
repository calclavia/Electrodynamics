package resonantinduction.contractor;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityEMContractor extends TileEntity
{
	public static int MAX_REACH = 40;
	public static double MAX_SPEED = .4;
	
	private ForgeDirection facing = ForgeDirection.UP;
	
	public AxisAlignedBB operationBounds;
	
	/**
	 * true = suck, false = push
	 */
	public boolean suck = true;
	
	@Override
	public void updateEntity()
	{
		if(operationBounds != null)
		{
			List<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, operationBounds);
				
			for(Entity entity : list)
			{
				if(entity instanceof EntityItem)
				{
					EntityItem entityItem = (EntityItem)entity;
						
					switch(facing)
					{
						case DOWN:
							entityItem.motionX = 0;
							entityItem.motionZ = 0;
								
							entityItem.motionY = Math.max(-MAX_SPEED, entityItem.motionY-.2);
								
							entityItem.isAirBorne = true;
							break;
						case UP:
							entityItem.motionX = 0;
							entityItem.motionZ = 0;
								
							entityItem.motionY = Math.min(MAX_SPEED, entityItem.motionY+.2);
								
							entityItem.isAirBorne = true;
							break;
						case NORTH:
							entityItem.motionX = 0;
							entityItem.motionY = 0;
								
							entityItem.motionY = Math.max(-MAX_SPEED, entityItem.motionY-.2);
								
							entityItem.isAirBorne = true;
							break;
						case SOUTH:
							entityItem.motionX = 0;
							entityItem.motionY = 0;
								
							entityItem.motionY = Math.min(MAX_SPEED, entityItem.motionY+.2);
								
							entityItem.isAirBorne = true;
							break;
						case WEST:
							entityItem.motionY = 0;
							entityItem.motionZ = 0;
								
							entityItem.motionY = Math.max(-MAX_SPEED, entityItem.motionY-.2);
								
							entityItem.isAirBorne = true;
							break;
						case EAST:
							entityItem.motionY = 0;
							entityItem.motionZ = 0;
								
							entityItem.motionY = Math.min(MAX_SPEED, entityItem.motionY+.2);
								
							entityItem.isAirBorne = true;
							break;
					}
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
