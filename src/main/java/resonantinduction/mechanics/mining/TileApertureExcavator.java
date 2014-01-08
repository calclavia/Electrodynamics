package resonantinduction.mechanics.mining;

import java.util.ArrayList;
import java.util.List;

import resonantinduction.api.MachineMiningEvent;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.minecraft.helpers.InvInteractionHelper;
import com.builtbroken.minecraft.prefab.TileEntityEnergyMachine;

/** @author Archadia */
public class TileApertureExcavator extends TileEntityEnergyMachine
{
    private InvInteractionHelper invExtractionHelper;
    private int lastY = -1;

    public TileApertureExcavator()
    {
        super(0, 3);
    }

    public InvInteractionHelper invHelper()
    {
        if (invExtractionHelper == null || invExtractionHelper.world != this.worldObj)
        {
            this.invExtractionHelper = new InvInteractionHelper(this.worldObj, new Vector3(this), new ArrayList<ItemStack>(), false);
        }
        return invExtractionHelper;
    }

    @Override
    public void updateEntity()
    {
        //TODO catch player place events and prevent them from placing blocks in the path of the drill head. This will save us time in coding blocks to represent the drill shafts
        super.updateEntity();
        if (!worldObj.isRemote && this.isFunctioning())
        {
            if (this.ticks % 20 == 0)
            {
                excavate();
            }
        }
    }

    @Override
    public boolean canFunction()
    {
        return super.canFunction() && this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
    }

    public void excavate()
    {
        if (lastY == -1)
        {
            lastY = this.yCoord - 1;
        }
        for (int y = this.yCoord - 1; y > 0 && y < this.yCoord; y--)
        {
            Vector3 target = new Vector3(this.xCoord, y, this.zCoord);
            if (this.consumePower(150, false))
            {
                Block block = Block.blocksList[target.getBlockID(this.worldObj)];
                if (MachineMiningEvent.doMachineMiningCheck(this.worldObj, target, this))
                {
                    List<ItemStack> items = MachineMiningEvent.getItemsMined(this.worldObj, target, this);
                    if (items != null)
                    {
                        for (ItemStack stack : items)
                        {
                            this.dropItems(stack);
                        }
                    }
                    worldObj.setBlockToAir(target.intX(), target.intY(), target.intZ());
                    this.consumePower(150, true);
                }
                else if (block != null)
                {
                    break;
                }
                if (y < this.lastY)
                {
                    this.lastY = y;
                    break;
                }
            }
        }
    }

    private void dropItems(ItemStack item)
    {
        if (item != null)
        {
            item = this.invHelper().storeItem(item, ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST);
            if (item != null)
            {
                this.invHelper().throwItem(new Vector3(this).modifyPositionFromSide(ForgeDirection.UP), item);
                item = null;
            }
        }
    }
}
