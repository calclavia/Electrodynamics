package mffs;

import icbm.api.IBlockFrequency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import mffs.api.IProjector;
import mffs.api.fortron.IFortronFrequency;
import mffs.api.modules.IModuleAcceptor;
import mffs.api.security.IInterdictionMatrix;
import mffs.api.security.Permission;
import mffs.fortron.FrequencyGrid;
import mffs.item.module.projector.ItemModeCustom;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import universalelectricity.core.vector.Vector3;
import calclavia.lib.CalculationHelper;

/**
 * A class containing some general helpful functions.
 * 
 * @author Calclavia
 * 
 */
public class MFFSHelper
{
	public static void transferFortron(IFortronFrequency transferer, Set<IFortronFrequency> frequencyTiles, TransferMode transferMode, int limit)
	{
		if (transferer != null && frequencyTiles.size() > 1)
		{
			/**
			 * Check spread mode. Equal, Give All, Take All
			 */
			int totalFortron = 0;
			int totalCapacity = 0;

			for (IFortronFrequency machine : frequencyTiles)
			{
				if (machine != null)
				{
					totalFortron += machine.getFortronEnergy();
					totalCapacity += machine.getFortronCapacity();
				}
			}

			if (totalFortron > 0 && totalCapacity > 0)
			{
				/**
				 * Test each mode and based on the mode, spread Fortron energy.
				 */
				switch (transferMode)
				{
					case EQUALIZE:
					{
						for (IFortronFrequency machine : frequencyTiles)
						{
							if (machine != null)
							{
								double capacityPercentage = (double) machine.getFortronCapacity() / (double) totalCapacity;
								int amountToSet = (int) (totalFortron * capacityPercentage);
								doTransferFortron(transferer, machine, amountToSet - machine.getFortronEnergy(), limit);
							}
						}

						break;
					}
					case DISTRIBUTE:
					{
						final int amountToSet = totalFortron / frequencyTiles.size();

						for (IFortronFrequency machine : frequencyTiles)
						{
							if (machine != null)
							{
								doTransferFortron(transferer, machine, amountToSet - machine.getFortronEnergy(), limit);
							}
						}

						break;
					}
					case DRAIN:
					{
						frequencyTiles.remove(transferer);

						for (IFortronFrequency machine : frequencyTiles)
						{
							if (machine != null)
							{
								double capacityPercentage = (double) machine.getFortronCapacity() / (double) totalCapacity;
								int amountToSet = (int) (totalFortron * capacityPercentage);

								if (amountToSet - machine.getFortronEnergy() > 0)
								{
									doTransferFortron(transferer, machine, amountToSet - machine.getFortronEnergy(), limit);
								}
							}
						}

						break;
					}
					case FILL:
					{
						if (transferer.getFortronEnergy() < transferer.getFortronCapacity())
						{
							frequencyTiles.remove(transferer);

							// The amount of energy required to be full.
							int requiredFortron = transferer.getFortronCapacity() - transferer.getFortronEnergy();

							for (IFortronFrequency machine : frequencyTiles)
							{
								if (machine != null)
								{
									int amountToConsume = Math.min(requiredFortron, machine.getFortronEnergy());
									int amountToSet = -machine.getFortronEnergy() - amountToConsume;

									if (amountToConsume > 0)
									{
										doTransferFortron(transferer, machine, amountToSet - machine.getFortronEnergy(), limit);
									}
								}
							}
						}

						break;
					}
				}
			}
		}
	}

	/**
	 * Tries to transfer Fortron to a specific machine from this capacitor. Renders an animation on
	 * the client side.
	 * 
	 * @param receiver: The machine to be transfered to.
	 * @param joules: The amount of energy to be transfered.
	 */
	public static void doTransferFortron(IFortronFrequency transferer, IFortronFrequency receiver, int joules, int limit)
	{
		if (transferer != null && receiver != null)
		{
			TileEntity tileEntity = ((TileEntity) transferer);
			World world = tileEntity.worldObj;

			boolean isCamo = false;

			if (transferer instanceof IModuleAcceptor)
			{
				isCamo = ((IModuleAcceptor) transferer).getModuleCount(ModularForceFieldSystem.itemModuleCamouflage) > 0;
			}

			if (joules > 0)
			{
				// Transfer energy to receiver.
				joules = Math.min(joules, limit);
				int toBeInjected = receiver.provideFortron(transferer.requestFortron(joules, false), false);
				toBeInjected = transferer.requestFortron(receiver.provideFortron(toBeInjected, true), true);

				// Draw Beam Effect
				if (world.isRemote && toBeInjected > 0 && !isCamo)
				{
					ModularForceFieldSystem.proxy.renderBeam(world, Vector3.add(new Vector3(tileEntity), 0.5), Vector3.add(new Vector3((TileEntity) receiver), 0.5), 0.6f, 0.6f, 1, 20);
				}
			}
			else
			{
				// Take energy from receiver.
				joules = Math.min(Math.abs(joules), limit);
				int toBeEjected = transferer.provideFortron(receiver.requestFortron(joules, false), false);
				toBeEjected = receiver.requestFortron(transferer.provideFortron(toBeEjected, true), true);

				// Draw Beam Effect
				if (world.isRemote && toBeEjected > 0 && !isCamo)
				{
					ModularForceFieldSystem.proxy.renderBeam(world, Vector3.add(new Vector3((TileEntity) receiver), 0.5), Vector3.add(new Vector3(tileEntity), 0.5), 0.6f, 0.6f, 1, 20);
				}

			}
		}

	}

	/**
	 * Gets the nearest active Interdiction Matrix.
	 */
	public static IInterdictionMatrix getNearestInterdictionMatrix(World world, Vector3 position)
	{
		for (IBlockFrequency frequencyTile : FrequencyGrid.instance().get())
		{
			if (((TileEntity) frequencyTile).worldObj == world && frequencyTile instanceof IInterdictionMatrix)
			{
				IInterdictionMatrix interdictionMatrix = (IInterdictionMatrix) frequencyTile;

				if (interdictionMatrix.isActive())
				{
					if (position.distanceTo(new Vector3((TileEntity) interdictionMatrix)) <= interdictionMatrix.getActionRange())
					{
						return interdictionMatrix;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Returns true of the interdictionMatrix has a specific set of permissions.
	 * 
	 * @param interdictionMatrix
	 * @param username
	 * @param permissions
	 * @return
	 */
	public static boolean isPermittedByInterdictionMatrix(IInterdictionMatrix interdictionMatrix, String username, Permission... permissions)
	{
		if (interdictionMatrix != null)
		{
			if (interdictionMatrix.isActive())
			{
				if (interdictionMatrix.getBiometricIdentifier() != null)
				{
					for (Permission permission : permissions)
					{
						if (!interdictionMatrix.getBiometricIdentifier().isAccessGranted(username, permission))
						{
							if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.itemModuleInvert) > 0)
							{
								return true;
							}
							else
							{
								return false;
							}
						}
					}
				}
			}
		}

		if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.itemModuleInvert) > 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public static List<String> splitStringPerWord(String string, int wordsPerLine)
	{
		String[] words = string.split(" ");
		List<String> lines = new ArrayList<String>();

		for (int lineCount = 0; lineCount < Math.ceil((float) words.length / (float) wordsPerLine); lineCount++)
		{
			String stringInLine = "";

			for (int i = lineCount * wordsPerLine; i < Math.min(wordsPerLine + lineCount * wordsPerLine, words.length); i++)
			{
				stringInLine += words[i] + " ";
			}

			lines.add(stringInLine.trim());
		}

		return lines;
	}

	/**
	 * Gets the first itemStack that is an ItemBlock in this TileEntity or in nearby chests.
	 * 
	 * @param itemStack
	 * @return
	 */
	public static ItemStack getFirstItemBlock(TileEntity tileEntity, ItemStack itemStack)
	{
		return getFirstItemBlock(tileEntity, itemStack, true);
	}

	public static ItemStack getFirstItemBlock(TileEntity tileEntity, ItemStack itemStack, boolean recur)
	{
		if (tileEntity instanceof IProjector)
		{
			for (int i : ((IProjector) tileEntity).getModuleSlots())
			{
				ItemStack checkStack = getFirstItemBlock(i, ((IProjector) tileEntity), itemStack);

				if (checkStack != null)
				{
					return checkStack;
				}
			}
		}
		else if (tileEntity instanceof IInventory)
		{
			IInventory inventory = (IInventory) tileEntity;

			for (int i = 0; i < inventory.getSizeInventory(); i++)
			{
				ItemStack checkStack = getFirstItemBlock(i, inventory, itemStack);

				if (checkStack != null)
				{
					return checkStack;
				}
			}
		}

		if (recur)
		{
			for (int i = 0; i < 6; i++)
			{
				ForgeDirection direction = ForgeDirection.getOrientation(i);
				Vector3 vector = new Vector3(tileEntity);
				vector.modifyPositionFromSide(direction);
				TileEntity checkTile = vector.getTileEntity(tileEntity.worldObj);

				if (checkTile != null)
				{
					ItemStack checkStack = getFirstItemBlock(checkTile, itemStack, false);

					if (checkStack != null)
					{
						return checkStack;
					}
				}
			}
		}

		return null;
	}

	public static ItemStack getFirstItemBlock(int i, IInventory inventory, ItemStack itemStack)
	{
		ItemStack checkStack = inventory.getStackInSlot(i);

		if (checkStack != null && checkStack.getItem() instanceof ItemBlock)
		{
			if (itemStack == null || checkStack.isItemEqual(itemStack))
			{
				return checkStack;
			}
		}
		return null;
	}

	public static Block getFilterBlock(ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof ItemBlock)
			{
				if (((ItemBlock) itemStack.getItem()).getBlockID() < Block.blocksList.length)
				{
					Block block = Block.blocksList[((ItemBlock) itemStack.getItem()).getBlockID()];

					if (block.renderAsNormalBlock())
					{
						return block;
					}
				}
			}
		}

		return null;

	}

	public static ItemStack getCamoBlock(IProjector projector, Vector3 position)
	{
		if (projector != null)
		{
			if (!((TileEntity) projector).worldObj.isRemote)
			{
				if (projector != null)
				{
					if (projector.getModuleCount(ModularForceFieldSystem.itemModuleCamouflage) > 0)
					{
						if (projector.getMode() instanceof ItemModeCustom)
						{
							HashMap<Vector3, int[]> fieldMap = ((ItemModeCustom) projector.getMode()).getFieldBlockMap(projector, projector.getModeStack());

							if (fieldMap != null)
							{
								Vector3 fieldCenter = new Vector3((TileEntity) projector).add(projector.getTranslation());
								Vector3 relativePosition = position.clone().subtract(fieldCenter);
								CalculationHelper.rotateByAngle(relativePosition, -projector.getRotationYaw(), -projector.getRotationPitch());
								int[] blockInfo = fieldMap.get(relativePosition.round());

								if (blockInfo != null && blockInfo[0] > 0)
								{
									return new ItemStack(Block.blocksList[blockInfo[0]], 1, blockInfo[1]);
								}
							}
						}

						for (int i : projector.getModuleSlots())
						{
							ItemStack checkStack = projector.getStackInSlot(i);
							Block block = getFilterBlock(checkStack);

							if (block != null)
							{
								return checkStack;
							}
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Gets a compound from an itemStack.
	 * 
	 * @param itemStack
	 * @return
	 */
	public static NBTTagCompound getNBTTagCompound(ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			return itemStack.getTagCompound();
		}

		return null;
	}

	public static boolean hasPermission(World world, Vector3 position, Permission permission, EntityPlayer player)
	{
		IInterdictionMatrix interdictionMatrix = getNearestInterdictionMatrix(world, position);

		if (interdictionMatrix != null)
		{
			return isPermittedByInterdictionMatrix(interdictionMatrix, player.username, permission);
		}

		return true;
	}

	public static boolean hasPermission(World world, Vector3 position, Action action, EntityPlayer player)
	{
		IInterdictionMatrix interdictionMatrix = getNearestInterdictionMatrix(world, position);

		if (interdictionMatrix != null)
		{
			return MFFSHelper.hasPermission(world, position, interdictionMatrix, action, player);
		}

		return true;
	}

	public static boolean hasPermission(World world, Vector3 position, IInterdictionMatrix interdictionMatrix, Action action, EntityPlayer player)
	{
		boolean hasPermission = true;

		if (action == Action.RIGHT_CLICK_BLOCK && position.getTileEntity(world) != null)
		{
			if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.itemModuleBlockAccess) > 0)
			{
				hasPermission = false;

				if (isPermittedByInterdictionMatrix(interdictionMatrix, player.username, Permission.BLOCK_ACCESS))
				{
					hasPermission = true;
				}
			}
		}

		if (hasPermission)
		{
			if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.itemModuleBlockAlter) > 0 && (player.getCurrentEquippedItem() != null || action == Action.LEFT_CLICK_BLOCK))
			{
				hasPermission = false;

				if (isPermittedByInterdictionMatrix(interdictionMatrix, player.username, Permission.BLOCK_ALTER))
				{
					hasPermission = true;
				}
			}
		}

		return hasPermission;
	}

}
