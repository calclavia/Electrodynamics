package resonantinduction.mechanical.energy.turbine;

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

public class BlockWaterTurbine extends BlockMechanicalTurbine
{
	public BlockWaterTurbine(int id)
	{
		super(id);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return RIBlockRenderingHandler.ID;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileWaterTurbine();
	}
}
