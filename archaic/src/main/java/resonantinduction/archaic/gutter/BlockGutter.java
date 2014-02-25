package resonantinduction.archaic.gutter;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.fluid.BlockFluidNetwork;
import resonantinduction.core.render.RIBlockRenderingHandler;
import universalelectricity.api.UniversalElectricity;
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
	public void addCollisionBoxesToList(World par1World, int x, int y, int z, AxisAlignedBB par5AxisAlignedBB, List par6List, Entity entity)
	{
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3125F, 1.0F);
		super.addCollisionBoxesToList(par1World, x, y, z, par5AxisAlignedBB, par6List, entity);
		float thickness = 0.125F;
		this.setBlockBounds(0.0F, 0.0F, 0.0F, thickness, 1.0F, 1.0F);
		super.addCollisionBoxesToList(par1World, x, y, z, par5AxisAlignedBB, par6List, entity);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, thickness);
		super.addCollisionBoxesToList(par1World, x, y, z, par5AxisAlignedBB, par6List, entity);
		this.setBlockBounds(1.0F - thickness, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		super.addCollisionBoxesToList(par1World, x, y, z, par5AxisAlignedBB, par6List, entity);
		this.setBlockBounds(0.0F, 0.0F, 1.0F - thickness, 1.0F, 1.0F, 1.0F);
		super.addCollisionBoxesToList(par1World, x, y, z, par5AxisAlignedBB, par6List, entity);
		this.setBlockBoundsForItemRender();
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
			((TileGutter) tile).fill(ForgeDirection.UNKNOWN, new FluidStack(FluidRegistry.WATER, 10), true);
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World par1World, int x, int y, int z, Entity entity)
	{
		entity.attackEntityFrom(DamageSource.cactus, 1.0F);
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
