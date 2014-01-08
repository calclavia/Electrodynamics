package resonantinduction.assemblyline.armbot.command;

import java.util.List;

import resonantinduction.assemblyline.api.coding.IProgrammableMachine;
import resonantinduction.assemblyline.api.coding.args.ArgumentData;
import resonantinduction.assemblyline.armbot.TaskBaseProcess;
import net.minecraft.nbt.NBTTagCompound;

import com.builtbroken.common.science.units.UnitHelper;

public class TaskIdle extends TaskBaseProcess
{

    /** The amount of time in which the machine will idle. */
    public int idleTime = 80;
    private int totalIdleTime = 80;

    public TaskIdle()
    {
        super("wait");
        this.args.add(new ArgumentData("idleTime", 20));
    }

    @Override
    public ProcessReturn onMethodCalled()
    {
        if (super.onMethodCalled() == ProcessReturn.CONTINUE)
        {

            if (UnitHelper.tryToParseInt(this.getArg("idleTime")) > 0)
            {
                this.totalIdleTime = this.idleTime = UnitHelper.tryToParseInt(this.getArg("idleTime"));
                return ProcessReturn.CONTINUE;
            }

            return ProcessReturn.ARGUMENT_ERROR;
        }
        return ProcessReturn.GENERAL_ERROR;
    }

    @Override
    public ProcessReturn onUpdate()
    {
        if (this.idleTime > 0)
        {
            this.idleTime--;
            return ProcessReturn.CONTINUE;
        }
        return ProcessReturn.DONE;
    }

    @Override
    public void load(NBTTagCompound taskCompound)
    {
        super.load(taskCompound);
        this.totalIdleTime = taskCompound.getInteger("idleTotal");
    }

    @Override
    public void save(NBTTagCompound taskCompound)
    {
        super.save(taskCompound);
        taskCompound.setInteger("idleTotal", this.totalIdleTime);
    }

    @Override
    public TaskBaseProcess loadProgress(NBTTagCompound taskCompound)
    {
        super.loadProgress(taskCompound);
        this.idleTime = taskCompound.getInteger("idleTime");
        return this;
    }

    @Override
    public NBTTagCompound saveProgress(NBTTagCompound taskCompound)
    {
        super.saveProgress(taskCompound);
        taskCompound.setInteger("idleTime", this.idleTime);
        return taskCompound;
    }

    @Override
    public String toString()
    {
        return super.toString() + " " + Integer.toString(this.totalIdleTime);
    }

    @Override
    public TaskBaseProcess clone()
    {
        return new TaskIdle();
    }

    @Override
    public boolean canUseTask(IProgrammableMachine device)
    {
        return true;
    }

    @Override
    public void getToolTips(List<String> list)
    {
        super.getToolTips(list);
        list.add(" Wait: " + this.totalIdleTime);
    }

}
