package resonantinduction.mechanical.process.crusher;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.render.RIBlockRenderingHandler;
import calclavia.lib.prefab.block.BlockRotatable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A block used to build machines.
 * 
 * @author Calclavia
 * 
 */
public class BlockMechanicalPiston extends BlockRotatable
{
	public BlockMechanicalPiston(int id)
	{
		super(id, Material.wood);
		setTextureName(Reference.PREFIX + "material_steel_dark");
		rotationMask = Byte.parseByte("111111", 2);
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

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileMechanicalPiston();
	}
}
