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

public class BlockWindTurbine extends BlockTurbine
{
	public BlockWindTurbine(int id)
	{
		super(id, Material.iron);
		setTextureName(Reference.PREFIX + "material_wood_surface");
		rotationMask = Byte.parseByte("111111", 2);
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		if (entityPlayer.getCurrentEquippedItem() == null)
		{
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

			if (tileEntity instanceof TileTurbine)
			{
				if (!world.isRemote && !((TileTurbine) tileEntity).getMultiBlock().isConstructed())
				{
					((TileTurbine) tileEntity).multiBlockRadius = Math.max(((TileTurbine) tileEntity).multiBlockRadius + 1, 1);
					entityPlayer.addChatMessage("Turbine radius: " + ((TileTurbine) tileEntity).multiBlockRadius);
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileTurbine)
		{
			if (!world.isRemote && !((TileTurbine) tileEntity).getMultiBlock().isConstructed())
			{
				((TileTurbine) tileEntity).multiBlockRadius = Math.max(((TileTurbine) tileEntity).multiBlockRadius - 1, 1);
				entityPlayer.addChatMessage("Turbine radius: " + ((TileTurbine) tileEntity).multiBlockRadius);
			}

			return true;
		}

		return false;
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
