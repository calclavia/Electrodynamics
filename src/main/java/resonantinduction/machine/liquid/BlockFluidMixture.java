package resonantinduction.machine.liquid;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;

/**
 * @author Calclavia
 * 
 */
public class BlockFluidMixture extends BlockFluidFinite implements ITileEntityProvider
{
	public BlockFluidMixture(int id, Fluid fluid)
	{
		super(id, fluid, Material.water);
		this.setTextureName("water_flow");
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileFluidMixture();
	}
}
