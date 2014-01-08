package resonantinduction.assemblyline.armbot.command;

import java.util.ArrayList;

import resonantinduction.assemblyline.api.IArmbot;
import resonantinduction.assemblyline.api.coding.ITask;
import resonantinduction.assemblyline.armbot.TaskBaseArmbot;
import resonantinduction.assemblyline.armbot.TaskBaseProcess;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.helpers.HelperMethods;

/** Used by arms to break a specific block in a position.
 * 
 * @author Calclavia */
public class TaskBreak extends TaskBaseArmbot
{
    protected int breakTicks = 30;
    protected boolean keep = false;

    public TaskBreak()
    {
        this("break");
    }

    public TaskBreak(String name)
    {
        super(name);
        this.breakTicks = 30;
    }

    @Override
    public ProcessReturn onUpdate()
    {
        if (super.onUpdate() == ProcessReturn.CONTINUE)
        {

            Vector3 serachPosition = ((IArmbot) this.program.getMachine()).getHandPos();
            Pair<World, Vector3> location = this.program.getMachine().getLocation();
            Block block = Block.blocksList[serachPosition.getBlockID(location.left())];
            this.breakTicks--;
            if (block != null && breakTicks <= 0)
            {
                ArrayList<ItemStack> items = block.getBlockDropped(location.left(), serachPosition.intX(), serachPosition.intY(), serachPosition.intZ(), serachPosition.getBlockMetadata(location.left()), 0);

                if (!this.keep || items.size() > 1)
                {
                    HelperMethods.dropBlockAsItem(location.left(), serachPosition);
                }
                else
                {
                    ((IArmbot) this.program.getMachine()).grabObject(new EntityItem(location.left(), serachPosition.intX() + 0.5D, serachPosition.intY() + 0.5D, serachPosition.intZ() + 0.5D, items.get(0)));
                }

                location.left().setBlock(serachPosition.intX(), serachPosition.intY(), serachPosition.intZ(), 0, 0, 3);
                return ProcessReturn.DONE;
            }
        }

        /** Notes on break command Beds Break Wrong Multi blocks don't work */
        return ProcessReturn.GENERAL_ERROR;
    }

    @Override
    public TaskBaseProcess clone()
    {
        return new TaskBreak();
    }

    @Override
    public ITask loadProgress(NBTTagCompound nbt)
    {
        this.breakTicks = nbt.getInteger("breakTicks");
        return this;
    }

    @Override
    public NBTTagCompound saveProgress(NBTTagCompound nbt)
    {
        nbt.setInteger("breakTicks", this.breakTicks);
        return nbt;
    }

}
