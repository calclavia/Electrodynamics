package resonantinduction.archaic.firebox;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.block.BlockRI;
import universalelectricity.api.vector.Vector2;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockHotPlate extends BlockRI
{
	private Icon topElectric;

	public BlockHotPlate()
	{
		super("hotPlate", Material.wood);
		setBlockBounds(0, 0, 0, 1, 0.2f, 1);
		this.setTickRandomly(true);
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconReg)
	{
		super.registerIcons(iconReg);
		topElectric = iconReg.registerIcon(Reference.PREFIX + "material_steel");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		return meta == 1 ? topElectric : blockIcon;
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileHotPlate)
		{
			TileHotPlate tile = (TileHotPlate) tileEntity;
			extractItem(tile, 0, player);
		}
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileHotPlate)
		{
			TileHotPlate tile = (TileHotPlate) tileEntity;

			if (!world.isRemote)
			{
				Vector2 hitVector = new Vector2(hitX, hitZ);
				final double regionLength = 1d / 2d;

				/**
				 * Crafting Matrix
				 */
				matrix:
				for (int j = 0; j < 2; j++)
				{
					for (int k = 0; k < 2; k++)
					{
						Vector2 check = new Vector2(j, k).scale(regionLength);

						if (check.distance(hitVector) < regionLength)
						{
							int slotID = j * 2 + k;
							interactCurrentItem(tile, slotID, player);
							break matrix;
						}
					}
				}

				tile.onInventoryChanged();
			}

			return true;
		}

		return false;
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileHotPlate)
		{
			TileHotPlate tile = (TileHotPlate) tileEntity;

			for (int j = 0; j < 2; j++)
			{
				for (int k = 0; k < 2; k++)
				{
					int i = j * 2 + k;

					if (tile.getStackInSlot(i) != null && tile.getSmeltTime(i) > 0)
					{
						int maxSmelt = TileHotPlate.MAX_SMELT_TIME * tile.getStackInSlot(i).stackSize;
						int maxParticles = (int) (((double) (maxSmelt - tile.getSmeltTime(i)) / (double) maxSmelt) * 30);

						for (int spawn = 0; spawn < maxParticles; spawn++)
						{
							Vector3 particlePosition = new Vector3(x, y, z).translate((double) (i / 2) / ((double) 2) + (0.5 / (2)), 0.2, (double) (i % 2) / ((double) 2) + (0.5 / (2)));
							particlePosition.translate(new Vector3((random.nextFloat() - 0.5) * 0.2, (random.nextFloat() - 0.5) * 0.2, (random.nextFloat() - 0.5) * 0.2));
							world.spawnParticle("smoke", particlePosition.x, particlePosition.y, particlePosition.z, 0.0D, 0.0D, 0.0D);
						}

						Vector3 particlePosition = new Vector3(x, y, z).translate((double) (i / 2) / ((double) 2) + (0.5 / (2)), 0.2, (double) (i % 2) / ((double) 2) + (0.5 / (2)));
						world.spawnParticle("flame", particlePosition.x, particlePosition.y, particlePosition.z, 0.0D, 0.01D, 0.0D);
					}
				}
			}
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity par5Entity)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileHotPlate)
		{
			TileHotPlate tile = (TileHotPlate) tileEntity;

			if (tile.isSmelting())
			{
				par5Entity.attackEntityFrom(DamageSource.inFire, 1);
			}
		}
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		par3List.add(new ItemStack(par1, 1, 0));
		par3List.add(new ItemStack(par1, 1, 1));
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileHotPlate();
	}
}
