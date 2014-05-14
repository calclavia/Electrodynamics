package resonantinduction.archaic.process;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonant.lib.prefab.block.BlockTile;
import resonant.lib.render.block.BlockRenderingHandler;
import resonant.lib.utility.inventory.InventoryUtility;
import resonantinduction.core.Reference;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCastingMold extends BlockTile
{
	public BlockCastingMold(int id)
	{
		super(id, Material.iron);
		setTextureName(Reference.PREFIX + "material_metal_side");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.ID;
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

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile != null)
			tile.updateEntity();
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

			if (te instanceof TileCastingMold)
			{
				TileCastingMold tile = (TileCastingMold) te;

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

		if (te instanceof TileCastingMold)
		{
			TileCastingMold tile = (TileCastingMold) te;
			tile.updateEntity();

			ItemStack current = player.inventory.getCurrentItem();

			ItemStack output = tile.getStackInSlot(0);

			if (output != null)
			{
				InventoryUtility.dropItemStack(world, new Vector3(player), output, 0);
				tile.setInventorySlotContents(0, null);
			}

			return true;
		}

		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileCastingMold();
	}
}
