package resonantinduction.electrical.armbot.task;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.IArmbot;
import resonantinduction.electrical.armbot.TaskBaseArmbot;
import resonantinduction.electrical.armbot.TaskBaseProcess;
import resonantinduction.electrical.encoder.coding.IProgrammableMachine;
import resonantinduction.electrical.encoder.coding.args.ArgumentIntData;
import universalelectricity.api.vector.Vector2;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.InventoryInteractionHelper;
import calclavia.lib.utility.MathUtility;

import com.builtbroken.common.science.units.UnitHelper;

public class TaskGive extends TaskBaseArmbot
{

	private ItemStack stack;
	private int ammount = -1;

	public TaskGive()
	{
		super("give");
		this.args.add(new ArgumentIntData("blockID", -1, Block.blocksList.length - 1, -1));
		this.args.add(new ArgumentIntData("blockMeta", -1, 15, -1));
		this.args.add(new ArgumentIntData("stackSize", -1, 64, -1));
		this.UV = new Vector2(60, 80);
	}

	@Override
	public ProcessReturn onMethodCalled()
	{
		if (super.onMethodCalled() == ProcessReturn.CONTINUE)
		{

			ammount = UnitHelper.tryToParseInt(this.getArg("stackSize"), -1);
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
			TileEntity targetTile = ((IArmbot) this.program.getMachine()).getHandPos().getTileEntity(this.program.getMachine().getLocation().left());

			if (targetTile != null && ((IArmbot) this.program.getMachine()).getHeldObject() instanceof ItemStack)
			{
				ForgeDirection direction = MathUtility.getFacingDirectionFromAngle((float) ((IArmbot) this.program.getMachine()).getRotation().x);
				ItemStack itemStack = (ItemStack) ((IArmbot) this.program.getMachine()).getHeldObject();
				Set<ItemStack> stacks = new HashSet<ItemStack>();
				if (this.stack != null)
				{
					stacks.add(stack);
				}
				InventoryInteractionHelper invEx = new InventoryInteractionHelper(this.program.getMachine().getLocation().left(), this.program.getMachine().getLocation().right(), stacks, false);
				ItemStack insertStack = invEx.tryPlaceInPosition(itemStack, new Vector3(targetTile), direction.getOpposite());
				if (((IArmbot) this.program.getMachine()).clear(itemStack))
				{
					((IArmbot) this.program.getMachine()).grabObject(insertStack);
				}
			}
			return ProcessReturn.CONTINUE;
		}
		return ProcessReturn.GENERAL_ERROR;
	}

	@Override
	public String toString()
	{
		return super.toString() + " " + (stack != null ? stack.toString() : "1x???@???");
	}

	@Override
	public TaskBaseProcess loadProgress(NBTTagCompound taskCompound)
	{
		super.loadProgress(taskCompound);
		this.stack = ItemStack.loadItemStackFromNBT(taskCompound.getCompoundTag("item"));
		return this;
	}

	@Override
	public NBTTagCompound saveProgress(NBTTagCompound taskCompound)
	{
		super.saveProgress(taskCompound);
		if (stack != null)
		{
			NBTTagCompound tag = new NBTTagCompound();
			this.stack.writeToNBT(tag);
			taskCompound.setTag("item", tag);
		}
		return taskCompound;
	}

	@Override
	public TaskBaseProcess clone()
	{
		return new TaskGive();
	}
}
