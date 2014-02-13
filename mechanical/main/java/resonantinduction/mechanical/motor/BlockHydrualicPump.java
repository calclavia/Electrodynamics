package resonantinduction.mechanical.motor;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.render.RIBlockRenderingHandler;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.prefab.block.BlockRotatable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockHydrualicPump extends BlockRotatable
{
	public BlockHydrualicPump(int id)
	{
		super(id, UniversalElectricity.machine);
		setTextureName(Reference.PREFIX + "material_steel");
		rotationMask = Byte.parseByte("111111", 2);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileFluidMotor();
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
	public int getRenderType()
	{
		return RIBlockRenderingHandler.ID;
	}

}
