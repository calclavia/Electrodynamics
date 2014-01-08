package resonantinduction.mechanics.armbot.command;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import resonantinduction.api.IArmbot;
import resonantinduction.api.coding.args.ArgumentIntData;
import resonantinduction.mechanics.armbot.TaskBaseProcess;

import com.builtbroken.common.science.units.UnitHelper;

public class TaskGrabItem extends TaskGrabPrefab
{
	ItemStack stack = null;

	public TaskGrabItem()
	{
		super("Grab-Item");
		this.args.add(new ArgumentIntData("blockID", -1, Block.blocksList.length - 1, -1));
		this.args.add(new ArgumentIntData("blockMeta", -1, 15, -1));
		this.args.add(new ArgumentIntData("stackSize", -1, 64, -1));
	}

	@Override
	public ProcessReturn onMethodCalled()
	{
		if (super.onMethodCalled() == ProcessReturn.CONTINUE)
		{
			int ammount = UnitHelper.tryToParseInt(this.getArg("stackSize"), -1);
			int blockID = UnitHelper.tryToParseInt(this.getArg("blockID"), -1);
			int blockMeta = UnitHelper.tryToParseInt(this.getArg("blockMeta"), 32767);

			if (blockID > 0)
			{
				stack = new ItemStack(blockID, ammount <= 0 ? 1 : ammount, blockMeta == -1 ? 32767 : blockMeta);
			}

			return ProcessReturn.CONTINUE;
		}
		return ProcessReturn.GENERAL_ERROR;
	}

	@Override
	public ProcessReturn onUpdate()
	{
		if (super.onUpdate() == ProcessReturn.CONTINUE)
		{
			if (((IArmbot) this.program.getMachine()).getHeldObject() != null)
			{
				return ProcessReturn.DONE;
			}
			List<EntityItem> found = this.program.getMachine().getLocation().left().getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(this.armPos.x - radius, this.armPos.y - radius, this.armPos.z - radius, this.armPos.x + radius, this.armPos.y + radius, this.armPos.z + radius));

			if (found != null && found.size() > 0)
			{
				for (EntityItem item : found)
				{
					if (stack == null || item.getEntityItem().isItemEqual(stack))
					{
						if (((IArmbot) this.program.getMachine()).grabObject(item))
						{
							if (this.belt != null)
							{
								belt.ignoreEntity(item);
							}
							return ProcessReturn.DONE;
						}
					}
				}
			}
			return ProcessReturn.CONTINUE;
		}
		return ProcessReturn.GENERAL_ERROR;
	}

	@Override
	public TaskBaseProcess clone()
	{
		return new TaskGrabItem();
	}
}
