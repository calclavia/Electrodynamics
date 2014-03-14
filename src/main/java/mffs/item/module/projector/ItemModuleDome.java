package mffs.item.module.projector;

import java.util.Iterator;
import java.util.Set;

import mffs.item.module.ItemModule;
import net.minecraft.tileentity.TileEntity;
import universalelectricity.api.vector.Vector3;
import calclavia.api.mffs.IFieldInteraction;

public class ItemModuleDome extends ItemModule
{
	public ItemModuleDome(int i)
	{
		super(i, "moduleDome");
		this.setMaxStackSize(1);
	}

	@Override
	public void onCalculate(IFieldInteraction projector, Set<Vector3> fieldBlocks)
	{
		Vector3 absoluteTranslation = new Vector3((TileEntity) projector).translate(projector.getTranslation());

		Iterator<Vector3> it = fieldBlocks.iterator();

		while (it.hasNext())
		{
			Vector3 position = it.next();

			if (position.y < absoluteTranslation.y)
			{
				it.remove();
			}
		}
	}
}