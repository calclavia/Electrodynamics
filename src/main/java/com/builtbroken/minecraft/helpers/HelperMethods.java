package com.builtbroken.minecraft.helpers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;

/**
 * Class full of generic re-usable methods
 * 
 * @author DarkGuardsman
 */
public class HelperMethods
{
	/**
	 * Used to find all tileEntities sounding the location you will have to filter for selective
	 * tileEntities
	 * 
	 * @param world - the world being searched threw
	 * @param x
	 * @param y
	 * @param z
	 * @return an array of up to 6 tileEntities
	 */
	public static TileEntity[] getSurroundingTileEntities(TileEntity ent)
	{
		return getSurroundingTileEntities(ent.worldObj, ent.xCoord, ent.yCoord, ent.zCoord);
	}

	public static TileEntity[] getSurroundingTileEntities(World world, Vector3 vec)
	{
		return getSurroundingTileEntities(world, vec.intX(), vec.intY(), vec.intZ());
	}

	public static TileEntity[] getSurroundingTileEntities(World world, int x, int y, int z)
	{
		TileEntity[] list = new TileEntity[6];
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			list[direction.ordinal()] = world.getBlockTileEntity(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
		}
		return list;
	}

	/**
	 * Used to find which of 4 Corners this block is in a group of blocks 0 = not a corner 1-4 = a
	 * corner of some direction
	 */
	public static int corner(TileEntity entity)
	{
		TileEntity[] en = getSurroundingTileEntities(entity.worldObj, entity.xCoord, entity.yCoord, entity.zCoord);
		TileEntity north = en[ForgeDirection.NORTH.ordinal()];
		TileEntity south = en[ForgeDirection.SOUTH.ordinal()];
		TileEntity east = en[ForgeDirection.EAST.ordinal()];
		TileEntity west = en[ForgeDirection.WEST.ordinal()];

		if (west != null && north != null && east == null && south == null)
		{
			return 3;
		}
		if (north != null && east != null && south == null && west == null)
		{
			return 4;
		}
		if (east != null && south != null && west == null && north == null)
		{
			return 1;
		}
		if (south != null && west != null && north == null && east == null)
		{
			return 2;
		}

		return 0;
	}

	/** gets all EntityItems in a location using a start and end point */
	public static List<EntityItem> findAllItemsIn(World world, Vector3 start, Vector3 end)
	{
		return world.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(start.x, start.y, start.z, end.x, end.y, end.z));
	}

	public static List<EntityItem> getEntitiesInDirection(World world, Vector3 center, ForgeDirection dir)
	{
		List<EntityItem> list = world.selectEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getAABBPool().getAABB(center.x + dir.offsetX, center.y + dir.offsetY, center.z + dir.offsetZ, center.x + dir.offsetX + 1, center.y + dir.offsetY + 1, center.z + dir.offsetZ + 1), IEntitySelector.selectAnything);
		return list.size() > 0 ? list : null;
	}

	/**
	 * Gets all EntityItems in an area and sorts them by a list of itemStacks
	 * 
	 * @param world - world being worked in
	 * @param start - start point
	 * @param end - end point
	 * @param disiredItems - list of item that are being looked for
	 * @return a list of EntityItem that match the itemStacks desired
	 */
	public static List<EntityItem> findSelectItems(World world, Vector3 start, Vector3 end, List<ItemStack> disiredItems)
	{
		List<EntityItem> entityItems = findAllItemsIn(world, start, end);
		return filterEntityItemsList(entityItems, disiredItems);
	}

	/** filters an EntityItem List to a List of Items */
	public static List<EntityItem> filterEntityItemsList(List<EntityItem> entityItems, List<ItemStack> disiredItems)
	{
		List<EntityItem> newItemList = new ArrayList<EntityItem>();
		for (ItemStack itemStack : disiredItems)
		{
			for (EntityItem entityItem : entityItems)
			{
				if (entityItem.getEntityItem().isItemEqual(itemStack) && !newItemList.contains(entityItem))
				{
					newItemList.add(entityItem);
					break;
				}
			}
		}
		return newItemList;
	}

	/** filters out EnittyItems from an Entity list */
	public static List<EntityItem> filterOutEntityItems(List<Entity> entities)
	{
		List<EntityItem> newEntityList = new ArrayList<EntityItem>();

		for (Entity entity : entities)
		{
			if (entity instanceof EntityItem)
			{
				newEntityList.add((EntityItem) entity);
			}

		}
		return newEntityList;
	}

	/**
	 * filter a list of itemStack to another list of itemStacks
	 * 
	 * @param totalItems - full list of items being filtered
	 * @param desiredItems - list the of item that are being filtered too
	 * @return a list of item from the original that are wanted
	 */
	public static List<ItemStack> filterItems(List<ItemStack> totalItems, List<ItemStack> desiredItems)
	{
		List<ItemStack> newItemList = new ArrayList<ItemStack>();

		for (ItemStack entityItem : totalItems)
		{
			for (ItemStack itemStack : desiredItems)
			{
				if (entityItem.itemID == itemStack.itemID && entityItem.getItemDamage() == itemStack.getItemDamage() && !newItemList.contains(entityItem))
				{
					newItemList.add(entityItem);
					break;
				}
			}
		}
		return newItemList;
	}

	/**
	 * grabs all the items that the block can drop then pass them onto dropBlockAsItem_do
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void dropBlockAsItem(World world, Vector3 loc)
	{
		if (!world.isRemote)
		{
			int meta = loc.getBlockMetadata(world);
			int id = loc.getBlockID(world);
			ArrayList<ItemStack> items = Block.blocksList[id].getBlockDropped(world, loc.intX(), loc.intY(), loc.intZ(), meta, 0);

			for (ItemStack item : items)
			{
				dropItemStack(world, loc, item, false);
			}
		}
	}

	public static ItemStack dropItemStack(World world, Vector3 location, ItemStack itemStack, boolean random)
	{
		if (!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
		{
			float f = 0.7F;
			double xx = 0;
			double yy = 0;
			double zz = 0;
			if (random)
			{
				xx = (world.rand.nextFloat() * f) + (1.0F - f) * 0.5D;
				yy = (world.rand.nextFloat() * f) + (1.0F - f) * 0.5D;
				zz = (world.rand.nextFloat() * f) + (1.0F - f) * 0.5D;
			}
			EntityItem entityitem = new EntityItem(world, location.x + xx, location.y + yy, location.z + zz, itemStack);
			entityitem.delayBeforeCanPickup = 10;
			world.spawnEntityInWorld(entityitem);
			return null;
		}
		return itemStack;
	}
}
