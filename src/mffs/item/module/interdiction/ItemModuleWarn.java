package mffs.item.module.interdiction;

import mffs.api.security.IInterdictionMatrix;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;

public class ItemModuleWarn extends ItemModuleInterdictionMatrix
{
	public ItemModuleWarn(int i)
	{
		super(i, "moduleWarn");
	}

	@Override
	public boolean onDefend(IInterdictionMatrix interdictionMatrix, EntityLiving entityLiving)
	{
		boolean hasPermission = false;
		if (!hasPermission && entityLiving instanceof EntityPlayer)
		{
			((EntityPlayer) entityLiving).addChatMessage("[" + interdictionMatrix.getInvName() + "] Leave this zone immediately. You have no right to enter.");
		}

		return false;
	}
}