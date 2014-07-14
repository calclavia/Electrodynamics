package resonantinduction.archaic.filter;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonant.lib.prefab.block.BlockTile;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.imprint.ItemImprint;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.vector.Vector2;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;
import codechicken.multipart.ControlKeyModifer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockImprinter extends BlockTile
{
	Icon imprinter_side;
	Icon imprinter_top;
	Icon imprinter_bottom;

	public BlockImprinter(int id)
	{
		super(id, UniversalElectricity.machine);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconReg)
	{
		this.imprinter_side = iconReg.registerIcon(Reference.PREFIX + "imprinter_side");
		this.imprinter_top = iconReg.registerIcon(Reference.PREFIX + "imprinter_top");
		this.imprinter_bottom = iconReg.registerIcon(Reference.PREFIX + "imprinter_bottom");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
	{
		return getIcon(side, 0);
	}

	/** Returns the block texture based on the side being looked at. Args: side */
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side == 1)
		{
			return this.imprinter_top;

		}
		else if (side == 0)
		{
			return this.imprinter_bottom;

		}

		return this.imprinter_side;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);

		if (te instanceof TileImprinter)
		{
			TileImprinter tile = (TileImprinter) te;
			int idOnTop = ((VectorWorld) new VectorWorld(world, x, y, z).translate(ForgeDirection.getOrientation(1))).getBlockID();

			if (Block.pistonMoving.blockID == blockID)
			{
				tile.onInventoryChanged();
			}
		}
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int hitSide, float hitX, float hitY, float hitZ)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);

		if (te instanceof TileImprinter)
		{
			TileImprinter tile = (TileImprinter) te;
			ItemStack current = player.inventory.getCurrentItem();

			if (hitSide == 1)
			{
				if (!world.isRemote)
				{
					Vector2 hitVector = new Vector2(hitX, hitZ);
					double regionLength = 1d / 3d;

					/**
					 * Crafting Matrix
					 */
					matrix:
					for (int j = 0; j < 3; j++)
					{
						for (int k = 0; k < 3; k++)
						{
							Vector2 check = new Vector2(j, k).scale(regionLength);

							if (check.distance(hitVector) < regionLength)
							{
								int slotID = j * 3 + k;
								boolean didInsert = false;
								ItemStack checkStack = tile.inventory[slotID];

								if (current != null)
								{
									if (checkStack == null || checkStack.isItemEqual(current))
									{
										if (ControlKeyModifer.isControlDown(player))
										{
											if (checkStack == null)
											{
												tile.inventory[slotID] = current;
											}
											else
											{
												tile.inventory[slotID].stackSize += current.stackSize;
												current.stackSize = 0;
											}

											current = null;
										}
										else
										{
											if (checkStack == null)
											{
												tile.inventory[slotID] = current.splitStack(1);
											}
											else
											{
												tile.inventory[slotID].stackSize++;
												current.stackSize--;
											}
										}

										if (current == null || current.stackSize <= 0)
										{
											player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
										}

										didInsert = true;
									}
								}

								if (!didInsert && checkStack != null)
								{
									InventoryUtility.dropItemStack(world, new Vector3(player), checkStack, 0);
									tile.inventory[slotID] = null;
								}

								break matrix;
							}
						}
					}

					world.markBlockForUpdate(x, y, z);
				}

				return true;
			}
			else if (hitSide != 0)
			{

				ItemStack output = tile.getStackInSlot(9);

				if (output != null)
				{
					InventoryUtility.dropItemStack(world, new Vector3(player), output, 0);
					tile.setInventorySlotContents(9, null);
				}
				else if (current != null && current.getItem() instanceof ItemImprint)
				{
					tile.setInventorySlotContents(9, current);
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				}
			}
		}

		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileImprinter();
	}
}
