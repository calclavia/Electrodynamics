package mffs.item.module.projector;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mffs.MFFSHelper;
import mffs.ModularForceFieldSystem;
import mffs.Settings;
import mffs.api.ICache;
import mffs.api.IProjector;
import mffs.api.modules.IProjectorMode;
import mffs.item.mode.ItemMode;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.flag.NBTFileLoader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemModeCustom extends ItemMode implements ICache
{
	private static final String NBT_ID = "id";
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

		Vector3 point1 = Vector3.readFromNBT(nbt.getCompoundTag(NBT_POINT_1));
		list.add("Point 1: " + point1.intX() + ", " + point1.intY() + ", " + point1.intZ());

		Vector3 point2 = Vector3.readFromNBT(nbt.getCompoundTag(NBT_POINT_2));
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
					Vector3 point1 = Vector3.readFromNBT(nbt.getCompoundTag(NBT_POINT_1));
					Vector3 point2 = Vector3.readFromNBT(nbt.getCompoundTag(NBT_POINT_2));

					if (nbt.hasKey(NBT_POINT_1) && nbt.hasKey(NBT_POINT_2) && !point1.equals(point2))
					{
						if (point1.distanceTo(point2) < Settings.MAX_FORCE_FIELD_SCALE)
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

							NBTTagList list = new NBTTagList();

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
											NBTTagCompound vectorTag = new NBTTagCompound();
											position.writeToNBT(vectorTag);
											vectorTag.setInteger(NBT_FIELD_BLOCK_ID, blockID);
											vectorTag.setInteger(NBT_FIELD_BLOCK_METADATA, targetCheck.getBlockMetadata(world));
											list.appendTag(vectorTag);
										}
									}
								}
							}

							NBTTagCompound saveNBT = new NBTTagCompound();
							saveNBT.setTag(NBT_FIELD_BLOCK_LIST, list);

							nbt.setInteger(NBT_FIELD_SIZE, list.tagCount());

							NBTFileLoader.saveData(getSaveDirectory(), NBT_FILE_SAVE_PREFIX + getModeID(itemStack), saveNBT);

							this.clearCache();

							entityPlayer.addChatMessage("Field structure saved.");
						}
					}
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
				Vector3 point1 = Vector3.readFromNBT(nbt.getCompoundTag(NBT_POINT_1));

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

	public Set<Vector3> getFieldBlocks(ItemStack itemStack)
	{
		return this.getFieldBlockMap(itemStack).keySet();
	}

	@SuppressWarnings("unchecked")
	public HashMap<Vector3, int[]> getFieldBlockMap(ItemStack itemStack)
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

		final HashMap<Vector3, int[]> fieldBlocks = new HashMap<Vector3, int[]>();

		NBTTagCompound nbt = NBTFileLoader.loadData(this.getSaveDirectory(), NBT_FILE_SAVE_PREFIX + getModeID(itemStack));

		if (nbt != null)
		{
			NBTTagList nbtTagList = nbt.getTagList(NBT_FIELD_BLOCK_LIST);

			for (int i = 0; i < nbtTagList.tagCount(); i++)
			{
				NBTTagCompound vectorTag = (NBTTagCompound) nbtTagList.tagAt(i);
				Vector3 position = Vector3.readFromNBT(vectorTag);
				int[] blockInfo = new int[] { vectorTag.getInteger(NBT_FIELD_BLOCK_ID), vectorTag.getInteger(NBT_FIELD_BLOCK_METADATA) };

				if (position != null)
				{
					fieldBlocks.put(position, blockInfo);
				}
			}
		}

		if (Settings.USE_CACHE)
		{
			this.cache.put(cacheID, fieldBlocks);
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
	public Set<Vector3> getExteriorPoints(IProjector projector)
	{
		return this.getFieldBlocks(projector.getModeStack());
	}

	@Override
	public Set<Vector3> getInteriorPoints(IProjector projector)
	{
		return new HashSet<Vector3>();
	}

	@Override
	public boolean isInField(IProjector projector, Vector3 position)
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
