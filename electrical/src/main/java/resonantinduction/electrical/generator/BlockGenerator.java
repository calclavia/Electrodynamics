package resonantinduction.electrical.generator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.render.RIBlockRenderingHandler;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.prefab.block.BlockRotatable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGenerator extends BlockRotatable
{
	public BlockGenerator(int id)
	{
		super(id, UniversalElectricity.machine);
		setTextureName(Reference.PREFIX + "material_stone");
		rotationMask = Byte.parseByte("111111", 2);
	}

	@Override
	public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileGenerator)
		{
			if (!world.isRemote)
			{
				((TileGenerator) tileEntity).isInversed = !((TileGenerator) tileEntity).isInversed;
				entityPlayer.addChatMessage("Generator now producing " + (((TileGenerator) tileEntity).isInversed ? "mechanical" : "electrical") + " energy.");
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileGenerator)
		{
			if (!world.isRemote)
			{
				int gear = ((TileGenerator) tileEntity).toggleGearRatio();
				entityPlayer.addChatMessage("Generator set to " + (gear == 0 ? "low" : gear == 1 ? "medium" : "high") + " gear.");
			}

			return true;
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileGenerator();
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
