package mffs.container;

import mffs.base.ContainerBase;
import mffs.slot.SlotBase;
import mffs.slot.SlotCard;
import mffs.tileentity.TileEntityBiometricIdentifier;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerBiometricIdentifier extends ContainerBase
{
	public ContainerBiometricIdentifier(EntityPlayer player, TileEntityBiometricIdentifier tileentity)
	{
		super(tileentity);

		this.addSlotToContainer(new SlotCard(tileentity, 0, 88, 91));

		this.addSlotToContainer(new SlotBase(tileentity, 1, 8, 46));
		this.addSlotToContainer(new SlotBase(tileentity, 2, 8, 91));

		for (int var4 = 0; var4 < 9; var4++)
		{
			this.addSlotToContainer(new SlotBase(tileentity, 3 + var4, 8 + var4 * 18, 111));
		}

		this.addPlayerInventory(player);
	}

}