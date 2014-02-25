package resonantinduction.archaic.process;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.render.RIBlockRenderingHandler;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.block.BlockTile;
import calclavia.lib.utility.inventory.InventoryUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCast extends BlockTile
{
	Icon topIcon;
	Icon side1Icon;
	Icon side2Icon;

	public BlockCast(int id)
	{
		super(id, Material.iron);
		setTextureName(Reference.PREFIX + "material_metal_side");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return RIBlockRenderingHandler.ID;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconReg)
	{
		topIcon = iconReg.registerIcon(Reference.PREFIX + "metalCast_top");
		side1Icon = iconReg.registerIcon(Reference.PREFIX + "metalCast_side_1");
		side2Icon = iconReg.registerIcon(Reference.PREFIX + "metalCast_side_2");
		super.registerIcons(iconReg);
	}

	/** Returns the block texture based on the side being looked at. Args: side */
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side == 0)
			return blockIcon;

		if (side == 1)
			return topIcon;

		return side % 2 == 0 ? side1Icon : side2Icon;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile != null)
			tile.updateEntity();
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
	{
		if (!world.isRemote)
		{
			TileEntity te = world.getBlockTileEntity(x, y, z);

			if (te instanceof TileCast)
			{
				TileCast tile = (TileCast) te;

				ItemStack output = tile.getStackInSlot(0);

				if (output != null)
				{
					InventoryUtility.dropItemStack(world, new Vector3(player), output, 0);
					tile.setInventorySlotContents(0, null);
				}

				tile.onInventoryChanged();
			}

		}
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int hitSide, float hitX, float hitY, float hitZ)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);

		if (te instanceof TileCast)
		{
			TileCast tile = (TileCast) te;
			tile.updateEntity();

			ItemStack current = player.inventory.getCurrentItem();

			ItemStack output = tile.getStackInSlot(0);

			if (output != null)
			{
				InventoryUtility.dropItemStack(world, new Vector3(player), output, 0);
				tile.setInventorySlotContents(0, null);
			}
		}

		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileCast();
	}
}
