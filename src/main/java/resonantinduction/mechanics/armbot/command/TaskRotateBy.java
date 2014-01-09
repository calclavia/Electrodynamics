package resonantinduction.mechanics.armbot.command;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import resonantinduction.api.IArmbot;
import resonantinduction.api.coding.args.ArgumentIntData;
import resonantinduction.mechanics.armbot.TaskBaseArmbot;
import resonantinduction.mechanics.armbot.TaskBaseProcess;
import universalelectricity.api.vector.Vector2;

import com.builtbroken.common.science.units.UnitHelper;

/**
 * Rotates an armbot by a set amount
 * 
 * @author DarkGuardsman
 */
public class TaskRotateBy extends TaskBaseArmbot
{

	int targetRotationYaw = 0, targetRotationPitch = 0, deltaPitch = 0, deltaYaw = 0;

	private TaskRotateTo rotateToCommand;

	public TaskRotateBy(int yaw, int pitch)
	{
		super("RotateBy");
		this.args.add(new ArgumentIntData("yaw", yaw, 360, 0));
		this.args.add(new ArgumentIntData("pitch", pitch, 360, 0));
		this.UV = new Vector2(80, 80);
	}

	public TaskRotateBy()
	{
		this(0, 0);
	}

	@Override
	public ProcessReturn onMethodCalled()
	{
		if (super.onMethodCalled() == ProcessReturn.CONTINUE)
		{
			this.targetRotationYaw = (int) MathHelper.clampAngleTo360((float) (((IArmbot) this.program.getMachine()).getRotation().x + UnitHelper.tryToParseInt(this.getArg("yaw"))));
			this.targetRotationPitch = (int) MathHelper.clampAngleTo360((float) (((IArmbot) this.program.getMachine()).getRotation().x + UnitHelper.tryToParseInt(this.getArg("pitch"))));
			return ProcessReturn.CONTINUE;
		}
		return ProcessReturn.GENERAL_ERROR;
	}

	@Override
	public ProcessReturn onUpdate()
	{
		if (this.rotateToCommand == null)
		{
			this.rotateToCommand = new TaskRotateTo(this.targetRotationYaw, this.targetRotationPitch);
			this.rotateToCommand.setProgram(this.program);
			this.rotateToCommand.onMethodCalled();
		}

		return this.rotateToCommand.onUpdate();
	}

	@Override
	public void load(NBTTagCompound taskCompound)
	{
		super.loadProgress(taskCompound);
		this.targetRotationPitch = taskCompound.getInteger("rotPitch");
		this.targetRotationYaw = taskCompound.getInteger("rotYaw");

	}

	@Override
	public void save(NBTTagCompound taskCompound)
	{
		super.saveProgress(taskCompound);
		taskCompound.setInteger("rotPitch", this.targetRotationPitch);
		taskCompound.setInteger("rotYaw", this.targetRotationYaw);

	}

	@Override
	public String toString()
	{
		return super.toString() + " Yaw:" + Integer.toString(this.targetRotationYaw) + " Pitch:" + Integer.toString(this.targetRotationPitch);
	}

	@Override
	public TaskBaseProcess clone()
	{
		return new TaskRotateBy();
	}

	@Override
	public void getToolTips(List<String> list)
	{
		super.getToolTips(list);
		list.add(" Yaw:   " + this.targetRotationYaw);
		list.add(" Pitch: " + this.targetRotationPitch);
	}
}
