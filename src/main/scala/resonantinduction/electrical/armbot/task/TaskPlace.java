package resonantinduction.electrical.armbot.task;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.IPlantable;
import resonantinduction.electrical.armbot.IArmbot;
import resonantinduction.electrical.armbot.TaskBaseArmbot;
import resonantinduction.electrical.armbot.TaskBaseProcess;
import universalelectricity.api.vector.Vector3;

/**
 * Used by arms to break a specific block in a position.
 * 
 * @author Calclavia
 */
public class TaskPlace extends TaskBaseArmbot
{
	int PLACE_TIME = 30;

	public TaskPlace()
	{
		super("Place");
		// TODO Auto-generated constructor stub
	}

	@Override
	public ProcessReturn onUpdate()
	{
		if (super.onUpdate() == ProcessReturn.CONTINUE)
		{
			Vector3 serachPosition = ((IArmbot) this.program.getMachine()).getHandPos();

			Block block = Block.blocksList[serachPosition.getBlockID(this.program.getMachine().getLocation().left())];

			if (block == null && ticks >= this.PLACE_TIME)
			{
				Object entity = ((IArmbot) this.program.getMachine()).getHeldObject();
				ItemStack itemStack = null;
				if (entity instanceof EntityItem)
				{
					itemStack = ((EntityItem) entity).getEntityItem();
				}
				if (entity instanceof ItemStack)
				{
					itemStack = (ItemStack) entity;
				}
				if (itemStack != null)
				{
					if (itemStack.getItem() instanceof ItemBlock)
					{
						((ItemBlock) itemStack.getItem()).placeBlockAt(itemStack, null, this.program.getMachine().getLocation().left(), serachPosition.intX(), serachPosition.intY(), serachPosition.intZ(), 0, 0.5f, 0.5f, 0.5f, itemStack.getItemDamage());

						((IArmbot) this.program.getMachine()).clear(entity);
						return ProcessReturn.DONE;
					}
					else if (itemStack.getItem() instanceof IPlantable)
					{
						IPlantable plantable = ((IPlantable) itemStack.getItem());
						Block blockBelow = Block.blocksList[Vector3.translate(serachPosition, new Vector3(0, -1, 0)).getBlockID(this.program.getMachine().getLocation().left())];

						if (blockBelow != null)
						{
							if (blockBelow.canSustainPlant(this.program.getMachine().getLocation().left(), serachPosition.intX(), serachPosition.intY(), serachPosition.intZ(), ForgeDirection.UP, plantable))
							{
								int blockID = plantable.getPlantID(this.program.getMachine().getLocation().left(), serachPosition.intX(), serachPosition.intY(), serachPosition.intZ());
								int blockMetadata = plantable.getPlantMetadata(this.program.getMachine().getLocation().left(), serachPosition.intX(), serachPosition.intY(), serachPosition.intZ());

								if (this.program.getMachine().getLocation().left().setBlock(serachPosition.intX(), serachPosition.intY(), serachPosition.intZ(), blockID, blockMetadata, 3))
								{
									if (this.program.getMachine().getLocation().left().getBlockId(serachPosition.intX(), serachPosition.intY(), serachPosition.intZ()) == blockID)
									{
										Block.blocksList[blockID].onBlockPlacedBy(this.program.getMachine().getLocation().left(), serachPosition.intX(), serachPosition.intY(), serachPosition.intZ(), null, itemStack);
										Block.blocksList[blockID].onPostBlockPlaced(this.program.getMachine().getLocation().left(), serachPosition.intX(), serachPosition.intY(), serachPosition.intZ(), blockMetadata);
										((IArmbot) this.program.getMachine()).clear(entity);
										return ProcessReturn.DONE;
									}
								}
							}
						}
					}
				}
			}
			return ProcessReturn.DONE;
		}
		return ProcessReturn.GENERAL_ERROR;
	}

	@Override
	public String toString()
	{
		return "PLACE";
	}

	@Override
	public TaskBaseProcess clone()
	{
		return new TaskPlace();
	}
}
