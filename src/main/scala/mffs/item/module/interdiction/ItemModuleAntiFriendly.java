package mffs.item.module.interdiction;

import calclavia.api.mffs.security.IInterdictionMatrix;
import mffs.ModularForceFieldSystem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.monster.IMob;

public class ItemModuleAntiFriendly extends ItemModuleInterdictionMatrix
{
	public ItemModuleAntiFriendly(int i)
	{
		super(i, "moduleAntiFriendly");
	}

	@Override
	public boolean onDefend(IInterdictionMatrix interdictionMatrix, EntityLivingBase entityLiving)
	{
		if (!(entityLiving instanceof IMob && !(entityLiving instanceof INpc)))
		{
			entityLiving.setHealth(1);
			entityLiving.attackEntityFrom(ModularForceFieldSystem.damagefieldShock, 100);
		}

		return false;
	}
}