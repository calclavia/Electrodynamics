package mffs.container;

import mffs.slot.SlotBase;
import mffs.slot.SlotCard;
import mffs.tile.TileFortronCapacitor;
import net.minecraft.entity.player.EntityPlayer;
import resonant.lib.gui.ContainerBase;

public class ContainerFortronCapacitor extends ContainerBase
{
	private TileFortronCapacitor tileEntity;

	public ContainerFortronCapacitor(EntityPlayer player, TileFortronCapacitor tileEntity)
	{
		super(tileEntity);
		this.tileEntity = tileEntity;

		// Frequency Card
		this.addSlotToContainer(new SlotCard(this.tileEntity, 0, 9, 74));
		this.addSlotToContainer(new SlotCard(this.tileEntity, 1, 27, 74));

		// Upgrades
		this.addSlotToContainer(new SlotBase(this.tileEntity, 2, 154, 47));
		this.addSlotToContainer(new SlotBase(this.tileEntity, 3, 154, 67));
		this.addSlotToContainer(new SlotBase(this.tileEntity, 4, 154, 87));

		this.addPlayerInventory(player);
	}
}