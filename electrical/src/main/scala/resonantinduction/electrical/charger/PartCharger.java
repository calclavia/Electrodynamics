package resonantinduction.electrical.charger;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.prefab.part.PartInventoryPanel;
import resonantinduction.electrical.Electrical;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.energy.IEnergyInterface;
import calclavia.lib.utility.WrenchUtility;
import calclavia.lib.utility.inventory.InventoryUtility;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Micro part machine designed to charge all items contained inside of it. Doesn't contain its own
 * battery and only acts as an inventory. Items are charged each time doReceive is called by an
 * energy supplier.
 * 
 * @author Darkguardsman, converted to Part by Calclavia */
@UniversalClass
public class PartCharger extends PartInventoryPanel implements IEnergyInterface
{
    @Override
    public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
    {
        if (WrenchUtility.isUsableWrench(player, player.inventory.getCurrentItem(), x(), y(), z()))
        {
            if (!world().isRemote)
            {
                WrenchUtility.damageWrench(player, player.inventory.getCurrentItem(), x(), y(), z());
                facing = (byte) ((facing + 1) % 4);
                sendDescUpdate();
                tile().notifyPartChange(this);
            }

            return true;
        }

        if (item != null)
        {
            if (getStackInSlot(0) == null && item != null && CompatibilityModule.isHandler(item.getItem()))
            {
                setInventorySlotContents(0, item);
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);

                if (!world().isRemote)
                    sendDescUpdate();

                return true;
            }
        }

        if (getStackInSlot(0) != null)
        {
            InventoryUtility.dropItemStack(world(), new universalelectricity.api.vector.Vector3(player), getStackInSlot(0), 0);
            setInventorySlotContents(0, null);
            if (!world().isRemote)
                sendDescUpdate();
        }

        return true;
    }

    @Override
    public boolean canConnect(ForgeDirection direction, Object obj)
    {
        return obj instanceof IEnergyInterface && placementSide != direction.getOpposite();
    }

    @Override
    public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
    {
        if (receive > 0)
        {
            long energyUsed = 0;
            for (int slot = 0; slot < this.getSizeInventory(); slot++)
            {
                energyUsed += CompatibilityModule.chargeItem(this.getStackInSlot(slot), receive - energyUsed, doReceive);
                if (energyUsed >= receive)
                    break;
            }

            if (energyUsed > 0)
                this.markedForUpdate = true;

            return energyUsed;
        }
        return 0;
    }

    @Override
    public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
    {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(Vector3 pos, float frame, int pass)
    {
        if (pass == 0)
        {
            RenderCharger.INSTANCE.render(this, pos.x, pos.y, pos.z);
        }
    }

    @Override
    protected ItemStack getItem()
    {
        return new ItemStack(Electrical.itemCharger);
    }

    @Override
    public String getType()
    {
        return "resonant_induction_charger";
    }

    @Override
    public Iterable<ItemStack> getDrops()
    {
        List<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(getItem());

        for (int i = 0; i < getSizeInventory(); i++)
            if (getStackInSlot(i) != null)
                drops.add(getStackInSlot(i));

        return drops;
    }

    @Override
    public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
    {
        return slot < this.getSizeInventory() && stack != null && CompatibilityModule.isHandler(stack.getItem());
    }

    @Override
    public String toString()
    {
        return "[PartCharger]" + x() + "x " + y() + "y " + z() + "z " + getSlotMask() + "s ";
    }
}
