package resonantinduction.electrical.armbot;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import resonant.lib.utility.nbt.NBTUtility;
import resonantinduction.core.ArgumentData;
import resonantinduction.electrical.encoder.coding.IProgram;
import resonantinduction.electrical.encoder.coding.IProgrammableMachine;
import resonantinduction.electrical.encoder.coding.ITask;
import universalelectricity.api.vector.Vector2;

/** @author DarkGuardsman */
public abstract class TaskBase implements ITask
{
	/** Program this is part of. Can be null while stores as a prefab waiting to be copied */
	protected IProgram program;
	protected String methodName;
	/** The amount of ticks this command has been running for. */
	protected long ticks;

	protected TaskType taskType;

	protected int col;
	protected int row;

	protected Vector2 UV;

	/** The parameters this command */
	protected List<ArgumentData> args = new ArrayList<ArgumentData>();

	public TaskBase(String name, TaskType tasktype)
	{
		this.methodName = name;
		this.taskType = tasktype;
		this.UV = this.taskType.UV;
	}

	@Override
	public void refresh()
	{

	}

	@Override
	public void reset()
	{

	}

	@Override
	public void setProgram(IProgram program)
	{
		this.program = program;
	}

	@Override
	public int getCol()
	{
		return this.col;
	}

	@Override
	public int getRow()
	{
		return this.row;
	}

	@Override
	public void setPosition(int col, int row)
	{
		this.col = col;
		this.row = row;
	}

	@Override
	public String getMethodName()
	{
		return this.methodName;
	}

	@Override
	public TaskType getType()
	{
		return this.taskType;
	}

	@Override
	public Object getArg(String name)
	{
		if (this.getArgs() != null)
		{
			for (ArgumentData arg : this.getArgs())
			{
				if (arg.getName().equalsIgnoreCase(name))
				{
					return arg.getData();
				}
			}
		}
		return null;
	}

	@Override
	public void setArg(String argName, Object obj)
	{
		if (this.getArgs() != null)
		{
			for (ArgumentData arg : this.getArgs())
			{
				if (arg.getName().equalsIgnoreCase(argName))
				{
					arg.setData(obj);
					break;
				}
			}
		}
	}

	@Override
	public List<ArgumentData> getArgs()
	{
		return this.args;
	}

	@Override
	public abstract TaskBase clone();

	@Override
	public void load(NBTTagCompound nbt)
	{
		this.col = nbt.getInteger("col");
		this.row = nbt.getInteger("row");
		if (this.getArgs() != null)
		{
			NBTTagCompound parms = nbt.getCompoundTag("args");
			for (ArgumentData arg : this.getArgs())
			{
				Object obj = NBTUtility.loadObject(parms, arg.getName());
				if (arg.isValid(obj))
				{
					arg.setData(obj);
				}
			}
		}
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		nbt.setInteger("col", this.col);
		nbt.setInteger("row", this.row);
		NBTTagCompound parms = new NBTTagCompound();
		for (ArgumentData arg : this.getArgs())
		{
			NBTUtility.saveObject(parms, arg.getName(), arg.getData());
		}
		nbt.setCompoundTag("args", parms);
	}

	@Override
	public ITask loadProgress(NBTTagCompound nbt)
	{
		return this;
	}

	@Override
	public NBTTagCompound saveProgress(NBTTagCompound nbt)
	{
		return nbt;
	}

	@Override
	public boolean canUseTask(IProgrammableMachine device)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toString()
	{
		return "Task[" + this.methodName + "]:";
	}

	@Override
	public ResourceLocation getTextureSheet()
	{
		return ITask.TaskType.TEXTURE;
	}

	@Override
	public Vector2 getTextureUV()
	{
		return this.UV;
	}

	@Override
	public void getToolTips(List<String> list)
	{
		list.add(this.getMethodName());
	}

}
