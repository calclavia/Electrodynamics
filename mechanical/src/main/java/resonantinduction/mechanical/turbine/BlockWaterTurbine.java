package resonantinduction.mechanical.turbine;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.render.RIBlockRenderingHandler;
import calclavia.lib.prefab.turbine.BlockTurbine;
import calclavia.lib.prefab.turbine.TileTurbine;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockWaterTurbine extends BlockTurbine
{
	public BlockWaterTurbine(int id)
	{
		super(id, Material.iron);
		setTextureName(Reference.PREFIX + "material_wood_surface");
		rotationMask = Byte.parseByte("111111", 2);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return RIBlockRenderingHandler.ID;
	}

	@Override
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileTurbine)
			if (!world.isRemote && !((TileTurbine) tileEntity).getMultiBlock().isConstructed())
				world.setBlockMetadataWithNotify(x, y, z, side, 3);

		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileWaterTurbine();
	}
}
