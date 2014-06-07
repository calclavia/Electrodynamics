package mffs.item.module.projector;

import mffs.ModularForceFieldSystem;
import mffs.item.module.ItemModule;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ItemModuleShock extends ItemModule
{
	public ItemModuleShock(int i)
	{
		super(i, "moduleShock");
	}

	@Override
	public boolean onCollideWithForceField(World world, int x, int y, int z, Entity entity, net.minecraft.item.ItemStack moduleStack)
	{
		entity.attackEntityFrom(ModularForceFieldSystem.damagefieldShock, moduleStack.stackSize);
		return false;
	}
}