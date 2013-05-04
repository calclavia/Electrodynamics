package mffs.item.module.projector;

import mffs.ModularForceFieldSystem;
import mffs.api.IProjector;
import mffs.base.TileEntityBase.TilePacketType;
import mffs.item.module.ItemModule;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.PacketManager;

public class ItemModuleDisintegration extends ItemModule
{
	public ItemModuleDisintegration(int id)
	{
		super(id, "moduleDisintegration");
		this.setMaxStackSize(1);
		this.setCost(20);
	}

	@Override
	public boolean onProject(IProjector projector, Vector3 position)
	{
		if (projector.getTicks() % 40 == 0)
		{
			TileEntity tileEntity = (TileEntity) projector;
			int blockID = position.getBlockID(tileEntity.worldObj);
			Block block = Block.blocksList[blockID];

			if (block != null)
			{
				PacketManager.sendPacketToClients(PacketManager.getPacket(ModularForceFieldSystem.CHANNEL, (TileEntity) projector, TilePacketType.FXS.ordinal(), 2, position.intX(), position.intY(), position.intZ()), ((TileEntity) projector).worldObj);

				if (projector.getTicks() % 80 == 0)
				{
					block.dropBlockAsItem(tileEntity.worldObj, position.intX(), position.intY(), position.intZ(), position.getBlockMetadata(tileEntity.worldObj), 0);
					position.setBlock(tileEntity.worldObj, 0);
				}
			}
			else
			{
				return false;
			}
		}

		return true;
	}
}