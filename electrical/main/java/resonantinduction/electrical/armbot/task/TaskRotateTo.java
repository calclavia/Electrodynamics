package resonantinduction.electrical.armbot.task;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import resonantinduction.electrical.armbot.IArmbot;
import resonantinduction.electrical.armbot.TaskBaseArmbot;
import resonantinduction.electrical.armbot.TaskBaseProcess;
import resonantinduction.electrical.encoder.coding.args.ArgumentIntData;
import universalelectricity.api.vector.Vector2;
import calclavia.lib.utility.MathUtility;

import com.builtbroken.common.science.units.UnitHelper;

/**
 * Rotates the armbot to a specific direction.
 * 
 * @author DarkGuardsman
 */
public class TaskRotateTo extends TaskBaseArmbot
{
	int targetRotationYaw = 0, targetRotationPitch = 0;

	public TaskRotateTo()
	{
		this(0, 0);
	}

	public TaskRotateTo(int yaw, int pitch)
	{
		this("RotateTo", yaw, pitch);
	}

	public TaskRotateTo(String string, int yaw, int pitch)
	{
		super(string);
		this.args.add(new ArgumentIntData("yaw", yaw, 360, 0));
		this.args.add(new ArgumentIntData("pitch", pitch, 360, 0));
		this.UV = new Vector2(100, 80);
	}

	@Override
	public ProcessReturn onMethodCalled()
	{
		if (super.onMethodCalled() == ProcessReturn.CONTINUE)
		{
			this.targetRotationYaw = (int) MathUtility.clampAngleTo360(UnitHelper.tryToParseInt(this.getArg("yaw")));
			this.targetRotationPitch = (int) MathUtility.clampAngleTo360(UnitHelper.tryToParseInt(this.getArg("pitch")));
			return ProcessReturn.CONTINUE;
		}
		return ProcessReturn.GENERAL_ERROR;
	}

	@Override
	public ProcessReturn onUpdate()
	{
		if (super.onUpdate() == ProcessReturn.CONTINUE)
		{
			((IArmbot) this.program.getMachine()).moveArmTo(this.targetRotationYaw, this.targetRotationPitch);
			return ((IArmbot) this.program.getMachine()).getRotation().intX() != this.targetRotationYaw || ((IArmbot) this.program.getMachine()).getRotation().intY() != this.targetRotationPitch ? ProcessReturn.CONTINUE : ProcessReturn.DONE;
		}
		return ProcessReturn.GENERAL_ERROR;
	}

	@Override
	public String toString()
	{
		return super.toString() + " Yaw:" + Integer.toString(this.targetRotationYaw) + " Pitch:" + Integer.toString(this.targetRotationPitch);
	}

	@Override
	public void load(NBTTagCompound taskCompound)
	{
		super.load(taskCompound);
		this.targetRotationPitch = taskCompound.getInteger("rotPitch");
		this.targetRotationYaw = taskCompound.getInteger("rotYaw");
	}

	@Override
	public void save(NBTTagCompound taskCompound)
	{
		super.save(taskCompound);
		taskCompound.setInteger("rotPitch", this.targetRotationPitch);
		taskCompound.setInteger("rotYaw", this.targetRotationYaw);
	}

	@Override
	public TaskBaseProcess clone()
	{
		return new TaskRotateTo();
	}

	@Override
	public void getToolTips(List<String> list)
	{
		super.getToolTips(list);
		list.add(" Yaw:   " + this.targetRotationYaw);
		list.add(" Pitch: " + this.targetRotationPitch);
	}
}
