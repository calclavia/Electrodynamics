package mffs.item.module.projector;

import java.util.HashMap;
import java.util.Set;

import mffs.ModularForceFieldSystem;
import mffs.api.Blacklist;
import mffs.api.EventStabilize;
import mffs.api.IProjector;
import mffs.base.TileMFFS.TilePacketType;
import mffs.item.mode.ItemModeCustom;
import mffs.item.module.ItemModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.IFluidBlock;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorHelper;
import calclavia.lib.network.PacketHandler;

public class ItemModuleStablize extends ItemModule
{
	private int blockCount = 0;

	public ItemModuleStablize(int id)
	{
		super(id, "moduleStabilize");
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
		int[] blockInfo = null;

		if (projector.getTicks() % 40 == 0)
		{
			if (projector.getMode() instanceof ItemModeCustom && !(projector.getModuleCount(ModularForceFieldSystem.itemModuleCamouflage) > 0))
			{
				HashMap<Vector3, int[]> fieldBlocks = ((ItemModeCustom) projector.getMode()).getFieldBlockMap(projector, projector.getModeStack());
				Vector3 fieldCenter = new Vector3((TileEntity) projector).translate(projector.getTranslation());
				Vector3 relativePosition = position.clone().subtract(fieldCenter);
				relativePosition.rotate(-projector.getRotationYaw(), -projector.getRotationPitch());
				blockInfo = fieldBlocks.get(relativePosition.round());
			}

			// Search nearby inventories to extract blocks.
			for (int dir = 0; dir < 6; dir++)
			{
				ForgeDirection direction = ForgeDirection.getOrientation(dir);
				TileEntity tileEntity = VectorHelper.getTileEntityFromSide(((TileEntity) projector).worldObj, new Vector3((TileEntity) projector), direction);

				if (tileEntity instanceof IInventory)
				{
					IInventory inventory = ((IInventory) tileEntity);

					for (int i = 0; i < inventory.getSizeInventory(); i++)
					{
						ItemStack checkStack = inventory.getStackInSlot(i);

						if (checkStack != null)
						{
							EventStabilize evt = new EventStabilize(((TileEntity) projector).worldObj, position.intX(), position.intY(), position.intZ(), checkStack);
							MinecraftForge.EVENT_BUS.post(evt);

							if (!evt.isCanceled())
							{
								if (checkStack.getItem() instanceof ItemBlock)
								{
									if (blockInfo == null || (blockInfo[0] == ((ItemBlock) checkStack.getItem()).getBlockID() && (blockInfo[1] == checkStack.getItemDamage() || projector.getModuleCount(ModularForceFieldSystem.itemModuleApproximation) > 0)) || (projector.getModuleCount(ModularForceFieldSystem.itemModuleApproximation) > 0 && this.isApproximationEqual(blockInfo[0], checkStack)))
									{
										try
										{
											if (((TileEntity) projector).worldObj.canPlaceEntityOnSide(((ItemBlock) checkStack.getItem()).getBlockID(), position.intX(), position.intY(), position.intZ(), false, 0, null, checkStack))
											{
												int metadata = blockInfo != null ? blockInfo[1] : (checkStack.getHasSubtypes() ? checkStack.getItemDamage() : 0);
												Block block = blockInfo != null ? Block.blocksList[blockInfo[0]] : null;

												if (Blacklist.stabilizationBlacklist.contains(block) || block instanceof BlockFluid || block instanceof IFluidBlock)
												{
													return 1;
												}

												((ItemBlock) checkStack.getItem()).placeBlockAt(checkStack, null, ((TileEntity) projector).worldObj, position.intX(), position.intY(), position.intZ(), 0, 0, 0, 0, metadata);
												inventory.decrStackSize(i, 1);
												PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket((TileEntity) projector, TilePacketType.FXS.ordinal(), 1, position.intX(), position.intY(), position.intZ()), ((TileEntity) projector).worldObj);

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
										catch (Exception e)
										{
											e.printStackTrace();
										}
									}
								}
							}
							else
							{
								return 1;
							}
						}
					}
				}
			}
		}

		return 1;
	}

	private boolean isApproximationEqual(int id, ItemStack checkStack)
	{
		return id == Block.grass.blockID && ((ItemBlock) checkStack.getItem()).getBlockID() == Block.dirt.blockID;
	}

	@Override
	public float getFortronCost(float amplifier)
	{
		return super.getFortronCost(amplifier) + (super.getFortronCost(amplifier) * amplifier);
	}
}
