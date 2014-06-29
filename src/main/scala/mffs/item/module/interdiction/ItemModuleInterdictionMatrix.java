package mffs.item.module.interdiction;

import resonant.api.mffs.modules.IInterdictionMatrixModule;
import resonant.api.mffs.security.IInterdictionMatrix;
import mffs.item.module.ItemModule;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import resonant.lib.utility.LanguageUtility;

import java.util.List;

public class ItemModuleInterdictionMatrix extends ItemModule implements IInterdictionMatrixModule
{
	public ItemModuleInterdictionMatrix(int id, String name)
	{
		super(id, name);
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b)
	{
		info.add("\u00a74" + LanguageUtility.getLocal("tile.mffs:interdictionMatrix.name"));
		super.addInformation(itemStack, player, info, b);
	}

	@Override
	public boolean onDefend(IInterdictionMatrix interdictionMatrix, EntityLivingBase entityLiving)
	{
		return false;
	}

}
