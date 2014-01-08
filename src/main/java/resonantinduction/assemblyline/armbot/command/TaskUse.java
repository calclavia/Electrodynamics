package resonantinduction.assemblyline.armbot.command;

import java.util.List;

import resonantinduction.assemblyline.api.IArmbot;
import resonantinduction.assemblyline.api.IArmbotUseable;
import resonantinduction.assemblyline.api.coding.IProcessTask;
import resonantinduction.assemblyline.api.coding.args.ArgumentIntData;
import resonantinduction.assemblyline.armbot.TaskBaseArmbot;
import resonantinduction.assemblyline.armbot.TaskBaseProcess;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.builtbroken.common.science.units.UnitHelper;

public class TaskUse extends TaskBaseArmbot
{

    protected int times, curTimes;

    public TaskUse()
    {
        super("use");
        this.args.add(new ArgumentIntData("repeat", 1, Integer.MAX_VALUE, 1));
    }

    @Override
    public ProcessReturn onMethodCalled()
    {
        if (super.onMethodCalled() == ProcessReturn.CONTINUE)
        {
            this.curTimes = 0;
            this.times = UnitHelper.tryToParseInt(this.getArg("repeat"));
            return ProcessReturn.CONTINUE;
        }
        return ProcessReturn.GENERAL_ERROR;
    }

    @Override
    public ProcessReturn onUpdate()
    {
        if (super.onUpdate() == ProcessReturn.CONTINUE)
        {
            Block block = Block.blocksList[((IArmbot) this.program.getMachine()).getHandPos().getBlockID(this.program.getMachine().getLocation().left())];
            TileEntity targetTile = ((IArmbot) this.program.getMachine()).getHandPos().getTileEntity(this.program.getMachine().getLocation().left());

            if (targetTile != null)
            {
                if (targetTile instanceof IArmbotUseable)
                {
                    ((IArmbotUseable) targetTile).onUse(((IArmbot) this.program.getMachine()), this.getArgs());
                }

            }
            else if (block != null)
            {
                try
                {
                    boolean f = block.onBlockActivated(this.program.getMachine().getLocation().left(), ((IArmbot) this.program.getMachine()).getHandPos().intX(), ((IArmbot) this.program.getMachine()).getHandPos().intY(), ((IArmbot) this.program.getMachine()).getHandPos().intZ(), null, 0, 0, 0, 0);
                }
                catch (Exception e)
                {

                    e.printStackTrace();
                }

            }

            this.curTimes++;

            if (this.curTimes >= this.times)
            {
                return ProcessReturn.DONE;
            }

            return ProcessReturn.CONTINUE;
        }
        return ProcessReturn.GENERAL_ERROR;
    }

    @Override
    public String toString()
    {
        return "USE " + Integer.toString(this.times);
    }

    @Override
    public void load(NBTTagCompound taskCompound)
    {
        super.load(taskCompound);
        this.times = taskCompound.getInteger("useTimes");
    }

    @Override
    public void save(NBTTagCompound taskCompound)
    {
        super.save(taskCompound);
        taskCompound.setInteger("useTimes", this.times);
    }

    @Override
    public IProcessTask loadProgress(NBTTagCompound nbt)
    {
        this.curTimes = nbt.getInteger("useCurTimes");
        return this;
    }

    @Override
    public NBTTagCompound saveProgress(NBTTagCompound nbt)
    {
        nbt.setInteger("useCurTimes", this.curTimes);
        return nbt;
    }

    @Override
    public TaskBaseProcess clone()
    {
        return new TaskUse();
    }

    @Override
    public void getToolTips(List<String> list)
    {
        super.getToolTips(list);
        list.add(" Repeat:   " + this.times);
    }
}
