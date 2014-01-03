package mffs.item.module.interdiction;

import java.util.List;

import mffs.api.modules.IInterdictionMatrixModule;
import mffs.api.security.IInterdictionMatrix;
import mffs.item.module.ItemModule;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import calclavia.lib.prefab.TranslationHelper;

public class ItemModuleInterdictionMatrix extends ItemModule implements IInterdictionMatrixModule
{
	public ItemModuleInterdictionMatrix(int id, String name)
	{
		super(id, name);
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b)
	{
		info.add("\u00a74" + TranslationHelper.getLocal("tile.mffs:interdictionMatrix.name"));
		super.addInformation(itemStack, player, info, b);
	}

	@Override
	public boolean onDefend(IInterdictionMatrix interdictionMatrix, EntityLivingBase entityLiving)
	{
		return false;
	}

}
