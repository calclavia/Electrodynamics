package mffs.item.module;

import java.util.List;
import java.util.Set;

import mffs.base.ItemMFFS;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.energy.UnitDisplay.Unit;
import universalelectricity.api.vector.Vector3;
import calclavia.api.mffs.IFieldInteraction;
import calclavia.api.mffs.IProjector;
import calclavia.api.mffs.modules.IModule;
import resonant.lib.utility.LanguageUtility;

public class ItemModule extends ItemMFFS implements IModule
{
	private float fortronCost = 0.5f;

	public ItemModule(int id, String name)
	{
		super(id, name);
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean b)
	{
		info.add(LanguageUtility.getLocal("info.item.fortron") + " " + UnitDisplay.getDisplay(getFortronCost(1) * 20, Unit.LITER) + "/s");
		super.addInformation(itemStack, player, info, b);
	}

	@Override
	public Set<Vector3> onPreCalculate(IFieldInteraction projector, Set<Vector3> position)
	{
		return position;
	}

	@Override
	public void onCalculate(IFieldInteraction projector, Set<Vector3> position)
	{
	}

	@Override
	public boolean onProject(IProjector projector, Set<Vector3> fields)
	{
		return false;
	}

	@Override
	public int onProject(IProjector projector, Vector3 position)
	{
		return 0;
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
	public ItemModule setMaxStackSize(int par1)
	{
		super.setMaxStackSize(par1);
		return this;
	}

	@Override
	public float getFortronCost(float amplifier)
	{
		return this.fortronCost;
	}

	@Override
	public boolean onDestroy(IProjector projector, Set<Vector3> field)
	{
		return false;
	}

	@Override
	public boolean requireTicks(ItemStack moduleStack)
	{
		return false;
	}
}