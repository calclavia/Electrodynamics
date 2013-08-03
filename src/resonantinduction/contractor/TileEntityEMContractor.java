package resonantinduction.contractor;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityEMContractor extends TileEntity
{
	public ForgeDirection facing = ForgeDirection.UP;
	
	@Override
	public void updateEntity()
	{
		
	}
	
	public void incrementFacing()
	{
		int newOrdinal = facing.ordinal() < 5 ? facing.ordinal()+1 : 0;
		facing = ForgeDirection.getOrientation(newOrdinal);
	}
}
