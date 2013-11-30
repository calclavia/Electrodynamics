package mffs.item.module.projector;

import java.util.Iterator;
import java.util.Set;

import mffs.api.IFieldInteraction;
import mffs.item.module.ItemModule;
import net.minecraft.tileentity.TileEntity;
import universalelectricity.core.vector.Vector3;

public class ItemModuleArray extends ItemModule
{
	public ItemModuleArray(int i)
	{
		super(i, "moduleArray");
	}

	@Override
	public void onCalculate(IFieldInteraction projector, Set<Vector3> fieldBlocks)
	{
		Iterator<Vector3> it = fieldBlocks.iterator();

		while (it.hasNext())
		{
			Vector3 position = it.next();

			if (position.y < ((TileEntity) projector).yCoord)
			{
				it.remove();
			}
		}
	}
}