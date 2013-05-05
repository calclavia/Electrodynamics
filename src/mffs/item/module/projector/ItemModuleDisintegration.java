package mffs.item.module.projector;

import java.util.Set;

import mffs.BlockDropDelayedEvent;
import mffs.ModularForceFieldSystem;
import mffs.api.IProjector;
import mffs.base.TileEntityBase.TilePacketType;
import mffs.item.module.ItemModule;
import mffs.tileentity.TileEntityForceFieldProjector;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.PacketManager;

public class ItemModuleDisintegration extends ItemModule
{
	private int blockCount = 0;

	public ItemModuleDisintegration(int id)
	{
		super(id, "moduleDisintegration");
		this.setMaxStackSize(1);
		this.setCost(20);
	}

	@Override
	public boolean onProject(IProjector projector, Set<Vector3> fields)
	{
		this.blockCount = 0;
		return false;
	}

	@Override
	public int onProject(IProjector projector, Vector3 position)
	{
		if (projector.getTicks() % 40 == 0)
		{
			TileEntity tileEntity = (TileEntity) projector;
			int blockID = position.getBlockID(tileEntity.worldObj);
			Block block = Block.blocksList[blockID];

			if (block != null)
			{
				PacketManager.sendPacketToClients(PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, (TileEntity) projector, TilePacketType.FXS.ordinal(), 2, position.intX(), position.intY(), position.intZ()), ((TileEntity) projector).worldObj);

				// TODO: Modularize this
				((TileEntityForceFieldProjector) projector).delayedEvents.add(new BlockDropDelayedEvent(39, block, tileEntity.worldObj, position));

				if (this.blockCount++ >= projector.getModuleCount(ModularForceFieldSystem.itemModuleSpeed) / 3)
				{
					return 2;
				}
				else
				{
					return 1;
				}
			}
		}

		return 1;
	}
}