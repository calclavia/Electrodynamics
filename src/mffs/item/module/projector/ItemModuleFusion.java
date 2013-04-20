package mffs.item.module.projector;

import java.util.Iterator;
import java.util.Set;

import mffs.api.IProjector;
import mffs.api.fortron.IFortronFrequency;
import mffs.base.TileEntityBase;
import mffs.fortron.FortronGrid;
import mffs.item.module.ItemModule;
import universalelectricity.core.vector.Vector3;

public class ItemModuleFusion extends ItemModule
{
	public ItemModuleFusion(int i)
	{
		super(i, "moduleFusion");
		this.setMaxStackSize(1);
		this.setCost(1f);
	}

	@Override
	public boolean onProject(IProjector projector, Set<Vector3> fieldBlocks)
	{
		Set<IFortronFrequency> machines = FortronGrid.instance().get(((IFortronFrequency) projector).getFrequency());

		for (IFortronFrequency compareProjector : machines)
		{
			if (compareProjector instanceof IProjector && compareProjector != projector)
			{
				if (((TileEntityBase) compareProjector).isActive())
				{
					Iterator<Vector3> it = fieldBlocks.iterator();

					while (it.hasNext())
					{
						Vector3 position = it.next();

						if (((IProjector) compareProjector).getMode().isInField((IProjector) compareProjector, position.clone()))
						{
							it.remove();
						}
					}
				}
			}
		}
		return false;
	}
}