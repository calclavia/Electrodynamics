package mffs.item.module.projector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import mffs.api.IFieldInteraction;
import mffs.item.module.ItemModule;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;

public class ItemModuleArray extends ItemModule
{
	public ItemModuleArray(int i)
	{
		super(i, "moduleArray");
	}

	@Override
	public void onPreCalculate(IFieldInteraction projector, Set<Vector3> fieldBlocks)
	{
		Set<Vector3> originalField = new HashSet<Vector3>(fieldBlocks);

		HashMap<ForgeDirection, Integer> longestDirectional = new HashMap<ForgeDirection, Integer>();
		longestDirectional.put(ForgeDirection.DOWN, 0);
		longestDirectional.put(ForgeDirection.UP, 0);
		longestDirectional.put(ForgeDirection.NORTH, 0);
		longestDirectional.put(ForgeDirection.SOUTH, 0);
		longestDirectional.put(ForgeDirection.WEST, 0);
		longestDirectional.put(ForgeDirection.EAST, 0);

		for (Vector3 fieldPosition : originalField)
		{
			int longestAxis = (int) Math.max(fieldPosition.intX(), Math.max(fieldPosition.intY(), fieldPosition.intZ()));

			if (fieldPosition.intX() > 0 && fieldPosition.intX() > longestDirectional.get(ForgeDirection.EAST))
			{
				longestDirectional.put(ForgeDirection.EAST, fieldPosition.intX());
			}
			else if (fieldPosition.intX() < 0 && fieldPosition.intX() < longestDirectional.get(ForgeDirection.WEST))
			{
				longestDirectional.put(ForgeDirection.WEST, fieldPosition.intX());
			}

			if (fieldPosition.intY() > 0 && fieldPosition.intY() > longestDirectional.get(ForgeDirection.UP))
			{
				longestDirectional.put(ForgeDirection.UP, fieldPosition.intY());
			}
			else if (fieldPosition.intY() < 0 && fieldPosition.intY() < longestDirectional.get(ForgeDirection.DOWN))
			{
				longestDirectional.put(ForgeDirection.DOWN, fieldPosition.intY());
			}

			if (fieldPosition.intZ() > 0 && fieldPosition.intZ() > longestDirectional.get(ForgeDirection.SOUTH))
			{
				longestDirectional.put(ForgeDirection.SOUTH, fieldPosition.intZ());
			}
			else if (fieldPosition.intZ() < 0 && fieldPosition.intZ() < longestDirectional.get(ForgeDirection.NORTH))
			{
				longestDirectional.put(ForgeDirection.NORTH, fieldPosition.intZ());
			}

		}

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			int copyAmount = projector.getSidedModuleCount(this, direction);

			for (int i = 0; i < copyAmount; i++)
			{
				for (Vector3 originalFieldBlock : originalField)
				{
					int directionalDisplacementScale = (Math.abs(longestDirectional.get(direction)) + Math.abs(longestDirectional.get(direction.getOpposite()))) * (i + 1) + 1;
					fieldBlocks.add(originalFieldBlock.clone().translate(new Vector3(direction).scale(directionalDisplacementScale)));
				}
			}
		}
	}
}