package mffs.item.module.projector;

import calclavia.api.mffs.IFieldInteraction;
import mffs.item.module.ItemModule;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ItemModuleArray extends ItemModule
{
	public ItemModuleArray(int i)
	{
		super(i, "moduleArray");
	}

	@Override
	public void onPreCalculate(IFieldInteraction projector, Set<Vector3> fieldBlocks)
	{
		Set<Vector3> originalField = new HashSet(fieldBlocks);

		HashMap<ForgeDirection, Integer> longestDirectional = this.getDirectionWidthMap(originalField);

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			int copyAmount = projector.getSidedModuleCount(this, direction);
			int directionalDisplacement = (Math.abs(longestDirectional.get(direction)) + Math.abs(longestDirectional.get(direction.getOpposite()))) + 1;

			for (int i = 0; i < copyAmount; i++)
			{
				int directionalDisplacementScale = directionalDisplacement * (i + 1);

				for (Vector3 originalFieldBlock : originalField)
				{
					Vector3 newFieldBlock = originalFieldBlock.clone().translate(new Vector3(direction).scale(directionalDisplacementScale));
					fieldBlocks.add(newFieldBlock);
				}
			}
		}
	}

	public HashMap<ForgeDirection, Integer> getDirectionWidthMap(Set<Vector3> field)
	{
		HashMap<ForgeDirection, Integer> longestDirectional = new HashMap<ForgeDirection, Integer>();
		longestDirectional.put(ForgeDirection.DOWN, 0);
		longestDirectional.put(ForgeDirection.UP, 0);
		longestDirectional.put(ForgeDirection.NORTH, 0);
		longestDirectional.put(ForgeDirection.SOUTH, 0);
		longestDirectional.put(ForgeDirection.WEST, 0);
		longestDirectional.put(ForgeDirection.EAST, 0);

		for (Vector3 fieldPosition : field)
		{
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

		return longestDirectional;
	}

	@Override
	public float getFortronCost(float amplifier)
	{
		return super.getFortronCost(amplifier) + (super.getFortronCost(amplifier) * amplifier) / 100f;
	}
}