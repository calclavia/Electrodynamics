package mffs.item.module.interdiction;

import mffs.ModularForceFieldSystem;
import mffs.api.security.IInterdictionMatrix;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.INpc;
import net.minecraft.entity.monster.IMob;

public class ItemModuleAntiFriendly extends ItemModuleInterdictionMatrix
{
	public ItemModuleAntiFriendly(int i)
	{
		super(i, "moduleAntiFriendly");
	}

	@Override
	public boolean onDefend(IInterdictionMatrix defenseStation, EntityLiving entityLiving)
	{
		if (!(entityLiving instanceof IMob && !(entityLiving instanceof INpc)))
		{
			entityLiving.attackEntityFrom(ModularForceFieldSystem.damagefieldShock, Integer.MAX_VALUE);
		}

		return false;
	}
}