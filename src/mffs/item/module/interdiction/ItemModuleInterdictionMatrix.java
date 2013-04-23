package mffs.item.module.interdiction;

import java.util.List;

import mffs.api.modules.IInterdictionMatrixModule;
import mffs.api.security.IInterdictionMatrix;
import mffs.item.module.ItemModule;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ItemModuleInterdictionMatrix extends ItemModule implements IInterdictionMatrixModule
{
	public ItemModuleInterdictionMatrix(int id, String name)
	{
		super(id, name);
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b)
	{
		info.add("\u00a74Interdiction Matrix");
		super.addInformation(itemStack, player, info, b);
	}

	@Override
	public boolean onDefend(IInterdictionMatrix interdictionMatrix, EntityLiving entityLiving)
	{
		return false;
	}

}
