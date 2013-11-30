package mffs.item.mode;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import mffs.MFFSHelper;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.ICache;
import mffs.api.IFieldInteraction;
import mffs.api.IProjector;
import mffs.api.modules.IProjectorMode;
import mffs.item.module.projector.ItemModuleArray;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;
import calclavia.lib.NBTFileLoader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemModeCustom extends ItemMode implements ICache
{
	private static final String NBT_ID = "id";
	private static final String NBT_MODE = "mode";
	private static final String NBT_POINT_1 = "point1";
	private static final String NBT_POINT_2 = "point2";
	private static final String NBT_FIELD_BLOCK_LIST = "fieldPoints";
	private static final String NBT_FIELD_BLOCK_ID = "blockID";
	private static final String NBT_FIELD_BLOCK_METADATA = "blockMetadata";
	private static final String NBT_FIELD_SIZE = "fieldSize";
	private static final String NBT_FILE_SAVE_PREFIX = "custom_mode_";
	private final HashMap<String, Object> cache = new HashMap<String, Object>();

	public ItemModeCustom(int i)
	{
		super(i, "modeCustom");
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List list, boolean par4)
	{
		NBTTagCompound nbt = MFFSHelper.getNBTTagCompound(itemStack);

		list.add("Mode: " + (nbt.getBoolean(NBT_MODE) ? "Subtraction" : "Additive"));

		Vector3 point1 = new Vector3(nbt.getCompoundTag(NBT_POINT_1));
		list.add("Point 1: " + point1.intX() + ", " + point1.intY() + ", " + point1.intZ());

		Vector3 point2 = new Vector3(nbt.getCompoundTag(NBT_POINT_2));
		list.add("Point 2: " + point2.intX() + ", " + point2.intY() + ", " + point2.intZ());

		int modeID = nbt.getInteger(NBT_ID);

		if (modeID > 0)
		{
			list.add("Mode ID: " + modeID);

			int fieldSize = nbt.getInteger(NBT_FIELD_SIZE);

			if (fieldSize > 0)
			{
				list.add("Field size: " + fieldSize);
			}
			else
			{
				list.add("Field not saved.");
			}
		}

		if (GuiScreen.isShiftKeyDown())
		{
			super.addInformation(itemStack, par2EntityPlayer, list, par4);
		}
		else
		{
			list.add("Hold shift for more...");
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer)
	{
		if (!world.isRemote)
		{
			if (entityPlayer.isSneaking())
			{
				NBTTagCompound nbt = MFFSHelper.getNBTTagCompound(itemStack);

				if (nbt != null)
				{
					Vector3 point1 = new Vector3(nbt.getCompoundTag(NBT_POINT_1));
					Vector3 point2 = new Vector3(nbt.getCompoundTag(NBT_POINT_2));

					if (nbt.hasKey(NBT_POINT_1) && nbt.hasKey(NBT_POINT_2) && !point1.equals(point2))
					{
						if (point1.distance(point2) < Settings.MAX_FORCE_FIELD_SCALE)
						{
							// Clear NBT Data
							nbt.removeTag(NBT_POINT_1);
							nbt.removeTag(NBT_POINT_2);

							Vector3 midPoint = new Vector3();
							midPoint.x = (point1.x + point2.x) / 2;
							midPoint.y = (point1.y + point2.y) / 2;
							midPoint.z = (point1.z + point2.z) / 2;
							midPoint = midPoint.floor();

							// Center the two coords to origin.
							point1.subtract(midPoint);
							point2.subtract(midPoint);

							Vector3 minPoint = new Vector3(Math.min(point1.x, point2.x), Math.min(point1.y, point2.y), Math.min(point1.z, point2.z));
							Vector3 maxPoint = new Vector3(Math.max(point1.x, point2.x), Math.max(point1.y, point2.y), Math.max(point1.z, point2.z));

							NBTTagCompound saveNBT = NBTFileLoader.loadData(this.getSaveDirectory(), NBT_FILE_SAVE_PREFIX + getModeID(itemStack));

							if (saveNBT == null)
							{
								saveNBT = new NBTTagCompound();
							}

							NBTTagList list;

							if (saveNBT.hasKey(NBT_FIELD_BLOCK_LIST))
							{
								list = (NBTTagList) saveNBT.getTag(NBT_FIELD_BLOCK_LIST);
							}
							else
							{
								list = new NBTTagList();
							}

							for (int x = minPoint.intX(); x <= maxPoint.intX(); x++)
							{
								for (int y = minPoint.intY(); y <= maxPoint.intY(); y++)
								{
									for (int z = minPoint.intZ(); z <= maxPoint.intZ(); z++)
									{
										Vector3 position = new Vector3(x, y, z);
										Vector3 targetCheck = Vector3.add(midPoint, position);
										int blockID = targetCheck.getBlockID(world);

										if (blockID > 0)
										{
											if (!nbt.getBoolean(NBT_MODE))
											{
												NBTTagCompound vectorTag = new NBTTagCompound();
												position.writeToNBT(vectorTag);
												vectorTag.setInteger(NBT_FIELD_BLOCK_ID, blockID);
												vectorTag.setInteger(NBT_FIELD_BLOCK_METADATA, targetCheck.getBlockMetadata(world));
												list.appendTag(vectorTag);
											}
											else
											{
												for (int i = 0; i < list.tagCount(); i++)
												{
													Vector3 vector = Vector3.readFromNBT((NBTTagCompound) list.tagAt(i));

													if (vector.equals(position))
													{
														list.removeTag(i);
													}
												}
											}
										}
									}
								}
							}

							saveNBT.setTag(NBT_FIELD_BLOCK_LIST, list);

							nbt.setInteger(NBT_FIELD_SIZE, list.tagCount());

							NBTFileLoader.saveData(getSaveDirectory(), NBT_FILE_SAVE_PREFIX + getModeID(itemStack), saveNBT);

							this.clearCache();

							entityPlayer.addChatMessage("Field structure saved.");
						}
					}
				}
			}
			else
			{

				NBTTagCompound nbt = MFFSHelper.getNBTTagCompound(itemStack);

				if (nbt != null)
				{
					nbt.setBoolean(NBT_MODE, !nbt.getBoolean(NBT_MODE));
					entityPlayer.addChatMessage("Changed selection mode to " + (nbt.getBoolean(NBT_MODE) ? "subtraction" : "additive"));
				}
			}
		}

		return itemStack;
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer entityPlayer, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (!world.isRemote)
		{
			NBTTagCompound nbt = MFFSHelper.getNBTTagCompound(itemStack);

			if (nbt != null)
			{
				Vector3 point1 = new Vector3(nbt.getCompoundTag(NBT_POINT_1));

				if (!nbt.hasKey(NBT_POINT_1) || point1.equals(new Vector3(0, 0, 0)))
				{
					nbt.setCompoundTag(NBT_POINT_1, new Vector3(x, y, z).writeToNBT(new NBTTagCompound()));
					entityPlayer.addChatMessage("Set point 1: " + x + ", " + y + ", " + z + ".");
				}
				else
				{
					nbt.setCompoundTag(NBT_POINT_2, new Vector3(x, y, z).writeToNBT(new NBTTagCompound()));
					entityPlayer.addChatMessage("Set point 2: " + x + ", " + y + ", " + z + ".");
				}

			}
		}

		return true;
	}

	public int getModeID(ItemStack itemStack)
	{
		NBTTagCompound nbt = MFFSHelper.getNBTTagCompound(itemStack);

		int id = nbt.getInteger(NBT_ID);

		if (id <= 0)
		{
			nbt.setInteger(NBT_ID, getNextAvaliableID());
			id = nbt.getInteger(NBT_ID);
		}

		return id;
	}

	public int getNextAvaliableID()
	{
		int i = 1;

		for (final File fileEntry : this.getSaveDirectory().listFiles())
		{
			i++;
		}

		return i;
	}

	public File getSaveDirectory()
	{
		File saveDirectory = NBTFileLoader.getSaveDirectory(MinecraftServer.getServer().getFolderName());

		if (!saveDirectory.exists())
		{
			saveDirectory.mkdir();
		}

		File file = new File(saveDirectory, "mffs");

		if (!file.exists())
		{
			file.mkdir();
		}

		return file;
	}

	public Set<Vector3> getFieldBlocks(IFieldInteraction projector, ItemStack itemStack)
	{
		return this.getFieldBlockMapClean(projector, itemStack).keySet();
	}

	@SuppressWarnings("unchecked")
	public HashMap<Vector3, int[]> getFieldBlockMap(IFieldInteraction projector, ItemStack itemStack)
	{
		String cacheID = "itemStack_" + itemStack.hashCode();

		if (Settings.USE_CACHE)
		{
			if (this.cache.containsKey(cacheID))
			{
				if (this.cache.get(cacheID) instanceof HashMap)
				{
					return (HashMap<Vector3, int[]>) this.cache.get(cacheID);
				}
			}
		}

		final HashMap<Vector3, int[]> fieldMap = this.getFieldBlockMapClean(projector, itemStack);

		/**
		 * Array out the field map.
		 */
		if (projector.getModuleCount(ModularForceFieldSystem.itemModuleArray) > 0)
		{
			HashMap<ForgeDirection, Integer> longestDirectional = ((ItemModuleArray) ModularForceFieldSystem.itemModuleArray).getDirectionWidthMap(fieldMap.keySet());

			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				int copyAmount = projector.getSidedModuleCount(ModularForceFieldSystem.itemModuleArray, direction);
				int directionalDisplacement = (Math.abs(longestDirectional.get(direction)) + Math.abs(longestDirectional.get(direction.getOpposite()))) + 1;

				for (int i = 0; i < copyAmount; i++)
				{
					int directionalDisplacementScale = directionalDisplacement * (i + 1);

					for (Vector3 originalFieldBlock : this.getFieldBlocks(projector, itemStack))
					{
						Vector3 newFieldBlock = originalFieldBlock.clone().translate(new Vector3(direction).scale(directionalDisplacementScale));
						fieldMap.put(newFieldBlock, fieldMap.get(originalFieldBlock));
					}
				}
			}
		}

		if (Settings.USE_CACHE)
		{
			this.cache.put(cacheID, fieldMap);
		}

		return fieldMap;
	}

	public HashMap<Vector3, int[]> getFieldBlockMapClean(IFieldInteraction projector, ItemStack itemStack)
	{
		float scale = (float) projector.getModuleCount(ModularForceFieldSystem.itemModuleScale) / 3;

		final HashMap<Vector3, int[]> fieldBlocks = new HashMap<Vector3, int[]>();

		if (this.getSaveDirectory() != null)
		{
			NBTTagCompound nbt = NBTFileLoader.loadData(this.getSaveDirectory(), NBT_FILE_SAVE_PREFIX + getModeID(itemStack));

			if (nbt != null)
			{
				NBTTagList nbtTagList = nbt.getTagList(NBT_FIELD_BLOCK_LIST);

				for (int i = 0; i < nbtTagList.tagCount(); i++)
				{
					NBTTagCompound vectorTag = (NBTTagCompound) nbtTagList.tagAt(i);
					Vector3 position = Vector3.readFromNBT(vectorTag);

					if (scale > 0)
					{
						position.scale(scale);
					}

					int[] blockInfo = new int[] { vectorTag.getInteger(NBT_FIELD_BLOCK_ID), vectorTag.getInteger(NBT_FIELD_BLOCK_METADATA) };

					if (position != null)
					{
						fieldBlocks.put(position, blockInfo);
					}
				}
			}
		}

		return fieldBlocks;
	}

	@Override
	public Object getCache(String cacheID)
	{
		return this.cache.get(cacheID);
	}

	@Override
	public void clearCache(String cacheID)
	{
		this.cache.remove(cacheID);
	}

	@Override
	public void clearCache()
	{
		this.cache.clear();
	}

	@Override
	public Set<Vector3> getExteriorPoints(IFieldInteraction projector)
	{
		return this.getFieldBlocks(projector, projector.getModeStack());
	}

	@Override
	public Set<Vector3> getInteriorPoints(IFieldInteraction projector)
	{
		return this.getExteriorPoints(projector);
	}

	@Override
	public boolean isInField(IFieldInteraction projector, Vector3 position)
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void render(IProjector projector, double x, double y, double z, float f, long ticks)
	{
		// Render random possible projections
		IProjectorMode[] modes = new IProjectorMode[] { ModularForceFieldSystem.itemModeCube, ModularForceFieldSystem.itemModeSphere, ModularForceFieldSystem.itemModeTube, ModularForceFieldSystem.itemModePyramid };
		modes[((TileEntity) projector).worldObj.rand.nextInt(modes.length - 1)].render(projector, x, y, z, f, ticks);
	}
}
