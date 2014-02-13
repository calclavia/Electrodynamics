package resonantinduction.archaic.process;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.block.BlockTile;
import calclavia.lib.utility.inventory.InventoryUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCast extends BlockTile
{
	Icon top;

	public BlockCast(int id)
	{
		super(id, Material.iron);
		setTextureName(Reference.PREFIX + "material_metal_side");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconReg)
	{
		this.top = iconReg.registerIcon(Reference.PREFIX + "material_wood_top");
		super.registerIcons(iconReg);
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
			return top;
		}

		return blockIcon;
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
