package resonantinduction.archaic.gutter;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.fluid.BlockFluidNetwork;
import resonantinduction.core.render.RIBlockRenderingHandler;
import universalelectricity.api.UniversalElectricity;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.render.RenderUtility;
import calclavia.lib.utility.FluidUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Early tier version of the basic pipe. Open on the top, and can't support pressure.
 * 
 * @author Darkguardsman
 */
public class BlockGutter extends BlockFluidNetwork
{
	public BlockGutter(int id)
	{
		super(id, UniversalElectricity.machine);
		setTextureName(Reference.PREFIX + "material_wood_surface");
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity entity)
	{
		float thickness = 0.1F;

		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile instanceof TileGutter)
		{
			byte renderSides = ((TileGutter) tile).renderSides;

			if (!RenderUtility.canRenderSide(renderSides, ForgeDirection.DOWN))
			{
				this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, thickness, 1.0F);
				super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
			}

			if (!RenderUtility.canRenderSide(renderSides, ForgeDirection.WEST))
			{
				this.setBlockBounds(0.0F, 0.0F, 0.0F, thickness, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
			}
			if (!RenderUtility.canRenderSide(renderSides, ForgeDirection.NORTH))
			{
				this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, thickness);
				super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
			}
			if (!RenderUtility.canRenderSide(renderSides, ForgeDirection.EAST))
			{
				this.setBlockBounds(1.0F - thickness, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
			}
			if (!RenderUtility.canRenderSide(renderSides, ForgeDirection.SOUTH))
			{
				this.setBlockBounds(0.0F, 0.0F, 1.0F - thickness, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
			}
		}

		setBlockBounds(0.0F, 0.0F, 0.0F, 1, 0.99f, 1);
	}

	public void setBlockBoundsForItemRender()
	{
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public void fillWithRain(World world, int x, int y, int z)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (!world.isRemote && tile instanceof TileGutter)
		{
			((TileGutter) tile).fill(ForgeDirection.UNKNOWN, new FluidStack(FluidRegistry.WATER, 1), true);
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (tile instanceof TileGutter)
		{
			if (((TileGutter) tile).getInternalTank().getFluidAmount() > 0)
			{
				int pressure = ((TileGutter) tile).getPressure(null);

				for (int i = 2; i < 6; i++)
				{
					ForgeDirection dir = ForgeDirection.getOrientation(i);
					Vector3 position = new Vector3(x, y, z).translate(dir);

					TileEntity checkTile = position.getTileEntity(world);

					if (checkTile instanceof TileGutter)
					{
						int deltaPressure = ((TileGutter) checkTile).getPressure(null) - pressure;

						entity.motionX += 0.01 * dir.offsetX * deltaPressure;
						entity.motionY += 0.01 * dir.offsetY * deltaPressure;
						entity.motionZ += 0.01 * dir.offsetZ * deltaPressure;
					}
				}
			}
		}

		if (entity instanceof EntityItem)
		{
			entity.noClip = true;
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (!world.isRemote && tile instanceof TileGutter)
		{
			return FluidUtility.playerActivatedFluidItem(world, x, y, z, entityplayer, side);
		}

		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileGutter();
	}

	@Override
	@SideOnly(Side.CLIENT)
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

}
