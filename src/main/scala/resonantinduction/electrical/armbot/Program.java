package resonantinduction.electrical.armbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import resonant.lib.utility.nbt.NBTUtility;
import resonantinduction.electrical.encoder.coding.ILogicTask;
import resonantinduction.electrical.encoder.coding.IProgram;
import resonantinduction.electrical.encoder.coding.IProgrammableMachine;
import resonantinduction.electrical.encoder.coding.ITask;
import resonantinduction.electrical.encoder.coding.TaskRegistry;
import universalelectricity.api.vector.Vector2;

public class Program implements IProgram
{
	protected Vector2 currentPos = new Vector2(0, 0);
	protected ITask currentTask;
	protected IProgrammableMachine machine;
	protected HashMap<Vector2, ITask> tasks = new HashMap();
	protected HashMap<String, Object> varables = new HashMap();
	protected int width = 0, hight = 0;
	boolean editing = false;

	@Override
	public void init(IProgrammableMachine machine)
	{
		this.machine = machine;
		int w = 0;
		int h = 0;
		List<Vector2> removeList = new ArrayList<Vector2>();
		for (Entry<Vector2, ITask> entry : this.tasks.entrySet())
		{
			if (entry.getValue() != null)
			{
				entry.getValue().setProgram(this);
				if (entry.getValue().getCol() > w)
				{
					w = entry.getValue().getCol();
				}
				if (entry.getValue().getRow() > h)
				{
					h = entry.getValue().getRow();
				}
			}
			else
			{
				// Should be rare that one of the slots would be null
				removeList.add(entry.getKey());
			}
		}
		for (Vector2 vec : removeList)
		{
			this.tasks.remove(vec);
		}
		this.width = w;
		this.hight = h;
	}

	@Override
	public IProgrammableMachine getMachine()
	{
		return this.machine;
	}

	@Override
	public HashMap<String, Object> getDeclairedVarables()
	{
		return varables;
	}

	@Override
	public ITask getNextTask()
	{
		if (this.currentTask != null)
		{
			this.currentTask.reset();
		}
		this.currentTask = this.getTaskAt(currentPos.intX(), currentPos.intY());
		this.currentPos.add(new Vector2(0, 1));
		if (this.currentTask != null)
		{
			this.currentTask.refresh();
		}
		if (this.currentTask instanceof ILogicTask)
		{
			if (((ILogicTask) this.currentTask).getExitPoint() != null)
			{
				this.currentTask = ((ILogicTask) this.currentTask).getExitPoint();
				this.currentPos = new Vector2(this.currentTask.getCol(), this.currentTask.getRow() + 1);
			}
		}
		return this.currentTask;
	}

	@Override
	public ITask getTaskAt(int col, int row)
	{
		return this.tasks.get(new Vector2(col, row));
	}

	@Override
	public HashMap<Vector2, ITask> getTaskMap()
	{
		return this.tasks;
	}

	@Override
	public void setTaskAt(int col, int row, ITask task)
	{
		if (!this.editing)
		{
			if (task != null)
			{
				task.setPosition(col, row);
				if (task.getCol() > this.width)
				{
					this.width = task.getCol();
				}
				if (task.getRow() > this.hight)
				{
					this.hight = task.getRow();
				}
				this.tasks.put(new Vector2(col, row), task);
			}
			else if (this.tasks.containsKey(new Vector2(col, row)))
			{
				this.tasks.remove(new Vector2(col, row));
				if (col == this.hight && !this.isThereATaskInRow(this.hight))
				{
					this.hight--;
				}
				else if (!this.isThereATaskInRow(col))
				{
					this.moveAll(col, true);
				}
			}
		}
	}

	public boolean isThereATaskInRow(int row)
	{
		int colume = 0;
		for (int x = 0; x <= this.width; x++)
		{
			if (this.getTaskAt(colume, row) != null)
			{
				return true;
			}
			colume++;
		}
		return false;
	}

	public boolean isThereATaskInColume(int colume)
	{
		int row = 0;
		for (int y = 0; y <= this.width; y++)
		{
			if (this.getTaskAt(row, colume) != null)
			{
				return true;
			}
			row++;
		}
		return false;
	}

	/**
	 * Move all tasks at the row and in the direction given.
	 * 
	 * @param row - row number or Y value of the position from the task
	 * @param up - true will move all the tasks up one, false will move all the tasks down one
	 */
	public void moveAll(int row, boolean up)
	{
		if (!this.editing)
		{
			this.editing = true;
			List<ITask> moveList = new ArrayList<ITask>();
			final int move = up ? -1 : 1;
			Vector2 targetPos;
			ITask tagetTask;
			/* Gather all task and remove them so they can be re-added wither there new positions */
			for (int x = 0; x <= this.width; x++)
			{
				for (int y = row; y <= this.hight; y++)
				{
					targetPos = new Vector2(x, y);
					tagetTask = this.getTaskAt(x, y);
					if (tagetTask != null)
					{
						// Add the task to the move list
						moveList.add(tagetTask);
						// Removes the task
						this.tasks.remove(targetPos);
					}
				}
			}
			/* Update all the task locations */
			for (ITask moveTask : moveList)
			{
				this.setTaskAt(moveTask.getCol(), moveTask.getRow() + move, moveTask);
			}
			this.editing = false;
		}
	}

	@Override
	public void insertTask(int col, int row, ITask task)
	{
		if (task != null && !this.editing)
		{
			if (this.getTaskAt(col, row) != null)
			{
				this.moveAll(row, false);
				this.setTaskAt(col, row, task);
			}
			else
			{
				this.setTaskAt(col, row, task);
			}

		}
	}

	@Override
	public void reset()
	{
		if (this.currentTask != null)
		{
			this.currentTask.reset();
		}
		this.currentPos = new Vector2(0, 0);
	}

	@Override
	public void setVar(String name, Object object)
	{
		if (name != null)
		{
			if (object != null)
			{
				this.varables.put(name, object);
			}
			else
			{
				this.varables.remove(name);
			}
		}

	}

	@Override
	public Object getVar(String name)
	{
		return this.varables.get(name);
	}

	@Override
	public NBTTagCompound save(NBTTagCompound nbt)
	{
		// Save process list
		NBTTagList taskList = new NBTTagList();
		for (Entry<Vector2, ITask> entry : this.tasks.entrySet())
		{
			entry.getValue().setPosition(entry.getKey().intX(), entry.getKey().intY());
			NBTTagCompound task = new NBTTagCompound();
			entry.getValue().save(task);
			if (this.currentTask != null && entry.getKey().equals(new Vector2(this.currentTask.getCol(), this.currentTask.getRow())))
			{
				task.setBoolean("currentTask", true);
				entry.getValue().saveProgress(task);
			}
			task.setString("methodName", entry.getValue().getMethodName());
			taskList.appendTag(task);
		}
		nbt.setTag("tasks", taskList);
		// save varables
		taskList = new NBTTagList();
		for (Entry<String, Object> var : this.varables.entrySet())
		{
			taskList.appendTag(NBTUtility.saveObject(var.getKey(), var.getValue()));
		}
		nbt.setTag("vars", taskList);
		return nbt;
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		// Load process list
		NBTTagList taskList = nbt.getTagList("tasks");
		for (int s = 0; s < taskList.tagCount(); ++s)
		{
			NBTTagCompound tag = (NBTTagCompound) taskList.tagAt(s);
			if (tag.hasKey("methodName"))
			{
				ITask task = TaskRegistry.getCommand(tag.getString("methodName"));
				if (task != null)
				{
					task = task.clone();
					if (task != null)
					{
						task.load(tag);
						if (tag.getBoolean("currentTask"))
						{
							this.currentTask = task;
							task.loadProgress(tag);
							this.currentPos = new Vector2(task.getCol(), task.getRow());
						}
						this.tasks.put(new Vector2(task.getCol(), task.getRow()), task);
						if (task.getCol() > this.width)
						{
							this.width = task.getCol();
						}
						if (task.getRow() > this.hight)
						{
							this.hight = task.getRow();
						}
					}
				}
				else
				{
					System.out.println("[CoreMachine]Error: failed to load task " + tag.getString("methodName"));
				}
			}
		}
		taskList = nbt.getTagList("vars");
		for (int s = 0; s < taskList.tagCount(); ++s)
		{
			NBTTagCompound tag = (NBTTagCompound) taskList.tagAt(s);
			this.varables.put(tag.getName(), NBTUtility.loadObject(tag, tag.getName()));
		}
	}

	@Override
	public Program clone()
	{
		Program program = new Program();
		program.load(this.save(new NBTTagCompound()));
		program.reset();
		return program;
	}

	@Override
	public Vector2 getSize()
	{
		return new Vector2(this.width, this.hight);
	}

}
