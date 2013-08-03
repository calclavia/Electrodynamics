package resonantinduction.contractor;

import java.lang.reflect.Method;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemExpireEvent;

/**
 * Spinoff of EntityItem for Contractors.
 * @author AidanBrady
 *
 */
public class EntityContractorItem extends EntityItem
{
	public boolean doGravityThisTick = true;
	
	public EntityContractorItem(World par1World) 
	{
		super(par1World);
	}

	@Override
    public void onUpdate()
    {
		super.onUpdate();
		
		if(!doGravityThisTick)
		{
			motionY = 0;
		}
    }
    
    public static EntityContractorItem get(EntityItem entityItem)
    {
    	EntityContractorItem item = new EntityContractorItem(entityItem.worldObj);
    	
    	item.posX = entityItem.posX;
    	item.posY = entityItem.posY;
    	item.posZ = entityItem.posZ;
    	
    	item.setEntityItemStack(entityItem.getEntityItem());
    	
    	item.motionX = entityItem.motionX;
    	item.motionY = entityItem.motionY;
    	item.motionZ = entityItem.motionZ;
    	
    	item.dataWatcher = entityItem.getDataWatcher();
    	
    	return item;
    }
}
