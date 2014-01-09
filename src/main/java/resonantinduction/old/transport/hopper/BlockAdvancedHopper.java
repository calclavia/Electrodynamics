package resonantinduction.old.transport.hopper;

import java.util.List;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Facing;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import resonantinduction.core.Settings;
import resonantinduction.old.client.render.RenderAdvancedHopper;
import resonantinduction.old.lib.prefab.BlockMachine;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block for an advanced version of the vanilla minecraft hopper
 * 
 * @author DarkGuardsman
 */
public class BlockAdvancedHopper extends BlockMachine
{
	@SideOnly(Side.CLIENT)
	public static Icon hopperIcon;
	@SideOnly(Side.CLIENT)
	public static Icon hopperTopIcon;
	@SideOnly(Side.CLIENT)
	public static Icon hopperInsideIcon;

	public BlockAdvancedHopper()
	{
		super(Settings.CONFIGURATION, "DMHopper", Material.iron);
		this.setCreativeTab(CreativeTabs.tabRedstone);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	/**
	 * Adds all intersecting collision boxes to a list. (Be sure to only add boxes to the list if
	 * they intersect the mask.) Parameters: World, X, Y, Z, mask, list, colliding entity
	 */
	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB box, List boxList, Entity entity)
	{
		float f = 0.125F;

		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.625F, 1.0F);

		super.addCollisionBoxesToList(world, x, y, z, box, boxList, entity);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);

		super.addCollisionBoxesToList(world, x, y, z, box, boxList, entity);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);

		super.addCollisionBoxesToList(world, x, y, z, box, boxList, entity);
		this.setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		super.addCollisionBoxesToList(world, x, y, z, box, boxList, entity);
		this.setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);

		super.addCollisionBoxesToList(world, x, y, z, box, boxList, entity);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float xSide, float ySide, float zSide, int meta)
	{
		meta = Facing.oppositeSide[side];
		if (meta == 1)
		{
			meta = 0;
		}
		return meta;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityAdvancedHopper();
	}

	@Override
	public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
	{
		list.add(new Pair<String, Class<? extends TileEntity>>("DMTileHopper", TileEntityAdvancedHopper.class));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
	{
		list.add(new Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>(TileEntityAdvancedHopper.class, new RenderAdvancedHopper()));
	}

	/** The type of render function that is called for this block */
	@Override
	public int getRenderType()
	{
		return -1;
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
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side)
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		return side == 1 ? BlockAdvancedHopper.hopperTopIcon : BlockAdvancedHopper.hopperIcon;
	}

	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		BlockAdvancedHopper.hopperIcon = par1IconRegister.registerIcon("hopper_outside");
		BlockAdvancedHopper.hopperTopIcon = par1IconRegister.registerIcon("hopper_top");
		BlockAdvancedHopper.hopperInsideIcon = par1IconRegister.registerIcon("hopper_inside");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemIconName()
	{
		return "hopper";
	}
}
