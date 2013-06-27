package mffs.item.mode;

import mffs.api.IFieldInteraction;
import mffs.api.IProjector;
import mffs.api.modules.IProjectorMode;
import mffs.base.ItemBase;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ItemMode extends ItemBase implements IProjectorMode
{
	public ItemMode(int i, String name)
	{
		super(i, name);
		this.setMaxStackSize(1);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void render(IProjector projector, double x, double y, double z, float f, long ticks)
	{

	}

	@Override
	public boolean isInField(IFieldInteraction projector, Vector3 position)
	{
		return false;
	}
}