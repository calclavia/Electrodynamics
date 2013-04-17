package mffs.item.module;

import java.util.Set;

import mffs.api.IProjector;
import mffs.api.modules.IModule;
import mffs.base.ItemBase;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

public class ItemModule extends ItemBase implements IModule
{
	private float fortronCost = 0.5f;

	public ItemModule(int id, String name)
	{
		super(id, name);
	}

	@Override
	public void onCalculate(IProjector projector, Set<Vector3> position)
	{
	}

	@Override
	public boolean onProject(IProjector projector, Set<Vector3> fields)
	{
		return false;
	}

	@Override
	public boolean onProject(IProjector projector, Vector3 position)
	{
		return false;
	}

	@Override
	public boolean onCollideWithForceField(World world, int x, int y, int z, Entity entity, ItemStack moduleStack)
	{
		return false;
	}

	public ItemModule setCost(float cost)
	{
		this.fortronCost = cost;
		return this;
	}

	@Override
	public float getFortronCost(int amplifier)
	{
		return this.fortronCost;
	}
}