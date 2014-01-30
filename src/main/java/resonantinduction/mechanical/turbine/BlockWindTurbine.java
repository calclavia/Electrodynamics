package resonantinduction.mechanical.turbine;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.Settings;
import resonantinduction.core.TabRI;
import resonantinduction.core.render.RIBlockRenderingHandler;
import resonantinduction.mechanical.network.IMechanical;
import calclavia.lib.prefab.turbine.BlockTurbine;
import calclavia.lib.prefab.turbine.TileTurbine;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockWindTurbine extends BlockTurbine
{
	public BlockWindTurbine()
	{
		this(Settings.getNextBlockID(), "windTurbine", Material.iron);
		setTextureName(Reference.PREFIX + "material_wood_surface");
		rotationMask = Byte.parseByte("111111", 2);
	}

	public BlockWindTurbine(int id, String name, Material material)
	{
		super(Settings.CONFIGURATION.getBlock(name, id).getInt(id), material);
		this.setUnlocalizedName(Reference.PREFIX + name);
		this.setCreativeTab(TabRI.CORE);
		this.setTextureName(Reference.PREFIX + name);
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
		return new TileWindTurbine();
	}
}
