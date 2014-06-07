package mffs.item.module.projector;

import java.util.Set;

import mffs.IDelayedEventHandler;
import mffs.MFFSHelper;
import mffs.ModularForceFieldSystem;
import mffs.base.TileMFFS.TilePacketType;
import mffs.base.TileMFFSInventory;
import mffs.event.BlockDropDelayedEvent;
import mffs.event.BlockInventoryDropDelayedEvent;
import mffs.item.module.ItemModule;
import mffs.tile.TileForceFieldProjector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.IFluidBlock;
import universalelectricity.api.vector.Vector3;
import calclavia.api.mffs.Blacklist;
import calclavia.api.mffs.IProjector;
import resonant.lib.network.PacketHandler;

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
				/**
				 * Placing a camouflage module makes the filter "inclusive". Otherwise it is
				 * exclusive.
				 */
				int blockMetadata = position.getBlockMetadata(tileEntity.worldObj);

				boolean filterMatch = false;

				for (int i : projector.getModuleSlots())
				{
					ItemStack filterStack = projector.getStackInSlot(i);

					if (MFFSHelper.getFilterBlock(filterStack) != null)
					{
						if (filterStack.isItemEqual(new ItemStack(blockID, 1, blockMetadata)) || (((ItemBlock) filterStack.getItem()).getBlockID() == blockID && projector.getModuleCount(ModularForceFieldSystem.itemModuleApproximation) > 0))
						{
							filterMatch = true;
							break;
						}
					}
				}

				if (projector.getModuleCount(ModularForceFieldSystem.itemModuleCamouflage) > 0 == !filterMatch)
				{
					return 1;
				}

				if (Blacklist.disintegrationBlacklist.contains(block) || block instanceof BlockFluid || block instanceof IFluidBlock)
				{
					return 1;
				}

				PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket((TileEntity) projector, TilePacketType.FXS.ordinal(), 2, position.intX(), position.intY(), position.intZ()), ((TileEntity) projector).worldObj);

				if (projector.getModuleCount(ModularForceFieldSystem.itemModuleCollection) > 0)
				{
					((TileForceFieldProjector) projector).queueEvent(new BlockInventoryDropDelayedEvent((IDelayedEventHandler) projector, 39, block, tileEntity.worldObj, position, (TileMFFSInventory) projector));
				}
				else
				{
					((TileForceFieldProjector) projector).queueEvent(new BlockDropDelayedEvent((IDelayedEventHandler) projector, 39, block, tileEntity.worldObj, position));
				}

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

	@Override
	public float getFortronCost(float amplifier)
	{
		return super.getFortronCost(amplifier) + (super.getFortronCost(amplifier) * amplifier);
	}
}