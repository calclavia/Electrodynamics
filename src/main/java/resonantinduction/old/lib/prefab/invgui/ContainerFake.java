package resonantinduction.old.lib.prefab.invgui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;

/**
 * Allows the use of a tile inv without the need for a container class
 * 
 * @author DarkGuardsman
 */
public class ContainerFake extends Container
{
	TileEntity entity = null;

	public ContainerFake(TileEntity entity)
	{
		this.entity = entity;
	}

	@Override
	public boolean canInteractWith(EntityPlayer par1EntityPlayer)
	{
		if (entity instanceof IInventory)
		{
			return ((IInventory) this.entity).isUseableByPlayer(par1EntityPlayer);
		}
		return true;
	}

}
