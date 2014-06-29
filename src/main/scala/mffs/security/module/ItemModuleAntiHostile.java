package mffs.security.module;

import resonant.api.mffs.security.IInterdictionMatrix;
import mffs.ModularForceFieldSystem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.monster.IMob;

public class ItemModuleAntiHostile extends ItemModuleInterdictionMatrix
{
	public ItemModuleAntiHostile(int i)
	{
		super(i, "moduleAntiHostile");
	}

	@Override
	public boolean onDefend(IInterdictionMatrix interdictionMatrix, EntityLivingBase entityLiving)
	{
		if (entityLiving instanceof IMob && !(entityLiving instanceof INpc))
		{
			entityLiving.attackEntityFrom(ModularForceFieldSystem.damagefieldShock, 20);
		}

		return false;
	}
}