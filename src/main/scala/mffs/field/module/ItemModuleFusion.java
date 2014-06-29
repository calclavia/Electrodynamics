package mffs.field.module;

import resonant.api.mffs.IProjector;
import resonant.api.mffs.fortron.FrequencyGrid;
import mffs.base.ItemModule;
import net.minecraft.tileentity.TileEntity;
import resonant.api.blocks.IBlockFrequency;
import universalelectricity.core.transform.vector.Vector3;

import java.util.Iterator;
import java.util.Set;

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
		Set<IBlockFrequency> machines = FrequencyGrid.instance().get(projector.getFrequency());

		for (IBlockFrequency compareProjector : machines)
		{
			if (compareProjector instanceof IProjector && compareProjector != projector)
			{
				if (((TileEntity) compareProjector).worldObj == ((TileEntity) projector).worldObj)
				{
					if (((TileMFFS) compareProjector).isActive() && ((IProjector) compareProjector).getMode() != null)
					{
						Iterator<Vector3> it = fieldBlocks.iterator();

						while (it.hasNext())
						{
							Vector3 position = it.next();

							if (((IProjector) compareProjector).getInteriorPoints().contains(position) || ((IProjector) compareProjector).getMode().isInField((IProjector) compareProjector, position.clone()))
							{
								it.remove();
							}
						}
					}
				}
			}
		}
		return false;
	}
}