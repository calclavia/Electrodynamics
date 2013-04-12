package mffs.container;

import mffs.base.ContainerBase;
import mffs.slot.SlotBase;
import mffs.slot.SlotCard;
import mffs.tileentity.TileEntityFortronCapacitor;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerFortronCapacitor extends ContainerBase
{
	private TileEntityFortronCapacitor tileEntity;

	public ContainerFortronCapacitor(EntityPlayer player, TileEntityFortronCapacitor tileEntity)
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