package resonantinduction;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TileMultipart;

/**
 * @author Calclavia
 * 
 */
public class Utility
{
	public static TileMultipart getMultipartTile(IBlockAccess access, BlockCoord pos)
	{
		TileEntity te = access.getBlockTileEntity(pos.x, pos.y, pos.z);
		return te instanceof TileMultipart ? (TileMultipart) te : null;
	}

	public static boolean canPlaceWireOnSide(World w, int x, int y, int z, ForgeDirection side, boolean _default)
	{
		if (!w.blockExists(x, y, z))
			return _default;

		Block b = Block.blocksList[w.getBlockId(x, y, z)];
		if (b == null)
			return false;
		// Manual list of allowed blocks that wire can sit on.
		if (b == Block.glowStone || b == Block.pistonBase || b == Block.pistonStickyBase || b == Block.pistonMoving)
			return true;
		return b.isBlockSolidOnSide(w, x, y, z, side);
	}
}
