package resonantinduction.core.resource;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.resource.item.ItemOreResource;
import calclavia.lib.prefab.block.BlockTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The block form of the item dust.
 * 
 * @author Calclavia
 * 
 */
public class BlockDust extends BlockTile
{
	public BlockDust(int id)
	{
		super(id, Material.sand);
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
		setBlockBoundsForDepth(0);
		setHardness(0.5f);
		setTextureName(Reference.PREFIX + "material_sand");
		setStepSound(soundGravelFootstep);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase par5EntityLivingBase, ItemStack itemStack)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileMaterial)
		{
			((TileMaterial) tileEntity).name = ItemOreResource.getMaterialFromStack(itemStack);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int colorMultiplier(IBlockAccess access, int x, int y, int z)
	{
		TileEntity tileEntity = access.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileMaterial)
		{
			return ((TileMaterial) tileEntity).getColor();
		}

		return 16777215;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		tryToFall(world, x, y, z);
	}

	@Override
	public void onNeighborBlockChange(World world, int y, int x, int z, int id)
	{
		tryToFall(world, y, x, z);
	}

	private void tryToFall(World world, int x, int y, int z)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile instanceof TileMaterial)
		{
			String materialName = ((TileMaterial) tile).name;
			int metadata = world.getBlockMetadata(x, y, z);

			if (canFallBelow(world, x, y - 1, z) && y >= 0)
			{
				byte b0 = 32;

				world.setBlockToAir(x, y, z);

				while (canFallBelow(world, x, y - 1, z) && y > 0)
				{
					--y;
				}

				if (y > 0)
				{
					world.setBlock(x, y, z, this.blockID, metadata, 3);

					TileEntity newTile = world.getBlockTileEntity(x, y, z);

					if (newTile instanceof TileMaterial)
					{
						((TileMaterial) newTile).name = materialName;
					}
				}
			}
		}

	}

	public static boolean canFallBelow(World par0World, int par1, int par2, int par3)
	{
		int l = par0World.getBlockId(par1, par2, par3);

		if (par0World.isAirBlock(par1, par2, par3))
		{
			return true;
		}
		else if (l == Block.fire.blockID)
		{
			return true;
		}
		else
		{
			Material material = Block.blocksList[l].blockMaterial;
			return material == Material.water ? true : material == Material.lava;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileMaterial();
	}

	/**
	 * Returns a bounding box from the pool of bounding boxes (this means this box can change after
	 * the pool has been
	 * cleared to be reused)
	 */
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		int l = par1World.getBlockMetadata(par2, par3, par4) & 7;
		float f = 0.125F;
		return AxisAlignedBB.getAABBPool().getAABB(par2 + this.minX, par3 + this.minY, par4 + this.minZ, par2 + this.maxX, par3 + l * f, par4 + this.maxZ);
	}

	/**
	 * Is this block (a) opaque and (b) a full 1m cube? This determines whether or not to render the
	 * shared face of two
	 * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this
	 * block.
	 */
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	/**
	 * If this block doesn't render as an ordinary block it will return False (examples: signs,
	 * buttons, stairs, etc)
	 */
	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	/**
	 * Sets the block's bounds for rendering it as an item
	 */
	@Override
	public void setBlockBoundsForItemRender()
	{
		this.setBlockBoundsForDepth(0);
	}

	/**
	 * Updates the blocks bounds based on its current state. Args: world, x, y, z
	 */
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
	{
		this.setBlockBoundsForDepth(par1IBlockAccess.getBlockMetadata(par2, par3, par4));
	}

	/**
	 * calls setBlockBounds based on the depth of the snow. Int is any values 0x0-0x7, usually this
	 * blocks metadata.
	 */
	protected void setBlockBoundsForDepth(int par1)
	{
		int j = par1 & 7;
		float f = 2 * (1 + j) / 16.0F;
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, f, 1.0F);
	}

	/**
	 * Returns the ID of the items to drop on destruction.
	 */
	@Override
	public int idDropped(int par1, Random par2Random, int par3)
	{
		return ResonantInduction.itemRefinedDust.itemID;
	}

	@Override
	public int idPicked(World par1World, int par2, int par3, int par4)
	{
		return ResonantInduction.itemRefinedDust.itemID;
	}

	@Override
	public int damageDropped(int par1)
	{
		return par1;
	}

	@Override
	public int getDamageValue(World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileMaterial)
		{
			return ResourceGenerator.getID(((TileMaterial) tileEntity).name);
		}
		return 0;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
	{
		return par5 == 1 ? true : super.shouldSideBeRendered(par1IBlockAccess, par2, par3, par4, par5);
	}

	@Override
	public int quantityDropped(int meta, int fortune, Random random)
	{
		return (meta & 7) + 1;
	}
}
