package resonantinduction.contractor;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityEMContractor extends TileEntity
{
	public static int MAX_REACH = 40;
	public static double MAX_SPEED = .1;
	public static double ACCELERATION = .01;
	
	private ForgeDirection facing = ForgeDirection.UP;
	
	public int pushDelay;
	
	public AxisAlignedBB operationBounds;
	
	/**
	 * true = suck, false = push
	 */
	public boolean suck = true;
	
	@Override
	public void updateEntity()
	{
		System.out.println(facing + " " + worldObj.isRemote);
		pushDelay = Math.max(0, pushDelay--);
		
		if(!suck && pushDelay == 0)
		{
			
		}
		
		if(operationBounds != null)
		{
			List<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, operationBounds);
			
			if(!list.isEmpty())
			{
				System.out.println("GOood");
			}
				
			for(Entity entity : list)
			{
				if(entity instanceof EntityItem)
				{
					EntityItem entityItem = (EntityItem)entity;
					
					switch(facing)
					{
						case DOWN:
							if(!worldObj.isRemote)
							{
								entityItem.setPosition(xCoord+0.5, entityItem.posY, zCoord+0.5);
							}
								
							entityItem.motionX = 0;
							entityItem.motionZ = 0;
							
							if(!suck)
							{
								entityItem.motionY = Math.max(-MAX_SPEED, entityItem.motionY-ACCELERATION);
							}
							else {
								entityItem.motionY = Math.min((MAX_SPEED*4), entityItem.motionY+(ACCELERATION*5));
							}
									
							entityItem.isAirBorne = true;
							break;
						case UP:
							if(!worldObj.isRemote)
							{
								entityItem.setPosition(xCoord+0.5, entityItem.posY, zCoord+0.5);
							}
								
							entityItem.motionX = 0;
							entityItem.motionZ = 0;
									
							if(!suck)
							{
								entityItem.motionY = Math.min((MAX_SPEED*4), entityItem.motionY+(ACCELERATION*5));
							}
							else {
								entityItem.motionY = Math.max(-MAX_SPEED, entityItem.motionY-ACCELERATION);
							}
									
							entityItem.isAirBorne = true;
							break;
						case NORTH:
							if(!worldObj.isRemote)
							{
								entityItem.setPosition(xCoord+0.5, yCoord+0.5, entityItem.posZ);
							}
							
							entityItem.motionX = 0;
							entityItem.motionY = 0;
									
							if(!suck)
							{
								entityItem.motionZ = Math.max(-MAX_SPEED, entityItem.motionZ-ACCELERATION);
							}
							else {
								entityItem.motionZ = Math.min(MAX_SPEED, entityItem.motionZ+ACCELERATION);
							}
									
							entityItem.isAirBorne = true;
							break;
						case SOUTH:
							if(!worldObj.isRemote)
							{
								entityItem.setPosition(xCoord+0.5, yCoord+0.5, entityItem.posZ);
							}
								
							entityItem.motionX = 0;
							entityItem.motionY = 0;
									
							if(!suck)
							{
								entityItem.motionZ = Math.min(MAX_SPEED, entityItem.motionZ+ACCELERATION);
							}
							else {
								entityItem.motionZ = Math.max(-MAX_SPEED, entityItem.motionZ-ACCELERATION);
							}
									
							entityItem.isAirBorne = true;
							break;
						case WEST:
							if(!worldObj.isRemote)
							{
								entityItem.setPosition(entityItem.posX, yCoord+0.5, zCoord+0.5);
							}
								
							entityItem.motionY = 0;
							entityItem.motionZ = 0;
									
							if(!suck)
							{
								entityItem.motionX = Math.max(-MAX_SPEED, entityItem.motionX-ACCELERATION);
							}
							else {
								entityItem.motionX = Math.min(MAX_SPEED, entityItem.motionX+ACCELERATION);
							}
									
							entityItem.isAirBorne = true;
							break;
						case EAST:
							if(!worldObj.isRemote)
							{
								entityItem.setPosition(entityItem.posX, yCoord+0.5, zCoord+0.5);
							}
								
							entityItem.motionY = 0;
							entityItem.motionZ = 0;
								
							if(!suck)
							{
								entityItem.motionX = Math.min(MAX_SPEED, entityItem.motionX+ACCELERATION);
							}
							else {
								entityItem.motionX = Math.max(-MAX_SPEED, entityItem.motionX-ACCELERATION);
							}
									
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
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord-MAX_REACH, xCoord+1, yCoord+1, zCoord);
				break;
			case SOUTH:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord-MAX_REACH, yCoord, zCoord, xCoord, yCoord+1, zCoord+1);
				break;
			case WEST:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, yCoord+1, zCoord+MAX_REACH);
				break;
			case EAST:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+MAX_REACH, yCoord+1, zCoord+1);
				break;
		}
	}
	
	public boolean isLatched()
	{
		ForgeDirection side = facing.getOpposite();
		
		TileEntity tile = worldObj.getBlockTileEntity(xCoord+facing.offsetX, yCoord+facing.offsetY, zCoord+facing.offsetZ);
		
		if(tile instanceof IInventory)
		{
			return true;
		}
		
		return false;
	}
	
	public void incrementFacing()
	{
		int newOrdinal = facing.ordinal() < 5 ? facing.ordinal()+1 : 0;
		setFacing(ForgeDirection.getOrientation(newOrdinal));
	}
	
	public ForgeDirection getFacing()
	{
		return facing;
	}
	
	public void setFacing(ForgeDirection side)
	{
		facing = side;
		updateBounds();
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
