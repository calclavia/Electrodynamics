package mffs.field;

import mffs.slot.SlotBase;
import mffs.slot.SlotCard;
import mffs.field.TileElectromagnetProjector;
import net.minecraft.entity.player.EntityPlayer;
import resonant.lib.gui.ContainerBase;

public class ContainerForceFieldProjector extends ContainerBase
{
	public ContainerForceFieldProjector(EntityPlayer player, TileElectromagnetProjector tileEntity)
	{
		super(tileEntity);

		/**
		 * Frequency Card
		 */
		this.addSlotToContainer(new SlotCard(tileEntity, 0, 10, 89));
		this.addSlotToContainer(new SlotCard(tileEntity, 1, 28, 89));

		/**
		 * Force Field Manipulation Matrix. Center slot is the mode.
		 */
		// Mode
		this.addSlotToContainer(new SlotBase(tileEntity, 2, 118, 45));

		int i = 3;
		// Misc Modules
		for (int xSlot = 0; xSlot < 4; xSlot++)
		{
			for (int ySlot = 0; ySlot < 4; ySlot++)
			{
				if (!(xSlot == 1 && ySlot == 1) && !(xSlot == 2 && ySlot == 2) && !(xSlot == 1 && ySlot == 2) && !(xSlot == 2 && ySlot == 1))
				{
					this.addSlotToContainer(new SlotBase(tileEntity, i, 91 + 18 * xSlot, 18 + 18 * ySlot));
					i++;
				}
			}
		}

		// Misc Modules
		for (int xSlot = 0; xSlot < 3; xSlot++)
		{
			for (int ySlot = 0; ySlot < 2; ySlot++)
			{
				this.addSlotToContainer(new SlotBase(tileEntity, i, 31 + 18 * xSlot, 36 + 18 * ySlot));
				i++;
			}
		}

		this.addPlayerInventory(player);
	}
}