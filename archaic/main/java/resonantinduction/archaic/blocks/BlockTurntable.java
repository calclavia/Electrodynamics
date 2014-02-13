package resonantinduction.archaic.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.Reference;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.block.BlockRotatable;
import calclavia.lib.prefab.block.IRotatableBlock;
import calclavia.lib.prefab.tile.IRotatable;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTurntable extends BlockRotatable
{
	private Icon top;

	public BlockTurntable(int id)
	{
		super(id, Material.piston);
		setTextureName(Reference.PREFIX + "turntable_side");
		setTickRandomly(true);
		rotationMask = Byte.parseByte("111111", 2);
	}

	@Override
	public int tickRate(World par1World)
	{
		return 5;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconReg)
	{
		super.registerIcons(iconReg);
		this.top = iconReg.registerIcon(Reference.PREFIX + "turntable");
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random par5Random)
	{
		this.updateTurntableState(world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int side)
	{
		int meta = par1IBlockAccess.getBlockMetadata(par2, par3, par4);

		if (side == meta)
		{
			return this.top;
		}

		return this.blockIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if (side == 1)
		{
			return this.top;
		}

		return this.blockIcon;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int side)
	{
		world.scheduleBlockUpdate(x, y, z, this.blockID, 10);
	}

	private void updateTurntableState(World world, int x, int y, int z)
	{
		if (world.isBlockIndirectlyGettingPowered(x, y, z))
		{
			try
			{
				ForgeDirection facing = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));

				Vector3 position = new Vector3(x, y, z).translate(facing);
				TileEntity tileEntity = position.getTileEntity(world);
				Block block = Block.blocksList[position.getBlockID(world)];

				if (!(tileEntity instanceof TileMultipart))
				{
					if (tileEntity instanceof IRotatable)
					{
						ForgeDirection blockRotation = ((IRotatable) tileEntity).getDirection();
						((IRotatable) tileEntity).setDirection(blockRotation.getRotation(facing.getOpposite()));
					}
					else if (block instanceof IRotatableBlock)
					{
						ForgeDirection blockRotation = ((IRotatableBlock) block).getDirection(world, position.intX(), position.intY(), position.intZ());
						((IRotatableBlock) block).setDirection(world, position.intX(), position.intY(), position.intZ(), blockRotation.getRotation(facing.getOpposite()));
					}
					else if (block != null)
					{
						Block.blocksList[blockID].rotateBlock(world, position.intX(), position.intY(), position.intZ(), facing.getOpposite());
					}

					world.markBlockForUpdate(position.intX(), position.intY(), position.intZ());
					world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "tile.piston.in", 0.5F, world.rand.nextFloat() * 0.15F + 0.6F);
				}
			}
			catch (Exception e)
			{
				System.out.println("Error while rotating a block near " + x + "x " + y + "y " + z + "z " + (world != null && world.provider != null ? world.provider.dimensionId + "d" : "null:world"));
				e.printStackTrace();
			}
		}
	}

}
