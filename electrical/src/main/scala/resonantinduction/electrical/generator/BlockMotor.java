package resonantinduction.electrical.generator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonant.lib.prefab.block.BlockRotatable;
import resonant.lib.render.block.BlockRenderingHandler;
import resonantinduction.core.Reference;
import universalelectricity.api.UniversalElectricity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMotor extends BlockRotatable
{
	public BlockMotor(int id)
	{
		super(id, UniversalElectricity.machine);
		setTextureName(Reference.PREFIX + "material_stone");
		rotationMask = Byte.parseByte("111111", 2);
	}

	@Override
	public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileMotor)
		{
			if (!world.isRemote)
			{
				int gear = ((TileMotor) tileEntity).toggleGearRatio();
				entityPlayer.addChatMessage("Generator set to " + (gear == 0 ? "low" : gear == 1 ? "medium" : "high") + " gear.");
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		return doRotateBlock(world, x, y, z, ForgeDirection.getOrientation(side));
	}

	@Override
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileMotor)
		{
			if (!world.isRemote)
			{
				((TileMotor) tileEntity).isInversed = !((TileMotor) tileEntity).isInversed;
				entityPlayer.addChatMessage("Generator now producing " + (((TileMotor) tileEntity).isInversed ? "mechanical" : "electrical") + " energy.");
			}

			return true;
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileMotor();
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
		return BlockRenderingHandler.ID;
	}
}
